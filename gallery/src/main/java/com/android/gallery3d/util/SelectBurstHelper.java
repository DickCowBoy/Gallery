package com.android.gallery3d.util;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.view.MarginLayoutParamsCompat;
import android.support.v4.view.ViewCompat;
import android.view.View;
import android.view.ViewGroup;

import java.io.Closeable;
import java.io.IOException;

public class SelectBurstHelper {

    public static int getMeasuredWidth(View v) {
        return (v == null) ? 0 : v.getMeasuredWidth();
    }

    public static int getWidth(View v) {
        return (v == null) ? 0 : v.getWidth();
    }

    public static int getWidthWithMargin(View v) {
        return getWidth(v) + getMarginHorizontally(v);
    }

    public static int getStart(View v) {
        return getStart(v, false);
    }

    public static int getStart(View v, boolean withoutPadding) {
        if (v == null) {
            return 0;
        }
        if (isLayoutRtl(v)) {
            return (withoutPadding) ? v.getRight() - getPaddingStart(v) : v.getRight();
        } else {
            return (withoutPadding) ? v.getLeft() + getPaddingStart(v) : v.getLeft();
        }
    }

    public static int getEnd(View v) {
        return getEnd(v, false);
    }

    public static int getEnd(View v, boolean withoutPadding) {
        if (v == null) {
            return 0;
        }
        if (isLayoutRtl(v)) {
            return (withoutPadding) ? v.getLeft() + getPaddingEnd(v) : v.getLeft();
        } else {
            return (withoutPadding) ? v.getRight() - getPaddingEnd(v) : v.getRight();
        }
    }

    public static int getPaddingStart(View v) {
        if (v == null) {
            return 0;
        }
        return ViewCompat.getPaddingStart(v);
    }

    public static int getPaddingEnd(View v) {
        if (v == null) {
            return 0;
        }
        return ViewCompat.getPaddingEnd(v);
    }

    public static int getPaddingHorizontally(View v) {
        if (v == null) {
            return 0;
        }
        return v.getPaddingLeft() + v.getPaddingRight();
    }

    public static int getMarginStart(View v) {
        if (v == null) {
            return 0;
        }
        ViewGroup.MarginLayoutParams lp = (ViewGroup.MarginLayoutParams) v.getLayoutParams();
        return MarginLayoutParamsCompat.getMarginStart(lp);
    }

    public static int getMarginEnd(View v) {
        if (v == null) {
            return 0;
        }
        ViewGroup.MarginLayoutParams lp = (ViewGroup.MarginLayoutParams) v.getLayoutParams();
        return MarginLayoutParamsCompat.getMarginEnd(lp);
    }

    public static int getMarginHorizontally(View v) {
        if (v == null) {
            return 0;
        }
        ViewGroup.MarginLayoutParams lp = (ViewGroup.MarginLayoutParams) v.getLayoutParams();
        return MarginLayoutParamsCompat.getMarginStart(lp) + MarginLayoutParamsCompat.getMarginEnd(lp);
    }

    public static boolean isLayoutRtl(View v) {
        return ViewCompat.getLayoutDirection(v) == ViewCompat.LAYOUT_DIRECTION_RTL;
    }

    public static void closeSilently(Closeable c) {
        if (c == null) {
            return;
        }
        try {
            c.close();
        } catch (IOException t) {
        }
    }

    private static boolean isUiThread() {
        return (Looper.myLooper() == Looper.getMainLooper());
    }

    public static void runOnUiThread(final Context context, Runnable r) {
        if (isUiThread()) {
            r.run();
        } else {
            Handler handler = new Handler(context.getMainLooper());
            handler.post(r);
        }
    }

    private SelectBurstHelper() {

    }

}