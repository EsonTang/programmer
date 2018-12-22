package com.prize.weather.util;

import android.annotation.SuppressLint;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * 
 * @author wangzhong
 *
 */
@SuppressLint("SimpleDateFormat")
public class CalendarUtils {

	public static boolean isDayTime() {
		Calendar cal = Calendar.getInstance();
		int hour = cal.get(Calendar.HOUR_OF_DAY);
		if (6 < hour && hour < 18) {
			return true;
		} else {
			return false;
		}
	}
	
	public static String getCurrentTime() {
		Date now = new Date();
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
		String currentTime = dateFormat.format(now);
		return currentTime;
	}
	
	public static Date formatDate(String date) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		Date time = null;
		try {
		   time = sdf.parse(date);
		} catch (ParseException e) {
		   e.printStackTrace();
		}
		return time;
	}
	
	public static Date formatDate1(String date) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm");
		Date time = null;
		try {
			time = sdf.parse(date);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return time;
	}
	
	public static boolean isToday(Date date) {
	    Calendar c1 = Calendar.getInstance();
	    c1.setTime(date);
	    int year1 = c1.get(Calendar.YEAR);
        int month1 = c1.get(Calendar.MONTH) + 1;
        int day1 = c1.get(Calendar.DAY_OF_MONTH);
        
        Calendar c2 = Calendar.getInstance();
        c2.setTime(new Date());
        int year2 = c2.get(Calendar.YEAR);
        int month2 = c2.get(Calendar.MONTH) + 1;
        int day2 = c2.get(Calendar.DAY_OF_MONTH);
        
        if (year1 == year2 && month1 == month2 && day1 == day2) {
        	return true;
        }
	    return false;
	}
	
	public static boolean isSameDay(String sDate1, String sDate2) {
		Date date1 = formatDate(sDate1);
		Date date2 = formatDate(sDate2);
		if (null != date1 && null != date2 && date1.getTime() == date2.getTime()) {
			return true;
		}
		return false;
	}
	
	public static boolean isLatestDate(String historyDate, String lastestDate) {
	    /*Calendar c1 = Calendar.getInstance();
	    c1.setTime(formatDate1(historyDate));
	    int year1 = c1.get(Calendar.YEAR);
        int month1 = c1.get(Calendar.MONTH) + 1;
        int day1 = c1.get(Calendar.DAY_OF_MONTH);
        int hour1 = c1.get(Calendar.HOUR_OF_DAY);
        int minute1 = c1.get(Calendar.MINUTE);
        
	    Calendar c2 = Calendar.getInstance();
	    c2.setTime(formatDate1(lastestDate));
	    int year2 = c2.get(Calendar.YEAR);
        int month2 = c2.get(Calendar.MONTH) + 1;
        int day2 = c2.get(Calendar.DAY_OF_MONTH);
        int hour2 = c2.get(Calendar.HOUR_OF_DAY);
        int minute2 = c2.get(Calendar.MINUTE);
        
        if (year2 > year1 && month2 > month1 && day2 > day1 && hour2 > hour1 && minute2 > minute1) {
			return true;
		}
        return false;*/
		
		Date hdt1 = formatDate1(historyDate);
		Date ldt2 = formatDate1(lastestDate);
		if (null != hdt1 && null != ldt2 && hdt1.getTime() < ldt2.getTime()) {
			return true;
		}
		return false;
	}
	
}
