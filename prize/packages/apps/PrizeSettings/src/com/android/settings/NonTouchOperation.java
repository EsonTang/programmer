/*******************************************
 * 版权所有©2015,深圳市铂睿智恒科技有限公司
 * 
 * 内容摘要：处理设置中的隔空操作设置
 * 当前版本：V1.0
 * 作    者：钟卫林
 * 完成日期：2015-06-08
 *********************************************/
package com.android.settings;

import android.content.Intent;
import android.os.Bundle;

import android.preference.PreferenceActivity;
import android.support.v14.preference.SwitchPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.Preference.OnPreferenceClickListener;
import android.support.v7.preference.PreferenceCategory;
import android.support.v7.preference.PreferenceScreen;
import android.support.v7.preference.CheckBoxPreference;

import android.provider.Settings;
import com.android.settings.SettingsPreferenceFragment;

import android.util.Log;

import com.mediatek.common.prizeoption.PrizeOption;

public class NonTouchOperation extends SettingsPreferenceFragment implements
		Preference.OnPreferenceChangeListener, OnPreferenceClickListener {
	private static final String TAG = "prize";
    
    private static final String ACTION_OPEN_DISTANCE_OPERATION = "com.android.prize.distanceoperation";
    private static final String KEY_EXTRA = "nontouch";
    private static final String ACTION_NON_TOUCH_OPERATION_UNLOCK = "com.android.settings.non_touch_operation_unlock";
    private static final String ACTION_NON_TOUCH_OPERATION_GALLERY = "com.android.settings.non_touch_operation_gallery";
    private static final String ACTION_NON_TOUCH_OPERATION_LAUNCHER = "com.android.settings.non_touch_operation_launcher";
    private static final String ACTION_NON_TOUCH_OPERATION_VIDEO = "com.android.settings.non_touch_operation_video";
    private static final String ACTION_NON_TOUCH_OPERATION_MUSIC = "com.android.settings.non_touch_operation_music";

    private static final String KEY_NON_TOUCH_OPERATION_UNLOCK = "non_touch_operation_unlock";
    private static final String KEY_NON_TOUCH_OPERATION_GALLERY = "non_touch_operation_gallery";
    private static final String KEY_NON_TOUCH_OPERATION_LAUNCHER = "non_touch_operation_launcher";
    private static final String KEY_NON_TOUCH_OPERATION_VIDEO = "non_touch_operation_video";
    private static final String KEY_NON_TOUCH_OPERATION_MUSIC = "non_touch_operation_music";

	private static final String SYSTEM_CATEGORY = "system_category";
	
    private SwitchPreference mNonTouchUnlockPref;//隔空解锁
    private SwitchPreference mNonTouchGalleryPref;//隔空操作图库
    private SwitchPreference mNonTouchLauncherPref;//隔空操作桌面
    private SwitchPreference mNonTouchVideoPref;//隔空操作视频
    private SwitchPreference mNonTouchMusicPref;//隔空操作音乐

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.non_touch_operation);
		Log.v(TAG, "*******Non-touch Operation********");
		initializeAllPreferences();
	}
	
	protected int getMetricsCategory(){
		return -1;
	}

	@Override
	public void onResume() {
		super.onResume();
	}

	@Override
	public void onPause() {
		super.onPause();
	}

	/**
	 * @Todo init-IntelligentSettings-UI
	 * @author zhongweilin
	 */
	private void initializeAllPreferences() {
		//隔空解锁
        mNonTouchUnlockPref = (SwitchPreference) findPreference(KEY_NON_TOUCH_OPERATION_UNLOCK);
        int nontouchunlock = Settings.System.getInt(getContentResolver(),
                                Settings.System.PRIZE_NON_TOUCH_OPERATION_UNLOCK, 0);
        if(nontouchunlock == 1){
            mNonTouchUnlockPref.setChecked(true);
        }else{
            mNonTouchUnlockPref.setChecked(false);
        }
        mNonTouchUnlockPref.setOnPreferenceChangeListener(this);

        //隔空操作图库
        mNonTouchGalleryPref = (SwitchPreference) findPreference(KEY_NON_TOUCH_OPERATION_GALLERY);
        int nontouchgallery = Settings.System.getInt(getContentResolver(),
                Settings.System.PRIZE_NON_TOUCH_OPERATION_GALLERY, 0);
        if(nontouchgallery == 1){
            mNonTouchGalleryPref.setChecked(true);
        }else{
            mNonTouchGalleryPref.setChecked(false);
        }
        mNonTouchGalleryPref.setOnPreferenceChangeListener(this);

        //隔空操作桌面
        mNonTouchLauncherPref = (SwitchPreference) findPreference(KEY_NON_TOUCH_OPERATION_LAUNCHER);
        int nontouchlauncher = Settings.System.getInt(getContentResolver(),
                Settings.System.PRIZE_NON_TOUCH_OPERATION_LAUNCHER, 0);
        if(nontouchlauncher == 1){
            mNonTouchLauncherPref.setChecked(true);
        }else{
            mNonTouchLauncherPref.setChecked(false);
        }
        mNonTouchLauncherPref.setOnPreferenceChangeListener(this);
    
        //隔空操作视频
        mNonTouchVideoPref = (SwitchPreference) findPreference(KEY_NON_TOUCH_OPERATION_VIDEO);
        int nontouchvideo = Settings.System.getInt(getContentResolver(),
                Settings.System.PRIZE_NON_TOUCH_OPERATION_VIDEO, 0);
        if(nontouchvideo == 1){
            mNonTouchVideoPref.setChecked(true);
        }else{
            mNonTouchVideoPref.setChecked(false);
        }
        mNonTouchVideoPref.setOnPreferenceChangeListener(this);

        //隔空操作音乐
        mNonTouchMusicPref = (SwitchPreference) findPreference(KEY_NON_TOUCH_OPERATION_MUSIC);
        int nontouchmusic = Settings.System.getInt(getContentResolver(),
                Settings.System.PRIZE_NON_TOUCH_OPERATION_MUSIC, 0);
        if(nontouchmusic == 1){
            mNonTouchMusicPref.setChecked(true);
        }else{
            mNonTouchMusicPref.setChecked(false);
        }
        mNonTouchMusicPref.setOnPreferenceChangeListener(this);

	}

	/**
	 * @Todo onclick-PreferenceChange
	 * @author zhongweilin
	 */
	@Override
	public boolean onPreferenceChange(Preference preference, Object objValue) {
		Log.v(TAG, "*********onPreferenceChange********");
		final String key = preference.getKey();
		if (preference == mNonTouchUnlockPref) {
			boolean nonTouchUnlockValue = (Boolean) objValue;
			Settings.System.putInt(getContentResolver(),
					Settings.System.PRIZE_NON_TOUCH_OPERATION_UNLOCK, nonTouchUnlockValue ? 1 : 0);
			mNonTouchUnlockPref.setChecked(nonTouchUnlockValue);
            sendBroadcastAction(ACTION_NON_TOUCH_OPERATION_UNLOCK, nonTouchUnlockValue);
		}

        if (preference == mNonTouchGalleryPref) {
            boolean nonTouchGalleryValue = (Boolean) objValue;
            Settings.System.putInt(getContentResolver(),
                    Settings.System.PRIZE_NON_TOUCH_OPERATION_GALLERY, nonTouchGalleryValue ? 1 : 0);
            mNonTouchGalleryPref.setChecked(nonTouchGalleryValue);
            sendBroadcastAction(ACTION_NON_TOUCH_OPERATION_GALLERY, nonTouchGalleryValue);
        }

        if (preference == mNonTouchLauncherPref) {
            boolean nonTouchLauncherValue = (Boolean) objValue;
            Settings.System.putInt(getContentResolver(),
                    Settings.System.PRIZE_NON_TOUCH_OPERATION_LAUNCHER, nonTouchLauncherValue ? 1 : 0);
            mNonTouchLauncherPref.setChecked(nonTouchLauncherValue);
            sendBroadcastAction(ACTION_NON_TOUCH_OPERATION_LAUNCHER, nonTouchLauncherValue);
        }

        if (preference == mNonTouchVideoPref) {
            boolean nonTouchVideoValue = (Boolean) objValue;
            Settings.System.putInt(getContentResolver(),
                    Settings.System.PRIZE_NON_TOUCH_OPERATION_VIDEO, nonTouchVideoValue ? 1 : 0);
            mNonTouchVideoPref.setChecked(nonTouchVideoValue);
            sendBroadcastAction(ACTION_NON_TOUCH_OPERATION_VIDEO, nonTouchVideoValue);
        }

        if (preference == mNonTouchMusicPref) {
            boolean nonTouchMusicValue = (Boolean) objValue;
            Settings.System.putInt(getContentResolver(),
                    Settings.System.PRIZE_NON_TOUCH_OPERATION_MUSIC, nonTouchMusicValue ? 1 : 0);
            mNonTouchMusicPref.setChecked(nonTouchMusicValue);
            sendBroadcastAction(ACTION_NON_TOUCH_OPERATION_MUSIC, nonTouchMusicValue);
        }
        sendDistanceOperationEnable();
		return true;
    }
    /**
     * @Todo onclick-PreferenceClick
     * @author zhongweilin
     */
    @Override
    public boolean onPreferenceClick(Preference preference) {
        Log.v(TAG, "*********onPreferenceClick********");
        return true;
    }

    private void sendDistanceOperationEnable(){
        int vales = 0;
        int nontouchunlock = Settings.System.getInt(getContentResolver(), Settings.System.PRIZE_NON_TOUCH_OPERATION_UNLOCK, 0);
        if(nontouchunlock != 0){
            vales++;
        }
        int nontouchGallery = Settings.System.getInt(getContentResolver(), Settings.System.PRIZE_NON_TOUCH_OPERATION_GALLERY, 0);
        if(nontouchGallery != 0){
            vales++;
        }

        int nontouchLauncher = Settings.System.getInt(getContentResolver(), Settings.System.PRIZE_NON_TOUCH_OPERATION_LAUNCHER, 0);
        if(nontouchLauncher != 0){
            vales++;
        }
        int nontouchvideo = Settings.System.getInt(getContentResolver(), Settings.System.PRIZE_NON_TOUCH_OPERATION_VIDEO, 0);
        if(nontouchvideo != 0){
            vales++;
        }
        int nontouchmusic = Settings.System.getInt(getContentResolver(), Settings.System.PRIZE_NON_TOUCH_OPERATION_MUSIC, 0);
        if(nontouchmusic != 0){
            vales++;
        }
        if(vales == 0){
            sendBroadcastAction(ACTION_OPEN_DISTANCE_OPERATION, false);
        }
        if(vales == 1){
            sendBroadcastAction(ACTION_OPEN_DISTANCE_OPERATION, true);
        }
    }

    private void sendBroadcastAction(String action, boolean ret){
           Intent intent = new Intent(action);
           intent.putExtra(KEY_EXTRA, ret);
           getActivity().sendBroadcast(intent);
    }
}
