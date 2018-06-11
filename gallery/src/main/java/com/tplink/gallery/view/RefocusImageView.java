/*
 * Copyright (C) 2018, TP-LINK TECHNOLOGIES CO., LTD.
 *
 * RefocusImageView.java
 *
 * 背景虚化自定义View
 *
 * Author LinJl
 *
 * Ver 1.0, 18-04-02, LinJl, Create file
 */
package com.tplink.gallery.view;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.tplink.gallery.R;

public class RefocusImageView extends android.support.v7.widget.AppCompatImageView
        implements View.OnTouchListener {

    private static final int DISMISS_FOCUS_RECT = 1;
    private static int DISMISS_FOCUS_RECT_DELAY = 2000;
    private int rotate = 0;
    private OnImageTapUp onImageTapUp;
    private int focusX  = -1;
    private int focusY = -1;
    private Drawable mFocusDrawable = null;
    private Handler mHandler;

    public RefocusImageView(Context context) {
        this(context, null);
    }

    public RefocusImageView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RefocusImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
        mHandler = new Handler(){
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                switch (msg.what) {
                    case DISMISS_FOCUS_RECT:
                        focusY = -1;
                        focusX = -1;
                        postInvalidate();
                        break;
                }
            }
        };
    }

    private void initView() {
        setOnTouchListener(this);
        mFocusDrawable = getResources().getDrawable(R.drawable.m_refocus_refocus);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        Drawable drawable = getDrawable();
        if (drawable == null) {
            return;
        }
        calMatrix(MeasureSpec.getSize(widthMeasureSpec),MeasureSpec.getSize(heightMeasureSpec));
    }

    public void setRotate(int rotate) {
        this.rotate = rotate;
    }

    public void setImageBitmap(Bitmap bm, float x, float y) {
        this.setImageBitmap(bm);
        float[] points = new float[2];
        getImageMatrix().mapPoints(points, new float[]{x, y});
        setFocusPoint(points[0], points[1]);
    }

    @Override
    public void setImageBitmap(Bitmap bm) {
        super.setImageBitmap(bm);
        calMatrix(getWidth(), getHeight());
    }


    private void calMatrix(int width, int height) {
        Drawable drawable = getDrawable();
        if (width == 0 || height == 0 || drawable == null) {
            return;
        }
        Matrix matrix = new Matrix();
        // 1.将图片居中
        // 2.旋转图片
        matrix.postRotate(-rotate, drawable.getIntrinsicWidth() / 2,
                drawable.getIntrinsicHeight() / 2);
        float scaleX = ((width) / 1.0F / ((rotate % 180 == 0)
                ? drawable.getIntrinsicWidth() : drawable.getIntrinsicHeight()));
        float scaleY = (height / 1.0F / ((rotate % 180 == 0)
                ? drawable.getIntrinsicHeight() : drawable.getIntrinsicWidth()));
        float scale = Math.min(scaleX, scaleY);
        matrix.postScale(scale, scale, drawable.getIntrinsicWidth() / 2,
                drawable.getIntrinsicHeight() / 2);
        matrix.postTranslate((width - drawable.getIntrinsicWidth()) / 2,
                (height - drawable.getIntrinsicHeight()) / 2);
        setImageMatrix(matrix);
        postInvalidate();
    }

    public void setFocusPoint(float focusX, float focusY) {
        this.focusX = (int) focusX;
        this.focusY = (int) focusY;
        postInvalidate();
        mHandler.removeMessages(DISMISS_FOCUS_RECT);
        Message message = mHandler.obtainMessage(DISMISS_FOCUS_RECT);
        mHandler.sendMessageDelayed(message, DISMISS_FOCUS_RECT_DELAY);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (mFocusDrawable != null && focusY != -1) {

            int width = mFocusDrawable.getIntrinsicWidth() * 2 / 3;
            mFocusDrawable.setBounds(focusX - width / 2,
                    focusY - width / 2,
                    focusX + width,
                    focusY + width);
            mFocusDrawable.draw(canvas);
        }
    }

    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        switch (motionEvent.getAction()) {

            case MotionEvent.ACTION_UP:
                // 计算点击的图片的位置
                RectF rect = new RectF(0, 0 , getDrawable().getIntrinsicWidth(),
                        getDrawable().getIntrinsicHeight());
                getImageMatrix().mapRect(rect);
                if (rect.contains(motionEvent.getX(), motionEvent.getY())) {
                    setFocusPoint(motionEvent.getX(), motionEvent.getY());
                    if (onImageTapUp != null) {
                        switch (rotate) {
                            case 0:
                                onImageTapUp.onImageTapUp(
                                        ((motionEvent.getX() - rect.left /1.0F) / rect.width()),
                                        ((motionEvent.getY() - rect.top /1.0F) / rect.height()));
                                break;
                            case 90:
                                onImageTapUp.onImageTapUp(
                                        ((rect.bottom - motionEvent.getY() /1.0F) / rect.height()),
                                        ((motionEvent.getX() - rect.left /1.0F) / rect.width()));
                                break;
                            case 180 :
                                onImageTapUp.onImageTapUp(
                                        ((rect.right - motionEvent.getX() /1.0F) / rect.width()),
                                        ((rect.bottom - motionEvent.getY() /1.0F) / rect.height()));
                                break;
                            case 270 :
                                onImageTapUp.onImageTapUp(
                                        (motionEvent.getY() - rect.top /1.0F) / rect.height(),
                                        (rect.right - motionEvent.getX()/1.0F) / rect.width());
                                break;
                        }

                    }
                }
                break;
        }
        return true;
    }

    public interface OnImageTapUp {
        void onImageTapUp(float x, float y);
    }

    public void setOnImageTapUp(OnImageTapUp onImageTapUp) {
        this.onImageTapUp = onImageTapUp;
    }
}
