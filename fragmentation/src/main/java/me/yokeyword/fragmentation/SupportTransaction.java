package me.yokeyword.fragmentation;

import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
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
     *            fragment with {@link SupportManager#findFragment(FragmentManager, String)}
     *            , SupportFragment.pop(String)
     *            or FragmentManager.findFragmentByTag(String).
     * @return the same SupportTransaction instance.
     */
    public abstract SupportTransaction setTag(String tag);

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
     * Add this transaction to the back stack.  This means that the transaction
     * will be remembered after it is committed, and will reverse its operation
     * when later popped off the stack.
     *
     * @param add default: true
     * @return the same SupportTransaction instance.
     */
    public abstract SupportTransaction addToBackStack(boolean add);

    public abstract void loadRootFragment(int container, SupportFragment fragment);

    public abstract void replaceLoadRootFragment(int container, SupportFragment fragment);

    public abstract void start(SupportFragment toFragment);

    public abstract void start(final SupportFragment toFragment, @SupportFragment.LaunchMode int launchMode);

    public abstract void startForResult(SupportFragment toFragment, int requestCode);

    public abstract void startWithPop(SupportFragment toFragment);

    public abstract void replace(SupportFragment toFragment);

    public abstract void replace(SupportFragment toFragment, @SupportFragment.LaunchMode int launchMode);

    public abstract void replaceForResult(SupportFragment toFragment, int requestCode);

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
        public SupportTransaction addSharedElement(View sharedElement, String sharedName) {
            if (mRecord.sharedElementList == null) {
                mRecord.sharedElementList = new ArrayList<>();
            }
            mRecord.sharedElementList.add(new TransactionRecord.SharedElement(sharedElement, sharedName));
            return this;
        }

        @Override
        public SupportTransaction addToBackStack(boolean add) {
            mRecord.addToBackStack = add;
            return this;
        }

        @Override
        public void loadRootFragment(int container, SupportFragment toFragment) {
            toFragment.setTransactionRecord(mRecord);
            mSupportFragment.loadRootFragment(container, toFragment);
        }

        @Override
        public void replaceLoadRootFragment(int container, SupportFragment toFragment) {
            toFragment.setTransactionRecord(mRecord);
            mSupportFragment.replaceLoadRootFragment(container, toFragment);
        }

        @Override
        public void start(SupportFragment toFragment) {
            toFragment.setTransactionRecord(mRecord);
            mSupportFragment.start(toFragment);
        }

        @Override
        public void start(SupportFragment toFragment, @SupportFragment.LaunchMode int launchMode) {
            toFragment.setTransactionRecord(mRecord);
            mSupportFragment.start(toFragment, launchMode);
        }

        @Override
        public void startForResult(SupportFragment toFragment, int requestCode) {
            toFragment.setTransactionRecord(mRecord);
            mSupportFragment.startForResult(toFragment, requestCode);
        }

        @Override
        public void startWithPop(SupportFragment toFragment) {
            toFragment.setTransactionRecord(mRecord);
            mSupportFragment.startWithPop(toFragment);
        }

        @Override
        public void replace(SupportFragment toFragment) {
            toFragment.setTransactionRecord(mRecord);
            mSupportFragment.replace(toFragment);
        }

        @Override
        public void replace(SupportFragment toFragment, @SupportFragment.LaunchMode int launchMode) {
            toFragment.setTransactionRecord(mRecord);
            mSupportFragment.replace(toFragment, launchMode);
        }

        @Override
        public void replaceForResult(SupportFragment toFragment, int requestCode) {
            toFragment.setTransactionRecord(mRecord);
            mSupportFragment.replaceForResult(toFragment, requestCode);
        }
    }
}
