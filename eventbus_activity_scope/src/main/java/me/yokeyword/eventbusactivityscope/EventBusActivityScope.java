package me.yokeyword.eventbusactivityscope;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Activity-scope EventBus.
 * <p>
 * Created by YoKey on 17/10/17.
 */
public class EventBusActivityScope {
    private static final String TAG = EventBusActivityScope.class.getSimpleName();

    private static AtomicBoolean sInitialized = new AtomicBoolean(false);
    private static volatile EventBus sInvalidEventBus;

    private static final ArrayList<Activity> sActivityList = new ArrayList<>();
    private static final Map<Activity, EventBus> sActivityEventBusScopePool = new ConcurrentHashMap<>();

    static void init(Context context) {
        if (sInitialized.getAndSet(true)) {
            return;
        }

        ((Application) context.getApplicationContext())
                .registerActivityLifecycleCallbacks(new Application.ActivityLifecycleCallbacks() {
                    @Override
                    public void onActivityCreated(Activity activity, Bundle bundle) {
                        synchronized (sActivityList) {
                            sActivityList.add(activity);
                        }
                    }

                    @Override
                    public void onActivityStarted(Activity activity) {
                    }

                    @Override
                    public void onActivityResumed(Activity activity) {
                    }

                    @Override
                    public void onActivityPaused(Activity activity) {
                    }

                    @Override
                    public void onActivityStopped(Activity activity) {
                    }

                    @Override
                    public void onActivitySaveInstanceState(Activity activity, Bundle bundle) {
                    }

                    @Override
                    public void onActivityDestroyed(Activity activity) {
                        synchronized (sActivityList) {
                            sActivityList.remove(activity);
                        }

                        synchronized (sActivityEventBusScopePool) {
                            sActivityEventBusScopePool.remove(activity);
                        }
                    }
                });
    }

    /**
     * Get the activity-scope EventBus instance
     */
    public static EventBus getDefault(Activity activity) {
        if (activity == null) {
            Log.e(TAG, "Can't find the Activity, the Activity is null!");
            return invalidEventBus();
        }

        synchronized (sActivityList) {
            if (!sActivityList.contains(activity)) {
                Log.e(TAG, "Can't find the Activity, it has been removed!");
                return invalidEventBus();
            }
        }

        EventBus eventBus = sActivityEventBusScopePool.get(activity);

        if (eventBus == null) {
            synchronized (sActivityEventBusScopePool) {
                if (sActivityEventBusScopePool.get(activity) == null) {
                    eventBus = new EventBus();
                    sActivityEventBusScopePool.put(activity, eventBus);
                }
            }
        }

        return eventBus;
    }

    private static EventBus invalidEventBus() {
        if (sInvalidEventBus == null) {
            synchronized (EventBusActivityScope.class) {
                if (sInvalidEventBus == null) {
                    sInvalidEventBus = new EventBus();

                }
            }
        }
        return sInvalidEventBus;
    }
}
