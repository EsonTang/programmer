package com.prize.runoldtest.lcd;

import java.util.Timer;
import java.util.TimerTask;

import com.prize.runoldtest.R;
import com.prize.runoldtest.util.Const;
import com.prize.runoldtest.util.DataUtil;
import com.prize.runoldtest.util.LogUtil;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.PowerManager;
import android.util.Log;
import android.view.WindowManager;
import android.widget.FrameLayout;

@SuppressLint({ "HandlerLeak", "Wakelock" })
@SuppressWarnings("deprecation")
public class StableLcdActivity extends Activity {
	private PowerManager.WakeLock wakeLock = null;
	private long test_counts;
	FrameLayout frame = null;
	private WindowManager.LayoutParams mLparmes;
	Timer timer = new Timer();
	private boolean mInitial = false;
	private boolean mHToL = false;
	private boolean mLToH = false;
	private boolean mHightNess = false;
	private boolean mLowNess = false;
	private boolean mPicOne = false;
	private boolean mPicTwo = false;
	private boolean mPicThree = false;
	private boolean mRed = false;
	private boolean mGreen = false;
	private boolean mBlue = false;
	private boolean mWhite = false;
	private boolean mBlack = false;
	private final static int INITIAL = 1;
	private final static int HTOL = 2;
	private final static int LTOH = 3;
	private final static int HIGHTNESS = 4;
	private final static int LOWNESS = 5;
	private final static int PICONE = 6;
	private final static int PICTWO = 7;
	private final static int PICTHREE = 8;
	private final static int RED = 9;
	private final static int GREEN = 10;
	private final static int BLUE = 11;
	private final static int WHITE = 12;
	private final static int BLACK = 13;
	private int mUpCounts = 3;

