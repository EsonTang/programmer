package com.prize.runoldtest;

import com.prize.runoldtest.receiver.Broadcast;
import com.prize.runoldtest.usb.UsbFalseActivity;
import com.prize.runoldtest.util.DataUtil;
import com.prize.runoldtest.util.LogToFile;
import com.prize.runoldtest.util.OldTestResult;
import com.prize.runoldtest.util.UsbService;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class RunInMainActivity extends Activity {
	private ListView ListView_Main;
	Intent show = null;
	private static String TAG="RunInMainActivity";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_run_in_main);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED  
	               | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON  
	               | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);  
		Intent mintent=getIntent();
		if(mintent.getFlags()==Intent.FLAG_ACTIVITY_CLEAR_TOP){
			finish();
			return;
		}
		show = new Intent(this, UsbService.class);
		startService(show);
		IntentFilter ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
		Intent batteryStatusIntent = registerReceiver(null, ifilter);
		int chargePlug = batteryStatusIntent.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1);
		boolean usbCharge = chargePlug == BatteryManager.BATTERY_PLUGGED_USB;
		boolean acCharge = chargePlug == BatteryManager.BATTERY_PLUGGED_AC;
		if (!usbCharge && !acCharge) {
			Intent intent = new Intent(RunInMainActivity.this, UsbFalseActivity.class);
			intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			startActivity(intent);
		}
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(RunInMainActivity.this,
				android.R.layout.simple_list_item_1);
		ListView_Main = (ListView) findViewById(R.id.main_list);
		ListView_Main.setAdapter(adapter);

		adapter.add(getResources().getString(R.string.run_all_test));
		adapter.add(getResources().getString(R.string.manual_test));
		adapter.add(getResources().getString(R.string.single_test));
		adapter.add(getResources().getString(R.string.stable_test));

		ListView_Main.setOnItemClickListener(new AdapterView.OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
				// TODO Auto-generated method stub
				if (position == 0) {
					Log.i("main", "pos = " + position + " Run All Test");
					Intent intent = new Intent(RunInMainActivity.this, RunAllTestActivity.class);
					startActivity(intent);
				}
				if (position == 1) {
					Log.i("main", "pos = " + position + "Manual Test");
					Intent intent = new Intent(RunInMainActivity.this, ManualTestActivity.class);
					 OldTestResult.CleanTestResult();
					 DataUtil.isManualTst=true;
					startActivity(intent);
				}
				if (position == 2) {
					Log.i("main", "pos = " + position + "SingleTestActivity");
					Intent intent = new Intent(RunInMainActivity.this, SingleTestActivity.class);
					 OldTestResult.CleanTestResult();
					startActivity(intent);
				}
				if (position == 3) {
					Log.i("main", "pos = " + position + "StableMainActivity");
					Intent intent = new Intent(RunInMainActivity.this, StableMainActivity.class);
					 OldTestResult.CleanTestResult();
					startActivity(intent);
				}
			}
		});

		LogToFile.init(RunInMainActivity.this);
		Broadcast.SteTag(false);
	}

	protected void onDestroy() {
		super.onDestroy();
		Log.e(TAG, "onDestroy");
		LogToFile. writeToFile(LogToFile.VERBOSE, TAG, "inmain onDestroy"+"\n");
		Intent hide = new Intent(this, UsbService.class);
		hide.putExtra(UsbService.ACTION, UsbService.HIDE_USB_INFO);
		startService(hide);
		Broadcast.SteTag(true);
	}
	@Override
	public void onBackPressed() {
		// TODO Auto-generated method stub
		super.onBackPressed();
		LogToFile. writeToFile(LogToFile.VERBOSE, TAG, "onBackPressed"+"\n");
		Intent hide = new Intent(this, UsbService.class);
		hide.putExtra(UsbService.ACTION, UsbService.HIDE_USB_INFO);
		startService(hide);
		Broadcast.SteTag(true);
	}
}
