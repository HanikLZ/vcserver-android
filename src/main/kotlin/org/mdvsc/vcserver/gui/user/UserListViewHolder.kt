package org.mdvsc.vcserver.gui.user

import android.databinding.ObservableBoolean
import android.databinding.ObservableField
import android.os.Bundle
import android.support.v7.widget.RecyclerView
import org.mdvsc.vcserver.databinding.ItemUserBinding
import org.mdvsc.vcserver.gui.GuiHelper

/**
 * @author haniklz
 * @since 16/4/6.
 */
class UserListViewHolder(val arguments:Bundle, binding: ItemUserBinding):RecyclerView.ViewHolder(binding.root) {

    val name = ObservableField<CharSequence>()
    val description = ObservableField<CharSequence>()
    val isAdmin = ObservableBoolean()
    val passwordEnabled = ObservableBoolean()

    lateinit var item:UserItem

    init {
        binding.vm = this
        binding.root.setOnClickListener {
            GuiHelper.startComponentActivity(it.context, UserEditFragment::class.java
                    , arguments.apply { putSerializable(GuiHelper.argumentUserItem, item) }
                    , item.info.name?:item.id)
        }
    }

    fun update(item:UserItem) {
        this.item = item
        if (item.info.name.isNullOrEmpty()) {
            name.set(item.id)
        } else {
            name.set("${item.id}(${item.info.name})")
        }
        description.set(item.info.description)
        isAdmin.set(item.info.isAdmin)
        passwordEnabled.set(item.info.enablePasswordAuthentication)
    }

}

