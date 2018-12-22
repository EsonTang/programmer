package com.prize.cloudlist;

import java.io.File;
import java.util.Calendar;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import com.prize.cloudlist.CloudUtils.CloudUpdateData;

public class CloundListService extends Service
{	
	public static final String TAG = CloudUtils.TAG;
	public static final boolean IS_DEBUG = CloudUtils.IS_DEBUG;

	@Override
	public IBinder onBind(Intent intent)
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void onCreate()
	{
		Log.i(TAG,"onCreate++++++");	
		//init data dir
		File file = new File(CloudUtils.getCloudListDir());
		if(!file.exists())
		{
			file.mkdir();
		}
		init();
		mHandler.postDelayed(new Runnable()
		{
			
			@Override
			public void run()
			{
				WhitsListUpdateData.bootcompletedSetproperty(CloundListService.this);
				
			}
		}, 10*1000);
		
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
		Log.i(TAG,"onDestroy++++++");
		release();		
		mWakeLock.release();
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
		mWorkerThread = new HandlerThread("CloudListService");
		mWorkerThread.start();
		mHandler = new WorkHandler(mWorkerThread.getLooper());

		mWakeLock.init(this);
		
		mHandler.sendEmptyMessage(MSG_START_INIT);

		// set error time
		// mHandler.sendEmptyMessageDelayed(MSG_START_ERROR, 3 * 1000);		
	}

	private void init()
	{
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

		initThread();
		
		//init upgradeservice
		
		//UpgradeService securityUpgrade = new UpgradeService(this,mWakeLock,mHandler,mCloudUtil,"com.prize.prizesecurity");
		//mUpgradeList.add(securityUpgrade);
		
		//load data
		//loadData();
		
		mContext = this;
		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(Intent.ACTION_SCREEN_OFF);
		intentFilter.addAction(Intent.ACTION_SCREEN_ON);
		intentFilter.addAction("com.prize.cloudlist.test");
		registerReceiver(mReceiver, intentFilter);
	}

	private void release()
	{
		mWorkerThread.quit();
		unregisterReceiver(mReceiver);
	}

	// ////////////////////////////////////////////////////////////////
	private BroadcastReceiver mReceiver = new BroadcastReceiver()
	{
		@Override
		public void onReceive(Context context, Intent intent)
		{
			Log.i(TAG, "onReceive action:" + intent.getAction());
			if (Intent.ACTION_SCREEN_OFF.equals(intent.getAction()))
			{
				mIsScreenOff = true;
				mScreenOffTime = System.currentTimeMillis();
				registerTimePicker();
			}
			else if (Intent.ACTION_SCREEN_ON.equals(intent.getAction()))
			{
				mIsScreenOff = false;
				ungisterTimePicker();
			}
			else if("com.prize.cloudlist.test".equals(intent.getAction()))
			{
				String url = intent.getStringExtra("url");
				mDownloader.setTestServer(url);
				mDownloader.startTest();
			}
			else if("com.prize.cloudlist.upgrade.test".equals(intent.getAction()))
			{
				String url = intent.getStringExtra("url");
				mDownloader.setTestServer(url);
				mDownloader.startTest();
			}
				
		}
	};

	private boolean mIsTimePickerRegistered = false;
	private BroadcastReceiver mTimePickerReceiver = new BroadcastReceiver()
	{

		@Override
		public void onReceive(Context context, Intent intent)
		{
			if (IS_DEBUG) Log.i(TAG, "onReceive action:" + intent.getAction());
			if (Intent.ACTION_TIME_TICK.equals(intent.getAction()))
			{
				mHandler.sendEmptyMessage(MSG_CHECK_DO_SOMETHING_EVERY_MIN);
			}
		}

	};

	private void registerTimePicker()
	{
		if (!mIsTimePickerRegistered && mIsScreenOff)
		{
			Log.i(TAG, "register timepicker");
			IntentFilter intentFilter = new IntentFilter();
			intentFilter.addAction(Intent.ACTION_TIME_TICK);
			registerReceiver(mTimePickerReceiver, intentFilter);
			mIsTimePickerRegistered = true;
		}
	}

	private void ungisterTimePicker()
	{
		if (mIsTimePickerRegistered && !mIsScreenOff)
		{
			Log.i(TAG, "unregister timepicker");
			unregisterReceiver(mTimePickerReceiver);
			mIsTimePickerRegistered = false;
		}
	}
	
	// /////////////////////////////////////////////////////////////
	// //////////////////////////////////////////////////////////////////
			
	
	private long mScreenOffTime;
	private boolean mIsScreenOff = false;	
	private WorkWakelock mWakeLock = new WorkWakelock();
	private WhiteListDownload mDownloader = new WhiteListDownload();
	

	// ///////////////////////////////////////
	public static final int MSG_START_INIT = 1;
	public static final int MSG_START_ERROR = 2;	

	private static final int SCREEN_OFF_SECONDS_DO_CHECK = 5*60;
	public static final int MSG_CHECK_DO_SOMETHING_EVERY_MIN = 100;

	private HandlerThread mWorkerThread = null;
	private WorkHandler mHandler = null;
	private Context mContext = null;

	public class WorkHandler extends Handler
	{
		public WorkHandler(Looper looper)
		{
			super(looper);
		}

		@Override
		public void handleMessage(Message msg)
		{
			switch (msg.what)
			{
			case MSG_START_INIT:
				Log.i(TAG, "MSG_START_INIT");	
				mDownloader.init(mContext, mWakeLock);			
				break;
			case MSG_START_ERROR:
				{
					String str = null;
					str.length();
				}
				break;

			case MSG_CHECK_DO_SOMETHING_EVERY_MIN:
				checkDoSomething();
				break;			
			}
		}
	}

	// ////////////////////////////////////////////////////////
	

	private void checkDoSomething()
	{
		// if(IS_DEBUG)Log.i(TAG,"screenoff:"+mIsScreenOff);
		if (!mIsScreenOff) { return; }

		long curtime = System.currentTimeMillis();
		// 1.screen off 5minute check root
		if (curtime - mScreenOffTime >= SCREEN_OFF_SECONDS_DO_CHECK * 1000)
		{

			Calendar c = Calendar.getInstance();
			int hour = c.get(Calendar.HOUR_OF_DAY);
			if (IS_DEBUG) Log.i(TAG, "cur hour:" + hour);
			// 2.screen off 5minute && time hour 2-5
			if ((hour >= 2 && hour <= 5) || (hour >= 11 && hour <= 13))
			{
				// for whitelist	
				mDownloader.doStartCheck();	
			}
		}
	}	
}
