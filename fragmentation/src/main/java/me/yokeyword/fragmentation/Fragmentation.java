package me.yokeyword.fragmentation;

import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.FragmentTransactionBugFixHack;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

import me.yokeyword.fragmentation.debug.DebugFragmentRecord;
import me.yokeyword.fragmentation.debug.DebugHierarchyViewContainer;
import me.yokeyword.fragmentation.helper.internal.OnFragmentDestoryViewListener;
import me.yokeyword.fragmentation.helper.internal.ResultRecord;
import me.yokeyword.fragmentation.helper.internal.TransactionRecord;


/**
 * Controller
 * Created by YoKeyword on 16/1/22.
 */
class Fragmentation {
    static final String TAG = Fragmentation.class.getSimpleName();

    static final String FRAGMENTATION_ARG_RESULT_RECORD = "fragment_arg_result_record";
    static final String FRAGMENTATION_ARG_IS_ROOT = "fragmentation_arg_is_root";
    static final String FRAGMENTATION_ARG_IS_SHARED_ELEMENT = "fragmentation_arg_is_shared_element";
    static final String FRAGMENTATION_ARG_CONTAINER = "fragmentation_arg_container";

    static final String FRAGMENTATION_STATE_SAVE_ANIMATOR = "fragmentation_state_save_animator";
    static final String FRAGMENTATION_STATE_SAVE_IS_HIDDEN = "fragmentation_state_save_status";
    static final String FRAGMENTATION_STATE_SAVE_IS_SUPPORT_VISIBLE = "fragmentation_state_save_support_visible";
    static final String FRAGMENTATION_STATE_SAVE_IS_INVISIBLE_WHEN_LEAVE = "fragmentation_state_save_invisible_when_leave";

    private long mShareElementDebounceTime;
    private static final long BUFFER_TIME = 50L;

    static final int TYPE_ADD = 0;
    static final int TYPE_ADD_WITH_POP = 1;
    static final int TYPE_ADD_RESULT = 2;

    private SupportActivity mActivity;

    private Handler mHandler;
    private FragmentManager mPopToTempFragmentManager;

    Fragmentation(SupportActivity activity) {
        this.mActivity = activity;
        mHandler = mActivity.getHandler();
    }

    /**
     * 分发load根Fragment事务
     *
     * @param containerId 容器id
     * @param to          目标Fragment
     */
    void loadRootTransaction(FragmentManager fragmentManager, int containerId, SupportFragment to) {
        bindContainerId(containerId, to);
        dispatchStartTransaction(fragmentManager, null, to, 0, SupportFragment.STANDARD, TYPE_ADD);
    }

    /**
     * replace分发load根Fragment事务
     *
     * @param containerId 容器id
     * @param to          目标Fragment
     */
    void replaceLoadRootTransaction(FragmentManager fragmentManager, int containerId, SupportFragment to, boolean addToBack) {
        replaceTransaction(fragmentManager, containerId, to, addToBack);
    }

