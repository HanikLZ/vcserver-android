package org.mdvsc.vcserver.server.command.git

import org.apache.sshd.server.Environment
import org.eclipse.jgit.api.Git
import org.eclipse.jgit.lib.Repository
import org.mdvsc.vcserver.server.PathProvider
import org.mdvsc.vcserver.server.RepoPermission
import org.mdvsc.vcserver.server.RepoPermissionConfig
import org.mdvsc.vcserver.server.ServerHelper
import org.mdvsc.vcserver.server.command.BaseCommand
import org.mdvsc.vcserver.server.model.GroupInfo
import org.mdvsc.vcserver.server.model.RepoInfo
import org.mdvsc.vcserver.util.GitUtils
import org.mdvsc.vcserver.util.JsonUtils
import java.io.File
import java.util.*

/**
 * @author haniklz
 * @since 16/3/26.
 */
abstract class GitCommand(pathProvider: PathProvider, val repoPath: String?) : BaseCommand(pathProvider) {

    companion object {
        val defaultRepoPermissionConf = RepoPermissionConfig()
    }

    var timeOutSecond = 30

    override fun onStart(env: Environment) {
        if (repoPath.isNullOrBlank()) {
            exit(0)
        } else {
            var path = repoPath!!
            var permissionList: List<RepoPermission> = emptyList()
            if (path.first() == '\'') {
                path = path.substring(1)
            }
            if (path.last() == '\'') {
                path = path.substring(0, path.length - 1)
            }
            // 读取项目配置信息
            var file = File(pathProvider.getPathFile(PathProvider.Folder.REPO, path), ServerHelper.repoInfoFileName)
            if (file.exists() && file.isFile) {
                JsonUtils.fromJson(file.readText(ServerHelper.defaultCharset), RepoInfo::class.java)?.permissions?.run {
                    file = pathProvider.getPathFile(PathProvider.Folder.GROUP)
                    val userGroupMap = HashMap<String, List<String>>()
                    val loadedGroupMap = HashMap<String, Boolean>()
                    permissionList = map {
                        it.groups?.filterNot { loadedGroupMap[it] ?: false }?.forEach {
                            val groupName = it
                            val groupInfoFile = File(file, groupName + ServerHelper.infoFileExtension)
                            if (groupInfoFile.exists() && groupInfoFile.isFile && groupInfoFile.canRead()) {
                                val groupInfo = JsonUtils.fromJson(groupInfoFile.readText(ServerHelper.defaultCharset), GroupInfo::class.java)
                                groupInfo?.users?.forEach {
                                    val list = userGroupMap[it]
                                    if (list == null) {
                                        userGroupMap[it] = arrayListOf(groupName)
                                    } else if (!list.contains(groupName)) {
                                        (list as ArrayList).add(groupName)
                                    }
                                }
                            }
                        }
                        val p = RepoPermission(userGroupMap, path, it.branch ?: ".*")
                        p.addRule(it.rule ?: "", it.groups?.map { it }, it.users?.map { it }, it.excludeUsers?.map { it })
                        p
                    }
                }
            }
            val runtimePermissions = ArrayList<RepoPermission>().apply {
                addAll(permissionList)
                addAll(ServerHelper.loadDefaultRepoPermissionConf(pathProvider, defaultRepoPermissionConf).getRepoPermitList(path))
            }

            val git = try { Git.open(pathProvider.getPathFile(PathProvider.Folder.REPO, path)) } catch (ignored: Exception) { null }
            val user = env.env[Environment.ENV_USER] ?: ""
            if (git != null) {
                onStart(git.repository, user, runtimePermissions)
                git.close()
            } else {
                onStart(GitUtils.buildRepository(pathProvider, path), user, runtimePermissions)
            }
            exit(0)
        }
    }

    abstract protected fun onStart(repo: Repository, userName: String, permissions: List<RepoPermission>)

}

