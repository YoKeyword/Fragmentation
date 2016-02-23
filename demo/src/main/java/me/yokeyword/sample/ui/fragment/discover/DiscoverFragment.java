package me.yokeyword.sample.ui.fragment.discover;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


import me.yokeyword.fragmentation.SupportFragment;
import me.yokeyword.sample.R;
import me.yokeyword.sample.adapter.DiscoverFragmentAdapter;
import me.yokeyword.sample.ui.BaseMainFragment;

/**
 * Created by YoKeyword on 16/2/3.
 */
public class DiscoverFragment extends BaseMainFragment {
    private Toolbar mToolbar;
    private TabLayout mTabLayout;
    private ViewPager mViewPager;

    public static DiscoverFragment newInstance() {
        return new DiscoverFragment();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_discover, container, false);

        initView(view);

        return view;
    }

    private void initView(View view) {
        mToolbar = (Toolbar) view.findViewById(R.id.toolbar);
        mTabLayout = (TabLayout) view.findViewById(R.id.tab_layout);
        mViewPager = (ViewPager) view.findViewById(R.id.viewPager);

        mToolbar.setTitle("发现");
        initToolbarNav(mToolbar);

        mTabLayout.addTab(mTabLayout.newTab().setText(R.string.recommend));
        mTabLayout.addTab(mTabLayout.newTab().setText(R.string.hot));
        mTabLayout.addTab(mTabLayout.newTab().setText(R.string.favorite));
        mViewPager.setAdapter(new DiscoverFragmentAdapter(getChildFragmentManager()));
        mTabLayout.setupWithViewPager(mViewPager);
    }
}
