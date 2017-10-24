package me.yokeyword.fragmentation_swipeback.core;

import me.yokeyword.fragmentation.SwipeBackLayout;

/**
 * Created by YoKey on 17/6/29.
 */
public interface ISwipeBackActivity {

    SwipeBackLayout getSwipeBackLayout();

    void setSwipeBackEnable(boolean enable);

    void setEdgeLevel(SwipeBackLayout.EdgeLevel edgeLevel);

    void setEdgeLevel(int widthPixel);

    /**
     * 限制SwipeBack的条件,默认栈内Fragment数 <= 1时 , 优先滑动退出Activity , 而不是Fragment
     *
     * @return true: Activity可以滑动退出, 并且总是优先;  false: Fragment优先滑动退出
     */
    boolean swipeBackPriority();
}
