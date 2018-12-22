/**
 * Decription: 
 *     This service is intended to determine if an app can access network when the phone
 *     is in screen-off state. It's purposed for battery saving.
 *
 * Note: We create a handler thread, and put all main operations in it, eg, processing
 *       broadcasts, processing content changed, setting firewall rule.  We do these without
 *       holding lock, so be carefull when you add or modify codes for this service.
 *
 * Creator: prize-linkh 20160713
 *
 * Version: v0.0.1
 *
 * @hide
 */
package com.android.server;

import android.content.Context;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.os.Looper;
import android.os.RemoteException;
import android.util.Slog;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.app.ActivityManagerNative;
import android.app.IActivityManager;
import android.app.IUidObserver;
import android.content.pm.PackageManager;
import android.os.INetworkManagementService;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ApplicationInfo;
import android.content.pm.IPackageManager;
import android.provider.Settings;
import android.os.ServiceManager;
import android.os.PowerManager;
import android.database.ContentObserver;
import android.util.SparseIntArray;
import android.util.SparseBooleanArray;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.SystemClock;
import android.os.UserHandle;
import android.util.ArraySet;
import java.util.List;
import com.mediatek.common.prizeoption.PrizeOption;
import android.os.Build;
/*-prize add by lihuangyuan,for whitelist -2017-05-06-start-*/
import android.os.IWhiteListService;
import android.os.IWhiteListChangeListener;
import android.os.WhiteListManager;
import android.os.WakeupItem;
/*-prize add by lihuangyuan,for whitelist -2017-05-06-end-*/

class PrizeAppNetworkManagerService extends SystemService {
    private static final String TAG = "PriAppNetMgrSrv";
    private static final boolean DBG = "eng".equals(Build.TYPE);
    private static final boolean DBG_IGNORE_CONTROLLING = DBG;
    
    private Context mContext;
    private final Object mLock = new Object();
    private MyHandler mHandler;
    private HandlerThread mHandlerThread;

    // Handler message id.
    private static final int CONTENT_CHANGED_MESSAGE = 0;
    private static final int TIMEOUT_MESSAGE = 1;
    private static final int PROC_UID_OBSERVER_MESSAGE = 2;

    private static final int OBSERVER_TYPE_CONTROL_SWITCH_STATUS = 0;
    private static final int OBSERVER_TYPE_BG_ENABLE_LIST = 1;

    private ActivityManager mActivityManager;
    private IActivityManager mIActivityManager;    
    private INetworkManagementService mINetMgr;
    private PackageManager mPackageManager;
    private IPackageManager mIPackageManager;
    private PowerManager mPowerManager;
    
    private boolean mEnableControlAppNetwork = true;
    private static final boolean DEFAULT_ENABLED = true;

    //variables related to clear bk service 
    private static final boolean USE_DATA_FROM_CLEAR_BK_SERVICE = false;
    private static final String PURE_BG_OPEN_STATUS = "pureBgStatusOpenValue";
    //private static final String PURE_BG_DISABLE_APP_LIST = "pureBgDisableAppList";
    private static final String PURE_BG_ENABLED_APPS_LIST = "pureBgEnableAppList";
    private static final String PURE_BG_ENABLED_APPS_LIST_SEPRATOR = ",";
    private static final boolean DEFAULT_BG_OPEN_ENABLED = true;  
    private boolean mIsEnabledClearBk;
    private final ArraySet<String> mBgEnabledAppsList = new ArraySet<>();
    private boolean mNeedRetrieveBgEnabledAppsListAgain;
    private MyContentObserver mBgEnabledAppsContentObserver;
    private boolean mHasRegisteredBgEnabledAppsObserver;
    //END...

    /*prize-change 15 to 5-modify by wangxianzhen-2016-09-08-start*/
    private static final long TIMEOUT_AFTER_SCREEN_OFF = 5 * 60 * 1000; // 5 minutes.
    private static final long TIMEOUT_TO_SEND_CONTENT_CHANGED_EVENT = 1000; // 1 second
    
    private long mNonInteractiveStartTime;
    private boolean mNonInteractive;
    private boolean mHasSentInstantMsg;
    
    private final MyBroadcastReceiver mBroadcastReceiver = new MyBroadcastReceiver();
    private boolean mHasRegisteredBroadcastReceiver;
    
    // Array that recording uid state.
    // the key is uid value, the value represents that this uid is allowed or disallowed
    // accessing network.
    private final SparseIntArray mUidState = new SparseIntArray();
    // Record uids that were already processed. Clear this array when screen becomes on!
    private final SparseIntArray mProcessedUids = new SparseIntArray();
    
    // White list that can escape firewall rule.
    private final ArraySet<String> mWhiteList = new ArraySet<>();

    // variables related to Intercepted wakeup alarm feature. 
    private static final boolean USE_DATA_FROM_INTERCEPTED_WAKEUP_ALARM = true;
    private ArraySet<String> mInterceptedWakeupAlarmPkgList;

