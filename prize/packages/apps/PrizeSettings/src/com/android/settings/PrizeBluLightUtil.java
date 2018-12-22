package com.android.settings;

import android.content.Context;
import android.provider.Settings;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * Created by Administrator on 2017/5/9.
 */

public class PrizeBluLightUtil {
    public static final String PRIZE_BLULIGHT_MODE_KEY ="prize_blulight_mode_state";
    public static final String PRIZE_BLULIGHT_TIME_KEY ="prize_blulight_time_state";

    public static final String BLULIGHT_TIME_OPEN_ACTION ="com.android.intent_action_open_blulight";
    public static final String BLULIGHT_TIME_CLOSE_ACTION ="com.android.intent_action_close_blulight";
    public static final String BOOT_COMPLETED ="android.intent.action.BOOT_COMPLETED";
	public static final String HUYAN_BOOT_COMPLETED ="com.prize.huyan.boot_completed";

    public static final int TIME_START_REQUEST_CODE = 101010;
    public static final int TIME_END_REQUEST_CODE = 101011;

    public static final String TIME_TPYE_KEY = "time_start_type_key";
    public static final int TIME_START_TYPE = 100001;
    public static final int TIME_END_TYPE = 100002;
    public static final int BOOT_COMPLETED_TYPE = 100003;

    //public static final int BLULIGHT_MIN_VALUE = 0; //prizehuyan
    public static final int BLULIGHT_MIN_VALUE = 130;//mtkhuyan
    public static final boolean BLUELIGHT_MTK = true;//use mtk or prize

    public static int[] parseBluLightTime(Context mContext,String key){
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

    public static boolean isTimeHasPassed(Context mContext,String key) {
        int[] mHourAndMinuteArr = parseBluLightTime(mContext,key);
        int hour = mHourAndMinuteArr[0];
        int minute = mHourAndMinuteArr[1];
        Log.d("BluLight", "PrizeBluLightService getAlarmTime() key = " + key + ",hour = " + hour + ",minute = " + minute);

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
                int[] mStartHourAndMinuteArr = parseBluLightTime(mContext,Settings.System.PRIZE_BLULIGHT_START_TIME);
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

    public static Calendar getAlarmTime(Context mContext,String key) {
        int[] mHourAndMinuteArr = parseBluLightTime(mContext,key);
        int hour = mHourAndMinuteArr[0];
        int minute = mHourAndMinuteArr[1];
        Log.d("BluLight","PrizeBluLightService getAlarmTime() key = " + key+",hour = "+hour+",minute = "+minute);

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
        if(android.provider.Settings.System.PRIZE_BLULIGHT_END_TIME.equals(key)){
            int[] mStartHourAndMinuteArr = parseBluLightTime(mContext,Settings.System.PRIZE_BLULIGHT_START_TIME);
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
        Log.d("BluLight","PrizeBluLightService Time : "+bgdate);
        return mCalendar;
    }
}
