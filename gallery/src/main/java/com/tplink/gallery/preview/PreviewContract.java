package com.tplink.gallery.preview;

import android.content.Intent;
import android.os.Bundle;

import com.tplink.base.BaseView;
import com.tplink.base.RxPresenter;
import com.tplink.gallery.bean.MediaBean;
import com.tplink.gallery.data.DataCacheManager;

import java.util.List;

public class PreviewContract {
    public interface PreviewView extends BaseView {
        void showMediaData(List<MediaBean> mediaBeans, int index, long version);

        void showHeader(String title);
    }


    public static abstract class PreviewPresenter<T extends PreviewView> extends RxPresenter<T>
            implements DataCacheManager.OnMediaChanged{

        public static final String CURRENT_MEDIA = "CURRENT_MEDIA";
        protected Bundle data;

        public PreviewPresenter(Bundle data, T view) {
            super(view);
            this.data = data;
        }

        public void resume() {
            DataCacheManager.initDataCacheManager().registerOnMediaChanged(this);
        }
        public void pause() {
            DataCacheManager.initDataCacheManager().unregisterOnMediaChanged(this);
        }

        @Override
        public void onMediaChanged(List<String> images, List<String> videos, boolean del) {
            loadPreviewData();
        }

        /**
         * load the data to preview
         */
        public abstract void loadPreviewData();

        public class PreviewInfo {
            public List<MediaBean> datas;
            public int index = 0;
        }
    }
}
