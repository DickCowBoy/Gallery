/*
 * Copyright (C) 2018, TP-LINK TECHNOLOGIES CO., LTD.
 *
 * BaseMediaBeanCollection.java
 *
 * Description 包含基本的更新操作
 *
 * Author LinJinLong
 *
 * Ver 1.0, 2018-04-20 LinJinLong, Create file
 */
package com.tplink.gallery.data;

import android.util.SparseIntArray;

import com.tplink.gallery.bean.MediaBean;

import java.util.List;

public abstract class BaseMediaBeanCollection extends MediaBeanCollection {



    @Override
    public void updateMediaBeans(List<MediaBean> beans) {

    }

    @Override
    public void delMediaBeans(SparseIntArray ids) {

    }
}
