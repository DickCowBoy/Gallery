/*
 * Copyright (C), 2016, TP-LINK TECHNOLOGIES CO., LTD.
 *
 * BurstMicroThumbTabProvider.java
 *
 * Author Yu Libo <yulibo@tp-link.net>
 *
 * Ver 1.0, 17-12-21, Yu Libo <yulibo@tp-link.net>, Create file
 */
package com.tplink.gallery.preview.camera.burst;

import android.content.Context;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Message;
import android.support.v4.view.PagerAdapter;
import android.util.SparseArray;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.tplink.gallery.GlideApp;
import com.tplink.gallery.R;
import com.tplink.gallery.bean.MediaBean;

public class BurstMicroThumbTabProvider implements SmartTabLayout.TabProvider {
    private static final int MSG_UPDATE_CHECKED = 2;

    private Context mContext;
    private SparseArray<ViewHolder> mTabView = new SparseArray<>();
    private Handler mNotifyHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_UPDATE_CHECKED: {
                    ViewHolder holder = mTabView.get(msg.arg1);
                    if (holder != null) {
                        holder.burstCheck.setVisibility(msg.arg2 == 1 ? View.VISIBLE : View.GONE);
                        Drawable drawable = holder.imgIndicator.getDrawable();
                        if (msg.arg2 == 1) {
                            drawable.setColorFilter(holder.maskColor, PorterDuff.Mode.SRC_OVER);
                        } else {
                            drawable.clearColorFilter();
                        }
                    }
                }
            }
        }
    };

    public BurstMicroThumbTabProvider(Context context) {
        mContext = context;
    }

    @Override
    public View createTabView(ViewGroup container, int position, PagerAdapter adapter) {
        ViewHolder holder = mTabView.get(position);
        if (holder == null) {
            holder = new ViewHolder(View.inflate(mContext, R.layout.cover_indicate_item, null));
            mTabView.append(position, holder);
            MediaBean item = ((BurstImagePreviewAdapter) adapter).getItemByPosition(position);
            GlideApp.with(mContext).load(item.getContentUri()).into(holder.imgIndicator);
        }

        return holder.container;
    }

    public void updateChecked(int position, boolean check) {
        mNotifyHandler.obtainMessage(MSG_UPDATE_CHECKED, position, check ? 1 : 0).sendToTarget();
    }

    private static class ViewHolder {
        final View container;
        final ImageView imgIndicator;
        final ImageView burstCheck;
        final int maskColor;

        public ViewHolder(View container) {
            this.container = container;
            imgIndicator = container.findViewById(R.id.iv_indicator);
            burstCheck = container.findViewById(R.id.iv_burst_check);
            maskColor = container.getContext().getResources().getColor(R.color.black_30_alpha);
        }
    }
}
