package com.prize.weather.framework;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

/**
 * 
 * @author wangzhong
 *
 */
public abstract class BaseService extends Service implements IBDLocationFinishedListener {

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		initLocation();
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		initLocation();
		return super.onStartCommand(intent, flags, startId);
	}
	
	public void initLocation() {
		FrameApplication.getInstance().setLocationOption();
		FrameApplication.getInstance().setmIBDLocationFinishedListener(this);
	}

}
