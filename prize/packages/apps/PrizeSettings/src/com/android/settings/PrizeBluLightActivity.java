package com.android.settings;

import android.app.AlarmManager;
import android.app.FragmentManager;
import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.UserHandle;

import android.provider.Settings;
import android.util.Log;

import com.mediatek.pq.PictureQuality;

import com.android.internal.logging.MetricsLogger;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import android.support.v14.preference.SwitchPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceScreen;
/**
 * Created by Administrator on 2017/5/4.
 */

public class PrizeBluLightActivity extends SubSettings {
    @Override
    public Intent getIntent() {
        Intent modIntent = new Intent(super.getIntent());
        modIntent.putExtra(EXTRA_SHOW_FRAGMENT, PrizeBluLightDefender.class.getName());
        return modIntent;
    }

    @Override
    protected boolean isValidFragment(String fragmentName) {
        if (PrizeBluLightDefender.class.getName().equals(fragmentName)) return true;
        return false;
    }

    public static class PrizeBluLightDefender extends SettingsPreferenceFragment implements
            Preference.OnPreferenceChangeListener, Preference.OnPreferenceClickListener {

        private static final String KEY_BLUELIGHT_MODE = "prize_blulight_mode_state";
        private static final String KEY_BLUELIGHT_TIME = "prize_blulight_time_state";
        private static final String KEY_BLUELIGHT_TIME_START = "prize_blulight_start_time";
        private static final String KEY_BLUELIGHT_TIME_END = "prize_blulight_end_time";
        private static final String KEY_BLUELIGHT_CONTRL = "prize_blulight_contrl";

        private SwitchPreference mBluLightModePreference;
        private SwitchPreference mBluLightTimePreference;
        private PrizeBluLightTimePreference mBluLightTimeStart;
        private PrizeBluLightTimePreference mBluLightTimeEnd;
        private PrizeBluLightContrlPreference mBluLightContrl;
        private ContentResolver mResolver;
        private Context mContext;
        private BluLightObserver mBluLightObserver;
	
	/* PRIZE-Add for BluLight-zhudaopeng-2017-05-04-Start */
        public static final int PRIZE_BLULIGHT = 239;
        public static final int QS_BLULIGHT = 240;
        /* PRIZE-Add for BluLight-zhudaopeng-2017-05-04-End */

        @Override
        public void onAttach(Context context) {
            super.onAttach(context);
            mContext = context;
        }

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            addPreferencesFromResource(R.xml.prize_blulight_defender);
            final PreferenceScreen root = getPreferenceScreen();
            mResolver = mContext.getContentResolver();

            mBluLightModePreference = (SwitchPreference) findPreference(KEY_BLUELIGHT_MODE);
            mBluLightTimePreference = (SwitchPreference) findPreference(KEY_BLUELIGHT_TIME);

            final FragmentManager mgr = getFragmentManager();

            mBluLightTimeStart = new PrizeBluLightTimePreference(mContext,mgr);
            mBluLightTimeStart.setKey(KEY_BLUELIGHT_TIME_START);
            mBluLightTimeStart.setTitle(R.string.bluLight_start_time);
            mBluLightTimeStart.setCallback(new PrizeBluLightTimePreference.Callback() {
                @Override
                public boolean onSetTime(final int hour, final int minute) {
                    Log.d("BluLightTime","BluLightTimeStart hour = "+hour+",minute"+minute);
                    AsyncTask.execute(new Runnable() {
                        public void run() {
                            int mBluLightModeStatus = Settings.System.getInt(mResolver,
                                    Settings.System.PRIZE_BLULIGHT_MODE_STATE, 0);
                            int mBluLightTimeStatus = Settings.System.getInt(mResolver,
                                    Settings.System.PRIZE_BLULIGHT_TIME_STATE, 0);

                            String hourAndMinute = formatTime(hour, minute);
                            Settings.System.putString(mResolver, mBluLightTimeStart.getKey(), hourAndMinute);
                            if(mBluLightModeStatus == 0 && mBluLightTimeStatus == 1){
                                setBluLightTime(PrizeBluLightUtil.TIME_START_REQUEST_CODE, mBluLightTimeStart.getKey(),
                                        PrizeBluLightUtil.BLULIGHT_TIME_OPEN_ACTION);
                            }
                        }
                    });
                    return true;
                }
            });
            root.addPreference(mBluLightTimeStart);

            mBluLightTimeEnd = new PrizeBluLightTimePreference(mContext,mgr);
            mBluLightTimeEnd.setKey(KEY_BLUELIGHT_TIME_END);
            mBluLightTimeEnd.setTitle(R.string.bluLight_end_time);
            mBluLightTimeEnd.setCallback(new PrizeBluLightTimePreference.Callback() {
                @Override
                public boolean onSetTime(final int hour, final int minute) {
                    Log.d("BluLightTime","BluLightTimeEnd hour = "+hour+",minute"+minute);
                    AsyncTask.execute(new Runnable() {
                        public void run() {
                            int mBluLightModeStatus = Settings.System.getInt(mResolver,
                                    Settings.System.PRIZE_BLULIGHT_MODE_STATE, 0);
                            int mBluLightTimeStatus = Settings.System.getInt(mResolver,
                                    Settings.System.PRIZE_BLULIGHT_TIME_STATE, 0);
                            int mBluLightTimingStatus = Settings.System.getInt(mResolver,
                                    Settings.System.PRIZE_BLULIGHT_TIMING_STATE, 0);
                            String hourAndMinute = formatTime(hour, minute);
                            Settings.System.putString(mResolver, mBluLightTimeEnd.getKey(),hourAndMinute);
                            if(mBluLightModeStatus== 1 && mBluLightTimeStatus == 1){
                                setBluLightTime(PrizeBluLightUtil.TIME_END_REQUEST_CODE, mBluLightTimeEnd.getKey(),
                                        PrizeBluLightUtil.BLULIGHT_TIME_CLOSE_ACTION);
                            }
                        }
                    });
                    return true;
                }
            });
            root.addPreference(mBluLightTimeEnd);

            mBluLightContrl = new PrizeBluLightContrlPreference(mContext);
            mBluLightContrl.setKey(KEY_BLUELIGHT_CONTRL);
            root.addPreference(mBluLightContrl);

            int mBluLightStatus = Settings.System.getInt(mResolver, Settings.System.PRIZE_BLULIGHT_MODE_STATE, 0);
            int mBluLightTimeStatus = Settings.System.getInt(mResolver, Settings.System.PRIZE_BLULIGHT_TIME_STATE, 0);

            mBluLightModePreference.setChecked(mBluLightStatus == 1?true:false);

            mBluLightTimePreference.setChecked(mBluLightTimeStatus!=0);
            if(mBluLightTimeStatus == 1){
                mBluLightTimeStart.setEnabled(true);
                mBluLightTimeEnd.setEnabled(true);
            } else {
                mBluLightTimeStart.setEnabled(false);
                mBluLightTimeEnd.setEnabled(false);
            }

            if(mBluLightStatus == 1 || mBluLightTimeStatus == 1){
                mBluLightContrl.setEnabled(true);
                AsyncTask.execute(new Runnable() {
                    public void run() {
						/*prize-modify for huyanmode-lihuangyuan-2017-06-09-start*/
						int mBlueLightStrength = 0;
						if(PrizeBluLightUtil.BLUELIGHT_MTK)
					      	{
                        				mBlueLightStrength = PictureQuality.getBlueLightStrength();
						}
						
						/*prize-modify for huyanmode-lihuangyuan-2017-06-09-end*/                        
                        Settings.System.putInt(mResolver, Settings.System.PRIZE_BLULIGHT_OLD_BLU_VALUE,
                                mBlueLightStrength);
                    }
                });
            } else if(mBluLightStatus == 0 && mBluLightTimeStatus == 0){
                mBluLightContrl.setEnabled(false);
            }

            mBluLightModePreference.setOnPreferenceChangeListener(this);
            mBluLightTimePreference.setOnPreferenceChangeListener(this);

            Handler handler = new Handler();
            mBluLightObserver = new BluLightObserver(handler);
        }

