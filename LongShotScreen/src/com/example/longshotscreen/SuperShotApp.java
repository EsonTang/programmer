package com.example.longshotscreen;

import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import com.example.longshotscreen.utils.Log;

public class SuperShotApp extends Application
{
	private static SuperShotApp mSuperShotApp;
	private Context mContext;
	private GlobalReceiver mGlobalReceiver;
	public static boolean isOnClickMove = false;
	public static boolean mMoveActionDown = false;
	
	public void onCreate()
	{
		super.onCreate();
		mSuperShotApp = this;
		this.mContext = getApplicationContext();
		((TelephonyManager)getSystemService("phone")).listen(new TeleListener(), PhoneStateListener.LISTEN_CALL_STATE);
		this.mGlobalReceiver = new GlobalReceiver();
		this.mGlobalReceiver.register();
	}

	public void onTerminate()
	{
		super.onTerminate();
		if (this.mGlobalReceiver != null)
		{
			this.mGlobalReceiver.unregister();
			this.mGlobalReceiver = null;
		}
		this.mContext.stopService(new Intent("com.freeme.supershot.MainFloatMenu"));
		this.mContext.stopService(new Intent("com.freeme.supershot.SuperShot.OverScroll"));
		this.mContext.stopService(new Intent("com.freeme.supershot.ScrollShot"));
		this.mContext.stopService(new Intent("com.freeme.supershot.FunnyShot"));
		this.mContext.stopService(new Intent("android.intent.action.ScreenRecorder"));
	}

	public class GlobalReceiver extends BroadcastReceiver
	{
		public GlobalReceiver()
		{
		}

		public void onReceive(Context paramContext, Intent paramIntent)
		{
			paramIntent.getAction();
			SuperShotApp.this.onTerminate();
		}

		public void register()
		{
			IntentFilter localIntentFilter = new IntentFilter();
			localIntentFilter.addAction("android.intent.action.FONT_CHANGED");
			localIntentFilter.addAction("android.intent.action.SKIN_CHANGED");
			//localIntentFilter.addAction("com.android.deskclock.ALARM_ALERT");
			SuperShotApp.this.mContext.registerReceiver(this, localIntentFilter);
		}

		public void unregister()
		{
			SuperShotApp.this.mContext.unregisterReceiver(this);
		}
	}

	class TeleListener extends PhoneStateListener
	{
		TeleListener()
		{
		}

		public void onCallStateChanged(int paramInt, String paramString)
		{
			super.onCallStateChanged(paramInt, paramString);
			switch (paramInt)
			{
			case TelephonyManager.CALL_STATE_IDLE:
				break;
			case TelephonyManager.CALL_STATE_RINGING:
				onTerminate();
				break;
			case TelephonyManager.CALL_STATE_OFFHOOK:
				break;
			default:
				break;
			}
		}
	}
}
