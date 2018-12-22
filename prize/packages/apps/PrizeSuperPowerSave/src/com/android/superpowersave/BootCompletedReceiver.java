package com.android.superpowersave;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.SystemProperties;
import android.util.Log;

import com.mediatek.common.prizeoption.PrizeOption;

public class BootCompletedReceiver extends BroadcastReceiver {
	private static final String BOOT_ACTION = "android.intent.action.BOOT_COMPLETED";
    private static final String TAG = "BootCompletedReceiver";
	@Override
	public void onReceive(Context context, Intent intent) {
		// TODO Auto-generated method stub
		if (intent.getAction().equals(BOOT_ACTION)) {
			if (PrizeOption.PRIZE_POWER_EXTEND_MODE) {
                Log.d(TAG, "PowerExtendMode==>BootCompleteReceiver() set persist.sys.power_extend_mode to false");
                SystemProperties.set("persist.sys.power_extend_mode", "false");
			}
		}
	}
}
