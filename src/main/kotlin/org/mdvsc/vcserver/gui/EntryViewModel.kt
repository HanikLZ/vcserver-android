package org.mdvsc.vcserver.gui

import android.app.Activity
import android.app.AlertDialog
import android.app.Service
import android.content.*
import android.databinding.ObservableBoolean
import android.databinding.ObservableField
import android.net.ConnectivityManager
import android.os.IBinder
import android.widget.CompoundButton
import android.widget.TextView
import org.mdvsc.vcserver.R
import org.mdvsc.vcserver.gui.common.BaseViewModel
import org.mdvsc.vcserver.server.ServerService
import javax.inject.Inject

/**
 * @author haniklz
 * @since 16/4/5.
 */

class EntryViewModel : BaseViewModel() {

    @Inject lateinit var context: Activity
    @Inject lateinit var preferences: AppPreference

    val serverUrl = ObservableField<CharSequence>()
    val serverStarted = ObservableBoolean(false)

    private val connectReceiver = object:BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (when(intent.action) {
                ConnectivityManager.CONNECTIVITY_ACTION -> true
                ServerService.messageStarted -> {
                    serverStarted.set(true)
                    true
                }
                ServerService.messageStopped -> {
                    serverStarted.set(false)
                    true
                }
                else -> false
            }) {
                setServerUrl(serviceConnection.serverManager?.serverUrl)
            }
        }
    }

    private var loginDialog: AlertDialog? = null
    private val serviceIntent: Intent by lazy { Intent(context, ServerService::class.java) }
    private val serviceConnection = object : ServerConnection() {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            super.onServiceConnected(name, service)
            serverStarted.set(serverManager?.isServerStarted ?: false)
            setServerUrl(serverManager?.serverUrl)
            if (preferences.enablePasswordLogin && loginDialog == null) {
                showLoginDialog()
            }
        }
    }

    val serverStartCloseChangeListener = CompoundButton.OnCheckedChangeListener {
        button, checked ->
        serverStarted.set(checked)
        serviceConnection.run {
            val serverStarted = serverManager?.isServerStarted ?: false
            if (checked && !serverStarted) {
                serverManager?.startServer()
                serverUrl.set(serverManager?.serverUrl)
            } else if (!checked && serverStarted) {
                serverManager?.stopServer()
            }
        }
    }

    fun setServerUrl(url:String?) {
        serverUrl.set(url)
        GuiHelper.postEvent(ServerUrlChangeEvent(url?:""), true)
    }

    fun workPath() = serviceConnection.serverManager?.workPath

    private fun showLoginDialog() {
        loginDialog = AlertDialog.Builder(context)
                .setTitle(R.string.manager_password)
                .setView(R.layout.dialog_input)
                .setCancelable(false)
                .setNegativeButton(android.R.string.cancel, { dialog, which -> context.finish() })
                .setPositiveButton(android.R.string.ok, {
                    dialog, which ->
                    if (!(serviceConnection.serverManager?.verifyAdminPassword(((dialog as AlertDialog).findViewById(R.id.text_view_input) as TextView).text.toString()) ?: false)) {
                        showLoginDialog()
                    }
                })
                .setOnDismissListener { loginDialog = null }
                .show()
    }

    override fun onCreate() {
        super.onCreate()
        context.startService(serviceIntent)
        context.registerReceiver(connectReceiver, IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION).apply {
            addAction(ServerService.messageStarted)
            addAction(ServerService.messageStopped)
        })
    }

    override fun onDestroy() {
        super.onDestroy()
        context.unregisterReceiver(connectReceiver)
    }

    override fun onResume() {
        super.onResume()
        context.bindService(serviceIntent, serviceConnection, Service.BIND_AUTO_CREATE)
    }

    override fun onPause() {
        super.onPause()
        context.unbindService(serviceConnection)
    }
}

