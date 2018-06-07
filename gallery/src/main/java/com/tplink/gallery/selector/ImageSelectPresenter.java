package com.tplink.gallery.selector;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import com.tplink.gallery.bean.AlbumBean;
import com.tplink.gallery.bean.MediaBean;
import com.tplink.gallery.dao.MediaDao;
import com.tplink.gallery.R;
import com.tplink.gallery.selector.wallpaper.ResultContainer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class ImageSelectPresenter extends MediaSelectorContract.MediaSelectorPresenter {

    public static final String SELECT_RESULT = "SELECT_RESULT";

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
    public boolean needPreview() {
        return mContainer.getCountLimit() != 1 || needPreview;
    }

    @Override
    public void setResult(Activity activity) {
        List<MediaBean> mediaEntries = mContainer.getMediaEntries();
        ArrayList<Uri> result = new ArrayList<>();
        for (MediaBean mediaEntry : mediaEntries) {
            result.add(mediaEntry.getContentUri());
        }
        Intent resultIntent = new Intent();
        resultIntent.putParcelableArrayListExtra(SELECT_RESULT,result);
        activity.setResult(Activity.RESULT_OK, resultIntent);
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
           if (countLimit != ResultContainer.UNLIMIT) {
               title = context.getResources()
                       .getQuantityString(R.plurals.select_pic_count_limit, count[0], count[0], count[1]);
           } else {
               title = context.getResources()
                       .getQuantityString(R.plurals.select_pic_count, count[0], count[0]);
           }
        }
        mView.showHeader(title);
    }
}
