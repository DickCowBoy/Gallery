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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public abstract class MediaBeanCollection<T> {

    public long lastLoad;
    public List<T> mediaBeans;

    public MediaBeanCollection(List<T> mediaBeans) {
        updateCollection(mediaBeans);
    }

    public abstract String key();

    public void updateCollection(List<T> mediaBeans) {
        this.mediaBeans = mediaBeans;
        lastLoad = System.currentTimeMillis();
    }
}
