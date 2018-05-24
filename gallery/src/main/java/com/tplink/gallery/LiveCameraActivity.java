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
    BitmapDrawable drawable;
    BitmapDrawable drawable1;
    BitmapDrawable drawable2;
    BigImageViewController bigImageViewController;
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setWindow();
        drawable = (BitmapDrawable) getResources().getDrawable(R.drawable.cba872e4e0a4b47596613ff39846ec8c);
        drawable1 = (BitmapDrawable) getResources().getDrawable(R.drawable.nature_4);
        drawable2 = (BitmapDrawable) getResources().getDrawable(R.drawable.nature_3);
        GalleryTextureView mTextureView = new GalleryTextureView(this);
        bigImageViewController = new BigImageViewController(mTextureView, new BigImageViewController.DrawContentProvider(bigImageViewController) {
            int index = 0;
            BigImageViewController.DrawContent content;
            BigImageViewController.DrawContent content1;
            BigImageViewController.DrawContent content2;
            @Override
            public boolean hasPreview() {
                return index > 0;
            }

            @Override
            public boolean hasNext() {
                return true;
            }

            public BigImageViewController.DrawContent getContentByIndex(int index) {
                if (index % 3 == 0) {
                    if (content != null) return content;
                    content = new BigImageViewController.DrawContent();
                    content.content = drawable.getBitmap();
                    content.width = content.content.getWidth();
                    content.height = content.content.getHeight();
                    return content;
                } else if (index % 3 == 1) {
                    if (content1 != null) return content1;
                    content1 = new BigImageViewController.DrawContent();
                    content1.content = drawable1.getBitmap();
                    content1.width = content1.content.getWidth();
                    content1.height = content1.content.getHeight();
                    return content1;
                } else {
                    if (content2 != null) return content2;
                    content2 = new BigImageViewController.DrawContent();
                    content2.content = drawable2.getBitmap();
                    content2.width = content2.content.getWidth();
                    content2.height = content2.content.getHeight();
                    return content2;
                }
            }


            @Override
            public BigImageViewController.DrawContent getCurrentDrawContent() {
                return getContentByIndex(index);
            }

            @Override
            public BigImageViewController.DrawContent getPreDrawContent(int offset) {
                offset += index;
                return getContentByIndex(offset);
            }

            @Override
            public BigImageViewController.DrawContent getNextDrawContent(int offset) {
                offset += index;
                return getContentByIndex(offset);
            }

            @Override
            public void switchToPre() {
                index --;
            }

            @Override
            public void switchToNext() {
                index ++;
            }
        });
        mTextureView.setViewController(bigImageViewController);
        setContentView(mTextureView);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // load image
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