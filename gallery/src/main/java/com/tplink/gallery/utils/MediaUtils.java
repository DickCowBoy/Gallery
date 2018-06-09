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

import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;

import com.android.gallery3d.util.BucketNames;
import com.android.gallery3d.util.GalleryUtils;

import java.util.List;

public class MediaUtils {

    public static final int CAMERA_BUCKET_ID = GalleryUtils.getBucketId(
            Environment.getExternalStorageDirectory().toString() + "/"
                    + BucketNames.CAMERA);

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

}
