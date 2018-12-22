package com.prize.runoldtest.receiver;

import com.prize.runoldtest.util.DataUtil;
import com.prize.runoldtest.util.UsbService;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class Broadcast extends	BroadcastReceiver{
	public static boolean ActivityisFinish=true;
    
	@Override
	public void onReceive(Context context, Intent intent) {    
        DataUtil.destoryActivity();
        if(!ActivityisFinish){
        	Intent  show = new Intent(context, UsbService.class);
            context.startService(show);
        }
	}

    public  static void SteTag(boolean isfinish){
    	ActivityisFinish=isfinish;
    }
 
   
}
