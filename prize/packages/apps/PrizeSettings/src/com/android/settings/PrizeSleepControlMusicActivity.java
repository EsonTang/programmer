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
public class PrizeSleepControlMusicActivity extends PreferenceActivity implements
		Preference.OnPreferenceClickListener,
		Preference.OnPreferenceChangeListener {
	private static final String SLEEP_PREVIOUS_MUSIC = "sleep_previous_music";
	private static final String SLEEP_PALY_PAUSE_MUSIC = "sleep_play_pause_music";
	private static final String SLEEP_NEXT_MUSIC = "sleep_next_music";

	private Preference mPreviousMusic;
	private Preference mPlayPauseMusic;
	private Preference mNextMusic;
	String string;
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.activity_prize_control_music);
		mPreviousMusic = (Preference) findPreference(SLEEP_PREVIOUS_MUSIC);
		mPlayPauseMusic = (Preference) findPreference(SLEEP_PALY_PAUSE_MUSIC);
		mNextMusic = (Preference) findPreference(SLEEP_NEXT_MUSIC);

		string = getIntent().getExtras().getString("gesture");
		
		mPreviousMusic.setOnPreferenceChangeListener(this);
		mPlayPauseMusic.setOnPreferenceChangeListener(this);
		mNextMusic.setOnPreferenceChangeListener(this);
		prizeSetLayoutParams(getListView());
		
	}

	@Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
		Log.e("liup","onPreferenceTreeClick");
		Intent intent = new Intent();
		if (preference == mPreviousMusic) {
            intent.putExtra("preference", "mPreviousMusic");
        }
		if (preference == mPlayPauseMusic) {
			intent.putExtra("preference", "mPlayPauseMusic");
        }
		if (preference == mNextMusic) {
			intent.putExtra("preference", "mNextMusic");
        }
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		intent.putExtra("gesture", string);
		PrizeSleepGestureActivity.sMusicControlBool = true;
        intent.setClass(this,PrizeSleepGestureActivity.class);
        startActivity(intent);
		
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
