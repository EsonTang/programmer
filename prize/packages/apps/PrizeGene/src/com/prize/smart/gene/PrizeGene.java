
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

package com.prize.smart.gene;
import java.util.List;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

/**
 * 
 * @author Prize
 * 
 * usage example:
 * extends PrizeGene class, with you onAction implementation.
 * 
 * PrizeGene mPrizeGeneSpecific1 = new PrizeGene(context, PrizeGene.ICON_SHAKE_MOVE_MODE) {
 * 		@Override
 * 		public void onAction(int action, Object params) {
 * 			if(action == ?) {
 * 				// do something
 * 			}
 * 		}
 * }
 * 
 * mPrizeGeneSpecific1.start();
 * mPrizeGeneSpecific1.stop();
 * 
 * scenario: 
 * 
 * 图片浏览	-- 			倾斜以缩放, 移动设备以浏览图片
 * 短信,电话簿, 浏览 -- 	体感拨号
 * 音乐播放器 -- 			翻转以静音/暂停, 口袋模式, 双击至顶部, 手掌触摸以静音/暂停
 * 通话中 -- 			智能防窃听, 智能免提		--- 这个功能 ？
 * 来电 -- 	口袋模式
 * 外出时需要带上手机时 -- 		智能提醒
 * 						但这个需要在熄屏情况下使用sensor, how?
 * 
 * 在调整桌面上的图标位置时， 或者拖放窗口小部件时	
 * 						-- 	智能平移
 * 查看当前的bt或者wifi列表时, 其他需要更新的场合,不必去手动按更新按纽
 * 						 --	晃动以更新
 * 
 * 
 */
public abstract class PrizeGene {
	// class properties:
	private final static String TAG = "prize";

	public static enum gene_modes{
		
	}
	public final static int INVALID_MODE = 0;
	public final static int SMART_HANDFREE_MODE = 1; // 智能免提
	public final static int ANTI_EAVESDROP_MODE = 2; // 智能防窃听
	public final static int DIRECT_CALL_MODE = 3;	// 体感拨号
	public final static int POCKET_MODE = 4; // 口袋模式
		// new added
	public final static int ICON_SHAKE_MOVE_MODE = 5; // ifrank, 智能平移 (or need gyroscope!)
	public final static int SMART_ALERT_MODE = 6; // ifrank, 智能提醒
	public final static int ZOOM_BY_SLOPE_MODE = 7; // ifrank, 倾斜以缩放
	public final static int ZOOM_BY_SLOPE_MODEX = 8;
	public final static int ZOOM_BY_SLOPE_MODEY = 9;
	public final static int DOUBLE_CLICK_TO_TOP_MODE = 10; // ifrank, 双击至顶部
	public final static int SHAKE_TO_UPDATE_MODE = 11; // 晃动以更新
	public final static int OVERTURN_TO_PAUSE_MODE = 12; // 翻转以静音/暂停
		// need device feature support
	public final static int VIEW_BY_MOVING_MODE = 13; // ifrank, need gyroscope support! // 移动设备以浏览图片
	public final static int PALM_OVERLAY_TO_PAUSE_MODE = 14; // 手掌触摸以静音/暂停
	public final static int PALM_SLIDE_TO_CAPTURE_MODE = 15; // 手掌滑动以捕捉, [[[ 这个需要独立实现么? ]]]
	public final static int ACTION_TO_UNLOCK_MODE = 16; // 动作解锁
		// 
	public final static int DETECT_PHONE_PICK_UP_MODE = 17; 
	
	public final static int SMART_ANSWER_CALL_MODE  = 18;
	
	public final static int DISTANCE_OPERATION  = 19; //隔空操作
	
	public final static int ANTIFAKE_TOUCH_MODE  = 20; //防误触模式

	private final static String[] __geneStrs = { // 要和上面的定义一致！
		"INVALID_MODE", // 0
		"SMART_HANDFREE_MODE", // 1
		"ANTI_EAVESDROP_MODE", // 2
		"DIRECT_CALL_MODE", // 3
		"POCKET_MODE", // 4
		"ICON_SHAKE_MOVE_MODE", // 5
		"SMART_ALERT_MODE", // 6
		"ZOOM_BY_SLOPE_MODE", // 7
		"ZOOM_BY_SLOPE_MODEX", // 8
		"ZOOM_BY_SLOPE_MODEY", // 9
		"DOUBLE_CLICK_TO_TOP_MODE", // 10
		"SHAKE_TO_UPDATE_MODE", // 11
		"OVERTURN_TO_PAUSE_MODE", // 12
		"VIEW_BY_MOVING_MODE", // 13
		"PALM_OVERLAY_TO_PAUSE_MODE", // 14
		"PALM_SLIDE_TO_CAPTURE_MODE", // 15
		"ACTION_TO_UNLOCK_MODE", // 16
		"DETECT_PHONE_PICK_UP_MODE", // 17
		"SMART_ANSWER_CALL_MODE", // 18
		"DISTANCE_OPERATION",    //19
		"ANTIFAKE_TOUCH_MODE" //20
	};
	
	// Handler messages
	private static enum gene_actions{
		ACTION0,
		ACTION1,
		ACTION2,
		ACTION3,
		ACTION4,
		ACTION5,
		ACTION6,
		ACTION7,
		ACTION8,
		ACTION9,
		ACTION10,
		ACTION11,
		ACTION12,
		ACTION13,
		ACTION14,
		ACTION15
	}
	public final static int INVALID_ACTION = gene_actions.ACTION0.ordinal();
	public final static int SMART_HANDFREE_ACTION = gene_actions.ACTION1.ordinal();
	public final static int ANTI_EAVESDROP_ACTION = gene_actions.ACTION2.ordinal();
	public final static int DIRECT_CALL_ACTION = gene_actions.ACTION3.ordinal();
	public final static int POCKET_MODE_ACTION = gene_actions.ACTION4.ordinal();
	public final static int ICON_SHAKE_MOVE_ACTION = gene_actions.ACTION5.ordinal();
	public final static int SMART_ALERT_ACTION = gene_actions.ACTION6.ordinal();
	public final static int ZOOM_BY_SLOPE_ACTION = gene_actions.ACTION7.ordinal();
	public final static int DOUBLE_CLICK_TO_TOP_ACTION = gene_actions.ACTION8.ordinal();
	public final static int SHAKE_TO_UPDATE_ACTION = gene_actions.ACTION9.ordinal();
	public final static int OVERTURN_TO_PAUSE_ACTION = gene_actions.ACTION10.ordinal();
	public final static int ACTION_TO_UNLOCK_ACTION = gene_actions.ACTION11.ordinal();
	public final static int DETECT_PHONE_PICK_UP_ACTION = gene_actions.ACTION12.ordinal();
	public final static int SMART_ANSWER_CALL_ACTION = gene_actions.ACTION13.ordinal();
	public final static int DISTANCE_OPERATION_ACTION = gene_actions.ACTION14.ordinal();
	public final static int ANTIFAKE_TOUCH_MODE_ACTION = gene_actions.ACTION15.ordinal();
	
		/* process thread message id */
	public final static int PROC_THREAD_MSG_ID_ANTI_EAVESDROP_MODE_PROX_CHANGED = 1;
	
	// instance variables below
	public int mMode = INVALID_MODE;
	private int mDebugLevel = 0;

	// to be extended
	public abstract void onAction(int action, Object params);
	
	private Context mContext = null;
	
	private interface PrizeGeneMode {
		public abstract void start();
		public abstract void stop();
	}
	private PrizeGeneMode mPrizeGeneMode = null;
    
	/* constructor */
	private void __setCurGeneMode(int mode) {
		mMode = mode;
		switch (mMode) {
		case SMART_HANDFREE_MODE:
			//mPrizeGeneMode = new PrizeSmartHandFree();
			mPrizeGeneMode = new PrizeSmartHandFreeV2();
			break;
		case ANTI_EAVESDROP_MODE:
			//mPrizeGeneMode = new PrizeAntiWireTapingMode();
			mPrizeGeneMode = new PrizeAntiWireTapingModeV2();
			break;
		case DIRECT_CALL_MODE:
			//mPrizeGeneMode = new PrizeDirectCallMode();
			mPrizeGeneMode = new PrizeDirectCallModeV2();
			break;
		case POCKET_MODE:
			//mPrizeGeneMode = new PrizeGeneImplPocketMode();
			mPrizeGeneMode = new PrizeGeneImplPocketModeV2();
			break;
		//
		case ICON_SHAKE_MOVE_MODE:
			mPrizeGeneMode = new PrizeGeneImplIconShakeMoving();
			break;
		case SMART_ALERT_MODE:
			mPrizeGeneMode = new PrizeGeneImplSmartAlert();
			break;
		case ZOOM_BY_SLOPE_MODE:
			mPrizeGeneMode = new PrizeGeneImplZoomBySlope(2);
			break;
		case ZOOM_BY_SLOPE_MODEX:
			mPrizeGeneMode = new PrizeGeneImplZoomBySlope(0);
			break;
		case ZOOM_BY_SLOPE_MODEY:
			mPrizeGeneMode = new PrizeGeneImplZoomBySlope(1);
			break;
		case DOUBLE_CLICK_TO_TOP_MODE:
			mPrizeGeneMode = new PrizeGeneImplDoubleClickToTop();
			break;
		case SHAKE_TO_UPDATE_MODE:
			mPrizeGeneMode = new PrizeGeneImplShakeToUpdate();
			break;
		case OVERTURN_TO_PAUSE_MODE:
			mPrizeGeneMode = new PrizeGeneImplOverturnToPause();
			break;
		case ACTION_TO_UNLOCK_MODE:
			mPrizeGeneMode = new PrizeGeneImplActionToUnlock();
			break;
		case DETECT_PHONE_PICK_UP_MODE:
			mPrizeGeneMode = new PrizeGeneImplDetectPhonePickUp();
			break;
		case SMART_ANSWER_CALL_MODE:
			mPrizeGeneMode = new PrizeDirectCallModeV2();
			break;
		case DISTANCE_OPERATION:
			mPrizeGeneMode = new PrizeGeneImplDistanceOperation();
			break;
		case ANTIFAKE_TOUCH_MODE:
			mPrizeGeneMode = new PrizeGeneImplAntifakeTouch();
			break;
		default:
			break;
		}		
	}
	private void startHandlerThead() {
		// do nothing!
	}
	
	/**/
	public PrizeGene(Context context, int mode) {
		PrizeLogs.v(TAG, "---->>>>> PrizeGene: mode=" + mode + "<<<<<---------------");

		mContext = context;
		mDebugLevel = 0;
		__setCurGeneMode(mode);
		startHandlerThead();
	}
	public PrizeGene(Context context, int mode, int debugLevel) {
		PrizeLogs.v(TAG, "PrizeGene: mode=" + mode);
		
		mContext = context;
		mDebugLevel = debugLevel;
		__setCurGeneMode(mode);
		startHandlerThead();
	}
	
	/**/
	public int getGeneModeIndex() {
		return mMode;
	}
	public String getGeneModeString() {
		return __geneStrs[mMode];
	}

	/**/
	public void start() {
		if (mPrizeGeneMode != null) {
			PrizeLogs.v(TAG, "PrizeGene.start()");
			mPrizeGeneMode.start();
		}
	}
	public void stop() {
		if (mPrizeGeneMode != null) {
			PrizeLogs.v(TAG, "PrizeGene.stop()");
			mPrizeGeneMode.stop();
		}
	}
	private void doActionProxy(int action, Object params) {
		PrizeLogs.v(TAG, "doAction: action=" + action);
		onAction(action, params);
	};

