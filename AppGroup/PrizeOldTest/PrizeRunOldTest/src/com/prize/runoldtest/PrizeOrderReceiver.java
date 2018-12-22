package com.prize.runoldtest;

import com.prize.runoldtest.util.Const;
import com.prize.runoldtest.util.LogUtil;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class PrizeOrderReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		LogUtil.e("reboot start Activity.......");
		if (intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) {
			PrizeRunOldTestApplication app = (PrizeRunOldTestApplication) context.getApplicationContext();
			boolean bReboot = app.getSharePref().getValue(Const.SHARE_PREF_REBOOT_SELECTE).equals("1");
			if (bReboot) {
				Intent intentAtaInfo = new Intent(context, RebootActivity.class);
				intentAtaInfo.putExtra("reboot", true);
				intentAtaInfo.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				context.startActivity(intentAtaInfo);
			}
			boolean mDdrReboot = app.getSharePref().getValue(Const.SHARE_PREF_DDR_TEST).equals("1");
			if(mDdrReboot){
				Intent intentAtaInfo = new Intent(context, DdrActivity.class);
				intentAtaInfo.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				context.startActivity(intentAtaInfo);
			}
		} else {
			String number = intent.getExtras().getString("input");
			if (number.equals("*#8822#")) {
				Intent intentFactory = new Intent(context, RunInMainActivity.class);
				intentFactory.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				context.startActivity(intentFactory);
			}
		}
	}
}
