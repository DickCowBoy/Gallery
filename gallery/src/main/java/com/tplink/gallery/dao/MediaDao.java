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
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.SparseIntArray;

import com.tplink.gallery.bean.MediaBean;
import com.tplink.gallery.utils.MediaUtils;

import java.util.List;

public class MediaDao extends BaseMediaDao {

    public MediaDao(Context context) {
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

    public List<MediaBean> queryVideoById(List<String> ids) {
        return queryVideo(MediaStore.Files.FileColumns._ID + " in " + buildInCondition(ids), null);
    }

    public List<MediaBean> queryImageById(List<String> ids) {
        return queryImage(MediaStore.Files.FileColumns._ID + " in " + buildInCondition(ids), null, true);
    }

    public SparseIntArray queryAllMediaIds() {
        SparseIntArray array = new SparseIntArray();
        query(MediaUtils.getImageUri(), new String[]{MediaStore.Files.FileColumns._ID}, null, null, DATA_MODIFY_DESC,
                new IntCursorProcessor(array));
        query(MediaUtils.getVideoUri(), new String[]{MediaStore.Files.FileColumns._ID}, null, null, DATA_MODIFY_DESC,
                new IntCursorProcessor(array));
        return array;
    }

    private String buildInCondition(List<String> ids) {
        StringBuilder sb = new StringBuilder();
        sb.append("(");
        for ( int i = 0; i < ids.size() - 2; i ++) {
            sb.append(ids.get(i) +" , ");
        }
        sb.append(")");
        return sb.toString();
    }

    private class IntCursorProcessor implements CursorProcessor<SparseIntArray> {

        SparseIntArray array;
        public IntCursorProcessor(SparseIntArray array) {
            this.array = array;
        }

        @Override
        public SparseIntArray process(Cursor cursor) {
            int anInt;
            while (cursor.moveToNext()) {
                anInt = cursor.getInt(0);
                array.append(anInt, anInt);
            }
            return array;
        }
    }
}
