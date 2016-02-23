package me.yokeyword.fragmentation.anim;

import android.support.annotation.AnimRes;

/**
 * Fragment动画实体类
 * Created by YoKeyword on 16/2/4.
 */
public class FragmentAnimator {
    @AnimRes
    protected int enter;
    @AnimRes
    protected int exit;
    @AnimRes
    protected int popEnter;
    @AnimRes
    protected int popExit;

    public FragmentAnimator() {
    }

    public FragmentAnimator(int enter, int exit) {
        this.enter = enter;
        this.exit = exit;
    }

    public FragmentAnimator(int enter, int exit, int popEnter, int popExit) {
        this.enter = enter;
        this.exit = exit;
        this.popEnter = popEnter;
        this.popExit = popExit;
    }

    public int getEnter() {
        return enter;
    }

    public void setEnter(int enter) {
        this.enter = enter;
    }

    public int getExit() {
        return exit;
    }

    public void setExit(int exit) {
        this.exit = exit;
    }

    public int getPopEnter() {
        return popEnter;
    }

    public void setPopEnter(int popEnter) {
        this.popEnter = popEnter;
    }

    public int getPopExit() {
        return popExit;
    }

    public void setPopExit(int popExit) {
        this.popExit = popExit;
    }
}
