package org.mdvsc.vcserver.gui

import android.app.Service
import android.content.ComponentName
import android.content.Intent
import android.os.Bundle
import android.os.IBinder
import android.support.v14.preference.PreferenceFragment
import android.support.v7.preference.EditTextPreference
import android.support.v7.preference.ListPreference
import android.support.v7.preference.Preference
import net.rdrei.android.dirchooser.DirectoryChooserConfig
import net.rdrei.android.dirchooser.DirectoryChooserFragment
import org.mdvsc.vcserver.R
import org.mdvsc.vcserver.server.ServerHelper
import org.mdvsc.vcserver.server.ServerPathProvider
import org.mdvsc.vcserver.server.ServerService
import org.mdvsc.vcserver.util.AppUtils
import org.mdvsc.vcserver.util.FileUtils

/**
 * @author haniklz
 * @since 16/4/5.
 */
class MainPreferenceFragment : PreferenceFragment() {

    val serverPathProvider by lazy { ServerPathProvider(arguments.getString(GuiHelper.argumentWorkPath)) }
    private val keyWorkPath = "workPath"
    private val keyAdminPassword = "adminPassword"
    private val keyAbout = "about"
    private val keyPort = "port"
    var portPreference: EditTextPreference? = null
    var workPathPreference: Preference? = null

    private val serviceConnection: ServerConnection = object : ServerConnection() {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            super.onServiceConnected(name, service)
            portPreference?.changeText(serverManager?.listenPort.toString())
            workPathPreference?.summary = serverManager?.workPath
        }
    }

    private fun EditTextPreference.changeText(content:String?) {
        text = content
        summary = content
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activity.bindService(Intent(activity, ServerService::class.java), serviceConnection, Service.BIND_AUTO_CREATE)
    }

    override fun onDestroy() {
        activity.unbindService(serviceConnection)
        super.onDestroy()
    }

    override fun onCreatePreferences(p0: Bundle?, p1: String?) {
        preferenceManager.sharedPreferencesName = AppPreference.preferenceName
        addPreferencesFromResource(R.xml.preferences);
        findPreference(keyAbout).summary = AppUtils.getVersion(activity)
        with(serviceConnection) {
            findPreference(keyAdminPassword).apply {
                setOnPreferenceChangeListener { preference, any ->
                    serverManager?.modifyAdminPassword(any.toString())
                    false
                }
            }
            workPathPreference = findPreference(keyWorkPath).apply {
                setOnPreferenceClickListener {
                    val config = DirectoryChooserConfig.builder()
                            .newDirectoryName(it.title?.toString())
                            .initialDirectory(it.summary?.toString())
                            .build()
                    val dialog = DirectoryChooserFragment.newInstance(config)
                    dialog.directoryChooserListener = object : DirectoryChooserFragment.OnFragmentInteractionListener {
                        override fun onCancelChooser() { dialog.dismiss() }
                        override fun onSelectDirectory(path: String) {
                            dialog.dismiss()
                            it.summary = path
                            serverManager?.workPath = path
                        }
                    }
                    dialog.show(activity.fragmentManager, null)
                    true
                }
            }
            portPreference = (findPreference(keyPort) as EditTextPreference).apply {
                setOnPreferenceChangeListener { preference, any ->
                    try { serverManager?.listenPort = Integer.parseInt(any.toString()) } catch (e: Exception) { }
                    changeText(any.toString())
                    false
                }
            }
            (findPreference("autoStartOn") as ListPreference).apply {
                summary = entry
                setOnPreferenceChangeListener { preference, any ->
                    val index = findIndexOfValue(any?.toString());
                    if (index >= 0) summary = entries[index];
                    true
                }
            }
            (findPreference("repoInitReadme") as EditTextPreference).apply {
                val file = ServerHelper.getDefaultReadmeFile(serverPathProvider)
                if (file.exists() && file.isFile && file.canRead()) text = file.readText(ServerHelper.defaultCharset)
                setOnPreferenceChangeListener { preference, any ->
                    FileUtils.makeSureNewFile(file).writeText(any.toString(), ServerHelper.defaultCharset)
                    true
                }
            }

            (findPreference("repoInitIgnore") as EditTextPreference).apply {
                val file = ServerHelper.getDefaultIgnoreFile(serverPathProvider)
                if (file.exists() && file.isFile && file.canRead()) text = file.readText(ServerHelper.defaultCharset)
                setOnPreferenceChangeListener { preference, any ->
                    FileUtils.makeSureNewFile(file).writeText(any.toString(), ServerHelper.defaultCharset)
                    true
                }
            }
        }
    }
}

