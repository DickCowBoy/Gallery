package com.tplink.gallery.media;

import android.provider.MediaStore;

public class MediaColumn {
    public static final String[] QUERY_PROJECTION = new String[]{
            MediaStore.Files.FileColumns._ID,
            MediaStore.Images.ImageColumns.BUCKET_ID,
            MediaStore.Images.ImageColumns.WIDTH,
            MediaStore.Images.ImageColumns.HEIGHT,
            MediaStore.Images.ImageColumns.MIME_TYPE,
            MediaStore.Images.ImageColumns.DATE_MODIFIED,
            MediaStore.Video.VideoColumns.DURATION};
}
