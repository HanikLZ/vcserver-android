package org.mdvsc.vcserver

import android.content.Context
import android.net.wifi.WifiManager
import dagger.Module
import dagger.Provides
import org.mdvsc.vcserver.gui.AppPreference
import org.mdvsc.vcserver.server.ServerPreference
import javax.inject.Singleton

/**
 * @author haniklz
 * @since 16/4/5.
 */
@Module
class ApplicationModule(private val mContext: Context) {
    @Provides fun context() = mContext
    @Provides fun resources() = mContext.resources
    @Singleton @Provides fun appPreferences() = AppPreference(mContext);
    @Singleton @Provides fun serverPreferences() = ServerPreference(mContext);
    @Singleton @Provides fun wifiManager() = mContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
}

