package me.yokeyword.fragmentation_swipeback;

import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;

import me.yokeyword.fragmentation.SupportFragment;
import me.yokeyword.fragmentation.SwipeBackLayout;

/**
 * Created by YoKeyword on 16/4/19.
 */
public abstract class SwipeBackFragment extends SupportFragment {
    private SwipeBackLayout mSwipeBackLayout;

    protected View toSwipeBackFragment(View view) {
        mSwipeBackLayout = new SwipeBackLayout(_mActivity);
        ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        mSwipeBackLayout.setLayoutParams(params);
        mSwipeBackLayout.setBackgroundColor(Color.TRANSPARENT);
        mSwipeBackLayout.addView(view);
        mSwipeBackLayout.setFragment(this, view);

        return mSwipeBackLayout;
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (hidden && mSwipeBackLayout != null) {
            mSwipeBackLayout.hiddenFragment();
        }
    }

    public SwipeBackLayout getSwipeBackLayout() {
        return mSwipeBackLayout;
    }

    public void setSwipeBackEnable(boolean enable) {
        mSwipeBackLayout.setEnableGesture(enable);
    }

    @Override
    protected void initFragmentBackground(View view, int bgColor) {
        if (!(view instanceof SwipeBackLayout) && view != null && view.getBackground() == null) {
            if (bgColor != 0) {
                view.setBackgroundColor(bgColor);
            } else {
                view.setBackgroundColor(Color.WHITE);
            }
        } else {
            if (view instanceof SwipeBackLayout) {
                View childView = ((SwipeBackLayout) view).getChildAt(0);
                if (childView != null && childView.getBackground() == null) {
                    if (bgColor != 0) {
                        childView.setBackgroundColor(bgColor);
                    } else {
                        childView.setBackgroundColor(Color.WHITE);
                    }
                }
            }
        }
    }
}
