package com.tplink.gallery.data;

import com.tplink.gallery.bean.AlbumBean;
import com.tplink.gallery.data.MediaBeanCollection;
import com.tplink.gallery.utils.MediaUtils;

import java.util.List;

public class AllAlbumMediaCollection extends MediaBeanCollection<AlbumBean>{

    public boolean needVideo;
    public boolean needImage;
    public boolean needGif;
    public boolean needResolveBurst;

    public AllAlbumMediaCollection(List<AlbumBean> data, boolean needVideo, boolean needImage, boolean needGif, boolean needResolveBurst) {
        super(data);
        this.needVideo = needVideo;
        this.needImage = needImage;
        this.needGif = needGif;
        this.needResolveBurst = needResolveBurst;
    }

    @Override
    public String key() {
        return MediaUtils.getAllAlbumKey(needVideo, needImage, needGif, needResolveBurst);
    }
}
