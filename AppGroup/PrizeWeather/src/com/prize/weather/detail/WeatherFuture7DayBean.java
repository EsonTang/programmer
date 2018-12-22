package com.prize.weather.detail;

import android.provider.BaseColumns;

/**
 * 
 * @author wangzhong
 *
 */
public class WeatherFuture7DayBean {
	
	public static final String CITY_STATE 		= "state";
	public static final String DATELINE 		= "dateline";
	public static final String DAY_WEATHER		= "dayweather";
	public static final String DAYWIND_POWER	= "windpower";
	public static final String DAY_TEMPERATURE	= "daytemp";
	public static final String NIGHT_TEMPERATURE= "nighttemp";
	
	public static final String[] forecastProjection = new String[] {
		BaseColumns._ID,
		CITY_STATE,
		DATELINE, 
		DAY_WEATHER,
		DAYWIND_POWER,
		DAY_TEMPERATURE,
		NIGHT_TEMPERATURE				
	};
	
	private String date;
	private String day;
	private String text;
	private String code;
	private String high;
	private String low;
	private String wind;

	public WeatherFuture7DayBean() {
		super();
	}

	public WeatherFuture7DayBean(String date, String day, String text,
			String code, String high, String low, String wind) {
		super();
		this.date = date;
		this.day = day;
		this.text = text;
		this.code = code;
		this.high = high;
		this.low = low;
		this.wind = wind;
	}

	public String getDate() {
		return date;
	}

	public void setDate(String date) {
		this.date = date;
	}

	public String getDay() {
		return day;
	}

	public void setDay(String day) {
		this.day = day;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getHigh() {
		return high;
	}

	public void setHigh(String high) {
		this.high = high;
	}

	public String getLow() {
		return low;
	}

	public void setLow(String low) {
		this.low = low;
	}

	public String getWind() {
		return wind;
	}

	public void setWind(String wind) {
		this.wind = wind;
	}

}

