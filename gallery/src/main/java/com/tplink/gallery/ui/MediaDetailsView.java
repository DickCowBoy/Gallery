/*
 * Copyright (C) 2016, TP-LINK TECHNOLOGIES CO., LTD.
 *
 * MediaDetailsView.java
 *
 * Description
 *
 * Author caixinhai
 *
 * Ver 1.0, 17-3-16, caixinhai, Create file
 */
package com.tplink.gallery.ui;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.gallery3d.data.MediaDetails;
import com.android.gallery3d.ui.DetailsHelper;
import com.android.gallery3d.util.GalleryUtils;
import com.tplink.gallery.R;
import com.tplink.gallery.bean.MediaBean;
import com.tplink.gallery.view.AutoFitRelative;
import com.tplink.gallery.view.FixSizeScrollView;

import java.util.Map;
import java.util.Set;

/**
 * 媒体详情信息View
 */
public class MediaDetailsView extends RelativeLayout implements AutoFitRelative.SystemRectFit {

    @Override
    public void onNewSystemRect(Rect rect, LayoutParams layoutParams) {
        if (layoutParams != null) {
            layoutParams.leftMargin = rect.left;
            layoutParams.rightMargin = rect.right;
            layoutParams.bottomMargin = rect.bottom;
        }
    }

    public interface MediaDetailsViewDelegate{
        public boolean isShowingMediaDtails();
    }

    public interface MediaDetailViewListener{
        public void onContentOutsideClicked();
    }

    private static final int SHOW_HIDE_ANIM_DURATION = 200;

    private Context mContext;
    private LayoutInflater mLayoutInflater;
    private ViewGroup mParentContainer;

    private LinearLayout mContainer;
    private FixSizeScrollView mDetailScrollView;

    private LocalMediaDetailLoader mLoaderTask;

    private boolean mIsShowing = false;

    private int titleColor;
    private int itemColor;


    private MediaDetailViewListener mMediaDetailViewListener;

    public MediaDetailsView(Context context, ViewGroup parentContainer) {
        super(context);
        this.mContext = context;
        this.mParentContainer = parentContainer;
        mLayoutInflater = LayoutInflater.from(mContext);

        mDetailScrollView = (FixSizeScrollView) mLayoutInflater.inflate(R.layout.layout_media_details_view, null);
        LayoutParams layoutParams =
                new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE);
        addView(mDetailScrollView, layoutParams);
        mContainer = (LinearLayout) findViewById(R.id.layout_detail_container);

