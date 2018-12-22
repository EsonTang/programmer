package com.prize.prizeappoutad.observer;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

/**
 * 屏幕监控
 * 
 * @author huangchangguo 2016.11.8
 * 
 */
public class ScreenObserver {
	private Context mContext;
	private ScreenBroadcastReceiver mScreenReceiver;
	private ScreenStateListener mScreenStateListener;

	public ScreenObserver(Context context) {
		mContext = context;
		mScreenReceiver = new ScreenBroadcastReceiver();
	}

	public void startScreenObserver(ScreenStateListener listener) {
		mScreenStateListener = listener;
		registerListener();
	//	getScreenState();
	}

	public void shutdownObserver() {
		unregisterListener();
	}

//	/**
//	 * 获取screen状态
//	 */
//	@SuppressLint("NewApi")
//	private void getScreenState() {
//		if (mContext == null) {
//			return;
//		}
//
//		PowerManager manager = (PowerManager) mContext
//				.getSystemService(Context.POWER_SERVICE);
//		if (manager.isScreenOn()) {
//			if (mScreenStateListener != null) {
//				mScreenStateListener.onScreenOn();
//			}
//		} else {
//			if (mScreenStateListener != null) {
//				mScreenStateListener.onScreenOff();
//			}
//		}
//	}

	private void registerListener() {
		if (mContext != null) {
			IntentFilter filter = new IntentFilter();
			filter.addAction(Intent.ACTION_SCREEN_ON);
			filter.addAction(Intent.ACTION_SCREEN_OFF);
			filter.addAction(Intent.ACTION_USER_PRESENT);
			mContext.registerReceiver(mScreenReceiver, filter);
		}
	}

	private void unregisterListener() {
		if (mContext != null)
			mContext.unregisterReceiver(mScreenReceiver);
	}

	private class ScreenBroadcastReceiver extends BroadcastReceiver {
		private String action = null;

		@Override
		public void onReceive(Context context, Intent intent) {
			action = intent.getAction();
			if (Intent.ACTION_SCREEN_ON.equals(action)) { // 开屏
				mScreenStateListener.onScreenOn();
			} else if (Intent.ACTION_SCREEN_OFF.equals(action)) { // 锁屏
				mScreenStateListener.onScreenOff();
			} else if (Intent.ACTION_USER_PRESENT.equals(action)) { // 解锁
				mScreenStateListener.onUserPresent();
			}
		}
	}

	public interface ScreenStateListener {
		void onScreenOn();

		void onScreenOff();

		void onUserPresent();
	}
}
