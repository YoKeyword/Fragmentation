package me.yokeyword.fragmentation;

import android.app.Activity;
import android.content.Intent;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.FragmentationMagician;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import me.yokeyword.fragmentation.anim.FragmentAnimator;
import me.yokeyword.fragmentation.helper.internal.AnimatorHelper;
import me.yokeyword.fragmentation.helper.internal.ResultRecord;
import me.yokeyword.fragmentation.helper.internal.TransactionRecord;
import me.yokeyword.fragmentation.helper.internal.VisibleDelegate;

public class SupportFragmentDelegate {
    private static final long NOT_FOUND_ANIM_TIME = 300L;

    static final int STATUS_UN_ROOT = 0;
    static final int STATUS_ROOT_ANIM_DISABLE = 1;
    static final int STATUS_ROOT_ANIM_ENABLE = 2;

    private int mRootStatus = STATUS_UN_ROOT;

    private boolean mIsSharedElement;
    FragmentAnimator mFragmentAnimator;
    AnimatorHelper mAnimHelper;
    boolean mLockAnim;
    private int mCustomEnterAnim = Integer.MIN_VALUE, mCustomExitAnim = Integer.MIN_VALUE, mCustomPopExitAnim = Integer.MIN_VALUE;

    private Handler mHandler;
    private boolean mFirstCreateView = true;
    private boolean mReplaceMode;
    private boolean mIsHidden = true;
    int mContainerId;

    private TransactionDelegate mTransactionDelegate;
    TransactionRecord mTransactionRecord;
    // SupportVisible
    private VisibleDelegate mVisibleDelegate;
    Bundle mNewBundle;
    private Bundle mSaveInstanceState;

    private ISupportFragment mSupportF;
    private Fragment mFragment;
    protected FragmentActivity _mActivity;
    private ISupportActivity mSupport;
    boolean mAnimByActivity = true;
    EnterAnimListener mEnterAnimListener;

    private boolean mRootViewClickable;

    public SupportFragmentDelegate(ISupportFragment support) {
        if (!(support instanceof Fragment))
            throw new RuntimeException("Must extends Fragment");
        this.mSupportF = support;
        this.mFragment = (Fragment) support;
    }

    /**
     * Perform some extra transactions.
     * 额外的事务：自定义Tag，添加SharedElement动画，操作非回退栈Fragment
     */
    public ExtraTransaction extraTransaction() {
        if (mTransactionDelegate == null)
            throw new RuntimeException(mFragment.getClass().getSimpleName() + " not attach!");

        return new ExtraTransaction.ExtraTransactionImpl<>((FragmentActivity) mSupport, mSupportF, mTransactionDelegate, false);
    }

    public void onAttach(Activity activity) {
        if (activity instanceof ISupportActivity) {
            this.mSupport = (ISupportActivity) activity;
            this._mActivity = (FragmentActivity) activity;
            mTransactionDelegate = mSupport.getSupportDelegate().getTransactionDelegate();
        } else {
            throw new RuntimeException(activity.getClass().getSimpleName() + " must impl ISupportActivity!");
        }
    }

    public void onCreate(@Nullable Bundle savedInstanceState) {
        getVisibleDelegate().onCreate(savedInstanceState);

        Bundle bundle = mFragment.getArguments();
        if (bundle != null) {
            mRootStatus = bundle.getInt(TransactionDelegate.FRAGMENTATION_ARG_ROOT_STATUS, STATUS_UN_ROOT);
            mIsSharedElement = bundle.getBoolean(TransactionDelegate.FRAGMENTATION_ARG_IS_SHARED_ELEMENT, false);
            mContainerId = bundle.getInt(TransactionDelegate.FRAGMENTATION_ARG_CONTAINER);
            mReplaceMode = bundle.getBoolean(TransactionDelegate.FRAGMENTATION_ARG_REPLACE, false);
            mCustomEnterAnim = bundle.getInt(TransactionDelegate.FRAGMENTATION_ARG_CUSTOM_ENTER_ANIM, Integer.MIN_VALUE);
            mCustomExitAnim = bundle.getInt(TransactionDelegate.FRAGMENTATION_ARG_CUSTOM_EXIT_ANIM, Integer.MIN_VALUE);
            mCustomPopExitAnim = bundle.getInt(TransactionDelegate.FRAGMENTATION_ARG_CUSTOM_POP_EXIT_ANIM, Integer.MIN_VALUE);
        }

        if (savedInstanceState == null) {
            getFragmentAnimator();
        } else {
            savedInstanceState.setClassLoader(getClass().getClassLoader());
            mSaveInstanceState = savedInstanceState;
            mFragmentAnimator = savedInstanceState.getParcelable(TransactionDelegate.FRAGMENTATION_STATE_SAVE_ANIMATOR);
            mIsHidden = savedInstanceState.getBoolean(TransactionDelegate.FRAGMENTATION_STATE_SAVE_IS_HIDDEN);
            mContainerId = savedInstanceState.getInt(TransactionDelegate.FRAGMENTATION_ARG_CONTAINER);

            // RootFragment
            if (mRootStatus != STATUS_UN_ROOT) {
                FragmentationMagician.reorderIndices(mFragment.getFragmentManager());
            }
        }

        // Fix the overlapping BUG on pre-24.0.0
        processRestoreInstanceState(savedInstanceState);
        mAnimHelper = new AnimatorHelper(_mActivity.getApplicationContext(), mFragmentAnimator);

        final Animation enter = getEnterAnim();
        if (enter == null) return;

        getEnterAnim().setAnimationListener(new Animation.AnimationListener() {

            @Override
            public void onAnimationStart(Animation animation) {
                mSupport.getSupportDelegate().mFragmentClickable = false;  // 开启防抖动

                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mSupport.getSupportDelegate().mFragmentClickable = true;
                    }
                }, enter.getDuration());
            }

