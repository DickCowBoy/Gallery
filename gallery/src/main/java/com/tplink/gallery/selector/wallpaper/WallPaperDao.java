package com.tplink.gallery.selector.wallpaper;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class WallPaperDao {


    private static final Uri sWallPaperUri = Uri.parse("content://com.tplink.tpcarousel/tpcarousel");
    private static final String IMAGE_ID = "image_id";

    private WallPaperDao(){}

    /**
     * 获取所有的轮播图片信息
     * @param context
     * @return
     */
    public static List<Integer> getAllWallPaper(Context context) {
        Cursor cursor = null;
        List<Integer> array = new ArrayList<>();
        try {
            cursor = context.getContentResolver().query(sWallPaperUri,
                    null, null, null, "_id desc");
            while (cursor.moveToNext()) {
                array.add(cursor.getInt(cursor.getColumnIndex(IMAGE_ID)));
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
                cursor = null;
            }
        }
        return array;
    }

    public static void delWallPaper(Context context, Set<Integer> longArray) {
        if (longArray == null || longArray.size() == 0) {
            return;
        }
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("(");
            String flag = "";
            for (Integer aLong : longArray) {
                sb.append(flag +  aLong);
                flag = ",";
            }
            sb.append(")");
            context.getContentResolver().delete(sWallPaperUri,
                    IMAGE_ID +" in " + sb.toString(), null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void addWallPaper(Context context, Set<Integer> longArray) {
        if (longArray == null || longArray.size() == 0) {
            return;
        }
        try {
            ContentValues[] values = new ContentValues[longArray.size()];
            ContentValues value = null;
            int index = 0;
            for (Integer aLong : longArray) {
                value = new ContentValues();
                value.put(IMAGE_ID, aLong);
                values[index++] = value;
            }
            context.getContentResolver().bulkInsert(sWallPaperUri,
                    values);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
