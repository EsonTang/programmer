package com.prize.weather.widget;

import java.util.ArrayList;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.util.Log;
import android.widget.RemoteViews;

import com.alibaba.fastjson.JSONException;
import com.prize.weather.R;
import com.prize.weather.framework.ISPCallBack;
import com.prize.weather.framework.model.WeatherDBCache;
import com.prize.weather.util.CalendarUtils;
import com.prize.weather.util.MathUtils;
import com.prize.weather.util.NetworkUtils;
import com.prize.weather.util.WeatherImageUtils;

public class WeatherWidgetProvider_4x2 extends AppWidgetProvider implements IWeatherWidgetView {
	
	private static final String TAG = "WeatherProvider";
	private static final String REFRESH_WIDGET = "com.prize.weather.refresh";
	public static final String SYSTEM_READY = "com.prize.intent.action.SYSTEM_READY";
	
	public static final ComponentName THIS_APPWIDGET = new ComponentName(
			"com.prize.weather",
			"com.prize.weather.widget.WeatherWidgetProvider_4x2");
	private static WeatherWidgetProvider_4x2 sInstance;
	
	private int cityNum;
	ArrayList<String> cityNameList = new ArrayList<String>();
	ArrayList<Integer> cityFlagList = new ArrayList<Integer>();
	ArrayList<Integer> cityCodeList = new ArrayList<Integer>();

	
	private WeatherWidgetPresenter mPresenter = null;
	private Context mContext;
	private String keyWord;
	private int cityCode;
	private boolean isSave = true;

    /*PRIZE-Add-PrizeWeather-wangzhong-2016_8_10-start*/
    private int mSendTemperature = 20;
    private String mSendWeather = WeatherImageUtils.mWeather[0];
    /*PRIZE-Add-PrizeWeather-wangzhong-2016_8_10-end*/

	public static synchronized WeatherWidgetProvider_4x2 getInstance() {
		if (sInstance == null) {
			sInstance = new WeatherWidgetProvider_4x2();
		}
		return sInstance;
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		super.onReceive(context, intent);
		this.mContext = context;		
		if (intent.getAction().equals(REFRESH_WIDGET) || SYSTEM_READY.equals(intent.getAction()) || Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
			updateCity(context, true);
		}		
	}

	@Override
	public void onDeleted(Context context, int[] appWidgetIds) {
		super.onDeleted(context, appWidgetIds);
		this.mContext = context;
	}

	@Override
	public void onDisabled(Context context) {
		super.onDisabled(context);
		this.mContext = context;
		Log.d(TAG, "onDisabled.....");
	}

	@Override
	public void onEnabled(Context context) {
		super.onEnabled(context);
		this.mContext = context;
		Log.d(TAG, "onEnabled.....");
		defaultView(context);
		Intent intent = new Intent(context, WidgetService.class);
		intent.setAction(WidgetService.ACTION_UPDATE_WIDGETCITY);
		context.startService(intent);
		/*IntentFilter filter = new IntentFilter();
		filter.addAction(Intent.ACTION_TIME_TICK);
		filter.addAction(Intent.ACTION_TIME_CHANGED);
		filter.addAction(Intent.ACTION_TIMEZONE_CHANGED);
		filter.addAction(Intent.ACTION_USER_PRESENT);*/
	}

	@Override
	public void onUpdate(Context context, AppWidgetManager appWidgetManager,
			int[] appWidgetIds) {
		super.onUpdate(context, appWidgetManager, appWidgetIds);
		this.mContext = context;
		Log.d(TAG, "onUpdate.....");
		defaultView(context);
		Intent intent = new Intent(context, WidgetService.class);
		intent.setAction(WidgetService.ACTION_UPDATE_WIDGETCITY);
		context.startService(intent);

		// 启动服务
//		Intent locationIntent = new Intent(context, LocationSvc.class);
//		locationIntent.setAction(Common.LOCATION_ACTION);
//		context.startService(locationIntent);
	}

	private void updateViews(Context context) {
		if (remontView == null) {
			remontView = new RemoteViews(context.getPackageName(),
					R.layout.weather_appwidget_4x2);
		}

//		updateCity(context, false);
		updateCity(context, true);
		Log.d(TAG,"updateViews......updateCity(context, true);.....");
		pushUpdate(context, null);
	}
	
