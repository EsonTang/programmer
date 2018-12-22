package com.prize.lockscreen.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.prize.lockscreen.application.LockScreenApplication;
import com.prize.lockscreen.service.LockScreenService;
import com.prize.lockscreen.utils.LogUtil;
import com.prize.lockscreen.utils.SharedPreferencesTool;
/***
 * 开关屏广播接收器
 * @author fanjunchen
 *
 */
public class ScreenOnReceiver extends BroadcastReceiver {
	
	private final static String ACTION_SCREEN_ON = "android.intent.action.SCREEN_ON";
	private final static String ACTION_SCREEN_OFF = "android.intent.action.SCREEN_OFF";
	
	private final static String ACTION_USER_PRESENT = "android.intent.action.USER_PRESENT";
	
	public final static String EMC_CLOSE_BRD = "emergency_dial_has_closed";
	
	private static LockScreenService mService;
	/**记数器*/
	private int count = 0;
	/**每N次启动一次服务, 以免服务停止*/
	private final int MAX_INTERVL = 15;
	/**是否已经启动过服务*/
	private boolean isStart = false;
	/***
	 * 设置服务对象
	 * @param sv
	 */
	public void setService(LockScreenService sv) {
		mService = sv;
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		String action = intent.getAction();
		
		if (ACTION_SCREEN_ON.equals(action) || ACTION_SCREEN_OFF.equals(action) 
				|| ACTION_USER_PRESENT.equals(action)
				|| EMC_CLOSE_BRD.equals(action)) {
			count ++;
			LogUtil.i("ScreenOnReceiver", "===>onReceive()" + action);
			if (!isStart || count % MAX_INTERVL == 0) {
				Intent itService = new Intent(context, LockScreenService.class);
				itService.putExtra(LockScreenService.P_START_NORMAL, 1);
	        	context.startService(itService);
	        	itService = null;
	        	isStart = true;
			}
//			KeyguardManager keyguardManager = (KeyguardManager)context.getSystemService(Context.KEYGUARD_SERVICE);
//			KeyguardLock keyguardLock = keyguardManager.newKeyguardLock("my_lockscreen");
//			keyguardLock.disableKeyguard();
			if (ACTION_SCREEN_ON.equals(action) || ACTION_USER_PRESENT.equals(action)) {
				/*KeyguardManager keyguardManager = (KeyguardManager)context.getSystemService(Context.KEYGUARD_SERVICE);
				KeyguardLock keyguardLock = keyguardManager.newKeyguardLock("my_lockscreen");
				keyguardLock.disableKeyguard();*/
				//keyguardLock.reenableKeyguard();
			} 
			else if (SharedPreferencesTool.isLockScreenEnable(context)) {
				LockScreenApplication.playSound(1);
			}
			
			if (ACTION_SCREEN_OFF.equals(action) || ACTION_USER_PRESENT.equals(action)
					|| EMC_CLOSE_BRD.equals(action) || count == 1) {
				/*Intent it = new Intent();
				it.setClassName("com.prize.prizelockscreen", "com.prize.lockscreen.ShowWhenLock");
				it.addCategory(Intent.CATEGORY_DEFAULT);
				it.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				context.startActivity(it);*/
				if (mService != null)
					mService.dealView();
				else {
					LogUtil.i("ScreenOnReceiver", "===>mService is null.");
					Intent itService = new Intent(context, LockScreenService.class);
					itService.putExtra(LockScreenService.P_START_NORMAL, 99);
		        	context.startService(itService);
		        	itService = null;
				}
			}
		}
	}
}
