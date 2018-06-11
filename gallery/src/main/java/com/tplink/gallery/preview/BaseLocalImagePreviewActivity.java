package com.tplink.gallery.preview;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.StatFs;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.android.gallery3d.util.Const;
import com.tplink.base.Consts;
import com.tplink.gallery.GalleryApplication;
import com.tplink.gallery.R;
import com.tplink.gallery.bean.MediaBean;
import com.tplink.gallery.preview.camera.SelectBurstActivity;
import com.tplink.gallery.refocus.RefocusEditActivity;
import com.tplink.gallery.utils.MediaUtils;
import com.tplink.gallery.utils.StorageUtils;

import java.io.File;
import java.util.ArrayList;

public abstract class BaseLocalImagePreviewActivity<T extends PreviewContract.PreviewPresenter>
        extends BasePreviewActivity<T>
        implements MediaOperationContract.MediaOperationView{

    private static final int REQUEST_CROP = 1;
    private static final int REQUEST_EDIT = 2;
    private static final int REQUEST_PLAY_VIDEO = 3;
    private static final int REQUEST_REFOCUS = 4;
    private static final int REQUEST_FILTER = 5;

    private static final String TAG = "LocalImagePreview";
    private BottomMenuManager bottomMenuManager;

    protected Toast mToast;

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
        // refresh the menu
        invalidateOptionsMenu();
    }

    private class BottomMenuManager implements View.OnClickListener {
        private View mContainerView;
        private ImageButton btnEdit;
        private ImageButton btnDel;
        private TextView tvBurst;

        private MediaBean item;
        public BottomMenuManager() {
            mContainerView = findViewById(R.id.photopage_bottom_controls);
            findViewById(R.id.photopage_bottom_control_share).setOnClickListener(this);
            btnEdit = findViewById(R.id.photopage_bottom_control_edit);
            btnEdit.setOnClickListener(this);
            btnDel = findViewById(R.id.photopage_bottom_control_delete);
            btnDel.setOnClickListener(this);
            tvBurst = findViewById(R.id.photopage_bottom_control_burst_select);
            tvBurst.setOnClickListener(this);
        }

        public void setVisible(boolean visible) {
            this.mContainerView.setVisibility(visible ? View.VISIBLE : View.GONE);
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
                    SelectBurstActivity.launchSelectBurst(
                            BaseLocalImagePreviewActivity.this,
                            getCurrentItem(), isSecureActivity(), 100);

            }
        }

    }

    protected boolean isSecureActivity() {
        return false;
    }

    protected MediaBean getCurrentItem() {
        return bottomMenuManager.item;
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

    protected void showMediaDetails() {
        super.showMediaDetails();
        updateBottomMenu();
    }

    @Override
    protected void hideMediaDetails() {
        super.hideMediaDetails();
        updateBottomMenu();
    }

    private void updateBottomMenu() {
        bottomMenuManager.setVisible(!mMediaDetailsView.isShowing());
    }

    private Intent getIntentBySingleSelectedPath(String action) {
        MediaBean currentItem = getCurrentItem();
        return new Intent(action).setDataAndType(currentItem.getContentUri(), currentItem.mimeType);
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
                showMediaDetails();
                return true;
            case R.id.action_refocus:
                launchRefocusActivity();
                break;
        }
        return false;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.photo, menu);
        MediaBean currentItem = getCurrentItem();
        menu.findItem(R.id.action_setas).setVisible(currentItem != null
                && currentItem.isImage() && !currentItem.isGif());
        menu.findItem(R.id.action_refocus).setVisible(
                currentItem != null
                        && GalleryApplication.getApp().isSupportRefocus() != Const.NONE_REFOCUS
                        && currentItem.refocusType != 0);
        return true;
    }


    /**
     * Launch RefocusActivity.
     */
    public void launchRefocusActivity() {
        MediaBean item = getCurrentItem();
        Log.i(TAG, "<launchRefocusActivity> item:" + item);
        if (item == null) {
            return;
        }

        File srcFile = new File(item.filePath);
        if (!srcFile.exists()) {
            Log.i(TAG, "<onItemSelected> abort editing photo when not exists!");
            return;
        }
        // 添加存储判断
        if (!StorageUtils.isSpaceEnough(srcFile)) {
            Log.i(TAG, "<onItemSelected> abort editing photo when no enough space!");
            if (mToast != null) {
                mToast.cancel();
            }
            mToast = Toast.makeText(this, R.string.msgs_no_disk_space, Toast.LENGTH_SHORT);
            mToast.show();
            return;
        }

        if (item.refocusType == MediaBean.FLAG_TPLINK_DEPTH_IMAGE) {
            // 虹软算法编辑
            RefocusEditActivity.lunchRefocusEdit(this, item,false);
        } else if (item.refocusType == MediaBean.FLAG_DEPTH_IMAGE){
            // MTK算法编辑
            Intent intent = new Intent("com.mediatek.refocus.action.REFOCUS");
            intent.setDataAndType(item.getContentUri(), "image/*");
            Log.d(TAG, "<startRefocusActivity> intent: " + intent);
            startActivityForResult(intent, REQUEST_REFOCUS);
        }
        // 过度动画
        overridePendingTransition(0, R.anim.gallery_to_refocus_exit_anim);
    }

}
