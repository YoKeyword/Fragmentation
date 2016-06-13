package me.yokeyword.fragmentation;

import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.FragmentTransactionBugFixHack;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import java.util.ArrayList;
import java.util.List;

import me.yokeyword.fragmentation.debug.DebugFragmentRecord;
import me.yokeyword.fragmentation.helper.FragmentResultRecord;
import me.yokeyword.fragmentation.helper.OnEnterAnimEndListener;


/**
 * Controller
 * Created by YoKeyword on 16/1/22.
 */
public class Fragmentation {
    static final String TAG = Fragmentation.class.getSimpleName();

    //    static final String ARG_REQUEST_CODE = "fragmentation_arg_request_code";
//    static final String ARG_RESULT_CODE = "fragmentation_arg_result_code";
//    static final String ARG_RESULT_BUNDLE = "fragmentation_arg_bundle";
    static final String ARG_RESULT_RECORD = "fragment_arg_result_record";

    static final String ARG_IS_ROOT = "fragmentation_arg_is_root";
    static final String ARG_IS_SHARED_ELEMENT = "fragmentation_arg_is_shared_element";
    static final String FRAGMENTATION_ARG_CONTAINER = "fragmentation_arg_container";

    static final String FRAGMENTATION_STATE_SAVE_ANIMATOR = "fragmentation_state_save_animator";
    static final String FRAGMENTATION_STATE_SAVE_IS_HIDDEN = "fragmentation_state_save_status";

    public static final long BUFFER_TIME = 300L;

    public static final int TYPE_ADD = 0;
    public static final int TYPE_ADD_WITH_POP = 1;
    public static final int TYPE_ADD_RESULT = 2;

    private SupportActivity mActivity;

    private Handler mHandler;

    public Fragmentation(SupportActivity activity) {
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
        dispatchStartTransaction(fragmentManager, null, to, 0, SupportFragment.STANDARD, TYPE_ADD, null, null);
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
            bundle.putBoolean(ARG_IS_ROOT, true);
        }

