/*
 * Copyright (C) 2018, TP-LINK TECHNOLOGIES CO., LTD.
 *
 * RefocusPresenter.java
 *
 * 背景虚化presenter
 *
 * Author LinJl
 *
 * Ver 1.0, 18-04-02, LinJl, Create file
 */
package com.tplink.gallery.refocus;


import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.support.annotation.NonNull;

import com.mediatek.camera.common.arcsoft.ArcSoftStereoCaptureInfo;
import com.mediatek.camera.common.arcsoft.ArcSoftStereoConfigInfo;
import com.mediatek.camera.common.arcsoft.ArcSoftStereoInfoAccessor;
import com.tplink.gallery.GalleryApplication;
import com.tplink.gallery.TPGalleryJNi;
import com.tplink.gallery.bean.MediaBean;
import com.tplink.gallery.utils.ContentUriUtil;
import com.tplink.gallery.utils.ExifInterface;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.nio.ByteBuffer;

import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import io.reactivex.FlowableEmitter;
import io.reactivex.FlowableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subscribers.DisposableSubscriber;

public class RefocusPresenter extends RefocusContract.Presenter{

    private static final String TMP_JPEG_FILE = "/storage/emulated/0/.com.tplink.gallery/refocus_jpeg.tmp";
    private static final int SAMPLE_SIZE = 4;
    private static final int MAX_IGNORE_PX = 5;
    private MediaBean mediaItem = null;
    private Bitmap coverBitmap;

    ArcSoftStereoCaptureInfo arcSoftStereoCaptureInfo;
    ArcSoftStereoConfigInfo arcSoftStereoConfigInfo;
    ArcSoftStereoConfigInfo arcPreviewSoftStereoConfigInfo;
    ArcSoftStereoConfigInfo arcPreviewProcessingSoftStereoConfigInfo;
    ExifInterface exifInterface;


    private boolean isProcessing = false;



    private byte[] coverNV21 = null;
    private byte[] tmpArgb = null;

    public RefocusPresenter(RefocusContract.View view) {
        super(view);
    }

