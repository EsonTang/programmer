package com.prize.weather.util;

import com.prize.weather.framework.FrameApplication;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.NetworkInfo.State;

/**
 * 
 * @author wangzhong
 * 
 */
public class NetworkUtils {
	
	private static ConnectivityManager getConnectivityManager() {
		ConnectivityManager conManager = (ConnectivityManager) 
				FrameApplication.getInstance().getApplicationContext().
				getSystemService(Context.CONNECTIVITY_SERVICE);
		return conManager;
	}
	
	/**
	 * Network state(Contains wifi, 3G and more).
	 * @return
	 */
	public static boolean isNetWorkActive() {
		NetworkInfo networkInfo = getConnectivityManager().
				getActiveNetworkInfo();
		if (networkInfo != null && 
				networkInfo.isAvailable() && 
				networkInfo.isConnected()) {
			return true;
		} else {
			return false;
		}
	}
	
	/**
	 * 3G state.
	 * @return
	 */
	public static boolean is3GActive() {
		State mobileState = getConnectivityManager().
				getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState();
		return judgeState(mobileState);
	}

	/**
	 * Wifi state.
	 * @return
	 */
	public static boolean isWifiActive() {
		State wifiState = getConnectivityManager().
				getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState();
		return judgeState(wifiState);
	}
	
	/**
	 * @return true means connection. Conversely, no!
	 */
	private static boolean judgeState(State state) {
		if (state == State.CONNECTED || 
				state == State.CONNECTING) {
			return true;
		} else {
			return false;
		}
	}
	
}
