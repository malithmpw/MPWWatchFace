/*
 * Copyright (C) 2017 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.mpw.wearable.watch.mpwwatchface.watchface;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.wearable.complications.ComplicationData;
import android.support.wearable.complications.rendering.ComplicationDrawable;
import android.support.wearable.watchface.CanvasWatchFaceService;
import android.support.wearable.watchface.WatchFaceService;
import android.support.wearable.watchface.WatchFaceStyle;
import android.util.Log;
import android.util.SparseArray;
import android.view.SurfaceHolder;
import android.view.WindowInsets;

import com.mpw.wearable.watch.mpwwatchface.R;
import com.mpw.wearable.watch.mpwwatchface.config.MPWComplicationConfigRecyclerViewAdapter;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

/**
 * Demonstrates two simple complications in a watch face.
 */
public class MPWComplicationWatchFaceService extends CanvasWatchFaceService {
    private static final String TAG = "MPWWatch";

    // Unique IDs for each complication. The settings activity that supports allowing users
    // to select their complication data provider requires numbers to be >= 0.
    private static final int BACKGROUND_COMPLICATION_ID = 0;
    private static final int TOP_LEFT_COMPLICATION_ID = 100;
    private static final int TOP_RIGHT_COMPLICATION_ID = 101;
    private static final int BOTTOM_LEFT_COMPLICATION_ID = 102;
    private static final int BOTTOM_RIGHT_COMPLICATION_ID = 103;
    private static final int BOTTOM_CENTER_COMPLICATION_ID = 104;

    // Background, Left and right complication IDs as array for Complication API.
    private static final int[] COMPLICATION_IDS = {
            BACKGROUND_COMPLICATION_ID, TOP_LEFT_COMPLICATION_ID, TOP_RIGHT_COMPLICATION_ID, BOTTOM_LEFT_COMPLICATION_ID, BOTTOM_RIGHT_COMPLICATION_ID, BOTTOM_CENTER_COMPLICATION_ID
    };

    // Left and right dial supported types.
    private static final int[][] COMPLICATION_SUPPORTED_TYPES = {
            {
                    ComplicationData.TYPE_LARGE_IMAGE
            },
            {
                    ComplicationData.TYPE_RANGED_VALUE,
                    ComplicationData.TYPE_ICON,
                    ComplicationData.TYPE_SHORT_TEXT,
                    ComplicationData.TYPE_SMALL_IMAGE
            },
            {
                    ComplicationData.TYPE_RANGED_VALUE,
                    ComplicationData.TYPE_ICON,
                    ComplicationData.TYPE_SHORT_TEXT,
                    ComplicationData.TYPE_SMALL_IMAGE
            },
            {
                    ComplicationData.TYPE_RANGED_VALUE,
                    ComplicationData.TYPE_ICON,
                    ComplicationData.TYPE_SHORT_TEXT,
                    ComplicationData.TYPE_SMALL_IMAGE
            },
            {
                    ComplicationData.TYPE_RANGED_VALUE,
                    ComplicationData.TYPE_ICON,
                    ComplicationData.TYPE_SHORT_TEXT,
                    ComplicationData.TYPE_SMALL_IMAGE
            },
            {
                    ComplicationData.TYPE_RANGED_VALUE,
                    ComplicationData.TYPE_ICON,
                    ComplicationData.TYPE_SHORT_TEXT,
                    ComplicationData.TYPE_SMALL_IMAGE
            }
    };

    // Used by {@link MPWComplicationConfigRecyclerViewAdapter} to check if complication location
    // is supported in settings config activity.
    public static int getComplicationId(
            MPWComplicationConfigRecyclerViewAdapter.ComplicationLocation complicationLocation) {
        // Add any other supported locations here.
        switch (complicationLocation) {
            case BACKGROUND:
                return BACKGROUND_COMPLICATION_ID;
            case TOP_LEFT:
                return TOP_LEFT_COMPLICATION_ID;
            case TOP_RIGHT:
                return TOP_RIGHT_COMPLICATION_ID;
            case BOTTOM_LEFT:
                return BOTTOM_LEFT_COMPLICATION_ID;
            case BOTTOM_RIGHT:
                return BOTTOM_RIGHT_COMPLICATION_ID;
            case BOTTOM_CENTER:
                return BOTTOM_CENTER_COMPLICATION_ID;
            default:
                return -1;
        }
    }

    // Used by {@link MPWComplicationConfigRecyclerViewAdapter} to retrieve all complication ids.
    public static int[] getComplicationIds() {
        return COMPLICATION_IDS;
    }

