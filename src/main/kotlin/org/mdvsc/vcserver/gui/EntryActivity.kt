package org.mdvsc.vcserver.gui

import android.Manifest
import android.content.pm.PackageManager
import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.AlertDialog
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import org.mdvsc.vcserver.R
import org.mdvsc.vcserver.databinding.ActivityEntryBinding
import org.mdvsc.vcserver.gui.common.BaseVMActivity
import org.mdvsc.vcserver.gui.repo.RepoListFragment
import org.mdvsc.vcserver.gui.user.UserListFragment

/**
 * @author haniklz
 * @since 16/3/24.
 */
class EntryActivity : BaseVMActivity<EntryViewModel>() {

    private val permissionRequestCode = 1

    override fun onCreateViewModel() = component.inject(EntryViewModel())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = DataBindingUtil.setContentView<ActivityEntryBinding>(this, R.layout.activity_entry)
        binding.vm = viewModel
        setSupportActionBar(binding.toolbar)
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        checkPermission()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        val bundle = Bundle()
        bundle.putString(GuiHelper.argumentWorkPath, viewModel.workPath())
        when(item?.itemId) {
            R.id.action_settings -> GuiHelper.startComponentActivity(this, MainPreferenceFragment::class.java, bundle, getString(R.string.preferences))
            R.id.action_users -> GuiHelper.startComponentActivity(this, UserListFragment::class.java, bundle, getString(R.string.user_manager))
            R.id.action_repos -> GuiHelper.startComponentActivity(this, RepoListFragment::class.java, bundle, getString(R.string.repo_manager))
            else -> return super.onOptionsItemSelected(item)
        }
        return true
    }

    private fun checkPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                AlertDialog.Builder(this)
                    .setTitle(R.string.permission_request)
                    .setMessage(R.string.permission_request_info)
                    .setPositiveButton(android.R.string.ok, {d, w -> requestPermission() })
                    .show()
            } else {
                requestPermission()
            }
        }
    }

    private fun requestPermission() {
        ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), permissionRequestCode)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode != permissionRequestCode) {
            return
        }
        if (permissions.isEmpty() || grantResults.isEmpty() || grantResults.any() { it == PackageManager.PERMISSION_DENIED }) {
            Toast.makeText(this, R.string.permission_request_fail, Toast.LENGTH_LONG).show();
        }
    }
}


