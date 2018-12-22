package com.prize.weather.detail;

import java.util.List;

/**
 * 
 * @author wangzhong
 *
 */
public class WeatherDetailBean {
	
	String status;
	List<WeatherDetailArrBean> weather;
	
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public List<WeatherDetailArrBean> getWeather() {
		return weather;
	}
	public void setWeather(List<WeatherDetailArrBean> weather) {
		this.weather = weather;
	}
	
}