    // Used by {@link MPWComplicationConfigRecyclerViewAdapter} to see which complication types
    // are supported in the settings config activity.
    public static int[] getSupportedComplicationTypes(
            MPWComplicationConfigRecyclerViewAdapter.ComplicationLocation complicationLocation) {
        // Add any other supported locations here.
        switch (complicationLocation) {
            case BACKGROUND:
                return COMPLICATION_SUPPORTED_TYPES[0];
            case TOP_LEFT:
                return COMPLICATION_SUPPORTED_TYPES[1];
            case TOP_RIGHT:
                return COMPLICATION_SUPPORTED_TYPES[2];
            case BOTTOM_LEFT:
                return COMPLICATION_SUPPORTED_TYPES[3];
            case BOTTOM_RIGHT:
                return COMPLICATION_SUPPORTED_TYPES[4];
            case BOTTOM_CENTER:
                return COMPLICATION_SUPPORTED_TYPES[5];
            default:
                return new int[]{};
        }
    }

    /*
     * Update rate in milliseconds for interactive mode. We update once a second to advance the
     * second hand.
     */
    private static final long INTERACTIVE_UPDATE_RATE_MS = TimeUnit.SECONDS.toMillis(1);

    @Override
    public Engine onCreateEngine() {
        return new Engine();
    }

    private class Engine extends CanvasWatchFaceService.Engine {
        private static final int MSG_UPDATE_TIME = 0;
        private static final float SECOND_TICK_STROKE_WIDTH = 2f;

        //private static final float CENTER_GAP_AND_CIRCLE_RADIUS = 4f;

        private static final int SHADOW_RADIUS = 6;

        private Calendar mCalendar;
        private boolean mRegisteredTimeZoneReceiver = false;
        private boolean mMuteMode;

        private float mCenterX;
        private float mCenterY;

        // Colors for all hands (hour, minute, seconds, ticks) based on photo loaded.
        private int mWatchHandAndComplicationsColor;
        private int mWatchHandHighlightColor;
        private int mWatchHandShadowColor;

        private int mTimeTextColor;
        private int mDateTextColor;
        private String mTimeTextStyle;
        private int mTimeTextSize;

        private int mBackgroundColor;

        private Paint mSecondAndHighlightPaint;
        private Paint mTickAndCirclePaint;

        private Paint mBackgroundPaint;

        private Paint mTimeTextPaint;
        private Paint mDateTextPaint;
        private Paint mDateNameTextPaint;
        private Paint mNotificationTextPaint;

        private boolean mIsRound;
        private int mChinSize;

        /* Maps active complication ids to the data for that complication. Note: Data will only be
         * present if the user has chosen a provider via the settings activity for the watch face.
         */
        private SparseArray<ComplicationData> mActiveComplicationDataSparseArray;

        /* Maps complication ids to corresponding ComplicationDrawable that renders the
         * the complication data on the watch face.
         */
        private SparseArray<ComplicationDrawable> mComplicationDrawableSparseArray;

        private boolean mAmbient;
        private boolean mLowBitAmbient;
        private boolean mBurnInProtection;

        // Used to pull user's preferences for background color, highlight color, and visual
        // indicating there are unread notifications.
        SharedPreferences mSharedPref;

        // User's preference for if they want visual shown to indicate unread notifications.
        private boolean mUnreadNotificationsPreference;
        private int mNumberOfUnreadNotifications = 0;

        private final BroadcastReceiver mTimeZoneReceiver =
                new BroadcastReceiver() {
                    @Override
                    public void onReceive(Context context, Intent intent) {
                        mCalendar.setTimeZone(TimeZone.getDefault());
                        invalidate();
                    }
                };

        // Handler to update the time once a second in interactive mode.
        private final Handler mUpdateTimeHandler =
                new Handler() {
                    @Override
                    public void handleMessage(Message message) {
                        invalidate();
                        if (shouldTimerBeRunning()) {
                            long timeMs = System.currentTimeMillis();
                            long delayMs =
                                    INTERACTIVE_UPDATE_RATE_MS
                                            - (timeMs % INTERACTIVE_UPDATE_RATE_MS);
                            mUpdateTimeHandler.sendEmptyMessageDelayed(MSG_UPDATE_TIME, delayMs);
                        }
                    }
                };

        @Override
        public void onApplyWindowInsets(WindowInsets insets) {
            super.onApplyWindowInsets(insets);
            Configuration config = getResources().getConfiguration();
            mIsRound = config.isScreenRound();
            // mIsRound = insets.isRound();
            mChinSize = insets.getStableInsetBottom();
        }

