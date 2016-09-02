package org.mdvsc.vcserver.server

import org.mdvsc.vcserver.util.FileUtils
import java.io.*
import java.util.*
import java.util.regex.Pattern

/**
 * @author haniklz
 * @since 16/3/31.
 */
class RepoPermissionConfig {

    companion object {
        val splitPattern = Pattern.compile(" +")
        val allPattern = Pattern.compile(".*")
        val indent = "    "
    }

    val groupUserMap = HashMap<String, List<String>>()
    val userGroupMap = HashMap<String, List<String>>()
    val repoPermissionList = ArrayList<RepoPermission>()

    private var lastModified: Long = 0
    private var file: File? = null

    private fun reload(file: File) {
        userGroupMap.clear(); repoPermissionList.clear()
        val groupList = ArrayList<String>()
        val userList = ArrayList<String>()
        val excludeGroupList = ArrayList<String>()
        val excludeUserList = ArrayList<String>()
        try {
            FileInputStream(file).use {
                val br = BufferedReader(InputStreamReader(it))
                var line = br.readLine()
                var repoPath: String? = null
                var repoBranch: String? = null
                var repoPermit: RepoPermission? = null
                while (line != null) {
                    val index = line.indexOf('=')
                    if (index > 0) {
                        var key = line.substring(0, index).trim()
                        val value = line.substring(index + 1).trim();
                        if (key.startsWith('@')) {
                            // 用户组
                            key = key.substring(1)
                            val splitList = value.split(splitPattern)
                            groupUserMap[key] = splitList
                            splitList.forEach {
                                val list = userGroupMap[it]
                                if (list == null) {
                                    userGroupMap[it] = arrayListOf(key)
                                } else if (!list.contains(key)) {
                                    (list as ArrayList).add(key)
                                }
                            }
                        } else if (repoPath != null) {
                            // 权限设置
                            val list = key.split(splitPattern)
                            val permit = if (list.size > 0) list[0] else ""
                            val branch = if (list.size > 1) list[1] else ""
                            if (repoPermit == null || branch != repoBranch) {
                                repoBranch = branch
                                repoPermit = try {
                                    RepoPermission(userGroupMap, repoPath, repoBranch)
                                } catch (ignored: Exception) {
                                    continue
                                }
                                repoPermissionList.add(repoPermit)
                            }
                            groupList.clear();userList.clear();excludeUserList.clear();excludeGroupList.clear()

                            value.split(splitPattern).forEach {
                                var excluded = false
                                line = it
                                if (line.startsWith('-')) {
                                    line = line.substring(1)
                                    excluded = true
                                }
                                if (line.startsWith('@')) {
                                    line = line.substring(1)
                                    if (excluded) excludeGroupList.add(line) else groupList.add(line)
                                } else {
                                    if (excluded) excludeUserList.add(line) else userList.add(line)
                                }
                            }
                            repoPermit.addRule(permit, groupList, userList, excludeGroupList, excludeGroupList)
                        }
                    } else {
                        line = line.trim()
                        if (line.startsWith("repo")) {
                            repoPermit = null
                            repoBranch = null
                            repoPath = line.substring(4).trim()
                        }
                    }
                    line = br.readLine()
                }
            }
        } catch (ignored: Exception) {
            ignored.printStackTrace()
        }
    }

    fun load(file: File, force: Boolean = false): RepoPermissionConfig {
        val lastModified = file.lastModified()
        if (force
                || this.file == null
                || this.lastModified != lastModified
                || this.file!!.absolutePath != file.absolutePath) {
            this.file = file
            this.lastModified = file.lastModified()
            reload(file)
        }
       return this
    }

    fun saveTo(file: File) {
        if (!file.isFile || (!file.exists() && FileUtils.safeMakeNewFile(file))) {
            val writeMap = { writer: Writer, map:Map<String,List<String>> -> map.forEach {
                writer.write("$indent${it.key} =")
                it.value.forEach {
                    writer.write(" $it")
                }
                writer.write("\r\n")
            }}
            file.writer(ServerHelper.defaultCharset).use {
                with (it) {
                    groupUserMap.filterValues { it.size > 0 }.forEach {
                        write("@${it.key}")
                        it.value.forEach { write(" $it") }
                        write("\r\n")
                    }
                    val groupMap = HashMap<String, ArrayList<String>>()
                    val userMap = HashMap<String, ArrayList<String>>()
                    var repoName = ""
                    repoPermissionList.forEach {
                        groupMap.clear(); userMap.clear()
                        it.permissionGroupMap.values.forEach { it.extract(groupMap, userMap) }
                        if (it.repoPattern.pattern() != repoName) {
                            repoName = it.repoPattern.pattern()
                            write("repo $repoName\r\n")
                        }
                        writeMap(this, groupMap)
                        writeMap(this, userMap)
                    }
                }
            }
        }
    }

    fun getRepoPermitList(repo: String): List<RepoPermission> {
        val path = if (repo.startsWith('/')) repo.substring(1) else repo
        val arrayList = ArrayList<RepoPermission>()
        repoPermissionList.filterTo(arrayList, { p -> p.isMatchRepo(path) })
        return arrayList
    }

    fun save() {
        if (file != null) {
            saveTo(file!!)
        }
    }
}

