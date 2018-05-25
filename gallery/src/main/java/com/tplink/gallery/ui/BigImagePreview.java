/*
 * Copyright (C) 2018, TP-LINK TECHNOLOGIES CO., LTD.
 *
 * BigImagePreview.java
 *
 * Description 负责显示大图
 *
 * Author LinJinLong
 *
 * Ver 1.0, 2018-04-20 LinJinLong, Create file
 */
package com.tplink.gallery.ui;

import android.app.Activity;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.View;

import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.tplink.gallery.GlideApp;
import com.tplink.gallery.base.BaseGalleryActivity;
import com.tplink.gallery.bean.MediaBean;
import com.tplink.gallery.view.BigImageViewController;
import com.tplink.gallery.view.DrawContent;
import com.tplink.gallery.view.GalleryTextureView;

import java.util.ArrayList;
import java.util.List;

public class BigImagePreview {

    private GalleryTextureView mLargeImageRecycle;
    BigImageViewController bigImageViewController;
    DrawContentProvider provider;

    private Activity activity;

    private List<GlideDrawableContent> drawContents;

    public BigImagePreview(Activity context, GalleryTextureView mLargeImageRecycle) {
        this.mLargeImageRecycle = mLargeImageRecycle;
        activity = context;
        provider = new DrawContentProvider();
        mLargeImageRecycle.setViewController(bigImageViewController);
        bigImageViewController = new BigImageViewController(mLargeImageRecycle, provider);
        mLargeImageRecycle.setViewController(bigImageViewController);
    }

    public void setData(List<MediaBean> data) {
        List<GlideDrawableContent> sources = new ArrayList<>();
        GlideDrawableContent content;
        for (MediaBean datum : data) {
            content = new GlideDrawableContent();
            content.width = 480;
            content.height = 640;
            content.mediaBean = datum;
            sources.add(content);
            //sources.add(ImageSource.uri(datum.getContentUri(), datum.width, datum.height, 0, datum.mimeType));
        }
        drawContents = sources;
    }


    public void showIndex(int index) {
        this.mLargeImageRecycle.setVisibility(View.VISIBLE);
        bigImageViewController.updateMatrix();
        provider.index = index;
        bigImageViewController.enable();
    }

    public void hide() {
        this.mLargeImageRecycle.setVisibility(View.GONE);
        bigImageViewController.disable();
    }

    public boolean isShow() {
        return this.mLargeImageRecycle.getVisibility() == View.VISIBLE;
    }

    private class DrawContentProvider extends BigImageViewController.DrawContentProvider {
        int index = 0;
        @Override
        public boolean hasPreview() {
            return drawContents != null && drawContents.size() > 0 && index > 0;
        }

        @Override
        public boolean hasNext() {
            return drawContents != null && drawContents.size() > 0 && index < (drawContents.size() -1);
        }

        public DrawContent getContentByIndex(int index) {
            if (drawContents == null) {
                return null;
            }
            if (index >= drawContents.size() || index < 0) {
                Log.e("ljl", "getContentByIndex: error" + index);
                return null;
            }
            GlideDrawableContent drawContent = drawContents.get(index);
            if (drawContent.getContent() == null) {
                drawContent.setTarget(new BigImageTarget<Drawable>(480, 480));
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (drawContent.target != null) {
                            GlideApp.with(activity).load(drawContent.mediaBean.getContentUri()).into(drawContent.target);
                        }
                    }
                });
            }
            return drawContents.get(index);
        }

        @Override
        public DrawContent getContentByOffset(int offset) {
            return getContentByIndex(this.index + offset);
        }


        @Override
        public DrawContent getCurrentDrawContent() {
            return getContentByIndex(index);
        }

        @Override
        public DrawContent getPreDrawContent(int offset) {
            offset += index;
            return getContentByIndex(offset);
        }

        @Override
        public DrawContent getNextDrawContent(int offset) {
            offset += index;
            return getContentByIndex(offset);
        }

        @Override
        public boolean switchToPre() {
            if (index > 0) {
                // cancel the glide target
                int t = index + 3;
                if (t < drawContents.size() && drawContents.get(t).target != null) {
                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            GlideApp.with(activity).clear(drawContents.get(t).target);
                            drawContents.get(t).target = null;
                            drawContents.get(t).setContent(null);
                        }
                    });
                }
                index--;
                return true;
            }
            return false;
        }

        @Override
        public boolean switchToNext() {
            if ( index == (drawContents.size() - 1)) {
                return false;
            }
            // cancel the glide target
            int t = index - 3;
            if (t >= 0 && t < drawContents.size() && drawContents.get(t).target != null) {
                GlideApp.with(activity).clear(drawContents.get(t).target);
                drawContents.get(t).target = null;
                drawContents.get(t).setContent(null);
            }

            index ++;
            return true;
        }
    }

    private class BigImageTarget<T extends Drawable> extends SimpleTarget<T> {

        public GlideDrawableContent content;

        public BigImageTarget(int width, int height) {
            super(width, height);
        }

        @Override
        public void onResourceReady(T resource, Transition<? super T> transition) {
            content.setContent(resource);
            provider.updateContent();
            if (content == provider.getCurrentDrawContent()) {
                ((BaseGalleryActivity) activity).show.setImageDrawable(resource);
            }
        }
    }

    public static class GlideDrawableContent extends DrawContent {
        private BigImageTarget target;
        public MediaBean mediaBean;

        public GlideDrawableContent() {
        }

        public void setTarget(BigImageTarget target) {
            this.target = target;
            target.content = this;
        }
    }

}
