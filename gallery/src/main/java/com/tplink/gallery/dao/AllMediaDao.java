/*
 * Copyright (C) 2018, TP-LINK TECHNOLOGIES CO., LTD.
 *
 * AllMediaDao.java
 *
 * Description 查询所有媒体数据
 *
 * Author LinJinLong
 *
 * Ver 1.0, 2018-04-19 LinJinLong, Create file
 */
package com.tplink.gallery.dao;

import android.content.Context;
import android.net.Uri;

import com.tplink.gallery.bean.MediaBean;
import com.tplink.gallery.utils.MediaUtils;

import java.util.List;

public class AllMediaDao extends BaseMediaDao {

    public AllMediaDao(Context context) {
        super(context);
    }

    public List<MediaBean> queryAllMedia(final boolean queryVideo, boolean queryImage, boolean queryGif) {
        Uri uri = queryVideo ? MediaUtils.getFileUri() : MediaUtils.getFileUri();

        String selection = null;
        String[] selectionArgs = null;
        if (queryImage && queryVideo) {
            selection = SELECTION_ALL;
            selectionArgs = SELECTION_ALL_ARGS;
        }
        return queryVideo ? queryFile(selection, selectionArgs, queryGif)
                : queryImage(selection, selectionArgs, queryGif);
    }
}
