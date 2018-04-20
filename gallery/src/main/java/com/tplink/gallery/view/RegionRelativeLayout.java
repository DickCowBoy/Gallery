package com.tplink.gallery.view;

import android.content.Context;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.RelativeLayout;

import com.tplink.view.ClickParser;

public class RegionRelativeLayout extends RelativeLayout implements ClickParser {
    private float clickX = -1;
    private float clickY = -1;
    private RectF mClickRectF = new RectF(0, 0, 0 ,0);

    public RegionRelativeLayout(Context context) {
        super(context);
    }

    public RegionRelativeLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public RegionRelativeLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_UP) {
            clickX = event.getX();
            clickY = event.getY();
        }
        return super.onTouchEvent(event);
    }

    @Override
    public boolean clickRegion() {
        return mClickRectF.contains(clickX / getWidth(), clickY / getHeight());
    }

    public void setClickRectF(RectF mClickRectF) {
        this.mClickRectF = mClickRectF;
    }
}
