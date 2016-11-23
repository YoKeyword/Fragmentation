package me.yokeyword.fragmentation;

import android.os.Bundle;
import android.support.annotation.Nullable;

/**
 * SupportFragment 提供的生命周期方法
 * Created by YoKey on 16/11/22.
 */
interface ISupportLifecycleCallback {
    /**
     * Fragment可见时回调
     * onHiddenChanged() + onResume()/onPause() + setUserVisibleHint() 的结合回调方法
     */
    void onSupportVisible();

    /**
     * Fragment 不可见时回调
     * onHiddenChanged() + onResume()/onPause() + setUserVisibleHint() 的结合回调方法
     */
    void onSupportInvisible();

    boolean isSupportVisible();

    /**
     * 懒加载，即：第一次Fragment可见时回调
     * 同级下的 懒加载 ＋ ViewPager下的懒加载  的结合回调方法
     */
    void onLazyInitView(@Nullable Bundle savedInstanceState);
}
