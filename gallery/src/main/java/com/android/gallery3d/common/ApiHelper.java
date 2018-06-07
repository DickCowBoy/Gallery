/*
 * Copyright (C) 2012 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.gallery3d.common;

import android.hardware.Camera;
import android.os.Build;
import android.provider.MediaStore.MediaColumns;
import android.view.View;

public class ApiHelper {
    public static interface VERSION_CODES {
        // These value are copied from Build.VERSION_CODES
        public static final int HONEYCOMB = 11;
        public static final int ICE_CREAM_SANDWICH = 14;
        public static final int JELLY_BEAN = 16;
        public static final int JELLY_BEAN_MR2 = 18;
        public static final int N = 24;
    }

    public static final boolean USE_888_PIXEL_FORMAT =
            Build.VERSION.SDK_INT >= VERSION_CODES.JELLY_BEAN;

    public static final boolean HAS_VIEW_SYSTEM_UI_FLAG_LAYOUT_STABLE =
            hasField(View.class, "SYSTEM_UI_FLAG_LAYOUT_STABLE");

    public static final boolean HAS_MEDIA_COLUMNS_WIDTH_AND_HEIGHT =
            hasField(MediaColumns.class, "WIDTH");

    public static final boolean HAS_REUSING_BITMAP_IN_BITMAP_REGION_DECODER =
            Build.VERSION.SDK_INT >= VERSION_CODES.JELLY_BEAN;

    public static final boolean HAS_INTENT_EXTRA_LOCAL_ONLY =
            Build.VERSION.SDK_INT >= VERSION_CODES.HONEYCOMB;

    public static final boolean HAS_SET_SYSTEM_UI_VISIBILITY =
            hasMethod(View.class, "setSystemUiVisibility", int.class);

    public static final boolean HAS_FACE_DETECTION;
    static {
        boolean hasFaceDetection = false;
        try {
            Class<?> listenerClass = Class.forName(
                    "android.hardware.Camera$FaceDetectionListener");
            hasFaceDetection =
                    hasMethod(Camera.class, "setFaceDetectionListener", listenerClass) &&
                    hasMethod(Camera.class, "startFaceDetection") &&
                    hasMethod(Camera.class, "stopFaceDetection") &&
                    hasMethod(Camera.Parameters.class, "getMaxNumDetectedFaces");
        } catch (Throwable t) {
        }
        HAS_FACE_DETECTION = hasFaceDetection;
    }

    public static final boolean HAS_MOTION_EVENT_TRANSFORM =
            Build.VERSION.SDK_INT >= VERSION_CODES.HONEYCOMB;

    public static final boolean HAS_MEDIA_PROVIDER_FILES_TABLE =
            Build.VERSION.SDK_INT >= VERSION_CODES.HONEYCOMB;

    public static final boolean HAS_OPTIONS_IN_MUTABLE =
            Build.VERSION.SDK_INT >= VERSION_CODES.HONEYCOMB;

    public static final boolean HAS_POST_ON_ANIMATION =
            Build.VERSION.SDK_INT >= VERSION_CODES.JELLY_BEAN;

    public static final boolean HAS_GLES20_REQUIRED =
            Build.VERSION.SDK_INT >= VERSION_CODES.HONEYCOMB;

    public static final boolean HAS_ORIENTATION_LOCK =
            Build.VERSION.SDK_INT >= VERSION_CODES.JELLY_BEAN_MR2;

    public static final boolean HAS_MULTI_WINDOW_MODE =
            Build.VERSION.SDK_INT >= VERSION_CODES.N;

    private static boolean hasField(Class<?> klass, String fieldName) {
        try {
            klass.getDeclaredField(fieldName);
            return true;
        } catch (NoSuchFieldException e) {
            return false;
        }
    }

    private static boolean hasMethod(
            Class<?> klass, String methodName, Class<?> ... paramTypes) {
        try {
            klass.getDeclaredMethod(methodName, paramTypes);
            return true;
        } catch (NoSuchMethodException e) {
            return false;
        }
    }
}
