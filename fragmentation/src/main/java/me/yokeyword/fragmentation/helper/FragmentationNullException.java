package me.yokeyword.fragmentation.helper;

/**
 * Created by YoKeyword on 16/1/25.
 */
public class FragmentationNullException extends NullPointerException {
    public FragmentationNullException() {
    }

    public FragmentationNullException(String detailMessage) {
        super(detailMessage + " must after onCreateView(in onActivityCreated)!");
    }
}
