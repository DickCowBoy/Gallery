package com.tplink.gallery.view;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.RectF;
import android.support.annotation.IntDef;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;

import com.tplink.gallery.bean.MediaBean;
import com.tplink.gallery.utils.ThreadUtils;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.List;

public class BigImageViewController extends GalleryTextureView.ViewController {

    public static final int STATE_NONE = 0;
    public static final int STATE_SCALE = 1;

    private static final float SUPER_MIN_MULTIPLIER = .75f;
    private static final float SUPER_MAX_MULTIPLIER = 1.25f;

    private RenderThread mRenderThread;
    private List<MediaBean> mediaBeans;
    private Bitmap bitmap;

    private float originScale;

    // TODO CALCULATE THE CORRECT VALUE
    private float minScale;
    private float maxScale;
    private float superMinScale;
    private float superMaxScale;
    private float normalizedScale;
    private float[] m;

    private ScaleGestureDetector mScaleDetector;
    private GestureDetector mGestureDetector;
    private GestureDetector.OnDoubleTapListener doubleTapListener = null;
    private Matrix mCurrentImageMatrix;

    @IntDef({STATE_NONE, STATE_SCALE})
    @Retention(RetentionPolicy.SOURCE)
    public @interface State {
    }



    private @State int state;

    public void setState(@State int state) {
        this.state = state;
    }

    public BigImageViewController(GalleryTextureView mTextureView) {
        super(mTextureView);
        init();
    }

    private void init() {
        mScaleDetector = new ScaleGestureDetector(mTextureView.getContext(), new ScaleListener());
        mGestureDetector = new GestureDetector(mTextureView.getContext(), new GestureListener());

        minScale = 1;
        maxScale = 3;
        superMinScale = SUPER_MIN_MULTIPLIER * minScale;
        superMaxScale = SUPER_MAX_MULTIPLIER * maxScale;
        normalizedScale = 1;
        m = new float[9];
    }

    private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {

        @Override
        public boolean onScaleBegin(ScaleGestureDetector detector) {
            setState(STATE_SCALE);
            return true;
        }

        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            scaleImage(detector.getScaleFactor(), detector.getFocusX(), detector.getFocusY(), false);
            mRenderThread.notifyDirty(System.currentTimeMillis());
            return true;
        }