    // monitor the live state of a process.
    private static final boolean SHOULD_MONITOR_LIVE_STATE_OF_PROCESS = true;
    // Store uids that AMS notify us
    private final SparseIntArray mPendingUids = new SparseIntArray();
    private boolean mHasRegisteredUidObserver;
    private ProcUidObserver mUidObserver;
    
    //Note: Don't change this value except you know what you are doing!!!
    private final static String FIREWALL_CHAIN_FOR_SLEEPING = "fw_sleeping";
    /*-prize add by lihuangyuan,for whitelist -2017-05-06-start-*/
    IWhiteListService mIWhiteListService;
    IWhiteListChangeListener mWhiteListChangeListener;
    public void doSleepNetDataChange(final String[] sleepnetlist)
    {
    	mHandler.post(new Runnable()
	{
		@Override
                public void run() {
			try
			{
				//String [] sleepnetlist = mIWhiteListService.getSleepNetList();
				synchronized (mWhiteList) 
				{				
					if(sleepnetlist != null)
					{
						mWhiteList.clear();
						for(int i=0;i<sleepnetlist.length;i++)
						{
							if(!mWhiteList.contains(sleepnetlist[i]))
							{
								Slog.i("whitelist","sleepnetlist add pkg:"+sleepnetlist[i]);
								mWhiteList.add(sleepnetlist[i]);
							}
							else
							{
								Slog.i("whitelist","sleepnetlist already exist pkg:"+sleepnetlist[i]);
							}						
						}
					}
				}
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
		}
	});
    }
    public static class MyWhiteListChangeListener extends IWhiteListChangeListener.Stub	
	{
		PrizeAppNetworkManagerService mService;
		MyWhiteListChangeListener(PrizeAppNetworkManagerService service)
		{
			mService = service;
		}
		@Override
		public void onPurebackgroundChange(boolean isHideChange,
				boolean isDefChange, boolean isnotkillChange)
				throws RemoteException {
			
		}

		@Override
		public void onNotificationChange(boolean isHideChange,
				boolean isDefChange) throws RemoteException {
			
		}

		@Override
		public void onFloatDefChange() throws RemoteException {
			
		}

		@Override
		public void onAutoLaunchDefChange() throws RemoteException {
			
		}

		@Override
		public void onNetForbadeListChange() throws RemoteException {
			
		}

		@Override
		public void onWakeupListChange(WakeupItem[] wakeuplist) throws RemoteException {
			
		}
              @Override
		public void onProviderWakeupListChange(WakeupItem[] wakeuplist) throws RemoteException {
			
		}
		@Override
		public void onSleepNetListChange(String[] sleepnetlist) throws RemoteException {
			mService.doSleepNetDataChange(sleepnetlist);
		}

		@Override
		public void onBlockActivityListChange(String[] blocklist) throws RemoteException {	
		}

		@Override
		public void onMsgWhiteListChange() throws RemoteException {
			
		}

		@Override
		public void onInstallWhiteListChange() throws RemoteException {			
			
		}
		
	};    
    /*-prize add by lihuangyuan,for whitelist -2017-05-06-end-*/
    
    public PrizeAppNetworkManagerService(Context context) {
        super(context);
        mContext = context;
    }

    @Override
    public void onStart() {
        mHandlerThread = new HandlerThread("PriAppNetMgrSrv");
        mHandlerThread.start();
    }

    @Override
    public void onBootPhase(int phase) {
        if (DBG) {
            Slog.d(TAG, "onBootPhase() phase=" + phase);
        }

        if (phase == PHASE_ACTIVITY_MANAGER_READY) {
            mHandler = new MyHandler(mHandlerThread.getLooper());
            if (DBG) {
                Slog.d(TAG, "Create handler in thread " + mHandlerThread.getThreadId());
            }
            
            //get service proxy.
            mActivityManager = (ActivityManager)mContext.getSystemService(Context.ACTIVITY_SERVICE);
            mIActivityManager = ActivityManagerNative.getDefault();
            mINetMgr = INetworkManagementService.Stub.asInterface(
                ServiceManager.getService(Context.NETWORKMANAGEMENT_SERVICE));
            mPackageManager = mContext.getPackageManager();
            mIPackageManager = IPackageManager.Stub.asInterface(ServiceManager.getService("package"));
            mPowerManager = (PowerManager)mContext.getSystemService(Context.POWER_SERVICE);

            mNonInteractive = !mPowerManager.isInteractive();
            mEnableControlAppNetwork = Settings.System.getInt(getContext().getContentResolver(), 
                Settings.System.PRIZE_CONTROL_APP_NETWORK_STATE, DEFAULT_ENABLED ? 1 : 0) == 1;            
            Slog.d(TAG, "mEnableControlAppNetwork=" + mEnableControlAppNetwork);

            MyContentObserver contentObserver = new MyContentObserver(mHandler, OBSERVER_TYPE_CONTROL_SWITCH_STATUS);
            mContext.getContentResolver().registerContentObserver(
                Settings.System.getUriFor(Settings.System.PRIZE_CONTROL_APP_NETWORK_STATE), false, contentObserver);
            
            // white list
            synchronized (mWhiteList) 
	     {
            	mWhiteList.clear();
            	final String[] list = mContext.getResources().getStringArray(
                                com.prize.internal.R.array.white_list_for_app_net_mgr_for_sleeping);
            	for (String pkg : list) {
                mWhiteList.add(pkg);
            	}
            }

            // intercepted wakeup alarm list data
            if (USE_DATA_FROM_INTERCEPTED_WAKEUP_ALARM) {
                final AlarmManagerService.LocalService alarmSrv = 
                    getLocalService(AlarmManagerService.LocalService.class);
                if (alarmSrv != null) {
                    mInterceptedWakeupAlarmPkgList = alarmSrv.getInterceptedPkgList();
                    if (DBG) {
                        if (mInterceptedWakeupAlarmPkgList == null) {
                            Slog.d(TAG, "Null wakeup alarm list data!");
                        } else {
                            Slog.d(TAG, "Intercepted wakeup alarm list: ");
                            for (int i = 0; i < mInterceptedWakeupAlarmPkgList.size(); ++i) {
                                Slog.d(TAG, "   " + mInterceptedWakeupAlarmPkgList.valueAt(i));
                            }
                        }
                    }
                } else {
                    Slog.w(TAG, "Can't get alarm local service!");
                }
            }
            
            mIsEnabledClearBk = false;
            mNeedRetrieveBgEnabledAppsListAgain = false;
            mBgEnabledAppsList.clear();
            
            if (mEnableControlAppNetwork) {
                registerBroadcastReceiver();
                registerContentObserver();
            }
	      /*-prize add by lihuangyuan,for whitelist -2017-05-06-start-*/
		 if(mWhiteListChangeListener == null)
		 {
		 	mWhiteListChangeListener = new MyWhiteListChangeListener(this);
		 }
	        mIWhiteListService = IWhiteListService.Stub.asInterface(ServiceManager.getService("whitelist"));
	       try
	       {
	           mIWhiteListService.unregisterChangeListener(mWhiteListChangeListener);
	     	    mIWhiteListService.registerChangeListener(mWhiteListChangeListener,WhiteListManager.WHITELIST_TYPE_SLEEPNET);
	       }
	       catch(Exception e)
	       {
	           e.printStackTrace();
	       }
		 /*-prize add by lihuangyuan,for whitelist -2017-05-06-end-*/
        } else if (phase == PHASE_BOOT_COMPLETED) {
            if (!mPowerManager.isInteractive()) {
                Slog.d(TAG, "send time out message because device is non-interactive when system boots completely.");
                mHandler.sendMessageDelayed(mHandler.obtainMessage(TIMEOUT_MESSAGE), TIMEOUT_AFTER_SCREEN_OFF);
            }
        }
    }

    // Note: This function will clear mBgEnabledAppsList data
    private String[] getEnabledAppsListFromClearBkProvider(final ArraySet<String> dataSet) {
        return getEnabledAppsListFromClearBkProvider(dataSet, true);
    }
    
    private String[] getEnabledAppsListFromClearBkProvider(final ArraySet<String> dataSet, boolean clear) {
        if (DBG) {
            Slog.d(TAG, "getDataFromClearBkProvider()... ");
        }

        if (clear && dataSet != null) {
            dataSet.clear();
        }
        
        String list = Settings.System.getString(getContext().getContentResolver(), 
                            PURE_BG_ENABLED_APPS_LIST);
        if (DBG) {
            Slog.d(TAG, "clear bg enabled apps list: " + list);
        }
        
        String[] dataList = null;
        if (list != null) {
            list = list.trim();
            if (!list.isEmpty()) {
                final String[] pkgs = list.split(PURE_BG_ENABLED_APPS_LIST_SEPRATOR);
                if (dataSet != null) {
                    for (String pkg : pkgs) {
                        dataSet.add(pkg);
                    }
                } else {
                    dataList = pkgs;
                }
            }
        }

        return dataList;
    }

    private boolean isWhiteListApp(String pkg) {
        boolean isWhite = false;
        if (pkg == null) {
            if (DBG_IGNORE_CONTROLLING) {
                Slog.d(TAG, "isWhiteListApp()  Null package name!!");
            }            
            return false;
        }

        // mWhiteList doesn't only contains exact package name.
        // It also can contain key characters of package name. So we must iterate this list.
        /*-prize add by lihuangyuan,for whitelist -2017-05-06-start-*/
	synchronized (mWhiteList) 
	{	
	        for (int i = 0; i < mWhiteList.size(); ++i) {
	            final String whitePkg = mWhiteList.valueAt(i);
	            if (pkg.contains(whitePkg)) {
	                if (DBG_IGNORE_CONTROLLING) {
	                    Slog.d(TAG, "Hit an item of white List. item=" + whitePkg);
	                }                
	                isWhite = true;
	                break;
	            }
	        }
        }
	/*-prize add by lihuangyuan,for whitelist -2017-05-06-end-*/

        if (DBG) {
            Slog.d(TAG, "isWhiteListApp() pkg=" + pkg + ", isWhiteApp=" + isWhite);
        }
        return isWhite;
    }
    
    // Note: Make sure mHandler is initialized before invoking this function!!
    private void registerContentObserver() {
        if (USE_DATA_FROM_CLEAR_BK_SERVICE) {
            if (DBG) {
                Slog.d(TAG, "Register content observer...");
            }
            
            if (mHasRegisteredBgEnabledAppsObserver) {
                return;
            }
            
            if (mBgEnabledAppsContentObserver == null) {
                mBgEnabledAppsContentObserver = new MyContentObserver(mHandler, OBSERVER_TYPE_BG_ENABLE_LIST);
            }
            mContext.getContentResolver().registerContentObserver(
                Settings.System.getUriFor(PURE_BG_ENABLED_APPS_LIST), false, mBgEnabledAppsContentObserver);
            mHasRegisteredBgEnabledAppsObserver = true;

            getEnabledAppsListFromClearBkProvider(mBgEnabledAppsList);
            mNeedRetrieveBgEnabledAppsListAgain = false;
            
            // We don't monitor PURE_BG_OPEN_STATUS status. We assume that only user can change these when the screen is on, 
            // there aren't other places to change this switch and
            // if the screen is off, we will retrieve this switch state again.
            // En, maybe we also needn't monitor PURE_BG_ENABLED_APPS_LIST status above.
            mIsEnabledClearBk = Settings.System.getInt(getContext().getContentResolver(), 
                PURE_BG_OPEN_STATUS, DEFAULT_BG_OPEN_ENABLED ? 1 : 0) == 1;
        }

    }

    private void unregisterContentObserver() {
        if (USE_DATA_FROM_CLEAR_BK_SERVICE) {
            if (DBG) {
                Slog.d(TAG, "Unregister content observer...");
            }
            
            if (!mHasRegisteredBgEnabledAppsObserver) {
                return;
            }
            
            mContext.getContentResolver().unregisterContentObserver(mBgEnabledAppsContentObserver);
            mHasRegisteredBgEnabledAppsObserver = false;
        }
    }
    
    // Note: Make sure mHandler is initialized before invoking this function!!
    private void registerUidObserver() {
        if (SHOULD_MONITOR_LIVE_STATE_OF_PROCESS) {
            if (mNonInteractive && !mHasRegisteredUidObserver) {
                if (mUidObserver == null) {
                    mUidObserver = new ProcUidObserver(mHandler, PROC_UID_OBSERVER_MESSAGE);
                }
                
                try {
                    if (DBG) {
                        Slog.d(TAG, "Register uid observer...");
                    }
                    mIActivityManager.registerUidObserver(mUidObserver, ActivityManager.UID_OBSERVER_PROCSTATE);
                    mHasRegisteredUidObserver = true;
                } catch (RemoteException ex) {
                    Slog.e(TAG, "Can't register uid observer. " + ex);
                }
            }
        }
    }
    
    private void unregisterUidObserver() {
        if (SHOULD_MONITOR_LIVE_STATE_OF_PROCESS) {
            if (mHasRegisteredUidObserver) {
                try {
                    if (DBG) {
                        Slog.d(TAG, "Unregister uid observer...");
                    }
                    mHasRegisteredUidObserver = false;
                    mIActivityManager.unregisterUidObserver(mUidObserver);
                } catch (RemoteException ex) {
                    Slog.e(TAG, "Can't unregister uid observer. " + ex);
                }
            }
        }
    }
    
    // Note: Make sure mHandler is initialized before invoking this function!!
    private void registerBroadcastReceiver() {
        if (DBG) {
            Slog.d(TAG, "register broadcast receiver.");
        }
        
        if (mHasRegisteredBroadcastReceiver) {
            return;
        }
        
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_SCREEN_OFF);
        filter.addAction(Intent.ACTION_SCREEN_ON);
        // When device enters non-interactive state, we
        // will send a time out message for controlling 
        // app net through hander. But the time for handler
        // doesn't including time spent in sleep, and if device
        // enters deep sleeping, we can't perform at the specified time.
        // En, We need a way to resolve it. Ok, If device is waken up for sleep,
        // time tick event almost is sent by system. we can receive this event and
        // calculate time spent.
        filter.addAction(Intent.ACTION_TIME_TICK);
        filter.setPriority(IntentFilter.SYSTEM_HIGH_PRIORITY);
        mContext.registerReceiver(mBroadcastReceiver, filter, null, mHandler/*process broadcasts in our own thread*/);
        mHasRegisteredBroadcastReceiver = true;
    }

