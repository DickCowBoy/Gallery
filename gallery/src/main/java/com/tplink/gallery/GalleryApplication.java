package com.tplink.gallery;

import android.app.Application;
import android.content.pm.PackageManager;

import com.android.gallery3d.util.Const;
import com.android.gallery3d.util.GalleryUtils;
import com.tplink.gallery.data.DataCacheManager;
import com.tplink.gallery.utils.GlideEngine;
import com.tplink.gallery.utils.MediaUtils;

import java.util.HashMap;
import java.util.Map;

public class GalleryApplication extends Application {

    private static GalleryApplication sInstance;
    public static GalleryApplication getApp() {
        return sInstance;
    }

    private int refocusType = Const.NONE_REFOCUS;

    private Map<String, Object> params = new HashMap<>();

    public void putParam(String key, Object value) {
        this.params.put(key, value);
    }

    public Object getParam(String key) {
        Object o = params.get(key);
        if (o != null) {
            params.remove(o);
        }
        return o;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        DataCacheManager.initDataCacheManager().initCache(this);
        MediaUtils.imageEngine = new GlideEngine();
        GalleryUtils.initialize(this);
        initRefocusType();
        sInstance = this;
    }

    private void initRefocusType() {
        if (TPGalleryJNi.isIsNativeSupport()) {
            refocusType = TPGalleryJNi.getRefocusType();
        } else {
            try {
                getPackageManager().getPackageInfo(TPGalleryJNi.REFOCUS_PACKAGE, PackageManager.GET_META_DATA);
                refocusType = Const.MTK_REFOCUS;
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    public int isSupportRefocus() {
        return refocusType;
    }
}
