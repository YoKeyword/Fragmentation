package me.yokeyword.sample;

import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import me.yokeyword.fragmentation.SupportActivity;
import me.yokeyword.fragmentation.SupportFragment;
import me.yokeyword.fragmentation.anim.FragmentAnimator;
import me.yokeyword.sample.ui.BaseMainFragment;
import me.yokeyword.sample.ui.fragment.account.LoginFragment;
import me.yokeyword.sample.ui.fragment.discover.DiscoverFragment;
import me.yokeyword.sample.ui.fragment.home.HomeFragment;
import me.yokeyword.sample.ui.fragment.shop.ShopFragment;

/**
 * Created by YoKeyword on 16/1/29.
 */
public class MainActivity extends SupportActivity
        implements NavigationView.OnNavigationItemSelectedListener, BaseMainFragment.OnFragmentOpenDrawerListener
        , LoginFragment.OnLoginSuccessListener {
    public static final String TAG = MainActivity.class.getSimpleName();

    private DrawerLayout mDrawer;
    private NavigationView mNavigationView;
    private TextView mTvName;   // NavigationView上的名字
    private ImageView mImgNav;  // NavigationView上的头像

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (savedInstanceState == null) {
            start(HomeFragment.newInstance());
        }

        initView();
    }

    @Override
    protected FragmentAnimator onCreateFragmentAnimator() {
        // 设置默认Fragment动画  默认竖向(和安卓5.0以上的动画相同)
        return super.onCreateFragmentAnimator();
        // 设置横向(和安卓4.x动画相同)
//        return new DefaultHorizontalAnimator();
        // 设置自定义动画
//        return new FragmentAnimator(enter,exit,popEnter,popExit);
    }

    private void initView() {
        mDrawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, mDrawer, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
//        mDrawer.setDrawerListener(toggle);
        toggle.syncState();

        mNavigationView = (NavigationView) findViewById(R.id.nav_view);
        mNavigationView.setNavigationItemSelectedListener(this);
        mNavigationView.setCheckedItem(R.id.nav_home);

        LinearLayout llNavHeader = (LinearLayout) mNavigationView.getHeaderView(0);
        mTvName = (TextView) llNavHeader.findViewById(R.id.tv_name);
        mImgNav = (ImageView) llNavHeader.findViewById(R.id.img_nav);
        llNavHeader.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDrawer.closeDrawer(GravityCompat.START);

                mDrawer.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        goLogin();
                    }
                }, 250);
            }
        });
    }

    @Override
    protected void onHandleSaveInstancState(Bundle savedInstanceState) {
        // 复写的时候 下面的super一定要删掉
//        super.onHandleSaveInstancState(savedInstanceState);
    }

    @Override
    public int setContainerId() {
        return R.id.fl_container;
    }

    @Override
    public void onBackPressed() {
        if (mDrawer.isDrawerOpen(GravityCompat.START)) {
            mDrawer.closeDrawer(GravityCompat.START);
        } else {

            Fragment topFragment = getTopFragment();

            // 主页的Fragment
            if (topFragment instanceof DiscoverFragment || topFragment instanceof ShopFragment) {
                mNavigationView.setCheckedItem(R.id.nav_home);
            }
            super.onBackPressed();
        }
    }

    /**
     * 打开抽屉
     */
    @Override
    public void onOpenDrawer() {
        if (!mDrawer.isDrawerOpen(GravityCompat.START)) {
            mDrawer.openDrawer(GravityCompat.START);
        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(final MenuItem item) {
        mDrawer.closeDrawer(GravityCompat.START);

        mDrawer.postDelayed(new Runnable() {
            @Override
            public void run() {
                int id = item.getItemId();

                final SupportFragment topFragment = getTopFragment();

                if (id == R.id.nav_home) {

                    HomeFragment fragment = findFragment(HomeFragment.class);
                    if (fragment == null) {
                        fragment = HomeFragment.newInstance();
                    }

                    Bundle newBundle = new Bundle();
                    newBundle.putString("from", "主页-->来自:" + topFragment.getClass().getSimpleName());
                    fragment.putNewBundle(newBundle);

                    start(fragment, SupportFragment.SINGLETASK);
                } else if (id == R.id.nav_discover) {
                    DiscoverFragment fragment = findFragment(DiscoverFragment.class);
                    if (fragment == null) {
                        popTo(HomeFragment.class, false, new Runnable() {
                            @Override
                            public void run() {
                                start(DiscoverFragment.newInstance());
                            }
                        });
                    } else {
                        Bundle newBundle = new Bundle();
                        newBundle.putString("from", "来自:" + topFragment.getClass().getName());
                        fragment.putNewBundle(newBundle);
                        start(fragment, SupportFragment.SINGLETASK);
                    }
                } else if (id == R.id.nav_msg) {
                    ShopFragment fragment = findFragment(ShopFragment.class);
                    if (fragment == null) {
                        popTo(HomeFragment.class, false, new Runnable() {
                            @Override
                            public void run() {
                                start(ShopFragment.newInstance());
                            }
                        });
                    } else {
                        Bundle newBundle = new Bundle();
                        newBundle.putString("from", "来自:" + topFragment.getClass().getName());
                        fragment.putNewBundle(newBundle);
                        start(fragment, SupportFragment.SINGLETASK);
                    }
                } else if (id == R.id.nav_login) {
                    goLogin();
                }
            }
        }, 250);

        return true;
    }

    private void goLogin() {
        start(LoginFragment.newInstance());
    }

    @Override
    public void onLoginSuccess(String account) {
        mTvName.setText(account);
        mImgNav.setImageResource(R.drawable.ic_account_circle_white_48dp);
        Toast.makeText(this, "登录成功,NavigationView的用户名已经更改!", Toast.LENGTH_SHORT).show();
    }
}
