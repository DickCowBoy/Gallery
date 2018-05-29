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
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Menu;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import com.tplink.gallery.bean.AlbumBean;
import com.tplink.gallery.bean.MediaBean;
import com.tplink.gallery.data.DataCacheManager;
import com.tplink.gallery.gallery.R;
import com.tplink.gallery.selector.AlbumChangedListener;
import com.tplink.gallery.selector.ItemChangedListener;
import com.tplink.gallery.ui.BigImagePreview;
import com.tplink.gallery.view.AutoFitToolBar;
import com.tplink.gallery.view.LoadingView;
import com.tplink.gallery.view.SelectViewPager;
import com.tplink.widget.SlidingTabStripTP;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public abstract class BaseGalleryActivity extends PermissionActivity implements AutoFitToolBar.OnPaddingListener,
        ImageSlotFragment.ImageSlotDataProvider, MediaContract.MediaView, AlbumSlotFragment.AlbumSlotDataProvider, Toolbar.OnMenuItemClickListener {

    public static final int TOOLBAR_STYLE_THUMB = 0;
    public static final int TOOLBAR_STYLE_PREVIEW = 1;

    private int actionbarStyle = TOOLBAR_STYLE_THUMB;// 默认0缩略图，1大图预览

    private LoadingView mLoadingView;
    protected AutoFitToolBar mNormalToolbar;
    private TabLayout mTabLayout;
    private SelectViewPager mPager;
    private ContainerPagerAdapter mPagerAdapter;
    private boolean isActive = false;
    private MediaContract.MediaPresenter mediaPresenter;
    private BigImagePreview bigImagePreview;
    private boolean firstLoad = true;
    private String currentKey;

    protected List<AlbumChangedListener> albumChangedListeners = new ArrayList<>();
    protected List<ItemChangedListener> itemChangedListeners = new ArrayList<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initView();
        mediaPresenter = new MediaPresenter(this, this, getAllowMimeTypes(), getNotAllowMimeTypes(), needResolveBurst(), needImage(), needVideo());
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
        mNormalToolbar.setOnMenuItemClickListener(this);

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
            showNormalBar();
            currentKey = null;
            return;
        }
        super.onBackPressed();
    }

    protected void showNormalBar(){
        setWindow();
        if (mNormalToolbar != null) {
            mNormalToolbar.setTitleTextColor(getResources().getColor(R.color.photo_normal_toolbar_title));
            mNormalToolbar.setNavigationIcon(R.drawable.photo_ic_menu_back_button);
            actionbarStyle = TOOLBAR_STYLE_THUMB;
        }
        invalidateOptionsMenu();
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        switch (actionbarStyle) {
            case TOOLBAR_STYLE_THUMB:
                menu.findItem(R.id.action_select).setVisible(true);
                break;
            case TOOLBAR_STYLE_PREVIEW:
                menu.findItem(R.id.action_select).setVisible(false);
                break;
        }
        return super.onPrepareOptionsMenu(menu);
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
    public boolean onCreateOptionsMenu(Menu menu) {
        if (getMenuId() != 0) {
            getMenuInflater().inflate(getMenuId(), menu);
        }
        return true;
    }

    protected abstract int getMenuId();

    @Override
    public void onPadding(int left, int top, int right, int bottom) {

    }

    protected void setWindow() {
        Window window = getWindow();
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
        window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);

        window.setBackgroundDrawable(null);
    }

    protected void setPreviewWindow() {
        Window window = getWindow();
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
        window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
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
                getAllowMimeTypes(), getNotAllowMimeTypes(), awaysInSelectMode());
        showViewFragment(albumImageSlotFragment);
    }

    @Override
    public void showAllImage(MediaBean data, int index, String key) {
        currentKey = key;
        bigImagePreview.setData(DataCacheManager.dataManager.getMediaBeanCollectionByKey(key).mediaBeans);
        bigImagePreview.showIndex(index);
        showPreviewBar();
    }

    private void showPreviewBar() {
        setPreviewWindow();
        if (mNormalToolbar != null) {
            mNormalToolbar.setTitleTextColor(getResources().getColor(R.color.white));
            mNormalToolbar.setNavigationIcon(R.drawable.ic_arrow_back);
            mNormalToolbar.getMenu().findItem(R.id.action_select).setVisible(false);
            actionbarStyle = TOOLBAR_STYLE_PREVIEW;
        }
        invalidateOptionsMenu();
    }

    @Override
    public void updateMediaIfNeed() {
        if (!TextUtils.isEmpty(currentKey)) {
            bigImagePreview.setData(DataCacheManager.dataManager.getMediaBeanCollectionByKey(currentKey).mediaBeans);
        }
    }

    public abstract ArrayList<String> getAllowMimeTypes();
    public abstract ArrayList<String> getNotAllowMimeTypes();

    public abstract boolean needVideo();
    public abstract boolean needImage();

    @Override
    public Collection<MediaBean> getSelectedDataBeans(long key) {
        return null;
    }

    public boolean isSelectionLoading() {
        return false;
    }

    public void regAlbumChangedListeners(AlbumChangedListener albumChangedListener) {
        this.albumChangedListeners.add(albumChangedListener);
    }

    public void unregAlbumChangedListeners(AlbumChangedListener albumChangedListener) {
        this.albumChangedListeners.remove(albumChangedListener);
    }

    @Override
    public void regItemChangedListeners(ItemChangedListener listener) {
        this.itemChangedListeners.add(listener);
    }

    @Override
    public void unregItemChangedListeners(ItemChangedListener listener) {
        this.itemChangedListeners.remove(listener);
    }

    @Override
    public boolean isAlbumSelected(long bucketId) {
        return false;
    }

    @Override
    public int getAlbumSelectedCount(long bucketId) {
        return 0;
    }

    @Override
    public boolean canSelectItem(MediaBean item, String opeSource) {
        return true;
    }

    @Override
    public void delSelectItem(MediaBean item, String opeSource) {

    }

    @Override
    public boolean canSelectAlbum(AlbumBean item) {
        return false;
    }

    @Override
    public void delSelectAlbum(AlbumBean item) {

    }
}
