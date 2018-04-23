

/*
 * Copyright (C), 2015, TP-LINK TECHNOLOGIES CO., LTD.
 *
 * LoadingView.java
 *
 * Author YuLibo
 *
 * Ver 1.0, 2015-8-5, YuLibo, Create file
 */
package com.tplink.gallery.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.ViewSwitcher;

import com.tplink.gallery.gallery.R;

public class LoadingView extends ViewSwitcher {
    private final static String TAG = "LoadingView";
    private InnerView mLoadingView;
    private boolean mDisloading;

    public LoadingView(Context context) {
        super(context, null);
    }

    public LoadingView(Context context, AttributeSet attrs) {
        super(context, attrs);
        if (!isInEditMode()) {
            mLoadingView = new InnerView(getContext());
            addView(mLoadingView);
        }
    }

    @Override
    public void addView(View child, int index, ViewGroup.LayoutParams params) {
        super.addView(child, index, params);

        if (getChildCount() == 2) {
            setLoading(mDisloading);
        }
    }

    public boolean isLoading() {
        return getCurrentView() == mLoadingView;
    }

    public void setLoading(boolean state) {
        if ((getCurrentView() == mLoadingView) != state) {
            showNext();
        }
    }

    public void setText(int stringResourceId) {
        mLoadingView.mLoadingText.setText(stringResourceId);
    }

    public void setText(String loadingText) {
        mLoadingView.mLoadingText.setText(loadingText);
    }

    public static class InnerView extends LinearLayout {
        private final TextView mLoadingText;

        public InnerView(Context context) {
            this(context, null);
        }

        public InnerView(Context context, AttributeSet attrs) {
            super(context, attrs);
            setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT));
            setOrientation(HORIZONTAL);

            LayoutInflater.from(context).inflate(R.layout.photo_layout_loadingview,
                    this, true);
            mLoadingText = (TextView) findViewById(R.id.tv_loading_view_msg);
        }
    }

}
