package org.mdvsc.vcserver.gui.repo

import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.support.v7.preference.EditTextPreference
import android.support.v7.preference.Preference
import android.support.v7.preference.PreferenceCategory
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.mdvsc.vcserver.MyApplication
import org.mdvsc.vcserver.R
import org.mdvsc.vcserver.gui.GuiHelper
import org.mdvsc.vcserver.gui.common.BaseEditFragment
import org.mdvsc.vcserver.server.PathProvider
import org.mdvsc.vcserver.server.ServerHelper
import org.mdvsc.vcserver.server.model.PermissionInfo
import org.mdvsc.vcserver.util.FileUtils
import org.mdvsc.vcserver.util.FileUtils.clear
import org.mdvsc.vcserver.util.FileUtils.moveTo
import org.mdvsc.vcserver.util.GitUtils
import org.mdvsc.vcserver.util.JsonUtils
import java.io.File
import java.util.*

/**
 * @author haniklz
 * @since 16/4/7.
 */
class RepoEditFragment : BaseEditFragment(R.xml.repo_preferences) {

    private lateinit var oldRepoPath: String
    private var infoChanged = false
    private var permissionCount = 0
    private val repoItem by lazy {
        (arguments.getSerializable(GuiHelper.argumentRepoItem) as RepoItem? ?: RepoItem.empty()).apply {
            oldRepoPath = getGitRepoPath(this.path)
        }
    }
    private lateinit var permissionListPreference: PreferenceCategory
    private val permissionMap = HashMap<String, PermissionInfo>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        GuiHelper.registerEventBus(this)
    }

    override fun onDestroy() {
        GuiHelper.unregisterEventBus(this)
        super.onDestroy()
    }

    override fun onCreatePreferences(p0: Bundle?, p1: String?) {
        super.onCreatePreferences(p0, p1)
        findPreference("path").apply {
            if (repoItem.path.isNotEmpty()) {
                (this as EditTextPreference).text = oldRepoPath
                summary = oldRepoPath
            }
            setOnPreferenceChangeListener { preference, any ->
                val path = any.toString()
                if (serverPathProvider.getPathFile(PathProvider.Folder.REPO, path).exists()) {
                    AlertDialog.Builder(context)
                            .setTitle(R.string.error)
                            .setMessage(R.string.rename_to_exist_repo)
                            .setPositiveButton(android.R.string.ok, null)
                            .show()
                    false
                } else {
                    infoChanged = true
                    repoItem.path = getGitRepoPath(path)
                    summary = repoItem.path
                    true
                }
            }
        }
        findPreference("name").apply {
            if (!repoItem.info.name.isNullOrEmpty()) {
                (this as EditTextPreference).text = repoItem.info.name
                summary = repoItem.info.name
            }
            setOnPreferenceChangeListener { preference, any ->
                repoItem.info.name = any.toString()
                summary = repoItem.info.name
                infoChanged = true
                true
            }
        }
        findPreference("description").apply {
            if (!repoItem.info.description.isNullOrEmpty()) {
                (this as EditTextPreference).text = repoItem.info.description
                summary = repoItem.info.description
            }
            setOnPreferenceChangeListener { preference, any ->
                repoItem.info.description = any.toString()
                summary = repoItem.info.description
                infoChanged = true
                true
            }
        }
        findPreference("addPermission").apply {
            setOnPreferenceClickListener {
                GuiHelper.startComponentActivity(context
                        , PermissionEditFragment::class.java
                        , arguments.apply {
                    putString(GuiHelper.argumentPermissionId, repoItem.path)
                    remove(GuiHelper.argumentPermissionInfo)
                }, getString(R.string.new_permission))
                true
            }
        }
        permissionListPreference = (findPreference("permissions") as PreferenceCategory).apply {
            repoItem.info.permissions?.forEach { addPermissionPreference(this, it) }
        }
    }

    fun getGitRepoPath(path: String) = if (path.isNotEmpty() && !path.endsWith(ServerHelper.gitRepoExtension)) {
        path + ServerHelper.gitRepoExtension
    } else path

    override fun onDelete(): Boolean {
        if (oldRepoPath.isNotBlank() && serverPathProvider.getPathFile(PathProvider.Folder.REPO, oldRepoPath).clear()) {
            GuiHelper.postEvent(RemoveRepoEvent(oldRepoPath))
        }
        return true
    }

    override fun onComplete(): Boolean {
        if (infoChanged && repoItem.path.isNotBlank()) {
            var repoPathFile = serverPathProvider.getPathFile(PathProvider.Folder.REPO, repoItem.path)
            GuiHelper.postEvent(if (oldRepoPath != repoItem.path) {
                if (oldRepoPath.isNullOrBlank()) {
                    val preferences = MyApplication.applicationComponent.appPreferences()
                    GitUtils.createRepository(repoPathFile
                            , preferences.getRepoInitName() ?: getString(R.string.app_name)
                            , preferences.getRepoInitEmail() ?: getString(R.string.app_email)
                            , preferences.getRepoInitCommit() ?: getString(R.string.app_init)
                            , ServerHelper.getDefaultReadmeFile(serverPathProvider)
                            , ServerHelper.getDefaultIgnoreFile(serverPathProvider))
                } else {
                    GuiHelper.postEvent(RemoveRepoEvent(oldRepoPath))
                    if (!serverPathProvider.getPathFile(PathProvider.Folder.REPO, oldRepoPath).moveTo(repoPathFile)) {
                        // 还原
                        repoPathFile.clear()
                        repoItem.path = oldRepoPath
                        repoPathFile = serverPathProvider.getPathFile(PathProvider.Folder.REPO, repoItem.path)
                    }
                }
                AddRepoEvent(repoItem)
            } else ModifyRepoEvent(repoItem))
            FileUtils.makeSureNewFile(File(repoPathFile, ServerHelper.repoInfoFileName))
                    .writeText(JsonUtils.toJson(repoItem.info.apply { permissions = permissionMap.values.toTypedArray() }), ServerHelper.defaultCharset)
        }
        return true
    }

    fun permissionId() = "${repoItem.path}:${permissionCount++}"

    @Subscribe(threadMode = ThreadMode.MAIN) fun event(event: ModifyPermissionEvent) {
        permissionListPreference.findPreference(event.id)?.apply {
            title = event.info.branch
            summary = event.info.rule
            permissionMap[key] = event.info
            infoChanged = true
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN) fun event(event: RemovePermissionEvent) {
        permissionListPreference.findPreference(event.id)?.apply {
            permissionListPreference.removePreference(this)
            permissionMap.remove(event.id)
            infoChanged = true
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN) fun event(event: AddPermissionEvent) {
        if (event.id == repoItem.path) {
            addPermissionPreference(permissionListPreference, event.info)
            infoChanged = true
        }
    }

    fun addPermissionPreference(preference: PreferenceCategory, info: PermissionInfo) {
        preference.addPreference(Preference(activity).apply {
            key = permissionId()
            title = info.branch
            summary = info.rule
            permissionMap[key] = info
            setOnPreferenceClickListener {
                GuiHelper.startComponentActivity(context, PermissionEditFragment::class.java, arguments.apply {
                    putString(GuiHelper.argumentPermissionId, key)
                    putSerializable(GuiHelper.argumentPermissionInfo, permissionMap[key])
                }, getString(R.string.edit_permission))
                true
            }
        })
    }
}

