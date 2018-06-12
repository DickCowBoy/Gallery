package com.tplink.gallery.preview;

import android.content.Context;
import android.net.Uri;
import android.support.annotation.NonNull;

import com.tplink.gallery.bean.MediaBean;
import com.tplink.gallery.dao.MediaDao;
import com.tplink.gallery.data.DataCacheManager;
import com.tplink.gallery.data.MediaBeanCollection;
import com.tplink.gallery.data.SingleMediaCollection;
import com.tplink.gallery.utils.MediaUtils;

import java.util.List;

import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import io.reactivex.FlowableEmitter;
import io.reactivex.FlowableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subscribers.DisposableSubscriber;

public class LocalSinglePresenter extends PreviewContract.PreviewPresenter {
    private Context context;
    private Uri data;
    private MediaDao mediaDao;
    public LocalSinglePresenter(Context context, Uri data, PreviewContract.PreviewView view) {
        super(null, view);
        this.context = context;
        this.data = data;
        mediaDao = new MediaDao(context);
    }
    @Override
    public void loadPreviewData() {
        if (isLoading) return;
        isLoading = true;
        Flowable.create(new FlowableOnSubscribe<PreviewInfo>() {
            @Override
            public void subscribe(
                    @NonNull FlowableEmitter<PreviewInfo> flowableEmitter)
                    throws Exception {
                PreviewInfo previewInfo = new PreviewInfo();
                SingleMediaCollection allMediaBeanCollection = loadMediaInfo();
                previewInfo.datas = allMediaBeanCollection.mediaBeans;
                flowableEmitter.onNext(previewInfo);
                flowableEmitter.onComplete();
            }
        }, BackpressureStrategy.LATEST)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new DisposableSubscriber<PreviewInfo>() {
                    @Override
                    public void onNext(PreviewInfo info) {
                        if (mView != null && mView.isActive()) {
                            mView.showMediaData(info.datas, info.index, DataCacheManager.dataManager.
                                    getMediaBeanCollectionByKey(data.toString())
                                    .lastLoad);
                        }
                    }

                    @Override
                    public void onError(Throwable throwable) {
                        if (mView != null && mView.isActive()) {
                            mView.showMediaData(null, 0 , -1);
                        }
                    }

                    @Override
                    public void onComplete() {
                        isLoading = false;
                    }

                    @Override
                    protected void onStart() {
                        super.onStart();
                    }
                });
    }

    private SingleMediaCollection loadMediaInfo() {

        MediaBeanCollection mediaBeanCollection = DataCacheManager.dataManager.
                getMediaBeanCollectionByKey(data.toString());
        SingleMediaCollection allImageMediaBeanCollection = null;
        List<MediaBean> mediaBeans;
        if (mediaBeanCollection != null) {
            allImageMediaBeanCollection = (SingleMediaCollection) mediaBeanCollection;
            if (DataCacheManager.dataManager.needReload(allImageMediaBeanCollection.lastLoad, true, true)) {
                mediaBeans = mediaDao.queryMediaByFileUri(data.toString().replace("file://", ""));
                allImageMediaBeanCollection.updateCollection(mediaBeans);
            }
        } else {
            mediaBeans = mediaDao.queryMediaByFileUri(data.toString().replace("file://", ""));
            allImageMediaBeanCollection = new SingleMediaCollection(mediaBeans, data.toString());
            DataCacheManager.dataManager.addMediaBeanCollection(allImageMediaBeanCollection);
        }
        return allImageMediaBeanCollection;
    }

}
