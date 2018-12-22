package com.pr.scuritycenter.aresengine;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * 
 * @author wangzhong
 *
 */
public class InterceptBootReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context arg0, Intent arg1) {
		String action = arg1.getAction();
		if (Intent.ACTION_BOOT_COMPLETED.equals(action)) {
			Intent iInterceptService = new Intent(arg0, InterceptService.class);
			arg0.startService(iInterceptService);
		}
	}

}
