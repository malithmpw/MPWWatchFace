package com.mpw.wearable.watch.mpwwatchface.model;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by malith on 6/17/18.
 */

public class AppLauncherData {
    private static AppLauncherData appLauncherData;
    private Context context;
    private ArrayList<AppInfo> appsList = new ArrayList<>();

    private AppLauncherData(Context context) {
        this.context = context;
        PackageManager pm = this.context.getPackageManager();
        Intent i = new Intent(Intent.ACTION_MAIN, null);
        i.addCategory(Intent.CATEGORY_LAUNCHER);

        List<ResolveInfo> allApps = pm.queryIntentActivities(i, 0);
        for (ResolveInfo ri : allApps) {
            if (!ri.activityInfo.packageName.equals("com.mpw.wearable.watch.mpwwatchface")) {
                AppInfo app = new AppInfo();
                app.setLabel(ri.loadLabel(pm));
                app.setPackageName(ri.activityInfo.packageName);
                app.setIcon(ri.activityInfo.loadIcon(pm));
                appsList.add(app);
            }
        }

    }

    public static AppLauncherData instance(Context context) {
        if (appLauncherData == null) {
            appLauncherData = new AppLauncherData(context);
        }
        return appLauncherData;
    }

    public ArrayList<AppInfo> getAppList() {
        return appsList;
    }
}
