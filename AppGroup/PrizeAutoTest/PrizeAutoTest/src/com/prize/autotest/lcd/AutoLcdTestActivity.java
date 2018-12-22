package com.prize.autotest.lcd;

import java.io.InputStream;

import com.prize.autotest.AutoConstant;
import com.prize.autotest.R;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.os.SystemClock;
import android.view.MotionEvent;
import android.view.WindowManager;
import android.widget.LinearLayout;
//import android.widget.Toast;
import android.view.KeyEvent;

public class AutoLcdTestActivity extends Activity {

	private static Handler mHandler;
	private int brightnessState = 0, imgSeq = 0;
	private float mBrightness = 1.0f;
	float x = 0, y = 0;
	WindowManager.LayoutParams mLayoutParams;

	private boolean ifLocked = false;
	//private PowerManager.WakeLock mWakeLock;
	private PowerManager mPowerManager;
	private LinearLayout mLinearLayout;

	private String cmdOrder = null;
	private BroadcastReceiver mBroadcast = null;
	private int[] mTestImg = { R.drawable.lcm_red, R.drawable.lcm_green,
			R.drawable.lcm_blue, R.drawable.lcm_black,
			R.drawable.lcm_gray};

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
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
//		mPowerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
//		mWakeLock = mPowerManager.newWakeLock(
//				PowerManager.SCREEN_BRIGHT_WAKE_LOCK, "BackLight");
//		mWakeLock.acquire();
		startHandler();
		
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
	}

	private class MyBroadcastReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			if (intent != null) {
				cmdOrder = intent.getStringExtra("back");
				runCmdOrder();
			}
			/*Toast.makeText(AutoLcdTestActivity.this,
					intent.getStringExtra("back"), Toast.LENGTH_SHORT).show();*/
		}
	}

	private void runCmdOrder() {
		if (cmdOrder == null || cmdOrder.length() < 1) {
			return;
		}

		String temp = cmdOrder.substring(1);
		//Toast.makeText(this, temp + "", Toast.LENGTH_SHORT).show();
		if (temp.startsWith(AutoConstant.CMD_LCD_RED)) {
			changeColorPic(0);
		} else if (temp.startsWith(AutoConstant.CMD_LCD_GREEN)) {
			changeColorPic(1);
		} else if (temp.startsWith(AutoConstant.CMD_LCD_BLUE)) {
			changeColorPic(2);
		} else if (temp.startsWith(AutoConstant.CMD_LCD_BLACK)) {
			changeColorPic(3);
		} else if (temp.startsWith(AutoConstant.CMD_LCD_GRAY)) {
			changeColorPic(4);
		} else if (temp.startsWith(AutoConstant.CMD_LCD_BRIGNESS_SLOW)) {
			mLayoutParams = getWindow().getAttributes();
			mLayoutParams.screenBrightness = 0.5f;
			getWindow().setAttributes(mLayoutParams);
		} else if (temp.startsWith(AutoConstant.CMD_LCD_SLEEP)) {
			PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE); 
			pm.goToSleep(SystemClock.uptimeMillis());
		} else if (temp.startsWith(AutoConstant.CMD_LCD_SUCCESS)) {
			unregisterReceiver(mBroadcast);
			AutoConstant.writeProInfo("P", 37);
			AutoConstant.SendDataToService(AutoConstant.RESULT_SUCCUSS, this);
			finish();
		} else if (temp.startsWith(AutoConstant.CMD_LCD_FAIL)) {
			unregisterReceiver(mBroadcast);
			AutoConstant.writeProInfo("F", 37);
			AutoConstant.SendDataToService(AutoConstant.RESULT_SUCCUSS, this);
			finish();
		} 
		AutoConstant.SendDataToService(AutoConstant.RESULT_SUCCUSS, this);
	}
	
	private void changeColorPic(int imgSeqCmd){
		imgSeq = imgSeqCmd;
		if (imgSeq <= mTestImg.length) {
			Message message = new Message();
			message.what = imgSeq;
			mHandler.sendMessage(message);
		} else {
			imgSeq = mTestImg.length;
		}
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
				if (brightnessState == 0) {
					// mBrightness = 0.01f;
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
					InputStream mInputSream = getResources().openRawResource(
							mTestImg[imgSeq]);
					Bitmap bitmap = BitmapFactory.decodeStream(mInputSream,
							null, option);
					BitmapDrawable bitmapDrawable = new BitmapDrawable(bitmap);
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
					//finish();
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
			}
			if (imgSeq != 0) {
				mHandler.postDelayed(mRunnable, 1000);
			}
			System.gc();
		}
	};

	@Override
	protected void onResume() {

		
		super.onResume();
	}

	@Override
	protected void onPause() {

		//mWakeLock.release();
		super.onPause();

		mHandler.removeCallbacks(mRunnable);
		setResult(RESULT_CANCELED);

	}
}
