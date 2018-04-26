package com.tplink.gallery.base;

import android.content.Context;
import android.support.annotation.NonNull;

import com.tplink.gallery.bean.AlbumBean;
import com.tplink.gallery.bean.MediaBean;
import com.tplink.gallery.dao.MediaDao;
import com.tplink.gallery.data.AllAlbumMediaCollection;
import com.tplink.gallery.data.AllMediaBeanCollection;
import com.tplink.gallery.data.DataCacheManager;
import com.tplink.gallery.data.MediaBeanCollection;
import com.tplink.gallery.utils.MediaUtils;

import java.util.List;

import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import io.reactivex.FlowableEmitter;
import io.reactivex.FlowableOnSubscribe;
import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subscribers.DisposableSubscriber;


public class MediaPresenter extends MediaContract.MediaPresenter implements DataCacheManager.OnMediaChanged {

    private MediaDao mediaDao;
    private boolean needImage;
    private boolean needVideo;
    private boolean needGif;
    private boolean needResolveBurst;

    public MediaPresenter(MediaContract.MediaView view, Context context, boolean needImage, boolean needVideo, boolean needGif, boolean needResolveBurst) {
        super(view);
        mediaDao = new MediaDao(context);
        this.needImage = needImage;
        this.needVideo = needVideo;
        this.needGif = needGif;
        this.needResolveBurst = needResolveBurst;
    }

    @Override
    public void resume() {
        DataCacheManager.dataManager.registerOnMediaChanged(this);
    }

    @Override
    public void pause() {
        DataCacheManager.dataManager.unregisterOnMediaChanged(this);
    }

    @Override
    public void loadMediaInfo() {
        Disposable disposable = Flowable.create(new FlowableOnSubscribe<AllMediaBeanCollection>() {
            @Override
            public void subscribe(
                    @NonNull FlowableEmitter<AllMediaBeanCollection> flowableEmitter)
                    throws Exception {
                flowableEmitter.onNext(loadImage());
                // 解析相册信息
                flowableEmitter.onComplete();
            }
        }, BackpressureStrategy.LATEST)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new DisposableSubscriber<AllMediaBeanCollection>() {
                    @Override
                    public void onNext(AllMediaBeanCollection albumList) {
                        if (mView.isActive()) {
                            mView.showMedias(albumList.mediaBeans, albumList.lastLoad);
                        }
                    }

                    @Override
                    public void onError(Throwable throwable) {
                        if (mView.isActive()) {
                            // 不做处理
                            mView.showAlbums(null, -1);
                        }
                    }

                    @Override
                    public void onComplete() {
                    }
                });
        addDispose(disposable);
    }

    @Override
    public void loadAlbumInfo() {
        Disposable disposable = Flowable.create(new FlowableOnSubscribe<AllAlbumMediaCollection>() {
            @Override
            public void subscribe(
                    @NonNull FlowableEmitter<AllAlbumMediaCollection> flowableEmitter)
                    throws Exception {

                flowableEmitter.onNext(loadAlbum());
                // 解析相册信息
                flowableEmitter.onComplete();
            }
        }, BackpressureStrategy.LATEST)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new DisposableSubscriber<AllAlbumMediaCollection>() {
                    @Override
                    public void onNext(AllAlbumMediaCollection albumList) {
                        if (mView.isActive()) {
                            mView.showAlbums(albumList.mediaBeans, albumList.lastLoad);
                        }
                    }

                    @Override
                    public void onError(Throwable throwable) {
                        if (mView.isActive()) {
                            // 不做处理
                            mView.showAlbums(null, -1);
                        }
                    }

                    @Override
                    public void onComplete() {
                    }
                });
        addDispose(disposable);
    }

    @Override
    public void onMediaChanged(List<String> images, List<String> videos, boolean del) {
        if (del) {
            updateMedia();
            return;
        }
        if (needImage && images.size() > 0) {
            updateMedia();
            return;
        }
        if (needVideo && videos.size() > 0) {
            updateMedia();
            return;
        }
    }

    private void updateMedia() {
        AllMediaBeanCollection allMediaBeanCollection = loadImage();
        AllAlbumMediaCollection allAlbumMediaCollection = loadAlbum();
        Observable.just(allMediaBeanCollection)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<AllMediaBeanCollection>() {
            @Override
            public void onSubscribe(Disposable d) {

            }

            @Override
            public void onNext(AllMediaBeanCollection value) {
                if (mView.isActive()) {
                    mView.showMedias(value.mediaBeans, value.lastLoad);
                }
            }

            @Override
            public void onError(Throwable e) {

            }

            @Override
            public void onComplete() {
            }
        });
        Observable.just(allAlbumMediaCollection)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<AllAlbumMediaCollection>() {
            @Override
            public void onSubscribe(Disposable d) {

            }

            @Override
            public void onNext(AllAlbumMediaCollection value) {
                if (mView.isActive()) {
                    mView.showAlbums(value.mediaBeans, value.lastLoad);
                }
            }

            @Override
            public void onError(Throwable e) {

            }

            @Override
            public void onComplete() {

            }
        });
    }

    private AllAlbumMediaCollection loadAlbum() {
        MediaBeanCollection mediaBeanCollectionByKey = DataCacheManager.dataManager.getMediaBeanCollectionByKey(
                MediaUtils.getAllAlbumKey(needVideo, needImage, needGif, needResolveBurst));
        AllAlbumMediaCollection allAlbumMediaCollection = null;
        List<AlbumBean> mediaBeans = null;
        if (mediaBeanCollectionByKey != null) {
            allAlbumMediaCollection = (AllAlbumMediaCollection) mediaBeanCollectionByKey;
            if (DataCacheManager.dataManager.needReload(allAlbumMediaCollection.lastLoad, needVideo, needImage)) {
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
        return allAlbumMediaCollection;
    }

    public AllMediaBeanCollection loadImage() {
        MediaBeanCollection mediaBeanCollectionByKey = DataCacheManager.dataManager.
                getMediaBeanCollectionByKey(
                        MediaUtils.getAllMediaKey(needVideo, needImage, needGif, needResolveBurst));
        AllMediaBeanCollection allAlbumMediaCollection = null;
        List<MediaBean> mediaBeans = null;
        if (mediaBeanCollectionByKey != null) {
            allAlbumMediaCollection = (AllMediaBeanCollection) mediaBeanCollectionByKey;
            if (DataCacheManager.dataManager.needReload(allAlbumMediaCollection.lastLoad, needVideo, needImage)) {
                mediaBeans = mediaDao.queryAllMedia(needVideo, needImage, needGif, needResolveBurst);
                allAlbumMediaCollection.updateCollection(mediaBeans);
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
        return allAlbumMediaCollection;
    }
}
