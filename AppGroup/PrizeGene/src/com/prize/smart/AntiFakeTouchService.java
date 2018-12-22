/*******************************************
 *版权所有©2015,深圳市铂睿智恒科技有限公司
 *
 *内容摘要：防误触模式功能实现
 *当前版本：
 *作	者：钟卫林
 *完成日期：2015-04-21
 *修改记录：
 *修改日期：
 *版 本 号：
 *修 改 人：
 *修改内容：
...
 *修改记录：
 *修改日期：
 *版 本 号：
 *修 改 人：
 *修改内容：
 *********************************************/

package com.prize.smart;

import com.prize.smart.CoverViewBase.Callback;
import com.prize.smart.gene.PrizeGene;
import com.prize.smart.gene.PrizeLogs;

import android.app.KeyguardManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.provider.Settings;
import android.graphics.Matrix;
import android.graphics.PixelFormat;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.os.SystemClock;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;

public class AntiFakeTouchService extends Service implements OnTouchListener {

	public static final String TAG = "prize";
	public static final long TIME_INTERVAL = 750;// 间隔时间
	private SensorManager mSensorManager;
	private Sensor mSensor;
	private Sensor mPxSensor;
	protected CoverView01 pushView;
	private Display mDisplay;
	private Matrix mDisplayMatrix;
	private DisplayMetrics mDisplayMetrics;
	private int heightPixels;
	private WindowManager.LayoutParams wmParams = null;
	public static boolean mSensorState = true;
	private static boolean mViewShow = false;
	private boolean mTouchDown = false;
	private boolean isDelayTime = true;// 标记是否时间推迟
	private boolean isScreenOn = false;// 标记屏幕是否点亮
	private boolean isFristFlag = true;// 标记第一次响应
	private long mLastTime = 0;// 记录上一次时间

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	private PrizeGene mPrizeGene = null;

	@Override
	public void onCreate() {
		super.onCreate();

		mPrizeGene = new PrizeGene(this, PrizeGene.ANTIFAKE_TOUCH_MODE) {
			@Override
			public void onAction(int action, Object params) {
				PrizeGene.PrizeGeneImplAntifakeTouchResult mResult = (PrizeGene.PrizeGeneImplAntifakeTouchResult) params;
				long curTime = SystemClock.uptimeMillis() / 1000;/* 当前时间 */
				Log.v(TAG, "******* AntiFakeTouchOn -----> isAntifakeTouch == " + mResult.isAntifakeTouch);
				if (mResult.isAntifakeTouch) {
					Log.v(TAG, "******* AntiFakeTouchOn00 -----> mLastTime == " + mLastTime);
					if (!mSensorState) {
						Log.v(TAG, "******* isAntiFakeTouchOn ********");
						enablePointerLocation();
						mSensorState = true;
						isFristFlag = false;
					}
					if (mCountDownTimer != null) {
						Log.v(TAG, "******* AntiFakeTouchOn111 -----> mCountDownTimer.cancel() ");
						mCountDownTimer.cancel();
					}
					Log.v(TAG, "******* AntiFakeTouchOn111 -----> mLastTime == " + mLastTime);
				} else {
					if (!isFristFlag) {
						if (mCountDownTimer != null) {
							Log.v(TAG, "******* AntiFakeTouchOn111 -----> mCountDownTimer.start() ");
							mCountDownTimer.start();
						}
					} else {
						Log.v(TAG, "******* AntiFakeTouch OFF -----> stop()");
						mPrizeGene.stop();
					}
				}
			}
		};
		PrizeLogs.v(TAG, "---->>>>> AntiFakeTouchService--------------");

		final IntentFilter filter = new IntentFilter();
		filter.addAction(Intent.ACTION_SCREEN_OFF);
		filter.addAction(Intent.ACTION_SCREEN_ON);
		registerReceiver(mBatInfoReceiver, filter);
	}

	/**
	 * 方法描述：判断是否解锁
	 * 
	 * @param 参数名
	 *            说明
	 * @return 返回类型 说明
	 * @see 类名/完整类名/完整类名#方法名
	 */
	private boolean isKeyguardLocked() {
		KeyguardManager mKeyguardManager = (KeyguardManager) getSystemService(Context.KEYGUARD_SERVICE);
		if (mKeyguardManager.isKeyguardLocked()) {
			return true;
		}
		return false;
	}

	/**
	 * 方法描述：设置定时器，定时结束时注销sensor并取消防误触界面
	 * 
	 * @see 类名/完整类名/完整类名#方法名
	 */
	CountDownTimer mCountDownTimer = new CountDownTimer(TIME_INTERVAL, 700) {
		@Override
		public void onTick(long millisUntilFinished) {
			Log.v(TAG, "**AntiFakeTouchOn-----> CountDownTimer--->onTick()");
		}

		@Override
		public void onFinish() {
			Log.v(TAG, "**AntiFakeTouchOn-----> CountDownTimer--->onFinish()");
			disablePointerLocation();
			mPrizeGene.stop();
		}
	};

