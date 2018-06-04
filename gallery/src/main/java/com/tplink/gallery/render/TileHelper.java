package com.tplink.gallery.render;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapRegionDecoder;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.util.Log;

import com.tplink.gallery.utils.Utils;

public class TileHelper {
    private static final String TAG = "TileHelper";
    protected BitmapRegionDecoder mRegionDecoder;
    protected int mImageWidth;
    protected int mImageHeight;
    protected int mLevelCount;
    private Drawable mScreenNail;

    public synchronized void clear() {
        mImageWidth = 0;
        mImageHeight = 0;
        mLevelCount = 0;
        mRegionDecoder = null;
    }

    // Caller is responsible to recycle the ScreenNail
    public synchronized void setScreenNail(
            Drawable screenNail, int width, int height) {
        mScreenNail = screenNail;
        mImageWidth = width;
        mImageHeight = height;
        mRegionDecoder = null;
        mLevelCount = calculateLevelCount();
    }

    public synchronized void setRegionDecoder(BitmapRegionDecoder decoder) {
        mRegionDecoder = decoder;
        mImageWidth = decoder.getWidth();
        mImageHeight = decoder.getHeight();
        mLevelCount = calculateLevelCount();
    }

    private int calculateLevelCount() {
        return Math.max(0, Utils.ceilLog2(
                (float) mImageWidth / mScreenNail.getIntrinsicWidth()));
    }

    // TODO reuse the tile bitmap
    public Bitmap getTile(int level, int x, int y, int tileSize) {
        return getTileWithoutReusingBitmap(level, x, y, tileSize);
    }

    private Bitmap getTileWithoutReusingBitmap(
            int level, int x, int y, int tileSize) {
        int t = tileSize << level;
        Rect wantRegion = new Rect(x, y, x + t, y + t);

        BitmapRegionDecoder regionDecoder;
        Rect overlapRegion;

        synchronized (this) {
            regionDecoder = mRegionDecoder;
            if (regionDecoder == null) return null;
            overlapRegion = new Rect(0, 0, mImageWidth, mImageHeight);
        }

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
        options.inPreferQualityOverSpeed = true;
        options.inSampleSize =  (1 << level);
        Bitmap bitmap = null;

        // In CropImage, we may call the decodeRegion() concurrently.
        synchronized (regionDecoder) {
            bitmap = regionDecoder.decodeRegion(overlapRegion, options);
        }

        if (bitmap == null) {
            Log.w(TAG, "fail in decoding region");
        }

        if (wantRegion.equals(overlapRegion)) return bitmap;

        Bitmap result = Bitmap.createBitmap(tileSize, tileSize, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(result);
        canvas.drawBitmap(bitmap,
                (overlapRegion.left - wantRegion.left) >> level,
                (overlapRegion.top - wantRegion.top) >> level, null);
        return result;
    }

}
