package com.tplink.gallery.selector;

import com.tplink.gallery.bean.MediaBean;

import java.util.List;

public interface AlbumChangedListener {
    void onChanged(int bucketId, boolean selected);
    void onChanged(MediaBean entity, boolean select);
    void showMediaBeans(List<MediaBean> entities);
    void showSelected();
}
