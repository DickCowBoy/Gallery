/*
 * Copyright (C) 2018, TP-LINK TECHNOLOGIES CO., LTD.
 *
 * ImageSlotFragment.java
 *
 * Description 只负责图片显示不做任何逻辑的操作
 *
 * Author LinJinLong
 *
 * Ver 1.0, 2018-- LinJinLong, Create file
 */
package com.tplink.gallery.base;

import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.tplink.base.DragSelectTouchHelper;
import com.tplink.gallery.bean.MediaBean;
import com.tplink.gallery.selector.AlbumChangedListener;
import com.tplink.gallery.ui.PhotoThumbView;
import com.tplink.view.CommonDataView;

import java.util.Collection;
import java.util.List;

public class ImageSlotFragment extends Fragment implements PhotoThumbView.PhotoThumbListener, AlbumChangedListener {

    protected static final String KEY_AWAYS_IN_SELECT_MODE = "KEY_AWAYS_IN_SELECT_MODE";
    protected static final String KEY_DATA_KEY = "KEY_DATA_KEY";

    private boolean needUpdateSelect = false;

    PhotoThumbView thumbView = null;
    private boolean awaysInSelectMode = false;
    private String dataKey;
    private DragSelectTouchHelper.InterceptController interceptController;
    protected ImageSlotDataProvider imageSlotDataProvider;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle arguments = getArguments();
        if (arguments != null) {
            awaysInSelectMode = arguments.getBoolean(KEY_AWAYS_IN_SELECT_MODE);
            dataKey = arguments.getString(KEY_DATA_KEY);
        }

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        CommonDataView commonDataView = new CommonDataView(getContext(), null);
        thumbView = new PhotoThumbView(getContext(), commonDataView, awaysInSelectMode, imageSlotDataProvider.longClickEnter());
        thumbView.setPhotoThumbListener(this);
        if (interceptController != null) {
            thumbView.setParentView(interceptController);
        }
        loadDataForView();
        return commonDataView;
    }

    public void setInterceptController(DragSelectTouchHelper.InterceptController interceptController) {
        this.interceptController = interceptController;
        if (thumbView != null) {
            thumbView.setParentView(interceptController);
        }
    }

    protected void loadDataForView() {
        if (imageSlotDataProvider != null) {
            showMediaBeans(imageSlotDataProvider.getDataBeans(dataKey));
        }
    }


    @Override
    public void onItemClick(MediaBean data, int index) {
        // 预览图片
        if (imageSlotDataProvider != null) {
            imageSlotDataProvider.onMediaItemClick(data, index, dataKey);
        }
    }

    @Override
    public boolean canSelectItem(MediaBean item) {
        return imageSlotDataProvider.canSelectItem(item, getOpeSource());
    }

    @Override
    public void delSelectItem(MediaBean item) {
        imageSlotDataProvider.delSelectItem(item, getOpeSource());
    }

    @Override
    public void onSelectModeChanged(boolean inSelectMode) {

    }

    @Override
    public void onSelectCountChanged(int count, int amount) {

    }

    public void showMediaBeans(List<MediaBean> beans) {
        thumbView.showMediaBeans(beans);
        showSelected();
        if (imageSlotDataProvider != null) {
            imageSlotDataProvider.updateMediaIfNeed();
        }
    }

    public static ImageSlotFragment newInstance(boolean selectMode, String key) {
        ImageSlotFragment imageSlotFragment = new ImageSlotFragment();
        Bundle bundle = new Bundle();
        bundle.putBoolean(KEY_AWAYS_IN_SELECT_MODE , selectMode);
        bundle.putString(KEY_DATA_KEY , key);
        imageSlotFragment.setArguments(bundle);
        return imageSlotFragment;
    }

    @Override
    public void onChanged(long bucketId, boolean selected, Collection<MediaBean> beans) {
        if (selected) {
            thumbView.setSelectItems(beans);
        } else {
            thumbView.delSelectItems(beans);
        }
    }

    public long getSelectedSetKey() {
        return -1;
    }

    @Override
    public void onChanged(MediaBean entity, boolean select) {
        if (select) {
            thumbView.addSelectItem(entity);
        } else {
            thumbView.delSelectItems(entity);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (needUpdateSelect) {
            showSelected();
        }
    }

    @Override
    public void showSelected() {
        if (thumbView != null && imageSlotDataProvider != null) {
            thumbView.setSelectItems(imageSlotDataProvider.getSelectedDataBeans(getSelectedSetKey()));
        } else {
            needUpdateSelect = true;
        }
    }

    @Override
    public String getOpeSource() {
        return getClass().getSimpleName();
    }

    public interface ImageSlotDataProvider {
        List<MediaBean> getDataBeans(String key);
        void onMediaItemClick(MediaBean data, int index, String key);
        void updateMediaIfNeed();
        Collection<MediaBean> getSelectedDataBeans(long key);
        void delSelectItem(MediaBean item, String opeSource);
        boolean canSelectItem(MediaBean item, String opeSource);
        void regAlbumChangedListeners(AlbumChangedListener albumChangedListener);

        void unregAlbumChangedListeners(AlbumChangedListener albumChangedListener);

        boolean longClickEnter();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof ImageSlotDataProvider) {
            imageSlotDataProvider = (ImageSlotDataProvider) context;
            imageSlotDataProvider.regAlbumChangedListeners(this);
        }

    }

    @Override
    public void onDetach() {
        super.onDetach();
        if (imageSlotDataProvider != null) {
            imageSlotDataProvider.unregAlbumChangedListeners(this);
        }
    }
}
