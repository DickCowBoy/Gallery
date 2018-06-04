package com.tplink.gallery.utils;

public class Utils {

    private Utils() {

    }

    public static int ceilLog2(float value) {
        int i;
        for (i = 0; i < 31; i++) {
            if ((1 << i) >= value) break;
        }
        return i;
    }
}
