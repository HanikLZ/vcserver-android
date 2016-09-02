package org.mdvsc.vcserver.gui.repo

import android.databinding.ObservableField
import android.os.Bundle
import android.support.v7.widget.RecyclerView
import org.mdvsc.vcserver.databinding.ItemRepoBinding
import org.mdvsc.vcserver.gui.GuiHelper

/**
 * @author haniklz
 * @since 16/4/6.
 */
class RepoListViewHolder(val arguments:Bundle, binding:ItemRepoBinding): RecyclerView.ViewHolder(binding.root) {

    val name = ObservableField<CharSequence>()
    val url = ObservableField<CharSequence>()
    val description = ObservableField<CharSequence>()
    lateinit var item:RepoItem

    init {
        binding.vm = this
        binding.root.setOnClickListener {
            GuiHelper.startComponentActivity(it.context, RepoEditFragment::class.java
                    , arguments.apply { putSerializable(GuiHelper.argumentRepoItem, item) }
                    , item.info.name ?: item.path)
        }
    }

    fun update(serverUrl:String, item: RepoItem) {
        this.item = item
        name.set(if (item.info.name.isNullOrEmpty()) item.path else item.info.name)
        url.set(if (serverUrl.isNullOrBlank()) item.path else "ssh://[user]@$serverUrl/${item.path}")
        description.set(item.info.description)
    }
}

