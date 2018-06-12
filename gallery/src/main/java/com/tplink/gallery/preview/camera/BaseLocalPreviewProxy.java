package com.tplink.gallery.preview.camera;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.tplink.gallery.R;
import com.tplink.gallery.bean.MediaBean;
import com.tplink.gallery.preview.MediaOperationContract;
import com.tplink.gallery.preview.MediaOperationContract.MediaOperationView;
import com.tplink.gallery.preview.MediaOperationPresenter;
import com.tplink.gallery.preview.PreviewContract;
import com.tplink.gallery.preview.PreviewProxy;
import com.tplink.gallery.refocus.RefocusEditActivity;
import com.tplink.gallery.utils.MediaUtils;
import com.tplink.gallery.utils.StorageUtils;

import java.io.File;
import java.util.ArrayList;

public abstract class BaseLocalPreviewProxy extends PreviewProxy
        implements MediaOperationView {

    private static final int REQUEST_CROP = 1;
    private static final int REQUEST_EDIT = 2;
    private static final int REQUEST_PLAY_VIDEO = 3;
    private static final int REQUEST_REFOCUS = 4;
    private static final int REQUEST_FILTER = 5;
    private static final String TAG = "LocalImagePreview";

    private BottomMenuManager bottomMenuManager;

    protected Toast mToast;

    private MediaOperationContract.MediaOperationPresenter mediaOperationPresenter;


    public BaseLocalPreviewProxy(Activity mActivity, Intent intent, PreviewContract.PreviewView previewView, PreviewProxyHost previewProxyHost) {
        super(mActivity, intent, previewView, previewProxyHost);
    }

    @Override
    public void initView() {
        bottomMenuManager = new BottomMenuManager();
        mediaOperationPresenter = new MediaOperationPresenter(mActivity, this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        mActivity.getMenuInflater().inflate(R.menu.photo, menu);
        return true;
    }

    @Override
    public void onStartCapture(Object command) {
        if (command != null && command.equals(R.id.photopage_bottom_control_share)) {
            ArrayList<Uri> shareFilesUriList = new ArrayList<>();
            Intent intent = new Intent(Intent.ACTION_SEND_MULTIPLE)
                    .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    .setType(bottomMenuManager.item.mimeType);  // 打开其他文件需要使用content uri
            shareFilesUriList.add(bottomMenuManager.item.getContentUri());
            intent.putParcelableArrayListExtra(Intent.EXTRA_STREAM,
                    shareFilesUriList);
            mActivity.startActivity(intent);
            overrideTransitionToEditor();
        }
    }

    @Override
    public void onPhotoChanged(int index, MediaBean item) {
        bottomMenuManager.updateBottomMenu(item);
    }

    @Override
    public int getLayoutId() {
        return R.layout.activity_local_media_preview;
    }

    @Override
    public boolean isActive() {
        return previewProxyHost.isActive();
    }

    private class BottomMenuManager implements View.OnClickListener {
        private View mContainerView;
        private ImageButton btnEdit;
        private ImageButton btnDel;
        private TextView tvBurst;

        private MediaBean item;
        public BottomMenuManager() {
            mContainerView = mActivity.findViewById(R.id.photopage_bottom_controls);
            mActivity.findViewById(R.id.photopage_bottom_control_share).setOnClickListener(this);
            btnEdit = mActivity.findViewById(R.id.photopage_bottom_control_edit);
            btnEdit.setOnClickListener(this);
            btnDel = mActivity.findViewById(R.id.photopage_bottom_control_delete);
            btnDel.setOnClickListener(this);
            tvBurst = mActivity.findViewById(R.id.photopage_bottom_control_burst_select);
            tvBurst.setOnClickListener(this);
        }

        public void setVisible(boolean visible) {
            this.mContainerView.setVisibility(visible ? View.VISIBLE : View.GONE);
        }

        public void updateBottomMenu(MediaBean mediaBean) {
            this.item = mediaBean;
            tvBurst.setVisibility(mediaBean.isBurst ? View.VISIBLE : View.GONE);
            if (!previewProxyHost.isFileMode()
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
                    previewProxyHost.popupShareMenu(item);
                    break;
                case R.id.photopage_bottom_control_edit :
                    launchPhotoEditor(item);
                    break;
                case R.id.photopage_bottom_control_delete :
                    mediaOperationPresenter.delPhoto(item);
                    break;
                case R.id.photopage_bottom_control_burst_select :
                    SelectBurstActivity.launchSelectBurst(
                            mActivity,
                            previewProxyHost.getCurrentMedia(), previewProxyHost.isSecureActivity(), 100);

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
            mActivity.startActivityForResult(intent, REQUEST_EDIT);
            overrideTransitionToEditor();
        }

    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        if (item.getItemId() == R.id.action_refocus) {
            launchRefocusActivity();
            return true;
        }
        return false;
    }

    private void overrideTransitionToEditor() {
        mActivity.overridePendingTransition(android.R.anim.fade_in,
                android.R.anim.fade_out);
    }


    /**
     * Launch RefocusActivity.
     */
    public void launchRefocusActivity() {
        MediaBean item = previewProxyHost.getCurrentMedia();
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
            mToast = Toast.makeText(mActivity, R.string.msgs_no_disk_space, Toast.LENGTH_SHORT);
            mToast.show();
            return;
        }

        if (item.refocusType == MediaBean.FLAG_TPLINK_DEPTH_IMAGE) {
            // 虹软算法编辑
            RefocusEditActivity.lunchRefocusEdit(mActivity, item,false);
        } else if (item.refocusType == MediaBean.FLAG_DEPTH_IMAGE){
            // MTK算法编辑
            Intent intent = new Intent("com.mediatek.refocus.action.REFOCUS");
            intent.setDataAndType(item.getContentUri(), "image/*");
            Log.d(TAG, "<startRefocusActivity> intent: " + intent);
            mActivity.startActivityForResult(intent, REQUEST_REFOCUS);
        }
        // 过度动画
        mActivity. overridePendingTransition(0, R.anim.gallery_to_refocus_exit_anim);
    }

    @Override
    public void onDetailViewVisibleChanged(boolean visible) {
        super.onDetailViewVisibleChanged(visible);
        bottomMenuManager.setVisible(!visible);
    }
}
