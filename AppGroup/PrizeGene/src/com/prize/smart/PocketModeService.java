/*******************************************
 *版权所有©2015,深圳市铂睿智恒科技有限公司
 *
 *内容摘要：口袋模式
 *当前版本：
 *作	者：钟卫林
 *完成日期：20150413
 *修改记录：
 *修改日期：
 *版 本 号：
 *修 改 人：
 *修改内容：
 *********************************************/

package com.prize.smart;

import com.prize.smart.gene.PrizeLogs;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.provider.Settings;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.WindowManager;

public class PocketModeService extends Service {
	private static final String TAG = "prize";
	private static final int MSG_LIGHT_SENSOR_VALUE = 3;
	private static final int MSG_PX_SENSOR_VALUE = 2;
	private static final int MSG_SENSOR_ACTION_VALUE = 1;
	private TelephonyManager mTelephonyManager;
	private AudioManager mAudioManager;
	private PhoneCallListener mPhoneListener;
	private SensorManager mSensorManager;
	private SensorPocketListener mSensorListener;
	private LightSensorPocketListener mLightSensorListener;
	private Sensor mSensor;
	private Sensor mPxSensor;
	private WindowManager.LayoutParams wmParams = null;
	private boolean mSensorLightState = false;
	private boolean mSensorPxState = false;
	private boolean mPocketModeState = true;
	private int mLightConut = 0;
	public int currentVolume = 0;
	public static int maxVolume = 0;
	public boolean currentAudioState = false; // true:非静音模式；false：静音模式
	private static int curringerMode = 0;

	@Override
	public IBinder onBind(Intent intent) {

		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void onCreate() {
		// TODO Auto-generated method stub

		// 电话管理器实例化：用来获取访问与手机通讯相关的状态和信息的get方法。
		mTelephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
		// 媒体管理器实例化
		mAudioManager = (AudioManager) this.getSystemService(AUDIO_SERVICE);
		maxVolume = mAudioManager.getStreamMaxVolume(AudioManager.STREAM_RING);
		currentVolume = mAudioManager.getStreamVolume(AudioManager.STREAM_RING);
		// 监听电话状态，PhoneCallListener是我们自定义的一个监听类
		mPhoneListener = new PhoneCallListener();
		// 注册电话状态监听器
		mTelephonyManager.listen(mPhoneListener, PhoneCallListener.LISTEN_CALL_STATE);
		// 传感器实例化
		mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);// 获取加速度传感器管理器
		// 传感器监听器，SensorListener是我们自定义的一个监听类
		if (mSensorListener == null) {
			mSensorListener = new SensorPocketListener();
		}
		if (mLightSensorListener == null) {
			mLightSensorListener = new LightSensorPocketListener();
		}
		PrizeLogs.v(TAG, "---->>>>> PocketMode Service--------------");
	}

