
/*******************************************
 *版权所有©2015,深圳市铂睿智恒科技有限公司
 *
 *内容摘要：日期工具类
 *当前版本：V1.0
 *作	者：zhouerlong
 *完成日期：2015-8-5
 *********************************************/
package com.android.launcher3;

import java.text.SimpleDateFormat;
import java.util.Date;

import android.content.ContentResolver;
import android.content.Context;

public class DateUtil {

	public static String getCurrentTime(Context context) {
		ContentResolver cv = context.getContentResolver(); 
		String strTimeFormat = android.provider.Settings.System.getString(cv,
				android.provider.Settings.System.TIME_12_24);
		if (strTimeFormat == null) {
			return getTime12(); 
		}
		if (strTimeFormat.equals("24")) {
			return getTime24();
		} else {
			return getTime12();
		}

	}

	public static String getTimestamp(Context context) {
		ContentResolver cv = context.getContentResolver(); 
		String strTimeFormat = android.provider.Settings.System.getString(cv,
				android.provider.Settings.System.TIME_12_24);
		if (strTimeFormat != null) {

			if (strTimeFormat.equals("24")) {
				return "";
			} else {
				return getTimeTD();
			}
		} else
			return getTimeTD();
	}

	public static String getTimeTD() {
		SimpleDateFormat df = new SimpleDateFormat("a");
		Date date = new Date(System.currentTimeMillis());
		return df.format(date);

	}

	public static String getTime24() { 
		SimpleDateFormat df = new SimpleDateFormat("HH-mm");
		Date date = new Date(System.currentTimeMillis());
		return df.format(date);
	}

	public static String getTime12() { 
		SimpleDateFormat df = new SimpleDateFormat("hh-mm");
		Date date = new Date(System.currentTimeMillis());

		return df.format(date);

	}

	public static String getDate(Context context) {
		String str = "dd";
		SimpleDateFormat df = new SimpleDateFormat(str);
		Date date = new Date(System.currentTimeMillis());
		return df.format(date);

	}

	public static String getWeek() {
		SimpleDateFormat df = new SimpleDateFormat("E");
		Date date = new Date(System.currentTimeMillis());
		return df.format(date);

	}
}
