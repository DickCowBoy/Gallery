package com.tplink.gallery.data;

import com.tplink.gallery.bean.MediaBean;
import com.tplink.gallery.data.MediaBeanCollection;
import com.tplink.gallery.utils.MediaUtils;

import java.util.List;


public class AlbumDetailCollection extends MediaBeanCollection<MediaBean> {

    private boolean needImage;
    private boolean needVideo;
    private boolean needGif;
    private long bucketId;

    public AlbumDetailCollection(long bucketId, List<MediaBean> mediaBeans, boolean needImage, boolean needVideo, boolean needGif) {
        super(mediaBeans);
        this.needImage = needImage;
        this.needVideo = needVideo;
        this.needGif = needGif;
        this.bucketId = bucketId;
    }

    @Override
    public String key() {
        return MediaUtils.getBucketId(bucketId, needVideo, needImage, needGif);
    }
}
