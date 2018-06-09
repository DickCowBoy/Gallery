package com.tplink.gallery.view;

import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowInsets;
import android.widget.RelativeLayout;

public class AutoFitRelative extends RelativeLayout {

    private Rect rect;
    public AutoFitRelative(Context context) {
        this(context, null);
    }

    public AutoFitRelative(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public AutoFitRelative(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setOnApplyWindowInsetsListener(new View.OnApplyWindowInsetsListener() {
            @Override
            public WindowInsets onApplyWindowInsets(View view, WindowInsets windowInsets) {
                view.setPadding(0, 0, 0, 0);
                Rect newRect = new Rect(
                        windowInsets.getSystemWindowInsetLeft(),
                        windowInsets.getSystemWindowInsetTop(),
                        windowInsets.getSystemWindowInsetRight(),
                        windowInsets.getSystemWindowInsetBottom());
                if (!newRect.equals(rect)) {
                    rect = newRect;
                    layoutChildView();
                }
                return windowInsets.consumeStableInsets();
            }
        });
    }

    private void layoutChildView() {
        View viewWithTag = null;
        for (int i = 0; i < getChildCount(); i++) {
            viewWithTag = getChildAt(i);
            if (viewWithTag != null && viewWithTag instanceof SystemRectFit) {
                ((SystemRectFit)viewWithTag).onNewSystemRect(rect, (LayoutParams) viewWithTag.getLayoutParams());
            }
        }
    }

    public void addView(View child, ViewGroup.LayoutParams params) {
        if (child instanceof SystemRectFit && rect != null) {
            ((SystemRectFit)child).onNewSystemRect(rect, (LayoutParams)params);
        }
        super.addView(child, params);
    }

    public interface SystemRectFit {
        void onNewSystemRect(Rect rect, LayoutParams layoutParams);
    }
}
