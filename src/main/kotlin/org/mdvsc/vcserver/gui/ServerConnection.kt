package org.mdvsc.vcserver.gui

import android.content.ComponentName
import android.content.ServiceConnection
import android.os.IBinder
import org.mdvsc.vcserver.IServerService

/**
 * @author haniklz
 * @since 16/4/6.
 */
open class ServerConnection: ServiceConnection {

    var serverManager: IServerService? = null
        private set

    override fun onServiceDisconnected(name: ComponentName?) {
        serverManager = null
    }

    override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
        serverManager = IServerService.Stub.asInterface(service)
    }

}
