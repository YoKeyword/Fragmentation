package me.yokeyword.sample.demo_flow.ui.fragment_swipe_back;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import me.yokeyword.sample.R;
import me.yokeyword.sample.demo_flow.adapter.PagerAdapter;
import me.yokeyword.sample.demo_flow.listener.OnItemClickListener;


public class RecyclerSwipeBackFragment extends BaseSwipeBackFragment {
    private static final String ARG_FROM = "arg_from";

    private Toolbar mToolbar;

    private RecyclerView mRecy;
    private PagerAdapter mAdapter;

    public static RecyclerSwipeBackFragment newInstance() {
        RecyclerSwipeBackFragment fragment = new RecyclerSwipeBackFragment();
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_swipe_back_recy, container, false);

        initView(view);

        return attachToSwipeBack(view);
    }

    private void initView(View view) {
        mRecy = (RecyclerView) view.findViewById(R.id.recy);

        mToolbar = (Toolbar) view.findViewById(R.id.toolbar);
        _initToolbar(mToolbar);

        mAdapter = new PagerAdapter(_mActivity);
        LinearLayoutManager manager = new LinearLayoutManager(_mActivity);
        mRecy.setLayoutManager(manager);
        mRecy.setAdapter(mAdapter);

        mAdapter.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(int position, View view) {
                start(FirstSwipeBackFragment.newInstance());
            }
        });

        // Init Datas
        List<String> items = new ArrayList<>();
        for (int i = 0; i < 20; i++) {
            String item;
            item = getString(R.string.favorite) + " " + i;
            items.add(item);
        }
        mAdapter.setDatas(items);
    }
}
