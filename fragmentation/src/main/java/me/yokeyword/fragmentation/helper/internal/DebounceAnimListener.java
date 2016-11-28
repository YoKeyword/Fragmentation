package me.yokeyword.fragmentation.helper.internal;

import android.view.animation.Animation;

import me.yokeyword.fragmentation.SupportFragment;

/**
 * Hide
 * 转场动画监听器: 主要是为了监听入场动画
 * Created by YoKeyword on 16/6/23.
 */
public class DebounceAnimListener implements Animation.AnimationListener {
    private SupportFragment fragment;

    public DebounceAnimListener(SupportFragment fragment) {
        this.fragment = fragment;
    }

    @Override
    public void onAnimationStart(Animation animation) {
    }

    @Override
    public void onAnimationEnd(Animation animation) {
        fragment.notifyEnterAnimEnd();
    }

    @Override
    public void onAnimationRepeat(Animation animation) {

    }
}
