package com.tplink.gallery.selector;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.MenuItem;
import android.widget.Toast;

import com.tplink.base.Consts;
import com.tplink.gallery.base.BaseGalleryActivity;
import com.tplink.gallery.bean.AlbumBean;
import com.tplink.gallery.bean.MediaBean;
import com.tplink.gallery.gallery.R;

import java.util.Collection;
import java.util.List;
import java.util.Set;

public abstract class BaseSelectActivity extends BaseGalleryActivity implements MediaSelectorContract.MediaSelectorView {

    private MediaSelectorContract.MediaSelectorPresenter mediaSelectorPresenter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        this.mediaSelectorPresenter = initSelectorPresenter();
        super.onCreate(savedInstanceState);
        this.mediaSelectorPresenter.loadSelectInfo(this, getIntent());
    }

    @Override
    protected boolean awaysInSelectMode() {
        return this.mediaSelectorPresenter.getMaxSelectCount() > 1;
    }

    @Override
    protected boolean needResolveBurst() {
        return false;
    }

    @Override
    public void showAlbums(List<AlbumBean> beans, long version) {
        super.showAlbums(beans, version);
        this.mediaSelectorPresenter.initDataByAlbums(beans, version);
    }

    @Override
    public void showMedias(List<MediaBean> beans, long version) {
        super.showMedias(beans, version);
        this.mediaSelectorPresenter.initDataByAllMedia(beans, version);
    }

    protected abstract MediaSelectorContract.MediaSelectorPresenter initSelectorPresenter();

    private long toastTime = 0;
    @Override
    public void showHint(String msg) {
        // 避免滑动多选时多次选中
        if (System.currentTimeMillis() - toastTime > Consts.SHORT_DELAY) {
            Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
            toastTime = System.currentTimeMillis();
        }
    }

    @Override
    public void showHeader(String title) {
        mNormalToolbar.setTitle(title);
    }

    @Override
    public void showErrorMsg(String title) {
        showHint(title);
    }

    @Override
    public void showSetResultFinished() {
        // 结束
        this.finish();
    }

    @Override
    public void showSetResultStart() {

    }

    @Override
    public void showSelected(List<MediaBean> datas) {
        for (AlbumChangedListener albumChangedListener : albumChangedListeners) {
            albumChangedListener.showSelected();
        }
    }

    @Override
    protected int getMenuId() {
        return R.menu.menu_select;
    }


    @Override
    public boolean onMenuItemClick(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_select:
                this.mediaSelectorPresenter.setResult(this);
                break;
        }
        return true;
    }

    @Override
    public Collection<MediaBean> getSelectedDataBeans(long key) {
        return this.mediaSelectorPresenter.getSelectBucketMedia(key);
    }

    public boolean isSelectionLoading() {
        return mediaSelectorPresenter.isFirstLoading();
    }

    @Override
    public boolean isAlbumSelected(long bucketId) {
        return mediaSelectorPresenter.isBucketSelected(bucketId);
    }

    @Override
    public int getAlbumSelectedCount(AlbumBean bean) {
        Set<MediaBean> selectBucketMedia = mediaSelectorPresenter.getSelectBucketMedia(bean.bucketId);
        return selectBucketMedia == null ? 0 : selectBucketMedia.size();
    }
}
