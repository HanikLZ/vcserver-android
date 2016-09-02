package org.mdvsc.vcserver.gui.repo

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import org.mdvsc.vcserver.databinding.ItemRepoBinding
import org.mdvsc.vcserver.gui.common.BaseListAdapter
import javax.inject.Inject

/**
 * @author haniklz
 * @since 16/4/6.
 */
class RepoListAdapter(val arguments:Bundle) : BaseListAdapter<RepoListViewHolder, RepoItem, Any, Any>() {

    @Inject lateinit var cotext: Context
    @Inject lateinit var inflater: LayoutInflater

    var serverUrl:String = ""
        set(value) {
           if (value != field) {
               field = value
               notifyDataSetChanged()
           }
        }

    override fun onBindDataItemViewHolder(holderList: RepoListViewHolder, data: RepoItem, dataItemPosition: Int, itemPosition: Int) = holderList.update(serverUrl, data)
    override fun onCreateDataItemViewHolder(parent: ViewGroup?) = RepoListViewHolder(arguments, ItemRepoBinding.inflate(inflater, parent, false))

}

