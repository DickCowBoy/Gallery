package com.tplink.gallery.preview.camera;

import android.app.Activity;
import android.content.Intent;

import com.tplink.gallery.preview.PreviewContract;

public class SecureCameraPreviewProxy extends CameraPreviewProxy {

    public SecureCameraPreviewProxy(Activity mActivity, Intent intent, PreviewContract.PreviewView previewView, PreviewProxyHost previewProxyHost) {
        super(mActivity, intent, previewView, previewProxyHost);
    }

    @Override
    public PreviewContract.PreviewPresenter initPreviewPresenter() {
        return null;
    }
}
