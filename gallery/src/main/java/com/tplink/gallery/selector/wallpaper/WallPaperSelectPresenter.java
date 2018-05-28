package com.tplink.gallery.selector.wallpaper;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;

import com.tplink.gallery.bean.AlbumBean;
import com.tplink.gallery.bean.MediaBean;
import com.tplink.gallery.dao.MediaDao;
import com.tplink.gallery.gallery.R;
import com.tplink.gallery.selector.MediaSelectorContract;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import io.reactivex.FlowableEmitter;
import io.reactivex.FlowableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subscribers.DisposableSubscriber;

public class WallPaperSelectPresenter extends MediaSelectorContract.MediaSelectorPresenter {

    public static final int WALLPAPER_COUNT_LIMIT = 500;

    private MediaDao mediaDao;

    protected ResultContainer mContainer = new ResultContainer();

    private Context context;

    private boolean isLoading = false;


    public WallPaperSelectPresenter(Context context, MediaSelectorContract.MediaSelectorView view) {
        super(view);
        this.context = context;
        mediaDao = new MediaDao(context);
    }

    @Override
    public void resume() {}

    @Override
    public void pause() {}

    @Override
    public int getMaxSelectCount() {
        return WALLPAPER_COUNT_LIMIT;
    }

    @Override
    public boolean needSelectAlbum() {
        return true;
    }

    @Override
    public boolean needPreview() {
        return true;
    }

    @Override
    public boolean addSingleMedia(MediaBean bean) {
        int result = mContainer.addItem(bean);
        if (result != 0) {
            if (mView != null) {
                mView.showErrorMsg(context.getString(R.string.select_count_over));
            }
        }
        return result == 0;
    }

    @Override
    public boolean isFirstLoading() {
        return isLoading;
    }

    @Override
    public void removeSingleMedia(MediaBean bean) {
        mContainer.delItem(bean);
    }

    @Override
    public boolean addAlbumMedia(long bucketId, List<MediaBean> datas) {
        int result = mContainer.addBucketItems(bucketId, datas);
        if (result != 0) {
            if (mView != null) {
                mView.showErrorMsg(context.getString(R.string.select_count_over));
            }
        }
        return result == 0;
    }

    @Override
    public void delAlbumMedia(long bucketId) {
        mContainer.delBucketItems(bucketId);
    }

    @Override
    public List<MediaBean> getSelectMedia() {
        return mContainer.getMediaEntries();
    }

    @Override
    public Set<MediaBean> getSelectBucketMedia(long bucketId) {
        if (bucketId == -1) {
            Set<MediaBean> ret = new HashSet<>();
            ret.addAll(mContainer.getMediaEntries());
            return ret;
        }
        return mContainer.getSelectBucketItems(bucketId);
    }


    @Override
    public boolean isBucketSelected(long bucketId) {
        Set<MediaBean> selectBucketItems = mContainer.getSelectBucketItems(bucketId);
        return selectBucketItems != null && mContainer.getSelectBucketItems(bucketId).size() > 0;
    }

    @Override
    public void initDataByAllMedia(List<MediaBean> datas, long version) {

    }

    @Override
    public void initDataByAlbums(List<AlbumBean> datas, long version) {

    }

    @Override
    public boolean needImage() {
        return true;
    }

    @Override
    public boolean needVideo() {
        return false;
    }

