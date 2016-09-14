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
import android.view.inputmethod.InputMethodManager;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import me.yokeyword.fragmentation.anim.FragmentAnimator;
import me.yokeyword.fragmentation.helper.AnimatorHelper;
import me.yokeyword.fragmentation.helper.DebounceAnimListener;
import me.yokeyword.fragmentation.helper.FragmentResultRecord;
import me.yokeyword.fragmentation.helper.OnEnterAnimEndListener;
import me.yokeyword.fragmentation.helper.OnFragmentDestoryViewListener;

/**
 * Created by YoKeyword on 16/1/22.
 */
public class SupportFragment extends Fragment implements ISupportFragment {
    // LaunchMode
    public static final int STANDARD = 0;
    public static final int SINGLETOP = 1;
    public static final int SINGLETASK = 2;

    // ResultCode
    public static final int RESULT_CANCELED = 0;
    public static final int RESULT_OK = -1;

    private static final long SHOW_SPACE = 200L;
    private static final long DEFAULT_ANIM_DURATION = 300L;

    private Bundle mNewBundle;

    private boolean mIsRoot, mIsSharedElement;
    private boolean mIsHidden = true;   // 用于记录Fragment show/hide 状态

    private InputMethodManager mIMM;
    private boolean mNeedHideSoft;  // 隐藏软键盘

    private OnEnterAnimEndListener mOnAnimEndListener; // fragmentation中需要

    protected SupportActivity _mActivity;
    protected Fragmentation mFragmentation;
    private int mContainerId;   // 该Fragment所处的Container的id

    private FragmentAnimator mFragmentAnimator;
    private AnimatorHelper mAnimHelper;
    private boolean mEnterAnimFlag = false; // 用于记录无动画时,解除 防抖动处理

    protected boolean mLocking; // 是否加锁 用于Fragmentation-SwipeBack库

