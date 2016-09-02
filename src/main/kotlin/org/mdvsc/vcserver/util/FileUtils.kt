package org.mdvsc.vcserver.util

import java.io.File

/**
 * @author haniklz
 * @since 16/4/1.
 */
object FileUtils {

    fun makeSureNewFile(file: File):File {
        if (file.exists()) {
            file.delete()
        } else {
            file.parentFile?.mkdirs()
        }
        file.createNewFile()
        return file
    }

    fun safeMakeNewFile(file:File):Boolean {
        if (!file.exists()) {
            file.parentFile?.mkdirs()
            return file.createNewFile();
        }
        return false;
    }

    fun File.moveTo(file:File):Boolean {
        val parentFile = file.parentFile
        if (parentFile != null && !parentFile.exists()) {
            parentFile.mkdirs()
        }
        return renameTo(file)
    }
/*

    fun File.moveTo(file:File) = try {
            ProcessBuilder("mv", absolutePath, file.absolutePath).start().run {
                waitFor()
                destroy()
                true
            }
        } catch (e:Exception) {
            false
        }
*/

    fun File.clear():Boolean {
        val result = deleteRecursively()
        if (result) {
            var file = parentFile
            while (file?.delete() ?: false) {
                file = file.parentFile
            }
        }
        return result
    }
}

