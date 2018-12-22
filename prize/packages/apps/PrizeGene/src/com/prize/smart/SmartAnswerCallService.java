/*******************************************
 *版权所有©2015,深圳市铂睿智恒科技有限公司
 *
 *内容摘要：
 *当前版本：
 *作	者：
 *完成日期：
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

import com.prize.smart.gene.PrizeGene;
import com.prize.smart.gene.PrizeLogs;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import android.app.Instrumentation;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.os.IBinder;
import android.os.Vibrator;
import android.provider.Settings;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.KeyEvent;
import com.android.internal.telephony.ITelephony;

public class SmartAnswerCallService extends Service {
	private static final String TAG = "prize";

	private TelephonyManager mAnswerCallTelephonyManager;
	private SensorManager mAnswerCallSensorManager;
	private GSensorListener mAnswerCallGSensorListener;
	private AudioManager mAnswerCallAudioManager;
	public static boolean mAnswerCallSensorState = true; // true:up false:down
	/* prize-modify-bugid 31875-lijimeng-20170410-start*/
	//public static boolean mAnswerCallGSensorState = true; // true:up false:down
	public static boolean mAnswerCallGSensorState = false; // true:up false:down
	/* prize-modify-bugid 31875-lijimeng-20170410-end*/
	private Sensor mAnswerCallGsensor;
	private static Vibrator mVibrator;  //震动
	private PxSensorListener mAnswerCallPxSensorListener;
	private Sensor mAnswerCallSensor;

	private PhoneCallListener mAnswerCallPhoneListener;

	PrizeGene mPrizeGeneSmartAnswerCall = null;
	
	private long MIN_CHANGE_TIME = 10;//ms
	private long MAX_CHANGE_TIME = 1500;//ms
	private float[] lastValue = new float[3];
	private long lastTime = 0;
	/* 当前数据是否被改变标志位，false 没有改变，true 改变 */
	private boolean isSmartAnswer = false;
	private boolean isFristSensorState = true;
	private int sensorCount = 0;
	private long _lastTriggerTime = -1;

	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void onCreate() {
		// TODO Auto-generated method stub

		// 电话管理器实例化：用来获取访问与手机通讯相关的状态和信息的get方法。
		mAnswerCallTelephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
		// 传感器实例化
		mAnswerCallSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);// 获取加速度传感器管理器
		//震动器
		mVibrator = (Vibrator)getSystemService(Context.VIBRATOR_SERVICE);  
		// 媒体管理器实例化
		mAnswerCallAudioManager = (AudioManager) this.getSystemService(AUDIO_SERVICE);
		// 监听电话状态，PhoneCallListener是我们自定义的一个监听类
		mAnswerCallPhoneListener = new PhoneCallListener();
		// 注册电话状态监听器
		mAnswerCallTelephonyManager.listen(mAnswerCallPhoneListener, PhoneCallListener.LISTEN_CALL_STATE);
		// 传感器监听器，SensorListener是我们自定义的一个监听类
		if (mAnswerCallGSensorListener == null) {
			mAnswerCallGSensorListener = new GSensorListener();
		}
		if (mAnswerCallPxSensorListener == null) {
			mAnswerCallPxSensorListener = new PxSensorListener();
		}
		PrizeLogs.v(TAG, "---->>>>> SmartAnswerCallService--------------");
	}

	// START_STICKY:兼容模式service异常关闭后系统自动重启
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		// TODO Auto-generated method stub
		Log.v(TAG, "--------SmartAnswerCallService.onStartCommand...");
		return START_STICKY;
	}

	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		// 注销监听器
		mAnswerCallTelephonyManager.listen(mAnswerCallPhoneListener, 0);
		unregisterSensorListener();
		Intent answerCallServiceIntent = new Intent(this, SmartAnswerCallService.class);
		startService(answerCallServiceIntent);
	}

	/**
	 * 注册sensorlistener
	 */
	private void registerSensorListener() {
		if (mAnswerCallGsensor == null) {
			mAnswerCallGsensor = mAnswerCallSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
		}
		if (mAnswerCallGsensor != null) {// 注册感应器监听
			Log.v(TAG, "*AnswerCall*** open G-Sensor  *****");
			mAnswerCallSensorManager.registerListener(mAnswerCallGSensorListener, mAnswerCallGsensor,
					SensorManager.SENSOR_DELAY_FASTEST);
		} else {// 没有对应的Sensor，取消
			Log.v(TAG, "*AnswerCall*** open G-Sensor is fail *****");
			stopSelf();
		}

		mAnswerCallSensor = mAnswerCallSensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);
		if (mAnswerCallSensor != null) {
			Log.v(TAG, "*AnswerCall*** open P-Sensor *****");
			mAnswerCallSensorManager.registerListener(mAnswerCallPxSensorListener, mAnswerCallSensor,
					SensorManager.SENSOR_DELAY_FASTEST);
		} else {
			Log.v(TAG, "**AnswerCall** open P-Sensor is fail *****");
			stopSelf();
		}
	}

	/**
	 * 注销sensorlistener
	 */
	private void unregisterSensorListener() {
		/* 为手机服务信息管理器和传感器解除注册 */
		mAnswerCallSensorManager.unregisterListener(mAnswerCallGSensorListener);
		mAnswerCallSensorManager.unregisterListener(mAnswerCallPxSensorListener);
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
					setCallSmartAnswerValue(TelephonyManager.CALL_STATE_IDLE);
					Log.v(TAG, "*AnswerCall**TelephonyManager.CALL_STATE_IDLE ******");
					break;
				case TelephonyManager.CALL_STATE_OFFHOOK:// 电话处于通话状态
					setCallSmartAnswerValue(TelephonyManager.CALL_STATE_OFFHOOK);
					Log.v(TAG, "*AnswerCall**TelephonyManager.CALL_STATE_OFFHOOK********");
					break;
				case TelephonyManager.CALL_STATE_RINGING:// 电话处于来电状态
					setCallSmartAnswerValue(TelephonyManager.CALL_STATE_RINGING);
					Log.v(TAG, "*AnswerCall**TelephonyManager.CALL_STATE_RINGING********incomingNumber == " + incomingNumber);
					break;
			}
			super.onCallStateChanged(state, incomingNumber);
		}

	}

	/**
	 * 方法描述：设置智能接听的值
	 * 
	 * @param 参数名
	 *            说明 根据来电的状态控制功能是否打开
	 * @return 返回类型 说明
	 * @see 类名/完整类名/完整类名#方法名
	 */
	private void setCallSmartAnswerValue(int callStatus) {
		int mPrizeAnswerCall = Settings.System.getInt(getContentResolver(), Settings.System.PRIZE_SMART_ANSWER_CALL, 0);
		if (mPrizeAnswerCall == 1) {
			if (callStatus == TelephonyManager.CALL_STATE_RINGING) {
				registerSensorListener();
				isSmartAnswer = true;
			} else {
				isSmartAnswer = false;
				unregisterSensorListener();
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
			//Log.v(TAG, "##### event.values[2222222] = " + event.values[2] + " #####");
			//Log.v(TAG, "##### event.values[1111111] = " + event.values[1] + " #####");
			//Log.v(TAG, "##### event.values[0000000] = " + event.values[0] + " #####");
			if(isSmartAnswer){
				if (isEffectiveRange(event)) {
					//Log.v(TAG, "***GSensorListener**** SmartAnswerCall On ********");
					/* prize-modify-bugid 31875-lijimeng-20170410-start*/
					//if(isBetweenTrue(event) && mAnswerCallSensorState){
					if((isBetweenTrue(event) || mAnswerCallGSensorState) && mAnswerCallSensorState){
					/* prize-modify-bugid 31875-lijimeng-20170410-end*/
						simulateKeystroke(KeyEvent.KEYCODE_CALL);
						isSmartAnswer = false;
					}
				} else {
                    lastTime = 0;
			    //	Log.v(TAG, "***GSensorListener**** SmartAnswerCall Off ********");
				}
			}
		}
		
	}

    private boolean isEffectiveRange(SensorEvent event){
        boolean a = (event.values[2] < 9) && (event.values[2] > -5);
        boolean b = false;
        if((event.values[2] < 0) && (event.values[0] < 0)){
            b = event.values[1] > 6;
        }else{
            b = event.values[1] > 2;
        }
        //Log.v(TAG, "**SmartCall****isEffectiveRange()-->a = " +a+",b = "+b);
        return a && b;
    }
	
	private boolean isBetweenTrue(SensorEvent event){
		if(lastTime == 0){
			lastTime = event.timestamp;
			lastValue[0] = event.values[0];
			lastValue[1] = event.values[1];
			lastValue[2] = event.values[2];
		}
		//Log.v(TAG, "***GSensorListener**** event.timestamp == " + event.timestamp + "------lastTime == "+lastTime);
		long curTime = (event.timestamp-lastTime)/1000000;

		//Log.v(TAG, "***GSensorListener**** change time == " + curTime);
		if(curTime > MAX_CHANGE_TIME){
			lastTime = event.timestamp;
			lastValue[0] = event.values[0];
			lastValue[1] = event.values[1];
			lastValue[2] = event.values[2];
			mAnswerCallSensorState = false;
		}
		if(curTime > MIN_CHANGE_TIME){
			if((mathAbs(event.values[1] - lastValue[1]) > 4) || (mathAbs(event.values[2] - lastValue[2])>4) || 
                    (mathAbs(event.values[0] - lastValue[0])>4)){
				Log.v(TAG, "***GSensorListener**** is true ````````");
				mAnswerCallGSensorState = true;
				return true;
			}else {
			//	Log.v(TAG, "***GSensorListener**** is false ````````");
				return false;
			}
		}
		
		return false;
	}
	
	private float mathAbs(float f){
		return Math.abs(f);
	}

	/**
	 * 类描述： G-Sensor 监听
	 * 
	 * @author 作者 zhongweilin
	 * @version 版本
	 */
	private class PxSensorListener implements SensorEventListener {
		public void onAccuracyChanged(Sensor sensor, int accuracy) {
			// TODO Auto-generated method stub
		}

		public void onSensorChanged(SensorEvent event) {
			Log.v(TAG, "****PxSensorListener*** event.values[0] = " + event.values[0]);
			if (isSmartAnswer) {
				if (isFristSensorState) {
					if (event.values[0] != 0) {
						isFristSensorState = false;
					}
				} else {
					if (event.values[0] == 0) {
						//Log.v(TAG, "****PxSensorListener*** SmartAnswerCall On ********");
						mAnswerCallSensorState = true;
					} else {
						//Log.v(TAG, "***PxSensorListener**** SmartAnswerCall Off ********");
						mAnswerCallSensorState = false;
					}
				}
			}
		}
	}
	/**
	 * 方法描述：只能用于单卡时接听电话
	 * @param 参数名 说明
	 * @return 返回类型 说明
	 * @see 类名/完整类名/完整类名#方法名
	 */
	private void smartAnswerCalling() {
		// Log.v(TAG, "##### inCalling susscceful111111 #####");
		new Thread(new Runnable() {
			public void run() {
				try {
					mAnswerCallTelephonyManager.answerRingingCall();
					mVibrator.vibrate(250);
					Log.v(TAG, "## AnswerCalling ### inCalling susscceful #####");
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}).start();
	}
	
	/**
	 * 方法描述：可以用于单卡或者双卡接听电话
	 * @param 参数名 模拟接听电话的物理按键
	 * @return 返回类型 说明
	 * @see 类名/完整类名/完整类名#方法名
	 */
	public static void simulateKeystroke(final int KeyCode) {
		new Thread(new Runnable() {
			public void run() {
				try {
					/* prize-modify-bugid 31875-lijimeng-20170410-start*/
					mAnswerCallGSensorState = false;
					/* prize-modify-bugid 31875-lijimeng-20170410-end*/
					Instrumentation inst = new Instrumentation();
					inst.sendKeyDownUpSync(KeyCode);
					mVibrator.vibrate(250);
					Log.v(TAG, "## sendKey ### inCalling susscceful #####");
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}).start();
	}
}
