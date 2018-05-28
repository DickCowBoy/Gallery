package com.tplink.gallery.selector;

import com.tplink.base.BaseView;
import com.tplink.base.RxPresenter;
import com.tplink.gallery.bean.AlbumBean;
import com.tplink.gallery.bean.MediaBean;

import java.util.List;

public interface MediaSelectorContract {

    public interface MediaSelectorView extends BaseView {
        void showHint(String msg);
        void showHeader(String title);
    }

    public static abstract class MediaSelectorPresenter extends RxPresenter<MediaSelectorView> {

        public MediaSelectorPresenter(MediaSelectorView view) {
            super(view);
        }
        public abstract void resume();
        public abstract void pause();
        public abstract void loadAlbumDetail();

        public abstract int getMaxSelectCount();
        public abstract boolean needSelectAlbum();
        public abstract boolean needPreview();
        public abstract boolean addSingleMedia();
        public abstract boolean addAlbumMedia();

        public abstract List<MediaBean> getSelectMedia();
        public abstract List<MediaBean> getSelectBucketMedia();
        public abstract boolean isBucketSelected(long bucketId);

        public abstract void initDataByAllMedia(List<MediaBean> datas, long version);
        public abstract void initDataByAlbums(List<AlbumBean> datas, long version);

        public abstract boolean needImage();

        public abstract boolean needVideo();

        public abstract boolean needGif();

        public abstract void setResult();



    }

}
