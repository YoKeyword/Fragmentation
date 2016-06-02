package me.yokeyword.fragmentation;


/**
 * Created by YoKeyword on 16/6/1.
 */
public interface ISupport {

    /**
     * 加载根Fragment, 即Activity内的第一个Fragment 或 Fragment内的第一个子Fragment
     *
     * @param containerId 容器id
     * @param toFragment  目标Fragment
     */
    void loadRootFragment(int containerId, SupportFragment toFragment);

    /**
     * 以replace方式加载根Fragment
     */
    void replaceLoadRootFragment(int containerId, SupportFragment toFragment, boolean addToBack);

    /**
     * 启动目标Fragment
     *
     * @param toFragment 目标Fragment
     */
    void start(SupportFragment toFragment);

    /**
     * @param toFragment 目标Fragment
     * @param launchMode 启动模式
     */
    void start(SupportFragment toFragment, @SupportFragment.LaunchMode int launchMode);

    /**
     * @param toFragment  目标Fragment
     * @param requestCode requsetCode
     */
    void startForResult(SupportFragment toFragment, int requestCode);

    /**
     * 启动目标Fragment,并pop当前Fragment
     *
     * @param toFragment 目标Fragment
     */
    void startWithPop(SupportFragment toFragment);

    /**
     * @return 栈顶Fragment
     */
    SupportFragment getTopFragment();

    /**
     * @param fragmentClass 目标Fragment的Class
     * @param <T>           继承自SupportFragment的Fragment
     * @return 目标Fragment
     */
    <T extends SupportFragment> T findFragment(Class<T> fragmentClass);

    /**
     * 出栈
     */
    void pop();

    /**
     * 出栈到目标Fragment
     *
     * @param fragmentClass 目标Fragment的Class
     * @param includeSelf   是否包含目标Fragment
     */
    void popTo(Class<?> fragmentClass, boolean includeSelf);

    /**
     * 出栈到目标Fragment,并在出栈后立即进行Fragment事务(可以防止出栈后,直接进行Fragment事务的异常)
     *
     * @param fragmentClass               目标Fragment的Class
     * @param includeSelf                 是否包含目标Fragment
     * @param afterPopTransactionRunnable 出栈后紧接着的Fragment事务
     */
    void popTo(Class<?> fragmentClass, boolean includeSelf, Runnable afterPopTransactionRunnable);
}
