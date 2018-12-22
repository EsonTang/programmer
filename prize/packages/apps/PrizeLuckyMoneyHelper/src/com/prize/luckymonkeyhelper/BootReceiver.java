package com.prize.luckymonkeyhelper;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.provider.Settings;
import android.util.Log;

public class BootReceiver extends BroadcastReceiver {
	private static final String TAG = "BootReceiver";

	@Override
	public void onReceive(Context context, Intent intent) {
		Log.d(TAG, "Receive BOOT_COMPLETED. intent=" + intent);			
		int value = Settings.System.getInt(context.getContentResolver(), 
							LuckyMoneyHelperService.ENABLE_LUCKY_MONEY_DATA_ITEM, -1);
		
		if(value == -1) {
			int defaultValue = LuckyMoneyApplication.ENABLE_AS_DEFAULT ? 1 : 0;
			//Not defined. set default value.
			Settings.System.putInt(context.getContentResolver(), 
					LuckyMoneyHelperService.ENABLE_LUCKY_MONEY_DATA_ITEM, 
					defaultValue);
			Log.d(TAG, "Use our predefined value. v=" + defaultValue);		
			value = defaultValue;
		}
		
		Log.d(TAG, "enabled=" + value);		
		if(value == 1) {
			Intent service = new Intent();
			service.setClass(context, LuckyMoneyHelperService.class);		
			ComponentName cn = null;			
			cn = context.startService(service);
			Log.d(TAG, "start lucky money helper service. cn=" + cn);			
		}

	}

}
