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
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

@SuppressLint("Wakelock")
@SuppressWarnings("deprecation")
public class StableMainActivity extends Activity implements OnClickListener {
	private PowerManager.WakeLock wakeLock = null;
	private Button lcd_bt;
	private Button flashlight_bt;
	private Button tf_bt;
	private Button camera_bt;
	private Button vibrate_bt;
	private Button auto_bt;
	private Button all_run_bt;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		LogUtil.e("onStartStableMainActivity");
		setContentView(R.layout.activity_stable_test);
		DataUtil.addDestoryActivity(StableMainActivity.this,
				"StableMainActivity");
		PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
		wakeLock = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK
				| PowerManager.ACQUIRE_CAUSES_WAKEUP, "StableMainActivity");
		if (wakeLock == null || !wakeLock.isHeld()) {
			wakeLock.acquire();
		}
		initView();
	}

	private void initView() {
		lcd_bt = (Button) findViewById(R.id.stable_lcd);
		lcd_bt.setOnClickListener(this);
		flashlight_bt = (Button) findViewById(R.id.stable_flashlight);
		flashlight_bt.setOnClickListener(this);
		tf_bt = (Button) findViewById(R.id.stable_tf);
		tf_bt.setOnClickListener(this);
		camera_bt = (Button) findViewById(R.id.stable_camera);
		camera_bt.setOnClickListener(this);
		vibrate_bt = (Button) findViewById(R.id.stable_vibrate);
		vibrate_bt.setOnClickListener(this);
		auto_bt = (Button) findViewById(R.id.stable_auto_test);
		auto_bt.setOnClickListener(this);
		all_run_bt = (Button) findViewById(R.id.stable_all_auto_test);
		all_run_bt.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.stable_lcd:
			startActivity(StableLcdActivity.class);
			break;
		case R.id.stable_flashlight:
			startActivity(StableFlightActivity.class);
			break;
		case R.id.stable_tf:
			startActivity(StableEmmcActivity.class);
			break;
		case R.id.stable_camera:
			startActivity(StableCameraTestActivity.class);
			break;
		case R.id.stable_vibrate:
			startActivity(VibrateTestActivity.class);
			break;
		case R.id.stable_auto_test:
			startActivity(StableManualTestActivity.class);
			break;
		case R.id.stable_all_auto_test:
			startActivity(StableRunAllActivity.class);
			break;
		default:
			break;
		}
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		LogUtil.e("onPauseStableMainActivity");
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		LogUtil.e("onDestroyStableMainActivity");
		if (wakeLock != null && wakeLock.isHeld()) {
			wakeLock.release();
			wakeLock = null;
		}
	}

	private void startActivity(Class<?> cls) {
		LogUtil.e(cls.getName());
		Intent intent = new Intent(StableMainActivity.this, cls);
		intent.putExtra(Const.EXTRA_MESSAGE, 10000);
		StableMainActivity.this.startActivity(intent);
	}
}
