package com.prize.autotest.mmi;

import com.prize.autotest.AutoConstant;
import com.prize.autotest.R;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.PowerManager;
import android.os.SystemProperties;
import android.os.PowerManager.WakeLock;
import android.os.Vibrator;
import android.view.KeyEvent;
//import android.widget.Toast;

public class AutoVibrateTestActivity extends Activity {
	private Handler mHandler = new Handler();
	private final long VIBRATOR_ON_TIME = 1000;
	private final long VIBRATOR_OFF_TIME = 500;
	String TAG = "Vibrate";
	Vibrator mVibrator = null;
	PowerManager powerManager = null;
	WakeLock wakeLock = null;
	long[] pattern = { VIBRATOR_OFF_TIME, VIBRATOR_ON_TIME };

	private String cmdOrder = null;
	private BroadcastReceiver mBroadcast = null;
	
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
		
		mBroadcast = new MyBroadcastReceiver();
		IntentFilter filter = new IntentFilter();
		filter.addAction(AutoConstant.ACTION_UI);
		registerReceiver(mBroadcast, filter);

		Intent intent = getIntent();
		cmdOrder = intent.getStringExtra("back");
		if (cmdOrder != null) {
			new Handler().post(new Runnable() {
				public void run() {
					runCmdOrder();
				}
			});
		}		
		
		mVibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
		powerManager = (PowerManager) this
				.getSystemService(Context.POWER_SERVICE);
		wakeLock = this.powerManager.newWakeLock(PowerManager.FULL_WAKE_LOCK,
				"My Lock");

	}

	private class MyBroadcastReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			if (intent != null) {
				cmdOrder = intent.getStringExtra("back");
				runCmdOrder();
			}
			/*Toast.makeText(AutoVibrateTestActivity.this,
					intent.getStringExtra("back"), Toast.LENGTH_SHORT).show();*/
		}
	}
	
	private void runCmdOrder() {
		if (cmdOrder == null || cmdOrder.length() < 1) {
			return;
		}

		String temp = cmdOrder.substring(1);
		//Toast.makeText(this, temp + "", Toast.LENGTH_SHORT).show();
		if (temp.startsWith(AutoConstant.CMD_MMI_VIBRATE_START)) {
			AutoConstant.SendDataToService(AutoConstant.RESULT_SUCCUSS,this);
			AutoConstant.writeFile("Vibrate : PASS" + "\n");
		}else if (temp.startsWith(AutoConstant.CMD_MMI_VIBRATE_STOP)) {
			AutoConstant.SendDataToService(AutoConstant.RESULT_SUCCUSS,this);
			finish();
		}


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
			mVibrator.vibrate(100000);
		}
	};
}
