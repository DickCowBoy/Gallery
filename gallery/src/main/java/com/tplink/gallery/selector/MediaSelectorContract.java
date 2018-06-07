package com.tplink.gallery.selector;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;

import com.tplink.base.BaseView;
import com.tplink.base.RxPresenter;
import com.tplink.gallery.bean.AlbumBean;
import com.tplink.gallery.bean.MediaBean;
import com.tplink.gallery.R;
import com.tplink.gallery.selector.wallpaper.ResultContainer;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public interface MediaSelectorContract {

    interface MediaSelectorView extends BaseView {
        void showHint(String msg);
        void showHeader(String title);
        void showErrorMsg(String title);
        void showSetResultFinished();
        void showSetResultStart();
        void showSelected(List<MediaBean> datas);
    }

    abstract class MediaSelectorPresenter extends RxPresenter<MediaSelectorView> {


        protected Context context;
        protected ResultContainer mContainer;
        protected boolean isLoading = false;
        protected boolean needPreview = false;
        private boolean needImage;
        private boolean needVideo;

        public MediaSelectorPresenter(Context context, MediaSelectorView view,
                                      int countLimit, long sizeLimit,
                                      boolean needPreview,
                                      boolean needImage,
                                      boolean needVideo) {
            super(view);
            this.context = context;
            this.needPreview = needPreview;
            this.needImage = needImage;
            this.needVideo = needVideo;
            mContainer = initRule(countLimit, sizeLimit);
        }

        protected ResultContainer initRule(int countLimit, long sizeLimit) {
            return new ResultContainer(countLimit, sizeLimit);
        }

        public abstract void resume();
        public abstract void pause();

        public abstract boolean isFirstLoading();

        public int getMaxSelectCount() {
            return mContainer.getCountLimit();
        }
        public abstract boolean needSelectAlbum();
        public boolean needPreview() {
            return needPreview;
        }
        public abstract Collection<MediaBean> addAlbumMedia(long bucketId);

        public Collection<MediaBean> delAlbumMedia(long bucketId) {
            Collection<MediaBean> mediaBeans = mContainer.delBucketItems(bucketId);
            showTitle();
            return mediaBeans;
        }

        public List<MediaBean> getSelectMedia() {
            return mContainer.getMediaEntries();
        }

        public Set<MediaBean> getSelectBucketMedia(long bucketId) {
            if (bucketId == -1) {
                Set<MediaBean> ret = new HashSet<>();
                ret.addAll(mContainer.getMediaEntries());
                return ret;
            }
            return mContainer.getSelectBucketItems(bucketId);
        }
        public abstract boolean isBucketSelected(long bucketId);

        public abstract void initDataByAllMedia(List<MediaBean> datas, long version);
        public abstract void initDataByAlbums(List<AlbumBean> datas, long version);

        public boolean needImage() {
            return needImage;
        }

        public boolean needVideo() {
            return needVideo;
        }

        public abstract void setResult(Activity activity);

        public abstract void loadSelectInfo(Context context, Intent intent);

        public boolean addSingleMedia(MediaBean bean) {
            int result = mContainer.addItem(bean);
            if (result != 0) {
                if (mView != null) {
                    mView.showErrorMsg(mContainer.getResultHint(context, result));
                }
            }
            if (result == 0 && mView != null) {
                showTitle();
            }
            return result == 0;
        }

        protected abstract void showTitle();

        public void removeSingleMedia(MediaBean bean) {
            mContainer.delItem(bean);
            if (mView != null) {
                showTitle();
            }
        }

        public boolean isItemSelected(MediaBean item) {
            Set<MediaBean> selectBucketItems = mContainer.getSelectBucketItems(item.bucketId);
            return selectBucketItems != null && selectBucketItems.contains(item);
        }

    }

}
