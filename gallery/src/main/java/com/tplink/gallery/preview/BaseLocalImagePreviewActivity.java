package com.tplink.gallery.preview;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.widget.ImageButton;
import android.widget.TextView;

import com.tplink.gallery.R;
import com.tplink.gallery.bean.MediaBean;

public abstract class BaseLocalImagePreviewActivity<T extends PreviewContract.PreviewPresenter> extends BasePreviewActivity<T> {

    private BottomMenuManager bottomMenuManager;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        bottomMenuManager = new BottomMenuManager();
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_local_media_preview;
    }

    @Override
    protected boolean canFilmMode() {
        return true;
    }

    @Override
    public void onPhotoChanged(int index, MediaBean item) {
        bottomMenuManager.updateBottomMenu(item);
    }

    private class BottomMenuManager {
        private ImageButton btnShare;
        private ImageButton btnEdit;
        private ImageButton btnDel;
        private TextView tvBurst;

        private MediaBean item;
        public BottomMenuManager() {
            btnShare = findViewById(R.id.photopage_bottom_control_share);
            btnEdit = findViewById(R.id.photopage_bottom_control_edit);
            btnDel = findViewById(R.id.photopage_bottom_control_delete);
            tvBurst = findViewById(R.id.photopage_bottom_control_burst_select);
        }

        public void updateBottomMenu(MediaBean mediaBean) {
            this.item = mediaBean;
        }
    }
}
