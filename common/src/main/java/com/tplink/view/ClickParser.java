/*
 * Copyright (C) 2018, TP-LINK TECHNOLOGIES CO., LTD.
 *
 * ClickParser.java
 *
 * Description 获取点击区域
 *
 * Author LinJinLong
 *
 * Ver 1.0, 2018-02-12 LinJinLong, Create file
 */
package com.tplink.view;


import android.graphics.RectF;

public interface ClickParser {
    boolean clickRegion();
    void setClickRectF(RectF clickRectF);
}