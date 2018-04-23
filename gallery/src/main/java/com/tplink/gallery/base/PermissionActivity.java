/*
 * Copyright (C), 2016, TP-LINK TECHNOLOGIES CO., LTD.
 *
 * PermissionActivity.java
 *
 * Author He Hanxuan
 *
 * Ver 1.0, 2016-06-03, He Hanxuan, Create file
 */
package com.tplink.gallery.base;


import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;

/**
 * 权限管理，默认{@link #onResume()}动态申请，没有权限时默认打开设置并{@link #finish()}
 * <p>
 * 重写{@link #processBusinessOnAllPermissionGranted()}响应权限完全申请成功
 */
public abstract class PermissionActivity extends AppCompatActivity {

    private static final int REQUEST_CODE_ASK_PERMISSIONS = 1001;

    private static final String PACKAGE_URL_SCHEME = "package:";// 权限方案

    public static final String[] NEED_PERMISSIONS = new String[]{
            Manifest.permission.CAMERA,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.READ_SMS,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    protected List<String> getNeedPermissions() {
        List<String> permissions = new ArrayList<>();
        for (String needPermission : NEED_PERMISSIONS) {
            if (checkSelfPermission(needPermission) != PackageManager.PERMISSION_GRANTED) {
                permissions.add(needPermission);
            }
        }
        return permissions;
    }

    protected boolean hasAllPermission() {
        return getNeedPermissions().size() == 0;
    }


    @Override
    protected void onResume() {
        super.onResume();
        checkCustomPermission();
    }

    /**
     * Check permission.
     *
     * @return true, if successful
     */
    public boolean checkCustomPermission() {

        List<String> needPermissions = getNeedPermissions();
        if (needPermissions.size() > 0) {
            askingPermission(needPermissions.toArray(new String[needPermissions.size()]));
        }
        /*
         * 如果不是都有权限，返回假。
         */
        return false;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CODE_ASK_PERMISSIONS:

                /*
                 * 当所有的权限判定结果为真时，才认为权限获取成功。
                 */
                Boolean result = true;
                for (int permissionResult : grantResults) {
                    if (permissionResult != PackageManager.PERMISSION_GRANTED) {
                        result = false;
                        break;
                    }
                }

                if (result) {
                    onRequestPermissionSuccess();
                } else {
                    onRequestPermissionFailure();
                }
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    // 打开系统应用设置(ACTION_APPLICATION_DETAILS_SETTINGS:系统设置权限)
    private void startAppSettings() {
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        intent.setData(Uri.parse(PACKAGE_URL_SCHEME + getPackageName()));
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    public void onRequestPermissionFailure() {
        startAppSettings();
        finish();
    }

    private void onRequestPermissionSuccess() {
        processBusinessOnAllPermissionGranted();
    }

    private void askingPermission(String[] permissions) {
        requestPermissions(permissions, REQUEST_CODE_ASK_PERMISSIONS);
    }

    public void processBusinessOnAllPermissionGranted() {

    }
}