        setBackground(context.getDrawable(R.drawable.media_detail_gradient_background));
        titleColor = Color.WHITE;
        itemColor = Color.WHITE;
    }

    public void setContentBackgroundColor(int color){
        mContainer.setBackgroundColor(color);
    }

    public void useDiolagLook(){
        setBackground(new ColorDrawable(Color.TRANSPARENT));
        setContentBackgroundColor(Color.WHITE);
        titleColor = getResources().getColor(R.color.black_87_alpha);
        itemColor = getResources().getColor(R.color.black_54_alpha);
    }

    public void show(Context context, MediaBean mediaBean) {
        mLoaderTask = new LocalMediaDetailLoader(context);
        mLoaderTask.execute(mediaBean);
        if (!mIsShowing) {
            Animator anim = ObjectAnimator.ofFloat(this, "alpha", 0f, 1f);
            anim.setDuration(SHOW_HIDE_ANIM_DURATION);
            anim.start();
            addToContainer();
            mContainer.setVisibility(INVISIBLE);
            mIsShowing = true;
        }
    }

    public void hide() {
        mLoaderTask.cancel(true);
        mIsShowing = false;
        Animator anim = ObjectAnimator.ofFloat(this, "alpha", 1f, 0f);
        anim.setDuration(SHOW_HIDE_ANIM_DURATION);
        anim.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                removeFromContainer();
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                removeFromContainer();
            }

            @Override
            public void onAnimationRepeat(Animator animation) {
            }
        });
        anim.start();
    }

    public boolean isShowing() {
        return mIsShowing;
    }

    public void toggle(MediaBean mediaBean) {
        if (mIsShowing) {
            hide();
        } else {
            show(getContext().getApplicationContext(), mediaBean);
        }
    }

    private void addToContainer() {
        LayoutParams layoutParams = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        mParentContainer.addView(this, layoutParams);
    }

    private void removeFromContainer() {
        ViewGroup parent = (ViewGroup) getParent();
        parent.removeView(this);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_UP: {
                Rect rect = new Rect();
                mDetailScrollView.getHitRect(rect);
                if (!rect.contains((int) event.getX(),(int) event.getY())){
                    if(mMediaDetailViewListener != null){
                        mMediaDetailViewListener.onContentOutsideClicked();
                    }
                }
            }
        }
        return true;
    }

    private class LocalMediaDetailLoader extends AsyncTask<MediaBean, Void, MediaDetails> {

        private Context context;

        public LocalMediaDetailLoader(Context context) {
            this.context = context;
        }

        @Override
        protected MediaDetails doInBackground(MediaBean... params) {
            if (params.length <= 0) {
                return null;
            }
            MediaBean object = params[0];
            if (object == null) {
                return null;
            }

            return object.getDetails(context);
        }

        @Override
        protected void onPostExecute(MediaDetails details) {
            if (details == null) {
                return;
            }
            if (isCancelled()) {
                return;
            }

            updateView(details);
        }
    }

    private void updateView(MediaDetails details) {

        mContainer.removeAllViews();
        Map<String, Set<String>> groupedDetails = DetailsHelper.groupMediaDetails(mContext, details);

        // 时间
        Set<String> dateSet = groupedDetails.get(DetailsHelper.GROUP_DATE);
        View groupView = createGroupView1(R.string.time, dateSet);
        if (groupView != null) {
            mContainer.addView(groupView);
        }

        // 拍摄
        Set<String> shotSet = groupedDetails.get(DetailsHelper.GROUP_SHOT);
        groupView = createGroupView1(R.string.photos, shotSet);
        if (groupView != null) {
            mContainer.addView(groupView);
        }

        // 更多
        Set<String> moreSet = groupedDetails.get(DetailsHelper.GROUP_MORE);
        groupView = createGroupView2(R.string.more, moreSet);
        if (groupView != null) {
            mContainer.addView(groupView);
        }

        // 播放内容由下往上的动画效果
        Animator anim = ObjectAnimator.ofFloat(mContainer, "translationY", GalleryUtils.dpToPixel(300), 0f);
        anim.setDuration(SHOW_HIDE_ANIM_DURATION);
        anim.start();

        mContainer.setVisibility(VISIBLE);
    }

    private View createGroupView1(int titleResId, Set<String> itemSet) {
        if (itemSet == null || itemSet.size() <= 0) {
            return null;
        }
        View groupView;
        TextView titleTv;
        ViewGroup itemContainer;
        TextView itemTv;
        LayoutInflater layoutInflater = LayoutInflater.from(mContext);
        groupView = layoutInflater.inflate(R.layout.media_detail_group_1, null);
        titleTv = (TextView) groupView.findViewById(R.id.media_detail_group_title_tv);
        titleTv.setText(titleResId);
        titleTv.setTextColor(titleColor);
        itemContainer = (ViewGroup) groupView.findViewById(R.id.media_detail_group_item_container);
        for (String s : itemSet) {
            itemTv = (TextView) layoutInflater.inflate(R.layout.media_detail_group_1_item, null);
            itemTv.setText(s);
            itemTv.setTextColor(itemColor);
            itemContainer.addView(itemTv);
        }
        return groupView;
    }

    private View createGroupView2(int titleResId, Set<String> itemSet) {
        if (itemSet == null || itemSet.size() <= 0) {
            return null;
        }
        View groupView;
        TextView titleTv;
        ViewGroup itemContainer;
        TextView itemTv;
        LayoutInflater layoutInflater = LayoutInflater.from(mContext);
        groupView = layoutInflater.inflate(R.layout.media_detail_group_2, null);
        titleTv = (TextView) groupView.findViewById(R.id.media_detail_group_title_tv);
        titleTv.setText(titleResId);
        titleTv.setTextColor(titleColor);
        itemContainer = (ViewGroup) groupView.findViewById(R.id.media_detail_group_item_container);
        int index = 0;
        for (String s : itemSet) {
            itemTv = (TextView) layoutInflater.inflate(R.layout.media_detail_group_2_item, null);
            index++;
            if (index == itemSet.size()){
                // 路径：最多两行,超长省略中间
                // 注意设置了maxline则setEllipsize没有效果,textview换行算法
                itemTv.setSingleLine(false);
            }
            itemTv.setText(s);
            itemTv.setTextColor(itemColor);
            itemContainer.addView(itemTv);
        }
        return groupView;
    }

    public void setMediaDetailViewListener(MediaDetailViewListener mediaDetailViewListener) {
        this.mMediaDetailViewListener = mediaDetailViewListener;
    }
}
