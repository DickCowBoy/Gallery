/*
 * Copyright (C) 2018, TP-LINK TECHNOLOGIES CO., LTD.
 *
 * BaseGalleryActivity.java
 *
 * Description 相册基类紧提供数据显示
 *
 * Author LinJinLong
 *
 * Ver 1.0, 2018-- LinJinLong, Create file
 */
package com.tplink.gallery.base;

import android.os.Bundle;
import android.support.annotation.Nullable;

import com.tplink.gallery.gallery.R;

public class BaseGalleryActivity extends PermissionActivity{

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_gallery);
    }
}
