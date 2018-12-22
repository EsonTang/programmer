package com.prize.weather.detail;

import android.util.Log;

import com.alibaba.fastjson.JSONException;
import com.prize.weather.framework.model.WeatherDBCache;
import com.prize.weather.framework.mvp.presenter.BasePresenter;

/**
 * 
 * @author wangzhong
 *
 */
public class WeatherDetailPresenter extends BasePresenter<IWeatherDetailView, WeatherDetailModel, WeatherDetailBean> {

	private WeatherDBCache weatherDBCache = null;
	
	public WeatherDetailPresenter(IWeatherDetailView mView) {
		super(mView);
		mModel = new WeatherDetailModel(this, this);
	}

	public void obtainWeatherData() {
		Log.d("WEATHER", "ACTION  PRESENTER  obtainWeatherData()");
		if (!isHttpRequest()) {
			setHttpRequest(true);
		}
		super.exeuteMultiTasks();
	}
	
	public void obtainCacheData() {
		Log.d("WEATHER", "ACTION  PRESENTER  obtainCacheData()");
		if (isHttpRequest()) {
			setHttpRequest(false);
		}
		super.exeuteMultiTasks();
	}
	
	/**
	 * Save the cached data.
	 * @param weatherDBCache
	 */
	public void saveDataToSQLite(WeatherDBCache weatherDBCache) {
		Log.d("WEATHER", "ACTION  PRESENTER  saveDataToSQLite(WeatherDBCache weatherDBCache)");
		this.weatherDBCache = weatherDBCache;
		if (isHttpRequest()) {
			setHttpRequest(false);
		}
		super.exeuteMultiTasks();
		//mModel.saveDataToSQLite(mView.getContext(), weatherDBCache);
	}
	
	@Override
	public WeatherDetailBean doInBackground() {
		Log.d("WEATHER", "ACTION  BACKGROUND_TASK  doInBackground()");
		WeatherDetailBean result = null;
		try {
			if (mView.isSave()) { // Save the cached data.
				mModel.saveDataToSQLite(mView.getContext(), weatherDBCache);
				result = new WeatherDetailBean();
			} else {
				if (isHttpRequest()) {
					if (mView.isLocation()) {
						/*if (mView.isFirst()) {
							result = mModel.obtainWeatherDataByCityName(mView.getName());
						} else {
							result = mModel.obtainNowWeatherDataByCityName(mView.getName());
						}*/
						result = mModel.obtainWeatherDataByCityName(mView.getName(), mView.isFirst());
					} else {
						/*if (mView.isFirst()) {
							result = mModel.obtainWeatherDataByCityID(mView.getID());
						} else {
							result = mModel.obtainNowWeatherDataByCityID(mView.getID());
						}*/
						result = mModel.obtainWeatherDataByCityID(mView.getID(), mView.isFirst());
					}
				} else {
					result = mModel.getCacheWithSQLite(mView.getContext(), mView.getName(), mView.isFirst());
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
	public void updateView(WeatherDetailBean mBackgroundHandler) {
		Log.d("WEATHER", "ACTION  MAIN_TASK  updateView()");
		if (null != mBackgroundHandler) {
			mView.updateView(mBackgroundHandler);
		} else {
			mView.closeUpdateStatus();
		}
	}

}
