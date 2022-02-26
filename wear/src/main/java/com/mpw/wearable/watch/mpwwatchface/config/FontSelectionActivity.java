package com.mpw.wearable.watch.mpwwatchface.config;

import android.app.Activity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.wear.widget.WearableRecyclerView;

import com.mpw.wearable.watch.mpwwatchface.R;
import com.mpw.wearable.watch.mpwwatchface.model.MPWComplicationConfigData;

/**
 * Created by malith on 6/16/18.
 */

public class FontSelectionActivity extends Activity {

    private static final String TAG = ColorSelectionActivity.class.getSimpleName();

    static final String EXTRA_SHARED_PREF =
            "com.mpw.wearable.watch.mpwwatchface.config.extra.EXTRA_SHARED_PREF";

    private WearableRecyclerView mConfigAppearanceWearableRecyclerView;

    private FontSelectionRecyclerViewAdapter mFontSelectionRecyclerViewAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_color_selection_config);

        // Assigns SharedPreference String used to save color selected.
        String sharedPrefString = getIntent().getStringExtra(EXTRA_SHARED_PREF);

        mFontSelectionRecyclerViewAdapter = new FontSelectionRecyclerViewAdapter(
                sharedPrefString,
                MPWComplicationConfigData.getFontStyleOptionsDataSet());

        mConfigAppearanceWearableRecyclerView =
                (WearableRecyclerView) findViewById(R.id.wearable_recycler_view);

        // Aligns the first and last items on the list vertically centered on the screen.
        mConfigAppearanceWearableRecyclerView.setEdgeItemsCenteringEnabled(false);

        mConfigAppearanceWearableRecyclerView.setLayoutManager(new GridLayoutManager(this, 1));

        // Improves performance because we know changes in content do not change the layout size of
        // the RecyclerView.
        mConfigAppearanceWearableRecyclerView.setHasFixedSize(true);

        mConfigAppearanceWearableRecyclerView.setAdapter(mFontSelectionRecyclerViewAdapter);
    }
}
