/*
 * Copyright (C) 2018, TP-LINK TECHNOLOGIES CO., LTD.
 *
 * BaseDao.java
 *
 * Description content查询基类
 *
 * Author LinJinLong
 *
 * Ver 1.0, 2018-04-19 LinJinLong, Create file
 */
package com.tplink.gallery.dao;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

public class BaseDao {

    protected Context context;

    protected BaseDao(Context context) {
        this.context = context;
    }

    protected <T> T query(Uri uri,
                         String[] projection,
                         String selection,
                         String[] selectionArgs,
                         String sortOrder,
                         CursorProcessor<T> processor) {
        Cursor cursor = null;
        try {
            cursor = context.getContentResolver().query(uri,
                    projection,
                    selection,
                    selectionArgs,
                    sortOrder);
            if (processor != null) {
                return processor.process(cursor);
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return null;
    }

    protected static interface CursorProcessor<T> {
        T process(Cursor cursor);
    }
}
