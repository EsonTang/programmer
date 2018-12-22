package com.prize.factorytest.Vibrate;

import com.prize.factorytest.R;
import com.prize.factorytest.PrizeFactoryTestListActivity;
import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.os.Vibrator;
import android.view.View;
import android.widget.Button;
import android.view.KeyEvent;

public class Vibrate extends Activity {
	private Handler mHandler = new Handler();
	private final long VIBRATOR_ON_TIME = 1000;
	private final long VIBRATOR_OFF_TIME = 500;
	String TAG = "Vibrate";
	Vibrator mVibrator = null;
	PowerManager powerManager = null;
	WakeLock wakeLock = null;
	long[] pattern = { VIBRATOR_OFF_TIME, VIBRATOR_ON_TIME };

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if(keyCode == KeyEvent.KEYCODE_BACK){
			return true;
		}
		return false;
	}
	
	@SuppressWarnings("deprecation")
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.vibrate);
		mVibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
		powerManager = (PowerManager) this
				.getSystemService(Context.POWER_SERVICE);
		wakeLock = this.powerManager.newWakeLock(PowerManager.FULL_WAKE_LOCK,
				"My Lock");
		confirmButton();
	}

	@Override
	protected void onPause() {
		mHandler.removeCallbacks(mRunnable);
		mVibrator.cancel();
		wakeLock.release();
		super.onPause();
	}

	@Override
	protected void onResume() {
		mHandler.postDelayed(mRunnable, 0);
		wakeLock.acquire();
		super.onResume();
	}

	private Runnable mRunnable = new Runnable() {

		public void run() {
			mHandler.removeCallbacks(mRunnable);
			mVibrator.vibrate(pattern, 0);
		}
	};

	public void confirmButton() {
		final Button buttonPass = (Button) findViewById(R.id.passButton);
		final Button buttonFail = (Button) findViewById(R.id.failButton);
		buttonPass.setOnClickListener(new Button.OnClickListener() {

			@Override
			public void onClick(View arg0) {
				if (PrizeFactoryTestListActivity.toStartAutoTest == true) {
					PrizeFactoryTestListActivity.itempos++;
				}
				setResult(RESULT_OK);
				finish();
			}
		});
		buttonFail.setOnClickListener(new Button.OnClickListener() {

			@Override
			public void onClick(View arg0) {
				if (PrizeFactoryTestListActivity.toStartAutoTest == true) {
					PrizeFactoryTestListActivity.itempos++;
				}
				setResult(RESULT_CANCELED);
				finish();

			}

		});
	}
}
