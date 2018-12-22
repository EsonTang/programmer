package com.prize.weather.framework.model;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

/**
 * 
 * @author wangzhong
 * The name of the table.
 */
@DatabaseTable(tableName = "weather_cache")
public class WeatherDBCache {

	/**
	 * Auto increment.
	 */
	@DatabaseField(generatedId = true)
	private int id;
	
	/**
	 * The cache creation time.
	 */
	@DatabaseField(columnName = "created_date")
	private String created_date;
	
	/**
	 * The city code.
	 */
	@DatabaseField(columnName = "city_code")
	private String city_code;
	
	/**
	 * The city name.
	 */
	@DatabaseField(columnName = "city_name")
	private String city_name;
	
	/**
	 * 
	 * The weather code.
	 */
	@DatabaseField(columnName = "weather_code")
	private String weather_code;
	
	/**
	 * 
	 * The weather text.
	 */
	@DatabaseField(columnName = "weather_text")
	private String weather_text;
	
	/**
	 * The temperature.
	 */
	@DatabaseField(columnName = "weather_temperature")
	private String weather_temperature;
	
	/**
	 * Cache data.
	 */
	@DatabaseField(columnName = "weather_data")
	private String weather_data;
	
	/**
	 * 
	 */
	@DatabaseField(columnName = "is_complete")
	private boolean is_complete;
	
	@DatabaseField(columnName = "high_temperature")
	private String high_temperature;
	
	@DatabaseField(columnName = "low_temperature")
	private String low_temperature;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getCreated_date() {
		return created_date;
	}

	public void setCreated_date(String created_date) {
		this.created_date = created_date;
	}

	public String getCity_code() {
		return city_code;
	}

	public void setCity_code(String city_code) {
		this.city_code = city_code;
	}

	public String getCity_name() {
		return city_name;
	}

	public void setCity_name(String city_name) {
		this.city_name = city_name;
	}

	public String getWeather_code() {
		return weather_code;
	}

	public void setWeather_code(String weather_code) {
		this.weather_code = weather_code;
	}

	public String getWeather_text() {
		return weather_text;
	}

	public void setWeather_text(String weather_text) {
		this.weather_text = weather_text;
	}

	public String getWeather_temperature() {
		return weather_temperature;
	}

	public void setWeather_temperature(String weather_temperature) {
		this.weather_temperature = weather_temperature;
	}

	public String getWeather_data() {
		return weather_data;
	}

	public void setWeather_data(String weather_data) {
		this.weather_data = weather_data;
	}

	public boolean isIs_complete() {
		return is_complete;
	}

	public void setIs_complete(boolean is_complete) {
		this.is_complete = is_complete;
	}

	public String getHigh_temperature() {
		return high_temperature;
	}

	public void setHigh_temperature(String high_temperature) {
		this.high_temperature = high_temperature;
	}

	public String getLow_temperature() {
		return low_temperature;
	}

	public void setLow_temperature(String low_temperature) {
		this.low_temperature = low_temperature;
	}
	
}
