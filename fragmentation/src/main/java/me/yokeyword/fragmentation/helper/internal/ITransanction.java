package me.yokeyword.fragmentation.helper.internal;

import me.yokeyword.fragmentation.ExtraTransaction;
import me.yokeyword.fragmentation.SupportFragment;

/**
 * Created by YoKey on 17/6/21.
 */

public interface ITransanction {
    ExtraTransaction extraTransaction();

    /**
     * 加载根Fragment, 即Activity内的第一个Fragment 或 Fragment内的第一个子Fragment
     *
     * @param containerId 容器id
     * @param toFragment  目标Fragment
     */
    void loadRootFragment(int containerId, SupportFragment toFragment);

    /**
     * @param addToBackStack 是否添加至回退栈
     * @param allowAnimation 是否显示入场动画
     */
    void loadRootFragment(int containerId, SupportFragment toFragment, boolean addToBackStack, boolean allowAnimation);

    /**
     * 加载多个同级根Fragment
     * 配合{@link #showHideFragment(SupportFragment, SupportFragment)}
     *
     * @param containerId 容器id
     * @param toFragments 目标Fragments
     */
    void loadMultipleRootFragment(int containerId, int showPosition, SupportFragment... toFragments);

    /**
     * show一个Fragment,hide其他同栈所有Fragment
     * 使用该方法时，要确保同级栈内无多余的Fragment,(只有通过loadMultipleRootFragment()载入的Fragment)
     * <p>
     * 建议使用更明确的{@link #showHideFragment(SupportFragment, SupportFragment)}
     *
     * 配合{@link #loadRootFragment(int, SupportFragment)}
     *
     * @param showFragment 需要show的Fragment
     */
    void showHideFragment(SupportFragment showFragment);

    /**
     * show一个Fragment,hide一个Fragment; 配合{@link #loadRootFragment(int, SupportFragment)}
     */
    void showHideFragment(SupportFragment showFragment, SupportFragment hideFragment);

    void start(SupportFragment toFragment);

    void start(SupportFragment toFragment, @SupportFragment.LaunchMode int launchMode);

    void startForResult(SupportFragment toFragment, int requestCode);

    void startWithPop(SupportFragment toFragment);

    void replaceFragment(SupportFragment toFragment, boolean addToBackStack);

    void pop();

    void popTo(Class<?> targetFragmentClass, boolean includeTargetFragment);

    void popTo(Class<?> targetFragmentClass, boolean includeTargetFragment, Runnable afterPopTransactionRunnable);

    void popTo(Class<?> targetFragmentClass, boolean includeTargetFragment, Runnable afterPopTransactionRunnable, int popAnim);
}
