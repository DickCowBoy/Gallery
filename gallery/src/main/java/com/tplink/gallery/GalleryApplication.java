package com.tplink.gallery;

import android.app.Application;

import com.tplink.gallery.data.DataCacheManager;
import com.tplink.gallery.utils.GlideEngine;
import com.tplink.gallery.utils.ImageEngine;
import com.tplink.gallery.utils.MediaUtils;

public class GalleryApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        DataCacheManager.getDataCacheManager().initCache(this);
        MediaUtils.imageEngine = new GlideEngine();
    }
}
