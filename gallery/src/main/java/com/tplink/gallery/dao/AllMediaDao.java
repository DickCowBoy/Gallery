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
import android.text.TextUtils;

import com.tplink.gallery.bean.MediaBean;
import com.tplink.gallery.media.MediaColumn;
import com.tplink.gallery.utils.MediaUtils;

import java.util.List;

public class AllMediaDao extends BaseDao {

    private static final String SELECTION_ALL =
            "(" + MediaStore.Files.FileColumns.MEDIA_TYPE + "=?"
                    + " OR "
                    + MediaStore.Files.FileColumns.MEDIA_TYPE + "=?)"
                    + " AND " + MediaStore.MediaColumns.SIZE + ">0";

    private static final String[] SELECTION_ALL_ARGS = {
            String.valueOf(MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE),
            String.valueOf(MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO),
    };

    private static final String SELECTION_ALL_FOR_SINGLE_MEDIA_TYPE =
            MediaStore.Files.FileColumns.MEDIA_TYPE + "=?"
                    + " AND " + MediaStore.MediaColumns.SIZE + ">0";


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

        if (!queryGif) {
            if (TextUtils.isEmpty(selection)) {
                selection = MediaStore.Files.FileColumns.MIME_TYPE + "!='image/gif'";
            } else {
                selection += " AND " + MediaStore.Files.FileColumns.MIME_TYPE + "!='image/gif'";
            }
        }

        return query(uri, queryVideo ? MediaColumn.QUERY_PROJECTION : MediaColumn.QUERY_IMAGE_PROJECTION,
                selection, selectionArgs,
                MediaStore.Files.FileColumns.DATE_MODIFIED +" DESC",
                new CursorProcessor<List<MediaBean>>() {
                    @Override
                    public List<MediaBean> process(Cursor cursor) {
                        return queryVideo ? MediaColumn.parseVideo(cursor) : MediaColumn.parseImage(cursor);
                    }
        });
    }

    private static String[] getSelectionArgsForSingleMediaType(int mediaType) {
        return new String[]{String.valueOf(mediaType)};
    }
}
