package org.mdvsc.vcserver.gui

import android.app.Activity
import dagger.Module
import dagger.Provides

/**
 * @author haniklz
 * @since 16/4/5.
 */
@Module
class GuiModule(val activity: Activity) {
    @Provides fun layoutInflater() = activity.layoutInflater
    @Provides fun activity() = activity
}

