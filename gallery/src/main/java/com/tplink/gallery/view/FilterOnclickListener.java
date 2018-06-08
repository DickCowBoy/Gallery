package com.tplink.gallery.view;


import android.view.View;

public abstract class FilterOnclickListener implements View.OnClickListener  {

    public FilterOnclickListener() {
        filerMillis = FILTER_MIN_MILLIS;
    }

    public FilterOnclickListener(long filerMillis) {
        this.filerMillis = filerMillis;
    }

    public static final long FILTER_MIN_MILLIS = 300;
    private long lastClick;
    private final long filerMillis;

    public abstract void click(View view);

    @Override
    public void onClick(View view) {
        if (System.currentTimeMillis() - lastClick > filerMillis) {
            lastClick = System.currentTimeMillis();
            click(view);
        }
    }
}
