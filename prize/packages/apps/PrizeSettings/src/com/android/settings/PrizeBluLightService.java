package com.android.settings;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ContentResolver;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;
import android.content.Context;

import android.provider.Settings;
import com.mediatek.pq.PictureQuality;

/**
 * Created by Administrator on 2017/5/9.
 */

public class PrizeBluLightService extends Service {

    private PrizeBluLightBinder mBinder = new PrizeBluLightBinder();
    private ContentResolver mResolver;
    private Context mContext;
	

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mContext = getApplicationContext();
        mResolver = getContentResolver();
		
    }

    @Override
    public int onStartCommand(final Intent intent, int flags, int startId) {
        if(intent == null){
            return super.onStartCommand(intent, flags, startId);
        }
        synchronized (this) {
            AsyncTask.execute(new Runnable() {
                public void run() {
                    Log.d("BluLight", "PrizeBluLightService onStartCommand()");
                    int mTimeType = intent.getIntExtra(PrizeBluLightUtil.TIME_TPYE_KEY, 0);
                    Log.d("BluLight", "PrizeBluLightService onStartCommand() mTimeType = " + mTimeType);
                    int mBluLightStatus = Settings.System.getInt(mResolver,
                            Settings.System.PRIZE_BLULIGHT_MODE_STATE, 0);
                    int mBluLightTimeStatus = Settings.System.getInt(mResolver,
                            Settings.System.PRIZE_BLULIGHT_TIME_STATE, 0);
                    int mBluLightTimingStatus = Settings.System.getInt(mResolver,
                            Settings.System.PRIZE_BLULIGHT_TIMING_STATE,0);                    
					/*prize-modify for huyanmode-lihuangyuan-2017-06-09-start*/
					boolean isBluELightEnabled = false;
					if(PrizeBluLightUtil.BLUELIGHT_MTK)
					{
						isBluELightEnabled = PictureQuality.isBlueLightEnabled();
						int bluelightStrength = PictureQuality.getBlueLightStrength();
						Log.d("BluLight","PrizeBluLightService onStartCommand() bluelightStrength="+bluelightStrength);
						if(bluelightStrength < PrizeBluLightUtil.BLULIGHT_MIN_VALUE)
						{
							PictureQuality.Range mPQrange;
							mPQrange = PictureQuality.getBlueLightStrengthRange();
							mPQrange.min = PrizeBluLightUtil.BLULIGHT_MIN_VALUE;
							int setStrength = mPQrange.max - mPQrange.min;
							setStrength = setStrength/2 + mPQrange.min;
							Log.d("BluLight","PrizeBluLightService onStartCommand() setBlueLightStrength:"+setStrength);
							PictureQuality.setBlueLightStrength(setStrength);
						}
					}
					
					/*prize-modify for huyanmode-lihuangyuan-2017-06-09-end*/
                    Log.d("BluLight", "PrizeBluLightService onStartCommand(), mBluLightStatus = " + mBluLightStatus +
                            ",mBluLightTimeStatus = " + mBluLightTimeStatus);
                    switch (mTimeType) {
                        case PrizeBluLightUtil.TIME_START_TYPE:
                            Log.d("BluLight", "PrizeBluLightService onStartCommand() StartReceiver");
                            if (mBluLightStatus == 0 && mBluLightTimeStatus == 1) {
                                Log.d("BluLight", "PrizeBluLightService onStartCommand() Timing Start");
                                Settings.System.putInt(mResolver, Settings.System.PRIZE_BLULIGHT_TIMING_STATE, 1);
                                Settings.System.putInt(mResolver, Settings.System.PRIZE_BLULIGHT_MODE_STATE, 1);
								/*prize-modify for huyanmode-lihuangyuan-2017-06-09-start*/
								if(PrizeBluLightUtil.BLUELIGHT_MTK)
								{
									PictureQuality.enableBlueLight(true);
								}
								
								/*prize-modify for huyanmode-lihuangyuan-2017-06-09-end*/                                
                                setBluLightTime(PrizeBluLightUtil.TIME_END_REQUEST_CODE,
                                        Settings.System.PRIZE_BLULIGHT_END_TIME, PrizeBluLightUtil.BLULIGHT_TIME_CLOSE_ACTION);
                        }
                        break;
                        case PrizeBluLightUtil.TIME_END_TYPE:
                            Log.d("BluLight", "PrizeBluLightService onStartCommand() EndReceiver");
                            if (mBluLightTimeStatus == 1 && isBluELightEnabled) {
                                Log.d("BluLight", "PrizeBluLightService onStartCommand() Timing End");
                                Settings.System.putInt(mResolver, Settings.System.PRIZE_BLULIGHT_TIMING_STATE, 0);
                                if(mBluLightStatus == 1){
                                    Settings.System.putInt(mResolver, Settings.System.PRIZE_BLULIGHT_MODE_STATE, 0);
                                }
								/*prize-modify for huyanmode-lihuangyuan-2017-06-09-start*/
								if(PrizeBluLightUtil.BLUELIGHT_MTK)
								{
									PictureQuality.enableBlueLight(false);
								}
								
								/*prize-modify for huyanmode-lihuangyuan-2017-06-09-end*/                                
                                Log.d("BluLight", "PrizeBluLightService onStartCommand() PictureQuality.enableBlueLight(false)");
                        }
                        break;
                        case PrizeBluLightUtil.BOOT_COMPLETED_TYPE:
                            Log.d("BluLight", "PrizeBluLightService onStartCommand() Boot_Completed mBluLightStatus = " + mBluLightStatus +
                                    ",mBluLightTimeStatus = " + mBluLightTimeStatus);								
							boolean isStartTimeHasPassed = PrizeBluLightUtil.isTimeHasPassed(mContext,
                                            Settings.System.PRIZE_BLULIGHT_START_TIME);
                            boolean isEndTimeHasPassed = PrizeBluLightUtil.isTimeHasPassed(mContext,
                                            Settings.System.PRIZE_BLULIGHT_END_TIME);
							
							if (mBluLightStatus == 1) {
                                if(mBluLightTimeStatus == 0){ 
									/*prize-modify for huyanmode-lihuangyuan-2017-06-09-start*/
									if(PrizeBluLightUtil.BLUELIGHT_MTK)
									{
										PictureQuality.enableBlueLight(true);
									}
									
									/*prize-modify for huyanmode-lihuangyuan-2017-06-09-end*/
								} else { 
									if(!isStartTimeHasPassed || isEndTimeHasPassed){
										Settings.System.putInt(mResolver, Settings.System.PRIZE_BLULIGHT_MODE_STATE, 0);
										Settings.System.putInt(mResolver, Settings.System.PRIZE_BLULIGHT_TIMING_STATE, 0);                                
										/*prize-modify for huyanmode-lihuangyuan-2017-06-09-start*/
										if(PrizeBluLightUtil.BLUELIGHT_MTK)
										{
											PictureQuality.enableBlueLight(false);
										}
										
										/*prize-modify for huyanmode-lihuangyuan-2017-06-09-end*/
									} else {
										/*prize-modify for huyanmode-lihuangyuan-2017-06-09-start*/
										if(PrizeBluLightUtil.BLUELIGHT_MTK)
										{
											PictureQuality.enableBlueLight(true);
										}
										
										/*prize-modify for huyanmode-lihuangyuan-2017-06-09-end*/
										Settings.System.putInt(mResolver, Settings.System.PRIZE_BLULIGHT_TIMING_STATE, 1);   
										setBluLightTime(PrizeBluLightUtil.TIME_END_REQUEST_CODE,
                                                    Settings.System.PRIZE_BLULIGHT_END_TIME,
                                                    PrizeBluLightUtil.BLULIGHT_TIME_CLOSE_ACTION);	
									}
								}
							} else { 
									Log.d("BluLight"," isStartTimeHasPassed:"+isStartTimeHasPassed+",isEndTimeHasPassed:"+isEndTimeHasPassed);
									if(!isStartTimeHasPassed || isEndTimeHasPassed){                                            
										/*prize-modify for huyanmode-lihuangyuan-2017-06-09-start*/
										if(PrizeBluLightUtil.BLUELIGHT_MTK)
										{
											PictureQuality.enableBlueLight(false);
										}
										
										/*prize-modify for huyanmode-lihuangyuan-2017-06-09-end*/
										Settings.System.putInt(mResolver, Settings.System.PRIZE_BLULIGHT_TIMING_STATE, 0);  
                                        setBluLightTime(PrizeBluLightUtil.TIME_START_REQUEST_CODE,
                                                Settings.System.PRIZE_BLULIGHT_START_TIME,
                                                PrizeBluLightUtil.BLULIGHT_TIME_OPEN_ACTION);
                                    } else {
                                        /*prize-modify for huyanmode-lihuangyuan-2017-06-09-start*/
										if(PrizeBluLightUtil.BLUELIGHT_MTK)
										{
											PictureQuality.enableBlueLight(true);
										}
										
										/*prize-modify for huyanmode-lihuangyuan-2017-06-09-end*/
										Settings.System.putInt(mResolver, Settings.System.PRIZE_BLULIGHT_MODE_STATE, 1);
										Settings.System.putInt(mResolver, Settings.System.PRIZE_BLULIGHT_TIMING_STATE, 1); 
                                        setBluLightTime(PrizeBluLightUtil.TIME_END_REQUEST_CODE,
                                                Settings.System.PRIZE_BLULIGHT_END_TIME,
                                                PrizeBluLightUtil.BLULIGHT_TIME_CLOSE_ACTION);
                                    }
							}
                        break;
                    }
                }
            });
        }
        return super.onStartCommand(intent, Service.START_REDELIVER_INTENT, startId);
    }

    private void setBluLightTime(int requestCode,String key, String action){
        Log.d("BluLight","PrizeBluLightService setBluLightTime() key = " + key);
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);

        Intent mIntent = new Intent(action);
        PendingIntent operation = PendingIntent.getBroadcast(mContext, requestCode /* requestCode */, mIntent, 0);

        long alarmTime = PrizeBluLightUtil.getAlarmTime(mContext,key).getTimeInMillis();
        alarmManager.setExact(AlarmManager.RTC_WAKEUP,alarmTime,operation);
    }

    public class PrizeBluLightBinder extends Binder {
        public PrizeBluLightService getService() {
            return PrizeBluLightService.this;
        }
    }

}
