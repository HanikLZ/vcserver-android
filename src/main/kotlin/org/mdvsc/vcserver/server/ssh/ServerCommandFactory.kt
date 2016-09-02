package org.mdvsc.vcserver.server.ssh

import org.apache.sshd.common.util.ThreadUtils
import org.apache.sshd.server.Command
import org.apache.sshd.server.CommandFactory
import org.mdvsc.vcserver.server.PathProvider
import org.mdvsc.vcserver.server.command.BaseCommand
import org.mdvsc.vcserver.server.command.MessageCommand
import org.mdvsc.vcserver.server.command.MyScpCommand
import org.mdvsc.vcserver.server.command.git.GitReceiveCommand
import org.mdvsc.vcserver.server.command.git.GitUploadCommand

/**
 * @author haniklz
 * @since 16/3/20.
 */
class ServerCommandFactory(val pathProvider: PathProvider) : CommandFactory {

    val threadPool = ThreadUtils.newCachedThreadPool("vcserver-thread-pool")

    override fun createCommand(commandStr: String?): Command {
        val list = commandStr?.split(' ')
        val commandName = list?.firstOrNull()?.trim()
        return when(commandName) {
            "git-upload-pack", "upload-pack" -> GitUploadCommand(pathProvider, list?.get(1))
            "git-receive-pack", "receive-pack" -> GitReceiveCommand(pathProvider, list?.get(1))
            "scp" -> MyScpCommand(pathProvider, commandStr, threadPool)
            else -> MessageCommand(pathProvider, "not support command $commandName")
        }.apply { if (this is BaseCommand) executorService = threadPool }
    }
}

