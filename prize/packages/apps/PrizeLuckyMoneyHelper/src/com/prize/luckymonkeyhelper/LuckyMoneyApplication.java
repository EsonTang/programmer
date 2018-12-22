package com.prize.luckymonkeyhelper;

import android.app.Application;
import android.content.ComponentName;
import android.content.Intent;
import android.provider.Settings;
import android.util.Log;

public class LuckyMoneyApplication extends Application {
	private static final String TAG = "LuckyMoneyApplication";
	public static final boolean ENABLE_AS_DEFAULT = true;
	
	@Override
	public void onCreate() {
		Log.d(TAG, "onCreate(). ");
		
		int value = Settings.System.getInt(getContentResolver(), 
				LuckyMoneyHelperService.ENABLE_LUCKY_MONEY_DATA_ITEM, -1);
		if(value == -1) {
			int defaultValue = ENABLE_AS_DEFAULT ? 1 : 0;
			//Not defined. set default value.
			Settings.System.putInt(getContentResolver(), 
					LuckyMoneyHelperService.ENABLE_LUCKY_MONEY_DATA_ITEM, 
					defaultValue);
			Log.d(TAG, "Use our predefined value. v=" + defaultValue);		
			value = defaultValue;			
		}
		
		Log.d(TAG, "Enabled=" + value);		
		if(value == 1) {
			Intent service = new Intent();
			service.setClass(this, LuckyMoneyHelperService.class);		
			ComponentName cn = null;			
			cn = startService(service);
			Log.d(TAG, "start lucky money helper service. cn=" + cn);			
		}		
		
	}
	
}
