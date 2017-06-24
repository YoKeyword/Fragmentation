package me.yokeyword.sample;

import android.os.Bundle;
import android.support.annotation.Nullable;

import me.yokeyword.fragmentation.SupportFragment;
import me.yokeyword.sample.demo_flow.ui.fragment.home.HomeFragment;

/**
 * 该类是展示 1.0 版本新特性 extraTransaction()
 *
 * Created by YoKey on 16/11/25.
 */
public class NewFeatureFragment extends SupportFragment {

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // 是否 可见
        boolean visible = isSupportVisible();
    }

    // 模拟执行一次 start Fragment
    private void onClickStartButton() {
        HomeFragment homeFragment = HomeFragment.newInstance();
        homeFragment.extraTransaction()
                .setTag("我是自定义Tag")
//                .addSharedElement()
                .start(homeFragment);
    }

    // 模拟执行一次 出栈
    private void onClickPopButton() {
//       SupportFragment fragment =  findFragment("我是自定义Tag");
        extraTransaction().popTo("我是自定义Tag", false);
    }

    @Override
    public void onLazyInitView(@Nullable Bundle savedInstanceState) {
        super.onLazyInitView(savedInstanceState);
        // 懒加载
        // 同级Fragment场景、ViewPager场景均适用
    }

    @Override
    public void onSupportVisible() {
        super.onSupportVisible();
        // 当对用户可见时 回调
        // 不管是 父Fragment还是子Fragment 都有效！
    }

    @Override
    public void onSupportInvisible() {
        super.onSupportInvisible();
        // 当对用户不可见时 回调
        // 不管是 父Fragment还是子Fragment 都有效！
    }
}
