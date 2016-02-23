package me.yokeyword.sample.ui.fragment.home;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import me.yokeyword.sample.R;
import me.yokeyword.sample.ui.BaseBackFragment;

/**
 * Created by YoKeyword on 16/2/3.
 */
public class DetailFragment extends BaseBackFragment {
    public static final String TAG = DetailFragment.class.getSimpleName();
    private static final int REQ_MODIFY_FRAGMENT = 100;
    private static final String ARG_TITLE = "arg_title";

    private CollapsingToolbarLayout mToolbarLayout;
    private Toolbar mToolbar;
    private String mTitle;

    public static DetailFragment newInstance(String title) {
        DetailFragment fragment = new DetailFragment();
        Bundle bundle = new Bundle();
        bundle.putString(ARG_TITLE, title);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle bundle = getArguments();
        if (bundle != null) {
            mTitle = bundle.getString(ARG_TITLE);
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_detail, container, false);

        initView(view);

        return view;
    }

    private void initView(View view) {
        mToolbarLayout = (CollapsingToolbarLayout) view.findViewById(R.id.toolbar_layout);
        mToolbar = (Toolbar) view.findViewById(R.id.toolbar);
        FloatingActionButton fab = (FloatingActionButton) view.findViewById(R.id.fab);

        mToolbarLayout.setCollapsedTitleTextAppearance(R.style.Toolbar_TextAppearance_White);
        mToolbarLayout.setExpandedTitleTextAppearance(R.style.Toolbar_TextAppearance_Expanded);
        mToolbarLayout.setTitle(mTitle);
        initToolbarNav(mToolbar);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startForResult(ModifyDetailFragment.newInstance(mTitle), REQ_MODIFY_FRAGMENT);
            }
        });
    }

    @Override
    public void onFragmentResult(int requestCode, int resultCode, Bundle data) {
        super.onFragmentResult(requestCode, resultCode, data);
        if (requestCode == REQ_MODIFY_FRAGMENT && resultCode == RESULT_OK && data != null) {
            mTitle = data.getString("title");
            mToolbarLayout.setTitle(mTitle);
            Toast.makeText(_mActivity, "修改标题成功!", Toast.LENGTH_SHORT).show();
        }
    }
}
