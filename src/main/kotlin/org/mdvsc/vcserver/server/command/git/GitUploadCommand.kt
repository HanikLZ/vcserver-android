package org.mdvsc.vcserver.server.command.git

import org.eclipse.jgit.lib.Repository
import org.eclipse.jgit.storage.pack.PackConfig
import org.eclipse.jgit.transport.RefFilter
import org.eclipse.jgit.transport.UploadPack
import org.mdvsc.vcserver.server.PathProvider
import org.mdvsc.vcserver.server.RepoPermission

/**
 * @author haniklz
 * @since 16/3/26.
 */
class GitUploadCommand(pathProvider: PathProvider, repoPath: String?) : GitCommand(pathProvider, repoPath) {

    override fun onStart(repo: Repository, userName: String, permissions: List<RepoPermission>) {
        if (!repo.directory.exists()) {
            return
        }
        UploadPack(repo).apply {
            setPackConfig(PackConfig().apply {
                isDeltaCompress = false
                threads = 1
            })
            refFilter = RefFilter { refs -> refs?.filterValues { ref -> permissions.any { it.canRead(ref.target.name, userName) } } }
            timeout = timeOutSecond
        }.upload(commandInputStream, commandOutputStream, commandErrorStream)
    }
}
