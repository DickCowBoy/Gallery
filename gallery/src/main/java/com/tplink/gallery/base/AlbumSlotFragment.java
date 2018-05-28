package com.tplink.gallery.base;

import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.tplink.base.DragSelectTouchHelper;
import com.tplink.gallery.bean.AlbumBean;
import com.tplink.gallery.bean.MediaBean;
import com.tplink.gallery.selector.ItemChangedListener;
import com.tplink.gallery.ui.AlbumView;
import com.tplink.view.CommonDataView;

import java.util.Collection;
import java.util.List;


public class AlbumSlotFragment extends Fragment implements AlbumView.AlbumOperateProcessor, ItemChangedListener{
    private static final String KEY_AWAYS_IN_SELECT_MODE = "KEY_AWAYS_IN_SELECT_MODE";
    private static final String KEY_DATA_KEY = "KEY_DATA_KEY";

    AlbumView albumView = null;
    private boolean awaysInSelectMode = false;
    private String dataKey;
    private DragSelectTouchHelper.InterceptController interceptController;
    private AlbumSlotDataProvider imageSlotDataProvider;

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
        albumView = new AlbumView(getContext(), commonDataView, awaysInSelectMode, this);
        showAlbumBeans(imageSlotDataProvider.getAlbumDataBeans(dataKey));
        return commonDataView;
    }

    public void showAlbumBeans(List<AlbumBean> beans) {
        albumView.showAlbums(beans);
    }

    public void setInterceptController(DragSelectTouchHelper.InterceptController interceptController) {
        this.interceptController = interceptController;
    }

    public static AlbumSlotFragment newInstance(boolean selectMode, String key) {
        AlbumSlotFragment imageSlotFragment = new AlbumSlotFragment();
        Bundle bundle = new Bundle();
        bundle.putBoolean(KEY_AWAYS_IN_SELECT_MODE , selectMode);
        bundle.putString(KEY_DATA_KEY , key);
        imageSlotFragment.setArguments(bundle);
        return imageSlotFragment;
    }

    @Override
    public void onItemClick(AlbumBean data, int index) {
        // 显示相册
        if (imageSlotDataProvider != null) {
            imageSlotDataProvider.showAlbumDetail(data);
        }
    }

    @Override
    public boolean canSelectItem(AlbumBean item) {
        return false;
    }

    @Override
    public void delSelectItem(AlbumBean item) {

    }

    @Override
    public boolean isItemChecked(AlbumBean item) {
        return imageSlotDataProvider.isAlbumSelected(item.bucketId);
    }

    @Override
    public int getAlbumSelectCount(AlbumBean item) {
        return imageSlotDataProvider.getAlbumSelectedCount(item);
    }

    @Override
    public void onChanged(MediaBean entity) {

    }

    public interface AlbumSlotDataProvider {
        List<AlbumBean> getAlbumDataBeans(String key);
        boolean isAlbumSelected(long bucketId);
        void showAlbumDetail(AlbumBean bean);
        int getAlbumSelectedCount(AlbumBean bean);
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof AlbumSlotDataProvider) {
            imageSlotDataProvider = (AlbumSlotDataProvider) context;
        }
    }
}
