package me.yokeyword.sample.demo_flow.ui.fragment.shop;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;

import java.util.ArrayList;
import java.util.Arrays;

import me.yokeyword.sample.R;
import me.yokeyword.sample.demo_flow.base.BaseMainFragment;
import me.yokeyword.sample.demo_flow.base.MySupportFragment;

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
        initView(view, savedInstanceState);
        return view;
    }

    private void initView(View view, Bundle savedInstanceState) {
        mToolbar = (Toolbar) view.findViewById(R.id.toolbar);

        mToolbar.setTitle(R.string.shop);
        initToolbarNav(mToolbar);

        if (findChildFragment(MenuListFragment.class) == null) {
            ArrayList<String> listMenus = new ArrayList<>(Arrays.asList(getResources().getStringArray(R.array.array_menu)));
            MenuListFragment menuListFragment = MenuListFragment.newInstance(listMenus);
            loadRootFragment(R.id.fl_list_container, menuListFragment);
            // false:  不加入回退栈;  false: 不显示动画
            loadRootFragment(R.id.fl_content_container, ContentFragment.newInstance(listMenus.get(0)), false, false);
        }
    }

    @Override
    public boolean onBackPressedSupport() {
        // ContentFragment是ShopFragment的栈顶子Fragment,会先调用ContentFragment的onBackPressedSupport方法
        Toast.makeText(_mActivity, "onBackPressedSupport-->return false, " + getString(R.string.upper_process), Toast.LENGTH_SHORT).show();
        return false;
    }

    /**
     * 替换加载 内容Fragment
     *
     * @param fragment
     */
    public void switchContentFragment(ContentFragment fragment) {
        MySupportFragment contentFragment = findChildFragment(ContentFragment.class);
        if (contentFragment != null) {
            contentFragment.replaceFragment(fragment, false);
        }
    }
}
