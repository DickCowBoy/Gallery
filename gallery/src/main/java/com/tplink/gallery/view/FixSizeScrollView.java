/*
 * Copyright (C) 2018, TP-LINK TECHNOLOGIES CO., LTD.
 *
 * FixSizeScrollView.java
 *
 * Description
 *
 * Author LinJl
 *
 * Ver 1.0, 18-01-25, LinJl, Create file
 */
package com.tplink.view;

import android.content.Context;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.ScrollView;

import com.tplink.gallery.R;

public class FixSizeScrollView extends ScrollView{
    private int marginTop;
    public FixSizeScrollView(Context context) {
        this(context, null);
    }

    public FixSizeScrollView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public FixSizeScrollView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        marginTop = getResources().getDimensionPixelOffset(R.dimen.detail_margin_top);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        View childAt = getChildAt(0);
        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        if (childAt.getMeasuredHeight() > (displayMetrics.heightPixels - marginTop)) {
            setMeasuredDimension(MeasureSpec.makeMeasureSpec(displayMetrics.widthPixels, MeasureSpec.EXACTLY)
            , MeasureSpec.makeMeasureSpec(displayMetrics.heightPixels - marginTop, MeasureSpec.EXACTLY));
        } else {
            setMeasuredDimension(MeasureSpec.makeMeasureSpec(displayMetrics.widthPixels, MeasureSpec.EXACTLY)
                    , MeasureSpec.makeMeasureSpec(childAt.getMeasuredHeight(), MeasureSpec.EXACTLY));
        }
    }
}
