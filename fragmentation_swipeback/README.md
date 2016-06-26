# 简介
滑动Activity/Fragment边缘即可类似IOS一样，拖动返回。

Activity内Fragment数大于1时，滑动返回的是Fragment，否则滑动返回的是Activity。

[原理分析](http://www.jianshu.com/p/626229ca4dc2)

# 截图
<img src="../gif/swipe.gif"/>

# 如何使用
1、项目下app的build.gradle中依赖：
````gradle
// appcompat v7包是必须的
compile 'me.yokeyword:fragmentation:最新版'
compile 'me.yokeyword:fragmentation-swipeback:0.3.1'
````
2、如果Activity也需要支持SwipeBack，则继承SwipeBackActivity:
````java
public class SwipeBackSampleActivity extends SwipeBackActivity {}
````
同时该Activity的theme添加如下属性：
````xml
 <item name="android:windowIsTranslucent">true</item>
````

3、如果Fragment需要支持SwipeBack，则继承SwipeBackFragment:
````java
public class SwipeBackSampleFragment extends SwipeBackFragment {
 @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.xxx, container, false);
        // 需要支持SwipeBack则这里必须调用toSwipeBackFragment(view);
        return attachToSwipeBack(view);
    }
}
````

更多方法:
````java
  getSwipeBackLayout().setEdgeOrientation(SwipeBackLayout.EDGE_RIGHT); // EDGE_LEFT(默认),EDGE_ALL

  getSwipeBackLayout().addSwipeListener(new SwipeBackLayout.OnSwipeListener() {
            @Override
            public void onDragStateChange(int state) {
                // Drag state
            }

            @Override
            public void onEdgeTouch(int edgeFlag) {
                // 触摸的边缘flag
            }

            @Override
            public void onDragScrolled(float scrollPercent) {
                // 滑动百分比
            }
   });

   // 对于SwipeBackActivity有下面控制SwipeBack优先级的方法:
   /**
     * 限制SwipeBack的条件,默认栈内Fragment数 <= 1时 , 优先滑动退出Activity , 而不是Fragment
     *
     * 可以通过复写该方法, 自由控制优先级
     *
     * @return true: Activity可以滑动退出, 并且总是优先;  false: Activity不允许滑动退出
     */
     @Override
     public boolean swipeBackPriority() {
        return super.swipeBackPriority();
        // 下面是默认实现:
        // return getSupportFragmentManager().getBackStackEntryCount() <= 1;
     }
````
