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
import android.support.v4.app.FragmentationMagician;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import java.util.ArrayList;
import java.util.List;

import me.yokeyword.fragmentation.exception.AfterSaveStateTransactionWarning;
import me.yokeyword.fragmentation.helper.internal.ResultRecord;
import me.yokeyword.fragmentation.helper.internal.TransactionRecord;
import me.yokeyword.fragmentation.queue.Action;
import me.yokeyword.fragmentation.queue.ActionQueue;


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
    static final String FRAGMENTATION_ARG_CUSTOM_ENTER_ANIM = "fragmentation_arg_custom_enter_anim";
    static final String FRAGMENTATION_ARG_CUSTOM_EXIT_ANIM = "fragmentation_arg_custom_exit_anim";

    static final String FRAGMENTATION_STATE_SAVE_ANIMATOR = "fragmentation_state_save_animator";
    static final String FRAGMENTATION_STATE_SAVE_IS_HIDDEN = "fragmentation_state_save_status";

    private static final String FRAGMENTATION_STATE_SAVE_RESULT = "fragmentation_state_save_result";

    static final int TYPE_ADD = 0;
    static final int TYPE_ADD_RESULT = 1;
    static final int TYPE_ADD_WITHOUT_HIDE = 2;
    static final int TYPE_ADD_RESULT_WITHOUT_HIDE = 3;
    static final int TYPE_REPLACE = 10;
    static final int TYPE_REPLACE_DONT_BACK = 11;

    private ISupportActivity mSupport;
    private FragmentActivity mActivity;

    private Handler mHandler;

    ActionQueue mActionQueue;

    TransactionDelegate(ISupportActivity support) {
        this.mSupport = support;
        this.mActivity = (FragmentActivity) support;
        mHandler = new Handler(Looper.getMainLooper());
        mActionQueue = new ActionQueue(mHandler);
    }

    void post(final Runnable runnable) {
        mActionQueue.enqueue(new Action() {
            @Override
            public void run() {
                runnable.run();
            }
        });
    }

    void loadRootTransaction(final FragmentManager fm, final int containerId, final ISupportFragment to, final boolean addToBackStack, final boolean allowAnimation) {
        enqueue(fm, new Action() {
            @Override
            public void run() {
                bindContainerId(containerId, to);

                String toFragmentTag = to.getClass().getName();
                TransactionRecord transactionRecord = to.getSupportDelegate().mTransactionRecord;
                if (transactionRecord != null) {
                    if (transactionRecord.tag != null) {
                        toFragmentTag = transactionRecord.tag;
                    }
                }

                start(fm, null, to, toFragmentTag, !addToBackStack, null, allowAnimation, TYPE_REPLACE);
            }
        });
    }

    void loadMultipleRootTransaction(final FragmentManager fm, final int containerId, final int showPosition, final ISupportFragment... tos) {
        enqueue(fm, new Action() {
            @Override
            public void run() {
                FragmentTransaction ft = fm.beginTransaction();
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

                supportCommit(fm, ft);
            }
        });
    }

    /**
     * Dispatch the start transaction.
     */
    void dispatchStartTransaction(final FragmentManager fm, final ISupportFragment from, final ISupportFragment to, final int requestCode, final int launchMode, final int type) {
        enqueue(fm, new Action() {
            @Override
            public void run() {
                doDispatchStartTransaction(fm, from, to, requestCode, launchMode, type);
            }
        });
    }

    /**
     * Show showFragment then hide hideFragment
     */
    void showHideFragment(final FragmentManager fm, final ISupportFragment showFragment, final ISupportFragment hideFragment) {
        enqueue(fm, new Action() {
            @Override
            public void run() {
                doShowHideFragment(fm, showFragment, hideFragment);
            }
        });
    }

    /**
     * Start the target Fragment and pop itself
     */
    void startWithPop(final FragmentManager fm, final ISupportFragment from, final ISupportFragment to) {
        enqueue(fm, new Action(Action.ACTION_POP_MOCK) {
            @Override
            public void run() {
                handleAfterSaveInStateTransactionException(fm, "popTo()");
                FragmentationMagician.executePendingTransactionsAllowingStateLoss(fm);
                ISupportFragment currentFrom = SupportHelper.getTopFragment(fm);
                currentFrom.getSupportDelegate().mLockAnim = true;
                if (!FragmentationMagician.isStateSaved(fm)) {
                    mockStartWithPopAnim(SupportHelper.getTopFragment(fm), to, currentFrom.getSupportDelegate().mAnimHelper.popExitAnim);
                }
                FragmentationMagician.popBackStackAllowingStateLoss(fm);
                FragmentationMagician.executePendingTransactionsAllowingStateLoss(fm);
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        FragmentationMagician.reorderIndices(fm);
                    }
                });
            }
        });

        dispatchStartTransaction(fm, from, to, 0, ISupportFragment.STANDARD, TransactionDelegate.TYPE_ADD);
    }

    /**
     * Remove
     */
    void remove(final FragmentManager fm, final Fragment fragment, final boolean showPreFragment) {
        enqueue(fm, new Action(Action.ACTION_POP, fm) {
            @Override
            public void run() {
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
        });
    }

    /**
     * Pop
     */
    void pop(final FragmentManager fm) {
        enqueue(fm, new Action(Action.ACTION_POP, fm) {
            @Override
            public void run() {
                handleAfterSaveInStateTransactionException(fm, "pop()");
                FragmentationMagician.popBackStackAllowingStateLoss(fm);
            }
        });
    }

    void popForSwipeBack(final FragmentManager fm){
        enqueue(fm, new Action(Action.ACTION_POP_MOCK, fm) {
            @Override
            public void run() {
                mSupport.getSupportDelegate().mPopMultipleNoAnim = true;
                FragmentationMagician.popBackStackAllowingStateLoss(fm);
                FragmentationMagician.executePendingTransactionsAllowingStateLoss(fm);
                mSupport.getSupportDelegate().mPopMultipleNoAnim = false;
            }
        });
    }

    /**
     * Pop the last fragment transition from the manager's fragment pop stack.
     *
     * @param targetFragmentTag     Tag
     * @param includeTargetFragment Whether it includes targetFragment
     */
    void popTo(final String targetFragmentTag, final boolean includeTargetFragment, final Runnable afterPopTransactionRunnable, final FragmentManager fm, final int popAnim) {
        enqueue(fm, new Action(Action.ACTION_POP_MOCK) {
            @Override
            public void run() {
                doPopTo(targetFragmentTag, includeTargetFragment, afterPopTransactionRunnable, fm, popAnim);
            }
        });

        if (afterPopTransactionRunnable != null) {
            afterPopTransactionRunnable.run();
        }
    }

    /**
     * Dispatch the pop-event. Priority of the top of the stack of Fragment
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

    void handleResultRecord(Fragment from) {
        try {
            Bundle args = from.getArguments();
            if (args == null) return;
            final ResultRecord resultRecord = args.getParcelable(FRAGMENTATION_ARG_RESULT_RECORD);
            if (resultRecord == null) return;

            final ISupportFragment targetFragment = (ISupportFragment) from.getFragmentManager().getFragment(from.getArguments(), FRAGMENTATION_STATE_SAVE_RESULT);
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    targetFragment.onFragmentResult(resultRecord.requestCode, resultRecord.resultCode, resultRecord.resultBundle);
                }
            });
        } catch (IllegalStateException ignored) {
            // Fragment no longer exists
        }
    }

    private void enqueue(FragmentManager fm, Action action) {
        if (fm == null) {
            Log.w(TAG, "FragmentManager is null, skip the action!");
            return;
        }
        mActionQueue.enqueue(action);
    }

    private void doDispatchStartTransaction(FragmentManager fm, ISupportFragment from, ISupportFragment to, int requestCode, int launchMode, int type) {
        checkNotNull(to, "toFragment == null");

        if ((type == TYPE_ADD_RESULT || type == TYPE_ADD_RESULT_WITHOUT_HIDE) && from != null) {
            if (!((Fragment) from).isAdded()) {
                Log.w(TAG, ((Fragment) from).getClass().getSimpleName() + " has not been attached yet! startForResult() converted to start()");
            } else {
                saveRequestCode(fm, (Fragment) from, (Fragment) to, requestCode);
            }
        }

        if (from != null) {
            if (from.getSupportDelegate().mContainerId == 0) {
                Fragment fromF = (Fragment) from;
                if (fromF.getTag() != null && !fromF.getTag().startsWith("android:switcher:")) {
                    throw new IllegalStateException("Can't find container, please call loadRootFragment() first!");
                }
            }
            from = SupportHelper.getTopFragment(fm, from.getSupportDelegate().mContainerId);
        } else { // compat Activity
            from = SupportHelper.getTopFragment(fm);
        }
        if (from == null) return;
        bindContainerId(from.getSupportDelegate().mContainerId, to);

        // process ExtraTransaction
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
                FragmentationMagician.reorderIndices(fm);
            }
        }

        if (handleLaunchMode(fm, from, to, toFragmentTag, launchMode)) return;

        start(fm, from, to, toFragmentTag, dontAddToBackStack, sharedElementList, false, type);
    }

    private void start(FragmentManager fm, final ISupportFragment from, ISupportFragment to, String toFragmentTag,
                       boolean dontAddToBackStack, ArrayList<TransactionRecord.SharedElement> sharedElementList, boolean allowRootFragmentAnim, int type) {
        FragmentTransaction ft = fm.beginTransaction();
        boolean addMode = (type == TYPE_ADD || type == TYPE_ADD_RESULT || type == TYPE_ADD_WITHOUT_HIDE || type == TYPE_ADD_RESULT_WITHOUT_HIDE);
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
                    args.putInt(FRAGMENTATION_ARG_CUSTOM_ENTER_ANIM, record.targetFragmentEnter);
                    args.putInt(FRAGMENTATION_ARG_CUSTOM_EXIT_ANIM, record.targetFragmentExit);
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
                if (type != TYPE_ADD_WITHOUT_HIDE && type != TYPE_ADD_RESULT_WITHOUT_HIDE) {
                    ft.hide(fromF);
                }
            } else {
                ft.replace(from.getSupportDelegate().mContainerId, toF, toFragmentTag);
            }
        }

        if (!dontAddToBackStack && type != TYPE_REPLACE_DONT_BACK) {
            ft.addToBackStack(toFragmentTag);
        }
        supportCommit(fm, ft);
    }

    private void doShowHideFragment(FragmentManager fm, ISupportFragment showFragment, ISupportFragment hideFragment) {
        if (showFragment == hideFragment) return;

        FragmentTransaction ft = fm.beginTransaction().show((Fragment) showFragment);

        if (hideFragment == null) {
            List<Fragment> fragmentList = FragmentationMagician.getActiveFragments(fm);
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
        supportCommit(fm, ft);
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

    private void supportCommit(FragmentManager fm, FragmentTransaction transaction) {
        handleAfterSaveInStateTransactionException(fm, "commit()");
        transaction.commitAllowingStateLoss();
    }

    private boolean handleLaunchMode(FragmentManager fm, ISupportFragment topFragment, final ISupportFragment to, String toFragmentTag, int launchMode) {
        if (topFragment == null) return false;
        final ISupportFragment stackToFragment = SupportHelper.findStackFragment(to.getClass(), toFragmentTag, fm);
        if (stackToFragment == null) return false;

        if (launchMode == ISupportFragment.SINGLETOP) {
            if (to == topFragment || to.getClass().getName().equals(topFragment.getClass().getName())) {
                handleNewBundle(to, stackToFragment);
                return true;
            }
        } else if (launchMode == ISupportFragment.SINGLETASK) {
            popTo(toFragmentTag, false, null, fm, DEFAULT_POPTO_ANIM);
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
    private void saveRequestCode(FragmentManager fm, Fragment from, Fragment to, int requestCode) {
        Bundle bundle = getArguments(to);
        ResultRecord resultRecord = new ResultRecord();
        resultRecord.requestCode = requestCode;
        bundle.putParcelable(FRAGMENTATION_ARG_RESULT_RECORD, resultRecord);
        fm.putFragment(bundle, FRAGMENTATION_STATE_SAVE_RESULT, from);
    }

    private void doPopTo(final String targetFragmentTag, boolean includeTargetFragment, final Runnable afterPopTransactionRunnable, FragmentManager fm, int popAnim) {
        handleAfterSaveInStateTransactionException(fm, "popTo()");

        Fragment targetFragment = fm.findFragmentByTag(targetFragmentTag);

        if (targetFragment == null) {
            Log.e(TAG, "Pop failure! Can't find FragmentTag:" + targetFragmentTag + " in the FragmentManager's Stack.");
            return;
        }

        int flag = 0;
        if (includeTargetFragment) {
            flag = FragmentManager.POP_BACK_STACK_INCLUSIVE;
            targetFragment = (Fragment) SupportHelper.getPreFragment(targetFragment);
        }

        ISupportFragment fromFragment = SupportHelper.getTopFragment(fm);
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
        final FragmentManager finalFragmentManager = fm;

        mockPopAnim(fm, fromFragment, (ISupportFragment) targetFragment, popAnimation, afterPopTransactionRunnable != null, new Callback() {
            @Override
            public void call() {
                popToFix(targetFragmentTag, finalFlag, finalFragmentManager);
            }
        });
    }

    /**
     * To fix the FragmentManagerImpl.mAvailIndices incorrect ordering when pop() multiple Fragments
     * on pre-support-v4-25.4.0
     */
    private void popToFix(String fragmentTag, int flag, final FragmentManager fm) {
        if (FragmentationMagician.getActiveFragments(fm) == null) return;

        mSupport.getSupportDelegate().mPopMultipleNoAnim = true;
        FragmentationMagician.popBackStackAllowingStateLoss(fm, fragmentTag, flag);
        FragmentationMagician.executePendingTransactionsAllowingStateLoss(fm);
        mSupport.getSupportDelegate().mPopMultipleNoAnim = false;

        mHandler.post(new Runnable() {
            @Override
            public void run() {
                FragmentationMagician.reorderIndices(fm);
            }
        });
    }

    private void mockStartWithPopAnim(ISupportFragment from, ISupportFragment to, final Animation exitAnim) {
        final Fragment fromF = (Fragment) from;
        final ViewGroup container = findContainerById(fromF, from.getSupportDelegate().mContainerId);
        if (container == null) return;

        final View fromView = fromF.getView();
        if (fromView == null) return;

        container.removeViewInLayout(fromView);
        final ViewGroup mock = addMockView(fromView, container);

        to.getSupportDelegate().mEnterAnimListener = new SupportFragmentDelegate.EnterAnimListener() {
            @Override
            public void onEnterAnimStart() {
                fromView.startAnimation(exitAnim);

                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            mock.removeViewInLayout(fromView);
                            container.removeViewInLayout(mock);
                        } catch (Exception ignored) {
                        }
                    }
                }, exitAnim.getDuration());
            }
        };
    }

    /**
     * Hack startWithPop/popTo anim
     */
    private void mockPopAnim(FragmentManager fm, ISupportFragment from, ISupportFragment targetF, Animation exitAnim, boolean afterRunnable, final Callback cb) {
        Fragment fromF = (Fragment) from;
        View fromView = fromF.getView();

        if (from == targetF || FragmentationMagician.isStateSaved(fm) || fromView == null) {
            if (cb != null) {
                cb.call();
            }
            return;
        }

        final ViewGroup container = findContainerById(fromF, from.getSupportDelegate().mContainerId);
        if (container == null) return;

        Fragment preF = (Fragment) SupportHelper.getPreFragment(fromF);
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
            handleMock(exitAnim, null, fromView, container, afterRunnable);
        } else {
            container.removeViewInLayout(fromView);
            handleMock(exitAnim, cb, fromView, container, afterRunnable);
        }
    }

    private void handleMock(final Animation exitAnim, Callback cb, final View fromView, final ViewGroup container, boolean afterRunnable) {
        final ViewGroup mock = addMockView(fromView, container);

        if (cb != null) {
            cb.call();
        }

        long delay = 0;
        if (afterRunnable) {
            delay = Action.BUFFER_TIME * 2;
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

        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                fromView.startAnimation(exitAnim);
            }
        }, delay);

        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                try {
                    mock.removeViewInLayout(fromView);
                    container.removeViewInLayout(mock);
                } catch (Exception ignored) {
                }
            }
        }, exitAnim.getDuration() + delay);
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

    private static <T> void checkNotNull(T value, String message) {
        if (value == null) {
            throw new NullPointerException(message);
        }
    }

    private void handleAfterSaveInStateTransactionException(FragmentManager fm, String action) {
        boolean stateSaved = FragmentationMagician.isStateSaved(fm);
        if (stateSaved) {
            AfterSaveStateTransactionWarning e = new AfterSaveStateTransactionWarning(action);
            if (Fragmentation.getDefault().getHandler() != null) {
                Fragmentation.getDefault().getHandler().onException(e);
            }
        }
    }

    private interface Callback {
        void call();
    }
}
