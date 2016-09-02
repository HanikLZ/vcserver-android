package org.mdvsc.vcserver.gui.user.group

import android.os.Bundle
import android.view.View
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.mdvsc.vcserver.R
import org.mdvsc.vcserver.gui.GuiHelper
import org.mdvsc.vcserver.gui.common.BaseListFragment
import org.mdvsc.vcserver.server.PathProvider
import org.mdvsc.vcserver.server.ServerHelper
import org.mdvsc.vcserver.server.ServerPathProvider
import org.mdvsc.vcserver.server.model.GroupInfo
import org.mdvsc.vcserver.util.JsonUtils
import java.io.File

/**
 * @author haniklz
 * @since 16/4/6.
 */
class GroupListFragment : BaseListFragment<GroupListAdapter>() {

    private val serverPathProvider by lazy { ServerPathProvider(arguments.getString(GuiHelper.argumentWorkPath)) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        GuiHelper.registerEventBus(this)
    }

    override fun onDestroy() {
        GuiHelper.unregisterEventBus(this)
        super.onDestroy()
    }

    override fun setup() {
        super.setup()
        floatingActionButton.apply {
            visibility = View.VISIBLE
            setImageResource(R.drawable.icon_group_add)
            setOnClickListener { GuiHelper.startComponentActivity(it.context
                    , GroupEditFragment::class.java
                    , arguments.apply { remove(GuiHelper.argumentGroupItem) }
                    , getString(R.string.new_group)) }
        }

        val groupAdapter = component.inject(GroupListAdapter(arguments))
        val dir = serverPathProvider.getPathFile(PathProvider.Folder.GROUP)
        groupAdapter.updateDataItem(dir.list()?.filter { it.toLowerCase().endsWith(ServerHelper.infoFileExtension) }
                ?.map {
                    val file = File(dir, it)
                    val info = JsonUtils.fromJson(file.readText(ServerHelper.defaultCharset), GroupInfo::class.java)
                    GroupItem(file.nameWithoutExtension, info?: GroupInfo())
                })
        adapter = groupAdapter
    }

    @Subscribe(threadMode = ThreadMode.MAIN) fun event(event:RemoveGroupEvent) {
        var a = adapter
        if (a != null) {
            val index = (0..a.getDataItemSize() - 1).indexOfFirst { a.getDataItem(it).id == event.id }
            if (index >= 0) a.removeDataItem(index)
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN) fun event(event:ModifyGroupEvent) {
        var a = adapter
        if (a != null) {
            val index = (0..a.getDataItemSize() - 1).indexOfFirst { a.getDataItem(it).id == event.item.id }
            if (index >= 0) a.setDataItem(index, event.item)
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN) fun event(event:AddGroupEvent) {
        adapter?.appendDataItem(event.item)
    }
}

