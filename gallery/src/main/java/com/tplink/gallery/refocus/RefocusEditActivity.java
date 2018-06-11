/*
 * Copyright (C) 2018, TP-LINK TECHNOLOGIES CO., LTD.
 *
 * RefocusEditActivity.java
 *
 * 背景虚化页面
 *
 * Author LinJl
 *
 * Ver 1.0, 18-04-02, LinJl, Create file
 */
package com.tplink.gallery.refocus;

import android.app.AlertDialog;
import android.app.DialogFragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.tplink.gallery.GalleryApplication;
import com.tplink.gallery.R;
import com.tplink.gallery.bean.MediaBean;
import com.tplink.gallery.view.AutoFitToolBar;
import com.tplink.gallery.view.CalibrationSeekBar;
import com.tplink.gallery.view.RefocusImageView;


import java.lang.ref.WeakReference;

public class RefocusEditActivity extends AppCompatActivity
        implements  CalibrationSeekBar.SeekValueListener, RefocusImageView.OnImageTapUp,
        RefocusContract.View, AutoFitToolBar.OnPaddingListener {

    public static final String EXTRA_TP_SECURE_CAMERA = "EXTRA_TP_SECURE_CAMERA";
    public static final String KEY_REFOCUS_MEDIA = "KEY_REFOCUS_MEDIA";

    private static final double PREVIEW_CAPTURE_D = 2d / 3;
    private static final long SCALE_DURATION_TIME = 400;
    private static final float SCALE_FACTOR_FOR_4_3 = 0.9f;
    private float SCALE_FACTOR_FOR_16_9 = 0.75f;
    private float SCALE_FACTOR_FOR_2_1 = 0.70f;
    private static final double RATIO_FOR_16_9 = 16d / 9;
    private static final double RATIO_FOR_2_1 = 2d / 1;
    private static final double RATIO_TOLERANCE = 0.02d;


    private static final int MAX_NUM = (int) (100 * PREVIEW_CAPTURE_D);
    private static final int SHOW_MAX_NUM = 15;
    private static final double VALUE_RX = SHOW_MAX_NUM / 1.0d / MAX_NUM;

    private boolean isResume;



    RefocusImageView imageView;
    byte[] tmpArgb;

    private AlertDialog mCancelDialog;
    private TextView mCurrentTick;
    private CalibrationSeekBar mRefocusSeekBar;
    private View seekBarControlView;
    private int mCurrentNum;

    private WeakReference<DialogFragment> mSavingProgressDialog;
    private WeakReference<DialogFragment> mLoadingProgressDialog;
    private MediaBean mMediaItem = null;
    private RefocusContract.Presenter mRefocusPresenter;
    private AutoFitToolBar mToolBar;
    private boolean mSecureCamera;

    private Rect mPadding = new Rect(0 ,0, 0, 0);

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_refocus);
        seekBarControlView  = this.findViewById(R.id.refocusImage_bottom_controls);
        initializeViews();

        mSecureCamera = getIntent().getBooleanExtra(EXTRA_TP_SECURE_CAMERA, false);
        if (mSecureCamera) {
            // Change the window flags so that secure camera can show when locked
            Window win = getWindow();
            WindowManager.LayoutParams params = win.getAttributes();
            params.flags |= WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED;
            win.setAttributes(params);

            try {
                if (mScreenOffReceiver != null) {
                    IntentFilter filter = new IntentFilter(Intent.ACTION_SCREEN_OFF);
                    registerReceiver(mScreenOffReceiver, filter);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        imageView = findViewById(R.id.img_show);

        mRefocusPresenter = new RefocusPresenter(this);
        // 1.通过XMP解析照片

        Object param = GalleryApplication.getApp().getParam(KEY_REFOCUS_MEDIA);
        if (param != null) {
            mMediaItem = (MediaBean) param;
        } else {
            finish();
            return;
        }
        mRefocusPresenter.loadRefocusData(this, mMediaItem);

        initRefocusSeekBar();
    }

    private void initializeViews() {
        initStatusBar();
        initToolBar();
        toggleStatusBarByOrientation();
    }

    private void initRefocusSeekBar() {

        mCurrentTick = seekBarControlView.findViewById(R.id.current_tick_text);
        TextView maxTick = seekBarControlView.findViewById(R.id.max_tick_text);
        maxTick.setText(String.format("%d", SHOW_MAX_NUM));

        mRefocusSeekBar = this.findViewById(R.id.refocusSeekBar);
        mRefocusSeekBar.setValues(0, MAX_NUM);
        mRefocusSeekBar.setVisibility(View.VISIBLE);
        mRefocusSeekBar.setSeekValueChangeListener(this);
        imageView.setOnImageTapUp(this);
    }

    // Shows status bar in portrait view, hide in landscape view
    private void toggleStatusBarByOrientation() {
        Window win = getWindow();
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            win.clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        } else {
            win.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        }
    }


    // set statusBar to be customized color
    private void initStatusBar() {
        Window window = getWindow();
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
        window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);

        window.setBackgroundDrawable(null);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.save_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_save:
                mRefocusPresenter.processCapture();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void initToolBar() {
        mToolBar = findViewById(R.id.toolbar);
        mToolBar.setTitle(""); // hide title
        setSupportActionBar(mToolBar);
        mToolBar.setNavigationIcon(R.drawable.ic_refocus_cancel);
        mToolBar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getCancelDialog().show();
            }
        });
        mToolBar.setOnPaddingListener(this);
    }

    private AlertDialog getCancelDialog() {
        if (mCancelDialog != null) {
            return mCancelDialog;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(RefocusEditActivity.this,
                R.style.AlertDialogTheme);
        builder.setMessage(R.string.refocus_canceling_dialog_title)
                // TODO LJL .setTPMode(true)
                .setNegativeButton(R.string.refocus_canceling_dialog_cancel,mDialogClickListener)
                .setPositiveButton(R.string.refocus_canceling_dialog_confirm,mDialogClickListener);
        mCancelDialog = builder.create();

        return mCancelDialog;
    }

    private DialogInterface.OnClickListener mDialogClickListener = new DialogInterface.OnClickListener() {

        @Override
        public void onClick(DialogInterface dialog, int which) {
            if (dialog == null) return;

            dialog.dismiss();
            if (DialogInterface.BUTTON_POSITIVE == which) {
                finish();
            }
        }
    };

    @Override
    public void onValueChange(int newValue) {
        mCurrentTick.setText(getShowValue(newValue));
        mCurrentNum = newValue;
        mRefocusPresenter.processPreview(mCurrentNum);
    }

    @Override
    public void onImageTapUp(float x, float y) {
        mRefocusPresenter.processPreview(x, y);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mRefocusPresenter != null) {
            mRefocusPresenter.destroy();
        }

        if (mSecureCamera) {
            try {
                if (mScreenOffReceiver != null) {
                    unregisterReceiver(mScreenOffReceiver);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static void lunchRefocusEdit(Context context, MediaBean bean, boolean isSecureCamera) {
        Intent intent = new Intent();
        intent.setClass(context, RefocusEditActivity.class);
        GalleryApplication.getApp().putParam(KEY_REFOCUS_MEDIA, bean);
        intent.putExtra(EXTRA_TP_SECURE_CAMERA, isSecureCamera);
        context.startActivity(intent);
    }

    private void animateRefocusView(Bitmap bitmap) {
        float ratio = Math.max(bitmap.getWidth(),bitmap.getHeight()) /
                Math.min(bitmap.getWidth(), bitmap.getHeight());
        float scaleFactor = SCALE_FACTOR_FOR_4_3;
        if (Math.abs(ratio - RATIO_FOR_16_9) <= RATIO_TOLERANCE) {
            scaleFactor = SCALE_FACTOR_FOR_16_9;
        }
        if (Math.abs(ratio - RATIO_FOR_2_1) <= RATIO_TOLERANCE) {
            scaleFactor = SCALE_FACTOR_FOR_2_1;
        }

        imageView.animate().scaleX(scaleFactor).scaleY(scaleFactor)
                .setDuration(SCALE_DURATION_TIME).start();
    }

    @Override
    public void finish() {
        if (imageView != null) {
            imageView.animate().scaleX(1.0f).scaleY(1.0f)
                    .withEndAction(new Runnable() {
                        @Override
                        public void run() {
                            RefocusEditActivity.super.finish();
                            overridePendingTransition(0, R.anim.refocus_to_gallery_exit_anim);
                        }
                    })
                    .setDuration(SCALE_DURATION_TIME).start();
        } else {
            super.finish();
        }
    }

    public void showLoadingProgress() {
        DialogFragment fragment;
        if (mLoadingProgressDialog != null) {
            fragment = mLoadingProgressDialog.get();
            if (fragment != null) {
                fragment.show(getFragmentManager(), null);
                return;
            }
        }
        final DialogFragment genProgressDialog = new ProgressFragment(R.string.loading_image);
        genProgressDialog.setCancelable(false);
        genProgressDialog.show(getFragmentManager(), null);
        genProgressDialog.setStyle(R.style.ProgressDialog, genProgressDialog.getTheme());
        mLoadingProgressDialog = new WeakReference<DialogFragment>(genProgressDialog);
    }

    public void hideLoadingProgress() {
        if (mLoadingProgressDialog != null) {
            DialogFragment fragment = mLoadingProgressDialog.get();
            if (fragment != null) {
                fragment.dismissAllowingStateLoss();
            }
        }
    }

    @Override
    public void showSavingProgress(String albumName) {
        DialogFragment fragment;
        if (mSavingProgressDialog != null) {
            fragment = mSavingProgressDialog.get();
            if (fragment != null) {
                fragment.show(getFragmentManager(), null);
                return;
            }
        }
        String progressText;
        if (albumName == null) {
            progressText = getString(R.string.saving_image);
        } else {
            progressText = getString(R.string.m_saving_image, albumName);
        }
        final DialogFragment genProgressDialog = new ProgressFragment(progressText);
        genProgressDialog.setCancelable(false);
        genProgressDialog.show(getFragmentManager(), null);
        genProgressDialog.setStyle(R.style.ProgressDialog, genProgressDialog.getTheme());
        mSavingProgressDialog = new WeakReference<DialogFragment>(genProgressDialog);
    }

    public void hideSavingProgress(int result) {
        if (mSavingProgressDialog != null) {
            DialogFragment progress = mSavingProgressDialog.get();
            if (progress != null) {
                progress.dismissAllowingStateLoss();
            }
            if (result == 0) {
                // 结束
                onBackPressed();
            }
        }
    }

    @Override
    public void showBitmap(Bitmap bitmap, int rotate, float x, float y, int blur, boolean anim) {
        imageView.setRotate(rotate);
        imageView.setImageBitmap(bitmap);
        if (anim) {
            imageView.setImageBitmap(bitmap, x, y);
            imageView.postDelayed(new Runnable() {
                @Override
                public void run() {
                    animateRefocusView(bitmap);
                }
            }, 500);
            mRefocusSeekBar.setValue(blur);
            mCurrentTick.setText(getShowValue(blur));
        } else {
            imageView.setImageBitmap(bitmap);
        }
        seekBarControlView.setVisibility(View.VISIBLE);
    }

    private String getShowValue(int blur) {
        return String.format("%d", Math.round(blur * VALUE_RX));
    }

    @Override
    protected void onPause() {
        super.onPause();
        hideLoadingProgress();
        hideSavingProgress(-1);
        isResume = false;
    }

    @Override
    public boolean isActive() {
        return isResume;
    }

    @Override
    public void onPadding(int left, int top, int right, int bottom) {
        if (mPadding.equals(new Rect(left, top, right, bottom))) {
            return;
        }
        mPadding.set(left, top, right, bottom);
        RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) seekBarControlView.getLayoutParams();
        layoutParams.bottomMargin = bottom + getResources().getDimensionPixelOffset(R.dimen.refocus_seekbar_bottom);
        seekBarControlView.requestLayout();
        if (bottom != 0) {
            SCALE_FACTOR_FOR_16_9 = 0.50F;
        } else {
            SCALE_FACTOR_FOR_16_9 = 0.75F;
        }
    }

    private BroadcastReceiver mScreenOffReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            finish();
        }
    };

    @Override
    protected void onResume() {
        super.onResume();
        isResume = true;
    }

}
