package me.yokeyword.fragmentation.helper.internal;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;

import java.util.List;

import me.yokeyword.fragmentation.SupportFragment;

/**
 * Created by YoKey on 17/4/4.
 */

public class VisibleDelegate {
    private static final String FRAGMENTATION_STATE_SAVE_IS_INVISIBLE_WHEN_LEAVE = "fragmentation_invisible_when_leave";

    // SupportVisible相关
    private boolean mIsSupportVisible;
    private boolean mNeedDispatch = true;
    private boolean mInvisibleWhenLeave;
    private boolean mIsFirstVisible = true;
    private boolean mFixStatePagerAdapter;
    private Bundle mSaveInstanceState;

    private SupportFragment mSupportFragment;

    public VisibleDelegate(SupportFragment fragment) {
        this.mSupportFragment = fragment;
    }

    public void onCreate(@Nullable Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            mSaveInstanceState = savedInstanceState;
            if (!mFixStatePagerAdapter) { // setUserVisibleHint() may be called before onCreate()
                mInvisibleWhenLeave = savedInstanceState.getBoolean(FRAGMENTATION_STATE_SAVE_IS_INVISIBLE_WHEN_LEAVE);
            }
        }
    }

    public void onSaveInstanceState(Bundle outState) {
        outState.putBoolean(FRAGMENTATION_STATE_SAVE_IS_INVISIBLE_WHEN_LEAVE, mInvisibleWhenLeave);
    }

    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        if (!mInvisibleWhenLeave && !mSupportFragment.isHidden() &&
                (mSupportFragment.getUserVisibleHint() || mFixStatePagerAdapter)) {
            if ((mSupportFragment.getParentFragment() != null && isFragmentVisible(mSupportFragment.getParentFragment()))
                    || mSupportFragment.getParentFragment() == null) {
                mNeedDispatch = false;
                dispatchSupportVisible(true);
            }
        }
    }

    public void onResume() {
        if (!mIsFirstVisible) {
            if (!mIsSupportVisible && !mInvisibleWhenLeave && isFragmentVisible(mSupportFragment)) {
                mNeedDispatch = false;
                dispatchSupportVisible(true);
            }
        }
    }

    public void onPause() {
        if (mIsSupportVisible && isFragmentVisible(mSupportFragment)) {
            mNeedDispatch = false;
            mInvisibleWhenLeave = false;
            dispatchSupportVisible(false);
        } else {
            mInvisibleWhenLeave = true;
        }
    }

    public void onHiddenChanged(boolean hidden) {
        if (mSupportFragment.isResumed()) {
            dispatchSupportVisible(!hidden);
        }
    }

    public void onDestroyView() {
        mIsFirstVisible = true;
        mFixStatePagerAdapter = false;
    }

    public void setUserVisibleHint(boolean isVisibleToUser) {
        if (mSupportFragment.isResumed() || (mSupportFragment.isDetached() && isVisibleToUser)) {
            if (!mIsSupportVisible && isVisibleToUser) {
                dispatchSupportVisible(true);
            } else if (mIsSupportVisible && !isVisibleToUser) {
                dispatchSupportVisible(false);
            }
        } else if (isVisibleToUser) {
            mInvisibleWhenLeave = false;
            mFixStatePagerAdapter = true;
        }
    }

    private void dispatchSupportVisible(boolean visible) {
        mIsSupportVisible = visible;

        if (visible) {
            mSupportFragment.onSupportVisible();

            if (mIsFirstVisible) {
                mIsFirstVisible = false;
                mSupportFragment.onLazyInitView(mSaveInstanceState);
            }
        } else {
            mSupportFragment.onSupportInvisible();
        }

        if (!mNeedDispatch) {
            mNeedDispatch = true;
        } else {
            FragmentManager fragmentManager = mSupportFragment.getChildFragmentManager();
            if (fragmentManager != null) {
                List<Fragment> childFragments = fragmentManager.getFragments();
                if (childFragments != null) {
                    for (Fragment child : childFragments) {
                        if (child instanceof SupportFragment && !child.isHidden() && child.getUserVisibleHint()) {
                            ((SupportFragment) child).getVisibleDelegate().dispatchSupportVisible(visible);
                        }
                    }
                }
            }
        }
    }

    private boolean isFragmentVisible(Fragment fragment) {
        return !fragment.isHidden() && fragment.getUserVisibleHint();
    }

    public boolean isSupportVisible() {
        return mIsSupportVisible;
    }
}