        @Override
        public void onResume() {
            super.onResume();
            mBluLightObserver.startObserving();
            int mBluLightStatus = Settings.System.getInt(mResolver, Settings.System.PRIZE_BLULIGHT_MODE_STATE, 0);
            int mBluLightTimeStatus = Settings.System.getInt(mResolver, Settings.System.PRIZE_BLULIGHT_TIME_STATE, 0);
            mBluLightModePreference.setChecked(mBluLightStatus!=0);
            mBluLightTimePreference.setChecked(mBluLightTimeStatus!=0);
        }

        @Override
        public void onPause() {
            super.onPause();
            mBluLightObserver.stopObserving();
        }

        private String formatTime(int hour, int minute){
            String hourStr = null;
            if(hour < 10){
                hourStr = "0"+hour;
            } else {
                hourStr = ""+hour;
            }
            String minuteStr = null;
            if(minute < 10){
                minuteStr = "0"+minute;
            } else {
                minuteStr = ""+minute;
            }
            String hourAndMinute = hourStr+":"+minuteStr;
            return hourAndMinute;
        }

        private void setBluLightTime(int requestCode,String key, String action){
            Log.d("BluLight","PrizeBluLightDefender setBluLightTime() key = " + key);
            AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);

            Intent mIntent = new Intent(action);
            PendingIntent operation = PendingIntent.getBroadcast(mContext, requestCode /* requestCode */, mIntent, 0);

