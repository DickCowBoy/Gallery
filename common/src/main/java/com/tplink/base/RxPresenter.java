/*
 * Copyright (C), 2017, TP-LINK TECHNOLOGIES CO., LTD.
 *
 * RxPresenter.java
 *
 * Author LinJl
 *
 * Ver 1.0, 2017-11-07, LinJl, Create file
 */
package com.tplink.base;


import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;

public abstract class RxPresenter<T extends BaseView> extends BasePresenter<T> {


    private CompositeDisposable mCompositeDisposable = new CompositeDisposable();

    public RxPresenter(T view) {
        super(view);
    }

    public void subscribe() {

    }

    public void unSubscribe() {
        if (mCompositeDisposable != null) {
            mCompositeDisposable.clear();
        }
    }

    protected void addDispose(Disposable disposable) {
        mCompositeDisposable.add(disposable);
    }
}
