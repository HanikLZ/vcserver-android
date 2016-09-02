package org.mdvsc.vcserver.server

import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.os.Handler
import android.os.IBinder
import android.support.v7.app.NotificationCompat
import org.apache.sshd.SshBuilder
import org.apache.sshd.SshServer
import org.mdvsc.vcserver.IServerService
import org.mdvsc.vcserver.MyApplication
import org.mdvsc.vcserver.R
import org.mdvsc.vcserver.gui.EntryActivity
import org.mdvsc.vcserver.server.model.UserInfo
import org.mdvsc.vcserver.util.FileUtils
import org.mdvsc.vcserver.util.InetAddressUtils
import java.lang.ref.WeakReference
import java.net.ServerSocket

/**
 * @author haniklz
 * @since 16/3/20.
 */
class ServerService : Service() {

    companion object {
        val command = "command"
        val commandStart = "start"
        val commandStop = "stop"
        val messageStarted = "org.mdvsc.vcserver.message.SERVICE_STARTED"
        val messageStopped = "org.mdvsc.vcserver.message.SERVICE_STOPED"
    }

    val commandIntentStop = "org.mdvsc.vcserver.STOP_SERVICE"

    val component = MyApplication.applicationComponent.serverComponent()
    val serverPreferences = MyApplication.applicationComponent.serverPreferences();

    private var sshServer: SshServer? = null
    private val postCloseHandler = Handler()
    private val autoCloseDelayMillis = 1000L * 60
    private val autoCloseRunnable = Runnable { if (!isServerStarted()) stopSelf() }

