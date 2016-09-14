package me.yokeyword.sample.demo_wechat.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import me.yokeyword.sample.demo_wechat.ui.fragment.second.FirstPagerFragment;
import me.yokeyword.sample.demo_wechat.ui.fragment.second.OtherPagerFragment;


/**
 * Created by YoKeyword on 16/6/5.
 */
public class WechatPagerFragmentAdapter extends FragmentPagerAdapter {
    private String[] mTab = new String[]{"全部", "陌生人"};

    public WechatPagerFragmentAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {
        if (position == 0) {
            return FirstPagerFragment.newInstance();
        } else {
            return OtherPagerFragment.newInstance();
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
