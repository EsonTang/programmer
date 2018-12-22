package com.prize.weather.widget;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import com.prize.weather.framework.BaseService;
import com.prize.weather.util.Common;

@SuppressLint("HandlerLeak")
public class WidgetService extends BaseService {
	
	private static final String TAG = "WidgetService";
	
	private static final String REFRESH_WIDGET_WEATHER	= "com.prize.weather.REFRESH_WIDGET_WEATHER";
	public static final String ACTION_UPDATE_WIDGETCITY = "com.prize.weatherwidget.ACTION_UPDATE_WIDGETCITY";
	public static final String ACTION_CLEARVIEW 		= "com.prize.weatherwidget.ACTION_CLEARVIEW";
	public static final String ACTION_UPDATE_TIME 		= "com.prize.weatherwidget.ACTION_UPDATE_TIME";
	public static final String ACTION_UPDATE_WEATHER 	= "com.prize.weatherwidget.ACTION_UPDATE_WEATHER";
	
	//private static final int REFRESH_TIME = 1000;
	private static final int REFRESH_WEATHER = 2000;
	
	private WeatherWidgetProvider_4x2 mAppWidgetProvider_4x2 = WeatherWidgetProvider_4x2.getInstance();
	
	private Handler mHandler = new Handler() {		
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case REFRESH_WEATHER:
				Log.d(TAG, "mHandler ACTION_UPDATE_WEATHER..........");
				mAppWidgetProvider_4x2.notifyChange(WidgetService.this, ACTION_UPDATE_WEATHER);
				break;
			}
		}
	};
	
	private BroadcastReceiver tickReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			Log.d(TAG,"receive broadcast action = "+action);
			if (action.equals(Common.UPDATE_WEATHET_WIDGET) //|| (action.equals(Intent.ACTION_USER_PRESENT)) 
					|| (action.equals(REFRESH_WIDGET_WEATHER)) || (action.equals(Intent.ACTION_TIME_CHANGED))
					|| (action.equals(Intent.ACTION_TIMEZONE_CHANGED))) {
				mHandler.sendEmptyMessage(REFRESH_WEATHER);
			}
			
			if(action.equals("android.net.conn.CONNECTIVITY_CHANGE")){
				initLocation();
				mHandler.sendEmptyMessage(REFRESH_WEATHER);
			}
		}
	};
	
	@Override
	public void onCreate() {
		super.onCreate();
		Log.d(TAG, "onCreate..........");	
		
		Message msg2 = mHandler.obtainMessage();
		msg2.what = REFRESH_WEATHER;
		mHandler.sendMessage(msg2);
		
		IntentFilter intentFilter = new IntentFilter();	
//		intentFilter.addAction(Intent.ACTION_USER_PRESENT);
		intentFilter.addAction(Common.UPDATE_WEATHET_WIDGET);
		intentFilter.addAction(REFRESH_WIDGET_WEATHER);
		intentFilter.addAction(Intent.ACTION_TIME_CHANGED);
		intentFilter.addAction(Intent.ACTION_TIMEZONE_CHANGED);
		intentFilter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
		registerReceiver(tickReceiver, intentFilter);
	}

	@Override
	public IBinder onBind(Intent arg0) {
		return null;
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		unregisterReceiver(tickReceiver);
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
//		super.onStart(intent, startId);
//		Log.d(TAG, "Widget Service started");
		return super.onStartCommand(intent, START_REDELIVER_INTENT, startId);
	}

	@Override
	public void onBDLocationFinishedListener() {
		mHandler.sendEmptyMessage(REFRESH_WEATHER);
	}
	
}
