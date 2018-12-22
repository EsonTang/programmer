package com.prize.weather.framework;

/**
 * SharedPreferences
 * @author wangzhong
 */
public interface ISPCallBack {
	
	//public String SHARED_PREFERENCES_FILE_NAME = "prize_weather";
	public String SHARED_PREFERENCES_FILE_NAME = "city";
	
	public String SP_CITY_NUM = "cityNum";
	
	public String SP_CITY_CODE = "city";
	public String SP_CITY_NAME = "cityName";
	public String SP_CITY_FLAG = "cityFlag";

    /*PRIZE-Add-PrizeWeather-wangzhong-2016_8_10-start*/
    public String SP_CITY_WEATHER_CODE = "cityWeatherCode";
    /*PRIZE-Add-PrizeWeather-wangzhong-2016_8_10-end*/

	public String SP_REFRESH_TIME = "refresh_time_";
	
	/**
	 * Initializes the SharedPreferences.<br>
	 * <b>(Note: According to the need to implement the callback)</b>
	 */
	public void initSharedPreferences();
	
}
