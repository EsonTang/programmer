package com.prize.smartcleaner.utils;

import android.util.Log;

/**
 * Created by xiarui on 2018/1/3.
 */

public class LogUtils {

    public static final boolean DEBUG = true;

    public static void d(String tag, String msg) {
        if (DEBUG) {
            Log.d("xr",  tag + "----" + msg);
        }
    }

    public static void i(String tag, String msg) {
        if (DEBUG) {
            Log.i("xr",  tag + "----" + msg);
        }
    }

    public static void e(String tag, String msg) {
        if (DEBUG) {
            Log.i("xr",  tag + "----" + msg);
        }
    }

    public static void w(String tag, String msg) {
        if (DEBUG) {
            Log.i("xr",  tag + "----" + msg);
        }
    }
}
