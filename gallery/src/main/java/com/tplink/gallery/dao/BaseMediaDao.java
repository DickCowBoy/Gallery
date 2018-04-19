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

public class BaseMediaDao extends BaseDao {

    protected static final String SELECTION_ALL =
            "(" + MediaStore.Files.FileColumns.MEDIA_TYPE + "=?"
                    + " OR "
                    + MediaStore.Files.FileColumns.MEDIA_TYPE + "=?)"
                    + " AND " + MediaStore.MediaColumns.SIZE + ">0";

    protected static final String[] SELECTION_ALL_ARGS = {
            String.valueOf(MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE),
            String.valueOf(MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO),
    };

    protected static final String SELECTION_ALL_FOR_SINGLE_MEDIA_TYPE =
            MediaStore.Files.FileColumns.MEDIA_TYPE + "=?"
                    + " AND " + MediaStore.MediaColumns.SIZE + ">0";

    protected static String DATA_MODIFY_DESC = MediaStore.Files.FileColumns.DATE_MODIFIED +" DESC";

    public BaseMediaDao(Context context) {
        super(context);
    }

    public List<MediaBean> queryFile(String selection, String[] selectionArgs, boolean queryGif) {

        if (!queryGif) {
            if (TextUtils.isEmpty(selection)) {
                selection = MediaStore.Files.FileColumns.MIME_TYPE + "!='image/gif'";
            } else {
                selection += " AND " + MediaStore.Files.FileColumns.MIME_TYPE + "!='image/gif'";
            }
        }

        return query(MediaUtils.getFileUri(), MediaColumn.QUERY_PROJECTION,
                selection, selectionArgs,
                DATA_MODIFY_DESC,
                cursor->MediaColumn.parseVideo(cursor)
        );
    }

    public List<MediaBean> queryImage(String selection, String[] selectionArgs, boolean queryGif) {

        if (!queryGif) {
            if (TextUtils.isEmpty(selection)) {
                selection = MediaStore.Files.FileColumns.MIME_TYPE + "!='image/gif'";
            } else {
                selection += " AND " + MediaStore.Files.FileColumns.MIME_TYPE + "!='image/gif'";
            }
        }

        return query(MediaUtils.getImageUri(), MediaColumn.QUERY_IMAGE_PROJECTION,
                selection, selectionArgs,
                DATA_MODIFY_DESC,
                cursor->MediaColumn.parseImage(cursor)
        );
    }

    public List<MediaBean> queryVideo(String selection, String[] selectionArgs, boolean queryGif) {
        if (!queryGif) {
            if (TextUtils.isEmpty(selection)) {
                selection = MediaStore.Files.FileColumns.MIME_TYPE + "!='image/gif'";
            } else {
                selection += " AND " + MediaStore.Files.FileColumns.MIME_TYPE + "!='image/gif'";
            }
        }

        return query(MediaUtils.getVideoUri(), MediaColumn.QUERY_IMAGE_PROJECTION,
                selection, selectionArgs,
                DATA_MODIFY_DESC,
                cursor->MediaColumn.parseImage(cursor)
        );
    }
}
