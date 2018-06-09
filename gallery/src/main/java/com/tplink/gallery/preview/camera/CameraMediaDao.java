package com.tplink.gallery.preview.camera;

import android.content.Context;
import android.database.Cursor;
import android.provider.MediaStore;

import com.android.gallery3d.util.GalleryUtils;
import com.tplink.gallery.bean.MediaBean;
import com.tplink.gallery.dao.MediaDao;
import com.tplink.gallery.media.MediaColumn;
import com.tplink.gallery.utils.MediaUtils;
import com.tplink.utils.NoneBoundArrayList;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CameraMediaDao extends MediaDao {
    public CameraMediaDao(Context context) {
        super(context);
    }

    public List<MediaBean> queryAllCamera() {

        List<String> dcimPaths = query(
                MediaUtils.getFileUri(),
                new String[]{MediaStore.Files.FileColumns.DATA},
                "parent = 0 AND _data like '%DCIM'",
                null,
                DATA_MODIFY_DESC,
                cursor->parseCursorToString(cursor)
        );
        StringBuilder builder = new StringBuilder(SELECTION_ALL);
        builder.append(" AND " + buildLikes(dcimPaths));
        return query(
                MediaUtils.getFileUri(),
                MediaColumn.QUERY_PROJECTION,
                builder.toString(),
                SELECTION_ALL_ARGS,
                DATA_MODIFY_DESC,
                cursor -> parseCursor(dcimPaths, cursor)
        );
    }

    private String buildLikes(List<String> dcimPaths) {
        StringBuilder sb = new StringBuilder("(");
        if (dcimPaths.size() > 0) {
            sb.append(MediaStore.Files.FileColumns.DATA +" like '" + dcimPaths.get(0) + "%'");
        }
        for (int i = 1; i < dcimPaths.size(); i++) {
            sb.append(" OR ");
            sb.append(MediaStore.Files.FileColumns.DATA +" like '" + dcimPaths.get(i) + "%'");
        }
        sb.append(")");
        return sb.toString();
    }

    public List<MediaBean> querySecureCameraMedia(List<String> ids) {
        return queryImageById(ids);
    }

    public static List<String> parseCursorToString(Cursor cursor) {
        List<String> result = new NoneBoundArrayList<>();
        if (cursor != null) {
            while (cursor.moveToNext()) {
                result.add(cursor.getString(0));
            }
        }
        return result;
    }

    public static String BURST_DIR_REGEX = "([0-9]{14})";
    public static List<MediaBean> parseCursor(List<String> paths,Cursor cursor) {
        List<MediaBean> result = new NoneBoundArrayList<>();
        List<Integer> burstBucketId = new ArrayList<>();
        for (String path : paths) {
            final File burstDir = new File(path + File.separator + "Burst" + File.separator);
            if (burstDir.exists() && burstDir.isDirectory()) {
                burstDir.list((file, s) -> {
                    File burstAlbum = new File(burstDir, s);
                    if (burstAlbum.isDirectory() && s.matches(BURST_DIR_REGEX)) {
                        burstBucketId.add(GalleryUtils.getBucketId(burstAlbum.getAbsolutePath()));
                    }
                    return false;
                });
            }
        }
        if (cursor != null) {
            Map<Long, MediaBean> resolve = new HashMap<>();

            if (cursor != null) {
                MediaBean bean;
                int anInt;
                while (cursor.moveToNext()) {
                    anInt = cursor.getInt(0);
                    // 检查是否存在
                    bean = MediaColumn.cacheItem(anInt);
                    bean.bucketId = cursor.getLong(1);
                    if (burstBucketId.contains((int)bean.bucketId)) {
                        if (resolve.containsKey(bean.bucketId)) {
                            resolve.get(bean.bucketId).burstCount++;
                            continue;
                        } else {
                            resolve.put(bean.bucketId, bean);
                            bean.isBurst = true;
                            bean.burstCount = 1;
                        }
                    }
                    bean.width = cursor.getInt(2);
                    bean.height = cursor.getInt(3);
                    bean.mimeType = cursor.getString(4);
                    bean.lastModify = cursor.getLong(5);
                    bean.duration = cursor.getLong(6);
                    bean.size = cursor.getLong(7);
                    bean.refocusType = cursor.getInt(8);
                    bean.orientation = cursor.getInt(9);
                    result.add(bean);
                }
            }
            return result;

        }

        return result;
    }
}