        ft.commit();
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
    void dispatchStartTransaction(FragmentManager fragmentManager, SupportFragment from, SupportFragment to, int requestCode, int launchMode, int type, View sharedElement, String name) {
        // 这里发现使用addSharedElement时,在被强杀重启时导致栈内顺序异常,这里进行一次hack顺序
        if (sharedElement != null) {
            FragmentTransactionBugFixHack.reorderIndices(fragmentManager);
        }

        if (type == TYPE_ADD_RESULT) {
            saveRequestCode(to, requestCode);
        }

        if (from != null) {
            bindContainerId(from.getContainerId(), to);
        }

        if (handleLaunchMode(fragmentManager, to, launchMode)) return;

        // 在SingleTask/SingleTop启动模式之后 开启防抖动
        mActivity.setFragmentClickable(false);

        switch (type) {
            case TYPE_ADD:
            case TYPE_ADD_RESULT:
                start(fragmentManager, from, to, sharedElement, name);
                break;
            case TYPE_ADD_WITH_POP:
                if (from != null) {
                    startWithPop(fragmentManager, from, to);
                } else {
                    throw new RuntimeException("startWithPop(): getTopFragment() is null");
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
        bindContainerId(containerId, to);
        FragmentTransaction ft = fragmentManager.beginTransaction();
        ft.replace(containerId, to, to.getClass().getName());
        if (addToBack) {
            ft.addToBackStack(to.getClass().getName());
        }
        ft.commit();
    }

    /**
     * show一个Fragment,hide一个Fragment ; 主要用于类似微信主页那种 切换tab的情况
     *
     * @param showFragment 需要show的Fragment
     * @param hideFragment 需要hide的Fragment
     */
    void showHideFragment(FragmentManager fragmentManager, SupportFragment showFragment, SupportFragment hideFragment) {
        if (showFragment == hideFragment) return;

        // 如果show和hide的Fragment不是同一个
        fragmentManager.beginTransaction()
                .show(showFragment)
                .hide(hideFragment)
                .commit();
    }

    void start(FragmentManager fragmentManager, SupportFragment from, SupportFragment to, View sharedElement, String name) {
        String toName = to.getClass().getName();
        FragmentTransaction ft = fragmentManager.beginTransaction();

        if (sharedElement == null) {
            ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        } else {
            Bundle bundle = to.getArguments();
            bundle.putBoolean(ARG_IS_SHARED_ELEMENT, true);
            ft.addSharedElement(sharedElement, name);
        }
        if (from == null) {
            ft.add(to.getArguments().getInt(FRAGMENTATION_ARG_CONTAINER), to, toName);

            Bundle bundle = to.getArguments();
            bundle.putBoolean(ARG_IS_ROOT, true);
        } else {
            ft.add(from.getContainerId(), to, toName);
            ft.hide(from);
        }

        ft.addToBackStack(toName);
        ft.commit();
    }

    void startWithPop(FragmentManager fragmentManager, SupportFragment from, SupportFragment to) {
        SupportFragment preFragment = getPreFragment(from);
        if (preFragment != null) {
            hackFinishAnim(preFragment, from, to);
        }

        fragmentManager.beginTransaction().remove(from).commit();
        handleBack(fragmentManager, true);

        String toName = to.getClass().getName();
        FragmentTransaction ft = fragmentManager.beginTransaction()
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                .add(from.getContainerId(), to, toName)
                .addToBackStack(toName);

        if (preFragment != null) {
            ft.hide(preFragment);
        }
        ft.commit();
    }

    /**
     * 获得栈顶SupportFragment
     *
     * @return
     */
    SupportFragment getTopFragment(FragmentManager fragmentManager) {
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
     * @return
     */
    SupportFragment getPreFragment(Fragment fragment) {
        List<Fragment> fragmentList = fragment.getFragmentManager().getFragments();
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
    <T extends SupportFragment> T findStackFragment(Class<T> fragmentClass, FragmentManager fragmentManager, boolean isChild) {
        Fragment fragment = null;
        if (isChild) {
            // 如果是 查找子Fragment,则有可能是在FragmentPagerAdapter/FragmentStatePagerAdapter中,这种情况下,
            // 它们的Tag是以android:switcher开头,所以这里我们使用下面的方式
            List<Fragment> childFragmentList = fragmentManager.getFragments();
            if (childFragmentList == null) return null;

            for (int i = childFragmentList.size() - 1; i >= 0; i--) {
                Fragment childFragment = childFragmentList.get(i);
                if (childFragment instanceof SupportFragment && childFragment.getClass().getName().equals(fragmentClass.getName())) {
                    fragment = childFragment;
                    break;
                }
            }
        } else {
            fragment = fragmentManager.findFragmentByTag(fragmentClass.getName());
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
                if (!supportFragment.isHidden() && supportFragment.getUserVisibleHint()) {
                    return getActiveFragment(supportFragment, supportFragment.getChildFragmentManager());
                }
            }
        }
        return parentFragment;
    }

    /**
     * handle LaunchMode
     *
     * @param fragmentManager
     * @param to
     * @param launchMode
     * @return
     */
    private boolean handleLaunchMode(FragmentManager fragmentManager, Fragment to, int launchMode) {

        if (launchMode == SupportFragment.SINGLETOP) {
            List<Fragment> fragments = fragmentManager.getFragments();
            int index = fragments.indexOf(to);
            // 在栈顶
            if (index == fragmentManager.getBackStackEntryCount() - 1) {
                if (handleNewBundle(to)) return true;
            }
        } else if (launchMode == SupportFragment.SINGLETASK) {
            popToFix(to, 0, fragmentManager);
            if (handleNewBundle(to)) return true;
        }
        return false;
    }

    private boolean handleNewBundle(Fragment to) {
        if (to instanceof SupportFragment) {
            SupportFragment supportTo = (SupportFragment) to;
            Bundle newBundle = supportTo.getNewBundle();
            supportTo.onNewBundle(newBundle);
            return true;
        }
        return false;
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
        FragmentResultRecord resultRecord = new FragmentResultRecord();
        resultRecord.requestCode = requestCode;
        bundle.putParcelable(ARG_RESULT_RECORD, resultRecord);
    }

    void back(FragmentManager fragmentManager) {
        int count = fragmentManager.getBackStackEntryCount();

        if (count > 1) {
            handleBack(fragmentManager, false);
        }
    }

    /**
     * handle result
     */
    private void handleBack(final FragmentManager fragmentManager, boolean fromStartWithPop) {
        List<Fragment> fragmentList = fragmentManager.getFragments();

        boolean flag = false;

        FragmentResultRecord fragmentResultRecord = null;
        long lastAnimTime = 0;

        for (int i = fragmentList.size() - 1; i >= 0; i--) {
            Fragment fragment = fragmentList.get(i);
            if (fragment instanceof SupportFragment) {
                final SupportFragment supportFragment = (SupportFragment) fragment;
                if (!flag) {
                    Bundle args = supportFragment.getArguments();
                    if (args == null || !args.containsKey(ARG_RESULT_RECORD)) break;
                    fragmentResultRecord = args.getParcelable(ARG_RESULT_RECORD);
                    if (fragmentResultRecord == null) break;

                    lastAnimTime = supportFragment.getExitAnimDuration();
                    flag = true;
                } else {
                    final FragmentResultRecord finalFragmentResultRecord = fragmentResultRecord;
                    long animTime = supportFragment.getPopEnterAnimDuration();

                    if (fromStartWithPop) {
                        fragmentManager.popBackStack();
                    } else {
                        fragmentManager.popBackStackImmediate();
                    }

                    mHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            supportFragment.onFragmentResult(finalFragmentResultRecord.requestCode, finalFragmentResultRecord.resultCode, finalFragmentResultRecord.resultBundle);
                        }
                    }, Math.max(animTime, lastAnimTime));
                    return;
                }
            }
        }

        if (fromStartWithPop) {
            fragmentManager.popBackStack();
        } else {
            fragmentManager.popBackStackImmediate();
        }
    }

