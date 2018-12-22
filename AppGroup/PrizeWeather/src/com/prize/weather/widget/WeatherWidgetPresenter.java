package com.prize.weather.widget;

import com.alibaba.fastjson.JSONException;
import com.prize.weather.detail.WeatherDetailArrBean;
import com.prize.weather.detail.WeatherDetailBean;
import com.prize.weather.detail.WeatherNowBean;
import com.prize.weather.detail.WeatherTodayBean;
import com.prize.weather.framework.model.WeatherDBCache;
import com.prize.weather.framework.mvp.presenter.BasePresenter;
import com.prize.weather.util.FastJsonUtils;

/**
 * 
 * @author wangzhong
 *
 */
public class WeatherWidgetPresenter extends BasePresenter<IWeatherWidgetView, WeatherWidgetModel, WeatherDBCache> {

	private WeatherDBCache weatherDBCache = null;
	
	public WeatherWidgetPresenter(IWeatherWidgetView mView) {
		super(mView);
		mModel = new WeatherWidgetModel(this, this);
	}

	public void obtainWeatherData() {
		if (!isHttpRequest()) {
			setHttpRequest(true);
		}
		super.exeuteMultiTasks();
	}
	
	public void obtainCacheData() {
		if (isHttpRequest()) {
			setHttpRequest(false);
		}
		super.exeuteMultiTasks();
	}
	
	public void saveDataToSQLite(WeatherDBCache weatherDBCache) {
		this.weatherDBCache = weatherDBCache;
		if (isHttpRequest()) {
			setHttpRequest(false);
		}
		super.exeuteMultiTasks();
	}
	
	@Override
	public WeatherDBCache doInBackground() {
		WeatherDBCache result = null;
		WeatherDetailBean wdb = null;
		try {
			if (mView.isSave()) {
				mModel.saveDataToSQLite(mView.getContext(), weatherDBCache);
			} else {
				if (isHttpRequest()) {
					if (mView.getID() < 1000) {
						wdb = mModel.obtainNowWeatherDataByCityName(mView.getName());					
					} else {
						wdb = mModel.obtainNowWeatherDataByCityID(mView.getID());
					}
				} else {
					wdb = mModel.getCacheWithSQLite(mView.getContext(), mView.getName());
				}
				if (null != wdb && null != wdb.getStatus() && wdb.getStatus().equals("OK")) {
					WeatherDetailArrBean weatherDetailArrBean = wdb.getWeather().get(0);
					WeatherNowBean weatherNowBean = weatherDetailArrBean.getNow();
					WeatherTodayBean weatherTodayBean = weatherDetailArrBean.getToday();
					
					result = new WeatherDBCache();
					result.setCreated_date(weatherDetailArrBean.getLast_update());
					result.setCity_code(weatherDetailArrBean.getCity_id());
					result.setCity_name(weatherDetailArrBean.getCity_name());
					result.setWeather_code(weatherNowBean.getCode());
					result.setWeather_text(weatherNowBean.getText());
					result.setWeather_temperature(weatherNowBean.getTemperature());
					result.setWeather_data(FastJsonUtils.convertDataToJsonString(wdb));
					result.setIs_complete(weatherTodayBean == null ? false : true);
					result.setHigh_temperature(weatherNowBean.getNow_high());
					result.setLow_temperature(weatherNowBean.getNow_low());
				}
			}
		} catch (Exception e) {
			if (e instanceof JSONException) {
				handleParseException((JSONException) e);
			} else {
				handleNetworkException(e, "Network anomalies");
			}
		}
		return result;
	}
	
	@Override
	public void updateView(WeatherDBCache mBackgroundHandler) {
		if (null != mBackgroundHandler) {
			mView.updateView(mBackgroundHandler);
		}
	}
	
}
