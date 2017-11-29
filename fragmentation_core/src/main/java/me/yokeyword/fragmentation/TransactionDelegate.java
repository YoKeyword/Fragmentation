package me.yokeyword.fragmentation;

import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.FragmentationHack;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import java.util.ArrayList;
import java.util.List;

import me.yokeyword.fragmentation.helper.internal.ResultRecord;
import me.yokeyword.fragmentation.helper.internal.TransactionRecord;


/**
 * Controller
 * Created by YoKeyword on 16/1/22.
 */
class TransactionDelegate {
    static final int DEFAULT_POPTO_ANIM = Integer.MAX_VALUE;

    private static final String TAG = "Fragmentation";

    static final String FRAGMENTATION_ARG_RESULT_RECORD = "fragment_arg_result_record";
    static final String FRAGMENTATION_ARG_ROOT_STATUS = "fragmentation_arg_root_status";
    static final String FRAGMENTATION_ARG_IS_SHARED_ELEMENT = "fragmentation_arg_is_shared_element";
    static final String FRAGMENTATION_ARG_CONTAINER = "fragmentation_arg_container";
    static final String FRAGMENTATION_ARG_REPLACE = "fragmentation_arg_replace";
    static final String FRAGMENTATION_ARG_CUSTOM_END_ANIM = "fragmentation_arg_custom_end_anim";

    static final String FRAGMENTATION_STATE_SAVE_ANIMATOR = "fragmentation_state_save_animator";
    static final String FRAGMENTATION_STATE_SAVE_IS_HIDDEN = "fragmentation_state_save_status";

    static final int TYPE_ADD = 0;
    static final int TYPE_ADD_WITH_POP = 1;
    static final int TYPE_ADD_RESULT = 2;
    static final int TYPE_ADD_WITHOUT_HIDE = 3;
    static final int TYPE_ADD_RESULT_WITHOUT_HIDE = 4;
    static final int TYPE_REPLACE = 10;
    static final int TYPE_REPLACE_DONT_BACK = 14;

    private final static long BUFFER_TIME = 50L;

    private ISupportActivity mSupport;
    private FragmentActivity mActivity;

    private long mShareElementDebounceTime;
    private Handler mHandler;
    private FragmentManager mPopToTempFragmentManager;

    TransactionDelegate(ISupportActivity support) {
        this.mSupport = support;
        this.mActivity = (FragmentActivity) support;
        mHandler = new Handler(Looper.getMainLooper());
    }

    void loadRootTransaction(FragmentManager fragmentManager, int containerId, ISupportFragment to, boolean addToBackStack, boolean allowAnimation) {
        fragmentManager = checkFragmentManager(fragmentManager, null);
        if (fragmentManager == null) return;

        bindContainerId(containerId, to);
        start(fragmentManager, null, to, to.getClass().getName(), !addToBackStack, null, allowAnimation, TYPE_REPLACE);
    }

    void loadMultipleRootTransaction(FragmentManager fragmentManager, int containerId, int showPosition, ISupportFragment... tos) {
        fragmentManager = checkFragmentManager(fragmentManager, null);
        if (fragmentManager == null) return;
        FragmentTransaction ft = fragmentManager.beginTransaction();
        for (int i = 0; i < tos.length; i++) {
            Fragment to = (Fragment) tos[i];

            Bundle args = getArguments(to);
            args.putInt(FRAGMENTATION_ARG_ROOT_STATUS, SupportFragmentDelegate.STATUS_ROOT_ANIM_DISABLE);
            bindContainerId(containerId, tos[i]);

            String toName = to.getClass().getName();
            ft.add(containerId, to, toName);

            if (i != showPosition) {
                ft.hide(to);
            }
        }

        supportCommit(fragmentManager, ft);
    }

