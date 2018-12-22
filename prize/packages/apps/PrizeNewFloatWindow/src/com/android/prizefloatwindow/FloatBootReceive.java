package com.android.prizefloatwindow;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.provider.Settings;
import android.util.Log;

public class FloatBootReceive extends BroadcastReceiver {

	public static final String PRIZE_FLOAT = "android.intent.action.PRIZE_NEW_FLOAT" ; 
	public static final String BOOT_COMPLETED = "android.intent.action.BOOT_COMPLETED";
	
	@Override
	public void onReceive(Context context, Intent intent) {
		Log.d("snail_mafei", "--------FloatBootReceive-----onReceive=="+intent.getAction());
		if (intent.getAction().equals(BOOT_COMPLETED) || intent.getAction().equals(PRIZE_FLOAT) ) {
			boolean isShow = Settings.System.getInt(context.getContentResolver(), Settings.System.PRIZE_NEW_FLOAT_WINDOW, 0) == 1;
			Log.d("snail_mafei", "--------FloatBootReceive-----isShow=="+isShow);
			if (isShow) {
				if(!ArcTipViewController.getInstance().isFloatShow()){
					Intent iFWService = new Intent(context,FloatWindowService.class);
					context.getApplicationContext().startService(iFWService);
				}
			}else {
				if(ArcTipViewController.getInstance().isFloatShow()){
					ArcTipViewController.getInstance().remove();
				}
				Intent iFWService = new Intent(context,FloatWindowService.class);
				context.stopService(iFWService);
			}
		}else if (intent.getAction().equals("android.intent.action.PRIZEHIDETEMP")) {
			if(ArcTipViewController.getInstance().isFloatShow()){
				ArcTipViewController.getInstance().updatePosMovetoEdge();
				ArcTipViewController.getInstance().remove();
			}
			Intent iFWService = new Intent(context,FloatWindowService.class);
			context.stopService(iFWService);
		}
	}
}
