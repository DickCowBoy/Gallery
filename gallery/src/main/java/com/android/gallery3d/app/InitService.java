/*
 * Copyright (C) 2018, TP-LINK TECHNOLOGIES CO., LTD.
 *
 * InitService.java
 *
 * 相机模块通过绑定该服务，确保预览图片时避免冷启动
 *
 * Author LinJinLong
 *
 * Ver 1.0, 17-3-16, LinJinLong, Create file
 */
package com.android.gallery3d.app;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteException;


public class InitService extends Service {
    @Override
    public IBinder onBind(Intent intent) {
        return new InitBinder();
    }



    /**
     * 初始化必要环境，完成一些类的加载，供相机调用避免冷启动慢
     */
    private void init() {
        try {
            // TODO LJL 提前加载类
            Class.forName("com.android.gallery3d.app.PickMediaActivity");

        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    class InitBinder extends IInitInterface.Stub {

        @Override
        public void initApplication() throws RemoteException {
            InitService.this.init();
        }
    }
}
