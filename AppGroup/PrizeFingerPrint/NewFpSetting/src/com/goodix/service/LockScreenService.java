package com.goodix.service;

import java.util.ArrayList;
import java.util.List;

import android.app.ActivityManager;
import android.app.ActivityManagerNative;
import android.app.IProcessObserver;
import android.app.Service;
import android.app.ActivityManager.RunningTaskInfo;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.PowerManager;
import android.os.RemoteException;
import android.os.SystemProperties;
import android.util.Log;
import android.os.Vibrator;

import com.goodix.aidl.IVerifyCallback;
import com.goodix.application.FpApplication;
import com.goodix.application.FpApplication.ServiceConnectCallback;
import com.goodix.applock.AppLockUtils;
import com.goodix.device.MessageType;
import com.goodix.service.FingerprintManager.VerifySession;

public class LockScreenService extends Service  implements ServiceConnectCallback{
	private RelativeBroadcastReceiver receiver;
	PowerManager mPmGer ;

	private static final String TAG = "LockScreenService";
	private static final int MSG_VERIFY_SUCCESS = 1;
	private static final int MSG_VERIFY_FAILED = 2;
	private static final int MSG_VERIFY_NO_ENROLL = 3;
	private static VerifySession mVerifySession = null;
	private ActivityManager mActivityManager;
	private Context mContext;

	private AppLockUtils mAppLockUtil;
	private  static Vibrator vibrator ;

	public static final int SHOW_PASSWORD_ACTIVITY_DELAY = 200;

	private MyHandler mHandler = null;

	private Handler mForeHandler = new Handler();

	private String[] mForegroundPkgs;


	private FpApplication mApplication;
	private FingerprintManager mManager;
	private static boolean mNoNeedShock = false;

	private final static String UNLOCK_SUCCESS = "com.prize.broadcast.unlock_success";
	private final static String UNLOCK_FAIL = "com.prize.broadcast.unlock_failed";
	@Override
	public void onCreate() {
		super.onCreate();
		mContext = getApplicationContext();
		mAppLockUtil = AppLockUtils.getInstance(mContext);
		mPmGer = (PowerManager)getSystemService(Context.POWER_SERVICE); 
		vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		initAppLock();
		flags = START_STICKY;  
		return super.onStartCommand(intent, flags, startId); 
	}


	public void initAppLock(){
		mActivityManager = (ActivityManager)getSystemService(Context.ACTIVITY_SERVICE);
		try {
			ActivityManagerNative.getDefault().registerProcessObserver(new IProcessObserver.Stub() {
				@Override
				public void onForegroundActivitiesChanged(int pid, int uid, boolean foregroundActivities) {
					Log.i("AppLockManagerService", "onForegroundActivitiesChanged : foregroundActivities= " + foregroundActivities);
					//					if (foregroundActivities) {
					if (SystemProperties.get("persist.sys.prize_fp_enable").equals("1")) {
						PackageManager pm = getPackageManager();
						mForegroundPkgs = pm.getPackagesForUid(uid);
						mForeHandler.removeCallbacks(mShowPwdRunnable);
						mForeHandler.postDelayed(mShowPwdRunnable, SHOW_PASSWORD_ACTIVITY_DELAY);
					}
				}

				@Override
				public void onProcessStateChanged(int pid, int uid, int importance) {
				}

				@Override
				public void onProcessDied(int pid, int uid) {
				}
			});
		} catch (RemoteException e) {
		}	

		IntentFilter filter = new IntentFilter();
		filter.addAction(Intent.ACTION_SCREEN_ON);
		filter.addAction(Intent.ACTION_SCREEN_OFF);
		filter.addAction(Intent.ACTION_USER_PRESENT);
		receiver = new RelativeBroadcastReceiver();
		filter.setPriority(1020);
		registerReceiver(receiver, filter);

		Log.d("LockScreenService", "getFpServiceManager() start:" + System.currentTimeMillis());

		mApplication = FpApplication.getInstance();
		if(mApplication.isFpServiceManagerEmpty()){
			mApplication.setCallback(this);
			mManager = mApplication.getFpServiceManager();
		}else{
			mManager = mApplication.getFpServiceManager();
			if (null == mVerifySession) {
				mVerifySession = mManager.newVerifySession(mVerifyCallback);
			}
			mVerifySession.enter();
		}
	}

	private Runnable mShowPwdRunnable = new Runnable() {
		public void run() {
			String runningTopPackage = mActivityManager.getRunningTasks(1).get(0).topActivity.getPackageName();  
			String runningTopClass = mActivityManager.getRunningTasks(1).get(0).topActivity.getClassName();  
			Log.i("AppLockManagerService", "runningTopPackage=" + runningTopPackage);
			Log.i("AppLockManagerService", "runningTopClass=" + runningTopClass);
			setAlreadyLockToUnlock();

			if (isAppNeedLock(runningTopPackage)) {
				Intent intent =  new Intent("android.intent.action.APP_LOCK"); 
				intent.putExtra("Already_Locked_App_Pkg", runningTopPackage);
				intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				startActivity(intent);  
			}
		}
	};

	private boolean isAppNeedLock(String pkg) {
		boolean needLock = mAppLockUtil.isAppNeedLock(pkg);
		boolean alreadyUnlocked = mAppLockUtil.isAppAlreadyUnlocked(pkg);
		return needLock && !alreadyUnlocked;
	}

