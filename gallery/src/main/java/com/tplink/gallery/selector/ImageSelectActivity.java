package com.tplink.gallery.selector;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;

import com.tplink.gallery.selector.wallpaper.ResultContainer;

import java.util.ArrayList;

public class ImageSelectActivity extends BaseSelectActivity {

    public static final String ALLOW_MIME_TYPES = "ALLOW_MIME_TYPES";
    public static final String NOT_ALLOW_MIME_TYPES = "NOT_ALLOW_MIME_TYPES";
    public static final String MAX_SIZE = "MAX_SIZE";
    public static final String MAX_COUNT = "MAX_COUNT";
    public static final String NEED_IMAGE = "NEED_IMAGE";
    public static final String NEED_VIDEO = "NEED_VIDEO";
    public static final String NEED_PREVIEW = "NEED_PREVIEW";

    private ArrayList<String> allowMimeTypes;
    private ArrayList<String> notAllowMimeTypes;
    private long maxSize;
    private int maxCount;
    private boolean needImage;
    private boolean needVideo;
    private boolean needPreview;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        parseIntent(getIntent());
        super.onCreate(savedInstanceState);
    }

    private void parseIntent(Intent intent) {
        if (intent != null) {
            allowMimeTypes = intent.getStringArrayListExtra(ALLOW_MIME_TYPES);
            notAllowMimeTypes = intent.getStringArrayListExtra(NOT_ALLOW_MIME_TYPES);
            maxSize = intent.getLongExtra(MAX_SIZE, ResultContainer.UNLIMIT);
            maxCount = intent.getIntExtra(MAX_COUNT, ResultContainer.UNLIMIT);
            needImage = intent.getBooleanExtra(NEED_IMAGE, true);
            needVideo = intent.getBooleanExtra(NEED_VIDEO, true);
            needPreview = intent.getBooleanExtra(NEED_PREVIEW, true);
        }
    }

    @Override
    protected MediaSelectorContract.MediaSelectorPresenter initSelectorPresenter() {
        return new ImageSelectPresenter(this, this,
                maxCount,
                maxSize,
                needPreview,
                needImage,
                needVideo);
    }

    @Override
    protected boolean needSureBottom() {
        return super.needSureBottom()
                || (mediaSelectorPresenter.getMaxSelectCount() == 1 && actionbarStyle == TOOLBAR_STYLE_PREVIEW);
    }

    @Override
    public ArrayList<String> getAllowMimeTypes() {
        return allowMimeTypes;
    }

    @Override
    public ArrayList<String> getNotAllowMimeTypes() {
        return notAllowMimeTypes;
    }

    @Override
    public boolean needVideo() {
        return needVideo;
    }

    @Override
    public boolean needImage() {
        return needImage;
    }
}
