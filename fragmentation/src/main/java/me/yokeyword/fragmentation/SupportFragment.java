package me.yokeyword.fragmentation;

import android.app.Activity;
import android.content.Context;
import android.content.res.TypedArray;
import android.os.Bundle;
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

import me.yokeyword.fragmentation.anim.FragmentAnimator;
import me.yokeyword.fragmentation.helper.OnEnterAnimEndListener;

/**
 * Created by YoKeyword on 16/1/22.
 */
public class SupportFragment extends Fragment {
    private static final String FRAGMENTATION_STATE_SAVE_ANIMATOR = "fragmentation_state_save_animator";
    private static final String FRAGMENTATION_STATE_SAVE_IS_HIDDEN = "fragmentation_state_save_status";

    // LaunchMode
    public static final int STANDARD = 0;
    public static final int SINGLETOP = 1;
    public static final int SINGLETASK = 2;

    public static final int RESULT_CANCELED = 0;
    public static final int RESULT_OK = -1;
    private static final long SHOW_SPACE = 200L;

    private int mRequestCode = 0, mResultCode = RESULT_CANCELED;
    private Bundle mResultBundle;
    private Bundle mNewBundle;
    private boolean mIsRoot;

    private InputMethodManager mIMM;
    private OnEnterAnimEndListener mOnAnimEndListener; // fragmentation所用

    protected SupportActivity _mActivity;
    protected Fragmentation mFragmentation;

    private FragmentAnimator mFragmentAnimator;
    private Animation mNoAnim, mEnterAnim, mExitAnim, mPopEnterAnim, mPopExitAnim;

    private boolean mNeedHideSoft;  // 隐藏软键盘
    protected boolean mLocking; // 是否加锁 用于SwipeBackLayout
    private boolean mIsHidden = true;   // 用于记录Fragment show/hide 状态

    private DebounceAnimListener mDebounceAnimListener; // 防抖动监听动画
    private boolean mEnterAnimFlag = false; // 用于记录无动画时 直接 解除防抖动处理

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        if (activity instanceof SupportActivity) {
            this._mActivity = (SupportActivity) activity;
            mFragmentation = _mActivity.getFragmentation();
        } else {
            throw new RuntimeException(activity.toString() + "must extends SupportActivity!");
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle bundle = getArguments();
        if (bundle != null) {
            mRequestCode = bundle.getInt(Fragmentation.ARG_REQUEST_CODE, 0);
            mResultCode = bundle.getInt(Fragmentation.ARG_RESULT_CODE, 0);
            mResultBundle = bundle.getBundle(Fragmentation.ARG_RESULT_BUNDLE);
            mIsRoot = bundle.getBoolean(Fragmentation.ARG_IS_ROOT, false);
        }

        if (savedInstanceState == null) {
            mFragmentAnimator = onCreateFragmentAnimation();
            if (mFragmentAnimator == null) {
                mFragmentAnimator = _mActivity.getFragmentAnimator();
            }
        } else {
            mFragmentAnimator = savedInstanceState.getParcelable(FRAGMENTATION_STATE_SAVE_ANIMATOR);
            mIsHidden = savedInstanceState.getBoolean(FRAGMENTATION_STATE_SAVE_IS_HIDDEN);
        }

        initAnim();
    }

    private void initAnim() {
        handleNoAnim();

        mNoAnim = AnimationUtils.loadAnimation(_mActivity, R.anim.no_anim);
        mEnterAnim = AnimationUtils.loadAnimation(_mActivity, mFragmentAnimator.getEnter());
        mExitAnim = AnimationUtils.loadAnimation(_mActivity, mFragmentAnimator.getExit());
        mPopEnterAnim = AnimationUtils.loadAnimation(_mActivity, mFragmentAnimator.getPopEnter());
        mPopExitAnim = AnimationUtils.loadAnimation(_mActivity, mFragmentAnimator.getPopExit());

        // 监听动画状态(for防抖动)
        mDebounceAnimListener = new DebounceAnimListener();
        mEnterAnim.setAnimationListener(mDebounceAnimListener);
    }

    private void handleNoAnim() {
        if (mFragmentAnimator.getEnter() == 0) {
            mEnterAnimFlag = true;
            mFragmentAnimator.setEnter(R.anim.no_anim);
        }
        if (mFragmentAnimator.getExit() == 0) {
            mFragmentAnimator.setExit(R.anim.no_anim);
        }
        if (mFragmentAnimator.getPopEnter() == 0) {
            mFragmentAnimator.setPopEnter(R.anim.no_anim);
        }
        if (mFragmentAnimator.getPopExit() == 0) {
            // 用于解决 start新Fragment时,转场动画过程中上一个Fragment页面空白问题
            mFragmentAnimator.setPopExit(R.anim.pop_exit_no_anim);
        }
    }

