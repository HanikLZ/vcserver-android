package org.mdvsc.vcserver.gui.common;

import android.graphics.Rect
import android.support.v7.widget.RecyclerView;
import android.view.View;
import org.mdvsc.vcserver.R

/**
 * @author haniklz
 * @version 1.0.0
 * @since 15/12/7
 */
class DefaultItemDecoration: RecyclerView.ItemDecoration() {

    var hasSplitSpace = true
    var hasTopSpace = false
    var hasBottomSpace = false
    var hasLeftSpace = false
    var hasRightSpace = false

    override fun getItemOffsets(outRect: Rect?, view: View?, parent: RecyclerView?, state: RecyclerView.State?) {
        super.getItemOffsets(outRect, view, parent, state)
        if (outRect != null) {
            val size = view?.resources?.getDimensionPixelSize(R.dimen.list_item_space) ?: 0
            with(outRect) {
                if (hasLeftSpace) left = size
                if (hasRightSpace) right = size
                val position = parent?.getChildAdapterPosition(view)?:-1
                if (hasTopSpace && position == 0) top = size
                if (hasBottomSpace && position == state?.itemCount?:0 - 1) bottom = size
                if (hasSplitSpace && position > 0 && position < state?.itemCount?:0) top = size
            }
        }
    }
}

