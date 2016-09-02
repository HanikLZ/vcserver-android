package org.mdvsc.vcserver.server.ssh

import org.apache.sshd.common.Session
import org.apache.sshd.common.file.FileSystemFactory
import org.apache.sshd.common.file.FileSystemView
import org.apache.sshd.common.file.nativefs.NativeFileSystemView
import org.mdvsc.vcserver.server.PathProvider

/**
 * @author haniklz
 * @since 16/4/5.
 */
class ServerFileSystemFactory(val pathProvider: PathProvider): FileSystemFactory {

    override fun createFileSystemView(session: Session?): FileSystemView? {
        return NativeFileSystemView(session?.username
                , mutableMapOf(Pair("/", pathProvider.getPathFile().absolutePath))
                , "/")
    }
}

