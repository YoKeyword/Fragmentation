# Fragmentation
A powerful library that manage Fragment for Android!

为"单Activity ＋ 多Fragment的架构","多模块Activity + 多Fragment的架构"而生，帮你简化使用过程，修复了官方Fragment库存在的一些BUG。

（新：Fragmentation-SwipeBack库，支持类似IOS的滑动边缘返回功能，[实现分析](http://www.jianshu.com/p/626229ca4dc2)）

为了更好的使用和了解该库,推荐你阅读下面2篇文章:

相关阅读：
[Fragment全解析系列（一）：那些年踩过的坑](http://www.jianshu.com/p/d9143a92ad94)    ， 
[Fragment全解析系列（二）：正确的使用姿势](http://www.jianshu.com/p/fd71d65f0ec6)

# Demo演示：
单Activity + 多Fragment

<img src="/gif/demo.gif"/>

# 特性
1、**为重度使用Fragment而生，提供了方便的管理Fragment的方法**

2、**有效解决Fragment重叠问题**

3、**完美的防抖动解决方案(防止用户点击速度过快,导致启动多个Fragment)**

4、**实时查看Fragment的(包括嵌套Fragment)栈视图的对话框和Log，方便调试**

5、**增加启动模式、startForResult等类似Activity方法**

6、**修复官方库里pop(tag/id)出栈多个Fragment时的一些BUG**

7、**完美解决进出栈动画的一些BUG，更自由的管理Fragment的动画**

8、**支持SwipeBack滑动边缘退出(需要使用Fragmentation_SwipeBack库,详情[README](https://github.com/YoKeyword/Fragmentation/blob/master/fragmentation_swipeback/README.md))**

<img src="/gif/log.png" width="400px"/>&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;<img src="/gif/SwipeBack.jpg" width="150px"/>

# 如何使用
1、项目下app的build.gradle中依赖：
````gradle
// appcompat v7包是必须的
compile 'me.yokeyword:fragmentation:0.6.5'
// 如果想使用SwipeBack 滑动边缘退出Fragment/Activity功能，请再添加下面的库
// compile 'me.yokeyword:fragmentation-swipeback:0.3.0'
````
2、Activity继承SupportActivity：
````java
public class MainActivity extends SupportActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(...);
        if (savedInstanceState == null) {
            start(HomeFragment.newInstance());
        }
    }

    /**
    *  设置Container的id，必须实现
    */
    @Override
    public int setContainerId() {
        return R.id.fl_container;
    }

    /**
     *  设置全局动画，在SupportFragment可以自由更改其动画
     */
    @Override
    protected FragmentAnimator onCreateFragmentAnimator() {
        // 默认竖向(和安卓5.0以上的动画相同)
        return super.onCreateFragmentAnimator();
        // 设置横向(和安卓4.x动画相同)
        // return new DefaultHorizontalAnimator();
        // 设置自定义动画
        // return new FragmentAnimator(enter,exit,popEnter,popExit);
        
        // 这里注意
        // new FragmentAnimator(enter,exit,popEnter,popExit)的4个参数与官方setCustomAnimations(enter,exit,popEnter,popExit)
        // 并不一致，对于官方来说Fragment出栈时的动画对应的是popExit（即第4个参数）
        // 对于FragmentAnimator来说Fragment出栈时的动画对应的是exit参数（即第2个参数）
        // 这里和官方不一致的原因主要出于我们绝大多数人认为enter,exit才是对应进出栈动画的角度考虑的。
    }
````
3、Fragment继承SupportFragment

# 重大更新日志

## 0.6.X:

1、完美的防抖动解决方案：使用Activity的dispatchTouchEvent方案替换了原来的通过时间判断方案（为0.7版本，管理Fragment嵌套等复杂场景的更新作铺垫）

2、提供了在第一次start复杂Fragment时,导致转场动画卡顿的解决方案(用例见demo的DetailFragment)

3、进行了一些方法的重构

4、进行了debug相关类的包迁移

# API

**SupportActivity**
打开 **栈视图** 的提示框，在开发时，有复杂嵌套的场景下，可以通过下面的方法查看不同阶级的栈视图。
````java
// 弹出 栈视图 提示框
showFragmentStackHierarchyView();
// 打印 栈视图 Log
logFragmentStackHierarchy(TAG);
````
除此之外包含大部分SupportFragment的方法，请自行查看。

**SupportFragment**

1、启动相关：
````java
// 启动新的Fragment
start(SupportFragment fragment)
// 以某种启动模式，启动新的Fragment
start(SupportFragment fragment, int launchMode)
// 启动新的Fragment，并能接收到新Fragment的数据返回
startForResult(SupportFragment fragment,int requestCode)
// 启动目标Fragment，并关闭当前Fragment
startWithPop(SupportFragment fragment)
````


2、关闭Fragment：
````java
// 关闭当前Fragment(在当前Fragment所在栈内pop)
pop();

// 关闭某一个Fragment栈内之上的Fragments
popTo(Class fragmentClass, boolean includeSelf);

// 如果想出栈后，紧接着开始.beginTransaction()开始一个事务，请使用下面的方法：
// 在 第二篇 文章内的 Fragment事务部分的问题 有解释原因
popTo(Class fragmentClass, boolean includeSelf, Runnable afterTransaction)
````
3、在SupportFragment内，支持监听返回按钮事件：
````java
@Override
public boolean onBackPressedSupport() {
   // 返回false,则继续传递返回事件； 返回true则不继续传递
    return false;
}
````

4、 定义当前Fragment的动画，复写`onCreateFragmentAnimation`方法：
````java
@Override
protected FragmentAnimator onCreateFragmentAnimation() {
    // 获取在SupportActivity里设置的全局动画对象 或者通过 _mActivity.getFragmentAnimator()获取
    FragmentAnimator fragmentAnimator = super.onCreateFragmentAnimation();
    fragmentAnimator.setEnter(0);
    fragmentAnimator.setExit(0);
    return fragmentAnimator;

    // 也可以直接通过
    // return new FragmentAnimator(enter,exit,popEnter,popExit)设置一个全新的动画
}
````
5、获取Fragment
````
// 获取所在栈内的栈顶(子)Fragment：
getTopFragment();
getTopChildFragment();

// 获取所在栈内的某个(子)Fragment对象：
findFragment(Class fragmentClass);
findChildFragment(Class fragmentClass);

// 获取所在栈内的前一个Fragment对象：
getPreFragment();
````

**其他**

隐藏/显示 输入法:(因为Fragment被销毁时,不会自动隐藏软键盘,以及弹出软键盘有些麻烦,故提供下面2个方法)
````java
// 隐藏软键盘 一般用在onHiden里
hideSoftInput();
// 显示软键盘,调用该方法后,会在onPause时自动隐藏软键盘
showSoftInput(View view);
````
下面是DetailFragment  `startForResult`  ModifyTitleFragment的代码：
````java
DetailFragment.class里:

startForResult(ModifyDetailFragment.newInstance(mTitle), REQ_CODE);

@Override
public void onFragmentResult(int requestCode, int resultCode, Bundle data) {
    super.onFragmentResult(requestCode, resultCode, data);
    if (requestCode == REQ_CODE && resultCode == RESULT_OK ) {
    // 在此通过Bundle data 获取返回的数据
    }
}

ModifyTitleFragment.class里:

Bundle bundle = new Bundle();
bundle.putString("title", "xxxx");
setFramgentResult(RESULT_OK, bundle);
````

下面是以SingleTask模式重新启动一个已存在的Fragment的标准代码：
比如：HomeFragment->BFragment->CFragment   CFragment以SingleTask模式重新启动HomeFragment
````java
HomeFragment fragment = findFragment(HomeFragment.class);
Bundle newBundle = new Bundle();
// 传递的bundle数据，会调用目标Fragment的onNewBundle(Bundle newBundle)方法
fragment.putNewBundle(newBundle);
// 栈内的homeFragment以SingleTask模式启动
start(fragment, SupportFragment.SINGLETASK);

// 在HomeFragment.class中：
@Override
protected void onNewBundle(Bundle newBundle){
    // 在此可以接收到数据  类似Activity中的onNewIntent()
}
````
# 关于Fragmentation帮你恢复Fragment，你需要知道的

2个概念：
>"同级"式：比如QQ的主界面，“消息”、“联系人”、“动态”，这3个Fragment属于同级关系
“流程”式：比如登录->注册/忘记密码->填写信息->跳转到主页Activity

对于Activity内的“流程”式Fragments（比如登录->注册/忘记密码->填写信息->跳转到主页Activity），Fragmentation帮助你处理了栈内的恢复，保证Fragment不会重叠，你不需要再自己处理了。

但是如果你的Activity内的Fragments是“同级”的，那么你需要自己去恢复处理，因为库并不能知道你想恢复哪个Fragment。

而如果你有Fragment嵌套，那么不管是“同级”式还是“流程”式，你都需要自己去恢复处理。

这2块内容，后续会持续想办法解决，如果你有比较好的建议，随时可以提出哈～

*已经有思路，正在尝试封装中...*

# 最后
有任何问题欢迎一起探讨，欢迎Fork，PR！
