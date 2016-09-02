package org.mdvsc.vcserver.server.command

import org.apache.sshd.server.Command
import org.apache.sshd.server.Environment
import org.apache.sshd.server.ExitCallback
import org.apache.sshd.server.SessionAware
import org.apache.sshd.server.session.ServerSession
import org.mdvsc.vcserver.server.PathProvider
import java.io.InputStream
import java.io.OutputStream
import java.nio.charset.Charset
import java.util.concurrent.ExecutorService
import java.util.concurrent.Future

/**
 * @author haniklz
 * @since 16/3/25.
 */
abstract class BaseCommand(val pathProvider: PathProvider) : Command, SessionAware {

    lateinit private var commandExitCallback: ExitCallback
    lateinit var clientSession: ServerSession
    lateinit var commandInputStream: InputStream
    lateinit var commandOutputStream: OutputStream
    lateinit var commandErrorStream: OutputStream
    var shutDownExecutor = false
    var executorService: ExecutorService? = null

    private var pendingFuture: Future<*>? = null

    override fun setSession(session: ServerSession) {
        this.clientSession = session
    }

    override fun setExitCallback(callback: ExitCallback) {
        this.commandExitCallback = callback
    }

    override fun setInputStream(input: InputStream) {
        commandInputStream = input
    }

    override fun setErrorStream(err: OutputStream) {
        commandErrorStream = err
    }

    override fun setOutputStream(out: OutputStream) {
        commandOutputStream = out
    }

    override fun destroy() {
        if (!(pendingFuture?.isDone ?: true)) {
            pendingFuture?.cancel(true)
        }
        pendingFuture = null
        if (shutDownExecutor) {
            executorService?.shutdown()
            executorService = null
        }
        commandInputStream.close()
        commandOutputStream.close()
        commandErrorStream.close()
    }

    fun writeErrorMessage(message: String?, charset: Charset = Charsets.UTF_8) {
        if (!message.isNullOrEmpty()) {
            with(commandErrorStream) {
                write(message!!.toByteArray(charset))
                flush()
            }
        }
    }

    fun writeOutputMessage(message: String?, charset: Charset = Charsets.UTF_8) {
        if (!message.isNullOrEmpty()) {
            with(commandOutputStream) {
                write(message!!.toByteArray(charset))
                flush()
            }
        }
    }

    final override fun start(env: Environment?) {
        if (env != null) {
            if (executorService != null) {
                pendingFuture = executorService!!.submit({ onStart(env) })
            } else {
                onStart(env)
            }
        }
    }

    abstract fun onStart(env: Environment);

    open fun exit(code: Int, message: String? = null) {
        if (message.isNullOrEmpty()) commandExitCallback.onExit(code) else commandExitCallback.onExit(code, message)
    }
}

