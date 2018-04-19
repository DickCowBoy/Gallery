package com.tplink.gallery;

import android.app.Application;

import com.tplink.gallery.data.DataCacheManager;

public class GalleryApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        DataCacheManager.getDataCacheManager().initCache(this);
    }
}
