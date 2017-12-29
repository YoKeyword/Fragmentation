package me.yokeyword.fragmentation.queue;

/**
 * Created by YoKey on 17/12/28.
 */

public abstract class Action {
    public static final int ACTION_POP = 1;
    public static final int ACTION_BACK = 2;

    public int action = 0;
    public long duration = 0;

    public Action() {
    }

    public Action(int action) {
        this.action = action;
    }

    public Action(int action, long duration) {
        this(action);
        this.duration = duration;
    }

    public abstract void run();
}
