package me.yokeyword.fragmentation.helper;

import android.os.Bundle;

import me.yokeyword.fragmentation.SupportFragment;

/**
 * Created by YoKey on 16/11/28.
 */
public class FragmentLifecycleCallbacks {

    /**
     * Called when the Fragment is called onHiddenChanged().
     */
    public void onFragmentHiddenChanged(SupportFragment fragment, boolean hidden) {

    }

    /**
     * Called when the Fragment is called setUserVisibleHint().
     */
    public void onFragmentSetUserVisibleHint(SupportFragment fragment, boolean isVisibleToUser) {

    }

    /**
     * Called when the Fragment is called onSaveInstanceState().
     */
    public void onFragmentSaveInstanceState(SupportFragment fragment, Bundle outState) {

    }

    /**
     * Called when the Fragment is called onEnterAnimationEnd().
     */
    public void onFragmentEnterAnimationEnd(SupportFragment fragment, Bundle savedInstanceState) {

    }

    /**
     * Called when the Fragment is called onLazyInitView().
     */
    public void onFragmentLazyInitView(SupportFragment fragment, Bundle savedInstanceState) {

    }

    /**
     * Called when the Fragment is called onSupportVisible().
     */
    public void onFragmentSupportVisible(SupportFragment fragment) {

    }

    /**
     * Called when the Fragment is called onSupportInvisible().
     */
    public void onFragmentSupportInvisible(SupportFragment fragment) {

    }

    /**
     * Called when the Fragment is called onAttach().
     */
    public void onFragmentAttached(SupportFragment fragment) {

    }

    /**
     * Called when the Fragment is called onCreate().
     */
    public void onFragmentCreated(SupportFragment fragment, Bundle savedInstanceState) {

    }

    // 因为我们一般会移除super.onCreateView()来复写 onCreateView()  所以这里一般是捕捉不到onFragmentCreateView
//    /**
//     * Called when the Fragment is called onCreateView().
//     */
//    public void onFragmentCreateView(SupportFragment fragment, Bundle savedInstanceState) {
//
//    }

    /**
     * Called when the Fragment is called onCreate().
     */
    public void onFragmentViewCreated(SupportFragment fragment, Bundle savedInstanceState) {

    }

    /**
     * Called when the Fragment is called onActivityCreated().
     */
    public void onFragmentActivityCreated(SupportFragment fragment, Bundle savedInstanceState) {

    }

    /**
     * Called when the Fragment is called onStart().
     */
    public void onFragmentStarted(SupportFragment fragment) {

    }

    /**
     * Called when the Fragment is called onResume().
     */
    public void onFragmentResumed(SupportFragment fragment) {

    }

    /**
     * Called when the Fragment is called onPause().
     */
    public void onFragmentPaused(SupportFragment fragment) {

    }

    /**
     * Called when the Fragment is called onStop().
     */
    public void onFragmentStopped(SupportFragment fragment) {

    }

    /**
     * Called when the Fragment is called onDestroyView().
     */
    public void onFragmentDestroyView(SupportFragment fragment) {

    }

    /**
     * Called when the Fragment is called onDestroy().
     */
    public void onFragmentDestroyed(SupportFragment fragment) {

    }

    /**
     * Called when the Fragment is called onDetach().
     */
    public void onFragmentDetached(SupportFragment fragment) {

    }
}
