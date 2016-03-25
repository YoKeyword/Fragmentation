package me.yokeyword.fragmentation;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

import me.yokeyword.fragmentation.anim.DefaultVerticalAnimator;
import me.yokeyword.fragmentation.anim.FragmentAnimator;
import me.yokeyword.fragmentation.helper.FragmentRecord;
import me.yokeyword.fragmentation.helper.HierarchyViewContainer;

/**
 * Created by YoKeyword on 16/1/22.
 */
public abstract class SupportActivity extends AppCompatActivity {
    private Fragmentation mFragmentation;

    private FragmentAnimator mFragmentAnimator;

    boolean mPopMulitpleNoAnim = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mFragmentation = new Fragmentation(this, setContainerId());

        mFragmentAnimator = onCreateFragmentAnimator();

        onHandleSaveInstancState(savedInstanceState);
    }

    protected void onHandleSaveInstancState(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            List<Fragment> fragments = getSupportFragmentManager().getFragments();

            if (fragments != null && fragments.size() > 0) {
                boolean showFlag = false;

                FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
                for (int i = fragments.size() - 1; i >= 0; i--) {
                    Fragment fragment = fragments.get(i);
                    if (fragment != null) {
                        if (!showFlag) {
                            ft.show(fragments.get(i));
                            showFlag = true;
                        } else {
                            ft.hide(fragments.get(i));
                        }
                    }
                }
                ft.commit();
            }
        }
    }

    /**
     * 创建全局Fragment的切换动画
     *
     * @return
     */
    protected FragmentAnimator onCreateFragmentAnimator() {
        return new DefaultVerticalAnimator();
    }

    Fragmentation getFragmentation() {
        return mFragmentation;
    }

    /**
     * Set Container's id
     */
    protected abstract int setContainerId();

    @Override
    public void onBackPressed() {
        SupportFragment topFragment = getTopFragment();
        if (topFragment != null) {
            boolean result = topFragment.onBackPressedSupport();
            if (result) {
                return;
            }
        }

        if (getSupportFragmentManager().getBackStackEntryCount() > 1) {
            mFragmentation.back(getSupportFragmentManager());
        } else {
            finish();
        }
    }

    /**
     * 获取栈内的framgent对象
     *
     * @param fragmentClass
     */
    public <T extends SupportFragment> T findFragment(Class<T> fragmentClass) {
        return mFragmentation.findStackFragment(fragmentClass, getSupportFragmentManager());
    }

    /**
     * 出栈
     */
    public void pop() {
        mFragmentation.back(getSupportFragmentManager());
    }

    /**
     * 出栈到目标fragment
     *
     * @param fragmentClass 目标fragment
     * @param includeSelf   是否包含该fragment
     */
    public void popTo(Class<?> fragmentClass, boolean includeSelf) {
        mFragmentation.popTo(fragmentClass, includeSelf, null, getSupportFragmentManager());
    }

    /**
     * 用于出栈后,立刻进行FragmentTransaction操作
     */
    public void popTo(Class<?> fragmentClass, boolean includeSelf, Runnable afterPopTransactionRunnable) {
        mFragmentation.popTo(fragmentClass, includeSelf, afterPopTransactionRunnable, getSupportFragmentManager());
    }

    public void start(SupportFragment toFragment) {
        start(toFragment, SupportFragment.STANDARD);
    }

    public void start(SupportFragment toFragment, @SupportFragment.LaunchMode int launchMode) {
        mFragmentation.dispatchTransaction(getTopFragment(), toFragment, 0, launchMode, Fragmentation.TYPE_ADD);
    }

    public void startForResult(SupportFragment to, int requestCode) {
        mFragmentation.dispatchTransaction(getTopFragment(), to, requestCode, SupportFragment.STANDARD, Fragmentation.TYPE_ADD);
    }

    public void startWithFinish(SupportFragment to) {
        mFragmentation.dispatchTransaction(getTopFragment(), to, 0, SupportFragment.STANDARD, Fragmentation.TYPE_ADD_FINISH);
    }

    /**
     * 得到位于栈顶Fragment
     * @return
     */
    public SupportFragment getTopFragment() {
        return mFragmentation.getTopFragment(getSupportFragmentManager());
    }

    /**
     * 获取设置的全局动画
     * @return
     */
    public FragmentAnimator getFragmentAnimator() {
        return new FragmentAnimator(
                mFragmentAnimator.getEnter(), mFragmentAnimator.getExit(),
                mFragmentAnimator.getPopEnter(), mFragmentAnimator.getPopExit()
        );
    }

    /**
     * 设置全局动画
     * @param fragmentAnimator
     */
    public void setFragmentAnimator(FragmentAnimator fragmentAnimator) {
        this.mFragmentAnimator = fragmentAnimator;
    }

    void preparePopMultiple() {
        mPopMulitpleNoAnim = true;
    }

    void popFinish() {
        mPopMulitpleNoAnim = false;
    }

    /**
     * 显示栈视图
     */
    public void showFragmentStackHierarchyView() {
        HierarchyViewContainer container = new HierarchyViewContainer(this);
        container.bindFragmentRecords(getFragmentRecords());
        container.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        new AlertDialog.Builder(this)
                .setTitle("栈视图")
                .setView(container)
                .setPositiveButton("关闭", null)
                .setCancelable(true)
                .show();
    }

    private List<FragmentRecord> getFragmentRecords() {
        List<FragmentRecord> fragmentRecords = new ArrayList<>();

        List<Fragment> fragmentList = getSupportFragmentManager().getFragments();
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
            fragmentRecords.add(new FragmentRecord(fragment.getClass().getSimpleName(), getChildFragmentRecords(fragment)));
        }
        return fragmentRecords;
    }
}
