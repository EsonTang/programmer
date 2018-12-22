/*******************************************
 *版权所有©2015,深圳市铂睿智恒科技有限公司
 *
 *内容摘要：封装Log类
 *当前版本：V1.0
 *作	者：fuqiang
 *完成日期：2015-8-4
 *********************************************/

package com.android.gallery3d.util;

import android.util.Log;

public class LogUtils {
	public static final boolean DEBUG = true;

	public LogUtils() {

	}

	public static void d(String tag, String msg) {
		if (DEBUG) {
			Log.d(tag, msg);
		}
	}

	public static void v(String tag, String msg) {
		if (DEBUG) {
			Log.v(tag, msg);
		}
	}

	public static void w(String tag, String msg) {
		if (DEBUG) {
			Log.w(tag, msg);
		}
	}

	public static void i(String tag, String msg) {
		if (DEBUG) {
			Log.i(tag, msg);
		}
	}

	public static void e(String tag, String msg) {
		if (DEBUG) {
			Log.e(tag, msg);
		}
	}

}
