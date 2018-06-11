/*
 * Copyright (C) 2018, TP-LINK TECHNOLOGIES CO., LTD.
 *
 * CalibrationSeekBar.java
 *
 *
 * Author ZhangYi
 *
 * Ver 1.0, 18-04-02, ZhangYi, Create file
 */
package com.tplink.gallery.view;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import com.tplink.gallery.R;

import java.util.Arrays;


public class CalibrationSeekBar extends View {
    private static final String TAG = "CalibrationSeekBar";
    protected static final float TICK_SHORT_FACTOR = 0.3f;
    protected static final float TICK_LONG_FACTOR = 0.5f;
    protected static final int DURATION_TICK_INACTIVE = 1000;

    protected Paint mTickSelectPaint;
    protected Paint mTickUnSelectPaint;

    protected int mTickCount;
    protected int mTickSelectCount;
    protected int mMaxValue;
    protected int mCurrentValue;
    protected int mTickSize;

    protected SeekValueListener mValueChangeListener;

    protected float mTickScaleSize[];

    public interface SeekValueListener {
        void onValueChange(int newValue);
    }

    public CalibrationSeekBar(Context context) {
        this(context, null);
    }

    public CalibrationSeekBar(Context context, AttributeSet attrs) {
        super(context, attrs);

        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.CalibrationSeekBar, 0, 0);
        int tickSelectedColor = Color.WHITE;
        int tickUnSelectedColor = Color.BLACK;

        try {
            tickSelectedColor = ta
                    .getColor(R.styleable.CalibrationSeekBar_selectedTickColor, Color.WHITE);
            tickUnSelectedColor = ta
                    .getColor(R.styleable.CalibrationSeekBar_unselectedTickColor, Color.BLACK);
            mMaxValue = ta
                    .getInt(R.styleable.CalibrationSeekBar_maxValue, 0);
            mCurrentValue = ta
                    .getInt(R.styleable.CalibrationSeekBar_currentValue, 0);
            mTickCount = ta
                    .getInt(R.styleable.CalibrationSeekBar_tickCount, 0);
            mTickSize = ta
                    .getDimensionPixelSize(R.styleable.CalibrationSeekBar_tickSize, 1);

            mTickScaleSize = new float[mTickCount];
            Arrays.fill(mTickScaleSize, TICK_SHORT_FACTOR);
        } finally {
            ta.recycle();
        }

        setValue(mCurrentValue);

        mTickSelectPaint = new Paint();
        mTickSelectPaint.setColor(tickSelectedColor);
        mTickSelectPaint.setStrokeWidth(mTickSize);
        mTickUnSelectPaint = new Paint();
        mTickUnSelectPaint.setColor(tickUnSelectedColor);
        mTickUnSelectPaint.setStrokeWidth(mTickSize);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        drawTick(canvas);
    }

    protected void drawTick(Canvas canvas) {
        float spacing = getDrawableWidth() * 1f / (mTickCount - 1);
        float startX;
        float startY;
        float endX;
        float endY;
        Paint tickPaint;

        for (int i = 0; i < mTickCount; i++) {
            startX = i * spacing;
            endX = startX;
            startY = (1 - mTickScaleSize[i]) * getHeight() / 2;
            endY = startY + mTickScaleSize[i] * getHeight();
            tickPaint = (i < mTickSelectCount ? mTickSelectPaint : mTickUnSelectPaint);
            canvas.drawLine(startX + 1, startY, endX + 1, endY, tickPaint);
        }
    }

    protected int getDrawableWidth() {
        return getWidth() - mTickSize;
    }

    public void setSeekValueChangeListener(SeekValueListener listener) {
        mValueChangeListener = listener;
    }

    public void setValue(int value) {
        if (mMaxValue <= 0) {
            Log.e(TAG,"setValue, please set max value first");
            return;
        }
        if (value != mCurrentValue) {
            mCurrentValue = value;
            mTickSelectCount = (int) (mCurrentValue * 1f / mMaxValue * mTickCount);
            invalidate();
        }
    }

    public void setMaxValue(int value) {
        if (mMaxValue != value) {
            mMaxValue = value;
            invalidate();
        }
    }

    public void setValues(int currentValue, int maxValue) {
        if (mMaxValue != maxValue) {
            mMaxValue = maxValue;
        }
        if (currentValue != mCurrentValue) {
            mCurrentValue = currentValue;
            mTickSelectCount = (int) (mCurrentValue * 1f / mMaxValue * mTickCount);
        }
        invalidate();
    }

    protected void calculateSelectTickCountByTouch(float x) {
        mTickSelectCount = (int) (x / getDrawableWidth() * mTickCount);
        int newValue = (int) (x / getDrawableWidth() * mMaxValue);

        if (newValue < 0) {
            newValue = 0;
        }

        if (newValue > mMaxValue) {
            newValue = mMaxValue;
        }

        if (newValue != mCurrentValue) {
            mCurrentValue = newValue;
            if (mValueChangeListener != null) {
                mValueChangeListener.onValueChange(mCurrentValue);
            }
        }
        invalidate();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_MOVE:
                calculateSelectTickCountByTouch(event.getX());
                updateTickScaleSize();
                return true;
            case MotionEvent.ACTION_UP:
                calculateSelectTickCountByTouch(event.getX());
                startTickInActiveAnimation();
                return true;
        }
        return super.onTouchEvent(event);
    }

    protected void startTickInActiveAnimation() {
        ValueAnimator valueAnimator = ValueAnimator.ofFloat(TICK_LONG_FACTOR, TICK_SHORT_FACTOR);
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                for (int i = 0; i < mTickCount; i++) {
                    mTickScaleSize[i] =
                            TICK_SHORT_FACTOR + (mTickScaleSize[i] - TICK_SHORT_FACTOR) * (1
                                    - animation.getAnimatedFraction());
                }
                invalidate();
            }
        });

        valueAnimator.setDuration(DURATION_TICK_INACTIVE);
        valueAnimator.start();
    }

    protected void updateTickScaleSize() {
        for (int i = 0; i < mTickCount; i++) {
            if (i == mTickSelectCount) {
                mTickScaleSize[i] = TICK_LONG_FACTOR;
            } else if (Math.abs(mTickSelectCount - i) < (mTickCount / 10)) {
                mTickScaleSize[i] = TICK_SHORT_FACTOR + 1f / Math.abs(mTickSelectCount - i) * (
                        TICK_LONG_FACTOR - TICK_SHORT_FACTOR) * 0.7f;
            } else {
                mTickScaleSize[i] = TICK_SHORT_FACTOR;
            }
        }
        invalidate();
    }

    public int getCurrentValue() {
        return mCurrentValue;
    }
}
