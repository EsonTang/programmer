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

package com.prize.prizeappoutad.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import com.prize.prizeappoutad.utils.JLog;

/**
 * 
 * @author huangchangguo 2016.11.9
 * 
 */
public class ScreenListener {
	private Context mContext;
	private ScreenBroadcastReceiver mScreenReceiver;
	private ScreenStateListener mScreenStateListener;
	private static float currentLevel = -1;
	private static String TAG = "huang-ScreenListener";

	public ScreenListener(Context context) {
		mContext = context;
		mScreenReceiver = new ScreenBroadcastReceiver();
		batteryLevel(mContext);
	}

	/**
	 * screen状态广播接收者
	 */
	private class ScreenBroadcastReceiver extends BroadcastReceiver {
		private String action = null;

		@Override
		public void onReceive(Context context, Intent intent) {
			action = intent.getAction();
			JLog.i(TAG, "action=" + action.toString());
			if (Intent.ACTION_SCREEN_ON.equals(action)) { // 开屏

				mScreenStateListener.onScreenOn();

			} else if (Intent.ACTION_SCREEN_OFF.equals(action)) {

				mScreenStateListener.onScreenOff();
			} else if (Intent.ACTION_USER_PRESENT.equals(action)) { // 解锁

				if (currentLevel >= 10) {
					mScreenStateListener.onUserPresent();
				} else {
					mScreenStateListener.onScreenOffNoRLLevel();
				}
			}
		}
	}

	/**
	 * 方法描述：注册电量监听广播
	 */
	private void batteryLevel(Context context) {
		BroadcastReceiver batteryLevelReceiver = new BroadcastReceiver() {
			public void onReceive(Context context, Intent intent) {
				int rawlevel = intent.getIntExtra("level", -1);// 获得当前电量
				int scale = intent.getIntExtra("scale", -1);

				// 获得总电量
				// int level = -1;
				if (rawlevel >= 0 && scale > 0) {
					currentLevel = (rawlevel * 100) / scale;
					// currentLevel==100
				}
			}
		};
		IntentFilter batteryLevelFilter = new IntentFilter(
				Intent.ACTION_BATTERY_CHANGED);
		batteryLevelFilter.setPriority(IntentFilter.SYSTEM_HIGH_PRIORITY);
		context.registerReceiver(batteryLevelReceiver, batteryLevelFilter);
	}

	/**
	 * 开始监听screen状态
	 */
	public void begin(ScreenStateListener listener) {
		mScreenStateListener = listener;
		registerListener();
		// getScreenState();
	}

	// /**
	// * 获取screen状态
	// */
	// private void getScreenState() {
	// PowerManager manager = (PowerManager) mContext
	// .getSystemService(Context.POWER_SERVICE);
	// if (manager.isScreenOn()) {
	// if (mScreenStateListener != null) {
	// mScreenStateListener.onScreenOn();
	// }
	// } else {
	// if (mScreenStateListener != null) {
	// if (currentLevel > 0.22) {
	// mScreenStateListener.onScreenOff();
	// }
	// }
	// }
	// }

	/**
	 * 停止screen状态监听
	 */
	public void unregisterListener() {
		if (mContext != null) {
			mContext.unregisterReceiver(mScreenReceiver);
		}
	}

	/**
	 * 启动screen状态广播接收器
	 */
	private void registerListener() {
		if (mContext != null) {
			IntentFilter filter = new IntentFilter();
			filter.addAction(Intent.ACTION_SCREEN_ON);
			filter.addAction(Intent.ACTION_SCREEN_OFF);
			filter.addAction(Intent.ACTION_USER_PRESENT);
			filter.setPriority(IntentFilter.SYSTEM_HIGH_PRIORITY);
			mContext.registerReceiver(mScreenReceiver, filter);
		}
	}

	public interface ScreenStateListener {
		public void onScreenOn();

		public void onScreenOff();

		public void onScreenOffNoRLLevel();

		public void onUserPresent();
	}
}
