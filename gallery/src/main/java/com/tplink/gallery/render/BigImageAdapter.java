package com.tplink.gallery.render;

import android.app.Activity;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bm.library.PhotoView;
import com.bm.library.tile.TileProvider;
import com.bumptech.glide.request.target.ViewTarget;
import com.bumptech.glide.request.transition.Transition;
import com.tplink.gallery.GlideApp;
import com.tplink.gallery.bean.MediaBean;
import com.tplink.gallery.R;

import java.util.List;

public class BigImageAdapter extends PagerAdapter implements ViewPager.OnPageChangeListener {
    private List<MediaBean> mediaBeans;
    private Activity mActivity;
    private ViewPager viewPager;
    private TileProvider tileProvider;
    private PhotoView currentItemView;
    private int mCurrentPagerState = ViewPager.SCROLL_STATE_IDLE;


    public BigImageAdapter(Activity mContext, ViewPager viewPager) {
        this.mActivity = mContext;
        this.viewPager = viewPager;
        this.viewPager.addOnPageChangeListener(this);
        this.viewPager.setAdapter(this);
        tileProvider = new TileProvider(mContext.getApplication());
    }

    public void setMediaBeans(List<MediaBean> mediaBeans) {
        this.mediaBeans = mediaBeans;
        notifyDataSetChanged();
    }

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
        GlideApp.with(mActivity).load(mediaBeans.get(position).getContentUri())
                //.override(imageWidthPixels, imageHeightPixels)
                .placeholder(R.mipmap.ic_launcher)
                .into(new PhotoViewTarget(view, mediaBeans.get(position)));
        container.addView(view);
        return view;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((View) object);
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
    }

    @Override
    public void onPageSelected(int position) {
    }

    @Override
    public void setPrimaryItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        if (object.equals(this.currentItemView)) {
            return;
        }
        this.currentItemView = (PhotoView) object;
        initTileLoader();
    }

    private void initTileLoader() {
        if (mCurrentPagerState == ViewPager.SCROLL_STATE_IDLE
                && currentItemView.getContentTag() != null
                && currentItemView.getContentTag().equals(mediaBeans.get(viewPager.getCurrentItem()))) {
            this.tileProvider.setRenderTarget(currentItemView);
            BigImageAdapter.this.tileProvider.initRegionDecoder(
                    mediaBeans.get(viewPager.getCurrentItem()).getContentUri());
        }
    }

    @Override
    public void onPageScrollStateChanged(int state) {
        this.mCurrentPagerState = state;
    }

    private class PhotoViewTarget extends ViewTarget<PhotoView, Drawable> {

        private MediaBean bean;
        public PhotoViewTarget(PhotoView view, MediaBean bean) {
            super(view);
            this.bean = bean;
        }

        @Override
        public void onResourceReady(Drawable resource, Transition<? super Drawable> transition) {
            view.setImageDrawableWithTag(resource, bean);
            if (currentItemView == view) {
                initTileLoader();
            }
        }
    }
}
