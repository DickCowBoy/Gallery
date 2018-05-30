/*
 * Copyright (C) 2018, TP-LINK TECHNOLOGIES CO., LTD.
 *
 * BigImageView.java
 *
 * Description 添加绘制背景避免加载过后图片出现较突兀
 *
 * Author LinJinLong
 *
 * Ver 1.0, 2018-04-26 LinJinLong, Create file
 */
package com.tplink.gallery.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.widget.ImageView;

@SuppressLint("AppCompatCustomView")
public class BigImageView extends ImageView {
    private float drawableDx;
    private Paint backPaint;
    public BigImageView(Context context) {
        this(context, null);
    }

    public BigImageView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public BigImageView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }

    private void initView() {
        backPaint = new Paint();
        backPaint.setAntiAlias(true);
        backPaint.setStyle(Paint.Style.FILL);
        backPaint.setColor(Color.GRAY);
    }

    public void setBackDx(float dx) {
        drawableDx = dx;
        postInvalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (getDrawable() == null) {
            drawBack(canvas);
        }
    }

    private void drawBack(Canvas canvas) {
        if (Float.compare(drawableDx, 0F) != 0) {
            // 计算位置范围
            int compare = Float.compare(drawableDx, (getWidth() / 1.0F / getHeight()));
            if (compare < 0) {
                int width = (int) (getHeight() * drawableDx);
                width = (getWidth() - width) /2;
                canvas.drawRect(width, 0, getWidth() - width, getHeight(), backPaint);
            } else if (compare > 0) {
                int height = (int) (getWidth() / drawableDx);
                height = (getHeight() - height) / 2;
                canvas.drawRect(0, height, getWidth(), getHeight() - height, backPaint);
            } else {
                canvas.drawRect(0, 0, getWidth(), getHeight(), backPaint);
            }
            //canvas.drawRect();
        }
    }
}
