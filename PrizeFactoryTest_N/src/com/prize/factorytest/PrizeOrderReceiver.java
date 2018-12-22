package com.prize.factorytest;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class PrizeOrderReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		
		if (intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) {
			FactoryTestApplication app=(FactoryTestApplication) context.getApplicationContext();
			boolean bReboot=app.getSharePref().getValue("reboot_selected").equals("1");
			if(bReboot){
				Intent intentAtaInfo = new Intent(context,AgingTestActivity.class);
				intentAtaInfo.putExtra("reboot", true);
	    		intentAtaInfo.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
	    		context.startActivity(intentAtaInfo);
			}
        }else{
        	String number = intent.getExtras().getString("input");
            if (number.equals("*#8804#")) { 
        		Intent intentFactory = new Intent(context,PrizeFactoryTestActivity.class);
				intentFactory.putExtra("isAutoTest", true);
        		intentFactory.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        		context.startActivity(intentFactory);
        	}
    		else if(number.equals("*#8801#")){
    			Intent intentSnInfo = new Intent(context,PrizeSnInfo.class);
        		intentSnInfo.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        		context.startActivity(intentSnInfo);
    		}
    		else if(number.equals("*#8818#")){
    			Intent intentAtaInfo = new Intent(context,PrizeAtaInfo.class);
        		intentAtaInfo.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        		context.startActivity(intentAtaInfo);
    		}
    		else if(number.equals("*#8805#")){
    			Intent intentAtaInfo = new Intent(context,AgingTestActivity.class);
        		intentAtaInfo.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        		context.startActivity(intentAtaInfo);
    		}
        }
	}
}
