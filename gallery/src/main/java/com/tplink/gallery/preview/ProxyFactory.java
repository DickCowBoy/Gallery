package com.tplink.gallery.preview;

import android.app.Activity;
import android.content.Intent;

import com.tplink.gallery.preview.camera.CameraPreviewProxy;
import com.tplink.gallery.preview.camera.SecureCameraPreviewProxy;
import com.tplink.gallery.preview.wallpaper.WallPaperPreviewProxy;

public class ProxyFactory {

    public static final String PREVIEW_WALLPAPER = "com.tplink.action.gallery.PREVIEW_WALLPAPER";
    public static final String PREVIEW_CAMERA = "com.android.camera.action.REVIEW";

    private ProxyFactory(){}


    // create proxy by intent
    public static PreviewProxy getProxy(Activity host, Intent intent,
                                        PreviewContract.PreviewView view, PreviewProxy.PreviewProxyHost previewProxyHost) {
        if (intent == null) {
            return null;
        } else if (intent.getAction().equals(PREVIEW_WALLPAPER)) {
            return new WallPaperPreviewProxy(host, intent, view, previewProxyHost);
        } else if (intent.getAction().equals(PREVIEW_CAMERA)) {
            if (previewProxyHost.isSecureActivity()) {
                return new SecureCameraPreviewProxy(host, intent, view, previewProxyHost);
            }
            return new CameraPreviewProxy(host, intent, view, previewProxyHost);
        } else if (intent.getAction().equals(Intent.ACTION_VIEW)) {
            // Common preview
            int showType = intent.getIntExtra(PreviewActivity.IMAGE_TYPE, PreviewActivity.IMAGE_TYPE_LOCAL_SINGLE);
            switch (showType) {
                case PreviewActivity.IMAGE_TYPE_LOCAL_ALL:
                    return new LocalPreviewProxy(host, intent, view, previewProxyHost);
                case PreviewActivity.IMAGE_TYPE_LOCAL_ALBUM:
                    return new LocalPreviewProxy(host, intent, view, previewProxyHost);
                case PreviewActivity.IMAGE_TYPE_LOCAL_CERTAIN:

                    return null;
                case PreviewActivity.IMAGE_TYPE_LOCAL_SINGLE:

                    return null;
            }
        }

        return null;
    }
}
