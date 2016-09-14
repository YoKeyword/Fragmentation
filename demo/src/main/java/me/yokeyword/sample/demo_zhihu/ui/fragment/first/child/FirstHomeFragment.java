package me.yokeyword.sample.demo_zhihu.ui.fragment.first.child;

import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.transition.Fade;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.ArrayList;
import java.util.List;

import me.yokeyword.sample.R;
import me.yokeyword.sample.demo_zhihu.MainActivity;
import me.yokeyword.sample.demo_zhihu.adapter.FirstHomeAdapter;
import me.yokeyword.sample.demo_zhihu.entity.Article;
import me.yokeyword.sample.demo_zhihu.event.TabSelectedEvent;
import me.yokeyword.sample.demo_zhihu.helper.DetailTransition;
import me.yokeyword.sample.demo_zhihu.listener.OnItemClickListener;
import me.yokeyword.sample.demo_zhihu.base.BaseFragment;

/**
 * Created by YoKeyword on 16/6/5.
 */
public class FirstHomeFragment extends BaseFragment implements SwipeRefreshLayout.OnRefreshListener {
    private Toolbar mToolbar;
    private RecyclerView mRecy;
    private SwipeRefreshLayout mRefreshLayout;
    private FloatingActionButton mFab;

    private FirstHomeAdapter mAdapter;

    private boolean mInAtTop = true;
    private int mScrollTotal;

    private String[] mTitles = new String[]{
            "超美艳的18位古装新娘：朱茵、唐嫣、赵丽颖、佟丽娅等",
            "盘点娱乐圈女星，你肯定想不到还有她",
            "娱圈中原配与情人相处无事的明星们，原来大傻一点都不傻",
            "娱乐圈十大演技好、绯闻绝缘体女星，无视潜规则难上位",
            "他是中国最牛X主持,是龙年春晚主持第七人"
    };

    private int[] mImgRes = new int[]{
            R.drawable.img_3, R.drawable.img_4, R.drawable.img_5, R.drawable.img_1, R.drawable.img_2
    };


    public static FirstHomeFragment newInstance() {

        Bundle args = new Bundle();

        FirstHomeFragment fragment = new FirstHomeFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.zhihu_fragment_first_home, container, false);
        EventBus.getDefault().register(this);
        initView(view);
        return view;
    }

    private void initView(View view) {
        mToolbar = (Toolbar) view.findViewById(R.id.toolbar);
        mRecy = (RecyclerView) view.findViewById(R.id.recy);
        mRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.refresh_layout);
        mFab = (FloatingActionButton) view.findViewById(R.id.fab);

        mToolbar.setTitle("首页");
        initToolbarMenu(mToolbar);

        mRefreshLayout.setColorSchemeResources(R.color.colorPrimary);
        mRefreshLayout.setOnRefreshListener(this);

        mAdapter = new FirstHomeAdapter(_mActivity);
        LinearLayoutManager manager = new LinearLayoutManager(_mActivity);
        mRecy.setLayoutManager(manager);
        mRecy.setAdapter(mAdapter);

        mAdapter.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(int position, View view, RecyclerView.ViewHolder vh) {
                FirstDetailFragment fragment = FirstDetailFragment.newInstance(mAdapter.getItem(position));

                // 这里是使用SharedElement的用例
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    setExitTransition(new Fade());
                    setSharedElementReturnTransition(new DetailTransition());
//                    fragment.setEnterTransition(new Fade());
                    fragment.setSharedElementEnterTransition(new DetailTransition());

                    // 因为使用add的原因,Material过渡动画只有在进栈时有,返回时没有;
                    // 如果想进栈和出栈都有过渡动画,需要replace,目前库暂不支持,后续会调研看是否可以支持
                    startWithSharedElement(fragment, ((FirstHomeAdapter.VH) vh).img, getResources().getString(R.string.image_transition));
                } else {
                    // 这里如果5.0以下系统调用startWithSharedElement(),会无动画,所以低于5.0,start(fragment)
                    start(fragment);
                }
            }
        });

        // Init Datas
        List<Article> articleList = new ArrayList<>();
        for (int i = 0; i < 15; i++) {
            int index = i % 5;
            Article article = new Article(mTitles[index], mImgRes[index]);
            articleList.add(article);
        }
        mAdapter.setDatas(articleList);

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
                if (dy > 5) {
                    mFab.hide();
                } else if (dy < -5) {
                    mFab.show();
                }
            }
        });

        mFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(_mActivity, "Action", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onRefresh() {
        mRefreshLayout.postDelayed(new Runnable() {
            @Override
            public void run() {
                mRefreshLayout.setRefreshing(false);
            }
        }, 2000);
    }

    private void scrollToTop() {
        mRecy.smoothScrollToPosition(0);
    }

    /**
     * 选择tab事件
     */
    @Subscribe
    public void onTabSelectedEvent(TabSelectedEvent event) {
        if (event.position != MainActivity.FIRST) return;

        if (mInAtTop) {
            mRefreshLayout.setRefreshing(true);
            onRefresh();
        } else {
            scrollToTop();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mRecy.setAdapter(null);
        EventBus.getDefault().unregister(this);
    }
}
