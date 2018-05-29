package com.tplink.gallery.selector;

import com.tplink.gallery.bean.MediaBean;

import java.util.Collection;
import java.util.List;

public interface AlbumChangedListener {
    void onChanged(long bucketId, boolean selected, Collection<MediaBean> beans);
    void onChanged(MediaBean entity, boolean select);
    void showMediaBeans(List<MediaBean> entities);
    void showSelected();
    String getOpeSource();
}