        @Override
        public void onCreate(SurfaceHolder holder) {
            Log.d(TAG, "onCreate");
            super.onCreate(holder);

            // Used throughout watch face to pull user's preferences.
            Context context = getApplicationContext();
            mSharedPref =
                    context.getSharedPreferences(
                            getString(R.string.analog_complication_preference_file_key),
                            Context.MODE_PRIVATE);

            mCalendar = Calendar.getInstance();

            setWatchFaceStyle(
                    new WatchFaceStyle.Builder(MPWComplicationWatchFaceService.this)
                            .setAcceptsTapEvents(true)
                            .setHideNotificationIndicator(true)
                            .build());

            loadSavedPreferences();
            initializeComplicationsAndBackground();
            initializeWatchFace();
        }


        // Pulls all user's preferences for watch face appearance.
        private void loadSavedPreferences() {

            String backgroundColorResourceName = getApplicationContext().getString(R.string.saved_background_color);
            mBackgroundColor = mSharedPref.getInt(backgroundColorResourceName, Color.BLACK);

            String markerColorResourceName = getApplicationContext().getString(R.string.saved_marker_color);
            // Set defaults for colors
            mWatchHandHighlightColor = mSharedPref.getInt(markerColorResourceName, Color.parseColor("#009688"));

            String timeColor = getApplicationContext().getString(R.string.saved_time_color);
            mTimeTextColor = mSharedPref.getInt(timeColor, Color.WHITE);

            String timeTextStyle = getApplicationContext().getString(R.string.saved_text_style);
            String timeTextSize = getApplicationContext().getString(R.string.saved_text_size);
            mTimeTextStyle = mSharedPref.getString(timeTextStyle, "fonts/Roboto-Medium.ttf");


            mTimeTextSize = mSharedPref.getInt(timeTextSize, 105);
            // mTimeTextSize = (int) mCenterX / 2;

            Log.i("Font Loaded:", mTimeTextStyle + " Size:" + mTimeTextSize);

            String dateColor = getApplicationContext().getString(R.string.saved_date_color);
            mDateTextColor = mSharedPref.getInt(dateColor, Color.WHITE);

            if (mBackgroundColor == Color.WHITE) {
                mWatchHandAndComplicationsColor = Color.BLACK;
                mWatchHandShadowColor = Color.WHITE;
                mTimeTextColor = Color.BLACK;
                mDateTextColor = Color.BLACK;
            } else {
                mWatchHandAndComplicationsColor = Color.WHITE;
                mWatchHandShadowColor = Color.BLACK;
            }

            String unreadNotificationPreferenceResourceName =
                    getApplicationContext().getString(R.string.saved_unread_notifications_pref);

            mUnreadNotificationsPreference =
                    mSharedPref.getBoolean(unreadNotificationPreferenceResourceName, true);
        }

        private void initializeComplicationsAndBackground() {
            Log.d(TAG, "initializeComplications()");

            // Initialize background color (in case background complication is inactive).
            mBackgroundPaint = new Paint();
            mBackgroundPaint.setColor(mBackgroundColor);

            mActiveComplicationDataSparseArray = new SparseArray<>(COMPLICATION_IDS.length);

            // Creates a ComplicationDrawable for each location where the user can render a
            // complication on the watch face. In this watch face, we create one for left, right,
            // and background, but you could add many more.
            ComplicationDrawable leftComplicationDrawable = new ComplicationDrawable(getApplicationContext());
            ComplicationDrawable rightComplicationDrawable = new ComplicationDrawable(getApplicationContext());
            ComplicationDrawable topComplicationDrawable = new ComplicationDrawable(getApplicationContext());
            ComplicationDrawable bottomComplicationDrawable = new ComplicationDrawable(getApplicationContext());
            ComplicationDrawable centerComplicationDrawable = new ComplicationDrawable(getApplicationContext());
            ComplicationDrawable backgroundComplicationDrawable = new ComplicationDrawable(getApplicationContext());

            // Adds new complications to a SparseArray to simplify setting styles and ambient
            // properties for all complications, i.e., iterate over them all.
            mComplicationDrawableSparseArray = new SparseArray<>(COMPLICATION_IDS.length);

            mComplicationDrawableSparseArray.put(TOP_LEFT_COMPLICATION_ID, leftComplicationDrawable);
            mComplicationDrawableSparseArray.put(TOP_RIGHT_COMPLICATION_ID, rightComplicationDrawable);
            mComplicationDrawableSparseArray.put(BOTTOM_LEFT_COMPLICATION_ID, topComplicationDrawable);
            mComplicationDrawableSparseArray.put(BOTTOM_RIGHT_COMPLICATION_ID, bottomComplicationDrawable);
            mComplicationDrawableSparseArray.put(BOTTOM_CENTER_COMPLICATION_ID, centerComplicationDrawable);
            mComplicationDrawableSparseArray.put(BACKGROUND_COMPLICATION_ID, backgroundComplicationDrawable);

            setComplicationsActiveAndAmbientColors(mWatchHandHighlightColor);
            setActiveComplications(COMPLICATION_IDS);
        }

