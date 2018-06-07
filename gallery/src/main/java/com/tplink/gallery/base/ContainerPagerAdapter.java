package com.tplink.gallery.base;
/*
 * Copyright (C) 2018, TP-LINK TECHNOLOGIES CO., LTD.
 *
 * ContainerPagerAdapter.java
 *
 * Author LinJinLong
 *
 * Ver 1.0, 2018-02-12 LinJinLong, Create file
 */

import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.os.Handler;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.support.v13.app.FragmentPagerAdapter;
import android.view.ViewGroup;

import com.tplink.base.CommonUtils;
import com.tplink.base.DragSelectTouchHelper;
import com.tplink.gallery.R;
import com.tplink.gallery.utils.MediaUtils;

import java.util.List;

public class ContainerPagerAdapter extends FragmentPagerAdapter {
    public static final String TAG = "PicPagerAdapter";

    public static final int TAB_NUM = 2;
    public static final int IMAGE_TAB = 0;
    public static final int ALBUM_TAB = 1;

    private final Context mContext;

    private FragmentManager mFragmentManager;
    private int mCurrentPos;
    private boolean awaysInSelectMode;
    private boolean needSelectedAlbum;
    private boolean needImage;
    private boolean needVideo;

    private ImageSlotFragment mMediaFragment = null;
    private AlbumSlotFragment mAlbumFragment = null;


    private List<String> allowMimeTyppes;
    private List<String> notAllowMimeTyppes;
    private boolean needResolveBurst;


    private DragSelectTouchHelper.InterceptController interceptController;

    public ContainerPagerAdapter(FragmentManager fm, Context context,
                                 DragSelectTouchHelper.InterceptController interceptController,
                                 boolean awaysInSelectMode, List<String> allowMimeTyppes,List<String> notAllowMimeTyppes,
                                 boolean needResolveBurst, boolean needImage, boolean needVideo, boolean needSelectedAlbum) {
        super(fm);
        mContext = context;
        mFragmentManager = fm;
        this.interceptController = interceptController;
        this.awaysInSelectMode = awaysInSelectMode;
        this.allowMimeTyppes = allowMimeTyppes;
        this.notAllowMimeTyppes = notAllowMimeTyppes;
        this.needResolveBurst = needResolveBurst;
        this.needVideo = needVideo;
        this.needImage = needImage;
        this.needSelectedAlbum = needSelectedAlbum;
    }

    public void setInterceptController(DragSelectTouchHelper.InterceptController interceptController) {
        this.interceptController = interceptController;
    }

    public @Nullable
    Fragment getCurrentFragment() {
        switch (getPosInDirection(mCurrentPos)) {
            case IMAGE_TAB:
                return mMediaFragment;
            case ALBUM_TAB:
                return mAlbumFragment;
            default:
                return null;
        }
    }

    public int getCurrentPos() {
        return mCurrentPos;
    }


    @Override
    public Fragment getItem(int i) {

        // 保证Fragment的复用
        if (getPosInDirection(i) == ALBUM_TAB) {
            if (mAlbumFragment == null) {
                mAlbumFragment = AlbumSlotFragment.newInstance(needSelectedAlbum,
                        MediaUtils.getAllAlbumKey(allowMimeTyppes, notAllowMimeTyppes, needResolveBurst, needImage, needVideo));
            }
            return mAlbumFragment;
        } else {
            if (mMediaFragment == null) {
                mMediaFragment = ImageSlotFragment.newInstance(awaysInSelectMode,
                        MediaUtils.getAllMediaKey (allowMimeTyppes, notAllowMimeTyppes, needResolveBurst, needImage, needVideo));
            }
            mMediaFragment.setInterceptController(interceptController);
            return mMediaFragment;
        }
    }

    @Override
    public void setPrimaryItem(ViewGroup container, int position, Object object) {
        super.setPrimaryItem(container, position, object);
        mCurrentPos = position;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        if (getPosInDirection(position) == ALBUM_TAB) {
            return mContext.getString(R.string.photo_tab_albums);
        } else {
            return mContext.getString(R.string.photo_tab_all_images);
        }
    }

    @Override
    public int getCount() {
        return TAB_NUM;
    }

    public int getPosInDirection(int position) {
        return CommonUtils.isRtl() ? getCount() - 1 - position : position;
    }

    @Override
    public void restoreState(Parcelable state, ClassLoader loader) {
        // 以fragment的tag作为标记找回并复用
        FragmentState fs = (FragmentState) state;
        mAlbumFragment = (AlbumSlotFragment) mFragmentManager
                .findFragmentByTag(fs.getPicAlbumTag());

        mMediaFragment = (ImageSlotFragment) mFragmentManager
                .findFragmentByTag(fs.getPicAlbumDetailTag());

        mMediaFragment.setInterceptController(interceptController);
        // 仅当语言方向改变时才通知
        if (CommonUtils.isRtl() != fs.isRtl()) {
            new Handler().post(() -> {
                notifyDataSetChanged();
            });
        }
    }

    @Override
    public Parcelable saveState() {
        // 以fragment的tag作为标记存储
        return new FragmentState(
                mAlbumFragment.getTag(),
                mMediaFragment.getTag(),
                CommonUtils.isRtl());
    }

    /**
     * 通知改变时会调用此函数检测位置变化详情
     *
     * @param object 某一个fragment
     * @return fragment的新位置
     * {@link #POSITION_UNCHANGED} fragment位置无改变
     * {@link #POSITION_NONE} fragment已不存在
     */
    @Override
    public int getItemPosition(Object object) {
        if (object == mAlbumFragment) {
            return getPosInDirection(ALBUM_TAB);
        }

        if (object == mMediaFragment) {
            return getPosInDirection(IMAGE_TAB);
        }

        return POSITION_NONE;
    }

    @Override
    public long getItemId(int position) {
        // 重写此函数以防止因为位置改变造成的显示错乱
        return getPosInDirection(position);
    }

    public interface OnTabSwitchedListener {
        void onTabSwitched(int position);
    }

    public ImageSlotFragment getAllFragment() {
        return mMediaFragment;
    }

    public AlbumSlotFragment getAlbumFragment() {
        return mAlbumFragment;
    }

    private static class FragmentState implements Parcelable {
        private final String mPicAlbumTag;
        private final String mPicAlbumDetailTag;
        private final boolean mIsRtl;

        private FragmentState(String picAlbumTag, String picAlbumDetailTag,
                              boolean isRtl) {
            mPicAlbumTag = picAlbumTag;
            mPicAlbumDetailTag = picAlbumDetailTag;
            mIsRtl = isRtl;
        }

        private String getPicAlbumTag() {
            return mPicAlbumTag;
        }

        private String getPicAlbumDetailTag() {
            return mPicAlbumDetailTag;
        }

        public boolean isRtl() {
            return mIsRtl;
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeString(mPicAlbumTag);
            dest.writeString(mPicAlbumDetailTag);
            dest.writeInt(mIsRtl ? 1 : 0);
        }

        public static final Parcelable.Creator<FragmentState> CREATOR =
                new Parcelable.Creator<FragmentState>() {
                    @Override
                    public FragmentState createFromParcel(Parcel source) {
                        return new FragmentState(
                                source.readString(), source.readString(),
                                source.readInt() == 1);
                    }

                    @Override
                    public FragmentState[] newArray(int size) {
                        return new FragmentState[size];
                    }
                };
    }


}