package com.prize.smartcleaner;

import android.app.ActivityManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.PowerManager;
import android.widget.Toast;

import com.prize.smartcleaner.bean.UserIdPkg;
import com.prize.smartcleaner.utils.CommonUtil;
import com.prize.smartcleaner.utils.LogUtils;
import com.prize.smartcleaner.utils.PrizeClearUtil;

import java.util.ArrayList;

public class PrizeClearSystemService extends Service {

    public static final String TAG = "PrizeClearSystemService";

    public static final String UPDATE_FILTER_LIST = "com.prize.android.service.UPDATE_FILTER_LIST";
    public static final String SCREEN_OFF_CLEAR = "com.prize.android.service.SCREEN_OFF_PROCESS_CLEAR";
    public static final String SCREEN_OFF_DELAY_CLEAR = "com.prize.android.service.SCREEN_OFF_DELAY_CLEAR";
    public static final String MSSTART = "com.prize.android.service.MSSTART";
    public static final String DELAYSTART = "com.prize.android.service.DELAYSTART";
    public static final String REQUEST_APP_CLEAN_RUNNING = "prize.intent.action.REQUEST_APP_CLEAN_RUNNING";

    private Context mContext;
    private PowerManager mPowerMgr;
    private PrizeClearProcessManager mClearProcessMgr;
    private PrizeClearRunningManager mClearRunningMgr;
    public static boolean mSupportMorningClean = true;
    public static boolean mSupportScreenOffClean = false;

    public IBinder onBind(Intent intent) {
        return null;
    }

    public void onCreate() {
        super.onCreate();
        init();
    }

