package org.mdvsc.vcserver.gui.common

import android.support.v4.app.ActivityCompat
import android.support.v7.app.AppCompatActivity
import android.view.MenuItem
import org.mdvsc.vcserver.MyApplication
import org.mdvsc.vcserver.gui.GuiModule

/**
 * @author haniklz
 * @since 16/4/5.
 */
open class BaseActivity : AppCompatActivity() {

    val component = MyApplication.applicationComponent.guiComponent(GuiModule(this))

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        if (item?.itemId == android.R.id.home) {
            return onHomeButtonClicked()
        }
        return super.onOptionsItemSelected(item)
    }

    open fun onHomeButtonClicked():Boolean {
        ActivityCompat.finishAfterTransition(this)
        return true
    }

}
