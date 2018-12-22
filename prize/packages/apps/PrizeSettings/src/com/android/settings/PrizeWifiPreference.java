package com.android.settings;

import android.content.Context;
import android.os.Handler;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceViewHolder;
import android.util.AttributeSet;
import android.widget.TextView;


public class PrizeWifiPreference extends Preference {
	public static final String TAG = "PrizeWifiPreference";

	public PrizeWifiPreference(Context context, AttributeSet attrs,int defStyle) {
		super(context, attrs, defStyle);
		// TODO Auto-generated constructor stub
	}
	
	public PrizeWifiPreference(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public PrizeWifiPreference(Context context) {
		super(context);
		setLayoutResource(R.layout.prize_wifi_layout_right);
		setWidgetLayoutResource(R.layout.prize_wifi_right_arrow);
	}


	@Override
    public void onBindViewHolder(PreferenceViewHolder view) {
        	super.onBindViewHolder(view);
	}
	
}
