package com.tplink.gallery.base;

import android.content.Context;
import android.support.annotation.NonNull;

import com.tplink.gallery.bean.AlbumBean;
import com.tplink.gallery.bean.MediaBean;
import com.tplink.gallery.dao.MediaDao;
import com.tplink.gallery.data.DataCacheManager;
import com.tplink.gallery.data.MediaBeanCollection;
import com.tplink.gallery.utils.MediaUtils;

import java.util.List;

import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import io.reactivex.FlowableEmitter;
import io.reactivex.FlowableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subscribers.DisposableSubscriber;


public class MediaPresenter extends MediaContract.MediaPresenter {

    private MediaDao mediaDao;

    public MediaPresenter(MediaContract.MediaView view, Context context) {
        super(view);
        mediaDao = new MediaDao(context);
    }

    @Override
    public void loadMediaInfo(boolean needImage, boolean needVideo, boolean needGif, boolean needResolveBurst) {
        Disposable disposable = Flowable.create(new FlowableOnSubscribe<List<MediaBean>>() {
            @Override
            public void subscribe(
                    @NonNull FlowableEmitter<List<MediaBean>> flowableEmitter)
                    throws Exception {

                MediaBeanCollection mediaBeanCollectionByKey = DataCacheManager.dataManager.
                        getMediaBeanCollectionByKey(
                                MediaUtils.getAllMediaKey(needVideo, needImage, needGif, needResolveBurst));
                AllMediaBeanCollection allAlbumMediaCollection = null;
                List<MediaBean> mediaBeans = null;
                if (mediaBeanCollectionByKey != null) {
                    allAlbumMediaCollection = (AllMediaBeanCollection) mediaBeanCollectionByKey;
                    if (DataCacheManager.dataManager.needReload(allAlbumMediaCollection.lastLoad)) {
                        mediaBeans = mediaDao.queryAllMedia(needVideo, needImage, needGif, needResolveBurst);
                        allAlbumMediaCollection.updateCollection(mediaBeans);
                    } else {
                        mediaBeans = allAlbumMediaCollection.mediaBeans;
                    }
                } else {
                    mediaBeans = mediaDao.queryAllMedia(needVideo, needImage, needGif, needResolveBurst);
                    allAlbumMediaCollection = new AllMediaBeanCollection(mediaBeans,
                            needVideo,
                            needImage,
                            needGif,
                            needResolveBurst);
                    DataCacheManager.dataManager.addMediaBeanCollection(allAlbumMediaCollection);
                }
                flowableEmitter.onNext(mediaBeans);
                // 解析相册信息
                flowableEmitter.onComplete();
            }
        }, BackpressureStrategy.LATEST)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new DisposableSubscriber<List<MediaBean>>() {
                    @Override
                    public void onNext(List<MediaBean> albumList) {
                        if (mView.isActive()) {
                            mView.showMedias(albumList);
                        }
                    }

                    @Override
                    public void onError(Throwable throwable) {
                        if (mView.isActive()) {
                            // 不做处理
                            mView.showAlbums(null);
                        }
                    }

                    @Override
                    public void onComplete() {
                    }
                });
        addDispose(disposable);
    }

    @Override
    public void loadAlbumInfo(boolean needImage, boolean needVideo, boolean needGif, boolean needResolveBurst) {
        Disposable disposable = Flowable.create(new FlowableOnSubscribe<List<AlbumBean>>() {
            @Override
            public void subscribe(
                    @NonNull FlowableEmitter<List<AlbumBean>> flowableEmitter)
                    throws Exception {
                MediaBeanCollection mediaBeanCollectionByKey = DataCacheManager.dataManager.getMediaBeanCollectionByKey(
                        MediaUtils.getAllAlbumKey(needVideo, needImage, needGif, needResolveBurst));
                AllAlbumMediaCollection allAlbumMediaCollection = null;
                List<AlbumBean> mediaBeans = null;
                if (mediaBeanCollectionByKey != null) {
                    allAlbumMediaCollection = (AllAlbumMediaCollection) mediaBeanCollectionByKey;
                    if (DataCacheManager.dataManager.needReload(allAlbumMediaCollection.lastLoad)) {
                        mediaBeans = mediaDao.queryAllAlbum(needVideo, needImage, needGif, needResolveBurst);
                        allAlbumMediaCollection.updateCollection(mediaBeans);
                    } else {
                        mediaBeans = allAlbumMediaCollection.mediaBeans;
                    }
                } else {
                    mediaBeans = mediaDao.queryAllAlbum(needVideo, needImage, needGif, needResolveBurst);
                    allAlbumMediaCollection = new AllAlbumMediaCollection(mediaBeans, needVideo, needImage, needGif, needResolveBurst);
                    DataCacheManager.dataManager.addMediaBeanCollection(allAlbumMediaCollection);
                }
                flowableEmitter.onNext(mediaBeans);
                // 解析相册信息
                flowableEmitter.onComplete();
            }
        }, BackpressureStrategy.LATEST)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new DisposableSubscriber<List<AlbumBean>>() {
                    @Override
                    public void onNext(List<AlbumBean> albumList) {
                        if (mView.isActive()) {
                            mView.showAlbums(albumList);
                        }
                    }

                    @Override
                    public void onError(Throwable throwable) {
                        if (mView.isActive()) {
                            // 不做处理
                            mView.showAlbums(null);
                        }
                    }

                    @Override
                    public void onComplete() {
                    }
                });
        addDispose(disposable);
    }
}
