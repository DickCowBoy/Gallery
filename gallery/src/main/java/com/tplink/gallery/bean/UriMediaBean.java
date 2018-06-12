package com.tplink.gallery.bean;

import android.content.Context;
import android.net.Uri;

import com.android.gallery3d.data.MediaDetails;
import com.android.gallery3d.util.GalleryUtils;

public class UriMediaBean extends MediaBean{

    private Uri mUri;

    public UriMediaBean(Uri mUri) {
        this.mUri = mUri;
        this._id = GalleryUtils.getBucketId(mUri.toString());
    }

    @Override
    public Uri getContentUri() {
        return mUri;
    }

    @Override
    public MediaDetails getDetails(Context context) {
        return new MediaDetails();
    }
}
