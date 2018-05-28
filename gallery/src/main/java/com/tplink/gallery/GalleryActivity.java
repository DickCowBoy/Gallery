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

import java.util.ArrayList;

public class GalleryActivity extends BaseGalleryActivity {

    @Override
    protected boolean awaysInSelectMode() {
        return false;
    }

    @Override
    protected boolean needResolveBurst() {
        return true;
    }

    @Override
    public ArrayList<String> getAllowMimeTypes() {
        ArrayList<String> ret = new ArrayList<>();
        ret.add("video/3gpp");
        ret.add("image/png");
        return ret;

    }

    @Override
    public ArrayList<String> getNotAllowMimeTypes() {
        return null;
    }
}
