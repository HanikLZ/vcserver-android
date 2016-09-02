package org.mdvsc.vcserver.gui

import android.os.Bundle
import android.support.v7.app.ActionBar
import android.support.v7.widget.Toolbar
import org.mdvsc.vcserver.R
import org.mdvsc.vcserver.gui.common.BaseActivity

/**
 * @author haniklz
 * @since 16/4/6.
 */
class ComponentActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        with(intent) {
            var containerId = if (getBooleanExtra(GuiHelper.argumentWithActionBar, false)) {
                setContentView(R.layout.activity_component)
                setSupportActionBar(findViewById(R.id.toolbar) as Toolbar)
                setupActionBar(supportActionBar!!)
                R.id.linear_layout_content
            } else android.R.id.content
            var name = getStringExtra(GuiHelper.argumentFragmentName)
            if (name != null) {
                val arguments = getBundleExtra(GuiHelper.argumentFragmentArgs)
                try {
                    val fragment = android.support.v4.app.Fragment.instantiate(this@ComponentActivity, name, arguments)
                    supportFragmentManager.beginTransaction().replace(containerId, fragment).commit()
                } catch (e: Exception) {
                    val fragment = android.app.Fragment.instantiate(this@ComponentActivity, name, arguments)
                    fragmentManager.beginTransaction().replace(containerId, fragment).commit()
                }
            }
            title = getStringExtra(GuiHelper.argumentTitle)
        }
    }

    protected fun setupActionBar(actionBar:ActionBar) {
        actionBar.setDisplayHomeAsUpEnabled(true)
    }
}

