# Fragmentation
A powerful library that manage Fragment for Android!

Fragmentation is specificly written for Android to easily implement "Single Activity + Mutilple Fragments" or "Multiple FragmentActivities + Multiple Fragments" architecture. It helps you to deal with problems that come with complicated nested Fragments. Also, it fixed a lot of bugs in Fragment class.

![](/gif/logo.png)


# Demo：
"Single Activity + Mutilple Fragments"
The first demo shows the basic use case of this library, the second demo shows the way to implement an app which is similar with WeChat,the third demo shows the way to implement complicated nested fragments.

<img src="/gif/demo.gif" width="280px"/>&emsp;<img src="/gif/wechat.gif" width="280px"/>
&emsp;<img src="/gif/nested.gif" width="280px"/>

# FEATURES

1、**Solved nested or "same-level" Fragments overlapping issue**

2、**Use the Fragment stack view dialog and logs to easily debug**

3、**add launch mode， startForResult and others that mimicking methods in Activity class**

4、**Add onBackPressedSupport() method to handle back button press in Fragment**

5、**New！！！ Add onSupportVisible() ,onLazyInitView() to simplify the dev of nested-fragment**

6、**Now you'll be able to easily manage your Fragment transition animations**

7、**Fixed bugs in Fragment class when poping multiple Fragments from back stack**

8、**Support SwipeBack to exist(need support of Fragmentation_SwipeBack library. For more, please [click here](https://github.com/YoKeyword/Fragmentation/blob/master/fragmentation_swipeback/README.md).**

<img src="/gif/log.png" width="400px"/>&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;<img src="/gif/SwipeBack.jpg" width="150px"/>

# ChangeLog
### V 0.8.X is available！！

[Detail](https://github.com/YoKeyword/Fragmentation/wiki/Home)

1、Added onSupportVisible(),onSupportInvisible() to simplify the dev of nested-fragment;

2、Added onLazyInitView();

3、Added registerFragmentLifecycleCallbacks() to SupportActivity to monitor Fragments Lifecycle;

4、Now you can set Tag！

5、Change the commit mode of start(), eg. commitAllowingStateLoss(),commitImmediate().

****

0.7.X

1、Added a ZhiHu mockup demo and a Wechat mockup demo. It demonstrates the scenario of dealing with complicated nested Fragments.

2、Support dealing with "same-level" Fragments

3、Support SharedElementTransition

4、Added onBackPressedSupport() method

# How to use this llibrary

**1. Add dependency in build.gradle file of your app module：**
````gradle
// appcompat v7 library is needed
compile 'me.yokeyword:fragmentation:0.8.3'
//If you want to integrate SwipeBack to exist Framgent/Activity feature, please also add this library
// compile 'me.yokeyword:fragmentation-swipeback:0.7.9'
````
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

**3. Your Fragment should extend SupportFragment：**
````java
public class HomeFragment extends SupportFragment {

    private void xxx() {
        // Launcher a Fragment, otherwise there are start(fragment,SINGTASK)、startForResult()、startWithPop() etc.
        start(DetailFragment.newInstance(HomeBean));
        // ... Other API of pop,find,anim setting etc,please check out wiki
    }
}
````

### [For more, please check out the wiki](https://github.com/YoKeyword/Fragmentation/wiki)

# Last but important
If you got any problems, feel free to make an issue or send me emails. PLEASE Star, Fork, PR this project!!!
