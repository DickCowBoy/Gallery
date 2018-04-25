package com.tplink.gallery.base;

import android.content.Context;
import android.support.annotation.NonNull;

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

public class AlbumDetailPresenter extends MediaContract.AlbumDetailPresenter {

    private MediaDao mediaDao;

    public AlbumDetailPresenter(Context context, MediaContract.AlbumView view) {
        super(view);
        mediaDao = new MediaDao(context);
    }

    @Override
    public void loadAlbumDetail(long bucketId, boolean needImage, boolean needVideo, boolean needGif) {
        Disposable disposable = Flowable.create(new FlowableOnSubscribe<List<MediaBean>>() {
            @Override
            public void subscribe(
                    @NonNull FlowableEmitter<List<MediaBean>> flowableEmitter)
                    throws Exception {
                MediaBeanCollection mediaBeanCollectionByKey = DataCacheManager.dataManager.getMediaBeanCollectionByKey(MediaUtils.getBucketId(bucketId, needVideo, needImage, needGif));
                AlbumDetailCollection albumDetailCollection = null;
                List<MediaBean> mediaBeans = null;
                if (mediaBeanCollectionByKey != null) {
                    albumDetailCollection = (AlbumDetailCollection) mediaBeanCollectionByKey;

                    if (DataCacheManager.dataManager.needReload(albumDetailCollection.lastLoad)) {
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

                flowableEmitter.onNext(mediaBeans);
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
                            mView.showMedias(null);
                        }
                    }

                    @Override
                    public void onComplete() {
                    }
                });
        addDispose(disposable);
    }
}
