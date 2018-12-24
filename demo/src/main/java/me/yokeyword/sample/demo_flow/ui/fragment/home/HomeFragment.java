package me.yokeyword.sample.demo_flow.ui.fragment.home;

import android.os.Bundle;
import androidx.core.view.GravityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.appcompat.widget.PopupMenu;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import me.yokeyword.fragmentation.ISupportActivity;
import me.yokeyword.fragmentation.anim.DefaultHorizontalAnimator;
import me.yokeyword.fragmentation.anim.DefaultNoAnimator;
import me.yokeyword.fragmentation.anim.DefaultVerticalAnimator;
import me.yokeyword.sample.R;
import me.yokeyword.sample.demo_flow.adapter.HomeAdapter;
import me.yokeyword.sample.demo_flow.base.BaseMainFragment;
import me.yokeyword.sample.demo_flow.entity.Article;
import me.yokeyword.sample.demo_flow.listener.OnItemClickListener;


public class HomeFragment extends BaseMainFragment implements Toolbar.OnMenuItemClickListener {
    private static final String TAG = "Fragmentation";

    private String[] mTitles;

    private String[] mContents;

    private Toolbar mToolbar;
    private RecyclerView mRecy;
    private HomeAdapter mAdapter;

    public static HomeFragment newInstance() {
        return new HomeFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        initView(view);
//        动态改动 当前Fragment的动画
//        setFragmentAnimator(fragmentAnimator);
        return view;
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_anim:
                final PopupMenu popupMenu = new PopupMenu(_mActivity, mToolbar, GravityCompat.END);
                popupMenu.inflate(R.menu.home_pop);
                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getItemId()) {
                            case R.id.action_anim_veritical:
                                ((ISupportActivity) _mActivity).setFragmentAnimator(new DefaultVerticalAnimator());
                                Toast.makeText(_mActivity, R.string.anim_v, Toast.LENGTH_SHORT).show();
                                break;
                            case R.id.action_anim_horizontal:
                                ((ISupportActivity) _mActivity).setFragmentAnimator(new DefaultHorizontalAnimator());
                                Toast.makeText(_mActivity, R.string.anim_h, Toast.LENGTH_SHORT).show();
                                break;
                            case R.id.action_anim_none:
                                ((ISupportActivity) _mActivity).setFragmentAnimator(new DefaultNoAnimator());
                                Toast.makeText(_mActivity, R.string.anim_none, Toast.LENGTH_SHORT).show();
                                break;
                        }
                        popupMenu.dismiss();
                        return true;
                    }
                });
                popupMenu.show();
                break;
        }
        return true;
    }

    private void initView(View view) {
        mToolbar = (Toolbar) view.findViewById(R.id.toolbar);
        mRecy = (RecyclerView) view.findViewById(R.id.recy);

        mTitles = getResources().getStringArray(R.array.array_title);
        mContents = getResources().getStringArray(R.array.array_content);

        mToolbar.setTitle(R.string.home);
        initToolbarNav(mToolbar, true);
        mToolbar.inflateMenu(R.menu.home);
        mToolbar.setOnMenuItemClickListener(this);

        mAdapter = new HomeAdapter(_mActivity);
        LinearLayoutManager manager = new LinearLayoutManager(_mActivity);
        mRecy.setLayoutManager(manager);
        mRecy.setAdapter(mAdapter);

        mAdapter.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(int position, View view) {
                start(DetailFragment.newInstance(mAdapter.getItem(position).getTitle()));
            }
        });

        // Init Datas
        List<Article> articleList = new ArrayList<>();
        for (int i = 0; i < 15; i++) {
            int index = (int) (Math.random() * 3);
            Article article = new Article(mTitles[index], mContents[index]);
            articleList.add(article);
        }
        mAdapter.setDatas(articleList);
    }

    /**
     * 类似于 Activity的 onNewIntent()
     */
    @Override
    public void onNewBundle(Bundle args) {
        super.onNewBundle(args);

        Toast.makeText(_mActivity, args.getString("from"), Toast.LENGTH_SHORT).show();
    }
}
