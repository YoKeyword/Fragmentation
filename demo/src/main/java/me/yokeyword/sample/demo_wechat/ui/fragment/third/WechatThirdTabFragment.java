package me.yokeyword.sample.demo_wechat.ui.fragment.third;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.List;

import me.yokeyword.sample.R;
import me.yokeyword.sample.demo_wechat.adapter.HomeAdapter;
import me.yokeyword.sample.demo_wechat.base.BaseMainFragment;
import me.yokeyword.sample.demo_wechat.entity.Article;
import me.yokeyword.sample.demo_wechat.event.StartBrotherEvent;
import me.yokeyword.sample.demo_wechat.listener.OnItemClickListener;

/**
 * Created by YoKeyword on 16/6/30.
 */
public class WechatThirdTabFragment extends BaseMainFragment {
    private RecyclerView mRecy;
    private Toolbar mToolbar;

    private HomeAdapter mAdapter;

    private boolean mAtTop = true;

    private int mScrollTotal;

    private String[] mTitles = new String[]{
            "航拍“摩托大军”返乡高峰 如蚂蚁搬家（组图）",
            "苹果因漏电召回部分电源插头",
            "IS宣称对叙利亚爆炸案负责"
    };

    private String[] mContents = new String[]{
            "1月30日，距离春节还有不到十天，“摩托大军”返乡高峰到来。航拍广西梧州市东出口服务站附近的骑行返乡人员，如同蚂蚁搬家一般。",
            "昨天记者了解到，苹果公司在其官网发出交流电源插头转换器更换计划，召回部分可能存在漏电风险的电源插头。",
            "极端组织“伊斯兰国”31日在社交媒体上宣称，该组织制造了当天在叙利亚首都大马士革发生的连环爆炸案。"
    };

    public static WechatThirdTabFragment newInstance() {

        Bundle args = new Bundle();

        WechatThirdTabFragment fragment = new WechatThirdTabFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.wechat_fragment_tab_third, container, false);
        initView(view);
        return view;
    }

    private void initView(View view) {
        mRecy = (RecyclerView) view.findViewById(R.id.recy);
        mToolbar = (Toolbar) view.findViewById(R.id.toolbar);

        mToolbar.setTitle("发现");
        initToolbarMenu(mToolbar);
    }


    @Override
    public void onLazyInitView(@Nullable Bundle savedInstanceState) {
        super.onLazyInitView(savedInstanceState);
        mAdapter = new HomeAdapter(_mActivity);
        LinearLayoutManager manager = new LinearLayoutManager(_mActivity);
        mRecy.setLayoutManager(manager);
        mRecy.setAdapter(mAdapter);

        mAdapter.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(int position, View view, RecyclerView.ViewHolder vh) {
                // 通知MainActivity跳转至DetailFragment
                EventBus.getDefault().post(new StartBrotherEvent(DetailFragment.newInstance(mAdapter.getItem(position).getTitle())));
            }
        });

        // Init Datas
        List<Article> articleList = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            Article article = new Article(mTitles[i], mContents[i]);
            articleList.add(article);
        }
        mAdapter.setDatas(articleList);

        mRecy.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                mScrollTotal += dy;
                if (mScrollTotal <= 0) {
                    mAtTop = true;
                } else {
                    mAtTop = false;
                }
            }
        });
    }
}
