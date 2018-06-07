package com.tplink.gallery;

import android.app.Application;

import com.android.gallery3d.util.GalleryUtils;
import com.tplink.gallery.data.DataCacheManager;
import com.tplink.gallery.utils.GlideEngine;
import com.tplink.gallery.utils.MediaUtils;

public class GalleryApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        DataCacheManager.initDataCacheManager().initCache(this);
        MediaUtils.imageEngine = new GlideEngine();
        GalleryUtils.initialize(this);
    }
}
