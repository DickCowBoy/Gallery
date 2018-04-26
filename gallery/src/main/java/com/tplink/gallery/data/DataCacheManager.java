/*
 * Copyright (C) 2018, TP-LINK TECHNOLOGIES CO., LTD.
 *
 * DataCacheManager.java
 *
 * Description 媒体缓存管理,并负责更新等相关操作
 *
 * Author LinJinLong
 *
 * Ver 1.0, 2018-04-18 LinJinLong, Create file
 */
package com.tplink.gallery.data;

import android.app.Application;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.SparseArray;
import android.util.SparseIntArray;

import com.tplink.gallery.bean.MediaBean;
import com.tplink.gallery.dao.MediaDao;
import com.tplink.gallery.utils.MediaUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import io.reactivex.Observable;
import io.reactivex.Scheduler;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;

public class DataCacheManager {

    public static DataCacheManager dataManager = null;

    private SparseArray<MediaBean> cacheItems = new SparseArray<>();

    private Map<String, MediaBeanCollection> cacheMediaBeanCollectionMap = new HashMap<>();
    private Set<OnMediaChanged> onMediaChanged = new HashSet<>();

    private ContentObserver observer;

    private Application application;

    private long lastNotifyImage;
    private long lastNotifyVideo;

    private DataCacheManager(){}

    public synchronized static DataCacheManager initDataCacheManager() {
        if (dataManager == null) {
            dataManager = new DataCacheManager();
        }
        return dataManager;
    }

    public void addMediaBeanCollection(MediaBeanCollection collection) {
        cacheMediaBeanCollectionMap.put(collection.key(), collection);
    }

    public MediaBeanCollection getMediaBeanCollectionByKey(String key) {
        return cacheMediaBeanCollectionMap.get(key);
    }

    public void cacheMediaBean(MediaBean bean) {
        cacheItems.put(bean._id, bean);
    }

    public MediaBean getCacheMediaBean(int id) {
        return cacheItems.get(id);
    }

    public void initCache(Application application) {
        // 注意新建Thread避免变化大时增加主线程负担造成卡顿问题
        HandlerThread handlerThread = new HandlerThread("process media change", Thread.NORM_PRIORITY);
        handlerThread.start();

        observer = new MediaObserver(new Handler(handlerThread.getLooper()));
        application.getContentResolver().registerContentObserver(MediaUtils.getVideoUri(), true, observer);
        application.getContentResolver().registerContentObserver(MediaUtils.getImageUri(), true, observer);
        this.application = application;
    }

    class MediaObserver extends ContentObserver {

        private Scheduler currentThread = null;

        public MediaObserver(Handler handler) {
            super(handler);
            currentThread = AndroidSchedulers.from(handler.getLooper());
        }

        boolean isChangedProcessing = false;
        long lastDelTime = 0;
        List<String> updateImageIds = new ArrayList<>();
        List<String> updateVideoIds = new ArrayList<>();



        // 能够获取变化的数据，不需要重新查询,合理使用selfChange
        @Override
        public void onChange(boolean selfChange, Uri uri) {
            // 需要在单一的线程中处理避免媒体库变化量大时问题
            // content://media/external/images/media/123 增加图片
            // content://media/external/video/media/125  增加视频
            // content://media/external // 图片或者视频被删除
            // 自己触发的删除自己处理不通过内容观察着处理
            List<String> pathSegments = uri.getPathSegments();
            if (pathSegments == null || (pathSegments.size() !=4 && pathSegments.size() != 1)) {
                return;
            }
            synchronized(updateImageIds) {
                if (pathSegments.size() == 4) {
                    if ("images".equals(pathSegments.get(1))) {
                        updateImageIds.add(pathSegments.get(3));
                        lastNotifyImage = System.currentTimeMillis();
                    } else if ("video".equals(pathSegments.get(1))) {
                        updateVideoIds.add(pathSegments.get(3));
                        lastNotifyVideo = System.currentTimeMillis();
                    }
                } else {
                    lastDelTime = System.currentTimeMillis();
                    lastNotifyImage = System.currentTimeMillis();
                    lastNotifyVideo = System.currentTimeMillis();
                }
            }

            if (!isChangedProcessing) {
                // 把待处理数据加入缓存中
                Observable.just("")
                        .subscribeOn(Schedulers.io())
                        .map(new Function<String, String>() {
                            @Override
                            public String apply(String updateCondition) throws Exception {
                                List<String> needUpdateImageIds = new ArrayList<>();
                                List<String> needUpdateVideoIds = new ArrayList<>();
                                needUpdateImageIds.addAll(updateImageIds);
                                needUpdateVideoIds.addAll(updateVideoIds);
                                long loadTime;
                                while (needUpdateImageIds.size() > 0 || needUpdateVideoIds.size() > 0 || lastDelTime != 0) {

                                    loadTime = System.currentTimeMillis();
                                    for (OnMediaChanged mediaChanged : onMediaChanged) {
                                        mediaChanged.onMediaChanged(needUpdateImageIds, needUpdateVideoIds, lastDelTime != 0);
                                    }

                                    updateImageIds.removeAll(needUpdateImageIds);
                                    needUpdateImageIds.clear();

                                    updateVideoIds.removeAll(needUpdateVideoIds);
                                    needUpdateVideoIds.clear();

                                    synchronized (updateImageIds) {
                                        needUpdateImageIds.addAll(updateImageIds);
                                        needUpdateVideoIds.addAll(updateVideoIds);
                                    }

                                    if (lastDelTime <= loadTime) {
                                        lastDelTime = 0;
                                    }

                                }

                                return "";
                            }
                        })
                        .observeOn(currentThread)
                        .subscribe();
            }
        }
    }

    public boolean needReload(long time, boolean video, boolean image) {
        boolean needReload = false;
        if (video) {
            needReload = (time < lastNotifyImage) || needReload;
        }
        if (image) {
            needReload = (time < lastNotifyVideo) || needReload;
        }
        return needReload;
    }

    public void registerOnMediaChanged(OnMediaChanged onMediaChanged) {
        this.onMediaChanged.add(onMediaChanged);
    }
    public void unregisterOnMediaChanged(OnMediaChanged onMediaChanged) {
        this.onMediaChanged.remove(onMediaChanged);
    }

    public interface OnMediaChanged {

        void onMediaChanged(List<String> images, List<String> videos, boolean del);
    }
}
