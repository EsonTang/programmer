/*****************************************
 * 版权所有©2015,深圳市铂睿智恒科技有限公司
 * <p>
 * 内容摘要：FlashlightTile的复制类，修改ui图片
 * 当前版本：V1.0
 * 作  者：liufan
 * 完成日期：2015-4-14
 * 修改记录：
 * 修改日期：
 * 版 本 号：
 * 修 改 人：
 * 修改内容：
 ********************************************/

package com.android.systemui.qs.tiles;

import android.app.ActivityManager;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.PowerManager;
import android.os.SystemClock;

import com.android.systemui.R;
import com.android.systemui.qs.QSTile;

import android.util.Log;
import android.database.ContentObserver;
import android.os.AsyncTask;
import android.provider.Settings;
import android.net.Uri;
import android.os.UserHandle;
import android.os.Handler;
import android.content.ContentResolver;
import com.android.internal.logging.MetricsLogger;

import com.mediatek.pq.PictureQuality;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import com.android.internal.logging.MetricsProto.MetricsEvent;

/**
 * 类描述：通知栏快速设置中，护眼模式的Tile
 * @author liufan
 * @version V1.0
 */
public class PrizeBluLightTileDefined extends QSTile<QSTile.BooleanState> implements SensorEventListener {

    private static final long RECENTLY_ON_DURATION_MILLIS = 3000;
    private final SensorManager mSensorManager;
    private final PowerManager mPowerManager;
    private final PowerManager.WakeLock mLocalWakeLock;
    private int labelId;
    private int drawableOn;
    private int drawableOff;
    private String configName;
    private BluLightObserver mBrightnessObserver;

    public static final boolean BLUELIGHT_MTK = true;//use mtk or prize

    private long mWasLastOn;

    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver(){
        @Override
        public void onReceive(Context context, Intent intent) {
            AsyncTask.execute(new Runnable() {
                public void run() {
                    final ContentResolver mResolver = mContext.getContentResolver();
                    int mBluLightModeStatus = Settings.System.getInt(mResolver,
                            Settings.System.PRIZE_BLULIGHT_MODE_STATE, 0);
                    int mBluLightTimeStatus = Settings.System.getInt(mResolver,
                            Settings.System.PRIZE_BLULIGHT_TIME_STATE, 0);
                    int mBluLightTimingStatus = Settings.System.getInt(mResolver,
                            Settings.System.PRIZE_BLULIGHT_TIMING_STATE,0);
                    boolean isTimeHasPassed = isTimeHasPassed(Settings.System.PRIZE_BLULIGHT_END_TIME);
                    Log.d("BluLight", "BroadcastReceiver onReceive() mBluLightStatus = "+mBluLightModeStatus+
                            ",mBluLightTimeStatus = "+ mBluLightTimeStatus+ ",isTimeHasPassed = "+isTimeHasPassed);
                    if(mBluLightModeStatus == 1){                        
						/*prize-modify for huyanmode-lihuangyuan-2017-06-09-start*/
					if(BLUELIGHT_MTK)
					{
                        			PictureQuality.enableBlueLight(true);
					}
					
						/*prize-modify for huyanmode-lihuangyuan-2017-06-09-end*/
                        if(mBluLightTimeStatus == 1){
                            setBluLightTime(101011,
                                        Settings.System.PRIZE_BLULIGHT_END_TIME,
                                        "com.android.intent_action_close_blulight");
                        }
//                        if(mBluLightTimeStatus == 0){
//                            PictureQuality.enableBlueLight(true);
//                        } else {
//                            if(isTimeHasPassed){
//                                PictureQuality.enableBlueLight(false);
//                                setBluLightTime(101010,
//                                        Settings.System.PRIZE_BLULIGHT_START_TIME,
//                                        "com.android.intent_action_open_blulight");
//                            }else {
//                                PictureQuality.enableBlueLight(true);
//                                setBluLightTime(101011,
//                                        Settings.System.PRIZE_BLULIGHT_END_TIME,
//                                        "com.android.intent_action_close_blulight");
//                            }
//                        }
                    } else {
                        if(mBluLightTimeStatus == 1){                            
							/*prize-modify for huyanmode-lihuangyuan-2017-06-09-start*/
							if(BLUELIGHT_MTK)
							{
								PictureQuality.enableBlueLight(false);
							}
							
							/*prize-modify for huyanmode-lihuangyuan-2017-06-09-end*/
                            setBluLightTime(101010,
                                    Settings.System.PRIZE_BLULIGHT_START_TIME,
                                    "com.android.intent_action_open_blulight");
                        }
                    }
                }
            });
        }
    };

