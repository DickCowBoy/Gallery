/*
 * Copyright (C) 2018, TP-LINK TECHNOLOGIES CO., LTD.
 *
 * RefocusContract.java
 *
 * 背景虚化契约类
 *
 * Author LinJl
 *
 * Ver 1.0, 18-04-02, LinJl, Create file
 */
package com.tplink.gallery.refocus;

import android.content.Context;
import android.graphics.Bitmap;

import com.tplink.base.BaseView;
import com.tplink.base.RxPresenter;
import com.tplink.gallery.bean.MediaBean;

public class RefocusContract {

    public interface View extends BaseView {
        void showLoadingProgress();
        void hideLoadingProgress();
        void showSavingProgress(String albumName);
        void hideSavingProgress(int result);
        void showBitmap(Bitmap bitmap, int rotate, float x, float y, int blur, boolean anim);
    }

    public abstract static class Presenter extends RxPresenter<View> {
        public Presenter(View view) {
            super(view);
        }

        /**
         * 删除媒体文件
         * @param context
         * @param item
         */
        public abstract void loadRefocusData(Context context, MediaBean item);
        public abstract void processPreview(int blur);
        public abstract void processPreview(float x, float y);
        public abstract void processCapture();
        public abstract void destroy();
    }
}
