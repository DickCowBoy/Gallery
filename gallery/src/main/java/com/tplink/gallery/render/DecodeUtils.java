package com.tplink.gallery.render;

import android.graphics.BitmapRegionDecoder;
import android.util.Log;

import java.io.FileDescriptor;

public class DecodeUtils {

    private static final String TAG = "DecodeUtils";

    public static BitmapRegionDecoder createBitmapRegionDecoder(String filePath, boolean shareable) {
        try {
            return BitmapRegionDecoder.newInstance(filePath, shareable);
        } catch (Throwable t)  {
            Log.w(TAG, t);
            return null;
        }
    }

    public static BitmapRegionDecoder createBitmapRegionDecoder(FileDescriptor fd, boolean shareable) {
        try {
            return BitmapRegionDecoder.newInstance(fd, shareable);
        } catch (Throwable t)  {
            Log.w(TAG, t);
            return null;
        }
    }

}
