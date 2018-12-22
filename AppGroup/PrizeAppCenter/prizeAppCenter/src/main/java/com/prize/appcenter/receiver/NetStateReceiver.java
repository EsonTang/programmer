package com.prize.appcenter.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.NetworkInfo.State;
import android.text.TextUtils;

import com.prize.app.BaseApplication;
import com.prize.app.beans.ClientInfo;
import com.prize.app.util.DataStoreUtils;
import com.prize.appcenter.callback.UpdateWatchedManager;
import com.prize.appcenter.ui.util.AIDLUtils;

/**
 * 网络监听
 */
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

		if (wifi == State.CONNECTED) {
			UpdateWatchedManager.notifyChange();
			// 打开所有下载
			if (TextUtils.isEmpty(DataStoreUtils.readLocalInfo("thisActivity"))) {
				// 没进入平台，不做恢复下载操作
				return;
			}
			AIDLUtils.continueAllDownload(context);
		} else if (mobile == State.CONNECTED) {
			UpdateWatchedManager.notifyChange();
			if (BaseApplication.isDownloadWIFIOnly()) {
				AIDLUtils.pauseAllDownload();
			}
		} else {
			// 断开所有下载
			AIDLUtils.pauseAllDownload();
		}
	}

}