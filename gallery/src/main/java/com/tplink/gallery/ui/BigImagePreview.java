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
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PagerSnapHelper;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.bumptech.glide.ListPreloader;
import com.bumptech.glide.util.FixedPreloadSizeProvider;
import com.davemorrissey.labs.subscaleview.ImageSource;
import com.tplink.gallery.bean.MediaBean;

import java.util.ArrayList;
import java.util.List;

public class BigImagePreview {

    private final LargeImagedAdapter largeImageAdapter;
    private final FilmCardAdapter mFilmCardAdapter;
    private RecyclerView mLargeImageRecycle;
    private RecyclerView mFilmImageRecycle;

    public BigImagePreview(Context context, RecyclerView mLargeImageRecycle, RecyclerView mFilmImageRecycle) {
        this.mLargeImageRecycle = mLargeImageRecycle;
        this.mFilmImageRecycle = mFilmImageRecycle;
        final LinearLayoutManager linearLayoutManager = new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false);
        final LinearLayoutManager linearLayoutManager2 = new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false);

        mLargeImageRecycle.setLayoutManager(linearLayoutManager);
        mFilmImageRecycle.setLayoutManager(linearLayoutManager2);

        mLargeImageRecycle.addItemDecoration(new SpaceItemDecoration(10));
        mFilmImageRecycle.addItemDecoration(new SpaceFilmItemDecoration(10));

        largeImageAdapter = new LargeImagedAdapter(context, mLargeImageRecycle);
        mFilmCardAdapter = new FilmCardAdapter(context, mFilmImageRecycle);

        mLargeImageRecycle.setAdapter(largeImageAdapter);
        mFilmImageRecycle.setAdapter(mFilmCardAdapter);

        PagerSnapHelper snapHelper = new PagerSnapHelper();
        PagerSnapHelper snapHelper1 = new PagerSnapHelper();
        snapHelper.attachToRecyclerView(mLargeImageRecycle);
        snapHelper1.attachToRecyclerView(mFilmImageRecycle);

    }

    public void setData(List<MediaBean> data) {
        List<ImageSource> sources = new ArrayList<>();
        for (MediaBean datum : data) {
            sources.add(ImageSource.uri(datum.getContentUri(), datum.width, datum.height, 0, datum.mimeType));
        }
        largeImageAdapter.setList(sources);
        largeImageAdapter.notifyDataSetChanged();
        mFilmCardAdapter.setList(sources);
        mFilmCardAdapter.notifyDataSetChanged();
    }






    class SpaceItemDecoration extends RecyclerView.ItemDecoration {
        int mSpace;

        /**
         * Retrieve any offsets for the given item. Each field of <code>outRect</code> specifies
         * the number of pixels that the item view should be inset by, similar to padding or margin.
         * The default implementation sets the bounds of outRect to 0 and returns.
         * <p>
         * <p>
         * If this ItemDecoration does not affect the positioning of item views, it should set
         * all four fields of <code>outRect</code> (left, top, right, bottom) to zero
         * before returning.
         * <p>
         * <p>
         * If you need to access Adapter for additional data, you can call
         * {@link RecyclerView#getChildAdapterPosition(View)} to get the adapter position of the
         * View.
         *
         * @param outRect Rect to receive the output.
         * @param view    The child view to decorate
         * @param parent  RecyclerView this ItemDecoration is decorating
         * @param state   The current state of RecyclerView.
         */
        @Override
        public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
            super.getItemOffsets(outRect, view, parent, state);

            outRect.left = mSpace;
            outRect.right = mSpace;

        }

        public SpaceItemDecoration(int space) {
            this.mSpace = space;
        }
    }

    class SpaceFilmItemDecoration extends RecyclerView.ItemDecoration {
        int mSpace;

        /**
         * Retrieve any offsets for the given item. Each field of <code>outRect</code> specifies
         * the number of pixels that the item view should be inset by, similar to padding or margin.
         * The default implementation sets the bounds of outRect to 0 and returns.
         * <p>
         * <p>
         * If this ItemDecoration does not affect the positioning of item views, it should set
         * all four fields of <code>outRect</code> (left, top, right, bottom) to zero
         * before returning.
         * <p>
         * <p>
         * If you need to access Adapter for additional data, you can call
         * {@link RecyclerView#getChildAdapterPosition(View)} to get the adapter position of the
         * View.
         *
         * @param outRect Rect to receive the output.
         * @param view    The child view to decorate
         * @param parent  RecyclerView this ItemDecoration is decorating
         * @param state   The current state of RecyclerView.
         */
        @Override
        public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
            super.getItemOffsets(outRect, view, parent, state);
            if (parent.getChildAdapterPosition(view) == 0) {
                outRect.left = mLargeImageRecycle.getWidth() / 6;
            } else {
                outRect.left = mSpace;
            }
            if (parent.getChildAdapterPosition(view) != (parent.getAdapter().getItemCount()-1)) {

                outRect.right = mSpace;
            } else {
                outRect.right = mLargeImageRecycle.getWidth() / 6;
            }
//            outRect.bottom = mSpace;
//            if (parent.getChildAdapterPosition(view) == 0) {
//                outRect.top = mSpace;
//            }

        }

        public SpaceFilmItemDecoration(int space) {
            this.mSpace = space;
        }
    }

    public void showIndex(int index) {
        mLargeImageRecycle.scrollToPosition(index);
        this.mLargeImageRecycle.setVisibility(View.VISIBLE);
        this.mFilmImageRecycle.setVisibility(View.GONE);
    }

    public void hide() {
        this.mLargeImageRecycle.setVisibility(View.GONE);
        this.mFilmImageRecycle.setVisibility(View.GONE);
    }

    public boolean isShow() {
        return this.mLargeImageRecycle.getVisibility() == View.VISIBLE;
    }

}
