package me.yokeyword.fragmentation_swipeback.core;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.ViewGroup;

import me.yokeyword.fragmentation.ISupportActivity;
import me.yokeyword.fragmentation.SwipeBackLayout;

/**
 * Created by YoKey on 17/6/29.
 */

public class SwipeBackActivityDelegate {
    private FragmentActivity mActivity;
    private SwipeBackLayout mSwipeBackLayout;

    public SwipeBackActivityDelegate(ISwipeBackActivity swipeBackActivity) {
        if (!(swipeBackActivity instanceof FragmentActivity) || !(swipeBackActivity instanceof ISupportActivity))
            throw new RuntimeException("Must extends FragmentActivity/AppCompatActivity and implements ISupportActivity");
        mActivity = (FragmentActivity) swipeBackActivity;
    }

    public void onCreate(Bundle savedInstanceState) {
        onActivityCreate();
    }

    public void onPostCreate(Bundle savedInstanceState) {
        mSwipeBackLayout.attachToActivity(mActivity);
    }

    public SwipeBackLayout getSwipeBackLayout() {
        return mSwipeBackLayout;
    }

    public void setSwipeBackEnable(boolean enable) {
        mSwipeBackLayout.setEnableGesture(enable);
    }

    public void setEdgeLevel(SwipeBackLayout.EdgeLevel edgeLevel) {
        mSwipeBackLayout.setEdgeLevel(edgeLevel);
    }

    public void setEdgeLevel(int widthPixel) {
        mSwipeBackLayout.setEdgeLevel(widthPixel);
    }

    /**
     * 限制SwipeBack的条件,默认栈内Fragment数 <= 1时 , 优先滑动退出Activity , 而不是Fragment
     *
     * @return true: Activity可以滑动退出, 并且总是优先;  false: Fragment优先滑动退出
     */
    public boolean swipeBackPriority() {
        return mActivity.getSupportFragmentManager().getBackStackEntryCount() <= 1;
    }

    private void onActivityCreate() {
        mActivity.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        mActivity.getWindow().getDecorView().setBackgroundDrawable(null);
        mSwipeBackLayout = new SwipeBackLayout(mActivity);
        ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        mSwipeBackLayout.setLayoutParams(params);
    }
}
