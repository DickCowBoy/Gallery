/*
 * Copyright (C) 2017, TP-LINK TECHNOLOGIES CO., LTD.
 *
 * CommonDataViewHolder.java
 *
 * Description
 *
 * Author LJL
 *
 * Ver 1.0, Feb 15, 2017, LJL, Create file
 */
package com.tplink.view;


import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.CheckBox;


public abstract class CommonDataViewHolder extends RecyclerView.ViewHolder {
    protected CheckBox mCheckBox;
    // 进入、退出动效时需要，需要具体页面item提供
    protected View mContainer;

    public CommonDataViewHolder(View itemView) {
        super(itemView);
        mCheckBox = itemView.findViewById(getCheckBoxId());
        initCommonView(itemView);
    }

    // 初始化checkbox 等通用组件
    protected abstract void initCommonView(View itemView);

    final public CheckBox getCheckBox() {
        return mCheckBox;
    }

    final public View getContainer() {
        return mContainer;
    }

    public abstract int getCheckBoxId();
}
