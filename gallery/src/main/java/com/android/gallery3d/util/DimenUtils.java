package com.android.gallery3d.util;

import android.content.Context;

public class DimenUtils {

    public static float dpToPixel(Context context, float dp) {
        return context.getResources().getDisplayMetrics().density * dp;
    }

    public static int dpToPixel(Context context , int dp) {
        return Math.round(dpToPixel(context, (float) dp));
    }

}