    @Override
    public void setResult(Activity activity) {
        // 删除不必要内容
        Flowable.create(new FlowableOnSubscribe<Integer>() {
            @Override
            public void subscribe(
                    @NonNull FlowableEmitter<Integer> flowableEmitter)
                    throws Exception {
                // 如何处理壁纸ID存在但实际图片不存在？ 查询id in (),对比删除不存在内容,
                // 是否更轮播数值？暂时不更新,由壁纸自己是现
                // 获取所有ID
                Set<Integer> newItems = new HashSet<>();
                Set<Integer> delItems = new HashSet<>();
                mContainer.getResult(newItems, delItems);
                if (newItems.size() > 0) {
                    WallPaperDao.addWallPaper(activity, newItems);
                }
                if (delItems.size() > 0) {
                    WallPaperDao.delWallPaper(activity, delItems);
                }
                // 剩下的allWallPaper未不存在内容，此处可以进行删除冗余数据
                flowableEmitter.onNext(1);
                flowableEmitter.onComplete();
            }
        }, BackpressureStrategy.LATEST)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new DisposableSubscriber<Integer>() {
                    @Override
                    public void onNext(Integer path) {
                        if (mView != null && mView.isActive()) {
                            mView.showSetResultFinished();
                        }
                    }

                    @Override
                    public void onError(Throwable throwable) {
                        if (mView != null && mView.isActive()) {
                            mView.showSetResultFinished();
                        }
                    }

                    @Override
                    public void onComplete() {

                    }

                    @Override
                    protected void onStart() {
                        super.onStart();
                        if (mView != null && mView.isActive()) {
                            mView.showSetResultStart();
                        }
                    }
                });
        // 添加新增内容
    }

    @Override
    public void loadSelectInfo(Context context, Intent intent) {
        isLoading = true;
        Flowable.create(new FlowableOnSubscribe<List<MediaBean>>() {
            @Override
            public void subscribe(
                    @NonNull FlowableEmitter<List<MediaBean>> flowableEmitter)
                    throws Exception {
                // 如何处理壁纸ID存在但实际图片不存在？ 查询id in (),对比删除不存在内容,
                // 是否更轮播数值？暂时不更新,由壁纸自己是现
                // 获取所有ID
                mContainer.clear();
                final List<Integer> allWallPaper = WallPaperDao.getAllWallPaper(context);
                mContainer.mWallPapers.addAll(allWallPaper);
                // 查询图片在的内容
                // 不存在内容初始化完成
                if (allWallPaper == null || allWallPaper.size() == 0) {
                    flowableEmitter.onNext(new ArrayList<>());
                    flowableEmitter.onComplete();
                    return;
                }
                // 存在内容需要查询对比媒体数据库
                List<MediaBean> medias = mediaDao.getMediasByIds(context, allWallPaper);
                // 排序,使用时间500张,20ms
                Collections.sort(medias, new Comparator<MediaBean>() {
                    @Override
                    public int compare(MediaBean o1, MediaBean o2) {
                        return allWallPaper.indexOf(o1._id) - allWallPaper.indexOf(o2._id);
                    }
                });
                mContainer.addItems(medias);
                flowableEmitter.onNext(medias);
                flowableEmitter.onComplete();
            }
        }, BackpressureStrategy.LATEST)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new DisposableSubscriber<List<MediaBean>>() {
                    @Override
                    public void onNext(List<MediaBean> path) {
                        isLoading = false;
                        if (mView != null && mView.isActive()) {
                            int[] count = mContainer.getCount();
                            String title =  context.getResources().getString(R.string.select_pic);
                            if (count[0] != 0) {
                                title = context.getResources()
                                                .getQuantityString(R.plurals.select_pic_count_limit, count[0], count[0], count[1]);
                            } else {
                                title = context.getResources()
                                        .getQuantityString(R.plurals.select_pic_count, count[0], count[0]);
                            }
                            mView.showHeader(title);
                            mView.showSelected(path);
                        }
                    }

                    @Override
                    public void onError(Throwable throwable) {
                        isLoading = false;
                        if (mView != null && mView.isActive()) {
                            mView.showSelected(null);
                        }
                    }

                    @Override
                    public void onComplete() {
                        isLoading = false;
                    }

                    @Override
                    protected void onStart() {
                        super.onStart();
                    }
                });
    }

    @Override
    public boolean isItemSelected(MediaBean item) {
        Set<MediaBean> selectBucketItems = mContainer.getSelectBucketItems(item.bucketId);
        return selectBucketItems != null && selectBucketItems.contains(item);
    }
}
