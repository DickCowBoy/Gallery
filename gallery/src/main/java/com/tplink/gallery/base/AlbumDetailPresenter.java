package com.tplink.gallery.base;

import android.content.Context;
import android.support.annotation.NonNull;

import com.tplink.gallery.bean.MediaBean;
import com.tplink.gallery.dao.MediaDao;
import com.tplink.gallery.data.AlbumDetailCollection;
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
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subscribers.DisposableSubscriber;

public class AlbumDetailPresenter extends MediaContract.AlbumDetailPresenter implements DataCacheManager.OnMediaChanged {

    private MediaDao mediaDao;
    private long bucketId;
    private boolean needImage;
    private boolean needVideo;
    private boolean needGif;

    public AlbumDetailPresenter(Context context, MediaContract.AlbumView view, long bucketId, boolean needImage, boolean needVideo, boolean needGif) {
        super(view);
        mediaDao = new MediaDao(context);
        this.bucketId = bucketId;
        this.needImage = needImage;
        this.needVideo = needVideo;
        this.needGif = needGif;
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
    public void loadAlbumDetail() {
        Disposable disposable = Flowable.create(new FlowableOnSubscribe<AlbumDetailCollection>() {
            @Override
            public void subscribe(
                    @NonNull FlowableEmitter<AlbumDetailCollection> flowableEmitter)
                    throws Exception {

                flowableEmitter.onNext(loadDetail());
                flowableEmitter.onComplete();
            }
        }, BackpressureStrategy.LATEST)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new DisposableSubscriber<AlbumDetailCollection>() {
                    @Override
                    public void onNext(AlbumDetailCollection albumList) {
                        if (mView.isActive()) {
                            mView.showMedias(albumList.mediaBeans, albumList.lastLoad);
                        }
                    }

                    @Override
                    public void onError(Throwable throwable) {
                        if (mView.isActive()) {
                            // 不做处理
                            mView.showMedias(null, -1);
                        }
                    }

                    @Override
                    public void onComplete() {
                    }
                });
        addDispose(disposable);
    }

    private AlbumDetailCollection loadDetail() {
        MediaBeanCollection mediaBeanCollectionByKey = DataCacheManager.dataManager.getMediaBeanCollectionByKey(MediaUtils.getBucketId(bucketId, needVideo, needImage, needGif));
        AlbumDetailCollection albumDetailCollection = null;
        List<MediaBean> mediaBeans = null;
        if (mediaBeanCollectionByKey != null) {
            albumDetailCollection = (AlbumDetailCollection) mediaBeanCollectionByKey;

            if (DataCacheManager.dataManager.needReload(albumDetailCollection.lastLoad, needVideo, needImage)) {
                mediaBeans = mediaDao.queryMediaByBucketId(bucketId, needVideo, needImage, needGif);
                albumDetailCollection.updateCollection(mediaBeans);
            } else {
                mediaBeans = albumDetailCollection.mediaBeans;
            }
        } else {
            mediaBeans = mediaDao.queryMediaByBucketId(bucketId, needVideo, needImage, needGif);
            albumDetailCollection = new AlbumDetailCollection(bucketId, mediaBeans, needImage, needVideo, needGif);
            DataCacheManager.dataManager.addMediaBeanCollection(albumDetailCollection);
        }
        return albumDetailCollection;
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
        AlbumDetailCollection albumDetailCollection = loadDetail();
        Observable.just(albumDetailCollection)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<AlbumDetailCollection>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onNext(AlbumDetailCollection value) {
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
    }
}
