package com.tplink.gallery.data;

import com.tplink.gallery.bean.MediaBean;
import com.tplink.gallery.data.MediaBeanCollection;
import com.tplink.gallery.utils.MediaUtils;

import java.util.List;


public class AlbumDetailCollection extends MediaBeanCollection<MediaBean> {

    private long bucketId;
    private List<String> allowMimeTypes;
    private List<String> notAllowMimeTypes;
    private boolean needImage;
    private boolean needVideo;

    public AlbumDetailCollection(long bucketId,
                                 List<MediaBean> mediaBeans,
                                 List<String> allowMimeTypes,
                                 List<String> notAllowMimeTypes,
                                 boolean needImage,
                                 boolean needVideo) {
        super(mediaBeans);
        this.bucketId = bucketId;
        this.allowMimeTypes = allowMimeTypes;
        this.notAllowMimeTypes = notAllowMimeTypes;
        this.needImage = needImage;
        this.needVideo = needVideo;
    }

    @Override
    public String key() {
        return MediaUtils.getBucketId(bucketId, allowMimeTypes, notAllowMimeTypes, needImage, needVideo);
    }
}
