package org.mdvsc.vcserver.gui.common

import android.os.Bundle
import android.support.design.widget.FloatingActionButton
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import org.mdvsc.vcserver.databinding.FragmentRecyclerBinding

/**
 * @author haniklz
 * @version 1.0.0
 * @since 15/12/19
 */
abstract class BaseListFragment<Adapter: RecyclerView.Adapter<*>>: BaseFragment() {

    protected lateinit var recyclerView:RecyclerView
    protected lateinit var floatingActionButton:FloatingActionButton

    protected var adapter:Adapter? = null
        set(value) {
            if (value != field) {
                field = value
                onAdapterChange()
                recyclerView.adapter = field
            }
        }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val binding = FragmentRecyclerBinding.inflate(inflater, container, false)
        binding.vm = this
        recyclerView = binding.recyclerView
        floatingActionButton = binding.floatingActionButton
        return binding.root
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setup()
    }

    open protected fun onAdapterChange() {}

    open protected fun setup() = with(recyclerView) {
        layoutManager = LinearLayoutManager(context)
        val decoration = DefaultItemDecoration()
        with(decoration) {
            hasTopSpace = true
            hasLeftSpace = true
            hasRightSpace = true
            hasBottomSpace = true
        }
        addItemDecoration(decoration)
    }
}

