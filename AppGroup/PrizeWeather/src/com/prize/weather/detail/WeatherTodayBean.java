package com.prize.weather.detail;

/**
 * 
 * @author wangzhong
 *
 */
public class WeatherTodayBean {

	String sunrise;
	String sunset;
	WeatherTodaySuggestionBean suggestion;
	
	public String getSunrise() {
		return sunrise;
	}
	public void setSunrise(String sunrise) {
		this.sunrise = sunrise;
	}
	public String getSunset() {
		return sunset;
	}
	public void setSunset(String sunset) {
		this.sunset = sunset;
	}
	public WeatherTodaySuggestionBean getSuggestion() {
		return suggestion;
	}
	public void setSuggestion(WeatherTodaySuggestionBean suggestion) {
		this.suggestion = suggestion;
	}
	
}
