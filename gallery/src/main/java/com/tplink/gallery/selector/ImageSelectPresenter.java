package com.tplink.gallery.selector;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;

import com.tplink.gallery.bean.AlbumBean;
import com.tplink.gallery.bean.MediaBean;
import com.tplink.gallery.dao.MediaDao;
import com.tplink.gallery.gallery.R;

import java.util.Collection;
import java.util.List;

public class ImageSelectPresenter extends MediaSelectorContract.MediaSelectorPresenter {

    private MediaDao mediaDao;
    private int countLimit;
    private long sizeLimit;

    public ImageSelectPresenter(Context context,
                                MediaSelectorContract.MediaSelectorView view,
                                int countLimit, long sizeLimit, boolean needPreview,
                                boolean needImage,
                                boolean needVideo) {
        super(context, view, countLimit, sizeLimit, needPreview, needImage, needVideo);
        this.countLimit = countLimit;
        this.sizeLimit = sizeLimit;
        mediaDao = new MediaDao(context);
    }

    @Override
    public void resume() {

    }

    @Override
    public void pause() {

    }

    @Override
    public boolean isFirstLoading() {
        return isLoading;
    }

    @Override
    public boolean needSelectAlbum() {
        return false;
    }


    @Override
    public Collection<MediaBean> addAlbumMedia(long bucketId) {
        return null;
    }

    @Override
    public boolean isBucketSelected(long bucketId) {
        return false;
    }

    @Override
    public void initDataByAllMedia(List<MediaBean> datas, long version) {

    }

    @Override
    public void initDataByAlbums(List<AlbumBean> datas, long version) {

    }

    @Override
    public void setResult(Activity activity) {
        activity.finish();
    }

    @Override
    public void loadSelectInfo(Context context, Intent intent) {

    }

    // TODO
    protected void showTitle() {
        int[] count = mContainer.getCount();
        String title =  context.getResources().getString(R.string.select_pic);
        if (count[0] != 0) {
            title = context.getResources()
                    .getQuantityString(R.plurals.select_pic_count_limit, count[0], count[0], count[1]);
        }
        mView.showHeader(title);
    }
}
