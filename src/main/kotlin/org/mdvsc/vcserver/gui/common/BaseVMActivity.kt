package org.mdvsc.vcserver.gui.common

import android.os.Bundle

/**
 *
 * @author  haniklz
 * @since   16/4/2
 * @version 1.0.0
 */
abstract class BaseVMActivity<T : BaseViewModel>: BaseActivity() {

    val viewModel: T by lazy { onCreateViewModel() }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        viewModel.onCreate()
    }

    override fun onDestroy() {
        viewModel.onDestroy()
        super.onDestroy()
    }

    override fun onResume() {
        super.onResume()
        viewModel.onResume()
    }

    override fun onPause() {
        viewModel.onPause()
        super.onPause()
    }

    abstract fun onCreateViewModel(): T

}
