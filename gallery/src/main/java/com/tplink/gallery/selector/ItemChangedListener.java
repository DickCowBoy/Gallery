package com.tplink.gallery.selector;

import com.tplink.gallery.bean.AlbumBean;
import com.tplink.gallery.bean.MediaBean;

import java.util.List;

public interface ItemChangedListener {
    void showAlbumBeans(List<AlbumBean> entities);
    void onChanged(MediaBean entity);
}
