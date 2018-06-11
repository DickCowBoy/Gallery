package com.tplink.gallery.preview.camera;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.Menu;
import android.view.MenuItem;

import com.tplink.gallery.R;
import com.tplink.gallery.bean.MediaBean;
import com.tplink.gallery.preview.BaseLocalImagePreviewActivity;
import com.tplink.gallery.ui.BurstPhotoCountControls;

public class CameraPreviewActivity extends BaseLocalImagePreviewActivity<CameraPreviewPresenter> {

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
        // refresh the menu
        invalidateOptionsMenu();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mBurstControls != null) mBurstControls.cleanup();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.photo, menu);
        MediaBean currentItem = getCurrentItem();
        menu.findItem(R.id.action_setas).setVisible(currentItem != null
                && currentItem.isImage() && !currentItem.isGif());
        return true;
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_setas:
                Intent intent = getIntentBySingleSelectedPath(Intent.ACTION_ATTACH_DATA)
                        .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                intent.putExtra("mimeType", intent.getType());
                startActivity(Intent.createChooser(
                        intent, getString(R.string.set_as)));
                return true;
            case R.id.action_details:

                return true;
        }
        return false;
    }

    private Intent getIntentBySingleSelectedPath(String action) {
        MediaBean currentItem = getCurrentItem();
        return new Intent(action).setDataAndType(currentItem.getContentUri(), currentItem.mimeType);
    }
}
