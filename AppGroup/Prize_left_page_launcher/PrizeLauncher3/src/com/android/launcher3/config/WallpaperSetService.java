package com.android.launcher3.config;

import com.android.launcher3.Utilities;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;

public class WallpaperSetService extends Service {

	@Override
	public IBinder onBind(Intent arg0) {
		return null;
	}

	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		super.onCreate();
		
	}

	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		System.exit(0);
	}

	@Override
	@Deprecated
	public void onStart(Intent intent, int startId) {
		// TODO Auto-generated method stub
		super.onStart(intent, startId);
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		// TODO Auto-generated method stub
		Utilities.setWallpaper(intent, this);
		return super.onStartCommand(intent, flags, startId);
	}

	@Override
	public boolean onUnbind(Intent intent) {
		// TODO Auto-generated method stub
		return super.onUnbind(intent);
	}
	


	class ServiceBinder extends Binder {
		public WallpaperSetService getService() {
			return WallpaperSetService.this;
		}
	}

}
