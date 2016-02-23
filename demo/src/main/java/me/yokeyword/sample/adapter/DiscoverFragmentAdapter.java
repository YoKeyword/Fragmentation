package me.yokeyword.sample.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import me.yokeyword.sample.ui.fragment.discover.PagerFragment;

/**
 * Created by YoKeyword on 16/2/5.
 */
public class DiscoverFragmentAdapter extends FragmentPagerAdapter {
    String[] mTitles = new String[]{"推荐", "热门", "收藏"};

    public DiscoverFragmentAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {
        if (position == 0) {
            return PagerFragment.newInstance(0);
        } else if (position == 1) {
            return PagerFragment.newInstance(1);
        } else {
            return PagerFragment.newInstance(2);
        }
    }

    @Override
    public int getCount() {
        return mTitles.length;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return mTitles[position];
    }
}
