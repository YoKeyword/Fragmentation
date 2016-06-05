package me.yokeyword.sample.multiple.ui.fragment.second;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import me.yokeyword.sample.R;
import me.yokeyword.sample.common.adapter.MultiPagerFragmentAdapter;
import me.yokeyword.sample.common.base.BaseFragment;

/**
 * Created by YoKeyword on 16/6/5.
 */
public class ViewPagerFragment extends BaseFragment {
    private TabLayout mTab;
    private ViewPager mViewPager;

    public static ViewPagerFragment newInstance() {

        Bundle args = new Bundle();

        ViewPagerFragment fragment = new ViewPagerFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.multi_fragment_second_pager, container, false);
        initView(view);
        return view;
    }

    private void initView(View view) {
        mTab = (TabLayout) view.findViewById(R.id.tab);
        mViewPager = (ViewPager) view.findViewById(R.id.viewPager);

        mTab.addTab(mTab.newTab());
        mTab.addTab(mTab.newTab());
        mTab.addTab(mTab.newTab());

        mViewPager.setAdapter(new MultiPagerFragmentAdapter(getChildFragmentManager()));
        mTab.setupWithViewPager(mViewPager);
    }
}
