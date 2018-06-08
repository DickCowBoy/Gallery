/*
 * Copyright (C) 2016, TP-LINK TECHNOLOGIES CO., LTD.
 *
 * AutoWeightLayout.java
 *
 * Description
 *
 * Author huwei
 *
 * Ver 1.0, 16-10-8, huwei, Create file
 */
package com.tplink.gallery.view;

import android.content.Context;
import android.support.v7.widget.ViewUtils;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

/**
 * 横向自动平均分配子view的控件,默认垂直居中
 */
public class AutoWeightLayout extends ViewGroup {

    public static final String TAG = "AutoWeightLayout";

    public AutoWeightLayout(Context context) {
        super(context);
    }

    public AutoWeightLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public AutoWeightLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public AutoWeightLayout(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        int maxMeasureHeight = 0;
        for (int i = 0; i < getChildCount(); i++) {
            View child = getChildAt(i);
            if (child != null && child.getVisibility() != GONE) {
                measureChild(child, widthMeasureSpec, heightMeasureSpec);
                if (child.getMeasuredHeight() > maxMeasureHeight) {
                    maxMeasureHeight = child.getMeasuredHeight();
                }
            }
        }

        setMeasuredDimension(getMeasuredWidth(), maxMeasureHeight + getPaddingTop() + getPaddingBottom());
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        int w = r - l - getPaddingLeft() - getPaddingRight();
        int height = b - t;

        int visiableCount = 0;
        for (int i = 0; i < getChildCount(); i++) {
            View child = getChildAt(i);
            if (child != null && child.getVisibility() != GONE) {
                w -= child.getMeasuredWidth();
                visiableCount++;
            }
        }

        if (visiableCount == 0) return;
        int space = 0;
        //放置子控件
        if (visiableCount > 1) {
            space = w / (visiableCount - 1);
        }

        if(ViewUtils.isLayoutRtl(this)) {
            int left = getPaddingLeft();
            int top = getPaddingTop();
            for (int i = getChildCount() - 1; i >=0; i--) {
                View child = getChildAt(i);
                if (child != null && child.getVisibility() != GONE) {
                    top = getPaddingTop();
                    //当只有一个View可见时，则该child便是该view，此时该view显示在右边
                    if(visiableCount == 1){
                        left = (getMeasuredWidth() - child.getMeasuredWidth()) / 2;
                        child.layout(left, top, left + child.getMeasuredWidth(), top + child.getMeasuredHeight());
                        break;
                    } else {
                        child.layout(left, top, left + child.getMeasuredWidth(), top + child.getMeasuredHeight());
                        left = left + space + child.getMeasuredWidth();
                    }

                }
            }
        }else{
            int left = getPaddingLeft();
            int top = getPaddingTop();
            for (int i = 0; i < getChildCount(); i++) {
                View child = getChildAt(i);
                if (child != null && child.getVisibility() != GONE) {
                    top = getPaddingTop();
                    //当只有一个View可见时，则该child便是该view，此时该view显示在右边
                    if(visiableCount == 1){
                        left = (getMeasuredWidth() - child.getMeasuredWidth()) / 2;
                        child.layout(left, top, left + child.getMeasuredWidth(), top + child.getMeasuredHeight());
                        break;
                    } else {
                        child.layout(left, top, left + child.getMeasuredWidth(), top + child.getMeasuredHeight());
                        left = left + space + child.getMeasuredWidth();
                    }

                }
            }
        }
    }
}