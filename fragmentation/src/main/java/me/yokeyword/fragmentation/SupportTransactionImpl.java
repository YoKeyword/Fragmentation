package me.yokeyword.fragmentation;

import android.view.View;

/**
 * Add some action when calling {@link SupportFragment#start(SupportFragment)
 * or SupportActivity/SupportFragment.startXXX()}
 * <p>
 * Created by YoKey on 16/11/24.
 */
final class SupportTransactionImpl extends SupportTransaction {

    @Override
    public SupportTransaction setTag(String tag) {
        return null;
    }

    @Override
    public SupportTransaction setCommitMode(@CommitMode int commitMode) {
        return null;
    }

    @Override
    public SupportTransaction setLaunchMode(@SupportFragment.LaunchMode int launchMode) {
        return null;
    }

    @Override
    public SupportTransaction withPop(boolean with) {
        return null;
    }

    @Override
    public SupportTransaction addSharedElement(View sharedElement, String sharedName) {
        return null;
    }
}