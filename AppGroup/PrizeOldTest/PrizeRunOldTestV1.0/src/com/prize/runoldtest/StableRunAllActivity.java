package com.prize.runoldtest;

import com.prize.runoldtest.camera.StableCameraTestActivity;
import com.prize.runoldtest.emmc.StableEmmcActivity;
import com.prize.runoldtest.flight.StableFlightActivity;
import com.prize.runoldtest.lcd.StableLcdActivity;
import com.prize.runoldtest.util.Const;
import com.prize.runoldtest.util.DataUtil;
import com.prize.runoldtest.util.LogUtil;
import com.prize.runoldtest.vibrate.VibrateTestActivity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.PowerManager;
import android.widget.TextView;

@SuppressLint("Wakelock")
@SuppressWarnings("deprecation")
public class StableRunAllActivity extends Activity {
	private PowerManager.WakeLock wakeLock = null;
	private TextView test_result_tv;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		LogUtil.e("onCreateStableRunAllActivity");
		setContentView(R.layout.activity_stable_run_all_hour);
		test_result_tv = (TextView) findViewById(R.id.run_all_tv);
		PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
		wakeLock = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK
				| PowerManager.ACQUIRE_CAUSES_WAKEUP, "StableRunAllActivity");
		if (wakeLock == null || !wakeLock.isHeld()) {
			wakeLock.acquire();
		}
		DataUtil.addDestoryActivity(StableRunAllActivity.this,
				"StableRunAllActivity");
		Intent intent = new Intent(StableRunAllActivity.this,
				StableLcdActivity.class);
		intent.putExtra(Const.EXTRA_MESSAGE, 30);
		startActivity(intent);
	}

	protected void onStart() {
		super.onStart();
		LogUtil.e("onStartStableRunAllActivity");
		if (DataUtil.FlagLight_stable) {
			DataUtil.FlagLight_stable = false;
			Intent intent = new Intent(this, StableFlightActivity.class);
			intent.putExtra(Const.EXTRA_MESSAGE, 1000);
			startActivity(intent);
		} else if (DataUtil.FlagVibrate_stable) {
			DataUtil.FlagVibrate_stable = false;
			Intent intent = new Intent(this, VibrateTestActivity.class);
			intent.putExtra(Const.EXTRA_MESSAGE, 1000);
			startActivity(intent);
		} else if (DataUtil.FlagCamera_stable) {
			DataUtil.FlagCamera_stable = false;
			Intent intent = new Intent(this, StableCameraTestActivity.class);
			intent.putExtra(Const.EXTRA_MESSAGE, 1000);
			startActivity(intent);
		} else if (DataUtil.FlagEmmc_stable) {
			DataUtil.FlagEmmc_stable = false;
			Intent intent = new Intent(this, StableEmmcActivity.class);
			intent.putExtra(Const.EXTRA_MESSAGE, 1000);
			startActivity(intent);
		} else if (DataUtil.FlagALL_stable) {
			DataUtil.FlagALL_stable = false;
			test_result_tv.setText("PASS TEST");
		}
	}

	@Override
	protected void onPause() {
		super.onPause();
		LogUtil.e("onPauseStableRunAllActivity");
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		LogUtil.e("onDestroyStableRunAllActivity");
		if (wakeLock != null || wakeLock.isHeld()) {
			wakeLock.release();
			wakeLock = null;
		}
		DataUtil.resetFlag();
	}

}
