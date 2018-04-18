/*
 * Copyright (C) 2017, TP-LINK TECHNOLOGIES CO., LTD.
 *
 * CommonDataView.java
 *
 * Description
 *
 * Author LJL
 *
 * Ver 1.0, Feb 15, 2017, LJL, Create file
 */
package com.tplink.view;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewStub;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.tplink.common.R;

/**
 * CommonDataView + CommonDataViewProxy 统一实现选择，滑动多选，删除动画，下拉回弹功能及无数据时展示空页面
 * 用户只需要关心如何显示数据，如何刷新数据，如何控制显示布局，监听选择状态个数的变化及监听点击事件(非选择模式下才会出发点击)
 *
 * 使用步骤：
 * 1.在需要显示数据的布局中加入该自定义控件查看fragment_download_list.xml
 * 2.继承CommonDataViewProxy实现细节部分参照DownloadListTAdapter.java
 * 3.在使用的地方初始化
 * mAdapter = new DownloadListAdapter(mContext, (CommonDataView) view.findViewById(R.id.cdv_view));
 * mAdapter.setListener(this);// 设置点击监听
 * mAdapter.setOnSelectModeChanged(this);//设置选择状态变化监听例如进入推出选择模式从而修改actionmode中的相关UI选择数量发生改变从而修改选中的数量
 *
 */
public class CommonDataView extends FrameLayout {
    private RecyclerView mDataView = null;
    private ViewStub mEmptyViewStub = null;
    private View mEmptyView = null;
    // empty view
    private ImageView mEmptyImg = null;
    private TextView mEmptyTxt = null;

    public CommonDataView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CommonDataView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        LayoutInflater.from(context).inflate(R.layout.common_data_view, this, true);
        mDataView = (RecyclerView) findViewById(R.id.rcw_common_data_content);
        mEmptyViewStub = (ViewStub) findViewById(R.id.vsb_common_data_empty);
    }

    public CommonDataView(Context context) {
        this(context, null);
    }

    public void showEmptyView(boolean showIcon, boolean showText, int icon, int text) {
        if (mEmptyView == null) {
            mEmptyView = mEmptyViewStub.inflate();
            mEmptyImg = (ImageView) mEmptyView.findViewById(R.id.iv_file_view_empty);
            mEmptyTxt = (TextView) mEmptyView.findViewById(R.id.tv_file_view_empty);

        }
        mEmptyView.setVisibility(View.VISIBLE);
        if (showIcon) {
            mEmptyImg.setVisibility(View.VISIBLE);
            mEmptyImg.setImageResource(icon);
        } else {
            mEmptyImg.setVisibility(View.GONE);
        }
        if (showText) {
            mEmptyTxt.setVisibility(View.VISIBLE);
            mEmptyTxt.setText(text);
        } else {
            mEmptyTxt.setVisibility(View.GONE);
        }
    }

    public void hideEmptyView() {
        if (mEmptyView != null) {
            mEmptyView.setVisibility(View.GONE);
        }
    }

    public RecyclerView getDataView() {
        return mDataView;
    }
}