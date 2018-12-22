package com.android.settings;

import android.os.Bundle;
import android.content.Intent;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceScreen;
import android.util.Log;

import android.widget.LinearLayout;
import android.widget.ListView;
import android.view.View;
public class PrizeSleepFunSelectActivity extends PreferenceActivity implements
		Preference.OnPreferenceClickListener,
		Preference.OnPreferenceChangeListener {
	private static final String SLEEP_OPEN_APP = "sleep_open_app";
	private static final String SLEEP_CONTROL_MUSIC = "sleep_control_music";
	private static final String SLEEP_UNLOCK_LOCK_SCREEN = "sleep_unlock_lock_screen";
	private static final String SLEEP_AWAKE = "sleep_awake";

	private Preference mOpenApp;
	private Preference mControlMusic;
	private Preference mUnlockLockScreen;
	private Preference mAwake;
	String string;
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.activity_prize_fun_select);
		mOpenApp = (Preference) findPreference(SLEEP_OPEN_APP);
		mControlMusic = (Preference) findPreference(SLEEP_CONTROL_MUSIC);
		mUnlockLockScreen = (Preference) findPreference(SLEEP_UNLOCK_LOCK_SCREEN);
		mAwake = (Preference) findPreference(SLEEP_AWAKE);

		mOpenApp.setOnPreferenceChangeListener(this);
		mControlMusic.setOnPreferenceChangeListener(this);
		mUnlockLockScreen.setOnPreferenceChangeListener(this);
		mAwake.setOnPreferenceChangeListener(this);
		prizeSetLayoutParams(getListView());
		string = getIntent().getExtras().getString("gesture");
	}

	@Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
		Log.e("liup","onPreferenceTreeClick");
		if (preference == mOpenApp) {
            Intent intent = new Intent();
            intent.putExtra("gesture", string);
            intent.setClass(this,PrizeOpenAppActivity.class);
            startActivity(intent);
        }
		if (preference == mControlMusic) {
            Intent intent = new Intent();
            intent.putExtra("gesture", string);
            intent.setClass(this,PrizeSleepControlMusicActivity.class);
            startActivity(intent);
        }
		if (preference == mUnlockLockScreen) {
			Intent intent = new Intent();
			intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);		
			intent.putExtra("gesture", string);
			PrizeSleepGestureActivity.sUnlockBool = true;
            intent.setClass(this,PrizeSleepGestureActivity.class);
            startActivity(intent);
        }
		if (preference == mAwake) {
			Intent intent = new Intent();
			intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);		
			intent.putExtra("gesture", string);
			PrizeSleepGestureActivity.sAwake = true;
            intent.setClass(this,PrizeSleepGestureActivity.class);
            startActivity(intent);
        }
		return false; 
	}
	@Override
	public boolean onPreferenceChange(Preference preference, Object newValue) {
		Log.e("liup","onPreferenceChange");
		return true;
	}

	@Override
	public boolean onPreferenceClick(Preference preference) {
		Log.e("liup","onPreferenceClick");
		return false;
	}
	
	public void prizeSetLayoutParams(ListView listview){
		 if(listview.getLayoutParams() instanceof LinearLayout.LayoutParams){
			LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,LinearLayout.LayoutParams.WRAP_CONTENT);
            int top = getResources().getDimensionPixelSize(R.dimen.prize_preferencefragment_card_magintop);
		   	int left = getResources().getDimensionPixelSize(R.dimen.prize_preferencefragment_card_maginleft);
		 	layoutParams.setMargins(left,top,left,top);
			listview.setLayoutParams(layoutParams);
			listview.setBackgroundResource(R.drawable.toponepreferencecategory_selector);
			listview.setDivider(getResources().getDrawable(R.drawable.prize_sleep_gesture_lines,null));
			listview.setDividerHeight(1);
         }
		
	}
}
