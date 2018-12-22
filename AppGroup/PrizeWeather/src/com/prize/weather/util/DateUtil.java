package com.prize.weather.util;

import android.content.Context;

import com.prize.weather.R;

public class DateUtil {

/*	public static int setHourPic(int num) {
    	if (num == 0){
    		return R.drawable.time_hour_0;
    	} else if (num == 1){
    		return R.drawable.time_hour_1;
    	}else if (num == 2){
    		return R.drawable.time_hour_2;
    	}else if (num == 3){
    		return R.drawable.time_hour_3;
    	}else if (num == 4){
    		return R.drawable.time_hour_4;
    	}else if (num == 5){
    		return R.drawable.time_hour_5;
    	}else if (num == 6){
    		return R.drawable.time_hour_6;
    	}else if (num == 7){
    		return R.drawable.time_hour_7;
    	}else if (num == 8){
    		return R.drawable.time_hour_8;
    	}else if (num == 9){
    		return R.drawable.time_hour_9;
    	} else {
    		return -1;
    	}
    }*/
	
/*	public static int setWidget2HourPic(int num) {
    	if (num == 0){
    		return R.drawable.time_2_0;
    	} else if (num == 1){
    		return R.drawable.time_2_1;
    	}else if (num == 2){
    		return R.drawable.time_2_2;
    	}else if (num == 3){
    		return R.drawable.time_2_3;
    	}else if (num == 4){
    		return R.drawable.time_2_4;
    	}else if (num == 5){
    		return R.drawable.time_2_5;
    	}else if (num == 6){
    		return R.drawable.time_2_6;
    	}else if (num == 7){
    		return R.drawable.time_2_7;
    	}else if (num == 8){
    		return R.drawable.time_2_8;
    	}else if (num == 9){
    		return R.drawable.time_2_9;
    	} else {
    		return -1;
    	}
    }*/
	
/*	public static int setMinutePic (int num) {
    	if (num == 0){
    		return R.drawable.time_minute_0;
    	} else if (num == 1){
    		return R.drawable.time_minute_1;
    	}else if (num == 2){
    		return R.drawable.time_minute_2;
    	}else if (num == 3){
    		return R.drawable.time_minute_3;
    	}else if (num == 4){
    		return R.drawable.time_minute_4;
    	}else if (num == 5){
    		return R.drawable.time_minute_5;
    	}else if (num == 6){
    		return R.drawable.time_minute_6;
    	}else if (num == 7){
    		return R.drawable.time_minute_7;
    	}else if (num == 8){
    		return R.drawable.time_minute_8;
    	}else if (num == 9){
    		return R.drawable.time_minute_9;
    	} else {
    		return -1;
    	}
    }*/

	public static CharSequence setDate(Context context, int year,int month, int date) {
		/*String widgetDate = year+context.getResources().getString(R.string.widget_year)+
				(month + 1) + context.getResources().getString(R.string.widget_month)
				+ date + context.getResources().getString(R.string.widget_date);
		return widgetDate;*/
		
		String widgetDate;
		int month2 = month + 1;
		if (month2 < 10) {
			if (date < 10) {
				widgetDate = "0" + (month + 1) + context.getResources().getString(R.string.widget_month)
						+ "0" + date + context.getResources().getString(R.string.widget_date);
			} else {
				widgetDate = "0" + (month + 1) + context.getResources().getString(R.string.widget_month)
						+ date + context.getResources().getString(R.string.widget_date);				
			}
		} else {
			if (date < 10) {
				widgetDate = (month + 1) + context.getResources().getString(R.string.widget_month)
						+ "0" + date + context.getResources().getString(R.string.widget_date);	
			} else {
				widgetDate = (month + 1) + context.getResources().getString(R.string.widget_month)
						+ date + context.getResources().getString(R.string.widget_date);					
			}		
		}
		return widgetDate;
	}

	public static CharSequence setDay(Context context, int day) {
		String str;
		switch (day) {
		case 1:
			str = context.getResources().getString(R.string.day_1);
			break;
		case 2:
			str = context.getResources().getString(R.string.day_2);
			break;
		case 3:
			str = context.getResources().getString(R.string.day_3);
			break;
		case 4:
			str = context.getResources().getString(R.string.day_4);
			break;
		case 5:
			str = context.getResources().getString(R.string.day_5);
			break;
		case 6:
			str = context.getResources().getString(R.string.day_6);
			break;
		case 0:
			str = context.getResources().getString(R.string.day_7);
			break;
		default:
			str = null;
			break;
		}
		String widgetDay = context.getResources().getString(R.string.widget_day) + str;
		return widgetDay;
	}

}
