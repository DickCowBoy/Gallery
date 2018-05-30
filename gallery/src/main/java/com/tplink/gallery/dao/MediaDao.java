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

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

public class MediaDao extends BaseMediaDao {

    public MediaDao(Context context) {
        super(context);
    }

    public List<MediaBean> queryAllMedia(List<String> allowMimeTypes, List<String> notAllowMimeTypes, boolean needResolveBurst, boolean needVideo, boolean needImage) {

        String selection = getSqlSelection(allowMimeTypes, notAllowMimeTypes);
        if (needVideo && ! needImage) {
            return queryVideo(selection, null);
        }  else if (needImage && !needVideo) {
            return queryImage(selection, null);
        } else {
            return queryFile(SELECTION_ALL, SELECTION_ALL_ARGS);
        }

    }

    public List<AlbumBean> queryAllAlbum(List<String> allowMimeTypes, List<String> notAllowMimeTypes, boolean needResolveBurst, boolean queryVideo, boolean queryImage) {
        String selection = getSqlSelection(allowMimeTypes, notAllowMimeTypes);
        Uri uri = MediaUtils.getFileUri();

        if (queryVideo && ! queryImage) {
            return query(MediaUtils.getVideoUri(), MediaColumn.ALBUM_PROJECTION,
                    (TextUtils.isEmpty(selection) ? "0=0" : selection) + ") GROUP BY (bucket_id",
                    null,DATA_MODIFY_DESC,
                    cursor -> MediaColumn.parseAlbum(cursor));
        }  else if (queryImage && !queryVideo) {
            return query(MediaUtils.getImageUri(), MediaColumn.ALBUM_PROJECTION,
                    (TextUtils.isEmpty(selection) ? "0=0" : selection) + ") GROUP BY (bucket_id",
                    null,DATA_MODIFY_DESC,
                    cursor -> MediaColumn.parseAlbum(cursor));
        } else {
            return query(uri,
                    MediaColumn.ALBUM_PROJECTION,
                    SELECTION_ALL + ") GROUP BY (bucket_id",
                    SELECTION_ALL_ARGS,
                    DATA_MODIFY_DESC,
                    cursor -> MediaColumn.parseAlbum(cursor));
        }

    }

    private  AlbumBean parseAlbum(Cursor cursor) {
        return null;
    }

    public List<MediaBean> queryVideoById(List<String> ids) {
        return queryVideo(MediaStore.Files.FileColumns._ID + " in " + buildInCondition(ids), null);
    }

    public List<MediaBean> queryImageById(List<String> ids) {
        return queryImage(MediaStore.Files.FileColumns._ID + " in " + buildInCondition(ids), null);
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

    public List<MediaBean> queryMediaByBucketId(long bucketId,
                                                List<String> allowMimeTypes, List<String> notAllowMimeTypes,
                                                boolean queryVideo, boolean queryImage) {

        String selection = MediaStore.Images.ImageColumns.BUCKET_ID + " = ?";
        String sqlSelection = getSqlSelection(allowMimeTypes, notAllowMimeTypes);
        if (!TextUtils.isEmpty(sqlSelection)) {
            selection += (" AND " + sqlSelection);
        }
        String[] selectionArgs = new String[]{String.valueOf(bucketId)};
        if (queryVideo && ! queryImage) {
            return queryVideo(selection, selectionArgs);
        }  else if (queryImage && !queryVideo) {
            return queryImage(selection, selectionArgs);
        }
        selection += " AND " + SELECTION_ALL;
        selectionArgs = new String[1 + SELECTION_ALL_ARGS.length];
        selectionArgs[0] = String.valueOf(bucketId);
        for (int i = 1; i < selectionArgs.length; i++) {
            selectionArgs[i] = SELECTION_ALL_ARGS[i -1];
        }
        return queryFile(selection, selectionArgs);
    }

    public String getSqlSelection(List<String> allowMimeTypes, List<String> notAllowMimeTypes) {
        if (allowMimeTypes != null && allowMimeTypes.size() > 0) {
            StringBuilder sb = new StringBuilder();
            sb.append(MediaStore.Files.FileColumns.MIME_TYPE +" IN (");
            for (int i = 0; i < allowMimeTypes.size() - 1; i++) {
                sb.append("\'");
                sb.append(allowMimeTypes.get(i));
                sb.append("\'");
                sb.append(",");
            }
            sb.append("\'");
            sb.append(allowMimeTypes.get(allowMimeTypes.size() -1));
            sb.append("\'");
            sb.append(" )");
            return sb.toString();
        } else if (notAllowMimeTypes != null && notAllowMimeTypes.size() > 0) {
            StringBuilder sb = new StringBuilder();
            sb.append(MediaStore.Files.FileColumns.MIME_TYPE +" NOT IN (");
            for (int i = 0; i < notAllowMimeTypes.size() - 1; i++) {
                sb.append("\'");
                sb.append(notAllowMimeTypes.get(i));
                sb.append("\'");
                sb.append(",");
            }
            sb.append("\'");
            sb.append(notAllowMimeTypes.get(notAllowMimeTypes.size() -1));
            sb.append("\'");
            sb.append(" )");
            return sb.toString();
        } else {
            return null;
        }
    }

    public List<MediaBean> getMediasByIds(Context context, List<Integer> allWallPaper) {
        return queryFile("_id in " + buildInArgs(allWallPaper), null);
    }

    private static String buildInArgs(List<Integer> allWallPaper) {
        StringBuilder sb = new StringBuilder("(");
        String flag = "";
        for (Integer integer : allWallPaper) {
            sb.append(flag+ integer);
            flag = ",";
        }
        sb.append(")");

        return sb.toString();
    }

}
