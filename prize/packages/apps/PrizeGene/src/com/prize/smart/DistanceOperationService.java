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

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.prize.smart.gene.PrizeGene;

import android.app.ActivityManager;
import android.app.ActivityManager.AppTask;
import android.app.ActivityManager.RecentTaskInfo;
import android.app.ActivityManager.RunningTaskInfo;
import android.app.KeyguardManager;
import android.app.KeyguardManager.KeyguardLock;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.input.InputManager;
import android.os.DeadObjectException;
import android.os.IBinder;
import android.os.RemoteException;
import android.os.SystemClock;
import android.os.UserHandle;
import android.provider.Settings;
import android.util.Log;
import android.app.Instrumentation;
import android.view.InputDevice;
import android.view.KeyCharacterMap;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.WindowManager;
import android.view.WindowManagerGlobal;
import android.widget.Toast;
import com.android.internal.widget.LockPatternUtils;

public class DistanceOperationService extends Service {
	private static final String TAG = "prize";
	
	private static final String ACTION_OPEN_DISTANCE_OPERATION = "com.android.prize.distanceoperation";
	private static final String KEY_EXTRA = "nontouch";
	private static final String PACKAGE_COCO_LAUNCHER = "com.cool.launcher";
	private static final String PACKAGE_COCO_UNILAUNCHER = "com.cooee.unilauncher";
	private static final String PACKAGE_LAUNCHER = "com.android.launcher";
	private static final String PACKAGE_LAUNCHER2 = "com.android.launcher2";
	private static final String PACKAGE_LAUNCHER3 = "com.android.launcher3";
	private static final String PACKAGE_GALLERY2 = "com.android.gallery3d";
	private static final String PACKAGE_VIDEO = "com.prize.videoc";
	private static final String PACKAGE_VIDEO_CLASS = "com.android.gallery3d.app.MovieActivity";
	private static final String PACKAGE_MUSIC = "com.prize.music";

	private static final int MIN_ACTION_TIME = 50;
	private static final int MAX_ACTION_TIME = 300;

