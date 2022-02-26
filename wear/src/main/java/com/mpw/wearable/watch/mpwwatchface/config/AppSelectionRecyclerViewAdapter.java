package com.mpw.wearable.watch.mpwwatchface.config;

import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.RecyclerView;
import android.support.wearable.view.CircledImageView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.mpw.wearable.watch.mpwwatchface.R;
import com.mpw.wearable.watch.mpwwatchface.model.AppInfo;

import java.util.ArrayList;

/**
 * Created by malith on 6/17/18.
 */

public class AppSelectionRecyclerViewAdapter extends
        RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final String TAG = AppSelectionRecyclerViewAdapter.class.getSimpleName();

    private ArrayList<AppInfo> appList;

    public AppSelectionRecyclerViewAdapter(
            ArrayList<AppInfo> apps) {
        appList = apps;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Log.d(TAG, "onCreateViewHolder(): viewType: " + viewType);

        RecyclerView.ViewHolder viewHolder = new AppSelectionRecyclerViewAdapter.AppViewHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.app_launcher_row, parent, false));
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int position) {
        Log.d(TAG, "Element " + position + " set.");

        AppInfo app = appList.get(position);
        AppSelectionRecyclerViewAdapter.AppViewHolder appViewHolder = (AppSelectionRecyclerViewAdapter.AppViewHolder) viewHolder;
        appViewHolder.appNameTextview.setText(app.getLabel());
        appViewHolder.setAppIcon(app.getIcon());
    }

    @Override
    public int getItemCount() {
        return appList.size();
    }

    /**
     * Displays color options for an item on the watch face and saves value to the
     * SharedPreference associated with it.
     */
    public class AppViewHolder extends RecyclerView.ViewHolder
            implements View.OnClickListener {

        private CircledImageView mAppIconCircleImageView;
        private TextView appNameTextview;

        public AppViewHolder(final View view) {
            super(view);
            mAppIconCircleImageView = (CircledImageView) view.findViewById(R.id.app_icon);
            appNameTextview = (TextView) view.findViewById(R.id.app_name);
            view.setOnClickListener(this);
        }

        public void setAppIcon(Drawable image) {
            mAppIconCircleImageView.setImageDrawable(image);
        }

        @Override
        public void onClick(View view) {
            int position = getAdapterPosition();
            AppInfo app = appList.get(position);

            Activity activity = (Activity) view.getContext();
            Intent launchIntent = activity.getPackageManager().getLaunchIntentForPackage(app.getPackageName().toString());
            activity.startActivity(launchIntent);
            activity.finish();

        }
    }
}