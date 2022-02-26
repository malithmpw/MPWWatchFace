package com.mpw.wearable.watch.mpwwatchface.config;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.mpw.wearable.watch.mpwwatchface.FontStyle;
import com.mpw.wearable.watch.mpwwatchface.R;

import java.util.ArrayList;

/**
 * Created by malith on 6/16/18.
 */

public class FontSelectionRecyclerViewAdapter extends
        RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final String TAG = FontSelectionRecyclerViewAdapter.class.getSimpleName();

    private ArrayList<FontStyle> mFontStyleOptionsDataSet;
    private String mSharedPrefString;

    public FontSelectionRecyclerViewAdapter(
            String sharedPrefString,
            ArrayList<FontStyle> fontStyleSettingsDataSet) {

        mSharedPrefString = sharedPrefString;
        mFontStyleOptionsDataSet = fontStyleSettingsDataSet;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Log.d(TAG, "onCreateViewHolder(): viewType: " + viewType);

        RecyclerView.ViewHolder viewHolder = new FontStyleViewHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.font_config_list_item, parent, false));
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int position) {
        Log.d(TAG, "Element " + position + " set.");

        FontStyle style = mFontStyleOptionsDataSet.get(position);
        FontStyleViewHolder fontStyleViewHolder = (FontStyleViewHolder) viewHolder;
        fontStyleViewHolder.setTextStyle(style.getStyle());
    }

    @Override
    public int getItemCount() {
        return mFontStyleOptionsDataSet.size();
    }

    /**
     * Displays color options for an item on the watch face and saves value to the
     * SharedPreference associated with it.
     */
    public class FontStyleViewHolder extends RecyclerView.ViewHolder
            implements View.OnClickListener {

        private TextView mFontStyleTextView;
        private Activity activity;

        public FontStyleViewHolder(final View view) {
            super(view);
            mFontStyleTextView = (TextView) view.findViewById(R.id.font_style_tv);
            mFontStyleTextView.setText("03:18");
            view.setOnClickListener(this);
            activity = (Activity) view.getContext();
            mFontStyleTextView.setTextColor(activity.getResources().getColor(R.color.white));
        }

        public void setTextStyle(String style) {
            Typeface fontTypeface = Typeface.createFromAsset(activity.getAssets(), style);
            mFontStyleTextView.setTypeface(fontTypeface);
        }

        @Override
        public void onClick(View view) {
            int position = getAdapterPosition();
            FontStyle style = mFontStyleOptionsDataSet.get(position);

            Log.d(TAG, "Font: " + style + " onClick() position: " + position);

            Activity activity = (Activity) view.getContext();

            if (mSharedPrefString != null && !mSharedPrefString.isEmpty()) {
                SharedPreferences sharedPref = activity.getSharedPreferences(
                        activity.getString(R.string.analog_complication_preference_file_key),
                        Context.MODE_PRIVATE);

                SharedPreferences.Editor editor = sharedPref.edit();
                editor.putString(mSharedPrefString, style.getStyle());
                editor.putInt("saved_text_size", style.getTextSize());
                editor.commit();
                Log.i(TAG, "Saved Font: " + style.getStyle() + " text size: " + style.getTextSize());
                // Let's Complication Config Activity know there was an update to colors.
                activity.setResult(Activity.RESULT_OK);
            }
            activity.finish();
        }
    }
}