package me.yokeyword.fragmentation.queue;

import android.support.v4.app.FragmentManager;

/**
 * Created by YoKey on 17/12/28.
 */

public abstract class Action {
    public static final int BUFFER_TIME = 60;

    public static final int ACTION_NORMAL = 0;
    public static final int ACTION_LOAD = 1;
    public static final int ACTION_POP = 2;
    public static final int ACTION_POP_MOCK = 3;
    public static final int ACTION_BACK = 4;

    public FragmentManager fragmentManager;
    public int action = ACTION_NORMAL;
    public long duration = 0;

    public Action() {
    }

    public Action(int action) {
        this.action = action;
        if (action == ACTION_POP_MOCK) {
            duration = BUFFER_TIME;
        }
    }

    public Action(int action, FragmentManager fragmentManager) {
        this(action);
        this.fragmentManager = fragmentManager;
    }

    public abstract void run();
}
