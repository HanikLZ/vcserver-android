package org.mdvsc.vcserver.gui.user

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import org.mdvsc.vcserver.databinding.ItemUserBinding
import org.mdvsc.vcserver.gui.common.BaseListAdapter
import javax.inject.Inject

/**
 * @author haniklz
 * @since 16/4/6.
 */
class UserListAdapter(val arguments:Bundle) : BaseListAdapter<UserListViewHolder, UserItem, Any, Any>() {

    @Inject lateinit var cotext: Context
    @Inject lateinit var inflater:LayoutInflater

    override fun onBindDataItemViewHolder(holderList: UserListViewHolder, data: UserItem, dataItemPosition: Int, itemPosition: Int) = holderList.update(data)

    override fun onCreateDataItemViewHolder(parent: ViewGroup?) = UserListViewHolder(arguments, ItemUserBinding.inflate(inflater, parent, false))

}

