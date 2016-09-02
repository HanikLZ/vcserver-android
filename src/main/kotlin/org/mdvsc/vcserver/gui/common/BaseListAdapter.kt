package org.mdvsc.vcserver.gui.common

import android.support.v7.widget.RecyclerView
import android.view.ViewGroup
import org.mdvsc.vcserver.R
import java.util.*

/**
 * @author haniklz
 * @version 1.0.0
 * @since 15/12/19
 */
abstract class BaseListAdapter<VH : RecyclerView.ViewHolder, E, HE, FE> : RecyclerView.Adapter<VH>() {

    private val dataItemList = ArrayList<E>()
    private val headerList = ArrayList<HE>()
    private val footerList = ArrayList<FE>()
    var transactionStarted = false
        set(value) {
            if (!value && field) {
                notifyDataSetChanged()
            }
            field = value
        }

    fun getDataItem(dataItemPosition: Int) = dataItemList[dataItemPosition]
    fun getHeader(headerPosition: Int) = headerList[headerPosition]
    fun getFooter(footerPosition: Int) = footerList[footerPosition]
    fun hasHeader(data: HE) = headerList.contains(data)
    fun hasFooter(data: FE) = footerList.contains(data)
    fun hasData(data: E) = dataItemList.contains(data)
    fun insertDataItem(itemList: List<E>) = insertDataItem(0, itemList)
    fun insertDataItem(dataItem: E) = insertDataItem(0, dataItem)
    fun findDataItemPosition(dataItem: E) = dataItemList.indexOf(dataItem)
    fun getDataItemSize() = dataItemList.size
    fun getHeaderSize() = headerList.size
    fun getFooterSize() = footerList.size
    fun removeDataItem(dataItem: E) = removeDataItem(dataItemList.indexOf(dataItem))

    open protected fun onCreateHeaderViewHolder(parent: ViewGroup?) = onCreateDataItemViewHolder(parent)
    open protected fun onCreateFooterViewHolder(parent: ViewGroup?) = onCreateDataItemViewHolder(parent)
    open protected fun onBindHeaderViewHolder(holder: VH, data: HE, headerPosition: Int, itemPosition: Int) { }
    open protected fun onBindFooterViewHolder(holder: VH, data: FE, footerPosition: Int, itemPosition: Int) { }
    open protected fun onBindDataItemViewHolder(holderList: VH, data: E, dataItemPosition: Int, itemPosition: Int) { }

    protected abstract fun onCreateDataItemViewHolder(parent: ViewGroup?): VH

    override fun getItemCount() = headerList.size + dataItemList.size + footerList.size



    fun addFooter(data: FE) {
        footerList.add(data)
        if (!transactionStarted) {
            notifyItemInserted(itemCount - 1)
        }
    }

    fun addFooters(vararg datas: FE) {
        val start = itemCount
        footerList.addAll(datas)
        if (!transactionStarted) {
            notifyItemRangeInserted(start, datas.size)
        }
    }

    fun setFooter(position: Int, data: FE) {
        if (position >= 0 && position < getFooterSize()) {
            footerList.set(position, data)
            if (!transactionStarted) {
                notifyItemChanged(getHeaderSize() + getDataItemSize() + position)
            }
        }
    }

    fun removeFooter(data: FE) {
        val index = footerList.indexOf(data)
        if (index >= 0) {
            footerList.removeAt(index)
            if (!transactionStarted) {
                notifyItemRemoved(getDataItemSize() + getFooterSize() + index)
            }
        }
    }

    fun clearFooter() {
        val size = footerList.size
        footerList.clear()
        if (size > 0 && !transactionStarted) {
            notifyItemRangeRemoved(getHeaderSize() + getDataItemSize(), size)
        }
    }

    fun addHeader(data: HE) {
        headerList.add(data)
        if (!transactionStarted) {
            notifyItemInserted(getHeaderSize() - 1)
        }
    }

