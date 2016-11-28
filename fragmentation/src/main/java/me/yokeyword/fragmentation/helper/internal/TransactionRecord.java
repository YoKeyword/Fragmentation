package me.yokeyword.fragmentation.helper.internal;

import android.view.View;

/**
 * Hide
 * Created by YoKey on 16/11/25.
 */
public final class TransactionRecord {
    public static final int COMMIT = 0;
    public static final int COMMIT_ALLOWING_STATE_LOSS = 1;
    public static final int COMMIT_IMMEDIATE = 2;

    public String tag;
    public Integer requestCode;
    public Integer launchMode;
    public Boolean withPop;
    public SharedElement sharedElement;
    public Integer commitMode;

    public static class SharedElement {
        public View sharedElement;
        public String sharedName;

        public SharedElement(View sharedElement, String sharedName) {
            this.sharedElement = sharedElement;
            this.sharedName = sharedName;
        }
    }
}
