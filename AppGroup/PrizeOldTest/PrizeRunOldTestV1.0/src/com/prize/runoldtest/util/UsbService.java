package com.prize.runoldtest.util;

import com.prize.runoldtest.usb.UsbFalseActivity;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.os.IBinder;
import android.util.Log;

/**
 * Created by allen on 17-5-27.
 */

public class UsbService extends Service {

	public static final String TAG = "UsbService";
	public static final String ACTION = "action";
	public static final int SHOW_USB_INFO = 100;
	public static final int HIDE_USB_INFO = 101;

	private static final int REFRESH_TIME = 1000;
	private static final int HANDLE_CHECK_ACTIVITY = 200;

	@Override
	public void onCreate() {
		super.onCreate();
		System.out.println("UsbService");
		IntentFilter filter = new IntentFilter();
		filter.addAction(Intent.ACTION_BATTERY_CHANGED);
		filter.addAction(Intent.ACTION_POWER_CONNECTED);
		filter.addAction(Intent.ACTION_POWER_DISCONNECTED);
		registerReceiver(usBroadcastReceiver, filter);
	}

	BroadcastReceiver usBroadcastReceiver = new BroadcastReceiver() {
		public void onReceive(Context context, Intent intent) {
			if (intent != null) {
				String action = intent.getAction();
				if (action.equals(Intent.ACTION_BATTERY_CHANGED)) {
					int BatteryN = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, 0);
					if (BatteryN < 70) {
						Log.e(TAG, "BatteryN<70 :" + BatteryN);
						setBatteryCmd(0);
					} else if (BatteryN > 80) {
						Log.e(TAG, "BatteryN>80:" + BatteryN);
						setBatteryCmd(1);
					}
				}
				if (action.equals(Intent.ACTION_POWER_CONNECTED)) {
					Log.e(TAG, "AC Connected! ");
				}
				if (action.equals(Intent.ACTION_POWER_DISCONNECTED)) {
					Log.e(TAG, "AC DisConnected! ");
					Intent intentusb = new Intent(UsbService.this, UsbFalseActivity.class);
					intentusb.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					context.startActivity(intentusb);
				}
			}
		}
	};

	@Override
	public void onStart(Intent intent, int startId) {
		super.onStart(intent, startId);
		if (intent != null) {
			int actionvalue = intent.getIntExtra(ACTION, 0);
			if (actionvalue == HIDE_USB_INFO) {
				Log.e(TAG, "UsbService stopSelf()! getPackageName:");
				this.unregisterReceiver(usBroadcastReceiver);
				stopSelf();
			}
		}
	}

	public void setBatteryCmd(int value) {
		try {
			String[] cmdMode = new String[] { "/system/bin/sh", "-c",
					"echo" + " 0 " + value + " > /proc/mtk_battery_cmd/current_cmd" };
			Runtime.getRuntime().exec(cmdMode);
			Log.e(TAG, "proc/mtk_battery_cmd/current_cmd write the -success");
		} catch (Exception e) {
			// TODO: handle exception
			Log.e(TAG, "proc/mtk_battery_cmd/current_cmd write the -fail");
		}
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		return super.onStartCommand(intent, flags, startId);
	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public void onDestroy() {
		setBatteryCmd(0);
		super.onDestroy();
		System.exit(0); 
	}
}
