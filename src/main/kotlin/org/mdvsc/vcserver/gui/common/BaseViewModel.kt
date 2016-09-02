package org.mdvsc.vcserver.gui.common

/**
 * @author haniklz
 * @since 16/4/5.
 */
open class BaseViewModel {
    open fun onCreate() {}
    open fun onDestroy() {}
    open fun onResume() {}
    open fun onPause() {}
}