/*
 * Copyright (C) 2018, TP-LINK TECHNOLOGIES CO., LTD.
 *
 * AllMediaBeanCollection.java
 *
 * Description 缓存所有图片信息
 *
 * Author LinJinLong
 *
 * Ver 1.0, 2018-04-25 LinJinLong, Create file
 */
package com.tplink.gallery.data;

import com.tplink.gallery.bean.MediaBean;
import com.tplink.gallery.data.MediaBeanCollection;
import com.tplink.gallery.utils.MediaUtils;

import java.util.List;


public class AllMediaBeanCollection extends MediaBeanCollection<MediaBean> {

    public boolean needVideo;
    public boolean needImage;
    public boolean needGif;
    public boolean needResolveBurst;

    public AllMediaBeanCollection(List<MediaBean> data, boolean needVideo, boolean needImage, boolean needGif, boolean needResolveBurst) {
        super(data);
        this.needVideo = needVideo;
        this.needImage = needImage;
        this.needGif = needGif;
        this.needResolveBurst = needResolveBurst;
    }

    @Override
    public String key() {
        return MediaUtils.getAllMediaKey(needVideo, needImage, needGif, needResolveBurst);
    }
}
