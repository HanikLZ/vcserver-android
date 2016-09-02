package org.mdvsc.vcserver.server.command

import org.apache.sshd.server.Environment
import org.apache.sshd.server.command.ScpCommand
import org.mdvsc.vcserver.server.PathProvider
import org.mdvsc.vcserver.server.ServerHelper
import java.util.concurrent.ExecutorService

/**
 *
 * @author  haniklz
 * @since   16/4/16
 * @version 1.0.0
 */
class MyScpCommand(val pathProvider: PathProvider, command:String?, executorService: ExecutorService) : ScpCommand(command, executorService) {

    override fun start(env: Environment) {
        val userName = env.env[Environment.ENV_USER] ?: ""
        val userInfo = ServerHelper.getUserInfo(pathProvider, userName)
        if (userInfo != null && userInfo.isAdmin) {
            super.start(env)
        } else {
            if (callback != null) {
                callback.onExit(0, "no permission to do scp.")
            }
        }
    }
}
