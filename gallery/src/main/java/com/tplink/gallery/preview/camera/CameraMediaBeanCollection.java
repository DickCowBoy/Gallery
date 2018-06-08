package com.tplink.gallery.preview.camera;

import com.tplink.gallery.bean.MediaBean;
import com.tplink.gallery.data.MediaBeanCollection;
import com.tplink.gallery.utils.MediaUtils;

import java.util.List;

public class CameraMediaBeanCollection extends MediaBeanCollection<MediaBean> {

    public CameraMediaBeanCollection(List<MediaBean> mediaBeans) {
        super(mediaBeans);
    }

    @Override
    public String key() {
        return MediaUtils.getAllCameraAlbumKey();
    }
}
