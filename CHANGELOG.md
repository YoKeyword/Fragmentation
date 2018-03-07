# Changelog

## 1.3.0
Added `startWithPopTo()`, Improve the stability of `pop()`/`popTo()`.

## 1.2.0
Added ActionQueue mechanism, this will improve stability.

## 1.0.0

1. Extends of SupportActivity/SupportFragment is no longer a forced requirement. Through `implements ISupportActivity/Fragment` ＋ `Delegate` to customize self-difined `SupportActivity/Fragment`

2. Remove `FragmentLifecycleCallbacks`(since support-25.1.0, `FragmentLifecycleCallbacks` is supported officially)

3. Add `extraTransaction()`to deal with self-defined tag, SharedElements, and transactions that are not managed by back stack

4. Compatible with support-25.4.0

5. Remove `replaceLoadRootFragment()` (use `loadRootFragment()` instead), Add `loadRootFragment(containerId, fragment, addToBack, allowEnterAnim)`

6. The lifecycle of Support can be triggered correctly through `replaceFragment()`

7. Dynamic change of Fragment's anim through `setFragmentAnimator()`

8. Support `popTo()` anim, add `popTo(f, includeF, afterRunnable, popAnimation)`

9. Further compatible with multi-touch, high-frequency transactions, optimize the problem of excessive drawing

## 0.10.0

1. Add a globally configurable `Fragmentation Builder`

* Provide a convenient way to open the stack view Dialog
    * bubble: click bubble to open stack view Dialog
    * shake: shake to open stack view Dialog
    * none: do not display stack view Dialog

* According to different environments, exceptions are dealt with respectively ("Can not perform this action after onSaveInstanceState!")

2、Fix `popToChild(f,includeF,afterRunnable)`hierarchical error problem

## 0.9.0

1. solve multi-touch problems, a number of optimizations, compatible problems etc.

## 0.8.0
1. Add `onLazyInitView()`, `onSupportVisible()`,`onSupportInvisible()` to simplify dev

2. SupportActivity provides ability to monitoring lifecycle of Fragment through `registerFragmentLifecycleCallbacks()`
 
3. Support self-defined tag
