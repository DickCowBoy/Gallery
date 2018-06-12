package com.tplink.gallery.preview.camera;

import android.app.Activity;
import android.content.Intent;

import com.tplink.gallery.preview.PreviewContract;

public class CameraPreviewProxy extends BaseLocalPreviewProxy {
    public CameraPreviewProxy(Activity mActivity, Intent intent, PreviewContract.PreviewView previewView, PreviewProxyHost previewProxyHost) {
        super(mActivity, intent, previewView, previewProxyHost);
    }

    @Override
    public PreviewContract.PreviewPresenter initPreviewPresenter() {
        return new CameraPreviewPresenter(null, mActivity, previewView);
    }
}
