package com.tplink.gallery.preview.wallpaper;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.tplink.gallery.R;
import com.tplink.gallery.bean.MediaBean;
import com.tplink.gallery.preview.PreviewContract;
import com.tplink.gallery.preview.PreviewProxy;

public class WallPaperPreviewProxy extends PreviewProxy
        implements WallPaperPreviewOperationContract.PreviewOpeView
        , View.OnClickListener
        , DialogInterface.OnClickListener{

    private WallPaperPreviewPresenter previewPresenter;

    public WallPaperPreviewProxy(Activity mActivity, Intent intent, PreviewContract.PreviewView previewView,
                                 PreviewProxyHost previewProxyHost) {
        super(mActivity, intent, previewView, previewProxyHost);
    }


    @Override
    public PreviewContract.PreviewPresenter initPreviewPresenter() {
        Bundle data = new Bundle();
        data.putParcelable(PreviewContract.PreviewPresenter.CURRENT_MEDIA, intent.getData());
        previewPresenter = new WallPaperPreviewPresenter(data, mActivity, previewView);
        previewPresenter.setOpeView(this);
        return previewPresenter;
    }

    @Override
    public int getLayoutId() {
        return R.layout.activity_wallpaper_preview;
    }

    @Override
    public boolean canFilmMode() {
        return false;
    }

    @Override
    public boolean onBackPressed() {
        previewPresenter.setResult(mActivity);
        return true;
    }

    @Override
    public void showSetResultFinished() {
        mActivity.finish();
    }

    @Override
    public void showSetResultStart() {

    }

    @Override
    public boolean isActive() {
        return previewProxyHost.isActive();
    }

    @Override
    public void initView() {
        mActivity.findViewById(R.id.photopage_bottom_control_delete).setOnClickListener(this);
    }

    @Override
    public void onPhotoChanged(int index, MediaBean item) {
        previewPresenter.showTitle(index);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.photopage_bottom_control_delete:
                AlertDialog.Builder ad = new AlertDialog.Builder(mActivity);
                //ad.setTPMode(true);
                ad.setMessage(R.string.sure_remove)
                        .setPositiveButton(R.string.remove_picture, this)
                        .setNegativeButton(R.string.cancel, null).create();
                ad.show();
                break;
        }
    }

    @Override
    public void onClick(DialogInterface dialogInterface, int i) {
        MediaBean mediaItem = previewProxyHost.getCurrentMedia();
        previewPresenter.removeSingleMedia(mediaItem, previewProxyHost.getCurrentMediaPosition());
    }
}