	// START_STICKY:兼容模式service异常关闭后系统自动重启
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		// TODO Auto-generated method stub
		Log.v(TAG, "--------PocketModeService.onStartCommand...");
		return START_STICKY;
	}

	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		// 注销监听器
		mTelephonyManager.listen(mPhoneListener, 0);
		Intent turnOverServiceIntent = new Intent(this, PocketModeService.class);
		startService(turnOverServiceIntent);
	}

	Handler handler = new Handler();
	Runnable runnable = new Runnable() {
		@Override
		public void run() {
			setAudioVolume();
			handler.postDelayed(runnable, 500);
		}
	};

	private void setAudioVolume() {
		int tempValume = mAudioManager.getStreamVolume(AudioManager.STREAM_RING);
		if (tempValume < maxVolume) {
			mAudioManager.adjustStreamVolume(AudioManager.STREAM_RING, AudioManager.ADJUST_RAISE,
					AudioManager.FX_FOCUS_NAVIGATION_UP);
		}
	}

	/**
	 * 类描述：电话状态监听
	 * 
	 * @author 作者 zhongweilin
	 * @version 版本
	 */

	private class PhoneCallListener extends PhoneStateListener {
		@Override
		public void onCallStateChanged(int state, String incomingNumber) {
			// TODO Auto-generated method stub
			switch (state) {
			case TelephonyManager.CALL_STATE_IDLE:// 电话处于待机状态
				try {
					Thread.sleep(500);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				setPocketModeValue(TelephonyManager.CALL_STATE_IDLE);
				Log.v(TAG, "*PocketMod**TelephonyManager.CALL_STATE_IDLE ********");
				break;
			case TelephonyManager.CALL_STATE_OFFHOOK:// 电话处于通话状态
				/*prize-add-volume does not recover bugid:47612-yaoshu-20180116 begin*/
				/*setPocketModeValue(TelephonyManager.CALL_STATE_OFFHOOK);*/
				/*prize-add-volume does not recover bugid:47612-yaoshu-20180116 end*/
				Log.v(TAG, "*PocketMod**TelephonyManager.CALL_STATE_OFFHOOK ********");
				break;
			case TelephonyManager.CALL_STATE_RINGING:// 电话处于来电状态
				setPocketModeValue(TelephonyManager.CALL_STATE_RINGING);
				Log.v(TAG, "*PocketMod**TelephonyManager.CALL_STATE_RINGING ********");
				break;
			}
			super.onCallStateChanged(state, incomingNumber);
		}
	}

	private void setPocketModeValue(int callStateIdle) {
		int antifakeTouch = Settings.System.getInt(getContentResolver(), Settings.System.PRIZE_POCKET_MODE, 0);
		if (antifakeTouch == 1) {
			if (callStateIdle == TelephonyManager.CALL_STATE_RINGING) {
				curringerMode = mAudioManager.getRingerMode();
				currentVolume = mAudioManager.getStreamVolume(AudioManager.STREAM_RING);
				currentAudioState = !((curringerMode == AudioManager.RINGER_MODE_SILENT)
						|| (curringerMode == AudioManager.RINGER_MODE_VIBRATE));
				PrizeLogs.v(TAG, "**PocketMode***currentAudioState = " + currentAudioState);
				if (currentAudioState) {
					PrizeLogs.v(TAG, "**setPocketModeValue***maxVolume = " + maxVolume + "**** currentVolume = "
							+ currentVolume);
					mPocketModeState = true;
					mSensorPxState = false;
					mSensorLightState = false;
					registerListenerSensor();
				}
			} else {
				if (currentAudioState) {
					int tempTurnOverVolume = mAudioManager.getStreamVolume(AudioManager.STREAM_RING);
					if (tempTurnOverVolume != currentVolume) {
						if (currentAudioState) {
							PrizeLogs.v(TAG, "**PocketMode***maxVolume = " + maxVolume + "**** currentVolume = "
									+ currentVolume);
							setRingVolume(AudioManager.STREAM_RING, currentVolume);
						}
					}
					currentAudioState = false;
					mLightConut = 0;
					unregisterListenerSensor();
				}
			}
		}
	}

	private void registerListenerSensor() {
		if (mSensorManager != null) {
			/* 注册监听光线传感器 */
			mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);
			if (mSensor != null) {
				Log.v(TAG, "***PocketMod* open L-Sensor*****");
				mSensorManager.registerListener(mLightSensorListener, mSensor, SensorManager.SENSOR_DELAY_FASTEST);
			} else {
				Log.v(TAG, "***PocketMod* open L-Sensor is fail *****");
				stopSelf();
			}
			/* 注册监听距离传感器 */
			mPxSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);
			if (mPxSensor != null) {
				Log.v(TAG, "**PocketMod** open Px-Sensor *****");
				mSensorManager.registerListener(mSensorListener, mPxSensor, SensorManager.SENSOR_DELAY_FASTEST);
			} else {
				Log.v(TAG, "**PocketMod** open Px-Sensor is fail *****");
				stopSelf();
			}
		}
		Log.v(TAG, "**PocketMod** registerListenerSensor *****");
	}

	private void unregisterListenerSensor() {
		if (mSensorManager != null) {
			mSensorManager.unregisterListener(mSensorListener);
			mSensorManager.unregisterListener(mLightSensorListener);
		}
		Log.v(TAG, "**PocketMode** unregisterListenerSensor *****");
	}
	
	/**
	 * 方法描述：设置定时器，定时结束时注销sensor并取消防误触界面
	 * @see 类名/完整类名/完整类名#方法名
	 */
	CountDownTimer mCountDownTimer = new CountDownTimer(1000,800) {  
		@Override
		public void onTick(long millisUntilFinished) {
			Log.v(TAG, "**PocketMod-----> CountDownTimer--->onTick()");
		}
		@Override
		public void onFinish() {
			Log.v(TAG, "**PocketMod-----> CountDownTimer--->onFinish()");
			unregisterListenerSensor();
		}  
	 };  

	private Handler mHandler = new Handler() {
		public void dispatchMessage(Message msg) {
			if(mCountDownTimer != null){
				mCountDownTimer.start();
			}
			if (MSG_SENSOR_ACTION_VALUE == msg.what) {
				if (mSensorPxState && mSensorLightState) {
					Log.v(TAG, "***PocketMod**** isPocketModeOn ********");
					setRingVolume(AudioManager.STREAM_RING, maxVolume);
					if(mCountDownTimer != null){
						mCountDownTimer.cancel();
					}
				}
			} else if (MSG_LIGHT_SENSOR_VALUE == msg.what) {
				if (mSensorManager != null) {
					mSensorManager.unregisterListener(mLightSensorListener);
				}
			} else if (MSG_PX_SENSOR_VALUE == msg.what) {
				if (mSensorManager != null) {
					mSensorManager.unregisterListener(mSensorListener);
				}
			}
		};
	};
	
	private void sendHandlerMessage(int msg){
		if(mCountDownTimer != null){
			mCountDownTimer.cancel();
		}
		mHandler.sendMessage(mHandler.obtainMessage(msg));
	}

	/**
	 * 类描述： Px-Sensor 监听
	 * 
	 * @author 作者 zhongweilin
	 * @version 版本
	 */
	private class SensorPocketListener implements SensorEventListener {
		public void onAccuracyChanged(Sensor sensor, int accuracy) {
			// TODO Auto-generated method stub
		}

		public void onSensorChanged(SensorEvent event) {
			if (!mSensorPxState) {
				Log.v(TAG, "**PocketMod*** event.values[0] = " + event.values[0] + " *****");
				if (event.values[0] == 0) {
					mSensorPxState = true;
					sendHandlerMessage(MSG_SENSOR_ACTION_VALUE);
				} else {
					// Log.v(TAG, "***PocketMod**** isPocketModOff ********");
					if (mLightConut >= 5) {
						Log.v(TAG, "***PocketMod**** unregisterListener PX ********");
						sendHandlerMessage(MSG_PX_SENSOR_VALUE);
					} else {
						mSensorPxState = false;
					}
				}
			}
		}
	}

	/**
	 * 类描述： light-Sensor 监听
	 * 
	 * @author 作者 zhongweilin
	 * @version 版本
	 */
	private class LightSensorPocketListener implements SensorEventListener {
		public void onAccuracyChanged(Sensor sensor, int accuracy) {
			// TODO Auto-generated method stub
		}

		public void onSensorChanged(SensorEvent event) {
			if (!mSensorLightState) {
				Log.v(TAG, "Light---PocketMod---->event..values[0] = " + event.values[0]);
				// Log.v(TAG, "Light------->event..values[1] = " +
				// event.values[1]);
				// Log.v(TAG, "Light------->event..values[2] = " +
				// event.values[2]);
				mLightConut++;
				if (event.values[0] <= 400) {
					mSensorLightState = true;
					sendHandlerMessage(MSG_SENSOR_ACTION_VALUE);
				} else {
					if (mLightConut >= 5) {
						Log.v(TAG, "***PocketMod**** unregisterListener Light ********");
						sendHandlerMessage(MSG_LIGHT_SENSOR_VALUE);
					} else {
						mSensorLightState = false;
					}
				}
			}
		}
	}

	public void setRingVolume(final int streamType, final int volume) {
		// mAudioManager.setStreamVolume(AudioManager.STREAM_RING, 0,
		// AudioManager.FLAG_SHOW_UI);
		new Thread(new Runnable() {
			public void run() {
				Log.v(TAG, "***PocketModeService***volume = " + volume);
				mAudioManager.setStreamVolume(streamType, volume, 0);
			}
		}, "setVolume").start();
	}

}
