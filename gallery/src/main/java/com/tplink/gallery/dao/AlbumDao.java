/*
 * Copyright (C) 2018, TP-LINK TECHNOLOGIES CO., LTD.
 *
 * AlbumDao.java
 *
 * Description 相册相关查询
 *
 * Author LinJinLong
 *
 * Ver 1.0, 2018-04-19 LinJinLong, Create file
 */
package com.tplink.gallery.dao;

import android.content.Context;
import android.provider.MediaStore;
import android.util.SparseIntArray;

import com.tplink.gallery.bean.MediaBean;

import java.util.List;

public class AlbumDao extends BaseMediaDao{

    public  AlbumDao(Context context) {
        super(context);
    }

    public List<MediaBean> queryMediasByBucket(long bucketId, boolean queryVideo) {
        return queryVideo ? queryFile(MediaStore.Video.VideoColumns.BUCKET_ID,
                new String[]{String.valueOf(bucketId)}, true) :
                queryImage(MediaStore.Video.VideoColumns.BUCKET_ID,
                        new String[]{String.valueOf(bucketId)}, true);
    }

}