    @Override
    public void loadRefocusData(Context context, MediaBean item) {
        mediaItem = item;
        // 1.加载显示图片
        // 2.加载处理需要的数据
        Flowable.create(new FlowableOnSubscribe<BitmapResult>() {
            @Override
            public void subscribe(
                    @NonNull FlowableEmitter<BitmapResult> flowableEmitter)
                    throws Exception {
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inSampleSize = SAMPLE_SIZE;
                options.inPreferredConfig = Bitmap.Config.ARGB_8888;
                coverBitmap = BitmapFactory.decodeFile(mediaItem.filePath, options);

                File file = new File(mediaItem.filePath);
                if (!file.exists()) {
                    flowableEmitter.onComplete();
                    return;
                }
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                InputStream ins = null;
                try {
                    ins = new FileInputStream(file);
                    byte[] buf = new byte[2048];
                    int read = -1;
                    while ((read = ins.read(buf)) != -1) {
                        bos.write(buf, 0, read);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    if (ins != null) {
                        ins.close();
                    }
                }
                ArcSoftStereoInfoAccessor accessor = new ArcSoftStereoInfoAccessor();
                arcSoftStereoCaptureInfo = accessor.readRefocusImage(bos.toByteArray());
                arcSoftStereoConfigInfo = ArcSoftStereoInfoAccessor.parseConfigJsonData(
                        arcSoftStereoCaptureInfo.configBuffer);
                arcPreviewSoftStereoConfigInfo = arcSoftStereoConfigInfo.cloneConfig();
                arcPreviewSoftStereoConfigInfo.i32MainHeight_CropNoScale =
                        arcPreviewSoftStereoConfigInfo.i32MainHeight_CropNoScale / SAMPLE_SIZE;
                arcPreviewSoftStereoConfigInfo.i32MainWidth_CropNoScale =
                        arcPreviewSoftStereoConfigInfo.i32MainWidth_CropNoScale / SAMPLE_SIZE;

                arcPreviewSoftStereoConfigInfo.touchCoordX1st =
                        arcPreviewSoftStereoConfigInfo.touchCoordX1st / SAMPLE_SIZE;
                arcPreviewSoftStereoConfigInfo.touchCoordY1st =
                        arcPreviewSoftStereoConfigInfo.touchCoordY1st / SAMPLE_SIZE;

                tmpArgb = new byte[arcPreviewSoftStereoConfigInfo.i32MainHeight_CropNoScale *
                        arcPreviewSoftStereoConfigInfo.i32MainWidth_CropNoScale * SAMPLE_SIZE];


                arcPreviewProcessingSoftStereoConfigInfo = arcSoftStereoConfigInfo.cloneConfig();

                arcPreviewProcessingSoftStereoConfigInfo.i32MainHeight_CropNoScale =
                        arcPreviewProcessingSoftStereoConfigInfo.i32MainHeight_CropNoScale / SAMPLE_SIZE;
                arcPreviewProcessingSoftStereoConfigInfo.i32MainWidth_CropNoScale =
                        arcPreviewProcessingSoftStereoConfigInfo.i32MainWidth_CropNoScale / SAMPLE_SIZE;

                arcPreviewProcessingSoftStereoConfigInfo.touchCoordX1st =
                        arcPreviewProcessingSoftStereoConfigInfo.touchCoordX1st / SAMPLE_SIZE;
                arcPreviewProcessingSoftStereoConfigInfo.touchCoordY1st =
                        arcPreviewProcessingSoftStereoConfigInfo.touchCoordY1st / SAMPLE_SIZE;
                Matrix matrix = new Matrix();
                matrix.postRotate(getRotateByOrientation(arcSoftStereoConfigInfo.orientation));
                Bitmap tmpBitmap = Bitmap.createBitmap(coverBitmap, 0 , 0,
                        coverBitmap.getWidth(), coverBitmap.getHeight(), matrix, true);

                // 旋转0度不会做处理
                if (tmpBitmap != coverBitmap) {
                    coverBitmap.recycle();
                    coverBitmap = tmpBitmap;
                }
                flowableEmitter.onNext(new BitmapResult(coverBitmap, getRotateByOrientation(
                        arcSoftStereoConfigInfo.orientation)));


                coverNV21 = new byte[item.width / SAMPLE_SIZE * item.height / SAMPLE_SIZE * 3 / 2];
                // 生成缩小的NV21数据
                TPGalleryJNi.getNV21AndScaleByJpeg(arcSoftStereoCaptureInfo.bayerBuffer, coverNV21,
                        getRotateByOrientation(arcSoftStereoConfigInfo.orientation), item.width, item.height);
                // 读取Exif信息
                exifInterface = new ExifInterface(mediaItem.filePath);
                flowableEmitter.onComplete();

            }
        }, BackpressureStrategy.LATEST)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new DisposableSubscriber<BitmapResult>() {
                    @Override
                    public void onNext(BitmapResult path) {
                        if (mView != null && mView.isActive()) {
                            mView.showBitmap(path.bitmap, path.rotate,
                                    arcPreviewSoftStereoConfigInfo.touchCoordX1st,
                                    arcPreviewSoftStereoConfigInfo.touchCoordY1st,
                                    arcPreviewSoftStereoConfigInfo.dofLevel, true);
                        }
                    }

                    @Override
                    public void onError(Throwable throwable) {
                        if (mView != null && mView.isActive()) {
                            // TODO 加载失败
                        }
                    }

                    @Override
                    public void onComplete() {
                        if (mView != null && mView.isActive()) {
                            mView.hideLoadingProgress();
                        }
                    }

                    @Override
                    protected void onStart() {
                        super.onStart();
                        if (mView != null) {
                            mView.showLoadingProgress();
                        }
                    }
                });
    }

