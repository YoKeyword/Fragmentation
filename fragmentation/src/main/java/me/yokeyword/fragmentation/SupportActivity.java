package me.yokeyword.fragmentation;

import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.DrawableRes;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.MotionEvent;

import java.util.ArrayList;

import me.yokeyword.fragmentation.anim.DefaultVerticalAnimator;
import me.yokeyword.fragmentation.anim.FragmentAnimator;
import me.yokeyword.fragmentation.helper.FragmentLifecycleCallbacks;
import me.yokeyword.fragmentation.helper.internal.LifecycleHelper;

/**
 * Created by YoKeyword on 16/1/22.
 */
public class SupportActivity extends AppCompatActivity implements ISupport {
    private Fragmentation mFragmentation;

    private LifecycleHelper mLifecycleHelper;
    private ArrayList<FragmentLifecycleCallbacks> mFragmentLifecycleCallbacks;

    private FragmentAnimator mFragmentAnimator;

    private int mDefaultFragmentBackground = 0;

    boolean mPopMultipleNoAnim = false;

    // 防抖动 是否可以点击
    private boolean mFragmentClickable = true;

    private Handler mHandler;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mFragmentation = getFragmentation();
        mFragmentAnimator = onCreateFragmentAnimator();
    }

    public void registerFragmentLifecycleCallbacks(FragmentLifecycleCallbacks callback) {
        synchronized (this) {
            if (mFragmentLifecycleCallbacks == null) {
                mFragmentLifecycleCallbacks = new ArrayList<>();
                mLifecycleHelper = new LifecycleHelper(mFragmentLifecycleCallbacks);
            }
            mFragmentLifecycleCallbacks.add(callback);
        }
    }

    public void unregisterFragmentLifecycleCallbacks(FragmentLifecycleCallbacks callback) {
        synchronized (this) {
            if (mFragmentLifecycleCallbacks != null) {
                mFragmentLifecycleCallbacks.remove(callback);
            }
        }
    }

    Fragmentation getFragmentation() {
        if (mFragmentation == null) {
            mFragmentation = new Fragmentation(this);
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
     * 获取设置的全局动画, copy
     *
     * @return FragmentAnimator
     */
    public FragmentAnimator getFragmentAnimator() {
        return new FragmentAnimator(
                mFragmentAnimator.getEnter(), mFragmentAnimator.getExit(),
                mFragmentAnimator.getPopEnter(), mFragmentAnimator.getPopExit()
        );
    }

    /**
     * 设置全局动画, 建议使用onCreateFragmentAnimator()设置
     */
    public void setFragmentAnimator(FragmentAnimator fragmentAnimator) {
        this.mFragmentAnimator = fragmentAnimator;
    }

    /**
     * 构建Fragment转场动画
     * <p/>
     * 如果是在Activity内实现,则构建的是Activity内所有Fragment的转场动画,
     * 如果是在Fragment内实现,则构建的是该Fragment的转场动画,此时优先级 > Activity的onCreateFragmentAnimator()
     *
     * @return FragmentAnimator对象
     */
    protected FragmentAnimator onCreateFragmentAnimator() {
        return new DefaultVerticalAnimator();
    }

    /**
     * 当Fragment根布局 没有 设定background属性时,
     * Fragmentation默认使用Theme的android:windowbackground作为Fragment的背景,
     * 可以通过该方法改变Fragment背景。
     */
    protected void setDefaultFragmentBackground(@DrawableRes int backgroundRes) {
        mDefaultFragmentBackground = backgroundRes;
    }

    /**
     * (因为事务异步的原因) 如果你想在onCreate()中使用start/pop等 Fragment事务方法, 请使用该方法把你的任务入队
     *
     * @param runnable 需要执行的任务
     */
    protected void enqueueAction(Runnable runnable) {
        getHandler().post(runnable);
    }

    /**
     * 不建议复写该方法,请使用 {@link #onBackPressedSupport} 代替
     */
    @Override
    final public void onBackPressed() {
        // 这里是防止动画过程中，按返回键取消加载Fragment
        if (!mFragmentClickable) {
            setFragmentClickable(true);
        }

        // 获取activeFragment:即从栈顶开始 状态为show的那个Fragment
        SupportFragment activeFragment = mFragmentation.getActiveFragment(null, getSupportFragmentManager());
        if (mFragmentation.dispatchBackPressedEvent(activeFragment)) return;

        onBackPressedSupport();
    }

    /**
     * 该方法回调时机为,Activity回退栈内Fragment的数量 小于等于1 时,默认finish Activity
     * 请尽量复写该方法,避免复写onBackPress(),以保证SupportFragment内的onBackPressedSupport()回退事件正常执行
     */
    public void onBackPressedSupport() {
        if (getSupportFragmentManager().getBackStackEntryCount() > 1) {
            pop();
        } else {
            finish();
        }
    }

    /**
     * 加载根Fragment, 即Activity内的第一个Fragment 或 Fragment内的第一个子Fragment
     *
     * @param containerId 容器id
     * @param toFragment  目标Fragment
     */
    @Override
    public void loadRootFragment(int containerId, SupportFragment toFragment) {
        mFragmentation.loadRootTransaction(getSupportFragmentManager(), containerId, toFragment);
    }

    /**
     * 以replace方式加载根Fragment
     */
    @Override
    public void replaceLoadRootFragment(int containerId, SupportFragment toFragment, boolean addToBack) {
        mFragmentation.replaceLoadRootTransaction(getSupportFragmentManager(), containerId, toFragment, addToBack);
    }

    /**
     * 加载多个根Fragment
     *
     * @param containerId 容器id
     * @param toFragments 目标Fragments
     */
    @Override
    public void loadMultipleRootFragment(int containerId, int showPosition, SupportFragment... toFragments) {
        mFragmentation.loadMultipleRootTransaction(getSupportFragmentManager(), containerId, showPosition, toFragments);
    }

    /**
     * show一个Fragment,hide上一个Fragment
     * 使用该方法时，要确保同级栈内无多余的Fragment,(只有通过loadMultipleRootFragment()载入的Fragment)
     *
     * @param showFragment 需要show的Fragment
     */
    @Override
    public void showHideFragment(SupportFragment showFragment) {
        showHideFragment(showFragment, null);
    }

    /**
     * show一个Fragment,hide一个Fragment ; 主要用于类似微信主页那种 切换tab的情况
     *
     * @param showFragment 需要show的Fragment
     * @param hideFragment 需要hide的Fragment
     */
    @Override
    public void showHideFragment(SupportFragment showFragment, SupportFragment hideFragment) {
        mFragmentation.showHideFragment(getSupportFragmentManager(), showFragment, hideFragment);
    }

    /**
     * 启动目标Fragment
     *
     * @param toFragment 目标Fragment
     */
    @Override
    public void start(SupportFragment toFragment) {
        start(toFragment, SupportFragment.STANDARD);
    }

    @Override
    public void start(SupportFragment toFragment, @SupportFragment.LaunchMode int launchMode) {
        mFragmentation.dispatchStartTransaction(getSupportFragmentManager(), getTopFragment(), toFragment, 0, launchMode, Fragmentation.TYPE_ADD, null, null);
    }

    @Override
    public void startForResult(SupportFragment toFragment, int requestCode) {
        mFragmentation.dispatchStartTransaction(getSupportFragmentManager(), getTopFragment(), toFragment, requestCode, SupportFragment.STANDARD, Fragmentation.TYPE_ADD_RESULT, null, null);
    }

    @Override
    public void startWithPop(SupportFragment toFragment) {
        mFragmentation.dispatchStartTransaction(getSupportFragmentManager(), getTopFragment(), toFragment, 0, SupportFragment.STANDARD, Fragmentation.TYPE_ADD_WITH_POP, null, null);
    }

    /**
     * 得到位于栈顶Fragment
     */
    @Override
    public SupportFragment getTopFragment() {
        return mFragmentation.getTopFragment(getSupportFragmentManager());
    }

    /**
     * 获取栈内的fragment对象
     */
    @Override
    public <T extends SupportFragment> T findFragment(Class<T> fragmentClass) {
        return mFragmentation.findStackFragment(fragmentClass, null, getSupportFragmentManager());
    }

    @Override
    public <T extends SupportFragment> T findFragment(String fragmentTag) {
        Fragmentation.checkNotNull(fragmentTag, "tag == null");
        return mFragmentation.findStackFragment(null, fragmentTag, getSupportFragmentManager());
    }

    /**
     * 出栈
     */
    @Override
    public void pop() {
        mFragmentation.back(getSupportFragmentManager());
    }

    /**
     * 出栈到目标fragment
     *
     * @param fragmentClass 目标fragment
     * @param includeSelf   是否包含该fragment
     */
    @Override
    public void popTo(Class<?> fragmentClass, boolean includeSelf) {
        popTo(fragmentClass.getName(), includeSelf);
    }

    @Override
    public void popTo(String fragmentTag, boolean includeSelf) {
        popTo(fragmentTag, includeSelf, null);
    }

    /**
     * 用于出栈后,立刻进行FragmentTransaction操作
     */
    @Override
    public void popTo(Class<?> fragmentClass, boolean includeSelf, Runnable afterPopTransactionRunnable) {
        popTo(fragmentClass.getName(), includeSelf, afterPopTransactionRunnable);
    }

    @Override
    public void popTo(String fragmentTag, boolean includeSelf, Runnable afterPopTransactionRunnable) {
        mFragmentation.popTo(fragmentTag, includeSelf, afterPopTransactionRunnable, getSupportFragmentManager());
    }

    void preparePopMultiple() {
        mPopMultipleNoAnim = true;
    }

    void popFinish() {
        mPopMultipleNoAnim = false;
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mFragmentLifecycleCallbacks != null) {
            mFragmentLifecycleCallbacks.clear();
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_BACK)) {
            // 这里是防止动画过程中，按返回键取消加载Fragment
            if (!mFragmentClickable) {
                setFragmentClickable(true);
            }
        }
        return super.onKeyDown(keyCode, event);
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

    public int getDefaultFragmentBackground() {
        return mDefaultFragmentBackground;
    }

    public void setFragmentClickable() {
        mFragmentClickable = true;
    }

    /**
     * 显示栈视图dialog,调试时使用
     */
    public void showFragmentStackHierarchyView() {
        mFragmentation.showFragmentStackHierarchyView();
    }

    /**
     * 显示栈视图日志,调试时使用
     */
    public void logFragmentStackHierarchy(String TAG) {
        mFragmentation.logFragmentRecords(TAG);
    }

    void dispatchFragmentLifecycle(int lifecycle, SupportFragment fragment) {
        dispatchFragmentLifecycle(lifecycle, fragment, null);
    }

    void dispatchFragmentLifecycle(int lifecycle, SupportFragment fragment, Bundle bundle) {
        dispatchFragmentLifecycle(lifecycle, fragment, bundle, false);
    }

    void dispatchFragmentLifecycle(int lifecycle, SupportFragment fragment, boolean visible) {
        dispatchFragmentLifecycle(lifecycle, fragment, null, visible);
    }

    void dispatchFragmentLifecycle(int lifecycle, SupportFragment fragment, Bundle bundle, boolean visible) {
        if (mLifecycleHelper != null) {
            mLifecycleHelper.dispatchLifecycle(lifecycle, fragment, bundle, visible);
        }
    }
}