        private void initializeWatchFace() {

            mSecondAndHighlightPaint = new Paint();
            mSecondAndHighlightPaint.setColor(mWatchHandHighlightColor);
            mSecondAndHighlightPaint.setStrokeWidth(SECOND_TICK_STROKE_WIDTH);
            mSecondAndHighlightPaint.setAntiAlias(true);
            mSecondAndHighlightPaint.setStrokeCap(Paint.Cap.ROUND);
            mSecondAndHighlightPaint.setShadowLayer(SHADOW_RADIUS, 0, 0, mWatchHandShadowColor);

            mTickAndCirclePaint = new Paint();
            mTickAndCirclePaint.setColor(mWatchHandAndComplicationsColor);
            mTickAndCirclePaint.setStrokeWidth(SECOND_TICK_STROKE_WIDTH);
            mTickAndCirclePaint.setAntiAlias(true);
            mTickAndCirclePaint.setStyle(Paint.Style.STROKE);
            mTickAndCirclePaint.setShadowLayer(SHADOW_RADIUS, 0, 0, mWatchHandShadowColor);


            Resources resources = MPWComplicationWatchFaceService.this.getResources();

            Typeface timeTextTypeface = Typeface.createFromAsset(getAssets(), mTimeTextStyle);
            Typeface dateTextTypeface = Typeface.createFromAsset(getAssets(), "fonts/NexaLight.ttf");

            mTimeTextPaint = new Paint();
            mTimeTextPaint.setColor(mTimeTextColor);
            mTimeTextPaint.setAntiAlias(true);
            mTimeTextPaint.setTextAlign(Paint.Align.CENTER);
            mTimeTextPaint.setTextSize(mTimeTextSize);
            mTimeTextPaint.setTypeface(timeTextTypeface);
            mTimeTextPaint.setFakeBoldText(true);

            mDateTextPaint = new Paint();
            mDateTextPaint.setColor(mDateTextColor);
            mDateTextPaint.setAntiAlias(true);
            mDateTextPaint.setTextAlign(Paint.Align.CENTER);
            mDateTextPaint.setTextSize(resources.getDimension(R.dimen.digital_date_text_size));
            mDateTextPaint.setTypeface(dateTextTypeface);

            mDateNameTextPaint = new Paint();
            mDateNameTextPaint.setColor(mDateTextColor);
            mDateNameTextPaint.setAntiAlias(true);
            mDateNameTextPaint.setTextAlign(Paint.Align.CENTER);
            mDateNameTextPaint.setTextSize(resources.getDimension(R.dimen.digital_date_name_text_size));
            mDateNameTextPaint.setTypeface(dateTextTypeface);

            mNotificationTextPaint = new Paint();
            mNotificationTextPaint.setColor(mDateTextColor);
            mNotificationTextPaint.setAntiAlias(true);
            mNotificationTextPaint.setTextAlign(Paint.Align.CENTER);
            mNotificationTextPaint.setTextSize(resources.getDimension(R.dimen.digital_notification_text_size));
            mNotificationTextPaint.setTypeface(dateTextTypeface);
        }

        /* Sets active/ambient mode colors for all complications.
         *
         * Note: With the rest of the watch face, we update the paint colors based on
         * ambient/active mode callbacks, but because the ComplicationDrawable handles
         * the active/ambient colors, we only set the colors twice. Once at initialization and
         * again if the user changes the highlight color via MPWComplicationConfigActivity.
         */
        private void setComplicationsActiveAndAmbientColors(int primaryComplicationColor) {
            int complicationId;
            ComplicationDrawable complicationDrawable;

            for (int i = 0; i < COMPLICATION_IDS.length; i++) {
                complicationId = COMPLICATION_IDS[i];
                complicationDrawable = mComplicationDrawableSparseArray.get(complicationId);

                if (complicationId == BACKGROUND_COMPLICATION_ID) {
                    // It helps for the background color to be black in case the image used for the
                    // watch face's background takes some time to load.
                    complicationDrawable.setBackgroundColorActive(Color.BLACK);
                } else {
                    // Active mode colors.
                    complicationDrawable.setBorderColorActive(primaryComplicationColor);
                    complicationDrawable.setRangedValuePrimaryColorActive(primaryComplicationColor);

                    // Ambient mode colors.
                    complicationDrawable.setBorderColorAmbient(Color.WHITE);
                    complicationDrawable.setRangedValuePrimaryColorAmbient(Color.WHITE);
                }
            }
        }