    private void unregisterBroadcastReceiver() {
        if (DBG) {
            Slog.d(TAG, "unregister broadcast receiver.");
        }
        
        if (!mHasRegisteredBroadcastReceiver) {
            return;
        }
        
        mHasRegisteredBroadcastReceiver = false;
        mContext.unregisterReceiver(mBroadcastReceiver);
    }

    private void enableAppNetManagment() {
        if (DBG) {
            Slog.d(TAG, "enableAppNetManagment()...");
        }

        Slog.d(TAG, "Enable control app network!");
                
        registerBroadcastReceiver();
        registerContentObserver();

        // En, If device is in interactive state, we don't register this observer.
        // If not, ok, we will send a delay message below to controll app net and we will
        // register this observer at that time.
        //registerUidObserver();
        
        // If the user is interactive with device, we needn't send this message. Because
        // we can receive screen-off broadcast later when device enters non-interactive state.
        if (mNonInteractive) {
            if (DBG) {
                Slog.d(TAG, "send time out message becaue of control state changed.");
            }
            
            mHandler.sendMessageDelayed(mHandler.obtainMessage(TIMEOUT_MESSAGE), TIMEOUT_AFTER_SCREEN_OFF);
        }        
    }

    private void disableAppNetManagment() {
        if (DBG) {
            Slog.d(TAG, "disableAppNetManagment()...");
        }
        
        Slog.d(TAG, "Disable control app network!");
        unregisterBroadcastReceiver();
        unregisterContentObserver();
        unregisterUidObserver();
        
        clearFirewallRuleForAppsIfNeeded();
    }
    
