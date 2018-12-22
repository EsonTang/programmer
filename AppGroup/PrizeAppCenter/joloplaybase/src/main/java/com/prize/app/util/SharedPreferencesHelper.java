package com.prize.app.util;

import android.app.Activity;
import android.content.SharedPreferences;

import com.prize.app.BaseApplication;

/**
 * Created by Tamic on 2016-03-24.
 */

/**
 * @创建者 longbaoxiu
 * @创建者 2016/12/17.15:08
 * @描述
 */
public class SharedPreferencesHelper {
//    private static SharedPreferencesHelper sInstance;
    private static String sSettings="sSettings";
//
//
//    public static synchronized SharedPreferencesHelper getInstance(Context outContext) {
//        if (sInstance == null) {
//            sInstance = new SharedPreferencesHelper(outContext.getApplicationContext());
//        }
//        return sInstance;
//    }

//    private SharedPreferencesHelper(Context outContext) {
//        String OUT_APP_PACKAGENAME = outContext.getPackageName();
//        sSettings = outContext.getSharedPreferences(OUT_APP_PACKAGENAME, Context.MODE_MULTI_PROCESS);
//    }

    public static void putLong(long lon) {
        synchronized (sSettings) {
//            sSettings.edit().putLong("TimeDifference", lon).commit();
            SharedPreferences share = BaseApplication.curContext
                    .getSharedPreferences(BaseApplication.curContext.getPackageName(), Activity.MODE_MULTI_PROCESS);

            if (share != null) {
                share.edit().putLong("TimeDifference", lon).apply();
            }
        }
    }


    public static long getTimeDifference(long defValue) {
        synchronized (sSettings) {
//            return sSettings.getLong("TimeDifference", defValue);
            SharedPreferences share = BaseApplication.curContext
                    .getSharedPreferences(BaseApplication.curContext.getPackageName(), Activity.MODE_MULTI_PROCESS);
            if (share != null) {
                return share.getLong("TimeDifference", defValue);
            }
            return defValue;
        }
    }



}
