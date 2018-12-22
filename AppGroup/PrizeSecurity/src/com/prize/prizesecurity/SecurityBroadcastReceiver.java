package com.prize.prizesecurity;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class SecurityBroadcastReceiver extends BroadcastReceiver
{
	public static final String TAG = "PureBackground";
	@Override
	public void onReceive(Context context, Intent intent)
	{
		String action = intent.getAction();
		Log.d(TAG,"onReceive action:"+action);
		if(Intent.ACTION_BOOT_COMPLETED.equals(action))
		{
			Intent intentser = new Intent();
			intentser.setComponent(new ComponentName("com.prize.prizesecurity","com.prize.prizesecurity.ClearBackgroundService"));
			context.startService(intentser);
		}
	}

}
