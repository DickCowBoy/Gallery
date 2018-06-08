package com.tplink.gallery.ui;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.View;

import com.android.gallery3d.app.OrientationManager;
import com.android.gallery3d.glrenderer.GLHost;
import com.android.gallery3d.ui.BitmapScreenNail;
import com.android.gallery3d.ui.GLRoot;
import com.android.gallery3d.ui.GLRootView;
import com.android.gallery3d.ui.GLView;
import com.android.gallery3d.ui.PhotoView;
import com.android.gallery3d.ui.ScreenNail;
import com.android.gallery3d.ui.TiledScreenNail;
import com.android.gallery3d.util.Const;
import com.bumptech.glide.request.transition.Transition;
import com.tplink.gallery.GlideApp;
import com.tplink.gallery.bean.MediaBean;
import com.tplink.utils.NoneBoundArrayList;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class BigImagePreviewGLView implements GLHost {

    public static final String TAG = "BigImagePreviewGLView";

    private GLRootView mGLRootView;
    private Activity activity;
    protected PhotoView mPhotoView;
    private List<MediaBean> data;
    private boolean canSwitch;
    private OrientationManager mOrientationManager;
    private PhotoViewListener photoViewListener;
    private PhotoAdapter photoAdapter;
    private DataListener dataListener;

    private boolean hasAdd = false;

    private final GLView mRootPane = new GLView() {
        @Override
        protected void onLayout(
                boolean changed, int left, int top, int right, int bottom) {
            mPhotoView.layout(0, 0, right - left, bottom - top);
        }
    };


    public void setDataListener(DataListener dataListener) {
        this.dataListener = dataListener;
    }

    public BigImagePreviewGLView(GLRootView mGLRootView, Activity activity, boolean canSwitch) {
        this.mGLRootView = mGLRootView;
        this.activity = activity;
        this.canSwitch = canSwitch;
    }


    public boolean isShow() {
        return mGLRootView.getVisibility() == View.VISIBLE;
    }

    public void hide() {
        mGLRootView.setVisibility(View.GONE);
        photoAdapter.hide();
    }

    public void setData(List data) {
        this.data = data;
    }

    //TODO LJL
    public void showIndex(int index) {
        mGLRootView.setVisibility(View.VISIBLE);
        mGLRootView.requestLayoutContentPane();
        photoAdapter.showImageIndex(index);
    }

    @Override
    public Context getContext() {
        return activity;
    }

    @Override
    public GLRoot getGLRoot() {
        return mGLRootView;
    }

    public void onCreate() {
        mPhotoView = new PhotoView(this, canSwitch);
        photoViewListener = new PhotoViewListener();
        mPhotoView.setListener(photoViewListener);
        mRootPane.addComponent(mPhotoView);
        photoAdapter = new PhotoAdapter();
        mPhotoView.setModel(photoAdapter);
        mOrientationManager = new OrientationManager(activity);
        mGLRootView.setOrientationSource(mOrientationManager);
    }

    public void onPause() {
        mPhotoView.pause();
        mOrientationManager.pause();
    }

    public void onStart() {

        if (hasAdd) { //当页面第一次加载或执行过onStop()，才执行后面的方法
            return;
        }
        hasAdd = true;
        mGLRootView.freeze(Const.UNFREEZE_GLROOT_TIMEOUT);
        mGLRootView.setContentPane(mRootPane);
    }

    public void onResume() {
        mOrientationManager.resume();
        mPhotoView.resume();
    }

    public void onDestroy() {
        mGLRootView.setOrientationSource(null);
    }

    public class PhotoViewListener implements PhotoView.Listener {

        @Override
        public void onSingleTapUp(int x, int y) {

        }

        @Override
        public void onActionBarAllowed(boolean allowed) {

        }

        @Override
        public void onActionBarWanted() {

        }

        @Override
        public void onCurrentImageUpdated() {

        }

        @Override
        public void onDeleteImage(MediaBean path, int offset) {

        }

        @Override
        public void onUndoDeleteImage() {

        }

        @Override
        public void onCommitDeleteImage() {

        }

        @Override
        public void onFilmModeChanged(boolean enabled) {

        }

        @Override
        public void onPictureCenter(boolean isCamera) {

        }

        @Override
        public void onUndoBarVisibilityChanged(boolean visible) {

        }

        @Override
        public void onJoinTransparent() {

        }
    }

    public interface DataListener {
        void onPhotoChanged(int index, MediaBean item);
    }


    private class PhotoAdapter implements PhotoView.Model {

        private static final int SCREEN_NAIL_MAX = PhotoView.SCREEN_NAIL_MAX;
        private static final int IMAGE_CACHE_SIZE = 2 * SCREEN_NAIL_MAX + 1;
        private int mCurrentIndex;
        private final MediaBean mMedias[] = new MediaBean[IMAGE_CACHE_SIZE];
        private final BigScreenTarget<MediaBean> bigScreenTargets[] = new BigScreenTarget[IMAGE_CACHE_SIZE];
        private Map<MediaBean, ScreenNail> screenNails = new HashMap();

        public PhotoAdapter() {
            for (int i = 0; i < bigScreenTargets.length; i++) {
                // TODO Ljl
                bigScreenTargets[i] = new BigScreenTarget<MediaBean>(720, 720){

                    @Override
                    public void onResourceReady(Drawable resource, Transition<? super Drawable> transition) {
                        int mediaIndex = findMediaIndex(getTag());
                        if (mediaIndex != -1) {
                            ScreenNail screenNail = screenNails.get(getTag());
                            if (screenNail instanceof TiledScreenNail) {
                                screenNail = ((TiledScreenNail) screenNail).combine(new BitmapScreenNail(((BitmapDrawable) resource).getBitmap()));
                                screenNails.put(getTag(), screenNail);
                            } else {
                                screenNails.put(getTag(), new BitmapScreenNail(((BitmapDrawable) resource).getBitmap()));
                            }
                            mPhotoView.notifyImageChange(mediaIndex - SCREEN_NAIL_MAX);
                        }
                    }
                };
            }
        }

        private int findMediaIndex(MediaBean tag) {
            for (int i = 0; i < mMedias.length; i++) {
                if (tag.equals(mMedias[i])) return i;
            }
            return  -1;
        }

        @Override
        public int getCurrentIndex() {
            return mCurrentIndex;
        }

        @Override
        public void moveTo(int index) {
            showIndex(index);
        }

        @Override
        public void getImageSize(int offset, PhotoView.Size size) {

        }

        @Override
        public MediaBean getMediaItem(int offset) {
            return null;
        }

        @Override
        public int getImageRotation(int offset) {
            return 0;
        }

        @Override
        public void setNeedFullImage(boolean enabled) {

        }

        @Override
        public boolean isCamera(int offset) {
            return false;
        }

        @Override
        public boolean isPanorama(int offset) {
            return false;
        }

        @Override
        public boolean isStaticCamera(int offset) {
            return false;
        }

        @Override
        public boolean isVideo(int offset) {
            return false;
        }

        @Override
        public boolean isBurstPhoto(int offset) {
            return false;
        }

        @Override
        public boolean isGifImage(int offset) {
            return false;
        }

        @Override
        public boolean isDeletable(int offset) {
            return false;
        }

        @Override
        public int getLoadingState(int offset) {
            return 0;
        }

        @Override
        public void setFocusHintDirection(int direction) {

        }

        @Override
        public void setFocusHintPath(MediaBean path) {

        }

        @Override
        public boolean hasNext() {
            if (data == null) return false;
            return mCurrentIndex < (data.size() - 1);
        }

        @Override
        public boolean hasPre() {
            if (data == null) return false;
            return mCurrentIndex > 0;
        }

        @Override
        public int getLevelCount() {
            return 0;
        }

        @Override
        public ScreenNail getScreenNail() {
           return getScreenNail(0);
        }

        private ScreenNail newPlaceholderScreenNail(MediaBean item) {
            int width = item.width;
            int height = item.height;
            return new TiledScreenNail(width, height);
        }

        @Override
        public ScreenNail getScreenNail(int offset) {
            if (data == null) {
                return null;
            }
            MediaBean mediaBean = data.get(mCurrentIndex + offset);
            if (mediaBean == null) return null;
            ScreenNail screenNail = screenNails.get(mediaBean);
            if (screenNail == null) {
                screenNail = newPlaceholderScreenNail(mediaBean);
                screenNails.put(mediaBean, screenNail);
            }
            return screenNail;
        }

        @Override
        public int getImageWidth() {
            if (data == null) return 0;
            MediaBean mediaBean = data.get(mCurrentIndex);
            if (mediaBean == null) return 0;
            if (mediaBean.orientation % 180 != 0) {
                return data.get(mCurrentIndex).height;
            }
            return mediaBean.width;
        }

        @Override
        public int getImageHeight() {
            if (data == null) return 0;
            MediaBean mediaBean = data.get(mCurrentIndex);
            if (mediaBean == null) return 0;
            if (mediaBean.orientation % 180 != 0) {
                return data.get(mCurrentIndex).width;
            }
            return mediaBean.height;
        }

        @Override
        public Bitmap getTile(int level, int x, int y, int tileSize) {
            return null;
        }

        public void hide() {
            for (int i = 0; i < mMedias.length; i++) {
                mMedias[i] = null;
            }
            for (BigScreenTarget<MediaBean> bigScreenTarget : bigScreenTargets) {
                GlideApp.with(activity).clear(bigScreenTarget);
            }
            screenNails.clear();
        }

        public void showImageIndex(int index) {
            mCurrentIndex = index;
            if (dataListener != null) {
                dataListener.onPhotoChanged(mCurrentIndex, data.get(mCurrentIndex));
            }
            int fromIndex[] = new int[IMAGE_CACHE_SIZE];
            MediaBean oldMedias[] = new MediaBean[IMAGE_CACHE_SIZE];
            System.arraycopy(mMedias, 0, oldMedias, 0, IMAGE_CACHE_SIZE);
            // Update the mPaths array.
            for (int i = 0, pos = 0; i < IMAGE_CACHE_SIZE; ++i) {
                pos = mCurrentIndex + i - SCREEN_NAIL_MAX;
                mMedias[i] = data.get(pos);
            }

            // Calculate the fromIndex array.
            for (int i = 0; i < IMAGE_CACHE_SIZE; i++) {
                MediaBean p = mMedias[i];
                if (p == null) {
                    fromIndex[i] = Integer.MAX_VALUE;
                    continue;
                }

                // Try to find the same path in the old array
                int j;
                for (j = 0; j < IMAGE_CACHE_SIZE; j++) {
                    if (oldMedias[j] == p) {
                        break;
                    }
                }
                fromIndex[i] = (j < IMAGE_CACHE_SIZE) ? j - SCREEN_NAIL_MAX : Integer.MAX_VALUE;
            }
            mPhotoView.notifyDataChange(fromIndex, -index, data.size() - index - 1);
            loadScreenNails();
        }

        private void loadScreenNails() {
            List<MediaBean> notLoad = new NoneBoundArrayList<>();
            for (MediaBean mediaBean : mMedias) {
                if (mediaBean != null) {
                    notLoad.add(mediaBean);
                }
            }
            Set<MediaBean> mediaBeans = new HashSet<>();
            mediaBeans.addAll(screenNails.keySet());
            mediaBeans.removeAll(notLoad);
            ScreenNail remove;
            for (MediaBean mediaBean : mediaBeans) {
                remove = screenNails.get(mediaBean);
                if (remove != null) {
                    screenNails.remove(remove);
                    remove.recycle();
                }
            }
            List<BigScreenTarget<MediaBean>> freeTarget = new NoneBoundArrayList<>();
            MediaBean tag;
            for (BigScreenTarget<MediaBean> bigScreenTarget : bigScreenTargets) {
                tag = bigScreenTarget.getTag();
                if (tag == null) {
                    freeTarget.add(bigScreenTarget);
                } else if (!notLoad.remove(tag)) {
                    freeTarget.add(bigScreenTarget);
                }
            }
            BigScreenTarget<MediaBean> freeRequest;
            for (MediaBean mediaBean : notLoad) {
                freeRequest = freeTarget.remove(0);
                if (freeRequest != null) {
                    Log.e(TAG, "loadScreenNails: " + mediaBean.toString());
                    freeRequest.setTag(mediaBean);
                    GlideApp.with(activity).load(mediaBean.getContentUri()).into(freeRequest);
                }
            }
        }
    }

    public MediaBean getCurrentBean() {
        return data.get(photoAdapter.getCurrentIndex());
    }


    public int getCurrentIndex() {
        return photoAdapter.getCurrentIndex();
    }
}
