package org.mdvsc.vcserver.gui

import android.content.Context

/**
 *
 * @author  haniklz
 * @since   16/4/2
 * @version 1.0.0
 */
class AppPreference(val context: Context) {

    companion object {
        val preferenceName = "main"
        private val keyEnablePasswordLogin = "enablePasswordLogin"
        private val keyRepoInitName = "repoInitName"
        private val keyRepoInitEmail = "repoInitEmail"
        private val keyRepoInitCommit = "repoInitCommit"
        private val keyAutoStartOn = "autoStartOn"
    }

    private val preferences by lazy { context.getSharedPreferences(preferenceName, Context.MODE_PRIVATE) }

    var enablePasswordLogin = false
        get() = preferences.getBoolean(keyEnablePasswordLogin, field)

    fun getRepoInitName() = preferences.getString(keyRepoInitName, null)
    fun getRepoInitEmail() = preferences.getString(keyRepoInitEmail, null)
    fun getRepoInitCommit() = preferences.getString(keyRepoInitCommit, null)
    fun getAutoStartMethod() = preferences.getString(keyAutoStartOn, null)
}

