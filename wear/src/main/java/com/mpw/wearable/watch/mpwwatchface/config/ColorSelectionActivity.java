package com.mpw.wearable.watch.mpwwatchface.config;

import android.app.Activity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.wear.widget.WearableRecyclerView;

import com.mpw.wearable.watch.mpwwatchface.R;
import com.mpw.wearable.watch.mpwwatchface.model.MPWComplicationConfigData;

/**
 * Allows user to select color for something on the watch face (background, highlight,etc.) and
 * saves it to {@link android.content.SharedPreferences} in
 * {@link android.support.v7.widget.RecyclerView.Adapter}.
 */
public class ColorSelectionActivity extends Activity {

    private static final String TAG = ColorSelectionActivity.class.getSimpleName();

    static final String EXTRA_SHARED_PREF =
            "com.mpw.wearable.watch.mpwwatchface.config.extra.EXTRA_SHARED_PREF";

    private WearableRecyclerView mConfigAppearanceWearableRecyclerView;

    private ColorSelectionRecyclerViewAdapter mColorSelectionRecyclerViewAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_color_selection_config);

        // Assigns SharedPreference String used to save color selected.
        String sharedPrefString = getIntent().getStringExtra(EXTRA_SHARED_PREF);

        mColorSelectionRecyclerViewAdapter = new ColorSelectionRecyclerViewAdapter(
                sharedPrefString,
                MPWComplicationConfigData.getColorOptionsDataSet());

        mConfigAppearanceWearableRecyclerView =
                (WearableRecyclerView) findViewById(R.id.wearable_recycler_view);

        // Aligns the first and last items on the list vertically centered on the screen.
        mConfigAppearanceWearableRecyclerView.setEdgeItemsCenteringEnabled(false);

        mConfigAppearanceWearableRecyclerView.setLayoutManager(new GridLayoutManager(this,3));

        // Improves performance because we know changes in content do not change the layout size of
        // the RecyclerView.
        mConfigAppearanceWearableRecyclerView.setHasFixedSize(true);

        mConfigAppearanceWearableRecyclerView.setAdapter(mColorSelectionRecyclerViewAdapter);
    }
}