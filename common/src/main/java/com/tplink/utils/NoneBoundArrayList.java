package com.tplink.utils;

import android.util.Log;

import java.util.ArrayList;

public class NoneBoundArrayList<E> extends ArrayList<E> {

    @Override
    public E get(int index) {
        if (index < 0 || index >= size()) {
            Log.e("NoneBoundArrayList", "IndexOutOfBounds size=" + size() +" index=" + index);
            return null;
        } else {
            return super.get(index);
        }
    }
}
