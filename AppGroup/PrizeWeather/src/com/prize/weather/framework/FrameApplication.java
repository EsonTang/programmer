package com.prize.weather.framework;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;

/**
 * 
 * @author wangzhong
 *
 */
public class FrameApplication extends Application implements ISPCallBack {
//		Thread.UncaughtExceptionHandler {
	
	public final static int LOCATION_POSITION	= 0;
	private final static String LOCATION_SUFFIX = "市";
	
	private static FrameApplication appInstance;
	
	private SharedPreferences mSharedPreferences;
	private SharedPreferences.Editor mSPEdit;
	
	public LocationClient mLocationClient = null;
	private IBDLocationFinishedListener mIBDLocationFinishedListener;
	
	public static FrameApplication getInstance() {
		return appInstance;
	}
	
	public SharedPreferences getSharedPreferences() {
		return mSharedPreferences;
	}

	public SharedPreferences.Editor getSPEdit() {
		return mSPEdit;
	}
	
	public void setmIBDLocationFinishedListener(
			IBDLocationFinishedListener mIBDLocationFinishedListener) {
		this.mIBDLocationFinishedListener = mIBDLocationFinishedListener;
	}

	private BDLocationListener mBDLocationListener = new BDLocationListener() {

		public void onReceivePoi(BDLocation bdlocation) {
			
		}

		@Override
		public void onReceiveLocation(BDLocation location) {
			if (null != location && null != mSharedPreferences && null != mSPEdit) {
				// city code.
				if (null != location.getCityCode()) {
				    /*PRIZE-Add-PrizeWeather-wangzhong-2016_8_10-start*/
					Log.d("WEATHER Location", "location.getCityCode() : " + location.getCityCode());
					/*PRIZE-Add-PrizeWeather-wangzhong-2016_8_10-end*/
					mSPEdit.putInt(ISPCallBack.SP_CITY_CODE + LOCATION_POSITION, Integer.parseInt(location.getCityCode()));
				}
				// city name
				String cityName = location.getCity();
				if (null != cityName) {
					if (cityName.substring(cityName.length() - 1, cityName.length()).equals(LOCATION_SUFFIX)) {
						cityName = cityName.substring(0, location.getCity().length() - 1);
					}
					/*PRIZE-Add-PrizeWeather-wangzhong-2016_8_10-start*/
					Log.d("WEATHER Location", "cityName : " + cityName);
					/*PRIZE-Add-PrizeWeather-wangzhong-2016_8_10-end*/
					mSPEdit.putString(ISPCallBack.SP_CITY_NAME + LOCATION_POSITION, cityName);
				}
				// city flag
				int num = mSharedPreferences.getInt(ISPCallBack.SP_CITY_NUM, 0);
				int flag = 1;
				for (int i = 1; i < num; i++) {
					int tmpflag = mSharedPreferences.getInt(ISPCallBack.SP_CITY_FLAG + i, -1);
					if (1 == tmpflag) {
						flag = 0;
						break;
					}
					flag = 1;
				}
				mSPEdit.putInt(ISPCallBack.SP_CITY_FLAG + LOCATION_POSITION, flag);
				mSPEdit.commit();
				stopBDLocation();
				if (null != mIBDLocationFinishedListener) {
					mIBDLocationFinishedListener.onBDLocationFinishedListener();
				}
			}
		}
	};

	@Override
	public void onCreate() {
		super.onCreate();
//		Thread.setDefaultUncaughtExceptionHandler(this);
		appInstance = this;
		initSharedPreferences();
		
		initBDLocation();
	}

//	@Override
//	public void uncaughtException(Thread thread, Throwable ex) {
//		Log.e("Exception uncaught at Thread = ", thread.getName());
//		Log.e("System.exit at", Log.getStackTraceString(ex));
//		
//	}

	@Override
	public void initSharedPreferences() {
		mSharedPreferences = getSharedPreferences(
				SHARED_PREFERENCES_FILE_NAME, Context.MODE_PRIVATE);
		mSPEdit = mSharedPreferences.edit();
	}
	
	/**
	 * Initialize the baidu Location.
	 */
	private void initBDLocation() {
		Log.d("hekeyi", "initBDLocation");
		mLocationClient = new LocationClient(this);
		mLocationClient.registerLocationListener(mBDLocationListener);
	}
	
	public void startBDLocation(LocationClientOption option) {
		if (null == mLocationClient) {
			initBDLocation();
		}
		mLocationClient.setLocOption(option);
		mLocationClient.start();
	}
	
	public void stopBDLocation() {
		if (null != mLocationClient && mLocationClient.isStarted()) {
			mLocationClient.stop();
		}
		mLocationClient = null;
	}
	
	public void setLocationOption() {
		LocationClientOption option = new LocationClientOption();
		option.setProdName("Compass");
//		option.setOpenGps(true);
		option.setAddrType("all");
		option.setCoorType("bd09ll");
		option.setServiceName("com.baidu.location.service_v2.9");
		/*option.setPoiExtraInfo(true);*//*PRIZE-Delete-PrizeWeather-wangzhong-2016_8_10*/
		option.setScanSpan(3);
//		option.setPriority(LocationClientOption.GpsFirst);
		option.setOpenGps(false);
		option.setPriority(LocationClientOption.NetWorkFirst);
		/*option.setPoiNumber(10);*///*PRIZE-Delete-PrizeWeather-wangzhong-2016_8_10*/
		option.disableCache(true);
		startBDLocation(option);
	}

	/*private boolean isGpsEnable() {
        LocationManager locationManager = (LocationManager)this.getSystemService("location");
        return locationManager.isProviderEnabled("gps");
    }*/
	
}
