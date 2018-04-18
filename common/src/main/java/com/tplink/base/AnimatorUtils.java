/*
 * Copyright (C) 2018, TP-LINK TECHNOLOGIES CO., LTD.
 *
 * AnimatorUtils.java
 *
 * Description 动画相关工具类
 *
 * Author Wang tao
 *
 * Ver 1.0, 2018-1-4 Wang tao, Create file
 */
package com.tplink.base;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.view.View;
import android.widget.CheckBox;

import com.tplink.common.R;

public class AnimatorUtils {

    public static final int TOTAL_ANIM_TIME = 150;

    private AnimatorUtils(){}

    public static Animator getAlphaAnimator(View view, long duration, float alphaFrom, float alphaTo) {
        return ObjectAnimator.ofFloat(view, View.ALPHA, alphaFrom, alphaTo).setDuration(duration);
    }

    public static Animator getTranslationXAnimator(View view, long duration, float xFrom, float xTo) {
        return ObjectAnimator.ofFloat(view, View.TRANSLATION_X, xFrom, xTo).setDuration(duration);
    }

    public static void enterSelectionModeWithAnim(Context context, final CheckBox checkBox, View container) {
        int transX = context.getResources().getInteger(R.integer.common_translation_x_distance);
        Animator checkBoxAlphaAnim = AnimatorUtils.getAlphaAnimator(checkBox, TOTAL_ANIM_TIME, 0, 1);
        Animator checkBoxTransXAnim = AnimatorUtils.getTranslationXAnimator(checkBox, TOTAL_ANIM_TIME, transX, 0);
        Animator itemTransXAnim = AnimatorUtils.getTranslationXAnimator(container, TOTAL_ANIM_TIME, transX, 0);

        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playTogether(checkBoxAlphaAnim, checkBoxTransXAnim, itemTransXAnim);
        animatorSet.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {

            }

            @Override
            public void onAnimationEnd(Animator animator) {
                checkBox.setAlpha(1);
                checkBox.setTranslationX(0);
            }

            @Override
            public void onAnimationCancel(Animator animator) {
                checkBox.setAlpha(1);
                checkBox.setTranslationX(0);
            }

            @Override
            public void onAnimationRepeat(Animator animator) {

            }
        });
        animatorSet.start();
    }

    public static void exitSelectionModeWithAnim(Context context, final CheckBox checkBox, final View container) {
        int transX = context.getResources().getInteger(R.integer.common_translation_x_distance);
        Animator checkBoxAlphaAnim = AnimatorUtils.getAlphaAnimator(checkBox, TOTAL_ANIM_TIME, 1, 0);
        Animator checkBoxTransXAnim = AnimatorUtils.getTranslationXAnimator(checkBox, TOTAL_ANIM_TIME, 0, transX);
        Animator itemTransXAnim = AnimatorUtils.getTranslationXAnimator(container, TOTAL_ANIM_TIME, 0, transX);

        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playTogether(checkBoxAlphaAnim, checkBoxTransXAnim, itemTransXAnim);
        animatorSet.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {

            }

            @Override
            public void onAnimationEnd(Animator animator) {
                // 动画使用了位移和透明度,在动画结束时候需要将其属性重置
                checkBox.setVisibility(View.GONE);
                checkBox.setAlpha(1);
                checkBox.setTranslationX(0);
                checkBox.setChecked(false);
                container.setTranslationX(0);
            }

            @Override
            public void onAnimationCancel(Animator animator) {
                checkBox.setVisibility(View.GONE);
                checkBox.setAlpha(1);
                checkBox.setTranslationX(0);
                checkBox.setChecked(false);
                container.setTranslationX(0);
            }

            @Override
            public void onAnimationRepeat(Animator animator) {

            }
        });
        animatorSet.start();
    }
}