            @Override
            public void onAnimationEnd(Animation animation) {
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
    }

    public Animation onCreateAnimation(int transit, boolean enter, int nextAnim) {
        if ((mSupport.getSupportDelegate().mPopMultipleNoAnim || mLockAnim)) {
            if (transit == FragmentTransaction.TRANSIT_FRAGMENT_CLOSE && enter) {
                return mAnimHelper.getNoneAnimFixed();
            }
            return mAnimHelper.getNoneAnim();
        }
        if (transit == FragmentTransaction.TRANSIT_FRAGMENT_OPEN) {
            if (enter) {
                Animation enterAnim;
                if (mRootStatus == STATUS_ROOT_ANIM_DISABLE) {
                    enterAnim = mAnimHelper.getNoneAnim();
                } else {
                    enterAnim = mAnimHelper.enterAnim;
                    fixAnimationListener(enterAnim);
                }
                return enterAnim;
            } else {
                return mAnimHelper.popExitAnim;
            }
        } else if (transit == FragmentTransaction.TRANSIT_FRAGMENT_CLOSE) {
            return enter ? mAnimHelper.popEnterAnim : mAnimHelper.exitAnim;
        } else {
            if (mIsSharedElement && enter) {
                compatSharedElements();
            }

            if (!enter) {
                return mAnimHelper.compatChildFragmentExitAnim(mFragment);
            }

            return null;
        }
    }

    public void onSaveInstanceState(Bundle outState) {
        getVisibleDelegate().onSaveInstanceState(outState);
        outState.putParcelable(TransactionDelegate.FRAGMENTATION_STATE_SAVE_ANIMATOR, mFragmentAnimator);
        outState.putBoolean(TransactionDelegate.FRAGMENTATION_STATE_SAVE_IS_HIDDEN, mFragment.isHidden());
        outState.putInt(TransactionDelegate.FRAGMENTATION_ARG_CONTAINER, mContainerId);
    }

    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        getVisibleDelegate().onActivityCreated(savedInstanceState);

        View view = mFragment.getView();
        if (view != null) {
            mRootViewClickable = view.isClickable();
            view.setClickable(true);
            setBackground(view);
        }


        if (savedInstanceState != null
                || mRootStatus == STATUS_ROOT_ANIM_DISABLE
                || (mFragment.getTag() != null && mFragment.getTag().startsWith("android:switcher:"))
                || (mReplaceMode && !mFirstCreateView)) {
            notifyEnterAnimEnd();
        } else if (mCustomEnterAnim != Integer.MIN_VALUE) {
            fixAnimationListener(mCustomEnterAnim == 0 ?
                    mAnimHelper.getNoneAnim() : AnimationUtils.loadAnimation(_mActivity, mCustomEnterAnim));
        }

