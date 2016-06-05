package me.yokeyword.sample.multiple.ui.fragment.second;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import me.yokeyword.sample.R;
import me.yokeyword.sample.multiple.ui.fragment.BaseLazyMainFragment;

/**
 * Created by YoKeyword on 16/6/3.
 */
public class MultiSecondFragment extends BaseLazyMainFragment {

    public static MultiSecondFragment newInstance() {

        Bundle args = new Bundle();

        MultiSecondFragment fragment = new MultiSecondFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.multi_fragment_second, container, false);
        initView(savedInstanceState);
        return view;
    }

    private void initView(Bundle savedInstanceState) {
        if (savedInstanceState == null) {
            loadRootFragment(R.id.fl_second_container, ViewPagerFragment.newInstance());
        }
    }

    @Override
    protected void initLazyView(@Nullable Bundle savedInstanceState) {
        // 这里可以不用懒加载,因为Adapter的场景下,Adapter内的子Fragment只有在父Fragment是show状态时,才会被Attach,Create
    }
}
