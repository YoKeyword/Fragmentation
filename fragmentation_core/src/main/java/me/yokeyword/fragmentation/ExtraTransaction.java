package me.yokeyword.fragmentation;

import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.View;

import java.util.ArrayList;

import me.yokeyword.fragmentation.helper.internal.TransactionRecord;

/**
 * Created by YoKey on 16/11/24.
 */
public abstract class ExtraTransaction {

    /**
     * @param tag Optional tag name for the fragment, to later retrieve the
     *            fragment with {@link SupportHelper#findFragment(FragmentManager, String)}
     *            , pop(String)
     *            or FragmentManager.findFragmentByTag(String).
     */
    public abstract ExtraSupportTransaction setTag(String tag);

    /**
     * Used with custom Transitions to map a View from a removed or hidden
     * Fragment to a View from a shown or added Fragment.
     * <var>sharedElement</var> must have a unique transitionName in the View hierarchy.
     *
     * @param sharedElement A View in a disappearing Fragment to match with a View in an
     *                      appearing Fragment.
     * @param sharedName    The transitionName for a View in an appearing Fragment to match to the shared
     *                      element.
     * @see Fragment#setSharedElementReturnTransition(Object)
     * @see Fragment#setSharedElementEnterTransition(Object)
     */
    @RequiresApi(Build.VERSION_CODES.LOLLIPOP_MR1)
    public abstract ExtraSupportTransaction addSharedElement(View sharedElement, String sharedName);

    /**
     * Don't add this extraTransaction to the back stack.
     */
    public abstract DontAddToBackStackTransaction dontAddToBackStack();

    /**
     * 使用dontAddToBackStack() 加载Fragment时， 使用remove()移除Fragment
     */
    public abstract void remove(ISupportFragment fragment, boolean showPreFragment);

    /**
     * 使用setTag()自定义Tag时，使用下面popTo()／popToChild()出栈
     *
     * @param targetFragmentTag     通过setTag()设置的tag
     * @param includeTargetFragment 是否包含目标(Tag为targetFragmentTag)Fragment
     */
    public abstract void popTo(String targetFragmentTag, boolean includeTargetFragment);

    public abstract void popTo(String targetFragmentTag, boolean includeTargetFragment, Runnable afterPopTransactionRunnable, int popAnim);

    public abstract void popToChild(String targetFragmentTag, boolean includeTargetFragment);

    public abstract void popToChild(String targetFragmentTag, boolean includeTargetFragment, Runnable afterPopTransactionRunnable, int popAnim);

    public interface DontAddToBackStackTransaction {
        /**
         * add() +  hide(preFragment)
         */
        void start(ISupportFragment toFragment);

        /**
         * Only add()
         */
        void add(ISupportFragment toFragment);

        /**
         * replace()
         */
        void replace(ISupportFragment toFragment);
    }

    public interface ExtraSupportTransaction {
        ExtraSupportTransaction setTag(String tag);

        @RequiresApi(Build.VERSION_CODES.LOLLIPOP_MR1)
        ExtraSupportTransaction addSharedElement(View sharedElement, String sharedName);

        void start(ISupportFragment toFragment);

        void start(ISupportFragment toFragment, @ISupportFragment.LaunchMode int launchMode);

        void startForResult(ISupportFragment toFragment, int requestCode);

        void startWithPop(ISupportFragment toFragment);

        void replace(ISupportFragment toFragment);
    }

    /**
     * Impl
     */
    final static class ExtraTransactionImpl<T extends ISupportFragment> extends ExtraTransaction implements DontAddToBackStackTransaction, ExtraSupportTransaction {
        private T mSupportF;
        private Fragment mFragment;
        private TransactionDelegate mTransactionDelegate;
        private boolean mFromActivity;
        private TransactionRecord mRecord;

        ExtraTransactionImpl(T supportF, TransactionDelegate transactionDelegate, boolean fromActivity) {
            this.mSupportF = supportF;
            this.mFragment = (Fragment) supportF;
            this.mTransactionDelegate = transactionDelegate;
            this.mFromActivity = fromActivity;
            mRecord = new TransactionRecord();
        }