	private void updateCity(final Context context, boolean needPush) {
		Log.d(TAG,"updateCity...........");
		this.mContext = context;
		if (remontView == null) {
			updateViews(context);
			return;
		}
		
		getCity(context);
		Log.d(TAG,"updateCity...........cityNum = "+cityNum);
		
		for (int i = 0; i < cityNum; i++) {
			
			Log.d(TAG,"updateCity...........cityFlagList.get("+i+") = "+cityFlagList.get(i));
			
			if (cityFlagList.get(i) == 1) {
				//remontView.setTextViewText(R.id.city_widget, cityNameList.get(i));
				
				cityCode = cityCodeList.get(i);
				keyWord = cityNameList.get(i);
				
				onRefreshData();
			}
		}
		if (needPush) {
			pushUpdate(context, null);
		}
	}
	
	private boolean hasInstances(Context context) {
		AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
		int[] appWidgetIds = appWidgetManager.getAppWidgetIds(new ComponentName(context, this.getClass()));
		return (appWidgetIds.length > 0);
	}

	RemoteViews remontView;

	private void defaultView(Context context) {
		if(remontView == null){
			remontView = new RemoteViews(context.getPackageName(),R.layout.weather_appwidget_4x2);			
		}
		clickListener(context);

		updateCity(context, true);
		updateWeatherViews(context, true);
		pushUpdate(context, null);
	}

	private void updateWeatherViews(Context context, boolean b) {
		if (remontView == null) {
			updateViews(context);
			return;
		}
	}

	
	public void notifyChange(WidgetService service, String what) {
		clickListener(service);
		if (hasInstances(service)) {
			if (WidgetService.ACTION_UPDATE_WEATHER.equals(what)) {
				updateCity(service, true);
			}
		}

        /*PRIZE-Add-PrizeWeather-wangzhong-2016_8_10-start*/
        getCity(mContext);
        for (int i = 0; i < cityNum; i++) {
            if (cityFlagList.get(i) == 1) {
                cityCode = cityCodeList.get(i);
                keyWord = cityNameList.get(i);
                onRefreshData();
            }
        }
        /*PRIZE-Add-PrizeWeather-wangzhong-2016_8_10-end*/
	}

	private void pushUpdate(Context context, int[] appWidgetIds) {
		Log.d(TAG,"pushUpdate...........remontView = "+remontView);
		if (remontView == null) {
			updateViews(context);
			return;
		}
		AppWidgetManager gm = AppWidgetManager.getInstance(context);
		if (appWidgetIds != null) {
			gm.updateAppWidget(appWidgetIds, remontView);
		} else {
			gm.updateAppWidget(THIS_APPWIDGET, remontView);
//			for(int i = 0;i<appWidgetIds.length;i++){
//				gm.updateAppWidget(appWidgetIds[i], remontView);
//			}
			Log.d(TAG,"pushUpdate...........update all widget  ");
		}
	}

	private void clickListener(Context context) {
		if (remontView == null) {
			updateViews(context);
			return;
		}
		// to clock.
		PackageManager pm = context.getPackageManager();
		Intent calendar = pm.getLaunchIntentForPackage("com.android.deskclock");
		PendingIntent calendar2 = PendingIntent.getActivity(context, 0, calendar, 0);
		remontView.setOnClickPendingIntent(R.id.left_layout, calendar2);
		// to app.
		Intent weahter = pm.getLaunchIntentForPackage("com.prize.weather");
		PendingIntent weahter2 = PendingIntent.getActivity(context, 0, weahter, 0);
		remontView.setOnClickPendingIntent(R.id.weather_layout, weahter2);
	}

	private void getCity(Context context) {
		SharedPreferences citySharePreferences = context.getSharedPreferences(ISPCallBack.SHARED_PREFERENCES_FILE_NAME, Context.MODE_MULTI_PROCESS);
		cityNum= citySharePreferences.getInt(ISPCallBack.SP_CITY_NUM, 1);
		cityNameList.clear();
		cityFlagList.clear();
		cityCodeList.clear();
		for (int i = 0; i < cityNum; i++) {
			cityNameList.add(citySharePreferences.getString(ISPCallBack.SP_CITY_NAME + i, ""));
			cityFlagList.add(citySharePreferences.getInt(ISPCallBack.SP_CITY_FLAG + i, 0));
			cityCodeList.add(citySharePreferences.getInt(ISPCallBack.SP_CITY_CODE + i, 0));
		}
	}

	
	
	
	
	
	
