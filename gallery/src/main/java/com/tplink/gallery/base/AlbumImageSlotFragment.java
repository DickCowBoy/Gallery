/*
 * Copyright (C) 2018, TP-LINK TECHNOLOGIES CO., LTD.
 *
 * AlbumImageSlotFragment.java
 *
 * Description 显示相册内部图片信息
 *
 * Author LinJinLong
 *
 * Ver 1.0, 2018-- LinJinLong, Create file
 */
package com.tplink.gallery.base;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.tplink.gallery.bean.MediaBean;
import com.tplink.gallery.gallery.R;
import com.tplink.gallery.utils.MediaUtils;

import java.util.List;

public class AlbumImageSlotFragment extends ImageSlotFragment implements MediaContract.AlbumView{

    public static final String KEY_BUCKET_ID = "KEY_BUCKET_ID";
    public static final String KEY_NEED_IMAGE = "KEY_NEED_IMAGE";
    public static final String KEY_NEED_VIDEO = "KEY_NEED_VIDEO";
    public static final String KEY_NEED_GIF = "KEY_NEED_GIF";

    private MediaContract.AlbumDetailPresenter albumDetailPresenter;
    private long bucketId;
    private boolean needImage;
    private boolean needVideo;
    private boolean needGif;

    private boolean isActive;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle arguments = getArguments();
        if (arguments != null) {
            bucketId = arguments.getLong(KEY_BUCKET_ID);
            needImage = arguments.getBoolean(KEY_NEED_IMAGE);
            needVideo = arguments.getBoolean(KEY_NEED_VIDEO);
            needGif = arguments.getBoolean(KEY_NEED_GIF);
        }
        albumDetailPresenter = new AlbumDetailPresenter(getContext(), this);
        albumDetailPresenter.loadAlbumDetail(bucketId, needImage, needVideo, needGif);
        isActive = true;
    }

    @Override
    public boolean isActive() {
        return isActive;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        isActive = false;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);
        view.setBackgroundColor(getResources().getColor(R.color.white));
        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    protected void loadDataForView() {}

    @Override
    public void showMedias(List<MediaBean> beans) {
        showMediaBeans(beans);
    }

    public static AlbumImageSlotFragment newInstance(long bucketId, boolean needImage, boolean needVideo, boolean needGif) {

        Bundle args = new Bundle();

        args.putLong(KEY_BUCKET_ID, bucketId);
        args.putBoolean(KEY_NEED_GIF, needGif);
        args.putBoolean(KEY_NEED_IMAGE, needImage);
        args.putBoolean(KEY_NEED_VIDEO, needVideo);

        args.putString(KEY_DATA_KEY , MediaUtils.getBucketId(bucketId, needVideo, needImage, needGif));

        AlbumImageSlotFragment fragment = new AlbumImageSlotFragment();
        fragment.setArguments(args);
        return fragment;
    }

}
