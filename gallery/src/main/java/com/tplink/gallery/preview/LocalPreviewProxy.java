package com.tplink.gallery.preview;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import com.tplink.gallery.preview.camera.BaseLocalPreviewProxy;

import java.util.ArrayList;

public class LocalPreviewProxy extends BaseLocalPreviewProxy {
    private int bucketId;
    private Uri data;
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
        data = intent.getData();

    }

    @Override
    public PreviewContract.PreviewPresenter initPreviewPresenter() {
        Bundle data = new Bundle();
        // TODO LJL promote
        data.putParcelable(LocalAllPresenter.CLICK_URI, intent.getData());
        int type = intent.getIntExtra(PreviewActivity.IMAGE_TYPE, PreviewActivity.IMAGE_TYPE_LOCAL_SINGLE);
        if (type == PreviewActivity.IMAGE_TYPE_LOCAL_SINGLE) {
            return new LocalSinglePresenter(mActivity, this.data, previewView);
        }
        if (bucketId == -1) {
            return new LocalAllPresenter(mActivity, data, previewView);
        } else {
            data.putInt(LocalBucketPreviewPresenter.KEY_BUCKET_ID, bucketId);
            return new LocalBucketPreviewPresenter(mActivity, data, previewView);
        }
    }
}
