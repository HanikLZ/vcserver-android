package org.mdvsc.vcserver.server.command

import org.apache.sshd.server.Environment
import org.mdvsc.vcserver.server.PathProvider

/**
 * @author haniklz
 * @since 16/3/26.
 */
class MessageCommand(pathProvider: PathProvider, var message: String) : BaseCommand(pathProvider) {
    override fun onStart(env: Environment) {
        writeErrorMessage(message)
        exit(0)
    }
}
