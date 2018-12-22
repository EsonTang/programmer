package com.prize.prizesecurity;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.pm.PackageStats;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.SystemClock;
import android.os.UserHandle;
import android.provider.Settings;
import android.provider.Settings.SettingNotFoundException;
import android.text.format.Formatter;
import android.util.Log;
import android.content.pm.IPackageDataObserver;
import android.content.pm.IPackageStatsObserver;
/*-prize add by lihuangyuan,for whitelist -2017-11-25-start-*/
import android.os.WhiteListManager;
/*-prize add by lihuangyuan,for whitelist -2017-11-25-end-*/
public class ClearBackgroundService extends Service
{
	public static final boolean DEBUG = false;
	public static final String TAG = "PureBackground";

	@Override
	public IBinder onBind(Intent intent)
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void onCreate()
	{
		Log.d(TAG, "onCreate+++++++++");
		Thread.UncaughtExceptionHandler handler = new Thread.UncaughtExceptionHandler()
		{
			@Override
			public void uncaughtException(Thread thread, Throwable ex)
			{
				Log.e(TAG, "ex:" + ex.toString());
				ex.printStackTrace();
				// restart the child thread
				initThread();
			}
		};
		Thread.setDefaultUncaughtExceptionHandler(handler);
		init();
		super.onCreate();
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId)
	{
		// TODO Auto-generated method stub
		return super.onStartCommand(intent, flags, startId);
	}

	@Override
	public void onDestroy()
	{
		release();
		super.onDestroy();
	}
	private void initThread()
	{
		if (mWorkerThread != null)
		{
			Log.d(TAG, "thread looper = " + mWorkerThread.getLooper() + ",id:" + mWorkerThread.getThreadId());
			Log.d(TAG, "thread state:" + mWorkerThread.getState() + " isalive:" + mWorkerThread.isAlive());
			mWorkerThread.quit();
		}
		mWorkerThread = new HandlerThread("ClearBkService");
		mWorkerThread.start();
		mHandler = new WorkHandler(mWorkerThread.getLooper());
		mHandler.sendEmptyMessageDelayed(MSG_START_INIT, 1000);

		// set error time
		//mHandler.sendEmptyMessageDelayed(MSG_START_ERROR, 3 * 1000);
	}

	private void init()
	{
		mContext = this;

		initThread();

		mPm = getPackageManager();
		mActivityMgr = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
		// Network connectivity receiver
		IntentFilter MyFilter = new IntentFilter();
		MyFilter.addAction(Intent.ACTION_SCREEN_ON);
		MyFilter.addAction(Intent.ACTION_SCREEN_OFF);
		MyFilter.setPriority(IntentFilter.SYSTEM_HIGH_PRIORITY);//set the priority high
		registerReceiver(mBroadcastReceiver, MyFilter);

	}

	private void release()
	{
		mWorkerThread.quit();
		unregisterReceiver(mBroadcastReceiver);
	}
	//////////////////////////////////////////////////////////////////////////////

	private static final String PURE_BG_DISABLE_APP_LIST = "pureBgDisableAppList";
	private static final String PURE_BG_ENABLE_APP_LIST = "pureBgEnableAppList";
	public static final String PURE_BG_STATUS_OPEN_VALUE = "pureBgStatusOpenValue";

	private HandlerThread mWorkerThread = null;
	private WorkHandler mHandler = null;

	private PackageManager mPm;
	private ActivityManager mActivityMgr;
	private ClearCacheObserver mClearCacheObserver;
	private PackageStatsObserver mPackageStatsObserver;
	private Context mContext;

	private boolean mIsScreenOff = false;
	private long mScreenOffTime = 0;

	public static final int KILL_STATE_THIRD_MARKET = 1;
	public static final int KILL_STATE_DISABLE_FIRST = 2;
	public static final int KILL_STATE_DISABLE_SECOND = 3;
	public static final int KILL_STATE_EVERY_15MIN = 4;
	private int mKillState = 0;

