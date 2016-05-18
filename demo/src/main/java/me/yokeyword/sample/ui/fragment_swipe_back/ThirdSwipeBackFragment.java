package me.yokeyword.sample.ui.fragment_swipe_back;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.view.GravityCompat;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import me.yokeyword.fragmentation.SwipeBackLayout;
import me.yokeyword.sample.R;

/**
 * Created by YoKeyword on 16/4/19.
 */
public class ThirdSwipeBackFragment extends BaseSwipeBackFragment {
    private Toolbar mToolbar;

    public static ThirdSwipeBackFragment newInstance() {

        Bundle args = new Bundle();

        ThirdSwipeBackFragment fragment = new ThirdSwipeBackFragment();
        fragment.setArguments(args);
        return fragment;
    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_swipe_back_third, container, false);

        initToolbar(view);
        view.findViewById(R.id.tv_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                start(RecyclerSwipeBackFragment.newInstance());
            }
        });

        return attachToSwipeBack(view);
    }

    private void initToolbar(View view) {
        mToolbar = (Toolbar) view.findViewById(R.id.toolbar);
        _initToolbar(mToolbar);

        Button btnSet = (Button) view.findViewById(R.id.btn_set);
        btnSet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final PopupMenu popupMenu = new PopupMenu(_mActivity, v, GravityCompat.END);
                popupMenu.inflate(R.menu.swipe_orientation);
                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getItemId()) {
                            case R.id.action_left:
                                getSwipeBackLayout().setEdgeOrientation(SwipeBackLayout.EDGE_LEFT);
                                Toast.makeText(_mActivity, "left", Toast.LENGTH_SHORT).show();
                                break;
                            case R.id.action_right:
                                getSwipeBackLayout().setEdgeOrientation(SwipeBackLayout.EDGE_RIGHT);
                                Toast.makeText(_mActivity, "right", Toast.LENGTH_SHORT).show();
                                break;
                            case R.id.action_all:
                                getSwipeBackLayout().setEdgeOrientation(SwipeBackLayout.EDGE_ALL);
                                Toast.makeText(_mActivity, "all", Toast.LENGTH_SHORT).show();
                                break;
                        }
                        popupMenu.dismiss();
                        return true;
                    }
                });
                popupMenu.show();
            }
        });
    }
}