    private void screenOff() {
        mNonInteractiveStartTime = SystemClock.elapsedRealtime();
        mNonInteractive = true;
        mHasSentInstantMsg = false;
        mHandler.removeMessages(TIMEOUT_MESSAGE);
        mHandler.sendMessageDelayed(mHandler.obtainMessage(TIMEOUT_MESSAGE), TIMEOUT_AFTER_SCREEN_OFF);
    }

    private void screenOn() {
        mHandler.removeMessages(TIMEOUT_MESSAGE);
        mNonInteractiveStartTime = 0;
        mNonInteractive = false;
        
        unregisterUidObserver();        

        synchronized(mLock) {
            mProcessedUids.clear();
        }
        clearFirewallRuleForAppsIfNeeded();
    }
    
    private boolean clearFirewallChain() {
        boolean successed = false;        
        if (mINetMgr != null) {
            try {
                Slog.d(TAG, "clearFirewallChain()...");
                mINetMgr.clearFirewallChain(FIREWALL_CHAIN_FOR_SLEEPING);
                successed = true;
            } catch (Exception e) {
                Slog.e(TAG, "clear firewall chain failed!  " + e);
            }
        }
        
        return successed;        
    }

    private boolean setFirewallUidRule(int uid, boolean allow) {
        boolean successed = false;
        if (mINetMgr != null) {
            try {
                Slog.d(TAG, "setFirewallUidRule() uid=" + uid + ", allow=" + allow);
                mINetMgr.setFirewallUidRuleForSleeping(uid, allow);
                successed = true;
            } catch (Exception e) {
                Slog.e(TAG, "set firewall uid rule failed!  " + e);
            }
        }
        
        return successed;
    }

