package com.prize.autotest;

import com.prize.autotest.mmi.BluetoothScanService;
import com.prize.autotest.mmi.WifiScanService;

import android.app.Activity;
import android.content.Intent;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class AutoTestActivity extends Activity implements OnClickListener {
	private static final String TAG = "zwl_camera";

	private Button btn_camera_test;
	private Button btn_audio_test;

	FactoryTestApplication app;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.prize_auto_test_activity);

		btn_camera_test = (Button) findViewById(R.id.btn_camera_test);
		btn_camera_test.setOnClickListener(this);
		btn_audio_test = (Button) findViewById(R.id.btn_audio_test);
		btn_audio_test.setOnClickListener(this);

		new Thread(new Runnable() {
			public void run() {
				try {
					Thread.sleep(1000);
					startService(new Intent(AutoTestActivity.this,
							WifiScanService.class));
					startService(new Intent(AutoTestActivity.this,
							BluetoothScanService.class));
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}).start();
	}

	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btn_camera_test:
			startActivity("com.prize.autotest",
					"com.prize.autotest.camera.AutoCameraActivity");
			break;
		case R.id.btn_audio_test:
			startActivity("com.prize.autotest",
					"com.prize.autotest.sensor.AutoSensorTestActivity");
			break;
		default:
			break;
		}
	}

	@Override
	protected void onDestroy() {
		if (app.getIsBluetoothScanning()) {
			stopService(new Intent(this, BluetoothScanService.class));
		}
		if (app.getIsWifiScanning()) {
			stopService(new Intent(this, WifiScanService.class));
		}

		super.onDestroy();
	}

	private void startActivity(String packageName, String className) {
		Intent startIntent = new Intent();
		startIntent.setClassName(packageName, className);
		startActivity(startIntent);
	}

}
