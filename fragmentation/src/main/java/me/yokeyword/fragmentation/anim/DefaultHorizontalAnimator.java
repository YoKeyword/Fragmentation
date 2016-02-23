package me.yokeyword.fragmentation.anim;

import me.yokeyword.fragmentation.R;

/**
 * Created by YoKeyword on 16/2/5.
 */
public class DefaultHorizontalAnimator extends FragmentAnimator {

    public DefaultHorizontalAnimator() {
        enter = R.anim.h_fragment_enter;
        exit = R.anim.h_fragment_exit;
        popEnter = R.anim.h_fragment_pop_enter;
        popExit = R.anim.h_fragment_pop_exit;
    }
}
