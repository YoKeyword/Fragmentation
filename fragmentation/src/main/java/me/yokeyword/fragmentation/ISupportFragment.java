package me.yokeyword.fragmentation;


/**
 * Created by YoKeyword on 16/6/2.
 */
public interface ISupportFragment extends ISupport {

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
}
