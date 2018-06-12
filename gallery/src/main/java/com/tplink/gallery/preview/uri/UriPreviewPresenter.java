package com.tplink.gallery.preview.uri;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;

import com.tplink.gallery.R;
import com.tplink.gallery.bean.UriMediaBean;
import com.tplink.gallery.preview.PreviewContract;
import com.tplink.gallery.preview.UriPreviewProxy;
import com.tplink.utils.NoneBoundArrayList;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;

import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import io.reactivex.FlowableEmitter;
import io.reactivex.FlowableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subscribers.DisposableSubscriber;

public class UriPreviewPresenter extends PreviewContract.PreviewPresenter {

    public  static String KEY_TP_APP_SUPPORT_EDIT = "tplink_apps_support_edit";
    public  static String KEY_TP_APP_SUPPORT_SHARE = "tplink_apps_support_share";
    public  static String KEY_TP_APP_SUPPORT_SAVE = "tplink_apps_support_save";
    public  static String KEY_TP_APP_SUPPORT_SAVE_DIR = "tplink_apps_support_save_dir";

    private int clickIndex;
    private NoneBoundArrayList<UriMediaBean> mediaBeans;
    private Context context;
    public UriPreviewPresenter(Context context, Bundle data, PreviewContract.PreviewView view) {
        super(data, view);
        this.context = context;

        mediaBeans = new NoneBoundArrayList<>();
        clickIndex = data.getInt(UriPreviewProxy.KEY_TP_SELECTED_POSITION, 0);
        ArrayList<Uri> uris = data.getParcelableArrayList(UriPreviewProxy.IMAGE_TYPE_URLS);
        UriMediaBean bean;
        if (uris != null) {
            for (Uri uri : uris) {
                bean = new UriMediaBean(uri);
                mediaBeans.add(bean);
                try {
                    InputStream inputStream = context.getContentResolver().openInputStream(uri);
                    BitmapFactory.Options options = new BitmapFactory.Options();
                    options.inJustDecodeBounds = true;
                    BitmapFactory.decodeStream(inputStream, null, options);
                    bean.width = options.outWidth;
                    bean.height = options.outHeight;
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }

            }
        }
    }

    @Override
    public void loadPreviewData() {

        if (isLoading) return;
        isLoading = true;
        // load the wallpaper info
        Flowable.create(new FlowableOnSubscribe<PreviewInfo>() {
            @Override
            public void subscribe(
                    @NonNull FlowableEmitter<PreviewInfo> flowableEmitter)
                    throws Exception {

                PreviewInfo previewInfo = new PreviewInfo();

                previewInfo.datas = mediaBeans;
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inJustDecodeBounds = true;
                for (UriMediaBean mediaBean : mediaBeans) {
                    try {
                        BitmapFactory.decodeStream(context.getContentResolver()
                                .openInputStream(mediaBean.getContentUri()), null, options);
                        mediaBean.width = options.outWidth;
                        mediaBean.height = options.outHeight;
                    } catch (FileNotFoundException e) {
                        continue;
                    }
                }
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
                            mView.showMediaData(mediaBeans, clickIndex, 0);
                            String title = context.getString(R.string.total_selected_count,
                                    clickIndex + 1, mediaBeans.size());
                            mView.showHeader(title);

                            mView.showMediaData(info.datas, info.index, 0);
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

    public int getCount() {
        return mediaBeans.size();
    }
}
