package com.prize.runoldtest;

import com.prize.runoldtest.util.LogUtil;
import com.prize.runoldtest.util.UsbService;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.BatteryManager;
import android.os.Bundle;
import android.os.PowerManager;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.Toast;

public class UsbFalseActivity extends Activity {
	private PowerManager.WakeLock wakeLock = null;
	Intent  batteryStatusIntent=null;
	private final static String ACTION = "android.hardware.usb.action.USB_STATE";//USB是否connect状态监听，而不是知否插入USB线监听
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
		IntentFilter  ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
		  batteryStatusIntent = registerReceiver(null, ifilter);   
		IntentFilter filter = new IntentFilter();
		
		//filter.addAction(ACTION);
		filter.addAction(ACTION2);
		
		registerReceiver(usBroadcastReceiver, filter);
		Log.e("UsbFalseActivity","Oncreat~~~");
		 
        SharedPreferences sharepreference = getSharedPreferences(DATABASE,  
                Activity.MODE_PRIVATE);
        Editor editor = sharepreference.edit();
        editor.putString("testenable", "false");  
        editor.commit();
        
        
	}

	protected void onStart() {
		super.onStart();
		//delete-zhuxiaoli-0818
		Intent hide = new Intent(this, UsbService.class);
		hide.putExtra(UsbService.ACTION, UsbService.HIDE_USB_INFO);
		startService(hide);
	}

	public boolean onKeyDown(int keyCode, KeyEvent event) {
		switch (keyCode) {
		case KeyEvent.KEYCODE_BACK:
			return true;

		case KeyEvent.KEYCODE_MENU:
			return true;

		default:
			break;
		}
		return super.onKeyDown(keyCode, event);
	}

	protected void onDestroy() {
		super.onDestroy();
		wakeLock.release();
	}

	BroadcastReceiver usBroadcastReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			// TODO Auto-generated method stub
			String action = intent.getAction();
			/*if (action.equals(ACTION)) {delete-zhuxiaoli-0819//注释掉，因为在USB断开时，也会触发此Action,致使不会出现提示用户界面
				
				boolean connected = intent.getExtras().getBoolean("connected");
				LogUtil.e("UsbFalseActivity,connected"+connected);
				if (connected) {
					LogUtil.e("USB connected ");					 
					Intent hide = new Intent(UsbFalseActivity.this, UsbService.class);
					hide.putExtra(UsbService.ACTION, UsbService.HIDE_USB_INFO);
					startService(hide);
					finish();                 
				}
			}*/
			 if(action.equals(ACTION2)){//插入电源或者usb触发此.add-zhuxiaoli-0818
				Toast.makeText(getApplicationContext(), "power connect！", Toast.LENGTH_SHORT).show();
				SharedPreferences sharepreference = UsbFalseActivity.this.getSharedPreferences(DATABASE,  
		                Activity.MODE_PRIVATE);
		        Editor editor = sharepreference.edit();
		        editor.putString("testenable", "true");  
		        editor.commit();
				Intent hide = new Intent(UsbFalseActivity.this, UsbService.class);
				hide.putExtra(UsbService.ACTION, UsbService.HIDE_USB_INFO);
				startService(hide);
				finish(); 
			}
			
		}
	};
	
	
	
}
