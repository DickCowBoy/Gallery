package com.tplink.gallery.view;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
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

    private static final int DIVIDER_WIDTH = 40;// todo

    private static final float SUPER_MIN_MULTIPLIER = .75f;
    private static final float SUPER_MAX_MULTIPLIER = 1.25f;

    private RenderThread mRenderThread;
    private List<MediaBean> mediaBeans;

    private DrawContentProvider drawContentProvider;

    // TODO CALCULATE THE CORRECT VALUE
    private float minScale;
    private float maxScale;
    private float superMinScale;
    private float superMaxScale;
    private float normalizedScale;
    private float filmScale;
    private float[] m;
    // Remember last point position for dragging
    private PointF last = new PointF();
    private Fling fling;

    private boolean isFilmModeEnable = true;
    private boolean isInFilmMode = false;

    public void setFilmModeEnable(boolean filmModeEnable) {
        isFilmModeEnable = filmModeEnable;
    }

    private ScaleGestureDetector mScaleDetector;
    private GestureDetector mGestureDetector;
    private AnimMatrix mCurrentImageMatrix;

    @IntDef({STATE_NONE, STATE_SCALE, STATE_ANIMATE_ZOOM, STATE_DRAG, STATE_FLING})
    @Retention(RetentionPolicy.SOURCE)
    public @interface State {
    }



    private @State int state;

    public void setState(@State int state) {
        this.state = state;
    }

    public BigImageViewController(GalleryTextureView mTextureView, DrawContentProvider drawContentProvider) {
        super(mTextureView);
        this.drawContentProvider = drawContentProvider;
        init();
    }

    private void init() {
        mScaleDetector = new ScaleGestureDetector(mTextureView.getContext(), new ScaleListener());
        mGestureDetector = new GestureDetector(mTextureView.getContext(), new GestureListener());

        minScale = 1;
        maxScale = 2;
        superMinScale = SUPER_MIN_MULTIPLIER * minScale;
        filmScale = superMinScale;
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
            if (isFilmModeEnable && !isInFilmMode && Float.compare(normalizedScale, 1.0F) == 0
                    && Float.compare(detector.getScaleFactor(), 1.0F) < 0) {
                // enter the film mode
                isInFilmMode = true;
                EnterFilmModeAnim anim = new EnterFilmModeAnim();
                compatPostOnAnimation(anim);
                // start the film anim
            } else {
                scaleImage(detector.getScaleFactor(),
                        detector.getFocusX(),
                        detector.getFocusY(),
                        false);
                mRenderThread.notifyDirty(System.currentTimeMillis());
            }
            return true;
        }

        @Override
        public void onScaleEnd(ScaleGestureDetector detector) {
            setState(STATE_NONE);
            return;
        }
    }

    /**
     * @param deltaScale
     * @param focusX
     * @param focusY
     * @param stretchImageToSuper
     * @return 0 scale normal 1 scale to the min 2 scale to the max
     */
    private int scaleImage(double deltaScale, float focusX, float focusY, boolean stretchImageToSuper) {

        int type = 0;
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
            type = 2;
            deltaScale = upperScale / origScale;
        } else if (normalizedScale < lowerScale) {
            normalizedScale = lowerScale;
            deltaScale = lowerScale / origScale;
            type = 1;
        }
        mCurrentImageMatrix.postScale((float) deltaScale, (float) deltaScale, focusX, focusY);
        fixScaleTrans();
        return type;
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
            Log.e("LJL", "DOUBLE TAP");
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
            RectF showRect = drawContentProvider.getCurrentDrawContent().getShowRect(mCurrentImageMatrix);
            if (Math.abs(velocityX) > Math.abs(velocityY) && (
                    Float.compare(normalizedScale, 1.0F) == 0 ||
                    Float.compare(showRect.left, 1.0F) > 0 ||
                    Float.compare(showRect.right, mTextureView.getWidth()) < 0
                    )) {

                if (state == STATE_DRAG) {
                    int pos = 0;

                    if (velocityX > 0 && drawContentProvider.hasPreview()) {
                        pos = -1;
                    } else if ((velocityX < 0 && drawContentProvider.hasNext())) {
                        pos = 1;
                    }
                    NormalScroll2Center normalScroll2Center = new NormalScroll2Center(showRect,
                            pos);
                    compatPostOnAnimation(normalScroll2Center);
                }
                return super.onFling(e1, e2, velocityX, velocityY);
            }

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
        float origW = drawContentProvider.getCurrentDrawContent().width;
        float origH = drawContentProvider.getCurrentDrawContent().height;
        float px = bx / origW;
        float py = by / origH;
        float finalX = m[Matrix.MTRANS_X] + getShowImageWidth() * px;
        float finalY = m[Matrix.MTRANS_Y] + getShowImageHeight() * py;
        return new PointF(finalX , finalY);
    }


    /**
     * apply the anim for enter film mode
     */
    private class EnterFilmModeAnim implements Runnable {
        private static final float ZOOM_TIME = 100;
        private long startTime;
        private float target = 0.35F;
        private AccelerateDecelerateInterpolator interpolator = new AccelerateDecelerateInterpolator();

        public EnterFilmModeAnim() {
            this.startTime = System.currentTimeMillis();
            setState(STATE_ANIMATE_ZOOM);
        }

        @Override
        public void run() {
            float t = interpolate();
            mCurrentImageMatrix = drawContentProvider.getCurrentDrawContent().calMatrix(mTextureView.getWidth(),
                    mTextureView.getHeight());
            float df = 1 - target * t;
            mCurrentImageMatrix.postScale(df, df, mTextureView.getWidth() / 2,
                    mTextureView.getHeight() / 2);
            mCurrentImageMatrix.baseScale = df;
            mRenderThread.notifyDirty(System.currentTimeMillis());

            if (t < 1f) {
                compatPostOnAnimation(this);
            } else {
                setState(STATE_NONE);
            }
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
    }


    private class NormalScroll2Center implements Runnable{
        private static final float ZOOM_TIME = 100;
        private long startTime;
        private int prePos;
        private float endX;
        private float preAnim = 0;
        private AccelerateDecelerateInterpolator interpolator = new AccelerateDecelerateInterpolator();

        /**
         *
         * @param prePos -1 means switch to the pre image 0 means no changes and 1 means next image
         */
        public NormalScroll2Center(RectF preRectF, int prePos) {
            this.startTime = System.currentTimeMillis();
            this.prePos = prePos;
            switch (prePos) {
                case 0:
                    if (Float.compare(preRectF.left, 0.0F) > 0) {
                        endX = 0 -  preRectF.left;
                    } else {
                        endX = mTextureView.getWidth() - preRectF.right;
                    }

                    break;
                case -1:
                    endX = mTextureView.getWidth() + DIVIDER_WIDTH - preRectF.left;
                    break;
                case 1:
                    endX = -preRectF.right - DIVIDER_WIDTH;
                    break;
            }
            preAnim = 0;
            setState(STATE_ANIMATE_ZOOM);
        }

        @Override
        public void run() {
            float t = interpolate();

            mCurrentImageMatrix.postTranslate((t- preAnim) * endX, 0);
            preAnim = t;
            mRenderThread.notifyDirty(System.currentTimeMillis());

            if (t < 1f) {
                compatPostOnAnimation(this);
            } else {
                setState(STATE_NONE);
                if (prePos == -1) {
                    drawContentProvider.switchToPre();
                } else if (prePos == 1) {
                    drawContentProvider.switchToNext();
                }
                normalizedScale = 1.0F;
                mCurrentImageMatrix = drawContentProvider.getCurrentDrawContent()
                        .calMatrix(mTextureView.getWidth(), mTextureView.getHeight());
            }

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
        float origW = drawContentProvider.getCurrentDrawContent().width;
        float origH = drawContentProvider.getCurrentDrawContent().height;
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

//    public void setBitmap(Bitmap bitmap) {
//        this.bitmap = bitmap;
//        mCurrentImageMatrix = calMatrix(mTextureView.getWidth(), mTextureView.getHeight());
//    }


    @Override
    protected void onAvailable(int width, int height) {
        mCurrentImageMatrix = drawContentProvider.getCurrentDrawContent().calMatrix(width,height);
        if (mRenderThread != null) {
            mRenderThread.notifyDirty(System.currentTimeMillis());
        }
    }

    @Override
    protected void onDisable() {

    }

    @Override
    protected void onSizeChanged(int width, int height) {
        mCurrentImageMatrix = drawContentProvider.getCurrentDrawContent().calMatrix(width,height);
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
        if (isInFilmMode) {
            processFilmTouchEvent(event);
        } else {
            mScaleDetector.onTouchEvent(event);
            mGestureDetector.onTouchEvent(event);
            processNormalTouchEvent(event);
        }

        return true;
    }

    private void processNormalTouchEvent(MotionEvent event) {
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
                    Log.e("LJL", "ACTION MOVE");
                    if (state == STATE_DRAG) {
                        float deltaX = curr.x - last.x;
                        mCurrentImageMatrix.postTranslate(deltaX, 0);
                        RectF showRect = drawContentProvider.getCurrentDrawContent().getShowRect(mCurrentImageMatrix);
                        if (Float.compare(showRect.left, 0.0F) < 0 && !drawContentProvider.hasNext()) {
                            mCurrentImageMatrix.postTranslate(-deltaX, 0);
                        } else if (Float.compare(showRect.right, mTextureView.getWidth()) > 0
                                && !drawContentProvider.hasPreview()) {
                            mCurrentImageMatrix.postTranslate(-deltaX, 0);
                        }
                        last.set(curr.x, curr.y);
                    }
                    break;

                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_POINTER_UP:
                    Log.e("LJL", "ACTION UP");
                    RectF showRect = drawContentProvider.getCurrentDrawContent().getShowRect(mCurrentImageMatrix);
                    if (state == STATE_DRAG && (
                            Float.compare(showRect.left, 0.0F) > 0
                                    || Float.compare(showRect.right, mTextureView.getWidth()) < 0)) {
                        int pos = 0;

                        if (Float.compare(showRect.left, 0.0F) >
                                0) {
                            pos = Float.compare(showRect.left, mTextureView.getWidth() / 3.0F) > 0 ? -1 : 0;

                        } else if (Float.compare(showRect.right, mTextureView.getWidth()) < 0){
                            pos = Float.compare(mTextureView.getWidth() - showRect.right,
                                    mTextureView.getWidth() / 3.0F) > 0 ? 1 : 0;
                        }
                        NormalScroll2Center normalScroll2Center = new NormalScroll2Center(showRect,
                                pos);
                        compatPostOnAnimation(normalScroll2Center);
                    }
                    setState(STATE_NONE);
                    break;
            }
        }
        if (mRenderThread != null) {
            // #TODO split screen error
            mRenderThread.notifyDirty(System.currentTimeMillis());
        }
    }

    private void processFilmTouchEvent(MotionEvent event) {

    }

    private float getFixDragTrans(float delta, float viewSize, float contentSize) {
        if (contentSize <= viewSize) {
            return 0;
        }
        return delta;
    }

    @Override
    protected void renderContent() {
        long time = System.currentTimeMillis();
        if (mTextureView.isAvailable()) {
            // render the content
            Canvas canvas = mTextureView.lockCanvas();
            if (canvas != null) {
                canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);// 清空画布
                if (isInFilmMode) {
                    renderFilm(canvas);
                } else {
                    renderNormal(canvas);
                }
                mTextureView.unlockCanvasAndPost(canvas);
            }
        }
        Log.e("LJL", "RENDER TIME" + (System.currentTimeMillis() - time));
    }

    // in aosp gallery render <=7 images
    private void renderFilm(Canvas canvas) {
        canvas.drawBitmap(drawContentProvider.getCurrentDrawContent().content, mCurrentImageMatrix, null);
        //TODO  render the blank space
        RectF showRect = drawContentProvider.getCurrentDrawContent().getShowRect(mCurrentImageMatrix);
        float divide = DIVIDER_WIDTH * mCurrentImageMatrix.baseScale;
        float left = showRect.left;
        DrawContent drawContent;
        AnimMatrix animMatrix;
        RectF rectF = new RectF();
        // render the left side
        for (int i = -1; i > -4; i --) {
            if (Float.compare(left, divide * mCurrentImageMatrix.baseScale) > 0) {
                drawContent = drawContentProvider.getPreDrawContent(i);
                if (drawContent == null) {
                    break;
                }
                animMatrix = drawContent.calMatrix(mTextureView.getWidth(), mTextureView.getHeight());
                animMatrix.postScale(mCurrentImageMatrix.baseScale, mCurrentImageMatrix.baseScale,
                        mTextureView.getWidth() / 2,
                        mTextureView.getHeight() / 2);
                rectF.left = 0;
                rectF.top = 0;
                rectF.right = drawContent.width;
                rectF.bottom = drawContent.height;
                animMatrix.mapRect(rectF);
                animMatrix.postTranslate(left - divide * mCurrentImageMatrix.baseScale - rectF.right,
                        0);
                left = rectF.left + (left - divide * mCurrentImageMatrix.baseScale - rectF.right);
                canvas.drawBitmap(drawContent.content, animMatrix, null);
            }
        }
        // render the right side
        float right = showRect.right;
        for (int i = 1; i < 4; i ++) {
            if (Float.compare(mTextureView.getWidth(), right + divide * mCurrentImageMatrix.baseScale) > 0) {
                drawContent = drawContentProvider.getNextDrawContent(i);
                if (drawContent == null) {
                    break;
                }
                animMatrix = drawContent.calMatrix(mTextureView.getWidth(), mTextureView.getHeight());
                animMatrix.postScale(mCurrentImageMatrix.baseScale, mCurrentImageMatrix.baseScale,
                        mTextureView.getWidth() / 2,
                        mTextureView.getHeight() / 2);
                rectF.left = 0;
                rectF.top = 0;
                rectF.right = drawContent.width;
                rectF.bottom = drawContent.height;
                animMatrix.mapRect(rectF);
                animMatrix.postTranslate(right + divide * mCurrentImageMatrix.baseScale - rectF.left,
                        0);
                right = rectF.right + (right + divide * mCurrentImageMatrix.baseScale - rectF.left);
                canvas.drawBitmap(drawContent.content, animMatrix, null);
            }
        }

    }

    private void renderNormal(Canvas canvas) {
        // render the main
        canvas.drawBitmap(drawContentProvider.getCurrentDrawContent().content, mCurrentImageMatrix, null);
        // cal whether need to render the left and right
        RectF showRect = drawContentProvider.getCurrentDrawContent().getShowRect(mCurrentImageMatrix);
        if (showRect.left > DIVIDER_WIDTH) {
            // render the pre pic
            DrawContent preDrawContent = drawContentProvider.getPreDrawContent(-1);
            Matrix matrix = preDrawContent.calMatrix(mTextureView.getWidth(), mTextureView.getHeight());
            matrix.postTranslate(showRect.left -mTextureView.getWidth() - DIVIDER_WIDTH,0);
            canvas.drawBitmap(preDrawContent.content, matrix, null);
        } else if (showRect.right < (mTextureView.getWidth() - DIVIDER_WIDTH)) {
            // render the next pic
            DrawContent nextDrawContent = drawContentProvider.getNextDrawContent(1);
            Matrix matrix = nextDrawContent.calMatrix(mTextureView.getWidth(), mTextureView.getHeight());
            matrix.postTranslate(showRect.right + DIVIDER_WIDTH,0);
            canvas.drawBitmap(nextDrawContent.content, matrix, null);
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
        return (int) (drawContentProvider.getCurrentDrawContent().getShowWidth() * normalizedScale);
    }

    private int getShowImageHeight() {
        return (int) (drawContentProvider.getCurrentDrawContent().getShowHeight() * normalizedScale);
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

    public static abstract class DrawContentProvider {
        private BigImageViewController controller;

        public DrawContentProvider(BigImageViewController controller) {
            this.controller = controller;
        }

        private void updateContent() {
            controller.mRenderThread.notifyDirty(System.currentTimeMillis());
        }

        public abstract boolean hasPreview();

        public abstract boolean hasNext();

        public abstract DrawContent getCurrentDrawContent();
        public abstract DrawContent getPreDrawContent(int index);
        public abstract DrawContent getNextDrawContent(int index);
        public abstract void switchToPre();
        public abstract void switchToNext();
    }

    public static class DrawContent {
        public int width;
        public int height;
        public Bitmap content;
        public float originScale;

        public int getShowWidth() {
            return (int) (width * originScale);
        }

        public int getShowHeight() {
            return (int) (height * originScale);
        }

        public RectF getShowRect(Matrix matrix) {
            RectF rectF = new RectF(0, 0, width, height);
            matrix.mapRect(rectF);
            return rectF;
        }

        public AnimMatrix calMatrix(int width, int height) {
            if (width == 0 || height == 0) {
                return null;
            }
            AnimMatrix matrix = new AnimMatrix();
            // 1.将图片居中
            // 2.旋转图片
            float scaleX = ((width) / 1.0F / this.width);
            float scaleY = (height / 1.0F / this.height);
            originScale = Math.min(scaleX, scaleY);
            matrix.postScale(originScale, originScale,
                    this.width / 2,
                    this.height / 2);
            matrix.postTranslate((width - this.width) / 2,
                    (height - this.height) / 2);
            return matrix;
        }
    }

    public static class AnimMatrix extends Matrix {
        public float baseScale;
    }
}