    fun addHeaders(vararg datas: HE) {
        val start = getHeaderSize()
        headerList.addAll(datas)
        if (!transactionStarted) {
            notifyItemRangeInserted(start, datas.size)
        }
    }

    fun setHeader(position: Int, data: HE) {
        if (position >= 0 && position < getHeaderSize()) {
            headerList[position] = data
            if (!transactionStarted) {
                notifyItemChanged(position);
            }
        }
    }

    fun clearHeader() {
        val size = headerList.size
        headerList.clear()
        if (size > 0 && !transactionStarted) {
            notifyItemRangeRemoved(0, size)
        }
    }

    fun removeHeader(data: HE) {
        val index = headerList.indexOf(data)
        if (index >= 0) {
            headerList.removeAt(index)
            if (!transactionStarted) {
                notifyItemRemoved(index)
            }
        }
    }

    fun updateDataItem(itemList: List<E>?) {
        dataItemList.clear()
        if (itemList != null) {
            doUpdateDataItem(itemList)
        }
    }

    protected fun doUpdateDataItem(itemList: List<E>) {
        dataItemList.addAll(itemList)
        if (!transactionStarted) {
            notifyDataSetChanged()
        }
    }

    fun clearDataItem() {
        var size = dataItemList.size;
        dataItemList.clear();
        if (size > 0 && !transactionStarted) {
            notifyItemRangeRemoved(getHeaderSize(), size)
        }
    }

    fun insertDataItem(position: Int, itemList: List<E>) {
        dataItemList.addAll(position, itemList)
        if (!transactionStarted) {
            notifyItemRangeInserted(getHeaderSize(), itemList.size)
        }
    }


    fun appendDataItem(itemList: List<E>) {
        val size = getHeaderSize() + getDataItemSize()
        dataItemList.addAll(itemList)
        if (!transactionStarted) {
            notifyItemRangeInserted(size, itemList.size)
        }
    }

    fun insertDataItem(position: Int, dataItem: E) {
        dataItemList.add(position, dataItem)
        if (!transactionStarted) {
            notifyItemInserted(getHeaderSize());
        }
    }

    fun appendDataItem(dataItem: E) {
        dataItemList.add(dataItem)
        if (!transactionStarted) {
            notifyItemInserted(getHeaderSize() + getDataItemSize())
        }
    }

    fun setDataItem(i: Int, dataItem: E) {
        if (i >= 0 && i < dataItemList.size) {
            dataItemList[i] = dataItem;
            if (!transactionStarted) {
                notifyItemChanged(getHeaderSize() + i)
            }
        }
    }


    fun removeDataItem(index: Int) {
        if (index >= 0 && index < dataItemList.size) {
            dataItemList.removeAt(index);
            if (!transactionStarted) {
                notifyItemRemoved(getHeaderSize() + index);
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int) = when (viewType) {
        R.id.list_item_type_data -> onCreateDataItemViewHolder(parent)
        R.id.list_item_type_header -> onCreateHeaderViewHolder(parent)
        R.id.list_item_type_footer -> onCreateFooterViewHolder(parent)
        else -> onCreateDataItemViewHolder(parent)
    }

    override fun getItemViewType(position: Int) = if (position < headerList.size)
        R.id.list_item_type_header
    else if (position > headerList.size + dataItemList.size)
        R.id.list_item_type_footer
    else R.id.list_item_type_data


    override final fun onBindViewHolder(holder: VH, position: Int) {
        var np = position;
        if (np < getHeaderSize()) {
            onBindHeaderViewHolder(holder, getHeader(np), np, position)
        } else {
            np -= getHeaderSize()
            if (np < getDataItemSize()) {
                onBindDataItemViewHolder(holder, getDataItem(np), np, position)
            } else {
                np -= getDataItemSize()
                if (np < getFooterSize()) {
                    onBindFooterViewHolder(holder, getFooter(np), np, position)
                }
            }
        }
    }
}


