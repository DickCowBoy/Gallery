/*
 * Copyright (C) 2017, TP-LINK TECHNOLOGIES CO., LTD.
 *
 * UndoBgTexture.java
 *
 *  undo背景
 *
 * Author Lin JinLong
 *
 * Ver 1.0, 2017-05-15, Lin JinLong, Create file.
 *
 */
package com.android.gallery3d.glrenderer;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

public class UndoBgTexture extends CanvasTexture {

    Paint paint;
    private int rount = 10;// 圆角
    public UndoBgTexture(int width, int height, int rount) {
        super(width, height);
        paint = new Paint();
        paint.setAntiAlias(true);
        paint.setColor(Color.rgb(0x3C, 0X3C, 0X3C));// 给定颜色
        this.rount = rount;
    }
    @Override
    protected void onDraw(Canvas canvas, Bitmap backing) {
        canvas.drawRoundRect(rount, 0, mWidth - rount, mHeight, rount, rount, paint);
    }
}
