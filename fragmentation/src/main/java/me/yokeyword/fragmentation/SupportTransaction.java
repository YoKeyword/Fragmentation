package me.yokeyword.fragmentation;

import android.support.v4.app.Fragment;
import android.view.View;

/**
 * Add some action when calling {@link SupportFragment#start(SupportFragment)
 * or SupportActivity/SupportFragment.startXXX()}
 * <p>
 * Created by YoKey on 16/11/24.
 */
public abstract class SupportTransaction {

    /**
     * @param tag Optional tag name for the fragment, to later retrieve the
     *            fragment with {@link SupportFragment#findFragment(String)}
     *            , SupportFragment.pop(String)
     *            or FragmentManager.findFragmentByTag(String).
     * @return the same SupportTransaction instance.
     */
    public abstract SupportTransaction setTag(String tag);

    /**
     * start a SupportFragment for which you would like a result when it exits.
     *
     * @param requestCode If >= 0, this code will be returned in
     *                    onFragmentResult() when the fragment exits.
     * @return the same SupportTransaction instance.
     */
    public abstract SupportTransaction forResult(int requestCode);

    /**
     * @param launchMode Can replace {@link SupportFragment#start(SupportFragment, int)}
     *                   <p>
     *                   May be one of {@link SupportFragment#STANDARD}, {@link SupportFragment#SINGLETASK}
     *                   or {@link SupportFragment#SINGLETOP}.
     * @return the same SupportTransaction instance.
     */
    public abstract SupportTransaction setLaunchMode(@SupportFragment.LaunchMode int launchMode);

    /**
     * Can replace {@link SupportFragment#startWithPop(SupportFragment)}
     *
     * @param with return true if you need pop currentFragment
     * @return the same SupportTransaction instance.
     */
    public abstract SupportTransaction withPop(boolean with);

    /**
     * Can replace {@link SupportFragment#startWithSharedElement(SupportFragment, View, String)}
     * <p>
     * Used with custom Transitions to map a View from a removed or hidden
     * Fragment to a View from a shown or added Fragment.
     * <var>sharedElement</var> must have a unique transitionName in the View hierarchy.
     *
     * @param sharedElement A View in a disappearing Fragment to match with a View in an
     *                      appearing Fragment.
     * @param sharedName    The transitionName for a View in an appearing Fragment to match to the shared
     *                      element.
     * @return the same SupportTransaction instance.
     * @see Fragment#setSharedElementReturnTransition(Object)
     * @see Fragment#setSharedElementEnterTransition(Object)
     */
    public abstract SupportTransaction addSharedElement(View sharedElement, String sharedName);

    /**
     * same as FragmentTransaction.commit()
     * <p>
     * Schedules a commit of this transaction.
     */
    public abstract <T extends SupportFragment> T commit();

    /**
     * Allows the commit to be executed after an
     * activity's state is saved.  This is dangerous because the commit can
     * be lost if the activity needs to later be restored from its state, so
     * this should only be used for cases where it is okay for the UI state
     * to change unexpectedly on the user.
     */
    public abstract <T extends SupportFragment> T commitAllowingStateLoss();

    /**
     * commit() + executePendingTransactions()
     * <p>
     * it is scheduled to be executed asynchronously on the process's main thread.
     */
    public abstract <T extends SupportFragment> T commitImmediate();
}
