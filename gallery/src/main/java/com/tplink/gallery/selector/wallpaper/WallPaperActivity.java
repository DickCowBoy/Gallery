package com.tplink.gallery.selector.wallpaper;

import com.tplink.gallery.selector.BaseSelectActivity;
import com.tplink.gallery.selector.MediaSelectorContract;

import java.util.ArrayList;

public class WallPaperActivity extends BaseSelectActivity {
    @Override
    protected MediaSelectorContract.MediaSelectorPresenter initSelectorPresenter() {
        return new WallPaperSelectPresenter(this, this);
    }

    @Override
    public ArrayList<String> getAllowMimeTypes() {
        return null;
    }

    // gif is not allow
    @Override
    public ArrayList<String> getNotAllowMimeTypes() {
        ArrayList<String> notAllowMimeTypes = new ArrayList<>();
        notAllowMimeTypes.add("image/gif");
        return notAllowMimeTypes;
    }

    @Override
    protected boolean needSelectAlbum() {
        return true;
    }

    @Override
    public boolean needVideo() {
        return false;
    }

    @Override
    public boolean needImage() {
        return true;
    }

    @Override
    public void showHint(String msg) {

    }
}
