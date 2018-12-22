package com.prize.app.util;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import android.text.format.DateUtils;

public class PrizeDateUtils extends DateUtils {

	/**
	 * 获取格式化后的时间字符串<br/>
	 * yyyy-MM-dd HH:mm
	 * 
	 * @param time
	 * @return
	 */
	public static String getTimeYMDHMS(long time) {
		return getTimeYMDHMS(new Date(time));
	}

	/**
	 * 获取格式化后的时间字符串<br/>
	 * yyyy-MM-dd HH:mm
	 * 
	 */
	public static String getTimeYMDHMS(Date date) {
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm",
				Locale.SIMPLIFIED_CHINESE);
		String strDate = formatter.format(date);
		return strDate;
	}

	/**
	 * 获取日期(天)<br/>
	 * dd
	 * 
	 */
	public static String getDAY(Date date) {
		SimpleDateFormat formatter = new SimpleDateFormat("dd",
				Locale.SIMPLIFIED_CHINESE);
		String strDate = formatter.format(date);
		return strDate;
	}

	public static String getFormatDate(Date date, String template) {
		SimpleDateFormat formatter = new SimpleDateFormat(template,
				Locale.SIMPLIFIED_CHINESE);
		String strDate = formatter.format(date);
		return strDate;
	}
}
