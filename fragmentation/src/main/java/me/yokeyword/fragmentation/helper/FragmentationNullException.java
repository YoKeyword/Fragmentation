package me.yokeyword.fragmentation.helper;

/**
 * Created by YoKeyword on 16/1/25.
 */
public class FragmentationNullException extends NullPointerException {
    public FragmentationNullException(String detailMessage) {
        super("Please call after onCreateView(e.g. onActivityCreated) when use " + detailMessage + " in Fragment!");
    }
}