    /**
     * hack anim
     */
    @Nullable
    private void hackFinishAnim(SupportFragment preFragment, SupportFragment from, SupportFragment to) {
        View view = preFragment.getView();
        if (view != null) {
            // 不调用 会闪屏
            view.setVisibility(View.VISIBLE);

            ViewGroup viewGroup;
            final View fromView = from.getView();

            if (fromView != null && view instanceof ViewGroup) {
                viewGroup = (ViewGroup) view;
                ViewGroup container = (ViewGroup) mActivity.findViewById(from.getContainerId());
                if (container != null) {
                    container.removeView(fromView);
                    if (fromView.getLayoutParams().height != ViewGroup.LayoutParams.MATCH_PARENT) {
                        fromView.getLayoutParams().height = ViewGroup.LayoutParams.MATCH_PARENT;
                    }

                    if (viewGroup instanceof LinearLayout) {
                        viewGroup.addView(fromView, 0);
                    } else {
                        viewGroup.addView(fromView);
                    }

                    final ViewGroup finalViewGroup = viewGroup;
                    to.setEnterAnimEndListener(new OnEnterAnimEndListener() {
                        @Override
                        public void onAnimationEnd() {
                            finalViewGroup.removeView(fromView);
                        }
                    });
                }
            }
        }
    }

