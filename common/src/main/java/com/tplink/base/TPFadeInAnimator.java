/*
 * Copyright (C) 2017, TP-LINK TECHNOLOGIES CO., LTD.
 *
 * com.tplink.filemanager.animation
 *
 * Description.
 *
 * Author tanminghui
 *
 * Ver 1.0, 09/27/2017, tanminghui, Create file
 */

package com.tplink.base;

import android.support.v4.view.ViewCompat;
import android.support.v7.widget.RecyclerView;
import android.view.animation.Interpolator;

public class TPFadeInAnimator extends BaseItemAnimator {

    private int checkBoxId;
    private int containerId;
    @Override
    public int getCheckBoxId() {
        return 0;
    }

    @Override
    public int getContainerId() {
        return 0;
    }

    public TPFadeInAnimator(int checkBoxId, int containerId) {
        this.checkBoxId = checkBoxId;
        this.containerId = containerId;
    }

    public TPFadeInAnimator(Interpolator interpolator) {
        mInterpolator = interpolator;
    }

    @Override
    protected void animateRemoveImpl(final RecyclerView.ViewHolder holder) {
        ViewCompat.animate(holder.itemView)
                .alpha(0)
                .setDuration(ViewHelper.REMOVE_DURATION)
                .setInterpolator(mInterpolator)
                .setListener(new DefaultRemoveVpaListener(holder))
                .setStartDelay(getRemoveDelay(holder))
                .start();
    }

}
