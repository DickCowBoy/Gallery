package com.tplink.gallery.preview.camera.burst;
/*
 * Copyright (C), 2016, TP-LINK TECHNOLOGIES CO., LTD.
 *
 * BurstMicroThumbTabProvider.java
 *
 * Author Yu Libo <yulibo@tp-link.net>
 *
 * Ver 1.0, 17-12-21, Yu Libo <yulibo@tp-link.net>, Create file
 */

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.view.PagerAdapter;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;

import com.bumptech.glide.request.target.ImageViewTarget;
import com.tplink.gallery.GlideApp;
import com.tplink.gallery.R;
import com.tplink.gallery.bean.MediaBean;

import java.util.List;

public class BurstImagePreviewAdapter extends PagerAdapter {
    private static final String TAG = "BurstImagePreviewAdapter";

    private final SparseArray<ViewHolder> mItems = new SparseArray<>();
    private final List<MediaBean> mMediaItems;
    private final LayoutInflater mLayoutInflater;
    private BurstImagePreviewControl mControl;
    private Context context;

    public BurstImagePreviewAdapter(Context context, BurstImagePreviewControl control, List<MediaBean> mediaItems) {
        mLayoutInflater = LayoutInflater.from(context);
        mControl = control;
        mMediaItems = mediaItems;
        this.context = context;
    }

    public MediaBean getItemByPosition(int pos) {
        return mMediaItems.get(pos);
    }

    @Override
    public Object instantiateItem(final ViewGroup container, final int position) {
        View view = mLayoutInflater.inflate(R.layout.cover_item, null);
        final ViewHolder viewHolder = new ViewHolder(position, view);
        viewHolder.checkBox.setTag(position);
        // 加载图片
        updateImagePreView(viewHolder, mControl.isImageSelected(position));
        viewHolder.checkBox.setOnCheckedChangeListener((buttonView, isChecked)-> {

            Object tag = buttonView.getTag();
            if (tag != null && tag instanceof Integer) {
                mControl.onCheckedChanged(((Integer) tag), isChecked);
            }
            Drawable drawable = viewHolder.imageView.getDrawable();
            if (drawable != null) {
                if (isChecked) {
                    drawable.setColorFilter(viewHolder.maskColor, PorterDuff.Mode.SRC_OVER);
                } else {
                    drawable.clearColorFilter();
                }
            }
        });
        viewHolder.imageView.setTag(R.id.iv_burst_check, position);
        viewHolder.imageView.setTag(R.id.iv_burst_icon, viewHolder);
        GlideApp.with(context).load(mMediaItems.get(position).getContentUri()).
                into(new ImageViewTarget<Drawable>(viewHolder.imageView) {
            @Override
            protected void setResource(@Nullable Drawable resource) {
                updateImagePreView(resource,
                        (ViewHolder) getView().getTag(R.id.iv_burst_icon),
                        mControl.isImageSelected((Integer) getView().getTag(R.id.iv_burst_check)));
            }
        });

        viewHolder.checkBox.setChecked(mControl.isImageSelected(position));

        viewHolder.imageView.setOnClickListener((v)-> {
            viewHolder.checkBox.setChecked(!viewHolder.checkBox.isChecked());
        });

        container.addView(viewHolder.container);
        mItems.append(position, viewHolder);

        return viewHolder;
    }

    private void updateImagePreView(ViewHolder viewHolder, boolean isChecked) {
        if (viewHolder != null) {
            // 加载图片
            Drawable drawable = viewHolder.imageView.getDrawable();
            if (drawable != null) {
                if (isChecked) {
                    drawable.setColorFilter(viewHolder.maskColor, PorterDuff.Mode.SRC_OVER);
                } else {
                    drawable.clearColorFilter();
                }
            }
        }
    }

    private void updateImagePreView(Drawable drawable, ViewHolder holder, boolean isChecked) {

        // 加载图片
        if (drawable != null) {
            holder.imageView.setImageDrawable(drawable);
        } else {
            holder.imageView.setImageResource(R.color.album_placeholder);
        }
        drawable = holder.imageView.getDrawable();
        if (drawable != null) {
            if (isChecked) {
                drawable.setColorFilter(holder.maskColor, PorterDuff.Mode.SRC_OVER);
            } else {
                drawable.clearColorFilter();
            }
        }

    }

    @Override
    public int getCount() {
        return mMediaItems.size();
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == ((ViewHolder) object).container;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        ViewHolder holder = (ViewHolder) object;
        container.removeView(holder.container);
        mItems.remove(position);
    }

    @Override
    public int getItemPosition(Object object) {
        final ViewHolder holder = (ViewHolder) object;

        return holder.position;
    }

    private static class ViewHolder {
        public final int position;
        public final View container;
        public final CheckBox checkBox;
        public final ImageView imageView;
        public final int maskColor;

        public ViewHolder(int position, View container) {
            this.position = position;
            this.container = container;
            checkBox = container.findViewById(R.id.iv_cover_checked);
            imageView = container.findViewById(R.id.iv_image_cover);
            maskColor = container.getContext().getResources().getColor(R.color.black_30_alpha);
        }
    }

    public interface BurstImagePreviewControl {
        boolean isImageSelected(int position);
        void onCheckedChanged(int position, boolean isChecked);
    }
}
