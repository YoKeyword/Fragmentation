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
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import me.yokeyword.fragmentation.helper.FragmentRecord;
import me.yokeyword.fragmentation.helper.OnAnimEndListener;


/**
 * Controller
 * Created by YoKeyword on 16/1/22.
 */
public class Fragmentation {
    static final String ARG_REQUEST_CODE = "fragmentation_arg_request_code";
    static final String ARG_RESULT_CODE = "fragmentation_arg_result_code";
    static final String ARG_RESULT_BUNDLE = "fragmentation_arg_bundle";
    static final String ARG_IS_ROOT = "fragmentation_arg_is_root";

    public static final long BUFFER_TIME = 200L;

    public static final int TYPE_ADD = 0;
    public static final int TYPE_ADD_FINISH = 1;

    public static final long CLICK_DEBOUNCE_TIME = 300L;
    private long mCurrentTime;

    private SupportActivity mActivity;
    private FragmentManager mFragmentManager;
    private int mContainerId;

    private Handler mHandler;

    public Fragmentation(SupportActivity activity, int containerId) {
        this.mActivity = activity;
        this.mContainerId = containerId;
        this.mFragmentManager = activity.getSupportFragmentManager();

        mHandler = mActivity.getHandler();
    }

    /**
     * 分发事务
     *
     * @param from
     * @param to
     * @param requestCode
     * @param launchMode
     * @param type
     */
    void dispatchStartTransaction(SupportFragment from, SupportFragment to, int requestCode,
                                  int launchMode, int type) {
        if (System.currentTimeMillis() - mCurrentTime < CLICK_DEBOUNCE_TIME) {
            return;
        }
        mCurrentTime = System.currentTimeMillis();

        if (from != null) {
            mFragmentManager = from.getFragmentManager();
        }

        // 移动到popTo 后
//        FragmentTransactionBugFixHack.reorderIndices(mFragmentManager);

        if (type == TYPE_ADD) {
            saveRequestCode(to, requestCode);
        }

        if (handleLaunchMode(to, launchMode)) return;

        switch (type) {
            case TYPE_ADD:
                start(from, to);
                break;
            case TYPE_ADD_FINISH:
                if (from != null) {
                    startWithFinish(from, to);
                } else {
                    throw new RuntimeException("startWithFinish(): getTopFragment() is null");
                }
                break;
        }
    }

    void start(SupportFragment from, SupportFragment to) {
        String toName = to.getClass().getName();
        FragmentTransaction ft = mFragmentManager.beginTransaction()
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                .add(mContainerId, to, toName);

        if (from != null) {
            ft.hide(from);
        } else {
            Bundle bundle = to.getArguments();
            bundle.putBoolean(ARG_IS_ROOT, true);
        }

        ft.addToBackStack(toName);
        ft.commit();
    }

    void startWithFinish(SupportFragment from, SupportFragment to) {
        Fragment preFragment = handlerFinish(from, to);
        passSaveResult(from, to);

        mFragmentManager.beginTransaction().remove(from).commit();
        mFragmentManager.popBackStack();

        String toName = to.getClass().getName();
        FragmentTransaction ft = mFragmentManager.beginTransaction()
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                .add(mContainerId, to, toName)
                .addToBackStack(toName);

        if (preFragment != null) {
            ft.hide(preFragment);
        }
        ft.commit();
    }

    /**
     * pass on Result
     *
     * @param from
     * @param to
     */
    private void passSaveResult(SupportFragment from, SupportFragment to) {
        saveRequestCode(to, from.getRequestCode());
        Bundle bundle = to.getArguments();
        bundle.putInt(ARG_RESULT_CODE, from.getResultCode());
        bundle.putBundle(ARG_RESULT_BUNDLE, from.getResultBundle());
    }

