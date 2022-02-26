package com.mpw.wearable.watch.mpwwatchface;

import android.app.Application;

import com.mpw.wearable.watch.mpwwatchface.model.AppLauncherData;

/**
 * Created by malith on 6/18/18.
 */

public class MPWApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        AppLauncherData.instance(this);
    }
}
