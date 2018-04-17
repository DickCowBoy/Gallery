/*
 * Copyright (C) 2018, TP-LINK TECHNOLOGIES CO., LTD.
 *
 * MediaBean.java
 *
 * Description 媒体文件实体类
 *
 * 为了保证加载速度部部分字段直接加载部分字段在需要时才加载
 *
 * Author LinJinLong
 *
 * Ver 1.0, 2018-04-17 LinJinLong, Create file
 */
package com.tplink.gallery.bean;

public class MediaBean {

    public static final int SUPPORT_DELETE = 1 << 0;
    public static final int SUPPORT_ROTATE = 1 << 1;
    public static final int SUPPORT_SHARE = 1 << 2;
    public static final int SUPPORT_CROP = 1 << 3;
    public static final int SUPPORT_SHOW_ON_MAP = 1 << 4;
    public static final int SUPPORT_SET_AS = 1 << 5;
    public static final int SUPPORT_FULL_IMAGE = 1 << 6;
    public static final int SUPPORT_PLAY = 1 << 7;
    public static final int SUPPORT_CACHE = 1 << 8;
    public static final int SUPPORT_EDIT = 1 << 9;
    public static final int SUPPORT_INFO = 1 << 10;
    public static final int SUPPORT_TRIM = 1 << 11;
    public static final int SUPPORT_UNLOCK = 1 << 12;
    public static final int SUPPORT_BACK = 1 << 13;
    public static final int SUPPORT_ACTION = 1 << 14;
    public static final int SUPPORT_CAMERA_SHORTCUT = 1 << 15;
    public static final int SUPPORT_MUTE = 1 << 16;
    public static final int SUPPORT_PRINT = 1 << 17;
    public static final int SUPPORT_PLAY_GIF = 1 << 18; // a bit marks whether gif image can be play
    public static final int SUPPORT_REFOCUS = 1 << 19;
    public static final int SUPPORT_TP_LINK_REFOCUS = 1 << 20;
    public static final int SUPPORT_ALL = 0xffffffff;

    // 查询直接加载字段
    public long _id;
    public long bucketId;
    public int width;
    public int height;
    public String mimeType = null;
    public long lastModify;
    public long duration;
    public int refocusType;

    // 使用到时才加载


    public int supportOperation = 0;
}
