package com.prize.uploadappinfo;

import java.util.List;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.app.Application;
import android.content.Context;

import com.baidu.location.LocationClientOption;
import com.baidu.location.LocationClientOption.LocationMode;
import com.prize.uploadappinfo.bean.ClientInfo;
import com.prize.uploadappinfo.constants.Constant;
import com.prize.uploadappinfo.database.PrizeDatabaseHelper;
import com.prize.uploadappinfo.http.XExtends;
import com.prize.uploadappinfo.utils.JLog;
import com.prize.uploadappinfo.utils.LocationUtils;
import com.prize.uploadappinfo.utils.PollingUtils;

public class BaseApplication extends Application {
	private static String TAG = BaseApplication.class.getSimpleName();
	// 记录当前 Context
	public static Context curContext;
	public LocationUtils locationService;


	@Override
	public void onCreate() {
		JLog.i("MyLocationListener", "onCreate");
		curContext = this;
		super.onCreate();
		String processName = getProcessName(this, android.os.Process.myTid());
		XExtends.Ext.init(this);
		XExtends.Ext.setDebug(false);
		if (processName != null) {
			boolean defaultProcess = processName.equals(getPackageName());
			if (defaultProcess) {
				locationService = new LocationUtils(getApplicationContext());
				ClientInfo.initClientInfo();
				PrizeDatabaseHelper.initPrizeSQLiteDatabase();
				PollingUtils.startPollingService(getApplicationContext(), Constant.PUSH_FOR_TIME);
				// MTA统计
			}
		}

	}

	/**
	 * @return null may be returned if the specified process not found
	 */
	public static String getProcessName(Context cxt, int pid) {
		ActivityManager am = (ActivityManager) cxt
				.getSystemService(Context.ACTIVITY_SERVICE);
		List<RunningAppProcessInfo> runningApps = am.getRunningAppProcesses();
		if (runningApps == null) {
			return null;
		}
		for (RunningAppProcessInfo procInfo : runningApps) {
			if (procInfo.pid == pid) {
				return procInfo.processName;
			}
		}
		return null;
	}

	private LocationClientOption mOption;

	public LocationClientOption getDefaultLocationClientOption() {
		if (mOption == null) {
			mOption = new LocationClientOption();
			mOption.setLocationMode(LocationMode.Hight_Accuracy);// 可选，默认高精度，设置定位模式，高精度，低功耗，仅设备
			mOption.setCoorType("bd09ll");// 可选，默认gcj02，设置返回的定位结果坐标系，如果配合百度地图使用，建议设置为bd09ll;
			mOption.setScanSpan(120000);// 可选，默认0，即仅定位一次，设置发起定位请求的间隔需要大于等于1000ms才是有效的
			mOption.setIsNeedAddress(true);// 可选，设置是否需要地址信息，默认不需要
			mOption.setIsNeedLocationDescribe(true);// 可选，设置是否需要地址描述
			mOption.setNeedDeviceDirect(false);// 可选，设置是否需要设备方向结果
			mOption.setLocationNotify(false);// 可选，默认false，设置是否当gps有效时按照1S1次频率输出GPS结果
			mOption.setIgnoreKillProcess(true);// 可选，默认true，定位SDK内部是一个SERVICE，并放到了独立进程，设置是否在stop的时候杀死这个进程，默认不杀死
			mOption.setIsNeedLocationDescribe(true);// 可选，默认false，设置是否需要位置语义化结果，可以在BDLocation.getLocationDescribe里得到，结果类似于“在北京天安门附近”
			mOption.setIsNeedLocationPoiList(false);// 可选，默认false，设置是否需要POI结果，可以在BDLocation.getPoiList里得到
			mOption.SetIgnoreCacheException(false);// 可选，默认false，设置是否收集CRASH信息，默认收集
		}
		return mOption;
	}
}