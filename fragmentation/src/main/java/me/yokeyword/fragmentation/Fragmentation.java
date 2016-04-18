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

import java.util.List;

import me.yokeyword.fragmentation.helper.OnAnimEndListener;


/**
 * Fragment Manager
 * Created by YoKeyword on 16/1/22.
 */
public class Fragmentation {
    static final String ARG_REQUEST_CODE = "yokeyword_arg_request_code";
    static final String ARG_RESULT_CODE = "yokeyword_arg_result_code";
    static final String ARG_RESULT_BUNDLE = "yokeyword_arg_bundle";
    static final String ARG_IS_ROOT = "yokeyword_arg_is_root";


    public static final int TYPE_ADD = 0;
    public static final int TYPE_ADD_FINISH = 1;

    private static final int CLICK_SPACE_TIME = 400;
    private long mCurrentTime;

    private SupportActivity mActivity;
    private FragmentManager mFragmentManager;
    private int mContainerId;

    private Handler mHandler;

    public Fragmentation(SupportActivity activity, int containerId) {
        this.mActivity = activity;
        this.mContainerId = containerId;
        this.mFragmentManager = activity.getSupportFragmentManager();

        mHandler = new Handler();
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
    void dispatchTransaction(SupportFragment from, SupportFragment to, int requestCode,
                             int launchMode, int type) {
        if (System.currentTimeMillis() - mCurrentTime < CLICK_SPACE_TIME) {
            return;
        }
        mCurrentTime = System.currentTimeMillis();

        if (from != null) {
            mFragmentManager = from.getFragmentManager();
        }

        FragmentTransactionBugFixHack.reorderIndices(mFragmentManager);

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
                .add(mContainerId, to, toName)
                .show(to);

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
                .show(to)
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
        Fragment preFragment = null;
        if (fragmentList != null) {
            int index = fragmentList.indexOf(fragment);
            for (int i = index - 1; i >= 0; i--) {
                preFragment = fragmentList.get(i);
                if (preFragment != null) {
                    break;
                }
            }
        }
        return preFragment;
    }

    @SuppressWarnings("unchecked")
    <T extends SupportFragment> T findStackFragment(Class<T> fragmentClass, FragmentManager fragmentManager) {
        Fragment fragment = fragmentManager.findFragmentByTag(fragmentClass.getName());
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
    private void handleBack(FragmentManager fragmentManager) {
        fragmentManager.popBackStack();

        List<Fragment> fragmentList = fragmentManager.getFragments();
        int count = 0;
        int requestCode = 0, resultCode = 0;
        Bundle data = null;

        for (int i = fragmentList.size() - 1; i >= 0; i--) {
            Fragment fragment = fragmentList.get(i);
            if (fragment != null && fragment instanceof SupportFragment) {
                SupportFragment supportFragment = (SupportFragment) fragment;
                if (count == 0) {
                    requestCode = supportFragment.getRequestCode();
                    resultCode = supportFragment.getResultCode();
                    data = supportFragment.getResultBundle();

                    count++;
                } else {
                    if (requestCode != 0) {
                        supportFragment.onFragmentResult(requestCode, resultCode, data);
                    }
                    // 解决在app因资源问题被回收后 重新进入app 在Fragment嵌套时,返回到嵌套的Fragment时,导致的错误问题
                    if (supportFragment.getChildFragmentManager().getFragments() != null) {
                        fragmentManager.beginTransaction().show(supportFragment).commit();
                    }
                    break;
                }
            }
        }
    }

    /**
     * 获得栈顶Fragment
     *
     * @return
     */
    SupportFragment getTopFragment(FragmentManager fragmentManager) {
        List<Fragment> fragmentList = fragmentManager.getFragments();
        if (fragmentList != null) {
            for (int i = fragmentList.size() - 1; i >= 0; i--) {
                Fragment fragment = fragmentList.get(i);
                if (fragment != null && fragment instanceof SupportFragment) {
                    return (SupportFragment) fragment;
                }
            }
        }
        return null;
    }

    /**
     * fix popTp anim
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
                            }, 200);
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
    private void popBackFix(Class<?> fragmentClass, int flag, FragmentManager fragmentManager) {
        mActivity.preparePopMultiple();
        fragmentManager.popBackStackImmediate(fragmentClass.getName(), flag);
        mActivity.popFinish();
    }
}
