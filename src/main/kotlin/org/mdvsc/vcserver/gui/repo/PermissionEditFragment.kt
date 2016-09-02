package org.mdvsc.vcserver.gui.repo

import android.os.Bundle
import android.support.v14.preference.MultiSelectListPreference
import android.support.v14.preference.SwitchPreference
import android.support.v7.preference.EditTextPreference
import org.mdvsc.vcserver.R
import org.mdvsc.vcserver.gui.GuiHelper
import org.mdvsc.vcserver.gui.common.BaseEditFragment
import org.mdvsc.vcserver.server.PathProvider
import org.mdvsc.vcserver.server.PermissionRule
import org.mdvsc.vcserver.server.ServerHelper
import org.mdvsc.vcserver.server.model.GroupInfo
import org.mdvsc.vcserver.server.model.PermissionInfo
import org.mdvsc.vcserver.server.model.UserInfo
import java.io.File
import java.util.*

/**
 * @author haniklz
 * @since 16/4/7.
 */
class PermissionEditFragment : BaseEditFragment(R.xml.permission_preferences) {

    val permissionId by lazy { arguments.getString(GuiHelper.argumentPermissionId) ?: "" }
    val permissionInfo by lazy { arguments.getSerializable(GuiHelper.argumentPermissionInfo) as PermissionInfo? ?: PermissionInfo() }
    val permissionRuleMap by lazy {
        val map = HashMap<PermissionRule, Boolean>()
        val addPair = {rule:PermissionRule ->
            map[rule] = permissionInfo.rule?.contains(rule.value, true) ?: false
        }
        addPair(PermissionRule.R)
        addPair(PermissionRule.W)
        addPair(PermissionRule.C)
        addPair(PermissionRule.D)
        addPair(PermissionRule.F)
        map
    }
    var infoChanged = false

    override fun onCreatePreferences(p0: Bundle?, p1: String?) {
        super.onCreatePreferences(p0, p1)
        val userMap = HashMap<String, UserInfo>()
        val groupMap = HashMap<String, GroupInfo>()

        val addUser = { dir: File, isInfo:Boolean ->
            if (dir.exists() && dir.isDirectory) {
                dir.listFiles().filter { it.isFile }.forEach {
                    var name = it.nameWithoutExtension
                    if (name.isEmpty()) name = it.name
                    userMap[name] = (if (isInfo) ServerHelper.getUserInfo(it) else null) ?: UserInfo()
                }
            }
        }

        val addGroup = { dir:File ->
            if (dir.exists() && dir.isDirectory) {
                dir.list().filter { it.endsWith(ServerHelper.infoFileExtension) }.map { File(dir, it) }.forEach {
                    groupMap[it.nameWithoutExtension] = ServerHelper.getInfo(it, GroupInfo::class.java) ?: GroupInfo()
                }
            }
        }

        val setRulePreference = { p:SwitchPreference, rule:PermissionRule -> p.apply {
                isChecked = permissionInfo.rule?.indexOf(rule.value)?:-1 >= 0
                setOnPreferenceChangeListener { preference, any ->
                    permissionRuleMap[rule] = any as Boolean
                    infoChanged = true
                    true
                }
            }
        }

        val userSummary = { d:Array<String>? -> if (d?.size?:0 > 0) { d!!.size.toString() } else "" }

        addUser(serverPathProvider.getPathFile(PathProvider.Folder.USER), true)
        addUser(serverPathProvider.getPathFile(PathProvider.Folder.SSH), false)
        addGroup(serverPathProvider.getPathFile(PathProvider.Folder.GROUP))

        (findPreference("branch") as EditTextPreference).apply {
            text = permissionInfo.branch
            if (!permissionInfo.branch.isNullOrEmpty()) { summary = permissionInfo.branch }
            setOnPreferenceChangeListener {preference, any ->
                permissionInfo.branch = any?.toString()?:permissionInfo.branch
                summary = permissionInfo.branch
                infoChanged = true
                true
            }
        }
        (findPreference("readable") as SwitchPreference).apply { setRulePreference(this, PermissionRule.R) }
        (findPreference("writable") as SwitchPreference).apply { setRulePreference(this, PermissionRule.W) }
        (findPreference("deletable") as SwitchPreference).apply { setRulePreference(this, PermissionRule.D) }
        (findPreference("creatable") as SwitchPreference).apply { setRulePreference(this, PermissionRule.C) }
        (findPreference("pushable") as SwitchPreference).apply { setRulePreference(this, PermissionRule.F) }

        (findPreference("users") as MultiSelectListPreference).apply {
            entries = userMap.keys.toTypedArray()
            entryValues = entries
            values = permissionInfo.users?.filter { userMap[it] != null }?.toSet() ?: emptySet()
            if (permissionInfo.users?.size?:0 > 0) summary = userSummary(permissionInfo.users)
            setOnPreferenceChangeListener { preference, any ->
                permissionInfo.users = (any as Set<String>).toTypedArray()
                summary = userSummary(permissionInfo.users)
                infoChanged = true
                true
            }
        }
        (findPreference("groups") as MultiSelectListPreference).apply {
            entries = groupMap.keys.toTypedArray()
            entryValues = entries
            values = permissionInfo.groups?.filter { groupMap[it] != null }?.toSet() ?: emptySet()
            if (permissionInfo.groups?.size?:0 > 0) summary = userSummary(permissionInfo.groups)
            setOnPreferenceChangeListener { preference, any ->
                permissionInfo.groups = (any as Set<String>).toTypedArray()
                summary = userSummary(permissionInfo.groups)
                infoChanged = true
                true
            }
        }
        (findPreference("excludeUsers") as MultiSelectListPreference).apply {
            entries = userMap.keys.toTypedArray()
            entryValues = entries
            values = permissionInfo.excludeUsers?.filter { userMap[it] != null }?.toSet() ?: emptySet()
            if (permissionInfo.excludeUsers?.size?:0 > 0) summary = userSummary(permissionInfo.excludeUsers)
            setOnPreferenceChangeListener { preference, any ->
                permissionInfo.excludeUsers = (any as Set<String>).toTypedArray()
                summary = userSummary(permissionInfo.excludeUsers)
                infoChanged = true
                true
            }
        }
    }

    override fun onComplete(): Boolean {
        if (infoChanged) {
            permissionInfo.rule = StringBuilder().apply {
                if (permissionRuleMap[PermissionRule.F] ?: false) {
                    if (permissionRuleMap[PermissionRule.W] ?: false) {
                        permissionRuleMap.remove(PermissionRule.W)
                        append(PermissionRule.W.value).append(PermissionRule.F.value)
                    }
                    permissionRuleMap.remove(PermissionRule.F)
                }
                permissionRuleMap.forEach { if (it.value) append(it.key.value) }
            }.toString()
            if (permissionInfo.branch.isNullOrBlank()) {
                permissionInfo.branch = ".*"
            }
            GuiHelper.postEvent(if (permissionId.lastIndexOf(':') > 0) ModifyPermissionEvent(permissionId, permissionInfo) else AddPermissionEvent(permissionId, permissionInfo))
        }
        return true
    }

    override fun onDelete(): Boolean {
        if (permissionId.isNotEmpty()) {
            GuiHelper.postEvent(RemovePermissionEvent(permissionId))
        }
        return true
    }

}

