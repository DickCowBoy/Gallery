package com.tplink.gallery.preview;

import android.content.Context;
import android.support.annotation.NonNull;

import com.tplink.gallery.bean.MediaBean;
import com.tplink.gallery.data.DataCacheManager;
import com.tplink.gallery.utils.MediaUtils;

import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import io.reactivex.FlowableEmitter;
import io.reactivex.FlowableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subscribers.DisposableSubscriber;

public class MediaOperationPresenter extends MediaOperationContract.MediaOperationPresenter {

    private Context context;

    public MediaOperationPresenter(Context context, MediaOperationContract.MediaOperationView view) {
        super(view);
        this.context = context;
    }

    @Override
    public void resume() {

    }

    @Override
    public void pause() {

    }

    @Override
    public void delPhoto(MediaBean mediaBean) {
        Flowable.create(new FlowableOnSubscribe<Integer>() {
            @Override
            public void subscribe(
                    @NonNull FlowableEmitter<Integer> flowableEmitter)
                    throws Exception {
                MediaUtils.deleteMedia(context, mediaBean);
                flowableEmitter.onNext(0);
                flowableEmitter.onComplete();
            }
        }, BackpressureStrategy.LATEST)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new DisposableSubscriber<Integer>() {
                    @Override
                    public void onNext(Integer info) {
                        if (mView != null && mView.isActive()) {
                        }
                    }

                    @Override
                    public void onError(Throwable throwable) {
                        if (mView != null && mView.isActive()) {
                        }
                    }

                    @Override
                    public void onComplete() {
                    }

                    @Override
                    protected void onStart() {
                        super.onStart();
                    }
                });
    }
}
