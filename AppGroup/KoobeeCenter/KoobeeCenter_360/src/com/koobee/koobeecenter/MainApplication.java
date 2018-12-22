package com.koobee.koobeecenter;


import java.util.List;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.content.Context;
import android.content.IntentFilter;
import android.net.ConnectivityManager;

import com.koobee.koobeecenter.receiver.NetStateReceiver;
import com.koobee.koobeecenter.receiver.ScreenListener;
import com.koobee.koobeecenter.receiver.ScreenListener.ScreenStateListener;
import com.prize.app.BaseApplication;
import com.prize.app.util.JLog;
import com.prize.custmerxutils.XExtends;

public class MainApplication extends BaseApplication {
	
	private static NetStateReceiver netstateReceiver;
	@Override
	public void onCreate() {
		super.onCreate();
		XExtends.Ext.init(this);
//		String processName = getProcessName(this, mMainThreadId);
//		JLog.i("MainApplication", "processName=" + processName);
//		if (processName != null) {
//			boolean defaultProcess = processName.equals(getPackageName());
//			if (defaultProcess) {
//				registerScreenLister();
//				regisetReceiver();
//			}
//		}
		registerScreenLister();
		regisetReceiver();
		JLog.i("MainApplication", "MainApplication onCreate()");
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

			
	/**
	 * 方法描述：注册锁屏监听者
	 */
	private void registerScreenLister() {
		JLog.i("MainApplication", "MainApplication registerScreenLister()");
		ScreenListener screenListener = new ScreenListener(this);
		screenListener.begin(new ScreenStateListener() {

			@Override
			public void onUserPresent() {

			}

			@Override
			public void onScreenOn() {
				
			}

			// 锁屏并且电量高于30%时才会调用
			@Override
			public void onScreenOff() {
				
			}

			@Override
			public void onScreenOffNoRLLevel() {
				
			}
		});
	}
	
	/**
	 * 
	 * 网络监听
	 */
	private void regisetReceiver() {
		netstateReceiver = new NetStateReceiver();
		this.registerReceiver(netstateReceiver, new IntentFilter(
				ConnectivityManager.CONNECTIVITY_ACTION));
	}
	
}
