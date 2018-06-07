/*
 * Copyright (C) 2016, TP-LINK TECHNOLOGIES CO., LTD.
 *
 * LogUtil.java
 *
 * Description
 *
 * Author huwei
 *
 * Ver 1.0, 16-9-12, huwei, Create file
 */
package com.android.gallery3d.util;

import android.util.Log;

public class LogUtil {
    public static final boolean LOGON = false;
    public static final String BUGLE_TAG = "BUGLE_TAG";

    public static void v(String tag, String msg) {
        if (LOGON) {
            Log.v(tag, msg);
        }
    }

    public static void v(String tag, String msg, Throwable tr) {
        if (LOGON) {
            Log.v(tag, msg, tr);
        }
    }

    public static void d(String tag, String msg) {
        if (LOGON) {
            Log.d(tag, msg);
        }
    }

    public static void d(String tag, String msg, Throwable tr) {
        if (LOGON) {
            Log.d(tag, msg, tr);
        }
    }

    public static void i(String tag, String msg) {
        if (LOGON) {
            Log.i(tag, msg);
        }
    }

    public static void i(String tag, String msg, Throwable tr) {
        if (LOGON) {
            Log.i(tag, msg, tr);
        }
    }

    // w和 e的Log一定要输出
    public static void w(String tag, String msg) {
        Log.w(tag, msg);
    }

    public static void w(String tag, String msg, Throwable tr) {
        Log.w(tag, msg, tr);
    }

    public static void w(String tag, Throwable tr) {
        Log.w(tag, tr);
    }

    public static void e(String tag, String msg) {
        Log.e(tag, msg);
    }

    public static void e(String tag, String msg, Throwable tr) {
        Log.e(tag, msg, tr);
    }
}
