/*
 * Copyright (C) 2016, TP-LINK TECHNOLOGIES CO., LTD.
 *
 * LabelKey.java
 *
 * Description
 *
 * Author huwei
 *
 * Ver 1.0, 2016-10-28, huwei, Create file
 */
package com.android.gallery3d.cache;



import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;

public class VideoLabelKey implements Key {
    private final long mDuration;
    private final int mSourceType;

    public VideoLabelKey(long mDuration, int mSourceType) {
        this.mDuration = mDuration;
        this.mSourceType = mSourceType;
    }

    @Override
    public void updateDiskCacheKey(MessageDigest messageDigest) throws UnsupportedEncodingException {

    }

    @Override
    public int hashCode() {
        return mSourceType;
    }

    @Override
    public boolean equals(Object o) {
        if(o instanceof VideoLabelKey){
            VideoLabelKey localKey = (VideoLabelKey) o;
            return mDuration == localKey.mDuration && mSourceType == localKey.mSourceType;
        }
        return false;
    }
}
