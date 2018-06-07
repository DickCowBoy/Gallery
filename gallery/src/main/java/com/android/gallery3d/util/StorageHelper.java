/*
 * Copyright (C), 2015, TP-LINK TECHNOLOGIES CO., LTD.
 *
 * StorageHelper.java
 *
 * Author yulibo
 *
 * Ver 1.0, Aug 19, 2015, yulibo, Create file
 */

package com.android.gallery3d.util;

import android.content.Context;
import android.os.Environment;
import android.os.storage.StorageManager;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class StorageHelper {

    private StorageManager mStorageManager;

    public StorageHelper(Context context) {
        mStorageManager = (StorageManager) context.getSystemService(Context.STORAGE_SERVICE);
    }

    public synchronized String[] getVolumePaths() {
        try {
            // 利用反射，获取所有所有挂载点
            Method method = mStorageManager.getClass().getMethod("getVolumePaths");
            return (String[]) method.invoke(mStorageManager);
        } catch (Exception e) {
            // 如果出错，则采用安卓默认api获取默认内置存储卡
            try {
                File externalStorage = Environment.getExternalStorageDirectory();
                String path = externalStorage.getCanonicalPath();
                return new String[] { path };
            } catch (IOException e1) {
                e1.printStackTrace();
                return null;
            }
        }
    }

    public String getStorageVolumeDescription(String path) {
        Object vol = getStorageVolume(path);
        return getSVDescription(vol);
    }

    public String getStorageVolumePath(String path) {
        Object vol = getStorageVolume(path);
        return getSVPath(vol);
    }

    private synchronized Object[] getStorageVolumes() {
        try {
            Method method = mStorageManager.getClass().getMethod("getVolumeList");
            return (Object[]) method.invoke(mStorageManager);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    private Object getStorageVolume(String path) {
        Object[] volumes = getStorageVolumes();
        if (volumes == null) {
            return null;
        }
        String fso = getAbsPath(path);
        for (Object vol : volumes) {
            String svPath = getSVPath(vol);
            if (svPath != null && fso.startsWith(svPath)) {
                return vol;
            }
        }
        return null;
    }

    private static String getAbsPath(String path) {
        try {
            return new File(path).getCanonicalPath();
        } catch (IOException e) {
            return new File(path).getAbsolutePath();
        }

    }

    private String getSVPath(Object vol) {
        if (vol == null) {
            return null;
        }
        try {
            Class<?> clazz = Class.forName("android.os.storage.StorageVolume");
            Method method = clazz.getMethod("getPath");
            return (String) method.invoke(vol);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
        return null;
    }

    private String getSVDescription(Object vol) {
        if (vol == null) {
            return null;
        }
        try {
            Class<?> clazz = Class.forName("android.os.storage.StorageVolume");
            Method method = clazz.getMethod("getUserLabel");
            return (String) method.invoke(vol);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
        return null;
    }

}
