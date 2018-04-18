/*
 * Copyright (C) 2018, TP-LINK TECHNOLOGIES CO., LTD.
 *
 * DataManager.java
 *
 * Description 所有媒体数据管理类
 *
 * Author LinJinLong
 *
 * Ver 1.0, 2018-04-18 LinJinLong, Create file
 */
package com.tplink.gallery.data;

public class DataManager {

    private static DataManager dataManager = null;

    private DataManager(){}

    public synchronized static DataManager getDataManager() {
        if (dataManager == null) {
            dataManager = new DataManager();
        }
        return dataManager;
    }
}
