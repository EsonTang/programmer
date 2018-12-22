package com.prize.music.helpers.utils;

import android.text.TextUtils;

/**
 * log工具类。发布上线 消除打印log、日志
 * 
 * @author Administrator
 *
 */
public class LogUtils {
	public static boolean isDebug = false;

	public static void v(String tag, String msg, Throwable t) {
		if (isDebug && !TextUtils.isEmpty(tag) && !TextUtils.isEmpty(msg))
			android.util.Log.v(tag, msg, t);
	}

	public static void d(String tag, String msg) {
		if (isDebug && !TextUtils.isEmpty(tag) && !TextUtils.isEmpty(msg))
			android.util.Log.d(tag, msg);
	}

	public static void d(String tag, String msg, Throwable t) {
		if (isDebug && !TextUtils.isEmpty(tag) && !TextUtils.isEmpty(msg))
			android.util.Log.d(tag, msg, t);
	}

	public static void i(String tag, String msg) {
		if (isDebug && !TextUtils.isEmpty(tag) && !TextUtils.isEmpty(msg))
			android.util.Log.i(tag, msg);
	}

	public static void i(String tag, String msg, Throwable t) {
		if (isDebug && !TextUtils.isEmpty(tag) && !TextUtils.isEmpty(msg))
			android.util.Log.i(tag, msg, t);
	}

	public static void w(String tag, String msg) {
		if (isDebug && !TextUtils.isEmpty(tag) && !TextUtils.isEmpty(msg))
			android.util.Log.w(tag, msg);
	}

	public static void w(String tag, String msg, Throwable t) {
		if (isDebug && !TextUtils.isEmpty(tag) && !TextUtils.isEmpty(msg))
			android.util.Log.w(tag, msg, t);
	}

	public static void e(String tag, String msg) {
		if (isDebug && !TextUtils.isEmpty(tag) && !TextUtils.isEmpty(msg))
			android.util.Log.e(tag, msg);
	}

	public static void e(String tag, String msg, Throwable t) {
		if (isDebug && !TextUtils.isEmpty(tag) && !TextUtils.isEmpty(msg))
			android.util.Log.e(tag, msg, t);
	}
}