	private void initPresenter() {
		if (null == mPresenter) {
			mPresenter = new WeatherWidgetPresenter(this);
		}
	}

	@Override
	public Context getContext() {
		return mContext;
	}

	@Override
	public void handleException(Exception e) {
		
	}

	@Override
	public void handleJsonExcetpion(JSONException e) {
		
	}

	@Override
	public void updateView(Object o) {
		WeatherDBCache weatherDBCache = (WeatherDBCache) o;
		if (null != weatherDBCache && null != weatherDBCache.getCity_name() && !weatherDBCache.getCity_name().equals("")) {
			saveCacheData(weatherDBCache);
			Log.d(TAG,"Cityname = "+keyWord+"  weather = "+weatherDBCache.getWeather_text()+"  temperature = "+weatherDBCache.getWeather_temperature());
			remontView.setTextViewText(R.id.city_widget, keyWord);
			remontView.setTextViewText(R.id.tv_widget_weathwetext, weatherDBCache.getWeather_temperature() + "\u2103");
			remontView.setTextViewText(R.id.tv_widget_weatherdes, weatherDBCache.getWeather_text());
			if (null != weatherDBCache.getWeather_code() && MathUtils.isNumeric(weatherDBCache.getWeather_code())) {
				remontView.setImageViewResource(R.id.iv_widget_weathweicon, WeatherImageUtils.setWeatherImage(
						Integer.valueOf(weatherDBCache.getWeather_code()), null, CalendarUtils.isDayTime(), 0)[4]);
			}
			pushUpdate(mContext, null);

            /*PRIZE-Add-PrizeWeather-wangzhong-2016_8_10-start*/
            if (null != weatherDBCache.getWeather_temperature() && MathUtils.isNumeric(weatherDBCache.getWeather_temperature())) {
                mSendTemperature = Integer.valueOf(weatherDBCache.getWeather_temperature());
            }
            if (null != weatherDBCache.getWeather_code() && MathUtils.isNumeric(weatherDBCache.getWeather_code())) {
                mSendWeather = WeatherImageUtils.getWeatherWithCode(Integer.valueOf(weatherDBCache.getWeather_code()));
            }
            Log.d(TAG, "mSendWeather.........mSendWeather = " + mSendWeather + ",  mSendTemperature = " + mSendTemperature);
            Intent intent = new Intent();
            intent.setAction("com.cooee.weather.Weather.action.REFRESH_UPDATE_LAUNCHER_FOR_3TH");
            intent.putExtra("T0_condition", mSendWeather);
            intent.putExtra("T0_tempc_now", mSendTemperature);
            mContext.sendBroadcast(intent);
            /*PRIZE-Add-PrizeWeather-wangzhong-2016_8_10-end*/

		} else {
			if (NetworkUtils.isNetWorkActive()) {
				onRefreshDataWithCacheData();
			}
		}
	}
	
	private void onRefreshData() {
		isSave = false;		// obtain the data.
		initPresenter();
		if (NetworkUtils.isNetWorkActive()) {
			mPresenter.obtainWeatherData();
		} else {
			mPresenter.obtainCacheData();
		}
	}
	
	private void onRefreshDataWithCacheData(){
		isSave = false;		// obtain the data.
		initPresenter();
		mPresenter.obtainCacheData();
	}
	
	private void saveCacheData(WeatherDBCache weatherDBCache) {
		isSave = true;
		if (NetworkUtils.isNetWorkActive()) {
			initPresenter();
			mPresenter.saveDataToSQLite(weatherDBCache);
		}
	}
	
	@Override
	public void openUpdateStatus() {
		
	}

	@Override
	public void closeUpdateStatus() {
		
	}

	@Override
	public void showNodataView(boolean isNodata) {
		
	}

	@Override
	public String getName() {
		return keyWord;
	}

	@Override
	public int getID() {
		return cityCode;
	}

	@Override
	public boolean isSave() {
		return isSave;
	}

}
