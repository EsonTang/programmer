package com.prize.prizesecurity;

import android.app.Application;
import android.content.ComponentName;
import android.content.Intent;
import android.content.res.Configuration;
import android.util.Log;

public class SecurityApplication extends Application
{
	public static final String TAG = "PureBackground";
	@Override
	public void onCreate()
	{
		Log.i(TAG,"SecurityApplication onCreate");
		super.onCreate();
//		Intent intent = new Intent();
//		intent.setComponent(new ComponentName("com.prize.prizesecurity","com.prize.prizesecurity.ClearbackgroundService"));
//		startService(intent);
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig)
	{
		Log.i(TAG,"SecurityApplication onConfigurationChanged");
		super.onConfigurationChanged(newConfig);
	}

}
