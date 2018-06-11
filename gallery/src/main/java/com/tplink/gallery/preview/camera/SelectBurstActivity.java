package com.tplink.gallery.preview.camera;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import com.android.gallery3d.util.BucketNames;
import com.android.gallery3d.util.FileUtils;
import com.tplink.gallery.GalleryApplication;
import com.tplink.gallery.R;
import com.tplink.gallery.base.AlbumDetailPresenter;
import com.tplink.gallery.base.MediaContract;
import com.tplink.gallery.bean.MediaBean;
import com.tplink.gallery.preview.camera.burst.BurstFilterDialog;
import com.tplink.gallery.preview.camera.burst.BurstImagePreviewAdapter;
import com.tplink.gallery.preview.camera.burst.BurstMicroThumbTabProvider;
import com.tplink.gallery.preview.camera.burst.CoverFlow;
import com.tplink.gallery.preview.camera.burst.PagerContainer;
import com.tplink.gallery.preview.camera.burst.SelectBurstViewPager;
import com.tplink.gallery.preview.camera.burst.SmartTabLayout;
import com.tplink.gallery.utils.MediaUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SelectBurstActivity extends AppCompatActivity
        implements Toolbar.OnMenuItemClickListener,
        BurstFilterDialog.OnDialogItemClick, BurstImagePreviewAdapter.BurstImagePreviewControl, MediaContract.AlbumView {
    private static final String TAG = "SelectBurstActivity";

    private static final String KEY_SELECTED_LIST = "KEY_SELECTED_LIST";
    public static final String KEY_FILTER_NEW_RESULT = "KEY_FILTER_NEW_RESULT";
    public static final String KEY_FILTER_DELETE_RESULT = "KEY_FILTER_DELETE_RESULT";
    public static final String KEY_COVER = "KEY_COVER";

    public static final String BURST = "BURST";

    private List<MediaBean> mMediaSet = new ArrayList<>();
    private MediaBean mBurstCover;
    private boolean[] mIsPicSelected;
    ArrayList<Integer> mSavedSelected;
    private SmartTabLayout viewPagerTab;
    private BurstMicroThumbTabProvider mTabProvider;

    private BurstImagePreviewAdapter mAdapter;
    private SelectBurstViewPager mViewPager;
    private PagerContainer mContainer;

    private Handler mHandler;
    private static final int MSG_SCAN_COMPLETE = 1;
    private static final int MSG_COPY_FAIL = 2;

    private boolean mSecureCamera;

    // 删除进度对话框
    private ProgressDialog mProgressDialog;
    private long mProgressStartTime;

    private ArrayList<Uri> mSavedImg = new ArrayList<>();

    private MediaContract.AlbumDetailPresenter albumDetailPresenter;

    private boolean isResume;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_burst);

        mBurstCover = (MediaBean) GalleryApplication.getApp().getParam(KEY_COVER);
        if (mBurstCover == null) {
            finish();
            Log.e(TAG, "arguments error mBurstCoverPath == null");
            return;
        }
        List<String> allowTypes = new ArrayList<>();
        allowTypes.add(MediaUtils.MIME_TYPE_JPEG);
        albumDetailPresenter = new AlbumDetailPresenter(this, this,
                mBurstCover.bucketId, allowTypes, null);
        albumDetailPresenter.loadAlbumDetail();
        mSecureCamera = getIntent().getBooleanExtra(SecureCameraPreview.EXTRA_TP_SECURE_CAMERA, false);
        if (mSecureCamera) {
            // Change the window flags so that secure camera can show when locked
            Window win = getWindow();
            WindowManager.LayoutParams params = win.getAttributes();
            params.flags |= WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED;
            win.setAttributes(params);

            try {
                if (mScreenOffReceiver != null) {
                    IntentFilter filter = new IntentFilter(Intent.ACTION_SCREEN_OFF);
                    registerReceiver(mScreenOffReceiver, filter);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        initView();
        initData();
        if (savedInstanceState != null) {
            mSavedSelected = savedInstanceState.getIntegerArrayList(KEY_SELECTED_LIST);
        }
    }

    @SuppressLint("HandlerLeak")
    private void initView() {
//         初始化toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setBackgroundColor(getColor(R.color.black));
        setSupportActionBar(toolbar);
        toolbar.setOnMenuItemClickListener(this);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(true);
        getSupportActionBar().setTitle(R.string.burst_selected);

        getWindow().setStatusBarColor(getColor(R.color.black));

        // 初始化连拍显示控件
        mContainer = findViewById(R.id.pager_container);
        mViewPager = mContainer.getViewPager();
        mAdapter = new BurstImagePreviewAdapter(this, this, mMediaSet);
        mViewPager.setAdapter(mAdapter);
        mViewPager.setClipChildren(false);
        mViewPager.setOffscreenPageLimit(7);
        boolean showTransformer = getIntent() != null ? getIntent().getBooleanExtra("showTransformer", false) : false;
        if (showTransformer) {
            new CoverFlow.Builder().with(mViewPager).scale(0.4f)
                    .pagerMargin(getResources().getDimensionPixelSize(R.dimen.pager_margin))
                    .spaceSize(0f).build();
        } else {
            mViewPager.setPageMargin(30);
        }

        // 初始化导航栏
        ViewGroup tab = findViewById(R.id.tab);
        tab.addView(LayoutInflater.from(this).inflate(R.layout.multy_photo_tag, tab, false));
        viewPagerTab = findViewById(R.id.viewpagertab);
        mTabProvider = new BurstMicroThumbTabProvider(this);
        viewPagerTab.setCustomTabView(mTabProvider);

        // 初始化Handler
        mHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case MSG_SCAN_COMPLETE:
                        // 设置保存的结果图片
                        if (mSavedImg.size() > 0) {
                            Intent data = new Intent();
                            data.putParcelableArrayListExtra(KEY_FILTER_NEW_RESULT, mSavedImg);
                            ArrayList<Uri> delImg = new ArrayList<>();
                            if (mMediaSet != null && mMediaSet.size() > 0) {
                                for (MediaBean mediaItem : mMediaSet) {
                                    delImg.add(mediaItem.getContentUri());
                                }
                            }
                            data.putParcelableArrayListExtra(KEY_FILTER_DELETE_RESULT, delImg);
                            setResult(RESULT_OK, data);
                        }
                        hideProgressDialog(MSG_SCAN_COMPLETE);
                        break;

                    case MSG_COPY_FAIL:
                        hideProgressDialog(MSG_COPY_FAIL);
                        break;

                    default:
                        break;
                }
            }
        };
    }

    private void initData() {
        // 获取宽高,以便调整位置
        mContainer.setImageWidth(mBurstCover.width);
        mContainer.setImageHeight(mBurstCover.height);

        // 获取屏幕宽高
        DisplayMetrics metric = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metric);
        mContainer.setScreenWidth(metric.widthPixels);
        mContainer.setScreenHeight(metric.heightPixels);
    }

    @Override
    protected void onResume() {
        super.onResume();
        isResume = true;
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mProgressDialog != null) {
            mProgressDialog.dismiss();
        }
        // Fix #38062 做必要的保存工作
        mSavedSelected = new ArrayList<>();
        if (mIsPicSelected != null) {
            for (int i = 0; i < mIsPicSelected.length; i++) {
                if (mIsPicSelected[i]) {
                    mSavedSelected.add(i);
                }
            }
        }
        isResume = false;
    }

    @Override
    public boolean isImageSelected(int position) {
        if (mIsPicSelected != null && (position >= 0 && position < mIsPicSelected.length)) {
            return mIsPicSelected[position];
        }

        return false;
    }

    @Override
    public void onCheckedChanged(int position, boolean isChecked) {
        if (mIsPicSelected != null && (position >= 0 && position < mIsPicSelected.length)) {
            mIsPicSelected[position] = isChecked;
            mTabProvider.updateChecked(position, isChecked);

        }
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        if (item.getItemId() == R.id.action_filter) {
            int count = getSelectedCount();
            if (count == 0) {
                // 没有选中，则直接退出
                SelectBurstActivity.this.finish();
                return false;
            }
            onMsg2Click();
        }
        return false;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.burst_filter_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onMsg1Click() {
        // 在load完毕之前删除会导致load之后刷新出现越界问题
        showProgressDialog();
        new Thread() {
            @Override
            public void run() {
                dealKeepAllPhotos();
            }
        }.start();

        return true;
    }

    @Override
    public boolean onMsg2Click() {
        // 在load完毕之前删除会导致load之后刷新出现越界问题
        showProgressDialog();
        new Thread() {
            @Override
            public void run() {
                dealKeepSelectedPhotos();
            }
        }.start();

        return true;
    }

    private void dealKeepAllPhotos() {
        // 保存选择的照片到相机目录
        List<String> choosedFilePaths = getChoosedPhotoFilePaths();
        List<String> savedFilePaths = saveChoosedPhotoToCameraDir(choosedFilePaths);
        if (choosedFilePaths.size() != savedFilePaths.size()) {
            // 没能完全复制成功，回滚，提示出错
            rollbackSavedFile(savedFilePaths);
            mHandler.sendEmptyMessage(MSG_COPY_FAIL);
            return;
        }

        // 扫描发生变更的路径,更新媒体库
        String[] paths = savedFilePaths.toArray(new String[]{});
        scanFiles(paths);
    }

    private void dealKeepSelectedPhotos() {
        // 保存选择的照片到相机目录
        List<String> choosedFilePaths = getChoosedPhotoFilePaths();
        List<String> savedFilePaths = saveChoosedPhotoToCameraDir(choosedFilePaths);
        if (choosedFilePaths.size() != savedFilePaths.size()) {
            // 没能完全复制成功，回滚，提示出错
            rollbackSavedFile(savedFilePaths);
            mHandler.sendEmptyMessage(MSG_COPY_FAIL);
            return;
        }
        // 删除所有连拍照片
        getContentResolver().delete(MediaUtils.getImageUri(), "bucket_id = ? AND _data like ?",
                new String[]{
                        String.valueOf(mBurstCover.bucketId),
                        getSelectionArgsForColumnData()
                });
        try {
            // 尝试删除父目录
            new File(choosedFilePaths.get(0)).getParentFile().delete();
        } catch (Exception e) {
            e.printStackTrace();
        }


        // 扫描发生变更的路径,更新媒体库
        List<String> changedPaths = new ArrayList<>();
        changedPaths.addAll(savedFilePaths);
        String[] paths = changedPaths.toArray(new String[]{});
        scanFiles(paths);
    }

    private void rollbackSavedFile(List<String> filePaths) {
        if (filePaths == null || filePaths.isEmpty()) {
            return;
        }
        try {
            for (String filePath : filePaths) {
                File file = new File(filePath);
                file.delete();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private List<String> getChoosedPhotoFilePaths() {
        List<String> filePaths = new ArrayList<>();
        mSavedSelected = new ArrayList<>();
        if (mIsPicSelected != null) {
            for (int i = 0; i < mIsPicSelected.length; i++) {
                if (mIsPicSelected[i]) {
                    filePaths.add(mMediaSet.get(i).filePath);
                    mSavedSelected.add(i);
                }
            }
        }
        return filePaths;
    }

    private List<String> saveChoosedPhotoToCameraDir(List<String> choosedPhotoFilePaths) {
        List<String> destFilePaths = new ArrayList<>();
        if (choosedPhotoFilePaths != null && !choosedPhotoFilePaths.isEmpty()) {
            for (String choosedFilePath : choosedPhotoFilePaths) {
                File srcFile = new File(choosedFilePath);
                File destFile = new File(getCameraFilePath(srcFile));
                try {
                    FileUtils.copyFile(srcFile, destFile, false);
                    destFilePaths.add(destFile.getAbsolutePath());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return destFilePaths;
    }

    private String getCameraFilePath(File burstFile) {
        String fileName = burstFile.getName().replaceFirst(BURST, "");
        String fileFullName = burstFile.getAbsolutePath();
        String parentPath = fileFullName.substring(0, fileFullName.indexOf(BucketNames.BURST)) + BucketNames.CAMERA;
        String cameraFilePath = parentPath + File.separator + fileName;
        File file = new File(cameraFilePath);
        Pattern p = Pattern.compile("\\((\\d+)\\)$");
        //如果文件存在，文件名加1继续检查
        while (file.exists()) {
            StringBuilder sb = new StringBuilder(cameraFilePath);
            int index = sb.lastIndexOf(".");
            if (index == -1) {
                return cameraFilePath;
            }
            String suffix = sb.substring(index);
            sb.delete(index, sb.length());
            Matcher m = p.matcher(sb);
            if (m.find()) {
                int currentNum = Integer.parseInt(m.group(1));
                sb.replace(m.start(1), m.end(1), String.valueOf(currentNum + 1));
            } else {
                sb.insert(index, "(1)");
            }
            sb.append(suffix);
            cameraFilePath = sb.toString();
            file = new File(cameraFilePath);
        }
        return cameraFilePath;
    }

    private void scanFiles(final String[] paths) {
        if (paths == null || paths.length <= 0) {
            Log.i(TAG, "no changed paths for scan.");
            mHandler.sendEmptyMessage(MSG_SCAN_COMPLETE);
            return;
        }
        final String lastPath = paths[paths.length - 1];
        MediaScannerConnection.scanFile(this, paths, null,
                new MediaScannerConnection.OnScanCompletedListener() {
                    @Override
                    public void onScanCompleted(String path, Uri uri) {
                        mSavedImg.add(uri);
                        if (lastPath.equals(path)) {
                            //最后的路径扫描完成才发送扫描完成消息
                            mHandler.sendEmptyMessage(MSG_SCAN_COMPLETE);
                        }
                    }
                });
    }

    private int getSelectedCount() {
        int result = 0;
        if (mIsPicSelected != null) {
            for (int i = 0; i < mIsPicSelected.length; i++) {
                if (mIsPicSelected[i]) {
                    result++;
                }
            }
        }

        return result;
    }

    // 显示进度
    private void showProgressDialog() {
        // 创建进度对话框
        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setTPMode(true);
        // 设置对话框消息
        mProgressDialog.setMessage(getString(R.string.waiting_dialog_msg));
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        // 设置可否取消
        mProgressDialog.setCancelable(false);
        // 设置是否显示进度
        mProgressDialog.setIndeterminate(true);
        mProgressDialog.show();
        mProgressStartTime = System.currentTimeMillis();
    }

    private void hideProgressDialog(final int msg) {
        // 如果对话框太快消失，应产品需求，最少1秒左右消失
        long disMissDelay = 1000 - Math.abs(System.currentTimeMillis() - mProgressStartTime);
        if (disMissDelay <= 0) {
            if (!isFinishing() && mProgressDialog != null && mProgressDialog.isShowing()) {
                mProgressDialog.dismiss();
            }
            if (msg == MSG_SCAN_COMPLETE) {
                finish();
            } else if (msg == MSG_COPY_FAIL) {
                // // 重新load
                showToast(R.string.msgs_no_disk_space);
            }
        } else {
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (!isFinishing() && mProgressDialog != null && mProgressDialog.isShowing()) {
                        mProgressDialog.dismiss();
                    }
                    if (msg == MSG_SCAN_COMPLETE) {
                        finish();
                    } else if (msg == MSG_COPY_FAIL) {
                        // 重新load
                        showToast(R.string.msgs_no_disk_space);
                    }
                }
            }, disMissDelay);
        }
    }

    private void showToast(int resId) {
        Toast.makeText(SelectBurstActivity.this, resId, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (mSecureCamera) {
            try {
                if (mScreenOffReceiver != null) {
                    unregisterReceiver(mScreenOffReceiver);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private BroadcastReceiver mScreenOffReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.e(TAG, "ext because of screen off");
            finish();
        }
    };

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        ArrayList<Integer> arrs = new ArrayList<>();
        if (mIsPicSelected != null) {
            for (int i = 0; i < mIsPicSelected.length; i++) {
                if (mIsPicSelected[i]) {
                    arrs.add(i);
                }
            }

        }
        outState.putIntegerArrayList(KEY_SELECTED_LIST, arrs);
    }

    @Override
    public void showMedias(List<MediaBean> beans, long lastLoad) {
        mMediaSet.clear();
        mMediaSet.addAll(beans);

        int size = beans.size();
        mIsPicSelected = new boolean[size];
        for (int i = 0; i < size; i++) {
            mIsPicSelected[i] = false;
        }
        if (mSavedSelected != null && mSavedSelected.size() > 0) {
            for (int i : mSavedSelected) {
                mIsPicSelected[i] = true;
            }
            mSavedSelected = null;
        }
        mAdapter.notifyDataSetChanged();
        viewPagerTab.setViewPager(mViewPager);
    }

    @Override
    public boolean isActive() {
        return isResume;
    }

    // 打开连拍图片筛选
    public static void launchSelectBurst(Activity activity, MediaBean cover, boolean secureCamera, int request) {
        if (cover == null) {
            return;
        }
        Intent intent = new Intent(activity, SelectBurstActivity.class);
        intent.putExtra(SecureCameraPreview.EXTRA_TP_SECURE_CAMERA, secureCamera);
        GalleryApplication.getApp().putParam(SelectBurstActivity.KEY_COVER, cover);
        activity.startActivityForResult(intent, request);
    }

    // 用于 查询媒体数据库时指定_data列的selectionArgs
    // 由于相机保存的连拍照片以BURSTyyyyMMddHHmmss.jpg 结尾，故需要类似结构的args
    private String getSelectionArgsForColumnData(){
        return "%BURST%";
    }
}
