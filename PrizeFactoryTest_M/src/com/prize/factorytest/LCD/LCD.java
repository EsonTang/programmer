package com.prize.factorytest.LCD;

import java.io.InputStream;

import com.prize.factorytest.R;
import com.prize.factorytest.PrizeFactoryTestListActivity;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.view.KeyEvent;
public class LCD extends Activity {

	private static Handler mHandler;
	private int brightnessState = 0, imgSeq = 0;
	private float mBrightness = 1.0f;
	float x = 0, y = 0;
	WindowManager.LayoutParams mLayoutParams;

	private boolean ifLocked = false;
	private PowerManager.WakeLock mWakeLock;
	private PowerManager mPowerManager;
	private LinearLayout mLinearLayout;

	private int[] mTestImg = { R.drawable.lcm_red, R.drawable.lcm_green,
			R.drawable.lcm_blue,  R.drawable.lcm_black,
			R.drawable.lcm_black_white_lump,R.drawable.lcm_girl_01,
			R.drawable.lcm_girl_02,R.drawable.lcm_girl_08 };

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
		setContentView(R.layout.lcd);
		mLayoutParams = getWindow().getAttributes();
		mLayoutParams.screenBrightness = 1;
		getWindow().setAttributes(mLayoutParams);

		mLinearLayout = (LinearLayout) findViewById(R.id.myLinearLayout1);
		mPowerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
		mWakeLock = mPowerManager.newWakeLock(
				PowerManager.SCREEN_BRIGHT_WAKE_LOCK, "BackLight");
		startHandler();
	}

	public boolean onTouchEvent(MotionEvent event) {
		switch (event.getAction()) {
		case 0: {
			if (imgSeq <= mTestImg.length) {
				Message message = new Message();
				message.what = imgSeq;
				mHandler.sendMessage(message);
				break;
			} else {
				imgSeq = mTestImg.length;
				break;
			}
		}
		}
		return super.onTouchEvent(event);

	}

	void startHandler() {
		mHandler = new Handler() {
			@SuppressWarnings("deprecation")
			@Override
			public void handleMessage(Message msg) {
				switch (msg.what) {
				case 0:
				case 1:
				case 2:
				case 3:
				case 4:
				case 5:
				case 6:
				case 7:
				case 8:
				case 9:

					if (brightnessState == 0) {
						//mBrightness = 0.01f;
						mBrightness = 1.0f;
						brightnessState = 1;

					} else {
						mBrightness = 1.0f;
						brightnessState = 0;
					}
					try {
						BitmapFactory.Options option = new BitmapFactory.Options();
						option.inPreferredConfig = Config.ARGB_8888;
						option.inPurgeable = true;
						option.inInputShareable = true;
						InputStream mInputSream = getResources()
								.openRawResource(mTestImg[imgSeq]);
						Bitmap bitmap = BitmapFactory.decodeStream(mInputSream,
								null, option);
						BitmapDrawable bitmapDrawable = new BitmapDrawable(
								bitmap);
						mLinearLayout.setBackgroundDrawable(bitmapDrawable);

					} catch (Exception e) {
						e.printStackTrace();
					}
					imgSeq++;
					mLayoutParams = getWindow().getAttributes();
					mLayoutParams.screenBrightness = mBrightness;
					getWindow().setAttributes(mLayoutParams);
					if (imgSeq < mTestImg.length) {

					} else {
						imgSeq = 0;
						confirmButton();
					}
					break;
				default:
					break;
				}
				super.handleMessage(msg);

			}
		};
	}

	public void start() {

		mHandler.removeCallbacks(mRunnable);
		mHandler.postDelayed(mRunnable, 0);
	}

	private Runnable mRunnable = new Runnable() {

		@SuppressWarnings("deprecation")
		public void run() {
			imgSeq++;
			if (brightnessState == 0) {
				mBrightness = 0.01f;
				brightnessState = 1;

			} else {
				mBrightness = 1.0f;
				brightnessState = 0;
			}
			try {
				BitmapFactory.Options option = new BitmapFactory.Options();
				option.inPreferredConfig = Config.ARGB_8888;
				option.inPurgeable = true;
				option.inInputShareable = true;
				InputStream mInputSream = getResources().openRawResource(
						mTestImg[imgSeq]);
				Bitmap bitmap = BitmapFactory.decodeStream(mInputSream, null,
						option);
				BitmapDrawable bitmapDrawable = new BitmapDrawable(bitmap);
				mLinearLayout.setBackgroundDrawable(bitmapDrawable);

			} catch (Exception e) {
				e.printStackTrace();
			}

			mLayoutParams = getWindow().getAttributes();
			mLayoutParams.screenBrightness = mBrightness;
			getWindow().setAttributes(mLayoutParams);
			if (imgSeq < mTestImg.length) {

			} else {
				imgSeq = 0;
				confirmButton();
			}
			if (imgSeq != 0) {
				mHandler.postDelayed(mRunnable, 1000);
			}
			System.gc();
		}
	};

	public void confirmButton() {
		setContentView(R.layout.lcd_confirm);
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

	@Override
	protected void onResume() {

		wakeLock();
		super.onResume();
	}

	@Override
	protected void onPause() {

		wakeUnlock();
		super.onPause();

		mHandler.removeCallbacks(mRunnable);
		setResult(RESULT_CANCELED);

	}

	private void wakeLock() {

		if (!ifLocked) {
			ifLocked = true;
			mWakeLock.acquire();
		}
	}

	private void wakeUnlock() {

		if (ifLocked) {
			mWakeLock.release();
			ifLocked = false;
		}
	}
}
