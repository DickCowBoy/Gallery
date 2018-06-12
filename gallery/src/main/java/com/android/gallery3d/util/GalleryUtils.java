/*
 * Copyright (C) 2010 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.gallery3d.util;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.ConditionVariable;
import android.os.Environment;
import android.os.Parcelable;
import android.preference.PreferenceManager;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.WindowManager;
import android.webkit.MimeTypeMap;

import com.android.gallery3d.common.ApiHelper;
import com.android.gallery3d.ui.TiledScreenNail;
import com.android.gallery3d.util.ThreadPool.CancelListener;
import com.android.gallery3d.util.ThreadPool.JobContext;
import com.tplink.gallery.R;
import com.tplink.gallery.bean.MediaBean;

import java.io.File;
import java.util.List;
import java.util.Locale;

public class GalleryUtils {
    private static final String TAG = "GalleryUtils";
    private static final String MAPS_PACKAGE_NAME = "com.google.android.apps.maps";
    private static final String MAPS_CLASS_NAME = "com.google.android.maps.MapsActivity";
    public static final String KEY_MEDIA_TYPES = "mediaTypes";
    public static final String KEY_BUCKET_ID = "bucketId";
    // The media type bit passed by the intent
    public static final int MEDIA_TYPE_ALL = 0;
    public static final int MEDIA_TYPE_IMAGE = 1;
    public static final int MEDIA_TYPE_VIDEO = 4;

    public static final String MIME_TYPE_IMAGE = "image/*";
    public static final String MIME_TYPE_VIDEO = "video/*";
    public static final String MIME_TYPE_PANORAMA360 = "application/vnd.google.panorama360+jpg";
    public static final String MIME_TYPE_ALL = "*/*";

    private static final String DIR_TYPE_IMAGE = "vnd.android.cursor.dir/image";
    private static final String DIR_TYPE_VIDEO = "vnd.android.cursor.dir/video";

    private static final String PREFIX_PHOTO_EDITOR_UPDATE = "editor-update-";
    private static final String PREFIX_HAS_PHOTO_EDITOR = "has-editor-";

    private static final String KEY_PACKAGES_VERSION  = "packages-version";

    private static float sPixelDensity = -1f;

    public static void initialize(Context context) {
        DisplayMetrics metrics = new DisplayMetrics();
        WindowManager wm = (WindowManager)
                context.getSystemService(Context.WINDOW_SERVICE);
        wm.getDefaultDisplay().getMetrics(metrics);
        sPixelDensity = metrics.density;
        Resources r = context.getResources();
        TiledScreenNail.setPlaceholderColor(r.getColor(
                R.color.bitmap_screennail_placeholder));
        initializeThumbnailSizes(metrics, r);
    }

    private static void initializeThumbnailSizes(DisplayMetrics metrics, Resources r) {
        int maxPixels = Math.max(metrics.heightPixels, metrics.widthPixels);

        // For screen-nails, we never need to completely fill the screen
        MediaBean.setThumbnailSizes(maxPixels, maxPixels / 2, maxPixels / 10);
        TiledScreenNail.setMaxSide(maxPixels / 2);
    }

    public static float[] intColorToFloatARGBArray(int from) {
        return new float[] {
            Color.alpha(from) / 255f,
            Color.red(from) / 255f,
            Color.green(from) / 255f,
            Color.blue(from) / 255f
        };
    }

    public static float dpToPixel(float dp) {
        return sPixelDensity * dp;
    }

    public static int dpToPixel(int dp) {
        return Math.round(dpToPixel((float) dp));
    }

    public static byte[] getBytes(String in) {
        byte[] result = new byte[in.length() * 2];
        int output = 0;
        for (char ch : in.toCharArray()) {
            result[output++] = (byte) (ch & 0xFF);
            result[output++] = (byte) (ch >> 8);
        }
        return result;
    }

    // Below are used the detect using database in the render thread. It only
    // works most of the time, but that's ok because it's for debugging only.

    private static volatile Thread sCurrentThread;
    private static volatile boolean sWarned;

    public static void setRenderThread() {
        sCurrentThread = Thread.currentThread();
    }

    public static void assertNotInRenderThread() {
        if (!sWarned) {
            if (Thread.currentThread() == sCurrentThread) {
                sWarned = true;
                Log.w(TAG, new Throwable("Should not do this in render thread"));
            }
        }
    }

    private static final double RAD_PER_DEG = Math.PI / 180.0;
    private static final double EARTH_RADIUS_METERS = 6367000.0;

    public static double fastDistanceMeters(double latRad1, double lngRad1,
            double latRad2, double lngRad2) {
       if ((Math.abs(latRad1 - latRad2) > RAD_PER_DEG)
             || (Math.abs(lngRad1 - lngRad2) > RAD_PER_DEG)) {
           return accurateDistanceMeters(latRad1, lngRad1, latRad2, lngRad2);
       }
       // Approximate sin(x) = x.
       double sineLat = (latRad1 - latRad2);

       // Approximate sin(x) = x.
       double sineLng = (lngRad1 - lngRad2);

       // Approximate cos(lat1) * cos(lat2) using
       // cos((lat1 + lat2)/2) ^ 2
       double cosTerms = Math.cos((latRad1 + latRad2) / 2.0);
       cosTerms = cosTerms * cosTerms;
       double trigTerm = sineLat * sineLat + cosTerms * sineLng * sineLng;
       trigTerm = Math.sqrt(trigTerm);

       // Approximate arcsin(x) = x
       return EARTH_RADIUS_METERS * trigTerm;
    }

    public static double accurateDistanceMeters(double lat1, double lng1,
            double lat2, double lng2) {
        double dlat = Math.sin(0.5 * (lat2 - lat1));
        double dlng = Math.sin(0.5 * (lng2 - lng1));
        double x = dlat * dlat + dlng * dlng * Math.cos(lat1) * Math.cos(lat2);
        return (2 * Math.atan2(Math.sqrt(x), Math.sqrt(Math.max(0.0,
                1.0 - x)))) * EARTH_RADIUS_METERS;
    }


    public static final double toMile(double meter) {
        return meter / 1609;
    }

    // For debugging, it will block the caller for timeout millis.
    public static void fakeBusy(JobContext jc, int timeout) {
        final ConditionVariable cv = new ConditionVariable();
        jc.setCancelListener(new CancelListener() {
            @Override
            public void onCancel() {
                cv.open();
            }
        });
        cv.block(timeout);
        jc.setCancelListener(null);
    }

    public static boolean isEditorAvailable(Context context, String mimeType) {
        int version = getPackagesVersion(context);

        String updateKey = PREFIX_PHOTO_EDITOR_UPDATE + mimeType;
        String hasKey = PREFIX_HAS_PHOTO_EDITOR + mimeType;

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        if (prefs.getInt(updateKey, 0) != version) {
            PackageManager packageManager = context.getPackageManager();
            List<ResolveInfo> infos = packageManager.queryIntentActivities(
                    new Intent(Intent.ACTION_EDIT).setType(mimeType), 0);
            prefs.edit().putInt(updateKey, version)
                        .putBoolean(hasKey, !infos.isEmpty())
                        .commit();
        }

        return prefs.getBoolean(hasKey, true);
    }

    public synchronized static int getPackagesVersion(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getInt(KEY_PACKAGES_VERSION, 1);
    }

    public static void startCameraActivity(Context context) {
        Intent intent = new Intent(MediaStore.INTENT_ACTION_STILL_IMAGE_CAMERA)
                .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
                        | Intent.FLAG_ACTIVITY_NEW_TASK);
        try {
            context.startActivity(intent);
        } catch (ActivityNotFoundException e) {
            // This will only occur if Camera was disabled while Gallery is open
            // since we cache our availability check. Just abort the attempt.
            Log.e(TAG, "Camera activity previously detected but cannot be found", e);
        }
    }

    public static String formatLatitudeLongitude(String format, double latitude,
            double longitude) {
        // We need to specify the locale otherwise it may go wrong in some language
        // (e.g. Locale.FRENCH)
        return String.format(Locale.ENGLISH, format, latitude, longitude);
    }

    public static void showOnMap(Context context, double latitude, double longitude) {
        try {
            // We don't use "geo:latitude,longitude" because it only centers
            // the MapView to the specified location, but we need a marker
            // for further operations (routing to/from).
            // The q=(lat, lng) syntax is suggested by geo-team.
            String uri = formatLatitudeLongitude("http://maps.google.com/maps?f=q&q=(%f,%f)",
                    latitude, longitude);
            ComponentName compName = new ComponentName(MAPS_PACKAGE_NAME,
                    MAPS_CLASS_NAME);
            Intent mapsIntent = new Intent(Intent.ACTION_VIEW,
                    Uri.parse(uri)).setComponent(compName);
            context.startActivity(mapsIntent);
        } catch (ActivityNotFoundException e) {
            // Use the "geo intent" if no GMM is installed
            Log.e(TAG, "GMM activity not found!", e);
            String url = formatLatitudeLongitude("geo:%f,%f", latitude, longitude);
            Intent mapsIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            context.startActivity(mapsIntent);
        }
    }

    public static int getBucketId(String path) {
        return path.toLowerCase(Locale.ENGLISH).hashCode();
    }

    // Returns a (localized) string for the given duration (in seconds).
    public static String formatDuration(final Context context, long duration) {
        long h = duration / 3600;
        long m = (duration - h * 3600) / 60;
        long s = duration - (h * 3600 + m * 60);
        String durationValue;
        if (h == 0) {
            durationValue = String.format(context.getString(R.string.details_ms), m, s);
        } else {
            durationValue = String.format(context.getString(R.string.details_hms), h, m, s);
        }
        return durationValue;
    }


    public static Uri convertFile2ContentUri(Context context, Uri uri) {
        if (!uri.getAuthority().equalsIgnoreCase(MediaStore.AUTHORITY)
                || (uri.getAuthority().equalsIgnoreCase(MediaStore.AUTHORITY) && uri.getPath().contains("file"))) {
            String filePath = getFilePathFromUri(context, uri);
            if (filePath != null) {
                String mimeType = GalleryUtils.getMimeTypeForUri(context, uri);
                if (mimeType != null) {
                    Uri baseUri = null;
                    if (mimeType.startsWith("image")) {
                        baseUri = MediaStore.Files.getContentUri("external");
                        Cursor cursor = null;
                        try {
                            cursor = context.getContentResolver().query(baseUri, new String[]{
                                    MediaStore.Files.FileColumns._ID,MediaStore.Files.FileColumns.DATA
                            }, MediaStore.Files.FileColumns.DATA + " like \"%tpnotes%\"",/*new String[]{
                                    filePath
                            }*/null, null, null);
                            if (cursor != null) {
                                while (cursor.moveToNext()) {
                                    Log.e("LJL", "DATA="+cursor.getString(1));
                                }
                            }

                            if (cursor != null && cursor.getCount() == 1) {
                                if (cursor.moveToNext()) {
                                    String mediaId = cursor.getString(0);
                                    return Uri.parse("content://media/external/file/" + mediaId);
                                }
                            }
                        } catch (Exception e) {
                            Log.e(TAG, e.getMessage());
                        } finally {
                            Utils.closeSilently(cursor);
                        }
                    }
                }
            }
        }
        return uri;
    }

    public static Uri coverToContentUri(Context context, Uri uri) {
        if (!uri.getAuthority().equalsIgnoreCase(MediaStore.AUTHORITY)
                || (uri.getAuthority().equalsIgnoreCase(MediaStore.AUTHORITY) && uri.getPath().contains("file"))) {
            String filePath = getFilePathFromUri(context, uri);
            if (filePath != null) {
                String mimeType = GalleryUtils.getMimeTypeForUri(context, uri);
                if (mimeType != null) {
                    Uri baseUri = null;
                    if (mimeType.startsWith("image")) {
                        baseUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                    } else if (mimeType.startsWith("video")) {
                        baseUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                    }
                    if (baseUri != null) {
                        Cursor cursor = null;
                        try {
                            cursor = context.getContentResolver().query(baseUri, new String[]{
                                    MediaStore.Files.FileColumns._ID
                            }, MediaStore.Files.FileColumns.DATA + " = ?", new String[]{
                                    filePath
                            }, null);
                            if (cursor != null && cursor.getCount() == 1) {
                                if (cursor.moveToNext()) {
                                    String mediaId = cursor.getString(0);
                                    if (mimeType.startsWith("image")) {
                                        return Uri.parse("content://media/external/images/media/" + mediaId);
                                    }

                                    return Uri.parse("content://media/external/video/media/" + mediaId);
                                }
                            }
                        } catch (Exception e) {
                            Log.e(TAG, e.getMessage());
                        } finally {
                            Utils.closeSilently(cursor);
                        }

                    }
                }
            }
        }

        return uri;
    }

    public static String getFilePathFromUri(Context context, Uri uri) {
        // DocumentProvider
        if (DocumentsContract.isDocumentUri(context, uri)) {
            return getFilePathFromDocumentProvider(context, uri);
        }
        // MediaStore (and general)
        else if ("content".equalsIgnoreCase(uri.getScheme())) {
            return getDataColumn(context, uri, null, null);
        }
        // File
        else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }

        return null;
    }

    public static String getFilePathFromDocumentProvider(Context context, Uri uri) {
        // ExternalStorageProvider
        if (isExternalStorageDocument(uri)) {
            return getFileForDocId(uri);
        }
        // DownloadsProvider
        else if (isDownloadsDocument(uri)) {
            final String id = DocumentsContract.getDocumentId(uri);
            final Uri contentUri = ContentUris.withAppendedId(
                    Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));

            return getDataColumn(context, contentUri, null, null);
        }
        // MediaProvider
        else if (isMediaDocument(uri)) {
            return getFileForMediaProvider(context, uri);
        }

        return null;
    }

    public static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    public static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    public static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }

    public static String getFileForDocId(Uri uri) {
        final String docId = DocumentsContract.getDocumentId(uri);
        final String[] split = docId.split(":");
        final String type = split[0];
        if ("primary".equalsIgnoreCase(type)) {
            return Environment.getExternalStorageDirectory() + File.separator + split[1];
        } else {
            return "/storage/" + type + File.separator + split[1];
        }
    }

    public static String getFileForMediaProvider(Context context, Uri uri) {
        final String docId = DocumentsContract.getDocumentId(uri);
        final String[] split = docId.split(":");
        final String type = split[0];

        Uri contentUri = null;
        if ("image".equals(type)) {
            contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        } else if ("video".equals(type)) {
            contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
        }

        if (contentUri == null) {
            return null;
        }

        final String selection = "_id = ?";
        final String[] selectionArgs = new String[]{
                split[1]
        };

        return getDataColumn(context, contentUri, selection, selectionArgs);
    }

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
                return cursor.getString(0);
            }
        } catch (Exception e) {
            // 有可能获取失败
        } finally {
            Utils.closeSilently(cursor);
        }
        return null;
    }

    public static String getMimeTypeForUri(Context context, Uri uri) {
        if (uri.getScheme() == null) {
            return null;
        }

        if (uri.getScheme().equals(ContentResolver.SCHEME_CONTENT)) {
            // 短信模块必须单独处理，不然会返回null
            if (uri.getAuthority().equals("com.android.messaging.datamodel.MediaScratchFileProvider")) {
                String ext = uri.getQueryParameter("ext");
                if (ext != null && ext.equals("3gp")) {
                    return "video/3gpp";
                }
            }
            ContentResolver cr = context.getContentResolver();
            return cr.getType(uri);
        } else if (uri.getScheme().equals(ContentResolver.SCHEME_FILE)) {
            final String path = uri.getPath();
            final MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
            String extension = MimeTypeMap.getFileExtensionFromUrl(path);
            if (TextUtils.isEmpty(extension)) {
                // getMimeTypeFromExtension() doesn't handle spaces in filenames nor can it handle
                // urlEncoded strings. Let's try one last time at finding the extension.
                final int dotPos = path.lastIndexOf('.');
                if (0 <= dotPos) {
                    extension = path.substring(dotPos + 1).toLowerCase(Locale.ENGLISH);
                }
            }
            return mimeTypeMap.getMimeTypeFromExtension(extension.toLowerCase());
        } else {
            Log.d(TAG, "Could not determine mime type for Uri " + uri);
            return null;
        }
    }

    public static void shareOneAttachment(Context context, Parcelable uri, String mimeType) {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.putExtra(Intent.EXTRA_STREAM, uri);
        shareIntent.setType(mimeType);

        try {
            context.startActivity(Intent.createChooser(shareIntent,
                    context.getString(R.string.share_to)));
        } catch (ActivityNotFoundException e) {
            // couldn't find activity for SEND_MULTIPLE intent
            Log.e(TAG, "Couldn't find Activity for intent", e);
        }
    }

    @SuppressLint("NewApi")
    public static boolean isInMultiWindowMode(Activity activity) {
        if (activity == null) {
            return false;
        }
        if (ApiHelper.HAS_MULTI_WINDOW_MODE && activity.isInMultiWindowMode()) {
            return true;
        }
        return false;
    }

    public static int getActionbarHeight(Context context) {
        int actionBarHeight = 0;
        TypedValue tv = new TypedValue();
        if (context.getTheme().resolveAttribute(android.R.attr.actionBarSize, tv, true)) {
            actionBarHeight = TypedValue.complexToDimensionPixelSize(tv.data, context.getResources().getDisplayMetrics());
        }

        return actionBarHeight;
    }


    private static int getMediaType(String type, int defaultType) {
        if (type == null) return defaultType;
        try {
            int value = Integer.parseInt(type);
            if ((value & (MEDIA_TYPE_IMAGE
                    | MEDIA_TYPE_VIDEO)) != 0) return value;
        } catch (NumberFormatException e) {
            Log.w(TAG, "invalid type: " + type, e);
        }
        return defaultType;
    }

    public static boolean isValidLocation(double latitude, double longitude) {
        // TODO: change || to && after we fix the default location issue
        return (latitude != MediaBean.INVALID_LATLNG || longitude != MediaBean.INVALID_LATLNG);
    }

    public static int getMediaIdByUri(Context context, Uri uri) {
        if (!uri.getAuthority().equalsIgnoreCase(MediaStore.AUTHORITY)
                || (uri.getAuthority().equalsIgnoreCase(MediaStore.AUTHORITY) && uri.getPath().contains("file"))) {
            String filePath = getFilePathFromUri(context, uri);
            if (filePath != null) {
                String mimeType = GalleryUtils.getMimeTypeForUri(context, uri);
                if (mimeType != null) {
                    Uri baseUri = null;
                    if (mimeType.startsWith("image")) {
                        baseUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                    } else if (mimeType.startsWith("video")) {
                        baseUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                    }
                    if (baseUri != null) {
                        Cursor cursor = null;
                        try {
                            cursor = context.getContentResolver().query(baseUri, new String[]{
                                    MediaStore.Files.FileColumns._ID
                            }, MediaStore.Files.FileColumns.DATA + " = ?", new String[]{
                                    filePath
                            }, null);
                            if (cursor != null && cursor.getCount() == 1) {
                                if (cursor.moveToNext()) {
                                    return cursor.getInt(0);
                                }
                            }
                        } catch (Exception e) {
                            Log.e(TAG, e.getMessage());
                        } finally {
                            Utils.closeSilently(cursor);
                        }

                    }
                }
            }
        }

        return -1;
    }

}
