package com.tplink.gallery.view;

/*
 * Copyright (C) 2018, TP-LINK TECHNOLOGIES CO., LTD.
 *
 * AutoFitToolBar.java
 *
 * 弹出框工具类
 *
 * 适配全面屏幕及横屏
 *
 * Ver 1.0, 18-03-14, LinJl, Create file
 */

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v7.widget.Toolbar;
import android.util.AttributeSet;
import android.view.View;
import android.view.WindowInsets;
import android.widget.RelativeLayout;

public class AutoFitToolBar extends Toolbar {

    private OnPaddingListener onPaddingListener;

    public AutoFitToolBar(Context context) {
        this(context, null);
    }

    public AutoFitToolBar(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public AutoFitToolBar(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setOnApplyWindowInsetsListener(new View.OnApplyWindowInsetsListener() {
            @Override
            public WindowInsets onApplyWindowInsets(View view, WindowInsets windowInsets) {
                view.setPadding(windowInsets.getSystemWindowInsetLeft(),
                        0,
                        windowInsets.getSystemWindowInsetRight(), 0);
                RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) view.getLayoutParams();
                layoutParams.topMargin = windowInsets.getSystemWindowInsetTop();
                if (onPaddingListener != null) {
                    onPaddingListener.onPadding(
                            windowInsets.getSystemWindowInsetLeft(),
                            windowInsets.getSystemWindowInsetTop(),
                            windowInsets.getSystemWindowInsetRight(),
                            windowInsets.getSystemWindowInsetBottom());
                }
                return windowInsets.consumeStableInsets();
            }
        });
    }

    public interface OnPaddingListener {
        void onPadding(int left, int top, int right, int bottom);
    }

    public void setOnPaddingListener(OnPaddingListener onPaddingListener) {
        this.onPaddingListener = onPaddingListener;
    }
}
