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

package com.prize.appcenter.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.PowerManager;

import com.prize.appcenter.service.PrizeAppCenterService;
import com.prize.appcenter.ui.util.PollMgr;

/**
 * 类描述：锁屏和监听器
 *
 * @author huanglingjun
 * @version 版本
 */
public class ScreenListener {
    private Context mContext;
    private ScreenBroadcastReceiver mScreenReceiver;
    private ScreenStateListener mScreenStateListener;
    private static float currentLevel;
    public static boolean isScreenoff = false;

    public ScreenListener(Context context) {
        mContext = context;
        mScreenReceiver = new ScreenBroadcastReceiver();
        batteryLevel(mContext);
    }

//	private Handler mHandler = new Handler() {
//		public void handleMessage(android.os.Message msg) {
//			if (msg.what == 1) {
//				mHandler.removeMessages(1);
//				mHandler.sendEmptyMessageDelayed(2, 60 * 1000);
//				return;
//			}
//			if (currentLevel >= 30) {
//				if (isScreenoff) {
//					mScreenStateListener.onScreenOff();
//				}
//			} else {
//				if (isScreenoff) {
//					mScreenStateListener.onScreenOffNoRLLevel();
//				}
//			}
//		};
//	};

    /**
     * screen状态广播接收者
     */
    private class ScreenBroadcastReceiver extends BroadcastReceiver {
        private String action = null;

        @Override
        public void onReceive(Context context, Intent intent) {
            action = intent.getAction();
            if (Intent.ACTION_SCREEN_ON.equals(action)) { // 开屏
                isScreenoff = false;
                mScreenStateListener.onScreenOn();
                PollMgr.stopPollingService(context, PrizeAppCenterService.class);
            } else if (Intent.ACTION_SCREEN_OFF.equals(action)) {
                isScreenoff = true;
                PollMgr.startPollingService(context, PrizeAppCenterService.class);

            } else if (Intent.ACTION_USER_PRESENT.equals(action)) { // 解锁
                mScreenStateListener.onUserPresent();
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
                }
            }
        };
        IntentFilter batteryLevelFilter = new IntentFilter(
                Intent.ACTION_BATTERY_CHANGED);
        batteryLevelFilter.setPriority(1000);
        context.registerReceiver(batteryLevelReceiver, batteryLevelFilter);
    }

    /**
     * 开始监听screen状态
     *
     * @param listener ScreenStateListener
     */
    public void begin(ScreenStateListener listener) {
        mScreenStateListener = listener;
        registerListener();
        getScreenState();
    }

    /**
     * 获取screen状态
     */
    private void getScreenState() {
        PowerManager manager = (PowerManager) mContext
                .getSystemService(Context.POWER_SERVICE);
        if (manager.isScreenOn()) {
            if (mScreenStateListener != null) {
                mScreenStateListener.onScreenOn();
            }
        } else {
            if (mScreenStateListener != null) {
                if (currentLevel > 0.33) {
                    mScreenStateListener.onScreenOff();
                }
            }
        }
    }

    /**
     * 停止screen状态监听
     */
    public void unregisterListener() {
        mContext.unregisterReceiver(mScreenReceiver);
    }

    /**
     * 启动screen状态广播接收器
     */
    private void registerListener() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_SCREEN_ON);
        filter.addAction(Intent.ACTION_SCREEN_OFF);
        filter.addAction(Intent.ACTION_USER_PRESENT);
        filter.setPriority(1000);
        mContext.registerReceiver(mScreenReceiver, filter);
    }

    public interface ScreenStateListener {// 返回给调用者屏幕状态信息

        void onScreenOn();

        void onScreenOff();

        void onScreenOffNoRLLevel();

        void onUserPresent();
    }
}
