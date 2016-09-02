package org.mdvsc.vcserver.gui.repo

import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.mdvsc.vcserver.R
import org.mdvsc.vcserver.gui.GuiHelper
import org.mdvsc.vcserver.gui.ServerUrlChangeEvent
import org.mdvsc.vcserver.gui.common.BaseListFragment
import org.mdvsc.vcserver.server.PathProvider
import org.mdvsc.vcserver.server.ServerHelper
import org.mdvsc.vcserver.server.ServerPathProvider
import org.mdvsc.vcserver.server.model.RepoInfo
import org.mdvsc.vcserver.util.JsonUtils
import java.io.File
import java.util.*

/**
 * @author haniklz
 * @since 16/4/6.
 */
class RepoListFragment : BaseListFragment<RepoListAdapter>() {

    private val serverPathProvider by lazy { ServerPathProvider(arguments.getString(GuiHelper.argumentWorkPath)) }
    private var stickServerUrl = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        GuiHelper.registerEventBus(this)
    }

    override fun onDestroy() {
        GuiHelper.unregisterEventBus(this)
        super.onDestroy()
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        inflater?.inflate(R.menu.repo, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?) = when (item?.itemId) {
        R.id.action_cleanup -> {
            true
        }
        else -> super.onOptionsItemSelected(item)
    }

    private fun listDir(dir:File, list:ArrayList<RepoItem>, path:String = "") {
        if (dir.exists() && dir.isDirectory) {
            dir.listFiles({ file -> file.isDirectory }).forEach {
                val currentPath = if (path.isNotEmpty()) path + File.separatorChar + it.name else it.name
                if (it.extension.isNotEmpty()) {
                    // 根据后缀名判断是否为某种类型的代码仓库
                    val infoFile = File(it, ServerHelper.repoInfoFileName)
                    val info = if (infoFile.exists() && infoFile.isFile && infoFile.canRead()) {
                        JsonUtils.fromJson(infoFile.readText(ServerHelper.defaultCharset), RepoInfo::class.java)
                    } else null
                    list.add(RepoItem(currentPath, info ?: RepoInfo()))
                } else {
                    listDir(it, list, currentPath)
                }
            }
        }
    }

    override fun setup() {
        super.setup()
        adapter = component.inject(RepoListAdapter(arguments)).apply {
            updateDataItem(ArrayList<RepoItem>().apply { listDir(serverPathProvider.getPathFile(PathProvider.Folder.REPO), this) })
            serverUrl = stickServerUrl
        }

        floatingActionButton.apply {
            visibility = View.VISIBLE
            setImageResource(R.drawable.icon_repo_add)
            setOnClickListener { GuiHelper.startComponentActivity(it.context
                    , RepoEditFragment::class.java
                    , arguments.apply { remove(GuiHelper.argumentRepoItem) }
                    , getString(R.string.new_repo)) }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN) fun event(event:RemoveRepoEvent) {
        val a = adapter
        if (a != null) {
            val index = (0..a.getDataItemSize() - 1).indexOfFirst { a.getDataItem(it).path == event.path }
            if (index >= 0) a.removeDataItem(index)
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN) fun event(event:ModifyRepoEvent) {
        val a = adapter
        if (a != null) {
            val index = (0..a.getDataItemSize() - 1).indexOfFirst { a.getDataItem(it).path == event.item.path }
            if (index >= 0) a.setDataItem(index, event.item)
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN) fun event(event:AddRepoEvent) {
        adapter?.appendDataItem(event.item)
    }

    @Subscribe(threadMode = ThreadMode.MAIN, sticky = true) fun event(event:ServerUrlChangeEvent) {
        adapter?.serverUrl = event.url
        stickServerUrl = event.url
    }
}

