package com.tplink.gallery.data;

import com.tplink.gallery.bean.MediaBean;

import java.util.List;

public class SingleMediaCollection extends MediaBeanCollection<MediaBean>{
    private String key;
    public SingleMediaCollection(List<MediaBean> mediaBeans, String key) {
        super(mediaBeans);
        this.key = key;
    }

    @Override
    public String key() {
        return key;
    }
}
