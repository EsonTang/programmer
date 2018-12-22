package com.goodix.service;

import com.goodix.util.L;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class ServiceStartReceiver extends BroadcastReceiver{
	private static final String INTENT_ACTION = "android.intent.action.Service_Start_Service";
	@Override
	public void onReceive(final Context context, Intent intent) {
		if(INTENT_ACTION.equals(intent.getAction())){
			Log.i("ServiceStartReceiver", "ServiceStartReceiver:" + intent.getAction());
			L.d("ServiceStartReceiver" + intent.getAction());
			Intent serviceIntent = new Intent(context, LockScreenService.class);
			context.startService(serviceIntent);
		}
	}	
}
