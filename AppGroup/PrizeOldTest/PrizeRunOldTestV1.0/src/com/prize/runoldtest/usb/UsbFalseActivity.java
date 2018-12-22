package com.prize.runoldtest.usb;

import com.prize.runoldtest.R;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.os.PowerManager;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.Toast;

public class UsbFalseActivity extends Activity {
	private PowerManager.WakeLock wakeLock = null;

	private final static String ACTION2 = "android.intent.action.ACTION_POWER_CONNECTED";
	/** 存储的文件名 */
	public static final String DATABASE = "Database";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_usb_false);

		PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
		wakeLock = pm.newWakeLock(
				PowerManager.FULL_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP | PowerManager.ON_AFTER_RELEASE,
				"My TAG");
		wakeLock.acquire();

		IntentFilter filter = new IntentFilter();
		filter.addAction(ACTION2);
		registerReceiver(usbBroadcastReceiver, filter);
		Log.e("UsbFalseActivity", "Oncreate~~~");

		SharedPreferences sharepreference = getSharedPreferences(DATABASE, Activity.MODE_PRIVATE);
		Editor editor = sharepreference.edit();
		editor.putString("testenable", "false");
		editor.commit();

	}

	protected void onStart() {
		super.onStart();
	}

	public boolean onKeyDown(int keyCode, KeyEvent event) {
		switch (keyCode) {
		case KeyEvent.KEYCODE_BACK:
			finish();
			return true;

		case KeyEvent.KEYCODE_MENU:
			return true;

		default:
			break;
		}
		return super.onKeyDown(keyCode, event);
	}

	@Override
	protected void onStop() {
		super.onStop();

	}

	protected void onDestroy() {
		super.onDestroy();
		wakeLock.release();
	}

	BroadcastReceiver usbBroadcastReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			// TODO Auto-generated method stub
			if (intent != null) {
				String action = intent.getAction();
				if (action.equals(ACTION2)) {// 插入电源或者usb触发此.add-zhuxiaoli-0818
					Toast.makeText(getApplicationContext(), "power connect！", Toast.LENGTH_SHORT).show();
					SharedPreferences sharepreference = UsbFalseActivity.this.getSharedPreferences(DATABASE,
							Activity.MODE_PRIVATE);
					Editor editor = sharepreference.edit();
					editor.putString("testenable", "true");
					editor.commit();
					finish();
				}
			}
		}
	};

}