	/**
	 * 方法描述：亮屏/灭屏监听
	 * 
	 * @param 参数名
	 *            说明
	 * @return 返回类型 说明
	 * @see 类名/完整类名/完整类名#方法名
	 */
	private final BroadcastReceiver mBatInfoReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			final String action = intent.getAction();
			if (Intent.ACTION_SCREEN_ON.equals(action)) {
				Log.d(TAG, "-----------------screen is on...");
				isScreenOn = true;
				int antifakeTouch = Settings.System.getInt(getContentResolver(), Settings.System.PRIZE_ANTIFAKE_TOUCH,
						0);
				if (antifakeTouch == 1) {
					mPrizeGene.start();
				}
			} else if (Intent.ACTION_SCREEN_OFF.equals(action)) {
				Log.v(TAG, "----------------- screen is off...");
				isScreenOn = false;
				isFristFlag = true;
				isDelayTime = true;
				disablePointerLocation();
				mPrizeGene.stop();
			}
		}
	};

	/**
	 * 方法描述：显示UI
	 * 
	 * @param 参数名
	 *            说明
	 * @return 返回类型 说明
	 * @see 类名/完整类名/完整类名#方法名
	 */
	private void enablePointerLocation() {
		initDisplayParams();
		if (mViewShow) {
			mViewShow = false;
			WindowManager wm = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
			if (pushView != null) {
				wm.removeView(pushView);
				pushView = null;
			}
		}

		if (pushView == null) {
			pushView = new CoverView01(getApplication());// (CoverViewBase)
															// LayoutInflater.from(this).inflate(
															// R.layout.layout_cover1,
															// null, true);
			pushView.setCallback(new Callback() {
				@Override
				public void onDoubleClick() {

					// TODO Auto-generated method stub
					disablePointerLocation();
					mPrizeGene.stop();
				}
			});

		}
		if (wmParams == null) {
			initLayoutParams();
		}
		if (!mViewShow) {
			mViewShow = true;
			WindowManager wm = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
			wm.addView(pushView, wmParams);
		}
	}

	public void initLayoutParams() {
		wmParams = new WindowManager.LayoutParams();
		wmParams.type = 2024;
		wmParams.flags = 0x20520;
		wmParams.x = 0x0;
		wmParams.y = 0x0;
		wmParams.width = mDisplayMetrics.widthPixels;
		wmParams.height = heightPixels;// mDisplayMetrics.heightPixels;
		wmParams.format = 0x1;
		wmParams.windowAnimations = 0x0;
	}

	private void initDisplayParams() {
		WindowManager windowManager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
		mDisplayMatrix = new Matrix();
		mDisplay = windowManager.getDefaultDisplay();
		mDisplayMetrics = new DisplayMetrics();
		mDisplay.getMetrics(mDisplayMetrics);

		heightPixels = mDisplayMetrics.heightPixels;
		Log.i(TAG, "---heightPixels--0 = " + heightPixels);
		if (Build.VERSION.SDK_INT >= 14 && Build.VERSION.SDK_INT < 17)
			try {
				heightPixels = (Integer) Display.class.getMethod("getRawHeight").invoke(mDisplay);

				Log.i(TAG, "---heightPixels--1 = " + heightPixels);
			} catch (Exception ignored) {
			}
		else if (Build.VERSION.SDK_INT >= 17)
			try {
				android.graphics.Point realSize = new android.graphics.Point();
				Display.class.getMethod("getRealSize", android.graphics.Point.class).invoke(mDisplay, realSize);
				heightPixels = realSize.y;
				Log.i(TAG, "---heightPixels--2 = " + heightPixels);
			} catch (Exception ignored) {
			}

		Log.i(TAG, "---heightPixels--3 = " + heightPixels);
	}

	/**
	 * 方法描述：关闭UI
	 * 
	 * @param 参数名
	 *            说明
	 * @return 返回类型 说明
	 * @see 类名/完整类名/完整类名#方法名
	 */
	private void disablePointerLocation() {
		if (mViewShow) {
			mViewShow = false;
			WindowManager wm = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
			if (pushView != null) {
				wm.removeView(pushView);
				pushView = null;
			}
		}
		mSensorState = false;
	}

	// START_STICKY:兼容模式service异常关闭后系统自动重启
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		// TODO Auto-generated method stub
		Log.v(TAG, "--------AntiFakeTouchService.onStartCommand...");
		return START_STICKY;
	}

	public void onDestroy() {
		unregisterReceiver(mBatInfoReceiver);
		Log.v(TAG, "*****AntiFakeTouchService.onDestroy()*****");
		Intent turnOverServiceIntent = new Intent(this, AntiFakeTouchService.class);
		startService(turnOverServiceIntent);
	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		int eventaction = event.getAction();
		switch (eventaction) {
		case MotionEvent.ACTION_DOWN:
			mTouchDown = false;
			break;
		}
		return false;
	};

}