        @Override
        public void onDestroy() {
            mUpdateTimeHandler.removeMessages(MSG_UPDATE_TIME);
            super.onDestroy();
        }

        @Override
        public void onPropertiesChanged(Bundle properties) {
            super.onPropertiesChanged(properties);
            Log.d(TAG, "onPropertiesChanged: low-bit ambient = " + mLowBitAmbient);

            mLowBitAmbient = properties.getBoolean(PROPERTY_LOW_BIT_AMBIENT, false);
            mBurnInProtection = properties.getBoolean(PROPERTY_BURN_IN_PROTECTION, false);

            // Updates complications to properly render in ambient mode based on the
            // screen's capabilities.
            ComplicationDrawable complicationDrawable;

            for (int i = 0; i < COMPLICATION_IDS.length; i++) {
                complicationDrawable = mComplicationDrawableSparseArray.get(COMPLICATION_IDS[i]);

                complicationDrawable.setLowBitAmbient(mLowBitAmbient);
                complicationDrawable.setBurnInProtection(mBurnInProtection);
            }
        }

        /*
         * Called when there is updated data for a complication id.
         */
        @Override
        public void onComplicationDataUpdate(
                int complicationId, ComplicationData complicationData) {
            Log.d(TAG, "onComplicationDataUpdate() id: " + complicationId);

            // Adds/updates active complication data in the array.
            mActiveComplicationDataSparseArray.put(complicationId, complicationData);

            // Updates correct ComplicationDrawable with updated data.
            ComplicationDrawable complicationDrawable =
                    mComplicationDrawableSparseArray.get(complicationId);
            complicationDrawable.setComplicationData(complicationData);

            invalidate();
        }

        @Override
        public void onTapCommand(int tapType, int x, int y, long eventTime) {
            Log.d(TAG, "OnTapCommand()");
            switch (tapType) {
                case TAP_TYPE_TAP:

                    // If your background complication is the first item in your array, you need
                    // to walk backward through the array to make sure the tap isn't for a
                    // complication above the background complication.
                    for (int i = COMPLICATION_IDS.length - 1; i >= 0; i--) {
                        int complicationId = COMPLICATION_IDS[i];
                        ComplicationDrawable complicationDrawable =
                                mComplicationDrawableSparseArray.get(complicationId);

                        boolean successfulTap = complicationDrawable.onTap(x, y);

                        if (successfulTap) {
                            return;
                        }
                    }
                    break;
            }
        }

        @Override
        public void onTimeTick() {
            super.onTimeTick();
            invalidate();
        }

        @Override
        public void onAmbientModeChanged(boolean inAmbientMode) {
            super.onAmbientModeChanged(inAmbientMode);
            Log.d(TAG, "onAmbientModeChanged: " + inAmbientMode);

            mAmbient = inAmbientMode;

            updateWatchPaintStyles();

            // Update drawable complications' ambient state.
            // Note: ComplicationDrawable handles switching between active/ambient colors, we just
            // have to inform it to enter ambient mode.
            ComplicationDrawable complicationDrawable;

            for (int i = 0; i < COMPLICATION_IDS.length; i++) {
                complicationDrawable = mComplicationDrawableSparseArray.get(COMPLICATION_IDS[i]);
                complicationDrawable.setInAmbientMode(mAmbient);
            }

            // Check and trigger whether or not timer should be running (only in active mode).
            updateTimer();
        }

        private void updateWatchPaintStyles() {
            Typeface timeTextTypeface = Typeface.createFromAsset(getAssets(), mTimeTextStyle);
            if (mAmbient) {

                mBackgroundPaint.setColor(Color.BLACK);
                mSecondAndHighlightPaint.setColor(Color.WHITE);
                mSecondAndHighlightPaint.setAntiAlias(false);
                mSecondAndHighlightPaint.clearShadowLayer();
                mTickAndCirclePaint.clearShadowLayer();
                mTimeTextPaint.setColor(Color.WHITE);
                mTimeTextPaint.setTypeface(timeTextTypeface);
                mTimeTextPaint.setTextSize(mTimeTextSize);
                mDateTextPaint.setColor(Color.WHITE);
                mDateNameTextPaint.setColor(Color.WHITE);
                mNotificationTextPaint.setColor(Color.WHITE);

            } else {

                mBackgroundPaint.setColor(mBackgroundColor);
                mTickAndCirclePaint.setColor(mWatchHandAndComplicationsColor);
                mSecondAndHighlightPaint.setColor(mWatchHandHighlightColor);
                mSecondAndHighlightPaint.setAntiAlias(true);
                mTickAndCirclePaint.setAntiAlias(true);
                mSecondAndHighlightPaint.setShadowLayer(SHADOW_RADIUS, 0, 0, mWatchHandShadowColor);
                mTickAndCirclePaint.setShadowLayer(SHADOW_RADIUS, 0, 0, mWatchHandShadowColor);
                mTimeTextPaint.setColor(mTimeTextColor);
                mTimeTextPaint.setTypeface(timeTextTypeface);
                mTimeTextPaint.setTextSize(mTimeTextSize);
                mDateTextPaint.setColor(mDateTextColor);
                mDateNameTextPaint.setColor(mDateTextColor);
                mNotificationTextPaint.setColor(mDateTextColor);
            }
        }

