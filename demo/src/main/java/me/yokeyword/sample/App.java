package me.yokeyword.sample;

import android.app.Application;

import org.greenrobot.eventbus.EventBus;

/**
 * Created by YoKey on 16/11/23.
 */
public class App extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        // init EventBus Index
        EventBus.builder()
                .addIndex(new EventBusIndex())
                .logNoSubscriberMessages(false)
                .installDefaultEventBus();
    }
}
