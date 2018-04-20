/*
 * Copyright (C) 2018, TP-LINK TECHNOLOGIES CO., LTD.
 *
 * PhotoThumbView.java
 *
 * Description 显示缩略图页面，只负责显示不负责数据加载
 *
 * Author LinJinLong
 *
 * Ver 1.0, 2018-04-20 LinJinLong, Create file
 */
package com.tplink.gallery.ui;

import android.content.Context;

import com.tplink.base.CommonUtils;
import com.tplink.gallery.bean.MediaBean;
import com.tplink.view.CommonDataView;
import com.tplink.view.CommonDataViewProxy;
import com.tplink.view.SpaceItemDecoration;

import java.util.List;
import java.util.Set;

public class PhotoThumbView implements CommonDataViewProxy.OnDataItemClick<MediaBean>,CommonDataViewProxy.SelectController<MediaBean>,CommonDataViewProxy.OnSelectStatusChanged {

    private MediaAdapter mDataProxy;

    private CommonDataView mCommonDataView;
    private SpaceItemDecoration spaceItemDecoration;
    private PhotoThumbListener photoThumbListener;
    private boolean awaysInSelectMode = false;

    public PhotoThumbView(Context context, CommonDataView recyclerView, boolean awaysInSelectMode) {
        this.mCommonDataView = recyclerView;
        this.awaysInSelectMode = awaysInSelectMode;
        mDataProxy = new MediaAdapter(context, recyclerView, awaysInSelectMode);
        spaceItemDecoration = new SpaceItemDecoration(4, CommonUtils.dp2px(context, 2),
                false);
        //spaceItemDecoration.setLastColumnMargin(activity.getNavigationBarHeight());
        mCommonDataView.getDataView().addItemDecoration(spaceItemDecoration);
        mDataProxy.setListener(this);
        mDataProxy.setSelectController(this);
        mDataProxy.setOnSelectModeChanged(this);
    }

    public void showMediaBeans(List<MediaBean> data) {
        mDataProxy.updateData(data);
    }

    public void delSelectItems(MediaBean item) {
        mDataProxy.delSelectItem(item);
    }

    public void delSelectItems(List<MediaBean> items) {
        mDataProxy.delSelectItems(items);
    }

    public void setSelectItems(Set<MediaBean> items) {
        mDataProxy.setSelectItems(items);
    }



    @Override
    public void onItemClick(MediaBean data, int index) {
        if (photoThumbListener != null) {
            photoThumbListener.onItemClick(data, index);
        }

    }

    @Override
    public boolean canSelectItem(MediaBean item) {
        if (photoThumbListener != null) {
            return photoThumbListener.canSelectItem(item);
        }
        return true;
    }

    @Override
    public void delSelectItem(MediaBean item) {
        if (photoThumbListener != null) {
            photoThumbListener.delSelectItem(item);
        }
    }

    @Override
    public void onSelectModeChanged(boolean inSelectMode) {
        if (photoThumbListener != null) {
            photoThumbListener.onSelectModeChanged(inSelectMode);
        }
    }

    @Override
    public void onSelectCountChanged(int count, int amount) {
        if (photoThumbListener != null) {
            photoThumbListener.onSelectCountChanged(count, amount);
        }
    }

    /**
     * 控制PhotoThumbView的行为
     */
    public static interface PhotoThumbListener {
        void onItemClick(MediaBean data, int index);

        boolean canSelectItem(MediaBean item);

        void delSelectItem(MediaBean item);

        void onSelectModeChanged(boolean inSelectMode);

        void onSelectCountChanged(int count, int amount);
    }

    public void setPhotoThumbListener(PhotoThumbListener photoThumbListener) {
        this.photoThumbListener = photoThumbListener;
    }
}
