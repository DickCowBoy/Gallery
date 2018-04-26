/*
 * Copyright (C) 2018, TP-LINK TECHNOLOGIES CO., LTD.
 *
 * MediaContract.java
 *
 * Description 媒体操作契约类
 *
 * Author LinJinLong
 *
 * Ver 1.0, 2018-04-24 LinJinLong, Create file
 */
package com.tplink.gallery.base;

import com.tplink.base.BaseView;
import com.tplink.base.RxPresenter;
import com.tplink.gallery.bean.AlbumBean;
import com.tplink.gallery.bean.MediaBean;

import java.util.List;

public class MediaContract {

    public interface MediaView extends BaseView{
        void showMedias(List<MediaBean> beans, long version);
        void showAlbums(List<AlbumBean> beans, long version);
    }

    public interface AlbumView extends BaseView {
        void showMedias(List<MediaBean> beans, long lastLoad);
    }

    public static abstract class MediaPresenter extends RxPresenter<MediaView> {

        public MediaPresenter(MediaView view) {
            super(view);
        }

        public abstract void resume();
        public abstract void pause();
        public abstract void loadMediaInfo();
        public abstract void loadAlbumInfo();
    }

    public static abstract class AlbumDetailPresenter extends RxPresenter<AlbumView> {

        public AlbumDetailPresenter(AlbumView view) {
            super(view);
        }
        public abstract void resume();
        public abstract void pause();
        public abstract void loadAlbumDetail();

    }


}
