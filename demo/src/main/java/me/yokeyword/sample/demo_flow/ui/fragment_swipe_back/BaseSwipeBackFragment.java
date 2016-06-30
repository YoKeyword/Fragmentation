package me.yokeyword.sample.demo_flow.ui.fragment_swipe_back;

import android.support.v7.widget.Toolbar;
import android.view.View;

import me.yokeyword.fragmentation_swipeback.SwipeBackFragment;
import me.yokeyword.sample.R;

/**
 * Created by YoKeyword on 16/4/21.
 */
public class BaseSwipeBackFragment extends SwipeBackFragment {

    void _initToolbar(Toolbar toolbar) {
        toolbar.setTitle("SwipeBackActivity的Fragment");
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_white_24dp);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                _mActivity.onBackPressed();
            }
        });
    }
}