    private val commandReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            when(intent?.action) {
                commandIntentStop -> stopSshServer()
                ConnectivityManager.CONNECTIVITY_ACTION -> if (isServerStarted()) startForeground()
            }
        }
    }

    fun isServerStarted() = sshServer != null

    fun checkInitDefaultConf() {
        val file = component.getServerPathProvider().getPathFile(PathProvider.Folder.REPO, ServerHelper.permissionConfigFileName)
        if (!file.exists() && FileUtils.safeMakeNewFile(file) && file.canWrite()) {
            file.writeText(ServerHelper.defaultRepoConfigContent, ServerHelper.defaultCharset)
        }
    }

    fun createNotification(): Notification {
        return NotificationCompat.Builder(this)
                .setOngoing(true)
                .setContentTitle(getString(R.string.server_has_started))
                .setContentText(getServerListenUrl())
                .setSmallIcon(R.drawable.icon_application)
                .setContentIntent(PendingIntent.getActivity(this, 0
                        , Intent(this, EntryActivity::class.java)
                            .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                            .addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
                            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        , PendingIntent.FLAG_UPDATE_CURRENT))
                .setDeleteIntent(PendingIntent.getBroadcast(this, 0, Intent(commandIntentStop), PendingIntent.FLAG_UPDATE_CURRENT))
                .build()
    }

    fun startSshServer() {
        if (sshServer != null) return
        cancelAutoClose()
        checkInitDefaultConf()
        var serverPort = serverPreferences.sshPort
        if (serverPort <= 0) {
            serverPort = try {ServerSocket(0).use {it.localPort}} catch (e : Exception) {29418}
            serverPreferences.sshPort = serverPort
        }

        val server = SshBuilder.server().randomFactory(component.getRandomFactory()).build().apply {
            with(component) {
                commandFactory = getCommandFactory()
                shellFactory = getShellFactory()
                fileSystemFactory = getFileSystemFactory()
                keyPairProvider = getKeyPairProvider()
                passwordAuthenticator = getPasswordAuthenticator()
                publickeyAuthenticator = getPublicKeyAuthenticator()
                port = serverPort
            }
        }
        try {
            server.start()
            sshServer = server
        } catch (e: Exception) {
            e.printStackTrace()
            return
        }
        startForeground()
        serverPreferences.startServerOnCreate = true
        sendBroadcast(Intent(messageStarted))
    }

    fun cancelAutoClose() = postCloseHandler.removeCallbacks(autoCloseRunnable)
    fun notifyAutoClose() {
        cancelAutoClose()
        postCloseHandler.postDelayed(autoCloseRunnable, autoCloseDelayMillis)
    }

    fun startForeground() = startForeground(hashCode(), createNotification())

    fun stopSshServer() {
        if (!isServerStarted()) return
        try {
            sshServer?.stop(true)
            sshServer = null
        } catch (e: Exception) {
            e.printStackTrace()
            return
        }
        stopForeground(true)
        serverPreferences.startServerOnCreate = false
        notifyAutoClose()
        sendBroadcast(Intent(messageStopped))
    }

    fun getServerListenUrl():String {
        return if (isServerStarted()) "${InetAddressUtils.getLocalV4Address()}:${sshServer?.port}" else ""
    }

    fun setSshPort(port:Int) {
        serverPreferences.sshPort = port
    }

    fun getSshPort() = sshServer?.port ?: serverPreferences.sshPort

    fun setWorkPath(path:String?) {
        serverPreferences.setWorkPath(path)
    }

    fun modifyPassword(username:String, password: String) {
        val pathProvider = component.getServerPathProvider()
        var user = ServerHelper.getUserInfo(pathProvider, username)
        if (user == null) {
            user = UserInfo()
        }
        user.password = ServerHelper.stringDigest(password)
        user.enablePasswordAuthentication = true
        ServerHelper.writeUserInfo(pathProvider, username, user)
    }

    fun verifyPassword(username: String, password: String?):Boolean {
        val user = ServerHelper.getUserInfo(component.getServerPathProvider(), username)
        return user?.password == ServerHelper.stringDigest(password)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when(intent?.getStringExtra(command)) {
            commandStart -> if (!isServerStarted()) startSshServer()
            commandStop -> if (isServerStarted()) stopSshServer()
        }
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onCreate() {
        super.onCreate()
        notifyAutoClose()
        registerReceiver(commandReceiver, IntentFilter(commandIntentStop).apply { addAction(ConnectivityManager.CONNECTIVITY_ACTION) })
        if (serverPreferences.startServerOnCreate) {
            startSshServer()
        }
    }

    override fun onDestroy() {
        cancelAutoClose()
        unregisterReceiver(commandReceiver)
        stopSshServer()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? {
        cancelAutoClose()
        return ServiceBinder(this)
    }

    override fun onRebind(intent: Intent?) {
        super.onRebind(intent)
        cancelAutoClose()
    }

    override fun onUnbind(intent: Intent?): Boolean {
        notifyAutoClose()
        return true
    }
}

class ServiceBinder(server:ServerService): IServerService.Stub() {

    val serverReference = WeakReference<ServerService>(server)

    override fun getServerUrl() = serverReference.get()?.getServerListenUrl()
    override fun verifyAdminPassword(password: String?) = serverReference.get()?.verifyPassword(ServerHelper.adminUserName, password) ?: false
    override fun isServerStarted() = serverReference.get()?.isServerStarted() ?: false
    override fun getListenPort() = serverReference.get()?.getSshPort() ?: 0
    override fun getWorkPath() = serverReference.get()?.component?.getServerPathProvider()?.getPathFile()?.absolutePath

    override fun setListenPort(port: Int) {
        serverReference.get()?.setSshPort(port)
    }

    override fun setWorkPath(path: String?) {
        serverReference.get()?.setWorkPath(path)
    }

    override fun startServer() {
        serverReference.get()?.startSshServer()
    }

    override fun stopServer() {
        serverReference.get()?.stopSshServer()
    }

    override fun modifyUserPassword(user: String?, password: String?) {
        if (user != null && password != null) {
            serverReference.get()?.modifyPassword(user, password)
        }
    }

    override fun modifyAdminPassword(password: String?) {
        if (password != null) {
            serverReference.get()?.modifyPassword(ServerHelper.adminUserName, password)
        }
    }
}

