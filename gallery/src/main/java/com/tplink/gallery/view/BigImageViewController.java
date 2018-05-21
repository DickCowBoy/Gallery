package com.tplink.gallery.view;

import android.graphics.Canvas;
import android.view.MotionEvent;

import com.tplink.gallery.bean.MediaBean;
import com.tplink.gallery.utils.ThreadUtils;

import java.util.List;

public class BigImageViewController extends GalleryTextureView.ViewController {
    private RenderThread mRenderThread;
    private List<MediaBean> mediaBeans;
    public BigImageViewController(GalleryTextureView mTextureView) {
        super(mTextureView);
    }

    public void updateMedias(List<MediaBean> mediaBeans) {
        this.mediaBeans = mediaBeans;
        this.mRenderThread.notifyDirty(System.currentTimeMillis());
    }

    @Override
    protected void onAvailable() {

    }

    @Override
    protected void onDisable() {

    }

    @Override
    protected void onSizeChanged(int width, int height) {

    }

    public void enable() {
        if (mRenderThread == null) {
            mRenderThread = new RenderThread();
            mRenderThread.start();
            mRenderThread.notifyDirty(System.currentTimeMillis());
        }
    }

    public void disable() {
        if (mRenderThread != null) {
            mRenderThread.quit();
        }
    }

    @Override
    protected boolean processTouchEvent(MotionEvent event) {
        return false;
    }

    @Override
    protected void renderContent() {
        if (mTextureView.isAvailable()) {
            // render the content
            Canvas canvas = mTextureView.lockCanvas();
            if (canvas != null) {




                mTextureView.unlockCanvasAndPost(canvas);
            }
        }
    }

    private class RenderThread extends Thread {

        private boolean enable = true;
        private long newestContent = -1;
        private long currentContent = -1;

        private void notifyDirty(long newestContent) {
            this.newestContent = newestContent;
            BigImageViewController.this.notifyAll();
        }

        private void quit() {
            enable = false;
            BigImageViewController.this.notifyAll();
        }

        @Override
        public void run() {
            while (enable) {
                if (currentContent >= newestContent) {
                    // it is already the newest content
                    ThreadUtils.waitWithoutInterrupt(BigImageViewController.this);
                    continue;
                }
                // need to render
                currentContent = System.currentTimeMillis();
                BigImageViewController.this.renderContent();
            }
        }
    }
}
