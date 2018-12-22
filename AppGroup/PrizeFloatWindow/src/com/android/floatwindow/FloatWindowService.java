package com.android.floatwindow;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningTaskInfo;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.widget.Toast;
import android.util.Log;

public class FloatWindowService extends Service {
	
	/**
	 * Regularly check the current environment 
	 * (desktop environment, according to the desktop environment hidden) 
	 * is displayed or remove suspended window.
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
		if(null == timer){
			timer =new Timer();
			timer.scheduleAtFixedRate(new MonitorTask(), 0, 200);
			Log.e("test","timer:"+timer);
		}
		
		return super.onStartCommand(intent, START_REDELIVER_INTENT, startId);
	}
	
	@Override
	public void onDestroy() {
		Log.e("test","onDestroy");
		super.onDestroy();
		timer.cancel();
		timer = null;
		Log.e("test", "timerindestroy:"+timer);
		
	}
	
	
	private class MonitorTask extends TimerTask{

		@Override
		public void run() {
			if(!FloatWindowController.isWindowShowing()){
				handler.post(new Runnable(){

					@Override
					public void run() {
						FloatWindowController.creatSmallWindow(getApplicationContext());
						
					}
				});
			}
		}
	}
	
//	private String getCurrentAPPPackage(){
//		ActivityManager am = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
//		List<RunningTaskInfo> rtiList = am.getRunningTasks(1);
//		return rtiList.get(0).topActivity.getPackageName();
//		
//	}
//	
//	private List<String> getShieldingAPP(){
//		List<String> appPackageNames = new ArrayList<String>();
//		appPackageNames.add("com.android.gallery3d");
//		return appPackageNames;
//	}
//	
//	private boolean isShielding(){
//		return !getShieldingAPP().contains(getCurrentAPPPackage());
//	}
	
	
	
	
	
}
