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
public abstract class ExtraTransaction {

    /**
     * @param tag Optional tag name for the fragment, to later retrieve the
     *            fragment with {@link SupportHelper#findFragment(FragmentManager, String)}
     *            , SupportFragment.pop(String)
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
    public abstract void remove(SupportFragment fragment);

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
        void start(SupportFragment toFragment);

        /**
         * Only add()
         */
        void add(SupportFragment toFragment);

        /**
         * replace()
         */
        void replace(SupportFragment toFragment);
    }

    public interface ExtraSupportTransaction {
        ExtraSupportTransaction setTag(String tag);

        @RequiresApi(Build.VERSION_CODES.LOLLIPOP_MR1)
        ExtraSupportTransaction addSharedElement(View sharedElement, String sharedName);

        void start(SupportFragment toFragment);

        void start(final SupportFragment toFragment, @SupportFragment.LaunchMode int launchMode);

        void startForResult(SupportFragment toFragment, int requestCode);

        void startWithPop(SupportFragment toFragment);

        void replace(SupportFragment toFragment);
    }

    /**
     * Add some action when calling {@link SupportFragment#start(SupportFragment)
     * or SupportActivity/SupportFragment.startXXX()}
     */
    final static class ExtraTransactionImpl<T extends SupportFragment> extends ExtraTransaction implements DontAddToBackStackTransaction, ExtraSupportTransaction {
        private T mSupportFragment;
        private TransactionDelegate mTransactionDelegate;
        private boolean mFromActivity;
        private TransactionRecord mRecord;

        ExtraTransactionImpl(T supportFragment, TransactionDelegate transactionDelegate, boolean fromActivity) {
            this.mSupportFragment = supportFragment;
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
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP) {
                if (mRecord.sharedElementList == null) {
                    mRecord.sharedElementList = new ArrayList<>();
                }
                mRecord.sharedElementList.add(new TransactionRecord.SharedElement(sharedElement, sharedName));
            }
            return this;
        }

        @Override
        public DontAddToBackStackTransaction dontAddToBackStack() {
            mRecord.dontAddToBackStack = true;
            return this;
        }

        @Override
        public void remove(SupportFragment fragment) {
            mTransactionDelegate.remove(mSupportFragment.getFragmentManager(), fragment);
        }

        @Override
        public void popTo(String targetFragmentTag, boolean includeTargetFragment) {
            popTo(targetFragmentTag, includeTargetFragment, null, 0);
        }

        @Override
        public void popTo(String targetFragmentTag, boolean includeTargetFragment, Runnable afterPopTransactionRunnable, int popAnim) {
            mTransactionDelegate.popTo(targetFragmentTag, includeTargetFragment, afterPopTransactionRunnable, mSupportFragment.getFragmentManager(), popAnim);
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
                mTransactionDelegate.popTo(targetFragmentTag, includeTargetFragment, afterPopTransactionRunnable, mSupportFragment.getChildFragmentManager(), popAnim);
            }
        }

        @Override
        public void add(SupportFragment toFragment) {
            toFragment.setTransactionRecord(mRecord);
            mTransactionDelegate.dispatchStartTransaction(mSupportFragment.getFragmentManager(), mSupportFragment, toFragment, 0, SupportFragment.STANDARD, TransactionDelegate.TYPE_ADD_WITHOUT_HIDE);
        }

        @Override
        public void start(SupportFragment toFragment) {
            start(toFragment, SupportFragment.STANDARD);
        }

        @Override
        public void replace(SupportFragment toFragment) {
            toFragment.setTransactionRecord(mRecord);
            mTransactionDelegate.dispatchStartTransaction(mSupportFragment.getFragmentManager(), mSupportFragment, toFragment, 0, SupportFragment.STANDARD, TransactionDelegate.TYPE_REPLACE);
        }

        @Override
        public void start(SupportFragment toFragment, @SupportFragment.LaunchMode int launchMode) {
            toFragment.setTransactionRecord(mRecord);
            mTransactionDelegate.dispatchStartTransaction(mSupportFragment.getFragmentManager(), mSupportFragment, toFragment, 0, launchMode, TransactionDelegate.TYPE_ADD);
        }

        @Override
        public void startForResult(SupportFragment toFragment, int requestCode) {
            toFragment.setTransactionRecord(mRecord);
            mTransactionDelegate.dispatchStartTransaction(mSupportFragment.getFragmentManager(), mSupportFragment, toFragment, requestCode, SupportFragment.STANDARD, TransactionDelegate.TYPE_ADD_RESULT);
        }

        @Override
        public void startWithPop(SupportFragment toFragment) {
            toFragment.setTransactionRecord(mRecord);
            mTransactionDelegate.dispatchStartTransaction(mSupportFragment.getFragmentManager(), mSupportFragment, toFragment, 0, SupportFragment.STANDARD, TransactionDelegate.TYPE_ADD_WITH_POP);
        }
    }
}