        @Override
        public void onInterruptionFilterChanged(int interruptionFilter) {
            super.onInterruptionFilterChanged(interruptionFilter);
            boolean inMuteMode = (interruptionFilter == WatchFaceService.INTERRUPTION_FILTER_NONE);

            /* Dim display in mute mode. */
            if (mMuteMode != inMuteMode) {
                mMuteMode = inMuteMode;
                mSecondAndHighlightPaint.setAlpha(inMuteMode ? 80 : 255);
                invalidate();
            }
        }

        @Override
        public void onSurfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            super.onSurfaceChanged(holder, format, width, height);
            /*
             * Find the coordinates of the center point on the screen, and ignore the window
             * insets, so that, on round watches with a "chin", the watch face is centered on the
             * entire screen, not just the usable portion.
             */
            mCenterX = width / 2f;
            mCenterY = height / 2f;
            placeComplicationsOnWatchFace(width, height);
        }

        private boolean isScreenRound() {
            Configuration config = getResources().getConfiguration();
            return config.isScreenRound();
        }

        private void placeComplicationsOnWatchFace(int width, int height) {
            mCenterX = width / 2f;
            mCenterY = height / 2f;
            mIsRound = isScreenRound();
            Log.i("faceType", "isRound :" + mIsRound + "  chinSize :" + mChinSize + "  width: " + width + "  height : " + height);

            /*
             * Calculates location bounds for right and left circular complications. Please note,
             * we are not demonstrating a long text complication in this watch face.
             *
             * We suggest using at least 1/4 of the screen width for circular (or squared)
             * complications and 2/3 of the screen width for wide rectangular complications for
             * better readability.
             */

            // For most Wear devices, width and height are the same, so we just chose one (width).
            int unit = width / 32;
            int sizeOfComplication = (width * 3) / 16;
            int midpointOfScreen = width / 2;

            int horizontalOffset = (midpointOfScreen) / 8;
            int verticalOffset = midpointOfScreen;

            Rect topLeftBounds = null;
            Rect topRightBounds = null;
            Rect bottomLeftBounds = null;
            Rect bottomRightBounds = null;
            Rect bottomCenterBounds = null;

            if (mIsRound) {
                topLeftBounds =
                        // Left, Top, Right, Bottom
                        new Rect(
                                horizontalOffset,
                                verticalOffset,
                                (horizontalOffset + sizeOfComplication),
                                (verticalOffset + sizeOfComplication));

                topRightBounds =
                        // Left, Top, Right, Bottom
                        new Rect(
                                (width - sizeOfComplication - horizontalOffset),
                                verticalOffset,
                                (width - horizontalOffset),
                                (verticalOffset + sizeOfComplication));

                bottomLeftBounds =
                        // Left, Top, Right, Bottom
                        new Rect(
                                (horizontalOffset * 3),
                                (verticalOffset + (horizontalOffset * 3)),
                                ((horizontalOffset * 3) + sizeOfComplication),
                                (verticalOffset + (horizontalOffset * 3) + sizeOfComplication));

                bottomRightBounds =
                        // Left, Top, Right, Bottom
                        new Rect(
                                width - ((horizontalOffset * 3) + sizeOfComplication),
                                (verticalOffset + (horizontalOffset * 3)),
                                (width - (horizontalOffset * 3)),
                                (verticalOffset + (horizontalOffset * 3) + sizeOfComplication));

                bottomCenterBounds =
                        // Left, Top, Right, Bottom
                        new Rect(
                                (midpointOfScreen - (sizeOfComplication / 2)),
                                (verticalOffset + (horizontalOffset * 3) + 10),
                                (midpointOfScreen + (sizeOfComplication / 2)),
                                (verticalOffset + (horizontalOffset * 3) + sizeOfComplication) + 10);
            } else {
                topLeftBounds =
                        // Left, Top, Right, Bottom
                        new Rect(
                                (midpointOfScreen - (horizontalOffset * 4) - sizeOfComplication - unit),
                                (verticalOffset + (horizontalOffset * 4) - sizeOfComplication + unit),
                                ((horizontalOffset * 4) - unit),
                                (verticalOffset + (horizontalOffset * 4) + unit));

                topRightBounds =
                        // Left, Top, Right, Bottom
                        new Rect(
                                (midpointOfScreen + (horizontalOffset * 4) + unit),
                                (verticalOffset + (horizontalOffset * 4) - sizeOfComplication + unit),
                                (midpointOfScreen + (horizontalOffset * 4) + sizeOfComplication + unit),
                                (verticalOffset + (horizontalOffset * 4) + unit));


                bottomLeftBounds =
                        // Left, Top, Right, Bottom
                        new Rect(
                                (midpointOfScreen - (horizontalOffset * 2) - sizeOfComplication),
                                (verticalOffset + (horizontalOffset * 4)),
                                (midpointOfScreen - (horizontalOffset * 2)),
                                (verticalOffset + (horizontalOffset * 4) + sizeOfComplication));

                bottomRightBounds =
                        // Left, Top, Right, Bottom
                        new Rect(
                                (verticalOffset + (horizontalOffset * 2)),
                                (verticalOffset + (horizontalOffset * 4)),
                                (verticalOffset + (horizontalOffset * 2) + sizeOfComplication),
                                (verticalOffset + (horizontalOffset * 4) + sizeOfComplication));

                bottomCenterBounds =
                        // Left, Top, Right, Bottom
                        new Rect(
                                (midpointOfScreen - (sizeOfComplication / 2)),
                                (verticalOffset + (horizontalOffset * 4)),
                                (midpointOfScreen + (sizeOfComplication / 2)),
                                (verticalOffset + (horizontalOffset * 4) + sizeOfComplication));
            }


            ComplicationDrawable leftComplicationDrawable = mComplicationDrawableSparseArray.get(TOP_LEFT_COMPLICATION_ID);
            leftComplicationDrawable.setBounds(topLeftBounds);

            ComplicationDrawable rightComplicationDrawable = mComplicationDrawableSparseArray.get(TOP_RIGHT_COMPLICATION_ID);
            rightComplicationDrawable.setBounds(topRightBounds);

            ComplicationDrawable topComplicationDrawable = mComplicationDrawableSparseArray.get(BOTTOM_LEFT_COMPLICATION_ID);
            topComplicationDrawable.setBounds(bottomLeftBounds);

            ComplicationDrawable bottomComplicationDrawable = mComplicationDrawableSparseArray.get(BOTTOM_RIGHT_COMPLICATION_ID);
            bottomComplicationDrawable.setBounds(bottomRightBounds);

            ComplicationDrawable centerComplicationDrawable = mComplicationDrawableSparseArray.get(BOTTOM_CENTER_COMPLICATION_ID);
            centerComplicationDrawable.setBounds(bottomCenterBounds);


            Rect screenForBackgroundBound =
                    // Left, Top, Right, Bottom
                    new Rect(0, 0, width, height);

            ComplicationDrawable backgroundComplicationDrawable =
                    mComplicationDrawableSparseArray.get(BACKGROUND_COMPLICATION_ID);
            backgroundComplicationDrawable.setBounds(screenForBackgroundBound);
        }

