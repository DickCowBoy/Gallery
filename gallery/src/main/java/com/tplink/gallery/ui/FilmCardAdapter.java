/*
 * Copyright (C) 2018, TP-LINK TECHNOLOGIES CO., LTD.
 *
 * FilmCardAdapter.java
 *
 * Description
 *
 * Author LinJinLong
 *
 * Ver 1.0, 2018-04-20 LinJinLong, Create file
 */
package com.tplink.gallery.ui;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.davemorrissey.labs.subscaleview.ImageSource;
import com.tplink.gallery.GlideApp;
import com.tplink.gallery.gallery.R;


import java.util.ArrayList;
import java.util.List;

public class FilmCardAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private final RecyclerView mView;
    private List<ImageSource> mList = new ArrayList<>();
    private Context context;
    public FilmCardAdapter(Context context, RecyclerView view) {
        this.context = context;
        this.mView = view;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.view_card_item1, parent, false);
        onCreateViewHolder(parent, itemView);
        return new ViewHolderImage(itemView);
    }

    public void setList(List<ImageSource> mList) {
        this.mList = mList;
        notifyDataSetChanged();
    }

    private ImageSource tt = null;
    private int index;

    public void onItemDismiss(int i) {
        tt = mList.remove(i);
        index = i;
        notifyItemRemoved(i);
    }

    public void restore() {
        if (tt != null) {
            mList.add(index, tt);
            notifyItemInserted(index);
        }
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, final int position) {
        // mCardAdapterHelper.onBindViewHolder(holder.itemView, position, getItemCount());/

        ViewHolderImage holderimg = (ViewHolderImage) holder;

        onCreateHalfHolder(mView, holderimg.itemView);

        holderimg.itemView.requestLayout();
        GlideApp.with(context).load(mList.get(position).getUri()).into(holderimg.mImageView);


    }

    @Override
    public int getItemCount() {
        return mList.size();
    }


    public class ViewHolderImage extends RecyclerView.ViewHolder {
        public final ImageView mImageView;
        public ViewHolderImage(final View itemView) {
            super(itemView);
            mImageView = (ImageView) itemView.findViewById(R.id.imageView);
            //mSubView = (SubsamplingScaleImageView) itemView.findViewById(R.id.subimageView);
        }

    }


    public void onCreateViewHolder(ViewGroup parent,  View itemView) {
        RecyclerView.LayoutParams lp = (RecyclerView.LayoutParams) itemView.getLayoutParams();
        lp.width = parent.getWidth();// - dip2px(itemView.getContext(), 2 * (mPagePadding + mShowLeftCardWidth));
        lp.height = parent.getHeight();
        lp.topMargin = 0;
        itemView.setLayoutParams(lp);
    }

    public void onCreateHalfHolder(ViewGroup parent,  View itemView) {
        RecyclerView.LayoutParams lp = (RecyclerView.LayoutParams) itemView.getLayoutParams();
        lp.width = parent.getWidth() * 2 / 3;// - dip2px(itemView.getContext(), 2 * (mPagePadding + mShowLeftCardWidth));
        lp.height = parent.getHeight() * 2 / 3;
        lp.topMargin = parent.getHeight() / 6;
        itemView.setLayoutParams(lp);
    }
}