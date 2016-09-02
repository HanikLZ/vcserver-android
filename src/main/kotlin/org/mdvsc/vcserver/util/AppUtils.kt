package org.mdvsc.vcserver.util

import android.content.Context

/**
 * @author haniklz
 * @since 16/4/5.
 */
object AppUtils {

    fun getVersion(context: Context):String {
        var packageManager = context.packageManager;
        var packInfo = packageManager.getPackageInfo(context.packageName, 0);
        return packInfo.versionName
    }
}
