package com.tplink.gallery.view;

import android.content.Context;
import android.graphics.Rect;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.tplink.gallery.R;

public class FitLinearLayout extends LinearLayout implements AutoFitRelative.SystemRectFit{
    public FitLinearLayout(Context context) {
        super(context);
    }

    public FitLinearLayout(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public FitLinearLayout(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public void onNewSystemRect(Rect rect, RelativeLayout.LayoutParams layoutParams) {
        int padding8Dp = getResources().getDimensionPixelSize(R.dimen.padding_8dp);
        // 增加视图与上边界的距离
        layoutParams.setMargins(padding8Dp + rect.left,
                rect.top + padding8Dp * 10, 0, 0);
    }
}