        @Override
        public void onDraw(Canvas canvas, Rect bounds) {
            long now = System.currentTimeMillis();
            mCalendar.setTimeInMillis(now);
            drawBackground(canvas);
            drawComplications(canvas, now);
            drawUnreadNotificationIcon(canvas);
            drawTimeText(canvas);
            drawDate(canvas);
            // canvas.drawCircle(mCenterX, mCenterY, 190, mBackgroundPaint);
        }

        private void drawDate(Canvas canvas) {
            Date date = mCalendar.getTime();
            String dateString = new SimpleDateFormat("YYYY-MM-dd", Locale.getDefault()).format(date);
            String dayOfWeek = new SimpleDateFormat("EEEE", Locale.ENGLISH).format(date);
            if (mAmbient) {
                canvas.drawText(dateString, mCenterX, mCenterY + 50f, mDateTextPaint);
                canvas.drawText(dayOfWeek, mCenterX, mCenterY + 80f, mDateNameTextPaint);
            } else {
                canvas.drawText(dateString, mCenterX, mCenterY + 25f, mDateTextPaint);
                canvas.drawText(dayOfWeek, mCenterX, mCenterY + 60f, mDateNameTextPaint);
            }
        }

        private void drawTimeText(Canvas canvas) {
            SimpleDateFormat time1 = new SimpleDateFormat("kk:mm");
            String timeText = time1.format(System.currentTimeMillis());
            if (mAmbient) {
                canvas.drawText(timeText, mCenterX, mCenterY + 10, mTimeTextPaint);
            } else {
                canvas.drawText(timeText, mCenterX, mCenterY - 20, mTimeTextPaint);
            }
        }

