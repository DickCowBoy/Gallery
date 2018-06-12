package com.tplink.gallery.preview;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import com.tplink.gallery.R;
import com.tplink.gallery.bean.MediaBean;
import com.tplink.gallery.preview.uri.UriPreviewPresenter;

public class UriPreviewProxy extends PreviewProxy {
    public static final String KEY_TP_SELECTED_POSITION = "tplink-selected-position";
    public static final String IMAGE_TYPE_URLS = "IMAGE_TYPE_URLS";

    private UriPreviewPresenter uriPreviewPresenter;

    public UriPreviewProxy(Activity mActivity, Intent intent, PreviewContract.PreviewView previewView, PreviewProxyHost previewProxyHost) {
        super(mActivity, intent, previewView, previewProxyHost);
    }

    @Override
    public PreviewContract.PreviewPresenter initPreviewPresenter() {
        Bundle data = new Bundle();
        data.putInt(KEY_TP_SELECTED_POSITION, intent.getIntExtra(KEY_TP_SELECTED_POSITION, 0));
        data.putParcelableArrayList(IMAGE_TYPE_URLS, intent.getParcelableArrayListExtra(IMAGE_TYPE_URLS));
        uriPreviewPresenter = new UriPreviewPresenter(mActivity, data, previewView);
        return uriPreviewPresenter;
    }

    @Override
    public void initView() {

    }

    @Override
    public void onPhotoChanged(int index, MediaBean item) {
        String title = mActivity.getString(R.string.total_selected_count,
                index + 1, uriPreviewPresenter.getCount());
        previewProxyHost.getNormalBar().setTitle(title);
    }
}
