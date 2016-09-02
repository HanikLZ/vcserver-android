package org.mdvsc.vcserver.server

import android.content.Context
import java.io.File

/**
 * @author haniklz
 * @since 16/3/25.
 */
class ServerPreference(val context: Context): PathProvider {

    companion object {
        private val preferenceName = "server"
        private val keyWorkPath = "workPath"
        private val keySshPort = "sshPort"
        private val defaultFolderName = "vcserver"
        private val keyStartServerOnCreate = "startServerOnCreate"
    }

    private val preferences by lazy {
        context.getSharedPreferences(preferenceName, Context.MODE_PRIVATE)
    }

    private val serverPathProvider = ServerPathProvider()

    val defaultWorkPathFile by lazy {
        File(context.getExternalFilesDir(null) ?: context.filesDir, defaultFolderName)
    }

    var startServerOnCreate = false
        get() = preferences.getBoolean(keyStartServerOnCreate, field)
        set(value) {
            preferences.edit().putBoolean(keyStartServerOnCreate, value).apply()
            field = value
        }

    var sshPort = 0
        get() = preferences.getInt(keySshPort, field)
        set(value) {
            preferences.edit().putInt(keySshPort, value).apply()
            field = value
        }

    fun getWorkPathFile(): File {
        var path = preferences.getString(keyWorkPath, null)
        var file: File
        if (!path.isNullOrBlank()) {
            file = File(path)
            if (if (!file.exists()) file.mkdirs() else file.isDirectory) {
                return file
            }
        }
        file = defaultWorkPathFile
        if (!file.exists()) {
            file.mkdirs()
        }
        return file
    }

    fun setWorkPath(path: String?) {
        preferences.edit().putString(keyWorkPath, if (path.isNullOrBlank()) defaultWorkPathFile.absolutePath else path).apply()
    }

    override fun getPathFile(folder: PathProvider.Folder?, path: String?) = serverPathProvider.apply { workPathFile = getWorkPathFile() }.getPathFile(folder, path)
}
