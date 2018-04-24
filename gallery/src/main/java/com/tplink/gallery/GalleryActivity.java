/*
 * Copyright (C) 2018, TP-LINK TECHNOLOGIES CO., LTD.
 *
 * GalleryActivity.java
 *
 * Description 显示所有照片及相册
 *
 * Author LinJinLong
 *
 * Ver 1.0, 2018-04-20 LinJinLong, Create file
 */
package com.tplink.gallery;

import com.tplink.gallery.base.BaseGalleryActivity;

public class GalleryActivity extends BaseGalleryActivity {

    @Override
    protected boolean needImage() {
        return true;
    }

    @Override
    protected boolean needVideo() {
        return true;
    }

    @Override
    protected boolean needGif() {
        return true;
    }

    @Override
    protected boolean awaysInSelectMode() {
        return false;
    }

    @Override
    protected boolean needResolveBurst() {
        return true;
    }
}
