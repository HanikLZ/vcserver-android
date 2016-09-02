package org.mdvsc.vcserver.gui.common

import android.support.v4.app.Fragment
import org.mdvsc.vcserver.MyApplication
import org.mdvsc.vcserver.gui.GuiModule

/**
 * @author haniklz
 * @since 16/4/6.
 */
open class BaseFragment: Fragment() {
    val component by lazy {
        if (activity is BaseActivity) {
            (activity as BaseActivity).component
        } else {
            MyApplication.applicationComponent.guiComponent(GuiModule(activity))
        }
    }
}

