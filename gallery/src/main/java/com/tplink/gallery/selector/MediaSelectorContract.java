package com.tplink.gallery.selector;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;

import com.tplink.base.BaseView;
import com.tplink.base.RxPresenter;
import com.tplink.gallery.bean.AlbumBean;
import com.tplink.gallery.bean.MediaBean;

import java.util.Collection;
import java.util.List;
import java.util.Set;

public interface MediaSelectorContract {

    public interface MediaSelectorView extends BaseView {
        void showHint(String msg);
        void showHeader(String title);
        void showErrorMsg(String title);
        void showSetResultFinished();
        void showSetResultStart();
        void showSelected(List<MediaBean> datas);
    }

    public static abstract class MediaSelectorPresenter extends RxPresenter<MediaSelectorView> {

        public MediaSelectorPresenter(MediaSelectorView view) {
            super(view);
        }
        public abstract void resume();
        public abstract void pause();

        public abstract boolean isFirstLoading();

        public abstract int getMaxSelectCount();
        public abstract boolean needSelectAlbum();
        public abstract boolean needPreview();
        public abstract boolean addSingleMedia(MediaBean bean);
        public abstract void removeSingleMedia(MediaBean bean);
        public abstract boolean addAlbumMedia(long bucketId, List<MediaBean> datas);
        public abstract void delAlbumMedia(long bucketId);

        public abstract List<MediaBean> getSelectMedia();
        public abstract Set<MediaBean> getSelectBucketMedia(long bucketId);
        public abstract boolean isBucketSelected(long bucketId);

        public abstract void initDataByAllMedia(List<MediaBean> datas, long version);
        public abstract void initDataByAlbums(List<AlbumBean> datas, long version);

        public abstract boolean needImage();

        public abstract boolean needVideo();

        public abstract void setResult(Activity activity);

        public abstract void loadSelectInfo(Context context, Intent intent);

        public abstract boolean isItemSelected(MediaBean item);
    }

}
