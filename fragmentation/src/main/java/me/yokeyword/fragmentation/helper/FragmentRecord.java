package me.yokeyword.fragmentation.helper;

import java.util.List;

/**
 * 为了调试时 查看栈视图
 * Created by YoKeyword on 16/2/21.
 */
public class FragmentRecord {
    public String fragmentName;
    public List<FragmentRecord> childFragmentRecord;

    public FragmentRecord(String fragmentName, List<FragmentRecord> childFragmentRecord) {
        this.fragmentName = fragmentName;
        this.childFragmentRecord = childFragmentRecord;
    }
}
