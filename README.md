[![Download](https://api.bintray.com/packages/yokeyword/maven/Fragmentation/images/download.svg) ](https://bintray.com/yokeyword/maven/Fragmentation/_latestVersion) [![Hex.pm](https://img.shields.io/hexpm/l/plug.svg)](https://www.apache.org/licenses/LICENSE-2.0)

# Fragmentation
Fragmentation is a powerful library managing Fragment for Android. It is designed for "Single Activity + Multi-Fragments" and "Multi-FragmentActivities + Multi-Fragments" architecture to simplify development process. 

# Demo
The first demo shows the basic usage of the library. The second one shows the way to implement an app which is similar to WeChat. Complicated nested fragments' usage demo are also showed.

<img src="/gif/demo.gif" width="280px"/> <img src="/gif/wechat.gif" width="280px"/>
 <img src="/gif/nested.gif" width="280px"/>

# Benefits

**1. Develop complicated nested fragment app rapidly**
**2. Use fragment's stack view dialog to debug easily**
**3. Add launch mode, startForResult etc. to provide similar behavior of Activity**
**4. Add onBackPressedSupport() method to support back button press monitoring in Fragment**
**5. Add onSupportVisible(), onLazyInitView() to simplify dev of nested-fragments**
**6. Easily manage Fragment transition animations**
**7. More compatibility, solve multi-touch, fragments overlapping problems**
**8. Support SwipeBack to exit(Fragmentation_SwipeBack lib are needed [README](https://github.com/YoKeyword/Fragmentation/blob/master/fragmentation_swipeback/README.md))**

<img src="/gif/log.png" width="400px"/>      <img src="/gif/SwipeBack.jpg" width="150px"/>

# Get started

**build.gradle**
````gradle
// appcompat-v7 is required
compile 'me.yokeyword:fragmentation:1.0.1'

// If you don't want to extends SupportActivity/Fragment and would like to customize your own support, just rely on fragmentation-core
// compile 'me.yokeyword:fragmentation-core:1.0.1'

// To use SwipeBack function, rely on both fragmentation & fragmentation-swipeback lib
compile 'me.yokeyword:fragmentation:1.0.1'

//swipeback is based on fragmentation. Refer to SwipeBackActivity/Fragment for your Customized SupportActivity/Fragment
compile 'me.yokeyword:fragmentation:1.0.1'
compile 'me.yokeyword:fragmentation-swipeback:1.0.1'
````

**Activity extends SupportActivity**
````java
// since v1.0.0, forced extends of SupportActivity is not required, you can use interface + delegate to implement your own SupportActivity 
public class MainActivity extends SupportActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(...);
      	// Stack view is recommended to initialize in the Application
        Fragmentation.builder()
          	 // show stack view. Mode: BUBBLE, SHAKE, NONE
             .stackViewMode(Fragmentation.BUBBLE)
             .debug(BuildConfig.DEBUG)
             ...
             .install();

        if (findFragment(HomeFragment.class) == null) {
            loadRootFragment(R.id.fl_container, HomeFragment.newInstance());  //load root Fragment
        }
    }
````

**Fragment extends SupportFragment**
````java
// since v1.0.0, forced extends of SupportActivity is not required, you can use interface + delegate to implement your own SupportActivity
public class HomeFragment extends SupportFragment {

    private void xxx() {
      	// launch a new Fragment, other methods: start(fragment,SINGTASK)、startForResult、startWithPop etc.
        start(DetailFragment.newInstance(HomeBean));
      	// check wiki for other pop, find and animation setting related API
    }
}
````

[## WIKI](https://github.com/YoKeyword/Fragmentation/wiki)

# Supplement
Please submit an issue or email me for any further questions. Star, fork and PR is welcomed.