    /**
     * hack popTo anim
     */
    @Nullable
    private void hacPopTokAnim(Fragment rootFragment, SupportFragment fromFragment) {
        if (rootFragment != null) {
            View view = rootFragment.getView();
            if (view != null) {
                // 不调用 会闪屏
                view.setVisibility(View.VISIBLE);

                ViewGroup viewGroup;
                final View fromView = fromFragment.getView();

                if (fromView != null && view instanceof ViewGroup) {
                    viewGroup = (ViewGroup) view;
                    ViewGroup container = (ViewGroup) mActivity.findViewById(fromFragment.getContainerId());
                    if (container != null) {
                        container.removeView(fromView);
                        if (fromView.getLayoutParams().height != ViewGroup.LayoutParams.MATCH_PARENT) {
                            fromView.getLayoutParams().height = ViewGroup.LayoutParams.MATCH_PARENT;
                        }

                        if (viewGroup instanceof LinearLayout) {
                            viewGroup.addView(fromView, 0);
                        } else {
                            viewGroup.addView(fromView);
                        }

                        final ViewGroup finalViewGroup = viewGroup;
                        mHandler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                finalViewGroup.removeView(fromView);
                            }
                        }, Math.max(fromFragment.getExitAnimDuration(), BUFFER_TIME));
                    }
                }
            }
        }
    }

    /**
     * 出栈到目标fragment
     *
     * @param fragmentClass 目标fragment
     * @param includeSelf   是否包含该fragment
     */
    void popTo(Class<?> fragmentClass, boolean includeSelf, Runnable afterPopTransactionRunnable, FragmentManager fragmentManager) {
        Fragment targetFragment = fragmentManager.findFragmentByTag(fragmentClass.getName());
        if (includeSelf) {
            targetFragment = getPreFragment(targetFragment);
            if (targetFragment == null) {
                throw new RuntimeException("Do you want to pop all Fragments? Please call _mActivity.finish()");
            }
        }
        SupportFragment fromFragment = getTopFragment(fragmentManager);

        int flag = includeSelf ? FragmentManager.POP_BACK_STACK_INCLUSIVE : 0;

        if (afterPopTransactionRunnable != null) {
            if (targetFragment == fromFragment) {
                mHandler.post(afterPopTransactionRunnable);
                return;
            }

            hacPopTokAnim(targetFragment, fromFragment);
            fragmentManager.beginTransaction().remove(fromFragment).commit();
            popToWithTransactionFix(fragmentClass, flag, fragmentManager);
            mHandler.post(afterPopTransactionRunnable);
        } else {
            popToFix(targetFragment, flag, fragmentManager);
        }
    }

    /**
     * 解决popTo多个fragment时动画引起的异常问题
     */
    private void popToWithTransactionFix(Class<?> fragmentClass, int flag, final FragmentManager fragmentManager) {
        mActivity.preparePopMultiple();
        fragmentManager.popBackStackImmediate(fragmentClass.getName(), flag);
        mActivity.popFinish();

        mHandler.post(new Runnable() {
            @Override
            public void run() {
                FragmentTransactionBugFixHack.reorderIndices(fragmentManager);
            }
        });
    }

    /**
     * 解决以singleTask或singleTop模式start时,pop多个fragment时动画引起的异常问题
     */
    private void popToFix(Fragment targetFragment, int flag, final FragmentManager fragmentManager) {
        if (fragmentManager.getFragments() == null) return;

        fragmentManager.popBackStackImmediate(targetFragment.getClass().getName(), flag);

        long popAniDuration;

        if (targetFragment instanceof SupportFragment) {
            SupportFragment fragment = (SupportFragment) targetFragment;
            popAniDuration = Math.max(fragment.getPopEnterAnimDuration(), fragment.getPopExitAnimDuration());
        } else {
            popAniDuration = BUFFER_TIME;
        }

        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                FragmentTransactionBugFixHack.reorderIndices(fragmentManager);
            }
        }, popAniDuration);
    }

    List<DebugFragmentRecord> getFragmentRecords() {
        List<DebugFragmentRecord> fragmentRecords = new ArrayList<>();

        List<Fragment> fragmentList = mActivity.getSupportFragmentManager().getFragments();
        if (fragmentList == null || fragmentList.size() < 1) return null;

        for (Fragment fragment : fragmentList) {
            if (fragment == null) continue;
            fragmentRecords.add(new DebugFragmentRecord(fragment.getClass().getSimpleName(), getChildFragmentRecords(fragment)));
        }

        return fragmentRecords;
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
