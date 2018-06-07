/*
 * Copyright (C) 2012 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.gallery3d.ui;

import android.content.Context;
import android.view.MotionEvent;

import com.android.gallery3d.glrenderer.GLCanvas;
import com.android.gallery3d.glrenderer.StringTexture;
import com.android.gallery3d.glrenderer.UndoBgTexture;
import com.android.gallery3d.util.GalleryUtils;
import com.android.gallery3d.util.Utils;
import com.tplink.gallery.R;


public class UndoBarView extends GLView {
    @SuppressWarnings("unused")
    private static final String TAG = "UndoBarView";

    private static final int WHITE = 0xFFFFFFFF;
    private static final int GRAY = 0xFFAAAAAA;

    private final StringTexture mUndoText;

    private final int mBarHeight;
    private final int mBarMargin;
    private final int mUndoTextMargin;
    private final int mIconSize;
    private final int mIconMargin;

    private final int mSeparatorRightMargin;


    private OnClickListener mOnClickListener;
    private boolean mDownOnButton;
    private UndoBgTexture mUndoBg;

    // This is the layout of UndoBarView. The unit is dp.
    //
    //    +-+----+----------------+-+--+----+-+------+--+-+
    // 48 | |    | Deleted        | |  | <- | | UNDO |  | |
    //    +-+----+----------------+-+--+----+-+------+--+-+
    //     4  16                   1 12  32  8        16 4
    public UndoBarView(Context context) {
        mBarHeight = GalleryUtils.dpToPixel(40);
        mBarMargin = GalleryUtils.dpToPixel(4);
        mUndoTextMargin = GalleryUtils.dpToPixel(16);
        mIconMargin = GalleryUtils.dpToPixel(8);
        mIconSize = GalleryUtils.dpToPixel(32);
        mSeparatorRightMargin = GalleryUtils.dpToPixel(12);


        mUndoText = StringTexture.newInstance(context.getString(R.string.undo),
                GalleryUtils.dpToPixel(14), WHITE, 0, true);
    }

    public void setOnClickListener(OnClickListener listener) {
        mOnClickListener = listener;
    }

    @Override
    protected void onMeasure(int widthSpec, int heightSpec) {
        setMeasuredSize(0 /* unused */, mBarHeight);
    }

    @Override
    protected void render(GLCanvas canvas) {
        super.render(canvas);
        advanceAnimation();
        if (mUndoBg == null) {
            mUndoBg = new UndoBgTexture(getWidth(), getHeight(), GalleryUtils.dpToPixel(14));
        }
        canvas.save(GLCanvas.SAVE_FLAG_ALPHA);
        canvas.multiplyAlpha(mAlpha);

        int w = getWidth();
        int h = getHeight();
        mUndoBg.draw(canvas, 0, 0, w, mBarHeight);

        int x = w - mBarMargin;
        int y;

        x = (w - mUndoText.getWidth()) / 2;
        y = (mBarHeight - mUndoText.getHeight()) / 2;
        mUndoText.draw(canvas, x, y);

        canvas.restore();
    }

    @Override
    protected boolean onTouch(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mDownOnButton = inUndoButton(event);
                break;
            case MotionEvent.ACTION_UP:
                if (mDownOnButton) {
                    if (mOnClickListener != null && inUndoButton(event)) {
                        mOnClickListener.onClick(this);
                    }
                    mDownOnButton = false;
                }
                break;
            case MotionEvent.ACTION_CANCEL:
                mDownOnButton = false;
                break;
        }
        return true;
    }

    // Check if the event is on the right of the separator
    private boolean inUndoButton(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();
        int w = getWidth();
        int h = getHeight();
        return (x >= 0 && x < w && y >= 0 && y < h);
    }

    ////////////////////////////////////////////////////////////////////////////
    //  Alpha Animation
    ////////////////////////////////////////////////////////////////////////////

    private static final long NO_ANIMATION = -1;
    private static long ANIM_TIME = 200;
    private long mAnimationStartTime = NO_ANIMATION;
    private float mFromAlpha, mToAlpha;
    private float mAlpha;

    private static float getTargetAlpha(int visibility) {
        return (visibility == VISIBLE) ? 1f : 0f;
    }

    @Override
    public void setVisibility(int visibility) {
        mAlpha = getTargetAlpha(visibility);
        mAnimationStartTime = NO_ANIMATION;
        super.setVisibility(visibility);
        invalidate();
    }

    public void animateVisibility(int visibility) {
        float target = getTargetAlpha(visibility);
        if (mAnimationStartTime == NO_ANIMATION && mAlpha == target) return;
        if (mAnimationStartTime != NO_ANIMATION && mToAlpha == target) return;

        mFromAlpha = mAlpha;
        mToAlpha = target;
        mAnimationStartTime = AnimationTime.startTime();

        super.setVisibility(VISIBLE);
        invalidate();
    }

    private void advanceAnimation() {
        if (mAnimationStartTime == NO_ANIMATION) return;

        float delta = (float) (AnimationTime.get() - mAnimationStartTime) /
                ANIM_TIME;
        mAlpha = mFromAlpha + ((mToAlpha > mFromAlpha) ? delta : -delta);
        mAlpha = Utils.clamp(mAlpha, 0f, 1f);

        if (mAlpha == mToAlpha) {
            mAnimationStartTime = NO_ANIMATION;
            if (mAlpha == 0) {
                super.setVisibility(INVISIBLE);
            }
        }
        invalidate();
    }
}
