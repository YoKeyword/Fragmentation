package me.yokeyword.sample.common.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import me.yokeyword.sample.multiple.ui.fragment.second.childpager.FirstPagerFragment;
import me.yokeyword.sample.multiple.ui.fragment.second.childpager.OtherPagerFragment;

/**
 * Created by YoKeyword on 16/6/5.
 */
public class MultiPagerFragmentAdapter extends FragmentPagerAdapter {
    private String[] mTab = new String[]{"推荐", "热门", "收藏"};

    public MultiPagerFragmentAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {
        if (position == 0) {
            return FirstPagerFragment.newInstance();
        } else {
            return OtherPagerFragment.newInstance(position);
        }
    }

    @Override
    public int getCount() {
        return mTab.length;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return mTab[position];
    }
}
