/*
 * Copyright (C) 2018, TP-LINK TECHNOLOGIES CO., LTD.
 *
 * AlbumBean.java
 *
 * Description 相册集合
 *
 * Author LinJinLong
 *
 * Ver 1.0, 2018-04-20 LinJinLong, Create file
 */
package com.tplink.gallery.bean;

import android.net.Uri;

import com.tplink.gallery.utils.MediaUtils;

public class AlbumBean {
    public long bucketId;
    public int coverId;
    public String displayName;
    public int count;
    public long lastModify;

    public Uri getContentUri() {
        return MediaUtils.getFileUri()
                .buildUpon().appendPath(String.valueOf(coverId)).build();
    }
}
