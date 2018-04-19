package com.tplink.gallery;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import com.tplink.gallery.bean.MediaBean;
import com.tplink.gallery.dao.MediaDao;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        TextView textView = new TextView(this);
        List<MediaBean> mediaBeans = new MediaDao(this).queryAllMedia(true, true, true);

        textView.setText(mediaBeans == null ? "0" : mediaBeans.size()+"");
        setContentView(textView);
    }
}
