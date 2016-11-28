package me.yokeyword.fragmentation.helper.internal;

import android.os.Bundle;

import java.util.ArrayList;

import me.yokeyword.fragmentation.SupportFragment;
import me.yokeyword.fragmentation.helper.FragmentLifecycleCallbacks;


/**
 * Hiden
 * Created by YoKey dispatch 16/11/28.
 */
public class LifecycleHelper {
    public static final int LIFECYLCE_ONHIDDENCHANGED = 1;
    public static final int LIFECYLCE_ONSETUSERVISIBLEHINT = 2;
    public static final int LIFECYLCE_ONSAVEINSTANCESTATE = 3;
    public static final int LIFECYLCE_ONENTERANIMATIONEND = 4;
    public static final int LIFECYLCE_ONLAZYINITVIEW = 5;
    public static final int LIFECYLCE_ONSUPPORTVISIBLE = 6;
    public static final int LIFECYLCE_ONSUPPORTINVISIBLE = 7;
    public static final int LIFECYLCE_ONATTACH = 8;
    public static final int LIFECYLCE_ONCREATE = 9;
//    public static final int LIFECYLCE_ONCREATEVIEW = 10;
    public static final int LIFECYLCE_ONVIEWCREATED = 11;
    public static final int LIFECYLCE_ONACTIVITYCREATED = 12;
    public static final int LIFECYLCE_ONSTART = 13;
    public static final int LIFECYLCE_ONRESUME = 14;
    public static final int LIFECYLCE_ONPAUSE = 15;
    public static final int LIFECYLCE_ONSTOP = 16;
    public static final int LIFECYLCE_ONDESTROYVIEW = 17;
    public static final int LIFECYLCE_ONDESTROY = 18;
    public static final int LIFECYLCE_ONDETACH = 19;

    private ArrayList<FragmentLifecycleCallbacks> mFragmentLifecycleCallbacks;

    public LifecycleHelper(ArrayList<FragmentLifecycleCallbacks> fragmentLifecycleCallbacks) {
        this.mFragmentLifecycleCallbacks = fragmentLifecycleCallbacks;
    }

    public void dispatchLifecycle(int lifecycle, SupportFragment fragment, Bundle bundle, boolean visible) {
        switch (lifecycle) {
            case LIFECYLCE_ONHIDDENCHANGED:
                dispatchFragmentHiddenChanged(fragment, visible);
                break;
            case LIFECYLCE_ONSETUSERVISIBLEHINT:
                dispatchFragmentSetUserVisibleHint(fragment, visible);
                break;
            case LIFECYLCE_ONSAVEINSTANCESTATE:
                dispatchFragmentSaveInstanceState(fragment, bundle);
                break;
            case LIFECYLCE_ONENTERANIMATIONEND:
                dispatchFragmentEnterAnimatidispatchEnd(fragment, bundle);
                break;
            case LIFECYLCE_ONLAZYINITVIEW:
                dispatchFragmentLazyInitView(fragment, bundle);
                break;
            case LIFECYLCE_ONSUPPORTVISIBLE:
                dispatchFragmentSupportVisible(fragment);
                break;
            case LIFECYLCE_ONSUPPORTINVISIBLE:
                dispatchFragmentSupportInvisible(fragment);
                break;
            case LIFECYLCE_ONATTACH:
                dispatchFragmentAttached(fragment);
                break;
            case LIFECYLCE_ONCREATE:
                dispatchFragmentCreated(fragment, bundle);
                break;
//            case LIFECYLCE_ONCREATEVIEW:
//                dispatchFragmentCreateView(fragment, bundle);
//                break;
            case LIFECYLCE_ONVIEWCREATED:
                dispatchFragmentViewCreated(fragment, bundle);
                break;
            case LIFECYLCE_ONACTIVITYCREATED:
                dispatchFragmentActivityCreated(fragment, bundle);
                break;
            case LIFECYLCE_ONSTART:
                dispatchFragmentStarted(fragment);
                break;
            case LIFECYLCE_ONRESUME:
                dispatchFragmentResumed(fragment);
                break;
            case LIFECYLCE_ONPAUSE:
                dispatchFragmentPaused(fragment);
                break;
            case LIFECYLCE_ONSTOP:
                dispatchFragmentStopped(fragment);
                break;
            case LIFECYLCE_ONDESTROYVIEW:
                dispatchFragmentDestroyView(fragment);
                break;
            case LIFECYLCE_ONDESTROY:
                dispatchFragmentDestroyed(fragment);
                break;
            case LIFECYLCE_ONDETACH:
                dispatchFragmentDetached(fragment);
                break;
        }
    }

    private void dispatchFragmentHiddenChanged(SupportFragment fragment, boolean hidden) {
        for (int i = 0; i < mFragmentLifecycleCallbacks.size(); i++) {
            mFragmentLifecycleCallbacks.get(i).onFragmentHiddenChanged(fragment, hidden);
        }
    }