    /**
     * Dispatch the start transaction.
     */
    void dispatchStartTransaction(FragmentManager fragmentManager, ISupportFragment from, ISupportFragment to, int requestCode, int launchMode, int type) {
        fragmentManager = checkFragmentManager(fragmentManager, from);
        if (fragmentManager == null) return;

        checkNotNull(to, "toFragment == null");

        if (from != null) {
            if (from.getSupportDelegate().mContainerId == 0) {
                Fragment fromF = (Fragment) from;
                if (fromF.getTag() != null && !fromF.getTag().startsWith("android:switcher:")) {
                    throw new RuntimeException("Can't find container, please call loadRootFragment() first!");
                }
            }
            bindContainerId(from.getSupportDelegate().mContainerId, to);
            from = SupportHelper.getTopFragment(fragmentManager, from.getSupportDelegate().mContainerId);
        }

        // process SupportTransaction
        String toFragmentTag = to.getClass().getName();
        boolean dontAddToBackStack = false;
        ArrayList<TransactionRecord.SharedElement> sharedElementList = null;
        TransactionRecord transactionRecord = to.getSupportDelegate().mTransactionRecord;
        if (transactionRecord != null) {
            if (transactionRecord.tag != null) {
                toFragmentTag = transactionRecord.tag;
            }
            dontAddToBackStack = transactionRecord.dontAddToBackStack;
            if (transactionRecord.sharedElementList != null) {
                sharedElementList = transactionRecord.sharedElementList;
                // Compat SharedElement
                FragmentationHack.reorderIndices(fragmentManager);
            }
        }

        if (type == TYPE_ADD_RESULT || type == TYPE_ADD_RESULT_WITHOUT_HIDE) {
            saveRequestCode((Fragment) to, requestCode);
        }

        if (handleLaunchMode(fragmentManager, from, to, toFragmentTag, launchMode)) return;

        if (type == TYPE_ADD_WITH_POP) {
            startWithPop(fragmentManager, from, to);
        } else {
            start(fragmentManager, from, to, toFragmentTag, dontAddToBackStack, sharedElementList, false, type);
        }
    }

    void showHideFragment(FragmentManager fragmentManager, ISupportFragment showFragment, ISupportFragment hideFragment) {
        fragmentManager = checkFragmentManager(fragmentManager, null);
        if (fragmentManager == null) return;

        if (showFragment == hideFragment) return;

        FragmentTransaction ft = fragmentManager.beginTransaction().show((Fragment) showFragment);

        if (hideFragment == null) {
            List<Fragment> fragmentList = FragmentationHack.getActiveFragments(fragmentManager);
            if (fragmentList != null) {
                for (Fragment fragment : fragmentList) {
                    if (fragment != null && fragment != showFragment) {
                        ft.hide(fragment);
                    }
                }
            }
        } else {
            ft.hide((Fragment) hideFragment);
        }
        supportCommit(fragmentManager, ft);
    }

    private void start(FragmentManager fragmentManager, final ISupportFragment from, ISupportFragment to, String toFragmentTag,
                       boolean dontAddToBackStack, ArrayList<TransactionRecord.SharedElement> sharedElementList, boolean allowRootFragmentAnim, int type) {
        FragmentTransaction ft = fragmentManager.beginTransaction();
        boolean addMode = (type == TYPE_ADD || type == TYPE_ADD_RESULT || type == TYPE_ADD_WITHOUT_HIDE);
        Fragment fromF = (Fragment) from;
        Fragment toF = (Fragment) to;
        Bundle args = getArguments(toF);
        args.putBoolean(FRAGMENTATION_ARG_REPLACE, !addMode);

        if (sharedElementList == null) {
            if (addMode) { // Replace mode forbidden animation, the replace animations exist overlapping Bug on support-v4.

                TransactionRecord record = to.getSupportDelegate().mTransactionRecord;
                if (record != null && record.targetFragmentEnter != Integer.MIN_VALUE) {
                    ft.setCustomAnimations(record.targetFragmentEnter, record.currentFragmentPopExit,
                            record.currentFragmentPopEnter, record.targetFragmentExit);
                    args.putInt(FRAGMENTATION_ARG_CUSTOM_END_ANIM, record.targetFragmentEnter);
                } else {
                    ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
                }
            } else {
                args.putInt(FRAGMENTATION_ARG_ROOT_STATUS, SupportFragmentDelegate.STATUS_ROOT_ANIM_DISABLE);
            }
        } else {
            args.putBoolean(FRAGMENTATION_ARG_IS_SHARED_ELEMENT, true);
            for (TransactionRecord.SharedElement item : sharedElementList) {
                ft.addSharedElement(item.sharedElement, item.sharedName);
            }
        }
        if (from == null) {
            ft.replace(args.getInt(FRAGMENTATION_ARG_CONTAINER), toF, toFragmentTag);
            if (!addMode) {
                ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
                args.putInt(FRAGMENTATION_ARG_ROOT_STATUS, allowRootFragmentAnim ?
                        SupportFragmentDelegate.STATUS_ROOT_ANIM_ENABLE : SupportFragmentDelegate.STATUS_ROOT_ANIM_DISABLE);
            }
        } else {
            if (addMode) {
                ft.add(from.getSupportDelegate().mContainerId, toF, toFragmentTag);
                if (type != TYPE_ADD_WITHOUT_HIDE) {
                    ft.hide(fromF);
                }
            } else {
                ft.replace(from.getSupportDelegate().mContainerId, toF, toFragmentTag);
            }
        }

        if (!dontAddToBackStack && type != TYPE_REPLACE_DONT_BACK) {
            ft.addToBackStack(toFragmentTag);
        }
        supportCommit(fragmentManager, ft);
    }

