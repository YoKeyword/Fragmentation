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

1、**可以快速开发出各种嵌套设计的Fragment App**

2、**悬浮球／摇一摇实时查看Fragment的栈视图Dialog，大大降低开发难度**

3、**增加启动模式、startForResult等类似Activity方法**

4、**类似Android事件分发机制的Fragment回退方法：onBackPressedSupport()，轻松为每个Fragment实现Back按键事件**

5、**New！！！ 提供onSupportVisible()等生命周期方法，简化嵌套Fragment的开发过程； 提供统一的onLazyInitView()懒加载方法**

6、**提供靠谱的 Fragment转场动画 的解决方案**

7、**更强的兼容性, 解决多点触控、重叠等问题**

8、**支持SwipeBack滑动边缘退出(需要使用Fragment｀ation_SwipeBack库,详情[README](https://github.com/YoKeyword/Fragmentation/blob/master/fragmentation_swipeback/README.md))**

<img src="/gif/log.png" width="400px"/>&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;<img src="/gif/SwipeBack.jpg" width="150px"/>

# TODO
* ~~栈视图悬浮球／摇一摇唤出 栈视图~~
* Activity侧滑返回：更换实现方式
* ~~Fragment侧滑返回：实现视觉差效果~~
* replace进一步的支持
* ~~Fragment路由module~~ （建议使用阿里巴巴出品的[ARouter](https://github.com/alibaba/ARouter)）
# 重大更新日志

### 0.10.X [详情点这里](https://github.com/YoKeyword/Fragmentation/wiki/Home)

1、添加可全局配置的Fragmentaion Builder类：
* 提供方便打开栈视图Dialog的方法：
    * bubble: 显示悬浮球，可点击唤出栈视图Dialog，可自由拖拽
    * shake: 摇一摇唤出栈视图Dialog
    * none: 默认不显示栈视图Dialog

* 根据是否是Debug环境，方便区别处理异常（"Can not perform this action after onSaveInstanceState!"）

2、Fix popToChild(fg,boolean,Runnable)层级错误问题.

### 0.9.X

1、解决多点触控问题，多项优化、兼容、Fix

2、对于25.1.0+的 v4包，完善了SharedElement！

### 0.8.X
1、提供onLazyInitView()懒加载，onSupportVisible()，onSupportInvisible()等生命周期方法，简化开发；

2、SupportActivity提供registerFragmentLifecycleCallbacks()来监控其下所有Fragment的生命周期；

3、自定义Tag

# 如何使用

**1. 项目下app的build.gradle中依赖：**
````gradle
// appcompat v7包是必须的
compile 'me.yokeyword:fragmentation:0.10.7'
// 如果想使用SwipeBack 滑动边缘退出Fragment/Activity功能，请再添加下面的库
// compile 'me.yokeyword:fragmentation-swipeback:0.10.4'
````

**2. Activity继承SupportActivity：**
````java
public class MainActivity extends SupportActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(...);
        if (savedInstanceState == null) {
            loadRootFragment(R.id.fl_container, HomeFragment.newInstance());  // 加载根Fragment
        }
        // 栈视图功能，大大降低Fragment的开发难度，建议在Application里初始化
        Fragmentation.builder()
                // 显示悬浮球 ; 其他Mode:SHAKE: 摇一摇唤出   NONE：隐藏
                .stackViewMode(Fragmentation.BUBBLE)
                .install();
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
