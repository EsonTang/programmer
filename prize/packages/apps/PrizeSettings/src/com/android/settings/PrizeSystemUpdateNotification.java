package com.android.settings;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;
import android.util.Log;
import android.provider.Settings;
import android.content.ContentResolver;
import android.content.ComponentName;
public class PrizeSystemUpdateNotification extends BroadcastReceiver{
    public static final String EXTRA_UNREAD_COMPONENT = "com.mediatek.intent.extra.UNREAD_COMPONENT";
    public static final String ACTION_UNREAD_CHANGED = "com.mediatek.action.UNREAD_CHANGED";
    public static final String EXTRA_UNREAD_NUMBER = "com.mediatek.intent.extra.UNREAD_NUMBER";
	@Override
	public void onReceive(Context context, Intent intent) {
		if(intent.getAction().equals("com.fota.custom_new_version")){
			Log.d("PrizeSystemUpdateNotification","com.fota.custom_new_version");
			sendUnread(context,new ComponentName("com.android.settings", "com.android.settings.Settings"),1,Settings.System.PRIZE_SYSTEM_UPDATE_CHANGE);
		}
		
	}
	public void sendUnread(Context c, ComponentName comp, int number, String Key) {

		Intent send = new Intent(ACTION_UNREAD_CHANGED);

		final ContentResolver cr = c.getContentResolver();
		send.putExtra(EXTRA_UNREAD_COMPONENT, comp);
		send.putExtra(EXTRA_UNREAD_NUMBER, number);
		try {
			c.sendBroadcast(send);
			Settings.System.putInt(cr, Key, number);
		} catch (Exception e) {
			// TODO: handle exception
		}
	}

}