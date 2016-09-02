package org.mdvsc.vcserver.server

import org.mdvsc.vcserver.server.model.UserInfo
import org.mdvsc.vcserver.util.FileUtils
import org.mdvsc.vcserver.util.JsonUtils
import java.io.File
import java.security.MessageDigest

/**
 *
 * @author  haniklz
 * @since   16/4/2
 * @version 1.0.0
 */
object ServerHelper {

    val adminUserName = ".admin"
    val infoFileExtension = ".json"
    val keyFileExtension = ".pub"
    val gitRepoExtension = ".git"
    val repoInfoFileName = "info.json"
    val permissionConfigFileName = "permission.conf"
    val defaultIgnoreFileName = ".gitignore"
    val defaultReadmeFileName = "README.md"
    val defaultCharset = Charsets.UTF_8
    val defaultRepoConfigContent = "repo .*\nCDRW+=$adminUserName"

    private val messageDigest = MessageDigest.getInstance("MD5")

    fun <T> getInfo(file: File, clazz:Class<T>) = if (file.exists() && file.isFile && file.canRead()) JsonUtils.fromJson(file.readText(defaultCharset), clazz) else null

    fun writeUserInfo(file:File, info: Any) {
        val content = JsonUtils.toJson(info)
        if (content.isEmpty()) {
            if (file.exists()) file.delete()
        } else if (file.exists() || FileUtils.safeMakeNewFile(file)) {
            file.writeText(content, defaultCharset)
        }
    }

    fun getDefaultReadmeFile(pathProvider: PathProvider) = pathProvider.getPathFile(PathProvider.Folder.FILE, defaultReadmeFileName)
    fun getDefaultIgnoreFile(pathProvider: PathProvider) = pathProvider.getPathFile(PathProvider.Folder.FILE, defaultIgnoreFileName)

    fun getUserInfo(pathProvider: PathProvider, username:String) = getUserInfo(pathProvider.getPathFile(PathProvider.Folder.USER, username + infoFileExtension))
    fun getUserInfo(file: File) = getInfo(file, UserInfo::class.java)
    fun writeUserInfo(pathProvider: PathProvider, username: String, info: UserInfo) = writeUserInfo(pathProvider.getPathFile(PathProvider.Folder.USER, username), info)

    fun loadDefaultRepoPermissionConf(pathProvider: PathProvider, repoPermissionConf: RepoPermissionConfig) = repoPermissionConf.load(pathProvider.getPathFile(PathProvider.Folder.REPO, permissionConfigFileName))

    fun stringDigest(str:String?) = if (str != null) {
        val sb = StringBuilder()
        with(messageDigest) {
            reset()
            update(str.toByteArray(defaultCharset))
            digest().forEach {
                sb.append(Integer.toHexString(0xFF.and(it.toInt())))
            }
            reset()
        }
        sb.toString()
    } else null
}

