package com.tplink.gallery.preview.camera;

import android.os.Bundle;
import android.support.annotation.Nullable;

import com.tplink.gallery.R;
import com.tplink.gallery.bean.MediaBean;
import com.tplink.gallery.preview.BaseLocalImagePreviewActivity;
import com.tplink.gallery.ui.BurstPhotoCountControls;

public class CameraPreviewActivity extends BaseLocalImagePreviewActivity<CameraPreviewPresenter>{

    BurstPhotoCountControls mBurstControls;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        showHeader("");
        mBurstControls = new BurstPhotoCountControls(this,
                findViewById(R.id.rl_gallery_root));
    }

    @Override
    protected CameraPreviewPresenter initPreviewPresenter(Bundle data) {
        return new CameraPreviewPresenter(data, this, this);
    }

    @Override
    public void onPhotoChanged(int index, MediaBean item) {
        super.onPhotoChanged(index, item);
        if (item.isBurst) {
            mBurstControls.refresh(item.burstCount);
        } else {
            mBurstControls.refresh(0);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mBurstControls != null) mBurstControls.cleanup();
    }
}
