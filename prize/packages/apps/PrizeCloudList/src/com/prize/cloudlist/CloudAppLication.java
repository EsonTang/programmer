package com.prize.cloudlist;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Handler;
import android.util.Log;

public class CloudAppLication extends Application
{
	public static final String TAG = CloudUtils.TAG;
	private Handler mHandler = new Handler();
	private Context mContext;
	@Override
	public void onCreate()
	{
		Log.i(TAG,"CloudAppLication onCreate");
		super.onCreate();
		mContext = this;
		mHandler.post(new Runnable()
		{
			
			@Override
			public void run()
			{
				Intent intent = new Intent(mContext,CloundListService.class);
				startService(intent);				
			}
		});		
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig)
	{
		Log.i(TAG,"CloudAppLication onConfigurationChanged");
		super.onConfigurationChanged(newConfig);
	}

}