    @Override
    public Animation onCreateAnimation(int transit, boolean enter, int nextAnim) {
        if (_mActivity.mPopMulitpleNoAnim || mLocking) {
            return mNoAnim;
        }
        if (transit == FragmentTransaction.TRANSIT_FRAGMENT_OPEN) {
            if (enter) {
                if (mIsRoot) {
                    mNoAnim.setAnimationListener(mDebounceAnimListener);
                    return mNoAnim;
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
     * 设定当前Fragmemt动画,优先级比在SupportActivity里高
     */
    protected FragmentAnimator onCreateFragmentAnimation() {
        return _mActivity.getFragmentAnimator();
    }

    /**
     * 入栈动画 结束时,回调
     */
    protected void onEnterAnimationEnd() {
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(FRAGMENTATION_STATE_SAVE_ANIMATOR, mFragmentAnimator);
        outState.putBoolean(FRAGMENTATION_STATE_SAVE_IS_HIDDEN, isHidden());
    }

    boolean isSupportHidden() {
        return mIsHidden;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        View view = getView();
        initFragmentBackground(view);
        // 防止某种情况 上一个Fragment仍可点击问题
        assert view != null;
        view.setClickable(true);

        if (savedInstanceState != null) {
            // 强杀重启时,系统默认Fragment恢复时无动画,所以这里手动调用下
            onEnterAnimationEnd();
            _mActivity.setFragmentClickable(true);
        } else if (mEnterAnimFlag) {
            _mActivity.setFragmentClickable(true);
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

    long getEnterAnimDuration() {
        return mEnterAnim.getDuration();
    }

    long getExitAnimDuration() {
        return mExitAnim.getDuration();
    }

    long getPopEnterAnimDuration() {
        return mPopEnterAnim.getDuration();
    }

    long getPopExitAnimDuration() {
        return mPopExitAnim.getDuration();
    }


    /**
     * (因为事务异步的原因) 如果你想在onCreateView/onActivityCreated中使用 start/pop 方法,请使用该方法把你的任务入队
     *
     * @param runnable 需要执行的任务
     */
    protected void enqueueAction(Runnable runnable) {
        _mActivity.getHandler().postDelayed(runnable, getEnterAnimDuration());
    }

    /**
     * 隐藏软键盘
     */
    protected void hideSoftInput() {
        if (getView() != null) {
            initImm();
            mIMM.hideSoftInputFromWindow(getView().getWindowToken(), 0);
        }
    }

    /**
     * 显示软键盘,调用该方法后,会在onPause时自动隐藏软键盘
     */
    protected void showSoftInput(final View view) {
        if (view == null) return;
        initImm();
        view.requestFocus();
        mNeedHideSoft = true;
        view.postDelayed(new Runnable() {
            @Override
            public void run() {
                mIMM.showSoftInput(view, InputMethodManager.SHOW_FORCED);
            }
        }, SHOW_SPACE);
    }

    private void initImm() {
        if (mIMM == null) {
            mIMM = (InputMethodManager) _mActivity.getSystemService(Context.INPUT_METHOD_SERVICE);
        }
    }


    @Override
    public void onPause() {
        super.onPause();
        if (mNeedHideSoft) {
            hideSoftInput();
        }
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
        return mFragmentation.findStackFragment(fragmentClass, getFragmentManager(), false);
    }

    /**
     * 获取栈内的子framgent对象
     *
     * @param fragmentClass
     */
    public <T extends SupportFragment> T findChildFragment(Class<T> fragmentClass) {
        return mFragmentation.findStackFragment(fragmentClass, getChildFragmentManager(), true);
    }

    /**
     * 得到位于栈顶的Fragment
     *
     * @return
     */
    public SupportFragment getPreFragment() {
        return mFragmentation.getPreFragment(this);
    }

    /**
     * 得到位于栈顶的Fragment
     *
     * @return
     */
    public SupportFragment getTopFragment() {
        return mFragmentation.getTopFragment(getFragmentManager());
    }

    /**
     * 得到位于栈顶的子Fragment
     *
     * @return
     */
    public SupportFragment getTopChildFragment() {
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
        popTo(fragmentClass, includeSelf, null);
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

    public void start(final SupportFragment toFragment, @LaunchMode final int launchMode) {
        mFragmentation.dispatchStartTransaction(this, toFragment, 0, launchMode, Fragmentation.TYPE_ADD);
    }

    public void startForResult(SupportFragment to, int requestCode) {
        mFragmentation.dispatchStartTransaction(this, to, requestCode, STANDARD, Fragmentation.TYPE_ADD);
    }

    public void startWithPop(SupportFragment to) {
        mFragmentation.dispatchStartTransaction(this, to, 0, STANDARD, Fragmentation.TYPE_ADD_WITH_POP);
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
     *
     * @param resultCode resultCode
     * @param bundle     设置Result数据
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
     *
     * @param requestCode requestCode
     * @param resultCode  resultCode
     * @param data        Result数据
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
     *
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

    void setEnterAnimEndListener(OnEnterAnimEndListener onAnimEndListener) {
        this.mOnAnimEndListener = onAnimEndListener;
    }

    /**
     * 为了防抖动(点击过快)的动画监听器
     */
    private class DebounceAnimListener implements Animation.AnimationListener {

        @Override
        public void onAnimationStart(Animation animation) {
        }

        @Override
        public void onAnimationEnd(Animation animation) {
            onEnterAnimationEnd();
            _mActivity.setFragmentClickable(true);

            if (mOnAnimEndListener != null) {
                mOnAnimEndListener.onAnimationEnd();
            }
        }

        @Override
        public void onAnimationRepeat(Animation animation) {

        }
    }
}
