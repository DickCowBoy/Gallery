package com.tplink.gallery.media;

import android.database.Cursor;
import android.provider.MediaStore;

import com.tplink.gallery.bean.MediaBean;
import com.tplink.gallery.data.DataCacheManager;

import java.util.ArrayList;
import java.util.List;

public class MediaColumn {
    public static final String[] QUERY_PROJECTION = new String[]{
            MediaStore.Files.FileColumns._ID,
            MediaStore.Images.ImageColumns.BUCKET_ID,
            MediaStore.Images.ImageColumns.WIDTH,
            MediaStore.Images.ImageColumns.HEIGHT,
            MediaStore.Images.ImageColumns.MIME_TYPE,
            MediaStore.Images.ImageColumns.DATE_MODIFIED,
            MediaStore.Video.VideoColumns.DURATION,
            MediaStore.Images.ImageColumns.SIZE,
            "camera_refocus",// MTK媒体库特有字段识别是否是虚化拍摄
    };

    public static final String[] QUERY_IMAGE_PROJECTION = new String[]{
            MediaStore.Files.FileColumns._ID,
            MediaStore.Images.ImageColumns.BUCKET_ID,
            MediaStore.Images.ImageColumns.WIDTH,
            MediaStore.Images.ImageColumns.HEIGHT,
            MediaStore.Images.ImageColumns.MIME_TYPE,
            MediaStore.Images.ImageColumns.DATE_MODIFIED,
            MediaStore.Images.ImageColumns.SIZE,
            "camera_refocus",// MTK媒体库特有字段识别是否是虚化拍摄
    };

    public static List<MediaBean> parseImage(Cursor cursor) {
        List<MediaBean> result = new ArrayList<>();
        if (cursor != null) {
            MediaBean bean = null;
            int anInt;
            DataCacheManager dataCacheManager = DataCacheManager.getDataCacheManager();
            while (cursor.moveToNext()) {
                anInt = cursor.getInt(0);
                // 检查是否存在
                MediaBean cacheMediaBean = dataCacheManager.getCacheMediaBean(anInt);
                if (cacheMediaBean != null) {
                    bean = cacheMediaBean;
                } else {
                    bean = new MediaBean();
                    bean._id = anInt;
                    dataCacheManager.cacheMediaBean(bean);
                }

                bean.bucketId = cursor.getLong(1);
                bean.width = cursor.getInt(2);
                bean.height = cursor.getInt(3);
                bean.mimeType = cursor.getString(4);
                bean.lastModify = cursor.getLong(5);
                bean.size = cursor.getLong(6);
                bean.refocusType = cursor.getInt(7);
                result.add(bean);
            }
        }
        return result;
    }

    public static List<MediaBean> parseVideo(Cursor cursor) {
        List<MediaBean> result = new ArrayList<>();
        if (cursor != null) {
            MediaBean bean = null;
            int anInt;
            DataCacheManager dataCacheManager = DataCacheManager.getDataCacheManager();
            while (cursor.moveToNext()) {
                anInt = cursor.getInt(0);
                // 检查是否存在
                MediaBean cacheMediaBean = dataCacheManager.getCacheMediaBean(anInt);
                if (cacheMediaBean != null) {
                    bean = cacheMediaBean;
                } else {
                    bean = new MediaBean();
                    bean._id = anInt;
                    dataCacheManager.cacheMediaBean(bean);
                }
                bean.bucketId = cursor.getLong(1);
                bean.width = cursor.getInt(2);
                bean.height = cursor.getInt(3);
                bean.mimeType = cursor.getString(4);
                bean.lastModify = cursor.getLong(5);
                bean.duration = cursor.getLong(6);
                bean.size = cursor.getLong(7);
                bean.refocusType = cursor.getInt(8);
                result.add(bean);
            }
        }
        return result;
    }
}
