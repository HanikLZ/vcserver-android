package org.mdvsc.vcserver

import android.app.Application

/**
 * @author haniklz
 * *
 * @since 16/3/28.
 */
class MyApplication : Application() {

    companion object {
        @JvmStatic lateinit var applicationComponent: ApplicationComponent
    }

    override fun onCreate() {
        super.onCreate()
        applicationComponent = DaggerApplicationComponent.builder().applicationModule(ApplicationModule(this)).build()
    }
}

