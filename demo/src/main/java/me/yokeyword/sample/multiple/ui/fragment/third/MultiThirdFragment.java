package me.yokeyword.sample.multiple.ui.fragment.third;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import me.yokeyword.sample.R;
import me.yokeyword.sample.flow.ui.fragment.shop.ShopFragment;
import me.yokeyword.sample.multiple.ui.fragment.BaseLazyMainFragment;

/**
 * Created by YoKeyword on 16/6/3.
 */
public class MultiThirdFragment extends BaseLazyMainFragment {

    public static MultiThirdFragment newInstance() {

        Bundle args = new Bundle();

        MultiThirdFragment fragment = new MultiThirdFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.multi_fragment_third, container, false);
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
