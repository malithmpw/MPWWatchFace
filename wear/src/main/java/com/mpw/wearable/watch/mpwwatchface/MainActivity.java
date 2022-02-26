package com.mpw.wearable.watch.mpwwatchface;

import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.wear.widget.WearableRecyclerView;
import android.support.wearable.activity.WearableActivity;

import com.mpw.wearable.watch.mpwwatchface.config.AppSelectionRecyclerViewAdapter;
import com.mpw.wearable.watch.mpwwatchface.model.AppInfo;
import com.mpw.wearable.watch.mpwwatchface.model.AppLauncherData;

import java.util.ArrayList;

/**
 * Created by malith on 6/17/18.
 */

public class MainActivity extends WearableActivity {

    private WearableRecyclerView apps_recycle_view;
    private AppSelectionRecyclerViewAdapter mAppSelectionRecyclerViewAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_launcher_layout);
        apps_recycle_view = findViewById(R.id.recycler_view_launcher_apps);

        ArrayList<AppInfo> appsList = AppLauncherData.instance(this).getAppList();

        mAppSelectionRecyclerViewAdapter = new AppSelectionRecyclerViewAdapter(appsList);
        apps_recycle_view = findViewById(R.id.recycler_view_launcher_apps);

        apps_recycle_view.setLayoutManager(new LinearLayoutManager(this));

        apps_recycle_view.setCircularScrollingGestureEnabled(false);

        // Improves performance because we know changes in content do not change the layout size of
        // the RecyclerView.
        apps_recycle_view.setHasFixedSize(true);
        apps_recycle_view.setVerticalScrollBarEnabled(true);

        apps_recycle_view.setAdapter(mAppSelectionRecyclerViewAdapter);

    }
}
