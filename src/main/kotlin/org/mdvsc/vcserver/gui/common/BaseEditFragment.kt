package org.mdvsc.vcserver.gui.common

import android.os.Bundle
import android.support.v14.preference.PreferenceFragment
import android.support.v4.app.ActivityCompat
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import org.mdvsc.vcserver.MyApplication
import org.mdvsc.vcserver.R
import org.mdvsc.vcserver.gui.GuiHelper
import org.mdvsc.vcserver.gui.GuiModule
import org.mdvsc.vcserver.server.ServerPathProvider

/**
 * @author haniklz
 * @since 16/4/11.
 */
abstract class BaseEditFragment(val preferenceResourceId:Int): PreferenceFragment() {

    val serverPathProvider by lazy { ServerPathProvider(arguments.getString(GuiHelper.argumentWorkPath)) }
    val component by lazy {
        if (activity is BaseActivity) {
            (activity as BaseActivity).component
        } else {
            MyApplication.applicationComponent.guiComponent(GuiModule(activity))
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.edit, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem) = when(item.itemId) {
        R.id.action_complete -> {
            if (onComplete()) ActivityCompat.finishAfterTransition(activity)
            true
        }
        R.id.action_delete -> {
            if (onDelete()) ActivityCompat.finishAfterTransition(activity)
            true
        }
        else -> super.onOptionsItemSelected(item)
    }

    override fun onCreatePreferences(p0: Bundle?, p1: String?) {
        preferenceManager.sharedPreferencesName = ""
        addPreferencesFromResource(preferenceResourceId)
    }

    abstract fun onComplete():Boolean

    abstract fun onDelete():Boolean

}