        if (mFirstCreateView) {
            mFirstCreateView = false;
        }
    }

    public void onResume() {
        getVisibleDelegate().onResume();
    }

    public void onPause() {
        getVisibleDelegate().onPause();
    }

    public void onDestroyView() {
        mSupport.getSupportDelegate().mFragmentClickable = true;
        getVisibleDelegate().onDestroyView();
        getHandler().removeCallbacks(mNotifyEnterAnimEndRunnable);
    }

    public void onDestroy() {
        mTransactionDelegate.handleResultRecord(mFragment);
    }

    public void onHiddenChanged(boolean hidden) {
        getVisibleDelegate().onHiddenChanged(hidden);
    }

    public void setUserVisibleHint(boolean isVisibleToUser) {
        getVisibleDelegate().setUserVisibleHint(isVisibleToUser);
    }

    /**
     * Causes the Runnable r to be added to the action queue.
     * <p>
     * The runnable will be run after all the previous action has been run.
     * <p>
     * 前面的事务全部执行后 执行该Action
     *
     * @deprecated Use {@link #post(Runnable)} instead.
     */
    @Deprecated
    public void enqueueAction(Runnable runnable) {
        post(runnable);
    }

    /**
     * Causes the Runnable r to be added to the action queue.
     * <p>
     * The runnable will be run after all the previous action has been run.
     * <p>
     * 前面的事务全部执行后 执行该Action
     */
    public void post(final Runnable runnable) {
        mTransactionDelegate.post(runnable);
    }

    /**
     * Called when the enter-animation end.
     * 入栈动画 结束时,回调
     */
    public void onEnterAnimationEnd(Bundle savedInstanceState) {
    }

    /**
     * Lazy initial，Called when fragment is first visible.
     * <p>
     * 同级下的 懒加载 ＋ ViewPager下的懒加载  的结合回调方法
     */
    public void onLazyInitView(@Nullable Bundle savedInstanceState) {
    }

    /**
     * Called when the fragment is visible.
     * <p>
     * 当Fragment对用户可见时回调
     * <p>
     * Is the combination of  [onHiddenChanged() + onResume()/onPause() + setUserVisibleHint()]
     */
    public void onSupportVisible() {
    }

    /**
     * Called when the fragment is invivible.
     * <p>
     * Is the combination of  [onHiddenChanged() + onResume()/onPause() + setUserVisibleHint()]
     */
    public void onSupportInvisible() {
    }

    /**
     * Return true if the fragment has been supportVisible.
     */
    final public boolean isSupportVisible() {
        return getVisibleDelegate().isSupportVisible();
    }

    /**
     * Set fragment animation with a higher priority than the ISupportActivity
     * 设定当前Fragmemt动画,优先级比在ISupportActivity里高
     */
    public FragmentAnimator onCreateFragmentAnimator() {
        return mSupport.getFragmentAnimator();
    }

    /**
     * 获取设置的全局动画
     *
     * @return FragmentAnimator
     */
    public FragmentAnimator getFragmentAnimator() {
        if (mSupport == null)
            throw new RuntimeException("Fragment has not been attached to Activity!");

        if (mFragmentAnimator == null) {
            mFragmentAnimator = mSupportF.onCreateFragmentAnimator();
            if (mFragmentAnimator == null) {
                mFragmentAnimator = mSupport.getFragmentAnimator();
            }
        }
        return mFragmentAnimator;
    }

    /**
     * Set the fragment animation.
     */
    public void setFragmentAnimator(FragmentAnimator fragmentAnimator) {
        this.mFragmentAnimator = fragmentAnimator;
        if (mAnimHelper != null) {
            mAnimHelper.notifyChanged(fragmentAnimator);
        }
        mAnimByActivity = false;
    }

    /**
     * 类似 {@link Activity#setResult(int, Intent)}
     * <p>
     * Similar to {@link Activity#setResult(int, Intent)}
     *
     * @see #startForResult(ISupportFragment, int)
     */
    public void setFragmentResult(int resultCode, Bundle bundle) {
        Bundle args = mFragment.getArguments();
        if (args == null || !args.containsKey(TransactionDelegate.FRAGMENTATION_ARG_RESULT_RECORD)) {
            return;
        }

        ResultRecord resultRecord = args.getParcelable(TransactionDelegate.FRAGMENTATION_ARG_RESULT_RECORD);
        if (resultRecord != null) {
            resultRecord.resultCode = resultCode;
            resultRecord.resultBundle = bundle;
        }
    }

    /**
     * 类似  {@link Activity#onActivityResult(int, int, Intent)}
     * <p>
     * Similar to {@link Activity#onActivityResult(int, int, Intent)}
     *
     * @see #startForResult(ISupportFragment, int)
     */
    public void onFragmentResult(int requestCode, int resultCode, Bundle data) {
    }

    /**
     * 在start(TargetFragment,LaunchMode)时,启动模式为SingleTask/SingleTop, 回调TargetFragment的该方法
     * 类似 {@link Activity#onNewIntent(Intent)}
     * <p>
     * Similar to {@link Activity#onNewIntent(Intent)}
     *
     * @param args putNewBundle(Bundle newBundle)
     * @see #start(ISupportFragment, int)
     */
    public void onNewBundle(Bundle args) {
    }

    /**
     * 添加NewBundle,用于启动模式为SingleTask/SingleTop时
     *
     * @see #start(ISupportFragment, int)
     */
    public void putNewBundle(Bundle newBundle) {
        this.mNewBundle = newBundle;
    }

    /**
     * Back Event
     *
     * @return false则继续向上传递, true则消费掉该事件
     */
    public boolean onBackPressedSupport() {
        return false;
    }

    /**********************************************************************************************/

    /**
     * 隐藏软键盘
     */
    public void hideSoftInput() {
        Activity activity = mFragment.getActivity();
        if (activity == null) return;
        View view = activity.getWindow().getDecorView();
        SupportHelper.hideSoftInput(view);
    }

    /**
     * 显示软键盘,调用该方法后,会在onPause时自动隐藏软键盘
     */
    public void showSoftInput(View view) {
        SupportHelper.showSoftInput(view);
    }


    /**
     * 加载根Fragment, 即Activity内的第一个Fragment 或 Fragment内的第一个子Fragment
     */
    public void loadRootFragment(int containerId, ISupportFragment toFragment) {
        loadRootFragment(containerId, toFragment, true, false);
    }

    public void loadRootFragment(int containerId, ISupportFragment toFragment, boolean addToBackStack, boolean allowAnim) {
        mTransactionDelegate.loadRootTransaction(getChildFragmentManager(), containerId, toFragment, addToBackStack, allowAnim);
    }

    /**
     * 加载多个同级根Fragment,类似Wechat, QQ主页的场景
     */
    public void loadMultipleRootFragment(int containerId, int showPosition, ISupportFragment... toFragments) {
        mTransactionDelegate.loadMultipleRootTransaction(getChildFragmentManager(), containerId, showPosition, toFragments);
    }

    /**
     * show一个Fragment,hide其他同栈所有Fragment
     * 使用该方法时，要确保同级栈内无多余的Fragment,(只有通过loadMultipleRootFragment()载入的Fragment)
     * <p>
     * 建议使用更明确的{@link #showHideFragment(ISupportFragment, ISupportFragment)}
     */
    public void showHideFragment(ISupportFragment showFragment) {
        showHideFragment(showFragment, null);
    }

    /**
     * show一个Fragment,hide一个Fragment ; 主要用于类似微信主页那种 切换tab的情况
     */
    public void showHideFragment(ISupportFragment showFragment, ISupportFragment hideFragment) {
        mTransactionDelegate.showHideFragment(getChildFragmentManager(), showFragment, hideFragment);
    }

    public void start(ISupportFragment toFragment) {
        start(toFragment, ISupportFragment.STANDARD);
    }

    /**
     * @param launchMode Similar to Activity's LaunchMode.
     */
    public void start(final ISupportFragment toFragment, @ISupportFragment.LaunchMode int launchMode) {
        mTransactionDelegate.dispatchStartTransaction(mFragment.getFragmentManager(), mSupportF, toFragment, 0, launchMode, TransactionDelegate.TYPE_ADD);
    }

    /**
     * Launch an fragment for which you would like a result when it poped.
     */
    public void startForResult(ISupportFragment toFragment, int requestCode) {
        mTransactionDelegate.dispatchStartTransaction(mFragment.getFragmentManager(), mSupportF, toFragment, requestCode, ISupportFragment.STANDARD, TransactionDelegate.TYPE_ADD_RESULT);
    }

    /**
     * Start the target Fragment and pop itself
     */
    public void startWithPop(ISupportFragment toFragment) {
        mTransactionDelegate.startWithPop(mFragment.getFragmentManager(), mSupportF, toFragment);
    }

    public void startWithPopTo(ISupportFragment toFragment, Class<?> targetFragmentClass, boolean includeTargetFragment) {
        mTransactionDelegate.startWithPopTo(mFragment.getFragmentManager(), mSupportF, toFragment, targetFragmentClass.getName(), includeTargetFragment);
    }

    public void replaceFragment(ISupportFragment toFragment, boolean addToBackStack) {
        mTransactionDelegate.dispatchStartTransaction(mFragment.getFragmentManager(), mSupportF, toFragment, 0, ISupportFragment.STANDARD, addToBackStack ? TransactionDelegate.TYPE_REPLACE : TransactionDelegate.TYPE_REPLACE_DONT_BACK);
    }

    public void startChild(ISupportFragment toFragment) {
        startChild(toFragment, ISupportFragment.STANDARD);
    }

    public void startChild(final ISupportFragment toFragment, @ISupportFragment.LaunchMode int launchMode) {
        mTransactionDelegate.dispatchStartTransaction(getChildFragmentManager(), getTopFragment(), toFragment, 0, launchMode, TransactionDelegate.TYPE_ADD);
    }

    public void startChildForResult(ISupportFragment toFragment, int requestCode) {
        mTransactionDelegate.dispatchStartTransaction(getChildFragmentManager(), getTopFragment(), toFragment, requestCode, ISupportFragment.STANDARD, TransactionDelegate.TYPE_ADD_RESULT);
    }

    public void startChildWithPop(ISupportFragment toFragment) {
        mTransactionDelegate.startWithPop(getChildFragmentManager(), getTopFragment(), toFragment);
    }

    public void replaceChildFragment(ISupportFragment toFragment, boolean addToBackStack) {
        mTransactionDelegate.dispatchStartTransaction(getChildFragmentManager(), getTopFragment(), toFragment, 0, ISupportFragment.STANDARD, addToBackStack ? TransactionDelegate.TYPE_REPLACE : TransactionDelegate.TYPE_REPLACE_DONT_BACK);
    }

    public void pop() {
        mTransactionDelegate.pop(mFragment.getFragmentManager());
    }

    /**
     * Pop the child fragment.
     */
    public void popChild() {
        mTransactionDelegate.pop(getChildFragmentManager());
    }

    /**
     * Pop the last fragment transition from the manager's fragment
     * back stack.
     * <p>
     * 出栈到目标fragment
     *
     * @param targetFragmentClass   目标fragment
     * @param includeTargetFragment 是否包含该fragment
     */
    public void popTo(Class<?> targetFragmentClass, boolean includeTargetFragment) {
        popTo(targetFragmentClass, includeTargetFragment, null);
    }

    /**
     * If you want to begin another FragmentTransaction immediately after popTo(), use this method.
     * 如果你想在出栈后, 立刻进行FragmentTransaction操作，请使用该方法
     */
    public void popTo(Class<?> targetFragmentClass, boolean includeTargetFragment, Runnable afterPopTransactionRunnable) {
        popTo(targetFragmentClass, includeTargetFragment, afterPopTransactionRunnable, TransactionDelegate.DEFAULT_POPTO_ANIM);
    }

    public void popTo(Class<?> targetFragmentClass, boolean includeTargetFragment, Runnable afterPopTransactionRunnable, int popAnim) {
        mTransactionDelegate.popTo(targetFragmentClass.getName(), includeTargetFragment, afterPopTransactionRunnable, mFragment.getFragmentManager(), popAnim);
    }

    public void popToChild(Class<?> targetFragmentClass, boolean includeTargetFragment) {
        popToChild(targetFragmentClass, includeTargetFragment, null);
    }

    public void popToChild(Class<?> targetFragmentClass, boolean includeTargetFragment, Runnable afterPopTransactionRunnable) {
        popToChild(targetFragmentClass, includeTargetFragment, afterPopTransactionRunnable, TransactionDelegate.DEFAULT_POPTO_ANIM);
    }

    public void popToChild(Class<?> targetFragmentClass, boolean includeTargetFragment, Runnable afterPopTransactionRunnable, int popAnim) {
        mTransactionDelegate.popTo(targetFragmentClass.getName(), includeTargetFragment, afterPopTransactionRunnable, getChildFragmentManager(), popAnim);
    }

    public void popQuiet() {
        mTransactionDelegate.popQuiet(mFragment.getFragmentManager(), mFragment);
    }

    private FragmentManager getChildFragmentManager() {
        return mFragment.getChildFragmentManager();
    }

    private ISupportFragment getTopFragment() {
        return SupportHelper.getTopFragment(getChildFragmentManager());
    }

    private void processRestoreInstanceState(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            FragmentTransaction ft = mFragment.getFragmentManager().beginTransaction();
            if (mIsHidden) {
                ft.hide(mFragment);
            } else {
                ft.show(mFragment);
            }
            ft.commitAllowingStateLoss();
        }
    }

    private void fixAnimationListener(Animation enterAnim) {
        // AnimationListener is not reliable.
        getHandler().postDelayed(mNotifyEnterAnimEndRunnable, enterAnim.getDuration());
        mSupport.getSupportDelegate().mFragmentClickable = true;

        if (mEnterAnimListener != null) {
            getHandler().post(new Runnable() {
                @Override
                public void run() {
                    mEnterAnimListener.onEnterAnimStart();
                    mEnterAnimListener = null;
                }
            });
        }
    }

    private Runnable mNotifyEnterAnimEndRunnable = new Runnable() {
        @Override
        public void run() {
            if (mFragment == null) return;
            mSupportF.onEnterAnimationEnd(mSaveInstanceState);

            if (mRootViewClickable) return;
            final View view = mFragment.getView();
            if (view == null) return;
            ISupportFragment preFragment = SupportHelper.getPreFragment(mFragment);
            if (preFragment == null) return;

            long prePopExitDuration = preFragment.getSupportDelegate().getPopExitAnimDuration();
            long enterDuration = getEnterAnimDuration();

            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    view.setClickable(false);
                }
            }, prePopExitDuration - enterDuration);
        }
    };

    private void compatSharedElements() {
        notifyEnterAnimEnd();
    }

    public void setBackground(View view) {
        if ((mFragment.getTag() != null && mFragment.getTag().startsWith("android:switcher:")) ||
                mRootStatus != STATUS_UN_ROOT ||
                view.getBackground() != null) {
            return;
        }

        int defaultBg = mSupport.getSupportDelegate().getDefaultFragmentBackground();
        if (defaultBg == 0) {
            int background = getWindowBackground();
            view.setBackgroundResource(background);
        } else {
            view.setBackgroundResource(defaultBg);
        }
    }

    private int getWindowBackground() {
        TypedArray a = _mActivity.getTheme().obtainStyledAttributes(new int[]{
                android.R.attr.windowBackground
        });
        int background = a.getResourceId(0, 0);
        a.recycle();
        return background;
    }

    private void notifyEnterAnimEnd() {
        getHandler().post(mNotifyEnterAnimEndRunnable);
        mSupport.getSupportDelegate().mFragmentClickable = true;
    }

    private Handler getHandler() {
        if (mHandler == null) {
            mHandler = new Handler(Looper.getMainLooper());
        }
        return mHandler;
    }

    public VisibleDelegate getVisibleDelegate() {
        if (mVisibleDelegate == null) {
            mVisibleDelegate = new VisibleDelegate(mSupportF);
        }
        return mVisibleDelegate;
    }

    public FragmentActivity getActivity() {
        return _mActivity;
    }

    private Animation getEnterAnim() {
        if (mCustomEnterAnim == Integer.MIN_VALUE) {
            if (mAnimHelper != null && mAnimHelper.enterAnim != null) {
                return mAnimHelper.enterAnim;
            }
        } else {
            try {
                return AnimationUtils.loadAnimation(_mActivity, mCustomEnterAnim);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    private long getEnterAnimDuration() {
        Animation enter = getEnterAnim();
        if (enter != null) {
            return enter.getDuration();
        }
        return NOT_FOUND_ANIM_TIME;
    }

    public long getExitAnimDuration() {
        if (mCustomExitAnim == Integer.MIN_VALUE) {
            if (mAnimHelper != null && mAnimHelper.exitAnim != null) {
                return mAnimHelper.exitAnim.getDuration();
            }
        } else {
            try {
                return AnimationUtils.loadAnimation(_mActivity, mCustomExitAnim).getDuration();
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
        return NOT_FOUND_ANIM_TIME;
    }

    private long getPopExitAnimDuration() {
        if (mCustomPopExitAnim == Integer.MIN_VALUE) {
            if (mAnimHelper != null && mAnimHelper.popExitAnim != null) {
                return mAnimHelper.popExitAnim.getDuration();
            }
        } else {
            try {
                return AnimationUtils.loadAnimation(_mActivity, mCustomPopExitAnim).getDuration();
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
        return NOT_FOUND_ANIM_TIME;
    }

    @Nullable
    Animation getExitAnim() {
        if (mCustomExitAnim == Integer.MIN_VALUE) {
            if (mAnimHelper != null && mAnimHelper.exitAnim != null) {
                return mAnimHelper.exitAnim;
            }
        } else {
            try {
                return AnimationUtils.loadAnimation(_mActivity, mCustomExitAnim);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    interface EnterAnimListener {
        void onEnterAnimStart();
    }
}