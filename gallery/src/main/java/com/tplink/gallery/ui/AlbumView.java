package com.tplink.gallery.ui;

import android.content.Context;

import com.tplink.gallery.base.AlbumAdapter;
import com.tplink.gallery.bean.AlbumBean;
import com.tplink.view.CommonDataView;
import com.tplink.view.CommonDataViewProxy;
import com.tplink.view.SpaceItemDecoration;

import java.util.List;

public class AlbumView implements CommonDataViewProxy.OnDataItemClick<AlbumBean>,
        CommonDataViewProxy.SelectController<AlbumBean>, AlbumAdapter.AlbumInfoInterface {

    protected CommonDataView mCommonDataView;
    private AlbumAdapter mDataProxy;
    private SpaceItemDecoration spaceItemDecoration;
    private boolean awaysInSelectMode = false;

    public AlbumView(Context context, CommonDataView recyclerView, boolean awaysInSelectMode) {
        this.mCommonDataView = recyclerView;
        this.awaysInSelectMode = awaysInSelectMode;

        mDataProxy = new AlbumAdapter(context, mCommonDataView, this, awaysInSelectMode);
        mDataProxy.setListener(this);
        mDataProxy.setSelectController(this);
        mDataProxy.setOnSelectModeChanged(null);

        spaceItemDecoration = new SpaceItemDecoration(1, 0, false);
        //spaceItemDecoration.setLastColumnMargin(mediaPickerActivity.getNavigationBarHeight());
        mCommonDataView.getDataView().addItemDecoration(spaceItemDecoration);


    }

    @Override
    public void onItemClick(AlbumBean data, int index) {

    }

    @Override
    public boolean canSelectItem(AlbumBean item) {
        return false;
    }

    @Override
    public void delSelectItem(AlbumBean item) {

    }

    @Override
    public int getAlbumSelectCount(AlbumBean entity) {
        return 0;
    }

    public void showAlbums(List<AlbumBean> beans) {
        mDataProxy.updateData(beans);
    }
}
