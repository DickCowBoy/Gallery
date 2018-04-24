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
import android.util.SparseIntArray;

import com.tplink.gallery.bean.AlbumBean;
import com.tplink.gallery.bean.MediaBean;
import com.tplink.gallery.media.MediaColumn;
import com.tplink.gallery.utils.MediaUtils;

import java.util.List;

public class MediaDao extends BaseMediaDao {

    public MediaDao(Context context) {
        super(context);
    }

    public List<MediaBean> queryAllMedia(boolean queryVideo, boolean queryImage, boolean queryGif, boolean needResolveBurst) {

        String selection = null;
        String[] selectionArgs = null;
        if (queryVideo && ! queryImage) {
            return queryVideo(null, null);
        }  else if (queryImage && !queryVideo) {
            return queryImage(null, null, queryGif);
        } else if (queryImage && queryVideo) {
            selection = SELECTION_ALL;
            selectionArgs = SELECTION_ALL_ARGS;
        }
        return queryFile(selection, selectionArgs, queryGif);
    }

    public List<AlbumBean> queryAllAlbum(boolean queryVideo, boolean queryImage, boolean queryGif, boolean needResolveBurst) {
        String selection = null;
        String[] selectionArgs = null;

        if (queryVideo && ! queryImage) {
            query(MediaUtils.getVideoUri(),
                    MediaColumn.ALBUM_PROJECTION,
                    ") GROUP BY (bucket_id",
                    null, DATA_MODIFY_DESC,
                    cursor -> MediaColumn.parseAlbum(cursor));
        }  else if (queryImage && !queryVideo) {
            if (!queryGif) {
                if (TextUtils.isEmpty(selection)) {
                    selection = MediaStore.Files.FileColumns.MIME_TYPE + "!='image/gif'";
                } else {
                    selection += " AND " + MediaStore.Files.FileColumns.MIME_TYPE + "!='image/gif'";
                }
            }
            query(MediaUtils.getImageUri(),
                    MediaColumn.ALBUM_PROJECTION,
                    (TextUtils.isEmpty(selection) ? "" : selection) + ") GROUP BY (bucket_id",
                    null, DATA_MODIFY_DESC,
                    cursor -> MediaColumn.parseAlbum(cursor));
        } else if (queryImage && queryVideo) {
            selection = SELECTION_ALL;
            selectionArgs = SELECTION_ALL_ARGS;
        }
        return query(MediaUtils.getFileUri(),
                MediaColumn.ALBUM_PROJECTION,
                selection + ") GROUP BY (bucket_id",
                selectionArgs,
                DATA_MODIFY_DESC,
                cursor -> MediaColumn.parseAlbum(cursor));

    }

    private  AlbumBean parseAlbum(Cursor cursor) {
        return null;
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

    public List<MediaBean> queryMediaByBucketId(long bucketId, boolean queryVideo, boolean queryImage, boolean queryGif) {

        String selection = MediaStore.Images.ImageColumns.BUCKET_ID + " = ?";
        String[] selectionArgs = new String[]{String.valueOf(bucketId)};
        if (queryVideo && ! queryImage) {
            return queryVideo(null, null);
        }  else if (queryImage && !queryVideo) {
            return queryImage(null, null, queryGif);
        } else if (queryImage && queryVideo) {
            selection = selection + " AND " + SELECTION_ALL;
            selectionArgs = new String[]{
                    String.valueOf(bucketId),
                    String.valueOf(MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE),
                    String.valueOf(MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO),
            };
        }
        return queryFile(selection, selectionArgs, queryGif);
    }
}
