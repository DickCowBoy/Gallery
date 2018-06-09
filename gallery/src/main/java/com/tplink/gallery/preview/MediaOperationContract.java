package com.tplink.gallery.preview;

import com.tplink.base.BaseView;
import com.tplink.base.RxPresenter;
import com.tplink.gallery.bean.MediaBean;
import com.tplink.gallery.data.DataCacheManager;

public class MediaOperationContract {
    public interface MediaOperationView extends BaseView {
    }


    public static abstract class MediaOperationPresenter extends RxPresenter<MediaOperationView> {

        public MediaOperationPresenter(MediaOperationView view) {
            super(view);
        }

        public abstract void resume();
        public abstract void pause();
        public abstract void delPhoto(MediaBean mediaBean);

    }
}
