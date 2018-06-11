/*
 * Copyright (C) 2018, TP-LINK TECHNOLOGIES CO., LTD.
 *
 * TPGalleryJNi.java
 *
 * 背景虚化jni接口类
 *
 * Author LinJl
 *
 * Ver 1.0, 18-04-02, LinJl, Create file
 */
package com.tplink.gallery;

public class TPGalleryJNi {

    public static final String REFOCUS_PACKAGE = "com.mediatek.refocus";
    private static boolean isNativeSupport = false;

    static {
        try {
            System.loadLibrary("tplink_gallery");
            isNativeSupport = true;
        } catch (UnsatisfiedLinkError e) {
            isNativeSupport = false;
            e.printStackTrace();
        }
    }

    private TPGalleryJNi(){}


    /**
     * 通过ARGB获取NV21格式数据
     *
     * @param argb
     * @param nv21
     * @param rotate
     */
    public static native void getNV21ByARGB(byte[] argb, byte[] nv21, int rotate, int width, int height);

    public static native int decodeNV21ToJpeg(String jpegFile, String nv21File, long width, long height);

    public static native int getNV21AndScaleByJpeg(byte[] jpeg, byte[] result, int rotate, int width, int height);

    /**
     * @param calibration
     * @param depth
     * @param main
     * @param width
     * @param height
     * @param auxWdith
     * @param auxHeight
     * @param blur
     * @param focusX
     * @param focusY
     * @param result      argb 格式数据直接显示图像
     */
    public static native void processRefocusPreview(byte[] calibration,
                                                    byte[] depth,
                                                    byte[] main,
                                                    int width,
                                                    int height,
                                                    int auxWdith,
                                                    int auxHeight,
                                                    int blur,
                                                    int focusX,
                                                    int focusY,
                                                    byte[] result,
                                                    int orientation,
                                                    boolean hasWatermark,// 是否存在水印
                                                    String model);// 水印类型

    /**
     * @param calibration
     * @param depth
     * @param main        主摄像头JPEG数据
     * @param width
     * @param height
     * @param auxWdith
     * @param auxHeight
     * @param blur
     * @param focusX
     * @param focusY
     * @param orientation 处理后需要旋转的方向
     * @return 处理后jpeg图像
     */
    public static native byte[] processRefocusCapture(byte[] calibration,
                                                      byte[] depth,
                                                      byte[] main,
                                                      int width,
                                                      int height,
                                                      int auxWdith,
                                                      int auxHeight,
                                                      int blur,
                                                      int focusX,
                                                      int focusY,
                                                      int orientation,
                                                      String cacheFile,
                                                      boolean hasWatermark,// 是否存在水印
                                                      String model,// 水印类型
                                                      byte[] exif);// Exif信息

    /**
     * 返回支持refocus的类型
     * -1不支持 0 MTK 1 ARCSOFT
     * @return
     */
    public static native int getRefocusType();

    public static boolean isIsNativeSupport() {
        return isNativeSupport;
    }
}
