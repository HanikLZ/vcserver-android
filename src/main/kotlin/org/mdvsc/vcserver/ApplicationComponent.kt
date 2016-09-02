package org.mdvsc.vcserver

import android.net.wifi.WifiManager
import dagger.Component
import org.mdvsc.vcserver.gui.AppPreference
import org.mdvsc.vcserver.gui.GuiComponent
import org.mdvsc.vcserver.gui.GuiModule
import org.mdvsc.vcserver.server.ServerComponent
import org.mdvsc.vcserver.server.ServerPreference
import javax.inject.Singleton

/**
 * @author haniklz
 * *
 * @since 16/3/28.
 */
@Component(modules = arrayOf(ApplicationModule::class))
@Singleton
interface ApplicationComponent {
    fun guiComponent(module: GuiModule): GuiComponent
    fun serverComponent():ServerComponent
    fun wifiManager(): WifiManager
    fun appPreferences(): AppPreference
    fun serverPreferences(): ServerPreference
}

