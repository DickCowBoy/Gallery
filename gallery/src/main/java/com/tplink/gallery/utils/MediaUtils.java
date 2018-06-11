/*
 * Copyright (C) 2018, TP-LINK TECHNOLOGIES CO., LTD.
 *
 * MediaUtils.java
 *
 * Description 媒体工具类
 *
 * Author LinJinLong
 *
 * Ver 1.0, 2018-04-19 LinJinLong, Create file
 */
package com.tplink.gallery.utils;

import android.content.ContentResolver;
import android.content.Context;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;

import com.android.gallery3d.util.BucketNames;
import com.android.gallery3d.util.GalleryUtils;
import com.tplink.gallery.bean.MediaBean;

import java.io.File;
import java.util.List;

public class MediaUtils {

    public static final int CAMERA_BUCKET_ID = GalleryUtils.getBucketId(
            Environment.getExternalStorageDirectory().toString() + "/"
                    + BucketNames.CAMERA);

    public static final String MIME_TYPE_JPEG = "image/jpeg";

    public static Uri getImageUri() {
        return MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
    }

    public static Uri getVideoUri() {
        return MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
    }

    public static Uri getFileUri() {
        return MediaStore.Files.getContentUri("external");
    }

    public static ImageEngine imageEngine = null;

    public static String getAllMediaKey(List<String> allowMimeTypes,
                                        List<String> notAllowMimeTypes, boolean needResolveBurst,
                                        boolean needImage, boolean needVideo) {
        return "ALL_MEDIA" + "_" + (allowMimeTypes!= null ? allowMimeTypes.toString() : "all") + "_"
                +  (notAllowMimeTypes!= null ? notAllowMimeTypes.toString() : "all")  + "_" + needResolveBurst
                + "_" + needImage + "_" + needVideo;
    }

    public static String getAllAlbumKey(List<String> allowMimeTypes,
                                        List<String> notAllowMimeTypes, boolean needResolveBurst,
                                        boolean needImage, boolean needVideo) {
        return "ALL_ALBUM" + "_" + (allowMimeTypes!= null ? allowMimeTypes.toString() : "all") + "_"
                + (notAllowMimeTypes!= null ? notAllowMimeTypes.toString() : "all")  + "_" + needResolveBurst
                + "_" + needImage + "_" + needVideo;
    }

    public static String getBucketId(long bucketId, List<String> allowMimeTypes,
                                     List<String> notAllowMimeTypes) {
        return String.valueOf(bucketId) + "_" + (allowMimeTypes!= null ? allowMimeTypes.toString() : "all")
                + "_" + (notAllowMimeTypes!= null ? notAllowMimeTypes.toString() : "all");
    }

    public static String getAllCameraAlbumKey() {
        return "CAMERA_ALL";
    }

    public static boolean isEditSupported(String mimeType) {
        if (mimeType == null) return false;
        mimeType = mimeType.toLowerCase();

        return mimeType.startsWith("image/")
                && (mimeType.endsWith("jpg") || mimeType.endsWith("jpeg")
                || mimeType.endsWith("png") || mimeType.endsWith("bmp"));
    }

    public static void deleteMedia(Context context, MediaBean mediaBean) {
        if (mediaBean.isImage()) {
            delImage(context, mediaBean);
        } else {
            delVideo(context, mediaBean);
        }
    }

    private static void delImage(Context context, MediaBean mediaBean) {
        Uri baseUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        ContentResolver contentResolver = context.getContentResolver();
        if (mediaBean.isBurst) {
            // 同一组内的连拍照片具有相同的bucket_id,因此删除具有相同bucketId的照片即可删除同一组内的连拍照片
            File burstDir = new File(mediaBean.filePath).getParentFile();
            contentResolver.delete(baseUri, "bucket_id=?",
                    new String[]{String.valueOf(mediaBean.bucketId)});
            // Fix #51105 empty dir does not be deleted when del burst photo
            if (burstDir.listFiles().length == 0) {
                burstDir.delete();
            }

        } else {
            contentResolver.delete(baseUri, "_id=?",
                    new String[]{String.valueOf(mediaBean._id)});
        }
    }

    private static void delVideo(Context context, MediaBean mediaBean) {
        Uri baseUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
        context.getContentResolver().delete(baseUri, "_id=?",
                new String[]{String.valueOf(mediaBean._id)});
    }

}
