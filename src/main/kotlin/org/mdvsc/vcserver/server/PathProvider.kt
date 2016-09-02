package org.mdvsc.vcserver.server

import java.io.File

/**
 * @author haniklz
 * @since 16/3/28.
 */
interface PathProvider {
    enum class Folder(val path: String) {
        KEY("key"),
        FILE("file"),
        SSH("ssh"),
        GROUP("group"),
        USER("user"),
        REPO("repo")
    }
    fun getPathFile(folder: Folder? = null, path: String? = null): File
}

