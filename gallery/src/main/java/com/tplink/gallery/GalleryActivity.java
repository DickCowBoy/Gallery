package com.tplink.gallery;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import com.tplink.gallery.bean.MediaBean;
import com.tplink.gallery.dao.MediaDao;
import com.tplink.gallery.gallery.R;
import com.tplink.gallery.ui.PhotoThumbView;

import java.util.HashSet;
import java.util.List;

public class GalleryActivity extends AppCompatActivity {

    PhotoThumbView thumbView = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.gallery_activity);
        thumbView = new PhotoThumbView(this, findViewById(R.id.rcl_gallery), false);
        List<MediaBean> mediaBeans = new MediaDao(this).queryAllMedia(true, true, true);
        thumbView.showMediaBeans(mediaBeans);
        HashSet<MediaBean> mediaBeans1 = new HashSet<>();
        mediaBeans1.addAll(mediaBeans);
//        thumbView.setSelectItems(mediaBeans1);
        thumbView.setPhotoThumbListener(new PhotoThumbView.PhotoThumbListener() {
            @Override
            public void onItemClick(MediaBean data, int index) {

            }

            @Override
            public boolean canSelectItem(MediaBean item) {
                return true;
            }

            @Override
            public void delSelectItem(MediaBean item) {

            }

            @Override
            public void onSelectModeChanged(boolean inSelectMode) {

            }

            @Override
            public void onSelectCountChanged(int count, int amount) {

            }
        });
    }
}
