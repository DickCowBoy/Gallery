package com.tplink.gallery.preview.wallpaper;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;

import com.tplink.gallery.R;
import com.tplink.gallery.bean.MediaBean;
import com.tplink.gallery.preview.BasePreviewActivity;
import com.tplink.gallery.preview.PreviewContract;

import java.util.List;

public class WallPaperPreviewActivity extends BasePreviewActivity<WallPaperPreviewPresenter>
        implements View.OnClickListener, DialogInterface.OnClickListener {
    @Override
    protected int getLayoutId() {
        return R.layout.activity_wallpaper_preview;
    }

    @Override
    protected WallPaperPreviewPresenter initPreviewPresenter(Bundle data) {
        return new WallPaperPreviewPresenter(data, this, new WallPaperView());
    }

    class WallPaperView extends ViewProxy implements WallpaperPreviewView {

        @Override
        public void showSetResultFinished() {
            finish();
        }

        @Override
        public void showSetResultStart() {
            // TODO ljl need to show progress dialog
        }
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        findViewById(R.id.photopage_bottom_control_delete).setOnClickListener(this);
    }

    @Override
    protected boolean canFilmMode() {
        return false;
    }

    @Override
    public void onPhotoChanged(int index, MediaBean item) {
        previewPresenter.showTitle(index);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.photopage_bottom_control_delete:
                AlertDialog.Builder ad = new AlertDialog.Builder(WallPaperPreviewActivity.this);
                //ad.setTPMode(true);
                ad.setMessage(R.string.sure_remove)
                        .setPositiveButton(R.string.remove_picture, WallPaperPreviewActivity.this)
                        .setNegativeButton(R.string.cancel, null).create();
                ad.show();
                break;
        }
    }

    @Override
    public void onClick(DialogInterface dialogInterface, int i) {
        MediaBean mediaItem = bigImagePreviewGLView.getCurrentBean();
        previewPresenter.removeSingleMedia(mediaItem, bigImagePreviewGLView.getCurrentIndex());
    }

    @Override
    public void onBackPressed() {
        previewPresenter.setResult(this);
    }
}
