/*
 * Copyright (C) 2018, TP-LINK TECHNOLOGIES CO., LTD.
 *
 * MediaBeanCollection.java
 *
 * Description 以一定规则组成的媒体集合
 *
 * Author LinJinLong
 *
 * Ver 1.0, 2018-04-20 LinJinLong, Create file
 */
package com.tplink.gallery.data;

import android.util.SparseIntArray;

import com.tplink.gallery.bean.MediaBean;

import java.util.List;

public abstract class MediaBeanCollection {
    public String key;

    public abstract void updateMediaBeans(List<MediaBean> beans);
    public abstract void delMediaBeans(SparseIntArray ids);
}