	private PrizeGene prizefene = null;
	private static boolean isLockKeyguard = true;

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	/**
	 * 方法描述：亮屏/灭屏监听
	 * 
	 * @param 参数名
	 *            说明
	 * @return 返回类型 说明
	 * @see 类名/完整类名/完整类名#方法名
	 */
	private final BroadcastReceiver mDispalyInfoReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			final String action = intent.getAction();
			if (Intent.ACTION_SCREEN_ON.equals(action)) {
				// Log.d(TAG, "-----------------screen is on...");
				if(isDistanceOperationEnable()){
					Log.v(TAG, "----isDistanceOperationEnable----prizefene.start()...");
					prizefene.start();
				}
			} else if (Intent.ACTION_SCREEN_OFF.equals(action)) {
				// Log.v(TAG, "----------------- screen is off...");
				if(isDistanceOperationEnable()){
					Log.v(TAG, "----isDistanceOperationEnable----prizefene.stop()...");
					prizefene.stop();
				}
			} else if (ACTION_OPEN_DISTANCE_OPERATION.equals(action)){
				boolean opendo = intent.getBooleanExtra(KEY_EXTRA, false);
				if(opendo){
					Log.v(TAG, "-----DistanceOperationService is open  ...");
					prizefene.start();
				}else{
					Log.v(TAG, "--DistanceOperationService is close  ...");
					prizefene.stop();
				}
			}
		}
	};

	@Override
	public void onCreate() {
		Log.v(TAG, "--------DistanceOperationService-------1");
		final IntentFilter filter = new IntentFilter();
		filter.addAction(Intent.ACTION_SCREEN_OFF);
		filter.addAction(Intent.ACTION_SCREEN_ON);
		filter.addAction(ACTION_OPEN_DISTANCE_OPERATION);
		registerReceiver(mDispalyInfoReceiver, filter);

		prizefene = new PrizeGene(this, PrizeGene.DISTANCE_OPERATION) {

			@Override
			public void onAction(int action, Object params) {
				PrizeGene.PrizeGeneImplDistanceOperationResult distanceOperation = (PrizeGene.PrizeGeneImplDistanceOperationResult) params;
				Log.v(TAG, "-------------> distanceOperation.diff + " + distanceOperation.diff);
				if (distanceOperation.diff >= MIN_ACTION_TIME && distanceOperation.diff <= MAX_ACTION_TIME) {
					disableKeyguard();
				}
			}
		};
		if(isDistanceOperationEnable()){
			Log.v(TAG, "----isDistanceOperationEnable----prizefene.start()...");
			prizefene.start();
		}
		super.onCreate();
	}

	// START_STICKY:兼容模式service异常关闭后系统自动重启
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		// TODO Auto-generated method stub
		Log.v(TAG, "--------DistanceOperationService.onStartCommand...");
		return START_STICKY;
	}

	@Override
	public void onDestroy() {
		prizefene.stop();
		// TODO Auto-generated method stub
		super.onDestroy();
	}
	
	private boolean isDistanceOperationEnable(){
        int vales = 0;
        int nontouchunlock = Settings.System.getInt(getContentResolver(), Settings.System.PRIZE_NON_TOUCH_OPERATION_UNLOCK, 0);
        if(nontouchunlock != 0){
            vales++;
        }
        int nontouchGallery = Settings.System.getInt(getContentResolver(), Settings.System.PRIZE_NON_TOUCH_OPERATION_GALLERY, 0);
        if(nontouchGallery != 0){
            vales++;
        }

        int nontouchLauncher = Settings.System.getInt(getContentResolver(), Settings.System.PRIZE_NON_TOUCH_OPERATION_LAUNCHER, 0);
        if(nontouchLauncher != 0){
            vales++;
        }
        int nontouchvideo = Settings.System.getInt(getContentResolver(), Settings.System.PRIZE_NON_TOUCH_OPERATION_VIDEO, 0);
        if(nontouchvideo != 0){
            vales++;
        }
        int nontouchmusic = Settings.System.getInt(getContentResolver(), Settings.System.PRIZE_NON_TOUCH_OPERATION_MUSIC, 0);
        if(nontouchmusic != 0){
            vales++;
        }
        if(vales == 0){
            return false;
        }
        return true;
    }

    LockPatternUtils mLockPatternUtils = null;
	private void disableKeyguard() {
		KeyguardManager mKeyguardManager = (KeyguardManager) getSystemService(Context.KEYGUARD_SERVICE);
        if(mLockPatternUtils == null){
           mLockPatternUtils = new LockPatternUtils(this);
        }
		if (mKeyguardManager.isKeyguardLocked()) {
			int nontouchUnlock = Settings.System
					.getInt(getContentResolver(), Settings.System.PRIZE_NON_TOUCH_OPERATION_UNLOCK, 0);
			if (nontouchUnlock == 1) {
				Log.d(TAG, "need to disableKeyguard");
				try {
                    if(!mLockPatternUtils.isSecure(UserHandle.USER_ALL)){
					    WindowManagerGlobal.getWindowManagerService().dismissKeyguard();
                    }else{
                        Log.d(TAG, "--->This is Secure Keyguard !!!!");
                      // Toast.makeText(this, getResources().getString(R.string.unlock_fail), Toast.LENGTH_LONG).show();
                    }
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		} else {
			sendKeyCode();
		}
	}

	/**
	 * 方法描述：发送模拟按键
	 * 
	 * @param 参数名
	 *            说明
	 * @return 返回类型 说明
	 * @see 类名/完整类名/完整类名#方法名
	 */
	private void sendKeyCode() {
		new Thread() {
			public void run() {
				try {
					String[] packageNametemp = getActivePackagesCompat();
					if (packageNametemp == null || packageNametemp.length != 2) {
						return;
					}
					String packageName = packageNametemp[0];
					String className = packageNametemp[1];
					if (packageName == null || className == null) {
						return;
					}
					Log.e(TAG, "***packageName == " + packageName + " *****");
					if (packageName.equals(PACKAGE_COCO_UNILAUNCHER) || packageName.equals(PACKAGE_COCO_LAUNCHER)
							|| packageName.equals(PACKAGE_LAUNCHER) || packageName.equals(PACKAGE_LAUNCHER2)
							|| packageName.equals(PACKAGE_LAUNCHER3)) {
						int nontouchLauncher = Settings.System.getInt(getContentResolver(),
								Settings.System.PRIZE_NON_TOUCH_OPERATION_LAUNCHER, 0);
						if (nontouchLauncher == 1) {
							sendMotionEvent(true);
							Log.e(TAG, "***Launcher***sendMotionEven-----");
						}
					} else if (packageName.equals(PACKAGE_GALLERY2) && !className.equals(PACKAGE_VIDEO_CLASS)) {
						int nontouchGallery = Settings.System.getInt(getContentResolver(),
								Settings.System.PRIZE_NON_TOUCH_OPERATION_GALLERY, 0);
						if (nontouchGallery == 1) {
							sendMotionEvent(true);
							Log.e(TAG, "***Gallery***sendMotionEven-----");
						}
					} else if (packageName.equals(PACKAGE_VIDEO)) {
						int nontouchvideo = Settings.System.getInt(getContentResolver(),
								Settings.System.PRIZE_NON_TOUCH_OPERATION_VIDEO, 0);
						if (nontouchvideo == 1) {
							sendMotionEvent(true);
							Log.e(TAG, "***video***sendMotionEven-----");
						}
					} else if (packageName.equals(PACKAGE_MUSIC)) {
						int nontouchmusic = Settings.System.getInt(getContentResolver(),
								Settings.System.PRIZE_NON_TOUCH_OPERATION_MUSIC, 0);
						if (nontouchmusic == 1) {
							//sendKeyEvent(InputDevice.SOURCE_KEYBOARD, KeyEvent.KEYCODE_DPAD_RIGHT, false);
							sendKeyEvent(KeyEvent.KEYCODE_DPAD_RIGHT);//left:上一首歌；  right：下一首歌
							//sendMotionEvent(true);
							Log.e(TAG, "***music***sendKeyCode-----");
						}
					} else if (className.equals(PACKAGE_VIDEO_CLASS)) {
						int nontouchvideo = Settings.System.getInt(getContentResolver(),
								Settings.System.PRIZE_NON_TOUCH_OPERATION_VIDEO, 0);
						if (nontouchvideo == 1) {
							sendKeyEvent(KeyEvent.KEYCODE_DPAD_RIGHT); //left:上一个视频；  right：下一个视频
							Log.e(TAG, "***videoPaly***sendMotionEven-----");
						}
					}
				} catch (Exception e) {
					Log.e("Exception when sendPointerSync", e.toString());
				}
			}
		}.start();
	}

	/**
	 * 方法描述：模拟滑动
	 * 
	 * @param 参数名
	 *            说明 isTurnRight:true 从右往左滑动；false 从左往右滑动
	 * @return 返回类型 说明
	 * @see 类名/完整类名/完整类名#方法名
	 */
	private void sendMotionEvent(boolean isTurnRight) {
		Instrumentation inst = new Instrumentation();
		if (isTurnRight) {
			MotionEvent e = MotionEvent.obtain(SystemClock.uptimeMillis(), SystemClock.uptimeMillis(), MotionEvent.ACTION_DOWN,
					400, 450, 0);
			inst.sendPointerSync(e);
			e = MotionEvent.obtain(SystemClock.uptimeMillis(), SystemClock.uptimeMillis(), MotionEvent.ACTION_MOVE, 400, 450, 0);
			inst.sendPointerSync(e);
			e = MotionEvent.obtain(SystemClock.uptimeMillis(), SystemClock.uptimeMillis(), MotionEvent.ACTION_MOVE, 350, 450, 0);
			inst.sendPointerSync(e);
			e = MotionEvent.obtain(SystemClock.uptimeMillis(), SystemClock.uptimeMillis(), MotionEvent.ACTION_MOVE, 300, 450, 0);
			inst.sendPointerSync(e);
			e = MotionEvent.obtain(SystemClock.uptimeMillis(), SystemClock.uptimeMillis(), MotionEvent.ACTION_MOVE, 250, 450, 0);
			inst.sendPointerSync(e);
			e = MotionEvent.obtain(SystemClock.uptimeMillis(), SystemClock.uptimeMillis(), MotionEvent.ACTION_MOVE, 200, 450, 0);
			inst.sendPointerSync(e);
			e = MotionEvent.obtain(SystemClock.uptimeMillis(), SystemClock.uptimeMillis() + 300, MotionEvent.ACTION_UP, 200, 450,
					0);
			inst.sendPointerSync(e);
		} else {
			MotionEvent e = MotionEvent.obtain(SystemClock.uptimeMillis(), SystemClock.uptimeMillis(), MotionEvent.ACTION_DOWN,
					200, 450, 0);
			inst.sendPointerSync(e);
			e = MotionEvent.obtain(SystemClock.uptimeMillis(), SystemClock.uptimeMillis(), MotionEvent.ACTION_MOVE, 200, 450, 0);
			inst.sendPointerSync(e);
			e = MotionEvent.obtain(SystemClock.uptimeMillis(), SystemClock.uptimeMillis(), MotionEvent.ACTION_MOVE, 250, 450, 0);
			inst.sendPointerSync(e);
			e = MotionEvent.obtain(SystemClock.uptimeMillis(), SystemClock.uptimeMillis(), MotionEvent.ACTION_MOVE, 300, 450, 0);
			inst.sendPointerSync(e);
			e = MotionEvent.obtain(SystemClock.uptimeMillis(), SystemClock.uptimeMillis(), MotionEvent.ACTION_MOVE, 350, 450, 0);
			inst.sendPointerSync(e);
			e = MotionEvent.obtain(SystemClock.uptimeMillis(), SystemClock.uptimeMillis(), MotionEvent.ACTION_MOVE, 400, 450, 0);
			inst.sendPointerSync(e);
			e = MotionEvent.obtain(SystemClock.uptimeMillis(), SystemClock.uptimeMillis() + 300, MotionEvent.ACTION_UP, 400, 450,
					0);
			inst.sendPointerSync(e);
		}
	}

	/**
	 * 方法描述：版本获取类名
	 * 
	 * @param 参数名
	 *            说明
	 * @return 返回类型 说明
	 * @see 类名/完整类名/完整类名#方法名
	 */
	private String[] getActivePackagesCompat() {
		ActivityManager mActivityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
		List<ActivityManager.RunningTaskInfo> taskInfo = mActivityManager.getRunningTasks(1);
		ComponentName componentName = taskInfo.get(0).topActivity;
		String[] activePackages = new String[2];
		activePackages[0] = componentName.getPackageName();
		activePackages[1] = componentName.getClassName();
		return activePackages;
	}

	/**
	 * 方法描述：Android应用是否在前台
	 * 
	 * @param 参数名
	 *            说明
	 * @return 返回类型 说明
	 * @see 类名/完整类名/完整类名#方法名
	 */
	public String[] getAppOnForeground() {
		ActivityManager mActivityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
		List<ActivityManager.AppTask> appTask = mActivityManager.getAppTasks();
		if (appTask == null) {
			return null;
		}
		RecentTaskInfo recentTask = appTask.get(0).getTaskInfo();
		String[] activePackages = new String[1];
		activePackages[0] = recentTask.baseIntent.toString();
		return activePackages;
	}

	/**
	 * 方法描述：获取当前所以后台进程
	 * 
	 * @param 参数名
	 *            说明
	 * @return 返回类型 说明
	 * @see 类名/完整类名/完整类名#方法名
	 */
	private String[] getActivePackages() {
		ActivityManager mActivityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
		final Set<String> activePackages = new HashSet<String>();
		final List<ActivityManager.RunningAppProcessInfo> processInfos = mActivityManager.getRunningAppProcesses();
		for (ActivityManager.RunningAppProcessInfo processInfo : processInfos) {
			if (processInfo.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
				activePackages.addAll(Arrays.asList(processInfo.pkgList));
			}
		}
		return activePackages.toArray(new String[activePackages.size()]);
	}
	
	/**
	 * 方法描述：发送模拟按键
	 * @param 参数名
	 *            说明
	 * @return 返回类型 说明
	 * @see 类名/完整类名/完整类名#方法名
	 */
	private void sendKeyEvent(int KeyCode){
		Instrumentation inst = new Instrumentation();
		inst.sendKeyDownUpSync(KeyCode);
	}

	/**
	 * 方法描述：模拟按键过程
	 * 
	 * @param 参数名
	 *            说明
	 * @return 返回类型 说明
	 * @see 类名/完整类名/完整类名#方法名
	 */
	private void sendKeyEvent(int inputSource, int keyCode, boolean longpress) {
		long now = SystemClock.uptimeMillis();
		injectKeyEvent(new KeyEvent(now, now, KeyEvent.ACTION_DOWN, keyCode, 0, 0, KeyCharacterMap.VIRTUAL_KEYBOARD, 0, 0,
				inputSource));
		if (longpress) {
			injectKeyEvent(new KeyEvent(now, now, KeyEvent.ACTION_DOWN, keyCode, 1, 0, KeyCharacterMap.VIRTUAL_KEYBOARD, 0,
					KeyEvent.FLAG_LONG_PRESS, inputSource));
		}
		injectKeyEvent(new KeyEvent(now, now, KeyEvent.ACTION_UP, keyCode, 0, 0, KeyCharacterMap.VIRTUAL_KEYBOARD, 0, 0,
				inputSource));
	}

	/**
	 * 方法描述：发送模拟按键信号
	 * 
	 * @param 参数名
	 *            说明
	 * @return 返回类型 说明
	 * @see 类名/完整类名/完整类名#方法名
	 */
	private void injectKeyEvent(KeyEvent event) {
		// Log.i(TAG, "injectKeyEvent: " + event);
		InputManager.getInstance().injectInputEvent(event, InputManager.INJECT_INPUT_EVENT_MODE_WAIT_FOR_FINISH);
	}

}
