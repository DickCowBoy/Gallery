package com.tplink.gallery.ui;

import android.support.v7.widget.RecyclerView;
import android.util.Log;

public class PagerSnapHelper extends android.support.v7.widget.PagerSnapHelper {

    private int lastPos = -1;
    PageListener pageListener;

    @Override
    public int findTargetSnapPosition(RecyclerView.LayoutManager layoutManager, int velocityX, int velocityY) {
        int targetSnapPosition = super.findTargetSnapPosition(layoutManager, velocityX, velocityY);
        if (lastPos != targetSnapPosition) {
            if (pageListener != null) {
                pageListener.onPageChanged(targetSnapPosition);
                lastPos = targetSnapPosition;
                Log.e("LJL", "findTargetSnapPosition: " + targetSnapPosition );
            }
        }
        return targetSnapPosition;
    }

    public interface PageListener {
        void onPageChanged(int pos);
    }

    public void setPageListener(PageListener pageListener) {
        this.pageListener = pageListener;
    }
}
