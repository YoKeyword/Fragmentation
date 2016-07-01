package me.yokeyword.sample.demo_wechat.ui.fragment.second;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.ArrayList;
import java.util.List;

import me.yokeyword.sample.R;
import me.yokeyword.sample.demo_wechat.adapter.PagerAdapter;
import me.yokeyword.sample.demo_wechat.base.BaseFragment;
import me.yokeyword.sample.demo_wechat.event.StartBrotherEvent;
import me.yokeyword.sample.demo_wechat.event.TabSelectedEvent;
import me.yokeyword.sample.demo_wechat.listener.OnItemClickListener;
import me.yokeyword.sample.demo_wechat.ui.fragment.CycleFragment;
import me.yokeyword.sample.demo_wechat.ui.fragment.MainFragment;

/**
 * Created by YoKeyword on 16/6/30.
 */
public class FirstPagerFragment extends BaseFragment implements SwipeRefreshLayout.OnRefreshListener {
    private SwipeRefreshLayout mRefreshLayout;
    private RecyclerView mRecy;
    private PagerAdapter mAdapter;

    private boolean mInAtTop = true;
    private int mScrollTotal;

    public static FirstPagerFragment newInstance() {

        Bundle args = new Bundle();

        FirstPagerFragment fragment = new FirstPagerFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.wechat_fragment_tab_second_pager_first, container, false);
        initView(view);
        return view;
    }

    private void initView(View view) {
        EventBus.getDefault().register(this);

        mRecy = (RecyclerView) view.findViewById(R.id.recy);
        mRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.refresh_layout);

        mRefreshLayout.setOnRefreshListener(this);

        mAdapter = new PagerAdapter(_mActivity);
        mRecy.setHasFixedSize(true);
        LinearLayoutManager manager = new LinearLayoutManager(_mActivity);
        mRecy.setLayoutManager(manager);
        mRecy.setAdapter(mAdapter);

        mRecy.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                mScrollTotal += dy;
                if (mScrollTotal <= 0) {
                    mInAtTop = true;
                } else {
                    mInAtTop = false;
                }
            }
        });

        mAdapter.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(int position, View view, RecyclerView.ViewHolder holder) {
                // 通知MainActivity跳转至CycleFragment
                EventBus.getDefault().post(new StartBrotherEvent(CycleFragment.newInstance(1)));
            }
        });

        // Init Datas
        List<String> items = new ArrayList<>();
        for (int i = 0; i < 20; i++) {
            String item = "联系人 " + i;
            items.add(item);
        }
        mAdapter.setDatas(items);
    }

    @Override
    public void onRefresh() {
        mRefreshLayout.postDelayed(new Runnable() {
            @Override
            public void run() {
                mRefreshLayout.setRefreshing(false);
            }
        }, 2500);
    }

    /**
     * Reselected Tab
     */
    @Subscribe
    public void onTabSelectedEvent(TabSelectedEvent event) {
        if (event.position != MainFragment.SECOND) return;

        if (mInAtTop) {
            mRefreshLayout.setRefreshing(true);
            onRefresh();
        } else {
            scrollToTop();
        }
    }

    private void scrollToTop() {
        mRecy.smoothScrollToPosition(0);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mRecy.setAdapter(null);
        EventBus.getDefault().unregister(this);
    }
}
