package com.prize.cloudlist;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class BootCompeteReceiver extends BroadcastReceiver
{

	private static final String TAG = CloudUtils.TAG;

	@Override
	public void onReceive(Context context, Intent intent)
	{
		// TODO Auto-generated method stub
		Log.v(TAG, "onReceive action:"+intent.getAction());
		//if (intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED))
		{			

			Intent intentservice = new Intent(context, CloundListService.class);
			// intentservice.setComponent(new
			// ComponentName("com.prize.cloudlist","com.prize.cloudlist.CloundListService"));
			context.startService(intentservice);
		}
	}

}
