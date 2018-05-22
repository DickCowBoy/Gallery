package com.tplink.gallery.view;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.TextureView;

public class GalleryTextureView extends TextureView implements TextureView.SurfaceTextureListener {
    private ViewController viewController;
    public GalleryTextureView(Context context) {
        this(context, null);
    }

    public GalleryTextureView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public void setViewController(ViewController viewController) {
        this.viewController = viewController;
    }

    public GalleryTextureView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }

    private void initView() {
        setOpaque(true);
        setSurfaceTextureListener(this);
    }

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surfaceTexture, int i, int i1) {
        viewController.onAvailable(i, i1);
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surfaceTexture, int i, int i1) {
        viewController.onSizeChanged(i, i1);
    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surfaceTexture) {
        viewController.onDisable();
        return false;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surfaceTexture) {
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        if (viewController != null) {
            return viewController.processTouchEvent(event);
        }
        return super.dispatchTouchEvent(event);
    }

    /**
     * whether create a new thread to render according to subclass
     */
    public static abstract class ViewController {
        protected GalleryTextureView mTextureView;

        public ViewController(GalleryTextureView mTextureView) {
            this.mTextureView = mTextureView;
        }

        protected abstract void onAvailable(int width, int height);
        protected abstract void onDisable();
        protected abstract void onSizeChanged(int width, int height);
        protected abstract boolean processTouchEvent(MotionEvent event);
        protected abstract void renderContent();

    }
}
