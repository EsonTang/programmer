package com.prize.weather.framework.db;

import java.util.List;

import com.prize.weather.framework.model.WeatherDBCache;

/**
 * 
 * @author wangzhong
 *
 */
public interface IWeatherCacheDataBaseDao {
	
	public int create(WeatherDBCache weatherDBCache);
	
	public List<WeatherDBCache> queryAll();
	
	public List<WeatherDBCache> queryByLike(String keyWord);

	public WeatherDBCache queryByID(int id);
	
	public int deleteAll();
	
	public int deleteByID(int id);
	
	public int update(WeatherDBCache weatherDBCache);
	
}
