package me.yokeyword.fragmentation;

import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ViewGroup;

import java.util.List;

import me.yokeyword.fragmentation.anim.DefaultVerticalAnimator;
import me.yokeyword.fragmentation.anim.FragmentAnimator;
import me.yokeyword.fragmentation.debug.DebugFragmentRecord;
import me.yokeyword.fragmentation.debug.DebugHierarchyViewContainer;

/**
 * Created by YoKeyword on 16/1/22.
 */
public abstract class SupportActivity extends AppCompatActivity {
    private Fragmentation mFragmentation;

    private FragmentAnimator mFragmentAnimator;

    boolean mPopMulitpleNoAnim = false;

    private boolean mFragmentClickable = true;

    private Handler mHandler;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mFragmentation = getFragmentation();

        mFragmentAnimator = onCreateFragmentAnimator();

        if (restoreInstanceState()) {
            processRestoreInstanceState(savedInstanceState);
        }
    }

    private void processRestoreInstanceState(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            List<Fragment> fragments = getSupportFragmentManager().getFragments();

            if (fragments != null && fragments.size() > 0) {

                FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
                for (int i = fragments.size() - 1; i >= 0; i--) {
                    Fragment fragment = fragments.get(i);

                    if (fragment instanceof SupportFragment) {
                        SupportFragment supportFragment = (SupportFragment) fragment;
                        if (supportFragment.isSupportHidden()) {
                            ft.hide(supportFragment);
                        } else {
                            ft.show(supportFragment);
                        }
                    }
                }
                ft.commit();
            }
        }
    }

    /**
     * 内存重启后,是否让Fragmentation帮你恢复Fragment状态
     *
     * @return
     */
    protected boolean restoreInstanceState() {
        return true;
    }

    /**
     * 创建全局Fragment的切换动画
     *
     * @return
     */
    protected FragmentAnimator onCreateFragmentAnimator() {
        return new DefaultVerticalAnimator();
    }

    /**
     * Set Container's id
     */
    protected abstract int setContainerId();

    Fragmentation getFragmentation() {
        if (mFragmentation == null) {
            mFragmentation = new Fragmentation(this, setContainerId());
        }
        return mFragmentation;
    }

    Handler getHandler() {
        if (mHandler == null) {
            mHandler = new Handler();
        }
        return mHandler;
    }

    /**
     * 获取设置的全局动画
     *
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
     *
     * @param fragmentAnimator
     */
    public void setFragmentAnimator(FragmentAnimator fragmentAnimator) {
        this.mFragmentAnimator = fragmentAnimator;
    }

    @Override
    public void onBackPressed() {
        // 这里是防止动画过程中，按返回键取消加载Fragment
        setFragmentClickable(true);

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
     * 得到位于栈顶Fragment
     *
     * @return
     */
    public SupportFragment getTopFragment() {
        return mFragmentation.getTopFragment(getSupportFragmentManager());
    }

    /**
     * 获取栈内的framgent对象
     *
     * @param fragmentClass
     */
    public <T extends SupportFragment> T findFragment(Class<T> fragmentClass) {
        return mFragmentation.findStackFragment(fragmentClass, getSupportFragmentManager(), false);
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
        mFragmentation.dispatchStartTransaction(getTopFragment(), toFragment, 0, launchMode, Fragmentation.TYPE_ADD);
    }

    public void startForResult(SupportFragment to, int requestCode) {
        mFragmentation.dispatchStartTransaction(getTopFragment(), to, requestCode, SupportFragment.STANDARD, Fragmentation.TYPE_ADD);
    }

    public void startWithPop(SupportFragment to) {
        mFragmentation.dispatchStartTransaction(getTopFragment(), to, 0, SupportFragment.STANDARD, Fragmentation.TYPE_ADD_WITH_POP);
    }

    void preparePopMultiple() {
        mPopMulitpleNoAnim = true;
    }

    void popFinish() {
        mPopMulitpleNoAnim = false;
    }


    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        // 防抖动(防止点击速度过快)
        if (!mFragmentClickable) return true;

        return super.dispatchTouchEvent(ev);
    }

    /**
     * 防抖动(防止点击速度过快)
     */
    void setFragmentClickable(boolean clickable) {
        mFragmentClickable = clickable;
    }

    /**
     * 显示栈视图,调试时使用
     */
    public void showFragmentStackHierarchyView() {
        DebugHierarchyViewContainer container = new DebugHierarchyViewContainer(this);
        container.bindFragmentRecords(mFragmentation.getFragmentRecords());
        container.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        new AlertDialog.Builder(this)
                .setTitle("栈视图")
                .setView(container)
                .setPositiveButton("关闭", null)
                .setCancelable(true)
                .show();
    }

    /**
     * 显示栈视图 日志 ,调试时使用
     */
    public void logFragmentStackHierarchy(String TAG) {
        List<DebugFragmentRecord> fragmentRecordList = mFragmentation.getFragmentRecords();
        if (fragmentRecordList == null) return;

        StringBuilder sb = new StringBuilder();

        for (int i = fragmentRecordList.size() - 1; i >= 0; i--) {
            DebugFragmentRecord fragmentRecord = fragmentRecordList.get(i);

            if (i == fragmentRecordList.size() - 1) {
                sb.append("═══════════════════════════════════════════════════════════════════════════════════\n");
                if (i == 0) {
                    sb.append("\t栈顶\t\t\t" + fragmentRecord.fragmentName + "\n");
                    sb.append("═══════════════════════════════════════════════════════════════════════════════════");
                } else {
                    sb.append("\t栈顶\t\t\t" + fragmentRecord.fragmentName + "\n\n");
                }
            } else if (i == 0) {
                sb.append("\t栈底\t\t\t" + fragmentRecord.fragmentName + "\n");
                sb.append("═══════════════════════════════════════════════════════════════════════════════════");
            } else {
                sb.append("\t↓\t\t\t" + fragmentRecord.fragmentName + "\n\n");
            }

            processChildLog(fragmentRecord.childFragmentRecord, sb);
        }

        Log.i(TAG, sb.toString());
    }

    private void processChildLog(List<DebugFragmentRecord> fragmentRecordList, StringBuilder sb) {
        if (fragmentRecordList == null || fragmentRecordList.size() == 0) return;

        for (int j = 0; j < fragmentRecordList.size(); j++) {
            DebugFragmentRecord childFragmentRecord = fragmentRecordList.get(j);
            if (j == 0) {
                sb.append("\t \t\t\t\t子栈顶\t\t\t" + childFragmentRecord.fragmentName + "\n\n");
            } else if (j == fragmentRecordList.size() - 1) {
                sb.append("\t \t\t\t\t子栈底\t\t\t" + childFragmentRecord.fragmentName + "\n\n");
            } else {
                sb.append("\t \t\t\t\t↓\t\t\t\t" + childFragmentRecord.fragmentName + "\n\n");
            }
            processChildLog(childFragmentRecord.childFragmentRecord, sb);
        }
    }
}
