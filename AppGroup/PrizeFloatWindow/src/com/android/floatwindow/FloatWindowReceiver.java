package com.android.floatwindow;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.provider.Settings;
import android.util.Log;

public class FloatWindowReceiver extends BroadcastReceiver {

	public static final String PRIZE_FLOAT_WINDOW = "android.intent.action.PRIZE_FLOAT_WINDOW" ;
	public static final String BOOT_COMPLETED = "android.intent.action.BOOT_COMPLETED";
	public static final String SCREEN_ON = "android.intent.action.USER_PRESENT";
	
	@Override
	public void onReceive(Context context, Intent intent) {
		if (null != intent && (intent.getAction().equals(PRIZE_FLOAT_WINDOW) || intent.getAction().equals(BOOT_COMPLETED) || intent.getAction().equals(SCREEN_ON))) {
			Log.i("test", "SCREEN_ON"+intent.getAction());
			boolean isShow = Settings.System.getInt(context.getContentResolver(), Settings.System.PRIZE_FLOAT_WINDOW, 0) == 1;
			if (isShow && !FloatWindowController.isWindowShowing()) {
				FloatWindowController.startFloatWindowService(context);
			}else {
				FloatWindowController.stopFloatWindowService(context);
			}
		}
		

	}

}
