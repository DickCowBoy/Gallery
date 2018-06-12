package com.tplink.gallery.preview;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.FocusFinder;
import android.view.View;

import com.tplink.gallery.R;
import com.tplink.gallery.bean.MediaBean;
import com.tplink.gallery.preview.uri.UriPreviewPresenter;

public class UriPreviewProxy extends PreviewProxy implements View.OnClickListener {
    public static final String KEY_TP_SELECTED_POSITION = "tplink-selected-position";
    public static final String IMAGE_TYPE_URLS = "IMAGE_TYPE_URLS";

    private UriPreviewPresenter uriPreviewPresenter;

    protected boolean supportEdit = false;
    protected boolean supportSave = false;
    protected boolean supportShare = false;
    private String saveDir = null;

    public UriPreviewProxy(Activity mActivity, Intent intent, PreviewContract.PreviewView previewView, PreviewProxyHost previewProxyHost) {
        super(mActivity, intent, previewView, previewProxyHost);
        supportEdit = intent.getBooleanExtra(UriPreviewPresenter.KEY_TP_APP_SUPPORT_EDIT,false);
        supportSave = intent.getBooleanExtra(UriPreviewPresenter.KEY_TP_APP_SUPPORT_SAVE,false);
        supportShare = intent.getBooleanExtra(UriPreviewPresenter.KEY_TP_APP_SUPPORT_SHARE,false);
        saveDir = intent.getStringExtra(UriPreviewPresenter.KEY_TP_APP_SUPPORT_SAVE_DIR);
    }

    @Override
    public int getLayoutId() {
        return R.layout.activity_uri_media_preview;
    }

    @Override
    public PreviewContract.PreviewPresenter initPreviewPresenter() {
        Bundle data = new Bundle();
        data.putInt(KEY_TP_SELECTED_POSITION, intent.getIntExtra(KEY_TP_SELECTED_POSITION, 0));
        data.putParcelableArrayList(IMAGE_TYPE_URLS, intent.getParcelableArrayListExtra(IMAGE_TYPE_URLS));
        uriPreviewPresenter = new UriPreviewPresenter(mActivity, data, previewView);
        return uriPreviewPresenter;
    }

    @Override
    public void initView() {
        View view = mActivity.findViewById(R.id.photopage_bottom_control_share);
        view.setVisibility(supportShare ? View.VISIBLE : View.GONE);
        view.setOnClickListener(this);
        view = mActivity.findViewById(R.id.photopage_bottom_control_delete);
        view.setVisibility(supportEdit ? View.VISIBLE : View.GONE);
        view.setOnClickListener(this);
        view = mActivity.findViewById(R.id.photopage_bottom_control_save);
        view.setVisibility(supportSave ? View.VISIBLE : View.GONE);
        view.setOnClickListener(this);
    }

    @Override
    public void onPhotoChanged(int index, MediaBean item) {
        String title = mActivity.getString(R.string.total_selected_count,
                index + 1, uriPreviewPresenter.getCount());
        previewProxyHost.getNormalBar().setTitle(title);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.photopage_bottom_control_delete:

                return;
            case R.id.photopage_bottom_control_share:

                return;
            case R.id.photopage_bottom_control_save:

                return;

        }
    }
}