    private String typeToString(int type) {
        String typeStr;
        
        switch (type) {
        case OBSERVER_TYPE_CONTROL_SWITCH_STATUS: {
            typeStr = "CONTROL_SWITCH_STATUS";
            break;
        }
        
        case OBSERVER_TYPE_BG_ENABLE_LIST: {
            typeStr = "BG_ENABLE_LIST";
            break;
        }
        
        default: {
            typeStr = "UNKNOWN";
            break;
        }
        }

        return typeStr;
    }
    
    private void handleContentChanged(int type) {
        if (DBG) {
            Slog.d(TAG, "handleContentChanged() type=" + typeToString(type));
        }

        if (type == OBSERVER_TYPE_CONTROL_SWITCH_STATUS) {
            mHandler.removeMessages(TIMEOUT_MESSAGE);
            // We need to retrieve this state here. Because the switch state changed
            // causes we maybe don't have the fresh device state.
            mNonInteractive = !mPowerManager.isInteractive();

            if (mEnableControlAppNetwork) {
                enableAppNetManagment();
            } else {
                disableAppNetManagment();
            }
        } else if (type == OBSERVER_TYPE_BG_ENABLE_LIST) {
            // If current the state of controlling app network is off or the screen is on,
            // we needn't to process this content changed event because 
            // we will retrieve data again when the state becomes on or the screen becomes off.
            if (mEnableControlAppNetwork && mNonInteractive) {
                final String[] dataList = getEnabledAppsListFromClearBkProvider(null);
                // 1. If dataList has a new package that isn't exists in mBgEnabledAppsList,
                //     Ok, we still set firewwall rule for this package.
                // 2. If mBgEnabledAppsList has a package that is already removed,
                //     Ok, we still keep firewall rule for it. Don't be worry. Because when screen becomes on,
                //     we will clear all firewall rules.                
                if (dataList != null) {
                    for (String pkg : dataList) {
                        if (!mBgEnabledAppsList.contains(pkg)) {
                            // A new package.
                            mBgEnabledAppsList.add(pkg);
                            // yeah, Although maybe this app isn't running,
                            // the screen is off, so we still can set firewall rule for it.
                            updateFirewallRuleForApp(pkg, false);
                        }
                    }
                }
                
                // OK. we need retrieve data again next screen-off time for this event.
                mNeedRetrieveBgEnabledAppsListAgain = true;                
            } else {
                // record this event.
                mNeedRetrieveBgEnabledAppsListAgain = true;
            }
        }
    }

