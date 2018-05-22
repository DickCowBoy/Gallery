package com.tplink.gallery;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.SurfaceTexture;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.hardware.Camera;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.TextureView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import com.tplink.gallery.gallery.R;
import com.tplink.gallery.view.BigImageViewController;
import com.tplink.gallery.view.GalleryTextureView;

import java.io.IOException;

public class LiveCameraActivity extends Activity {

    BigImageViewController bigImageViewController;
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setWindow();
        GalleryTextureView mTextureView = new GalleryTextureView(this);
        bigImageViewController = new BigImageViewController(mTextureView);
        mTextureView.setViewController(bigImageViewController);
        setContentView(mTextureView);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // load image
        BitmapDrawable drawable = (BitmapDrawable) getResources().getDrawable(R.drawable.cba872e4e0a4b47596613ff39846ec8c);
        bigImageViewController.setBitmap(drawable.getBitmap());
        bigImageViewController.enable();
    }

    @Override
    protected void onPause() {
        super.onPause();
        bigImageViewController.disable();
    }

    protected void setWindow() {
        Window window = getWindow();
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
        window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);

        window.setBackgroundDrawable(null);
    }
}