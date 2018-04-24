package com.tplink.gallery.base;

import android.content.Context;
import android.support.annotation.NonNull;

import com.tplink.gallery.bean.AlbumBean;
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


public class MediaPresenter extends MediaContract.MediaPresenter {

    private MediaDao mediaDao;

    public MediaPresenter(MediaContract.MediaView view, Context context) {
        super(view);
        mediaDao = new MediaDao(context);
    }

    @Override
    public void loadMediaInfo(boolean needImage, boolean needVideo, boolean needGif, boolean needResolveBurst) {
        Disposable disposable = Flowable.create(new FlowableOnSubscribe<Result>() {
            @Override
            public void subscribe(
                    @NonNull FlowableEmitter<Result> flowableEmitter)
                    throws Exception {
                List<MediaBean> mediaBeans = mediaDao.queryAllMedia(needVideo, needImage, needGif, needResolveBurst);
                Result result = new Result();
                result.type = 0;
                result.mediaBeans = mediaBeans;
                flowableEmitter.onNext(result);
                // 解析相册信息

                flowableEmitter.onComplete();
            }
        }, BackpressureStrategy.LATEST)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new DisposableSubscriber<Result>() {
                    @Override
                    public void onNext(Result albumList) {
                        if (mView.isActive()) {
                            if (albumList.type == 0) {
                                mView.showMedias(albumList.mediaBeans);
                            }

                            if (albumList.type == 1) {
                                mView.showAlbums(albumList.albumBeans);
                            }
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
        Disposable disposable = Flowable.create(new FlowableOnSubscribe<Result>() {
            @Override
            public void subscribe(
                    @NonNull FlowableEmitter<Result> flowableEmitter)
                    throws Exception {
                List<AlbumBean> mediaBeans = mediaDao.queryAllAlbum(needVideo, needImage, needGif, needResolveBurst);
                Result result = new Result();
                result.type = 1;
                result.albumBeans = mediaBeans;
                flowableEmitter.onNext(result);
                // 解析相册信息

                flowableEmitter.onComplete();
            }
        }, BackpressureStrategy.LATEST)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new DisposableSubscriber<Result>() {
                    @Override
                    public void onNext(Result albumList) {
                        if (mView.isActive()) {
                            if (albumList.type == 0) {
                                mView.showMedias(albumList.mediaBeans);
                            }

                            if (albumList.type == 1) {
                                mView.showAlbums(albumList.albumBeans);
                            }
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


    class Result {
        public int type;// 0 所有 1相册
        public List<MediaBean> mediaBeans;
        public List<AlbumBean> albumBeans;
    }
}
