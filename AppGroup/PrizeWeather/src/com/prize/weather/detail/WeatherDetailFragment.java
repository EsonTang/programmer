package com.prize.weather.detail;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.RenderScript;
import android.renderscript.ScriptIntrinsicBlur;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v4.widget.SwipeRefreshLayout.OnRefreshListener;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.prize.weather.R;
import com.prize.weather.WeatherHomeActivity;
import com.prize.weather.framework.BaseFragment;
import com.prize.weather.framework.FrameApplication;
import com.prize.weather.framework.ISPCallBack;
import com.prize.weather.framework.model.WeatherDBCache;
import com.prize.weather.util.CalendarUtils;
import com.prize.weather.util.FastJsonUtils;
import com.prize.weather.util.IcallBack;   //move
import com.prize.weather.util.MathUtils;
import com.prize.weather.util.NetworkUtils;
import com.prize.weather.util.WeatherImageUtils;
import com.prize.weather.view.BlurScrollView;
import com.prize.weather.view.BlurScrollView.OnScrollListener;
import com.prize.weather.view.DialChartView;
import com.prize.weather.view.HourWeatherLayout;

/**
 * 
 * @author wangzhong
 *
 */
@SuppressLint("InlinedApi")
public class WeatherDetailFragment extends BaseFragment<WeatherDetailPresenter> 
		implements IWeatherDetailView, OnRefreshListener {
	
	private final static String TAG = "WEATHER--DETAIL--FRAGMENT";
	
	//private final static String TEMPORARILY_NO_DATA = "暂无数据";
	private final static String TEMPORARILY_NO_DATA = "";
	
	private SwipeRefreshLayout refreshableView;
	private BlurScrollView blurScrollView;
	private RelativeLayout rl_nodata;
	private LinearLayout ll_havedata;
	
	// main
	private LinearLayout wfm_ll_main;	
	private TextView wfm_iv_txt;  //20150924
	private ImageView wfm_iv_ten;
	private ImageView wfm_iv_one;
	private TextView wfm_tv_up;
	private TextView wfm_tv_down;
	private ImageView wfm_iv_weather_icon;
	private TextView wfm_tv_weather_text;
	private TextView wfm_tv_wind_direction;
	
	// air
	private TextView wfm_tv_air_quality_text;
	private TextView wfm_tv_air_quality_quality;
	private DialChartView pm_dcv;
	private TextView pm_tips;
	
	
	private HourWeatherLayout mHourWeatherLayout;
	private List<Map<String, Object>> listHourWeather = new ArrayList<Map<String, Object>>();
	private HourWeatherAdapter mHourWeatherAdapter;
	/*private ListView mLVWeatherFuture;
	private List<WeatherFuture7DayBean> listFutureWeather = new ArrayList<WeatherFuture7DayBean>();
	private WeatherFutureListItemAdapter mWeatherFutureListItemAdapter;*/
	private LinearLayout weather_future_list;
	
	
	// para
	private TextView wfp_humidity;
	private TextView wfp_ultraviolet;
	private TextView wfp_wind;
	private TextView wfp_sunrise;
	private TextView wfp_sunset;
	
	// Index of living.
	private TextView wfi_dressing;
	private TextView wfi_travel;
	private TextView wfi_car_washing;
	private TextView wfi_flu;
	private TextView wfi_shopping;
	private TextView wfi_traffic;
	private TextView wfi_morning;
	private TextView wfi_air;
	
	private int index;
	private int weatherCode = -1;
	private boolean isFirst = true;
	private boolean isSave	= true;
	
	/*PRIZE-Add-PrizeWeather-wangzhong-2016_8_10-start*/
	/*private boolean isJustCreatedView = true;
	private int mCountGetCacheDataJust = 0;*/
	/*PRIZE-Add-PrizeWeather-wangzhong-2016_8_10-end*/
	
	public int getIndex() {
		return index;
	}

	public void setIndex(int index) {
		this.index = index;
	}

	public WeatherDetailFragment() {
		super();
	}
	
	public WeatherDetailFragment(int index) {
		super();
		setIndex(index);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		if (null == mView) {
			mView = mActivity.getLayoutInflater().inflate(R.layout.weather_fragment, container, false);
			
			initRefreshWeather();
			blurScrollView = (BlurScrollView) mView.findViewById(R.id.blur_scroll);
			rl_nodata = (RelativeLayout) mView.findViewById(R.id.rl_nodata);
			ll_havedata = (LinearLayout) mView.findViewById(R.id.ll_havedata);
			
			//initVideoView();
			initBlurView();
			
			wfm_ll_main = (LinearLayout) mView.findViewById(R.id.wfm_ll_main);
			wfm_iv_txt = (TextView) mView.findViewById(R.id.wfm_iv_txt);  //20150924
			wfm_iv_ten = (ImageView) mView.findViewById(R.id.wfm_iv_ten);
			wfm_iv_one = (ImageView) mView.findViewById(R.id.wfm_iv_one);
			wfm_tv_up = (TextView) mView.findViewById(R.id.wfm_tv_up);
			wfm_tv_down = (TextView) mView.findViewById(R.id.wfm_tv_down);
			wfm_tv_weather_text = (TextView) mView.findViewById(R.id.wfm_tv_weather_text);
			wfm_iv_weather_icon = (ImageView) mView.findViewById(R.id.wfm_iv_weather_icon);
			wfm_tv_wind_direction = (TextView) mView.findViewById(R.id.wfm_tv_wind_direction);
			
			
			wfm_tv_air_quality_text = (TextView) mView.findViewById(R.id.wfm_tv_air_quality_text);
			wfm_tv_air_quality_quality = (TextView) mView.findViewById(R.id.wfm_tv_air_quality_quality);
			pm_dcv = (DialChartView) mView.findViewById(R.id.pm_dcv);
			pm_tips = (TextView) mView.findViewById(R.id.pm_tips);
			
			
			mHourWeatherLayout = (HourWeatherLayout)mView.findViewById(R.id.hourweatherLayout);
			//mLVWeatherFuture = (ListView) mView.findViewById(R.id.weather_list);
			weather_future_list = (LinearLayout) mView.findViewById(R.id.weather_future_list);
			
			
			wfp_humidity = (TextView) mView.findViewById(R.id.wfp_humidity);
			wfp_ultraviolet = (TextView) mView.findViewById(R.id.wfp_ultraviolet);
			wfp_wind = (TextView) mView.findViewById(R.id.wfp_wind);
			wfp_sunrise = (TextView) mView.findViewById(R.id.wfp_sunrise);
			wfp_sunset = (TextView) mView.findViewById(R.id.wfp_sunset);
			
			
			wfi_dressing = (TextView) mView.findViewById(R.id.wfi_dressing);
			wfi_travel = (TextView) mView.findViewById(R.id.wfi_travel);
			wfi_car_washing = (TextView) mView.findViewById(R.id.wfi_car_washing);
			wfi_flu = (TextView) mView.findViewById(R.id.wfi_flu);
			wfi_shopping = (TextView) mView.findViewById(R.id.wfi_shopping);
			wfi_traffic = (TextView) mView.findViewById(R.id.wfi_traffic);
			wfi_morning = (TextView) mView.findViewById(R.id.wfi_morning);
			wfi_air = (TextView) mView.findViewById(R.id.wfi_air);
			
			/*initHourWeather(0);
			initWeatherList();*/
			
			isFirst = true;
			isSave	= true;
			
			/*PRIZE-Add-PrizeWeather-wangzhong-2016_8_10-start*/
			/*isJustCreatedView = true;
			mCountGetCacheDataJust = 0;*/
			/*PRIZE-Add-PrizeWeather-wangzhong-2016_8_10-end*/
		}
		return mView;
	}
	
	private void initRefreshWeather(){
		refreshableView = (SwipeRefreshLayout) mView.findViewById(R.id.parent_layout);
		refreshableView.setOnRefreshListener(this);
		refreshableView.setColorSchemeResources(
				android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_red_light,
                android.R.color.holo_orange_light);
	}
	
	private void initPresenter() {
		if (null == mPresenter) {
			mPresenter = new WeatherDetailPresenter(this);
		}
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		/*PRIZE-Delete-PrizeWeather-wangzhong-2016_8_10-start*/
		/*onRefresh();*/
		/*PRIZE-Delete-PrizeWeather-wangzhong-2016_8_10-end*/
	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();
		isFirst = true;
		isSave	= true;

		/*PRIZE-Add-PrizeWeather-wangzhong-2016_8_10-start*/
		/*isJustCreatedView = true;
		mCountGetCacheDataJust = 0;*/

		if (null != mPresenter) mPresenter.onCancel();
		/*PRIZE-Add-PrizeWeather-wangzhong-2016_8_10-end*/

		if (null != mView) {
			ViewGroup vg = (ViewGroup) mView.getParent();
			if (null != vg) {
				vg.removeView(mView);
			}
		}
		System.gc();
	}

	@Override
	public void openUpdateStatus() {
		
	}

	@Override
	public void closeUpdateStatus() {
		/*PRIZE-Add-PrizeWeather-wangzhong-2016_8_10-start*/
		/*if (isJustCreatedView) {
			Log.d(TAG, "No local cache, so refresh the internet data!    isJustCreatedView : " + isJustCreatedView);
			isFirst = true;
			isJustCreatedView = false;
			onRefresh();
		}*/
		/*PRIZE-Add-PrizeWeather-wangzhong-2016_8_10-end*/

	    if (null != refreshableView) {
			refreshableView.setRefreshing(false);
		}
	}
	
	@Override
	public void showNodataView(boolean isNodata) {
		Log.d(TAG, "showNodataView(boolean isNodata) isNodata : " + isNodata);
		if (null != ll_havedata) {
			ll_havedata.setVisibility(isNodata ? View.GONE : View.VISIBLE);
			wfm_ll_main.setMinimumHeight(((WeatherHomeActivity) mActivity).getViewpagerHeight());
		}
		if (null != rl_nodata) {
			rl_nodata.setVisibility(isNodata ? View.VISIBLE : View.GONE);
			rl_nodata.setMinimumHeight(((WeatherHomeActivity) mActivity).getViewpagerHeight() * 4 / 5);
		}
	}
	
	@Override
	public boolean isLocation() {
		if (index == 0) {
			return true;
		}
		return false;
	}

	@Override
	public int getID() {
		return FrameApplication.getInstance().getSharedPreferences().getInt(ISPCallBack.SP_CITY_CODE + index, 0);
	}

	@Override
	public boolean isFirst() {
		/*String date = getRefreshTime();
		if (!date.equals(TEMPORARILY_NO_DATA) && CalendarUtils.isToday(CalendarUtils.formatDate(date))) {
			return false;
		} else {
			return true;
		}*/
		return isFirst;
	}

	@Override
	public boolean isSave() {
		return isSave;
	}
	
	@Override
	public String getName() {
		return FrameApplication.getInstance().getSharedPreferences().getString(ISPCallBack.SP_CITY_NAME + index, "");
	}
	
	public String getTitle() {
		//return FrameApplication.getInstance().getSharedPreferences().getString(ISPCallBack.SP_CITY_NAME + index, "");
		return getName();
	}
	
	public String getRefreshTime() {
		return FrameApplication.getInstance().getSharedPreferences().getString(ISPCallBack.SP_REFRESH_TIME + index, TEMPORARILY_NO_DATA);
	}
	
	public int getWeatherCode() {
		Log.d(TAG, "getWeatherCode() : weatherCode : " + weatherCode);
		return weatherCode;
	}
	
	public void setScrollViewScrollTo(int y) {
		Log.d(TAG, "Scroll  setScrollViewScrollTo : y : " + y + "   blurScrollView : " + blurScrollView);
		if (null != blurScrollView) {
			//blurScrollView.smoothScrollTo(0, y);
			blurScrollView.scrollTo(0, y);
		}
	}

/*	private void initHourWeather(int currentItem) {
		listHourWeather.clear();
		Time time = new Time();
		time.setToNow();
		int hour = time.hour;
		for (int i = 0; i < 24; i++) {
			Map<String, Object> map = new HashMap<String, Object>();
			if (hour > 23) hour = 0;
			map.put("hourTime", hour + ":00");
			hour++;
			map.put("hourIcon", "00");
			map.put("hourTemp", "20");
			listHourWeather.add(map);
		}
		mHourWeatherAdapter = new HourWeatherAdapter(mView.getContext(), listHourWeather);
		mHourWeatherLayout.setAdapter(mHourWeatherAdapter);
	}

	private void initWeatherList() {
		listFutureWeather.clear();
		for (int i = 0; i < 7; i++) {
			WeatherFuture7DayBean item = new WeatherFuture7DayBean();
			item.setDate("0/0");
			item.setDay("星期一");
			item.setText("晴");
			item.setCode("4");
			item.setHigh(32 + "");
			item.setLow(25 + "");
			item.setWind("微风");
			listFutureWeather.add(item);
		}
		mWeatherFutureListItemAdapter = new WeatherFutureListItemAdapter(mActivity, listFutureWeather);
		mLVWeatherFuture.setAdapter(mWeatherFutureListItemAdapter);
		((WeatherHomeActivity) mActivity).setHeight(mLVWeatherFuture, listFutureWeather.size());
	}*/

	@SuppressLint("InflateParams")
	@Override
	public void updateView(Object o) {
		WeatherDetailBean weatherDetailBean = (WeatherDetailBean) o;
		if (null != weatherDetailBean && null != weatherDetailBean.getStatus() && weatherDetailBean.getStatus().equals("OK")) {
			WeatherDetailArrBean weatherDetailArrBean = weatherDetailBean.getWeather().get(0);
			WeatherNowBean weatherNowBean = weatherDetailArrBean.getNow();
			List<WeatherHourlyBean> list24 = weatherDetailArrBean.getHourly();
			List<WeatherFutureBean> list7 = weatherDetailArrBean.getFuture();
			WeatherTodayBean weatherTodayBean = weatherDetailArrBean.getToday();
			
			WeatherTodaySuggestionBean weatherTodaySuggestionBean = null;
			if (null != weatherTodayBean) {
				weatherTodaySuggestionBean = weatherTodayBean.getSuggestion();
			}
			
			WeatherNowAirQualityCityBean weatherNowAirQualityCityBean = weatherNowBean.getAir_quality().getCity();
//			List<WeatherNowAlarmsBean> listAlarms = weatherNowBean.getAlarms();
			
			
			// cache.
			WeatherDBCache weatherDBCache = new WeatherDBCache();
			weatherDBCache.setCreated_date(weatherDetailArrBean.getLast_update());
			weatherDBCache.setCity_code(weatherDetailArrBean.getCity_id());
			weatherDBCache.setCity_name(weatherDetailArrBean.getCity_name());
			weatherDBCache.setWeather_code(weatherNowBean.getCode());
			weatherDBCache.setWeather_text(weatherNowBean.getText());
			weatherDBCache.setWeather_temperature(weatherNowBean.getTemperature());
			weatherDBCache.setWeather_data(FastJsonUtils.convertDataToJsonString(weatherDetailBean));
			weatherDBCache.setIs_complete(weatherTodayBean == null ? false : true);
			Log.d("receive_hky","Now_high() = "+weatherNowBean.getNow_high() +" Now_low = "+weatherNowBean.getNow_low());
			weatherDBCache.setHigh_temperature(weatherNowBean.getNow_high());    //20151228
			weatherDBCache.setLow_temperature(weatherNowBean.getNow_low());   //20151228
			saveCacheData(weatherDBCache);
			
			if (null != weatherNowBean.getCode() && weatherNowBean.getCode().length() > 0 && MathUtils.isNumeric(weatherNowBean.getCode())) {
				weatherCode = Integer.valueOf(weatherNowBean.getCode());
//				updateVideoURI(weatherCode);
			}
			
			/*PRIZE-Change-PrizeWeather-wangzhong-2016_8_10-start*/
			// Air.
			if (null != weatherNowAirQualityCityBean && null != weatherNowAirQualityCityBean.getAqi() && !weatherNowAirQualityCityBean.getAqi().equals("")) {
				if (null != wfm_tv_air_quality_text) wfm_tv_air_quality_text.setText(weatherNowAirQualityCityBean.getAqi());
				if (null != wfm_tv_air_quality_quality) {
					wfm_tv_air_quality_quality.setText(weatherNowAirQualityCityBean.getQuality());
					wfm_tv_air_quality_quality.setVisibility(View.VISIBLE);
				}
				
				if (null != weatherNowAirQualityCityBean.getPm25() && MathUtils.isNumeric(weatherNowAirQualityCityBean.getPm25())) {
					pm_dcv.setCurrentStatus(Integer.valueOf(weatherNowAirQualityCityBean.getPm25()));
				} else {
					pm_dcv.setCurrentStatus(0);
				}
				if (null != pm_tips) pm_tips.setText("空气质量：" + weatherNowAirQualityCityBean.getQuality());
			} else {
				// default.
			    if (null != wfm_tv_air_quality_text) wfm_tv_air_quality_text.setText("此城市无空气指数数据");
			    if (null != wfm_tv_air_quality_quality) wfm_tv_air_quality_quality.setVisibility(View.GONE);
				
				pm_dcv.setCurrentStatus(0);
				if (null != pm_tips) pm_tips.setText("此城市无PM2.5数据");
			}
			pm_dcv.invalidate();
			
			// Alarms.
			
			
			// main.
//			setImageViewSRCWithTemperature(weatherNowBean.getTemperature());   //20150924
			setSRCWithTemperature(weatherNowBean.getTemperature());   //20150924
			
			if (null != wfm_tv_up) wfm_tv_up.setText(weatherNowBean.getNow_high());
			if (null != wfm_tv_down) wfm_tv_down.setText(weatherNowBean.getNow_low());
			
//			if (null != list7 && list7.size() > 0) {
//				wfm_tv_up.setText(list7.get(0).getHigh());
//				wfm_tv_down.setText(list7.get(0).getLow());
//				//wfm_tv_weather_text.setText(list7.get(1).getText());
//				//WeatherImageUtils.setWeatherImage(Integer.valueOf(list7.get(1).getCode()), wfm_iv_weather_icon, CalendarUtils.isDayTime());
//			}
			if (null != wfm_tv_weather_text) wfm_tv_weather_text.setText(weatherNowBean.getText());
			if (null != weatherNowBean.getCode() && weatherNowBean.getCode().length() > 0 && MathUtils.isNumeric(weatherNowBean.getCode())) {
				WeatherImageUtils.setWeatherImage(Integer.valueOf(weatherNowBean.getCode()), wfm_iv_weather_icon, CalendarUtils.isDayTime());
			}
			if (null != wfm_tv_wind_direction) wfm_tv_wind_direction.setText(weatherNowBean.getWind_direction());
			
			// 24h.
			if (null != list24 && list24.size() > 0) {
				mHourWeatherLayout.removeAllViews();
				listHourWeather.clear();
				for (int i = 0; i < list24.size(); i++) {
					Map<String, Object> map = new HashMap<String, Object>();
					map.put("hourTime", list24.get(i).getTime());
					map.put("hourIcon", list24.get(i).getCode());
					map.put("hourTemp", list24.get(i).getTemperature());
					listHourWeather.add(map);
				}
				if (null == mHourWeatherAdapter) {
					mHourWeatherAdapter = new HourWeatherAdapter(mView.getContext(), listHourWeather);
				}
				mHourWeatherLayout.setAdapter(mHourWeatherAdapter);
			}

			// 7day.
			if (null != list7 && list7.size() > 0) {
				Log.d(TAG, "In the next seven days the weather Settings! " + getName() + " list7.size() : " + list7.size());
				/*listFutureWeather.clear();
				for (int i = 2; i < 9; i++) {
					WeatherFuture7DayBean item = new WeatherFuture7DayBean();
					WeatherFutureBean weatherFutureBean = list7.get(i);
					item.setDate(weatherFutureBean.getDate());
					item.setDay(weatherFutureBean.getDay());
					item.setText(weatherFutureBean.getText());
					item.setCode(weatherFutureBean.getCode());
					item.setHigh(weatherFutureBean.getHigh());
					item.setLow(weatherFutureBean.getLow());
					item.setWind(weatherFutureBean.getWind());
					listFutureWeather.add(item);
				}
//				if (null == mWeatherFutureListItemAdapter) {
//					mWeatherFutureListItemAdapter = new WeatherFutureListItemAdapter(mActivity, listFutureWeather);
//					mLVWeatherFuture.setAdapter(mWeatherFutureListItemAdapter);
//				} else {
//					mWeatherFutureListItemAdapter.notifyDataSetChanged();
//				}
				mWeatherFutureListItemAdapter = new WeatherFutureListItemAdapter(mActivity, listFutureWeather);
				mLVWeatherFuture.setAdapter(mWeatherFutureListItemAdapter);
				((WeatherHomeActivity) mActivity).setHeight(mLVWeatherFuture, listFutureWeather.size());*/
				
				// Solve the problem of the Y axis position.
				int startFuture = 1;
				int endFuture = 8;
				if (null != weather_future_list) {
					weather_future_list.removeAllViews();
				}
				if(list7.size()<endFuture){
					endFuture = list7.size();
				}
				for (int i = startFuture; i < endFuture; i++) {
					WeatherFutureBean weatherFutureBean = list7.get(i);
					View futureView = LayoutInflater.from(getContext()).inflate(R.layout.weather_future_listitem, null);
					View futureDivider = LayoutInflater.from(getContext()).inflate(R.layout.weather_fragment_future_listitem_divider, null);
					TextView future_date = ((TextView) futureView.findViewById(R.id.future_date));
					TextView future_day = (TextView) futureView.findViewById(R.id.future_day);
					ImageView future_icon = (ImageView) futureView.findViewById(R.id.future_icon);
					TextView future_temp = (TextView) futureView.findViewById(R.id.future_temp);
					
					future_date.setText(weatherFutureBean.getDate());
					future_day.setText(weatherFutureBean.getDay());
					future_temp.setText(weatherFutureBean.getLow() + "~" + weatherFutureBean.getHigh() + "℃");
//					Log.d("hekeyi","weatherFutureBean.getCode() = "+weatherFutureBean.getCode());
					if(MathUtils.isNumeric(weatherFutureBean.getCode())){
						WeatherImageUtils.setWeatherImage(Integer.valueOf(weatherFutureBean.getCode()), future_icon, CalendarUtils.isDayTime(), 3);
					}
					
					
					weather_future_list.addView(futureView);
					if (i < endFuture - 1) {
						weather_future_list.addView(futureDivider);
					}
				}
			}
			
			// para.
			if (null != wfp_humidity) wfp_humidity.setText(weatherNowBean.getHumidity());
			if (null != wfp_wind) wfp_wind.setText(weatherNowBean.getWind_direction());
			if (null != weatherTodayBean) {
			    if (null != wfp_sunrise) wfp_sunrise.setText(weatherTodayBean.getSunrise());
			    if (null != wfp_sunset) wfp_sunset.setText(weatherTodayBean.getSunset());
			}
			
			// suggestion.
			if (null != weatherTodaySuggestionBean) {
				if(weatherTodaySuggestionBean.getUv()!=null){
				    if (null != wfp_ultraviolet) wfp_ultraviolet.setText(weatherTodaySuggestionBean.getUv().getBrief());
				}
				if(weatherTodaySuggestionBean.getDressing()!=null){
				    if (null != wfi_dressing) wfi_dressing.setText(weatherTodaySuggestionBean.getDressing().getBrief());
				}
				if(weatherTodaySuggestionBean.getTravel()!=null){
				    if (null != wfi_travel) wfi_travel.setText(weatherTodaySuggestionBean.getTravel().getBrief());
				}
				if(weatherTodaySuggestionBean.getCar_washing()!=null){
				    if (null != wfi_car_washing) wfi_car_washing.setText(weatherTodaySuggestionBean.getCar_washing().getBrief());
				}
				if(weatherTodaySuggestionBean.getFlu()!=null){
				    if (null != wfi_flu) wfi_flu.setText(weatherTodaySuggestionBean.getFlu().getBrief());
				}
				if(weatherTodaySuggestionBean.getShopping()!=null){
				    if (null != wfi_shopping) wfi_shopping.setText(weatherTodaySuggestionBean.getShopping().getBrief());
				}
				if(weatherTodaySuggestionBean.getTraffic()!=null){
				    if (null != wfi_traffic) wfi_traffic.setText(weatherTodaySuggestionBean.getTraffic().getBrief());
				}
				if(weatherTodaySuggestionBean.getMorning()!=null){
				    if (null != wfi_morning) wfi_morning.setText(weatherTodaySuggestionBean.getMorning().getBrief());
				}
				if(weatherTodaySuggestionBean.getAir()!=null){
				    if (null != wfi_air) wfi_air.setText(weatherTodaySuggestionBean.getAir().getBrief());
				}
			}
			/*PRIZE-Change-PrizeWeather-wangzhong-2016_8_10-end*/

			
			// sync data
			String time = CalendarUtils.getCurrentTime();
			//String time = weatherDetailArrBean.getLast_update();
			//String historyTime = FrameApplication.getInstance().getSharedPreferences().getString(ISPCallBack.SP_REFRESH_TIME + index, TEMPORARILY_NO_DATA);
			if (((WeatherHomeActivity) mActivity).getViewpagerCurrentItem() == index) {
				/*if (historyTime.equals(TEMPORARILY_NO_DATA) || CalendarUtils.isLatestDate(historyTime, time)) { // Start for the first time, when no data.
					((WeatherHomeActivity) mActivity).setRefreshTime(time);
					FrameApplication.getInstance().getSPEdit().putString(ISPCallBack.SP_REFRESH_TIME + index, time);
					FrameApplication.getInstance().getSPEdit().commit();
				}*/
				((WeatherHomeActivity) mActivity).setRefreshTime(time);
				((WeatherHomeActivity) mActivity).setWeatherBG(weatherCode);
				((WeatherHomeActivity) mActivity).showFullTopView();
				if (0 != index) {  //2015.8.31
					Log.d("title","setCurrentCityName  cityName = frag");
					((WeatherHomeActivity) mActivity).setCurrentCityName(weatherDetailArrBean.getCity_name());
				}
			}
			Log.d("WEATHERCODE", "weatherCode = " + weatherCode);
			FrameApplication.getInstance().getSPEdit().putString(ISPCallBack.SP_REFRESH_TIME + index, time);
			String cityCode = weatherDetailArrBean.getCity_id();
			if (null != cityCode && MathUtils.isNumeric(cityCode)) {
				FrameApplication.getInstance().getSPEdit().putInt(ISPCallBack.SP_CITY_CODE + index, Integer.parseInt(cityCode));
			}
			/*PRIZE-Change-PrizeWeather-wangzhong-2016_8_10-start*/
			/*FrameApplication.getInstance().getSPEdit().putString(ISPCallBack.SP_CITY_NAME + index, weatherDetailArrBean.getCity_name());*/
			if (!isLocation()) {
				FrameApplication.getInstance().getSPEdit().putString(ISPCallBack.SP_CITY_NAME + index, weatherDetailArrBean.getCity_name());
			}
			/*PRIZE-Change-PrizeWeather-wangzhong-2016_8_10-end*/

			/*PRIZE-Add-PrizeWeather-wangzhong-2016_8_10-start*/
			FrameApplication.getInstance().getSPEdit().putInt(ISPCallBack.SP_CITY_WEATHER_CODE + index, weatherCode);
			/*PRIZE-Add-PrizeWeather-wangzhong-2016_8_10-end*/

			FrameApplication.getInstance().getSPEdit().commit();

			if (isFirst) {
				Log.d(TAG, "to onRefresh() after isFirst : " + isFirst);
				isFirst = false;
				onRefresh();
			}
			/*PRIZE-Add-PrizeWeather-wangzhong-2016_8_10-start*/
			/*Log.d(TAG, "to onRefresh() after isFirst : " + isFirst + " , isJustCreatedView : " + isJustCreatedView);
			if (mCountGetCacheDataJust == 2) {
				mCountGetCacheDataJust++;
				isJustCreatedView = false;
				isFirst = true;
				onRefresh();
			}*/
			
			weatherDetailArrBean = null;
			weatherNowBean = null;
			weatherTodayBean = null;
			/*PRIZE-Add-PrizeWeather-wangzhong-2016_8_10-end*/

			setScrollViewScrollTo(((WeatherHomeActivity) mActivity).getmCurrentScrollY());
			//setScrollViewScrollTo(0);
			/*if (null != refreshableView && !refreshableView.isShown()) {
				refreshableView.setVisibility(View.VISIBLE);
			}*/
			showNodataView(false);
		} else if (null != weatherDetailBean && null != weatherDetailBean.getStatus() && weatherDetailBean.getStatus().equals("EMPTY")) {   // 2015.8.18
			/*PRIZE-Add-PrizeWeather-wangzhong-2016_8_10-start*/
			/*if (isJustCreatedView) {
				isFirst = true;
				isJustCreatedView = false;
				onRefresh();
			}*/
			/*PRIZE-Add-PrizeWeather-wangzhong-2016_8_10-end*/
			showNodataView(true);
		}
		/*PRIZE-Add-PrizeWeather-wangzhong-2016_8_10-start*/
		weatherDetailBean = null;
		/*PRIZE-Add-PrizeWeather-wangzhong-2016_8_10-end*/
		refreshableView.setRefreshing(false);
	}

	private void setImageViewSRCWithTemperature(String temperature) {
		Log.d("hekeyi","temperature = "+temperature);
		if (null != temperature && temperature.length() > 0 && MathUtils.isNumeric(temperature)) {
			int length = temperature.length();
			if (length == 1) {
				wfm_iv_ten.setVisibility(View.GONE);
				wfm_iv_one.setVisibility(View.VISIBLE);
				WeatherImageUtils.setTemperatureImage(Integer.valueOf(temperature), wfm_iv_one);
			} else {
				String ten = temperature.substring(0, 1);
				String one = temperature.substring(1, 2);
				wfm_iv_ten.setVisibility(View.VISIBLE);
				wfm_iv_one.setVisibility(View.VISIBLE);
				WeatherImageUtils.setTemperatureImage(Integer.valueOf(ten), wfm_iv_ten);
				WeatherImageUtils.setTemperatureImage(Integer.valueOf(one), wfm_iv_one);
			}
		}
	}
	
	private void setSRCWithTemperature(String temperature){
//		Log.d("hekeyi","temperature = "+temperature);
//		Log.d("hekeyi","MathUtils.isNumeric(temperature) = "+MathUtils.isNumeric(temperature));
//		temperature = "-29";
		if (null != temperature && temperature.length() > 0 ){
			wfm_iv_txt.setText(temperature);
			wfm_iv_txt.setTypeface(Typeface.createFromAsset(mActivity.getAssets(), "FZLTCXHJW_0.TTF"));
		}
	}

	@Override
	public void onResume() {
		super.onResume();
		/*PRIZE-Add-PrizeWeather-wangzhong-2016_11_3-start*/
		String date = getRefreshTime();
		if (!date.equals(TEMPORARILY_NO_DATA) && !CalendarUtils.isToday(CalendarUtils.formatDate(date))) {
		    isFirst = true;
		}
		/*PRIZE-Add-PrizeWeather-wangzhong-2016_11_3-end*/

		/*PRIZE-Change-PrizeWeather-wangzhong-2016_8_10-start*/
		onRefresh();
		/*if (FrameApplication.LOCATION_POSITION != index) {
			onRefresh();
		}*/
		/*PRIZE-Change-PrizeWeather-wangzhong-2016_8_10-end*/
	}

	@Override
	public void onRefresh() {	// 2
		isSave = false;
		Log.d(TAG, "ACTION METHOD  onRefresh()   **************  isFirst = " + isFirst + ", isSave =" + isSave);
		initPresenter();
		/*PRIZE-Change-PrizeWeather-wangzhong-2016_8_10-start*/
		if (NetworkUtils.isNetWorkActive()) {
			mPresenter.obtainWeatherData();
		} else {
			mPresenter.obtainCacheData();
			if(null != WeatherDetailFragment.this.getActivity()){  //20150906
				Toast.makeText(mActivity, getResources().getString(R.string.noNetwork), Toast.LENGTH_SHORT).show();
			}
		}
		/*if (isJustCreatedView) {
			mCountGetCacheDataJust++;
			mPresenter.obtainCacheData();
		} else {
			if (NetworkUtils.isNetWorkActive()) {
				mPresenter.obtainWeatherData();
			} else {
				if (null != WeatherDetailFragment.this.getActivity()) {
					Toast.makeText(mActivity, getResources().getString(R.string.noNetwork), Toast.LENGTH_SHORT).show();
				}
				if (null != refreshableView) {
					refreshableView.setRefreshing(false);
				}
				isFirst = false;
				mPresenter.obtainCacheData();
			}
		}*/
		/*PRIZE-Change-PrizeWeather-wangzhong-2016_8_10-end*/
	}
	
	private void saveCacheData(WeatherDBCache weatherDBCache) {	// 1
		isSave = true;
		Log.d(TAG, "ACTION METHOD  saveCacheData(WeatherDBCache weatherDBCache)   **************  isFirst = " + isFirst + ", isSave =" + isSave);
		if (NetworkUtils.isNetWorkActive()) {
			initPresenter();
			mPresenter.saveDataToSQLite(weatherDBCache);
		}
	}

	

	@Override
	public void onSaveInstanceState(Bundle outState) {
		// TODO Auto-generated method stub
		//super.onSaveInstanceState(outState);
	}



	//private VideoView mVideoView;
	private LinearLayout contentLayout;
	//private Uri uri;
	//private int sec;
	//private Bitmap bitmap, blurbitmap;
	/*private int isBlur = -1;*//*PRIZE-Delete-PrizeWeather-wangzhong-2016_8_10*/
	private BitmapDrawable bd;
	private ValueAnimator animator;

	/*private void initVideoView() {
		mVideoView = (VideoView) mActivity.findViewById(R.id.weather_video);
//		uri = Uri.parse("android.resource://" + mActivity.getPackageName() + "/" + R.raw.weather_thundershower);
		updateVideoURI(weatherCode);
		mVideoView.start();
		mVideoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
			@Override
			public void onPrepared(MediaPlayer mp) {
				mp.start();
				mp.setLooping(true);
			}
		});
	}*/
	
	/*private void updateVideoURI(int weatherCode) {
		int[] res = WeatherImageUtils.setWeatherImage(weatherCode, null, CalendarUtils.isDayTime(), 2);
		uri = Uri.parse("android.resource://" + mActivity.getPackageName() + "/" + res[2]);
		mVideoView.setVideoURI(uri);
		mVideoView.start();
		mVideoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
			@Override
			public void onPrepared(MediaPlayer mp) {
				mp.start();
				mp.setLooping(true);
			}
		});
	}*/

	//private FrameLayout weatherFrameLayout;
	private void initBlurView() {
		
		final WeatherImageUtils mWeatherImageUtils = WeatherImageUtils.mWeatherImageUtils;   //move
		contentLayout = (LinearLayout) mActivity.findViewById(R.id.weather_blur);
		blurScrollView = (BlurScrollView) mView.findViewById(R.id.blur_scroll);
		//weatherFrameLayout = (FrameLayout) mActivity.findViewById(R.id.weather_content);
		blurScrollView.setOnScrollListener(new OnScrollListener() {
			@Override
			public void onScroll(int scrollY) {
				Log.d(TAG, "Scroll  setmCurrentScrollY : ScrollView.OnScrollListener.onScroll : " + scrollY);
				((WeatherHomeActivity) mActivity).setmCurrentScrollY(scrollY);
				/*PRIZE-Change-PrizeWeather-wangzhong-2016_8_10-start*/
				/*if (scrollY == 0) {
					if (isBlur == 1) {
						mHandler.sendEmptyMessage(2);
						isBlur = 0;
						mWeatherImageUtils.OnMoveRestart(weatherCode);   //move
					}
				} else {
					if (isBlur != 1) {
						Thread thread = new Thread(r);
						thread.start();
						isBlur = 1;
						mWeatherImageUtils.OnMoveStop(weatherCode);   //move
					}
				}*/
				if (scrollY == 0) {
					if (WeatherHomeActivity.isBlur == WeatherHomeActivity.BLUR_VISIBLE) {
						mHandler.sendEmptyMessage(2);
						WeatherHomeActivity.isBlur = WeatherHomeActivity.BLUR_INVISIBLE;
						mWeatherImageUtils.OnMoveRestart(weatherCode);
					}
				} else {
					if (WeatherHomeActivity.isBlur == WeatherHomeActivity.BLUR_INVISIBLE) {
						Thread thread = new Thread(r);
						thread.start();
						WeatherHomeActivity.isBlur = WeatherHomeActivity.BLUR_VISIBLE;
						mWeatherImageUtils.OnMoveStop(weatherCode);
					}
				}
				/*PRIZE-Change-PrizeWeather-wangzhong-2016_8_10-end*/
			}
		});
	}

	private Bitmap blurbitmap;
	
	@SuppressLint("HandlerLeak")
	public Handler mHandler = new Handler() {
		@SuppressWarnings("deprecation")
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case 1:
				// Bitmap bitmap =
				// ((BitmapDrawable)weatherFrameLayout.getBackground()).getBitmap();
				// Bitmap bitmap =
				// BitmapFactory.decodeResource(mActivity.getResources(),
				// R.drawable.weather_bg_cloudy_daytime);
				// bitmap = Bitmap.createBitmap(720, 1280,
				// Bitmap.Config.ARGB_8888);
				// blurbitmap = blurBitmap(bitmap);
				/*
				 * if(null==bitmap){ bitmap = Bitmap.createBitmap(720, 1280,
				 * Bitmap.Config.ARGB_8888); }
				 */

				if ((null == blurbitmap) || (blurbitmap.isRecycled())) {
					blurbitmap = blurBitmap();
					bd = new BitmapDrawable(blurbitmap);
					contentLayout.setBackground(bd);
				}

				animator = ValueAnimator.ofFloat(0f, 1f);
				animator.setDuration(1000);
				animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
					@Override
					public void onAnimationUpdate(ValueAnimator animation) {
						contentLayout.setAlpha((Float) animation.getAnimatedValue());
					}
				});
				// animator.addListener(new AnimatorListenerAdapter() {
				// @Override
				// public void onAnimationEnd(Animator animation) {
				// super.onAnimationEnd(animation);
				// float alpha = 0.7f;
				// contentLayout.setAlpha(alpha);
				// }
				// });
				animator.start();
				break;
			case 2:
				animator = ValueAnimator.ofFloat(1f, 0f);
				animator.setDuration(1000);
				animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
					@Override
					public void onAnimationUpdate(ValueAnimator animation) {
						contentLayout.setAlpha((Float) animation.getAnimatedValue());
					}
				});
				// animator.addListener(new AnimatorListenerAdapter() {
				// @Override
				// public void onAnimationEnd(Animator animation) {
				// super.onAnimationEnd(animation);
				// float alpha = 0f;
				// contentLayout.setAlpha(alpha);
				// contentLayout.setBackground(null);
				// }
				// });
				animator.start();
				// sec = mVideoView.getCurrentPosition();
				// mVideoView.setVideoURI(uri);
				// mVideoView.seekTo(sec);
				// mVideoView.start();
				break;
			}
		}
	};

	private Runnable r = new Runnable() {
		@Override
		public void run() {
			Log.d(TAG, "new thread to blur the background");
			// sec = mVideoView.getCurrentPosition();
			// mVideoView.pause();
			// MediaMetadataRetriever retriever = new MediaMetadataRetriever();
			// retriever.setDataSource(mActivity, uri);
			// Bitmap bitmap = retriever.getFrameAtTime(sec,
			// MediaMetadataRetriever.OPTION_CLOSEST_SYNC);
			// mVideoView.stopPlayback();

			// Bitmap bitmap
			// =((BitmapDrawable)weatherFrameLayout.getBackground()).getBitmap();
			// blurbitmap = blurBitmap(bitmap);
			// bd = new BitmapDrawable(blurbitmap);

			Message message = new Message();
			message.what = 1;
			mHandler.sendMessage(message);
		}
	};

	public Bitmap blurBitmap(Bitmap bitmap) {

		// Let's create an empty bitmap with the same size of the bitmap we want
		// to blur
		Bitmap outBitmap = Bitmap.createBitmap(bitmap.getWidth(),
				bitmap.getHeight(), Config.ARGB_8888);

		// Instantiate a new Renderscript
		RenderScript rs = RenderScript
				.create(mActivity.getApplicationContext());

		// Create an Intrinsic Blur Script using the Renderscript
		ScriptIntrinsicBlur blurScript = ScriptIntrinsicBlur.create(rs,
				Element.U8_4(rs));

		// Create the Allocations (in/out) with the Renderscript and the in/out
		// bitmaps
		Allocation allIn = Allocation.createFromBitmap(rs, bitmap);
		Allocation allOut = Allocation.createFromBitmap(rs, outBitmap);

		// Set the radius of the blur
		blurScript.setRadius(25f);

		// Perform the Renderscript
		blurScript.setInput(allIn);
		blurScript.forEach(allOut);
		// Copy the final bitmap created by the out Allocation to the outBitmap
		allOut.copyTo(outBitmap);
		// recycle the original bitmap
		bitmap.recycle();

		// After finishing everything, we destroy the Renderscript.
		rs.destroy();
		Canvas canvas = new Canvas(outBitmap);
		canvas.drawColor(0xB2000000);
		return outBitmap;
	}

	//private LruCache<String, Bitmap> mMemoryCache;

	Bitmap outBitmap = null;
	@SuppressWarnings("deprecation")
	public Bitmap blurBitmap() {

		// Let's create an empty bitmap with the same size of the bitmap we want
		// to blur

		//2015.8.31  begin
		InputStream is;
//		outBitmap = Bitmap.createBitmap(720, 1280, Config.ARGB_8888);
		is = mActivity.getResources().openRawResource(R.drawable.weather_bg_blur);
		BitmapFactory.Options options = new BitmapFactory.Options();  
//        options.inJustDecodeBounds = true; 
        options.inPurgeable = true;
		options.inSampleSize = 4;
		options.inInputShareable = true;    
		//2015.8.31 end
		if(outBitmap==null){
	        outBitmap = BitmapFactory.decodeStream(is, null, options).copy(Bitmap.Config.ARGB_8888, true);		
//			outBitmap = LruCacheUtils.mLruCacheUtils.loadBitmap(mActivity, R.drawable.weather_bg_blur);
		}
		
        
		// Instantiate a new Renderscript
		/*RenderScript rs = RenderScript
				.create(mActivity.getApplicationContext());

		// Create an Intrinsic Blur Script using the Renderscript
		ScriptIntrinsicBlur blurScript = ScriptIntrinsicBlur.create(rs,Element.U8_4(rs));

		// Create the Allocations (in/out) with the Renderscript and the in/out
		// bitmaps
		// Allocation allIn = Allocation.createFromBitmap(rs, bitmap);
		Allocation allOut = Allocation.createFromBitmap(rs, outBitmap);

		// Set the radius of the blur
		blurScript.setRadius(25f);

		// Perform the Renderscript
		// blurScript.setInput(allIn);
		blurScript.forEach(allOut);
		// Copy the final bitmap created by the out Allocation to the outBitmap
		allOut.copyTo(outBitmap);
		// recycle the original bitmap
		// bitmap.recycle();

		// After finishing everything, we destroy the Renderscript.
		rs.destroy();*/
//		Canvas canvas = new Canvas(outBitmap);
		Canvas canvas = new Canvas();
		canvas.drawBitmap(outBitmap,0,0,null);
//		canvas.drawColor(0xB2000000);
		return outBitmap;
	}

}
