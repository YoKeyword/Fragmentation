package me.yokeyword.fragmentation;


import android.view.View;

/**
 * Created by YoKeyword on 16/6/2.
 */
interface ISupportFragment extends ISupport {

    /**
     * add目标Fragment, 以addSharedElement方式
     *
     * @param toFragment    TargetFragment
     * @param sharedElement A View in a disappearing Fragment to match with a View in an
     *                      appearing Fragment.
     * @param sharedName    The transitionName for a View in an appearing Fragment to match to the shared
     *                      element.
     */
    void startWithSharedElement(SupportFragment toFragment, View sharedElement, String sharedName);

    /**
     * 同上, startForResult版本的addSharedElement
     */
    void startForResultWithSharedElement(SupportFragment toFragment, int requestCode, View sharedElement, String sharedName);

    /**
     * replace目标Fragment, 主要用于Fragment之间的replace
     *
     * @param toFragment 目标Fragment
     * @param addToBack  是否添加到回退栈
     */
    void replaceFragment(SupportFragment toFragment, boolean addToBack);

    /**
     * @return 位于栈顶的子Fragment
     */
    SupportFragment getTopChildFragment();

    /**
     * @return 当前Fragment的前一个Fragment
     */
    SupportFragment getPreFragment();

    /**
     * @param fragmentClass 目标子Fragment的Class
     * @param <T>           继承自SupportFragment的Fragment
     * @return 目标子Fragment
     */
    <T extends SupportFragment> T findChildFragment(Class<T> fragmentClass);

    <T extends SupportFragment> T findChildFragment(String fragmentTag);

    /**
     * 子栈内 出栈
     */
    void popChild();

    /**
     * 子栈内 出栈到目标Fragment
     *
     * @param fragmentClass 目标Fragment的Class
     * @param includeSelf   是否包含目标Fragment
     */
    void popToChild(Class<?> fragmentClass, boolean includeSelf);

    void popToChild(String fragmentTag, boolean includeSelf);

    /**
     * 子栈内 出栈到目标Fragment,并在出栈后立即进行Fragment事务(可以防止出栈后,直接进行Fragment事务的异常)
     *
     * @param fragmentClass               目标Fragment的Class
     * @param includeSelf                 是否包含目标Fragment
     * @param afterPopTransactionRunnable 出栈后紧接着的Fragment事务
     */
    void popToChild(Class<?> fragmentClass, boolean includeSelf, Runnable afterPopTransactionRunnable);

    void popToChild(String fragmentTag, boolean includeSelf, Runnable afterPopTransactionRunnable);
}