        @Override
        public ExtraSupportTransaction setTag(String tag) {
            mRecord.tag = tag;
            return this;
        }

        @Override
        public ExtraSupportTransaction addSharedElement(View sharedElement, String sharedName) {
            if (mRecord.sharedElementList == null) {
                mRecord.sharedElementList = new ArrayList<>();
            }
            mRecord.sharedElementList.add(new TransactionRecord.SharedElement(sharedElement, sharedName));
            return this;
        }

        @Override
        public DontAddToBackStackTransaction dontAddToBackStack() {
            mRecord.dontAddToBackStack = true;
            return this;
        }

        @Override
        public void remove(ISupportFragment fragment, boolean showPreFragment) {
            mTransactionDelegate.remove(mFragment.getFragmentManager(), (Fragment) fragment, showPreFragment);
        }

        @Override
        public void popTo(String targetFragmentTag, boolean includeTargetFragment) {
            popTo(targetFragmentTag, includeTargetFragment, null, 0);
        }

        @Override
        public void popTo(String targetFragmentTag, boolean includeTargetFragment, Runnable afterPopTransactionRunnable, int popAnim) {
            mTransactionDelegate.popTo(targetFragmentTag, includeTargetFragment, afterPopTransactionRunnable, mFragment.getFragmentManager(), popAnim);
        }

        @Override
        public void popToChild(String targetFragmentTag, boolean includeTargetFragment) {
            popToChild(targetFragmentTag, includeTargetFragment, null, 0);
        }

        @Override
        public void popToChild(String targetFragmentTag, boolean includeTargetFragment, Runnable afterPopTransactionRunnable, int popAnim) {
            if (mFromActivity) {
                popTo(targetFragmentTag, includeTargetFragment, afterPopTransactionRunnable, popAnim);
            } else {
                mTransactionDelegate.popTo(targetFragmentTag, includeTargetFragment, afterPopTransactionRunnable, mFragment.getChildFragmentManager(), popAnim);
            }
        }

        @Override
        public void add(ISupportFragment toFragment) {
            toFragment.getSupportDelegate().mTransactionRecord = mRecord;
            mTransactionDelegate.dispatchStartTransaction(mFragment.getFragmentManager(), mSupportF, toFragment, 0, ISupportFragment.STANDARD, TransactionDelegate.TYPE_ADD_WITHOUT_HIDE);
        }

        @Override
        public void start(ISupportFragment toFragment) {
            start(toFragment, ISupportFragment.STANDARD);
        }

        @Override
        public void replace(ISupportFragment toFragment) {
            toFragment.getSupportDelegate().mTransactionRecord = mRecord;
            mTransactionDelegate.dispatchStartTransaction(mFragment.getFragmentManager(), mSupportF, toFragment, 0, ISupportFragment.STANDARD, TransactionDelegate.TYPE_REPLACE);
        }

        @Override
        public void start(ISupportFragment toFragment, @ISupportFragment.LaunchMode int launchMode) {
            toFragment.getSupportDelegate().mTransactionRecord = mRecord;
            mTransactionDelegate.dispatchStartTransaction(mFragment.getFragmentManager(), mSupportF, toFragment, 0, launchMode, TransactionDelegate.TYPE_ADD);
        }

        @Override
        public void startForResult(ISupportFragment toFragment, int requestCode) {
            toFragment.getSupportDelegate().mTransactionRecord = mRecord;
            mTransactionDelegate.dispatchStartTransaction(mFragment.getFragmentManager(), mSupportF, toFragment, requestCode, ISupportFragment.STANDARD, TransactionDelegate.TYPE_ADD_RESULT);
        }

        @Override
        public void startWithPop(ISupportFragment toFragment) {
            toFragment.getSupportDelegate().mTransactionRecord = mRecord;
            mTransactionDelegate.dispatchStartTransaction(mFragment.getFragmentManager(), mSupportF, toFragment, 0, ISupportFragment.STANDARD, TransactionDelegate.TYPE_ADD_WITH_POP);
        }
    }
}
