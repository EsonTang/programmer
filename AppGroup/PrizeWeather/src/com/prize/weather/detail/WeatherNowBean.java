package com.prize.weather.detail;

import java.util.List;

/**
 * 
 * @author wangzhong
 *
 */
public class WeatherNowBean {

	String text;
	String code;
	String temperature;
	String wind_direction;
	String wind_speed;
	String humidity;
	WeatherNowAirQualityBean air_quality;
	List<WeatherNowAlarmsBean> alarms;
    /**20150824*/
	String now_high;
	String now_low;
	
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
	public String getTemperature() {
		return temperature;
	}
	public void setTemperature(String temperature) {
		this.temperature = temperature;
	}
	public String getWind_direction() {
		return wind_direction;
	}
	public void setWind_direction(String wind_direction) {
		this.wind_direction = wind_direction;
	}
	public String getWind_speed() {
		return wind_speed;
	}
	public void setWind_speed(String wind_speed) {
		this.wind_speed = wind_speed;
	}
	public String getHumidity() {
		return humidity;
	}
	public void setHumidity(String humidity) {
		this.humidity = humidity;
	}
	public WeatherNowAirQualityBean getAir_quality() {
		return air_quality;
	}
	public void setAir_quality(WeatherNowAirQualityBean air_quality) {
		this.air_quality = air_quality;
	}
	public List<WeatherNowAlarmsBean> getAlarms() {
		return alarms;
	}
	public void setAlarms(List<WeatherNowAlarmsBean> alarms) {
		this.alarms = alarms;
	}
	public String getNow_high() {
		return now_high;
	}
	public void setNow_high(String now_high) {
		this.now_high = now_high;
	}
	public String getNow_low() {
		return now_low;
	}
	public void setNow_low(String now_low) {
		this.now_low = now_low;
	}
}
