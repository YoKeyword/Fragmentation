package me.yokeyword.fragmentation;

import android.view.View;

import me.yokeyword.fragmentation.helper.internal.TransactionRecord;

/**
 * Add some action when calling {@link SupportFragment#start(SupportFragment)
 * or SupportActivity/SupportFragment.startXXX()}
 * <p>
 * Created by YoKey on 16/11/24.
 */
final class SupportTransactionImpl<T extends SupportFragment> extends SupportTransaction {
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
        mRecord.sharedElement = new TransactionRecord.SharedElement(sharedElement, sharedName);
        return this;
    }

    @Override
    public T commit() {
        mRecord.commitMode = TransactionRecord.COMMIT;
        mSupportFragment.setTransactionRecord(mRecord);
        return mSupportFragment;
    }

    @Override
    public T commitAllowingStateLoss() {
        mRecord.commitMode = TransactionRecord.COMMIT_ALLOWING_STATE_LOSS;
        mSupportFragment.setTransactionRecord(mRecord);
        return mSupportFragment;
    }

    @Override
    public T commitImmediate() {
        mRecord.commitMode = TransactionRecord.COMMIT_IMMEDIATE;
        mSupportFragment.setTransactionRecord(mRecord);
        return mSupportFragment;
    }
}