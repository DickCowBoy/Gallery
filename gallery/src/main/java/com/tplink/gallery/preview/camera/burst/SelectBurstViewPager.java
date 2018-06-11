package com.tplink.gallery.preview.camera.burst;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;

public class SelectBurstViewPager extends ViewPager {

    private int preX = 0;

    public SelectBurstViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public SelectBurstViewPager(Context context) {
        super(context);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            preX = (int) event.getX();
        } else {
            if (Math.abs(event.getX() - preX) > 5) {
                return true;
            } else {
                preX = (int) event.getX();
            }
        }
        return super.onInterceptTouchEvent(event);
    }

}
