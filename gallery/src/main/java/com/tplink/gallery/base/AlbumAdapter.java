/*
 * Copyright (C) 2018, TP-LINK TECHNOLOGIES CO., LTD.
 *
 * AlbumAdapter.java
 *
 * Description 相册Adapter
 *
 * Author LinJinLong
 *
 * Ver 1.0, 2018-02-12 LinJinLong, Create file
 */
package com.tplink.gallery.base;

import android.content.Context;
import android.graphics.RectF;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.tplink.base.Consts;
import com.tplink.gallery.bean.AlbumBean;
import com.tplink.gallery.gallery.R;
import com.tplink.gallery.utils.MediaUtils;
import com.tplink.gallery.view.RegionRelativeLayout;
import com.tplink.view.CommonDataView;
import com.tplink.view.CommonDataViewHolder;
import com.tplink.view.CommonDataViewProxy;

public class AlbumAdapter extends CommonDataViewProxy<AlbumBean, AlbumAdapter.AlbumViewHolder> {

    private AlbumInfoInterface mAlbumInfoInterface;

    public AlbumAdapter(Context context, CommonDataView commonDataView, AlbumInfoInterface albumInfoInterface, boolean awaysInSelectMode) {
        super(context, commonDataView);
        // 直接进入选择模式
        if (awaysInSelectMode) {
            mSelector.enterSelectionMode();
        }
        mAlbumInfoInterface = albumInfoInterface;
    }

    @Override
    protected RecyclerView.LayoutManager getLayoutManager() {
        LinearLayoutManager layoutManager = new LinearLayoutManager(mContext);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        return layoutManager;
    }

    @Override
    public AlbumViewHolder onCreateViewHolderImpl(ViewGroup viewGroup) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.photo_gridview_item_pic_album,
                viewGroup, false);
        return new AlbumViewHolder(view);
    }

    @Override
    public void onBindViewHolder(AlbumViewHolder viewHolder, int i) {
        AlbumBean picAlbum = getItem(i);
        int albumSelectCount = mAlbumInfoInterface.getAlbumSelectCount(picAlbum);
        if (albumSelectCount > 0) {
            if (!mSelector.isItemSelected(picAlbum)) {
                mSelector.toggle(picAlbum, i);
            }
        } else {
            if (mSelector.isItemSelected(picAlbum)) {
                mSelector.toggle(picAlbum, i);
            }
        }
        super.onBindViewHolder(viewHolder, i);
        MediaUtils.imageEngine.loadImage(mContext,
                picAlbum.lastModify, null, viewHolder.mIconImageView,
                picAlbum.getContentUri());
        viewHolder.mAlbumName.setText(picAlbum.displayName);
        viewHolder.mAlbumPicCount.setText(getCountText(picAlbum));
        if (mSelector.inSelectionMode()) {
            viewHolder.getCheckBox().setChecked(mAlbumInfoInterface.isAlbumSelected(picAlbum));
        }
    }

    protected String getCountText(AlbumBean picAlbum) {
        return mAlbumInfoInterface.getAlbumSelectCount(picAlbum) + "/" + String.valueOf(picAlbum.count);
    }

    @Override
    protected void onMultiNode(AlbumViewHolder viewHolder, int i) {
        AlbumBean picAlbum = getItem(i);
        viewHolder.mAlbumPicCount.setText(getCountText(picAlbum));
    }

    @Override
    protected int getContainerId() {
        return -1;
    }

    @Override
    protected int getCheckBoxId() {
        return R.id.checkbox_select;
    }

    // 不能进入选择模式
    @Override
    protected boolean canEnterSelectMode() {
        return mAlbumInfoInterface.longClickEnter();
    }

    @Override
    protected int getRecycleType() {
        return Consts.RECYCLE_TYPE_VIDEO;
    }

    @Override
    public int getEmptyIcon() {
        return R.drawable.photo_empty_icon;
    }

    @Override
    public boolean needAnim() {
        return false;
    }

    public static class AlbumViewHolder extends CommonDataViewHolder {
        public ImageView mIconImageView;
        public TextView mAlbumName;
        public TextView mAlbumPicCount;

        public AlbumViewHolder(View itemView) {
            super(itemView);
            ((RegionRelativeLayout) itemView).setClickRectF(new RectF(0.3F, 0, 1.0F, 1.0F));
        }

        @Override
        protected void initCommonView(View itemView) {
            mIconImageView = itemView.findViewById(R.id.iv_pic_album);
            mAlbumName = itemView.findViewById(R.id.tv_album_title);
            mAlbumPicCount = itemView.findViewById(R.id.tv_album_count);
        }

        @Override
        public int getCheckBoxId() {
            return R.id.checkbox_select;
        }
    }

    public interface AlbumInfoInterface {
        int getAlbumSelectCount(AlbumBean entity);
        boolean isAlbumSelected(AlbumBean entity);
        boolean longClickEnter();
    }

    public void updateBucket(long bucketId, int selectCount) {
        int index = -1;
        for (int i = 0; i < mData.size(); i++) {
            if (mData.get(i).bucketId == bucketId) {
                index = i;
                break;
            }
        }
        if (index != -1) {
            AlbumBean entity = mData.get(index);
            if (selectCount > 0) {
                // 需要选中
                if (!mSelector.isItemSelected(entity)) {
                    mSelector.toggle(entity, index);
                }
            } else {
                if (mSelector.isItemSelected(entity)) {
                    mSelector.toggle(entity, index);
                }
            }
            notifyItemChanged(index, Payload.MULTI_MODE);
        }
    }
}

