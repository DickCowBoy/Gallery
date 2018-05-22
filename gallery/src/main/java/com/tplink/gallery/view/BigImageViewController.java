package com.tplink.gallery.view;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.PorterDuff;
import android.graphics.RectF;
import android.os.Build;
import android.support.annotation.IntDef;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.OverScroller;
import android.widget.Scroller;

import com.ortiz.touch.TouchImageView;
import com.tplink.gallery.bean.MediaBean;
import com.tplink.gallery.utils.ThreadUtils;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.List;

public class BigImageViewController extends GalleryTextureView.ViewController {

    public static final int STATE_NONE = 0;
    public static final int STATE_SCALE = 1;
    public static final int STATE_ANIMATE_ZOOM = 2;
    public static final int STATE_DRAG = 3;
    public static final int STATE_FLING = 4;

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
    // Remember last point position for dragging
    private PointF last = new PointF();
    private Fling fling;


    private ScaleGestureDetector mScaleDetector;
    private GestureDetector mGestureDetector;
    private Matrix mCurrentImageMatrix;

    @IntDef({STATE_NONE, STATE_SCALE, STATE_ANIMATE_ZOOM, STATE_DRAG, STATE_FLING})
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
        maxScale = 2;
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
        @Override
        public boolean onDoubleTap(MotionEvent e) {
            boolean consumed = false;
            if (state == STATE_NONE) {
                float targetZoom = (normalizedScale == minScale) ? maxScale : minScale;
                DoubleTapZoom doubleTap = new DoubleTapZoom(targetZoom, e.getX(), e.getY(), false);
                compatPostOnAnimation(doubleTap);
                consumed = true;
            }
            return consumed;
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {

            if (fling != null) {
                //
                // If a previous fling is still active, it should be cancelled so that two flings
                // are not run simultaenously.
                //
                fling.cancelFling();
            }
            fling = new Fling((int) velocityX, (int) velocityY);
            compatPostOnAnimation(fling);
            return super.onFling(e1, e2, velocityX, velocityY);
        }
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private void compatPostOnAnimation(Runnable runnable) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            mTextureView.postOnAnimation(runnable);

        } else {
            mTextureView.postDelayed(runnable, 1000/60);
        }
    }

    /**
     * Inverse of transformCoordTouchToBitmap. This function will transform the coordinates in the
     * drawable's coordinate system to the view's coordinate system.
     * @param bx x-coordinate in original bitmap coordinate system
     * @param by y-coordinate in original bitmap coordinate system
     * @return Coordinates of the point in the view's coordinate system.
     */
    private PointF transformCoordBitmapToTouch(float bx, float by) {
        mCurrentImageMatrix.getValues(m);
        float origW = bitmap.getWidth();
        float origH = bitmap.getHeight();
        float px = bx / origW;
        float py = by / origH;
        float finalX = m[Matrix.MTRANS_X] + getShowImageWidth() * px;
        float finalY = m[Matrix.MTRANS_Y] + getShowImageHeight() * py;
        return new PointF(finalX , finalY);
    }

    private class DoubleTapZoom implements Runnable {

        private long startTime;
        private static final float ZOOM_TIME = 200;
        private float startZoom, targetZoom;
        private float bitmapX, bitmapY;
        private boolean stretchImageToSuper;
        private AccelerateDecelerateInterpolator interpolator = new AccelerateDecelerateInterpolator();
        private PointF startTouch;
        private PointF endTouch;

        DoubleTapZoom(float targetZoom, float focusX, float focusY, boolean stretchImageToSuper) {
            setState(STATE_ANIMATE_ZOOM);
            startTime = System.currentTimeMillis();
            this.startZoom = normalizedScale;
            this.targetZoom = targetZoom;
            this.stretchImageToSuper = stretchImageToSuper;
            PointF bitmapPoint = transformCoordTouchToBitmap(focusX, focusY, false);
            this.bitmapX = bitmapPoint.x;
            this.bitmapY = bitmapPoint.y;

            //
            // Used for translating image during scaling
            //
            startTouch = transformCoordBitmapToTouch(bitmapX, bitmapY);
            endTouch = new PointF(mTextureView.getWidth() / 2, mTextureView.getHeight() / 2);
        }

        @Override
        public void run() {
            float t = interpolate();
            double deltaScale = calculateDeltaScale(t);
            scaleImage(deltaScale, bitmapX, bitmapY, stretchImageToSuper);
            translateImageToCenterTouchPosition(t);
            fixScaleTrans();
            mRenderThread.notifyDirty(System.currentTimeMillis());
//
//TODO            if (touchImageViewListener != null) {
//                touchImageViewListener.onMove();
//            }

            if (t < 1f) {
                //
                // We haven't finished zooming
                //
                compatPostOnAnimation(this);

            } else {
                //
                // Finished zooming
                //
                setState(STATE_NONE);
            }
        }

        /**
         * Interpolate between where the image should start and end in order to translate
         * the image so that the point that is touched is what ends up centered at the end
         * of the zoom.
         * @param t
         */
        private void translateImageToCenterTouchPosition(float t) {
            float targetX = startTouch.x + t * (endTouch.x - startTouch.x);
            float targetY = startTouch.y + t * (endTouch.y - startTouch.y);
            PointF curr = transformCoordBitmapToTouch(bitmapX, bitmapY);
            mCurrentImageMatrix.postTranslate(targetX - curr.x, targetY - curr.y);
        }

        /**
         * Use interpolator to get t
         * @return
         */
        private float interpolate() {
            long currTime = System.currentTimeMillis();
            float elapsed = (currTime - startTime) / ZOOM_TIME;
            elapsed = Math.min(1f, elapsed);
            return interpolator.getInterpolation(elapsed);
        }

        /**
         * Interpolate the current targeted zoom and get the delta
         * from the current zoom.
         * @param t
         * @return
         */
        private double calculateDeltaScale(float t) {
            double zoom = startZoom + t * (targetZoom - startZoom);
            return zoom / normalizedScale;
        }
    }

    /**
     * This function will transform the coordinates in the touch event to the coordinate
     * system of the drawable that the imageview contain
     * @param x x-coordinate of touch event
     * @param y y-coordinate of touch event
     * @param clipToBitmap Touch event may occur within view, but outside image content. True, to clip return value
     * 			to the bounds of the bitmap size.
     * @return Coordinates of the point touched, in the coordinate system of the original drawable.
     */
    private PointF transformCoordTouchToBitmap(float x, float y, boolean clipToBitmap) {
        mCurrentImageMatrix.getValues(m);
        float origW = bitmap.getWidth();
        float origH = bitmap.getHeight();
        float transX = m[Matrix.MTRANS_X];
        float transY = m[Matrix.MTRANS_Y];
        float finalX = ((x - transX) * origW) / getShowImageWidth();
        float finalY = ((y - transY) * origH) / getShowImageHeight();

        if (clipToBitmap) {
            finalX = Math.min(Math.max(finalX, 0), origW);
            finalY = Math.min(Math.max(finalY, 0), origH);
        }

        return new PointF(finalX , finalY);
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
        PointF curr = new PointF(event.getX(), event.getY());

        if (state == STATE_NONE || state == STATE_DRAG || state == STATE_FLING) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    last.set(curr);
                    if (fling != null)
                        fling.cancelFling();
                    setState(STATE_DRAG);
                    break;

                case MotionEvent.ACTION_MOVE:
                    if (state == STATE_DRAG) {
                        float deltaX = curr.x - last.x;
                        float deltaY = curr.y - last.y;
                        float fixTransX = getFixDragTrans(deltaX, mTextureView.getWidth(), getShowImageWidth());
                        float fixTransY = getFixDragTrans(deltaY, mTextureView.getHeight(), getShowImageHeight());
                        mCurrentImageMatrix.postTranslate(fixTransX, fixTransY);
                        fixTrans();
                        last.set(curr.x, curr.y);
                    }
                    break;

                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_POINTER_UP:
                    setState(STATE_NONE);
                    break;
            }
        }
        mRenderThread.notifyDirty(System.currentTimeMillis());

        return true;
    }

    private float getFixDragTrans(float delta, float viewSize, float contentSize) {
        if (contentSize <= viewSize) {
            return 0;
        }
        return delta;
    }

    @Override
    protected void renderContent() {
        if (mTextureView.isAvailable()) {
            // render the content
            Canvas canvas = mTextureView.lockCanvas();
            if (canvas != null) {
                canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);// 清空画布
                canvas.drawBitmap(bitmap, mCurrentImageMatrix, null);
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


    @TargetApi(Build.VERSION_CODES.GINGERBREAD)
    private class CompatScroller {
        Scroller scroller;
        OverScroller overScroller;
        boolean isPreGingerbread;

        public CompatScroller(Context context) {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.GINGERBREAD) {
                isPreGingerbread = true;
                scroller = new Scroller(context);

            } else {
                isPreGingerbread = false;
                overScroller = new OverScroller(context);
            }
        }

        public void fling(int startX, int startY, int velocityX, int velocityY, int minX, int maxX, int minY, int maxY) {
            if (isPreGingerbread) {
                scroller.fling(startX, startY, velocityX, velocityY, minX, maxX, minY, maxY);
            } else {
                overScroller.fling(startX, startY, velocityX, velocityY, minX, maxX, minY, maxY);
            }
        }

        public void forceFinished(boolean finished) {
            if (isPreGingerbread) {
                scroller.forceFinished(finished);
            } else {
                overScroller.forceFinished(finished);
            }
        }

        public boolean isFinished() {
            if (isPreGingerbread) {
                return scroller.isFinished();
            } else {
                return overScroller.isFinished();
            }
        }

        public boolean computeScrollOffset() {
            if (isPreGingerbread) {
                return scroller.computeScrollOffset();
            } else {
                overScroller.computeScrollOffset();
                return overScroller.computeScrollOffset();
            }
        }

        public int getCurrX() {
            if (isPreGingerbread) {
                return scroller.getCurrX();
            } else {
                return overScroller.getCurrX();
            }
        }

        public int getCurrY() {
            if (isPreGingerbread) {
                return scroller.getCurrY();
            } else {
                return overScroller.getCurrY();
            }
        }
    }


    /**
     * Fling launches sequential runnables which apply
     * the fling graphic to the image. The values for the translation
     * are interpolated by the Scroller.
     * @author Ortiz
     *
     */
    private class Fling implements Runnable {

        CompatScroller scroller;
        int currX, currY;

        Fling(int velocityX, int velocityY) {
            setState(STATE_FLING);
            scroller = new CompatScroller(mTextureView.getContext());
            mCurrentImageMatrix.getValues(m);

            int startX = (int) m[Matrix.MTRANS_X];
            int startY = (int) m[Matrix.MTRANS_Y];
            int minX, maxX, minY, maxY;

            if (getShowImageWidth() > mTextureView.getWidth()) {
                minX = mTextureView.getWidth() - (int) getShowImageWidth();
                maxX = 0;

            } else {
                minX = maxX = startX;
            }

            if (getShowImageHeight() > mTextureView.getHeight()) {
                minY = mTextureView.getHeight() - (int) getShowImageHeight();
                maxY = 0;

            } else {
                minY = maxY = startY;
            }

            scroller.fling(startX, startY, (int) velocityX, (int) velocityY, minX,
                    maxX, minY, maxY);
            currX = startX;
            currY = startY;
        }

        public void cancelFling() {
            if (scroller != null) {
                setState(STATE_NONE);
                scroller.forceFinished(true);
            }
        }

        @Override
        public void run() {

            //
            // OnTouchImageViewListener is set: TouchImageView listener has been flung by user.
            // Listener runnable updated with each frame of fling animation.
            //
//todo            if (touchImageViewListener != null) {
//                touchImageViewListener.onMove();
//            }

            if (scroller.isFinished()) {
                scroller = null;
                return;
            }

            if (scroller.computeScrollOffset()) {
                int newX = scroller.getCurrX();
                int newY = scroller.getCurrY();
                int transX = newX - currX;
                int transY = newY - currY;
                currX = newX;
                currY = newY;
                mCurrentImageMatrix.postTranslate(transX, transY);
                fixTrans();
                mRenderThread.notifyDirty(System.currentTimeMillis());
                compatPostOnAnimation(this);
            }
        }
    }
}
