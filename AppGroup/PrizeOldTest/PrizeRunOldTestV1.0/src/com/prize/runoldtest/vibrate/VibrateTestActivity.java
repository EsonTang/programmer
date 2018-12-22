package com.prize.runoldtest.vibrate;

import java.util.Timer;
import java.util.TimerTask;

import com.prize.runoldtest.R;
import com.prize.runoldtest.util.Const;
import com.prize.runoldtest.util.DataUtil;
import com.prize.runoldtest.util.LogUtil;
import com.prize.runoldtest.util.ShowMsg;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.os.Vibrator;

@SuppressLint({ "HandlerLeak", "Wakelock" })
@SuppressWarnings("deprecation")
public class VibrateTestActivity extends Activity {
	private PowerManager.WakeLock wakeLock = null;
	private Vibrator mVibrator;
	private long test_counts;

	private Handler tosatHandler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case 1:
				ShowMsg.showmsg(VibrateTestActivity.this, "一共震动(" + test_counts
						+ ")次");
				break;
			default:
				break;
			}
		};
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		LogUtil.e("onCreateVibrateTestActivity");
		setContentView(R.layout.activity_vibrate);
		DataUtil.addDestoryActivity(VibrateTestActivity.this,
				"VibrateTestActivity");
		PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
		wakeLock = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK
				| PowerManager.ACQUIRE_CAUSES_WAKEUP, "VibrateTestActivity");
		if (wakeLock == null || !wakeLock.isHeld()) {
			wakeLock.acquire();
		}
		try {
			mVibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
			long[] pattern = { 1000, 2000 };
			mVibrator.vibrate(pattern, 0);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			LogUtil.e("Stable Vibrator test fail");
			e.printStackTrace();
		}
		Intent intent = getIntent();
		test_counts = intent.getIntExtra(Const.EXTRA_MESSAGE, 0);
		timer.schedule(task, test_counts * 3000);
		LogUtil.e("VibrateTestActivity-----test_counts = " + test_counts);
	}

	Timer timer = new Timer();
	TimerTask task = new TimerTask() {
		public void run() {
			Message mMessage = new Message();
			mMessage.what = 1;
			tosatHandler.sendMessage(mMessage);
			mVibrator.cancel();
			DataUtil.FlagCamera_stable = true;
			VibrateTestActivity.this.finish();
		}
	};

	protected void onPause() {
		super.onPause();
		LogUtil.e("onPauseVibrateTestActivity");
		timer.cancel();
		mVibrator.cancel();
	};

	@Override
	protected void onDestroy() {
		super.onDestroy();
		LogUtil.e("onDestroyVibrateTestActivity");
		if (wakeLock != null && wakeLock.isHeld()) {
			wakeLock.release();
			wakeLock = null;
		}
	}

}
