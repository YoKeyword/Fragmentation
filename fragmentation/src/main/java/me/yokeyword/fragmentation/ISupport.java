package me.yokeyword.fragmentation;


import android.app.Activity;

import me.yokeyword.fragmentation.anim.FragmentAnimator;

/**
 * Created by YoKeyword on 16/6/1.
 */
public interface ISupport {

    FragmentAnimator onCreateFragmentAnimator();

    void onBackPressedSupport();

    Activity getActivity();
}
