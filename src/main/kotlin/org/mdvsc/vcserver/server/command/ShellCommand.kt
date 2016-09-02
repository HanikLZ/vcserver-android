package org.mdvsc.vcserver.server.command

import org.apache.sshd.server.Environment
import org.mdvsc.vcserver.server.PathProvider
import org.mdvsc.vcserver.server.ServerHelper
import java.io.BufferedReader
import java.io.InputStreamReader
import java.util.regex.Pattern

/**
 * @author haniklz
 * @since 16/3/26.
 */
class ShellCommand(pathProvider: PathProvider) : BaseCommand(pathProvider) {

    private val commandSplitStr = "\n+"
    private val commandSplitPattern = Pattern.compile(commandSplitStr)

    override fun onStart(env: Environment) {
        val userName = env.env[Environment.ENV_USER] ?: ""
        val userInfo = ServerHelper.getUserInfo(pathProvider, userName)
        if (userInfo != null && userInfo.isAdmin) {
            val beginLine = "$userName: "
            echo(beginLine)
            val readBuffer = CharArray(1)
            BufferedReader(InputStreamReader(commandInputStream)).use {
                val commandLine = StringBuilder()
                while (true) {
                    if (it.read(readBuffer) <= 0) return@use
                    val line = String(readBuffer).replace('\r', '\n')
                    var execute = false
                    line.split(commandSplitPattern).forEachIndexed {
                        i, s ->
                        echo(s)
                        if (execute) {
                            echo("\r\n")
                            if (processCommand(commandLine.toString())) return@use
                            commandLine.setLength(0)
                            echo(beginLine)
                            execute = false
                        } else {
                            execute = true
                        }
                        commandLine.append(s)
                    }
                }
            }
            exit(0)
        } else {
            exit(0, "no permission to open shell.")
        }
    }

    fun echo(vararg messages: String) {
        if (messages.isNotEmpty()) {
            messages.forEach { commandOutputStream.write(it.toByteArray()) }
            commandOutputStream.flush()
        }
    }

    fun processCommand(command: String) = when (command) {
        "exit" -> true
        else -> false
    }
}

