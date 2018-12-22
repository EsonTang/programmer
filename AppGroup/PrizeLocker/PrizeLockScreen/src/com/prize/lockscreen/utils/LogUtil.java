
package com.prize.lockscreen.utils;

import android.util.Log;
//
/**
 * Log工具类
 */
public class LogUtil {
    public static final String TAG = "prizeLockScreen";

    // 锁，是否关闭Log日志输出
    public static boolean LogON = true;

    public static void e(String Tag, String msg) {
        if (LogON) {
            Log.e(TAG, "[" + Tag + "]" + msg);
        }
    }
    public static void e(String tag,String msg, Throwable tr){
        if(LogON){
            Log.e(TAG,"[" + tag +"]:"+ msg,tr);
        }
    }
    public static void d(String Tag, String msg) {
        if (LogON) {
            Log.d(TAG, "[" + Tag + "]" + msg);
        }
    }

    public static void i(String Tag, String msg) {
        if (LogON) {
            Log.i(TAG, "[" + Tag + "]" + msg);
        }
    }

    public static void v(String Tag, String msg) {
        if (LogON) {
            Log.v(TAG, "[" + Tag + "]" + msg);
        }
    }

    public static void w(String Tag, String msg) {
        if (LogON) {
            Log.w(TAG, "[" + Tag + "]" + msg);
        }
    }
}
