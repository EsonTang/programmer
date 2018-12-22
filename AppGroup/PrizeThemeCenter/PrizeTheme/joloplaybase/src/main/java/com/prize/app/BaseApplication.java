package com.prize.app;

import android.app.Application;
import android.content.ComponentName;
import android.content.Context;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.text.TextUtils;

import com.prize.app.beans.ClientInfo;
import com.prize.app.util.DataStoreUtils;

public class BaseApplication extends Application implements ServiceConnection {
	private static String TAG = BaseApplication.class.getSimpleName();
	// 记录当前 Context
	public static Context curContext;

	public static Handler handler = new Handler();

	// private static Context appContext;

	/** 主线程ID */
	protected static int mMainThreadId = -1;
	/** 主线程Handler */
	private static Handler mMainThreadHandler;
	/** 主线程Looper */
	private static Looper mMainLooper;

	/** 系统版和三方版切换开关，系统版为false，三方版为true */
	public static boolean isThird = false;
	/** 酷赛版和酷比版切换开关，酷赛版为true，酷比版为false */
	public static boolean isCoosea = true;
	/** 新旧签名开关，新签名true，旧签名false */
	public static boolean isNewSign = true;
	/**
	 * Global request queue for Volley
	 */
//	private static RequestQueue mRequestQueue;
	public static boolean isOnCreate = false;

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

	public static boolean isDownloadWIFIOnly() {
		String wifiSettingString = DataStoreUtils
				.readLocalInfo(DataStoreUtils.DOWNLOAD_WIFI_ONLY);
		if (TextUtils.isEmpty(wifiSettingString)) {
			return true;
		}
		if (null != wifiSettingString) {
			return wifiSettingString
					.equals(DataStoreUtils.DOWNLOAD_WIFI_ONLY_ENABLE);
		}
		return false;
	}

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

	/**
	 * @return The Volley Request queue, the queue will be created if it is null
	 */
//	public static RequestQueue getRequestQueue() {
//		// lazy initialize the request queue, the queue instance will be
//		// created when it is accessed for the first time
//		if (mRequestQueue == null) {
//			mRequestQueue = Volley.newRequestQueue(curContext);
//		}
//
//		return mRequestQueue;
//	}

	/**
	 * Adds the specified request to the global queue, if tag is specified then
	 * it is used else Default TAG is used.
	 * 
	 * @param req
	 * @param tag
	 */
//	public static <T> void addToRequestQueue(Request<T> req, Object tag) {
//		// set the default tag if tag is empty
//		req.setShouldCache(false);
//		req.setTag(tag == null ? TAG : tag);
//		req.setRetryPolicy(new DefaultRetryPolicy(10000, 0,
//				DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
//		getRequestQueue().add(req);
//	}
//

	/**
	 * Adds the specified request to the global queue using the Default TAG.
	 * 
	 * @param req
	 * @param tag
	 */
//	public static <T> void addToRequestQueue(Request<T> req) {
//		// set the default tag if tag is empty
//		req.setTag(TAG);
//		req.setRetryPolicy(new DefaultRetryPolicy(10000, 0,
//				DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
//		getRequestQueue().add(req);
//	}

	/**
	 * Cancels all pending requests by the specified TAG, it is important to
	 * specify a TAG so that the pending/ongoing requests can be cancelled.
	 * 
	 * @param
	 */
//	public static void cancelPendingRequests(Object tag) {
//		JLog.i(TAG, "cancelPendingRequests(Object tag)--tag:" + tag);
//		if (mRequestQueue != null) {
//			mRequestQueue.cancelAll(tag);
//		}
//	}

	@Override
	public void onServiceConnected(ComponentName name, IBinder service) {

	}

	@Override
	public void onServiceDisconnected(ComponentName name) {

	}

}