    /**
     * fix anim
     */
    @Nullable
    private Fragment handlerFinish(SupportFragment from, SupportFragment to) {
        Fragment preFragment = getPreFragment(from);
        if (preFragment != null) {
            View view = preFragment.getView();
            if (view != null) {
                // 不调用 会闪屏
                view.setVisibility(View.VISIBLE);

                ViewGroup viewGroup;
                final View fromView = from.getView();

                if (fromView != null) {
                    if (view instanceof ViewGroup) {
                        viewGroup = (ViewGroup) view;
                        ViewGroup container = (ViewGroup) mActivity.findViewById(mContainerId);
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
                            to.setNeedAnimListener(true, new OnAnimEndListener() {
                                @Override
                                public void onAnimationEnd() {
                                    finalViewGroup.removeView(fromView);
                                }
                            });
                        }
                    }
                }
            }
        }
        return preFragment;
    }

    /**
     * @param fragment
     * @return
     */
    private Fragment getPreFragment(Fragment fragment) {
        List<Fragment> fragmentList = mFragmentManager.getFragments();
        if (fragmentList == null) return null;

        int index = fragmentList.indexOf(fragment);
        for (int i = index - 1; i >= 0; i--) {
            Fragment preFragment = fragmentList.get(i);
            if (preFragment instanceof SupportFragment) {
                return preFragment;
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
                if (childFragment != null && childFragment.getClass().getName().equals(fragmentClass.getName())) {
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
     * handle LaunchMode
     *
     * @param to
     * @param launchMode
     * @return
     */
    private boolean handleLaunchMode(Fragment to, int launchMode) {
        Fragment fragment = mFragmentManager.findFragmentByTag(to.getClass().getName());

        if (fragment != null) {
            if (launchMode == SupportFragment.SINGLETOP) {
                List<Fragment> fragments = mFragmentManager.getFragments();
                int index = fragments.indexOf(fragment);
                // 在栈顶
                if (index == mFragmentManager.getBackStackEntryCount() - 1) {
                    if (handleNewBundle(to, fragment)) return true;
                }
            } else if (launchMode == SupportFragment.SINGLETASK) {
                popBackFix(to.getClass(), 0, mFragmentManager);
                if (handleNewBundle(to, fragment)) return true;
            }
        }
        return false;
    }

    private boolean handleNewBundle(Fragment to, Fragment fragment) {
        if (fragment instanceof SupportFragment) {
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
        bundle.putInt(ARG_REQUEST_CODE, requestCode);
    }

    void back(FragmentManager fragmentManager) {
        int count = fragmentManager.getBackStackEntryCount();

        if (count > 1) {
            handleBack(fragmentManager);
        }
    }

    /**
     * handle result
     */
    private void handleBack(final FragmentManager fragmentManager) {
        List<Fragment> fragmentList = fragmentManager.getFragments();
        int count = 0;
        int requestCode = 0, resultCode = 0;
        long lastAnimTime = 0;
        Bundle data = null;

        for (int i = fragmentList.size() - 1; i >= 0; i--) {
            Fragment fragment = fragmentList.get(i);
            if (fragment instanceof SupportFragment) {
                final SupportFragment supportFragment = (SupportFragment) fragment;
                if (count == 0) {
                    requestCode = supportFragment.getRequestCode();
                    resultCode = supportFragment.getResultCode();
                    data = supportFragment.getResultBundle();

                    lastAnimTime = supportFragment.getExitAnimDuration();

                    count++;
                } else {

                    if (requestCode != 0 && resultCode != 0) {
                        final int finalRequestCode = requestCode;
                        final int finalResultCode = resultCode;
                        final Bundle finalData = data;

                        long animTime = supportFragment.getPopEnterAnimDuration();

                        fragmentManager.popBackStackImmediate();

                        mHandler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                supportFragment.onFragmentResult(finalRequestCode, finalResultCode, finalData);
                            }
                        }, Math.max(animTime, lastAnimTime));
                        return;
                    }
                    break;
                }
            }
        }

        fragmentManager.popBackStackImmediate();
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
     * fix popTo anim
     */
    @Nullable
    private void fixPopToAnim(Fragment rootFragment, SupportFragment fromFragment) {
        if (rootFragment != null) {
            View view = rootFragment.getView();
            if (view != null) {
                // 不调用 会闪屏
                view.setVisibility(View.VISIBLE);

                ViewGroup viewGroup;
                final View fromView = fromFragment.getView();

                if (fromView != null) {
                    if (view instanceof ViewGroup) {
                        viewGroup = (ViewGroup) view;
                        ViewGroup container = (ViewGroup) mActivity.findViewById(mContainerId);
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
                            }, BUFFER_TIME);
                        }
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
        try {
            Fragment rootFragment = fragmentManager.findFragmentByTag(fragmentClass.getName());
            if (includeSelf) {
                rootFragment = getPreFragment(rootFragment);
            }
            SupportFragment fromFragment = getTopFragment(fragmentManager);

            if (rootFragment == fromFragment && afterPopTransactionRunnable != null) {
                mHandler.post(afterPopTransactionRunnable);
                return;
            }

            fixPopToAnim(rootFragment, fromFragment);
            fragmentManager.beginTransaction().remove(fromFragment).commit();

            int flag = includeSelf ? FragmentManager.POP_BACK_STACK_INCLUSIVE : 0;
            popBackFix(fragmentClass, flag, fragmentManager);
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(mActivity, "Exception", Toast.LENGTH_SHORT).show();
        }

        if (afterPopTransactionRunnable != null) {
            mHandler.post(afterPopTransactionRunnable);
        }
    }

    /**
     * 解决pop多个fragment异常问题
     *
     * @param fragmentClass
     * @param flag
     */
    private void popBackFix(Class<?> fragmentClass, int flag, final FragmentManager fragmentManager) {
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

    List<FragmentRecord> getFragmentRecords() {
        List<FragmentRecord> fragmentRecords = new ArrayList<>();

        List<Fragment> fragmentList = mActivity.getSupportFragmentManager().getFragments();
        if (fragmentList == null || fragmentList.size() < 1) return null;

        for (Fragment fragment : fragmentList) {
            if (fragment == null) continue;
            fragmentRecords.add(new FragmentRecord(fragment.getClass().getSimpleName(), getChildFragmentRecords(fragment)));
        }

        return fragmentRecords;
    }

    private List<FragmentRecord> getChildFragmentRecords(Fragment parentFragment) {
        List<FragmentRecord> fragmentRecords = new ArrayList<>();

        List<Fragment> fragmentList = parentFragment.getChildFragmentManager().getFragments();
        if (fragmentList == null || fragmentList.size() < 1) return null;


        for (int i = fragmentList.size() - 1; i >= 0; i--) {
            Fragment fragment = fragmentList.get(i);
            if (fragment != null) {
                fragmentRecords.add(new FragmentRecord(fragment.getClass().getSimpleName(), getChildFragmentRecords(fragment)));
            }
        }
        return fragmentRecords;
    }
}
