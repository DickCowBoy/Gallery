package com.tplink.gallery.ui;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.tplink.gallery.R;

public class BurstPhotoCountControls {
    private final String TAG = "BurstPhotoCountControls";

    private ViewGroup mParentLayout;
    private ViewGroup mContainer;
    private TextView mCountTextView;
    private boolean mVisible;
    private Context mContext;

    private Animation mContainerAnimIn = new AlphaAnimation(0f, 1f);
    private Animation mContainerAnimOut = new AlphaAnimation(1f, 0f);
    private static final int CONTAINER_ANIM_DURATION_MS = 200;

    private static final int CONTROL_ANIM_DURATION_MS = 150;
    private static Animation getControlAnimForVisibility(boolean visible) {
        Animation anim = visible ? new AlphaAnimation(0f, 1f)
                : new AlphaAnimation(1f, 0f);
        anim.setDuration(CONTROL_ANIM_DURATION_MS);
        return anim;
    }

    public BurstPhotoCountControls(Context context, RelativeLayout layout) {
        mParentLayout = layout;
        mContext = context;

        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mContainer = (ViewGroup) inflater
                .inflate(R.layout.photo_burst_count, mParentLayout, false);
        mCountTextView = (TextView) mContainer.findViewById(R.id.tv_burst_count);
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        mParentLayout.addView(mContainer, params);

        mContainerAnimIn.setDuration(CONTAINER_ANIM_DURATION_MS);
        mContainerAnimOut.setDuration(CONTAINER_ANIM_DURATION_MS);
    }

    public synchronized void refresh(int burstCount) {
        boolean visible = true;
        if (burstCount <= 0) {
            // 个数为0，直接不显示
            visible = false;
        }
        Log.d(TAG, "refresh delegate is " + visible + " current is " + mVisible);
        boolean containerVisibilityChanged = (visible != mVisible);
        if (containerVisibilityChanged) {
            if (visible) {
                mCountTextView.setText(mContext.getResources().getQuantityString(
                        R.plurals.burst_photos_num, burstCount, burstCount));
                show();
            } else {
                hide();
            }
            mVisible = visible;
        } else {
            // 只需要更新个数即可
            if (mVisible) {
                mCountTextView.setText(mContext.getResources().getQuantityString(
                        R.plurals.burst_photos_num, burstCount, burstCount));
            }
        }
    }

    public void cleanup() {
        mParentLayout.removeView(mContainer);
    }

    private void hide() {
        mContainer.clearAnimation();
        mContainerAnimOut.reset();
        mContainer.startAnimation(mContainerAnimOut);
        mContainer.setVisibility(View.INVISIBLE);
    }

    private void show() {
        mContainer.clearAnimation();
        mContainerAnimIn.reset();
        mContainer.startAnimation(mContainerAnimIn);
        mContainer.setVisibility(View.VISIBLE);
    }
}
