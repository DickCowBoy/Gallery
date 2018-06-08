package com.tplink.gallery.preview.camera;

import android.os.Bundle;
import android.support.annotation.Nullable;

import com.tplink.gallery.preview.BaseLocalImagePreviewActivity;

public class CameraPreviewActivity extends BaseLocalImagePreviewActivity<CameraPreviewPresenter> {


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        showHeader("");
    }

    @Override
    protected CameraPreviewPresenter initPreviewPresenter() {
        return new CameraPreviewPresenter(this, this);
    }

}
