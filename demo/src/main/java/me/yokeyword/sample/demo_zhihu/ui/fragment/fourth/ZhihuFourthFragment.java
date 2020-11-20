package me.yokeyword.sample.demo_zhihu.ui.fragment.fourth;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;

import me.yokeyword.sample.R;
import me.yokeyword.sample.demo_zhihu.base.BaseMainFragment;
import me.yokeyword.sample.demo_zhihu.ui.fragment.fourth.child.AvatarFragment;
import me.yokeyword.sample.demo_zhihu.ui.fragment.fourth.child.MeFragment;

/**
 * Created by YoKeyword on 16/6/3.
 */
public class ZhihuFourthFragment extends BaseMainFragment {
    private Toolbar mToolbar;
    private View mView;

    public static ZhihuFourthFragment newInstance() {

        Bundle args = new Bundle();

        ZhihuFourthFragment fragment = new ZhihuFourthFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.zhihu_fragment_fourth, container, false);
        return mView;
    }

    @Override
    public void onLazyInitView(@Nullable Bundle savedInstanceState) {
        super.onLazyInitView(savedInstanceState);
        if (findChildFragment(AvatarFragment.class) == null) {
            loadFragment();
        }

        mToolbar = (Toolbar) mView.findViewById(R.id.toolbar);
        mToolbar.setTitle(R.string.me);
    }

    private void loadFragment() {
        loadRootFragment(R.id.fl_fourth_container_upper, AvatarFragment.newInstance());
        loadRootFragment(R.id.fl_fourth_container_lower, MeFragment.newInstance());
    }

    public void onBackToFirstFragment() {
        _mBackToFirstListener.onBackToFirstFragment();
    }
}