    private void clearFirewallRuleForAppsIfNeeded() {
        if (DBG) {
            Slog.d(TAG, "clearFirewallRuleForAppsIfNeeded()...");
        }        
        if (mUidState.size() > 0) {
            if (clearFirewallChain()) {
                mUidState.clear();
            } else {
                Slog.w(TAG, "clear firewall rule failed!!");
            }
        }
    }
    
    private void setFirewallRuleForAppIfNeeded(int uid, boolean allow) {
        if (DBG) {
            Slog.d(TAG, "setFirewallRuleForApp() uid=" + uid + ", allow=" + allow);
        }

        final int state = mUidState.get(uid, -1);
        final int newState = allow ? 1 : 0;
        if (DBG) {
            Slog.d(TAG, "setFirewallRuleForApp() state=" + state + ", newState=" + newState);
        }
        
        if (state == -1 || state != newState) {
            if (setFirewallUidRule(uid, allow)) {
                mUidState.put(uid, newState);
            }
        }
    }

    /** Note: This fuction doesn't check if the supplied uid is belongs to the supplied pkg.
      *     You are responsible for this checking yourself!!! Also you must supply
      *     an valid uid.
      */
    private void updateFirewallRuleForApp(String pkg, int uid, boolean allow) {
        if (DBG) {
            Slog.d(TAG, "updateFirewallRuleForApp() pkg=" + pkg + ", uid=" + uid
                + ", allow=" + allow);
        }
       
        // Step 1. check if it is an app uid.
        if (!UserHandle.isApp(uid)) {
            if (DBG_IGNORE_CONTROLLING) {
                Slog.d(TAG, "Not an app uid! Give up.");
            }
            return;
        }

        // Step 2. get application info. If failed, give up setting firewall rule.
        ApplicationInfo ai = null;
        try {
            if (pkg == null) {
                //pkg = mPackageManager.getNameForUid(uid);
                final String[] packages = mPackageManager.getPackagesForUid(uid);
                if (packages == null) {
                    Slog.w(TAG, "Can't get package name for uid " + uid + ". Give up.");
                    return;
                } else if (packages.length > 1) {
                    Slog.w(TAG, "Not a single package name for uid " + uid + ". Give up.");
                    return;
                }
                pkg = packages[0];
            }

            ai = mIPackageManager.getApplicationInfo(pkg, 0, UserHandle.getUserId(uid));
            if (ai == null) {
                Slog.w(TAG, "Can't get application for pkg " + pkg + ". Give up.");
                return;
            }
        } catch (RemoteException e) {
            Slog.e(TAG, "get application info failed!! " + e);
            return;
        }

        // Step 2-1. check if it has INTERNET permission. 
        //           If no, system will prevents it from accessing network, and so we don't need set firewall rule for it.
        if (mPackageManager.checkPermission(android.Manifest.permission.INTERNET, pkg) 
                != PackageManager.PERMISSION_GRANTED) {
            if (DBG_IGNORE_CONTROLLING) {
                Slog.d(TAG, "This package doesn't have INTERNET permission. Needn't set firewall rule.");                
            }

            return;
        }
        
        // Step 3. check if it's in white list.
        if (isWhiteListApp(pkg)) {
            if (DBG_IGNORE_CONTROLLING) {
                Slog.d(TAG, "a white-list app! Give up.");
            }
            return;
        }

        // Step 4. check if it's a third system app or data app. If yes, disallow accessing network.
        //         In Lollipop version, there hasn't an additional feature that manages all third system apps.
        //         So for contorlling network of those third system apps, we temporarily use intercepted wakeup alarm list data here.
        if (ai != null && ai.isSystemApp()) {
            if (!ai.isPrebuiltThirdApp()) {
                if (USE_DATA_FROM_INTERCEPTED_WAKEUP_ALARM && mInterceptedWakeupAlarmPkgList != null
                    && mInterceptedWakeupAlarmPkgList.contains(pkg)) {
                    if (DBG) {
                        Slog.d(TAG, "a system app. But it's in intercepted alarm list. Keep going.");
                    }
                } else {
                    if (DBG_IGNORE_CONTROLLING) {
                        Slog.d(TAG, "a system app. Give up.");
                    }
                    return;
                }
            }
        }
        
        // Step 5. check if it's disallowed running in background.
        //         Note: some apps are still allowed running background
        //         even they aren't in enabled apps list.
        if (USE_DATA_FROM_CLEAR_BK_SERVICE) {
            if (mIsEnabledClearBk && !mBgEnabledAppsList.contains(pkg)) {
                if (DBG_IGNORE_CONTROLLING) {
                    Slog.d(TAG, "a disallowed running background app! Don't need to set firewall rule for it.");
                }
                return;
            }
        }

        // Step 6. Ok. Now we have checked everything. Go to set firewall rule.
        setFirewallRuleForAppIfNeeded(uid, allow);
    }

    private void updateFirewallRuleForApp(int uid, boolean allow) {
        updateFirewallRuleForApp(null, uid, allow);
    }
    
