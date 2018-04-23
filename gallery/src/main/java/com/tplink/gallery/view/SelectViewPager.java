/*
 * Copyright (C) 2017, TP-LINK TECHNOLOGIES CO., LTD.
 *
 * SelectViewPager.java
 *
 * Description
 *
 * Author LJL
 *
 * Ver 1.0, May 15, 2018, LJL, Create file
 */
package com.tplink.gallery.view;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;

import com.tplink.base.DragSelectTouchHelper;

public class SelectViewPager extends ViewPager implements DragSelectTouchHelper.InterceptController {

    private boolean intercept = true;

    public SelectViewPager(@NonNull Context context) {
        super(context);
    }

    public SelectViewPager(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return this.intercept && super.onTouchEvent(event);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_UP:
                intercept = true;
                break;
        }
        return this.intercept && super.onInterceptTouchEvent(event);
    }

    @Override
    public void setIntercept(boolean intercept) {
        this.intercept = intercept;
    }
}
