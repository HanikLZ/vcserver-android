package org.mdvsc.vcserver.gui.user

import android.os.Bundle
import android.util.SparseBooleanArray
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.mdvsc.vcserver.R
import org.mdvsc.vcserver.gui.GuiHelper
import org.mdvsc.vcserver.gui.common.BaseListFragment
import org.mdvsc.vcserver.gui.user.group.GroupListFragment
import org.mdvsc.vcserver.server.PathProvider
import org.mdvsc.vcserver.server.ServerHelper
import org.mdvsc.vcserver.server.ServerPathProvider
import org.mdvsc.vcserver.server.model.UserInfo
import java.io.File
import java.util.*

/**
 * @author haniklz
 * @since 16/4/6.
 */
class UserListFragment : BaseListFragment<UserListAdapter>() {

    private val serverPathProvider by lazy { ServerPathProvider(arguments.getString(GuiHelper.argumentWorkPath)) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        GuiHelper.registerEventBus(this)
    }

    override fun onDestroy() {
        GuiHelper.unregisterEventBus(this)
        super.onDestroy()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.user, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?) = when(item?.itemId) {
        R.id.action_groups -> {
            GuiHelper.startComponentActivity(context, GroupListFragment::class.java, arguments, getString(R.string.group_manager))
            true
        }
        else -> super.onOptionsItemSelected(item)
    }

    override fun setup() {
        super.setup()
        val userAdapter = component.inject(UserListAdapter(arguments))
        val userList = ArrayList<UserItem>()
        val userMap = SparseBooleanArray()
        val addUser = {
            dir: File, isInfo:Boolean ->
            if (dir.exists() && dir.isDirectory) {
                dir.listFiles().forEach {
                    if (it.isFile) {
                        var name = it.nameWithoutExtension
                        if (name.isEmpty()) name = it.name
                        val id = name.hashCode()
                        if (!userMap.get(id)) {
                            userMap.put(id, true)
                            val userInfo = if (isInfo) ServerHelper.getUserInfo(it) else null
                            userList.add(UserItem(name, userInfo ?: UserInfo()))
                        }
                    }
                }
            }
        }
        addUser(serverPathProvider.getPathFile(PathProvider.Folder.USER), true)
        addUser(serverPathProvider.getPathFile(PathProvider.Folder.SSH), false)
        userAdapter.updateDataItem(userList)
        adapter = userAdapter

        floatingActionButton.apply {
            visibility = View.VISIBLE
            setImageResource(R.drawable.icon_user_add)
            setOnClickListener { GuiHelper.startComponentActivity(it.context, UserEditFragment::class.java
                    , arguments.apply { remove(GuiHelper.argumentUserItem) }
                    , getString(R.string.new_user)) }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN) fun event(event:RemoveUserEvent) {
        val a = adapter
        if (a != null) {
            val index = (0..a.getDataItemSize() - 1).indexOfFirst { a.getDataItem(it).id == event.id }
            if (index >= 0) a.removeDataItem(index)
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN) fun event(event:AddUserEvent) {
        adapter?.appendDataItem(event.item)
    }

    @Subscribe(threadMode = ThreadMode.MAIN) fun event(event:ModifyUserEvent) {
        val a = adapter
        if (a != null) {
            val index = (0..a.getDataItemSize() - 1).indexOfFirst { a.getDataItem(it).id == event.item.id }
            if (index >= 0) a.setDataItem(index, event.item)
        }
    }
}

