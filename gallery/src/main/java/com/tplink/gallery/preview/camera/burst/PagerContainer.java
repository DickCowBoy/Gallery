package com.tplink.gallery.preview.camera.burst;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Point;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.FrameLayout;

import com.tplink.gallery.R;

/**
 * PagerContainer: A layout that displays a ViewPager with its children that are
 * outside the typical pager bounds.
 *
 * @see(<a href = "https://gist.github.com/devunwired/8cbe094bb7a783e37ad1"></>)
 */
public class PagerContainer extends FrameLayout implements ViewPager.OnPageChangeListener {

    private SelectBurstViewPager mViewPager;
    boolean mNeedsRedraw = false;
    private float mRatio = 0.78f;
    private Context mContext;
    private int mImageWidth = 0;
    private int mImageHeight = 0;
    private float mScale = 0.78f;
    private int mScreenWith = 0;
    private int mScreenHeight = 0;

    public PagerContainer(Context context) {
        super(context);
        init(context);
    }

    public void setImageWidth(int width) {
        mImageWidth = width;
    }

    public void setImageHeight(int height) {
        mImageHeight = height;
    }

    public void setScreenWidth(int width) {
        mScreenWith = width;
    }

    public void setScreenHeight(int height) {
        mScreenHeight = height;
    }

    public PagerContainer(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public PagerContainer(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    private void init(Context context) {
        mContext = context;
        // Disable clipping of children so non-selected pages are visible
        setClipChildren(false);
    }

    @SuppressLint("MissingSuperCall")
    @Override
    protected void onFinishInflate() {
        try {
            mViewPager = (SelectBurstViewPager) getChildAt(0);
            mViewPager.addOnPageChangeListener(this);
        } catch (Exception e) {
            throw new IllegalStateException("The root child of PagerContainer must be a ViewPager");
        }
    }

    public SelectBurstViewPager getViewPager() {
        return mViewPager;
    }

    private Point mCenter = new Point();

    private Point mInitialTouch = new Point();

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        mCenter.x = w / 2;
        mCenter.y = h / 2;
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        // We capture any touches not already handled by the ViewPager
        // to implement scrolling from a touch outside the pager bounds.
        switch (ev.getAction()) {

            case MotionEvent.ACTION_DOWN:
                mInitialTouch.x = (int)ev.getX();
                mInitialTouch.y = (int)ev.getY();
            default:
                float deltaX = mCenter.x - mInitialTouch.x;
                float deltaY = mCenter.y - mInitialTouch.y;
                ev.offsetLocation(deltaX, deltaY);
                break;
        }

        // 这里不能使用dispatchTouchEvent对事件进行分发
        // 否则会导致１:1的图片出现x,y位置不对的情况,ImageView做了onTouchListener处理的giu
        return mViewPager.onTouchEvent(ev);
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
        // Force the container to redraw on scrolling.
        // Without this the outer pages render initially and then stay static
        if (mNeedsRedraw)
            invalidate();
    }

    @Override
    public void onPageSelected(int position) {

    }

    @Override
    public void onPageScrollStateChanged(int state) {
        mNeedsRedraw = (state != ViewPager.SCROLL_STATE_IDLE);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        // 根据当前的横竖情况计算viewpager的展示大小
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);

        int height = 0;
        int width = 0;
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            width = mScreenHeight != 0
                    ? (int) (mScreenHeight * mScale)
                    : getResources().getDimensionPixelSize(R.dimen.burst_big_img_lwidth);
        }else{
            width = mScreenWith != 0
                    ? (int)(mScreenWith * mScale)
                    : getResources().getDimensionPixelSize(R.dimen.burst_big_img_pwidth);
        }
        height = (int)(width / getRatio());
        if (height > heightSize) {
            // 超出了可使用范围，重新计算高度
            height = heightSize;
            width = (int) (height * getRatio());
        }
        height = MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY);
        width = MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY);
        if (mViewPager != null) {
            mViewPager.measure(width, height);
        }
    }

    // 获取照片的宽高比
    private float getRatio() {
        if (mImageWidth != 0 && mImageHeight != 0) {
            return mImageWidth / 1.0F / mImageHeight;
        }
        return mRatio;
    }
}