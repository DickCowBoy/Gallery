package com.tplink.gallery.preview;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import com.tplink.gallery.preview.camera.BaseLocalPreviewProxy;

public class LocalPreviewProxy extends BaseLocalPreviewProxy {
    private int bucketId;
    public LocalPreviewProxy(Activity mActivity,
                             Intent intent,
                             PreviewContract.PreviewView previewView,
                             PreviewProxyHost previewProxyHost) {
        super(mActivity, intent, previewView, previewProxyHost);
        try {
            bucketId = Integer.parseInt(intent.getStringExtra(LocalBucketPreviewPresenter.KEY_BUCKET_ID));
        } catch (NumberFormatException e) {
            bucketId = -1;
        }

    }

    @Override
    public PreviewContract.PreviewPresenter initPreviewPresenter() {
        Bundle data = new Bundle();
        // TODO LJL promote
        data.putParcelable(LocalAllPresenter.CLICK_URI, intent.getData());
        if (bucketId == -1) {
            return new LocalAllPresenter(mActivity, data, previewView);
        } else {
            data.putInt(LocalBucketPreviewPresenter.KEY_BUCKET_ID, bucketId);
            return new LocalBucketPreviewPresenter(mActivity, data, previewView);
        }
    }
}
