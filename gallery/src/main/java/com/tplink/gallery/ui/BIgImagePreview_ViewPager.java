package com.tplink.gallery.ui;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bm.library.PhotoView;
import com.davemorrissey.labs.subscaleview.ImageSource;
import com.tplink.gallery.GlideApp;
import com.tplink.gallery.bean.MediaBean;
import com.tplink.gallery.gallery.R;
import com.tplink.gallery.render.BigImageAdapter;

import java.util.ArrayList;
import java.util.List;

public class BIgImagePreview_ViewPager {

    private ViewPager mViewPager;

    private Activity mActivity;
    private List<MediaBean> mediaBeans;
    private BigImagePreview.BigPreviewCallback bigPreviewCallback;
    private BigImageAdapter largeImageAdapter;

    public BIgImagePreview_ViewPager(Activity mActivity,ViewPager mViewPager, BigImagePreview.BigPreviewCallback bigPreviewCallback) {
        this.mViewPager = mViewPager;
        mViewPager.setPageMargin((int) (mActivity.getResources().getDisplayMetrics().density * 15));
        this.mActivity = mActivity;
        this.bigPreviewCallback = bigPreviewCallback;
        largeImageAdapter = new BigImageAdapter(mActivity, this.mViewPager);
    }

    public void setData(List<MediaBean> data) {
        List<ImageSource> sources = new ArrayList<>();
        for (MediaBean datum : data) {
            sources.add(ImageSource.uri(datum.getContentUri(), datum.width, datum.height, 0, datum.mimeType));
        }
        mediaBeans = data;
        largeImageAdapter.setMediaBeans(mediaBeans);
    }

    public void showIndex(int index) {
        mViewPager.setCurrentItem(index);
        this.mViewPager.setVisibility(View.VISIBLE);
    }

    public void hide() {
        this.mViewPager.setVisibility(View.GONE);
    }

    public boolean isShow() {
        return this.mViewPager.getVisibility() == View.VISIBLE;
    }
}
