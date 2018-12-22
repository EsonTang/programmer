/*
* Nav bar color customized feature.
* created. prize-linkh-20150724
*/
package com.android.settings;

import android.content.Context;
import android.support.v7.preference.Preference;
import android.util.AttributeSet;
import android.content.res.TypedArray;

public class PrizeSwipeUpGestureStylePreference extends Preference {
    private static final String TAG = "PrizeSwipeUpGestureStylePreference";

    public PrizeSwipeUpGestureStylePreference(Context context, AttributeSet attrs,
            int defStyle) {
        super(context, attrs, defStyle);

        setLayoutResource(R.layout.preference_swipe_up_gesture_style_prize);
        setWidgetLayoutResource(R.layout.preference_widget_swipe_up_gesture_style_prize);
    }

    public PrizeSwipeUpGestureStylePreference(Context context, AttributeSet attrs) {
        this(context, attrs,
                com.android.internal.R.attr.checkBoxPreferenceStyle);
    }

    public PrizeSwipeUpGestureStylePreference(Context context) {
        this(context, null);
    }

}
