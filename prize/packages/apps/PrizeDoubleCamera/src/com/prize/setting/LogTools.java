package com.prize.setting;

import com.android.camera.Log;

public class LogTools {
	public static final String TAG = "CameraApp";
	private static final boolean DEBUG = true;
	
	public static void e(String tag, String msg) {
		if (DEBUG) {
			Log.e(TAG, tag + "----------->" + msg);
		}
	}
	
	public static void w(String tag, String msg) {
		if (DEBUG) {
			Log.w(TAG, tag + "----------->" + msg);
		}
	}
	
	public static void i(String tag, String msg) {
		if (DEBUG) {
			Log.i(TAG, tag + "----------->" + msg);
		}
	}
	
	public static void d(String tag, String msg) {
		if (DEBUG) {
			Log.d(TAG, tag + "----------->" + msg);
		}
	}
	
	public static void v(String tag, String msg) {
		if (DEBUG) {
			Log.v(TAG, tag + "----------->" + msg);
		}
	}
}

