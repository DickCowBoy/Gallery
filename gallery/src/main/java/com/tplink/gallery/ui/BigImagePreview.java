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

import android.content.Context;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PagerSnapHelper;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import com.bumptech.glide.ListPreloader;
import com.bumptech.glide.util.FixedPreloadSizeProvider;
import com.davemorrissey.labs.subscaleview.ImageSource;
import com.tplink.gallery.bean.MediaBean;
import com.tplink.gallery.gallery.R;
import com.tplink.gallery.view.BigImageViewController;
import com.tplink.gallery.view.DrawContent;
import com.tplink.gallery.view.GalleryTextureView;

import java.util.ArrayList;
import java.util.List;

public class BigImagePreview {

    private GalleryTextureView mLargeImageRecycle;
    BigImageViewController bigImageViewController;
    BigImageViewController.DrawContentProvider provider;

    private List<DrawContent> drawContents;

    public BigImagePreview(Context context, GalleryTextureView mLargeImageRecycle) {
        this.mLargeImageRecycle = mLargeImageRecycle;

        provider = new BigImageViewController.DrawContentProvider() {
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
                return drawContents.get(index);
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
                index ++;
                return true;
            }
        };
        mLargeImageRecycle.setViewController(bigImageViewController);
        bigImageViewController = new BigImageViewController(mLargeImageRecycle, provider);
        mLargeImageRecycle.setViewController(bigImageViewController);
    }

    public void setData(List<MediaBean> data) {
        List<DrawContent> sources = new ArrayList<>();
        DrawContent content;
        for (MediaBean datum : data) {
            content = new DrawContent();
            content.width = datum.width;
            content.height = datum.height;
            sources.add(content);
            //sources.add(ImageSource.uri(datum.getContentUri(), datum.width, datum.height, 0, datum.mimeType));
        }
        drawContents = sources;
    }


    public void showIndex(int index) {
        this.mLargeImageRecycle.setVisibility(View.VISIBLE);
        bigImageViewController.enable();

    }

    public void hide() {
        this.mLargeImageRecycle.setVisibility(View.GONE);
        bigImageViewController.disable();
    }

    public boolean isShow() {
        return this.mLargeImageRecycle.getVisibility() == View.VISIBLE;
    }
}
