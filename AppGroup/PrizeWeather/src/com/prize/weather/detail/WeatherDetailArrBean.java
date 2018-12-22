package com.prize.weather.detail;

import java.util.List;

/**
 * 
 * @author wangzhong
 *
 */
public class WeatherDetailArrBean {

	String city_name;
	String city_id;
	String last_update;
	WeatherNowBean now;
	WeatherTodayBean today;
	List<WeatherHourlyBean> hourly;
	List<WeatherFutureBean> future;
	
	public String getCity_name() {
		return city_name;
	}
	public void setCity_name(String city_name) {
		this.city_name = city_name;
	}
	public String getCity_id() {
		return city_id;
	}
	public void setCity_id(String city_id) {
		this.city_id = city_id;
	}
	public String getLast_update() {
		return last_update;
	}
	public void setLast_update(String last_update) {
		this.last_update = last_update;
	}
	public WeatherNowBean getNow() {
		return now;
	}
	public void setNow(WeatherNowBean now) {
		this.now = now;
	}
	public WeatherTodayBean getToday() {
		return today;
	}
	public void setToday(WeatherTodayBean today) {
		this.today = today;
	}
	public List<WeatherHourlyBean> getHourly() {
		return hourly;
	}
	public void setHourly(List<WeatherHourlyBean> hourly) {
		this.hourly = hourly;
	}
	public List<WeatherFutureBean> getFuture() {
		return future;
	}
	public void setFuture(List<WeatherFutureBean> future) {
		this.future = future;
	}
	
}
