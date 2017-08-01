package me.yokeyword.fragmentation.helper.internal;

import android.view.View;

import java.util.ArrayList;

/**
 * @hide Created by YoKey on 16/11/25.
 */
public final class TransactionRecord {
    public String tag;
    public int targetFragmentEnter = Integer.MIN_VALUE;
    public int currentFragmentPopExit = Integer.MIN_VALUE;
    public int currentFragmentPopEnter = Integer.MIN_VALUE;
    public int targetFragmentExit = Integer.MIN_VALUE;
    public boolean dontAddToBackStack = false;
    public ArrayList<SharedElement> sharedElementList;

    public static class SharedElement {
        public View sharedElement;
        public String sharedName;

        public SharedElement(View sharedElement, String sharedName) {
            this.sharedElement = sharedElement;
            this.sharedName = sharedName;
        }
    }
}
