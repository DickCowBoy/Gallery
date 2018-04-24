
/*
 * Copyright (C) 2018, TP-LINK TECHNOLOGIES CO., LTD.
 *
 * InterceptCheckBox.java
 *
 *
 * Author LinJl
 *
 * Ver 1.0, 18-02-02, LinJl, Create file
 */
package com.tplink.gallery.view;

import android.content.Context;
import android.support.v7.widget.AppCompatCheckBox;
import android.support.v7.widget.Toolbar;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.ViewGroup;


public class InterceptCheckBox extends AppCompatCheckBox {
    private ToggleIntercept toggleIntercept;

    public InterceptCheckBox(Context context) {
        super(context);
    }

    public InterceptCheckBox(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public InterceptCheckBox(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }


    public interface ToggleIntercept {
        boolean canToggle(boolean isCheck);
    }

    @Override
    public boolean performClick() {
        if (toggleIntercept != null && toggleIntercept.canToggle(isChecked())) {
            return super.performClick();
        } else {
            return false;
        }
    }

    public void setToggleIntercept(ToggleIntercept toggleIntercept) {
        this.toggleIntercept = toggleIntercept;
    }

    @Override
    public void setLayoutParams(ViewGroup.LayoutParams params) {
        ((Toolbar.LayoutParams) params).gravity = Gravity.CENTER_VERTICAL | Gravity.END;
        super.setLayoutParams(params);
    }
}
