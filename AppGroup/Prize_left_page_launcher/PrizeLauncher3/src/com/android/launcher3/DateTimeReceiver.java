package com.android.launcher3;

import java.util.Calendar;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class DateTimeReceiver extends BroadcastReceiver {

	private static final String ACTION_DATE_CHANGED = "android.intent.action.TIME_SET";

	@Override
	public void onReceive(Context context, Intent intent) {/*

		String action = intent.getAction();

		if (ACTION_DATE_CHANGED.equals(action)) {
			Calendar c = Calendar.getInstance(); // 秒
			int data=c.get(Calendar.DAY_OF_MONTH);
			int week =c.get(Calendar.DAY_OF_WEEK);
			LauncherAppState app = LauncherAppState.getInstance();
			app.getModel().getCallBacks().get().dochangeDataIcon( data, week,"calendar");
		}

	*/}

}
