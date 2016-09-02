package org.mdvsc.vcserver.gui.user

import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.support.v7.preference.EditTextPreference
import android.support.v7.preference.SwitchPreferenceCompat
import org.mdvsc.vcserver.R
import org.mdvsc.vcserver.gui.GuiHelper
import org.mdvsc.vcserver.gui.common.BaseEditFragment
import org.mdvsc.vcserver.server.PathProvider
import org.mdvsc.vcserver.server.ServerHelper
import org.mdvsc.vcserver.util.FileUtils
import org.mdvsc.vcserver.util.JsonUtils
import java.io.File

/**
 * @author haniklz
 * @since 16/4/9.
 */
class UserEditFragment : BaseEditFragment(R.xml.user_preferences) {

    val userItem by lazy {
        (arguments.getSerializable(GuiHelper.argumentUserItem) as UserItem? ?: UserItem.empty).apply {
            oldUserId = this.id
        }
    }
    lateinit var oldUserId:String
    var infoChanged = false
    var changedKey:String? = null

    override fun onCreatePreferences(p0: Bundle?, p1: String?) {
        super.onCreatePreferences(p0, p1)
        findPreference("id").apply {
            if (userItem.id.isNotEmpty()) {
                (this as EditTextPreference).text = userItem.id
                summary = userItem.id
            }
            setOnPreferenceChangeListener { preference, any ->
                val newId = any.toString()
                if (serverPathProvider.getPathFile(PathProvider.Folder.USER, newId + ServerHelper.infoFileExtension).exists()
                        || serverPathProvider.getPathFile(PathProvider.Folder.SSH, newId + ServerHelper.keyFileExtension).exists()) {
                    AlertDialog.Builder(context)
                            .setTitle(R.string.error)
                            .setMessage(R.string.rename_to_exist_user)
                            .setPositiveButton(android.R.string.ok, null)
                            .show()
                    false
                } else {
                    infoChanged = true
                    userItem.id = newId
                    summary = newId
                    true
                }
            }
        }
        findPreference("name").apply {
            if (!userItem.info.name.isNullOrEmpty()) {
                (this as EditTextPreference).text = userItem.info.name
                summary = userItem.info.name
            }
            setOnPreferenceChangeListener { preference, any ->
                userItem.info.name = any.toString()
                summary = userItem.info.name
                infoChanged = true
                true
            }
        }
        findPreference("description").apply {
            if (!userItem.info.description.isNullOrEmpty()) {
                (this as EditTextPreference).text = userItem.info.description
                summary = userItem.info.description
            }
            setOnPreferenceChangeListener { preference, any ->
                userItem.info.description = any.toString()
                summary = userItem.info.description
                infoChanged = true
                true
            }
        }
        findPreference("email").apply {
            if (!userItem.info.description.isNullOrEmpty()) {
                (this as EditTextPreference).text = userItem.info.email
                summary = userItem.info.email
            }
            setOnPreferenceChangeListener { preference, any ->
                userItem.info.email = any.toString()
                summary = userItem.info.email
                infoChanged = true
                true
            }
        }
        findPreference("isAdmin").apply {
            (this as SwitchPreferenceCompat).isChecked = userItem.info.isAdmin
            setOnPreferenceChangeListener { preference, any ->
                userItem.info.isAdmin = any as Boolean
                infoChanged = true
                true
            }
        }
        findPreference("enablePassword").apply {
            (this as SwitchPreferenceCompat).isChecked = userItem.info.enablePasswordAuthentication
            setOnPreferenceChangeListener { preference, any ->
                userItem.info.enablePasswordAuthentication = any as Boolean
                infoChanged = true
                true
            }
        }
        findPreference("key").apply {
            if (userItem.id.isNotEmpty()) {
                val file = getUserKeyFile(userItem.id, false)
                if (file.exists() && file.isFile && file.canRead()) {
                    val key = file.readText(ServerHelper.defaultCharset)
                    (this as EditTextPreference).text = key
                    if (key.isNotEmpty()) summary = key
                }
            }
            setOnPreferenceChangeListener { preference, any ->
                changedKey = any.toString()
                summary = changedKey
                true
            }
        }
        findPreference("password").apply {
            setOnPreferenceChangeListener { preference, any ->
                userItem.info.password = ServerHelper.stringDigest(any.toString())
                infoChanged = true
                true
            }
        }
    }

    override fun onComplete(): Boolean {
        val userId = userItem.id
        if (!userId.isNullOrBlank()) {
            if (infoChanged) {
                getUserInfoFile(userId).writeText(JsonUtils.toJson(userItem.info), ServerHelper.defaultCharset)
                if (oldUserId != userId) {
                    if (oldUserId.isNotEmpty()) {
                        getUserInfoFile(oldUserId).delete()
                        GuiHelper.postEvent(RemoveUserEvent(oldUserId))
                    }
                    GuiHelper.postEvent(AddUserEvent(userItem))
                } else {
                    GuiHelper.postEvent(ModifyUserEvent(userItem))
                }
            }
            if (changedKey != null) {
                getUserKeyFile(userId).writeText(changedKey!!, ServerHelper.defaultCharset)
                if (oldUserId != userId) { getUserKeyFile(oldUserId).delete() }
            } else if (oldUserId != userId) {
                val oldKeyFile = getUserKeyFile(oldUserId, false)
                if (oldKeyFile.exists()) {
                    val newKeyFile = getUserKeyFile(userId, false)
                    if (newKeyFile.exists()) {
                        newKeyFile.delete()
                    }
                    oldKeyFile.renameTo(newKeyFile)
                }
            }
        }
        return true
    }

    override fun onDelete(): Boolean {
        if (oldUserId.isNotEmpty()) {
            getUserKeyFile(oldUserId).delete()
            getUserInfoFile(oldUserId).delete()
            GuiHelper.postEvent(RemoveUserEvent(oldUserId))
        }
        return true
    }

    private fun getUserInfoFile(userId:String, createIfNotExist:Boolean = true):File {
        val file = serverPathProvider.getPathFile(PathProvider.Folder.USER, userId + ServerHelper.infoFileExtension)
        if (createIfNotExist) {
            return FileUtils.makeSureNewFile(file)
        } else {
            return file
        }
    }

    private fun getUserKeyFile(userId:String, createIfNotExist: Boolean = true):File {
        val file = serverPathProvider.getPathFile(PathProvider.Folder.SSH, userId + ServerHelper.keyFileExtension)
        if (createIfNotExist) {
            return FileUtils.makeSureNewFile(file)
        } else {
            return file
        }
    }
}

