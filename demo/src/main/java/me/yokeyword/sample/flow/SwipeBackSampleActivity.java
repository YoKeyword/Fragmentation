package me.yokeyword.sample.flow;

import android.os.Bundle;

import me.yokeyword.fragmentation.SwipeBackLayout;
import me.yokeyword.fragmentation.anim.DefaultHorizontalAnimator;
import me.yokeyword.fragmentation.anim.FragmentAnimator;
import me.yokeyword.fragmentation_swipeback.SwipeBackActivity;
import me.yokeyword.sample.R;
import me.yokeyword.sample.flow.ui.fragment_swipe_back.FirstSwipeBackFragment;

/**
 * Created by YoKeyword on 16/4/19.
 */
public class SwipeBackSampleActivity extends SwipeBackActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_swipe_back);

        if (savedInstanceState == null) {
            loadRootFragment(R.id.fl_container, FirstSwipeBackFragment.newInstance());
        }

        getSwipeBackLayout().setEdgeOrientation(SwipeBackLayout.EDGE_ALL);
    }

    public FragmentAnimator onCreateFragmentAnimator() {
        return new DefaultHorizontalAnimator();
    }
}
