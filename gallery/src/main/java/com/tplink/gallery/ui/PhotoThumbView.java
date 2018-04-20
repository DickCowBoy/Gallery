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
import com.tplink.view.SpaceItemDecoration;

import java.util.List;

public class PhotoThumbView {

    private MediaAdapter mDataProxy;

    private CommonDataView mCommonDataView;
    private SpaceItemDecoration spaceItemDecoration;

    public PhotoThumbView(Context context, CommonDataView recyclerView) {
        this.mCommonDataView = recyclerView;
        mDataProxy = new MediaAdapter(context, recyclerView);
        spaceItemDecoration = new SpaceItemDecoration(4, CommonUtils.dp2px(context, 2),
                false);
        //spaceItemDecoration.setLastColumnMargin(activity.getNavigationBarHeight());
        mCommonDataView.getDataView().addItemDecoration(spaceItemDecoration);
    }

    public void showMediaBeans(List<MediaBean> data) {
        mDataProxy.updateData(data);
    }


}
