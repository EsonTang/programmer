package com.android.contacts.common.prize;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.BatteryManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

public class PrizeBatteryBroadcastReciver extends BroadcastReceiver {

	public static boolean isBatteryLow = false; 
	
	@Override
	public void onReceive(Context context, Intent intent) {
		Bundle extras = intent.getExtras();
        int level = extras.getInt(BatteryManager.EXTRA_LEVEL,0);
        if(level <= 15){
        	isBatteryLow = true;
        }else{
        	isBatteryLow = false;
        }
	}
}
