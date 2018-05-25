package com.tplink.gallery.view;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;

public class DrawContent {
    public int width;
    public int height;
    private Drawable content;
    public float originScale;
    public static Paint paint;
    static {
        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setAntiAlias(true);
        paint.setDither(true);
    }

    public void setContent(Drawable content) {
        this.content = content;
    }

    public Drawable getContent() {
        return content;
    }

    public static Paint bgPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    static {
        bgPaint.setColor(Color.GRAY);
        bgPaint.setAntiAlias(true);
        bgPaint.setStyle(Paint.Style.FILL);
    }

    public int getShowWidth() {
        return (int) (width * originScale);
    }

    public void drawContent(Canvas canvas, Matrix matrix) {
        if (content != null) {
            final int saveCount = canvas.getSaveCount();
            canvas.save();
            canvas.concat(matrix);
            content.setBounds(0, 0, width, height);
            content.draw(canvas);
            canvas.restoreToCount(saveCount);
//            if (content instanceof BitmapDrawable) {
//                canvas.drawBitmap(((BitmapDrawable)content).getBitmap(), matrix, paint);
//            }
//            canvas.setMatrix(matrix);
//            content.draw(canvas);
        } else {
            canvas.drawRect(getShowRect(matrix), bgPaint);
        }
    }

    public int getShowHeight() {
        return (int) (height * originScale);
    }

    public RectF getShowRect(Matrix matrix) {
        RectF rectF = new RectF(0, 0, width, height);
        matrix.mapRect(rectF);
        return rectF;
    }

    public BigImageViewController.AnimMatrix calMatrix(int width, int height, float normalScale) {
        if (width == 0 || height == 0) {
            return null;
        }
        BigImageViewController.AnimMatrix matrix = new BigImageViewController.AnimMatrix();
        // 1.将图片居中
        // 2.旋转图片
        float scaleX = ((width) / 1.0F / this.width);
        float scaleY = (height / 1.0F / this.height);
        originScale = Math.min(scaleX, scaleY);
        matrix.postScale(originScale * normalScale, originScale * normalScale,
                this.width / 2,
                this.height / 2);
        matrix.postTranslate((width - this.width) / 2,
                (height - this.height) / 2);
        matrix.baseScale = normalScale;
        return matrix;
    }

    public BigImageViewController.AnimMatrix calMatrix(int width, int height) {
        return calMatrix(width, height, 1.0F);
    }
}
