package com.tplink.gallery.utils;

public class ThreadUtils {

    private ThreadUtils(){}

    public static void waitWithoutInterrupt(Object object) {
        try {
            object.wait();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
