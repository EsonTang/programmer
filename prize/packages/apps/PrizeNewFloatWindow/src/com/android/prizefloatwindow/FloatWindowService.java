package com.android.prizefloatwindow;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import com.android.prizefloatwindow.appmenu.AppMenuActivity;
import com.android.prizefloatwindow.appmenu.MyAppInfo;
import com.android.prizefloatwindow.utils.ActionUtils;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

public class FloatWindowService extends Service {

	/**
	 * Regularly check the current environment (desktop environment, according
	 * to the desktop environment hidden) is displayed or remove suspended
	 * window.
	 */
	private Timer timer;

	/**
	 * A floating window is operated west the show and hide.
	 */
	private Handler handler = new Handler();

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		
		if (null == timer) {
			timer = new Timer();
			timer.scheduleAtFixedRate(new MonitorTask(), 0, 20000);
		}
		return super.onStartCommand(intent, START_REDELIVER_INTENT, startId);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();  
		timer.cancel();
		timer = null;

	}

	private class MonitorTask extends TimerTask {

		@Override
		public void run() {
			if(!ArcTipViewController.getInstance().isFloatShow()){
				handler.post(new Runnable() {
					@Override
					public void run() {
						 ArcTipViewController.getInstance().show();
					}
				});
			}
		}
	}
	

}