    private void init() {
        mContext = getApplicationContext();
        mPowerMgr = (PowerManager) getSystemService(Context.POWER_SERVICE);
        mPowerMgr.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, this.getClass().getName()).acquire(200);
        mClearProcessMgr = PrizeClearProcessManager.getInstance();
        mClearRunningMgr = PrizeClearRunningManager.getInstance();
        /**
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Intent.ACTION_SCREEN_ON);
        intentFilter.addAction(Intent.ACTION_SCREEN_OFF);
        intentFilter.setPriority(IntentFilter.SYSTEM_HIGH_PRIORITY);//set the priority high
        registerReceiver(mBroadcastReceiver, intentFilter);
        **/
    }

    /**
    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            LogUtils.d(TAG, "onReceive action:" + action);
            if (Intent.ACTION_SCREEN_OFF.equals(action)) {
                LogUtils.d(TAG, "SCREEN OFF BROADCAST RECEIVER!!");
                if (mSupportScreenOffClean) {
                    PrizeClearUtil.setScreenOffClearAlarm(context);
                }
            } else if (Intent.ACTION_SCREEN_ON.equals(action)) {
                if (mSupportScreenOffClean) {
                    PrizeClearUtil.cancelScreenOffClearAlarm(context);
                }
            }
        }
    };
    **/

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        LogUtils.d(TAG, "PrizeClearSystemService onStartCommand!!!!");
        if (intent != null) {
            String action = intent.getAction();
            LogUtils.d(TAG, "PrizeClearSystemService onStartCommand action: " + action);

//            new Thread(new Runnable() {
//                @Override
//                public void run() {
//                    CommonUtil.getNeedKillAudioList(PrizeClearSystemService.this);
//                }
//            }).start();

            if (UPDATE_FILTER_LIST.equals(action)) {
                LogUtils.d(TAG, "start update filter list");
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        PrizeClearFilterManager.getInstance().initSysClearAppFilterList(mContext);
                    }
                }).start();
            } else if (DELAYSTART.equals(action) || MSSTART.equals(action)) {
                mPowerMgr.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "MorningClear").acquire(30000);
                if (action != null && action.equals(MSSTART)) {
                    PrizeClearUtil.setMorningClearAlarm(mContext);
                }
                mClearProcessMgr.init(mContext);
                mClearProcessMgr.prepareToKillProcess();

            } else if (SCREEN_OFF_DELAY_CLEAR.equals(action)) {
                if (PrizeClearUtil.isUserAMonkey()) {
                    LogUtils.d(TAG, "monkey running, return");
                } else if (PrizeClearUtil.isMorningCleanning()) {
                    LogUtils.d(TAG, "morning clear running, return");
                } else {
                    mClearRunningMgr.initDataFromBundle(mContext, null, null, null);
                    mClearRunningMgr.sendStartDelayClearMsg();
                }
            }

            /* else if (SCREEN_OFF_CLEAR.equals(action)) {

                mClearProcessMgr.init(mContext);
                mClearProcessMgr.startKillProcessByScreenOff();

            }*/ else if (REQUEST_APP_CLEAN_RUNNING.equals(action)) {
                if (PrizeClearUtil.isUserAMonkey()) {
                    LogUtils.d(TAG, "monkey running, return");
                } else if (PrizeClearUtil.isMorningCleanning()) {
                    LogUtils.d(TAG, "morning clear running, return");
                } else {
                    Bundle extras = intent.getExtras();
                    if (PrizeClearUtil.isServiceRunning()) {
                        boolean isLearningClearing = false;
                        if (extras != null && extras.getBoolean("isLearningClearing", false)) {
                            isLearningClearing = true;
                        }
                        if (!(isLearningClearing || mClearRunningMgr.mClearThreadRunning)) {
                            PrizeClearUtil.sendBroadcastFinishRemoveTask(mContext);
                            new Handler(Looper.getMainLooper()).post(new Runnable() {
                                @Override
                                public void run() {
                                    CharSequence string = mContext.getResources().getString(R.string.memery_clean_empty_result);
                                    Toast.makeText(mContext, string, Toast.LENGTH_SHORT).show();
                                    LogUtils.d(TAG, "clear thread has finished removing task, show message and return");
                                }
                            });
                        }
                        LogUtils.d(TAG, "clear is running, return");
                    } else {
                        int uid = 0;
                        int userId;
                        int appIndex;
                        UserIdPkg userIdPkg;
                        String packageName;
                        ArrayList<String> filterApplist = intent.getStringArrayListExtra("filterapplist");
                        if (extras == null || !extras.containsKey("KillAppFilterName")) {
                            packageName = null;
                        } else {
                            String filterPackageName = extras.getString("KillAppFilterName");
                            LogUtils.d(TAG, "onStartCommand, filter package:" + filterPackageName);
                            packageName = filterPackageName;
                        }
                        if (extras == null || !extras.containsKey("KillAppFilterUserId")) {
                            userId = 0;
                        } else {
                            userId = extras.getInt("KillAppFilterUserId");
                            LogUtils.d(TAG, "onStartCommand, filter package userId=" + userId);
                        }
                        //app twins
                        if (extras == null || !extras.containsKey("KillAppFilterAppIndex")) {
                            appIndex = 0;
                        } else {
                            appIndex = extras.getInt("KillAppFilterAppIndex");
                            LogUtils.d(TAG, "onStartCommand, filter package appIndex=" + appIndex);
                        }
                        if (packageName != null) {
                            try {
                                uid = mContext.getPackageManager().getPackageUidAsUser(packageName, userId);
                            } catch (PackageManager.NameNotFoundException e) {
                                LogUtils.w(TAG, "can't find package " + packageName);
                            }
                            LogUtils.d(TAG, "onStartCommand, filter package uid=" + uid);
                            userIdPkg = new UserIdPkg(uid, userId, packageName, appIndex);
                        } else {
                            userIdPkg = null;
                        }
                        if (extras != null && extras.containsKey("isLearningClearing")) {
                            boolean isLearningClearing = extras.getBoolean("isLearningClearing");
                            LogUtils.i(TAG, "onStartCommand, isLearningClearing:" + isLearningClearing);
                            if (isLearningClearing && PrizeClearUtil.isScreenOn(mContext)) {
                                PrizeClearUtil.forceStopSelf(mContext);
                            }
                        }
                        mClearRunningMgr.initDataFromBundle(mContext, extras, userIdPkg, filterApplist);
                        mClearRunningMgr.sendStartClearMsg();
                    }
                }
            }
        } else {
            LogUtils.e(TAG, "onStartCommand intent == null");
        }
        return START_NOT_STICKY;
    }
}
