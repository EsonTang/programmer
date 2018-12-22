/*******************************************
 *版权所有©2015,深圳市铂睿智恒科技有限公司
 *
 *内容摘要：翻转静音服务
 *当前版本：
 *作	者：zhongweilin
 *完成日期：20150408
 *修改记录：
 *修改日期：
 *版 本 号：
 *修 改 人：
 *修改内容：
 *********************************************/

package com.prize.smart;

import java.lang.reflect.Method;
import java.util.List;
import com.prize.smart.gene.PrizeGene;
import com.prize.smart.gene.PrizeLogs;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.os.IBinder;
import android.provider.Settings;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;

public class TurnOverToPauseService extends Service {
	private static final String TAG = "prize";

	private TelephonyManager mTelephonyManager;
	private SensorManager mSensorManager;
	private GSensorListener mGSensorListener;
	private PxSensorListener mPxSensorListener;
	private AudioManager mAudioManager;
	public static boolean mTurnOverSensorState = true; // true:up false:down
	private Sensor mGsensor;
	private Sensor mPxSensor;

	private PhoneCallListener mPhoneListener;

	/* 当前数据是否被改变标志位，false 没有改变，true 改变 */
	private boolean mSensorState = false;
	private static boolean ringerModeChange = true;
	private static int ringerMode = 0;
	public static int currentTurnOverVolume = 0;
	public boolean curAudioState = false; // true:非静音模式；false：静音模式

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
		// 传感器实例化
		mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);// 获取加速度传感器管理器
		// 媒体管理器实例化
		mAudioManager = (AudioManager) this.getSystemService(Context.AUDIO_SERVICE);
		ringerMode = mAudioManager.getRingerMode();
		currentTurnOverVolume = mAudioManager.getStreamVolume(AudioManager.STREAM_RING);
		// 监听电话状态，PhoneCallListener是我们自定义的一个监听类
		mPhoneListener = new PhoneCallListener();
		// 注册电话状态监听器
		mTelephonyManager.listen(mPhoneListener, PhoneCallListener.LISTEN_CALL_STATE);
		// 传感器监听器，SensorListener是我们自定义的一个监听类
		if (mGSensorListener == null) {
			mGSensorListener = new GSensorListener();
		}
		if(mPxSensorListener == null){
			mPxSensorListener = new PxSensorListener();
		}		

		PrizeLogs.v(TAG, "---->>>>> TurnOverToPauseService--------------");
	}

	// START_STICKY:兼容模式service异常关闭后系统自动重启
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		// TODO Auto-generated method stub
		Log.v(TAG, "--------TurnOverToPauseService.onStartCommand...");
		return START_STICKY;
	}

	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		// 注销监听器
		mTelephonyManager.listen(mPhoneListener, 0);
		unregisterSensorListener();
		Intent turnOverServiceIntent = new Intent(this, TurnOverToPauseService.class);
		startService(turnOverServiceIntent);
	}

	/**
	 * 注册sensorlistener
	 */
	private void registerSensorListener() {
		if (mGsensor == null) {
			mGsensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);// 获取G-Sensor
		}
		if (mGsensor != null) {// 注册感应器监听
			mSensorManager.registerListener(mGSensorListener, mGsensor, SensorManager.SENSOR_DELAY_NORMAL);
		} else {// 没有对应的Sensor，取消
			stopSelf();
		}
		if(mPxSensor == null){
			mPxSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);
		}	
                if (mPxSensor != null) {
                        mSensorManager.registerListener(mPxSensorListener, mPxSensor,
                                        SensorManager.SENSOR_DELAY_FASTEST);
                } else {
                        stopSelf();
                }

	}

	/**
	 * 注销sensorlistener
	 */
	private void unregisterSensorListener() {
		/* 为手机服务信息管理器和传感器解除注册 */
		mSensorManager.unregisterListener(mGSensorListener);
		mSensorManager.unregisterListener(mPxSensorListener);
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
				setCallFlipSilentValue(TelephonyManager.CALL_STATE_IDLE);
				break;
			case TelephonyManager.CALL_STATE_OFFHOOK:// 电话处于通话状态
				setCallFlipSilentValue(TelephonyManager.CALL_STATE_OFFHOOK);
				break;
			case TelephonyManager.CALL_STATE_RINGING:// 电话处于来电状态
				setCallFlipSilentValue(TelephonyManager.CALL_STATE_RINGING);
				break;
			}
			super.onCallStateChanged(state, incomingNumber);
		}

	}

	/**
	 * 方法描述： 设置翻转静音的值
	 * 
	 * @param 参数名
	 *            说明 根据来电的状态控制功能是否打开
	 * @return 返回类型 说明
	 * @see 类名/完整类名/完整类名#方法名
	 */
	private void setCallFlipSilentValue(int callStatus) {
		int flipSilent = Settings.System.getInt(getContentResolver(), Settings.System.PRIZE_FLIP_SILENT, 0);
		if (flipSilent == 1) {
			if (callStatus == TelephonyManager.CALL_STATE_RINGING) {
				ringerMode = mAudioManager.getRingerMode();
				currentTurnOverVolume = mAudioManager.getStreamVolume(AudioManager.STREAM_RING);
				curAudioState = !((ringerMode == AudioManager.RINGER_MODE_SILENT) || (ringerMode == AudioManager.RINGER_MODE_VIBRATE));
				if (curAudioState) {
					registerSensorListener();
				}
			} else {
				if (curAudioState) {
					mTurnOverSensorState = true;
					int tempTurnOverVolume = mAudioManager.getStreamVolume(AudioManager.STREAM_RING);
					if (tempTurnOverVolume != currentTurnOverVolume) {
						Log.v(TAG, "**TurnOverToPauseService****curAudioState = " + curAudioState);
						setRingVolume(AudioManager.STREAM_RING, currentTurnOverVolume);
						mAudioManager.setRingerMode(ringerMode);
					}
					ringerModeChange = true;
					unregisterSensorListener();
				}
			}
		}
	}

	/**
	 * 类描述： G-Sensor 监听
	 * 
	 * @author 作者 zhongweilin
	 * @version 版本
	 */
	private class GSensorListener implements SensorEventListener {
		public void onAccuracyChanged(Sensor sensor, int accuracy) {
			// TODO Auto-generated method stub
		}

		public void onSensorChanged(SensorEvent event) {
			// TODO Auto-generated method stub
			/* 屏幕面朝下且来电时，铃声模式设置为静音，否则设置为普通模式 */
            Log.v(TAG, "**TurnOver**GSensorListener*** event.values[2] = " + event.values[2]);
			if(mSensorState){
                Log.v(TAG, "***TurnOverSensor  mTurnOverSensorState = "+mTurnOverSensorState+" ****");
				if (mTurnOverSensorState) {
					// mAudioManager.setRingerMode(ringerMode);
					if (!(event.values[2] < 4)) {
						mTurnOverSensorState = false;// 检测到屏幕朝上时
						Log.v(TAG, "***TurnOverSensor  screen is up  ****");
					}
				} else {
					if (event.values[2] < -5) {
						// RingerMode 和AudioManager.RINGER_MODE_SILENT ,
						// AudioManager.RINGER_MODE_NORMAL ,
						// AudioManager.RINGER_MODE_VIBRATE
                        Log.v(TAG, "***TurnOverSensor  screen is down  ****");
						if (ringerModeChange && curAudioState) {
							setRingVolume(AudioManager.STREAM_RING, 0);
							ringerModeChange = false;
						}
					}
				}
			}
		}
	}
	private class PxSensorListener implements SensorEventListener {
                public void onAccuracyChanged(Sensor sensor, int accuracy) {
                        // TODO Auto-generated method stub
                }

		public void onSensorChanged(SensorEvent event) {
			Log.v(TAG, "****PxSensorListener*** event.values[0] = " + event.values[0]);
			if (event.values[0] == 0) {
				mSensorState = false;
			} else {
				mSensorState = true;
			}
		}
        }


	public void setRingVolume(final int streamType, final int volume) {
		// mAudioManager.setStreamVolume(AudioManager.STREAM_RING, 0,
		// AudioManager.FLAG_SHOW_UI);
		new Thread(new Runnable() {
			public void run() {
				Log.v(TAG, "***TurnOverToPauseService***volume = " + volume);
				mAudioManager.setStreamVolume(streamType, volume, 0);
			}
		}, "setVolume").start();
	}
}
