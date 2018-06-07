/*
 * Copyright (C) 2016, TP-LINK TECHNOLOGIES CO., LTD.
 *
 * ImageUtils.java
 *
 * Description
 *
 * Author huwei
 *
 * Ver 1.0, 2016-12-28, huwei, Create file
 */
package com.android.gallery3d.util;

//缩略图需要识别的图片种类 从1000开始   不要这VideoUtils里面的种类重复
public class SourceType {
    public static final int INVALIDE = -1;

    //视频种类
    public static final int VIDEO_COM =  1;
    public static final int VIDEO_SLOW_MOTION = 2;   //慢动作视频
    public static final int VIDEO_TIME_LAPSE = 3;   //延时视频

    public static final int PANORAMA = 1000;
    public static final int BURST = 1001;
}
