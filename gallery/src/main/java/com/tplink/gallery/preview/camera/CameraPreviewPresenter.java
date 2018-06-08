package com.tplink.gallery.preview.camera;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;

import com.tplink.gallery.bean.MediaBean;
import com.tplink.gallery.data.DataCacheManager;
import com.tplink.gallery.data.MediaBeanCollection;
import com.tplink.gallery.preview.PreviewContract;
import com.tplink.gallery.utils.MediaUtils;

import java.util.List;

import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import io.reactivex.FlowableEmitter;
import io.reactivex.FlowableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subscribers.DisposableSubscriber;

public class CameraPreviewPresenter extends PreviewContract.PreviewPresenter<PreviewContract.PreviewView> {

    private CameraMediaDao mediaDao;
    private Context context;

    public CameraPreviewPresenter(Context context, PreviewContract.PreviewView view) {
        super(view);
        mediaDao = new CameraMediaDao(context);
        this.context = context;
    }

    @Override
    public void resume() {

    }

    @Override
    public void pause() {

    }

    @Override
    public void loadPreviewData(Bundle data) {
        Flowable.create(new FlowableOnSubscribe<PreviewInfo>() {
            @Override
            public void subscribe(
                    @NonNull FlowableEmitter<PreviewInfo> flowableEmitter)
                    throws Exception {
                PreviewInfo previewInfo = new PreviewInfo();
                CameraMediaBeanCollection cameraMediaBeanCollection = loadCameraMedias();
                previewInfo.datas = cameraMediaBeanCollection.mediaBeans;
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
                            mView.showMediaData(info.datas, info.index,DataCacheManager.dataManager.
                                    getMediaBeanCollectionByKey(
                                            MediaUtils.getAllCameraAlbumKey()).lastLoad);
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
                    }

                    @Override
                    protected void onStart() {
                        super.onStart();
                    }
                });
    }

    private CameraMediaBeanCollection loadCameraMedias() {
        MediaBeanCollection mediaBeanCollection = DataCacheManager.dataManager.
                getMediaBeanCollectionByKey(
                        MediaUtils.getAllCameraAlbumKey());
        CameraMediaBeanCollection cameraMediaBeanCollection = null;
        List<MediaBean> mediaBeans = null;
        if (mediaBeanCollection != null) {
            cameraMediaBeanCollection = (CameraMediaBeanCollection) mediaBeanCollection;
            if (DataCacheManager.dataManager.needReload(cameraMediaBeanCollection.lastLoad, true, true)) {
                mediaBeans = mediaDao.queryAllCamera();
                cameraMediaBeanCollection.updateCollection(mediaBeans);
            }
        } else {
            mediaBeans = mediaDao.queryAllCamera();
            cameraMediaBeanCollection = new CameraMediaBeanCollection(mediaBeans);
            DataCacheManager.dataManager.addMediaBeanCollection(cameraMediaBeanCollection);
        }
        return cameraMediaBeanCollection;
    }
}
