package com.prize.lockscreen.service;

import com.prize.lockscreen.BootActivity;

import android.app.KeyguardManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

public class StarLockService extends Service
{
	
	private static final boolean DBG = true;
	private static final String TAG = "FxLockService";
	private Intent mFxLockIntent = null;
	private KeyguardManager mKeyguardManager = null ;
	private KeyguardManager.KeyguardLock mKeyguardLock = null ;

	@Override
	public void onCreate() 
	{
		// TODO Auto-generated method stub
		super.onCreate();
		refreshInfo();
		if (DBG)Log.d(TAG, "-->onCreate()");
		mFxLockIntent = new Intent(StarLockService.this, BootActivity.class);
		mFxLockIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
	}
	
	
	
	@Override
	public void onStart(Intent intent, int startId)
	{
		// TODO Auto-generated method stub
		super.onStart(intent, startId);
		if(DBG) Log.d(TAG, "-->onStart()");
	}



	@Override
	public void onDestroy() 
	{
		// TODO Auto-generated method stub
		super.onDestroy();
		if(DBG) Log.d(TAG, "-->onDestroy()");
		//被销毁时启动自身，保持自身在后台存活
		startService(new Intent(StarLockService.this, StarLockService.class));
	}
	
	@Override
	public IBinder onBind(Intent arg0)
	{
		// TODO Auto-generated method stub
		if(DBG) Log.d(TAG, "-->onBind()");
		return null;
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		// TODO Auto-generated method stub
		if(DBG) Log.d(TAG, "-->onStartCommand()");
		return Service.START_STICKY;
	}
	
	//监听来自用户按Power键点亮点暗屏幕的广播
	private BroadcastReceiver mScreenOnOrOffReceiver = new BroadcastReceiver() {
		
		@Override
		public void onReceive(Context context, Intent intent)
		{
			if(DBG) Log.d(TAG, "mScreenOffReceiver-->" + intent.getAction());
			
			if (intent.getAction().equals("android.intent.action.SCREEN_ON")
					|| intent.getAction().equals("android.intent.action.SCREEN_OFF"))
			{
				refreshInfo();
				mKeyguardManager = (KeyguardManager) context.getSystemService(Context.KEYGUARD_SERVICE);
				mKeyguardLock = mKeyguardManager.newKeyguardLock("FxLock");
				//屏蔽手机内置的锁屏
				mKeyguardLock.disableKeyguard();
				//启动该第三方锁屏
				startActivity(mFxLockIntent);
			}	
			
			if(intent.getAction().equals("com.phicomm.hu.action.music"))
			{
				refreshInfo();
			}
		}
	};
	
	//刷新音乐播放信息
	private void refreshInfo()
	{
		setLockViewText();
	}
	
	//锁屏界面时隐藏音乐播放信息
	private void setLockViewText()
	{
		BootActivity.mStatusViewManager.mArtistView.setText(null);
	}

}