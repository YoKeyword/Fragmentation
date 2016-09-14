package me.yokeyword.sample.demo_zhihu.ui.fragment.third;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import me.yokeyword.sample.R;
import me.yokeyword.sample.demo_zhihu.base.BaseLazyMainFragment;
import me.yokeyword.sample.demo_zhihu.ui.fragment.third.child.ShopFragment;

/**
 * Created by YoKeyword on 16/6/3.
 */
public class ZhihuThirdFragment extends BaseLazyMainFragment {

    public static ZhihuThirdFragment newInstance() {

        Bundle args = new Bundle();

        ZhihuThirdFragment fragment = new ZhihuThirdFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.zhihu_fragment_third, container, false);
        return view;
    }

    @Override
    protected void initLazyView(@Nullable Bundle savedInstanceState) {
        if (savedInstanceState == null) {
            // ShopFragment是flow包里的
            loadRootFragment(R.id.fl_third_container, ShopFragment.newInstance());
        } else { // 这里可能会出现该Fragment没被初始化时,就被强杀导致的没有load子Fragment
            if (findChildFragment(ShopFragment.class) == null) {
                loadRootFragment(R.id.fl_third_container, ShopFragment.newInstance());
            }
        }
    }
}
