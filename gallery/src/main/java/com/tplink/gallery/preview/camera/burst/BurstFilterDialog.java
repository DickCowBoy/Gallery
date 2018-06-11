package com.tplink.gallery.preview.camera.burst;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.TextView;

import com.tplink.gallery.R;

public class BurstFilterDialog extends Dialog implements View.OnClickListener {

    private String mTitle;
    private String mMsg1;
    private String mMsg2;
    private OnDialogItemClick mOnDialogItemClick;

    public BurstFilterDialog(Context context, String title, String msg1, String msg2) {
        super(context, 0);
        mTitle = title;
        mMsg1 = msg1;
        mMsg2 = msg2;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);

        setContentView(R.layout.dialog_burst_filter);
        TextView titleTV = (TextView) findViewById(R.id.tv_title);
        titleTV.setText(mTitle);
        TextView msg1TV = (TextView) findViewById(R.id.tv_message);
        msg1TV.setText(mMsg1);

        TextView msg2TV = (TextView) findViewById(R.id.tv_keepsome);
        msg2TV.setText(mMsg2);

        msg1TV.setOnClickListener(this);
        msg2TV.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.tv_message) {
            if (mOnDialogItemClick != null) {
                if (mOnDialogItemClick.onMsg1Click()) {
                    dismiss();
                }
            } else {
                dismiss();
            }
        } else if (v.getId() == R.id.tv_keepsome) {
            if (mOnDialogItemClick != null) {
                if (mOnDialogItemClick.onMsg2Click()) {
                    dismiss();
                }
            } else {
                dismiss();
            }
        }
    }

    public interface OnDialogItemClick {
        boolean onMsg1Click();

        boolean onMsg2Click();
    }

    public void setOnDialogItemClick(OnDialogItemClick onDialogItemClick) {
        this.mOnDialogItemClick = onDialogItemClick;
    }

}