    public PrizeBluLightTileDefined(Host host, int labelId, int drawableOn, int drawableOff,
                                 OnTileClickListener onTileClickListener, String configName) {
        super(host);
        this.labelId = labelId;
        this.drawableOn = drawableOn;
        this.drawableOff = drawableOff;
        super.onTileClickListener = onTileClickListener;
        this.configName = configName;
        mBrightnessObserver = new BluLightObserver(mHandler);
        mBrightnessObserver.startObserving();

        // receive broadcasts
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_SCREEN_ON);
        mContext.registerReceiverAsUser(mBroadcastReceiver, UserHandle.ALL, filter, null, null);

        mSensorManager = (SensorManager)mContext.getSystemService(Context.SENSOR_SERVICE);
        mPowerManager = (PowerManager)mContext.getSystemService(Context.POWER_SERVICE);
        mLocalWakeLock = mPowerManager.newWakeLock(32, "MyPower");
	 //removed by lihuangyuan,for do not use sensor
        /*mSensorManager.registerListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY),
                SensorManager.SENSOR_DELAY_NORMAL);*/
		
    }

    @Override
    protected void handleDestroy() {
        super.handleDestroy();
        mBrightnessObserver.stopObserving();
        mContext.unregisterReceiver(mBroadcastReceiver);
        if(mSensorManager!=null){
            mSensorManager.unregisterListener(this);
            mLocalWakeLock.release();
        }
    }

    @Override
    public BooleanState newTileState() {
        return new BooleanState();
    }

    @Override
    public void setListening(boolean listening) {
    }

    @Override
    protected void handleUserSwitch(int newUserId) {
    }

    @Override
    public int getMetricsCategory() {
        return MetricsEvent.QS_PANEL;
    }
    @Override
    public CharSequence getTileLabel() {
        return mContext.getString(R.string.quick_settings_blulight_mode);
    }
    @Override
    public Intent getLongClickIntent(){
		return null;
	}
    @Override
    protected void handleClick() {
        if (ActivityManager.isUserAMonkey()) {
            return;
        }
        boolean newState = getBluLightMode();
        if (super.onTileClickListener != null) {
            super.onTileClickListener.onTileClick(newState, configName);
            Log.d("BrightnessTileDefined", "BrightnessTileDefined--->" + configName);
        }
        toggleBluLightMode(newState);
        newState = !newState;
        refreshState(newState);
    }

    /**
     * Toggle BluLight mode
     */
    private void toggleBluLightMode(final boolean isOpen) {
        AsyncTask.execute(new Runnable() {
            public void run() {
                final ContentResolver mResolver = mContext.getContentResolver();
                Settings.System.putIntForUser(mResolver,
                        Settings.System.PRIZE_BLULIGHT_MODE_STATE,
                        isOpen ? 0 : 1, UserHandle.USER_CURRENT);
                int mBluLightTimeStatus = Settings.System.getIntForUser(mResolver,
                        Settings.System.PRIZE_BLULIGHT_TIME_STATE, 0, UserHandle.USER_CURRENT);
                int mBluLightTimingStatus = Settings.System.getIntForUser(mResolver,
                        Settings.System.PRIZE_BLULIGHT_TIMING_STATE,0, UserHandle.USER_CURRENT);
                if(isOpen){                    
					/*prize-modify for huyanmode-lihuangyuan-2017-06-09-start*/
					if(BLUELIGHT_MTK)
					{
						PictureQuality.enableBlueLight(false);
					}
					
					/*prize-modify for huyanmode-lihuangyuan-2017-06-09-end*/
                    Log.d("BluLight", "PrizeBluLightTileDefined toggleBluLightMode() PictureQuality.enableBlueLight(false)");
                    if(mBluLightTimeStatus == 1){
                        setBluLightTime(101010,Settings.System.PRIZE_BLULIGHT_START_TIME,
                                "com.android.intent_action_open_blulight");
                    }
                } else {                    
					/*prize-modify for huyanmode-lihuangyuan-2017-06-09-start*/
					if(BLUELIGHT_MTK)
					{
						PictureQuality.enableBlueLight(true);
					}
					
					/*prize-modify for huyanmode-lihuangyuan-2017-06-09-end*/
                    if(mBluLightTimingStatus == 1){
                        setBluLightTime(101011,
                                Settings.System.PRIZE_BLULIGHT_END_TIME,
                                "com.android.intent_action_close_blulight");
                    }
                }
                Settings.System.putIntForUser(mResolver,Settings.System.PRIZE_BLULIGHT_TIMING_STATE,
                        0, UserHandle.USER_CURRENT);
            }
        });
    }

    public boolean isTimeHasPassed(String key) {
        int[] mHourAndMinuteArr = parseBluLightTime(key);
        int hour = mHourAndMinuteArr[0];
        int minute = mHourAndMinuteArr[1];
        Log.d("BluLight", "PrizeBluLightTileDefined isTimeHasPassed() key = " + key + ",hour = " + hour + ",minute = " + minute);

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());

        int mYear = calendar.get(Calendar.YEAR);
        int mMonth = calendar.get(Calendar.MONTH);
        int mDay = calendar.get(Calendar.DAY_OF_MONTH);

        Calendar mCalendar = Calendar.getInstance();
        mCalendar.set(Calendar.YEAR, mYear);
        mCalendar.set(Calendar.MONTH, mMonth);
        mCalendar.set(Calendar.DAY_OF_MONTH, mDay);
        mCalendar.set(Calendar.HOUR_OF_DAY, hour);
        mCalendar.set(Calendar.MINUTE, minute);
        mCalendar.set(Calendar.SECOND, 0);
        mCalendar.set(Calendar.MILLISECOND, 0);

        long mCurrentMillis = calendar.getTimeInMillis();
        long mTimerMillis = mCalendar.getTimeInMillis();
        if (mTimerMillis < mCurrentMillis) {
            boolean isEndEarlyStart = false;
            if(Settings.System.PRIZE_BLULIGHT_END_TIME.equals(key)){
                int[] mStartHourAndMinuteArr = parseBluLightTime(Settings.System.PRIZE_BLULIGHT_START_TIME);
                int mStartHour = mStartHourAndMinuteArr[0];
                int mEndHour = mStartHourAndMinuteArr[1];
                Calendar mStartCalendar = Calendar.getInstance();
                mStartCalendar.set(Calendar.YEAR, mYear);
                mStartCalendar.set(Calendar.MONTH, mMonth);
                mStartCalendar.set(Calendar.DAY_OF_MONTH, mDay);
                mStartCalendar.set(Calendar.HOUR_OF_DAY, mStartHour);
                mStartCalendar.set(Calendar.MINUTE, mEndHour);
                mStartCalendar.set(Calendar.SECOND, 0);
                mStartCalendar.set(Calendar.MILLISECOND, 0);

                long mStartMillis = mStartCalendar.getTimeInMillis();
                isEndEarlyStart = mTimerMillis < mStartMillis;
            }

            if(isEndEarlyStart){
                mCalendar.set(Calendar.DAY_OF_MONTH, mDay+1);
            }
            mTimerMillis = mCalendar.getTimeInMillis();
            if(mTimerMillis < mCurrentMillis){
                return true;
            }
            return false;
        }else {
            return false;
        }
    }

    /**
     * Parse BluLight Time
     */
    private int[] parseBluLightTime(String key){
        String mBluLightTimeStatus = Settings.System.getString(mContext.getContentResolver(), key);
        int[] mHourAndMinuteArr = new int[2];
        if(mBluLightTimeStatus != null){
            String[] mTimeArr = mBluLightTimeStatus.split(":");
            for(int i=0;i<mTimeArr.length;i++){
                mHourAndMinuteArr[i] = Integer.parseInt(mTimeArr[i]);
            }
        }
        return mHourAndMinuteArr;
    }

    /**
     * Set BluLight Timer
     */
    private void setBluLightTime(int requestCode,String key, String action){
        Log.d("BluLight","PrizeBluLightTileDefined setBluLightTime() key = " + key);
        AlarmManager alarmManager = (AlarmManager) mContext.getSystemService(Context.ALARM_SERVICE);

        Intent mIntent = new Intent(action);
        PendingIntent operation = PendingIntent.getBroadcast(mContext, requestCode /* requestCode */, mIntent, 0);

        long alarmTime = getAlarmTime(key).getTimeInMillis();

        alarmManager.setExact(AlarmManager.RTC_WAKEUP,alarmTime,operation);
    }

    public Calendar getAlarmTime(String key) {
        int[] mHourAndMinuteArr = parseBluLightTime(key);
        int hour = mHourAndMinuteArr[0];
        int minute = mHourAndMinuteArr[1];
        Log.d("BluLight","PrizeBluLightTileDefined getAlarmTime() key = " + key+",hour = "+hour+",minute = "+minute);

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());

        int mYear = calendar.get(Calendar.YEAR);
        int mMonth = calendar.get(Calendar.MONTH);
        int mDay = calendar.get(Calendar.DAY_OF_MONTH);

        Calendar mCalendar = Calendar.getInstance();
        mCalendar.set(Calendar.YEAR, mYear);
        mCalendar.set(Calendar.MONTH, mMonth);
        mCalendar.set(Calendar.DAY_OF_MONTH, mDay);
        mCalendar.set(Calendar.HOUR_OF_DAY, hour);
        mCalendar.set(Calendar.MINUTE, minute);
        mCalendar.set(Calendar.SECOND, 0);
        mCalendar.set(Calendar.MILLISECOND, 0);

        long mCurrentMillis = calendar.getTimeInMillis();
        long mTimerMillis = mCalendar.getTimeInMillis();

        boolean isTimerEarlyCurrent = mTimerMillis < mCurrentMillis;
        boolean isEndEarlyStart = false;
        if(Settings.System.PRIZE_BLULIGHT_END_TIME.equals(key)){
            int[] mStartHourAndMinuteArr = parseBluLightTime(Settings.System.PRIZE_BLULIGHT_START_TIME);
            int mStartHour = mStartHourAndMinuteArr[0];
            int mEndHour = mStartHourAndMinuteArr[1];
            Calendar mStartCalendar = Calendar.getInstance();
            mStartCalendar.set(Calendar.YEAR, mYear);
            mStartCalendar.set(Calendar.MONTH, mMonth);
            mStartCalendar.set(Calendar.DAY_OF_MONTH, mDay);
            mStartCalendar.set(Calendar.HOUR_OF_DAY, mStartHour);
            mStartCalendar.set(Calendar.MINUTE, mEndHour);
            mStartCalendar.set(Calendar.SECOND, 0);
            mStartCalendar.set(Calendar.MILLISECOND, 0);

            long mStartMillis = mStartCalendar.getTimeInMillis();
            isEndEarlyStart = mTimerMillis < mStartMillis;
        }

        if(isTimerEarlyCurrent || isEndEarlyStart){
            mCalendar.set(Calendar.DAY_OF_MONTH, mDay+1);
        }

        SimpleDateFormat dfs = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        String bgdate = dfs.format(mCalendar.getTime());
        Log.d("BluLight","PrizeBluLightTileDefined Time : "+bgdate);
        return mCalendar;
    }

    /**
     * Cancel timer
     */
    private void cancelAlarm(String action){
        Intent intent = new Intent();
        intent.setAction(action);
        PendingIntent pi = PendingIntent.getBroadcast(mContext, 0, intent, 0);
        AlarmManager am = (AlarmManager) mContext.getSystemService(Context.ALARM_SERVICE);
        am.cancel(pi);
    }

    /**
     * Whether the current eye protection mode is open
     */
    private boolean getBluLightMode() {
        int mBluLightModeStatus = Settings.System.getIntForUser(mContext.getContentResolver(),
                Settings.System.PRIZE_BLULIGHT_MODE_STATE, 0, UserHandle.USER_CURRENT);

        boolean isOpenBluLightMode = mBluLightModeStatus != 0;
        return isOpenBluLightMode;

    }

    @Override
    public void onSensorChanged(final SensorEvent event) {
        AsyncTask.execute(new Runnable() {
            public void run() {
                final ContentResolver mResolver = mContext.getContentResolver();
                int mBluLightModeStatus = Settings.System.getInt(mResolver,
                        Settings.System.PRIZE_BLULIGHT_MODE_STATE, 0);
                int mBluLightTimeStatus = Settings.System.getInt(mResolver,
                        Settings.System.PRIZE_BLULIGHT_TIME_STATE, 0);
                boolean isTimeHasPassed = isTimeHasPassed(Settings.System.PRIZE_BLULIGHT_END_TIME);

                boolean shouldSetBluLightMode = mBluLightModeStatus == 1 && mBluLightTimeStatus == 1 && !isTimeHasPassed;
                boolean shouldExitBluLightMode = mBluLightModeStatus == 0 && mBluLightTimeStatus == 1 && isTimeHasPassed;
                Log.d("BluLight","onSensorChanged() shouldSetBluLightMode = "+shouldSetBluLightMode);
                Log.d("BluLight","onSensorChanged() shouldExitBluLightMode = "+shouldExitBluLightMode);
                float[] its = event.values;
                if (its != null && event.sensor.getType() == Sensor.TYPE_PROXIMITY) {
                    if (its[0] == 0.0) {
                        // Close to mobile phone
                        Log.d("BluLight","onSensorChanged() Close to mobile phone");
                    } else {
                        // Stay away from mobile phones
                        Log.d("BluLight","onSensorChanged() Stay away from mobile phones");
                        if(shouldSetBluLightMode){
                            mHandler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    Log.d("BluLight","onSensorChanged() postDelayed().run()");                                    
									/*prize-modify for huyanmode-lihuangyuan-2017-06-09-start*/
									if(BLUELIGHT_MTK)
									{
										PictureQuality.enableBlueLight(true);
									}
									
									/*prize-modify for huyanmode-lihuangyuan-2017-06-09-end*/
                                }
                            }, 1000);
                        } else if(shouldExitBluLightMode){
                            mHandler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    Log.d("BluLight","onSensorChanged() postDelayed().run()");                                    
									/*prize-modify for huyanmode-lihuangyuan-2017-06-09-start*/
									if(BLUELIGHT_MTK)
									{
										PictureQuality.enableBlueLight(false);
									}
									
									/*prize-modify for huyanmode-lihuangyuan-2017-06-09-end*/
                                }
                            }, 1000);
                        }
                    }
                }
            }
        });
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    protected void handleUpdateState(BooleanState state, Object arg) {
        if (state.value) {
            mWasLastOn = SystemClock.uptimeMillis();
        }

        if (arg instanceof Boolean) {
            state.value = (Boolean) arg;
        }

        if (!state.value && mWasLastOn != 0) {
            if (SystemClock.uptimeMillis() > mWasLastOn + RECENTLY_ON_DURATION_MILLIS) {
                mWasLastOn = 0;
            } else {
                mHandler.removeCallbacks(mRecentlyOnTimeout);
                mHandler.postAtTime(mRecentlyOnTimeout, mWasLastOn + RECENTLY_ON_DURATION_MILLIS);
            }
        }
        state.value = getBluLightMode();

        // Always show the tile when the flashlight is or was recently on. This is needed because
        // the camera is not available while it is being used for the flashlight.
        //state.visible = mWasLastOn != 0 || mFlashlightController.isAvailable();
        //state.visible = true;
        state.label = mContext.getString(labelId);
        //state.iconId = state.value ? drawableOn : drawableOff;
        state.icon = ResourceIcon.get(state.value ? drawableOn : drawableOff);
        state.colorId = state.value ? 1 : 0;
        state.contentDescription = state.label;
    }

    @Override
    protected String composeChangeAnnouncement() {
        //if (mState.value) {
        //    return mContext.getString(R.string.accessibility_quick_settings_flashlight_changed_on);
        //} else {
        //    return mContext.getString(R.string.accessibility_quick_settings_flashlight_changed_off);
        //}
        return null;
    }

    private Runnable mRecentlyOnTimeout = new Runnable() {
        @Override
        public void run() {
            refreshState();
        }
    };

    private class BluLightObserver extends ContentObserver {

        private final Uri BLULIGHT_MODE_URI =
                Settings.System.getUriFor(Settings.System.PRIZE_BLULIGHT_MODE_STATE);
//        private final Uri BLULIGHT_TIMING_URI = Settings.System
//                .getUriFor(Settings.System.PRIZE_BLULIGHT_TIMING_STATE);

        public BluLightObserver(Handler handler) {
            super(handler);
        }

        @Override
        public void onChange(boolean selfChange) {
            onChange(selfChange, null);
        }

        @Override
        public void onChange(boolean selfChange, Uri uri) {
            if (selfChange) return;
            if(BLULIGHT_MODE_URI.equals(uri)){
                refreshState();
            }
//            else if(BLULIGHT_TIMING_URI.equals(uri)){
//                final ContentResolver mResolver = mContext.getContentResolver();
//                int mBluLightModeStatus = Settings.System.getInt(mResolver,
//                        Settings.System.PRIZE_BLULIGHT_MODE_STATE, 0);
//                int mBluLightTimeStatus = Settings.System.getInt(mResolver,
//                        Settings.System.PRIZE_BLULIGHT_TIME_STATE, 0);
//                int mBluLightTimingStatus = Settings.System.getInt(mResolver,
//                        Settings.System.PRIZE_BLULIGHT_TIMING_STATE,0);
//                if (BLULIGHT_TIMING_URI.equals(uri)) {
//                    Log.d("BluLight", "onChange() mBluLightStatus = "+mBluLightModeStatus+",mBluLightTimeStatus = "+
//                            mBluLightTimeStatus+ ",mBluLightTimingStatus = "+mBluLightTimingStatus);
//                    if(mBluLightModeStatus == 0 && mBluLightTimeStatus == 1 && mBluLightTimingStatus == 1){
//                        PictureQuality.enableBlueLight(true);
//                        int mBlueLightStrength = PictureQuality.getBlueLightStrength();
//                        Log.d("BluLight", "onChange() mBlueLightStrength = "+mBlueLightStrength);
//                        PictureQuality.setBlueLightStrength(mBlueLightStrength);
//                    }
//                }
//            }
        }

        public void startObserving() {
            final ContentResolver cr = mContext.getContentResolver();
            cr.unregisterContentObserver(this);
            cr.registerContentObserver(BLULIGHT_MODE_URI,
                    false, this, UserHandle.USER_ALL);
//            cr.registerContentObserver(BLULIGHT_TIMING_URI,
//                    false, this, UserHandle.USER_ALL);
        }

        public void stopObserving() {
            final ContentResolver cr = mContext.getContentResolver();
            cr.unregisterContentObserver(this);
        }
    }
}
