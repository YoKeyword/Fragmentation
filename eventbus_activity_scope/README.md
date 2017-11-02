# EventBusActivityScope
To simplify the communication between Fragments. / Activity作用域的EventBus帮助类。


## How do I use EventBusActivityScope?
1、build.gradle：

````gradle
compile 'me.yokeyword:eventbus-activity-scope:1.1.0'
// Your EventBus's version
compile 'org.greenrobot:eventbus:{version}'
````

2、Java：

Use `EventBusActivityScope.getDefault(activity)` instead of `EventBus.getDefault()`.