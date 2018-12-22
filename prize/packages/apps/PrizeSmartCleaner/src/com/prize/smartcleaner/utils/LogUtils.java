package com.prize.smartcleaner.utils;

import android.os.Build;
import android.util.Log;

/**
 * Created by xiarui on 2018/1/3.
 */

public class LogUtils {

    public static final String TAG = "PrizeSysClean";
    public static final boolean DEBUG = Build.TYPE.equals("eng") || Build.TYPE.equals("userdebug");

    public static void d(String tag, String msg) {
        if (DEBUG) {
            Log.d(TAG,  tag + ":" + msg);
        }
    }

    public static void i(String tag, String msg) {
        Log.i(TAG,  tag + ":" + msg);
    }

    public static void e(String tag, String msg) {
        if (DEBUG) {
            Log.i(TAG,  tag + ":" + msg);
        }
    }

    public static void w(String tag, String msg) {
        if (DEBUG) {
            Log.i(TAG,  tag + ":" + msg);
        }
    }
}
