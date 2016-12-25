package me.yokeyword.fragmentation;

import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v4.app.Fragment;
import android.view.View;

import java.util.ArrayList;

import me.yokeyword.fragmentation.helper.internal.TransactionRecord;

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
    @RequiresApi(Build.VERSION_CODES.LOLLIPOP_MR1)
    public abstract SupportTransaction addSharedElement(View sharedElement, String sharedName);

    /**
     * same as FragmentTransaction.commitAllowingStateLoss()
     * <p>
     * Allows the commit to be executed after an
     * activity's state is saved.  This is dangerous because the commit can
     * be lost if the activity needs to later be restored from its state, so
     * this should only be used for cases where it is okay for the UI state
     * to change unexpectedly on the user.
     */
    public abstract <T extends SupportFragment> T commit();

    /**
     * Add some action when calling {@link SupportFragment#start(SupportFragment)
     * or SupportActivity/SupportFragment.startXXX()}
     */
    final static class SupportTransactionImpl<T extends SupportFragment> extends SupportTransaction {
        private T mSupportFragment;
        private TransactionRecord mRecord;

        SupportTransactionImpl(T supportFragment) {
            this.mSupportFragment = supportFragment;
            mRecord = new TransactionRecord();
        }

        @Override
        public SupportTransaction setTag(String tag) {
            mRecord.tag = tag;
            return this;
        }

        @Override
        public SupportTransaction forResult(int requestCode) {
            mRecord.requestCode = requestCode;
            return this;
        }

        @Override
        public SupportTransaction setLaunchMode(@SupportFragment.LaunchMode int launchMode) {
            mRecord.launchMode = launchMode;
            return this;
        }

        @Override
        public SupportTransaction withPop(boolean with) {
            mRecord.withPop = with;
            return this;
        }

        @Override
        public SupportTransaction addSharedElement(View sharedElement, String sharedName) {
            if (mRecord.sharedElementList == null) {
                mRecord.sharedElementList = new ArrayList<>();
            }
            mRecord.sharedElementList.add(new TransactionRecord.SharedElement(sharedElement, sharedName));
            return this;
        }

        @Override
        public T commit() {
            mSupportFragment.setTransactionRecord(mRecord);
            return mSupportFragment;
        }
    }
}
