package com.tplink.gallery.base;

import android.content.Context;
import android.support.annotation.NonNull;

import com.tplink.gallery.bean.MediaBean;
import com.tplink.gallery.dao.MediaDao;

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
                List<MediaBean> mediaBeans = mediaDao.queryMediaByBucketId(bucketId, needVideo, needImage, needGif);

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
