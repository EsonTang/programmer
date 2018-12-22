package com.android.settings;

import android.content.Context;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceViewHolder;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;


public class PrizeNoRightArrowPreference extends Preference {
	public static final String TAG = "PrizeNoRightArrowPreference";
	
	
	public PrizeNoRightArrowPreference(Context context, AttributeSet attrs,
			int defStyle) {
		super(context, attrs, defStyle);
		// TODO Auto-generated constructor stub
	}


	public PrizeNoRightArrowPreference(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
	}


	public PrizeNoRightArrowPreference(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}


	@Override
	public void onBindViewHolder(PreferenceViewHolder view) {
		ImageView img = (ImageView)view.findViewById(R.id.settingsRightArrowWidget);
		if(null!=img)
			img.setVisibility(View.GONE);
		super.onBindViewHolder(view);
	}
}
