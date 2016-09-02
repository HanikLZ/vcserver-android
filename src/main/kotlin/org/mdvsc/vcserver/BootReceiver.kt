package org.mdvsc.vcserver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.wifi.WifiManager
import org.mdvsc.vcserver.server.ServerService

/**
 *
 * @author  haniklz
 * @since   16/4/26
 * @version 1.0.0
 */

class BootReceiver: BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val preferences = MyApplication.applicationComponent.appPreferences()
        val startCommand = "start"
        val stopCommand = "stop"
        val commandStr = when(intent.action) {
            Intent.ACTION_BOOT_COMPLETED -> {
                preferences.getAutoStartMethod() == "follow_system_boot"
                startCommand
            }
            WifiManager.WIFI_STATE_CHANGED_ACTION -> {
                if (preferences.getAutoStartMethod() == "when_wifi_on") {
                    when (intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE, WifiManager.WIFI_STATE_UNKNOWN)) {
                        WifiManager.WIFI_STATE_ENABLED -> startCommand
                        WifiManager.WIFI_STATE_DISABLED -> stopCommand
                        else -> null
                    }
                } else {
                    null
                }
            }
            "android.net.wifi.WIFI_AP_STATE_CHANGED" -> {
                if (preferences.getAutoStartMethod() == "when_wifi_hotspot") {
                    when(intent.getIntExtra("wifi_state", 0)) {
                        13 -> startCommand
                        11 -> stopCommand
                        else -> null
                    }
                } else {
                    null
                }
            }
            else -> null
        }
        if (commandStr != null) {
            context.startService(Intent(context, ServerService::class.java).putExtra("command", commandStr))
        }
    }
}
