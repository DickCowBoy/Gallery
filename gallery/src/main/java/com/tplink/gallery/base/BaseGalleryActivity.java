/*
 * Copyright (C) 2018, TP-LINK TECHNOLOGIES CO., LTD.
 *
 * BaseGalleryActivity.java
 *
 * Description 相册基类紧提供数据显示
 *
 * Author LinJinLong
 *
 * Ver 1.0, 2018-- LinJinLong, Create file
 */
package com.tplink.gallery.base;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import com.tplink.gallery.bean.AlbumBean;
import com.tplink.gallery.bean.MediaBean;
import com.tplink.gallery.data.DataCacheManager;
import com.tplink.gallery.gallery.R;
import com.tplink.gallery.ui.BigImagePreview;
import com.tplink.gallery.view.AutoFitToolBar;
import com.tplink.gallery.view.LoadingView;
import com.tplink.gallery.view.SelectViewPager;
import com.tplink.widget.SlidingTabStripTP;

import java.util.ArrayList;
import java.util.List;

public abstract class BaseGalleryActivity extends PermissionActivity implements AutoFitToolBar.OnPaddingListener,
        ImageSlotFragment.ImageSlotDataProvider, MediaContract.MediaView, AlbumSlotFragment.AlbumSlotDataProvider {

    private LoadingView mLoadingView;
    private AutoFitToolBar mNormalToolbar;
    private TabLayout mTabLayout;
    private SelectViewPager mPager;
    private ContainerPagerAdapter mPagerAdapter;
    private boolean isActive = false;
    private MediaContract.MediaPresenter mediaPresenter;
    private BigImagePreview bigImagePreview;
    private RecyclerView bigImageView;
    private RecyclerView filmImageView;
    private boolean firstLoad = true;
    private String currentKey;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initView();
        mediaPresenter = new MediaPresenter(this, this, getAllowMimeTypes(), getNotAllowMimeTypes(), needResolveBurst());
        loadData();
    }

    private void initView() {
        setContentView(R.layout.layout_gallery);
        setWindow();

        mLoadingView = findViewById(R.id.loading_file_category);
        mLoadingView.setLoading(true);
        mLoadingView.setText(getString(R.string.photo_operation_loading));

        mNormalToolbar = findViewById(R.id.toolbar);
        mNormalToolbar.setOnPaddingListener(this);
        setSupportActionBar(mNormalToolbar);
        mNormalToolbar.setNavigationIcon(R.drawable.photo_ic_menu_back_button);
        mNormalToolbar.setNavigationOnClickListener((v) -> onBackPressed());

        mPager = findViewById(R.id.pager);
        mTabLayout = findViewById(R.id.tab_layout);

        if (mPagerAdapter == null) {
            mPagerAdapter = new ContainerPagerAdapter(getFragmentManager(),
                    this, mPager, awaysInSelectMode(), getAllowMimeTypes(),getNotAllowMimeTypes(), needResolveBurst());
        } else {
            mPagerAdapter.setInterceptController(mPager);
        }

        mPager.setAdapter(mPagerAdapter);
        mPager.setCurrentItem(mPagerAdapter.getPosInDirection(ContainerPagerAdapter.IMAGE_TAB));
        mTabLayout.setupWithViewPager(mPager);

        SlidingTabStripTP shapeIndicatorView = findViewById(R.id
                .custom_indicator);
        shapeIndicatorView.setupWithTabLayout(mTabLayout);
        shapeIndicatorView.setupWithViewPager(mPager);

        RecyclerView bigImageView = findViewById(R.id.rcl_gallery);
        RecyclerView filmImageView = findViewById(R.id.rcl_sub_gallery);
        bigImagePreview = new BigImagePreview(this, bigImageView, filmImageView);
    }

    @Override
    public void onBackPressed() {
        if (bigImagePreview.isShow()) {
            bigImagePreview.hide();
            currentKey = null;
            return;
        }
        super.onBackPressed();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!firstLoad) {
            loadData();
        }
        firstLoad = false;
        isActive = true;
        mediaPresenter.resume();
    }

    private void loadData() {
        mediaPresenter.loadMediaInfo();
        mediaPresenter.loadAlbumInfo();
    }

    @Override
    protected void onPause() {
        super.onPause();
        isActive = false;
        mediaPresenter.pause();
    }

    public void showViewFragment(Fragment fragment) {

        FragmentTransaction ft = getFragmentManager().beginTransaction();
        ft.replace(R.id.fl_container, fragment);
        ft.addToBackStack(fragment.getClass().getSimpleName());
        ft.commit();
    }

    @Override
    public void processBusinessOnAllPermissionGranted() {
        if (hasAllPermission()) {
        }
    }

    @Override
    public void onPadding(int left, int top, int right, int bottom) {

    }

    protected void setWindow() {
        Window window = getWindow();
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
        window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);

        window.setBackgroundDrawable(null);
    }

    @Override
    public List<MediaBean> getDataBeans(String key) {
        return beans == null ? new ArrayList<>() : beans;
    }

    protected abstract boolean awaysInSelectMode();
    protected abstract boolean needResolveBurst();

    @Override
    public boolean isActive() {
        return isActive;
    }

    private List<MediaBean> beans;
    private long mediaVersion = -1;
    @Override
    public void showMedias(List<MediaBean> beans, long version) {
        if (version <= mediaVersion) {
            return;
        }
        mediaVersion = version;
        ImageSlotFragment allFragment = mPagerAdapter.getAllFragment();
        if (allFragment != null) {
            allFragment.showMediaBeans(beans);
        }
        this.beans = beans;
        mLoadingView.setLoading(false);
    }

    private List<AlbumBean> albumBeans;
    private long albumVersion;
    @Override
    public void showAlbums(List<AlbumBean> beans, long version) {
        if (version <= albumVersion) {
            return;
        }
        albumVersion = version;
        AlbumSlotFragment allFragment = mPagerAdapter.getAlbumFragment();
        if (allFragment != null) {
            allFragment.showAlbumBeans(beans);
        }
        this.albumBeans = beans;
    }

    @Override
    public List<AlbumBean> getAlbumDataBeans(String key) {
        return albumBeans;
    }

    @Override
    public void showAlbumDetail(AlbumBean bean) {
        AlbumImageSlotFragment albumImageSlotFragment = AlbumImageSlotFragment.newInstance(bean.bucketId,
                getAllowMimeTypes(), getNotAllowMimeTypes());
        showViewFragment(albumImageSlotFragment);
    }

    @Override
    public void showAllImage(MediaBean data, int index, String key) {
        currentKey = key;
        bigImagePreview.setData(DataCacheManager.dataManager.getMediaBeanCollectionByKey(key).mediaBeans);
        bigImagePreview.showIndex(index);
    }

    @Override
    public void updateMediaIfNeed() {
        if (!TextUtils.isEmpty(currentKey)) {
            bigImagePreview.setData(DataCacheManager.dataManager.getMediaBeanCollectionByKey(currentKey).mediaBeans);
        }
    }

    public abstract ArrayList<String> getAllowMimeTypes();
    public abstract ArrayList<String> getNotAllowMimeTypes();
}