    private void updateFirewallRuleForApp(String pkg, boolean allow) {
        final int currentUserId = mActivityManager.getCurrentUser();
        
        if (DBG) {
            Slog.d(TAG, "updateFirewallRuleForApp() pkg=" + pkg + ", currentUserId=" + currentUserId);
        }
        try {
            int uid = mPackageManager.getPackageUid(pkg, currentUserId);
            updateFirewallRuleForApp(pkg, uid, allow);
        } catch (NameNotFoundException e) {
            Slog.e(TAG, "get package uid failed! " + e);
        }
    }
    
    private void updateFirewallRuleForApps() {
        Slog.d(TAG, "updateFirewallRuleForApps()...");

        // Step 0. check if device is in non-interactive state.
        if (!mNonInteractive) {
            Slog.d(TAG, "Device is in interactive state. Give up.");
            return;
        }
        
        // Step 1. clear firwall rule if needed.
        clearFirewallRuleForAppsIfNeeded();

        // Step 2. get data related to clear bg service
        if (USE_DATA_FROM_CLEAR_BK_SERVICE) {
            mIsEnabledClearBk = Settings.System.getInt(getContext().getContentResolver(), 
                PURE_BG_OPEN_STATUS, DEFAULT_BG_OPEN_ENABLED ? 1 : 0) == 1;
            if (mNeedRetrieveBgEnabledAppsListAgain) {
                getEnabledAppsListFromClearBkProvider(mBgEnabledAppsList);
                mNeedRetrieveBgEnabledAppsListAgain = false;
            }
        } else {
            mIsEnabledClearBk = false;
            mBgEnabledAppsList.clear();
        }

        // Step 3. get running processes.        
        final List<RunningAppProcessInfo> runningAppProcesses = mActivityManager.getRunningAppProcesses();
        if (runningAppProcesses == null) {
            if (DBG) {
                Slog.d(TAG, "There aren't any running app processes");
            }
            //Nothing to do.
            return;
        }

        // Step 4. set firewall rule for each running app.
        final SparseBooleanArray uniqueUidArray = new SparseBooleanArray();
        for (RunningAppProcessInfo proc : runningAppProcesses) {
            if (DBG) {
                StringBuilder sb = new StringBuilder();
                sb.append("process ")
                .append(proc.processName)
                .append(" [pid ")
                .append(proc.pid)
                .append(" uid ")
                .append(proc.uid);
                if (proc.pkgList != null) {
                    sb.append(" pkgList=[ ");
                    for (String pkg : proc.pkgList) {
                        sb.append(pkg);
                        sb.append(" ");
                    }
                    sb.append("]]");
                } else {
                    sb.append("]");
                }

                Slog.d(TAG, sb.toString());
            }

            // 0. Check if this uid were already processed. 
            //    We will clear this array when screen becomes on.            
            synchronized(mLock) {
                if (mProcessedUids.get(proc.uid) == 1) {
                    continue;
                } else {
                    mProcessedUids.put(proc.uid, 1);
                }
            }
            
            // 1. Only consider app process.
            if (!UserHandle.isApp(proc.uid)) {
                continue;
            }
            
            // 2. pkgList of almost apps only has one package in it, so
            //    we make a optimization that ignoring null pkgList or which more than
            //    one package in.
            if (proc.pkgList == null || proc.pkgList.length > 1) {
                continue;
            }           
            
            // 3. We needn't set firewall rule for same uid again.
            if (uniqueUidArray.get(proc.uid)) {
                continue;
            }

            // 4. En... Everything seems ok!  Let us do it now.
            
            // the package in pkgList owns proc.uid. So we pass them together.
            // Note we pass them based on second pacakge-list checking!!!
            updateFirewallRuleForApp(proc.pkgList[0], proc.uid, false);
            
            // 5. Record the uid state
            uniqueUidArray.put(proc.uid, true);
        }
        
    }
    
    private void handleTimeoutMessage() {
        if (DBG) {
            Slog.d(TAG, "handleTimeoutMessage()...");
        }

        updateFirewallRuleForApps();
        //Perhaps current device is in non-interactive state. So we register if needed.
        registerUidObserver();
    }
    
