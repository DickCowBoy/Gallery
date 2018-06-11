/*
 * Copyright (C), 2018, TP-LINK TECHNOLOGIES CO., LTD.
 *
 * ContentUriUtil.java
 * 根据URI获取文件路径
 * Author LinJL
 *
 * Ver 1.0, 2018-01-24, MaoJun, Create file
 */
package com.tplink.gallery.utils;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.util.Log;

import java.io.File;
import java.io.IOException;

public class ContentUriUtil {

    public static final String BLUETOOTH_PROVIDER_HOST = "com.android.bluetooth.opp.fileprovider";
    private static final String TAG = ContentUriUtil.class.getSimpleName();
    private static final int MILLISEC_PER_SEC = 1000;

    private ContentUriUtil(){}

    public static String getPath(final Context context, final Uri uri) {

        if (uri.getHost().equals(BLUETOOTH_PROVIDER_HOST)) {
            return Environment.getExternalStorageDirectory() + uri.getPath().substring(10);
        }
        // DocumentProvider
        if (DocumentsContract.isDocumentUri(context, uri)) {
            // ExternalStorageProvider
            if (isExternalStorageDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                if ("primary".equalsIgnoreCase(type)) {
                    return Environment.getExternalStorageDirectory() + "/" + split[1];
                }

                // TODO handle non-primary volumes
            } else if (isDownloadsDocument(uri)) {

                final String id = DocumentsContract.getDocumentId(uri);
                final Uri contentUri = ContentUris.withAppendedId(
                        Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));

                return getDataColumn(context, contentUri, null, null);
            } else if (isMediaDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                Uri contentUri = null;
                if ("image".equals(type)) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                } else if ("video".equals(type)) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                } else if ("audio".equals(type)) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                }

                final String selection = "_id=?";
                final String[] selectionArgs = new String[] {
                        split[1]
                };

                return getDataColumn(context, contentUri, selection, selectionArgs);
            }
        } else if ("content".equalsIgnoreCase(uri.getScheme())) {

            // Return the remote address
            if (isGooglePhotosUri(uri))
                return uri.getLastPathSegment();

            return null;
        } else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }

        return null;
    }
    /**
     **
     * Get the value of the data column for this Uri. This is useful for
     * MediaStore Uris, and other file-based ContentProviders.
     *
     * @param context The context.
     * @param uri The Uri to query.
     * @param selection (Optional) Filter used in the query.
     * @param selectionArgs (Optional) Selection arguments used in the query.
     * @return The value of the _data column, which is typically a file path.
     */
    public static String getDataColumn(Context context, Uri uri, String selection,
                                       String[] selectionArgs) {

        Cursor cursor = null;
        final String column = "_data";
        final String[] projection = {
                column
        };

        try {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs,
                    null);
            if (cursor != null && cursor.moveToFirst()) {
                final int index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(index);
            }
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return null;
    }


    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is ExternalStorageProvider.
     */
    public static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is DownloadsProvider.
     */
    public static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is MediaProvider.
     */
    public static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is Google Photos.
     */
    public static boolean isGooglePhotosUri(Uri uri) {
        return "com.google.android.apps.photos.content".equals(uri.getAuthority());
    }

    /**
     * Insert the information to DB.
     * @param context The context, through which it can do DB operation.
     * @param sourceUri The source uri.
     * @param file The file object.
     * @param saveFileName The file name.
     * @return DB uri.
     */
    public static Uri insertContent(Context context, Uri sourceUri, File file,
                                    String saveFileName) {
        long now = System.currentTimeMillis() / MILLISEC_PER_SEC;

        final ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.TITLE, saveFileName);
        values.put(MediaStore.Images.Media.DISPLAY_NAME, file.getName());
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
        values.put(MediaStore.Images.Media.DATE_TAKEN, now);
        values.put(MediaStore.Images.Media.DATE_MODIFIED, now);
        values.put(MediaStore.Images.Media.DATE_ADDED, now);
        values.put(MediaStore.Images.Media.ORIENTATION, 0);
        values.put(MediaStore.Images.Media.DATA, file.getAbsolutePath());
        values.put(MediaStore.Images.Media.SIZE, file.length());
        try {
            ExifInterface exif = new ExifInterface(file.getAbsolutePath());
            int imageLength = exif.getAttributeInt(
                    ExifInterface.TAG_IMAGE_LENGTH, 0);
            int imageWidth = exif.getAttributeInt(
                    ExifInterface.TAG_IMAGE_WIDTH, 0);
            values.put(MediaStore.Images.Media.WIDTH, imageWidth);
            values.put(MediaStore.Images.Media.HEIGHT, imageLength);
        } catch (IOException ex) {
            Log.w(TAG, "ExifInterface throws IOException", ex);
        }

        final String[] projection = new String[] { MediaStore.Images.ImageColumns.DATE_TAKEN,
                MediaStore.Images.ImageColumns.LATITUDE, MediaStore.Images.ImageColumns.LONGITUDE, };
        querySource(context, sourceUri, projection,
                new ContentResolverQueryCallback() {

                    @Override
                    public void onCursorResult(Cursor cursor) {
                        values.put(MediaStore.Images.Media.DATE_TAKEN, cursor.getLong(0));

                        double latitude = cursor.getDouble(1);
                        double longitude = cursor.getDouble(2);
                        if ((latitude != 0f) || (longitude != 0f)) {
                            values.put(MediaStore.Images.Media.LATITUDE, latitude);
                            values.put(MediaStore.Images.Media.LONGITUDE, longitude);
                        }
                    }
                });
        Uri insertUri = context.getContentResolver().insert(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
        Log.d(TAG, "insertUri = " + insertUri);
        return insertUri;
    }

    private static void querySource(Context context, Uri sourceUri,
                                    String[] projection, ContentResolverQueryCallback callback) {
        ContentResolver contentResolver = context.getContentResolver();
        Cursor cursor = null;

        cursor = contentResolver.query(sourceUri, projection, null, null,
                null);
        if ((cursor != null) && cursor.moveToNext()) {
            callback.onCursorResult(cursor);
        }
        if (cursor != null) {
            cursor.close();
        }
    }

    /**
     * Content resolver query callback.
     */
    private interface ContentResolverQueryCallback {
        void onCursorResult(Cursor cursor);
    }

    /**
     * Update content.
     *
     * @param context
     *            context
     * @param sourceUri
     *            source uri
     * @param file
     *            output file
     * @return source uri
     */
    public static Uri updateContent(Context context, Uri sourceUri, File file) {
        long now = System.currentTimeMillis() / MILLISEC_PER_SEC;
        final ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.DATE_MODIFIED, now);
        values.put(MediaStore.Images.Media.DATE_ADDED, now);
        values.put(MediaStore.Images.Media.SIZE, file.length());

        context.getContentResolver().update(sourceUri, values, null, null);
        return sourceUri;
    }


}
