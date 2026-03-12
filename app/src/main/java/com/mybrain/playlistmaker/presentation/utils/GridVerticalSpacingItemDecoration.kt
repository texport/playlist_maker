package com.mybrain.playlistmaker.presentation.utils

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView

class GridVerticalSpacingItemDecoration(
    private val verticalSpacingPx: Int
) : RecyclerView.ItemDecoration() {

    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        val layoutManager = parent.layoutManager as? GridLayoutManager ?: return
        val position = parent.getChildAdapterPosition(view)
        if (position == RecyclerView.NO_POSITION) return

        val spanCount = layoutManager.spanCount
        val row = position / spanCount
        outRect.top = if (row > 0) verticalSpacingPx else 0
    }
}