        private void drawUnreadNotificationIcon(Canvas canvas) {

            if (mUnreadNotificationsPreference && (mNumberOfUnreadNotifications > 0)) {
                String notificationText;
                if (mNumberOfUnreadNotifications == 1) {
                    notificationText = "1 Notification";
                } else {
                    notificationText = mNumberOfUnreadNotifications + " Notifications";
                }
                /*
                 * Ensure center highlight circle is only drawn in interactive mode. This ensures
                 * we don't burn the screen with a solid circle in ambient mode.
                 */
                if (mAmbient) {
                    canvas.drawText(notificationText, canvas.getWidth() / 2, 110, mNotificationTextPaint);
                } else {
                    canvas.drawText(notificationText, canvas.getWidth() / 2, 80, mNotificationTextPaint);
                }
            } else {
                String notificationText = "MPW Watch Face";
                canvas.drawText(notificationText, canvas.getWidth() / 2, (canvas.getWidth() / 6), mNotificationTextPaint);
            }
        }

        private void drawBackground(Canvas canvas) {

            if (mAmbient && (mLowBitAmbient || mBurnInProtection)) {
                canvas.drawColor(Color.WHITE);
                canvas.drawCircle(mCenterX, mCenterY, 195, new Paint(Color.BLACK));
            } else {
                canvas.drawColor(mWatchHandHighlightColor);
                canvas.drawCircle(mCenterX, mCenterY, 195, mBackgroundPaint);
            }
        }

        private void drawComplications(Canvas canvas, long currentTimeMillis) {
            int complicationId;
            ComplicationDrawable complicationDrawable;
            if (!mAmbient) {
                for (int i = 0; i < COMPLICATION_IDS.length; i++) {
                    complicationId = COMPLICATION_IDS[i];
                    complicationDrawable = mComplicationDrawableSparseArray.get(complicationId);
                    complicationDrawable.draw(canvas, currentTimeMillis);
                }
            }
        }

        @Override
        public void onVisibilityChanged(boolean visible) {
            super.onVisibilityChanged(visible);

            if (visible) {

                // Preferences might have changed since last time watch face was visible.
                loadSavedPreferences();

                // With the rest of the watch face, we update the paint colors based on
                // ambient/active mode callbacks, but because the ComplicationDrawable handles
                // the active/ambient colors, we only need to update the complications' colors when
                // the user actually makes a change to the highlight color, not when the watch goes
                // in and out of ambient mode.
                setComplicationsActiveAndAmbientColors(mWatchHandHighlightColor);
                updateWatchPaintStyles();

                registerReceiver();
                // Update time zone in case it changed while we weren't visible.
                mCalendar.setTimeZone(TimeZone.getDefault());
                invalidate();
            } else {
                unregisterReceiver();
            }

            /* Check and trigger whether or not timer should be running (only in active mode). */
            updateTimer();
        }

        @Override
        public void onUnreadCountChanged(int count) {
            Log.d(TAG, "onUnreadCountChanged(): " + count);

            if (mUnreadNotificationsPreference) {

                if (mNumberOfUnreadNotifications != count) {
                    mNumberOfUnreadNotifications = count;
                    invalidate();
                }
            }
        }

        private void registerReceiver() {
            if (mRegisteredTimeZoneReceiver) {
                return;
            }
            mRegisteredTimeZoneReceiver = true;
            IntentFilter filter = new IntentFilter(Intent.ACTION_TIMEZONE_CHANGED);
            MPWComplicationWatchFaceService.this.registerReceiver(mTimeZoneReceiver, filter);
        }

        private void unregisterReceiver() {
            if (!mRegisteredTimeZoneReceiver) {
                return;
            }
            mRegisteredTimeZoneReceiver = false;
            MPWComplicationWatchFaceService.this.unregisterReceiver(mTimeZoneReceiver);
        }

        /**
         * Starts/stops the {@link #mUpdateTimeHandler} timer based on the state of the watch face.
         */
        private void updateTimer() {
            mUpdateTimeHandler.removeMessages(MSG_UPDATE_TIME);
            if (shouldTimerBeRunning()) {
                mUpdateTimeHandler.sendEmptyMessage(MSG_UPDATE_TIME);
            }
        }

        /**
         * Returns whether the {@link #mUpdateTimeHandler} timer should be running. The timer should
         * only run in active mode.
         */
        private boolean shouldTimerBeRunning() {
            return isVisible() && !mAmbient;
        }

    }
}
