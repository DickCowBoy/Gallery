package com.tplink.gallery;

import android.content.Context;

import com.bumptech.glide.GlideBuilder;
import com.bumptech.glide.annotation.GlideModule;
import com.bumptech.glide.load.DecodeFormat;
import com.bumptech.glide.module.AppGlideModule;
import com.bumptech.glide.request.RequestOptions;

@GlideModule
public final class GalleryGlideModule extends AppGlideModule {
    @Override
    public void applyOptions(Context context, GlideBuilder builder) {
        // 使用Hardware Bitmaps减少内存使用
        builder.setDefaultRequestOptions(new RequestOptions().
                format(DecodeFormat.PREFER_ARGB_8888).disallowHardwareConfig());
    }
}