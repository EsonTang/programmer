package com.prize.runoldtest.cpu;

import java.util.Timer;
import java.util.TimerTask;

import com.prize.runoldtest.ManualTestActivity;
import com.prize.runoldtest.R;
import com.prize.runoldtest.RunAll6HourActivity;
import com.prize.runoldtest.R.id;
import com.prize.runoldtest.R.layout;
import com.prize.runoldtest.R.raw;
import com.prize.runoldtest.util.Const;
import com.prize.runoldtest.util.DataUtil;
import com.prize.runoldtest.util.LogToFile;
import com.prize.runoldtest.util.OldTestResult;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.os.Vibrator;
import android.provider.Settings;
import android.util.Log;
import android.view.WindowManager;
import android.widget.TextView;

public class CpuTestActivity extends Activity {
	private long cpu_time;
	private Vibrator vibrator;
	private MediaPlayer mplayer;
	private WifiManager manager;
	private PowerManager.WakeLock wakeLock = null;
	public String TAG = "RunInCPUTest";
	BluetoothAdapter mBluetoothAdapter;
	private Timer timer = null;
	TimerTask task = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_cpu_test);
		PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
		wakeLock = pm.newWakeLock(
				PowerManager.FULL_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP | PowerManager.ON_AFTER_RELEASE,
				"My TAG");
		wakeLock.acquire();
		DataUtil.addDestoryActivity(CpuTestActivity.this, "CpuTestActivity");

		LogToFile.writeToFile(LogToFile.VERBOSE, TAG, "beggin CpuTest......." + "\n");
		Log.e(TAG, "onCreate");
		
		timer = new Timer();
		task = new TimerTask() {
			public void run() {
				Log.e("yangnan", "KEYCODE_BACKFlagLcd");
				DataUtil.FlagLcd = true;
				LogToFile.writeToFile(LogToFile.VERBOSE, TAG, "CpuTest  finish......." + "\n");
				OldTestResult.CpuTestresult = true;

				// 数据是使用Intent返回
				Intent intent = new Intent();
				// 把返回数据存入Intent
				intent.putExtra("result", "CPUTest:PASS");
				// 设置返回数据
				CpuTestActivity.this.setResult(RESULT_OK, intent);
				CpuTestActivity.this.finish();
			}
		};

		getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
				| WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		Intent mintent = getIntent();
		cpu_time = mintent.getIntExtra(Const.EXTRA_MESSAGE, 0);
		TextView textView = (TextView) findViewById(R.id.tvShow);
		cpu_time = cpu_time * 60 * 1000;
		textView.setText(cpu_time + "");
		timer.schedule(task, cpu_time);
		try {
			mplayer = new MediaPlayer();
			mplayer = MediaPlayer.create(CpuTestActivity.this, R.raw.mp3);
			mplayer.start();
			mplayer.setLooping(true);
			LogToFile.writeToFile(LogToFile.VERBOSE, TAG, "CpuTest  MediaPlayer is playing..." + "\n");
		} catch (Exception e) {
			Log.e(TAG, "mp3 test fail");
			e.printStackTrace();
		}
		try {
			vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
			long[] pattern = { 1000, 500 };
			vibrator.vibrate(pattern, 0);
			LogToFile.writeToFile(LogToFile.VERBOSE, TAG, "CpuTest  Vibrator is working..." + "\n");
		} catch (Exception e) {
			Log.e(TAG, "Vibrator test fail");
			e.printStackTrace();
		}
		try {
			manager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
			if (!manager.setWifiEnabled(true)) {
				Log.e(TAG, "open wifi fail");
				LogToFile.writeToFile(LogToFile.VERBOSE, TAG, "CpuTest Wifi:open wifi fail..." + "\n");
			} else {
				Settings.Global.putInt(getContentResolver(), Settings.Global.WIFI_SCAN_ALWAYS_AVAILABLE, 1);
				LogToFile.writeToFile(LogToFile.VERBOSE, TAG, "CpuTest Wifi:open wifi success..." + "\n");
			}
		} catch (Exception e) {
			Log.e(TAG, "open wifi fail");
			e.printStackTrace();
		}
		try {
			mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
			if (!mBluetoothAdapter.enable()) {
				Log.e(TAG, "open bt fail");
				LogToFile.writeToFile(LogToFile.VERBOSE, TAG, "CpuTest Bluetooth:open Bluetooth fail..." + "\n");
			} else {
				Settings.Global.putInt(getContentResolver(), Settings.Global.BLE_SCAN_ALWAYS_AVAILABLE, 1);
				LogToFile.writeToFile(LogToFile.VERBOSE, TAG, "CpuTest Bluetooth:open Bluetooth success..." + "\n");
			}
		} catch (Exception e) {
			Log.e(TAG, "open bt fail");
			e.printStackTrace();
		}

		if (Build.VERSION.SDK_INT >= 23) {
			Log.e(TAG, "uild.VERSION.SDK_IN>23");
			if (!Settings.canDrawOverlays(this)) {
				Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
						Uri.parse("package:" + getPackageName()));
				startActivityForResult(intent, 10);
			}
		}
		Intent show = new Intent(this, DeviceInfoWindowService.class);
		show.putExtra(DeviceInfoWindowService.ACTION, DeviceInfoWindowService.SHOW_DEVICE_INFO);
		startService(show);
	}

	protected void onStart() {
		super.onStart();
		Log.e(TAG, "onStart");
	}

	public void onPause() {
		super.onPause();
		Log.e(TAG, "onPause");
	}

	@Override
	public void onBackPressed() {
		// TODO Auto-generated method stub
		super.onBackPressed();
		if (wakeLock.isHeld()) {
			wakeLock.release();
		}
		DataUtil.finishBackPressActivity();
		// 数据是使用Intent返回
		/*
		 * Intent intent = new Intent(); //把返回数据存入Intent
		 * intent.putExtra("result", "CPUTest:FAIL"); //设置返回数据
		 * CpuTestActivity.this.setResult(RESULT_OK, intent);
		 * CpuTestActivity.this.finish();
		 */

	}

	protected void onDestroy() {
		super.onDestroy();
		Intent intent = new Intent(this, DeviceInfoWindowService.class);
		intent.putExtra(DeviceInfoWindowService.ACTION, DeviceInfoWindowService.HIDE_DEVICE_INFO);
		startService(intent);
		Log.e("yangnan", "onPauseCpuTestActivity");
		if (timer != null) {
			timer.cancel();
		}
		if (task != null) {
			task.cancel();
		}

		if (wakeLock.isHeld()) {
			wakeLock.release();
		}
		mplayer.stop();
		vibrator.cancel();
		if (manager.isWifiEnabled()) {
			manager.setWifiEnabled(false);// 设置为关闭状态
		}
		mBluetoothAdapter.disable();
		LogToFile.writeToFile(LogToFile.VERBOSE, TAG, "CpuTest onDestroy" + "\n");
		Log.e(TAG, "onDestroy");
	}
}
