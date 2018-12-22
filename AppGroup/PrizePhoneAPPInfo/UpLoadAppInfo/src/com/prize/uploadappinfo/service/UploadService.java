package com.prize.uploadappinfo.service;

import org.json.JSONException;
import org.json.JSONObject;
import org.xutils.common.Callback.CommonCallback;
import org.xutils.http.RequestParams;

import android.app.Service;
import android.content.ContentValues;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.IBinder;
import android.text.TextUtils;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.prize.uploadappinfo.BaseApplication;
import com.prize.uploadappinfo.constants.Constant;
import com.prize.uploadappinfo.database.AppStateTable;
import com.prize.uploadappinfo.database.PrizeDatabaseHelper;
import com.prize.uploadappinfo.database.dao.AppStateDAO;
import com.prize.uploadappinfo.http.HttpUtils;
import com.prize.uploadappinfo.http.HttpUtils.RequestPIDCallBack;
import com.prize.uploadappinfo.http.XExtends;
import com.prize.uploadappinfo.receiver.NetStateReceiver;
import com.prize.uploadappinfo.threads.InitInstalledAppTask;
import com.prize.uploadappinfo.utils.CommonUtils;
import com.prize.uploadappinfo.utils.JLog;
import com.prize.uploadappinfo.utils.LocationUtils;
import com.prize.uploadappinfo.utils.PollingUtils;
import com.prize.uploadappinfo.utils.PreferencesUtils;

/**
 * 
 * 上传安装卸载手机应用服务
 * 
 * @author prize
 *
 */
public class UploadService extends Service {
	private static final String TAG = "UploadService";
	private static NetStateReceiver netstateReceiver;
	/** 操作类型, int型,0.表示启动service扫描应用； 1.表示有网络连接时； 2 安装卸载广播触发事件； 3.定时任务触发事件 */
	public static final String OPT_TYPE = "optType";
	public static final int NET_CONNECTED = 1;
	public BDLocationListener myListener = new MyLocationListener(null, null);
	public BDLocationListener appListener = null;
	private LocationUtils mLocationUtils;
	/** */
	public static final String APP_NAME = "app_name";
	public static final String APP_PACKAGE = "app_package";
	/** app是安装还是卸载动作 */
	public static final String APP_TYPE = "app_type";

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public void onCreate() {
		netstateReceiver = new NetStateReceiver();
		this.registerReceiver(netstateReceiver, new IntentFilter(
				ConnectivityManager.CONNECTIVITY_ACTION));
		super.onCreate();
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		int optType = intent.getIntExtra(OPT_TYPE, 0);
		switch (optType) {

		case 0:
			JLog.i(TAG, "onStartCommand-0.表示启动service扫描应用；");
			if (!CommonUtils.isInitIntalledAppOk()) {
				if (!InitInstalledAppTask.isRun()) {
					InitInstalledAppTask task = new InitInstalledAppTask(this);
					task.execute();
				}
			}
			break;

		case 1: // 1,表示有网络连接时
			JLog.i(TAG,
					"onStartCommand-1,表示有网络连接时--"
							+ PreferencesUtils.getKEY_TID());
			if (TextUtils.isEmpty(PreferencesUtils.getKEY_TID())) {
				getPidAndUUIdFromServer();
			}
			if (AppStateDAO.getInstance().getApps().size() > 0) {

			}
			String time = PreferencesUtils
					.getAddressTime(getApplicationContext());
			long currentTime = System.currentTimeMillis();
			//当前地址或则请求地址时保存的时间为空，或者间隔时间大于需求时，此时采取请求请求定位
			if (TextUtils.isEmpty(PreferencesUtils
					.getAddress(getApplicationContext()))
					|| TextUtils.isEmpty(time)
					|| (currentTime - Long.parseLong(time) > Constant.REQUEST_ADDRESS_RATE)) {
				mLocationUtils = ((BaseApplication) getApplication()).locationService;
				mLocationUtils.registerListener(myListener);
				mLocationUtils.start();
			}
			break;
		case 2: // 2.安装卸载逻辑
			JLog.i(TAG, "onStartCommand-2.安装卸载逻辑");
			String appName = null;
			String type = null;
			String pkgName = null;
			try {
				appName = intent.getStringExtra(APP_NAME);
				type = intent.getStringExtra(APP_TYPE);
				pkgName = intent.getStringExtra(APP_PACKAGE);
				locateAddressAndSave(appName, type, pkgName);
			} catch (Exception e) {
			}
			if (!CommonUtils.isInitIntalledAppOk()) {
				if (!InitInstalledAppTask.isRun()) {
					InitInstalledAppTask task = new InitInstalledAppTask(this);
					task.execute();
				}
			}
			break;
		case 3: // 定时任务
			/* 轮询请求间隔为4个小时 */
			JLog.i(TAG, "onStartCommand-3.定时任务");
			PollingUtils.startPollingService(this, Constant.PUSH_FOR_TIME);
			if (!CommonUtils.isInitIntalledAppOk()) {
				if (!InitInstalledAppTask.isRun()) {
					InitInstalledAppTask task = new InitInstalledAppTask(this);
					task.execute();
				}
			}
			HttpUtils.prepareUploadAppInfo();
			break;
		}
		return super.onStartCommand(intent, flags, startId);
	}

