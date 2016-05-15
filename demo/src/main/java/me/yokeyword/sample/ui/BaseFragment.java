package me.yokeyword.sample.ui;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;


import me.yokeyword.fragmentation.SupportFragment;
import me.yokeyword.sample.R;

/**
 * Created by YoKeyword on 16/2/3.
 */
public class BaseFragment extends SupportFragment {
    private static final String TAG = "Fragmentation";

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    protected void initToolbarMenu(Toolbar toolbar) {
        toolbar.inflateMenu(R.menu.hierarchy);
        toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.action_hierarchy:
                        _mActivity.showFragmentStackHierarchyView();
                        _mActivity.logFragmentStackHierarchy(TAG);
                        break;
                }
                return true;
            }
        });
    }
}
