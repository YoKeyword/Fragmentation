package me.yokeyword.sample.demo_wechat.ui.fragment.second;

import android.os.Bundle;
import androidx.annotation.Nullable;
import com.google.android.material.tabs.TabLayout;
import androidx.viewpager.widget.ViewPager;
import androidx.appcompat.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import me.yokeyword.sample.R;
import me.yokeyword.sample.demo_wechat.adapter.WechatPagerFragmentAdapter;
import me.yokeyword.sample.demo_wechat.base.BaseMainFragment;

/**
 * Created by YoKeyword on 16/6/30.
 */
public class WechatSecondTabFragment extends BaseMainFragment {
    private TabLayout mTab;
    private Toolbar mToolbar;
    private ViewPager mViewPager;

    public static WechatSecondTabFragment newInstance() {

        Bundle args = new Bundle();

        WechatSecondTabFragment fragment = new WechatSecondTabFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.wechat_fragment_tab_second, container, false);
        initView(view);
        return view;
    }

    private void initView(View view) {
        mToolbar = (Toolbar) view.findViewById(R.id.toolbar);
        mTab = (TabLayout) view.findViewById(R.id.tab);
        mViewPager = (ViewPager) view.findViewById(R.id.viewPager);

        mToolbar.setTitle(R.string.discover);

        mTab.addTab(mTab.newTab());
        mTab.addTab(mTab.newTab());
    }

    @Override
    public void onLazyInitView(@Nullable Bundle savedInstanceState) {
        super.onLazyInitView(savedInstanceState);
        mViewPager.setAdapter(new WechatPagerFragmentAdapter(getChildFragmentManager()
                , getString(R.string.all), getString(R.string.more)));
        mTab.setupWithViewPager(mViewPager);
    }
}
