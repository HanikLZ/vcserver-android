package org.mdvsc.vcserver.gui.user.group

import android.os.Bundle
import android.support.v14.preference.MultiSelectListPreference
import android.support.v7.app.AlertDialog
import android.support.v7.preference.EditTextPreference
import android.support.v7.preference.Preference
import android.support.v7.preference.PreferenceCategory
import android.support.v7.preference.PreferenceGroup
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.mdvsc.vcserver.R
import org.mdvsc.vcserver.gui.GuiHelper
import org.mdvsc.vcserver.gui.common.BaseEditFragment
import org.mdvsc.vcserver.gui.user.RemoveUserEvent
import org.mdvsc.vcserver.gui.user.UserEditFragment
import org.mdvsc.vcserver.gui.user.UserItem
import org.mdvsc.vcserver.server.PathProvider
import org.mdvsc.vcserver.server.ServerHelper
import org.mdvsc.vcserver.server.model.UserInfo
import org.mdvsc.vcserver.util.FileUtils
import org.mdvsc.vcserver.util.JsonUtils
import java.io.File
import java.util.*

/**
 * @author haniklz
 * @since 16/4/7.
 */
class GroupEditFragment : BaseEditFragment(R.xml.group_preferences) {

    private lateinit var oldGroupId: String
    private lateinit var userListPreference:PreferenceCategory

    private var infoChanged = false
    private val groupItem by lazy {
        (arguments.getSerializable(GuiHelper.argumentGroupItem) as GroupItem? ?: GroupItem.empty()).apply {
            oldGroupId = this.id
        }
    }

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
        val userMap = HashMap<String, UserInfo>()
        val addUserPreference = {
            group:PreferenceGroup, key:String ->
            group.addPreference(Preference(activity).apply {
                val info = userMap[key]!!
                title = "$key(${info.name})"
                summary = info.description
                setOnPreferenceClickListener {
                    GuiHelper.startComponentActivity(context, UserEditFragment::class.java, arguments.apply {
                        putSerializable(GuiHelper.argumentUserItem, UserItem(key, info))
                    }, key)
                    true
                }
            })
        }
        val addUser = {
            dir: File, isInfo:Boolean ->
            if (dir.exists() && dir.isDirectory) {
                dir.listFiles().filter { it.isFile }.forEach {
                    var name = it.nameWithoutExtension
                    if (name.isEmpty()) name = it.name
                    if (isInfo) {
                        userMap[name] = ServerHelper.getUserInfo(it) ?: UserInfo()
                    } else if (userMap[name] == null) {
                        userMap[name] = UserInfo()
                    }
                }
            }
        }
        addUser(serverPathProvider.getPathFile(PathProvider.Folder.USER), true)
        addUser(serverPathProvider.getPathFile(PathProvider.Folder.SSH), false)

        (findPreference("id") as EditTextPreference).apply {
            if (groupItem.id.isNotEmpty()) {
                text = groupItem.id
                summary = groupItem.id
            }
            setOnPreferenceChangeListener { preference, any ->
                val newId = any.toString()
                if (serverPathProvider.getPathFile(PathProvider.Folder.GROUP, newId + ServerHelper.infoFileExtension).exists()) {
                    AlertDialog.Builder(context)
                            .setTitle(R.string.error)
                            .setMessage(R.string.rename_to_exist_group)
                            .setPositiveButton(android.R.string.ok, null)
                            .show()
                    false
                } else {
                    infoChanged = true
                    groupItem.id = newId
                    summary = newId
                    true
                }
            }
        }
        (findPreference("name") as EditTextPreference).apply {
            if (!groupItem.info.name.isNullOrEmpty()) {
                text = groupItem.info.name
                summary = groupItem.info.name
            }
            setOnPreferenceChangeListener { preference, any ->
                groupItem.info.name = any.toString()
                summary = groupItem.info.name
                infoChanged = true
                true
            }
        }
        (findPreference("description") as EditTextPreference).apply {
            if (!groupItem.info.description.isNullOrEmpty()) {
                text = groupItem.info.description
                summary = groupItem.info.description
            }
            setOnPreferenceChangeListener { preference, any ->
                groupItem.info.description = any.toString()
                summary = groupItem.info.description
                infoChanged = true
                true
            }
        }

        userListPreference = (findPreference("users") as PreferenceCategory).apply {
            groupItem.info.users?.filter { userMap[it] != null }?.forEach { addUserPreference(this, it) }
        }

        (findPreference("edit") as MultiSelectListPreference).apply {
            entries = userMap.keys.map {
                val info = userMap[it]
                if (info != null) "$it(${info.name})" else it
            }.toTypedArray()
            entryValues = userMap.keys.toTypedArray()
            values = groupItem.info.users?.toSet() ?: emptySet()
            setOnPreferenceChangeListener { preference, any ->
                groupItem.info.users = (any as Set<String>).toTypedArray()
                userListPreference.run {
                    removeAll()
                    groupItem.info.users?.filter { userMap[it] != null }?.forEach { addUserPreference(this, it) }
                    addPreference(this@apply)
                }
                infoChanged = true
                true
            }
        }
    }

    private fun saveChanges() {
        val groupId = groupItem.id
        if (infoChanged && !groupId.isNullOrBlank()) {
            FileUtils.makeSureNewFile(getGroupInfo(groupId)).writeText(JsonUtils.toJson(groupItem.info), ServerHelper.defaultCharset)
            if (oldGroupId != groupId) {
                if (oldGroupId.isNotEmpty()) {
                    getGroupInfo(groupId).delete()
                    GuiHelper.postEvent(RemoveGroupEvent(oldGroupId))
                }
                GuiHelper.postEvent(AddGroupEvent(groupItem))
            } else {
                GuiHelper.postEvent(ModifyGroupEvent(groupItem))
            }
            infoChanged = false
        }
    }

    override fun onComplete(): Boolean {
        saveChanges()
        return true
    }

    override fun onDelete(): Boolean {
        if (oldGroupId.isNotEmpty()) {
            getGroupInfo(oldGroupId).delete()
            GuiHelper.postEvent(RemoveGroupEvent(groupItem.id))
        }
        return true
    }

    private fun getGroupInfo(groupId: String) = serverPathProvider.getPathFile(PathProvider.Folder.GROUP, groupId + ServerHelper.infoFileExtension)

    @Subscribe(threadMode = ThreadMode.MAIN) fun event(event: RemoveUserEvent) {
        if (groupItem.info.users?.any { it == event.id } ?: false) {
            userListPreference.removePreference(userListPreference.findPreference(event.id))
            groupItem.info.users = groupItem.info.users?.filterNot { it == event.id }?.toTypedArray()
        }
    }
}

