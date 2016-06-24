# Fragmentation
A powerful library that manage Fragment for Android!

为"单Activity ＋ 多Fragment","多模块Activity + 多Fragment"架构而生，帮你大大简化使用过程，轻松解决各种复杂嵌套等问题，修复了官方Fragment库中存在的一些BUG。

Fragmentation is specificly written for Android to easily implement "Single Activity + Mutilple Fragments" or "Multiple FragmentActivities + Multiple Fragments" architecture. It helps you to deal with problems that come with complicated nested Fragments. Also, it fixed a lot of bugs in Fragment class.

![](/gif/logo.png)


为了更好的使用和了解该库，推荐阅读下面的文章:

For more, please read these articles below:

[Fragment全解析系列（一）：那些年踩过的坑](http://www.jianshu.com/p/d9143a92ad94)

[Fragment全解析系列（二）：正确的使用姿势](http://www.jianshu.com/p/fd71d65f0ec6)


# Demo演示：
单Activity + 多Fragment，第一个为简单场景demo，第二个为仿知乎的复杂嵌套demo

"Single Activity + Mutilple Fragments"
The first demo shows the basic use case of this library, the second demo shows how to implement complicated nested fragments.

<img src="/gif/demo.gif" width="320px"/>
&emsp;&emsp;&emsp;&emsp;<img src="/gif/nested.gif" width="320px"/>

# 特性
# FEATURES

1、**有效解决各种复杂嵌套、同级等Fragment重叠问题**

1、**Solved nested or "same-level" Fragments overlapping issue**

2、**实时查看Fragment的(包括嵌套Fragment)栈视图的对话框和Log，方便调试**

2、**Use the Fragment stack view dialog and logs to easily debug**

3、**增加启动模式、startForResult等类似Activity方法**

3、**add launch mode， startForResult and others that mimicking methods in Activity class**

4、**类似Android事件分发机制的Fragment回退方法：onBackPressedSupport()，轻松为每个Fragment实现Back按键事件**

4、**Add onBackPressedSupport() method to handle back button press in Fragment**

5、**完美的防抖动解决方案(防止用户点击速度过快,导致启动多个Fragment)**

5、**Perfect settlement for click debouncing(Avoid creating multiple Fragments when user clicks rapidly)**

6、**修复官方库里pop(tag/id)出栈多个Fragment时的一些BUG**

6、**Fixed bugs in Fragment class when poping multiple Fragments from back stack**

7、**完美解决进出栈动画的一些BUG，更自由的管理Fragment的动画**

7、**Fixed bugs in Fragment transition aniamtion. Now you'll be able to easily manage your Fragment transition animations**

8、**支持SwipeBack滑动边缘退出(需要使用Fragmentation_SwipeBack库,详情[README](https://github.com/YoKeyword/Fragmentation/blob/master/fragmentation_swipeback/README.md))**

8、**Support SwipeBack to exist(need support of Fragmentation_SwipeBack library. For more, please click here.(https://github.com/YoKeyword/Fragmentation/blob/master/fragmentation_swipeback/README.md) )**

<img src="/gif/log.png" width="400px"/>&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;<img src="/gif/SwipeBack.jpg" width="150px"/>

# 重大更新日志
### 0.7.X 来了！！

# ChangeLog
### V 0.7.X is available！！

1、仿知乎的新Demo，展示复杂嵌套Fragment的交互场景

1、Added a ZhiHu mockup demo. It demonstrates the scenario of dealing with complicated nested Fragments.


2、全新的Fragment恢复机制

2、New mechanism of Fragment restoration


3、更容易编写各种嵌套Fragment的代码

3、Easier to code for nested Fragments


4、支持同级Fragment的处理

4、Support dealing with "same-level" Fragments


5、实验性支持SharedElement－Material过渡动画

5、Support SharedElementTransition


6、全新的类似Android事件分发机制的onBackPressedSupport()

6、Added onBackPressedSupport() method

# 如何使用
# How to use this llibrary

**1. 项目下app的build.gradle中依赖：**
**1. Add dependency in build.gradle file of your app module：**
````gradle
// appcompat v7包是必须的
// appcompat v7 library is needed
compile 'me.yokeyword:fragmentation:0.7.5'
// 如果想使用SwipeBack 滑动边缘退出Fragment/Activity功能，请再添加下面的库
//If you want to integrate SwipeBack to exist Framgent/Activity feature, please also add this library
// compile 'me.yokeyword:fragmentation-swipeback:0.3.0'
````

**2. Activity继承SupportActivity：**
**2. Your Activity should extend SupportActivity：**
````java
public class MainActivity extends SupportActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(...);
        if (savedInstanceState == null) {
            loadRootFragment(R.id.fl_container, HomeFragment.newInstance());  
        }
    }
````

**3. Fragment继承SupportFragment：**
**3. Your Fragment should extend SupportFragment：**
````java
public class HomeFragment extends SupportFragment {

    private void xxx() {
        // 启动新的Fragment, 同时还有start(fragment,SINGTASK)、startForResult、startWithPop等启动方法
        start(DetailFragment.newInstance(HomeBean));
        // ... 其他pop, find, 设置动画等等API, 请自行查看WIKI
    }
}
````

### [进一步使用，查看wiki](https://github.com/YoKeyword/Fragmentation/wiki)
### [For more, please check out the wiki](https://github.com/YoKeyword/Fragmentation/wiki)

# 最后
有任何问题欢迎提issue或发邮件一起探讨，欢迎Star，Fork，PR！

# Last but important
If you got any problems, feel free to make an issue or send me emails. PLEASE Star, Fork, PR this project!!!
