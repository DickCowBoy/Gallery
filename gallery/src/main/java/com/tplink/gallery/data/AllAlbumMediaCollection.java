package com.tplink.gallery.data;

import com.tplink.gallery.bean.AlbumBean;
import com.tplink.gallery.data.MediaBeanCollection;
import com.tplink.gallery.utils.MediaUtils;

import java.util.List;

public class AllAlbumMediaCollection extends MediaBeanCollection<AlbumBean>{


    List<String> notAllowMimeTypes;
    List<String> allowMimeTypes;
    public boolean needResolveBurst;
    public boolean needImage;
    public boolean needVideo;

    public AllAlbumMediaCollection(List<AlbumBean> data, List<String> allowMimeTypes,
                                   List<String> notAllowMimeTypes, boolean needResolveBurst, boolean needImage, boolean needVideo) {
        super(data);
        this.allowMimeTypes = allowMimeTypes;
        this.notAllowMimeTypes = notAllowMimeTypes;
        this.needImage = needImage;
        this.needVideo = needVideo;
        this.needResolveBurst = needResolveBurst;
    }

    @Override
    public String key() {
        return MediaUtils.getAllAlbumKey(allowMimeTypes, notAllowMimeTypes, needResolveBurst, needImage, needVideo);
    }
}
