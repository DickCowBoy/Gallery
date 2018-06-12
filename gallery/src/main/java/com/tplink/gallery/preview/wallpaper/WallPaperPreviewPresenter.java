package com.tplink.gallery.preview.wallpaper;

import android.app.Activity;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;

import com.tplink.gallery.R;
import com.tplink.gallery.bean.MediaBean;
import com.tplink.gallery.dao.MediaDao;
import com.tplink.gallery.preview.PreviewContract;
import com.tplink.gallery.selector.wallpaper.ResultContainer;
import com.tplink.gallery.selector.wallpaper.WallPaperDao;
import com.tplink.gallery.selector.wallpaper.WallPaperSelectPresenter;

import java.util.ArrayList;
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

public class WallPaperPreviewPresenter extends PreviewContract.PreviewPresenter {

    private Context context;
    private MediaDao mediaDao;
    protected ResultContainer mContainer;
    protected boolean isLoading = false;
    private int version = -1;
    private WallPaperPreviewOperationContract.PreviewOpeView opeView;

    public WallPaperPreviewPresenter(Bundle data, Context context, PreviewContract.PreviewView view) {
        super(data, view);
        this.context = context;
        mediaDao = new MediaDao(context);
        mContainer = new ResultContainer(WallPaperSelectPresenter.WALLPAPER_COUNT_LIMIT,
                ResultContainer.UNLIMIT);
    }

    @Override
    public void resume() {

    }

    @Override
    public void pause() {

    }

    public void setOpeView(WallPaperPreviewOperationContract.PreviewOpeView opeView) {
        this.opeView = opeView;
    }

    public void removeSingleMedia(MediaBean bean, int currentIndex) {
        mContainer.delItem(bean);
        List<MediaBean> mediaEntries = mContainer.getMediaEntries();
        if (currentIndex >= mediaEntries.size()) {
            currentIndex = mediaEntries.size() - 1;
        }
        if (currentIndex < 0) {
            currentIndex = 0;
        }
        mView.showMediaData(mediaEntries, currentIndex , version++);

        if (mView != null) {
            showTitle(currentIndex);
        }
    }

    @Override
    public void loadPreviewData() {
        if (isLoading) return;
        isLoading = true;
        // load the wallpaper info
        final Uri current = data.getParcelable(CURRENT_MEDIA);
        Flowable.create(new FlowableOnSubscribe<PreviewInfo>() {
            @Override
            public void subscribe(
                    @NonNull FlowableEmitter<PreviewInfo> flowableEmitter)
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
                    flowableEmitter.onNext(new PreviewInfo());
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
                PreviewInfo previewInfo = new PreviewInfo();
                String lastPathSegment = current.getLastPathSegment();
                try {
                    int id = Integer.parseInt(lastPathSegment);
                    for (int i = 0; i < medias.size(); i++) {
                        if (medias.get(i)._id == id) {
                            previewInfo.index = i;
                            break;
                        }
                    }
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                }
                previewInfo.datas = medias;
                mContainer.addItems(medias);
                flowableEmitter.onNext(previewInfo);
                flowableEmitter.onComplete();
            }
        }, BackpressureStrategy.LATEST)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new DisposableSubscriber<PreviewInfo>() {
                    @Override
                    public void onNext(PreviewInfo info) {
                        isLoading = false;
                        if (mView != null && mView.isActive()) {
                            showTitle(info.index);
                            mView.showMediaData(info.datas, info.index,version++);
                        }
                    }

                    @Override
                    public void onError(Throwable throwable) {
                        isLoading = false;
                        if (mView != null && mView.isActive()) {
                            mView.showMediaData(null, 0 , -1);
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

    public void showTitle(int currentIndex) {
        int[] count = mContainer.getCount();
        String title =  context.getResources().getString(R.string.select_pic);
        if (count[0] != 0) {
            title =  context.getString(R.string.total_selected_count,
                    currentIndex + 1, count[0]);
        }
        mView.showHeader(title);
    }

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
                            opeView.showSetResultFinished();
                        }
                    }

                    @Override
                    public void onError(Throwable throwable) {
                        if (mView != null && mView.isActive() && opeView != null && opeView != null) {
                            opeView.showSetResultFinished();
                        }
                    }

                    @Override
                    public void onComplete() {

                    }

                    @Override
                    protected void onStart() {
                        super.onStart();
                        if (mView != null && mView.isActive() && opeView != null) {
                            opeView.showSetResultStart();
                        }
                    }
                });
        // 添加新增内容
    }
}
