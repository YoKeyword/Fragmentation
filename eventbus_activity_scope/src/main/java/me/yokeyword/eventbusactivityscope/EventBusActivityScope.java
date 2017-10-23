package me.yokeyword.eventbusactivityscope;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import org.greenrobot.eventbus.EventBus;

import java.util.HashMap;
import java.util.Map;
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

    private static final Map<Activity, EventBus> sActivityEventBusScopePool = new HashMap<>();

    static void init(Context context) {
        if (sInitialized.getAndSet(true)) {
            return;
        }

        ((Application) context.getApplicationContext())
                .registerActivityLifecycleCallbacks(new Application.ActivityLifecycleCallbacks() {
                    private Handler mainHandler = new Handler(Looper.getMainLooper());

                    @Override
                    public void onActivityCreated(Activity activity, Bundle bundle) {
                        synchronized (sActivityEventBusScopePool) {
                            sActivityEventBusScopePool.put(activity, null);
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
                    public void onActivityDestroyed(final Activity activity) {
                        if (!sActivityEventBusScopePool.containsKey(activity)) return;

                        mainHandler.post(new Runnable() { // Make sure Fragment's onDestroy() has been called.
                            @Override
                            public void run() {
                                synchronized (sActivityEventBusScopePool) {
                                    sActivityEventBusScopePool.remove(activity);
                                }
                            }
                        });
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

        synchronized (sActivityEventBusScopePool) {
            if (!sActivityEventBusScopePool.containsKey(activity)) {
                Log.e(TAG, "Can't find the Activity, it has been removed!");
                return invalidEventBus();
            }

            EventBus eventBus = sActivityEventBusScopePool.get(activity);
            if (eventBus == null) {
                eventBus = new EventBus();
                sActivityEventBusScopePool.put(activity, eventBus);
            }
            return eventBus;
        }
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
