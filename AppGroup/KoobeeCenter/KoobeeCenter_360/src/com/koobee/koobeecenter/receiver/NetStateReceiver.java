package com.koobee.koobeecenter.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.NetworkInfo.State;

import com.prize.app.beans.ClientInfo;

public class NetStateReceiver extends BroadcastReceiver {
	private static State currentWifi = State.UNKNOWN;
	private static State currentMobile = State.UNKNOWN;

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

//		if (MainActivity.thisActivity == null) {
//			// 没进入平台，不做恢复下载操作
//			return;
//		}
//		if (wifi == State.CONNECTED) {
//			// 打开所有下载
//			 AppManagerCenter.continueAllDownload();
//		} else if (mobile == State.CONNECTED) {
//			if (!BaseApplication.isDownloadWIFIOnly()) {
//				 AppManagerCenter.continueAllDownload();
//			}
//		} else {
//			if (BaseApplication.isDownloadWIFIOnly()
//					&& AppManagerCenter.hasDownloadingApp()) {
//				// 断开所有下载
//				AppManagerCenter.pauseAllDownload();
//			}
//		}
	}

}