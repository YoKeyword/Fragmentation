package me.yokeyword.sample.ui.fragment.shop;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import java.util.ArrayList;

import me.yokeyword.sample.R;
import me.yokeyword.sample.ui.BaseMainFragment;

/**
 * Created by YoKeyword on 16/2/4.
 */
public class ShopFragment extends BaseMainFragment {
    public static final String TAG = ShopFragment.class.getSimpleName();

    private Toolbar mToolbar;

    public static ShopFragment newInstance() {
        Bundle args = new Bundle();

        ShopFragment fragment = new ShopFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_shop, container, false);
        initView(view);
        return view;
    }

    private void initView(View view) {
        mToolbar = (Toolbar) view.findViewById(R.id.toolbar);

        mToolbar.setTitle("商店");
        initToolbarNav(mToolbar);

        MenuListFragment listFragment = findChildFragment(MenuListFragment.class);

        if (listFragment == null) {
            ArrayList<String> listMenus = new ArrayList<>();
            listMenus.add("销量排行");
            listMenus.add("当季特选");
            listMenus.add("炒菜");
            listMenus.add("汤面类");
            listMenus.add("煲类");
            listMenus.add("汤");
            listMenus.add("小菜");
            listMenus.add("酒水饮料");
            listMenus.add("盖浇饭类");
            listMenus.add("炒面类");
            listMenus.add("拉面类");
            listMenus.add("盖浇面类");
            listMenus.add("特色菜");
            listMenus.add("加料");
            listMenus.add("馄饨类");
            listMenus.add("其他");

            listFragment = MenuListFragment.newInstance(listMenus);
            startChildFragment(R.id.fl_list_container, listFragment, false);
        }
    }

    @Override
    public boolean onBackPressedSupport() {
        // ContentFragment是ShopFragment的栈顶子Fragment,会先调用ContentFragment的onBackPressedSupport方法
        Toast.makeText(_mActivity, "onBackPressedSupport-->ShopFragment处理了返回!", Toast.LENGTH_SHORT).show();
        pop();
        return true;
    }

    /**
     * 替换加载 内容Fragment
     *
     * @param fragment
     */
    public void showContentFragment(ContentFragment fragment) {
        replaceChildFragment(R.id.fl_content_container, fragment, false);
    }
}
