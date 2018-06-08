package com.tplink.gallery.preview;

import android.content.Intent;
import android.os.Bundle;

import com.tplink.base.BaseView;
import com.tplink.base.RxPresenter;
import com.tplink.gallery.bean.MediaBean;

import java.util.List;

public class PreviewContract {
    public interface PreviewView extends BaseView {
        void showMediaData(List<MediaBean> mediaBeans, int index, long version);

        void showHeader(String title);
    }


    public static abstract class PreviewPresenter<T extends PreviewView> extends RxPresenter<T> {

        public static final String CURRENT_MEDIA = "CURRENT_MEDIA";

        public PreviewPresenter(T view) {
            super(view);
        }

        public abstract void resume();
        public abstract void pause();

        /**
         * load the data to preview
         */
        public abstract void loadPreviewData(Bundle data);

        public class PreviewInfo {
            public List<MediaBean> datas;
            public int index = 0;
        }
    }
}
