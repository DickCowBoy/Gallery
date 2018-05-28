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

    public boolean needResolveBurst;
    public List<String> allowMimeTypes;
    public List<String> notAllowMimeTypes;

    public AllMediaBeanCollection(List<MediaBean> data, List<String> allowMimeTypes, List<String> notAllowMimeTypes, boolean needResolveBurst) {
        super(data);
        this.allowMimeTypes = allowMimeTypes;
        this.notAllowMimeTypes = notAllowMimeTypes;
        this.needResolveBurst = needResolveBurst;
    }

    @Override
    public String key() {
        return MediaUtils.getAllMediaKey(allowMimeTypes, notAllowMimeTypes, needResolveBurst);
    }
}
