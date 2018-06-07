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

package com.android.gallery3d.anim;

import android.graphics.Rect;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;

import com.android.gallery3d.glrenderer.GLCanvas;
import com.android.gallery3d.glrenderer.RawTexture;
import com.android.gallery3d.ui.GLView;
import com.android.gallery3d.ui.TiledScreenNail;

public class StateTransitionAnimation extends Animation {

    public static class Spec {
        public static final Spec OUTGOING;
        public static final Spec INCOMING;
        public static final Spec PHOTO_INCOMING;

        private static final Interpolator DEFAULT_INTERPOLATOR =
                new DecelerateInterpolator();

        public int duration = 200;
        public float backgroundAlphaFrom = 0;
        public float backgroundAlphaTo = 0;
        public float backgroundScaleFrom = 0;
        public float backgroundScaleTo = 0;
        public float contentAlphaFrom = 1;
        public float contentAlphaTo = 1;
        public float contentScaleFrom = 1;
        public float contentScaleTo = 1;
        public float overlayAlphaFrom = 0;
        public float overlayAlphaTo = 0;
        public float overlayScaleFrom = 0;
        public float overlayScaleTo = 0;
        public Interpolator interpolator = DEFAULT_INTERPOLATOR;

        static {
            OUTGOING = new Spec();
            OUTGOING.backgroundAlphaFrom = 0.5f;
            OUTGOING.backgroundAlphaTo = 0f;
            OUTGOING.backgroundScaleFrom = 1f;
            OUTGOING.backgroundScaleTo = 0f;
            OUTGOING.contentAlphaFrom = 0.5f;
            OUTGOING.contentAlphaTo = 1f;
            OUTGOING.contentScaleFrom = 3f;
            OUTGOING.contentScaleTo = 1f;

            INCOMING = new Spec();
            INCOMING.backgroundAlphaFrom = 1f;
            INCOMING.backgroundAlphaTo = 0f;
            INCOMING.backgroundScaleFrom = 1f;
            INCOMING.backgroundScaleTo = 1f;
            INCOMING.overlayAlphaFrom = 0f;
            INCOMING.overlayAlphaTo = 0f;
            INCOMING.overlayScaleFrom = 1f;
            INCOMING.overlayScaleTo = 1f;
            INCOMING.contentAlphaFrom = 1f;
            INCOMING.contentAlphaTo = 1f;
            INCOMING.contentScaleFrom = 0.25f;
            INCOMING.contentScaleTo = 1f;

            PHOTO_INCOMING = INCOMING;
        }

        private static Spec specForTransition(Transition t) {
            switch (t) {
                case Outgoing:
                    return Spec.OUTGOING;
                case Incoming:
                    return Spec.INCOMING;
                case PhotoIncoming:
                    return Spec.PHOTO_INCOMING;
                case None:
                default:
                    return null;
            }
        }
    }

    public static enum Transition { None, Outgoing, Incoming, PhotoIncoming }

    private final Spec mTransitionSpec;
    private float mCurrentContentScale;
    private float mCurrentContentAlpha;
    private float mCurrentBackgroundScale;
    private float mCurrentBackgroundAlpha;
    private float mCurrentOverlayScale;
    private float mCurrentOverlayAlpha;
    private RawTexture mOldScreenTexture;
    private Rect mClickRect;

    public StateTransitionAnimation(Transition t, RawTexture oldScreen, Rect rect) {
        this(Spec.specForTransition(t), oldScreen, rect);
    }

    public StateTransitionAnimation(Spec spec, RawTexture oldScreen, Rect rect) {
        mTransitionSpec = spec != null ? spec : Spec.OUTGOING;
        setDuration(mTransitionSpec.duration);
        setInterpolator(mTransitionSpec.interpolator);
        mOldScreenTexture = oldScreen;
        // 需要开启填充颜色，否则会存在大张图片第一次打开的时候没有放大的动画效果
        TiledScreenNail.enableDrawPlaceholder();
        mClickRect = rect;
    }

    @Override
    public boolean calculate(long currentTimeMillis) {
        boolean retval = super.calculate(currentTimeMillis);
        if (!isActive()) {
            if (mOldScreenTexture != null) {
                mOldScreenTexture.recycle();
                mOldScreenTexture = null;
            }
            TiledScreenNail.enableDrawPlaceholder();
        }
        return retval;
    }

    @Override
    protected void onCalculate(float progress) {
        mCurrentContentScale = mTransitionSpec.contentScaleFrom
                + (mTransitionSpec.contentScaleTo - mTransitionSpec.contentScaleFrom) * progress;
        mCurrentContentAlpha = mTransitionSpec.contentAlphaFrom
                + (mTransitionSpec.contentAlphaTo - mTransitionSpec.contentAlphaFrom) * progress;
        mCurrentBackgroundAlpha = mTransitionSpec.backgroundAlphaFrom
                + (mTransitionSpec.backgroundAlphaTo - mTransitionSpec.backgroundAlphaFrom)
                * progress;
        mCurrentBackgroundScale = mTransitionSpec.backgroundScaleFrom
                + (mTransitionSpec.backgroundScaleTo - mTransitionSpec.backgroundScaleFrom)
                * progress;
        mCurrentOverlayScale = mTransitionSpec.overlayScaleFrom
                + (mTransitionSpec.overlayScaleTo - mTransitionSpec.overlayScaleFrom) * progress;
        mCurrentOverlayAlpha = mTransitionSpec.overlayAlphaFrom
                + (mTransitionSpec.overlayAlphaTo - mTransitionSpec.overlayAlphaFrom) * progress;
    }

    private void applyOldTexture(GLView view, GLCanvas canvas, float alpha, float scale, boolean clear) {
        if (mOldScreenTexture == null)
            return;
        if (clear) canvas.clearBuffer(view.getBackgroundColor());
        canvas.save();
        canvas.setAlpha(alpha);
        int xOffset = view.getWidth() / 2;
        int yOffset = view.getHeight() / 2;
        canvas.translate(xOffset, yOffset);
        canvas.scale(scale, scale, 1);
        mOldScreenTexture.draw(canvas, -xOffset, -yOffset);
        canvas.restore();
    }

    public void applyBackground(GLView view, GLCanvas canvas) {
        if (mCurrentBackgroundAlpha > 0f) {
            applyOldTexture(view, canvas, mCurrentBackgroundAlpha, mCurrentBackgroundScale, true);
        }
    }

    public void applyContentTransform(GLView view, GLCanvas canvas) {
        // 该位置就是执行放大动画的起始位置
        int xOffset = view.getWidth() / 2;
        int yOffset = view.getHeight() / 2;
        if (mClickRect != null){
            xOffset = mClickRect.centerX();
            yOffset = mClickRect.centerY();
        }
        canvas.translate(xOffset, yOffset);
        canvas.scale(mCurrentContentScale, mCurrentContentScale, 1);
        canvas.translate(-xOffset, -yOffset);
        canvas.setAlpha(mCurrentContentAlpha);
    }

    public void applyOverlay(GLView view, GLCanvas canvas) {
        if (mCurrentOverlayAlpha > 0f) {
            applyOldTexture(view, canvas, mCurrentOverlayAlpha, mCurrentOverlayScale, false);
        }
    }
}
