package me.yokeyword.fragmentation.helper;

import java.util.List;

/**
 * 查看栈视图 的实体类
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
