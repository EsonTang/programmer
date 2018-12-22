package com.prize.runoldtest.flight;

import java.io.FileOutputStream;
import java.util.Timer;
import java.util.TimerTask;
import com.prize.runoldtest.R;
import com.prize.runoldtest.util.Const;
import com.prize.runoldtest.util.DataUtil;
import com.prize.runoldtest.util.LogUtil;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.os.Bundle;
import android.os.Handler;
import android.os.PowerManager;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.RelativeLayout;

@SuppressLint({ "HandlerLeak", "Wakelock" })
@SuppressWarnings("deprecation")
public class StableFlightActivity extends Activity implements OnClickListener {
	public static final boolean HasFlashlightFile = false;
	private RelativeLayout mSFLlayout = null;
	private boolean mOpenLight = false;
	private boolean mCloseLight = false;
	private final static int OPENLIGHT = 1;
	private final static int CLOSELIGHT = 2;

	private PowerManager.WakeLock wakeLock = null;
	private Camera mycam = null;
	private Parameters camerPara = null;
	final byte[] LIGHTE_ON = { '2', '5', '5' };
	final byte[] LIGHTE_OFF = { '0' };
	private long test_counts;

	private Handler mHandler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case OPENLIGHT:
				trun_ON_Flashlight();
				mSFLlayout.setBackgroundResource(R.drawable.light_on);
				mHandler.postDelayed(mRunnable, 1000);
				mOpenLight = false;
				mCloseLight = true;
				break;
			case CLOSELIGHT:
				trun_OFF_Flashlight();
				mSFLlayout.setBackgroundResource(R.drawable.light_off);
				mHandler.postDelayed(mRunnable, 1000);
				mCloseLight = false;
				mOpenLight = true;
			default:
				break;
			}
		};
	};

	Timer timer = new Timer();

	TimerTask task = new TimerTask() {
		public void run() {
			mHandler.removeCallbacks(mRunnable);
			DataUtil.FlagVibrate_stable = true;
			StableFlightActivity.this.finish();
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		LogUtil.e("onCreateStableFlightActivity");
		setContentView(R.layout.activity_stable_flashlight);
		mSFLlayout = (RelativeLayout) findViewById(R.id.sfl_rl);
		DataUtil.addDestoryActivity(StableFlightActivity.this,
				"StableFlightActivity");
		PowerManager pm = (PowerManager) getSystemService(POWER_SERVICE);
		wakeLock = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK
				| PowerManager.ACQUIRE_CAUSES_WAKEUP, "StableFlightActivity");
		if (wakeLock == null || !wakeLock.isHeld()) {
			wakeLock.acquire();
		}
		mOpenLight = true;
		mHandler.post(mRunnable);
		Intent intent = getIntent();
		test_counts = intent.getIntExtra(Const.EXTRA_MESSAGE, 0);
		timer.schedule(task, test_counts * 2000);
		LogUtil.e("StableFlightActivity-----test_counts = " + test_counts);
	}

	@Override
	protected void onPause() {
		super.onPause();
		LogUtil.e("onPauseStableFlightActivity");
		mHandler.removeCallbacks(mRunnable);
		trun_OFF_Flashlight();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		LogUtil.e("onDestroyStableFlightActivity");
		if (wakeLock != null && wakeLock.isHeld()) {
			wakeLock.release();
			wakeLock = null;
		}
	};

	Runnable mRunnable = new Runnable() {
		public void run() {
			if (mOpenLight) {
				mHandler.sendEmptyMessage(OPENLIGHT);
			}
			if (mCloseLight) {
				mHandler.sendEmptyMessage(CLOSELIGHT);
			}
		}
	};

	protected void trun_ON_Flashlight() {
		if (!HasFlashlightFile) {
			try {
				mycam = Camera.open(Camera.CameraInfo.CAMERA_FACING_BACK);
				camerPara = mycam.getParameters();
				camerPara.setFlashMode(Parameters.FLASH_MODE_TORCH);
				mycam.setParameters(camerPara);
			} catch (Exception e) {
			}
		} else {

			FileOutputStream flashlight;
			try {
				flashlight = new FileOutputStream(
						"/sys/class/leds/flashlight/brightness");
				flashlight.write(LIGHTE_ON);
				flashlight.close();

			} catch (Exception e) {
			}
		}
	}

	protected void trun_OFF_Flashlight() {
		if (!HasFlashlightFile) {
			try {
				mycam = Camera.open(Camera.CameraInfo.CAMERA_FACING_BACK);
				camerPara = mycam.getParameters();
				camerPara.setFlashMode(Parameters.FLASH_MODE_OFF);
				mycam.setParameters(camerPara);
			} catch (Exception e) {
			}
		} else {
			FileOutputStream flashlight;
			try {
				flashlight = new FileOutputStream(
						"/sys/class/leds/flashlight/brightness");
				flashlight.write(LIGHTE_OFF);
				flashlight.close();
			} catch (Exception e) {
			}
		}
	}

	@Override
	public void onBackPressed() {
		super.onBackPressed();
		StableFlightActivity.this.finish();
	}

	protected void onStart() {
		super.onStart();
	}

	@Override
	public void onClick(View v) {
	}
}
