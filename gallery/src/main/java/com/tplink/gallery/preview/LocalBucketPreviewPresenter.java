package com.tplink.gallery.preview;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;

import com.android.gallery3d.util.GalleryUtils;
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
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subscribers.DisposableSubscriber;

public class LocalBucketPreviewPresenter extends PreviewContract.PreviewPresenter {
    public static final String KEY_BUCKET_ID = "bucketId";
    private Context context;
    public Uri clickToShowUri;// the uri clicked by user
    private int clickId = -1;
    private MediaDao mediaDao;
    private int bucketId;
    public LocalBucketPreviewPresenter(Context context, Bundle data, PreviewContract.PreviewView view) {
        super(data, view);
        mediaDao = new MediaDao(context);
        this.context = context;
        clickToShowUri = data.getParcelable(LocalAllPresenter.CLICK_URI);
        bucketId = data.getInt(KEY_BUCKET_ID);
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
                AlbumDetailCollection albumMediaBeanCollection = loadBucketInfo();
                previewInfo.datas = albumMediaBeanCollection.mediaBeans;
                previewInfo.index = findPos(albumMediaBeanCollection.mediaBeans);
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
                                    getMediaBeanCollectionByKey(
                                            MediaUtils.getBucketId(bucketId,null,
                                                    null,
                                                    true,
                                                    false)).lastLoad);
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

    private int findPos(List<MediaBean> mediaBeans) {
        if (clickId < 0) {
            clickId = GalleryUtils.getMediaIdByUri(context, clickToShowUri);
        }
        if (clickId < 0) {
            return 0;
        }
        for (int i = 0; i < mediaBeans.size(); i++) {
            if (mediaBeans.get(i)._id == clickId) {
                return i;
            }
        }
        return 0;
    }

    private AlbumDetailCollection loadBucketInfo() {

        MediaBeanCollection mediaBeanCollection = DataCacheManager.dataManager.
                getMediaBeanCollectionByKey(
                        MediaUtils.getBucketId(bucketId,null,
                                null,
                                true,
                                false));
        AlbumDetailCollection allImageMediaBeanCollection;
        List<MediaBean> mediaBeans;
        if (mediaBeanCollection != null) {
            allImageMediaBeanCollection = (AlbumDetailCollection) mediaBeanCollection;
            if (DataCacheManager.dataManager.needReload(allImageMediaBeanCollection.lastLoad, false, true)) {
                mediaBeans = mediaDao.queryMediaByBucketId(bucketId, null,
                        null, false, true);
                allImageMediaBeanCollection.updateCollection(mediaBeans);
            }
        } else {
            mediaBeans = mediaDao.queryMediaByBucketId(bucketId, null,
                    null, false, true);
            allImageMediaBeanCollection = new AlbumDetailCollection(bucketId, mediaBeans,
                    null, null, true, false);
            DataCacheManager.dataManager.addMediaBeanCollection(allImageMediaBeanCollection);
        }
        return allImageMediaBeanCollection;
    }
}
