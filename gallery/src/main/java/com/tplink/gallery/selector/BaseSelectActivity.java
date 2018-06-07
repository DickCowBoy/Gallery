package com.tplink.gallery.selector;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.tplink.base.Consts;
import com.tplink.gallery.base.BaseGalleryActivity;
import com.tplink.gallery.bean.AlbumBean;
import com.tplink.gallery.bean.MediaBean;
import com.tplink.gallery.R;
import com.tplink.gallery.selector.wallpaper.ResultContainer;
import com.tplink.gallery.view.InterceptCheckBox;

import java.util.Collection;
import java.util.List;
import java.util.Set;

public abstract class BaseSelectActivity extends BaseGalleryActivity
        implements MediaSelectorContract.MediaSelectorView, InterceptCheckBox.ToggleIntercept {

    protected MediaSelectorContract.MediaSelectorPresenter mediaSelectorPresenter;
    private InterceptCheckBox mCheckBox;
    private MediaBean currentMedia;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        this.mediaSelectorPresenter = initSelectorPresenter();
        super.onCreate(savedInstanceState);
        this.mediaSelectorPresenter.loadSelectInfo(this, getIntent());
        mCheckBox = findViewById(R.id.cb_item_selected);
        mCheckBox.setToggleIntercept(this);
    }

    @Override
    protected boolean awaysInSelectMode() {
        return this.mediaSelectorPresenter.getMaxSelectCount() > 1
                || this.mediaSelectorPresenter.getMaxSelectCount() == ResultContainer.UNLIMIT;
    }

    @Override
    protected boolean needSureBottom() {
        return  mediaSelectorPresenter.getMaxSelectCount() != 1
                && super.needSureBottom();
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
                if (actionbarStyle == TOOLBAR_STYLE_PREVIEW) {
                    this.mediaSelectorPresenter.addSingleMedia(currentMedia);
                }
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
    public int getAlbumSelectedCount(long bucketId) {
        Set<MediaBean> selectBucketMedia = mediaSelectorPresenter.getSelectBucketMedia(bucketId);
        return selectBucketMedia == null ? 0 : selectBucketMedia.size();
    }

    @Override
    public boolean canSelectItem(MediaBean item, String opeSource) {
        boolean b = mediaSelectorPresenter.addSingleMedia(item);
        if (b) {
            for (AlbumChangedListener albumChangedListener : albumChangedListeners) {
                if (albumChangedListener.getOpeSource().equals(opeSource)) {
                    continue;
                }
                albumChangedListener.onChanged(item, true);
            }

            for (ItemChangedListener itemChangedListener : itemChangedListeners) {
                itemChangedListener.onChanged(item);
            }
        }
        return b;
    }

    @Override
    public void delSelectItem(MediaBean item, String opeSource) {
        mediaSelectorPresenter.removeSingleMedia(item);
        for (AlbumChangedListener albumChangedListener : albumChangedListeners) {
            if (albumChangedListener.getOpeSource().equals(opeSource)) {
                continue;
            }
            albumChangedListener.onChanged(item, false);
        }


        for (ItemChangedListener itemChangedListener : itemChangedListeners) {
            itemChangedListener.onChanged(item);
        }
    }

    @Override
    public boolean canSelectAlbum(AlbumBean item) {
        Collection<MediaBean> mediaBeans = mediaSelectorPresenter.addAlbumMedia(item.bucketId);
        if (mediaBeans != null) {
            for (AlbumChangedListener albumChangedListener : albumChangedListeners) {
                albumChangedListener.onChanged(item.bucketId, true, mediaBeans);
            }
        }
        return mediaBeans != null && mediaBeans.size() > 0;
    }

    @Override
    public void delSelectAlbum(AlbumBean item) {
        Collection<MediaBean> mediaBeans = mediaSelectorPresenter.delAlbumMedia(item.bucketId);
        for (AlbumChangedListener albumChangedListener : albumChangedListeners) {
            albumChangedListener.onChanged(item.bucketId, false, mediaBeans);
        }
    }

    @Override
    protected void showNormalBar() {
        super.showNormalBar();
        mCheckBox.setVisibility(View.GONE);
    }

    @Override
    protected void showPreviewBar(MediaBean data) {
        super.showPreviewBar(data);
        onImageChanged(data);
        mCheckBox.setVisibility(needBigImageCheckBox() ? View.VISIBLE: View.GONE);
        currentMedia = data;
    }

    protected boolean needBigImageCheckBox() {
        return this.mediaSelectorPresenter.getMaxSelectCount() != 1;
    }

    @Override
    public void onImageChanged(MediaBean current) {
        currentMedia = current;
        if (needBigImageCheckBox()) {
            mCheckBox.setChecked(mediaSelectorPresenter.isItemSelected(current));
        }
    }

    @Override
    public boolean canToggle(boolean isCheck) {
        if (isCheck == false) {
            // 检测是否还能添加
            return canSelectItem(currentMedia, getClass().getName());
        } else {
            delSelectItem(currentMedia, getClass().getName());
            return true;
        }
    }

    @Override
    public void onMediaItemClick(MediaBean data, int index, String key) {
        if (mediaSelectorPresenter.needPreview()) {
            super.onMediaItemClick(data, index, key);
        } else {
            mediaSelectorPresenter.addSingleMedia(data);
            // not need to preview
            mediaSelectorPresenter.setResult(this);
        }
    }

    @Override
    public boolean longClickEnter() {
        return false;
    }
}
