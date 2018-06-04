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

import java.util.ArrayList;
import java.util.List;

public class BIgImagePreview_ViewPager {

    private ViewPager mViewPager;

    private Activity mActivity;
    private List<MediaBean> mediaBeans;
    private BigImagePreview.BigPreviewCallback bigPreviewCallback;
    private PagerAdapter largeImageAdapter;

    public BIgImagePreview_ViewPager(Activity mActivity,ViewPager mViewPager, BigImagePreview.BigPreviewCallback bigPreviewCallback) {
        this.mViewPager = mViewPager;
        mViewPager.setPageMargin((int) (mActivity.getResources().getDisplayMetrics().density * 15));
        this.mActivity = mActivity;
        this.bigPreviewCallback = bigPreviewCallback;
        this.mViewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                if (bigPreviewCallback != null) {
                    bigPreviewCallback.onImageChanged(mediaBeans.get(position));
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
        largeImageAdapter = new PagerAdapter() {
            @Override
            public int getCount() {
                return mediaBeans == null ? 0 : mediaBeans.size();
            }

            @Override
            public boolean isViewFromObject(View view, Object object) {
                return view == object;
            }

            @Override
            public Object instantiateItem(ViewGroup container, int position) {
                PhotoView view = new PhotoView(mActivity);
                view.enable();
                view.setScaleType(ImageView.ScaleType.FIT_CENTER);
                GlideApp.with(mActivity).asBitmap().load(mediaBeans.get(position).getContentUri())
                        //.override(imageWidthPixels, imageHeightPixels)
                        .placeholder(R.mipmap.ic_launcher)
                        .into(view);
                container.addView(view);
                return view;
            }

            @Override
            public void destroyItem(ViewGroup container, int position, Object object) {
                container.removeView((View) object);
            }

        };
        this.mViewPager.setAdapter(largeImageAdapter);
    }

    public void setData(List<MediaBean> data) {
        List<ImageSource> sources = new ArrayList<>();
        for (MediaBean datum : data) {
            sources.add(ImageSource.uri(datum.getContentUri(), datum.width, datum.height, 0, datum.mimeType));
        }
        mediaBeans = data;
        largeImageAdapter.notifyDataSetChanged();

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
