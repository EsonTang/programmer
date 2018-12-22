package com.android.settings;

import android.net.Uri;
import android.os.Bundle;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.view.Menu;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.preference.Preference;
//import android.preference.CheckBoxPreference;
import android.preference.PrizeSwitchPreference;
import com.mediatek.settings.sim.RadioPowerPreference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceScreen;
import android.util.Log;
import android.content.ContentResolver;
import android.provider.Settings;
import java.io.IOException;
//import com.mediatek.wifi.hotspot.HotspotSwitchPreference;
import android.view.KeyEvent;
import android.app.ActionBar;
import android.widget.Switch;
import android.view.Gravity;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.ComponentName;
import java.io.*;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.view.View;
import android.view.MenuItem;
public class PrizeSleepGestureActivity extends PreferenceActivity implements
		Preference.OnPreferenceClickListener,
		Preference.OnPreferenceChangeListener{
	private static final String MAIN_PREFERENCE_KEY = "main_preference_key";
	private PreferenceScreen mPreferenceScreen;
	private ContentResolver mContentResolver;
	public static boolean sAppBool = false;
	public static boolean sUnlockBool = false;
	public static boolean sMusicControlBool = false;
	public static boolean sAwake = false;
	private String item;
	private String summary;
	private static int bGesture = 0;
	private Switch actionBarSwitch;
	private static boolean bSleepGesture;
	private ContentValues values = new ContentValues();
	private static Uri queryUri = Uri.parse("content://com.prize.sleepgesture/sleepgesture");
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.activity_prize_sleep_gesture);
		mPreferenceScreen = (PreferenceScreen) findPreference(MAIN_PREFERENCE_KEY);
		mContentResolver = getContentResolver();
		addActionBar();
		initSelectWhat();
		initPreferences();
		prizeSetLayoutParams(getListView());
		
		
		onoff_sleep_gesture();
	}
	private void initPreferences() {

		Cursor cursor = mContentResolver.query(queryUri, null, null, null,null);
		if (cursor != null && cursor.moveToFirst()) {
			do {
				PrizeSwitchPreference mPrizeSwitchPreference = new PrizeSwitchPreference(this);
				String key = cursor.getString(cursor.getColumnIndex("item"));
				mPrizeSwitchPreference.setKey(key);

				String name = cursor.getString(cursor.getColumnIndex("name"));
				mPrizeSwitchPreference.setTitle(getItemTitle(name));
				
				String packageName = cursor.getString(cursor.getColumnIndex("packagename"));
				String className = cursor.getString(cursor.getColumnIndex("classname"));
				
				if(packageName.equals("unlock")){
					summary = this.getString(R.string.sleep_unlock_lock_screen);
				}else if(packageName.equals("mPreviousMusic")){
					summary = this.getString(R.string.sleep_previous_music);
				}else if(packageName.equals("mPlayPauseMusic")){
					summary = this.getString(R.string.sleep_play_pause_music);
				}else if(packageName.equals("mNextMusic")){
					summary = this.getString(R.string.sleep_next_music);
				}else if(packageName.equals("awake")){
					summary = this.getString(R.string.sleep_awake);
				}else{
					try {
						PackageManager packageManager = getPackageManager();
						ComponentName componentName = new ComponentName(packageName, className);
						ActivityInfo info = packageManager.getActivityInfo(componentName, 0);
						summary = info.loadLabel(packageManager).toString();
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
				int onoff = cursor.getInt(cursor.getColumnIndex("onoff"));
				mPrizeSwitchPreference.setSummary(summary);
				mPrizeSwitchPreference.setPrizeOn(onoff == 1);
				mPrizeSwitchPreference.setOnPreferenceClickListener(this);
				mPrizeSwitchPreference.setOnPreferenceChangeListener(this);
				mPrizeSwitchPreference.enablePreferenceChange(true);


				mPreferenceScreen.addPreference(mPrizeSwitchPreference);
				
				Log.e("liup", "gestureStr key=" + key + " packageName ="
						+ packageName + " classNmae = " + className
						+ " onoff= " + onoff);
			} while (cursor.moveToNext());
		}
		if(cursor != null) {
			cursor.close();
		}
	}

	private String getItemTitle(String name){
		if(name.equals("DoubleClick")){
			name = this.getString(R.string.sleep_doubleclick);
			return name;
		}else if(name.equals("Up")){
			name = this.getString(R.string.sleep_up);
		}if(name.equals("Down")){
			name = this.getString(R.string.sleep_down);
		}if(name.equals("Left")){
			name = this.getString(R.string.sleep_left);
		}if(name.equals("Right")){
			name = this.getString(R.string.sleep_right);
		}
		String title = getResources().getString(R.string.sleep_event_gesture, name);
		return title;
	}
	
	@Override
	public boolean onPreferenceChange(Preference preference, Object object) {
		Log.e("liup","onPreferenceChange");
		String key = preference.getKey();
		Cursor cursor = mContentResolver.query(queryUri, null,"item" + "=?",new String[] { key }, null);
		if (cursor != null && cursor.moveToFirst()) {
			int onoff = cursor.getInt(cursor.getColumnIndex("onoff"));
			if(onoff == 1){
				onoff = 0;
			}else{
				onoff = 1;
			}
			values.put("onoff", onoff);
			mContentResolver.update(queryUri,values, "item"+"=?", new String[] { key });
		}	
		if(cursor != null) {
			cursor.close();
		}
		return true;
	}

	@Override
	public boolean onPreferenceClick(Preference preference) {
		Log.e("liup","onPreferenceClick");
		String key = preference.getKey();
		Cursor cursor = mContentResolver.query(queryUri, null,"item" + "=?",new String[] { key }, null);
		if (cursor != null && cursor.moveToFirst()) {
			String item = cursor.getString(cursor.getColumnIndex("item"));
			try {
				Intent intent = new Intent();
				intent.putExtra("gesture", item);
				intent.setClass(this, PrizeSleepFunSelectActivity.class);
				startActivity(intent);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return false;
	}
	private void addActionBar(){
		actionBarSwitch = new Switch(this);
		final boolean bSleepGesture = Settings.System.getInt(getContentResolver(),Settings.System.PRIZE_SLEEP_GESTURE,0) == 1;
		if(bSleepGesture){
			actionBarSwitch.setChecked(true);
		}else{
			actionBarSwitch.setChecked(false);
		}
		final int padding = this.getResources().getDimensionPixelSize(R.dimen.action_bar_switch_padding);
		this.getActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM,ActionBar.DISPLAY_SHOW_CUSTOM);
		ActionBar.LayoutParams lp = new ActionBar.LayoutParams(ActionBar.LayoutParams.WRAP_CONTENT,
			ActionBar.LayoutParams.WRAP_CONTENT,Gravity.CENTER_VERTICAL | Gravity.END);
		lp.setMargins(0, 0, padding, 0);
		this.getActionBar().setCustomView(actionBarSwitch, lp);
		 this.getActionBar().setDisplayHomeAsUpEnabled(true);
         this.getActionBar().setHomeButtonEnabled(true);
		actionBarSwitch.setOnCheckedChangeListener(new OnCheckedChangeListener(){
			@Override
			public void onCheckedChanged(CompoundButton buttonView,boolean isChecked) {
				onoff_sleep_gesture();
			}		
		});
		
		
	}	
	private void initSelectWhat(){
		if(sAppBool){
			sAppBool = false;
			item = getIntent().getExtras().getString("gesture");		
			int postion = getIntent().getExtras().getInt("postion");
			summary = PrizeOpenAppActivity.mlistAppInfo.get(postion).getAppLabel();
			String packageName = PrizeOpenAppActivity.mlistAppInfo.get(postion).getPkgName();
			String className = PrizeOpenAppActivity.mlistAppInfo.get(postion).getActivityName();
			Cursor cursor = mContentResolver.query(queryUri, null,"item" + "=?",new String[] { item }, null);
			if (cursor != null && cursor.moveToFirst()) {
				String item = cursor.getString(cursor.getColumnIndex("item"));
		        values.put("packagename", packageName);  
		        values.put("classname", className);  
				mContentResolver.update(queryUri,values, "item"+"=?", new String[] { item });
			}
		}
		if(sUnlockBool){
			sUnlockBool = false;
			item = getIntent().getExtras().getString("gesture");		
			Cursor cursor = mContentResolver.query(queryUri, null,"item" + "=?",new String[] { item }, null);
			if (cursor != null && cursor.moveToFirst()) {
				String item = cursor.getString(cursor.getColumnIndex("item"));
		        values.put("packagename", "unlock");  
				mContentResolver.update(queryUri,values, "item"+"=?", new String[] { item });
			}
		}
		if(sMusicControlBool){
			sMusicControlBool = false;
			item = getIntent().getExtras().getString("gesture");
			String preference = getIntent().getExtras().getString("preference");
			Cursor cursor = mContentResolver.query(queryUri, null,"item" + "=?",new String[] { item }, null);
			if (cursor != null && cursor.moveToFirst()) {
				String item = cursor.getString(cursor.getColumnIndex("item"));
		        values.put("packagename", preference);  
				mContentResolver.update(queryUri,values, "item"+"=?", new String[] { item });
			}
		}
		if(sAwake){
			sAwake = false;
			item = getIntent().getExtras().getString("gesture");		
			Cursor cursor = mContentResolver.query(queryUri, null,"item" + "=?",new String[] { item }, null);
			if (cursor != null && cursor.moveToFirst()) {
				String item = cursor.getString(cursor.getColumnIndex("item"));
		        values.put("packagename", "awake");  
				mContentResolver.update(queryUri,values, "item"+"=?", new String[] { item });
			}
		}
	}
			
	private void onoff_sleep_gesture(){
		if(actionBarSwitch.isChecked()){
			Settings.System.putInt(getContentResolver(),Settings.System.PRIZE_SLEEP_GESTURE,1);
			mPreferenceScreen.setEnabled(true);
			for(int i=0;i<mPreferenceScreen.getPreferenceCount();i++){
				PrizeSwitchPreference mPrizeSwitchPreference = (PrizeSwitchPreference)mPreferenceScreen.getPreference(i);
				mPrizeSwitchPreference.setPrizeEnabled(true);
			}

		}else{
			Settings.System.putInt(getContentResolver(),Settings.System.PRIZE_SLEEP_GESTURE,0);
			mPreferenceScreen.setEnabled(false);
			for(int i=0;i<mPreferenceScreen.getPreferenceCount();i++){
				PrizeSwitchPreference mPrizeSwitchPreference = (PrizeSwitchPreference)mPreferenceScreen.getPreference(i);
				mPrizeSwitchPreference.setPrizeEnabled(false);
			}
		}
		Cursor cursor = mContentResolver.query(queryUri, null, null, null,null);
		if (cursor != null && cursor.moveToFirst()) {
			do {
				int onoff = cursor.getInt(cursor.getColumnIndex("onoff"));
				bGesture = bGesture + onoff;
			}while (cursor.moveToNext());
		}	
		
		bSleepGesture = Settings.System.getInt(getContentResolver(),Settings.System.PRIZE_SLEEP_GESTURE,0) == 1;
		Log.e("liup","bSleepGesture = " + bSleepGesture);
		if(bSleepGesture && bGesture > 0){
				Log.e("liup","write1");
				/*
				try {
					File file = new File("/proc/gt9xx_enable");    
					Log.e("liup","write11");
	        FileOutputStream fos = new FileOutputStream(file);   
					fos = new FileOutputStream(file);
					Log.e("liup","write111");
					fos.write("1".getBytes());
					Log.e("liup","write1111");
					fos.close();
				} catch (IOException e) {
					Log.e("liup","write11111");
					e.printStackTrace();
				}
			*/
			try {	
	    		String[] cmdMode = new String[]{"/system/bin/sh","-c","echo" + " " + 1 + " > /proc/gt9xx_enable"};
				Runtime.getRuntime().exec(cmdMode);				
			} catch (IOException e) {
				e.printStackTrace();
			}
			
		}else{
		/*
			File file = new File("/proc/gt9xx_enable");
			FileOutputStream fos;
			Log.e("liup","write0");
			try {
				Log.e("liup","write00");
				fos = new FileOutputStream(file);
				Log.e("liup","write000");
				fos.write("0".getBytes());
				Log.e("liup","write0000");
				fos.close();
			} catch (IOException e) {
				Log.e("liup","write000000");
				e.printStackTrace();
			}
			*/
			try {	
	    		String[] cmdMode = new String[]{"/system/bin/sh","-c","echo" + " " + 0 + " > /proc/gt9xx_enable"};
				Log.e("liup","cmdMode");
				Runtime.getRuntime().exec(cmdMode);				
			} catch (IOException e) {
				e.printStackTrace();
			}
			
		}
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
	
	  @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
			onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
	
	@Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}
