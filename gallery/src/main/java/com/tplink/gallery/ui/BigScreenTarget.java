package com.tplink.gallery.ui;

import android.graphics.drawable.Drawable;

import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;

public abstract class BigScreenTarget<T> extends SimpleTarget<Drawable>{

    private T tag;

    public BigScreenTarget(int width, int height) {
        super(width, height);
    }

    public T getTag() {
        return tag;
    }

    public void setTag(T tag) {
        this.tag = tag;
    }
}
