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
import android.provider.MediaStore;

public class MediaUtils {

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

    public static String getAllMediaKey(boolean needVideo, boolean needImage, boolean needGif, boolean needResolveBurst) {
        return "ALL_MEDIA" + "_" + needGif + "_" + needImage + "_" + needResolveBurst + "_" + needVideo;
    }

    public static String getAllAlbumKey(boolean needVideo, boolean needImage, boolean needGif, boolean needResolveBurst) {
        return "ALL_ALBUM" + "_" + needGif + "_" + needImage + "_" + needResolveBurst + "_" + needVideo;
    }

    public static String getBucketId(long bucketId, boolean needVideo, boolean needImage, boolean needGif) {
        return String.valueOf(bucketId) + "_" + needGif + "_" + needImage + "_" + needVideo;
    }


}
