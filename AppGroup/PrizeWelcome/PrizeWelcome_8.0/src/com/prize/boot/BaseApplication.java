package com.prize.boot;

import org.xutils.x;

import com.prize.boot.util.ClientInfo;

import android.app.Application;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;

public class BaseApplication extends Application {
	// 记录当前 Context
	public static Context curContext;
	
	// WIFI设置 wifi环境下下载
	public static final String DOWNLOAD_WIFI_ONLY = "y";
	public static final String DOWNLOAD_WIFI_ONLY_ENABLE = "1";
	public static final String DOWNLOAD_WIFI_ONLY_UNABLE = "0";

	public static Handler handler = new Handler();

	// private static Context appContext;

	/** 主线程ID */
	private static int mMainThreadId = -1;
	/** 主线程Handler */
	private static Handler mMainThreadHandler;
	/** 主线程Looper */
	private static Looper mMainLooper;

	/** 系统版和三方版切换开关，系统版为false，三方版为true */
	public static boolean isThird = false;
	/** 酷赛版和酷比版切换开关，酷赛版为true，酷比版为false */
	public static boolean isCoosea = false;

	@Override
	public void onCreate() {
		curContext = this;
		mMainThreadId = android.os.Process.myTid();
		mMainThreadHandler = new Handler();
		mMainLooper = getMainLooper();
		super.onCreate();
		// appContext = getApplicationContext();
	}

	/**
	 * 初始化全部所需的数据库
	 * 
	 * @return void
	 */
	public static void initBaseApp() {
		ClientInfo.initClientInfo();
	}

/*	public static boolean isDownloadWIFIOnly() {
		String wifiSettingString = DataStoreUtils.readLocalInfo(DataStoreUtils.DOWNLOAD_WIFI_ONLY);
		if (TextUtils.isEmpty(wifiSettingString)) {
			return true;
		}
		if (null != wifiSettingString) {
			return wifiSettingString.equals(DataStoreUtils.DOWNLOAD_WIFI_ONLY_ENABLE);
		}
		return false;
	}
*/
	/** 获取主线程ID */
	public static int getMainThreadId() {
		return mMainThreadId;
	}

	/** 获取主线程的handler */
	public static Handler getMainThreadHandler() {
		return mMainThreadHandler;
	}

	/** 获取主线程的looper */
	public static Looper getMainThreadLooper() {
		return mMainLooper;
	}
}
