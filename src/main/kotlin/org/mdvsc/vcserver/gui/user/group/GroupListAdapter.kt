package org.mdvsc.vcserver.gui.user.group

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import org.mdvsc.vcserver.databinding.ItemGroupBinding
import org.mdvsc.vcserver.gui.common.BaseListAdapter
import javax.inject.Inject

/**
 * @author haniklz
 * @since 16/4/6.
 */
class GroupListAdapter(val arguments:Bundle) : BaseListAdapter<GroupListViewHolder, GroupItem, Any, Any>() {

    @Inject lateinit var cotext: Context
    @Inject lateinit var inflater:LayoutInflater

    override fun onBindDataItemViewHolder(holderList: GroupListViewHolder, data: GroupItem, dataItemPosition: Int, itemPosition: Int) = holderList.update(data)
    override fun onCreateDataItemViewHolder(parent: ViewGroup?) = GroupListViewHolder(arguments, ItemGroupBinding.inflate(inflater, parent, false))

}

