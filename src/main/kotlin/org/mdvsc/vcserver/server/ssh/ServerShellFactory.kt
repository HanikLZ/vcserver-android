package org.mdvsc.vcserver.server.ssh

import android.content.res.Resources
import org.apache.sshd.common.Factory
import org.apache.sshd.common.util.ThreadUtils
import org.apache.sshd.server.Command
import org.mdvsc.vcserver.server.PathProvider
import org.mdvsc.vcserver.server.command.ShellCommand

/**
 * @author haniklz
 * @since 16/3/20.
 */
class ServerShellFactory(val resources: Resources, val pathProvider: PathProvider) : Factory<Command> {
    override fun create() = ShellCommand(pathProvider).apply {
        executorService = ThreadUtils.newSingleThreadExecutor("shell")
        shutDownExecutor = true
    }
}

