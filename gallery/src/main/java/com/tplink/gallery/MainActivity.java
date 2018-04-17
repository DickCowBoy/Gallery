package com.tplink.gallery;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import com.tplink.gallery.media.MediaColumn;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        TextView textView = new TextView(this);
        textView.setText(MediaColumn.QUERY_COLUMN[0]);
        setContentView(textView);
    }
}
