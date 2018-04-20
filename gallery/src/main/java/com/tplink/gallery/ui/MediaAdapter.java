package com.tplink.gallery.ui;
/*
 * Copyright (C) 2018, TP-LINK TECHNOLOGIES CO., LTD.
 *
 * AllPicAdapter.java
 *
 * Description
 *
 * Author Wang tao
 *
 * Ver 1.0, 2018-1-16, Wang tao, Create file
 */

import android.content.Context;
import android.graphics.RectF;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.tplink.base.Consts;
import com.tplink.gallery.bean.MediaBean;
import com.tplink.gallery.gallery.R;
import com.tplink.gallery.utils.MediaUtils;
import com.tplink.gallery.view.RegionRelativeLayout;
import com.tplink.view.CommonDataView;
import com.tplink.view.CommonDataViewHolder;
import com.tplink.view.CommonDataViewProxy;

public class MediaAdapter extends CommonDataViewProxy<MediaBean, MediaAdapter.MediaViewHolder> {
    private Context mContext;

    public MediaAdapter(Context context, CommonDataView commonDataView) {
        super(context, commonDataView);
        mContext = context;
    }

    @Override
    protected RecyclerView.LayoutManager getLayoutManager() {
        GridLayoutManager layoutManager = new GridLayoutManager(mContext, 4);
        return layoutManager;
    }


    @Override
    public MediaViewHolder onCreateViewHolderImpl(ViewGroup viewGroup) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.photo_gridview_media_detail,
                viewGroup, false);
        return new MediaViewHolder(view);
    }

    @Override
    public void onBindViewHolder(MediaViewHolder viewHolder, int i) {
        super.onBindViewHolder(viewHolder, i);
        MediaBean pic = getItem(i);
        MediaUtils.imageEngine.loadImage(mContext, pic.lastModify,
                null, viewHolder.mThumbnail, pic.getContentUri());
    }

    @Override
    protected int getContainerId() {
        return -1;
    }

    @Override
    protected int getCheckBoxId() {
        return R.id.checkbox_select;
    }

    @Override
    protected boolean canEnterSelectMode() {
        return true;
    }

    @Override
    protected int getRecycleType() {
        // 网格状
        return Consts.RECYCLE_TYPE_GRID;
    }

    @Override
    public int getEmptyIcon() {
        return R.drawable.photo_empty_icon;
    }

    @Override
    public boolean needAnim() {
        return false;
    }

    public static class MediaViewHolder extends CommonDataViewHolder {
        public ImageView mThumbnail;

        public MediaViewHolder(View itemView) {
            super(itemView);
            ((RegionRelativeLayout)itemView).setClickRectF(new RectF(0, 0, 1.0F, 0.5F));
        }

        @Override
        protected void initCommonView(View itemView) {
            mThumbnail = itemView.findViewById(R.id.iv_pic_detail);
        }

        @Override
        public int getCheckBoxId() {
            return R.id.checkbox_select;
        }
    }

}