    /**
     * 加载多个根Fragment
     */
    void loadMultipleRootTransaction(FragmentManager fragmentManager, int containerId, int showPosition, SupportFragment... tos) {
        fragmentManager = checkFragmentManager(fragmentManager, null);
        if (fragmentManager == null) return;
        FragmentTransaction ft = fragmentManager.beginTransaction()
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        for (int i = 0; i < tos.length; i++) {
            SupportFragment to = tos[i];

            bindContainerId(containerId, tos[i]);

            String toName = to.getClass().getName();
            ft.add(containerId, to, toName);

            if (i != showPosition) {
                ft.hide(to);
            }

            Bundle bundle = to.getArguments();
            bundle.putBoolean(FRAGMENTATION_ARG_IS_ROOT, true);
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

        if ((from != null && from.isRemoving())) {
            Log.e(TAG, from.getClass().getSimpleName() + " is poped, maybe you want to call startWithPop()!");
            return;
        }

        checkNotNull(to, "toFragment == null");

        if (from != null) {
            bindContainerId(from.getContainerId(), to);
        }

        // process SupportTransaction
        String toFragmentTag = to.getClass().getName();
        TransactionRecord transactionRecord = to.getTransactionRecord();
        if (transactionRecord != null) {
            if (transactionRecord.tag != null) {
                toFragmentTag = transactionRecord.tag;
            }

            if (transactionRecord.requestCode != null && transactionRecord.requestCode != 0) {
                requestCode = transactionRecord.requestCode;
                type = TYPE_ADD_RESULT;
            }

            if (transactionRecord.launchMode != null) {
                launchMode = transactionRecord.launchMode;
            }

            if (transactionRecord.withPop != null && transactionRecord.withPop) {
                type = TYPE_ADD_WITH_POP;
            }

            // 这里发现使用addSharedElement时,在被强杀重启时导致栈内顺序异常,这里进行一次hack顺序
            if (transactionRecord.sharedElementList != null) {
                FragmentTransactionBugFixHack.reorderIndices(fragmentManager);
            }
        }

        if (type == TYPE_ADD_RESULT) {
            saveRequestCode(to, requestCode);
        }

        if (handleLaunchMode(fragmentManager, to, toFragmentTag, launchMode)) return;

        switch (type) {
            case TYPE_ADD:
            case TYPE_ADD_RESULT:
                start(fragmentManager, from, to, toFragmentTag, transactionRecord == null ? null : transactionRecord.sharedElementList);
                break;
            case TYPE_ADD_WITH_POP:
                if (from != null) {
                    startWithPop(fragmentManager, from, to, toFragmentTag);
                }
                break;
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
     * replace事务, 主要用于子Fragment之间的replace
     *
     * @param from      当前Fragment
     * @param to        目标Fragment
     * @param addToBack 是否添加到回退栈
     */
    void replaceTransaction(SupportFragment from, SupportFragment to, boolean addToBack) {
        replaceTransaction(from.getFragmentManager(), from.getContainerId(), to, addToBack);
    }

    /**
     * replace事务, 主要用于子Fragment之间的replace
     */
    void replaceTransaction(FragmentManager fragmentManager, int containerId, SupportFragment to, boolean addToBack) {
        fragmentManager = checkFragmentManager(fragmentManager, null);
        if (fragmentManager == null) return;

        checkNotNull(to, "toFragment == null");
        bindContainerId(containerId, to);
        FragmentTransaction ft = fragmentManager.beginTransaction();
        ft.replace(containerId, to, to.getClass().getName());
        if (addToBack) {
            ft.addToBackStack(to.getClass().getName());
        }
        Bundle bundle = to.getArguments();
        bundle.putBoolean(FRAGMENTATION_ARG_IS_ROOT, true);
        supportCommit(fragmentManager, ft);
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
            List<Fragment> fragmentList = fragmentManager.getFragments();
            if (fragmentList != null) {
                for (Fragment fragment : fragmentList) {
                    if (fragment != showFragment) {
                        ft.hide(fragment);
                    }
                }
            }
        } else {
            ft.hide(hideFragment);
        }
        supportCommit(fragmentManager, ft);
    }

    void start(FragmentManager fragmentManager, SupportFragment from, SupportFragment to, String toFragmentTag, ArrayList<TransactionRecord.SharedElement> sharedElementList) {
        FragmentTransaction ft = fragmentManager.beginTransaction();

        Bundle bundle = to.getArguments();

        if (sharedElementList == null) {
            ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        } else {
            bundle.putBoolean(FRAGMENTATION_ARG_IS_SHARED_ELEMENT, true);
            for (TransactionRecord.SharedElement item : sharedElementList) {
                ft.addSharedElement(item.sharedElement, item.sharedName);
            }
        }
        if (from == null) {
            ft.add(bundle.getInt(FRAGMENTATION_ARG_CONTAINER), to, toFragmentTag);
            bundle.putBoolean(FRAGMENTATION_ARG_IS_ROOT, true);
        } else {
            ft.add(from.getContainerId(), to, toFragmentTag);
            if (from.getTag() != null) {
                ft.hide(from);
            }
        }

        ft.addToBackStack(toFragmentTag);
        supportCommit(fragmentManager, ft);
    }

    void startWithPop(final FragmentManager fragmentManager, SupportFragment from, SupportFragment to, String toFragmentTag) {
        fragmentManager.executePendingTransactions();
        if (from.isHidden()) {
            Log.e(TAG, from.getClass().getSimpleName() + " is hidden, " + "the transaction of startWithPop() is invalid!");
            return;
        }

        SupportFragment preFragment = getPreFragment(from);
        handlePopAnim(preFragment, from, to);

        FragmentTransaction removeFt = fragmentManager.beginTransaction().remove(from);
        supportCommit(fragmentManager, removeFt);
        debouncePop(from, fragmentManager);

        FragmentTransaction ft = fragmentManager.beginTransaction()
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                .add(from.getContainerId(), to, toFragmentTag)
                .addToBackStack(toFragmentTag);

        if (preFragment != null) {
            ft.hide(preFragment);
        }

        supportCommit(fragmentManager, ft);
        fragmentManager.executePendingTransactions();
    }

    private void supportCommit(FragmentManager fragmentManager, FragmentTransaction transaction) {
        boolean stateSaved = FragmentTransactionBugFixHack.isStateSaved(fragmentManager);
        if (stateSaved) {
            // 这里的警告请重视，请在Activity回来后，在onPostResume()中执行该事务
            Log.e(TAG, "Please beginTransaction in onPostResume() after the Activity returns!");
            IllegalStateException e = new IllegalStateException("Can not perform this action after onSaveInstanceState!");
            e.printStackTrace();
            mActivity.onExceptionAfterOnSaveInstanceState(e);
        }
        transaction.commitAllowingStateLoss();
    }

    /**
     * 获得栈顶SupportFragment
     */
    SupportFragment getTopFragment(FragmentManager fragmentManager) {
        fragmentManager = checkFragmentManager(fragmentManager, null);
        if (fragmentManager == null) return null;
        List<Fragment> fragmentList = fragmentManager.getFragments();
        if (fragmentList == null) return null;

        for (int i = fragmentList.size() - 1; i >= 0; i--) {
            Fragment fragment = fragmentList.get(i);
            if (fragment instanceof SupportFragment) {
                return (SupportFragment) fragment;
            }
        }
        return null;
    }

    /**
     * 获取目标Fragment的前一个Fragment
     *
     * @param fragment 目标Fragment
     */
    SupportFragment getPreFragment(Fragment fragment) {
        FragmentManager fragmentManager = fragment.getFragmentManager();
        fragmentManager = checkFragmentManager(fragmentManager, null);
        if (fragmentManager == null) return null;

        List<Fragment> fragmentList = fragmentManager.getFragments();
        if (fragmentList == null) return null;

        int index = fragmentList.indexOf(fragment);
        for (int i = index - 1; i >= 0; i--) {
            Fragment preFragment = fragmentList.get(i);
            if (preFragment instanceof SupportFragment) {
                return (SupportFragment) preFragment;
            }
        }
        return null;
    }

    /**
     * find Fragment from FragmentStack
     */
    @SuppressWarnings("unchecked")
    <T extends SupportFragment> T findStackFragment(Class<T> fragmentClass, String toFragmentTag, FragmentManager fragmentManager) {
        fragmentManager = checkFragmentManager(fragmentManager, null);
        if (fragmentManager == null) return null;

        Fragment fragment = null;

        if (toFragmentTag == null) {
            // 如果是 查找Fragment时,则有可能是在FragmentPagerAdapter/FragmentStatePagerAdapter中,这种情况下,
            // 它们的Tag是以android:switcher开头,所以这里我们使用下面的方式
            List<Fragment> fragmentList = fragmentManager.getFragments();
            if (fragmentList == null) return null;

            int sizeChildFrgList = fragmentList.size();

            for (int i = sizeChildFrgList - 1; i >= 0; i--) {
                Fragment brotherFragment = fragmentList.get(i);
                if (brotherFragment instanceof SupportFragment && brotherFragment.getClass().getName().equals(fragmentClass.getName())) {
                    fragment = brotherFragment;
                    break;
                }
            }
        } else {
            fragment = fragmentManager.findFragmentByTag(toFragmentTag);
        }

        if (fragment == null) {
            return null;
        }
        return (T) fragment;
    }

    /**
     * 从栈顶开始查找,状态为show & userVisible的Fragment
     */
    SupportFragment getActiveFragment(SupportFragment parentFragment, FragmentManager fragmentManager) {
        List<Fragment> fragmentList = fragmentManager.getFragments();
        if (fragmentList == null) {
            return parentFragment;
        }
        for (int i = fragmentList.size() - 1; i >= 0; i--) {
            Fragment fragment = fragmentList.get(i);
            if (fragment instanceof SupportFragment) {
                SupportFragment supportFragment = (SupportFragment) fragment;
                if (supportFragment.isResumed() && !supportFragment.isHidden() && supportFragment.getUserVisibleHint()) {
                    return getActiveFragment(supportFragment, supportFragment.getChildFragmentManager());
                }
            }
        }
        return parentFragment;
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
    private boolean handleLaunchMode(FragmentManager fragmentManager, SupportFragment toFragment, String toFragmentTag, int launchMode) {
        SupportFragment topFragment = getTopFragment(fragmentManager);
        if (topFragment == null) return false;
        Fragment stackToFragment = findStackFragment(toFragment.getClass(), toFragmentTag, fragmentManager);
        if (stackToFragment == null) return false;

        if (launchMode == SupportFragment.SINGLETOP) {
            // 在栈顶
            if (toFragment == topFragment || toFragment.getClass().getName().equals(topFragment.getClass().getName())) {
                handleNewBundle(toFragment, stackToFragment);
                return true;
            }
        } else if (launchMode == SupportFragment.SINGLETASK) {
            popToFix(toFragmentTag, 0, fragmentManager);
            handleNewBundle(toFragment, stackToFragment);
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

    void back(FragmentManager fragmentManager) {
        fragmentManager = checkFragmentManager(fragmentManager, null);
        if (fragmentManager == null) return;

        int count = fragmentManager.getBackStackEntryCount();
        if (count > 0) {
            debouncePop(null, fragmentManager);
        }
    }

    private void debouncePop(SupportFragment from, FragmentManager fragmentManager) {
        // Debounce
        if (from == null) {
            from = getTopFragment(fragmentManager);
        }
        long now = System.currentTimeMillis();
        if (now < mShareElementDebounceTime) return;
        mShareElementDebounceTime = System.currentTimeMillis() + from.getExitAnimDuration();

        fragmentManager.popBackStackImmediate();
    }

    void handleResultRecord(Fragment from) {
        SupportFragment preFragment = getPreFragment(from);
        if (preFragment == null) return;

        Bundle args = from.getArguments();
        if (args == null || !args.containsKey(FRAGMENTATION_ARG_RESULT_RECORD)) return;

        ResultRecord resultRecord = args.getParcelable(FRAGMENTATION_ARG_RESULT_RECORD);
        if (resultRecord == null) return;

        preFragment.onFragmentResult(resultRecord.requestCode, resultRecord.resultCode, resultRecord.resultBundle);
    }

    /**
     * 出栈到目标fragment
     *
     * @param fragmentTag tag
     * @param includeSelf 是否包含该fragment
     */
    void popTo(String fragmentTag, boolean includeSelf, Runnable afterPopTransactionRunnable, FragmentManager fragmentManager) {
        fragmentManager = checkFragmentManager(fragmentManager, null);
        if (fragmentManager == null) return;

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

        if (afterPopTransactionRunnable != null) {
            if (targetFragment != fromFragment) {
                handlePopAnim(targetFragment, fromFragment, null);
            }
            popToFix(fragmentTag, flag, fragmentManager);

            final FragmentManager finalFragmentManager = fragmentManager;
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    mPopToTempFragmentManager = finalFragmentManager;
                }
            });
            mHandler.post(afterPopTransactionRunnable);
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    mPopToTempFragmentManager = null;
                }
            });
        } else {
            popToFix(fragmentTag, flag, fragmentManager);
        }
    }

    /**
     * 解决popTo多个fragment时动画引起的异常问题
     */
    private void popToFix(String fragmentTag, int flag, final FragmentManager fragmentManager) {
        if (fragmentManager.getFragments() == null) return;

        mActivity.preparePopMultiple();
        fragmentManager.popBackStackImmediate(fragmentTag, flag);
        mActivity.popFinish();

        mHandler.post(new Runnable() {
            @Override
            public void run() {
                FragmentTransactionBugFixHack.reorderIndices(fragmentManager);
            }
        });
    }

    /**
     * hack startWithPop/popTo anim
     */
    private void handlePopAnim(Fragment targetFragment, SupportFragment fromFragment, SupportFragment toFragment) {
        if (targetFragment == null) return;

        View view = targetFragment.getView();
        if (view == null || !(view instanceof ViewGroup)) return;
        ViewGroup viewGroup = (ViewGroup) view;

        final View fromView = fromFragment.getView();
        if (fromView == null) return;

        SupportFragment preFragment = null;
        ViewGroup preViewGroup = null;

        // 在5.0之前的设备, popTo(Class<?> fragmentClass, boolean includeSelf, Runnable afterPopTransactionRunnable)
        // 在出栈多个Fragment并随后立即执行start操作时,会出现一瞬间的闪屏. 下面的代码为解决该问题
        if (toFragment == null && Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            preFragment = getPreFragment(fromFragment);
            if (preFragment != null && preFragment != targetFragment) {
                View preView = preFragment.getView();
                if (preView != null && preView instanceof ViewGroup) {
                    preViewGroup = (ViewGroup) preView;
                }
            }
        }

        // 不调用 会闪屏
        view.setVisibility(View.VISIBLE);

        try {
            ViewGroup container = (ViewGroup) mActivity.findViewById(fromFragment.getContainerId());
            if (container != null) {
                container.removeView(fromView);
                if (fromFragment.getSaveInstanceState() != null && toFragment != null) {
                    viewGroup = container;
                }

                if (fromView.getLayoutParams().height != ViewGroup.LayoutParams.MATCH_PARENT) {
                    fromView.getLayoutParams().height = ViewGroup.LayoutParams.MATCH_PARENT;
                }

                if (preViewGroup != null) {
                    showHideChildView(preViewGroup, false);
                    preViewGroup.addView(fromView);

                    final ViewGroup finalPreViewGroup = preViewGroup;
                    final ViewGroup finalViewGroup = viewGroup;
                    preFragment.setOnFragmentDestoryViewListener(new OnFragmentDestoryViewListener() {
                        @Override
                        public void onDestoryView() {
                            finalPreViewGroup.removeView(fromView);

                            showHideChildView(finalViewGroup, false);
                            finalViewGroup.addView(fromView);
                            restoreView(finalViewGroup, fromView, BUFFER_TIME);
                        }
                    });
                } else {
                    showHideChildView(viewGroup, false);
                    viewGroup.addView(fromView);
                    restoreView(viewGroup, fromView, toFragment == null ? BUFFER_TIME : Math.max(fromFragment.getEnterAnimDuration(), fromFragment.getPopExitAnimDuration()) + BUFFER_TIME);
                }
            }
        } catch (Exception ignored) {
        }
    }

    private void restoreView(final ViewGroup viewGroup, final View fromView, long delay) {
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                viewGroup.removeView(fromView);
                showHideChildView(viewGroup, true);
            }
        }, delay);
    }

    private void showHideChildView(ViewGroup viewGroup, boolean show) {
        for (int i = 0; i < viewGroup.getChildCount(); i++) {
            View child = viewGroup.getChildAt(i);
            child.setVisibility(show ? View.VISIBLE : View.GONE);
        }
    }

    static <T> T checkNotNull(T value, String message) {
        if (value == null) {
            throw new NullPointerException(message);
        }
        return value;
    }

    private FragmentManager checkFragmentManager(FragmentManager fragmentManager, Fragment from) {
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

    /**
     * 调试相关:以dialog形式 显示 栈视图
     */
    void showFragmentStackHierarchyView() {
        DebugHierarchyViewContainer container = new DebugHierarchyViewContainer(mActivity);
        container.bindFragmentRecords(getFragmentRecords());
        container.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        new AlertDialog.Builder(mActivity)
                .setTitle("栈视图")
                .setView(container)
                .setPositiveButton("关闭", null)
                .setCancelable(true)
                .show();
    }

    /**
     * 调试相关:以log形式 打印 栈视图
     */
    void logFragmentRecords(String tag) {
        List<DebugFragmentRecord> fragmentRecordList = getFragmentRecords();
        if (fragmentRecordList == null) return;

        StringBuilder sb = new StringBuilder();

        for (int i = fragmentRecordList.size() - 1; i >= 0; i--) {
            DebugFragmentRecord fragmentRecord = fragmentRecordList.get(i);

            if (i == fragmentRecordList.size() - 1) {
                sb.append("═══════════════════════════════════════════════════════════════════════════════════\n");
                if (i == 0) {
                    sb.append("\t栈顶\t\t\t").append(fragmentRecord.fragmentName).append("\n");
                    sb.append("═══════════════════════════════════════════════════════════════════════════════════");
                } else {
                    sb.append("\t栈顶\t\t\t").append(fragmentRecord.fragmentName).append("\n\n");
                }
            } else if (i == 0) {
                sb.append("\t栈底\t\t\t").append(fragmentRecord.fragmentName).append("\n\n");
                processChildLog(fragmentRecord.childFragmentRecord, sb, 1);
                sb.append("═══════════════════════════════════════════════════════════════════════════════════");
                Log.i(tag, sb.toString());
                return;
            } else {
                sb.append("\t↓\t\t\t").append(fragmentRecord.fragmentName).append("\n\n");
            }

            processChildLog(fragmentRecord.childFragmentRecord, sb, 1);
        }
    }

    private List<DebugFragmentRecord> getFragmentRecords() {
        List<DebugFragmentRecord> fragmentRecordList = new ArrayList<>();

        List<Fragment> fragmentList = mActivity.getSupportFragmentManager().getFragments();

        if (fragmentList == null || fragmentList.size() < 1) return null;

        for (Fragment fragment : fragmentList) {
            if (fragment == null) continue;
            fragmentRecordList.add(new DebugFragmentRecord(fragment.getClass().getSimpleName(), getChildFragmentRecords(fragment)));
        }
        return fragmentRecordList;
    }

    private void processChildLog(List<DebugFragmentRecord> fragmentRecordList, StringBuilder sb, int childHierarchy) {
        if (fragmentRecordList == null || fragmentRecordList.size() == 0) return;

        for (int j = 0; j < fragmentRecordList.size(); j++) {
            DebugFragmentRecord childFragmentRecord = fragmentRecordList.get(j);
            for (int k = 0; k < childHierarchy; k++) {
                sb.append("\t\t\t");
            }
            if (j == 0) {
                sb.append("\t子栈顶\t\t").append(childFragmentRecord.fragmentName).append("\n\n");
            } else if (j == fragmentRecordList.size() - 1) {
                sb.append("\t子栈底\t\t").append(childFragmentRecord.fragmentName).append("\n\n");
                processChildLog(childFragmentRecord.childFragmentRecord, sb, ++childHierarchy);
                return;
            } else {
                sb.append("\t↓\t\t\t").append(childFragmentRecord.fragmentName).append("\n\n");
            }

            processChildLog(childFragmentRecord.childFragmentRecord, sb, childHierarchy);
        }
    }

    private List<DebugFragmentRecord> getChildFragmentRecords(Fragment parentFragment) {
        List<DebugFragmentRecord> fragmentRecords = new ArrayList<>();

        List<Fragment> fragmentList = parentFragment.getChildFragmentManager().getFragments();
        if (fragmentList == null || fragmentList.size() < 1) return null;


        for (int i = fragmentList.size() - 1; i >= 0; i--) {
            Fragment fragment = fragmentList.get(i);
            if (fragment != null) {
                fragmentRecords.add(new DebugFragmentRecord(fragment.getClass().getSimpleName(), getChildFragmentRecords(fragment)));
            }
        }
        return fragmentRecords;
    }
}
