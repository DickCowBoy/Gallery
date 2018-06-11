package com.tplink.gallery.utils;

import android.os.StatFs;
import android.util.Log;

import java.io.File;

public class StorageUtils {

    private static final String TAG = "StorageUtils";
    private static final int STORAGE_CAPACITY_BASE = 1024;
    private static final int VIDEO_MUTE_MIN_SPACE_DEFAULT = 48;
    /**
     * calculate the space for video muted is enough or not lowStorageThreshold
     * is reserve space. ram optimize projec is 9M, the others is 48M.
     */
    public static boolean isSpaceEnough(File srcFile) {
        long spaceNeed;
        long lowStorageThreshold = VIDEO_MUTE_MIN_SPACE_DEFAULT * STORAGE_CAPACITY_BASE
                * STORAGE_CAPACITY_BASE;
        spaceNeed = srcFile.length() + lowStorageThreshold;
        if (getAvailableSpace(srcFile.getPath()) < spaceNeed) {
            Log.i(TAG, "<isSpaceEnough> space is not enough!!!");
            return false;
        } else {
            return true;
        }
    }
    /// @}


    /// M: [BUG.ADD] disable mute when sdcard is full. @{

    /**
     * get available space which storage source video is in.
     *
     * @return the available sapce size, -1 means max storage size.
     */
    public static long getAvailableSpace(String path) {
        // Here just use one directory to stat fs.
        StatFs stat = new StatFs(path);
        long availableSize = stat.getAvailableBlocks() * (long) stat.getBlockSize();
        Log.i(TAG, "<getAvailableSpace> path " + path + ", availableSize(MB) "
                + (availableSize / STORAGE_CAPACITY_BASE / STORAGE_CAPACITY_BASE));
        return availableSize;
    }
}
