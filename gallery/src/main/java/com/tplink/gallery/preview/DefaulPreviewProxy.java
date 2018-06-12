package com.tplink.gallery.preview;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;

import com.tplink.gallery.R;
import com.tplink.gallery.bean.MediaBean;
import com.tplink.gallery.bean.UriMediaBean;
import com.tplink.utils.NoneBoundArrayList;

import java.util.ArrayList;

public class DefaulPreviewProxy extends UriPreviewProxy {
    public DefaulPreviewProxy(Activity mActivity, Intent intent, PreviewContract.PreviewView previewView, PreviewProxyHost previewProxyHost) {
        super(mActivity, intent, previewView, previewProxyHost);
        supportEdit = false;
        supportSave = false;
        supportShare = false;
    }

    @Override
    public PreviewContract.PreviewPresenter initPreviewPresenter() {
        ArrayList<Uri> list = new ArrayList<>();
        list.add(intent.getData());
        intent.putParcelableArrayListExtra(IMAGE_TYPE_URLS, list);
        return super.initPreviewPresenter();
    }

    @Override
    public void initView() {

    }

    @Override
    public void onPhotoChanged(int index, MediaBean item) {

    }


}
