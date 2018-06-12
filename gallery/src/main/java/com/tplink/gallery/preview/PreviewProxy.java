package com.tplink.gallery.preview;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.tplink.gallery.R;
import com.tplink.gallery.bean.MediaBean;

public abstract class PreviewProxy {
    protected Activity mActivity;
    protected Intent intent;
    protected PreviewContract.PreviewView previewView;
    protected PreviewProxyHost previewProxyHost;
    public abstract PreviewContract.PreviewPresenter initPreviewPresenter();

    public int getLayoutId() {
        return R.layout.activity_local_media_preview;
    }

    public boolean canFilmMode() {
        return true;
    }

    public boolean onMenuItemClick(MenuItem item){
        return false;
    }

    public boolean onBackPressed() {
        return false;
    }

    public interface PreviewProxyHost {
        Toolbar getNormalBar();
        boolean isActive();
        MediaBean getCurrentMedia();
        int getCurrentMediaPosition();
        boolean isFileMode();
        boolean isSecureActivity();
        void popupShareMenu(MediaBean bean);
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        return false;
    }

    public PreviewProxy(Activity mActivity, Intent intent, PreviewContract.PreviewView previewView, PreviewProxyHost previewProxyHost) {
        this.mActivity = mActivity;
        this.intent = intent;
        this.previewView = previewView;
        this.previewProxyHost = previewProxyHost;
    }

    public void onStartCapture(Object command) {

    }

    public abstract void initView();

    public abstract void onPhotoChanged(int index, MediaBean item);

    public void onDetailViewVisibleChanged(boolean visible) {

    }
}
