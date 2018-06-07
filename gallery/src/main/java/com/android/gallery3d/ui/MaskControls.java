/*
 * Copyright (C) 2016, TP-LINK TECHNOLOGIES CO., LTD.
 *
 * MaskControls.java
 *
 * Description
 *
 * Author huwei
 *
 * Ver 1.0, 2016-10-11, huwei, Create file
 */
package com.android.gallery3d.ui;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.Interpolator;
import android.widget.RelativeLayout;

import com.tplink.gallery.R;

public class MaskControls {
    private ViewGroup mParentLayout;
    private View mMaskTopView, mMaskBottomView;

    public MaskControls(Context context, RelativeLayout layout) {
        mParentLayout = layout;

        int maskHeight = context.getResources().getDimensionPixelSize(R.dimen.mask_height);
        mMaskTopView = new View(context);
        mMaskTopView.setBackground(context.getResources().getDrawable(R.drawable.mask_top_photopage));
        RelativeLayout.LayoutParams topLayoutParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, maskHeight);
        layout.addView(mMaskTopView, topLayoutParams);

        mMaskBottomView = new View(context);
        mMaskBottomView.setBackground(context.getResources().getDrawable(R.drawable.mask_bottom_photopage));
        RelativeLayout.LayoutParams bottomLayoutParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, maskHeight);
        bottomLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE);
        layout.addView(mMaskBottomView, bottomLayoutParams);

        int duration = 200;
        Interpolator interpolator = new AccelerateInterpolator();
        Animation anim = new AlphaAnimation(0f, 1f);
        anim.setInterpolator(interpolator);
        anim.setDuration(duration);
        mMaskTopView.startAnimation(anim);
        anim = new AlphaAnimation(0f, 1f);
        anim.setDuration(duration);
        anim.setInterpolator(interpolator);
        mMaskBottomView.startAnimation(anim);
    }

    public void cleanup() {
        mParentLayout.removeView(mMaskTopView);
        mParentLayout.removeView(mMaskBottomView);
    }

    public void hide() {
        mMaskTopView.setVisibility(View.GONE);
        mMaskBottomView.setVisibility(View.GONE);
    }

    public void show() {
        mMaskTopView.setVisibility(View.VISIBLE);
        mMaskBottomView.setVisibility(View.VISIBLE);
    }

    public void hideBottom() {
        mMaskBottomView.setVisibility(View.GONE);
    }

    public void showBottom() {
        mMaskBottomView.setVisibility(View.VISIBLE);
    }
}