	// third market
	private static String[] sThirdMarketAry = new String[]
	{
			"com.tencent.android.qqdownloader", // yingyongbao
			"com.baidu.appsearch",// baidu
			"com.dragon.android.pandaspace",// new baidu
			"com.qihoo.appstore",// 360
			"com.sogou.androidtool",// sougou
			"com.wandoujia.phoenix2",// wandoujia
			"com.pp.assistant",// pp
			"com.hiapk.marketpho",// android market
			"com.ekesoo.font",// font
			"com.nd.assistance",// 91lianjiezhusou
			"com.oem91.market",// 91zhoushou
	};
	// special app not kill
	public static final String[] purebackground_notkillapplist =
	{
			// android
			"com.android.dialer",// bohao
			"com.android.phone",// phone
			"com.android.mms", //mmd
			"com.android.deskclock", //deskclock
			"com.android.launcher", ////launcher
			"com.android.purebackground",//purebackground
			"com.android.prize", //
			"com.android.bluetooth",//bluetooth
			"com.android.defcontainer",//

			// core app
			"com.android.systemui", //
			"android.process.media",//
			"android.process.acore",//
			"android.ext.services",//

			"system",//
			"com.mediatek",//
	};	

	// ////////////////////////////////////////////////////////////////////
	public static boolean isThirdMarket(String pkg)
	{
		if (pkg == null) return false;

		for (int i = 0; i < sThirdMarketAry.length; i++)
		{
			if (pkg.equals(sThirdMarketAry[i])) { return true; }
		}
		return false;
	}

	/*
	 * return true is must filter package
	 */
	public boolean skipKillPackage(String processName)
	{
		if (processName == null) { return false; }
		for (int i = 0; i < purebackground_notkillapplist.length; i++)
		{
			if (processName.contains(purebackground_notkillapplist[i])) return true;
		}
		for(int i=0;i<mDefInputMethodList.size();i++)
		{
			if (processName.contains(mDefInputMethodList.get(i))) return true;
		}
		return false;
	}
       /*-prize add by lihuangyuan,for whitelist -2017-11-25-start-*/
	public static boolean isInList(String pkgname,String []arylist)
	{
	   	if(arylist == null)return false;
		for(int i=0;i<arylist.length;i++)
		{
			if(pkgname.equals(arylist[i]))
			{
				return true;
			}
		}
		return false;
	}
	/*-prize add by lihuangyuan,for whitelist -2017-11-25-end-*/
	public void getDefInputMethod()
	{
		mDefInputMethodList.clear();
		try
		{
			String defaultInputMethodCls = Settings.Secure.getString(mContext.getContentResolver(), Settings.Secure.DEFAULT_INPUT_METHOD);

			if (defaultInputMethodCls != null && defaultInputMethodCls.length() > 0)
			{
				defaultInputMethodCls = defaultInputMethodCls.split("/")[0];
				mDefInputMethodList.add(defaultInputMethodCls);
			}
		}
		catch (Exception e)
		{

		}

	}	
	private ArrayList<String> mDefInputMethodList = new ArrayList<String>();
	private static ArrayList<String> mKillAppList = new ArrayList<String>();
	private static ArrayList<String> mKillServiceList = new ArrayList<String>();
	private Timer mTimer = null;
	private static final int KILL_ONETIME_EVERY_TIME = 20 * 60;// 20min

	private final static int KILL_TIME_THIRD_MARKET = 5;// 5s
	private final static int KILL_TIME_FIRST_DISABLE = 10 * 60;// 10min
	private final static int KILL_TIME_SECOND_DISABLE = 20 * 60;// 20min

	private void startTimerTask()
	{
		Log.i(TAG, "setTimerTask ...");
		if (mTimer != null)
		{
			mTimer.cancel();
		}
		mTimer = new Timer();
		mTimer.schedule(new TimerTask()
		{
			@Override
			public void run()
			{
				mHandler.sendEmptyMessage(MSG_CALCULATE_SCREEN_OFF_TIMER);
			}
		}, 1000, 1000);
	}

	private void stopTimerTask()
	{
		if (mTimer != null)
		{
			mTimer.cancel();
		}
		mTimer = null;
	}

