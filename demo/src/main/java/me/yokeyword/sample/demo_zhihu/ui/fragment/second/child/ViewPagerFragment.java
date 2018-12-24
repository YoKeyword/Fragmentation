package me.yokeyword.sample.demo_zhihu.ui.fragment.second.child;

import android.os.Bundle;
import androidx.annotation.Nullable;
import com.google.android.material.tabs.TabLayout;
import androidx.viewpager.widget.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import me.yokeyword.fragmentation.SupportFragment;
import me.yokeyword.sample.R;
import me.yokeyword.sample.demo_zhihu.adapter.ZhihuPagerFragmentAdapter;

/**
 * Created by YoKeyword on 16/6/5.
 */
public class ViewPagerFragment extends SupportFragment {
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
        View view = inflater.inflate(R.layout.zhihu_fragment_second_pager, container, false);
        initView(view);
        return view;
    }

    private void initView(View view) {
        mTab = (TabLayout) view.findViewById(R.id.tab);
        mViewPager = (ViewPager) view.findViewById(R.id.viewPager);

        mTab.addTab(mTab.newTab());
        mTab.addTab(mTab.newTab());
        mTab.addTab(mTab.newTab());

        mViewPager.setAdapter(new ZhihuPagerFragmentAdapter(getChildFragmentManager(),
                getString(R.string.recommend), getString(R.string.hot), getString(R.string.favorite),
                getString(R.string.more)));
        mTab.setupWithViewPager(mViewPager);
    }
}
