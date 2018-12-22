package com.prize.weather.widget;

import android.content.Context;

import com.prize.weather.detail.WeatherDetailBean;
import com.prize.weather.detail.WeatherDetailModel;
import com.prize.weather.framework.http.IJsonParseExceptionHandler;
import com.prize.weather.framework.http.INetworkExcetpionHandler;
import com.prize.weather.framework.model.WeatherDBCache;

/*public class WeatherWidgetModel extends BaseModel {

	public WeatherWidgetModel(
			INetworkExcetpionHandler mINetworkExcetpionHandler,
			IJsonParseExceptionHandler mIJsonParseExceptionHandler) {
		super(mINetworkExcetpionHandler, mIJsonParseExceptionHandler);
	}
	
	public WeatherDBCache getCacheWithSQLite(Context context, String keyWord) throws Exception {
		WeatherCacheDataBaseDao weatherCacheDataBaseDao = new WeatherCacheDataBaseDao(context);
		List<WeatherDBCache> list = weatherCacheDataBaseDao.queryByLike(keyWord);
		if (null != list && list.size() > 0) {
			WeatherDBCache wdbc = list.get(list.size() - 1);
			return wdbc;
		} else {
			return null;
		}
	}
	
	public WeatherDetailBean getCacheWithSQLite(Context context, String keyWord) throws Exception{
		WeatherCacheDataBaseDao weatherCacheDataBaseDao = new WeatherCacheDataBaseDao(context);
		List<WeatherDBCache> list = weatherCacheDataBaseDao.queryByLike(keyWord);
		if (null != list && list.size() > 0) {
			WeatherDBCache wdbc = list.get(list.size() - 1);
			return parseData(wdbc.getWeather_data());
		} else {
			return null;
		}
	}
	
	public WeatherDetailBean obtainNowWeatherDataByCityID(int id) throws Exception {
		mHttpConnection.resetConnection();
		mHttpConnection.setFullURL("http://dt.koobeemobile.com/weathernow.php?id=" + id);
		String result = mHttpConnection.doGet();
		return parseData(result);
	}

	public WeatherDetailBean obtainNowWeatherDataByCityName(String name) throws Exception {
		mHttpConnection.resetConnection();
		mHttpConnection.setFullURL("http://dt.koobeemobile.com/weathernow_name.php?name=" + name);
		String result = mHttpConnection.doGet();
		return parseData(result);
	}
	
	private WeatherDetailBean parseData(String result) throws Exception  {
		return FastJsonUtils.getSingleBean(result, WeatherDetailBean.class);
	}
	
}
*/

/**
 * 
 * @author wangzhong
 *
 */
public class WeatherWidgetModel extends WeatherDetailModel {

	public WeatherWidgetModel(
			INetworkExcetpionHandler mINetworkExcetpionHandler,
			IJsonParseExceptionHandler mIJsonParseExceptionHandler) {
		super(mINetworkExcetpionHandler, mIJsonParseExceptionHandler);
	}
	
	public WeatherDetailBean getCacheWithSQLite(Context context, String keyWord) throws Exception {
		return getCacheWithSQLite(context, keyWord, false);
	}
	
	@Override
	public boolean saveDataToSQLite(Context context, WeatherDBCache weatherDBCache) {
		weatherDBCache.setIs_complete(false);
		return super.saveDataToSQLite(context, weatherDBCache);
	}
	
	public WeatherDetailBean obtainNowWeatherDataByCityID(int id) throws Exception {
		return obtainWeatherDataByCityID(id, false);
	}
	
	public WeatherDetailBean obtainNowWeatherDataByCityName(String name) throws Exception {
		return obtainWeatherDataByCityName(name, false);
	}
	
}
