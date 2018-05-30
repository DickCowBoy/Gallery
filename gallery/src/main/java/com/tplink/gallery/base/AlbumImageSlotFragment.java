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

import java.util.ArrayList;
import java.util.List;

public class AlbumImageSlotFragment extends ImageSlotFragment implements MediaContract.AlbumView{

    public static final String KEY_BUCKET_ID = "KEY_BUCKET_ID";
    public static final String KEY_ALLOW_MIME_TYPES = "KEY_ALLOW_MIME_TYPES";
    public static final String KEY_NOT_ALLOW_MIME_TYPES = "KEY_NOT_ALLOW_MIME_TYPES";

    private MediaContract.AlbumDetailPresenter albumDetailPresenter;
    private long bucketId;

    private boolean isActive;
    private boolean firstLoad = true;

    private List<String> allowMimeTypes;
    private List<String> notAllowMimeTypes;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle arguments = getArguments();
        if (arguments != null) {
            bucketId = arguments.getLong(KEY_BUCKET_ID);
            allowMimeTypes = arguments.getStringArrayList(KEY_ALLOW_MIME_TYPES);
            notAllowMimeTypes = arguments.getStringArrayList(KEY_NOT_ALLOW_MIME_TYPES);
        }
        albumDetailPresenter = new AlbumDetailPresenter(getContext(), this, bucketId, allowMimeTypes, notAllowMimeTypes);
        albumDetailPresenter.loadAlbumDetail();
    }

    @Override
    public void onResume() {
        super.onResume();
        albumDetailPresenter.resume();
        isActive = true;
        if (!firstLoad) {
            albumDetailPresenter.loadAlbumDetail();
        }
        firstLoad = false;
    }

    @Override
    public void onPause() {
        super.onPause();
        isActive = false;
        albumDetailPresenter.pause();
    }

    @Override
    public boolean isActive() {
        return isActive;
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

    private long dataVersion;
    @Override
    public void showMedias(List<MediaBean> beans, long version) {
        if (version <= dataVersion){
            return;
        }
        dataVersion = version;
        showMediaBeans(beans);
        showSelected();
    }

    public static AlbumImageSlotFragment newInstance(long bucketId, ArrayList<String> allowMimeTypes, ArrayList<String> notAllowMimeTypes, boolean selectMode
    ) {

        Bundle args = new Bundle();

        args.putLong(KEY_BUCKET_ID, bucketId);
        args.putStringArrayList(KEY_ALLOW_MIME_TYPES, allowMimeTypes);
        args.putStringArrayList(KEY_NOT_ALLOW_MIME_TYPES, notAllowMimeTypes);
        args.putBoolean(KEY_AWAYS_IN_SELECT_MODE , selectMode);

        args.putString(KEY_DATA_KEY , MediaUtils.getBucketId(bucketId, allowMimeTypes, notAllowMimeTypes));

        AlbumImageSlotFragment fragment = new AlbumImageSlotFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public long getSelectedSetKey() {
        return bucketId;
    }
}