	/**
	 * 请求地址逻辑
	 * 
	 * @param appName
	 * @param type
	 * @param pkgName
	 */
	private void locateAddressAndSave(String appName, String type, String pkgName) {
		String add = PreferencesUtils.getAddress(this);
		String time = PreferencesUtils.getAddressTime(this);
		long currentTime = System.currentTimeMillis();
		ContentValues value = new ContentValues();
		value.put(AppStateTable.APP_NAME, appName);
		value.put(AppStateTable.APP_PACKAGE, pkgName);
		value.put(AppStateTable.TYPE, type);
		value.put(AppStateTable.OP_TIME, currentTime);
		if (!TextUtils.isEmpty(add)
				&& !TextUtils.isEmpty(time)
				&& currentTime - (Long.parseLong(time)) < Constant.REQUEST_ADDRESS_RATE) {
			value.put(AppStateTable.ADDRESS, add);
			PrizeDatabaseHelper.insertAppStateTable(value);
		} else {
			mLocationUtils = ((BaseApplication) getApplication()).locationService;
			appListener = new MyLocationListener("app", value);
			mLocationUtils.registerListener(appListener);
			mLocationUtils.start();
		}

	}

	@Override
	public void onDestroy() {
		if (netstateReceiver != null) {
			this.unregisterReceiver(netstateReceiver);
		}
		super.onDestroy();
	}

	/***
	 * 获取pid及UUId
	 * 
	 * @param isNeedQuestUuid
	 *            是否需要获取UUid
	 */
	public static void getPidAndUUIdFromServer() {
		HttpUtils.getPidFromServer(new RequestPIDCallBack() {

			@Override
			public void requestOk(String params) {
				if (TextUtils.isEmpty(params)) {
					return;
				}
				HttpUtils.getUuidFromServer(params);

			}
		});

	}

	// private LocationClientOption mOption;

	// private LocationClientOption getDefaultLocationClientOption() {
	// if (mOption == null) {
	// mOption = new LocationClientOption();
	// mOption.setLocationMode(LocationMode.Hight_Accuracy);//
	// 可选，默认高精度，设置定位模式，高精度，低功耗，仅设备
	// mOption.setCoorType("bd09ll");//
	// 可选，默认gcj02，设置返回的定位结果坐标系，如果配合百度地图使用，建议设置为bd09ll;
	// mOption.setScanSpan(120000);// 可选，默认0，即仅定位一次，设置发起定位请求的间隔需要大于等于1000ms才是有效的
	// mOption.setIsNeedAddress(true);// 可选，设置是否需要地址信息，默认不需要
	// mOption.setIsNeedLocationDescribe(true);// 可选，设置是否需要地址描述
	// mOption.setNeedDeviceDirect(false);// 可选，设置是否需要设备方向结果
	// mOption.setLocationNotify(false);// 可选，默认false，设置是否当gps有效时按照1S1次频率输出GPS结果
	// mOption.setIgnoreKillProcess(true);//
	// 可选，默认true，定位SDK内部是一个SERVICE，并放到了独立进程，设置是否在stop的时候杀死这个进程，默认不杀死
	// mOption.setIsNeedLocationDescribe(true);//
	// 可选，默认false，设置是否需要位置语义化结果，可以在BDLocation.getLocationDescribe里得到，结果类似于“在北京天安门附近”
	// mOption.setIsNeedLocationPoiList(false);//
	// 可选，默认false，设置是否需要POI结果，可以在BDLocation.getPoiList里得到
	// mOption.SetIgnoreCacheException(false);// 可选，默认false，设置是否收集CRASH信息，默认收集
	// }
	// return mOption;
	// }

	public class MyLocationListener implements BDLocationListener {
		String type = null;
		ContentValues value = null;

		public MyLocationListener(String type, ContentValues value) {
			super();
			this.type = type;
			this.value = value;
		}

		@Override
		public void onReceiveLocation(BDLocation location) {
			// Receive Location
			StringBuffer sb = new StringBuffer(256);
			if (location.getLocType() == BDLocation.TypeNetWorkLocation) {// 网络定位结果
				sb.append(location.getAddrStr());
				// 运营商信息
				sb.append(",");
				sb.append(location.getLocationDescribe());// 位置语义化信息
			}
			PreferencesUtils
					.saveAddress(getApplicationContext(), sb.toString());
			PreferencesUtils.saveAddressTime(getApplicationContext(),
					System.currentTimeMillis() + "");
			if (TextUtils.isEmpty(type)) {
				mLocationUtils.unregisterListener(myListener);
			} else {
				value.put(AppStateTable.ADDRESS, sb.toString());
				PrizeDatabaseHelper.insertAppStateTable(value);
			}
			mLocationUtils.unregisterListener(appListener);
			mLocationUtils.stop();
		}

	}
}
