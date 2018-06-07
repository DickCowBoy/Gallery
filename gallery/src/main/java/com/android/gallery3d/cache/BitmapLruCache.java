/*
 * Copyright (C) 2016, TP-LINK TECHNOLOGIES CO., LTD.
 *
 * BitmapLruCache.java
 *
 * Description
 *
 * Author huwei
 *
 * Ver 1.0, 2016-10-28, huwei, Create file
 */
package com.android.gallery3d.cache;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.v4.util.LruCache;
import android.util.Log;


/**
 * 能够在多线程环境使用的LruCache
 */
public class BitmapLruCache extends LruCache<Key,Bitmap> {
    public static final String TAG = "BitmapLruCache";

    private static BitmapLruCache sBitmapLruCache;
    /**
     * Constructor for LruResourceCache.
     *
     * @param size The maximum size in bytes the in memory cache can use.
     */
    public BitmapLruCache(int size) {
        super(size);
    }

    public static synchronized BitmapLruCache getBitmapCache(Context context) {
        if(sBitmapLruCache == null){
            sBitmapLruCache = new BitmapLruCache(MemorySizeCalculator.get(context).getMemoryCacheSize());
        }
        return sBitmapLruCache;
    }

    @Override
    protected int sizeOf(Key key, Bitmap bitmap) {
        return bitmap.getAllocationByteCount();
    }

    @Override
    protected void entryRemoved(boolean evicted, Key key, Bitmap oldValue, Bitmap newValue) {
        super.entryRemoved(evicted, key, oldValue, newValue);

        Log.i(TAG,"entryRemoved");

        //不放入BitmapPool 会出现正在使用的图片被  回收问题
//        GalleryBitmapPool.getInstance().put(oldValue);
        //回收图片  交给GalleryBitmapPool去处理 是否回收 还是重用
//        oldValue.recycle();
        oldValue = null;
    }
}
