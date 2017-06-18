package me.yokeyword.fragmentation;

import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
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
    private static final String TAG = "Fragmentation";

    static final String FRAGMENTATION_ARG_RESULT_RECORD = "fragment_arg_result_record";
    static final String FRAGMENTATION_ARG_ANIM_DISABLE = "fragmentation_arg_is_root";
    static final String FRAGMENTATION_ARG_IS_SHARED_ELEMENT = "fragmentation_arg_is_shared_element";
    static final String FRAGMENTATION_ARG_CONTAINER = "fragmentation_arg_container";
    static final String FRAGMENTATION_ARG_REPLACE = "fragmentation_arg_replace";

    static final String FRAGMENTATION_STATE_SAVE_ANIMATOR = "fragmentation_state_save_animator";
    static final String FRAGMENTATION_STATE_SAVE_IS_HIDDEN = "fragmentation_state_save_status";

    static final int TYPE_ADD = 0;
    static final int TYPE_ADD_WITH_POP = 1;
    static final int TYPE_ADD_RESULT = 2;
    static final int TYPE_ADD_WITHOUT_HIDE = 3;
    static final int TYPE_REPLACE = 10;
    static final int TYPE_REPLACE_DONT_BACK = 14;

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

    /**
     * 分发load根Fragment事务
     *
     * @param containerId    容器id
     * @param to             目标Fragment
     * @param addToBackStack
     * @param allowAnimation
     */
    void loadRootTransaction(FragmentManager fragmentManager, int containerId, SupportFragment to, boolean addToBackStack, boolean allowAnimation) {
        bindContainerId(containerId, to);
        start(fragmentManager, null, to, to.getClass().getName(), !addToBackStack, null, allowAnimation, TYPE_ADD);
    }

    /**
     * 加载多个根Fragment
     */
    void loadMultipleRootTransaction(FragmentManager fragmentManager, int containerId, int showPosition, SupportFragment... tos) {
        fragmentManager = checkFragmentManager(fragmentManager, null);
        if (fragmentManager == null) return;
        FragmentTransaction ft = fragmentManager.beginTransaction();
        for (int i = 0; i < tos.length; i++) {
            SupportFragment to = tos[i];

            Bundle args = to.getArguments();
            if (args == null) {
                args = new Bundle();
                to.setArguments(args);
            }
            args.putBoolean(FRAGMENTATION_ARG_ANIM_DISABLE, true);
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
     * 分发start事务
     *
     * @param from        当前Fragment
     * @param to          目标Fragment
     * @param requestCode requestCode
     * @param launchMode  启动模式
     * @param type        类型
     */
    void dispatchStartTransaction(FragmentManager fragmentManager, SupportFragment from, SupportFragment to, int requestCode, int launchMode, int type) {
        fragmentManager = checkFragmentManager(fragmentManager, from);
        if (fragmentManager == null) return;

        checkNotNull(to, "toFragment == null");

        if (from != null) {
            if (from.getContainerId() == 0) {
                throw new RuntimeException("Can't find container, please call loadRootFragment() first!");
            }
            bindContainerId(from.getContainerId(), to);
        }

        // process SupportTransaction
        String toFragmentTag = to.getClass().getName();
        boolean dontAddToBackStack = false;
        ArrayList<TransactionRecord.SharedElement> sharedElementList = null;
        TransactionRecord transactionRecord = to.getTransactionRecord();
        if (transactionRecord != null) {
            if (transactionRecord.tag != null) {
                toFragmentTag = transactionRecord.tag;
            }
            dontAddToBackStack = transactionRecord.dontAddToBackStack;
            if (transactionRecord.sharedElementList != null) {
                sharedElementList = transactionRecord.sharedElementList;
                // 这里发现使用addSharedElement时,在被强杀重启时导致栈内顺序异常,这里进行一次hack顺序
                FragmentationHack.reorderIndices(fragmentManager);
            }
        }

        if (type == TYPE_ADD_RESULT) {
            saveRequestCode(to, requestCode);
        }

        if (handleLaunchMode(fragmentManager, to, toFragmentTag, launchMode)) return;

        if (type == TYPE_ADD_WITH_POP) {
            startWithPop(fragmentManager, from, to, toFragmentTag, type);
        } else {
            start(fragmentManager, from, to, toFragmentTag, dontAddToBackStack, sharedElementList, false, type);
        }
    }

    private void bindContainerId(int containerId, SupportFragment to) {
        Bundle args = to.getArguments();
        if (args == null) {
            args = new Bundle();
            to.setArguments(args);
        }
        args.putInt(FRAGMENTATION_ARG_CONTAINER, containerId);
    }

    /**
     * show一个Fragment,hide另一个／多个Fragment ; 主要用于类似微信主页那种 切换tab的情况
     *
     * @param showFragment 需要show的Fragment
     * @param hideFragment 需要hide的Fragment
     */
    void showHideFragment(FragmentManager fragmentManager, SupportFragment showFragment, SupportFragment hideFragment) {
        fragmentManager = checkFragmentManager(fragmentManager, null);
        if (fragmentManager == null) return;

        if (showFragment == hideFragment) return;

        FragmentTransaction ft = fragmentManager.beginTransaction().show(showFragment);

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
            ft.hide(hideFragment);
        }
        supportCommit(fragmentManager, ft);
    }

    private void start(FragmentManager fragmentManager, final SupportFragment from, SupportFragment to, String toFragmentTag,
                       boolean dontAddToBackStack, ArrayList<TransactionRecord.SharedElement> sharedElementList, boolean allowRootFragmentAnim, int type) {
        FragmentTransaction ft = fragmentManager.beginTransaction();
        boolean addMode = (type == TYPE_ADD || type == TYPE_ADD_RESULT || type == TYPE_ADD_WITHOUT_HIDE);
        Bundle bundle = to.getArguments();
        bundle.putBoolean(FRAGMENTATION_ARG_REPLACE, !addMode);

        if (sharedElementList == null) {
            if (addMode) { // replace模式禁止动画，官方的replace动画存在重叠Bug
                ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
            }
        } else {
            bundle.putBoolean(FRAGMENTATION_ARG_IS_SHARED_ELEMENT, true);
            for (TransactionRecord.SharedElement item : sharedElementList) {
                ft.addSharedElement(item.sharedElement, item.sharedName);
            }
        }
        if (from == null) {
            ft.add(bundle.getInt(FRAGMENTATION_ARG_CONTAINER), to, toFragmentTag);
            bundle.putBoolean(FRAGMENTATION_ARG_ANIM_DISABLE, !allowRootFragmentAnim);
        } else {
            if (addMode) {
                ft.add(from.getContainerId(), to, toFragmentTag);
                if (from.getTag() != null && type != TYPE_ADD_WITHOUT_HIDE) {
                    ft.hide(from);
                }
            } else {
                ft.replace(from.getContainerId(), to, toFragmentTag);
            }
        }

        if (!dontAddToBackStack && type != TYPE_REPLACE_DONT_BACK) {
            ft.addToBackStack(toFragmentTag);
        }
        supportCommit(fragmentManager, ft);
    }

    private void startWithPop(final FragmentManager fragmentManager, final SupportFragment from, final SupportFragment to, final String toFragmentTag, final int type) {
        fragmentManager.executePendingTransactions();

        final SupportFragment preFragment = getPreFragment(from);
        mockPopAnim(from, preFragment, from.mAnimHelper.popExitAnim, new Callback() {
            @Override
            public void call() {
                fragmentManager.popBackStackImmediate();
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        FragmentationHack.reorderIndices(fragmentManager);
                        if (preFragment != null) {
                            preFragment.start(to);
                        } else {
                            from.start(to);
                        }
                        fragmentManager.executePendingTransactions();
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
                // 这里的警告请重视，请在Activity回来后，在onPostResume()中执行该事务
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

    private SupportFragment getTopFragment(FragmentManager fragmentManager) {
        return SupportHelper.getTopFragment(fragmentManager);
    }

    private SupportFragment getPreFragment(Fragment fragment) {
        return SupportHelper.getPreFragment(fragment);
    }

    /**
     * 分发回退事件, 优先栈顶(有子栈则是子栈的栈顶)的Fragment
     */
    boolean dispatchBackPressedEvent(SupportFragment activeFragment) {
        if (activeFragment != null) {
            boolean result = activeFragment.onBackPressedSupport();
            if (result) {
                return true;
            }

            Fragment parentFragment = activeFragment.getParentFragment();
            if (dispatchBackPressedEvent((SupportFragment) parentFragment)) {
                return true;
            }
        }
        return false;
    }

    /**
     * handle LaunchMode
     */
    private boolean handleLaunchMode(FragmentManager fragmentManager, final SupportFragment toFragment, String toFragmentTag, int launchMode) {
        SupportFragment topFragment = getTopFragment(fragmentManager);
        if (topFragment == null) return false;
        final Fragment stackToFragment = SupportHelper.findStackFragment(toFragment.getClass(), toFragmentTag, fragmentManager);
        if (stackToFragment == null) return false;

        if (launchMode == SupportFragment.SINGLETOP) {
            if (toFragment == topFragment || toFragment.getClass().getName().equals(topFragment.getClass().getName())) {
                handleNewBundle(toFragment, stackToFragment);
                return true;
            }
        } else if (launchMode == SupportFragment.SINGLETASK) {
            popTo(toFragmentTag, false, null, fragmentManager, 0);
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    handleNewBundle(toFragment, stackToFragment);
                }
            });
            return true;
        }

        return false;
    }

    private void handleNewBundle(SupportFragment toFragment, Fragment stackToFragment) {
        Bundle argsNewBundle = toFragment.getNewBundle();

        Bundle args = toFragment.getArguments();
        if (args.containsKey(FRAGMENTATION_ARG_CONTAINER)) {
            args.remove(FRAGMENTATION_ARG_CONTAINER);
        }

        if (argsNewBundle != null) {
            args.putAll(argsNewBundle);
        }

        ((SupportFragment) stackToFragment).onNewBundle(args);
    }

    /**
     * save requestCode
     */
    private void saveRequestCode(Fragment to, int requestCode) {
        Bundle bundle = to.getArguments();
        if (bundle == null) {
            bundle = new Bundle();
            to.setArguments(bundle);
        }
        ResultRecord resultRecord = new ResultRecord();
        resultRecord.requestCode = requestCode;
        bundle.putParcelable(FRAGMENTATION_ARG_RESULT_RECORD, resultRecord);
    }

    void handleResultRecord(Fragment from) {
        final SupportFragment preFragment = getPreFragment(from);
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

    void remove(FragmentManager fm, SupportFragment fragment) {
        fm.beginTransaction()
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_CLOSE)
                .remove(fragment)
                .show(SupportHelper.getPreFragment(fragment))
                .commit();
    }


    void back(FragmentManager fm) {
        fm = checkFragmentManager(fm, null);
        if (fm == null) return;

        int count = fm.getBackStackEntryCount();
        if (count > 0) {
            debouncePop(fm);
        }
    }

    private void debouncePop(FragmentManager fm) {
        Fragment popF = fm.findFragmentByTag(fm.getBackStackEntryAt(fm.getBackStackEntryCount() - 1).getName());
        if (popF instanceof SupportFragment) {
            SupportFragment supportF = (SupportFragment) popF;
            if (supportF.mIsSharedElement) {
                long now = System.currentTimeMillis();
                if (now < mShareElementDebounceTime) {
                    mShareElementDebounceTime = System.currentTimeMillis() + supportF.getExitAnimDuration();
                    return;
                }
            }
            mShareElementDebounceTime = System.currentTimeMillis() + supportF.getExitAnimDuration();
        }

        fm.popBackStackImmediate();
    }

    /**
     * 出栈到目标fragment
     *
     * @param fragmentTag tag
     * @param includeSelf 是否包含该fragment
     */
    void popTo(final String fragmentTag, boolean includeSelf, final Runnable afterPopTransactionRunnable, FragmentManager fragmentManager, int popAnim) {
        fragmentManager = checkFragmentManager(fragmentManager, null);
        if (fragmentManager == null) return;

        fragmentManager.executePendingTransactions();
        Fragment targetFragment = fragmentManager.findFragmentByTag(fragmentTag);

        if (targetFragment == null) {
            Log.e(TAG, "Pop failure! Can't find FragmentTag:" + fragmentTag + " in the FragmentManager's Stack.");
            return;
        }

        int flag = 0;
        if (includeSelf) {
            flag = FragmentManager.POP_BACK_STACK_INCLUSIVE;
            targetFragment = getPreFragment(targetFragment);
        }

        SupportFragment fromFragment = getTopFragment(fragmentManager);
        Animation popAnimation;

        if (afterPopTransactionRunnable == null && popAnim == 0) {
            popAnimation = fromFragment.mAnimHelper.exitAnim;
        } else {
            if (popAnim == 0) {
                popAnimation = new Animation() {
                };
                popAnimation.setDuration(fromFragment.mAnimHelper.exitAnim.getDuration());
            } else {
                popAnimation = AnimationUtils.loadAnimation(mActivity, popAnim);
            }
        }

        final int finalFlag = flag;
        final FragmentManager finalFragmentManager = fragmentManager;

        mockPopAnim(fromFragment, targetFragment, popAnimation, new Callback() {
            @Override
            public void call() {
                popToFix(fragmentTag, finalFlag, finalFragmentManager);
                if (afterPopTransactionRunnable != null) {
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            mPopToTempFragmentManager = finalFragmentManager;
                            afterPopTransactionRunnable.run();
                            mPopToTempFragmentManager = null;
                        }
                    });
                }
            }
        });
    }

    /**
     * 解决popTo多个fragment时动画引起的异常问题
     */
    private void popToFix(String fragmentTag, int flag, final FragmentManager fragmentManager) {
        if (FragmentationHack.getActiveFragments(fragmentManager) == null) return;

        mSupport.getSupportDelegate().mPopMultipleNoAnim = true;
        fragmentManager.popBackStack(fragmentTag, flag);
        fragmentManager.executePendingTransactions();
        mSupport.getSupportDelegate().mPopMultipleNoAnim = false;

        mHandler.post(new Runnable() {
            @Override
            public void run() {
                FragmentationHack.reorderIndices(fragmentManager);
            }
        });
    }

    /**
     * hack startWithPop/popTo anim
     */
    private void mockPopAnim(SupportFragment fromF, Fragment targetF, Animation exitAnim, final Callback cb) {
        if (fromF == targetF) {
            if (cb != null) {
                cb.call();
            }
            return;
        }

        View view = mActivity.findViewById(fromF.getContainerId());
        final View fromView = fromF.getView();
        if (view instanceof ViewGroup && fromView != null) {
            final ViewGroup container = (ViewGroup) view;

            SupportFragment preF = getPreFragment(fromF);
            ViewGroup preViewGroup = null;

            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP && preF != targetF) {
                if (preF != null && preF.getView() instanceof ViewGroup) {
                    preViewGroup = (ViewGroup) preF.getView();
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
                handleMock(fromF, exitAnim, null, fromView, container);
            } else {
                container.removeViewInLayout(fromView);
                handleMock(fromF, exitAnim, cb, fromView, container);
            }
        }
    }

    private void handleMock(SupportFragment fromF, Animation exitAnim, Callback cb, View fromView, final ViewGroup container) {
        final ViewGroup mock = new ViewGroup(mActivity) {
            @Override
            protected void onLayout(boolean changed, int l, int t, int r, int b) {
            }
        };
        mock.addView(fromView);
        container.addView(mock);
        fromF.mLockAnim = true;

        if (cb != null) {
            cb.call();
        }
        exitAnim.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                mock.setVisibility(View.INVISIBLE);
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        container.removeView(mock);
                    }
                });
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });
        mock.startAnimation(exitAnim);
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

    private FragmentManager checkFragmentManager(FragmentManager fragmentManager, Fragment
            from) {
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