    private void dispatchFragmentSetUserVisibleHint(SupportFragment fragment, boolean isVisibleToUser) {
        for (int i = 0; i < mFragmentLifecycleCallbacks.size(); i++) {
            mFragmentLifecycleCallbacks.get(i).onFragmentSetUserVisibleHint(fragment, isVisibleToUser);
        }
    }

    private void dispatchFragmentSaveInstanceState(SupportFragment fragment, Bundle outState) {
        for (int i = 0; i < mFragmentLifecycleCallbacks.size(); i++) {
            mFragmentLifecycleCallbacks.get(i).onFragmentSaveInstanceState(fragment, outState);
        }
    }

    private void dispatchFragmentEnterAnimatidispatchEnd(SupportFragment fragment, Bundle savedInstanceState) {
        for (int i = 0; i < mFragmentLifecycleCallbacks.size(); i++) {
            mFragmentLifecycleCallbacks.get(i).onFragmentEnterAnimationEnd(fragment, savedInstanceState);
        }
    }

    private void dispatchFragmentLazyInitView(SupportFragment fragment, Bundle savedInstanceState) {
        for (int i = 0; i < mFragmentLifecycleCallbacks.size(); i++) {
            mFragmentLifecycleCallbacks.get(i).onFragmentLazyInitView(fragment, savedInstanceState);
        }
    }

    private void dispatchFragmentSupportVisible(SupportFragment fragment) {
        for (int i = 0; i < mFragmentLifecycleCallbacks.size(); i++) {
            mFragmentLifecycleCallbacks.get(i).onFragmentSupportVisible(fragment);
        }
    }

    private void dispatchFragmentSupportInvisible(SupportFragment fragment) {
        for (int i = 0; i < mFragmentLifecycleCallbacks.size(); i++) {
            mFragmentLifecycleCallbacks.get(i).onFragmentSupportInvisible(fragment);
        }
    }

    private void dispatchFragmentAttached(SupportFragment fragment) {
        for (int i = 0; i < mFragmentLifecycleCallbacks.size(); i++) {
            mFragmentLifecycleCallbacks.get(i).onFragmentAttached(fragment);
        }
    }

    private void dispatchFragmentCreated(SupportFragment fragment, Bundle savedInstanceState) {
        for (int i = 0; i < mFragmentLifecycleCallbacks.size(); i++) {
            mFragmentLifecycleCallbacks.get(i).onFragmentCreated(fragment, savedInstanceState);
        }
    }

//    private void dispatchFragmentCreateView(SupportFragment fragment, Bundle savedInstanceState) {
//        for (int i = 0; i < mFragmentLifecycleCallbacks.size(); i++) {
//            mFragmentLifecycleCallbacks.get(i).onFragmentCreateView(fragment, savedInstanceState);
//        }
//    }

    private void dispatchFragmentViewCreated(SupportFragment fragment, Bundle savedInstanceState) {
        for (int i = 0; i < mFragmentLifecycleCallbacks.size(); i++) {
            mFragmentLifecycleCallbacks.get(i).onFragmentViewCreated(fragment, savedInstanceState);
        }
    }

    private void dispatchFragmentActivityCreated(SupportFragment fragment, Bundle savedInstanceState) {
        for (int i = 0; i < mFragmentLifecycleCallbacks.size(); i++) {
            mFragmentLifecycleCallbacks.get(i).onFragmentActivityCreated(fragment, savedInstanceState);
        }
    }

    private void dispatchFragmentStarted(SupportFragment fragment) {
        for (int i = 0; i < mFragmentLifecycleCallbacks.size(); i++) {
            mFragmentLifecycleCallbacks.get(i).onFragmentStarted(fragment);
        }
    }

    private void dispatchFragmentResumed(SupportFragment fragment) {
        for (int i = 0; i < mFragmentLifecycleCallbacks.size(); i++) {
            mFragmentLifecycleCallbacks.get(i).onFragmentResumed(fragment);
        }
    }

    private void dispatchFragmentPaused(SupportFragment fragment) {
        for (int i = 0; i < mFragmentLifecycleCallbacks.size(); i++) {
            mFragmentLifecycleCallbacks.get(i).onFragmentPaused(fragment);
        }
    }

    private void dispatchFragmentStopped(SupportFragment fragment) {
        for (int i = 0; i < mFragmentLifecycleCallbacks.size(); i++) {
            mFragmentLifecycleCallbacks.get(i).onFragmentStopped(fragment);
        }
    }

    private void dispatchFragmentDestroyView(SupportFragment fragment) {
        for (int i = 0; i < mFragmentLifecycleCallbacks.size(); i++) {
            mFragmentLifecycleCallbacks.get(i).onFragmentDestroyView(fragment);
        }
    }

    private void dispatchFragmentDestroyed(SupportFragment fragment) {
        for (int i = 0; i < mFragmentLifecycleCallbacks.size(); i++) {
            mFragmentLifecycleCallbacks.get(i).onFragmentDestroyed(fragment);
        }
    }

    private void dispatchFragmentDetached(SupportFragment fragment) {
        for (int i = 0; i < mFragmentLifecycleCallbacks.size(); i++) {
            mFragmentLifecycleCallbacks.get(i).onFragmentDetached(fragment);
        }
    }
}