	public boolean isPurebkgroundOpen()
	{
		int mSwitchValues = 0;
		try
		{
			mSwitchValues = (int) Settings.System.getLong(mContext.getContentResolver(), PURE_BG_STATUS_OPEN_VALUE);
		}
		catch (SettingNotFoundException e)
		{
			// TODO Auto-generated catch block
			Log.i(TAG, "MSG_SCREEN_OFF SettingNotFoundException " + e);
			e.printStackTrace();
		}
		return (mSwitchValues == 1);
	}

	public void killProcess(String packageName)
	{
		if (mPackageStatsObserver == null)
		{
			mPackageStatsObserver = new PackageStatsObserver();
		}
		mPm.getPackageSizeInfo(packageName, mPackageStatsObserver);
		Log.i(TAG, "kill app : " + packageName);
		mActivityMgr.forceStopPackage(packageName);
	}

	private void doKillOneTime()
	{
		if (!isPurebkgroundOpen()) { return; }
		Log.i(TAG, "doKillOneTime ...");

		doPureBkGroundKillTask(KILL_STATE_EVERY_15MIN);
		doPureBkGroundKillTask(KILL_STATE_THIRD_MARKET);

		if (mIsScreenOff)
		{
			mHandler.removeMessages(MSG_CLEAR_ONE_TIME);
			mHandler.sendEmptyMessageDelayed(MSG_CLEAR_ONE_TIME, KILL_ONETIME_EVERY_TIME * 1000);
		}
	}

	private void resetKillList()
	{
		mKillAppList.clear();
		mKillServiceList.clear();
	}

	private void doPureBkGroundKillTask(int killstate)
	{
		Log.d(TAG, "doPureBkGroundKillTask killstate:" + killstate);
		getDefInputMethod();
		updateRunningServicesList(killstate);
		updateRunningAppList(killstate);
		killRunningAppList(killstate);
		killRunningServiceList(killstate);
		removeAppTask(killstate);
		resetKillList();
		Log.d(TAG, "doPureBkGroundKillTask finished");
	}

	private void updateRunningServicesList(int killstate)
	{
		List<ActivityManager.RunningServiceInfo> services = mActivityMgr.getRunningServices(100);
		List<ActivityManager.RunningTaskInfo> runningServicesTasks = mActivityMgr.getRunningTasks(1);
		String topPackageName = null;
		if (runningServicesTasks != null && runningServicesTasks.size() > 0)
		{
			topPackageName = runningServicesTasks.get(0).topActivity.getPackageName();
		}
		if (topPackageName != null) Log.i(TAG, "updateRunningServicesList topPackageName " + topPackageName + ", runningServicesTasks.size()=" + runningServicesTasks.size());

		int i = 0;
		String pkgName = null;
		for (ActivityManager.RunningServiceInfo service : services)
		{
			pkgName = service.service.getPackageName();
			if (DEBUG) Log.i(TAG, "updateRunningServicesList process:" + service.process + ", pkg:" + pkgName);

			if (pkgName == null)
			{
				continue;
			}

			// some apk skip
			if (skipKillPackage(pkgName))
			{
				continue;
			}
			// skip top
			if (mKillState < KILL_STATE_DISABLE_SECOND)//killstate == KILL_STATE_THIRD_MARKET || killstate == KILL_STATE_DISABLE_FIRST)
			{
				if (topPackageName != null)
				{
					if (pkgName.contains(topPackageName))
					{
						continue;
					}
				}
			}
			// skip repeat
			if (mKillServiceList.contains(pkgName))
			{
				continue;
			}
			if (killstate == KILL_STATE_THIRD_MARKET)
			{
				if (!isThirdMarket(pkgName))
				{
					continue;
				}
			}

			Log.i(TAG, "mKillServiceList add " + pkgName);
			mKillServiceList.add(i, pkgName);
			i++;
		}
	}