    private OnFragmentDestoryViewListener mFragmentDestoryViewListener;

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
            mIsRoot = bundle.getBoolean(Fragmentation.ARG_IS_ROOT, false);
            mIsSharedElement = bundle.getBoolean(Fragmentation.ARG_IS_SHARED_ELEMENT, false);
            mContainerId = bundle.getInt(Fragmentation.FRAGMENTATION_ARG_CONTAINER);
        }

        if (savedInstanceState == null) {
            mFragmentAnimator = onCreateFragmentAnimator();
            if (mFragmentAnimator == null) {
                mFragmentAnimator = _mActivity.getFragmentAnimator();
            }
        } else {
            mFragmentAnimator = savedInstanceState.getParcelable(Fragmentation.FRAGMENTATION_STATE_SAVE_ANIMATOR);
            mIsHidden = savedInstanceState.getBoolean(Fragmentation.FRAGMENTATION_STATE_SAVE_IS_HIDDEN);
        }

        if (restoreInstanceState()) {
            // 解决重叠问题
            processRestoreInstanceState(savedInstanceState);
        }

        initAnim();
    }

    private void processRestoreInstanceState(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            FragmentTransaction ft = getFragmentManager().beginTransaction();
            if (isSupportHidden()) {
                ft.hide(this);
            } else {
                ft.show(this);
            }
            ft.commit();
        }
    }

    /**
     * 内存重启后,是否让Fragmentation帮你恢复子Fragment状态
     */
    protected boolean restoreInstanceState() {
        return true;
    }

    private void initAnim() {
        mAnimHelper = new AnimatorHelper(_mActivity.getApplicationContext(), mFragmentAnimator);
        if (mAnimHelper.enterAnim == mAnimHelper.getNoAnim()) {
            mEnterAnimFlag = true;
        }

        // 监听入栈动画结束(1.为了防抖动; 2.为了Fragmentation的回调所用)
        mAnimHelper.enterAnim.setAnimationListener(new DebounceAnimListener(this));
    }

    @Override
    public Animation onCreateAnimation(int transit, boolean enter, int nextAnim) {
        if (_mActivity.mPopMultipleNoAnim || mLocking) {
            if (transit == FragmentTransaction.TRANSIT_FRAGMENT_CLOSE && enter) {
                // fix popTo(在设置为库中横向动画时),引起的一个闪烁问题
                return mAnimHelper.getFixNoAnim();
            }
            return mAnimHelper.getNoAnim();
        }
        if (transit == FragmentTransaction.TRANSIT_FRAGMENT_OPEN) {
            if (enter) {
                if (mIsRoot) {  // 根Fragment设置为无入栈动画
                    mEnterAnimFlag = true;
                    return mAnimHelper.getNoAnim();
                }
                return mAnimHelper.enterAnim;
            } else {
                return mAnimHelper.popExitAnim;
            }
        } else if (transit == FragmentTransaction.TRANSIT_FRAGMENT_CLOSE) {
            if (enter) {
                return mAnimHelper.popEnterAnim;
            } else {
                return mAnimHelper.exitAnim;
            }
        } else if (mIsSharedElement) {
            if (enter) {    // 此处在设置SharedElement时,回调  transit=0, enter=true, nextAnim=0
                mEnterAnimFlag = true;
            } else if (getEnterTransition() == null) {
                return mAnimHelper.exitAnim;
            }
        }
        return super.onCreateAnimation(transit, enter, nextAnim);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(Fragmentation.FRAGMENTATION_STATE_SAVE_ANIMATOR, mFragmentAnimator);
        outState.putBoolean(Fragmentation.FRAGMENTATION_STATE_SAVE_IS_HIDDEN, isHidden());
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        View view = getView();
        initFragmentBackground(view);
        // 防止某种情况 上一个Fragment仍可点击问题
        if (view != null) {
            view.setClickable(true);
        }

        if (savedInstanceState != null) {
            // 强杀重启时,系统默认Fragment恢复时无动画,所以这里手动调用下
            notifyEnterAnimationEnd(savedInstanceState);
            _mActivity.setFragmentClickable(true);
        } else if (mEnterAnimFlag) { // 无动画
            notifyEnterAnimationEnd(null);
            _mActivity.setFragmentClickable(true);
        }
    }

    protected void initFragmentBackground(View view) {
        setBackground(view);
    }

    protected void setBackground(View view) {
        if (view != null && view.getBackground() == null) {
            int defaultBg = _mActivity.getDefaultFragmentBackground();
            if (defaultBg == 0) {
                int background = getWindowBackground();
                view.setBackgroundResource(background);
            } else {
                view.setBackgroundResource(defaultBg);
            }
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

    /**
     * 仅在内存重启后有意义(saveInstanceState!=null时)
     *
     * @return Fragment状态 hide : show
     */
    public boolean isSupportHidden() {
        return mIsHidden;
    }

    /**
     * 获取该Fragment所在的容器id
     */
    int getContainerId() {
        return mContainerId;
    }

    long getEnterAnimDuration() {
        if (mIsRoot) {
            return 0;
        }
        if (mAnimHelper == null) {
            return DEFAULT_ANIM_DURATION;
        }
        return mAnimHelper.enterAnim.getDuration();
    }

    long getExitAnimDuration() {
        if (mAnimHelper == null) {
            return DEFAULT_ANIM_DURATION;
        }
        return mAnimHelper.exitAnim.getDuration();
    }

    long getPopEnterAnimDuration() {
        if (mAnimHelper == null) {
            return DEFAULT_ANIM_DURATION;
        }
        return mAnimHelper.popEnterAnim.getDuration();
    }

    long getPopExitAnimDuration() {
        if (mAnimHelper == null) {
            return DEFAULT_ANIM_DURATION;
        }
        return mAnimHelper.popExitAnim.getDuration();
    }

    /**
     * 设定当前Fragmemt动画,优先级比在SupportActivity里高
     */
    protected FragmentAnimator onCreateFragmentAnimator() {
        return _mActivity.getFragmentAnimator();
    }

    /**
     * 入栈动画 结束时,回调
     */
    protected void onEnterAnimationEnd(Bundle savedInstanceState) {
    }

    private void notifyEnterAnimationEnd(final Bundle savedInstanceState) {
        _mActivity.getHandler().post(new Runnable() {
            @Override
            public void run() {
                onEnterAnimationEnd(savedInstanceState);
            }
        });
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
     * 按返回键触发,前提是SupportActivity的onBackPressed()方法能被调用
     *
     * @return false则继续向上传递, true则消费掉该事件
     */
    public boolean onBackPressedSupport() {
        return false;
    }

    @Override
    public void loadRootFragment(int containerId, SupportFragment toFragment) {
        mFragmentation.loadRootTransaction(getChildFragmentManager(), containerId, toFragment);
    }

    @Override
    public void replaceLoadRootFragment(int containerId, SupportFragment toFragment, boolean addToBack) {
        mFragmentation.replaceLoadRootTransaction(getChildFragmentManager(), containerId, toFragment, addToBack);
    }

    @Override
    public void loadMultipleRootFragment(int containerId, int showPosition, SupportFragment... toFragments) {
        mFragmentation.loadMultipleRootTransaction(getChildFragmentManager(), containerId, showPosition, toFragments);
    }

    @Override
    public void showHideFragment(SupportFragment showFragment, SupportFragment hideFragment) {
        mFragmentation.showHideFragment(getChildFragmentManager(), showFragment, hideFragment);
    }

    @Override
    public void start(SupportFragment toFragment) {
        start(toFragment, STANDARD);
    }

    @Override
    public void start(final SupportFragment toFragment, @LaunchMode final int launchMode) {
        mFragmentation.dispatchStartTransaction(getFragmentManager(), this, toFragment, 0, launchMode, Fragmentation.TYPE_ADD, null, null);
    }

    @Override
    public void startForResult(SupportFragment toFragment, int requestCode) {
        mFragmentation.dispatchStartTransaction(getFragmentManager(), this, toFragment, requestCode, STANDARD, Fragmentation.TYPE_ADD_RESULT, null, null);
    }

    @Override
    public void startWithPop(SupportFragment toFragment) {
        mFragmentation.dispatchStartTransaction(getFragmentManager(), this, toFragment, 0, STANDARD, Fragmentation.TYPE_ADD_WITH_POP, null, null);
    }

    @Override
    public void startWithSharedElement(SupportFragment toFragment, View sharedElement, String name) {
        mFragmentation.dispatchStartTransaction(getFragmentManager(), this, toFragment, 0, STANDARD, Fragmentation.TYPE_ADD, sharedElement, name);
    }

    @Override
    public void startForResultWithSharedElement(SupportFragment toFragment, int requestCode, View sharedElement, String name) {
        mFragmentation.dispatchStartTransaction(getFragmentManager(), this, toFragment, requestCode, STANDARD, Fragmentation.TYPE_ADD_RESULT, sharedElement, name);
    }

    @Override
    public void replaceFragment(SupportFragment toFragment, boolean addToBack) {
        mFragmentation.replaceTransaction(this, toFragment, addToBack);
    }

    /**
     * @return 位于栈顶的Fragment
     */
    @Override
    public SupportFragment getTopFragment() {
        return mFragmentation.getTopFragment(getFragmentManager());
    }

    /**
     * @return 位于栈顶的子Fragment
     */
    @Override
    public SupportFragment getTopChildFragment() {
        return mFragmentation.getTopFragment(getChildFragmentManager());
    }

    /**
     * @return 位于当前Fragment的前一个Fragment
     */
    @Override
    public SupportFragment getPreFragment() {
        return mFragmentation.getPreFragment(this);
    }

    /**
     * @return 栈内fragmentClass的fragment对象
     */
    @Override
    public <T extends SupportFragment> T findFragment(Class<T> fragmentClass) {
        return mFragmentation.findStackFragment(fragmentClass, getFragmentManager(), false);
    }

    /**
     * @return 栈内fragmentClass的子fragment对象
     */
    @Override
    public <T extends SupportFragment> T findChildFragment(Class<T> fragmentClass) {
        return mFragmentation.findStackFragment(fragmentClass, getChildFragmentManager(), true);
    }

    /**
     * 出栈
     */
    @Override
    public void pop() {
        mFragmentation.back(getFragmentManager());
    }

    /**
     * 子栈内 出栈
     */
    @Override
    public void popChild() {
        mFragmentation.back(getChildFragmentManager());
    }

    /**
     * 出栈到目标fragment
     *
     * @param fragmentClass 目标fragment
     * @param includeSelf   是否包含该fragment
     */
    @Override
    public void popTo(Class<?> fragmentClass, boolean includeSelf) {
        popTo(fragmentClass, includeSelf, null);
    }

    /**
     * 子栈内
     */
    @Override
    public void popToChild(Class<?> fragmentClass, boolean includeSelf) {
        popToChild(fragmentClass, includeSelf, null);
    }

    /**
     * 用于出栈后,立刻进行FragmentTransaction操作
     */
    @Override
    public void popTo(Class<?> fragmentClass, boolean includeSelf, Runnable afterPopTransactionRunnable) {
        mFragmentation.popTo(fragmentClass, includeSelf, afterPopTransactionRunnable, getFragmentManager());
    }

    /**
     * 子栈内
     */
    @Override
    public void popToChild(Class<?> fragmentClass, boolean includeSelf, Runnable afterPopTransactionRunnable) {
        mFragmentation.popTo(fragmentClass, includeSelf, afterPopTransactionRunnable, getChildFragmentManager());
    }

    void popForSwipeBack() {
        mLocking = true;
        mFragmentation.back(getFragmentManager());
        mLocking = false;
    }

    /**
     * 设置Result数据 (通过startForResult)
     *
     * @param resultCode resultCode
     * @param bundle     设置Result数据
     */
    public void setFramgentResult(int resultCode, Bundle bundle) {
        Bundle args = getArguments();
        if (args == null || !args.containsKey(Fragmentation.ARG_RESULT_RECORD)) {
            return;
        }

        FragmentResultRecord fragmentResultRecord = args.getParcelable(Fragmentation.ARG_RESULT_RECORD);
        if (fragmentResultRecord != null) {
            fragmentResultRecord.resultCode = resultCode;
            fragmentResultRecord.resultBundle = bundle;
        }
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

    void setEnterAnimEndListener(OnEnterAnimEndListener onAnimEndListener) {
        this.mOnAnimEndListener = onAnimEndListener;
    }

    /**
     * 入场动画结束时,回调
     */
    public final void notifyEnterAnimEnd() {
        notifyEnterAnimationEnd(null);
        _mActivity.setFragmentClickable(true);

        if (mOnAnimEndListener != null) {
            mOnAnimEndListener.onAnimationEnd();
        }
    }

    /**
     * @see OnFragmentDestoryViewListener
     */
    void setOnFragmentDestoryViewListener(OnFragmentDestoryViewListener listener) {
        this.mFragmentDestoryViewListener = listener;
    }

    @Override
    public void onDestroyView() {
        if (mFragmentDestoryViewListener != null) {
            mFragmentDestoryViewListener.onDestoryView();
        }
        super.onDestroyView();
        mFragmentDestoryViewListener = null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mOnAnimEndListener = null;
    }
}
