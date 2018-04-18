/*
 * Copyright (C) 2017, TP-LINK TECHNOLOGIES CO., LTD.
 *
 * com.tplink.filemanager.widget
 *
 * Description.
 *
 * Author tanminghui
 *
 * Ver 1.0, 3/21/17, tanminghui, Create file
 */

package com.tplink.view;

import android.graphics.Rect;
import android.support.v7.widget.RecyclerView;
import android.view.View;

/**
 * Empty space for each item in RecyclerView.
 */
public class SpaceItemDecoration extends RecyclerView.ItemDecoration {

    private final int mSpanCount;
    private final int mSpacing;
    private final boolean mIncludeEdge;
    private int lastColumnMargin = 0;

    public SpaceItemDecoration(int spanCount, int spacing, boolean includeEdge) {
        this.mSpanCount = spanCount;
        this.mSpacing = spacing;
        this.mIncludeEdge = includeEdge;
    }

    public void setLastColumnMargin(int lastColumnMargin) {
        this.lastColumnMargin = lastColumnMargin;
    }

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State
            state) {
        int position = parent.getChildAdapterPosition(view); // item position
        int column = position % mSpanCount; // item column

        if (mIncludeEdge) {
            // mSpacing - column * ((1f / spanCount) * mSpacing)
            outRect.left = mSpacing - column * mSpacing / mSpanCount;
            // (column + 1) * ((1f / spanCount) * mSpacing)
            outRect.right = (column + 1) * mSpacing / mSpanCount;

            if (position < mSpanCount) { // top edge
                outRect.top = mSpacing;
            }
            outRect.bottom = mSpacing; // item bottom
        } else {
            // column * ((1f / spanCount) * mSpacing)
            outRect.left = column * mSpacing / mSpanCount;
            // mSpacing - (column + 1) * ((1f /    spanCount) * mSpacing)
            outRect.right = mSpacing - (column + 1) * mSpacing / mSpanCount;
            if (position >= mSpanCount) {
                outRect.top = mSpacing; // item top
            }
            int firstNeed = parent.getAdapter().getItemCount() / mSpanCount * mSpanCount;
            if (firstNeed == parent.getAdapter().getItemCount()) {
                firstNeed = parent.getAdapter().getItemCount() - mSpanCount;
            }
            if (position >= firstNeed) {
                outRect.bottom = lastColumnMargin + mSpacing;
            }
        }
    }
}
