package me.yokeyword.sample;

import android.app.Application;

/**
 * Created by YoKey on 16/11/23.
 */
public class App extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        // init EventBus Index  建议配合build.gradle里apt{}开启
//        EventBus.builder()
//                .addIndex(new EventBusIndex())
//                .logNoSubscriberMessages(false)
//                .installDefaultEventBus();
    }
}
