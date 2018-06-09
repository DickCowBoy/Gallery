package com.tplink.gallery.preview;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import com.tplink.gallery.R;
import com.tplink.gallery.bean.MediaBean;
import com.tplink.gallery.ui.BigImagePreviewGLView;

import java.util.List;

public abstract class BasePreviewActivity<T extends PreviewContract.PreviewPresenter>
        extends AppCompatActivity
        implements BigImagePreviewGLView.DataListener, PreviewContract.PreviewView, BigImagePreviewGLView.BigPreviewDelete {

    protected BigImagePreviewGLView bigImagePreviewGLView;
    protected T previewPresenter;
    private long mediaVersion = Integer.MIN_VALUE;
    private boolean isActive = false;
    private Toolbar mNormalToolbar;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(getLayoutId());
        bigImagePreviewGLView = new BigImagePreviewGLView(findViewById(R.id.gl_root_view), this, this, canFilmMode());
        previewPresenter = initPreviewPresenter();
        bigImagePreviewGLView.onCreate();
        Bundle data = new Bundle();
        data.putParcelable(PreviewContract.PreviewPresenter.CURRENT_MEDIA, getIntent().getData());
        previewPresenter.loadPreviewData(data);
        setPreviewWindow();
        mNormalToolbar = findViewById(R.id.toolbar);
        setSupportActionBar(mNormalToolbar);

        setSupportActionBar(mNormalToolbar);
        mNormalToolbar.setNavigationIcon(R.drawable.ic_arrow_back);
        mNormalToolbar.setNavigationOnClickListener((v) -> {
            onBackPressed();
        });
        bigImagePreviewGLView.setDataListener(this);
    }

    protected abstract int getLayoutId();

    protected abstract T initPreviewPresenter();

    protected abstract boolean canFilmMode();

    @Override
    public void showMediaData(List<MediaBean> mediaBeans, int index, long version) {
        if (mediaBeans == null || mediaBeans.size() == 0) {
            onBackPressed();
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
        bigImagePreviewGLView.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        bigImagePreviewGLView.onDestroy();
    }

    @Override
    public boolean isActive() {
        return isActive;
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

    }

    protected class ViewProxy implements PreviewContract.PreviewView {

        @Override
        public void showMediaData(List<MediaBean> mediaBeans, int index, long version) {
            BasePreviewActivity.this.showMediaData(mediaBeans, index, version);
        }

        @Override
        public void showHeader(String title) {
            BasePreviewActivity.this.showHeader(title);
        }

        @Override
        public boolean isActive() {
            return BasePreviewActivity.this.isActive();
        }
    }

    public boolean isFileMode() {
        return bigImagePreviewGLView.isInFilmMode();
    }
}
