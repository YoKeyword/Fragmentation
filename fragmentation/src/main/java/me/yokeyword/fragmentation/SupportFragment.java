package me.yokeyword.fragmentation;

import android.app.Activity;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.IntDef;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.Collections;

import me.yokeyword.fragmentation.anim.FragmentAnimator;
import me.yokeyword.fragmentation.helper.FragmentationNullException;
import me.yokeyword.fragmentation.helper.OnAnimEndListener;

/**
 * Created by YoKeyword on 16/1/22.
 */
public class SupportFragment extends Fragment {
    private static final String STATE_SAVE_ENTER = "yokeyword_sate_save_enter";
    private static final String STATE_SAVE_EXIT = "yokeyword_sate_save_exit";
    private static final String STATE_SAVE_POP_ENTER = "yokeyword_sate_save_pop_enter";
    private static final String STATE_SAVE_POP_EXIT = "yokeyword_sate_save_pop_exit";

    // LaunchMode
    public static final int STANDARD = 0;
    public static final int SINGLETOP = 1;
    public static final int SINGLETASK = 2;

    public static final int RESULT_CANCELED = 0;
    public static final int RESULT_OK = -1;

    private int mRequestCode = 0, mResultCode = RESULT_CANCELED;
    private Bundle mResultBundle;
    private Bundle mNewBundle;
    private boolean mIsRoot;

    private InputMethodManager mIMM;
    private boolean mNeedAnimListener;
    private OnAnimEndListener mOnAnimEndListener;

    protected SupportActivity _mActivity;
    protected Fragmentation mFragmentation;

    private int mEnter, mExit, mPopEnter, mPopExit;
    private Animation mNoAnim, mEnterAnim, mExitAnim, mPopEnterAnim, mPopExitAnim;

    private boolean mNeedHideSoft;  // 隐藏软键盘
    protected boolean mLocking; // 是否加锁 用于SwipeBackLayout

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        if (activity instanceof SupportActivity) {
            this._mActivity = (SupportActivity) activity;
        } else {
            throw new ClassCastException(activity.toString() + "must extends SupportActivity!");
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        mIMM = (InputMethodManager) _mActivity.getSystemService(Context.INPUT_METHOD_SERVICE);

        Bundle bundle = getArguments();
        if (bundle != null) {
            mRequestCode = bundle.getInt(Fragmentation.ARG_REQUEST_CODE, 0);
            mResultCode = bundle.getInt(Fragmentation.ARG_RESULT_CODE, 0);
            mResultBundle = bundle.getBundle(Fragmentation.ARG_RESULT_BUNDLE);
            mIsRoot = bundle.getBoolean(Fragmentation.ARG_IS_ROOT, false);
        }

        if (savedInstanceState == null) {
            FragmentAnimator fragmentAnimator = onCreateFragmentAnimation();
            if (fragmentAnimator == null) {
                SupportActivity activity = _mActivity;
                fragmentAnimator = activity.getFragmentAnimator();
            }

            mEnter = fragmentAnimator.getEnter();
            mExit = fragmentAnimator.getExit();
            mPopEnter = fragmentAnimator.getPopEnter();
            mPopExit = fragmentAnimator.getPopExit();
        } else {
            mEnter = savedInstanceState.getInt(STATE_SAVE_ENTER);
            mExit = savedInstanceState.getInt(STATE_SAVE_EXIT);
            mPopEnter = savedInstanceState.getInt(STATE_SAVE_POP_ENTER);
            mPopExit = savedInstanceState.getInt(STATE_SAVE_POP_EXIT);
        }

        handleNoAnim();

        mNoAnim = AnimationUtils.loadAnimation(_mActivity, R.anim.no_anim);
        mEnterAnim = AnimationUtils.loadAnimation(_mActivity, mEnter);
        mExitAnim = AnimationUtils.loadAnimation(_mActivity, mExit);
        mPopEnterAnim = AnimationUtils.loadAnimation(_mActivity, mPopEnter);
        mPopExitAnim = AnimationUtils.loadAnimation(_mActivity, mPopExit);
    }

