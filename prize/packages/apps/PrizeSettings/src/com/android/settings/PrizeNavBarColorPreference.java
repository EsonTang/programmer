/*
* Nav bar color customized feature.
* created. prize-linkh-20150724
*/
package com.android.settings;

import android.content.Context;
import android.support.v7.preference.Preference;
import android.util.AttributeSet;
import android.content.res.TypedArray;

public class PrizeNavBarColorPreference extends Preference {
    private static final String TAG = "PrizeNavBarColorPreference";

    public PrizeNavBarColorPreference(Context context, AttributeSet attrs,
            int defStyle) {
        super(context, attrs, defStyle);

        setLayoutResource(R.layout.preference_nav_bar_color_prize);
        setWidgetLayoutResource(R.layout.preference_widget_nav_color_panel_prize);
    }

    public PrizeNavBarColorPreference(Context context, AttributeSet attrs) {
        this(context, attrs,
                com.android.internal.R.attr.checkBoxPreferenceStyle);
    }

    public PrizeNavBarColorPreference(Context context) {
        this(context, null);
    }

}
