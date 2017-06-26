 [![Download](https://api.bintray.com/packages/yokeyword/maven/Fragmentation/images/download.svg) ](https://bintray.com/yokeyword/maven/Fragmentation/_latestVersion)
# Fragmentation

[![Join the chat at https://gitter.im/fragmentationx/Lobby](https://badges.gitter.im/fragmentationx/Lobby.svg)](https://gitter.im/fragmentationx/Lobby?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge&utm_content=badge)
A powerful library that manage Fragment for Android!

## [English README](https://github.com/YoKeyword/Fragmentation/blob/master/README_EN.md)

为"单Activity ＋ 多Fragment","多模块Activity + 多Fragment"架构而生，简化开发，轻松解决动画、嵌套、事务相关等问题，修复了官方Fragment库中存在的一些BUG。

![](/gif/logo.png)


为了更好的使用和了解该库，推荐阅读下面的文章:

[Fragment全解析系列（一）：那些年踩过的坑](http://www.jianshu.com/p/d9143a92ad94)

[Fragment全解析系列（二）：正确的使用姿势](http://www.jianshu.com/p/fd71d65f0ec6)


# Demo演示：
均为单Activity + 多Fragment，第一个为简单流式demo，第二个为仿微信交互的demo(全页面支持滑动退出)，第三个为仿知乎交互的复杂嵌套demo

<img src="/gif/demo.gif" width="280px"/>&emsp;<img src="/gif/wechat.gif" width="280px"/>
&emsp;<img src="/gif/nested.gif" width="280px"/>

# 特性

1、**可以快速开发出各种嵌套设计的Fragment App**

2、**悬浮球／摇一摇实时查看Fragment的栈视图Dialog，降低开发难度**

3、**增加启动模式、startForResult等类似Activity方法**

4、**类似Android事件分发机制的Fragment回退方法：onBackPressedSupport()，轻松为每个Fragment实现Back按键事件**

5、**提供onSupportVisible()等生命周期方法，简化嵌套Fragment的开发过程； 提供统一的onLazyInitView()懒加载方法**

6、**提供 Fragment转场动画 系列解决方案，动态更换动画**

7、**更强的兼容性, 解决多点触控、重叠等问题**

8、**支持SwipeBack滑动边缘退出(需要使用Fragment｀ation_SwipeBack库,详情[README](https://github.com/YoKeyword/Fragmentation/blob/master/fragmentation_swipeback/README.md))**

<img src="/gif/log.png" width="400px"/>&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;<img src="/gif/SwipeBack.jpg" width="150px"/>

# 如何使用

**1. 项目下app的build.gradle中依赖：**
````gradle
// appcompat-v7包是必须的
compile 'me.yokeyword:fragmentation:1.0.0'

// 如果不想继承SupportActivity/Fragment，自己定制Support，可仅依赖:
// compile 'me.yokeyword:fragmentation-core:1.0.0'

// 如果想使用SwipeBack 滑动边缘退出Fragment/Activity功能，完整的添加规则如下：
compile 'me.yokeyword:fragmentation:1.0.0'
// swipeback基于fragmentation, 如果是自定制SupportActivity/Fragment，则参照SwipeBackActivity/Fragment实现即可
compile 'me.yokeyword:fragmentation-swipeback:1.0.0'
````

**2. Activity继承SupportActivity：**
````java
// v1.0.0开始，不强制继承SupportActivity，可使用接口＋委托形式来实现自己的SupportActivity
public class MainActivity extends SupportActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(...);
        // 栈视图等功能，建议在Application里初始化
        Fragmentation.builder()
             // 显示悬浮球 ; 其他Mode:SHAKE: 摇一摇唤出   NONE：隐藏
             .stackViewMode(Fragmentation.BUBBLE)
             .debug(BuildConfig.DEBUG)
             ...
             .install();

        if (findFragment(HomeFragment.class) == null) {
            loadRootFragment(R.id.fl_container, HomeFragment.newInstance());  // 加载根Fragment
        }
    }
````

**3. Fragment继承SupportFragment：**
````java
// v1.0.0开始，不强制继承SupportFragment，可使用接口＋委托形式来实现自己的SupportFragment
public class HomeFragment extends SupportFragment {

    private void xxx() {
        // 启动新的Fragment, 另有start(fragment,SINGTASK)、startForResult、startWithPop等启动方法
        start(DetailFragment.newInstance(HomeBean));
        // ... 其他pop, find, 设置动画等等API, 请自行查看WIKI
    }
}
````

## [进一步使用、ChangeLog，查看wiki](https://github.com/YoKeyword/Fragmentation/wiki)

# 最后
有任何问题欢迎提issue或发邮件一起探讨，欢迎Star，Fork，PR！
