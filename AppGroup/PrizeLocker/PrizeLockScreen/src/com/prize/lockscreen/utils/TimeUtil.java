package com.prize.lockscreen.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.regex.Pattern;

/**
 * Time to deal with
 * 
 * @author sunyunlong
 * modified by fanjunchen
 * 
 */
public class TimeUtil {

	private static final String TAG = TimeUtil.class.getName();
	public static final String PATTERN_1 = "yyyy-MM-dd HH:mm:ss";
	public static final String PATTERN_2 = "yyyy-MM-dd HH:mm";
	public static final String PATTERN_3 = "yyyy-MM-dd";
	public static final String PATTERN_4 = "mm:ss";
	private static final long TIME_DELAY = 1 * 60 * 1000;
	public static final int RESULT_OK = 0;
	public static final int NEGATIVE = -1;
	public static final int SHORT = 1;
	
	private static Calendar sCalendar = null;

	/**
	 * 获取当前的hour：minute
	 * 
	 * @return HH-mm
	 */
	public static String getCurrentTime() {
		// 取得当前的hour minute即可
		sCalendar = Calendar.getInstance();
		SimpleDateFormat curTimeFormat = new SimpleDateFormat("HH:mm");
		String curTime = curTimeFormat.format(sCalendar.getTime());
		return curTime;
	}
	/***
	 * 获取当前的时间,通过Calendar对象获取
	 * @return
	 */
	public static Date getTime() {
		sCalendar = Calendar.getInstance();
		return sCalendar.getTime();
	}

	/**
	 * 获取当前日期的年月日
	 * 
	 * @return yyyy-MM-dd
	 */
	public static String getCurrentDate() {
		sCalendar = Calendar.getInstance();
		SimpleDateFormat curDateFormat = new SimpleDateFormat(PATTERN_3);
		String curDate = curDateFormat.format(sCalendar.getTime());
		return curDate;
	}

	public static String getCurrentDateAndTime() {
		sCalendar = Calendar.getInstance();
		SimpleDateFormat curDateFormat = new SimpleDateFormat(PATTERN_2);
		String result = curDateFormat.format(sCalendar.getTime());
		return result;
	}

	/**
	 * 毫秒转换成日期格式
	 * 
	 * @param pattern
	 *            日期格式
	 * @param dateTime
	 *            毫秒
	 * @return
	 */
	public static String ConvertMillisecondToDateTime(String pattern,
			long dateTime) {
		SimpleDateFormat sDateFormat = new SimpleDateFormat(pattern);
		return sDateFormat.format(new Date(dateTime + 0));
	}

	/**
	 * 将日期转换成毫秒 表示的自 1970 年 1 月 1 日 00:00:00 GMT 以来的毫秒数。
	 * 
	 * @param pattern
	 *            日期格式
	 * @param dateTime
	 *            日期
	 * @return
	 */
	public static long ConvertDateTimeToMillisecond(String pattern,
			String dateTime) {
		SimpleDateFormat sdf = new SimpleDateFormat(pattern);
		try {
			Date date = sdf.parse(dateTime);
			return date.getTime();
		} catch (ParseException e) {
			e.printStackTrace();
			LogUtil.d(TAG, e.getMessage());
		}
		return 0;
	}

	/**
	 * 定时发送的时间比当前时间大的判断
	 * 
	 * @param timeMillisecond
	 *            定时发送的时间
	 * @return
	 */
	public static int checkTimeCondition(long timeMillisecond) {
		long difference = timeMillisecond
				- ConvertDateTimeToMillisecond(PATTERN_2,
						getCurrentDateAndTime());
		LogUtil.d(TAG, "--->checkTimeCondition() difference：" + difference
				/ 1000 + "s");
		if (difference > TIME_DELAY) {
			return RESULT_OK;
		} else if (difference < 0) {
			return NEGATIVE;
		} else {
			return SHORT;
		}
	}

	/**
	 * 将毫秒转化成时间格式 HH:mm
	 * 
	 * @param timeMillisecond
	 *            毫秒
	 * @return
	 */
	public static String millisecondToTime(long timeMillisecond) {
		SimpleDateFormat sDateFormat = new SimpleDateFormat(PATTERN_2);
		String dateStr = sDateFormat.format(new Date(timeMillisecond + 0));
		String[] temp = dateStr.split(" ");
		return temp[1];
	}
	
	/**
	 * 毫秒装换成MM:SS
	 * @param timeMillisecond
	 * @return
	 */
	public static String millisecondToMM(long timeMillisecond) {
		SimpleDateFormat sDateFormat = new SimpleDateFormat(PATTERN_4);
		String timeStr = sDateFormat.format(new Date(timeMillisecond + 0));
		return timeStr;
	}

	/**
	 * 根据年 月 获取对应的月份 天数
	 * 
	 * @param strCurrentYearAndmonth
	 *            格式yyyy-mm
	 * @return
	 */
	public static int getDaysByYearMonth(String strCurrentYearAndmonth) {
		String[] temp = strCurrentYearAndmonth.split("-");
		int year = Integer.valueOf(temp[0]);
		int month = Integer.valueOf(temp[1]);
		Calendar a = Calendar.getInstance();
		a.set(Calendar.YEAR, year);
		a.set(Calendar.MONTH, month - 1);
		a.set(Calendar.DATE, 1);
		a.roll(Calendar.DATE, -1);
		int maxDate = a.get(Calendar.DATE);
		return maxDate;
	}

	/**
	 * 根据日期 找到对应日期的 星期
	 * 
	 * @param date
	 *            格式 yyyy-MM-dd
	 * @return
	 */
	public static String getDayOfWeekByDate(String date) {
		String dayOfweek = "-1";
		try {
			SimpleDateFormat myFormatter = new SimpleDateFormat(PATTERN_3);
			Date myDate = myFormatter.parse(date);
			SimpleDateFormat formatter = new SimpleDateFormat("E");
			String str = formatter.format(myDate);
			dayOfweek = str;

		} catch (Exception e) {
			System.out.println("错误!");
		}
		return dayOfweek;
	}

	/**
	 * 根据毫秒  找到对应日期 星期
	 * @param when 日期对应的毫秒
	 * @return 
	 */
	public static String getDayOfWeekByDate(long when) {
		String date = ConvertMillisecondToDateTime(PATTERN_3, when);
		return getDayOfWeekByDate(date);
	}
	
	private static Pattern ALPHA_P = Pattern.compile("(?i)[a-z]");
	/***
	 * 给定的字符串中是否包括至少一个字母
	 * @param str
	 * @return
	 */
	public static boolean hasAlpha(String str) {
		return ALPHA_P.matcher(str).find();
	}
}