	private void updateRunningAppList(int killstate)
	{
		List<RunningAppProcessInfo> appProcesses = mActivityMgr.getRunningAppProcesses();

		List<ActivityManager.RunningTaskInfo> runningAppTasks = mActivityMgr.getRunningTasks(1);
		String topPackageName = null;
		if (runningAppTasks != null && runningAppTasks.size() > 0)
		{
			topPackageName = runningAppTasks.get(0).topActivity.getPackageName();
		}
		if(topPackageName != null)Log.i(TAG, "updateRunningAppList appProcesses.topPackageName " + topPackageName + "==>" + ", runningAppTasks.size()=" + runningAppTasks.size());
		int i = 0;
		for (RunningAppProcessInfo appProcess : appProcesses)
		{
			if (DEBUG) Log.i(TAG, "updateRunningAppList process:" + appProcess.processName);

			if (appProcess.processName.contains(":"))
			{
				appProcess.processName = appProcess.processName.substring(0, appProcess.processName.indexOf(":"));
			}

			// some apk skip
			if (skipKillPackage(appProcess.processName))
			{
				continue;
			}
			// skip top
			if (mKillState < KILL_STATE_DISABLE_SECOND)//killstate == KILL_STATE_THIRD_MARKET || killstate == KILL_STATE_DISABLE_FIRST)
			{
				if (topPackageName != null)
				{
					if (appProcess.processName.contains(topPackageName))
					{
						continue;
					}
				}
			}
			// skip repeat
			if (mKillAppList.contains(appProcess.processName))
			{
				continue;
			}
			if (killstate == KILL_STATE_THIRD_MARKET)
			{
				if (!isThirdMarket(appProcess.processName))
				{
					continue;
				}
			}

			Log.i(TAG, "mKillAppList add " + appProcess.processName);
			mKillAppList.add(i, appProcess.processName);
			i++;
		}
	}

	private void killRunningAppList(int killstate)
	{
		if (0 == mKillAppList.size()) { return; }
		/*-prize add by lihuangyuan,for whitelist -2017-11-25-start-*/
		WhiteListManager whiteListMgr = (WhiteListManager)mContext.getSystemService(Context.WHITELIST_SERVICE);
		String [] selectedList = whiteListMgr.getPuregackgroundDisableList();
		String[] delaykilllist = whiteListMgr.getPurebackgroundNotKillList();
		/*-prize add by lihuangyuan,for whitelist -2017-11-25-end-*/
		if (selectedList == null && killstate != KILL_STATE_THIRD_MARKET) return;

		for (int i = 0; i < mKillAppList.size(); i++)
		{
			if (DEBUG) Log.i(TAG, "running app list[" + i + "]=" + mKillAppList.get(i));

			// third market just kill it
			if (killstate == KILL_STATE_THIRD_MARKET)
			{
				killProcess(mKillAppList.get(i));
				continue;
			}

			// check disable list
			for (int j = 0; j < selectedList.length; j++)
			{
				if (killstate == KILL_STATE_DISABLE_FIRST)
				{
					if (mKillAppList.get(i).contains(selectedList[j]) && !isInList(mKillAppList.get(i),delaykilllist))
					{
						killProcess(mKillAppList.get(i));
					}
				}
				else
				{
					if (mKillAppList.get(i).contains(selectedList[j]))
					{
						killProcess(mKillAppList.get(i));
					}
				}
			}

		}
	}

	private void killRunningServiceList(int killstate)
	{
		if (0 == mKillServiceList.size()) { return; }
		/*-prize add by lihuangyuan,for whitelist -2017-11-25-start-*/
		WhiteListManager whiteListMgr = (WhiteListManager)mContext.getSystemService(Context.WHITELIST_SERVICE);
		String [] selectedList = whiteListMgr.getPuregackgroundDisableList();
		String[] delaykilllist = whiteListMgr.getPurebackgroundNotKillList();
		/*-prize add by lihuangyuan,for whitelist -2017-11-25-end-*/
		if (selectedList == null && killstate != KILL_STATE_THIRD_MARKET) return;

		for (int i = 0; i < mKillServiceList.size(); i++)
		{
			if (DEBUG) Log.i(TAG, "running service list[" + i + "]=" + mKillServiceList.get(i));
			if (killstate == KILL_STATE_THIRD_MARKET)
			{
				killProcess(mKillServiceList.get(i));
				continue;
			}

			for (int j = 0; j < selectedList.length; j++)
			{
				if (killstate == KILL_STATE_DISABLE_FIRST)
				{
					if (mKillServiceList.get(i).contains(selectedList[j]) && !isInList(mKillServiceList.get(i),delaykilllist))
					{
						killProcess(mKillServiceList.get(i));
					}
				}
				else
				{
					if (mKillServiceList.get(i).contains(selectedList[j]))
					{
						killProcess(mKillServiceList.get(i));
					}
				}
			}
		}
	}