class UserPermissionGroup(val rule:PermissionRule) {

    val groupMap = HashMap<String, Boolean>()
    val additionalUserMap = HashMap<String, Boolean>()

    fun addUser(user: String, isExclude: Boolean = false) {
        additionalUserMap[user] = isExclude
    }

    fun addGroup(group: String, isExclude: Boolean = false) {
        groupMap[group] = isExclude
    }

    fun extract(groups:HashMap<String, ArrayList<String>>, users:HashMap<String, ArrayList<String>>) {
        val g = HashMap<String, Int>()
        val u = HashMap<String, Int>()
        val convert = {map:HashMap<String,Boolean>, outMap:HashMap<String,Int>->
            map.forEach {
                val key = if (it.value) "-${it.key}" else it.key
                outMap[key] = outMap[key]?.or(rule.code) ?: rule.code
            }
        }
        val add = {userToPermission:Map<String,Int>, permissionToUser:HashMap<String, ArrayList<String>> ->
            userToPermission.forEach {
                val key = intPermissionToString(it.value)
                var list = permissionToUser[key]
                if (list == null) {
                    list = ArrayList()
                    permissionToUser[key] = list
                }
                list.add(it.key)
            }
        }
        convert(groupMap, g)
        convert(additionalUserMap, u)
        add(g, groups)
        add(u, users)
    }

    fun success(groups: List<String>?, user: String?):Boolean {
        var success = groups?.any {
            val test = groupMap[it]
            test != null && !test
        }?:false
        if (user != null) {
            val test = additionalUserMap[user]
            success = success || (test != null && !test)
        }
        return success
    }

    private fun intPermissionToString(p:Int):String {
        val sb = StringBuilder()
        listOf(PermissionRule.C, PermissionRule.D, PermissionRule.R, PermissionRule.W, PermissionRule.F).forEach {
            if (p.and(it.code) == it.code) {
                sb.append(it.value)
            }
        }
        return sb.toString()
    }
}

class RepoPermission(val userGroupMap: Map<String, List<String>>, repoPath: String, branch: String) {

    val permissionGroupMap = mapOf(
        PermissionRule.R to UserPermissionGroup(PermissionRule.R)
        , PermissionRule.W to UserPermissionGroup(PermissionRule.W)
        , PermissionRule.D to UserPermissionGroup(PermissionRule.D)
        , PermissionRule.C to UserPermissionGroup(PermissionRule.C)
        , PermissionRule.F to UserPermissionGroup(PermissionRule.F)
    )

    val branchPattern = if (branch.isBlank()) RepoPermissionConfig.allPattern else Pattern.compile(branch)
    val repoPattern = if (repoPath.isBlank()) RepoPermissionConfig.allPattern else Pattern.compile(repoPath)

    fun isMatchRepo(repoPath: String) = repoPattern.matcher(repoPath).matches()
    fun isPermitted(rule: PermissionRule, branch: String, user: String) = branchPattern.matcher(branch).matches() && permissionGroupMap[rule]?.success(userGroupMap[user], user) ?: false
    fun canRead(branch: String, user: String) = isPermitted(PermissionRule.R, branch, user)
    fun canWrite(branch: String, user: String) = isPermitted(PermissionRule.W, branch, user)
    fun canForcePush(branch: String, user: String) = isPermitted(PermissionRule.F, branch, user)
    fun canDelete(branch: String, user: String) = isPermitted(PermissionRule.D, branch, user)
    fun canCreate(branch: String, user: String) = isPermitted(PermissionRule.C, branch, user)

    private fun add(rule: PermissionRule
                    , groups: Collection<String>?
                    , users: Collection<String>?
                    , excludeGroups: Collection<String>?
                    , excludeUsers: Collection<String>?) {
        val permissionGroup = permissionGroupMap[rule]
        if (permissionGroup != null) {
            groups?.forEach { permissionGroup.addGroup(it) }
            excludeGroups?.forEach { permissionGroup.addGroup(it, true) }
            users?.forEach { permissionGroup.addUser(it) }
            excludeUsers?.forEach { permissionGroup.addUser(it, true) }
        }
    }

    fun addRule(permit: String
                , groups: Collection<String>?
                , users: Collection<String>?
                , excludeGroups: Collection<String>? = null
                , excludeUsers: Collection<String>? = null) {
        var lastIsWrite = false
        permit.forEach {
            var isWrite = false
            when (it) {
                PermissionRule.C.value -> add(PermissionRule.C, groups, users, excludeGroups, excludeUsers)
                PermissionRule.R.value -> add(PermissionRule.R, groups, users, excludeGroups, excludeUsers)
                PermissionRule.D.value -> add(PermissionRule.D, groups, users, excludeGroups, excludeUsers)
                PermissionRule.W.value -> {
                    isWrite = true
                    add(PermissionRule.W, groups, users, excludeGroups, excludeUsers)
                }
                PermissionRule.F.value -> {
                    if (lastIsWrite) {
                        add(PermissionRule.F, groups, users, excludeGroups, excludeUsers)
                    }
                }
                else -> isWrite = false
            }
            lastIsWrite = isWrite
        }
    }

}

enum class PermissionRule(val value:Char, val code:Int) {
    C('C', 1), D('D', 2), R('R', 4), W('W', 8), F('+', 8 or 16);
}

