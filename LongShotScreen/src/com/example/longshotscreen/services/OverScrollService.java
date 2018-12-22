package com.example.longshotscreen.services;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.provider.Settings;
import android.provider.Settings.System;
import com.example.longshotscreen.utils.Log;

public class OverScrollService extends Service {
	public IBinder onBind(Intent paramIntent) {
		return null;
	}

	public void onCreate() {
		super.onCreate();
	}

	public void onDestroy() {
		super.onDestroy();
	}

	public int onStartCommand(Intent intent, int flag, int startId) {
		if (intent != null) {
			if (intent.getBooleanExtra("isReachBottom", true))
			{
				com.example.longshotscreen.ui.ScrollShotView.mIsReachBottom = true;
				Settings.System.putInt(getContentResolver(),
						"supershot_overscroll", 1);
			}
		}

		Log.i("connorlin", "intent = " + intent.getStringExtra("from"));
		stopSelf();
		com.example.longshotscreen.ui.ScrollShotView.mIsFromScrollView = true;
		return super.onStartCommand(intent, flag, startId);
	}
}