	/***********************************************************************/
	/* following is all kinds of implementation */
	/***********************************************************************/
	
	
	/***********************************************************************/
	/*
	 * 智能免提 Prize_GENE_SMART_HANDFREE
	 * 
	 */
	private class PrizeSmartHandFree implements PrizeGeneMode, SensorEventListener {
		private SensorManager mPrizeSensorManager;
		private AudioManager mAudioManage;
		public PrizeSmartHandFree() {
			mPrizeSensorManager = (SensorManager)mContext.getSystemService(Context.SENSOR_SERVICE);
			mAudioManage = (AudioManager)mContext.getSystemService(Context.AUDIO_SERVICE);
		}
		@Override
		public void onAccuracyChanged(Sensor sensor, int accuracy) {
			// TODO Auto-generated method stub
		}
		@Override
		public void onSensorChanged(SensorEvent event) {
			int sensorType = event.sensor.getType();
			/* 判断是否插着耳机 */
			Boolean isWriedHeadSet = mAudioManage.isWiredHeadsetOn();
			/* 判断是否是扩音器状态 */
			Boolean isSpeak = mAudioManage.isSpeakerphoneOn();
			/* Log蓝牙连接状态 */
			Boolean isBluetooth = mAudioManage.isBluetoothScoOn();
			// PrizeLogs.i("zhaoyang",
			// "BLUETOOTH   "+Boolean.toString(isBluetoothAudioConnected()));
			/* Log扩音器状态 */
			// PrizeLogs.i("zhaoyang", "ISSPEAKER   "+Boolean.toString(isSpeak));
			/* Log耳机插入状态 */
			// PrizeLogs.i("zhaoyang", "EAR   "+Boolean.toString(isWriedHeadSet));
			if (sensorType == Sensor.TYPE_PROXIMITY 
					&& !isWriedHeadSet
					&& !isSpeak && !isBluetooth) {
				PrizeLogs.v(TAG, "value = " + event.values[1]);
			}
		}
		@Override
		public void start() {
			mPrizeSensorManager.registerListener(this, 
					mPrizeSensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY),
					SensorManager.SENSOR_DELAY_UI);
		}
		@Override
		public void stop() {
			mPrizeSensorManager.unregisterListener(this);
		}
	}
	
	/***********************************************************************/
	/*
	 * 体感拨号 Prize_GENE_DIRECT_CALL ?
	 * 
	 * i-frank, 2012.12.26
	 * calculateAngleForY, calculateAngleForZ就不知道了!
	 */
	private class PrizeDirectCallMode 
		implements PrizeGeneMode, SensorEventListener {

		private SensorManager sensorManager;
		private Sensor accelerometerSensor;
		private Sensor proximitySensor;

		public static final double GRAVITY = 9.81;
		public static final double DEGREE_PI = 180;
		private static final int SPEED_SHRESHOLD = 45; // 速度阈值

		private static final double ACCELERATION_SHRESHOLD_X = 1.0; // X轴方向加速度阈值
		private static final double ACCELERATION_SHRESHOLD_Y = 2.0; // Y轴方向加速度阈值
		private static final double ACCELERATION_SHRESHOLD_Z = 4.0; // Z轴方向加速度阈值

		private static final int UPTATE_INTERVAL_TIME = 70; // 两次检测的时间间隔

		private float PROXIMITY_THRESHOLD = 119.0f;// modified by chinn
													// 20120801 for 900

		// 手机上一个位置时重力感应坐标
		private float lastX;
		private float lastY;
		private float lastZ;

		private long lastUpdateTime = 0;// 上次检测时间
		private double speedDelay; // 记录瞬间的最大速度

		private boolean conditionsFlag = false; // 判断体感拨号时X、Y和Z轴方向的加速度是否满足所设定的阈值
		private boolean threadFlag = true;
		private double lastDegreeDblY;
		private double angleForY; // 记录Y轴瞬间变化的最大旋转角度
		private double maxDegreeDblY; // 记录瞬间的沿着Y轴转动的最大角度

		double speed;
		double g1 = GRAVITY;
		double degreeDblY;
		double mDegreeDblY;

		double g2 = GRAVITY;
		double degreeDblZ;
		double mDegreeDblZ;

		float accelerationForX; // 记录X轴方向的加速度
		float accelerationForY; // 记录Y轴方向的加速度
		float accelerationForZ; // 记录Z轴方向的加速度

		private float gravity[] = new float[3];
		private float linear_acceleration[] = new float[3];

		double currentTime = 0;
		double lastTime = 0; // 记录时间

		Handler prizeDirectCallHandler = new Handler();
		Runnable prizeDirectCallRunnable = new Runnable() {
			@Override
			public void run() {
				// TODO Auto-generated method stub
				speedDelay = 0.0;
				maxDegreeDblY = 0.0;
				conditionsFlag = false;
				threadFlag = true;
			}
		};

		public PrizeDirectCallMode() {
			if (sensorManager == null) {
				// 获取感应器管理
				sensorManager = (SensorManager) mContext.getSystemService(Context.SENSOR_SERVICE);
			}
			// 获取加速度传感器
			accelerometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
			// 获取距离传感器
			proximitySensor = sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);
		}

		@Override
		public void start() {
			sensorManager.registerListener(this, accelerometerSensor,SensorManager.SENSOR_DELAY_NORMAL);
			sensorManager.registerListener(this, proximitySensor,SensorManager.SENSOR_DELAY_NORMAL);
		}

		@Override
		public void stop() {
			sensorManager.unregisterListener(this, accelerometerSensor);
			sensorManager.unregisterListener(this, proximitySensor);
		}

		private void onSensorChangedAccelerometer(SensorEvent event) {
			conditionsFlag = false;
				// 
			long currentUpdateTime = System.currentTimeMillis();
			long timeInterval = currentUpdateTime - lastUpdateTime;
			if (timeInterval < UPTATE_INTERVAL_TIME) {
				return;
			}
			lastUpdateTime = currentUpdateTime;
			
			final float alpha = 0.8f; // 权重
			// 分别算出X,Y,Z重力方向的加速度
			gravity[0] = alpha * gravity[0] + (1 - alpha) * event.values[0];
			gravity[1] = alpha * gravity[1] + (1 - alpha) * event.values[1];
			gravity[2] = alpha * gravity[2] + (1 - alpha) * event.values[2];
			// 得到X,Y,Z各个方向的加速度，把重力加速度的影响去掉
			linear_acceleration[0] = event.values[0] - gravity[0];
			linear_acceleration[1] = event.values[1] - gravity[1];
			linear_acceleration[2] = event.values[2] - gravity[2];

			// Y轴方向不除去重力影响的加速度
			accelerationForX = event.values[0];
			accelerationForY = event.values[1];
			accelerationForZ = event.values[2];

			// 获得x,y,z各个方向的加速度的变化值
			float deltaX = linear_acceleration[0] - lastX;
			float deltaY = linear_acceleration[1] - lastY;
			float deltaZ = linear_acceleration[2] - lastZ;
			lastX = linear_acceleration[0];
			lastY = linear_acceleration[1];
			lastZ = linear_acceleration[2];

			// 计算手机绕Y轴旋转角度
			angleForY = calculateAngleForY(event.values[1]);

			// 计算手机绕Z轴旋转角度
			mDegreeDblZ = calculateAngleForZ(event.values[2]);

			speed = Math.sqrt(deltaX * deltaX + deltaY * deltaY + deltaZ * deltaZ)
					/ timeInterval * 10000;
			// PrizeLogs.i("xiaxiaoling", "speed=     " + speed);
			if (speed >= SPEED_SHRESHOLD && threadFlag == true) {
				PrizeLogs.i("xiaxiaoling", "speed      " + speed);
				if (angleForY >= 15.0) {
					maxDegreeDblY = angleForY;
					angleForY = 0.0;
					// PrizeLogs.i("xiaxiaoling", "angleForY2=      " +
					// angleForY);
				}
				speedDelay = speed;
				threadFlag = false;
				prizeDirectCallHandler.postDelayed(prizeDirectCallRunnable,2000);
			}
			if (Math.abs(accelerationForX) >= ACCELERATION_SHRESHOLD_X
					&& Math.abs(accelerationForZ) <= ACCELERATION_SHRESHOLD_Z
					&& mDegreeDblZ >= -40.0 && mDegreeDblZ <= 20.0
					&& maxDegreeDblY >= 15.0) {
				// PrizeLogs.i("xiaxiaoling", "conditionsFlag      " +
				// conditionsFlag);
				conditionsFlag = true;
			}			
		}
		private void onSensorChangedProximity(SensorEvent event) {
			float proximity = event.values[1];
			if (event.values[1] == -1f) {
				proximity = event.values[0];
				PROXIMITY_THRESHOLD = 0f;
				if (speedDelay >= SPEED_SHRESHOLD && conditionsFlag == true) {
					// PrizeLogs.i("xiaxiaoling", "proximity0=      " +
					// proximity);
					if (proximity <= PROXIMITY_THRESHOLD) {
						speedDelay = 0.0;
						maxDegreeDblY = 0.0;
						conditionsFlag = false;
						// ContactList recipients =
						// isRecipientsEditorVisible() ? mRecipientsEditor
						// .constructContactsFromInput(false)
						// : getRecipients();
						// PrizeDirectCall(recipients);
						doActionProxy(DIRECT_CALL_ACTION, 0);
					}
				}
			} else {
				if (speedDelay >= SPEED_SHRESHOLD && conditionsFlag == true) {
					// PrizeLogs.i("xiaxiaoling", "proximity1=      " +
					// proximity);
					if (proximity >= PROXIMITY_THRESHOLD) {
						speedDelay = 0.0;
						maxDegreeDblY = 0.0;
						conditionsFlag = false;
						// ContactList recipients =
						// isRecipientsEditorVisible() ? mRecipientsEditor
						// .constructContactsFromInput(false)
						// : getRecipients();
						// PrizeDirectCall(recipients);
						doActionProxy(DIRECT_CALL_ACTION, 0);
					}
				}
			}
		}
		@Override
		public void onSensorChanged(SensorEvent event) {
			if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
				onSensorChangedAccelerometer(event);
			}
			if (event.sensor.getType() == Sensor.TYPE_PROXIMITY) {
				onSensorChangedProximity(event);
			}
		}

		/**
		 * 计算手机绕Y轴旋转角度变化值
		 */
		public double calculateAngleForY(float value) {
			// 获取当前时间
			currentTime = System.currentTimeMillis();
			if (currentTime - lastTime > 200) {
				// 计算手机绕Y轴旋转角度
				degreeDblY = value;
				if (g1 < Math.abs(degreeDblY)) {
					g1 = Math.abs(degreeDblY);
				}
				degreeDblY = Math.asin(degreeDblY / g1);
				mDegreeDblY = degreeDblY * DEGREE_PI / Math.PI;
				double angleY = Math.abs(mDegreeDblY) - Math.abs(lastDegreeDblY);
				if (Math.abs(angleY) >= 15.0) {
					angleForY = Math.abs(angleY);
					// PrizeLogs.i("xiaxiaoling", "angleForY1=      " +
					// angleForY);
				}
				lastDegreeDblY = mDegreeDblY;
				lastTime = currentTime;// 重新计时
			}
			return angleForY;
		}
		/**
		 * 计算手机绕Z轴的旋转角度
		 */
		public double calculateAngleForZ(float value) {
			degreeDblZ = value;
			if (g2 < Math.abs(degreeDblZ)) {
				g2 = Math.abs(degreeDblZ);
			}
			degreeDblZ = Math.asin(degreeDblZ / g2);
			mDegreeDblZ = degreeDblZ * DEGREE_PI / Math.PI;
			return mDegreeDblZ;
		}
		@Override
		public void onAccuracyChanged(Sensor arg0, int arg1) {
			// TODO Auto-generated method stub
		}
	}
	/***********************************************************************/
	/*
	 * 智能防窃听 Prize_GENE_ANTI_EAVESDROP
	 * 
	 */
	private class PrizeAntiWireTapingMode 
		implements PrizeGeneMode, SensorEventListener {

		private SensorManager mPrizeSensorManager;
		private AudioManager mAudioManage;
		/* added by chinn 20120801 start */
		private float[] mValues;
		private float mCalibratedValue = -0.00001f;
		private float mSumOfValues = 0.0f;
		private int mNumOfValues = 0;

		/* added by chinn 20120801 end */

		public PrizeAntiWireTapingMode() {
			/* 获取真机的传感器管理服务 */
			mPrizeSensorManager = (SensorManager) mContext
					.getSystemService(Context.SENSOR_SERVICE);
			mAudioManage = (AudioManager) mContext
					.getSystemService(Context.AUDIO_SERVICE);
			/* added by chinn 20120801 start */
			/* initialize the near five values */
			mValues = new float[5];
			for (int i = 0; i < 5; i++) {
				mValues[i] = -0.00001f;
			}
			/* added by chinn 20120801 end */
		}

		@Override
		public void start() {
			// PrizeLogs.i("zhaoyang", "Proximity--Start");
			/* 为系统的近距离传感器注册监听器 */
			mPrizeSensorManager
					.registerListener(this, mPrizeSensorManager
							.getDefaultSensor(Sensor.TYPE_PROXIMITY),
							SensorManager.SENSOR_DELAY_UI);
		}

		@Override
		public void stop() {
			/* 程序退出时取消注册传感器监听器 */
			mPrizeSensorManager.unregisterListener(this);
			// PrizeLogs.i("zhaoyang", "Proximity--End");
		}

		@Override
		public void onAccuracyChanged(Sensor sensor, int accuracy) {

		}

		@Override
		public void onSensorChanged(SensorEvent event) {
			int sensorType = event.sensor.getType();
			/* 判断是否插着耳机 */
			Boolean isWriedHeadSet = mAudioManage.isWiredHeadsetOn();
			/* 判断是否是扩音器状态 */
			Boolean isSpeak = mAudioManage.isSpeakerphoneOn();
			/* Log蓝牙连接状态 */
			Boolean isBluetooth = mAudioManage.isBluetoothScoOn();
			// PrizeLogs.i("zhaoyang",
			// "BLUETOOTH   "+Boolean.toString(isBluetoothAudioConnected()));
			/* Log扩音器状态 */
			// PrizeLogs.i("zhaoyang", "ISSPEAKER   "+Boolean.toString(isSpeak));
			/* Log耳机插入状态 */
			// PrizeLogs.i("zhaoyang", "EAR   "+Boolean.toString(isWriedHeadSet));
			if (sensorType == Sensor.TYPE_PROXIMITY && !isWriedHeadSet
					&& !isSpeak && !isBluetooth) {
				int Level;
				/* added by chinn 20120801 */
				mSumOfValues = 0.0f;
				mNumOfValues = 0;
				for (int i = 0; i < 4; i++) // 前四个顺次挪位
				{
					mValues[i] = mValues[i + 1];
					if (mValues[i] > -0.00001f) {
						mSumOfValues += mValues[i];
						mNumOfValues++;
					}
				}
				mValues[4] = event.values[1];
				mSumOfValues += event.values[1];
				mNumOfValues++;
				// PrizeLogs.i("zhaoyang", "chinn----mSumOfValues:"+
				// Float.toString(mSumOfValues)+"; mNumOfValues:" +
				// mNumOfValues);
				mCalibratedValue = mSumOfValues / mNumOfValues;
				/* added by chinn 20120801 end */
				// if(event.values[1]>=1023f)
				if (mCalibratedValue >= 1023f) // modified by chinn 20120801
				{
					Level = 3;
					/* Log打印音量格数，4格 */
					// PrizeLogs.i("zhaoyang",
					// "44444444444444    "+Float.toString(event.values[1])+
					// "---mCalibratedValue:"+
					// Float.toString(mCalibratedValue));
				} else if (mCalibratedValue < 1023f && mCalibratedValue >= 248f) {
					Level = 4;
					/* Log打印音量格数，5格 */
					// PrizeLogs.i("zhaoyang",
					// "555555555555555    "+Float.toString(event.values[1])+
					// "---mCalibratedValue:"+
					// Float.toString(mCalibratedValue));
				} else if (mCalibratedValue < 248f && mCalibratedValue >= 119f){
					Level = 5;
					/* Log打印音量格数，6格 */
					// PrizeLogs.i("zhaoyang",
					// "666666666666666   "+Float.toString(event.values[1])+
					// "---mCalibratedValue:"+
					// Float.toString(mCalibratedValue));
				} else {
					Level = 6;
					/* Log打印音量格数，7格 */
					// PrizeLogs.i("zhaoyang",
					// "777777777777777    "+Float.toString(event.values[1])+
					// "---mCalibratedValue:"+
					// Float.toString(mCalibratedValue));
				}
				// mAudioManage.setStreamVolume(AudioManager.STREAM_VOICE_CALL,
				// Leval, 0);
				doActionProxy(ANTI_EAVESDROP_ACTION, Level);
			}
		}
	}

	/*
	 * 口袋模式 Prize_GENE_POCKET_MODE
	 */
	public class PrizeGeneImplPocketMode 
		implements PrizeGeneMode,SensorEventListener {
		private static final int LIGHT_SENSOR_VALUE_MIN = 28; // e900
		private AudioManager am = null;// 获取铃声
		private int currentVol = 0;// 当前的铃声音量
		/* 传感器管理器 */
		private SensorManager sensorManager = null;
		/* 光线传感器 */
		private Sensor lightsSensor = null;
		/* 加速度传感器 */
		private Sensor AccelerometerSensor = null;
		/* 符合增大音量标志位 */
		private boolean incState = false;
		/* 添加传感器最先响应标志位 */
		private int sensorFlag = 0;
		/*
		 * 两次检测的时间间隔单位毫秒
		 * 这里通过几次测试，第一次获取到加速度值到第一次获取到光感值之间的时间（758ms，515ms，391ms，347
		 * ms，196ms，212ms）， 所以我们这里暂时设定为1000，如果超过1000ms，仍然没有获得光感值，这时我们强制认为光感为最低值
		 */
		private static final int UPDATE_INTERVAL_TIME = 1000;
		/* 上次检测时间 */
		private long lastUpdateTime = 0;

		/**
		 * 构造方法
		 * 
		 * @param c
		 *            上下文
		 */
		public PrizeGeneImplPocketMode() {
			// 获得监听对象
			lightSensorInit();
		}

		@Override
		public void start() {
			lightSensorStart();
		}

		@Override
		public void stop() {
			lightSensorStop();
		}

		@Override
		public void onAccuracyChanged(Sensor sensor, int accuracy) {
			// TODO Auto-generated method stub
			// PrizeLogs.d(TAG, "xww-onAccuracyChanged ");
			// 需要考虑哪个传感器最先响应，设置标志位
			if (sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
				if (sensorFlag != Sensor.TYPE_LIGHT) {
					sensorFlag = Sensor.TYPE_ACCELEROMETER;
				}
			}
			if (sensor.getType() == Sensor.TYPE_LIGHT) {
				if (sensorFlag != Sensor.TYPE_ACCELEROMETER) {
					sensorFlag = Sensor.TYPE_LIGHT;
				}
			}
		}

		@Override
		public void onSensorChanged(SensorEvent event) {
			// TODO Auto-generated method stub
			if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
				// PrizeLogs.d(TAG, "xww-acc");
				if (sensorFlag == Sensor.TYPE_LIGHT) {// 光感快于加速度反应
					if (incState
							&& (Math.abs(event.values[0]) >= 5 || Math
									.abs(event.values[1]) >= 5)) {
						// am.setStreamVolume(
						// AudioManager.STREAM_RING,
						// am.getStreamMaxVolume(AudioManager.STREAM_RING),
						// 0);
						doActionProxy(POCKET_MODE_ACTION,
								am.getStreamMaxVolume(AudioManager.STREAM_RING));
					}
					lightSensorStop();
					return;
				} else {// 加速度快于光感反应
						// 如果静止情况下,x、y轴与水平方向的夹角小于30度左右 不会增大音量
					if (Math.abs(event.values[0]) >= 5
							|| Math.abs(event.values[1]) >= 5) {
						// 加速度传感器符合增大音量情况
						incState = true;
					} else {
						incState = false;
					}
					/*
					 * 由于光感被压迫时，无法传值上来，所以这里始终用加速度感应器来跑进这个onSensorChanged方法，
					 * 所以这里不做注销处理
					 */
					// 只取进入的第一个值，所以取完值马上注销加速度传感器
					// unregisterAcclerometerListener();
				}
			}
			if (event.sensor.getType() == Sensor.TYPE_LIGHT) {
				// PrizeLogs.d(TAG, "xww-light=" + event.values[0]);
				if (sensorFlag == Sensor.TYPE_ACCELEROMETER) {// 加速度快于光感反应
					/*
					 * 符合下面三个条件才增大音量处理 条件一：加速度符合 incState = true;为符合
					 * 条件二：非静音或震动模式(am.getStreamVolume(AudioManager.STREAM_RING)
					 * == 0为静音或者震动模式) 条件三： 光感小于一定值 这里是最小值LIGHT_SENSOR_VALUE_MIN
					 */
					if (incState && (currentVol != 0)
							&& (event.values[0] <= LIGHT_SENSOR_VALUE_MIN)) {
						// PrizeLogs.d(TAG, "xww-0-silent");
						// am.setStreamVolume(
						// AudioManager.STREAM_RING,
						// am.getStreamMaxVolume(AudioManager.STREAM_RING),
						// 0);
						doActionProxy(POCKET_MODE_ACTION,
								am.getStreamMaxVolume(AudioManager.STREAM_RING));
					}
					/*
					 * 只取进入的第一个值，所以取完值马上注销光感传感器
					 * 这里做注销加速度传感器操作。动作已经完成，无需再跑onSensorChanged方法，所以把传感器都注销掉
					 */
					lightSensorStop();
					return;
				} else {// 光感快于加速度反应
					if (event.values[0] <= LIGHT_SENSOR_VALUE_MIN) {
						// 光感传感器符合增大音量情况
						incState = true;
					} else {
						incState = false;
					}
				}
			}

			if (sensorFlag == Sensor.TYPE_ACCELEROMETER) {// 加速度快于光感反应
				/*
				 * 当光感受压迫时，光感无法获取到值，这种情况下 ，我们强制认为光感为最低值
				 * 通过时间来检测，我们这里设置1000ms内，如果没有光感值则认为光感为最低值
				 */
				long currentUpdateTime = System.currentTimeMillis();
				// PrizeLogs.d(TAG, "xwww-0-time=" + currentUpdateTime);
				// PrizeLogs.d(TAG, "xwww-1-time=" + lastUpdateTime);
				long timeInterval = currentUpdateTime - lastUpdateTime;
				// PrizeLogs.d(TAG, "xwww-3-time=" + timeInterval);
				if (lastUpdateTime == 0) { // 第一次进入
					// 现在的时间变成last时间
					lastUpdateTime = currentUpdateTime;
					return;
				} else if (timeInterval < UPDATE_INTERVAL_TIME) {
					return;
				} else {// 如果光感没有获取到value值，我们这里强制认为光感值为最低，来对音量再次做处理
					lastUpdateTime = 0;
					/*
					 * 因为没有获取到光感值，所以只需要符合下面两个条件就可增大音量处理 条件一：加速度符合 incState =
					 * true;为符合
					 * 条件二：非静音或震动模式(am.getStreamVolume(AudioManager.STREAM_RING)
					 * == 0为静音或者震动模式)
					 */
					if (incState && (currentVol != 0)) {
						// PrizeLogs.d(TAG, "xww-1-silent");
						// am.setStreamVolume(
						// AudioManager.STREAM_RING,
						// am.getStreamMaxVolume(AudioManager.STREAM_RING),
						// 0);
						doActionProxy(POCKET_MODE_ACTION,
								am.getStreamMaxVolume(AudioManager.STREAM_RING));
					}
					/*
					 * 只取进入的第一个值，所以取完值马上注销光感传感器
					 * 这里做注销加速度传感器操作。动作已经完成，无需再跑onSensorChanged方法，所以把传感器都注销掉
					 */
					lightSensorStop();
				}
			}

		}

		/**
		 * 实例化
		 */
		private void lightSensorInit() {
			// 获得传感器管理器实例
			sensorManager = (SensorManager) mContext
					.getSystemService(Context.SENSOR_SERVICE);
			// 获得光线传感器实例
			if (sensorManager != null) {
				// 获得光线传感器实例
				lightsSensor = sensorManager
						.getDefaultSensor(Sensor.TYPE_LIGHT);
				// 获得加速度传感器实例
				AccelerometerSensor = sensorManager
						.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
			}
			// 获得媒体实例
			if (am == null) {
				am = (AudioManager) mContext
						.getSystemService(Context.AUDIO_SERVICE);
			}
		}

		/**
		 * 注册传感器
		 */
		private void lightSensorStart() {
			// 为光线传感器管理器注册监听
			if (lightsSensor != null) {
				sensorManager.registerListener(this, lightsSensor,
						SensorManager.SENSOR_DELAY_FASTEST);
			}
			// 为加速度传感器管理器注册监听
			if (AccelerometerSensor != null) {
				sensorManager.registerListener(this, AccelerometerSensor,
						SensorManager.SENSOR_DELAY_FASTEST);
			}
		}

		/**
		 * 注销加速度传感器
		 */
		private void unregisterAcclerometerListener() {
			if (AccelerometerSensor != null) {
				sensorManager.unregisterListener(this, AccelerometerSensor);
			}
		}

		/**
		 * 注销光线传感器
		 */
		private void unregisterLightListener() {
			if (lightsSensor != null) {
				sensorManager.unregisterListener(this, lightsSensor);
			}
		}

		/**
		 * 注销传感器
		 */
		private void lightSensorStop() {
			// 注销光线传感器
			unregisterLightListener();
			// 注销加速度传感器
			unregisterAcclerometerListener();
			// 确保下次resume进入的时候 这个变量为0
			lastUpdateTime = 0;
			// 置标志位为初始化状态 false
			incState = false;
			// 恢复标志位
			sensorFlag = 0;
		}
	}
	
	///////////////////////////////////////////////////////////////////////////////////
	///////////////////////////////////////////////////////////////////////////////////
	
	/***********************************************************************/
	/* ifrank,2012.12.19, 智能免提
	 * 实现： 
	 * 判断proximity的距离,当接近脸部时，
	 * 		同时通过accelerometer判断手机为竖直情况， 则认为应该关免提.
	 * 当远离脸部时，而不关是否手机竖直, 认为开免提
	 * 
	 * 竖直判断时，判断y轴和全局xy平面的夹角大小
	 * 
	 * */
	public static class PrizeSmartHandFreeV2Result{
		public boolean isClose;
		public double angle;
		public double dist;
		public PrizeSmartHandFreeV2Result(boolean isClose, double angle, double dist) {
			this.isClose = isClose;
			this.angle = angle;
			this.dist = dist;
		}
	}
	private class PrizeSmartHandFreeV2 implements PrizeGeneMode {
		private static final String LOG_TAG = "PrizeSmartHandFreeV2";
		
		private static final int __ANGLE_FLAG_STABLE_WAITTIME = 40;
		
		private SensorManager mSenMgr;
		private boolean isClose = false;
		private boolean lastClose = false;
		
			// proximity
		private Sensor mProxSensor;
		private boolean mProxLisRegistered = false;
		private float mCurDistance = 0;
		private float mLastDistance = 0;
		private boolean mFirstFlag = false;
		
			// accelerometer
		private Sensor mAccSensor;
		private boolean mAccLisRegistered = false;
		private int mAccSkipCnt = 0;
		private float[] curGravity = new float[3];
		private double curAngle;
		private double lastAngle;
		private boolean curAngleFlag = false; /* true-手机是在直立着, false-手机躺倒了 */
		private boolean lastAngleFlag = false;
		private int angleStableTimeCnt = 0;
		
		private PrizeGeneHelper.lowPassFilter filter1;
		private PrizeGeneHelper.lowPassFilter filter2;
		
		private PrizeGeneHelper.firLowPassFilter firFilter1;
		private PrizeGeneHelper.firLowPassFilter firFilter2;
		private PrizeGeneHelper.firLowPassFilter firFilter3;
		
		/**/
		private static final int __PROC_TIME_DELAY = 800; // unit:ms
		private int __procTimeDelayCnt = 0;
		private long mLastEvtTime;
		private boolean __isTimeElapse() {
			long curTime = System.currentTimeMillis();
			if((curTime - mLastEvtTime) > __PROC_TIME_DELAY)  {
				mLastEvtTime = curTime;
				return true;
			}
			return false;
		}
		
		/**/
		private static final int __PROC_DELAY_TIME = 10; // 间隔10次后,再检查是否可以进行处理
		private int __procDelayCnt = 0;
		private void __process() {
			boolean curFlag = curAngleFlag;
			if(!__isTimeElapse()) {
				__procDelayCnt = __PROC_DELAY_TIME;
				return ;
			}
			__procDelayCnt = 0;
			curFlag = true; // do not check accelerometer !!!!
			
			PrizeLogs.v(LOG_TAG, "*** process angle flag=" + curFlag + " prox=" + mCurDistance);
			
			if(curFlag == false) {
				if(isClose == true) {
					isClose = lastClose = false;
					doActionProxy(SMART_HANDFREE_ACTION,
						new PrizeSmartHandFreeV2Result(isClose, curAngle, mCurDistance));
				}
				return ;
			}
			// 只处理2值
			if(mCurDistance == 0) { // 接近
				isClose = true;
			}else{ // 远离
				isClose = false;
			}
			if(mFirstFlag == false) { // 第一次，给出一个动作
				mFirstFlag = true;
				if(isClose)
					lastClose = false;
				else
					lastClose = true;
			}
			if(isClose != lastClose) {
				lastClose = isClose;
				doActionProxy(SMART_HANDFREE_ACTION,
					new PrizeSmartHandFreeV2Result(isClose, curAngle, mCurDistance));
			}
		}
		
		private static final int __PROX_CHANGED_DELAY = 50;
		private int __proxChangedDelayCnt = 0;
		private SensorEventListener mProxListener = new SensorEventListener() {
			@Override
			public void onAccuracyChanged(Sensor sensor, int accuracy) {
			}
			@Override
			public void onSensorChanged(SensorEvent event) {
				mCurDistance = event.values[0];
				
				PrizeLogs.v(LOG_TAG, "0=" + event.values[0] + " 1=" + event.values[1]
					+ " cur=" + mCurDistance + " last=" + mLastDistance);
				
				if(mFirstFlag == false) { // 第一次，设置last和current不同，以便执行一次动作!
					if(mCurDistance != 0) {
						mLastDistance = 0;
					} else {
						mLastDistance = 1;
					}
				}
				if(mLastDistance != mCurDistance)
				{
					mLastDistance = mCurDistance;
					__proxChangedDelayCnt = __PROX_CHANGED_DELAY;
					PrizeLogs.v(LOG_TAG, "-- prox changed = " + mCurDistance);
				}
			}
		};
		
		//private static final double __ANGLE_DIFF_DELTA = 0.4f;
		private static final double __ANGLE_THRESHOLD = 25.0f;
		private static final double __ANGLE_THRESHOLD_DELTA = 5.0f;
		private static final int __ACC_SKIP_SAMPLES = 50;	// 跳过刚开始的若干个加速度采样
		private SensorEventListener mAccListener = new SensorEventListener() {
			@Override
			public void onAccuracyChanged(Sensor sensor, int accuracy) {
			}
			@Override
			public void onSensorChanged(SensorEvent event) {
				
				curGravity[0] = (float)firFilter1.filter1((double)event.values[0]);
				curGravity[1] = (float)firFilter2.filter1((double)event.values[1]);
				curGravity[2] = (float)firFilter3.filter1((double)event.values[2]);
				
				curGravity = filter1.SMAFilter(curGravity);

				/* 计算手机y轴和全局xy平面的夹角, 在[-90度~90度]范围 */
				/* 重力的总长度 */
				double totLen = PrizeGeneHelper.vecLength(curGravity);
				/* 重力在xz平面的投影长度 */
				double zxLen = PrizeGeneHelper.vecLength(new double[]{curGravity[0], curGravity[2]});
				double absAngle = Math.toDegrees(Math.acos(zxLen/totLen));
				curAngle = filter2.filter((float)absAngle);
				if(curGravity[1] < 0.0f) {
					curAngle = 0.0f - curAngle;
				}
				if(mAccSkipCnt < __ACC_SKIP_SAMPLES) {
					mAccSkipCnt++;
					lastAngle = curAngle;
					return ;
				}
				//double diff = Math.abs(lastAngle - curAngle);
				//if(diff > __ANGLE_DIFF_DELTA) 
				{
					lastAngle = curAngle;
					if(curAngleFlag) {
						if(curAngle <= (__ANGLE_THRESHOLD-__ANGLE_THRESHOLD_DELTA)) {
							angleStableTimeCnt++;
							if(angleStableTimeCnt >= __ANGLE_FLAG_STABLE_WAITTIME) 
							{
								angleStableTimeCnt = 0;
								curAngleFlag = false;
							}
						}else{
							angleStableTimeCnt = 0;
						}
					}else{
						if(curAngle >= (__ANGLE_THRESHOLD+__ANGLE_THRESHOLD_DELTA)) {
							angleStableTimeCnt++;
							if(angleStableTimeCnt >= __ANGLE_FLAG_STABLE_WAITTIME) 
							{
								angleStableTimeCnt = 0;
								curAngleFlag = true;
							}
						}else{
							angleStableTimeCnt = 0;
						}					
					}
					if(curAngleFlag != lastAngleFlag) {
						lastAngleFlag = curAngleFlag;
						// angle changed
						PrizeLogs.v(LOG_TAG, "-- angle changed flag=" + curAngleFlag + " angle=" + curAngle);
						__process();
					}
				}
				
				if(__proxChangedDelayCnt != 0) {
					__proxChangedDelayCnt--;
					if(__proxChangedDelayCnt == 0) {
						PrizeLogs.v(LOG_TAG, "-- prox process!");
						__process();
					}
				}
				if(__procDelayCnt != 0) {
					__procDelayCnt--;
					if(__procDelayCnt == 0) {
						PrizeLogs.v(LOG_TAG, "-- delayed not to process!");
						__process();
					}
				}
			}
		};
		
			// constructor
		public PrizeSmartHandFreeV2() {
			mSenMgr = (SensorManager)mContext.getSystemService(Context.SENSOR_SERVICE);
			mProxSensor = mSenMgr.getDefaultSensor(Sensor.TYPE_PROXIMITY);
			PrizeGeneHelper.checksNull(mProxSensor, "prox sensor");
			mAccSensor = mSenMgr.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
			PrizeGeneHelper.checksNull(mAccSensor, "acc sensor");
			
			filter1 = new PrizeGeneHelper.lowPassFilter(1, 30);
			filter2 = new PrizeGeneHelper.lowPassFilter(0, 0.9f);
			
			firFilter1 = new PrizeGeneHelper.firLowPassFilter(_NTAPS, h);
			firFilter2 = new PrizeGeneHelper.firLowPassFilter(_NTAPS, h);
			firFilter3 = new PrizeGeneHelper.firLowPassFilter(_NTAPS, h);
		}
		@Override
		public void start() {
			// TODO Auto-generated method stub
			if(mSenMgr != null) {
				// 开始判断时，做初始化
				mAccSkipCnt = 0;
				curAngleFlag = false;
				lastAngleFlag = false;
				isClose = false;
				lastClose = false;
				mFirstFlag = false;
				
				// start listening
				if(mProxSensor != null) {
					if(mProxLisRegistered == false) {
						mSenMgr.registerListener(mProxListener, 
							mProxSensor,SensorManager.SENSOR_DELAY_FASTEST);
						mProxLisRegistered = true;
					}
				}
				if(mAccSensor != null) {
					if(mAccLisRegistered == false) {
						mSenMgr.registerListener(mAccListener, 
							mAccSensor,SensorManager.SENSOR_DELAY_FASTEST);
						mAccLisRegistered = true;
					}					
				}
			}
		}
		@Override
		public void stop() {
			// TODO Auto-generated method stub
			if(mSenMgr !=null) {
				// stop listening
				if(mProxSensor != null) {
					if(mProxLisRegistered == true) {
						mSenMgr.unregisterListener(mProxListener);
						mProxLisRegistered = false;					
					}
				}
				if(mAccSensor != null) {
					if(mAccLisRegistered == true) {
						mSenMgr.unregisterListener(mAccListener);
						mAccLisRegistered = false;					
					}					
				}
			}
		}
		/* a fir filter */
		private int _NTAPS = 6;
		private double[] h = {
			0.125514644795420960,
			0.414388923238107440,
			-0.013420976983735622,
			-0.013420976983735622,
			0.414388923238107440,
			0.125514644795420960
			};
	}
	
	/********************************************/
	/* i,frank, 2012.12.21 , 智能防窃听
	 * 当打电话时，越靠近耳朵，声音越小
	 * 
	 * 实现: 接近传感器， 判断当前和脸部的距离
	 * accelerometer判断当前手机是否在竖直状态
	 * 兼容两值型接近传感器, 即， 只有0, 1两级可调
	 * 
	 */
	public static class PrizeAntiWireTapingModeV2Result{
		public float[] rawProx;
		public int level; // 等级越大，越靠近耳朵
		public double angle;
		public double dist;
		public PrizeAntiWireTapingModeV2Result(float[] rawProx, int level, double angle, double dist) {
			this.rawProx = rawProx.clone();
			this.level = level;
			this.angle = angle;
			this.dist = dist;
		}
	}
	private class PrizeAntiWireTapingModeV2 implements PrizeGeneMode {
		private static final String LOG_TAG = "PrizeSmartHandFreeV2";
		
		private static final int __ACC_SKIP_SAMPLES = 40;	// 跳过刚开始的若干个加速度采样
		

		private SensorManager mSenMgr;
		private int curLevel = 0;
		private int lastLevel = 0;
		private long mLastEvtTime;
		
		private Sensor mProxSensor;
		private boolean mProxLisRegistered = false;
		private float mCurDistance = 0;
		private float mLastDistance = 0;
		private float[] mRawProx = new float[3];
		private boolean mFirstFlag = false;
		
		private Sensor mAccSensor;
		private boolean mAccLisRegistered = false;
		private int mAccSkipCnt = 0;
		private float[] curGravity = new float[3];
		private double curAngle;
		private double lastAngle;
		private boolean angleFlag = false;
		private boolean angleFlagLast = false;
		
		private PrizeGeneHelper.lowPassFilter filter1;
		private PrizeGeneHelper.lowPassFilter filter2;
		
		private PrizeGeneHelper.firLowPassFilter firFilter1;
		private PrizeGeneHelper.firLowPassFilter firFilter2;
		private PrizeGeneHelper.firLowPassFilter firFilter3;
		
			/**/
		private static final long __CLOSE_DELAY = 100;
		private boolean __isTimeElapse() {
			long curTime = System.currentTimeMillis();
			if((curTime - mLastEvtTime) > __CLOSE_DELAY)  {
				mLastEvtTime = curTime;
				return true;
			}
			return false;
		}
		
		/* new method here 
		 * 1> 使得proximity的值, 手机角度值, 变的不那么快
		 * 2> 处理逻辑判断
		 * 
		 * */
		private static final int __PROC_TIME_DELAY = 10;
		private int __procTimeDelayCnt = 0;
		private void __process2() {
			boolean tAngleFlag = angleFlag;
			tAngleFlag = true;
			
			if(!__isTimeElapse()) {
				__procTimeDelayCnt = __PROC_TIME_DELAY;
				return ;
			}
			__procTimeDelayCnt = 0;
			
			if(tAngleFlag == false) { // 一旦手机不在竖直状态，直接恢复0等级
				if(curLevel != 0) {
					curLevel = lastLevel = 0;
						doActionProxy(ANTI_EAVESDROP_ACTION,
							new PrizeAntiWireTapingModeV2Result(mRawProx, curLevel, curAngle, mCurDistance));
				}
				return ;
			}
			
			// 只处理2值
			if(mCurDistance == 0) { // 接近
				curLevel = 1;
			}else{ // 远离
				curLevel = 0;
			}
			if(mFirstFlag == false) { // 第一次，给出一个动作!
				mFirstFlag = true;
				if(curLevel != 0) {
					lastLevel = 0;
				} else {
					lastLevel = 1;
				}
			}
			if(curLevel != lastLevel) {
				lastLevel = curLevel;
				doActionProxy(ANTI_EAVESDROP_ACTION,
					new PrizeAntiWireTapingModeV2Result(mRawProx, curLevel, curAngle, mCurDistance));
			}
		}

		/* 这里依靠accelerometer数据的定时更新, 来得到一个定时基准 
		 * 更新大约为20~40ms一次
		 * */
		private static final int __PROX_DELAY = 50;
		private int __proxDelayCnt = 0;
		private SensorEventListener mProxListener = new SensorEventListener() {
			@Override
			public void onAccuracyChanged(Sensor sensor, int accuracy) {
			}
			@Override
			public void onSensorChanged(SensorEvent event) {
				/* 
				 * 
				 * 虽然values[1]是原始的值，但这个值只在有事件时才上报上来, 也就是values[0]发生变化时. 
				 * 
				 * */
				mCurDistance = event.values[0];
				if(mFirstFlag == false) {
					if(mCurDistance != 0) {
						mLastDistance = 0;
					} else {
						mLastDistance = 1;
					}
				}
				if(mLastDistance != mCurDistance) 
				{
					mLastDistance = mCurDistance;
					mRawProx = event.values.clone();
					PrizeLogs.v(LOG_TAG, "--Prox got data: " + mCurDistance);
					__proxDelayCnt = __PROX_DELAY;
				}
			}
		};
		
		//private static final double __ANGLE_DELTA = 0.2f;
		private static final double __ANGLE_THRESHOLD = 35.0f;
		private static final double __ANGLE_SLUGGISH = 5.0f;
		private static final int __ANGLE_STABLE_DELAY = 50;
		private int __angleStableDelayCnt = 0;
		private SensorEventListener mAccListener = new SensorEventListener() {
			@Override
			public void onAccuracyChanged(Sensor sensor, int accuracy) {
			}
			@Override
			public void onSensorChanged(SensorEvent event) {
				//PrizeLogs.v(LOG_TAG, "Acc got data!!!");
				
				curGravity[0] = (float)firFilter1.filter1((double)event.values[0]);
				curGravity[1] = (float)firFilter2.filter1((double)event.values[1]);
				curGravity[2] = (float)firFilter3.filter1((double)event.values[2]);
				
				curGravity = filter1.SMAFilter(curGravity);

				/* 计算手机y轴和全局xy平面的夹角, 在[-90度~90度]范围 */
				/* 重力的总长度 */
				double totLen = PrizeGeneHelper.vecLength(curGravity);
				/* 重力在xz平面的投影长度 */
				double zxLen = PrizeGeneHelper.vecLength(new double[]{curGravity[0], curGravity[2]});
				double absAngle = Math.toDegrees(Math.acos(zxLen/totLen));
				curAngle = filter2.filter((float)absAngle);
				if(curGravity[1] < 0.0f) {
					curAngle = 0.0f - curAngle;
				}
				if(mAccSkipCnt < __ACC_SKIP_SAMPLES) {
					mAccSkipCnt++;
					lastAngle = curAngle;
					return ;
				}
				//double diff = Math.abs(lastAngle - curAngle);
				//if(diff > __ANGLE_DELTA) 
				{
					lastAngle = curAngle;
					
					if(angleFlag == false) {
						if(curAngle >= (__ANGLE_THRESHOLD+__ANGLE_SLUGGISH)) {
							__angleStableDelayCnt++;
							if(__angleStableDelayCnt >= __ANGLE_STABLE_DELAY) {
								__angleStableDelayCnt = 0;
								angleFlag = true;
							}
						} else {
							__angleStableDelayCnt = 0;
						}
					} else {
						if(curAngle <= (__ANGLE_THRESHOLD-__ANGLE_SLUGGISH)) {
							__angleStableDelayCnt++;
							if(__angleStableDelayCnt >= __ANGLE_STABLE_DELAY) {
								__angleStableDelayCnt = 0;
								angleFlag = false;
							}
						} else {
							__angleStableDelayCnt = 0;
						}
					}
					if(angleFlagLast != angleFlag) {
						angleFlagLast = angleFlag;
						// angle changed
						PrizeLogs.v(LOG_TAG, "-- angle changed! flag=" + angleFlag + " angle=" + curAngle);
						__process2();
					}
				}
				
				if(__proxDelayCnt != 0) {
					__proxDelayCnt--;
					if(__proxDelayCnt == 0) {
						// proximity value changed
						PrizeLogs.v(LOG_TAG, "-- prox to process! ");
						__process2();
					}
				}
				if(__procTimeDelayCnt != 0) {
					__procTimeDelayCnt--;
					if(__procTimeDelayCnt == 0) {
						PrizeLogs.v(LOG_TAG, "-- delayed now to process! ");
						__process2();
					}
				}
			}
		};
		
			// constructor
		public PrizeAntiWireTapingModeV2() {
			mSenMgr = (SensorManager)mContext.getSystemService(Context.SENSOR_SERVICE);
			mProxSensor = mSenMgr.getDefaultSensor(Sensor.TYPE_PROXIMITY);
			PrizeGeneHelper.checksNull(mProxSensor, "prox sensor");
			mAccSensor = mSenMgr.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
			PrizeGeneHelper.checksNull(mProxSensor, "acc sensor");
			
			filter1 = new PrizeGeneHelper.lowPassFilter(1, 40);
			filter2 = new PrizeGeneHelper.lowPassFilter(0, 0.9f);
			
			firFilter1 = new PrizeGeneHelper.firLowPassFilter(_NTAPS, h);
			firFilter2 = new PrizeGeneHelper.firLowPassFilter(_NTAPS, h);
			firFilter3 = new PrizeGeneHelper.firLowPassFilter(_NTAPS, h);
		}
		@Override
		public void start() {
			// TODO Auto-generated method stub
			if(mSenMgr != null) {
				// 开始判断时，做初始化
				mAccSkipCnt = 0;
				angleFlagLast = angleFlag = false;
				__angleStableDelayCnt = 0;
				mLastDistance = 0;
				mFirstFlag = false;
				
				// start listening
				if(mProxSensor != null) {
					if(mProxLisRegistered == false) {
						mSenMgr.registerListener(mProxListener, 
							mProxSensor,SensorManager.SENSOR_DELAY_FASTEST);
						mProxLisRegistered = true;
					}
				}
				if(mAccSensor != null) {
					if(mAccLisRegistered == false) {
						mSenMgr.registerListener(mAccListener, 
							mAccSensor,SensorManager.SENSOR_DELAY_FASTEST);
						mAccLisRegistered = true;
					}					
				}
			}
		}
		@Override
		public void stop() {
			// TODO Auto-generated method stub
			if(mSenMgr !=null) {
				// stop listening
				if(mProxSensor != null) {
					if(mProxLisRegistered == true) {
						mSenMgr.unregisterListener(mProxListener);
						mProxLisRegistered = false;					
					}
				}
				if(mAccSensor != null) {
					if(mAccLisRegistered == true) {
						mSenMgr.unregisterListener(mAccListener);
						mAccLisRegistered = false;					
					}					
				}
			}
		}
		/* a fir filter */
		private int _NTAPS = 6;
		private double[] h = {
			0.125514644795420960,
			0.414388923238107440,
			-0.013420976983735622,
			-0.013420976983735622,
			0.414388923238107440,
			0.125514644795420960
			};
	}
	/***********************************************************************/
	/* ifrank,2012.12.19, 体感拨号
	 * 实现: 
	 * 接近传感器判断贴近脸部, 加速度判断手机当前的静态姿态是否竖直
	 * 
	 **/
	public static class PrizeDirectCallModeV2Result{
		public boolean isClose;
		public double angle;
		public double dist;
		public PrizeDirectCallModeV2Result(boolean isClose, double angle, double dist) {
			this.isClose = isClose;
			this.angle = angle;
			this.dist = dist;
		}
	}
	private class PrizeDirectCallModeV2
		implements PrizeGeneMode {
		private static final String LOG_TAG = "PrizeDirectCallModeV2";
		
		private SensorManager mSenMgr;
		private boolean isClose = false;
		private boolean lastClose = false;
		private long mLastEvtTime;
		
		private Sensor mProxSensor;
		private boolean mProxLisRegistered = false;
		private float mCurDistance = 0;
		private float mLastdistance = 0;
		
		private Sensor mAccSensor;
		private boolean mAccLisRegistered = false;
		private int mAccSkipCnt = 0;
		private float[] curGravity = new float[3];
		private double curAngle;
		private double lastAngle;
		private boolean curAngleFlag = false;
		private boolean lastAngleFlag = false;
		
		private PrizeGeneHelper.lowPassFilter filter1;
		private PrizeGeneHelper.lowPassFilter filter2;
		
		private PrizeGeneHelper.firLowPassFilter firFilter1;
		private PrizeGeneHelper.firLowPassFilter firFilter2;
		private PrizeGeneHelper.firLowPassFilter firFilter3;
		

		private static final long __CLOSE_DELAY = 500;
		private boolean __isTimeElapse() {
			long curTime = System.currentTimeMillis();
			if((curTime - mLastEvtTime) > __CLOSE_DELAY)  {
				mLastEvtTime = curTime;
				return true;
			}
			return false;
		}
			/**/
		private static final int __PROC_CHECK_DELAY = 10;
		private int __procCheckDelayCnt = 0;
		private void __process() {
			if(!__isTimeElapse()) {
				__procCheckDelayCnt = __PROC_CHECK_DELAY;
				return ;
			}
			__procCheckDelayCnt = 0;
			if(curAngleFlag == false) {
				if(isClose) {
					isClose = lastClose = false;
					doActionProxy(DIRECT_CALL_ACTION,
						new PrizeDirectCallModeV2Result(isClose, curAngle, mCurDistance));
				}
				return ;
			}
			
			// 只处理2值
			if(mCurDistance == 0) { // 接近
				isClose = true;
			}else{ // 远离
				isClose = false;
			}
			if(isClose != lastClose) {
				lastClose = isClose;
				doActionProxy(DIRECT_CALL_ACTION,
					new PrizeDirectCallModeV2Result(isClose, curAngle, mCurDistance));
			}
		}
		
		private static final int __PROX_PROC_DELAY_TIME = 40;
		private int __proxDelayCnt = 0;
		private SensorEventListener mProxListener = new SensorEventListener() {
			@Override
			public void onAccuracyChanged(Sensor sensor, int accuracy) {
			}
			@Override
			public void onSensorChanged(SensorEvent event) {
				mCurDistance = event.values[0];
				if(mLastdistance != mCurDistance) 
				{
					mLastdistance = mCurDistance;
					__proxDelayCnt = __PROX_PROC_DELAY_TIME;
					PrizeLogs.v(LOG_TAG, "prox changed:" + mCurDistance + " value=" + event.values[1]);
				}
			}
		};
		
		private static final int __ACC_SKIP_SAMPLES = 30;	// 跳过刚开始的若干个加速度采样
		//private static final double __ANGLE_DIFF = 0.2f;
		private static final double __ANGLE_THRESHOLD = 10.0f;	// 适当设置大一些
		private static final double __ANGLE_THRESHOLD_SLUGGISH = 5.0f;
		private static final int __ANGLE_STABLE_DELAY = 10;
		private int __angleStableDelayCnt = 0;
		private SensorEventListener mAccListener = new SensorEventListener() {
			@Override
			public void onAccuracyChanged(Sensor sensor, int accuracy) {
			}
			@Override
			public void onSensorChanged(SensorEvent event) {
				
				curGravity[0] = (float)firFilter1.filter1((double)event.values[0]);
				curGravity[1] = (float)firFilter2.filter1((double)event.values[1]);
				curGravity[2] = (float)firFilter3.filter1((double)event.values[2]);
				
				curGravity = filter1.SMAFilter(curGravity);

				if(mAccSkipCnt < __ACC_SKIP_SAMPLES) {
					mAccSkipCnt++;
					lastAngle = curAngle;
					return ;
				}
				
				/*
				float[] linearAcc = new float[]{
					event.values[0]-curGravity[0],
					event.values[1]-curGravity[1],
					event.values[2]-curGravity[2]
				};
				double linearLen = PrizeGeneHelper.vecLength(linearAcc);
				*/
				
				/* 计算手机y轴和全局xy平面的夹角, 应该 在[-90度~90度]范围 */
				/* 重力的总长度 */
				double totLen = PrizeGeneHelper.vecLength(curGravity);
				/* 重力在xz平面的投影长度 */
				double zxLen = PrizeGeneHelper.vecLength(new double[]{curGravity[0], curGravity[2]});
				/* 夹角 */
				double absAngle = Math.toDegrees(Math.acos(zxLen/totLen));
				curAngle = filter2.filter((float)absAngle);
				if(curGravity[1] < 0.0f) { // 判断角度的符号
					curAngle = 0.0f - curAngle;
				}
				if(mAccSkipCnt < __ACC_SKIP_SAMPLES) {
					mAccSkipCnt++;
					lastAngle = curAngle;
					return ;
				}

				//double diff = Math.abs(lastAngle - curAngle);
				//if(diff > __ANGLE_DIFF) 
				{
					lastAngle = curAngle;
					if(curAngleFlag == false) {
						if(lastAngle >= (__ANGLE_THRESHOLD+__ANGLE_THRESHOLD_SLUGGISH)) {
							__angleStableDelayCnt++;
							if(__angleStableDelayCnt >= __ANGLE_STABLE_DELAY) {
								__angleStableDelayCnt = 0;
								curAngleFlag = true;
							}
						}else{
							__angleStableDelayCnt = 0;
						}
					} else {
						if(lastAngle <= (__ANGLE_THRESHOLD-__ANGLE_THRESHOLD_SLUGGISH)) {
							__angleStableDelayCnt++;
							if(__angleStableDelayCnt >= __ANGLE_STABLE_DELAY) {
								__angleStableDelayCnt = 0;
								curAngleFlag = false;
							}
						}else{
							__angleStableDelayCnt = 0;
						}
					}
					if(curAngleFlag != lastAngleFlag) {
						lastAngleFlag = curAngleFlag;
						PrizeLogs.v(LOG_TAG, "--angle changed:" + curAngleFlag + " angle=" + curAngle);
						__process();
					}
				}

				if(__proxDelayCnt > 0) {
					__proxDelayCnt--;
					if(__proxDelayCnt == 0) {
						PrizeLogs.v(LOG_TAG, "--prox to process now!");
						__process();
					}
				}
				if(__procCheckDelayCnt > 0) {
					__procCheckDelayCnt--;
					if(__procCheckDelayCnt == 0) {
						__process();
					}
				}
			}
		};
		

			// constructor
		public PrizeDirectCallModeV2() {
			mSenMgr = (SensorManager)mContext.getSystemService(Context.SENSOR_SERVICE);
			mProxSensor = mSenMgr.getDefaultSensor(Sensor.TYPE_PROXIMITY);
			PrizeGeneHelper.checksNull(mProxSensor, "prox sensor");
			mAccSensor = mSenMgr.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
			PrizeGeneHelper.checksNull(mAccSensor, "acc sensor");
			
			filter1 = new PrizeGeneHelper.lowPassFilter(1, 20);
			filter2 = new PrizeGeneHelper.lowPassFilter(0, 0.9f);
			
			firFilter1 = new PrizeGeneHelper.firLowPassFilter(_NTAPS, h);
			firFilter2 = new PrizeGeneHelper.firLowPassFilter(_NTAPS, h);
			firFilter3 = new PrizeGeneHelper.firLowPassFilter(_NTAPS, h);
		}
		@Override
		public void start() {
			// TODO Auto-generated method stub
			PrizeLogs.v(LOG_TAG, "--angle changed:" + curAngleFlag + " angle=" + curAngle);
			if(mSenMgr != null) {
				// 开始判断时，做初始化
				mAccSkipCnt = 0;
				isClose = lastClose = false;
				mLastdistance = 0;
				curAngleFlag = lastAngleFlag = false;
				
				// start listening
				if(mProxSensor != null) {
					if(mProxLisRegistered == false) {
						mSenMgr.registerListener(mProxListener, 
							mProxSensor,SensorManager.SENSOR_DELAY_FASTEST);
						mProxLisRegistered = true;
					}
				}
				if(mAccSensor != null) {
					if(mAccLisRegistered == false) {
						mSenMgr.registerListener(mAccListener, 
							mAccSensor,SensorManager.SENSOR_DELAY_FASTEST);
						mAccLisRegistered = true;
					}					
				}
			}
		}
		@Override
		public void stop() {
			// TODO Auto-generated method stub
			if(mSenMgr !=null) {
				// stop listening
				if(mProxSensor != null) {
					if(mProxLisRegistered == true) {
						mSenMgr.unregisterListener(mProxListener);
						mProxLisRegistered = false;					
					}
				}
				if(mAccSensor != null) {
					if(mAccLisRegistered == true) {
						mSenMgr.unregisterListener(mAccListener);
						mAccLisRegistered = false;					
					}					
				}
			}
		}
		/* a fir filter */
		private int _NTAPS = 6;
		private double[] h = {
			0.125514644795420960,
			0.414388923238107440,
			-0.013420976983735622,
			-0.013420976983735622,
			0.414388923238107440,
			0.125514644795420960
			};
	}
	
	/***********************************************************************/
	/* ifrank,2012.12.19, 口袋模式
	 * 
	 * 目前方法: 当判断手机不是平躺着，和光感较小时，即认为是放到口袋里了。
	 * 
	 * 默认认为不在口袋模式里,这样，当处于口袋时，会判断到在口袋里了. frankie,2013.03.14, not work ?
	 * 
	 * */
	public static class PrizeGeneImplPocketModeV2Result {
		public boolean state; // true-in pocket, false - not
		public boolean inPocket;
		public float[] light;
		public double angle;
		public double xyLen;
		public double tLen;
		public PrizeGeneImplPocketModeV2Result(boolean state, boolean isDark, float[] light, double angle, double xyLen, double tLen) {
			this.state = state;
			this.inPocket = isDark;
			this.light = light.clone();
			this.angle = angle;
			this.xyLen = xyLen;
			this.tLen = tLen;
		}
	}
	public class PrizeGeneImplPocketModeV2 implements PrizeGeneMode {
		private static final String LOG_TAG = "prize";

		
		private static final long __REPORT_DELAY = 500; // unit:ms
		
		private SensorManager sensorManager = null;
		private int __skip_samples_cnt = 0;
		private long lastEvtTime = 0;
		private boolean first = false;
		private boolean first_acc = false;
		private boolean last_report_state = false;
		
		private Sensor mDefaultAccSensor = null;
		private float[] curGravity = new float[3];
		private double curAngle; // 重力矢量和xy平面的夹角
		private double gravityProjToXY;	// 或者判断重力矢量在xy平面的投影长度
		private double gravityLen; //
		private int angleSteadyCnt = 0;
		private boolean angleFlag = false; // true-在竖直姿态, false-不在
		private boolean angleLastFlag = false;
		
		private Sensor mDefaultLightSensor = null;
		private float[] curLight;
		private boolean isDark = false;
		private boolean lastDark = false;
		private int curLightCount = 0;
		private PrizeGeneHelper.lowPassFilter filter1,filter2,filter3;
		
		private SensorEventListener mLightListener = new SensorEventListener() {
			@Override
			public void onAccuracyChanged(Sensor sensor, int accuracy) {
			}
			@Override
			public void onSensorChanged(SensorEvent event) {
				//processLightOnSensorChanged(event);
				processLightOnSensorChanged2(event);
			}
		};
		private SensorEventListener mAccListener = new SensorEventListener() {
			@Override
			public void onAccuracyChanged(Sensor sensor, int accuracy) {
			}
			@Override
			public void onSensorChanged(SensorEvent event) {
				//processAccOnSensorChanged(event);
				processAccOnSensorChanged2(event);
			}
		};
		private boolean mLightSensorEventListenerHaveRegsitered = false;
		private boolean mAccSensorEventListenerHaveRegsitered = false;
		
		public PrizeGeneImplPocketModeV2() {
			PrizeLogs.v(LOG_TAG, "PrizeGeneImplPocketModeV2 ... ");

			sensorManager = (SensorManager)mContext.getSystemService(Context.SENSOR_SERVICE);
			
			mDefaultAccSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
			PrizeGeneHelper.checksNull(mDefaultAccSensor, "default acc sensor");
			
		    mDefaultLightSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);
		    PrizeGeneHelper.checksNull(mDefaultLightSensor, "default light sensor");
		    
		    	// 这里有点奇怪, lowPassFilter(0, 0.99f);的系数设置小,结果也偏小
			filter1 = new PrizeGeneHelper.lowPassFilter(1, 25); // sma filter
			filter2 = new PrizeGeneHelper.lowPassFilter(0, 0.99f); // simple filter // weight越大越接近当前值
			filter3 = new PrizeGeneHelper.lowPassFilter(0, 0.99f);
		}
		
		@Override
		public void start() {
			PrizeLogs.v(LOG_TAG, "start. ");
			
			// 初始化判断
			// 开始时,应该给出一个事件, frankie,2013.03.14
			__skip_samples_cnt = 0;
			angleSteadyCnt = 0;
			angleFlag = angleLastFlag = false;	// !!!
			first = true;
			first_acc = true;
			
			if(sensorManager != null) {
				if(mDefaultAccSensor != null) {
				  if(mAccSensorEventListenerHaveRegsitered == false) {
					  PrizeLogs.v(LOG_TAG, "regsiter ... ");
			    	  sensorManager.registerListener(mAccListener,mDefaultAccSensor, SensorManager.SENSOR_DELAY_FASTEST); // 20ms  
			    	  mAccSensorEventListenerHaveRegsitered = true;
				  }
				}	
				if(mDefaultLightSensor != null) {
				  if(mLightSensorEventListenerHaveRegsitered == false) {
					  PrizeLogs.v(LOG_TAG, "regsiter ... ");
				   	  sensorManager.registerListener(mLightListener,mDefaultLightSensor, SensorManager.SENSOR_DELAY_FASTEST); // 20ms  
				   	  mLightSensorEventListenerHaveRegsitered = true;
				  }
				}
			}
		}

		@Override
		public void stop() {
			PrizeLogs.v(LOG_TAG, "stop. ");
			if(sensorManager != null) {
				if(mDefaultAccSensor != null) {
					if(mAccSensorEventListenerHaveRegsitered == true) {
						PrizeLogs.v(LOG_TAG, "unregsiter ... ");
						sensorManager.unregisterListener(mAccListener);
						mAccSensorEventListenerHaveRegsitered = false;
					}
				}	
				if(mDefaultLightSensor != null) {
					if(mLightSensorEventListenerHaveRegsitered == true) {
						PrizeLogs.v(LOG_TAG, "unregsiter ... ");
						sensorManager.unregisterListener(mLightListener);
						mLightSensorEventListenerHaveRegsitered = false;
					}
				}
			}
		}
		
		/* frankie,2013.03.18, old code */
		private boolean isLastReportTimeout() {
			long cur = System.currentTimeMillis();
			if((cur - lastEvtTime) > __REPORT_DELAY) {
				lastEvtTime = cur;
				return true;
			}
			return false;
		}
		
		private static final int __PROC_CHECK_DELAY = 10;
		private int __procCheckDelayCnt = 0;
		private void __process2() {
			if(!isLastReportTimeout()) {
				__procCheckDelayCnt = __PROC_CHECK_DELAY;
				return ;
			}
			__procCheckDelayCnt = 0;
			if(angleFlag == false) {
				if(lastDark == true) {
					lastDark = false;
					doActionProxy(POCKET_MODE_ACTION,
						new PrizeGeneImplPocketModeV2Result(false, false, curLight, curAngle, gravityProjToXY, gravityLen));					
				}
				return ;
			}
			
			// 在手机直立时,光线发生变化,就动作
			if(isDark != lastDark) {
				lastDark = isDark;
				doActionProxy(POCKET_MODE_ACTION,
					new PrizeGeneImplPocketModeV2Result(false, isDark, curLight, curAngle, gravityProjToXY, gravityLen));						
			}
		}
		private static final int __SKIP_SMAPLES = 30;
		private static final double __ANGLE_THRESHOLD = 50.0f; // unit:degree	// frankie,2013.03.18, change from 25.0f
		private static final double __ANGLE_SLUGGISH = 5.0f;
		private static final int __ANGLE_STEADY_COUNTS = 10; // 状态改变去抖, 小于 __SKIP_SMAPLES
		private static final int __LIGHT_CHANGED_DELAY = 60; // 50;
		private int __lightChangedDelayCnt = 0;
		private int __lightRawValueChangedCnt = 0;
		
		private void processAccOnSensorChanged(SensorEvent event) {
			curGravity = filter1.SMAFilter(filter2.filter(event.values));
			double vLen = PrizeGeneHelper.vecLength(curGravity, 3);
			double prjLen = PrizeGeneHelper.vecLength(curGravity, 2);
			gravityLen = vLen;
			gravityProjToXY = prjLen;
			double angle = Math.toDegrees(Math.acos(gravityProjToXY/gravityLen));
			curAngle = filter3.filter((float)angle);
			if(__skip_samples_cnt < __SKIP_SMAPLES) {
				__skip_samples_cnt++;
				return ;
			}
			if(angleFlag) {
				if(curAngle > (__ANGLE_THRESHOLD + __ANGLE_SLUGGISH)) {
					angleSteadyCnt++;
					if(angleSteadyCnt > __ANGLE_STEADY_COUNTS) {
						angleSteadyCnt = 0;
						angleFlag = false;
					}
				} else {
					angleSteadyCnt = 0;
				}
			} else {
				if(curAngle < (__ANGLE_THRESHOLD - __ANGLE_SLUGGISH)) {
					angleSteadyCnt++;
					if(angleSteadyCnt > __ANGLE_STEADY_COUNTS) {
						angleSteadyCnt = 0;
						angleFlag = true;
					}
				} else {
					angleSteadyCnt = 0;
				}				
			}
			if(angleFlag != angleLastFlag) {
				angleLastFlag = angleFlag;
				PrizeLogs.v(LOG_TAG, "***************angle changed:" + angleFlag + " angle=" + curAngle);
				__process2();
			}
			if(__lightChangedDelayCnt > 0) {
				__lightChangedDelayCnt--;
				if(__lightChangedDelayCnt == 0) {
					PrizeLogs.v(LOG_TAG, "--light changed do! = " + isDark);
					__process2();
				}
			}
			if(__procCheckDelayCnt > 0) {
				__procCheckDelayCnt--;
				if(__procCheckDelayCnt == 0) {
					__process2();
				}
			}
		}
		
		private static final int __LIGHT_THRESHOLD = 200;
		private static final int __LIGHT_SLUGGISH = 5; // 15~25
		private void processLightOnSensorChanged(SensorEvent event) {
			/*
			 * 10,20,40,80,120
			 * 这里的数据是有梯度的离散值, 不能进行滤波
			 * 但在三星手机上,输出却是连续变化的
			 * */
			curLight = event.values.clone();
			PrizeLogs.v(LOG_TAG, "LightOnSenso--light:" + curLight[0]);
			curLightCount++;
			if(curLightCount >= 3){
				if(__skip_samples_cnt < __SKIP_SMAPLES) {
					return ;
				}
				if(isDark) {
					if(curLight[0] >= (__LIGHT_THRESHOLD + __LIGHT_SLUGGISH)) {
						isDark = false;
						__lightChangedDelayCnt = __LIGHT_CHANGED_DELAY;
						PrizeLogs.v(LOG_TAG, "--light changed:" + isDark);
					}
				} else {
					if(curLight[0] <= (__LIGHT_THRESHOLD - __LIGHT_SLUGGISH)) {
						isDark = true;
						__lightChangedDelayCnt = __LIGHT_CHANGED_DELAY;
						PrizeLogs.v(LOG_TAG, "--light changed:" + isDark);
					}				
				}
				curLightCount = 0;
			}
		}
		
		/********************************************/
		/* frankie, 2013.03.18, new code */
		private void processAccOnSensorChanged2(SensorEvent event) {
			curGravity = filter1.SMAFilter(filter2.filter(event.values));
			double vLen = PrizeGeneHelper.vecLength(curGravity, 3);
			double prjLen = PrizeGeneHelper.vecLength(curGravity, 2);
			gravityLen = vLen;
			gravityProjToXY = prjLen;
			double angle = Math.toDegrees(Math.acos(gravityProjToXY/gravityLen));
			curAngle = filter3.filter((float)angle);
			
			if(first_acc) {
				first_acc = false;
				if(curAngle < __ANGLE_THRESHOLD) {
					angleLastFlag = angleFlag = true;
				} else {
					angleLastFlag = angleFlag = false;
				}
			} else {
				if(angleFlag) {
					if(curAngle > (__ANGLE_THRESHOLD + __ANGLE_SLUGGISH)) {
						angleSteadyCnt++;
						if(angleSteadyCnt > __ANGLE_STEADY_COUNTS) {
							angleSteadyCnt = 0;
							angleFlag = false;
						}
					} else {
						angleSteadyCnt = 0;
					}
				} else {
					if(curAngle < (__ANGLE_THRESHOLD - __ANGLE_SLUGGISH)) {
						angleSteadyCnt++;
						if(angleSteadyCnt > __ANGLE_STEADY_COUNTS) {
							angleSteadyCnt = 0;
							angleFlag = true;
						}
					} else {
						angleSteadyCnt = 0;
					}				
				}
				if(angleFlag != angleLastFlag) {
					angleLastFlag = angleFlag;
					
						if(angleFlag) {
							if(last_report_state != isDark) {
							doActionProxy(POCKET_MODE_ACTION,
									new PrizeGeneImplPocketModeV2Result(false, isDark, curLight, curAngle, gravityProjToXY, gravityLen));
							last_report_state = isDark;
							}
						} else {
							if(last_report_state != false) {
							doActionProxy(POCKET_MODE_ACTION,
									new PrizeGeneImplPocketModeV2Result(false, false, curLight, curAngle, gravityProjToXY, gravityLen));
							last_report_state = false;
							}
						}
				}
			}
			//if(__skip_samples_cnt < __SKIP_SMAPLES) {
			//	__skip_samples_cnt++;
			//	return ;
			//}
			
			//PrizeLogs.v(LOG_TAG, "--angle:" + angleFlag + " value:" + curAngle);
			
			if(__lightRawValueChangedCnt != 0) {
				__lightRawValueChangedCnt--;
				if(__lightRawValueChangedCnt == 0) {
					float light = curLight[0];
					
					Log.v(LOG_TAG, "light steady value:" + curLight[0]);
					if(first) {
						first = false;
						if(light < __LIGHT_THRESHOLD) { // 认为 dark
							isDark = lastDark = true; // !!!
						}
						else { // 认为 bright
							isDark = lastDark = false; // !!!
						}
						
						if(angleFlag) {
							doActionProxy(POCKET_MODE_ACTION,
									new PrizeGeneImplPocketModeV2Result(false, isDark, curLight, curAngle, gravityProjToXY, gravityLen));
							last_report_state = isDark;
						} else {
							doActionProxy(POCKET_MODE_ACTION,
									new PrizeGeneImplPocketModeV2Result(false, false, curLight, curAngle, gravityProjToXY, gravityLen));
							last_report_state = false;
						}
					}
					else {
						if(isDark) {
							if(light >= (__LIGHT_THRESHOLD + __LIGHT_SLUGGISH)) {
								isDark = false;
								PrizeLogs.v(LOG_TAG, "--light changed11111:" + isDark);
							}
						} else {
							if(light <= (__LIGHT_THRESHOLD - __LIGHT_SLUGGISH)) {
								isDark = true;
								PrizeLogs.v(LOG_TAG, "--light changed22222:" + isDark);
							}				
						}
						if(isDark != lastDark) { // 光线发生变化
							lastDark = isDark;
							if(angleFlag) {
								if(last_report_state != isDark) {
								doActionProxy(POCKET_MODE_ACTION,
									new PrizeGeneImplPocketModeV2Result(false, isDark, curLight, curAngle, gravityProjToXY, gravityLen));
								last_report_state = isDark;
								}
							} else {
								if(last_report_state != false) {
								doActionProxy(POCKET_MODE_ACTION,
									new PrizeGeneImplPocketModeV2Result(false, false, curLight, curAngle, gravityProjToXY, gravityLen));	
								last_report_state = false;
								}
							}
						}
					}
				}
			}
			
			// process
		}
		// 当打开时,这个不一定马上会有数据出来 ?
		// 默认为是在口袋中.
		private void processLightOnSensorChanged2(SensorEvent event) {
			curLight = event.values.clone();
			PrizeLogs.v(LOG_TAG, "--light:" + curLight[0]);
			//if(__skip_samples_cnt < __SKIP_SMAPLES) {
			//	return ;
			//}
			__lightRawValueChangedCnt = __LIGHT_CHANGED_DELAY;
		}
		
	}
	/********************************************/
	/* ifrank, 2012.12.17, 智能平移
	 * 
	 * 使用场景：
	 * 调整桌面图标位置时，轻轻左右甩动设备，以移动到上一页/下一页.
	 * 
	 * 实现：判断xy平面上的加速度的值，是否超过阀值;
	 * 		 通过xy平面的加速度矢量和x轴的夹角判断当前是左甩，还是右甩
	 * */
	public static class PrizeGeneImplIconShakeMovingResult {
		public int dir;
		public PrizeGeneImplIconShakeMovingResult(int dir) {
			this.dir = dir;
		}
	}
	private class PrizeGeneImplIconShakeMoving implements PrizeGeneMode, SensorEventListener {
		private static final String LOG_TAG = "PrizeGeneImplIconShakeMoving";
		
		  private final double __mDitherThreshold = 15.0;
		  private final double __mDitherThresholdxy = 10.0;
		  private final long __mDitherDelayTime = 800; // unit:ms
		  
		private String service_name = Context.SENSOR_SERVICE;
		private SensorManager sensorManager;
		private List<Sensor> mAccelerometers;
		private Sensor mCurAccele;
		
		private Sensor mDefaultAcc;
		private PrizeGeneHelper.lowPassFilter filter1;
		private PrizeGeneHelper.lowPassFilter filter2;
		private boolean mSensorEventListenerHaveRegsitered = false;
		
		private float[] curAccVal = new float[3];
		private float[] curLinearAcc= new float[3];
		private float[] curGravity = new float[3];
		
		  private int __mDitherMaxCnt = 0;
		  private float[] lastLinearAcc = new float[3];
		  private long __mDitherLastTime = 0;
		  
		public PrizeGeneImplIconShakeMoving() {
			PrizeLogs.v(LOG_TAG, "PrizeGeneImplIconShakeMoving ... ");
			mSensorEventListenerHaveRegsitered = false;
			sensorManager = (SensorManager)mContext.getSystemService(Context.SENSOR_SERVICE);
			mDefaultAcc = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
			PrizeGeneHelper.checksNull(mDefaultAcc, "default acc sensor");
			
			filter1 = new PrizeGeneHelper.lowPassFilter(1, 20); // sma filter
			filter2 = new PrizeGeneHelper.lowPassFilter(0, 0.9f); // simple filter // weight越大越接近当前值
		}

		  //
		  private void processLinearAcc(float[] curLinearAcc) {
//		  		// 计算加速度的总值
//			float diffx = Math.abs(lastLinearAcc[0] - curLinearAcc[0]);
//			float diffy = Math.abs(lastLinearAcc[1] - curLinearAcc[1]);
//			float diffz = Math.abs(lastLinearAcc[2] - curLinearAcc[2]);
//			double diff_vector_length = Math.sqrt(diffx*diffx + diffy*diffy + diffz*diffz);
//			if(diff_vector_length > __mDitherThreshold) {
//				__mDitherMaxCnt++;
//				if(__mDitherMaxCnt > 2) {
//					
//					lastLinearAcc[0] = curLinearAcc[0];
//					lastLinearAcc[1] = curLinearAcc[1];
//					lastLinearAcc[2] = curLinearAcc[2];	
//					
//					long curTime = System.currentTimeMillis();
//					long diff = curTime - __mDitherLastTime;
//					
//					if(diff > __mDitherDelayTime) {
//						
//						PrizeLogs.v(LOG_TAG,"diff_vector_length=" + diff_vector_length);
//						PrizeLogs.v(LOG_TAG, "dither len = " + String.valueOf(diff_vector_length));
//						doActionProxy(ICON_SHAKE_MOVE_ACTION_TEST, null);
//						__mDitherLastTime = curTime;
//					}
//				}
//			}
//			else {
//				__mDitherMaxCnt = 0;
//				lastLinearAcc[0] = curLinearAcc[0];
//				lastLinearAcc[1] = curLinearAcc[1];
//				lastLinearAcc[2] = curLinearAcc[2];				
//			}
		  
		  	// 只判断x方向的加速度!
		  float diffx = lastLinearAcc[0] - curLinearAcc[0];
		  float diffy = lastLinearAcc[1] - curLinearAcc[1];
		  double diff_vector_length = Math.sqrt(Math.abs(diffx*diffx) + Math.abs(diffy*diffy));
		  double curAng = Math.toDegrees(Math.atan2(diffx, diffy));
		  	// angle: vector and x-axis
		  
		  if(diff_vector_length > __mDitherThresholdxy) {
			  PrizeLogs.v(LOG_TAG, "curAng = " + curAng);
			  __mDitherMaxCnt++;
			  if(__mDitherMaxCnt >= 2) {
				  long curTime = System.currentTimeMillis();
				  long diff = curTime - __mDitherLastTime;
				  if(diff > __mDitherDelayTime) {
					  PrizeLogs.v(LOG_TAG, "-------- xy diff_vector_length = " + diff_vector_length);
					  PrizeLogs.v(LOG_TAG, "-------- angle = " + curAng);
					  if(curAng < 0.0) {
						  doActionProxy(ICON_SHAKE_MOVE_ACTION,
							new PrizeGeneImplIconShakeMovingResult(0));
					  } else {
						  doActionProxy(ICON_SHAKE_MOVE_ACTION,
							new PrizeGeneImplIconShakeMovingResult(1));
					  }
					  __mDitherLastTime = curTime;
				  }
			  }
		  }else {
			  __mDitherMaxCnt = 0;
		  }
			lastLinearAcc[0] = curLinearAcc[0];
			lastLinearAcc[1] = curLinearAcc[1];
			lastLinearAcc[2] = curLinearAcc[2];		
	  }
		  
		@Override
		public void onAccuracyChanged(Sensor sensor, int accuracy) {
			// TODO Auto-generated method stub
		}
		@Override
		public void onSensorChanged(SensorEvent event) {
			// TODO Auto-generated method stub
			curAccVal = event.values.clone();

			//printAccleData0(curAccVal);
			//curGravityAcc = filter1.filter(curVal);
			curGravity = filter1.SMAFilter(curAccVal);
			
			curLinearAcc[0] = curAccVal[0] - curGravity[0];
			curLinearAcc[1] = curAccVal[1] - curGravity[1];
			curLinearAcc[2] = curAccVal[2] - curGravity[2];
			curLinearAcc = filter2.filter(curLinearAcc);

			processLinearAcc(curLinearAcc);
		}

		@Override
		public void start() {
			// TODO Auto-generated method stub
			PrizeLogs.v(LOG_TAG, "start. ");
			
			if(mDefaultAcc != null) {
			  if(mSensorEventListenerHaveRegsitered == false) {
				  PrizeLogs.v(LOG_TAG, "regsiter ... ");
		    	  sensorManager.registerListener(this,mDefaultAcc, 
		    		SensorManager.SENSOR_DELAY_FASTEST
	//	    		SensorManager.SENSOR_DELAY_NORMAL
		    		); // 20ms  
		    	  mSensorEventListenerHaveRegsitered = true;
			  }
			}
		}

		@Override
		public void stop() {
			// TODO Auto-generated method stub
			PrizeLogs.v(LOG_TAG, "stop. ");
			
			if(mDefaultAcc != null) {
				if(mSensorEventListenerHaveRegsitered == true) {
					PrizeLogs.v(LOG_TAG, "unregsiter ... ");
					sensorManager.unregisterListener(this);
					mSensorEventListenerHaveRegsitered = false;
				}
			}
		}
		
	  	// 间隔50次,连续打印5次
		  private static final int __mPrintData0Delay = 50;
		  private static final int __mPrintData0Delay2 = 5;
		  private int __mPrintData0Cnt = 0;
		  private int __mPrintData0Cnt2 = 0;
		  private boolean __mPrintData0Flag = false;
		  private long __mPrintData0LastTime = 0;
		  private void printAccleData0(float[] data) {
			  if(__mPrintData0Flag) {
				  __mPrintData0Cnt2++;
				  if(__mPrintData0Cnt2 >= __mPrintData0Delay2) {
					  __mPrintData0Cnt2 = 0;
					  __mPrintData0Flag = false;
				  }
				  long curTime = System.currentTimeMillis();
				  long diffTime = curTime - __mPrintData0LastTime;
//				  PrizeLogs.v(LOG_TAG, "TSP="+String.valueOf(curTime)
//					+ ", x= " + String.valueOf(data[0])
//					+ ", y= " + String.valueOf(data[1])
//					+ ", z= " + String.valueOf(data[2])
//					+ ", diffTime=" + String.valueOf(diffTime));
				  System.out.printf("TSP=%010d x=%2.2f y=%2.2f z=%2.2f diffTime=%04d\r\n",
					curTime, data[0], data[1], data[2], diffTime);
				  __mPrintData0LastTime = curTime;
				  return ;
			  }
			  __mPrintData0Cnt++;
			  if(__mPrintData0Cnt >= __mPrintData0Delay) {
				  __mPrintData0Cnt = 0;
				  __mPrintData0Cnt2 = 0;
				  __mPrintData0Flag = true;
				  __mPrintData0LastTime = System.currentTimeMillis();
				  PrizeLogs.v(LOG_TAG,"------------------- data:");
			  }
		  }
	}
	
	/********************************************/
	/* ifrank, 2012.12.17 , 智能提醒
	 * 当你拿起手机时，手机能通过振动，提醒你查看漏掉的来电、短信或日程安排提醒。
	 * 
	 * 场景： 
	 * 		当手机放到桌上，外出时，要带上手机时，此时拿起手机，给予提醒！
	 * 实现： 
	 * 		仅仅判断加速度值，并且只给出一次晃动提醒!
	 * 问题: 
	 * 		如果手机在休眠状态下,那么代码是不执行的, 也就判断不了当前手机是否拿起了;
	 * 		否则,只能做成熄屏,但不停止程序的执行, 但这样会严重增加耗电!
	 * 		如何处理?
	 * 
	 * */
	private class PrizeGeneImplSmartAlert implements PrizeGeneMode {
		@Override
		public void start() {
			
		}
		@Override
		public void stop() {
		}
	}
	
	/********************************************/
	/* ifrank,2012.12.21, 倾斜以缩放
	 * 实现: 加速度传感器, 检测当前的重力方向和手机的xy平面的夹角 
	 * */
	public static class PrizeGeneImplZoomBySlopeResult {
		public double angle;
		public double angle2;
		public float[] gravity;
		public double tLen;
		public double sLen;
		public double zLen;
		public PrizeGeneImplZoomBySlopeResult(double angle, double angle2, 
			float[] gravity, double tLen, double sLen, double zLen) {
			this.angle = angle;
			this.angle2 = angle2;
			this.gravity = gravity.clone();
			this.tLen = tLen;
			this.sLen = sLen;
			this.zLen = zLen;
		}
	}
	private class PrizeGeneImplZoomBySlope implements PrizeGeneMode {
		private static final String LOG_TAG = "PrizeGeneImplZoomBySlope";
		private static final int _SKIP_SAMPLES = 20; // 跳过刚开始的若干个采样
		private static final float __VECTOR_MIN_LENGTH = 2.0f; // 实际测
		
		private String service_name = Context.SENSOR_SERVICE;
		private SensorManager sensorManager;
		private Sensor mDefaultAcc;
		
		private PrizeGeneHelper.lowPassFilter filter1;
		private PrizeGeneHelper.lowPassFilter filter2; // ?
		private PrizeGeneHelper.lowPassFilter filter3; // ?
		
		private PrizeGeneHelper.firLowPassFilter firFilter1;
		private PrizeGeneHelper.firLowPassFilter firFilter2;
		private PrizeGeneHelper.firLowPassFilter firFilter3;
		
		private boolean mSensorEventListenerHaveRegsitered = false;
		private int skip_sample_count = _SKIP_SAMPLES;
		
		private float[] curAccVal = new float[3];
		private float[] curGravity1 = new float[3];
		private float[] curGravity2 = new float[3];
		private double _lastAngle = 0.0f;
		private double _lastAngle2 = 0.0f;
		private double _curAngle = 0.0f;
		private double _curAngle2 = 0.0f;
		private double _tot_length = 0;
		private double _dimen_length = 0;
		private double _z_length = 0;
		private int __type = 0;
		private boolean firstFlag = true;
		
		private static final double _ANGLE_DELTA = 0.3f;

		private float[] __fir__(float[] in) {
			float[] out = new float[3];
			out[0] = (float)firFilter1.filter1((double)in[0]);
			out[1] = (float)firFilter2.filter1((double)in[1]);
			out[2] = (float)firFilter3.filter1((double)in[2]);
			return out;
		}
		private SensorEventListener mSensorListener = new SensorEventListener() {
			@Override
			public void onAccuracyChanged(Sensor sensor, int accuracy) {
			}
			@Override
			public void onSensorChanged(SensorEvent event) {
				curAccVal = event.values.clone();
				curGravity1 = filter1.SMAFilter(curAccVal);

					// 这个效果似乎不好，数据抖动比单独用sma filter大! 所以在前面再加sma filter
				curGravity2 = __fir__(curGravity1);
			
				if(skip_sample_count > 0) {
					skip_sample_count--;
					return ;
				}
				if(__type == 0) { // 绕x
					// 竖立时, 计算yz平面上投影矢量和y轴的夹角
					double yzLen = PrizeGeneHelper.vecLength(new float[]{curGravity2[1], curGravity2[2]});
					double y = curGravity2[1];
					double z = curGravity2[2];
					double yAngle = 0;
					_tot_length = yzLen;
					_dimen_length = y;
					if(yzLen >= __VECTOR_MIN_LENGTH) {
						float rawAngle = (float)Math.toDegrees(Math.acos(y/yzLen));
						//yAngle = filter2.filter(rawAngle); // problem? why?
						yAngle = rawAngle;
						_curAngle = yAngle;
						_z_length = z;
					}
					if(firstFlag) { // 第一次,给出一个动作!
						firstFlag = false;
						_lastAngle = _curAngle;
					}
					double diff = Math.abs(_lastAngle - _curAngle);
					if(diff > _ANGLE_DELTA) {
						_lastAngle = _curAngle;
						doActionProxy(ZOOM_BY_SLOPE_ACTION,
							new PrizeGeneImplZoomBySlopeResult(
								_curAngle, 0, curGravity2, _tot_length, _dimen_length, _z_length));
						
					}
				}
				else if(__type == 1) { // 绕y
					// 平放时, 计算zx平面上投影矢量和x轴的夹角
					double xzLen = PrizeGeneHelper.vecLength(new float[]{curGravity2[0], curGravity2[2]});
					double x = curGravity2[0];
					double z = curGravity2[2];
					double xAngle = 0;
					_tot_length = xzLen;
					_dimen_length = x;
					if(xzLen >= __VECTOR_MIN_LENGTH) {
						float rawAngle = (float)Math.toDegrees(Math.acos(x/xzLen));
						//xAngle = filter2.filter(rawAngle); // problem? why?
						xAngle = rawAngle;
						_curAngle = xAngle;
						_z_length = z;
					}
					if(firstFlag) { // 第一次,给出一个动作!
						firstFlag = false;
						_lastAngle = _curAngle;
					}
					double diff = Math.abs(_lastAngle - _curAngle);
					if(diff > _ANGLE_DELTA) {
						_lastAngle = _curAngle;
						doActionProxy(ZOOM_BY_SLOPE_ACTION,
							new PrizeGeneImplZoomBySlopeResult(
								_curAngle, 0, curGravity2, _tot_length, _dimen_length, _z_length));
						
					}
				}
				else if(__type == 2) {
					// 给出xy两个方向的倾角
					double yz_len = PrizeGeneHelper.vecLength(new float[]{curGravity2[1], curGravity2[2]});
					double xz_len = PrizeGeneHelper.vecLength(new float[]{curGravity2[0], curGravity2[2]});
					double x = curGravity2[0];
					double y = curGravity2[1];
					double z = curGravity2[2];
					double xAngle;
					double yAngle;
					
					if(yz_len >= __VECTOR_MIN_LENGTH) {
						xAngle = (float)Math.toDegrees(Math.acos(y/yz_len));
						_curAngle = xAngle;
					}
					if(xz_len >= __VECTOR_MIN_LENGTH) {
						yAngle = (float)Math.toDegrees(Math.acos(x/xz_len));
						_curAngle2 = yAngle;
					}
					if(firstFlag) { // 第一次,给出一个动作!
						firstFlag = false;
						_lastAngle = _curAngle + _ANGLE_DELTA + _ANGLE_DELTA; // 使得:_lastAngle -_curAngle > _ANGLE_DELTA
					}
					double diff = Math.abs(_lastAngle - _curAngle);
					double diff2 = Math.abs(_lastAngle2 - _curAngle2);
					if(diff > _ANGLE_DELTA || diff2 > _ANGLE_DELTA) {
						_lastAngle = _curAngle;
						_lastAngle2 = _curAngle2;
						doActionProxy(ZOOM_BY_SLOPE_ACTION,
							new PrizeGeneImplZoomBySlopeResult(
								_curAngle, _curAngle2, curGravity2, _tot_length, _dimen_length, _z_length));
					}
				}

			}
		};
		
			// constructor
		public PrizeGeneImplZoomBySlope(int type) {
			PrizeLogs.v(LOG_TAG, "PrizeGeneImplZoomBySlope ... ");

			sensorManager = (SensorManager)mContext.getSystemService(Context.SENSOR_SERVICE);
			mDefaultAcc = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
			PrizeGeneHelper.checksNull(mDefaultAcc, "default acc sensor");
			
			filter1 = new PrizeGeneHelper.lowPassFilter(1, 15); // sma filter
			filter2 = new PrizeGeneHelper.lowPassFilter(0, 0.9f); // weight越大越接近当前值
			filter3 = new PrizeGeneHelper.lowPassFilter(0, 0.9f);
			
			firFilter1 = new PrizeGeneHelper.firLowPassFilter(_NTAPS, h);
			firFilter2 = new PrizeGeneHelper.firLowPassFilter(_NTAPS, h);
			firFilter3 = new PrizeGeneHelper.firLowPassFilter(_NTAPS, h);
			
			mSensorEventListenerHaveRegsitered = false;
			__type = type; 
		}
		@Override
		public void start() {
			// 初始化
			firstFlag = true;
			
			// TODO Auto-generated method stub
			if(sensorManager != null && mDefaultAcc != null) {
				if(mSensorEventListenerHaveRegsitered == false) {
					sensorManager.registerListener(mSensorListener, mDefaultAcc, SensorManager.SENSOR_DELAY_FASTEST);
					skip_sample_count = _SKIP_SAMPLES;
					mSensorEventListenerHaveRegsitered = true;
				}
			}
		}

		@Override
		public void stop() {
			// TODO Auto-generated method stub
			if(sensorManager != null && mDefaultAcc != null) {
				if(mSensorEventListenerHaveRegsitered == true) {
					sensorManager.unregisterListener(mSensorListener);
					mSensorEventListenerHaveRegsitered = false;
				}
			}
		}
		
		/*
		 * 用scopeFIR得到的系数
		 * sample freq = 50Hz
		 * number of taps = 6
		 * passband upper freq = 2Hz
		 * stopband lower freq = 3Hz
		 * passband ripple = 1dB
		 * stopband attenuation = 1dB
		 * 
		 * 0.125514644795420960
		 * 0.414388923238107440
		 * -0.013420976983735622
		 * -0.013420976983735622
		 * 0.414388923238107440
		 * 0.125514644795420960	
		 * 
		 * 试下这个的效果!
		 */
		private int _NTAPS = 6;
		private double[] h = {
			0.125514644795420960,
			0.414388923238107440,
			-0.013420976983735622,
			-0.013420976983735622,
			0.414388923238107440,
			0.125514644795420960
			};
	}
	/********************************************/
	/* ifrank,2012.12.21, 双击移至顶部
	 * 实现: 加速度传感器检测双击
	 * 
	 * 计算手机y轴相对和全局z轴的夹角, 以判断手机是否竖直
	 * 同时判断在y轴方向的双击
	 * 
	 *          alpha < threshold
	 *       \ <----> /
	 *        \      /
	 *         \    / 
	 *       ---\  / -----
	 *         
	 * */
	public static class PrizeGeneImplDoubleClickToTopResult {
		public float[] curGrav;
		public float[] curLinearAcc;
		public float[] maxLinearAcc;
		public double yAngle;
		
		public PrizeGeneImplDoubleClickToTopResult(
			float[] curGrav, float[] curLinearAcc, float[] maxLinearAcc, double yAngle) {
			this.curGrav = curGrav.clone();
			this.curLinearAcc = curLinearAcc.clone();
			this.maxLinearAcc = maxLinearAcc.clone();
			this.yAngle = yAngle;
		}
	}
	private class PrizeGeneImplDoubleClickToTop implements PrizeGeneMode {
		private static final String LOG_TAG = "PrizeGeneImplDoubleClickToTop";
		
		private static final int __SKIP_ACC_SAMPLES = 25;
		private static final double __ANGLE_DELTA = 0.1f;
		private static final double __ANGLE_THRESHOLD = 70.0f; // 
		private static final double __ANGLE_SLUGGISH = 3.0f; // 
		private static final double __ANGLE_DEBOUNCE = 10;//
		private static final boolean ifCheckAngle = true; /* 是否判断手机在竖直方向的角度 */
		
		private SensorManager senMgr;
		private Sensor accSensor;
		private SensorEventListener mAccListener = new SensorEventListener() {
			@Override
			public void onAccuracyChanged(Sensor sensor, int accuracy) {
			}
			@Override
			public void onSensorChanged(SensorEvent event) {
				accEventProcess(event);
			}
		};
		private boolean isAccLisRegistered = false;
		private int __skip_acc_samples_cnt = 0;
		private float[] curGravity = new float[3];
		private float[] curLinearAcc = new float[3];
		private float[] maxAbsLinearAcc = new float[3];
		private double __last_angle = 0f;
		private double __cur_angle = 0f;
		private int __angleDebounceCnt = 0;
		private boolean __angleFlag = false;
		
		private PrizeGeneHelper.lowPassFilter filter1;
		private PrizeGeneHelper.lowPassFilter filter2;
		private PrizeGeneHelper.lowPassFilter filter3;
		
		public PrizeGeneImplDoubleClickToTop() {
			senMgr = (SensorManager)mContext.getSystemService(Context.SENSOR_SERVICE);
			accSensor = senMgr.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
			PrizeGeneHelper.checksNull(accSensor, "default acc sensor");
			
		    isAccLisRegistered = false;
		    
			filter1 = new PrizeGeneHelper.lowPassFilter(1, 20); // sma filter
			filter2 = new PrizeGeneHelper.lowPassFilter(0, 0.9f); // weight越大越接近当前值
			filter3 = new PrizeGeneHelper.lowPassFilter(0, 0.9f);
		}
		
		@Override
		public void start() {
			// 开始处理时的初始化
			__skip_acc_samples_cnt = 0;
			__angleFlag = false;
			__angleDebounceCnt = 0;
			for(int i=0;i<3;i++) {
				maxAbsLinearAcc[i] = 0;
			}
			
			// start listening
			if(senMgr != null) {
				if(accSensor != null) {
					if(isAccLisRegistered == false) {
						senMgr.registerListener(mAccListener, 
							accSensor, SensorManager.SENSOR_DELAY_FASTEST);
						isAccLisRegistered = true;
					}
				}
			}
		}

		@Override
		public void stop() {
			if(senMgr != null) {
				if(accSensor != null) {
					if(isAccLisRegistered == true) {
						senMgr.unregisterListener(mAccListener);
						isAccLisRegistered = false;
					}
				}
			}
		}
		

		/* check method 2 */
	    private float[] __cur_values = new float[3];
	    private long __cur_tsp;
	    private float[] __cur_linear = new float[3];
	    
	    private static final float ALPHA = 0.8f;
	    private float[] __grav = new float[3];
	    private float[] highPass(float x, float y, float z) {
	    	__grav[0] = ALPHA*__grav[0] + (1-ALPHA)*x;
	    	__grav[1] = ALPHA*__grav[1] + (1-ALPHA)*y;
	    	__grav[2] = ALPHA*__grav[2] + (1-ALPHA)*z;
	    	return new float[]{
	    		x-__grav[0],
	    		y-__grav[1],
	    		z-__grav[2]
	    	};
	    }
	    private boolean isBetween(float i, float min, float max) {
	    	if(i >= min && i <= max) {
	    		return true;
	    	}
	    	return false;
	    }
	    
	    /*
	     * 有连续两次过线, 每次过线的时间间隔很小
	     * 有较平坦的前段
	     * 后段有一段也较平坦, 也就是除前两次的抖动外, 再没后续连续的抖动
	     * 
	     * */
	    private final static float __IDLE_GRAV_HIGH = 1.0f;
	    private final static float __IDLE_GRAV_LOW = -1.0f;
	    private final static int __IDLE_TIME = 25; // 
	    private final static float __LINEAR_HIGH = 2.0f;
	    private final static float __LINEAR_LOW = -3.0f;
	    private final static float __LINEAR_VERY_LARGE = 8.0f;
	    
	    private int __state = 0; // state 
	    private int __idle_timer = 0;
	    private long __last_tsp = 0;
	    private long __state1_time = 0;
	    private int __wait_timer = 0;
	    
	    private void accSensorProc() { // ~20ms called
	    	__cur_linear = highPass(__cur_values[0], __cur_values[1], __cur_values[2]); // !!!
	    	
	    	//PrizeLogs.v(TAG, "__state = " + __state);
			if(Math.abs(__cur_linear[1]) >= __LINEAR_VERY_LARGE) { // 
				__idle_timer = 0;
				__state = 0;
				return ;
			}
			
	    	if(__state == 0) { // check idle
		    	if(isBetween(__cur_linear[1], __IDLE_GRAV_LOW, __IDLE_GRAV_HIGH)) {
		    		if(__idle_timer < __IDLE_TIME) {
		    			__idle_timer++;
		    		}
		    		if(__idle_timer >= __IDLE_TIME) {
		    			__idle_timer = 0;
		    			__state = 1;
		    			PrizeLogs.v(TAG, "__state = " + __state);
		    			__state1_time = __cur_tsp; // 转到1状态时的时间点
		    		}
		    	} else if(Math.abs(__cur_linear[1]) >= __LINEAR_VERY_LARGE) {
		    		__idle_timer = 0;
		    	} else {
		    		if(__idle_timer < __IDLE_TIME) {
		    			__idle_timer = 0;
		    		}
		    	}
	    	}
	    		// 以下等待两次,可改为判断2~3次?
	    	else if(__state == 1) { // 过线了
	    		if(__cur_linear[1] >= __LINEAR_HIGH) {
	    			__last_tsp = __cur_tsp;
	    			__state = 2; // 转2
	    			PrizeLogs.v(TAG, "__state = " + __state);
	    		}
	    	}
	    	else if(__state == 2) { // 等待到线以下
	    		if((__cur_tsp - __last_tsp) > 200) { // 在线上的时间间隔, ms!!!
	    			long diff = __cur_tsp - __last_tsp;
	    			__state = 0; // 转0
	    			PrizeLogs.v(TAG, "__state = " + __state + " err2" + " diff = " + diff);
	    			return ;
	    		}
	    		if(__cur_linear[1] < __LINEAR_HIGH) {
	    			long diff = __cur_tsp - __last_tsp;
	    			PrizeLogs.v(TAG, "--------- 1 diff = " + diff);
	    			__last_tsp = __cur_tsp;
	    			__state = 3;
	    			PrizeLogs.v(TAG, "__state = " + __state);
	    		}    		
	    	}
	    	else if(__state == 3) { // 过线了
	    		if((__cur_tsp - __last_tsp) > 320) { // 两次过线的间隔
	    			__state = 0; // 转0
	    			PrizeLogs.v(TAG, "__state = " + __state + "err3");
	    			return ;
	    		}
	    		if(__cur_linear[1] >= __LINEAR_HIGH) {
	    			long diff = __cur_tsp - __last_tsp;
	    			PrizeLogs.v(TAG, "--------- *** diff = " + diff);
	    			
	    			__last_tsp = __cur_tsp;
	    			__state = 4; // 转4
	    			PrizeLogs.v(TAG, "__state = " + __state);
	    		}
	    	}
	    	else if(__state == 4) { // 等待到线以下
	    		if((__cur_tsp - __last_tsp) > 200) { // 在线上的时间间隔
	    			__state = 0; // 转0
	    			PrizeLogs.v(TAG, "__state = " + __state + "err4");
	    			return ;
	    		}
	    		if(__cur_linear[1] < __LINEAR_HIGH) {
	    			long diff = __cur_tsp - __last_tsp;
	    			PrizeLogs.v(TAG, "--------- 2 diff = " + diff);
	    			__state = 5;
	    			PrizeLogs.v(TAG, "__state = " + __state);
	    			__wait_timer = 0;
	    			__last_tsp = __cur_tsp; // 最后一次线下时间
	    		}    		
	    	}
	    		/* 后段 */
	    	else if(__state == 5) {
	    		if(__cur_linear[1] >= __LINEAR_HIGH
	    			|| __cur_linear[1] <= __LINEAR_LOW) { // 
	    			__state = 0; // 转0
	    			PrizeLogs.v(TAG, "__state = " + __state + "err5");
	    			return ;
	    		}
	    		__wait_timer++;
	    		if(__wait_timer > 20) { // 计次数
	    			__state = 0; // 转0
	    			PrizeLogs.v(TAG, "__state = " + __state);
	    			// action ?
					doActionProxy(DOUBLE_CLICK_TO_TOP_ACTION,
						new PrizeGeneImplDoubleClickToTopResult(
							curGravity, curLinearAcc, maxAbsLinearAcc, __last_angle));
	    		}
	    	}
	    	
	    }		
		
	    /*
	     * 把频谱显示出来看看啥样子？ i,frank,2013.01.30
	     * 截取一段采样进行fft运算
	     * 不能区分双击和单击? 两个结合?
	     * 
	     * frankie, 2013.02.06, 每得到一个采样，滑动计算fft
	     * 只要有两次过冲， 
	     * 
	     * frankie, 2013.02.07, add here!!!
	     * 感觉还可以吧
	     * frankie, 2013.02.27, 好象有时拿起手机会动作一下, 为何?
	     * 
	     * */
	    private static final int __SAMPLES_SIZE = 64;
	    private PrizeGeneHelper.FftImpl1 fft1 = new PrizeGeneHelper.FftImpl1();
	    private double[] __in_samples = new double[__SAMPLES_SIZE];
	    private int __in_size = 0;
	    private double[] __out_results = new double[__SAMPLES_SIZE];
	    private double[] __out_results2 = new double[__SAMPLES_SIZE];
	    private int __in_cnts = 0;
	    private int __skip_samples = 0;
	    private int __skip_post = 0;
	    
	    private final double __IDLE_GRAV_LOW2 = -0.5f;
	    private final double __IDLE_GRAV_HIGH2 = 0.5f;
	    private final int __IDLE_TIME2 = 1;
	    private long __idle_last_time = 0;
	    private int __samples_start_cnts = 0;
	    private double[] __samples_start_values = new double[__SAMPLES_SIZE];
	    
	    // 记录敲击过程中的最大最小值
	    private double __check_min = 0.0f;
	    private double __check_max = 0.0f;
	    private int __under_state = 0;
	    private int __under_cnts = 0;
	    
	    private void accSensorProc3() {
	    	__cur_linear = highPass(__cur_values[0], __cur_values[1], __cur_values[2]); // !!!
	    	double y = __cur_linear[1];
	    	if(__skip_samples <= __SAMPLES_SIZE) {
	    		__skip_samples++;
	    		return ;
	    	}
	    	
	    	// 整个
	    	if(__in_size < __SAMPLES_SIZE) { // 数据入队
	    		__in_samples[__in_size++] = y;
	    		return ;
	    	}else{
	    		int k;
	    		for(k=1;k<__SAMPLES_SIZE;k++) {
	    			__in_samples[k-1] = __in_samples[k];
	    		}
	    		__in_samples[__SAMPLES_SIZE-1] = y;
	    	}

			//KonkeLog.v(TAG, "cur __state = " + __state);
			
			// 从起始开始判断
			if(__state > 1) {
				PrizeLogs.v(TAG, "cur __state=" + __state + " y=" + y);
				if(__samples_start_cnts < __SAMPLES_SIZE) {
					__samples_start_values[__samples_start_cnts++] = y;
				}
				if(__check_min > y) {
					__check_min = y;
				}
				if(__check_max < y) {
					__check_max = y;
				}
				// 判断过程中,记录几次超过下界 1.0f!
				if(__under_state == 0) {
					if(y < -1.0f) {
						__under_state = 1;
					}
				} else if(__under_state == 1) {
					if(y > -0.8f) {
						__under_state = 0;
						__under_cnts++;
					}
				}
			} else {
				__check_min = 0;
				__check_max = 0;
				__under_state = 0;
				__under_cnts = 0;
			}
			// 判断开始
	    	if(__state == 0) { // check idle
		    	if(PrizeGeneHelper.isBetween(y, __IDLE_GRAV_LOW2, __IDLE_GRAV_HIGH2)) {
		    		if(__idle_timer < __IDLE_TIME2) { // 收到几次后
		    			__idle_timer++;
		    			__idle_last_time = __cur_tsp;
		    		}
		    		else {
		    			if(Math.abs(__cur_tsp - __idle_last_time) > (200)) {
			    			__state = 1;
			    			__in_cnts = 0;
			    			PrizeLogs.v(TAG, "__state = " + __state);
			    			__state1_time = __cur_tsp; // 转到1状态时的时间点
		    			}
		    		}
		    	} else {
		    		__idle_timer = 0;
		    	}
	    	}
	    	else if(__state == 1) { // 等待开始条件
	    		if(y >= 1.0f) {// > ? 就开始
	    			__last_tsp = __cur_tsp;
	    			__state = 2; // 转2
	    			
	    			__samples_start_cnts = 0;	// 开始采
	    			PrizeGeneHelper.arrayClear(__samples_start_values);
	    			__samples_start_values[__samples_start_cnts++] = y; // first sample!
	    			PrizeLogs.v(TAG, "turn to __state = " + __state);
	    		}
	    	}
	    	else if(__state == 2) { // 等待过冲到线以上
				if((__cur_tsp - __last_tsp) > 200) { // 
					long diff = __cur_tsp - __last_tsp;
					PrizeLogs.v(TAG, "err __state = " + __state + " diff = " + diff);
					__state = 0; // 转0
					return ;
				}
				if(y > 1.5f) { // 2.0f
					__last_tsp = __cur_tsp;
					__state = 3;
					PrizeLogs.v(TAG, "turn to __state = " + __state);
				}
	    	}
	    	else if(__state == 3) { // 等待到线下!
	    		PrizeLogs.v(TAG, "__state = " + __state + " y=" + y);
	    		
				if((__cur_tsp - __last_tsp) > 500) { // 线上超过200ms, error!
					long diff = __cur_tsp - __last_tsp;
					PrizeLogs.v(TAG, "err __state = " + __state + " diff=" + diff + " y=" + y + "111");
					__state = 0; // 转0
					return ;
				}
				if(y < 0.8f ) { // 1.5f
					__last_tsp = __cur_tsp;
					__state = 4;
					PrizeLogs.v(TAG, "turn to __state = " + __state);				
				}
	    	}
	    	else if(__state == 4) { // 等待再次过冲到线以上
				if((__cur_tsp - __last_tsp) > 450) { // 
					long diff = __cur_tsp - __last_tsp;
					PrizeLogs.v(TAG, "err __state = " + __state + " diff = " + diff + " 111");
					__state = 0; // 转0
					return ;
				}
				if(y > 1.0f) { // 2.0f
					if((__cur_tsp - __last_tsp) < 100) { // 时间太短
						long diff = __cur_tsp - __last_tsp;
						PrizeLogs.v(TAG, "err __state = " + __state + " diff=" + diff + " y=" + y + " 222");
						__state = 0; // 转0
						return ;				
					}
					__last_tsp = __cur_tsp;
					__state = 5;
					PrizeLogs.v(TAG, "turn to __state = " + __state);
				}    		
	    	}
	    	else if(__state == 5) {
				if((__cur_tsp - __last_tsp) > 500) { // 线上超过200ms, error!
					long diff = __cur_tsp - __last_tsp;
					PrizeLogs.v(TAG, "err __state = " + __state + " diff = " + diff);
					__state = 0; // 转0
					return ;
				}
				if(y < 1.0f ) { // 1.5f
					__last_tsp = __cur_tsp;
					__state = 6;
					PrizeLogs.v(TAG, "turn to __state = " + __state);				
				}    		
	    	}
	    	else if(__state == 6) {
	    		if(y > 1.5f) { // 2.0f
					long diff = __cur_tsp - __last_tsp;
					PrizeLogs.v(TAG, "err __state = " + __state + " diff = " + diff);
					__state = 0; // 转0
					return ;    			
	    		}
	    		if((__cur_tsp - __last_tsp) > 150) { // 200
	    			// ok
	    			if(__check_min < -10.0f) {
	    				PrizeLogs.v(TAG, "err __state = " + __state + " __check_min = " + __check_min);
	    				__state = 0;
	    				return ;
	    			}
	    			if(__check_max > 8.0f) {
	    				PrizeLogs.v(TAG, "err __state = " + __state + " __check_max = " + __check_max);
	    				__state = 0;
	    				return ;
	    			}
	    			__out_results = fft1.fft_sy(__in_samples).clone();
	    			__out_results2 = fft1.fft_sy(__samples_start_values).clone();
	    			
	    			// 计算标准差
	    			double avg1 = PrizeGeneHelper.get_average(__out_results);		// 整个历史的波形
	    			double sd1 = PrizeGeneHelper.get_std_deviation(__out_results);	
	    			double avg2 = PrizeGeneHelper.get_average(__out_results2);		// 单次的检测到的波形数据
	    			double sd2 = PrizeGeneHelper.get_std_deviation(__out_results2);
	    			PrizeLogs.v(TAG, " avg=" + avg1 + " sd=" + sd1);
	    			PrizeLogs.v(TAG, " avg2=" + avg2 + " sd2=" + sd2);
	    			PrizeLogs.v(TAG, " __check_min=" + __check_min + " __check_max=" + __check_max);
	    			PrizeLogs.v(TAG, " __samples_start_cnts = " + __samples_start_cnts);
	    			PrizeLogs.v(TAG, " __under_cnts = " + __under_cnts);
	    			if(__under_cnts >= 2) {
	    			if(avg1 < 20 && sd1 < 10) { // 15,8
	    				PrizeLogs.v(TAG, "----->### detected!!!");
		    			// action ?
						doActionProxy(DOUBLE_CLICK_TO_TOP_ACTION,
								new PrizeGeneImplDoubleClickToTopResult(
									curGravity, curLinearAcc, maxAbsLinearAcc, __last_angle));
	    			}
	    			}
	    			
	    			__state = 0;
	    		}
	    	}
	    }

		private void accEventProcess(SensorEvent event) {
			__cur_values = event.values.clone(); /* check method 2 */
			__cur_tsp = event.timestamp/1000000;
			
			
			// record maximum value!
//			float[] raw = filter2.filter(event.values);
//			curGravity = filter1.SMAFilter(raw);
//			curLinearAcc = filter3.filter(new float[]{
//				curGravity[0] - raw[0],
//				curGravity[1] - raw[1],
//				curGravity[2] - raw[2]
//			});
//			for(int i=0;i<3;i++) {
//				double abs = Math.abs(curLinearAcc[i]);
//				if(maxAbsLinearAcc[i] < abs) {
//					maxAbsLinearAcc[i] = (float)abs;
//				}
//			}
			
			// real process
			//accSensorProc();
			accSensorProc3(); // frankie, 2013.02.25
		}
		
	}
	/********************************************/
	/* ifrank,2012.12.21, 晃动以更新
	 * 实现: 加速度传感器检测到摇动
	 * */
	public static class PrizeGeneImplShakeToUpdateResult {
		public int state;
		public PrizeGeneImplShakeToUpdateResult(int state) {
			this.state = state;
		}
	}
	private class PrizeGeneImplShakeToUpdate implements PrizeGeneMode {
	    private SensorManager sMgr;
	    
    	// ----------------------------- accelerometer
	    private Sensor accelerometerSensor;
	    private boolean isReg = false;
		
	    private static final int __SKIP_FIRST_SAMPLES = 15;
	    private int __skip_cnts = 0;
	    private float[] __cur_values = new float[3];
	    private long __cur_tsp;
	    private float[] __cur_linear = new float[3];
	    private double __tot_length = 0;
	   
	    private int __state = 0; // state 
	    private int __idle_timer = 0;
	    private long __last_tsp = 0;
	    private long __state1_time = 0;
	    
		public PrizeGeneImplShakeToUpdate() {
			sMgr = (SensorManager)mContext.getSystemService(Context.SENSOR_SERVICE);
			if(sMgr != null) {
				accelerometerSensor = sMgr.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
				PrizeGeneHelper.checksNull(accelerometerSensor, "acc sensor");
			}
		}

	    private PrizeGeneHelper.FftImpl1 fft1 = new PrizeGeneHelper.FftImpl1();
	    private double[] __in_samples = new double[__SAMPLES_SIZE];
	    private int __in_size = 0;
	    private int __in_cnts = 0;
	    private double[] __out_results;
	    private double __check_value = 0;
	    
	    private static final int __SAMPLES_SIZE = 64;
	    private static final double __MAX_AVERAGE = 45.0f;
	    private static final double __MAX_STANDARD_DEVIATION = 55.0f;
	    private final static float __LINEAR_VERY_LARGE = 20.0f; // 晃动时设置较大
	    private final static int __IDLE_TIME = 8; // 15; // 
	    private final static float __IDLE_GRAV_HIGH = 3.0f;
	    private final static float __IDLE_GRAV_LOW = 0.0f;

	    private void ShakeToUpdateProc2() { // ~20ms called
	    	__cur_linear = PrizeGeneHelper.highPass(__cur_values[0], __cur_values[1], __cur_values[2]); // !!!
	    	__tot_length = PrizeGeneHelper.vecLength(__cur_linear);
	    	__check_value = __tot_length;
	    	//__check_value = __cur_linear[1];
			if(__skip_cnts > 0) {
				__skip_cnts--;
				return ;
			}
	    	if(__state == 0) { // check idle
		    	if(PrizeGeneHelper.isBetween((float)__check_value, __IDLE_GRAV_LOW, __IDLE_GRAV_HIGH)) {
		    		if(__idle_timer < __IDLE_TIME) {
		    			__idle_timer++;
		    		}
		    		if(__idle_timer >= __IDLE_TIME) {
		    			__idle_timer = 0;
		    			__state = 1;
		    			__in_cnts = 0;
		    			PrizeLogs.v(TAG, "__state = " + __state);
		    			__state1_time = __cur_tsp; // 转到1状态时的时间点
		    		}
		    	} else if(Math.abs(__check_value) >= __LINEAR_VERY_LARGE) {
		    		__idle_timer = 0;
		    	} else {
		    		if(__idle_timer < __IDLE_TIME) {
		    			__idle_timer = 0;
		    		}
		    	}
	    	}
	    	else if(__state == 1) { // 等待开始条件
	    		if(__check_value >= 2.0f) {// > ? 就开始
	    			__last_tsp = __cur_tsp;
	    			__state = 2; // 转2
	    		}
	    	}
	    	else if(__state == 2) { // 开始采64个点, 要1s多
	    		__in_samples[__in_cnts] = __check_value;
	    		__in_cnts++;
	    		if(__in_cnts >= __SAMPLES_SIZE) {
	    			// 采样结束,是否继续?
	    			__state = 0; // 转0
	    			
	    			__out_results = fft1.fft_sy(__in_samples);
	    			// 计算标准差
	    			double avg = PrizeGeneHelper.get_average(__out_results);
	    			double sd = PrizeGeneHelper.get_std_deviation(__out_results);
	    			
	    			PrizeLogs.v(TAG, "show it #avg=" + avg + " #sd=" + sd);
	    			if(avg > __MAX_AVERAGE			/* average check */
	    				&& sd > __MAX_STANDARD_DEVIATION		/* standard deviation check */
	    				) {
	    				PrizeLogs.v(TAG, "----- *got it! --------- ");
	    				//PrizeGeneHelper.__notify_sound(mContext);
						doActionProxy(SHAKE_TO_UPDATE_ACTION, new PrizeGeneImplShakeToUpdateResult(0));
	    			} 
	    		}
	    	}
	    	else {
	    		__state = 0; // 转0
	    	}
	    }
	    
	    // frankie,2013.02.25
	    private static final double __MAX_AVERAGE2 = 30.0f;
	    private static final double __MAX_STANDARD_DEVIATION2 = 50.0f;
	    private static final double __MIN_AVERAGE2 = 20.0f;
	    private static final double __MIN_STANDARD_DEVIATION2 = 40.0f;
	    private int __cal_skip_cnts = 0;
	    private boolean __is_shake = false;
	    private int __shaking_cnts = 0;
	    private void ShakeToUpdateProc3() { // ~20ms called
	    	__cur_linear = PrizeGeneHelper.highPass(__cur_values[0], __cur_values[1], __cur_values[2]); // !!!
	    	__tot_length = PrizeGeneHelper.vecLength(__cur_linear);
	    	__check_value = __tot_length;
	    	// skip
			if(__skip_cnts > 0) {
				__skip_cnts--;
				if(__skip_cnts == 0) {
					__cal_skip_cnts = 0;
					__in_size = 0;
					__is_shake = false;
				}
				return ;
			}
	    	// 数据入队
	    	double v = __check_value;
	    	if(__in_size < __SAMPLES_SIZE) { 
	    		__in_samples[__in_size++] = v;
	    		return ;
	    	}else{
	    		int k;
	    		for(k=1;k<__SAMPLES_SIZE;k++) {
	    			__in_samples[k-1] = __in_samples[k];
	    		}
	    		__in_samples[__SAMPLES_SIZE-1] = v;
	    	}			
	    	// 间隔几个点才计算
	    	__cal_skip_cnts++;
	    	if(__cal_skip_cnts < 10) { // ~200ms
	    		return ;
	    	}
	    	__cal_skip_cnts = 0;
			__out_results = fft1.fft_sy(__in_samples);
			// 计算标准差
			double avg = PrizeGeneHelper.get_average(__out_results);
			double sd = PrizeGeneHelper.get_std_deviation(__out_results);	
	    	
			PrizeLogs.v(TAG, "show it #avg=" + avg + " #sd=" + sd);
			
			if(__is_shake == false) {
				if(avg > __MAX_AVERAGE2	&& sd > __MAX_STANDARD_DEVIATION2) {
					PrizeLogs.v(TAG, "----- *got it! --------- ");
					__is_shake = true;
					__shaking_cnts = 0;
					//PrizeGeneHelper.__notify_sound(mContext);
					doActionProxy(SHAKE_TO_UPDATE_ACTION, new PrizeGeneImplShakeToUpdateResult(1));
					
					//__is_shake = false; // 清除历史数据
					//PrizeGeneHelper.arrayClear(__in_samples);
					//__in_size = 0;
				}
			} else {
				// 一直在摇晃呢,间隔一段时间触发
				if(avg > __MAX_AVERAGE2	&& sd > __MAX_STANDARD_DEVIATION2) {
					__shaking_cnts++;
					if(__shaking_cnts > 4) {
						__shaking_cnts = 0;
						PrizeLogs.v(TAG, "----- *got it again! --------- ");
						doActionProxy(SHAKE_TO_UPDATE_ACTION, new PrizeGeneImplShakeToUpdateResult(2));
					}
				}
				if(avg < __MIN_AVERAGE2	&& sd < __MIN_STANDARD_DEVIATION2) { // 因为之前数据的积累, 导致延迟后才下降下来!
					PrizeLogs.v(TAG, "----- *lost it! --------- ");
					__is_shake = false;
					//PrizeGeneHelper.__notify_sound(mContext);
					doActionProxy(SHAKE_TO_UPDATE_ACTION, new PrizeGeneImplShakeToUpdateResult(0));				
				}
			}
	    }
	    
	    private SensorEventListener accListener = new SensorEventListener() {
			public void onAccuracyChanged(Sensor sensor, int accuracy) {
			}
			public void onSensorChanged(SensorEvent event) {
				__cur_values = event.values.clone();
				__cur_tsp = event.timestamp/1000000;

				//ShakeToUpdateProc2();
				ShakeToUpdateProc3();
			}
	    };
		@Override
		public void start() {
			// initialization state
	    	__state = 0;
	    	__idle_timer = 0;
	    	__skip_cnts = __SKIP_FIRST_SAMPLES;
	    	if(sMgr != null) {
	    		if(accelerometerSensor == null) {
	    			accelerometerSensor = sMgr.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
	    		}
	    		if(accelerometerSensor != null && isReg == false) {
	    			sMgr.registerListener(accListener, accelerometerSensor,SensorManager.SENSOR_DELAY_FASTEST);
	    			isReg = true;
	    		}
	    	}			
		}
		@Override
		public void stop() {
	    	if(sMgr != null) {
	    		if(accelerometerSensor != null && isReg == true) {
	    			sMgr.unregisterListener(accListener, accelerometerSensor);
	    			//sMgr.unregisterListener(accListener);
	    			isReg = false;
	    		}
	    	}		
		}
		
	}
	
	/********************************************/
	/* ifrank,2012.12.21, 翻转以静音
	 * 实现:判断重力在z方向的变化, 接近-9.8时，为屏幕朝下，接近9.8时，屏幕完全朝上
	 * 每次判断到朝下时，等待一定的稳定时间后给出动作提示，
	 * 
	 * */
	public static class PrizeGeneImplOverturnToPauseResult{
		public float[] curGravity;
		public double zAngle;
		public boolean curUpDownFlag;
		
		public PrizeGeneImplOverturnToPauseResult(
			float[] curGravity, double zAngle, boolean curUpDownFlag) {
			this.curGravity = curGravity.clone();
			this.zAngle = zAngle;
			this.curUpDownFlag = curUpDownFlag;
		}
	}
	private class PrizeGeneImplOverturnToPause implements PrizeGeneMode {
		private static final String LOG_TAG = "PrizeGeneImplOverturnToPause";
		private static final int __SKIP_ACC_SAMPLES = 25;
		private static final double __PROT_FREEFALL_THRESHOLD = 1.0f;	// 当跌落时
		private static final double __PROT_STRONG_HIT = 30.0f;
		private static final int __PROT_DELAY = 50;	// 当有强烈冲击或者跌落时, 延迟检测
		private static final double __ANGLE_DIFF_THRESHOLD = 0.4f;
		private static final double __ANGLE_THRESHOLD = 135;
		private static final double __ANGLE_SLUGGISH = 5;
		private static final int __DEBOUNCE_CNT = 40;
		
		private SensorManager senMgr;
		private Sensor accSensor;
		private boolean isAccLisRegistered = false;

		private float[] curGravity = new float[3];
		private double zCurAngle = 0f;
		private int __skip_acc_samples_cnt = 0;
		private boolean mUpDownFlag = true;
		private int __debounce_cnts = 0;
		private int __prot_delay = 0;
		private boolean first_flag = true;
		
		private SensorEventListener mAccListener = new SensorEventListener() {
			@Override
			public void onAccuracyChanged(Sensor sensor, int accuracy) {
			}
			@Override
			public void onSensorChanged(SensorEvent event) {
				accEventProcess(event);
			}
		};
		
		private PrizeGeneHelper.lowPassFilter filter1;
		private PrizeGeneHelper.lowPassFilter filter2;
		
		public PrizeGeneImplOverturnToPause() {
			PrizeLogs.v(LOG_TAG, "PrizeGeneImplOverturnToPause new");
			
			senMgr = (SensorManager)mContext.getSystemService(Context.SENSOR_SERVICE);
			accSensor = senMgr.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
			PrizeGeneHelper.checksNull(accSensor, "default acc sensor");
			
		    isAccLisRegistered = false;
		    
			filter1 = new PrizeGeneHelper.lowPassFilter(1, 20); // sma filter
			filter2 = new PrizeGeneHelper.lowPassFilter(0, 0.9f); // weight越大越接近当前值
		}
		@Override
		public void start() {
			// TODO Auto-generated method stub
			// 开始处理时的初始化
			__skip_acc_samples_cnt = 0;
			mUpDownFlag = true;
			__debounce_cnts = 0;
			__prot_delay = 0;
			first_flag = true;
			
			// start listening
			if(senMgr != null) {
				if(accSensor != null) {
					if(isAccLisRegistered == false) {
						senMgr.registerListener(mAccListener, accSensor, SensorManager.SENSOR_DELAY_FASTEST);
						isAccLisRegistered = true;
					}
				}
			}
		}
		@Override
		public void stop() {
			// TODO Auto-generated method stub
			if(senMgr != null) {
				if(accSensor != null) {
					if(isAccLisRegistered == true) {
						senMgr.unregisterListener(mAccListener);
						isAccLisRegistered = false;
					}
				}
			}			
		}
		private void accEventProcess(SensorEvent event) {
			float[] raw = filter2.filter(event.values);
			curGravity = filter1.SMAFilter(raw);
			
			//PrizeLogs.v(LOG_TAG, "Acc got data . ");
			
			if(__prot_delay > 0) {
				__prot_delay--;
				return ;
			}
			double force = PrizeGeneHelper.vecLength(curGravity);
			if(force > __PROT_STRONG_HIT || force < __PROT_FREEFALL_THRESHOLD) {
				__prot_delay = __PROT_DELAY;
				return ;
			}
			
				// 这个角度范围 0~180
			double zLen = curGravity[2];
			double tLen = PrizeGeneHelper.vecLength(curGravity);
			zCurAngle = Math.toDegrees(Math.acos(zLen/tLen));
			if(__skip_acc_samples_cnt < __SKIP_ACC_SAMPLES) {
				__skip_acc_samples_cnt++;
				return ;
			}
			
			if(first_flag) {
				first_flag = false;
				if(zCurAngle > __ANGLE_THRESHOLD) {
					mUpDownFlag = true;
					__debounce_cnts = 0;
				} else {
					mUpDownFlag = false;
				}
			}
			
			//PrizeLogs.v(LOG_TAG, "z angle = " + zCurAngle);
			if(mUpDownFlag) {
				if(zCurAngle >= (__ANGLE_THRESHOLD+__ANGLE_SLUGGISH)) {
					__debounce_cnts++;
					if(__debounce_cnts >= __DEBOUNCE_CNT) {
						PrizeLogs.v(LOG_TAG, "to false");
						mUpDownFlag = false;
						doActionProxy(OVERTURN_TO_PAUSE_ACTION,
							new PrizeGeneImplOverturnToPauseResult(curGravity, zCurAngle, mUpDownFlag));
					}
				} else {
					__debounce_cnts = 0;
				}
			} else {
				if(zCurAngle < (__ANGLE_THRESHOLD-__ANGLE_SLUGGISH)) {
					__debounce_cnts++;
					if(__debounce_cnts >= __DEBOUNCE_CNT) {
						PrizeLogs.v(LOG_TAG, "to true");
						mUpDownFlag = true;
						doActionProxy(OVERTURN_TO_PAUSE_ACTION,
							new PrizeGeneImplOverturnToPauseResult(curGravity, zCurAngle, mUpDownFlag));
					}
				} else {
					__debounce_cnts = 0;
				}	
			}
				
		}
		
		/* class end */
	}
	public static class PrizeGeneImplActionToUnlockResult {
		public long diff;
		public PrizeGeneImplActionToUnlockResult(long diff) {
			this.diff = diff;
		}
	}
	private class PrizeGeneImplActionToUnlock implements PrizeGeneMode{
		private static final String LOG_TAG = "PrizeGeneImplActionToUnlock";
		
		private static final long __LOW_LAST_DIFF_TIME_THRESHOLD_LOW = 50; // ms
		//private static final long __LOW_LAST_DIFF_TIME_THRESHOLD_HIGH = 210; // 这个值试用时,感觉有些短了
		private static final long __LOW_LAST_DIFF_TIME_THRESHOLD_HIGH = 300; // frankie, 2013.02.06
				
		private SensorManager senMgr;
		private Sensor proxSensor;
		private boolean isProxListenerReg = false;
		private float __last_value = 0;
		private boolean first_flag = true;
		private long __last_time = 0;
		private long __cur_diff = 0;
		
			// non-arguments constructor
		public PrizeGeneImplActionToUnlock() {
			PrizeLogs.v(LOG_TAG, "PrizeGeneImplActionToUnlock new");
			
			senMgr = (SensorManager)mContext.getSystemService(Context.SENSOR_SERVICE);
			proxSensor = senMgr.getDefaultSensor(Sensor.TYPE_PROXIMITY);
			PrizeGeneHelper.checksNull(proxSensor, "default prox sensor");
			
			isProxListenerReg = false;		
		}
		@Override
		public void start() {
			// data initializing
			first_flag = true;
			
			// start listening
			if(senMgr == null) {
				return ;
			}
			if(proxSensor == null)
				return ;
			if(isProxListenerReg == true)
				return ;
			PrizeLogs.v(LOG_TAG, "start!");
			senMgr.registerListener(proxListener, proxSensor, SensorManager.SENSOR_DELAY_FASTEST);
			isProxListenerReg = true;
		}
		@Override
		public void stop() {
			if(senMgr == null)
				return ;
			if(proxSensor == null)
				return ;
			if(isProxListenerReg == false)
				return ;
			PrizeLogs.v(LOG_TAG, "stop!");
			senMgr.unregisterListener(proxListener, proxSensor);
			isProxListenerReg = false;
		}
		
		private boolean isBetween(long v, long min, long max) {
			if(v >= min && v <= max)
				return true;
			return false;
		}
		private SensorEventListener proxListener = new SensorEventListener() {
			@Override
			public void onAccuracyChanged(Sensor sensor, int accuracy) {
			}
			@Override
			public void onSensorChanged(SensorEvent event) {
				if(first_flag) {
					first_flag = false;
					__last_value = event.values[0];
					return ;
				}
				if(__last_value != event.values[0]) {
					if(__last_value != 0 && event.values[0] == 0) { // falling edge
						__last_time = event.timestamp;
					} else if(__last_value == 0 && event.values[0] != 0) { // raising edge
						__cur_diff = event.timestamp - __last_time;
						__cur_diff = __cur_diff/1000000;
						PrizeLogs.v(LOG_TAG, "__cur_diff = " + __cur_diff);
						// check it!
						if(isBetween(__cur_diff,
							__LOW_LAST_DIFF_TIME_THRESHOLD_LOW,
							__LOW_LAST_DIFF_TIME_THRESHOLD_HIGH)) {
							doActionProxy(ACTION_TO_UNLOCK_ACTION,
								new PrizeGeneImplActionToUnlockResult(__cur_diff));
						}
					}
					__last_value = event.values[0];
				}
			}
		};
	}
	
	/* frankie, 2013.03.06 add
	 * when in call coming screen show, pick up phone will decrease the volume
	 * this gene is to detect pick up action in this scenario!
demo example:
	PrizeGene gene = new PrizeGene(mContext, PrizeGene.DETECT_PHONE_PICK_UP_MODE) {
		@Override
		public void onAction(int action, Object params) {
			if(PrizeGene.DETECT_PHONE_PICK_UP_ACTION == action) {
				PrizeGene.PrizeGeneImplDetectPhonePickUpResult vobj = 
					(PrizeGene.PrizeGeneImplDetectPhonePickUpResult)params;
				// do your jobs here!
			}
		}
	}
	gene.start();
	gene.stop();
	 * */
	public static class PrizeGeneImplDetectPhonePickUpResult {
		public float[] gravity = new float[3];
		public float[] linear = new float[3];
		public double length = 0;
		public PrizeGeneImplDetectPhonePickUpResult(float[] g, float[] l, double t) {
			gravity = g.clone();
			linear = l.clone();
			length = t;
		}
	}
	private class PrizeGeneImplDetectPhonePickUp implements PrizeGeneMode {
		private static final String LOG_TAG = "PrizeGeneImplDetectPhonePickUp";
		private static final int __SKIP_SAMPLES = 20;
		private static final double __LENGTH_MAX = 8.0f;
		private static final double __LENGTH_THRESHOLD = 2.0f;
		
		private SensorManager senMgr;
		private Sensor accSensor;

		PrizeGeneHelper.lowPassFilter lowFilter1;
		PrizeGeneHelper.lowPassFilter lowFilter2;
		
		private boolean __acc_is_listen = false;
		private int __skip_cnt = 0;
		private float[] __cur_values = new float[3];
		private float[] __cur_gravity = new float[3];
		private float[] __cur_linear = new float[3];
		private double __cur_length = 0;
		private long __last_tsp = 0;
		private boolean __state = false;
		private long __cur_tsp = 0;
		
		public PrizeGeneImplDetectPhonePickUp() {
			PrizeLogs.v(LOG_TAG, "PrizeGeneImplActionToUnlock new");
			
			senMgr = (SensorManager)mContext.getSystemService(Context.SENSOR_SERVICE);
			accSensor = senMgr.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
			PrizeGeneHelper.checksNull(accSensor, "default acc sensor");
			
			lowFilter1 = new PrizeGeneHelper.lowPassFilter(0,0.2f);
			lowFilter2 = new PrizeGeneHelper.lowPassFilter(1,__SKIP_SAMPLES);
			__acc_is_listen = false;	
		}
		private SensorEventListener accListener = new SensorEventListener() {
			@Override
			public void onAccuracyChanged(Sensor sensor, int accuracy) {
			}
			@Override
			public void onSensorChanged(SensorEvent event) {
				__cur_values = event.values.clone();
				__cur_tsp = event.timestamp;
				process();
			}
		};
		private void process() {
			//__cur_gravity = lowFilter2.SMAFilter(lowFilter1.filter(__cur_values));
			__cur_gravity = lowFilter2.SMAFilter(__cur_values);
			
			if(__skip_cnt < __SKIP_SAMPLES) {
				__skip_cnt++;
				return ;
			}
			__cur_linear[0] = __cur_values[0] - __cur_gravity[0];
			__cur_linear[1] = __cur_values[1] - __cur_gravity[1];
			__cur_linear[2] = __cur_values[2] - __cur_gravity[2];
			__cur_length = PrizeGeneHelper.vecLength(__cur_linear);
		
			//doActionProxy(DETECT_PHONE_PICK_UP_ACTION,new PrizeGeneImplDetectPhonePickUpResult(__cur_gravity, __cur_linear, __cur_length));
			
			if(__state == false) {
				if(__cur_length > (__LENGTH_MAX + __LENGTH_THRESHOLD)) {
					if((__cur_tsp - __last_tsp) > 2000) {
					__last_tsp = __cur_tsp;
					doActionProxy(DETECT_PHONE_PICK_UP_ACTION,new PrizeGeneImplDetectPhonePickUpResult(__cur_gravity, __cur_linear, __cur_length));
					__state = true;
					}
				}
			} else {
				if(__cur_length < (__LENGTH_MAX - __LENGTH_THRESHOLD)) {
					__state = false;
				}
			}
		}
		@Override
		public void start() {
			__skip_cnt = 0;
			__state = false;
			__last_tsp = 0;
			if(senMgr != null && accSensor != null) {
				if(!__acc_is_listen) {
					__acc_is_listen = true;
					senMgr.registerListener(accListener, accSensor, SensorManager.SENSOR_DELAY_FASTEST);
				}
			}
		}
		@Override
		public void stop() {
			if(senMgr != null && accSensor != null) {
				if(__acc_is_listen) {
					__acc_is_listen = false;
					//senMgr.unregisterListener(accListener, accSensor);
					senMgr.unregisterListener(accListener);
				}
			}
		}

	}
	
	/********************************************/
	/* ifrank,2015.06.05, 隔空操作
	 * 
	 * */
	public static class PrizeGeneImplDistanceOperationResult {
		public long diff;
		public PrizeGeneImplDistanceOperationResult(long diff) {
			this.diff = diff;
		}
	}
	private class PrizeGeneImplDistanceOperation implements PrizeGeneMode{
		private static final String LOG_TAG = "PrizeGeneImplDistanceOperation";
		
		private static final long __LOW_LAST_DIFF_TIME_THRESHOLD_LOW = 10; // ms
		//private static final long __LOW_LAST_DIFF_TIME_THRESHOLD_HIGH = 210; // 这个值试用时,感觉有些短了
		private static final long __LOW_LAST_DIFF_TIME_THRESHOLD_HIGH = 500; 
				
		private SensorManager senMgr;
		private Sensor proxSensor;
		private boolean isProxListenerReg = false;
		private float __last_value = 0;
		private boolean first_flag = true;
		private long __last_time = 0;
		private long __cur_diff = 0;
		
			// non-arguments constructor
		public PrizeGeneImplDistanceOperation() {
			PrizeLogs.v(LOG_TAG, "PrizeGeneImplActionToUnlock new");
			
			senMgr = (SensorManager)mContext.getSystemService(Context.SENSOR_SERVICE);
			proxSensor = senMgr.getDefaultSensor(Sensor.TYPE_PROXIMITY);
			PrizeGeneHelper.checksNull(proxSensor, "default prox sensor");
			
			isProxListenerReg = false;		
		}
		@Override
		public void start() {
			// data initializing
			first_flag = true;
			
			// start listening
			if(senMgr == null) {
				return ;
			}
			if(proxSensor == null)
				return ;
			if(isProxListenerReg == true)
				return ;
			PrizeLogs.v(LOG_TAG, "start!");
			senMgr.registerListener(proxListener, proxSensor, SensorManager.SENSOR_DELAY_FASTEST);
			isProxListenerReg = true;
		}
		@Override
		public void stop() {
			if(senMgr == null)
				return ;
			if(proxSensor == null)
				return ;
			if(isProxListenerReg == false)
				return ;
			PrizeLogs.v(LOG_TAG, "stop!");
			senMgr.unregisterListener(proxListener, proxSensor);
			isProxListenerReg = false;
		}
		
		private boolean isBetween(long v, long min, long max) {
			if(v >= min && v <= max)
				return true;
			return false;
		}
		private SensorEventListener proxListener = new SensorEventListener() {
			@Override
			public void onAccuracyChanged(Sensor sensor, int accuracy) {
			}
			@Override
			public void onSensorChanged(SensorEvent event) {
				if(first_flag) {
					first_flag = false;
					__last_value = event.values[0];
					return ;
				}
				if(__last_value != event.values[0]) {
					if(__last_value != 0 && event.values[0] == 0) { // falling edge
						__last_time = event.timestamp;
					} else if(__last_value == 0 && event.values[0] != 0) { // raising edge
						__cur_diff = event.timestamp - __last_time;
						__cur_diff = __cur_diff/1000000;
						PrizeLogs.v(LOG_TAG, "__cur_diff = " + __cur_diff);
						// check it!
						if(isBetween(__cur_diff,
							__LOW_LAST_DIFF_TIME_THRESHOLD_LOW,
							__LOW_LAST_DIFF_TIME_THRESHOLD_HIGH)) {
							doActionProxy(DISTANCE_OPERATION_ACTION,
								new PrizeGeneImplDistanceOperationResult(__cur_diff));
						}
					}
					__last_value = event.values[0];
				}
			}
		};
	}
	
	/********************************************/
	/* ifrank,2015.06.05, 防误触模式     ANTIFAKE_TOUCH_MODE
	 * zhongweilin
	 * */
	public static class PrizeGeneImplAntifakeTouchResult {
		public boolean isAntifakeTouch;
		public PrizeGeneImplAntifakeTouchResult(boolean isAntifakeTouch) {
			this.isAntifakeTouch = isAntifakeTouch;
		}
	}
	private class PrizeGeneImplAntifakeTouch implements PrizeGeneMode{
		private static final String LOG_TAG = "PrizeGeneImplActionToUnlock";
		
		@SuppressWarnings("unused")
		private static final long __LOW_LAST_DIFF_TIME_THRESHOLD_LOW = 50; // ms
		//private static final long __LOW_LAST_DIFF_TIME_THRESHOLD_HIGH = 210; // 这个值试用时,感觉有些短了
		private static final long __LOW_LAST_DIFF_TIME_THRESHOLD_HIGH = 300; 
				
		private SensorManager senMgr;
		private Sensor proxSensor;
		private boolean isProxListenerReg = false;
		
			// non-arguments constructor
		public PrizeGeneImplAntifakeTouch() {
			PrizeLogs.v(LOG_TAG, "PrizeGeneImplActionToUnlock new");
			
			senMgr = (SensorManager)mContext.getSystemService(Context.SENSOR_SERVICE);
			proxSensor = senMgr.getDefaultSensor(Sensor.TYPE_PROXIMITY);
			PrizeGeneHelper.checksNull(proxSensor, "default prox sensor");
			
			isProxListenerReg = false;		
		}
		@Override
		public void start() {
			// start listening
			if(senMgr == null) {
				return ;
			}
			if(proxSensor == null)
				return ;
			if(isProxListenerReg == true)
				return ;
			PrizeLogs.v(LOG_TAG, "start!");
			senMgr.registerListener(proxListener, proxSensor, SensorManager.SENSOR_DELAY_FASTEST);
			isProxListenerReg = true;
		}
		@Override
		public void stop() {
			if(senMgr == null)
				return ;
			if(proxSensor == null)
				return ;
			if(isProxListenerReg == false)
				return ;
			PrizeLogs.v(LOG_TAG, "stop!");
			senMgr.unregisterListener(proxListener, proxSensor);
			isProxListenerReg = false;
		}
		
		private SensorEventListener proxListener = new SensorEventListener() {
			@Override
			public void onAccuracyChanged(Sensor sensor, int accuracy) {
			}
			@Override
			public void onSensorChanged(SensorEvent event) {
				Log.v("prize","--PX_sensor ------event.values[0] = " + event.values[0]);
				if(0 == event.values[0]) {
					doActionProxy(ANTIFAKE_TOUCH_MODE_ACTION,
						new PrizeGeneImplAntifakeTouchResult(true));
				}else{
					doActionProxy(ANTIFAKE_TOUCH_MODE_ACTION,
							new PrizeGeneImplAntifakeTouchResult(false));
				}
			}
		};
	}
	
}