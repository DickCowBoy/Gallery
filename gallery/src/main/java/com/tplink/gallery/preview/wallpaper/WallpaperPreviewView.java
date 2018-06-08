package com.tplink.gallery.preview.wallpaper;

import com.tplink.gallery.preview.PreviewContract;

public interface WallpaperPreviewView extends PreviewContract.PreviewView {
    void showSetResultFinished();
    void showSetResultStart();
}
