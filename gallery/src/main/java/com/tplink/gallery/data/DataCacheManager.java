/*
 * Copyright (C) 2018, TP-LINK TECHNOLOGIES CO., LTD.
 *
 * DataCacheManager.java
 *
 * Description 媒体缓存管理
 *
 * Author LinJinLong
 *
 * Ver 1.0, 2018-04-18 LinJinLong, Create file
 */
package com.tplink.gallery.data;

public class DataCacheManager {

    private static DataCacheManager dataManager = null;

    private DataCacheManager(){}

    public synchronized static DataCacheManager getDataManager() {
        if (dataManager == null) {
            dataManager = new DataCacheManager();
        }
        return dataManager;
    }
}
