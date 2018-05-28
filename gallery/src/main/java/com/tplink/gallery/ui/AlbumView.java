package com.tplink.gallery.ui;

import android.content.Context;

import com.tplink.gallery.base.AlbumAdapter;
import com.tplink.gallery.bean.AlbumBean;
import com.tplink.view.CommonDataView;
import com.tplink.view.CommonDataViewProxy;
import com.tplink.view.SpaceItemDecoration;

import java.util.Collection;
import java.util.List;

public class AlbumView implements CommonDataViewProxy.OnDataItemClick<AlbumBean>,
        CommonDataViewProxy.SelectController<AlbumBean>, AlbumAdapter.AlbumInfoInterface {

    protected CommonDataView mCommonDataView;
    private AlbumAdapter mDataProxy;
    private SpaceItemDecoration spaceItemDecoration;
    private boolean awaysInSelectMode = false;
    private AlbumOperateProcessor albumOperateProcessor;

    public AlbumView(Context context, CommonDataView recyclerView, boolean awaysInSelectMode, AlbumOperateProcessor albumOperateProcessor) {
        this.mCommonDataView = recyclerView;
        this.awaysInSelectMode = awaysInSelectMode;

        mDataProxy = new AlbumAdapter(context, mCommonDataView, this, awaysInSelectMode);
        mDataProxy.setListener(this);
        mDataProxy.setSelectController(this);
        mDataProxy.setOnSelectModeChanged(null);

        spaceItemDecoration = new SpaceItemDecoration(1, 0, false);
        //spaceItemDecoration.setLastColumnMargin(mediaPickerActivity.getNavigationBarHeight());
        mCommonDataView.getDataView().addItemDecoration(spaceItemDecoration);

        this.albumOperateProcessor = albumOperateProcessor;
    }

    @Override
    public void onItemClick(AlbumBean data, int index) {
        if (albumOperateProcessor != null) {
            albumOperateProcessor.onItemClick(data, index);
        }
    }

    @Override
    public boolean canSelectItem(AlbumBean item) {
        if (albumOperateProcessor != null) {
            return albumOperateProcessor.canSelectItem(item);
        }
        return false;
    }

    @Override
    public void delSelectItem(AlbumBean item) {
        if (albumOperateProcessor != null) {
            albumOperateProcessor.delSelectItem(item);
        }
    }

    @Override
    public int getAlbumSelectCount(AlbumBean entity) {
        return albumOperateProcessor.getAlbumSelectCount(entity);
    }

    @Override
    public boolean isAlbumSelected(AlbumBean entity) {
        return albumOperateProcessor.isItemChecked(entity);
    }

    public void showAlbums(List<AlbumBean> beans) {
        mDataProxy.updateData(beans);
    }

    public interface AlbumOperateProcessor {
        void onItemClick(AlbumBean data, int index);
        boolean canSelectItem(AlbumBean item);
        void delSelectItem(AlbumBean item);
        boolean isItemChecked(AlbumBean item);
        int getAlbumSelectCount(AlbumBean item);
    }

    public void setSelectItems(Collection<AlbumBean> items) {
        mDataProxy.setSelectItems(items);
    }

    public void  deleteSelectedItems(){
        mDataProxy.deleteSelectedItems();
    }


}
