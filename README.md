# Fragmentation
A powerful library that manage Fragment for Android!

## [English README](https://github.com/YoKeyword/Fragmentation/blob/master/README_EN.md)

为"单Activity ＋ 多Fragment","多模块Activity + 多Fragment"架构而生，帮你大大简化使用过程，轻松解决各种复杂嵌套等问题，修复了官方Fragment库中存在的一些BUG。

![](/gif/logo.png)


为了更好的使用和了解该库，推荐阅读下面的文章:

[Fragment全解析系列（一）：那些年踩过的坑](http://www.jianshu.com/p/d9143a92ad94)

[Fragment全解析系列（二）：正确的使用姿势](http://www.jianshu.com/p/fd71d65f0ec6)


# Demo演示：
均为单Activity + 多Fragment，第一个为简单流式demo，第二个为仿微信交互的demo(全页面支持滑动退出)，第三个为仿知乎交互的复杂嵌套demo

<img src="/gif/demo.gif" width="280px"/>&emsp;<img src="/gif/wechat.gif" width="280px"/>
&emsp;<img src="/gif/nested.gif" width="280px"/>

# 特性

1、**有效解决各种复杂嵌套、同级等Fragment重叠问题**

2、**实时查看Fragment的(包括嵌套Fragment)栈视图的对话框和Log，方便调试**

3、**增加启动模式、startForResult等类似Activity方法**

4、**类似Android事件分发机制的Fragment回退方法：onBackPressedSupport()，轻松为每个Fragment实现Back按键事件**

5、**完美的防抖动解决方案(防止用户点击速度过快,导致启动多个Fragment)**

6、**提供可轻松 设定Fragment转场动画 的解决方案**

7、**修复官方库里pop(tag/id)出栈多个Fragment时的一些BUG**

8、**支持SwipeBack滑动边缘退出(需要使用Fragmentation_SwipeBack库,详情[README](https://github.com/YoKeyword/Fragmentation/blob/master/fragmentation_swipeback/README.md))**

<img src="/gif/log.png" width="400px"/>&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;<img src="/gif/SwipeBack.jpg" width="150px"/>

# 重大更新日志
### 0.7.X 来了！！

新: 0.7.7 : 更改方法,添加参数 onEnterAnimtionEnd() -> onEnterAnimtionEnd(Bundle savedInstanceState)

1、2个新demo: 仿知乎交互 ＋ 仿微信交互的新Demo，展示复杂嵌套Fragment的交互场景

2、全新的Fragment恢复机制

3、更容易编写各种嵌套Fragment的代码

4、支持同级Fragment的处理

5、实验性支持SharedElement－Material过渡动画

6、全新的类似Android事件分发机制的onBackPressedSupport()

# 如何使用

**1. 项目下app的build.gradle中依赖：**
````gradle
// appcompat v7包是必须的
compile 'me.yokeyword:fragmentation:0.7.13'
// 如果想使用SwipeBack 滑动边缘退出Fragment/Activity功能，请再添加下面的库
// compile 'me.yokeyword:fragmentation-swipeback:0.7.9'
````

**2. Activity继承SupportActivity：**
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
````java
public class HomeFragment extends SupportFragment {

    private void xxx() {
        // 启动新的Fragment, 另外还有start(fragment,SINGTASK)、startForResult、startWithPop等启动方法
        start(DetailFragment.newInstance(HomeBean));
        // ... 其他pop, find, 设置动画等等API, 请自行查看WIKI
    }
}
````

### [进一步使用，查看wiki](https://github.com/YoKeyword/Fragmentation/wiki)

# 最后
有任何问题欢迎提issue或发邮件一起探讨，欢迎Star，Fork，PR！