	private void removeAppTask(int killstate)
	{
		Log.d(TAG, "removeAppTask killstate:" + killstate);
		List<ActivityManager.RecentTaskInfo> taskList = mActivityMgr.getRecentTasksForUser(100, ActivityManager.RECENT_IGNORE_HOME_STACK_TASKS | ActivityManager.RECENT_IGNORE_UNAVAILABLE
				| ActivityManager.RECENT_INCLUDE_PROFILES | ActivityManager.RECENT_WITH_EXCLUDED, UserHandle.CURRENT.getIdentifier());
		if (taskList == null) return;
		Log.d(TAG, "removeAppTask taskszie:" + taskList.size());

		/*-prize add by lihuangyuan,for whitelist -2017-11-25-start-*/
		WhiteListManager whiteListMgr = (WhiteListManager)mContext.getSystemService(Context.WHITELIST_SERVICE);
		String [] selectedList = whiteListMgr.getPuregackgroundDisableList();
		String[] delaykilllist = whiteListMgr.getPurebackgroundNotKillList();
		/*-prize add by lihuangyuan,for whitelist -2017-11-25-end-*/
		if (selectedList == null && killstate != KILL_STATE_THIRD_MARKET) return;

		String pkgname = null;
		for (int i = 0; i < taskList.size(); i++)
		{
			ActivityManager.RecentTaskInfo taskinfo = taskList.get(i);
			if (taskinfo.baseIntent == null) continue;
			pkgname = taskinfo.baseIntent.getComponent().getPackageName();
			if (pkgname == null) continue;
			if (DEBUG) Log.i(TAG, "task " + taskinfo.id + ",persistentId:" + taskinfo.persistentId + ",pkg:" + pkgname + ",baseIntent:" + taskinfo.baseIntent);

			if (killstate == KILL_STATE_THIRD_MARKET)
			{
				if (isThirdMarket(pkgname) && (mKillAppList.contains(pkgname) || mKillServiceList.contains(pkgname)))
				{
					Log.i(TAG, "remove task:" + taskinfo.persistentId + " " + pkgname);
					mActivityMgr.removeTask(taskinfo.persistentId);
				}
				continue;
			}

			for (int j = 0; j < selectedList.length; j++)
			{
				// Log.d(TAG,"selectedList[j]:"+selectedList[j]);
				if (killstate == KILL_STATE_DISABLE_FIRST)
				{
					if (pkgname.contains(selectedList[j]) && !isInList(pkgname,delaykilllist) 
							&& (mKillAppList.contains(pkgname) || mKillServiceList.contains(pkgname)) )
					{
						Log.i(TAG, "remove task:" + taskinfo.persistentId + " " + pkgname);
						mActivityMgr.removeTask(taskinfo.persistentId);
					}
				}
				else
				{
					if (pkgname.contains(selectedList[j]))
					{
						Log.i(TAG, "remove task:" + taskinfo.persistentId + " " + pkgname);
						mActivityMgr.removeTask(taskinfo.persistentId);
					}
				}
			}

		}
	}

	// //////////////////////////////////////////////////////////////////
	private final static int MSG_SCREEN_ON = 0x01;
	private final static int MSG_SCREEN_OFF = 0x02;
	private final static int MSG_CLEAR_ONE_TIME = 0x03;
	private final static int MSG_CALCULATE_SCREEN_OFF_TIMER = 0x04;
	private final static int MSG_START_INIT = 0x05;

	private final static int MSG_START_ERROR = 0xFF;

	public class WorkHandler extends Handler
	{
		public WorkHandler(Looper looper)
		{
			super(looper);
		}

