package com.android.gallery3d.glrenderer;

import android.content.Context;

import com.android.gallery3d.ui.GLRoot;

public interface GLHost {
    Context getContext();
    GLRoot getGLRoot();
}