    private void bindContainerId(int containerId, ISupportFragment to) {
        Bundle args = getArguments((Fragment) to);
        args.putInt(FRAGMENTATION_ARG_CONTAINER, containerId);
    }

    private Bundle getArguments(Fragment fragment) {
        Bundle bundle = fragment.getArguments();
        if (bundle == null) {
            bundle = new Bundle();
            fragment.setArguments(bundle);
        }
        return bundle;
    }

    private void startWithPop(final FragmentManager fragmentManager, final ISupportFragment from, final ISupportFragment to) {
        if (FragmentationHack.isExecutingActions(fragmentManager)) {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    executeStartWithPop(fragmentManager, from, to);
                }
            });
            return;
        }

        executeStartWithPop(fragmentManager, from, to);
    }

    private void executeStartWithPop(final FragmentManager fragmentManager, final ISupportFragment from, final ISupportFragment to) {
        fragmentManager.executePendingTransactions();
        final ISupportFragment preFragment = getPreFragment((Fragment) from);
        final int fromContainerId = from.getSupportDelegate().mContainerId;

        mockStartWithPopAnim(from, to, from.getSupportDelegate().mAnimHelper.popExitAnim);
        fragmentManager.popBackStackImmediate();

        mHandler.post(new Runnable() {
            @Override
            public void run() {
                /**
                 * This `post` is for compatibility with v4-27.0.0
                 * @see android.support.v4.app.FragmentManagerImpl#animateRemoveFragment
                 */
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        fragmentManager.executePendingTransactions();
                        FragmentationHack.reorderIndices(fragmentManager);
                        if (preFragment != null && preFragment.getSupportDelegate().mContainerId == fromContainerId) {
                            preFragment.getSupportDelegate().start(to);
                        } else {
                            dispatchStartTransaction(fragmentManager, from, to, 0, ISupportFragment.STANDARD, TYPE_ADD);
                        }
                    }
                });
            }
        });
    }

    private void supportCommit(FragmentManager fragmentManager, FragmentTransaction transaction) {
        if (Fragmentation.getDefault().isDebug()) {
            transaction.commit();
        } else {
            boolean stateSaved = FragmentationHack.isStateSaved(fragmentManager);
            if (stateSaved) {
                Log.e(TAG, "Please beginTransaction in onPostResume() after the Activity returns!");
                IllegalStateException e = new IllegalStateException("Can not perform this action after onSaveInstanceState!");
                e.printStackTrace();
                if (Fragmentation.getDefault().getHandler() != null) {
                    Fragmentation.getDefault().getHandler().onException(e);
                }
            }
            transaction.commitAllowingStateLoss();
        }
    }

    private ISupportFragment getTopFragment(FragmentManager fragmentManager) {
        return SupportHelper.getTopFragment(fragmentManager);
    }

    private ISupportFragment getPreFragment(Fragment fragment) {
        return SupportHelper.getPreFragment(fragment);
    }

    /**
     * Dispatch the back-event. Priority of the top of the stack of Fragment
     */
    boolean dispatchBackPressedEvent(ISupportFragment activeFragment) {
        if (activeFragment != null) {
            boolean result = activeFragment.onBackPressedSupport();
            if (result) {
                return true;
            }

            Fragment parentFragment = ((Fragment) activeFragment).getParentFragment();
            if (dispatchBackPressedEvent((ISupportFragment) parentFragment)) {
                return true;
            }
        }
        return false;
    }

    /**
     * handle LaunchMode
     */
    private boolean handleLaunchMode(FragmentManager fragmentManager, ISupportFragment topFragment, final ISupportFragment to, String toFragmentTag, int launchMode) {
        if (topFragment == null) return false;
        final ISupportFragment stackToFragment = SupportHelper.findStackFragment(to.getClass(), toFragmentTag, fragmentManager);
        if (stackToFragment == null) return false;

        if (launchMode == ISupportFragment.SINGLETOP) {
            if (to == topFragment || to.getClass().getName().equals(topFragment.getClass().getName())) {
                handleNewBundle(to, stackToFragment);
                return true;
            }
        } else if (launchMode == ISupportFragment.SINGLETASK) {
            popTo(toFragmentTag, false, null, fragmentManager, DEFAULT_POPTO_ANIM);
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    handleNewBundle(to, stackToFragment);
                }
            });
            return true;
        }

        return false;
    }

    private void handleNewBundle(ISupportFragment toFragment, ISupportFragment stackToFragment) {
        Bundle argsNewBundle = toFragment.getSupportDelegate().mNewBundle;

        Bundle args = getArguments((Fragment) toFragment);
        if (args.containsKey(FRAGMENTATION_ARG_CONTAINER)) {
            args.remove(FRAGMENTATION_ARG_CONTAINER);
        }

        if (argsNewBundle != null) {
            args.putAll(argsNewBundle);
        }

        stackToFragment.onNewBundle(args);
    }

    /**
     * save requestCode
     */
    private void saveRequestCode(Fragment to, int requestCode) {
        Bundle bundle = getArguments(to);
        ResultRecord resultRecord = new ResultRecord();
        resultRecord.requestCode = requestCode;
        bundle.putParcelable(FRAGMENTATION_ARG_RESULT_RECORD, resultRecord);
    }

    void handleResultRecord(Fragment from) {
        final ISupportFragment preFragment = getPreFragment(from);
        if (preFragment == null) return;

        Bundle args = from.getArguments();
        if (args == null || !args.containsKey(FRAGMENTATION_ARG_RESULT_RECORD)) return;

        final ResultRecord resultRecord = args.getParcelable(FRAGMENTATION_ARG_RESULT_RECORD);
        if (resultRecord == null) return;

        mHandler.post(new Runnable() {
            @Override
            public void run() {
                preFragment.onFragmentResult(resultRecord.requestCode, resultRecord.resultCode, resultRecord.resultBundle);
            }
        });
    }

    void remove(FragmentManager fm, Fragment fragment, boolean showPreFragment) {
        FragmentTransaction ft = fm.beginTransaction()
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_CLOSE)
                .remove(fragment);

        if (showPreFragment) {
            ISupportFragment preFragment = SupportHelper.getPreFragment(fragment);
            if (preFragment instanceof Fragment) {
                ft.show((Fragment) preFragment);
            }
        }
        supportCommit(fm, ft);
    }


    void back(FragmentManager fm) {
        fm = checkFragmentManager(fm, null);
        if (fm == null) return;

        int count = fm.getBackStackEntryCount();
        if (count > 0) {
            executeDebouncePop(fm);
        }
    }

    private void executeDebouncePop(FragmentManager fm) {
        Fragment popF = fm.findFragmentByTag(fm.getBackStackEntryAt(fm.getBackStackEntryCount() - 1).getName());
        if (popF instanceof ISupportFragment) {
            ISupportFragment supportF = (ISupportFragment) popF;
            if (supportF.getSupportDelegate().mIsSharedElement) {
                long now = System.currentTimeMillis();
                if (now < mShareElementDebounceTime) {
                    mShareElementDebounceTime = System.currentTimeMillis() + supportF.getSupportDelegate().mAnimHelper.exitAnim.getDuration();
                    return;
                }
            }
            mShareElementDebounceTime = System.currentTimeMillis() + supportF.getSupportDelegate().mAnimHelper.exitAnim.getDuration();
        }

        fm.popBackStack();
    }

    /**
     * Pop the last fragment transition from the manager's fragment back stack.
     *
     * @param targetFragmentTag     Tag
     * @param includeTargetFragment Whether it includes targetFragment
     */
    void popTo(final String targetFragmentTag, final boolean includeTargetFragment, final Runnable afterPopTransactionRunnable, FragmentManager fragmentManager, final int popAnim) {
        fragmentManager = checkFragmentManager(fragmentManager, null);
        if (fragmentManager == null) return;

        if (FragmentationHack.isExecutingActions(fragmentManager)) {
            final FragmentManager finalFragmentManager = fragmentManager;
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    executePopTo(targetFragmentTag, includeTargetFragment, afterPopTransactionRunnable, finalFragmentManager, popAnim);
                }
            });
            return;
        }
        executePopTo(targetFragmentTag, includeTargetFragment, afterPopTransactionRunnable, fragmentManager, popAnim);
    }

    private void executePopTo(final String targetFragmentTag, boolean includeTargetFragment, final Runnable afterPopTransactionRunnable, FragmentManager fragmentManager, int popAnim) {
        fragmentManager.executePendingTransactions();
        Fragment targetFragment = fragmentManager.findFragmentByTag(targetFragmentTag);

        if (targetFragment == null) {
            Log.e(TAG, "Pop failure! Can't find FragmentTag:" + targetFragmentTag + " in the FragmentManager's Stack.");
            return;
        }

        int flag = 0;
        if (includeTargetFragment) {
            flag = FragmentManager.POP_BACK_STACK_INCLUSIVE;
            targetFragment = (Fragment) getPreFragment(targetFragment);
        }

        ISupportFragment fromFragment = getTopFragment(fragmentManager);
        Animation popAnimation;

        if (afterPopTransactionRunnable == null && popAnim == DEFAULT_POPTO_ANIM) {
            popAnimation = fromFragment.getSupportDelegate().mAnimHelper.exitAnim;
        } else {
            if (popAnim == DEFAULT_POPTO_ANIM) {
                popAnimation = new Animation() {
                };
                popAnimation.setDuration(fromFragment.getSupportDelegate().mAnimHelper.exitAnim.getDuration());
            } else if (popAnim == 0) {
                popAnimation = new Animation() {
                };
            } else {
                popAnimation = AnimationUtils.loadAnimation(mActivity, popAnim);
            }
        }

        final int finalFlag = flag;
        final FragmentManager finalFragmentManager = fragmentManager;

        mockPopAnim(fromFragment, (ISupportFragment) targetFragment, popAnimation, afterPopTransactionRunnable != null, new Callback() {
            @Override
            public void call() {
                popToFix(targetFragmentTag, finalFlag, finalFragmentManager);
                if (afterPopTransactionRunnable != null) {
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            /**
                             * This `post` is for compatibility with v4-27.0.0
                             * @see android.support.v4.app.FragmentManagerImpl#animateRemoveFragment
                             */
                            mHandler.post(new Runnable() {
                                @Override
                                public void run() {
                                    mPopToTempFragmentManager = finalFragmentManager;
                                    afterPopTransactionRunnable.run();
                                    mPopToTempFragmentManager = null;
                                }
                            });
                        }
                    });
                }
            }
        });
    }

    /**
     * To fix the FragmentManagerImpl.mAvailIndices incorrect ordering when pop() multiple Fragments
     * on pre-support-v4-25.4.0
     */
    private void popToFix(String fragmentTag, int flag, final FragmentManager fragmentManager) {
        if (FragmentationHack.getActiveFragments(fragmentManager) == null) return;

        mSupport.getSupportDelegate().mPopMultipleNoAnim = true;
        fragmentManager.popBackStackImmediate(fragmentTag, flag);
        fragmentManager.executePendingTransactions();
        mSupport.getSupportDelegate().mPopMultipleNoAnim = false;

        mHandler.post(new Runnable() {
            @Override
            public void run() {
                FragmentationHack.reorderIndices(fragmentManager);
            }
        });
    }

    private void mockStartWithPopAnim(ISupportFragment from, ISupportFragment to, final Animation exitAnim) {
        Fragment fromF = (Fragment) from;
        final ViewGroup container = findContainerById(fromF, from.getSupportDelegate().mContainerId);
        if (container == null) return;

        from.getSupportDelegate().mLockAnim = true;
        View fromView = fromF.getView();
        container.removeViewInLayout(fromView);

        final ViewGroup mock = addMockView(fromView, container);

        to.getSupportDelegate().mEnterAnimListener = new SupportFragmentDelegate.EnterAnimListener() {
            @Override
            public void onEnterAnimStart() {
                mock.startAnimation(exitAnim);

                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        container.removeView(mock);
                    }
                }, exitAnim.getDuration() + BUFFER_TIME);
            }
        };
    }

    /**
     * Hack startWithPop/popTo anim
     */
    private void mockPopAnim(ISupportFragment from, ISupportFragment targetF, Animation exitAnim, boolean afterRunnable, final Callback cb) {
        if (from == targetF) {
            if (cb != null) {
                cb.call();
            }
            return;
        }

        Fragment fromF = (Fragment) from;
        final ViewGroup container = findContainerById(fromF, from.getSupportDelegate().mContainerId);
        if (container == null) return;

        View fromView = fromF.getView();
        Fragment preF = (Fragment) getPreFragment(fromF);
        ViewGroup preViewGroup = null;
        from.getSupportDelegate().mLockAnim = true;

        // Compatible with flicker on pre-L when calling popTo()
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            if (preF != targetF) {
                if (preF != null && preF.getView() instanceof ViewGroup) {
                    preViewGroup = (ViewGroup) preF.getView();
                }
            }
        }

        if (preViewGroup != null) {
            hideChildView(preViewGroup);
            container.removeViewInLayout(fromView);
            preViewGroup.addView(fromView);
            if (cb != null) {
                cb.call();
            }
            preViewGroup.removeViewInLayout(fromView);
            handleMock(exitAnim, afterRunnable, null, fromView, container);
        } else {
            container.removeViewInLayout(fromView);
            handleMock(exitAnim, afterRunnable, cb, fromView, container);
        }
    }

    private void handleMock(Animation exitAnim, boolean afterRunnable, Callback cb, final View fromView, final ViewGroup container) {
        final ViewGroup mock = addMockView(fromView, container);

        if (cb != null) {
            cb.call();
        }

        long delay = exitAnim.getDuration();
        if (afterRunnable) {
            long duration = exitAnim.getDuration();
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
                duration = duration + BUFFER_TIME * 2;
                delay = duration + BUFFER_TIME;
            } else {
                duration = duration + BUFFER_TIME;
                delay = duration;
            }
            exitAnim.setDuration(duration);
        }

        exitAnim.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                mock.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });

        mock.startAnimation(exitAnim);

        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                mock.removeViewInLayout(fromView);
                container.removeViewInLayout(mock);
            }
        }, delay);
    }

    @NonNull
    private ViewGroup addMockView(View fromView, ViewGroup container) {
        ViewGroup mock = new ViewGroup(mActivity) {
            @Override
            protected void onLayout(boolean changed, int l, int t, int r, int b) {
            }
        };
        mock.addView(fromView);
        container.addView(mock);
        return mock;
    }

    private ViewGroup findContainerById(Fragment fragment, int containerId) {
        if (fragment.getView() == null) return null;

        View container;
        Fragment parentFragment = fragment.getParentFragment();
        if (parentFragment != null) {
            if (parentFragment.getView() != null) {
                container = parentFragment.getView().findViewById(containerId);
            } else {
                container = findContainerById(parentFragment, containerId);
            }
        } else {
            container = mActivity.findViewById(containerId);
        }

        if (container instanceof ViewGroup) {
            return (ViewGroup) container;
        }

        return null;
    }

    private void hideChildView(ViewGroup viewGroup) {
        for (int i = 0; i < viewGroup.getChildCount(); i++) {
            View child = viewGroup.getChildAt(i);
            child.setVisibility(View.GONE);
        }
    }

    private static <T> T checkNotNull(T value, String message) {
        if (value == null) {
            throw new NullPointerException(message);
        }
        return value;
    }

    private FragmentManager checkFragmentManager(FragmentManager fragmentManager, ISupportFragment from) {
        if (fragmentManager == null) {
            if (mPopToTempFragmentManager == null) {
                String fromName = from == null ? "Fragment" : from.getClass().getSimpleName();
                Log.e(TAG, fromName + "'s FragmentManager is null, " + " Please check if " + fromName + " is destroyed!");
                return null;
            }
            return mPopToTempFragmentManager;
        }
        return fragmentManager;
    }

    private interface Callback {
        void call();
    }
}