    private void handleNoAnim() {
        if (mEnter == 0) {
            mEnter = R.anim.no_anim;
        }
        if (mExit == 0) {
            mExit = R.anim.no_anim;
        }
        if (mPopEnter == 0) {
            mPopEnter = R.anim.no_anim;
        }
        if (mPopExit == 0) {
            mPopExit = R.anim.pop_exit_no_anim;
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(STATE_SAVE_ENTER, mEnter);
        outState.putInt(STATE_SAVE_EXIT, mExit);
        outState.putInt(STATE_SAVE_POP_ENTER, mPopEnter);
        outState.putInt(STATE_SAVE_POP_EXIT, mPopExit);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        View view = getView();
        initFragmentBackground(view);
        assert view != null;
        view.setClickable(true);

        mFragmentation = _mActivity.getFragmentation();

        // 解决栈内有嵌套Fragment时,APP被强杀后恢复BUG问题(顶层Fragment为hidden状态)
        if (savedInstanceState != null) {
            _mActivity.getHandler().post(new Runnable() {
                @Override
                public void run() {
                    if (SupportFragment.this == getTopFragment() && isHidden()) {
                        getFragmentManager().beginTransaction()
                                .show(SupportFragment.this)
                                .commit();
                    }
                }
            });
        }
    }

    protected void initFragmentBackground(View view) {
        if (view != null && view.getBackground() == null) {
            int background = getWindowBackground();
            view.setBackgroundResource(background);
        }
    }

    protected int getWindowBackground() {
        TypedArray a = _mActivity.getTheme().obtainStyledAttributes(new int[]{
                android.R.attr.windowBackground
        });
        int background = a.getResourceId(0, 0);
        a.recycle();
        return background;
    }

    long getExitAnimDuration() {
        return mExitAnim.getDuration();
    }

    long getPopEnterAnimDuration() {
        return mPopEnterAnim.getDuration();
    }


    /**
     * 设定当前Fragmemt动画,优先级比在SupportActivity里高
     */
    protected FragmentAnimator onCreateFragmentAnimation() {
        return null;
    }

    @Override
    public Animation onCreateAnimation(int transit, boolean enter, int nextAnim) {
        if (_mActivity.mPopMulitpleNoAnim || mLocking) {
            return mNoAnim;
        }
        if (transit == FragmentTransaction.TRANSIT_FRAGMENT_OPEN) {
            if (enter) {
                if (mIsRoot) {
                    return mNoAnim;
                }
                if (mNeedAnimListener) {
                    mEnterAnim.setAnimationListener(new Animation.AnimationListener() {
                        @Override
                        public void onAnimationStart(Animation animation) {

                        }

                        @Override
                        public void onAnimationEnd(Animation animation) {
                            mEnterAnim.setAnimationListener(null);
                            mNeedAnimListener = false;
                            if (mOnAnimEndListener != null) {
                                mOnAnimEndListener.onAnimationEnd();
                            }
                        }

                        @Override
                        public void onAnimationRepeat(Animation animation) {

                        }
                    });
                }
                return mEnterAnim;
            } else {
                return mPopExitAnim;
            }
        } else if (transit == FragmentTransaction.TRANSIT_FRAGMENT_CLOSE) {
            if (enter) {
                return mPopEnterAnim;
            } else {
                return mExitAnim;
            }
        }
        return null;
    }

    /**
     * 隐藏软键盘
     */
    protected void hideSoftInput() {
        if (getView() != null) {
            mIMM.hideSoftInputFromWindow(getView().getWindowToken(), 0);
        }
    }

    /**
     * 显示软键盘
     */
    protected void showSoftInput(final View view) {
        if (view == null) return;
        view.requestFocus();
        mNeedHideSoft = true;
        view.postDelayed(new Runnable() {
            @Override
            public void run() {
                mIMM.showSoftInput(view, InputMethodManager.SHOW_FORCED);
            }
        }, 200);
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);

        if (hidden) {
            onHidden();
        } else {
            onShow();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mNeedHideSoft) {
            hideSoftInput();
        }
    }

    /**
     * 显示
     */
    protected void onShow() {
    }

    /**
     * 隐藏 不可见
     */
    protected void onHidden() {
    }


    @IntDef({STANDARD, SINGLETOP, SINGLETASK})
    @Retention(RetentionPolicy.SOURCE)
    public @interface LaunchMode {
    }


    /**
     * 按下返回键触发
     *
     * @return
     */
    public boolean onBackPressedSupport() {
        SupportFragment fragment = getTopChildFragment();
        if (fragment != null) {
            boolean result = fragment.onBackPressedSupport();
            if (result) return true;
        }
        return false;
    }

    /**
     * 获取栈内的framgent对象
     *
     * @param fragmentClass
     */
    public <T extends SupportFragment> T findFragment(Class<T> fragmentClass) {
        if (mFragmentation == null) {
            throw new FragmentationNullException("findFragment()");
        }
        return mFragmentation.findStackFragment(fragmentClass, getFragmentManager());
    }

    /**
     * 获取栈内的子framgent对象
     *
     * @param fragmentClass
     */
    @SuppressWarnings("unchecked")
    public <T extends Fragment> T findChildFragment(Class<T> fragmentClass) {
        Fragment fragment = getChildFragmentManager().findFragmentByTag(fragmentClass.getName());
        if (fragment == null) {
            return null;
        }
        return (T) fragment;
    }


    /**
     * 得到位于栈顶的Fragment
     *
     * @return
     */
    public SupportFragment getTopFragment() {
        if (mFragmentation == null) {
            throw new FragmentationNullException("getTopFragment()");
        }
        return mFragmentation.getTopFragment(getFragmentManager());
    }

