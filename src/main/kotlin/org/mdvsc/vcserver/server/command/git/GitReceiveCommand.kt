package org.mdvsc.vcserver.server.command.git

import org.eclipse.jgit.lib.Repository
import org.eclipse.jgit.transport.PreReceiveHook
import org.eclipse.jgit.transport.ReceiveCommand
import org.eclipse.jgit.transport.ReceivePack
import org.mdvsc.vcserver.server.PathProvider
import org.mdvsc.vcserver.server.RepoPermission

/**
 * @author haniklz
 * @since 16/3/26.
 */
class GitReceiveCommand(pathProvider: PathProvider, repoPath: String?) : GitCommand(pathProvider, repoPath) {

    override fun onStart(repo: Repository, userName: String, permissions: List<RepoPermission>) {
        if (!repo.directory.exists()) {
            if (permissions.any {it.canCreate("", userName)}) {
                repo.run { create(true) }
            } else {
                return
            }
        }
        ReceivePack(repo).apply {
            timeout = timeOutSecond
            preReceiveHook = PreReceiveHook { rp, commands ->
                    commands.forEach { command ->
                        when(command.type) {
                            ReceiveCommand.Type.CREATE -> if (!permissions.any { it.canCreate(command.refName, userName)}) {
                                command.result = ReceiveCommand.Result.REJECTED_NOCREATE
                            }
                            ReceiveCommand.Type.DELETE -> if (!permissions.any { it.canDelete(command.refName, userName)}) {
                                command.result = ReceiveCommand.Result.REJECTED_NODELETE
                            }
                            ReceiveCommand.Type.UPDATE, ReceiveCommand.Type.UPDATE_NONFASTFORWARD -> if (!permissions.any { it.canWrite(command.refName, userName)}) {
                                command.result = ReceiveCommand.Result.REJECTED_NONFASTFORWARD
                            }
                            else -> {}
                        }
                    }
            }
        }.receive(commandInputStream, commandOutputStream, commandErrorStream)
    }
}

