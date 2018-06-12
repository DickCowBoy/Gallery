package com.tplink.gallery.preview;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.RelativeLayout;

import com.tplink.gallery.R;
import com.tplink.gallery.bean.MediaBean;
import com.tplink.gallery.ui.BigImagePreviewGLView;
import com.tplink.gallery.ui.MediaDetailsView;

import java.util.List;

public class PreviewActivity extends AppCompatActivity
        implements BigImagePreviewGLView.DataListener, PreviewContract.PreviewView,
        BigImagePreviewGLView.BigPreviewDelete, Toolbar.OnMenuItemClickListener,
        MediaDetailsView.MediaDetailViewListener, PreviewProxy.PreviewProxyHost {

    public static final String EXTRA_TP_SECURE_CAMERA = "EXTRA_TP_SECURE_CAMERA";

    public static final int IMAGE_TYPE_CAMERA = 0;// 只查看相机图片
    public static final int IMAGE_TYPE_LOCAL_ALL = 1;// 查看本地所有图片
    public static final int IMAGE_TYPE_LOCAL_ALBUM = 2;// 查看本地相册
    public static final int IMAGE_TYPE_LOCAL_CERTAIN = 3;// 指定查看文件
    public static final int IMAGE_TYPE_LOCAL_SINGLE = 4;// 指定查看文件

    public static final String IMAGE_TYPE = "IMAGE_TYPE";
    public static final String IMAGE_TYPE_URLS = "IMAGE_TYPE_URLS";
    public static final String IS_CAMERA = "isCamera";
    public static final String KEY_SHOW_WHEN_LOCKED = "show_when_locked";

    private static final String TAG = "PreviewActivity";
    protected BigImagePreviewGLView bigImagePreviewGLView;
    protected  PreviewContract.PreviewPresenter previewPresenter;
    private long mediaVersion = Integer.MIN_VALUE;
    private boolean isActive = false;
    private Toolbar mNormalToolbar;
    protected MediaDetailsView mMediaDetailsView;
    private PreviewProxy mProxy;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mProxy = ProxyFactory.getProxy(this, getIntent(), this, this);
        if (mProxy == null) {
            finish();
            return;
        }
        setContentView(mProxy.getLayoutId());
        bigImagePreviewGLView = new BigImagePreviewGLView(
                findViewById(R.id.gl_root_view), this,
                this, mProxy.canFilmMode());
        previewPresenter = mProxy.initPreviewPresenter();
        mProxy.initView();
        bigImagePreviewGLView.onCreate();
        previewPresenter.loadPreviewData();
        setPreviewWindow();
        mNormalToolbar = findViewById(R.id.toolbar);
        setSupportActionBar(mNormalToolbar);

        setSupportActionBar(mNormalToolbar);
        mNormalToolbar.setNavigationIcon(R.drawable.ic_arrow_back);
        mNormalToolbar.setNavigationOnClickListener((v) -> {
            onBackPressed();
        });
        mNormalToolbar.setOnMenuItemClickListener(this);
        mNormalToolbar.setTitle("");
        bigImagePreviewGLView.setDataListener(this);
    }

    @Override
    public void showMediaData(List<MediaBean> mediaBeans, int index, long version) {

        if (mediaBeans == null || mediaBeans.size() == 0) {
            onBackPressed();
            return;
        }
        if (version <= mediaVersion) {
            return;
        }
        mediaVersion = version;
        bigImagePreviewGLView.setData(mediaBeans);
        bigImagePreviewGLView.showIndex(index);
    }

    @Override
    protected void onResume() {
        super.onResume();
        isActive = true;
        previewPresenter.resume();
        bigImagePreviewGLView.onResume();
    }

    @Override
    protected void onStart() {
        super.onStart();
        bigImagePreviewGLView.onStart();
    }

    @Override
    protected void onPause() {
        super.onPause();
        isActive = false;
        previewPresenter.pause();
        bigImagePreviewGLView.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        bigImagePreviewGLView.onDestroy();
    }

    @Override
    public Toolbar getNormalBar() {
        return mNormalToolbar;
    }

    @Override
    public boolean isActive() {
        return isActive;
    }

    @Override
    public MediaBean getCurrentMedia() {
        return bigImagePreviewGLView.getCurrentBean();
    }

    @Override
    public int getCurrentMediaPosition() {
        return bigImagePreviewGLView.getCurrentIndex();
    }

    protected void setPreviewWindow() {
        Window window = getWindow();
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
        window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
    }

    @Override
    public void showHeader(String title) {
        mNormalToolbar.setTitle(title);
    }

    @Override
    public void onStartCapture(Object command) {
        mProxy.onStartCapture(command);
    }

    private void enableCapture(Object command) {
        bigImagePreviewGLView.enableCapture(command);
    }

    @Override
    public void popupShareMenu(Object bean) {
        enableCapture(bean);
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
        }
        return mProxy.onMenuItemClick(item);
    }

    private Intent getIntentBySingleSelectedPath(String action) {
        MediaBean currentItem = getCurrentMedia();
        return new Intent(action).setDataAndType(currentItem.getContentUri(), currentItem.mimeType);
    }

    @Override
    public void onPhotoChanged(int index, MediaBean item) {
        invalidateOptionsMenu();
        mProxy.onPhotoChanged(index, item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
       return mProxy.onCreateOptionsMenu(menu);
    }

    public boolean isFileMode() {
        return bigImagePreviewGLView.isInFilmMode();
    }

    @Override
    public boolean isSecureActivity() {
        return getIntent().getBooleanExtra(EXTRA_TP_SECURE_CAMERA, false);
    }

    @Override
    public void onContentOutsideClicked() {
        hideMediaDetails();
    }

    @Override
    public void onBackPressed() {
        if (mMediaDetailsView != null && mMediaDetailsView.isShowing()) {
            hideMediaDetails();
        } else {
            if (mProxy.onBackPressed()) {
                return;
            }
            super.onBackPressed();
        }
    }


    protected void showMediaDetails() {
        MediaBean item = bigImagePreviewGLView.getCurrentBean();
        if (mMediaDetailsView == null) {
            RelativeLayout galleryRoot = findViewById(R.id.rl_gallery_root);
            mMediaDetailsView = new MediaDetailsView(this, galleryRoot);
            mMediaDetailsView.setMediaDetailViewListener(this);
        }
        mMediaDetailsView.show(getApplicationContext(), item);
        mProxy.onDetailViewVisibleChanged(true);
    }


    protected void hideMediaDetails() {
        if (mMediaDetailsView == null || !mMediaDetailsView.isShowing()) {
            return;
        }
        mMediaDetailsView.hide();
        mProxy.onDetailViewVisibleChanged(false);
    }
}
