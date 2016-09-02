package org.mdvsc.vcserver.gui.user.group

import android.databinding.ObservableField
import android.os.Bundle
import android.support.v7.widget.RecyclerView
import org.mdvsc.vcserver.databinding.ItemGroupBinding
import org.mdvsc.vcserver.gui.GuiHelper

/**
 * @author haniklz
 * @since 16/4/7.
 */
class GroupListViewHolder(val arguments:Bundle, binding: ItemGroupBinding): RecyclerView.ViewHolder(binding.root) {

    val name = ObservableField<CharSequence>()
    val description = ObservableField<CharSequence>()
    lateinit var item:GroupItem

    init {
        binding.vm = this
        binding.root.setOnClickListener {
            GuiHelper.startComponentActivity(it.context, GroupEditFragment::class.java
                    , arguments.apply { putSerializable(GuiHelper.argumentGroupItem, item) }
                    , item.info.name ?: item.id)
        }
    }

    fun update(item:GroupItem) {
        this.item = item
        name.set(if (item.info.name == null) item.id else "${item.id}(${item.info.name})")
        description.set(item.info.description)
    }
}

