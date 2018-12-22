package com.prize.weather.detail;

import java.util.List;

import android.content.Context;
import android.util.Log;

import com.prize.weather.framework.db.WeatherCacheDataBaseDao;
import com.prize.weather.framework.http.IJsonParseExceptionHandler;
import com.prize.weather.framework.http.INetworkExcetpionHandler;
import com.prize.weather.framework.model.WeatherDBCache;
import com.prize.weather.framework.mvp.model.BaseModel;
import com.prize.weather.util.CalendarUtils;
import com.prize.weather.util.FastJsonUtils;

/**
 * 
 * @author wangzhong
 *
 */
public class WeatherDetailModel extends BaseModel {

	public WeatherDetailModel(
			INetworkExcetpionHandler mINetworkExcetpionHandler,
			IJsonParseExceptionHandler mIJsonParseExceptionHandler) {
		super(mINetworkExcetpionHandler, mIJsonParseExceptionHandler);
	}
	
	private WeatherDetailBean parseData(String result) throws Exception  {
		Log.d("WEATHER", "ACTION  MODEL  parseData(String result)  result = " + result);
		return FastJsonUtils.getSingleBean(result, WeatherDetailBean.class);
	}

	public WeatherDetailBean getCacheWithSQLite(Context context, String keyWord, boolean isFirst) throws Exception {
		Log.d("WEATHER", "ACTION  MODEL  getCacheWithSQLite(Context context, String keyWord, boolean isFirst)  keyWord = " + keyWord + ", isFirst = " + isFirst);
		WeatherCacheDataBaseDao weatherCacheDataBaseDao = new WeatherCacheDataBaseDao(context);
		List<WeatherDBCache> list = weatherCacheDataBaseDao.queryByLike(keyWord);
		if (null != list && list.size() > 0) {
			WeatherDBCache wdbc = null;
			for (int i = list.size(); i > 0; i--) {
				wdbc = list.get(i - 1);
				if (isFirst) {
					if (wdbc.isIs_complete()) {
						break;
					}
				} else {
					if (!wdbc.isIs_complete()) {
						break;
					}
				}
			}
			return parseData(wdbc.getWeather_data());
		} else {
			return null;
		}
	}
	
	public boolean saveDataToSQLite(Context context, WeatherDBCache weatherDBCache) {
		Log.d("WEATHER", "ACTION  MODEL  saveDataToSQLite(Context context, WeatherDBCache weatherDBCache)  getCity_name() = " + weatherDBCache.getCity_name());
		WeatherCacheDataBaseDao weatherCacheDataBaseDao = new WeatherCacheDataBaseDao(context);
		
		// Query the database whether all existing cities on the day of the current data and real-time data.
		boolean isAll = weatherDBCache.isIs_complete();
		int cacheID = -1;
		int flag = 0;
		List<WeatherDBCache> list = weatherCacheDataBaseDao.queryByLike(weatherDBCache.getCity_name());
		if (null != list && list.size() > 0) {
			for (int i = list.size(); i > 0; i--) {
				WeatherDBCache wdbc = list.get(i - 1);
				String currentDate = weatherDBCache.getCreated_date();
				String cacheDate = wdbc.getCreated_date();
				if (null != currentDate && null != cacheDate && CalendarUtils.isSameDay(currentDate, cacheDate)) { // The current day.
					if (isAll) {
						if (wdbc.isIs_complete()) {
							cacheID = wdbc.getId();
							break;
						}
					} else {
						if (!wdbc.isIs_complete()) {
							cacheID = wdbc.getId();
							break;
						}
					}
				}
			}
		}
		
		
		if (cacheID == -1) {
			flag = weatherCacheDataBaseDao.create(weatherDBCache);
		} else {
			weatherDBCache.setId(cacheID);
			flag = weatherCacheDataBaseDao.update(weatherDBCache);
		}
		return flag == 0 ? false : true;
	}
	
	public WeatherDetailBean obtainWeatherDataByCityID(int id, boolean isFirst) throws Exception {
		Log.d("WEATHER", "ACTION  MODEL  obtainWeatherDataByCityID(int id, boolean isFirst)  id = " + id + ", isFirst = " + isFirst);
		mHttpConnection.resetConnection();
		if (isFirst) {
			mHttpConnection.setFullURL("http://weather.szprize.cn/weather.php?id=" + id);
		} else {
			mHttpConnection.setFullURL("http://weather.szprize.cn/weathernow.php?id=" + id);
		}
		String result = mHttpConnection.doGet();
		return parseData(result);
	}
	
	public WeatherDetailBean obtainWeatherDataByCityName(String name, boolean isFirst) throws Exception {
		Log.d("WEATHER", "ACTION  MODEL  obtainWeatherDataByCityName(String name, boolean isFirst)  name = " + name + ", isFirst = " + isFirst);
		mHttpConnection.resetConnection();
		if (isFirst) {
			mHttpConnection.setFullURL("http://weather.szprize.cn/weather_name.php?name=" + name);
		} else {
			mHttpConnection.setFullURL("http://weather.szprize.cn/weathernow_name.php?name=" + name);
		}
		String result = mHttpConnection.doGet();
		return parseData(result);
	}
	
}