	private Handler handler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case INITIAL:
				frame.setBackgroundResource(R.drawable.m1);
				mLparmes = getWindow().getAttributes();
				mLparmes.screenBrightness = 1.0f;
				handler.postDelayed(mRunnable, 2000);
				mInitial = false;
				mHToL = true;
				break;
			case HTOL:
				onDownBrightNess();
				handler.postDelayed(mRunnable, 5100);
				mHToL = false;
				mLToH = true;
				break;
			case LTOH:
				onUpBrightNess();
				handler.postDelayed(mRunnable, 5100);
				mUpCounts--;
				if (mUpCounts > 0) {
					Log.e("StableLcdActivity", "mUpCounts = " + mUpCounts);
					mLToH = false;
					mHToL = true;
				} else {
					mLToH = false;
					mHightNess = true;
				}
				break;
			case HIGHTNESS:
				Log.e("StableLcdActivity", "HIGHTNESS");
				mLparmes = getWindow().getAttributes();
				mLparmes.screenBrightness = 0.6f;
				getWindow().setAttributes(mLparmes);
				handler.postDelayed(mRunnable, 5000);
				mHightNess = false;
				mLowNess = true;
				break;
			case LOWNESS:
				Log.e("StableLcdActivity", "LOWNESS");
				mLparmes = getWindow().getAttributes();
				mLparmes.screenBrightness = 1.0f;
				getWindow().setAttributes(mLparmes);
				handler.postDelayed(mRunnable, 5000);
				mLowNess = false;
				mPicOne = true;
				break;
			case PICONE:
				frame.setBackgroundResource(R.drawable.pic1);
				mLparmes = getWindow().getAttributes();
				mLparmes.screenBrightness = 1.0f;
				handler.postDelayed(mRunnable, 5000);
				mPicOne = false;
				mPicTwo = true;
				break;
			case PICTWO:
				frame.setBackgroundResource(R.drawable.pic2);
				handler.postDelayed(mRunnable, 5000);
				mPicTwo = false;
				mPicThree = true;
				break;
			case PICTHREE:
				frame.setBackgroundResource(R.drawable.pic3);
				handler.postDelayed(mRunnable, 5000);
				mPicThree = false;
				mRed = true;
				break;
			case RED:
				frame.setBackgroundResource(R.drawable.m3);
				handler.postDelayed(mRunnable, 5000);
				mRed = false;
				mGreen = true;
				break;
			case GREEN:
				frame.setBackgroundResource(R.drawable.m5);
				handler.postDelayed(mRunnable, 5000);
				mGreen = false;
				mBlue = true;
				break;
			case BLUE:
				frame.setBackgroundResource(R.drawable.m4);
				handler.postDelayed(mRunnable, 5000);
				mBlue = false;
				mWhite = true;
				break;
			case WHITE:
				frame.setBackgroundResource(R.drawable.m1);
				handler.postDelayed(mRunnable, 5000);
				mWhite = false;
				mBlack = true;
				break;
			case BLACK:
				frame.setBackgroundResource(R.drawable.m2);
				handler.postDelayed(mRunnable, 5000);
				mBlack = false;
				mInitial = true;
				break;
			default:
				break;
			}
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		LogUtil.e("onCreateStableLcdActivity");
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setContentView(R.layout.activity_stable_lcd);
		DataUtil.addDestoryActivity(StableLcdActivity.this, "StableLcdActivity");
		PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
		wakeLock = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK
				| PowerManager.ACQUIRE_CAUSES_WAKEUP, "StableLcdActivity");
		if (wakeLock == null || !wakeLock.isHeld()) {
			wakeLock.acquire();
		}
		mLparmes = getWindow().getAttributes();
		DataUtil.addDestoryActivity(StableLcdActivity.this, "StableLcdActivity");
		Intent intent = getIntent();
		test_counts = intent.getIntExtra(Const.EXTRA_MESSAGE, 0);
		timer.schedule(task, test_counts * 86600);
		LogUtil.e("StableLcdActivity-----test_counts = " + test_counts);
		frame = (FrameLayout) findViewById(R.id.stable_myFrame);
		mInitial = true;
		handler.post(mRunnable);
	}

	Runnable mRunnable = new Runnable() {
		@Override
		public void run() {
			// TODO Auto-generated method stub
			if (mInitial) {
				LogUtil.e("mInitial = " + mInitial);
				handler.sendEmptyMessage(INITIAL);
			}
			if (mHToL) {
				LogUtil.e("mHToL = " + mHToL);
				handler.sendEmptyMessage(HTOL);
			}
			if (mLToH) {
				LogUtil.e("mLToH = " + mLToH);
				handler.sendEmptyMessage(LTOH);
			}
			if (mHightNess) {
				LogUtil.e("mHightNess = " + mHightNess);
				handler.sendEmptyMessage(HIGHTNESS);
			}
			if (mLowNess) {
				LogUtil.e("mLowNess = " + mLowNess);
				handler.sendEmptyMessage(LOWNESS);
			}
			if (mPicOne) {
				LogUtil.e("mPicOne = " + mPicOne);
				handler.sendEmptyMessage(PICONE);
			}
			if (mPicTwo) {
				LogUtil.e("mPicTwo = " + mPicTwo);
				handler.sendEmptyMessage(PICTWO);
			}
			if (mPicThree) {
				LogUtil.e("mPicThree = " + mPicThree);
				handler.sendEmptyMessage(PICTHREE);
			}
			if (mRed) {
				LogUtil.e("mRed = " + mRed);
				handler.sendEmptyMessage(RED);
			}
			if (mGreen) {
				LogUtil.e("mGreen = " + mGreen);
				handler.sendEmptyMessage(GREEN);
			}
			if (mBlue) {
				LogUtil.e("mBlue = " + mBlue);
				handler.sendEmptyMessage(BLUE);
			}
			if (mWhite) {
				LogUtil.e("mWhite = " + mWhite);
				handler.sendEmptyMessage(WHITE);
			}
			if (mBlack) {
				LogUtil.e("mBlack = " + mBlack);
				handler.sendEmptyMessage(BLACK);
			}
		}
	};

	TimerTask task = new TimerTask() {
		public void run() {
			LogUtil.e("KEYCODE_BACKFlag3D");
			DataUtil.FlagLight_stable = true;
			StableLcdActivity.this.finish();
		}
	};

	private void onDownBrightNess() {
		for (float i = 1.0f; i >= 0.0f; i = i - 0.001f) {
			Log.e("StableLcdActivity", "HightToLowLight = " + i);
			mLparmes = getWindow().getAttributes();
			mLparmes.screenBrightness = i;
			getWindow().setAttributes(mLparmes);
		}
	}

	private void onUpBrightNess() {
		for (float i = 0.0f; i <= 1.0f; i = i + 0.001f) {
			Log.e("StableLcdActivity", "LowToHightLight = " + i);
			mLparmes = getWindow().getAttributes();
			mLparmes.screenBrightness = i;
			getWindow().setAttributes(mLparmes);
		}
	}

	public void onPause() {
		super.onPause();
		LogUtil.e("onPauseStableLcdActivity");
		handler.removeCallbacks(mRunnable);
		timer.cancel();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		LogUtil.e("onDestroyStableAcdActivity");
		if (wakeLock != null && wakeLock.isHeld()) {
			wakeLock.release();
			wakeLock = null;
		}
	}
}
