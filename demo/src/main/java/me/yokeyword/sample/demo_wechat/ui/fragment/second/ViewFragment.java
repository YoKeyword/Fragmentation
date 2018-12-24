package me.yokeyword.sample.demo_wechat.ui.fragment.second;

import android.os.Bundle;
import androidx.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import me.yokeyword.fragmentation.SupportFragment;
import me.yokeyword.sample.R;

/**
 * Created by YoKey on 17/8/1.
 */

public class ViewFragment extends SupportFragment {

    public static ViewFragment newInstance() {

        Bundle args = new Bundle();

        ViewFragment fragment = new ViewFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.wechat_fragment_view, container, false);
        return view;
    }
}
