package com.tplink.gallery.preview;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import com.android.gallery3d.ui.GLRoot;
import com.android.gallery3d.util.GalleryUtils;
import com.tplink.gallery.R;
import com.tplink.gallery.bean.MediaBean;
import com.tplink.gallery.utils.MediaUtils;

import java.util.ArrayList;

public abstract class BaseLocalImagePreviewActivity<T extends PreviewContract.PreviewPresenter>
        extends BasePreviewActivity<T> implements MediaOperationContract.MediaOperationView{

    private static final int REQUEST_CROP = 1;
    private static final int REQUEST_EDIT = 2;
    private static final int REQUEST_PLAY_VIDEO = 3;
    private static final int REQUEST_REFOCUS = 4;
    private static final int REQUEST_FILTER = 5;

    private static final String TAG = "LocalImagePreview";
    private BottomMenuManager bottomMenuManager;

    private MediaOperationContract.MediaOperationPresenter mediaOperationPresenter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        bottomMenuManager = new BottomMenuManager();
        mediaOperationPresenter = new MediaOperationPresenter(this, this);
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

    private class BottomMenuManager implements View.OnClickListener {
        private ImageButton btnEdit;
        private ImageButton btnDel;
        private TextView tvBurst;

        private MediaBean item;
        public BottomMenuManager() {
            findViewById(R.id.photopage_bottom_control_share).setOnClickListener(this);
            btnEdit = findViewById(R.id.photopage_bottom_control_edit);
            btnEdit.setOnClickListener(this);
            btnDel = findViewById(R.id.photopage_bottom_control_delete);
            btnDel.setOnClickListener(this);
            tvBurst = findViewById(R.id.photopage_bottom_control_burst_select);
            tvBurst.setOnClickListener(this);
        }

        public void updateBottomMenu(MediaBean mediaBean) {
            this.item = mediaBean;
            tvBurst.setVisibility(mediaBean.isBurst ? View.VISIBLE : View.GONE);
            if (!isFileMode()
                    && MediaUtils.isEditSupported(mediaBean.mimeType)
                    && mediaBean.isImage()) {
                btnEdit.setVisibility(View.VISIBLE);
            } else {
                btnEdit.setVisibility(View.GONE);
            }
        }

        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.photopage_bottom_control_share :
                    popupShareMenu(item);
                    break;
                case R.id.photopage_bottom_control_edit :
                    launchPhotoEditor(item);
                    break;
                case R.id.photopage_bottom_control_delete :
                    mediaOperationPresenter.delPhoto(item);
                    break;
                case R.id.photopage_bottom_control_burst_select :

                    break;
            }
        }
    }

    protected void launchPhotoEditor(MediaBean mediaBean) {
        if (mediaBean == null || !MediaUtils.isEditSupported(mediaBean.mimeType)) {
            Log.i(TAG, "<onItemSelected> abort editing photo, current MediaItem is null or not support edit!");
            return;
        }

        // TODO check file and free space ljl
        Intent intent = new Intent();
        intent.setDataAndType(mediaBean.getContentUri(), mediaBean.mimeType);
        intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        // 启动应用外部的图片编辑器
        intent.setAction(Intent.ACTION_EDIT);
        startActivityForResult(intent, REQUEST_EDIT);
        overrideTransitionToEditor();
    }

    private void overrideTransitionToEditor() {
        overridePendingTransition(android.R.anim.fade_in,
                android.R.anim.fade_out);
    }

    protected void popupShareMenu(MediaBean item) {
        if (item == null) return;
        enableCapture(R.id.photopage_bottom_control_share);
    }

    protected void enableCapture(Object command) {
        bigImagePreviewGLView.enableCapture(command);
    }

    @Override
    public void onStartCapture(Object command) {
        // do the action
        if (command != null && command.equals(R.id.photopage_bottom_control_share)) {
            ArrayList<Uri> shareFilesUriList = new ArrayList<>();
            Intent intent = new Intent(Intent.ACTION_SEND_MULTIPLE)
                    .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    .setType(bottomMenuManager.item.mimeType);  // 打开其他文件需要使用content uri
            shareFilesUriList.add(bottomMenuManager.item.getContentUri());
            intent.putParcelableArrayListExtra(Intent.EXTRA_STREAM,
                    shareFilesUriList);
            startActivity(intent);
            overrideTransitionToEditor();
        }
    }
}
