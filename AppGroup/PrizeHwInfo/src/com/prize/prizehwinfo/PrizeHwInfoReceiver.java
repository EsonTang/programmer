package com.prize.prizehwinfo;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class PrizeHwInfoReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context arg0, Intent arg1) {
		if ((arg1.getAction().equals("com.prize.HARDWAREINFO"))) {
			Intent intent = new Intent();
			intent.setClass(arg0, PrizeHwInfo.class);
			intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			arg0.startActivity(intent);
		}
	}
}