        @Override
        public void onScaleEnd(ScaleGestureDetector detector) {
            setState(STATE_NONE);
            return;
        }
    }

    private void scaleImage(double deltaScale, float focusX, float focusY, boolean stretchImageToSuper) {

        float lowerScale, upperScale;
        if (stretchImageToSuper) {
            lowerScale = superMinScale;
            upperScale = superMaxScale;

        } else {
            lowerScale = minScale;
            upperScale = maxScale;
        }

        float origScale = normalizedScale;
        normalizedScale *= deltaScale;
        if (normalizedScale > upperScale) {
            normalizedScale = upperScale;
            deltaScale = upperScale / origScale;
        } else if (normalizedScale < lowerScale) {
            normalizedScale = lowerScale;
            deltaScale = lowerScale / origScale;
        }
        mCurrentImageMatrix.postScale((float) deltaScale, (float) deltaScale, focusX, focusY);
        fixScaleTrans();
    }

    private float getFixTrans(float trans, float viewSize, float contentSize) {
        float minTrans, maxTrans;

        if (contentSize <= viewSize) {
            minTrans = 0;
            maxTrans = viewSize - contentSize;

        } else {
            minTrans = viewSize - contentSize;
            maxTrans = 0;
        }

        if (trans < minTrans)
            return -trans + minTrans;
        if (trans > maxTrans)
            return -trans + maxTrans;
        return 0;
    }

    /**
     * Performs boundary checking and fixes the image matrix if it
     * is out of bounds.
     */
    private void fixTrans() {
        mCurrentImageMatrix.getValues(m);
        float transX = m[Matrix.MTRANS_X];
        float transY = m[Matrix.MTRANS_Y];

        float fixTransX = getFixTrans(transX, mTextureView.getWidth(), getShowImageWidth());
        float fixTransY = getFixTrans(transY, mTextureView.getHeight(), getShowImageHeight());

        if (fixTransX != 0 || fixTransY != 0) {
            mCurrentImageMatrix.postTranslate(fixTransX, fixTransY);
        }
    }

    /**
     * When transitioning from zooming from focus to zoom from center (or vice versa)
     * the image can become unaligned within the view. This is apparent when zooming
     * quickly. When the content size is less than the view size, the content will often
     * be centered incorrectly within the view. fixScaleTrans first calls fixTrans() and
     * then makes sure the image is centered correctly within the view.
     */
    private void fixScaleTrans() {
        fixTrans();
        mCurrentImageMatrix.getValues(m);
        if (getShowImageWidth() <= mTextureView.getWidth()) {
            m[Matrix.MTRANS_X] = (mTextureView.getWidth() - getShowImageWidth()) / 2;
        }

        if (getShowImageHeight() <= mTextureView.getHeight()) {
            m[Matrix.MTRANS_Y] = (mTextureView.getHeight() - getShowImageHeight()) / 2;
        }
        mCurrentImageMatrix.setValues(m);
    }

    private class GestureListener extends GestureDetector.SimpleOnGestureListener {

    }

    public void updateMedias(List<MediaBean> mediaBeans) {
        this.mediaBeans = mediaBeans;
        this.mRenderThread.notifyDirty(System.currentTimeMillis());
    }

    public void setBitmap(Bitmap bitmap) {
        this.bitmap = bitmap;
        mCurrentImageMatrix = calMatrix(mTextureView.getWidth(), mTextureView.getHeight());
    }

    private Matrix calMatrix(int width, int height) {
        if (width == 0 || height == 0 || bitmap == null) {
            return null;
        }
        Matrix matrix = new Matrix();
        // 1.将图片居中
        // 2.旋转图片
        float scaleX = ((width) / 1.0F / bitmap.getWidth());
        float scaleY = (height / 1.0F / bitmap.getHeight());
        originScale = Math.min(scaleX, scaleY);
        matrix.postScale(originScale, originScale, bitmap.getWidth() / 2,
                bitmap.getHeight() / 2);
        matrix.postTranslate((width - bitmap.getWidth()) / 2,
                (height - bitmap.getHeight()) / 2);
        return matrix;
    }

    @Override
    protected void onAvailable(int width, int height) {
        mCurrentImageMatrix = calMatrix(width,height);
        mRenderThread.notifyDirty(System.currentTimeMillis());
    }

    @Override
    protected void onDisable() {

    }

    @Override
    protected void onSizeChanged(int width, int height) {
        mCurrentImageMatrix = calMatrix(width,height);
    }

    public void enable() {
        if (mRenderThread == null) {
            mRenderThread = new RenderThread("RenderThread");
            mRenderThread.start();
            mRenderThread.notifyDirty(System.currentTimeMillis());
        }
    }

    public void disable() {
        if (mRenderThread != null) {
            mRenderThread.quit();
            mRenderThread = null;
        }
    }

    @Override
    protected boolean processTouchEvent(MotionEvent event) {
        mScaleDetector.onTouchEvent(event);
        mGestureDetector.onTouchEvent(event);
        return true;
    }

    Paint paint = new Paint();
    @Override
    protected void renderContent() {
        if (mTextureView.isAvailable()) {
            // render the content
            Canvas canvas = mTextureView.lockCanvas();
            if (canvas != null) {
                canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);// 清空画布
                canvas.drawBitmap(bitmap, mCurrentImageMatrix, null);
                RectF rect = new RectF(0, 0 , bitmap.getWidth(), bitmap.getHeight());
                mCurrentImageMatrix.mapRect(rect);
                paint.setColor(Color.RED);
                paint.setStyle(Paint.Style.STROKE);
                canvas.drawRect(rect, paint);
                mTextureView.unlockCanvasAndPost(canvas);
            }
        }
    }

    private class RenderThread extends Thread {

        private boolean enable = true;
        private long newestContent = -1;
        private long currentContent = -1;

        public RenderThread(String name) {
            super(name);
        }

        private synchronized void notifyDirty(long newestContent) {
            this.newestContent = newestContent;
            this.notifyAll();
        }

        private synchronized void quit() {

            enable = false;
            this.notifyAll();

        }

        @Override
        public void run() {
            while (enable) {
                if (currentContent >= newestContent) {
                    synchronized (this) {
                        // it is already the newest content
                        ThreadUtils.waitWithoutInterrupt(this);
                        continue;
                    }
                }
                // need to render
                currentContent = System.currentTimeMillis();
                BigImageViewController.this.renderContent();
            }
        }
    }

    private int getShowImageWidth() {
        return (int) (bitmap.getWidth() * normalizedScale * originScale);
    }

    private int getShowImageHeight() {
        return (int) (bitmap.getHeight() * normalizedScale * originScale);
    }
}
