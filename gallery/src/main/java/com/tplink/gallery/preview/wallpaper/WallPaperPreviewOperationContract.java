package com.tplink.gallery.preview.wallpaper;

import com.tplink.base.BaseView;
import com.tplink.gallery.bean.MediaBean;

import java.util.List;

public class WallPaperPreviewOperationContract {

    public interface PreviewOpeView extends BaseView {
        void showSetResultFinished();
        void showSetResultStart();
    }


}
