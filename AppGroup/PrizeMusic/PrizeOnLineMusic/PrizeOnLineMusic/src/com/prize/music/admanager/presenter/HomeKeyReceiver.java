package com.prize.music.admanager.presenter;

import com.prize.music.admanager.AdSplash;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
/**
 * 监听home的状态
 * @author Administrator
 *
 */
public class HomeKeyReceiver extends BroadcastReceiver {
	private static final String TAG = "huang-HomeKeyReceiver";
	private static final String SYSTEM_DIALOG_REASON_KEY = "reason";
	private static final String SYSTEM_DIALOG_REASON_RECENT_APPS = "recentapps";
	private static final String SYSTEM_DIALOG_REASON_HOME_KEY = "homekey";
	private static final String SYSTEM_DIALOG_REASON_LOCK = "lock";
	private static final String SYSTEM_DIALOG_REASON_ASSIST = "assist";

	@Override
	public void onReceive(Context context, Intent intent) {
		String action = intent.getAction();
		Log.i(TAG, "onReceive: action: " + action);
		if (action.equals(Intent.ACTION_CLOSE_SYSTEM_DIALOGS)) {
			// android.intent.action.CLOSE_SYSTEM_DIALOGS
			String reason = intent.getStringExtra(SYSTEM_DIALOG_REASON_KEY);
			Log.i(TAG, "reason: " + reason);

			if (SYSTEM_DIALOG_REASON_HOME_KEY.equals(reason)) {
				// 短按Home键
				Log.i(TAG, "homekey");
				AdSplash.getInstance(context).onDestory();

			} else if (SYSTEM_DIALOG_REASON_RECENT_APPS.equals(reason)) {
				// 长按Home键 或者 activity切换键
				Log.i(TAG, "long press home key or activity switch");

			} else if (SYSTEM_DIALOG_REASON_LOCK.equals(reason)) {
				// 锁屏
				Log.i(TAG, "lock");
			} else if (SYSTEM_DIALOG_REASON_ASSIST.equals(reason)) {
				// samsung 长按Home键
				Log.i(TAG, "assist");
			}

		}
	}

}