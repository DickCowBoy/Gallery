/*
 * Copyright (C), 2016, TP-LINK TECHNOLOGIES CO., LTD.
 *
 * BasePresenter.java
 *
 * Author MaoJun
 *
 * Ver 1.0, 2016-12-19, MaoJun, Create file
 */
package com.tplink.base;

public abstract class BasePresenter<T extends BaseView> {
    protected T mView;
    public BasePresenter(T view) {
        mView = view;
    }
}