		public void handleMessage(Message msg)
		{
			switch (msg.what)
			{
			case MSG_SCREEN_ON:
				mIsScreenOff = false;
				mHandler.removeMessages(MSG_CLEAR_ONE_TIME);
				stopTimerTask();
				break;
			case MSG_SCREEN_OFF:
				mIsScreenOff = true;
				mHandler.removeMessages(MSG_CLEAR_ONE_TIME);

				if (!isPurebkgroundOpen())
				{
					break;
				}
				mScreenOffTime = SystemClock.elapsedRealtime();
				mKillState = KILL_STATE_THIRD_MARKET;
				startTimerTask();

				break;
			case MSG_CALCULATE_SCREEN_OFF_TIMER:
			{
				long currentScreenOffTime = SystemClock.elapsedRealtime();
				long screenoffseconds = (currentScreenOffTime / 1000) - (mScreenOffTime / 1000);
				if (DEBUG)
				{
					Log.i(TAG, "MSG_SCREEN_OFF_TIMER traceTime=" + screenoffseconds);
				}

				// kill third market
				if (screenoffseconds < KILL_TIME_THIRD_MARKET)
				{
					break;
				}
				if (mKillState == KILL_STATE_THIRD_MARKET)
				{
					mKillState = KILL_STATE_DISABLE_FIRST;
					doPureBkGroundKillTask(KILL_STATE_THIRD_MARKET);
				}

				// kill disable first
				if (screenoffseconds < KILL_TIME_FIRST_DISABLE)
				{
					break;
				}
				if (mKillState == KILL_STATE_DISABLE_FIRST)
				{
					mKillState = KILL_STATE_DISABLE_SECOND;
					doPureBkGroundKillTask(KILL_STATE_DISABLE_FIRST);
				}

				// kill disable second
				if (screenoffseconds < KILL_TIME_SECOND_DISABLE)
				{
					break;
				}
				if (mKillState == KILL_STATE_DISABLE_SECOND)
				{
					mKillState = KILL_STATE_EVERY_15MIN;

					doPureBkGroundKillTask(KILL_STATE_DISABLE_SECOND);
					doPureBkGroundKillTask(KILL_STATE_THIRD_MARKET);
				}
				stopTimerTask();
				// start kill every 15min
				mHandler.removeMessages(MSG_CLEAR_ONE_TIME);
				mHandler.sendEmptyMessageDelayed(MSG_CLEAR_ONE_TIME, KILL_ONETIME_EVERY_TIME*1000);
				break;
			}
			case MSG_CLEAR_ONE_TIME:
				doKillOneTime();
				break;
			case MSG_START_INIT:
				Log.d(TAG, "MSG_START_INIT threaid:" + Thread.currentThread().getId());
				break;
			case MSG_START_ERROR:
			{
				String str = null;
				str.length();
				break;
			}

			}
		}
	}

	// //////////////////////////////////////////////////////////
	private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver()
	{
		@Override
		public void onReceive(Context context, Intent intent)
		{
			String action = intent.getAction();
			Log.d(TAG, "onReceive action:" + action);
			if (Intent.ACTION_SCREEN_OFF.equals(action))
			{
				mHandler.sendEmptyMessage(MSG_SCREEN_OFF);
			}
			if (Intent.ACTION_SCREEN_ON.equals(action))
			{
				mHandler.sendEmptyMessage(MSG_SCREEN_ON);
			}
		}
	};

	class ClearCacheObserver extends IPackageDataObserver.Stub
	{
		public void onRemoveCompleted(final String packageName, final boolean succeeded)
		{
			if (succeeded)
			{
				Log.i(TAG, "Clear " + packageName + " cache succeed.");
			}
		}
	}

	class PackageStatsObserver extends IPackageStatsObserver.Stub
	{
		public void onGetStatsCompleted(PackageStats stats, boolean succeeded)
		{
			if (DEBUG) Log.i(TAG, "onGetStatsCompleted succeeded " + succeeded);
			if (stats == null) { return; }

			if (succeeded)
			{
				Log.i(TAG, stats.packageName + " cache " + formatFileSize(stats.cacheSize) + ",dataSize=" + formatFileSize(stats.dataSize) + ",codeSize=" + formatFileSize(stats.codeSize));
			}
			if (mClearCacheObserver == null)
			{
				mClearCacheObserver = new ClearCacheObserver();
			}
			mPm.deleteApplicationCacheFiles(stats.packageName, mClearCacheObserver);
		}
	}

	/**
	 * long-string KB/MB
	 */
	private String formatFileSize(long number)
	{
		return Formatter.formatFileSize(mContext, number);
	}

}
