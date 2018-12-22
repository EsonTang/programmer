package com.prize.lockscreen.receiver;

import com.prize.lockscreen.utils.LogUtil;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class HomeWatcherReceiver extends BroadcastReceiver {
	private static final String TAG = HomeWatcherReceiver.class.getName();
	private static final String SYSTEM_DIALOG_REASON_KEY = "reason";
	private static final String SYSTEM_DIALOG_REASON_RECENT_APPS = "recentapps";
	private static final String SYSTEM_DIALOG_REASON_HOME_KEY = "homekey";

	@Override
	public void onReceive(Context context, Intent intent) {
		String action = intent.getAction();
		LogUtil.i(TAG, "onReceive: action: " + action);
		if (action.equals(Intent.ACTION_CLOSE_SYSTEM_DIALOGS)) {
			String reason = intent.getStringExtra(SYSTEM_DIALOG_REASON_KEY);
			if (SYSTEM_DIALOG_REASON_HOME_KEY.equals(reason)) {
				// 短按Home键
				LogUtil.i(TAG, "------->short press home key");

			} else if (SYSTEM_DIALOG_REASON_RECENT_APPS.equals(reason)) {
				// 长按Home键 或者 activity切换键
				LogUtil.i(TAG, "------>long press home key");

			}
		}
		
	}

}
