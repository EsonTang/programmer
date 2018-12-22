package com.android.calendar.hormonth;

import android.annotation.SuppressLint;
import android.content.Context;
import android.text.format.Time;
import android.util.Log;

import com.android.calendar.Utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;


public class DateUtil {
	public static final DateUtil dateUtil = new DateUtil();
	private DateUtil(){

	}

	public int getMonthDays(int year, int month) {
		if (month > 12) {
			month = 1;
			year += 1;
		} else if (month < 1) {
			month = 12;
			year -= 1;
		}
		int[] arr = { 31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31 };
		int days = 0;

		if ((year % 4 == 0 && year % 100 != 0) || year % 400 == 0) {
			arr[1] = 29; 
		}

		try {
			days = arr[month - 1];
		} catch (Exception e) {
			e.getStackTrace();
		}

		return days;
	}
	
	public int getYear() {
		Time mTime = new Time();
		mTime.setToNow();
		mTime.normalize(true);
		return mTime.year;
	}

	public int getMonth() {
		Time mTime = new Time();
		mTime.setToNow();
		mTime.normalize(true);
		return mTime.month + 1;
	}

	public int getCurrentMonthDay() {
		Time mTime = new Time();
		mTime.setToNow();
		mTime.normalize(true);
		return mTime.monthDay;
	}

	public int getCurrentMonthDay(String timeZone) {
		Time mTime = new Time(timeZone);
		mTime.setToNow();
		mTime.normalize(true);
		return mTime.monthDay;
	}

	
	public int getWeekDay() {
		Time mTime = new Time();
		mTime.setToNow();
		mTime.normalize(true);
		return mTime.WEEK_DAY;
	}

	public int getHour() {
		Time mTime = new Time();
		mTime.setToNow();
		mTime.normalize(true);
		return mTime.hour;
	}
	public int getMinute() {
		Time mTime = new Time();
		mTime.setToNow();
		mTime.normalize(true);
		return mTime.minute;
	}


	public int[] getWeekSunday(int year, int month, int day, int pervious) {
		int[] time = new int[3];
		Calendar c = Calendar.getInstance();
		c.set(Calendar.YEAR, year);
		c.set(Calendar.MONTH, month);
		c.set(Calendar.DAY_OF_MONTH, day);
		c.add(Calendar.DAY_OF_MONTH, pervious);
		time[0] = c.get(Calendar.YEAR);
		time[1] = c.get(Calendar.MONTH )+1;
		time[2] = c.get(Calendar.DAY_OF_MONTH);
		return time;

	}

	public int getWeekDayFromDate(int year, int month) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(getDateFromString(year, month));
		int week_index = cal.get(Calendar.DAY_OF_WEEK) - 1;
		if (week_index < 0) {
			week_index = 0;
		}
		return week_index;
	}

	@SuppressLint("SimpleDateFormat")
	public static Date getDateFromString(int year, int month) {
		String dateString = year + "-" + (month > 9 ? month : ("0" + month))
				+ "-01";
		Date date = null;
		try {
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
			date = sdf.parse(dateString);
		} catch (ParseException e) {
			System.out.println(e.getMessage());
		}
		return date;
	}
	public boolean isToday(CustomDate date){
		return(date.year == getYear() &&
				date.month == getMonth()
				&& date.day == getCurrentMonthDay());
	}
	
	public  boolean isCurrentMonth(CustomDate date){
		return(date.year == getYear() &&
				date.month == getMonth());
	}

	public int setRows(int year, int month, Context context){
		Calendar c = Calendar.getInstance();
		if( Utils.getFirstDayOfWeek(context)==1){
			c.setFirstDayOfWeek(Calendar.MONDAY);
		}else if( Utils.getFirstDayOfWeek(context)==0){
			c.setFirstDayOfWeek(Calendar.SUNDAY);
		}else if( Utils.getFirstDayOfWeek(context)==6){
			c.setFirstDayOfWeek(Calendar.SATURDAY);
		}
		c.set(Calendar.YEAR, year);
		c.set(Calendar.MONTH, month-1);
		c.set(Calendar.DATE,1);
		return c.getActualMaximum(Calendar.WEEK_OF_MONTH);
	}

	public String setTitleMonth(int month){
		String monthStr;
		switch (month){
			case 1:
				monthStr = "Jan.";
				break;
			case 2:
				monthStr = "Feb.";
				break;
			case 3:
				monthStr = "Mar.";
				break;
			case 4:
				monthStr = "Apr.";
				break;
			case 5:
				monthStr = "May.";
				break;
			case 6:
				monthStr = "Jun.";
				break;
			case 7:
				monthStr = "Jul.";
				break;
			case 8:
				monthStr = "Aug.";
				break;
			case 9:
				monthStr = "Sep.";
				break;
			case 10:
				monthStr = "Oct.";
				break;
			case 11:
				monthStr = "Nov.";
				break;
			case 12:
				monthStr = "Dec.";
				break;
			default:
				monthStr = "";
				break;
		}
		return monthStr;
	}


	public String getCurrentTimeZone()
	{
		TimeZone tz = TimeZone.getDefault();
		return createGmtOffsetString(true,true,tz.getRawOffset());
	}

	private String createGmtOffsetString(boolean includeGmt,
										 boolean includeMinuteSeparator, int offsetMillis) {
		int offsetMinutes = offsetMillis / 60000;
		char sign = '+';
		if (offsetMinutes < 0) {
			sign = '-';
			offsetMinutes = -offsetMinutes;
		}
		StringBuilder builder = new StringBuilder(9);
		if (includeGmt) {
			builder.append("GMT");
		}
		builder.append(sign);
		appendNumber(builder, 2, offsetMinutes / 60);
		/*if (includeMinuteSeparator) {
			builder.append(':');
		}
		appendNumber(builder, 2, offsetMinutes % 60);*/
		return builder.toString();
	}

	private void appendNumber(StringBuilder builder, int count, int value) {
		String string = Integer.toString(value);
		for (int i = 0; i < count - string.length(); i++) {
//			builder.append('0');
		}
		builder.append(string);
	}
}
