package com.prize.cloudlist;

import android.content.Context;
import android.os.PowerManager;
import android.util.Log;

public class WorkWakelock
{
	private static final String TAG = CloudUtils.TAG+"-wakelock";
	private PowerManager.WakeLock mWakelock;
	private int mWakeCount = 0;
	private Context mContext;
	public void init(Context context)
	{
		mContext = context;
		if(mWakelock == null)
		{
			PowerManager pwmgr = (PowerManager)mContext.getSystemService(Context.POWER_SERVICE);		
			mWakelock = pwmgr.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,"CloudList"); 
		}
		else
		{
			mWakeCount = 0;
			if(mWakelock.isHeld())
			{				
				mWakelock.release();
			}
		}
	}
	public void release()
	{
		mWakeCount = 0;
		if(mWakelock != null && mWakelock.isHeld())
		{
			mWakelock.release();
		}
	}
	
	public void addWakeLock()
	{
	    mWakeCount ++;
	    Log.d(TAG,"addWakeLock "+mWakeCount);
	    if(!mWakelock.isHeld())
	    {
	    	Log.d(TAG,"reduceWakeLock acquire wakelock");
	    	mWakelock.acquire();
	    }
	}
	public void reduceWakeLock()
	{
		mWakeCount --;
		if(mWakeCount < 0)mWakeCount = 0;
		Log.d(TAG,"reduceWakeLock "+mWakeCount);
		if(mWakeCount == 0)
		{
			if(mWakelock.isHeld())
			{
				Log.d(TAG,"reduceWakeLock release wakelock");
				mWakelock.release();
			}
		}
	}
	
}
