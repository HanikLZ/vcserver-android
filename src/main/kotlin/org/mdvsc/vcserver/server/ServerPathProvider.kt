package org.mdvsc.vcserver.server

import java.io.File

/**
 * @author haniklz
 * @since 16/4/6.
 */
class ServerPathProvider(var workPathFile:File): PathProvider {

    constructor(workPath:String = "/"):this(File(workPath))

    override fun getPathFile(folder: PathProvider.Folder?, path: String?): File {
        var file = workPathFile
        if (!file.exists()) {
            file.mkdirs()
        }
        file = if (folder == null) file else File(file, folder.path)
        file = if (path == null) file else File(file, path)
        return file
    }
}