    /**
     * 得到位于栈顶的子Fragment
     *
     * @return
     */
    public SupportFragment getTopChildFragment() {
        if (mFragmentation == null) {
            throw new FragmentationNullException("getTopFragment()");
        }
        return mFragmentation.getTopFragment(getChildFragmentManager());
    }

    void popForSwipeBack() {
        mLocking = true;
        mFragmentation.back(getFragmentManager());
        mLocking = false;
    }

    /**
     * 出栈
     */
    public void pop() {
        mFragmentation.back(getFragmentManager());
    }

    /**
     * 出栈到目标fragment
     *
     * @param fragmentClass 目标fragment
     * @param includeSelf   是否包含该fragment
     */
    public void popTo(Class<?> fragmentClass, boolean includeSelf) {
        mFragmentation.popTo(fragmentClass, includeSelf, null, getFragmentManager());
    }

    /**
     * 用于出栈后,立刻进行FragmentTransaction操作
     */
    public void popTo(Class<?> fragmentClass, boolean includeSelf, Runnable afterPopTransactionRunnable) {
        mFragmentation.popTo(fragmentClass, includeSelf, afterPopTransactionRunnable, getFragmentManager());
    }

    public void start(SupportFragment toFragment) {
        start(toFragment, STANDARD);
    }

    public void start(SupportFragment toFragment, @LaunchMode int launchMode) {
        if (mFragmentation == null) {
            throw new FragmentationNullException("start()");
        }

        mFragmentation.dispatchTransaction(this, toFragment, 0, launchMode, Fragmentation.TYPE_ADD);
    }

    public void startForResult(SupportFragment to, int requestCode) {
        if (mFragmentation == null) {
            throw new FragmentationNullException("startForResult()");
        }

        mFragmentation.dispatchTransaction(this, to, requestCode, STANDARD, Fragmentation.TYPE_ADD);
    }

    public void startWithFinish(SupportFragment to) {
        if (mFragmentation == null) {
            throw new FragmentationNullException("startWithFinish()");
        }
        mFragmentation.dispatchTransaction(this, to, 0, STANDARD, Fragmentation.TYPE_ADD_FINISH);
    }

    public void startChildFragment(int childContainer, Fragment childFragment, boolean addToBack) {
        FragmentTransaction ft = getChildFragmentManager().beginTransaction();
        ft.add(childContainer, childFragment, childFragment.getClass().getName())
                .show(childFragment);
        if (addToBack) {
            ft.addToBackStack(childFragment.getClass().getName());
        }
        ft.commit();
    }

    /**
     * 替换子Fragment
     *
     * @param childContainer
     * @param childFragment
     * @param addToBack
     */
    public void replaceChildFragment(int childContainer, Fragment childFragment, boolean addToBack) {
        FragmentTransaction ft = getChildFragmentManager().beginTransaction();
        ft.replace(childContainer, childFragment, childFragment.getClass().getName())
                .show(childFragment);
        if (addToBack) {
            ft.addToBackStack(childFragment.getClass().getName());
        }
        ft.commit();
    }

    /**
     * 设置Result数据 (通过startForResult)
     * @param resultCode  resultCode
     * @param bundle    设置Result数据
     */
    public void setFramgentResult(int resultCode, Bundle bundle) {
        mResultCode = resultCode;
        mResultBundle = bundle;

        Bundle args = getArguments();
        if (args == null) {
            args = new Bundle();
        }
        args.putInt(Fragmentation.ARG_RESULT_CODE, mResultCode);
        args.putBundle(Fragmentation.ARG_RESULT_BUNDLE, mResultBundle);
    }

    /**
     * 接受Result数据 (通过startForResult的返回数据)
     * @param requestCode   requestCode
     * @param resultCode    resultCode
     * @param data          Result数据
     */
    protected void onFragmentResult(int requestCode, int resultCode, Bundle data) {
    }

    int getRequestCode() {
        return mRequestCode;
    }

    int getResultCode() {
        return mResultCode;
    }

    /**
     * 在start(TargetFragment,LaunchMode)时,启动模式为SingleTask/SingleTop, TargetFragment回调该方法
     * @param args 通过上个Fragment的putNewBundle(Bundle newBundle)时传递的数据
     */
    protected void onNewBundle(Bundle args) {
    }

    /**
     * 添加NewBundle,用于启动模式为SingleTask/SingleTop时
     */
    public void putNewBundle(Bundle newBundle) {
        this.mNewBundle = newBundle;
    }

    Bundle getNewBundle() {
        return mNewBundle;
    }


    Bundle getResultBundle() {
        return mResultBundle;
    }

    void setNeedAnimListener(boolean needAnimListener, OnAnimEndListener onAnimEndListener) {
        this.mNeedAnimListener = needAnimListener;
        this.mOnAnimEndListener = onAnimEndListener;
    }
}
