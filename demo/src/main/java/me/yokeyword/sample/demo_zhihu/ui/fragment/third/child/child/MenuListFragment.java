package me.yokeyword.sample.demo_zhihu.ui.fragment.third.child.child;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

import me.yokeyword.fragmentation.anim.DefaultNoAnimator;
import me.yokeyword.fragmentation.anim.FragmentAnimator;
import me.yokeyword.sample.R;
import me.yokeyword.sample.demo_zhihu.adapter.MenuAdapter;
import me.yokeyword.sample.demo_zhihu.listener.OnItemClickListener;
import me.yokeyword.sample.demo_zhihu.base.BaseFragment;
import me.yokeyword.sample.demo_zhihu.ui.fragment.third.child.ShopFragment;

/**
 * Created by YoKeyword on 16/2/9.
 */
public class MenuListFragment extends BaseFragment {
    private static final String ARG_MENUS = "arg_menus";
    private static final String SAVE_STATE_POSITION = "save_state_position";

    private RecyclerView mRecy;
    private MenuAdapter mAdapter;

    private ArrayList<String> mMenus;
    private int mCurrentPosition = -1;

    public static MenuListFragment newInstance(ArrayList<String> menus) {

        Bundle args = new Bundle();
        args.putStringArrayList(ARG_MENUS, menus);

        MenuListFragment fragment = new MenuListFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle args = getArguments();
        if (args != null) {
            mMenus = args.getStringArrayList(ARG_MENUS);
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_list_menu, container, false);
        initView(view);
        return view;
    }

    @Override
    protected FragmentAnimator onCreateFragmentAnimator() {
        return new DefaultNoAnimator();
    }

    private void initView(View view) {
        mRecy = (RecyclerView) view.findViewById(R.id.recy);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        LinearLayoutManager manager = new LinearLayoutManager(_mActivity);
        mRecy.setLayoutManager(manager);
        mAdapter = new MenuAdapter(_mActivity);
        mRecy.setAdapter(mAdapter);
        mAdapter.setDatas(mMenus);

        mAdapter.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(int position, View view, RecyclerView.ViewHolder vh) {
                showContent(position);
            }
        });

        if (savedInstanceState != null) {
            mCurrentPosition = savedInstanceState.getInt(SAVE_STATE_POSITION);
            mAdapter.setItemChecked(mCurrentPosition);
        } else {
            mCurrentPosition = 0;
            mAdapter.setItemChecked(0);
        }
    }

    private void showContent(int position) {
        if (position == mCurrentPosition) {
            return;
        }

        mCurrentPosition = position;

        mAdapter.setItemChecked(position);

        ContentFragment fragment = ContentFragment.newInstance(mMenus.get(position));

        ((ShopFragment) getParentFragment()).switchContentFragment(fragment);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(SAVE_STATE_POSITION, mCurrentPosition);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mRecy.setAdapter(null);
    }
}
