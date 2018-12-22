package com.prize.prizeappoutad.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.NetworkInfo.State;

import com.baidu.android.pushservice.PushConstants;
import com.baidu.android.pushservice.PushManager;
import com.prize.prizeappoutad.bean.ClientInfo;
import com.prize.prizeappoutad.constants.Constants;
import com.prize.prizeappoutad.utils.JLog;

public class NetStateReceiver extends BroadcastReceiver {
	private static final String TAG = "huang-NetStateReceiver";
	private static State currentWifi = State.UNKNOWN;
	private static State currentMobile = State.UNKNOWN;
	private static Boolean isNetEnable = false;

	@Override
	public void onReceive(Context context, Intent intent) {
		// 更新网络类型
		ClientInfo.getAPNType(context);

		// 判断网络状态
		ConnectivityManager manager = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo info = manager
				.getNetworkInfo(ConnectivityManager.TYPE_WIFI);

		State wifi = (info != null) ? info.getState() : State.DISCONNECTED;
		info = manager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);

		State mobile = (info != null) ? info.getState() : State.DISCONNECTED;

		if ((currentWifi == wifi) && (currentMobile == mobile)) {
			// 两者都没变化
			return;
		} else {
			currentWifi = wifi;
			currentMobile = mobile;
		}

		// if (wifi == State.CONNECTED) {
		// } else if (mobile == State.CONNECTED) {
		// } else {
		// }

		if (ClientInfo.networkType == ClientInfo.NONET) {
			JLog.i(TAG, " Net Changed:" + isNetEnable);
			isNetEnable = true;
		} else {
			/**
			 * BUG#25181：当发送banner或插屏广告push后，由无效wifi切换至正常wifi下， 手机灭屏再亮屏时接收不到广告
			 * fixed by huangchangguo Strategy:重新绑定百度推送
			 * *****************start******************
			 */
			/*
			 * JLog.i(TAG, " Net Changed:" + isNetEnable); if (isNetEnable) {
			 * PushManager.startWork(context, PushConstants.LOGIN_TYPE_API_KEY,
			 * Constants.PUSH_API_KEY.trim()); } isNetEnable = false;
			 */
			/***************** end ******************/
		}
	}
}