	private List<String> getHomes() {  
		List<String> names = new ArrayList<String>();  
		PackageManager packageManager = this.getPackageManager();  
		Intent intent = new Intent(Intent.ACTION_MAIN);  
		intent.addCategory(Intent.CATEGORY_HOME);  
		List<ResolveInfo> resolveInfo = packageManager.queryIntentActivities(intent,  
				PackageManager.MATCH_DEFAULT_ONLY);  
		for(ResolveInfo ri : resolveInfo){  
			names.add(ri.activityInfo.packageName);  
		}  
		return names;  
	}  

	public boolean isHome(List<String> homePackageNames){  
		ActivityManager mActivityManager = (ActivityManager)getSystemService(Context.ACTIVITY_SERVICE);  
		List<RunningTaskInfo> rti = mActivityManager.getRunningTasks(1);  
		return homePackageNames.contains(rti.get(0).topActivity.getPackageName());  
	}  

	private void setAlreadyLockToUnlock(){
		if (isHome(getHomes())) {
			AppLockUtils utils = AppLockUtils.getInstance(mContext);
			Cursor cursor = utils.getAllLockedApp();
			while(null != cursor && cursor.moveToNext()){
				String pkg = cursor.getString(cursor.getColumnIndex(AppLockUtils.PKG_NAME));
				utils.setAppAlreadyLocked(pkg);
			}
			if(null != cursor){
				cursor.close();
			}
		}
	}

	@Override
	public void onDestroy() {
		unregisterReceiver(receiver);
		Log.d(TAG,"LockScreenService.onDestroy");
		Intent sevice = new Intent(this, LockScreenService.class);  
		startService(sevice);
		super.onDestroy();
	}

	@Override
	public IBinder onBind(Intent arg0) {
		return null;
	}

	private static class MyHandler extends Handler {
		private Context mContext;

		public MyHandler(Context context) {
			mContext = context;
		}

		public void handleMessage(Message msg) {
			if (mContext == null)
				return;

			switch (msg.what) {
			case MSG_VERIFY_SUCCESS:
				if (msg.arg2 > 0) {
					if(null != mVerifySession){
						mVerifySession.exit();
						mVerifySession = null;
					}
					if(!mNoNeedShock){
						vibrateFeedback();
					}
					Intent intent = new Intent();
					intent.setAction(UNLOCK_SUCCESS);
					mContext.sendBroadcast(intent);
				}
				break;
			case MSG_VERIFY_FAILED:
				if(!mNoNeedShock){
					vibrateFeedback();
				}
				Intent intent = new Intent();
				intent.setAction(UNLOCK_FAIL);
				mContext.sendBroadcast(intent);
				break;
			case MSG_VERIFY_NO_ENROLL:

				break;
			default:
				break;
			}
		}
	}

	private static void vibrateFeedback(){
		vibrator.vibrate(new long[] { 50, 100 }, -1);
	}

	private IVerifyCallback mVerifyCallback = new IVerifyCallback.Stub() {
		@Override
		public boolean handleMessage(int msg, int arg0, int arg1, byte[] data)
				throws RemoteException {
			Log.d("FpSetting", "LockScreenService IVerifyCallback msg = " + MessageType.getString(msg));
			if (msg == MessageType.MSG_TYPE_RECONGNIZE_SUCCESS) {
				mHandler.sendMessage(mHandler.obtainMessage(MSG_VERIFY_SUCCESS,msg, arg0, arg1));
			} else if (msg == MessageType.MSG_TYPE_RECONGNIZE_FAILED) {
				mHandler.sendMessage(mHandler.obtainMessage(MSG_VERIFY_FAILED,msg, arg0, arg1));
			} else if (msg == MessageType.MSG_TYPE_RECONGNIZE_NO_REGISTER_DATA) {
				mHandler.sendMessage(mHandler.obtainMessage(MSG_VERIFY_NO_ENROLL, msg, arg0, arg1));
			}
			return false;
		}
	};


	private class RelativeBroadcastReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			Log.d(TAG,"onReceive.Intent.strAction == " + intent.getAction());
			if (intent.getAction().equals(Intent.ACTION_SCREEN_ON)
					||intent.getAction().equals(Intent.ACTION_SCREEN_OFF)
					) {

				if(mApplication.isFpServiceManagerEmpty()){
					mApplication.setCallback(LockScreenService.this);
					mManager = mApplication.getFpServiceManager();
				}else{
					mManager = mApplication.getFpServiceManager();
					if (null == mVerifySession) {
						mVerifySession = mManager.newVerifySession(mVerifyCallback);
					}
					mVerifySession.enter();
				}
			} 
			if(intent.getAction().equals(Intent.ACTION_USER_PRESENT)){
				mNoNeedShock = true;
			}else if(intent.getAction().equals(Intent.ACTION_SCREEN_OFF)){
				mNoNeedShock = false;
			}

		}
	}

	@Override
	public void serviceConnect() {
		mManager = FpApplication.getInstance().getFpServiceManager();
		if(null == mVerifySession){
			mVerifySession = mManager.newVerifySession(mVerifyCallback);
		}
		Log.d(TAG, "LockScreenService initAppLock() newVerifySession success");
		mVerifySession.enter();
		if (null == mHandler) {
			mHandler = new MyHandler(getApplicationContext());
		}
	}
}
