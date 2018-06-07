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
package com.android.gallery3d.ui;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.text.TextUtils;
import android.text.format.Formatter;
import android.view.View.MeasureSpec;

import com.android.gallery3d.data.MediaDetails;
import com.android.gallery3d.ui.DetailsAddressResolver.AddressResolvingListener;
import com.android.gallery3d.util.GalleryUtils;
import com.android.gallery3d.util.StorageHelper;
import com.android.gallery3d.util.Utils;
import com.tplink.gallery.R;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class DetailsHelper {
    private static DetailsAddressResolver sAddressResolver;
    private DetailsViewContainer mContainer;

    private static final DecimalFormat mDecimalFormat = new DecimalFormat(".####");

    public static String GROUP_DATE = "group_date";
    public static String GROUP_SHOT = "group_shot";
    public static String GROUP_MORE = "group_more";

    public interface DetailsSource {
        public int size();
        public int setIndex();
        public MediaDetails getDetails();
    }

    public interface CloseListener {
        public void onClose();
    }

    public interface DetailsViewContainer {
        public void reloadDetails();
        public void setCloseListener(CloseListener listener);
        public void show();
        public void hide();
    }

    public interface ResolutionResolvingListener {
        public void onResolutionAvailable(int width, int height);
    }

    public DetailsHelper(Activity activity, GLView rootPane, DetailsSource source) {
        mContainer = new DialogDetailsView(activity, source);
    }

    public void layout(int left, int top, int right, int bottom) {
        if (mContainer instanceof GLView) {
            GLView view = (GLView) mContainer;
            view.measure(MeasureSpec.UNSPECIFIED,
                    MeasureSpec.makeMeasureSpec(bottom - top, MeasureSpec.AT_MOST));
            view.layout(0, top, view.getMeasuredWidth(), top + view.getMeasuredHeight());
        }
    }

    public static String resolveAddress(Activity activity, double[] latlng,
                                        AddressResolvingListener listener) {
        if (sAddressResolver == null) {
            sAddressResolver = new DetailsAddressResolver(activity);
        } else {
            sAddressResolver.cancel();
        }
        return sAddressResolver.resolveAddress(latlng, listener);
    }

    public static void resolveResolution(String path, ResolutionResolvingListener listener) {
        Bitmap bitmap = BitmapFactory.decodeFile(path);
        if (bitmap == null) return;
        listener.onResolutionAvailable(bitmap.getWidth(), bitmap.getHeight());
    }

    public static void pause() {
        if (sAddressResolver != null) sAddressResolver.cancel();
    }

    public void show() {
        mContainer.show();
    }

    public void hide() {
        mContainer.hide();
    }

    public static String getDetailsName(Context context, int key) {
        switch (key) {
            case MediaDetails.INDEX_TITLE:
                return context.getString(R.string.title);
            case MediaDetails.INDEX_DESCRIPTION:
                return context.getString(R.string.description);
            case MediaDetails.INDEX_DATETIME:
                return context.getString(R.string.time);
            case MediaDetails.INDEX_LOCATION:
                return context.getString(R.string.location);
            case MediaDetails.INDEX_PATH:
                return context.getString(R.string.path);
            case MediaDetails.INDEX_WIDTH:
                return context.getString(R.string.width);
            case MediaDetails.INDEX_HEIGHT:
                return context.getString(R.string.height);
            case MediaDetails.INDEX_ORIENTATION:
                return context.getString(R.string.orientation);
            case MediaDetails.INDEX_DURATION:
                return context.getString(R.string.duration);
            case MediaDetails.INDEX_MIMETYPE:
                return context.getString(R.string.mimetype);
            case MediaDetails.INDEX_SIZE:
                return context.getString(R.string.file_size);
            case MediaDetails.INDEX_MAKE:
                return context.getString(R.string.maker);
            case MediaDetails.INDEX_MODEL:
                return context.getString(R.string.model);
            case MediaDetails.INDEX_FLASH:
                return context.getString(R.string.flash);
            case MediaDetails.INDEX_APERTURE:
                return context.getString(R.string.aperture);
            case MediaDetails.INDEX_FOCAL_LENGTH:
                return context.getString(R.string.focal_length);
            case MediaDetails.INDEX_WHITE_BALANCE:
                return context.getString(R.string.white_balance);
            case MediaDetails.INDEX_EXPOSURE_TIME:
                return context.getString(R.string.exposure_time);
            case MediaDetails.INDEX_ISO:
                return context.getString(R.string.iso);
            default:
                return "Unknown key" + key;
        }
    }

    public static Map<String, Set<String>> groupMediaDetails(Context context, MediaDetails details){
        Map<String, Set<String>> groupMap = new HashMap<>();
        if (details == null){
            return groupMap;
        }

        Set<String> dateSet = new LinkedHashSet<>();
        String value = getDetailValue(context, MediaDetails.INDEX_DATETIME, details);
        if(!TextUtils.isEmpty(value)) {
            dateSet.add(value);
        }

        Set<String> shotSet = new LinkedHashSet<>();
        //dateSet.add(getDetailValue(context, MediaDetails.INDEX_FOCAL_LENGTH, details));
        value = getDetailValue(context, MediaDetails.INDEX_EXPOSURE_TIME, details);
        if(!TextUtils.isEmpty(value)) {
            shotSet.add(value);
        }
        value = getDetailValue(context, MediaDetails.INDEX_FOCAL_LENGTH, details);
        if(!TextUtils.isEmpty(value)) {
            shotSet.add(value);
        }
        String titlePre = context.getString(R.string.iso);
        value = getDetailValue(context, MediaDetails.INDEX_ISO, details);
        if (!TextUtils.isEmpty(value)) {
            shotSet.add(titlePre + " " + value);
        }

        Set<String> moreSet = new LinkedHashSet<>();
        titlePre = context.getString(R.string.title_append_colon);
        value = getDetailValue(context, MediaDetails.INDEX_TITLE, details);
        if (!TextUtils.isEmpty(value)) {
            moreSet.add(titlePre + value);
        }
        titlePre = context.getString(R.string.duration_append_colon);
        value = getDetailValue(context, MediaDetails.INDEX_DURATION, details);
        if (!TextUtils.isEmpty(value)) {
            moreSet.add(titlePre + value);
        }
        titlePre = context.getString(R.string.resolution_append_colon);
        String width = getDetailValue(context, MediaDetails.INDEX_WIDTH, details);
        String height = getDetailValue(context, MediaDetails.INDEX_HEIGHT, details);
        if (!TextUtils.isEmpty(width) && !TextUtils.isEmpty(height)) {
            moreSet.add(titlePre + width + "x" + height);
        }
        titlePre = context.getString(R.string.size_append_colon);
        value = getDetailValue(context, MediaDetails.INDEX_SIZE, details);
        if (!TextUtils.isEmpty(value)) {
            moreSet.add(titlePre + value);
        }
        titlePre = context.getString(R.string.path_append_colon);
        value = getDetailValue(context, MediaDetails.INDEX_PATH, details);
        if (!TextUtils.isEmpty(value)) {
            moreSet.add(titlePre + value);
        }

        groupMap.put(GROUP_DATE, dateSet);
        groupMap.put(GROUP_SHOT, shotSet);
        groupMap.put(GROUP_MORE, moreSet);

        return groupMap;
    }

    private static String getDetailValue(Context context, int key, MediaDetails details) {
        Object detailObj = details.getDetail(key);
        if (detailObj == null){
            return null;
        }
        String value;
        switch (key) {
            case MediaDetails.INDEX_LOCATION: {
                double[] latlng = (double[]) detailObj;
                value = GalleryUtils.formatLatitudeLongitude("(%f,%f)", latlng[0], latlng[1]);
                break;
            }
            case MediaDetails.INDEX_SIZE: {
                value = Formatter.formatFileSize(
                        context, (Long) detailObj);
                break;
            }
            case MediaDetails.INDEX_WHITE_BALANCE: {
                value = "1".equals(detailObj)
                        ? context.getString(R.string.manual)
                        : context.getString(R.string.auto);
                break;
            }
            case MediaDetails.INDEX_FLASH: {
                MediaDetails.FlashState flash =
                        (MediaDetails.FlashState) detailObj;
                // TODO: camera doesn't fill in the complete values, show more information
                // when it is fixed.
                if (flash.isFlashFired()) {
                    value = context.getString(R.string.flash_on);
                } else {
                    value = context.getString(R.string.flash_off);
                }
                break;
            }
            case MediaDetails.INDEX_EXPOSURE_TIME: {
                value = (String) detailObj;
                double time = Double.valueOf(value);
                if (time < 1.0f) {
                    value = String.format(Locale.getDefault(), "%d/%d", 1,
                            (int) (0.5f + 1 / time));
                } else {
                    int integer = (int) time;
                    time -= integer;
                    value = String.valueOf(integer) + "''";
                    if (time > 0.0001) {
                        value += String.format(Locale.getDefault(), " %d/%d", 1,
                                (int) (0.5f + 1 / time));
                    }
                }
                break;
            }
            case MediaDetails.INDEX_WIDTH:
                if (detailObj.toString().equalsIgnoreCase("0")) {
                    value = context.getString(R.string.unknown);
                } else {
                    value = toLocalInteger(detailObj);
                }
                break;
            case MediaDetails.INDEX_HEIGHT: {
                if (detailObj.toString().equalsIgnoreCase("0")) {
                    value = context.getString(R.string.unknown);
                } else {
                    value = toLocalInteger(detailObj);
                }
                break;
            }
            case MediaDetails.INDEX_PATH:
                value = transferPathToDescription(context, detailObj.toString());
                break;
            case MediaDetails.INDEX_ISO:
                value = toLocalNumber(Integer.parseInt((String) detailObj));
                break;
            case MediaDetails.INDEX_FOCAL_LENGTH:
                double focalLength = Double.parseDouble(detailObj.toString());
                value = toLocalNumber(focalLength);
                break;
            case MediaDetails.INDEX_ORIENTATION:
                value = toLocalInteger(detailObj);
                break;
            default: {
                Object valueObj = detailObj;
                // This shouldn't happen, log its key to help us diagnose the problem.
                if (valueObj == null) {
                    Utils.fail("%s's value is Null",
                            DetailsHelper.getDetailsName(context, key));
                }
                value = valueObj.toString();
            }
        }

        if (details.hasUnit(key)) {
            value = String.format("%s %s", value, context.getString(details.getUnit(key)));
        }

        return value;
    }

    /**
     * Converts the given integer (given as String or Integer object) to a
     * localized String version.
     */
    private static String toLocalInteger(Object valueObj) {
        if (valueObj instanceof Integer) {
            return toLocalNumber((Integer) valueObj);
        } else {
            String value = valueObj.toString();
            try {
                value = toLocalNumber(Integer.parseInt(value));
            } catch (NumberFormatException ex) {
                // Just keep the current "value" if we cannot
                // parse it as a fallback.
            }
            return value;
        }
    }

    /** Converts the given integer to a localized String version. */
    private static String toLocalNumber(int n) {
        return String.format(Locale.getDefault(), "%d", n);
    }

    /** Converts the given double to a localized String version. */
    private static String toLocalNumber(double n) {
        return String.format(Locale.getDefault(), "%.2f", n);
    }

    // 将挂载点路径替换为对挂载点的描述
    private static String transferPathToDescription(Context context, String path) {
        StorageHelper helper = new StorageHelper(context);
        String volumn = helper.getStorageVolumePath(path);
        String description = helper.getStorageVolumeDescription(path);
        if (volumn != null) {
            return path.replaceFirst(volumn, description);
        }

        return path;
    }
}


