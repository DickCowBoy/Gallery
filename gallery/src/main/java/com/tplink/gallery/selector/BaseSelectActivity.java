package com.tplink.gallery.selector;

import android.os.Bundle;
import android.support.annotation.Nullable;

import com.tplink.gallery.base.BaseGalleryActivity;
import com.tplink.gallery.bean.AlbumBean;
import com.tplink.gallery.bean.MediaBean;

import java.util.List;

public abstract class BaseSelectActivity extends BaseGalleryActivity implements MediaSelectorContract.MediaSelectorView{

    private MediaSelectorContract.MediaSelectorPresenter mediaSelectorPresenter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        this.mediaSelectorPresenter = initSelectorPresenter();
        super.onCreate(savedInstanceState);
    }

    @Override
    protected boolean awaysInSelectMode() {
        return this.mediaSelectorPresenter.getMaxSelectCount() > 1;
    }

    @Override
    protected boolean needResolveBurst() {
        return false;
    }

    @Override
    public void showAlbums(List<AlbumBean> beans, long version) {
        super.showAlbums(beans, version);
        this.mediaSelectorPresenter.initDataByAlbums(beans, version);
    }

    @Override
    public void showMedias(List<MediaBean> beans, long version) {
        super.showMedias(beans, version);
        this.mediaSelectorPresenter.initDataByAllMedia(beans, version);
    }

    protected abstract MediaSelectorContract.MediaSelectorPresenter initSelectorPresenter();
}