    @Override
    public void processPreview(int blur) {
        // 判断参数是否修改
        if (arcPreviewSoftStereoConfigInfo.dofLevel == blur) {
            return;
        }
        arcPreviewSoftStereoConfigInfo.dofLevel = blur;
        if (!isProcessing) {
            // 处理新参数
            processPreview();
        }
    }
    @Override
    public void processPreview(float x, float y) {
        // 根据百分比计算出实际内容
        // 判断参数是否修改
        int dx = (int) (arcPreviewSoftStereoConfigInfo.i32MainWidth_CropNoScale * x);
        int dy = (int) (arcPreviewSoftStereoConfigInfo.i32MainHeight_CropNoScale * y);
        if (Math.abs(arcPreviewSoftStereoConfigInfo.touchCoordX1st - dx) < MAX_IGNORE_PX &&
                Math.abs(arcPreviewSoftStereoConfigInfo.touchCoordY1st - dy) < MAX_IGNORE_PX) {
            return;
        }
        arcPreviewSoftStereoConfigInfo.touchCoordY1st = dy;
        arcPreviewSoftStereoConfigInfo.touchCoordX1st = dx;
        if (!isProcessing) {
            // 处理新参数
            processPreview();
        }
    }

    private void processPreview() {
        Flowable.create(new FlowableOnSubscribe<BitmapResult>() {
            @Override
            public void subscribe(
                    @NonNull FlowableEmitter<BitmapResult> flowableEmitter)
                    throws Exception {

                while (true) {
                    if (arcPreviewSoftStereoConfigInfo.equals(arcPreviewProcessingSoftStereoConfigInfo)) {
                        break;
                    } else {

                        arcPreviewProcessingSoftStereoConfigInfo.updateData(arcPreviewSoftStereoConfigInfo);
                        TPGalleryJNi.processRefocusPreview(arcSoftStereoCaptureInfo.calibrationBuffer,
                                arcSoftStereoCaptureInfo.depthBuffer, coverNV21,
                                arcPreviewProcessingSoftStereoConfigInfo.i32MainWidth_CropNoScale,
                                arcPreviewProcessingSoftStereoConfigInfo.i32MainHeight_CropNoScale,
                                arcPreviewProcessingSoftStereoConfigInfo.i32AuxWidth_CropNoScale,
                                arcPreviewProcessingSoftStereoConfigInfo.i32AuxHeight_CropNoScale,
                                arcPreviewProcessingSoftStereoConfigInfo.dofLevel / SAMPLE_SIZE,
                                arcPreviewProcessingSoftStereoConfigInfo.touchCoordX1st,
                                arcPreviewProcessingSoftStereoConfigInfo.touchCoordY1st,
                                tmpArgb,
                                (360 - getRotateByOrientation(
                                        arcPreviewProcessingSoftStereoConfigInfo.orientation)) % 360,
                                arcPreviewProcessingSoftStereoConfigInfo.hasWatermark == 1,
                                exifInterface.getAttribute(ExifInterface.TAG_MODEL));
                        ByteBuffer buffer = ByteBuffer.wrap(tmpArgb);
                        buffer.rewind();
                        coverBitmap.copyPixelsFromBuffer(buffer);
                        flowableEmitter.onNext(new BitmapResult(coverBitmap, getRotateByOrientation(
                                arcPreviewSoftStereoConfigInfo.orientation)));
                    }
                }

                flowableEmitter.onComplete();
            }
        }, BackpressureStrategy.LATEST)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new DisposableSubscriber<BitmapResult>() {
                    @Override
                    public void onNext(BitmapResult path) {

                        if (mView != null && mView.isActive()) {
                            mView.showBitmap(path.bitmap, path.rotate,
                                    arcPreviewSoftStereoConfigInfo.touchCoordX1st,
                                    arcPreviewSoftStereoConfigInfo.touchCoordY1st,
                                    arcPreviewSoftStereoConfigInfo.dofLevel,false);
                        }
                    }

                    @Override
                    public void onError(Throwable throwable) {
                        isProcessing = false;
                    }

                    @Override
                    public void onComplete() {
                        isProcessing = false;
                    }

                    @Override
                    protected void onStart() {
                        super.onStart();
                        isProcessing = true;
                    }
                });
    }

    @Override
    public void processCapture() {

        Flowable.create(new FlowableOnSubscribe<BitmapResult>() {
            @Override
            public void subscribe(
                    @NonNull FlowableEmitter<BitmapResult> flowableEmitter)
                    throws Exception {

                // 确保临时文件存在
                File file = new File(TMP_JPEG_FILE);
                if (!file.exists()) {
                    File parentFile = file.getParentFile();
                    if (!parentFile.exists()) {
                        parentFile.mkdirs();
                    }
                    file.createNewFile();
                }
                // 处理
                arcSoftStereoCaptureInfo.jpgBuffer = TPGalleryJNi.processRefocusCapture(
                        arcSoftStereoCaptureInfo.calibrationBuffer,
                        arcSoftStereoCaptureInfo.depthBuffer,
                        arcSoftStereoCaptureInfo.bayerBuffer,
                        arcPreviewSoftStereoConfigInfo.i32MainWidth_CropNoScale * SAMPLE_SIZE,
                        arcPreviewSoftStereoConfigInfo.i32MainHeight_CropNoScale * SAMPLE_SIZE,
                        arcPreviewSoftStereoConfigInfo.i32AuxWidth_CropNoScale,
                        arcPreviewSoftStereoConfigInfo.i32AuxHeight_CropNoScale,
                        arcPreviewSoftStereoConfigInfo.dofLevel,
                        arcPreviewSoftStereoConfigInfo.touchCoordX1st * SAMPLE_SIZE,
                        arcPreviewSoftStereoConfigInfo.touchCoordY1st * SAMPLE_SIZE,
                        getRotateByOrientation(arcPreviewSoftStereoConfigInfo.orientation), TMP_JPEG_FILE,
                        arcPreviewSoftStereoConfigInfo.hasWatermark == 1,
                        exifInterface.getAttribute(ExifInterface.TAG_MODEL),
                        exifInterface.exifBytes == null ? new byte[0] : exifInterface.exifBytes);
                // 最终保存图片
                ArcSoftStereoInfoAccessor accessor = new ArcSoftStereoInfoAccessor();
                arcSoftStereoConfigInfo.dofLevel = arcPreviewSoftStereoConfigInfo.dofLevel;
                arcSoftStereoConfigInfo.touchCoordX1st = arcPreviewSoftStereoConfigInfo.touchCoordX1st * SAMPLE_SIZE;
                arcSoftStereoConfigInfo.touchCoordY1st = arcPreviewSoftStereoConfigInfo.touchCoordY1st * SAMPLE_SIZE;

                arcSoftStereoCaptureInfo.configBuffer = arcSoftStereoConfigInfo.toJSONString().getBytes();
                byte[] bytes = accessor.writeStereoCaptureInfo(arcSoftStereoCaptureInfo);
                // 写入文件
                FileOutputStream fos = null;
                try {
                    fos = new FileOutputStream(mediaItem.filePath);
                    fos.write(bytes);
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    if (fos != null) {
                        fos.close();
                    }
                }

                // 扫描该文件
                ContentUriUtil.updateContent(GalleryApplication.getApp(),
                        mediaItem.getContentUri(),
                        new File(mediaItem.filePath));

                flowableEmitter.onComplete();
            }
        }, BackpressureStrategy.LATEST)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new DisposableSubscriber<BitmapResult>() {
                    @Override
                    public void onNext(BitmapResult path) {}

                    @Override
                    public void onError(Throwable throwable) {
                        if (mView != null && mView.isActive()) {
                            mView.hideSavingProgress(-1);
                        }
                    }

                    @Override
                    public void onComplete() {
                        if (mView != null && mView.isActive()) {
                            mView.hideSavingProgress(0);
                        }
                    }

                    @Override
                    protected void onStart() {
                        super.onStart();
                        if (mView != null) {
                            mView.showSavingProgress(null);
                        }
                    }
                });
    }

    @Override
    public void destroy() {
        arcSoftStereoCaptureInfo = null;
        arcSoftStereoConfigInfo = null;
        if (coverBitmap != null) {
            coverBitmap.recycle();
            coverBitmap = null;
        }
    }

    class BitmapResult {
        Bitmap bitmap;
        int rotate;

        public BitmapResult(Bitmap bitmap, int rotate) {
            this.bitmap = bitmap;
            this.rotate = rotate;
        }
    }

    private int getRotateByOrientation(int orientation) {
        switch (orientation) {
            case 0 :
                return  270;
            case 180:
                return  90;
            case 90:
                return  180;
        }
        return 0;
    }
}