    private final class MyBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            
            if (Intent.ACTION_SCREEN_ON.equals(action)) {
                Slog.d(TAG, "onReceive(). ACTION_SCREEN_ON");
                screenOn();
            } else if (Intent.ACTION_SCREEN_OFF.equals(action)) {
                Slog.d(TAG, "onReceive(). ACTION_SCREEN_OFF");
                screenOff();
            } else if (Intent.ACTION_TIME_TICK.equals(action)) {
                if (DBG) {
                    Slog.d(TAG, "onReceive(). ACTION_TIME_TICK");
                }
                if (mNonInteractive && mNonInteractiveStartTime > 0 && !mHasSentInstantMsg) {
                    final long now = SystemClock.elapsedRealtime();
                    // if the specified time is up, and there have time-out messages in handler
                    // ok, we should perform controlling action immediately.
                    if (now -  mNonInteractiveStartTime > TIMEOUT_AFTER_SCREEN_OFF
                            && mHandler.hasMessages(TIMEOUT_MESSAGE)) {
                        Slog.d(TAG, "Time out. Perform contorlling app net immediately through hander.");
                        mHandler.removeMessages(TIMEOUT_MESSAGE);
                        mHandler.sendMessage(mHandler.obtainMessage(TIMEOUT_MESSAGE));
                        mHasSentInstantMsg = true;
                    }
                }
            }
        }
    }
    
    private final class MyHandler extends Handler {
        public MyHandler(Looper looper) {
            super(looper);
        }
        
        @Override
        public void handleMessage(Message msg) {
            if (DBG) {
                Slog.d(TAG, "handleMessage(). what=" + msg.what);
            }
            
            switch (msg.what) {
            case CONTENT_CHANGED_MESSAGE: {
                handleContentChanged((int)msg.obj);
                break;
            }
            case TIMEOUT_MESSAGE: {
                handleTimeoutMessage();
                break;
            }
            case PROC_UID_OBSERVER_MESSAGE: {
                SparseIntArray pendingUids;
                synchronized(mLock) {
                    if (!mNonInteractive) {
                        if (DBG) {
                            Slog.d(TAG, "Interactive state! clear pending array and return!");
                        }
                        mPendingUids.clear();
                        return;
                    }
                    
                    pendingUids = mPendingUids.clone();
                    mPendingUids.clear();
                }
                
                Slog.d(TAG, "Process pending uids. size=" + pendingUids.size());
                for (int i = 0; i < pendingUids.size(); ++i) {
                    updateFirewallRuleForApp(pendingUids.keyAt(i), false);
                }
            }
            }
        }
    }

    private final class MyContentObserver extends ContentObserver {
        private final int type;
        private final Integer object;
        public MyContentObserver(Handler handler, int observerType) {
            super(handler);
            type = observerType;
            object = new Integer(observerType);
        }

        @Override
        public void onChange(boolean selfChange) {        
            if (DBG) {
                Slog.d(TAG, "onChange(). type=" + typeToString(type));
            }

            boolean shouldNotify = false;
            switch (type) {
            case OBSERVER_TYPE_CONTROL_SWITCH_STATUS: {
                final boolean enabled = Settings.System.getInt(
                        getContext().getContentResolver(), Settings.System.PRIZE_CONTROL_APP_NETWORK_STATE, DEFAULT_ENABLED ? 1 : 0) == 1;
                if (DBG) {
                    Slog.d(TAG, "onChange(). mEnableControlAppNetwork=" + mEnableControlAppNetwork + ", enabled=" + enabled);
                }

                if (mEnableControlAppNetwork != enabled) {
                    mEnableControlAppNetwork = enabled;
                    shouldNotify = true;
                }
                
                break;
            }
            case OBSERVER_TYPE_BG_ENABLE_LIST: {
                shouldNotify = true;
                break;
            }  
            }

            if (shouldNotify) {
                if (DBG) {
                    Slog.d(TAG, "Delay "+ TIMEOUT_TO_SEND_CONTENT_CHANGED_EVENT + " ms to send message for content changed");
                }                
                mHandler.removeMessages(CONTENT_CHANGED_MESSAGE, object);
                mHandler.sendMessageDelayed(
                    mHandler.obtainMessage(CONTENT_CHANGED_MESSAGE, object), TIMEOUT_TO_SEND_CONTENT_CHANGED_EVENT);
            }
        }
    }

    private final class ProcUidObserver extends IUidObserver.Stub {
        private final Handler mHandler;
        private final int mMsgId;
        
        public ProcUidObserver(Handler handler, int messageId) {
            mHandler = handler;
            mMsgId = messageId;
        }
        
        @Override
        public void onUidStateChanged(int uid, int procState) {
            // when this callback is called, It means this uid becomes live. 
            // So we need to check if it should be in-controlling.
            boolean sendMsg = false;

            if (DBG) {
                Slog.d(TAG, "onUidStateChanged() uid=" + uid + ", procState=" + procState);
            }

            if (!UserHandle.isApp(uid)) {
                return;
            }
            
            synchronized(mLock) {
                if (mProcessedUids.get(uid) == 1) {
                    return;
                } else {
                    mProcessedUids.put(uid, 1);
                }

                if (DBG) {
                    Slog.d(TAG, "onUidStateChanged() mPendingUids.size()=" + mPendingUids.size());
                }
                
                if (mPendingUids.size() == 0) {
                    mPendingUids.put(uid, 1);
                    sendMsg = true;
                } else if (mPendingUids.get(uid) != 1) {
                    mPendingUids.put(uid, 1);
                } else if (DBG) {
                    Slog.d(TAG, "onUidStateChanged() This uid is already in pending array!");
                }
            }

            if (sendMsg) {
                if (DBG) {
                    Slog.d(TAG, "Delay 10 second to send message for uid changed");
                }
                mHandler.sendMessageDelayed(mHandler.obtainMessage(mMsgId), 10000);
            }
        }

        @Override
        public void onUidGone(int uid) {
        }

        @Override
        public void onUidActive(int uid) {
        }

        @Override
        public void onUidIdle(int uid) {
        }        
    }
}