            long alarmTime = PrizeBluLightUtil.getAlarmTime(mContext,key).getTimeInMillis();
            alarmManager.setExact(AlarmManager.RTC_WAKEUP,alarmTime,operation);
        }

        @Override
        public boolean onPreferenceChange(final Preference preference, Object newValue) {
            final Boolean isChecked = (Boolean) newValue;
            Log.d("PrizeBluLight","isChecked = "+isChecked);
            if(preference instanceof SwitchPreference){
                ((SwitchPreference)preference).setChecked(isChecked);
            }

            AsyncTask.execute(new Runnable() {
                public void run() {
                    if(isChecked){
                        Settings.System.putInt(mResolver, preference.getKey(), 1);
                    } else {
                        Settings.System.putInt(mResolver, preference.getKey(), 0);
                    }
                }
            });

            return true;
        }

        @Override
        public boolean onPreferenceClick(Preference preference) {
            return false;
        }

        @Override
        protected int getMetricsCategory() {
            return PRIZE_BLULIGHT;
        }

        /** ContentObserver to watch blulight mode status **/
        private class BluLightObserver extends ContentObserver {

            private final Uri BLULIGHT_MODE_URI = Settings.System
                    .getUriFor(Settings.System.PRIZE_BLULIGHT_MODE_STATE);
            private final Uri BLULIGHT_TIME_URI = Settings.System
                    .getUriFor(Settings.System.PRIZE_BLULIGHT_TIME_STATE);

            public BluLightObserver(Handler handler) {
                super(handler);
            }

            @Override
            public void onChange(boolean selfChange) {
                onChange(selfChange, null);
            }

            @Override
            public void onChange(boolean selfChange, Uri uri) {
                if (selfChange)
                    return;
                int mBluLightModeStatus = Settings.System.getInt(mResolver,
                        Settings.System.PRIZE_BLULIGHT_MODE_STATE, 0);
                int mBluLightTimeStatus = Settings.System.getInt(mResolver,
                        Settings.System.PRIZE_BLULIGHT_TIME_STATE, 0);
                int mBluLightTimingStatus = Settings.System.getInt(mResolver,
                        Settings.System.PRIZE_BLULIGHT_TIMING_STATE,0);

                if (BLULIGHT_MODE_URI.equals(uri)) {
                    if(mBluLightModeStatus == 1){
						/*prize-modify for huyanmode-lihuangyuan-2017-06-09-start*/
					      if(PrizeBluLightUtil.BLUELIGHT_MTK)
					      	{
                        				PictureQuality.enableBlueLight(true);
					      	}
						
						/*prize-modify for huyanmode-lihuangyuan-2017-06-09-end*/
                        mBluLightModePreference.setChecked(true);
                        Log.d("BluLight", "PrizeBluLightActivity onChange(0) PictureQuality.enableBlueLight(false)");
                        mBluLightContrl.setEnabled(true);
                        if(mBluLightTimeStatus == 1){
                            setBluLightTime(PrizeBluLightUtil.TIME_END_REQUEST_CODE, mBluLightTimeEnd.getKey(),
                                    PrizeBluLightUtil.BLULIGHT_TIME_CLOSE_ACTION);
                            if(mBluLightTimingStatus == 0){
                                cancelAlarm(PrizeBluLightUtil.BLULIGHT_TIME_OPEN_ACTION);
                            }
                        }
                    } else {                        
						/*prize-modify for huyanmode-lihuangyuan-2017-06-09-start*/
						if(PrizeBluLightUtil.BLUELIGHT_MTK)
					      	{
                        				PictureQuality.enableBlueLight(false);
						}
						
						/*prize-modify for huyanmode-lihuangyuan-2017-06-09-end*/
                        mBluLightModePreference.setChecked(false);
                        Log.d("BluLight", "PrizeBluLightActivity onChange(1) PictureQuality.enableBlueLight(false)");
                        if(mBluLightTimeStatus == 1){
                            Settings.System.putInt(mResolver, Settings.System.PRIZE_BLULIGHT_TIMING_STATE, 0);
                            setBluLightTime(PrizeBluLightUtil.TIME_START_REQUEST_CODE,
                                    mBluLightTimeStart.getKey(), PrizeBluLightUtil.BLULIGHT_TIME_OPEN_ACTION);
                        } else {
                            mBluLightContrl.setEnabled(false);
                        }
                    }
                } else if (BLULIGHT_TIME_URI.equals(uri)) {
                    if(mBluLightTimeStatus == 1){
                        mBluLightTimeStart.setEnabled(true);
                        mBluLightTimeEnd.setEnabled(true);
                        boolean isStartTimeHasPassed = PrizeBluLightUtil.isTimeHasPassed(mContext,
                                Settings.System.PRIZE_BLULIGHT_START_TIME);
                        boolean isEndTimeHasPassed = PrizeBluLightUtil.isTimeHasPassed(mContext,
                                Settings.System.PRIZE_BLULIGHT_END_TIME);
                        if(isStartTimeHasPassed && !isEndTimeHasPassed){
                            Settings.System.putInt(mResolver, Settings.System.PRIZE_BLULIGHT_TIMING_STATE, 1);
                            Settings.System.putInt(mResolver, Settings.System.PRIZE_BLULIGHT_MODE_STATE, 1);
//                          PictureQuality.enableBlueLight(true);
//                          setBluLightTime(PrizeBluLightUtil.TIME_END_REQUEST_CODE, mBluLightTimeEnd.getKey(),
//                                 PrizeBluLightUtil.BLULIGHT_TIME_CLOSE_ACTION);
                        } else {
                            if(mBluLightModeStatus == 0){
                                setBluLightTime(PrizeBluLightUtil.TIME_START_REQUEST_CODE,
                                        mBluLightTimeStart.getKey(), PrizeBluLightUtil.BLULIGHT_TIME_OPEN_ACTION);
                            } else {
                                setBluLightTime(PrizeBluLightUtil.TIME_END_REQUEST_CODE, mBluLightTimeEnd.getKey(),
                                        PrizeBluLightUtil.BLULIGHT_TIME_CLOSE_ACTION);
                            }
                        }
                        mBluLightContrl.setEnabled(true);
                    } else {
                        mBluLightTimeStart.setEnabled(false);
                        mBluLightTimeEnd.setEnabled(false);
                        if(mBluLightModeStatus == 0){                            
							/*prize-modify for huyanmode-lihuangyuan-2017-06-09-start*/
							if(PrizeBluLightUtil.BLUELIGHT_MTK)
					      		{
								PictureQuality.enableBlueLight(false);
							}
							
							/*prize-modify for huyanmode-lihuangyuan-2017-06-09-end*/
                            mBluLightContrl.setEnabled(false);
                            Log.d("BluLight", "PrizeBluLightActivity onChange(2) PictureQuality.enableBlueLight(false)");
                            if(mBluLightTimingStatus == 0){
                                cancelAlarm(PrizeBluLightUtil.BLULIGHT_TIME_OPEN_ACTION);
                            } else {
                                cancelAlarm(PrizeBluLightUtil.BLULIGHT_TIME_CLOSE_ACTION);
                            }
                        }
                    }
                }
            }

            public void startObserving() {
                final ContentResolver cr = mContext.getContentResolver();
                cr.unregisterContentObserver(this);
                cr.registerContentObserver(BLULIGHT_MODE_URI, false, this,
                        UserHandle.USER_ALL);
                cr.registerContentObserver(BLULIGHT_TIME_URI, false, this,
                        UserHandle.USER_ALL);
            }

            public void stopObserving() {
                final ContentResolver cr = mContext.getContentResolver();
                cr.unregisterContentObserver(this);
            }
        }

        private void cancelAlarm(String action){
            Intent intent = new Intent();
            intent.setAction(action);
            PendingIntent pi = PendingIntent.getBroadcast(mContext, 0,
                    intent, 0);
            AlarmManager am = (AlarmManager)getSystemService(ALARM_SERVICE);
            am.cancel(pi);
        }
    }
}
