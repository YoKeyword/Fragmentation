package me.yokeyword.sample.demo_zhihu;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;

import me.yokeyword.eventbusactivityscope.EventBusActivityScope;
import me.yokeyword.fragmentation.SupportActivity;
import me.yokeyword.fragmentation.SupportFragment;
import me.yokeyword.sample.R;
import me.yokeyword.sample.demo_zhihu.base.BaseMainFragment;
import me.yokeyword.sample.demo_zhihu.event.TabSelectedEvent;
import me.yokeyword.sample.demo_zhihu.ui.fragment.first.ZhihuFirstFragment;
import me.yokeyword.sample.demo_zhihu.ui.fragment.first.child.FirstHomeFragment;
import me.yokeyword.sample.demo_zhihu.ui.fragment.fourth.ZhihuFourthFragment;
import me.yokeyword.sample.demo_zhihu.ui.fragment.fourth.child.MeFragment;
import me.yokeyword.sample.demo_zhihu.ui.fragment.second.ZhihuSecondFragment;
import me.yokeyword.sample.demo_zhihu.ui.fragment.second.child.ViewPagerFragment;
import me.yokeyword.sample.demo_zhihu.ui.fragment.third.ZhihuThirdFragment;
import me.yokeyword.sample.demo_zhihu.ui.fragment.third.child.ShopFragment;
import me.yokeyword.sample.demo_zhihu.ui.view.BottomBar;
import me.yokeyword.sample.demo_zhihu.ui.view.BottomBarTab;

/**
 * 类知乎 复杂嵌套Demo tip: 多使用右上角的"查看栈视图"
 * Created by YoKeyword on 16/6/2.
 */
public class MainActivity extends SupportActivity implements BaseMainFragment.OnBackToFirstListener {
    public static final int FIRST = 0;
    public static final int SECOND = 1;
    public static final int THIRD = 2;
    public static final int FOURTH = 3;

    private SupportFragment[] mFragments = new SupportFragment[4];

    private BottomBar mBottomBar;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.zhihu_activity_main);

        SupportFragment firstFragment = findFragment(ZhihuFirstFragment.class);
        if (firstFragment == null) {
            mFragments[FIRST] = ZhihuFirstFragment.newInstance();
            mFragments[SECOND] = ZhihuSecondFragment.newInstance();
            mFragments[THIRD] = ZhihuThirdFragment.newInstance();
            mFragments[FOURTH] = ZhihuFourthFragment.newInstance();

            loadMultipleRootFragment(R.id.fl_container, FIRST,
                    mFragments[FIRST],
                    mFragments[SECOND],
                    mFragments[THIRD],
                    mFragments[FOURTH]);
        } else {
            // 这里库已经做了Fragment恢复,所有不需要额外的处理了, 不会出现重叠问题

            // 这里我们需要拿到mFragments的引用
            mFragments[FIRST] = firstFragment;
            mFragments[SECOND] = findFragment(ZhihuSecondFragment.class);
            mFragments[THIRD] = findFragment(ZhihuThirdFragment.class);
            mFragments[FOURTH] = findFragment(ZhihuFourthFragment.class);
        }

        initView();
    }

    private void initView() {
        mBottomBar = (BottomBar) findViewById(R.id.bottomBar);

        mBottomBar.addItem(new BottomBarTab(this, R.drawable.ic_home_white_24dp))
                .addItem(new BottomBarTab(this, R.drawable.ic_discover_white_24dp))
                .addItem(new BottomBarTab(this, R.drawable.ic_message_white_24dp))
                .addItem(new BottomBarTab(this, R.drawable.ic_account_circle_white_24dp));

        mBottomBar.setOnTabSelectedListener(new BottomBar.OnTabSelectedListener() {
            @Override
            public void onTabSelected(int position, int prePosition) {
                showHideFragment(mFragments[position], mFragments[prePosition]);
            }

            @Override
            public void onTabUnselected(int position) {

            }

            @Override
            public void onTabReselected(int position) {
                final SupportFragment currentFragment = mFragments[position];
                int count = currentFragment.getChildFragmentManager().getBackStackEntryCount();

                // 如果不在该类别Fragment的主页,则回到主页;
                if (count > 1) {
                    if (currentFragment instanceof ZhihuFirstFragment) {
                        currentFragment.popToChild(FirstHomeFragment.class, false);
                    } else if (currentFragment instanceof ZhihuSecondFragment) {
                        currentFragment.popToChild(ViewPagerFragment.class, false);
                    } else if (currentFragment instanceof ZhihuThirdFragment) {
                        currentFragment.popToChild(ShopFragment.class, false);
                    } else if (currentFragment instanceof ZhihuFourthFragment) {
                        currentFragment.popToChild(MeFragment.class, false);
                    }
                    return;
                }


                // 这里推荐使用EventBus来实现 -> 解耦
                if (count == 1) {
                    // 在FirstPagerFragment中接收, 因为是嵌套的孙子Fragment 所以用EventBus比较方便
                    // 主要为了交互: 重选tab 如果列表不在顶部则移动到顶部,如果已经在顶部,则刷新
                    EventBusActivityScope.getDefault(MainActivity.this).post(new TabSelectedEvent(position));
                }
            }
        });
    }

    @Override
    public void onBackPressedSupport() {
        if (getSupportFragmentManager().getBackStackEntryCount() > 1) {
            pop();
        } else {
            ActivityCompat.finishAfterTransition(this);
        }
    }

    @Override
    public void onBackToFirstFragment() {
        mBottomBar.setCurrentItem(0);
    }

    /**
     * 这里暂没实现,忽略
     */
//    @Subscribe
//    public void onHiddenBottombarEvent(boolean hidden) {
//        if (hidden) {
//            mBottomBar.hide();
//        } else {
//            mBottomBar.show();
//        }
//    }
}
