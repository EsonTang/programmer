package com.prize.smartcleaner;

import android.app.ActivityManager;
import android.app.ActivityManager.RecentTaskInfo;
import android.app.ActivityManager.RunningTaskInfo;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IDeviceIdleController;
import android.os.Message;
import android.os.ServiceManager;
import android.app.StatusBarManager;
import android.os.RemoteException;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.os.storage.StorageEventListener;
import android.os.storage.StorageManager;
import android.text.format.Formatter;

import com.prize.smartcleaner.utils.CommonUtil;
import com.prize.smartcleaner.utils.LogUtils;
import android.widget.Toast;
import com.prize.smartcleaner.bean.UserIdPkg;
import com.prize.smartcleaner.utils.PrizeClearUtil;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import com.mediatek.common.prizeoption.PrizeOption;
import android.util.PrizeAppInstanceUtils;
import android.app.ActivityManagerNative;

/**
 * Created by xiarui on 2018/1/3.
 */


public class PrizeClearRunningManager {

    public static final String TAG = "PrizeClearRunningManager";

    private static PrizeClearRunningManager mClearRunningManager = null;
    private static String mStorageOldState = Environment.MEDIA_MOUNTED;
    private static String mStorageNewState = Environment.MEDIA_MOUNTED;
    private ActivityManager mActivityMgr;
    private StatusBarManager mStatusBarMgr;
    private PackageManager mPackageMgr;
    private boolean mIsLearningClearing = false;
    private UserIdPkg mUserIdPkg = null;
    private ArrayList<String> mFilterApplist = null;
    private boolean mHasForcetrim = false;//SystemProperties.getBoolean("prize.clear.forcetrim", true);
    private boolean mHasTrashclean = false;//SystemProperties.getBoolean("prize.clear.trashclean", true);
    private Handler H = new PrizeClearHandler();
    public boolean mClearThreadRunning = false;
    private StorageEventListener mPrizeStorageEventListener = new PrizeStorageEventListener();
    private Context mContext;
    private boolean mCleanRunning = false;
    private StorageManager mStorageMgr = null;
    private boolean mShowRebootToast = false;
    private boolean mCameraRunning = false;
    private boolean mShowCleanFinishToast = true;
    private boolean mCleanTrash = true;
    private boolean mClearLock = false;
    private boolean mClearTask = true;
    private boolean mClearFront = false;
    private boolean mClearSystem = true;
    private boolean mScreenOnLowMemClear = false;
    private String mKillAppFilterActivityName = null;
    private int mKillAppFilterAppIndex = 0;

    public static final int START_DELAY_CLEAN_RUNNING = 2000;
    public static final int START_CLEAN_RUNNING = 2003;
    public static final int SHOW_CLEAN_RESULT = 1003;
    public static final int CLEAN_ALL_RUNNING_FINISH = 3003;
    public static final int FORCE_STOP_SELF = 5003;

    public static final PrizeClearRunningManager getInstance() {
        if (mClearRunningManager == null) {
            mClearRunningManager = new PrizeClearRunningManager();
        }
        return mClearRunningManager;
    }

    public void initDataFromBundle(Context context, Bundle bundle, UserIdPkg userIdPkg, ArrayList<String> filterApplist) {
        mContext = context;
        mActivityMgr = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        mStatusBarMgr = (StatusBarManager) mContext.getSystemService(Context.STATUS_BAR_SERVICE);
        mPackageMgr = context.getPackageManager();
        if (mStorageMgr == null) {
            mStorageMgr = (StorageManager) context.getSystemService(Context.STORAGE_SERVICE);
            mStorageMgr.registerListener(mPrizeStorageEventListener);
        }
        if (bundle != null) {
            mIsLearningClearing = bundle.getBoolean("isLearningClearing", false);
            mShowCleanFinishToast = bundle.getBoolean("IsShowCleanFinishToast", true);
            mScreenOnLowMemClear = bundle.getBoolean("screenOnLowMemClear", false);
            mCleanTrash = bundle.getBoolean("clean_trash", true);
            mClearLock = bundle.getBoolean("clear_lock", false);
            mClearTask = bundle.getBoolean("clear_task", true);
            mClearFront = bundle.getBoolean("clear_front", false);
            mClearSystem = bundle.getBoolean("clear_system", true);
            mKillAppFilterActivityName = bundle.getString("KillAppFilterActivityName");
            mKillAppFilterAppIndex = bundle.getInt("KillAppFilterAppIndex", 0);
        } else {
            mIsLearningClearing = false;
            mShowCleanFinishToast = true;
            mScreenOnLowMemClear = false;
            mCleanTrash = true;
            mClearLock = false;
            mClearTask = true;
            mClearFront = false;
            mClearSystem = true;
            mKillAppFilterActivityName = null;
            mKillAppFilterAppIndex = 0;
        }
        LogUtils.d(TAG, "learn=" + mIsLearningClearing
                + ",toast=" + mShowCleanFinishToast
                + ",screenon=" + mScreenOnLowMemClear
                + ",trash=" + mCleanTrash
                + ",lock=" + mClearLock
                + ",task=" + mClearTask
                + ",front=" + mClearFront
                + ",system=" + mClearSystem);
        mUserIdPkg = userIdPkg;
        mFilterApplist = filterApplist;
    }

    private void unregisterStorageEventListener() {
        if (mStorageMgr != null) {
            mStorageMgr.unregisterListener(mPrizeStorageEventListener);
        }
    }

    public void sendStartClearMsg() {
        if (!mCleanRunning) {
            mCleanRunning = true;
            PrizeClearUtil.setClearRunningAppServiceRunning(true);
            if (!mIsLearningClearing) {
                SystemProperties.set("prize.clear.running", "1");
            }
            H.sendEmptyMessageDelayed(START_CLEAN_RUNNING, 0);
        }
    }

    public void sendStartDelayClearMsg() {
        if (!mCleanRunning) {
            mCleanRunning = true;
            PrizeClearUtil.setClearRunningAppServiceRunning(true);
            if (!mIsLearningClearing) {
                SystemProperties.set("prize.clear.running", "1");
            }
            H.sendEmptyMessageDelayed(START_DELAY_CLEAN_RUNNING, 0);
        }
    }

    private void showMountingToast() {
        CharSequence string = mContext.getResources().getString(R.string.mounting_internal_storage);
        Toast.makeText(mContext, string, Toast.LENGTH_SHORT).show();
    }

    private void showCleanResultToast(int count, long size) {
        if (mShowCleanFinishToast) {
            String formatFileSize = Formatter.formatFileSize(mContext, size);
            CharSequence format = String.format(mContext.getResources().getString(R.string.memery_clean_result), new Object[]{PrizeClearUtil.formatCount(count), formatFileSize});
            if (count == 0) {
                format = mContext.getResources().getString(R.string.memery_clean_empty_result);
            }
            if (count != 0 && size == 0) {
                format = String.format(mContext.getResources().getString(R.string.memery_clean_data_empty_result), new Object[]{PrizeClearUtil.formatCount(count)});
            }
            if (mShowRebootToast) {
                format = mContext.getResources().getString(R.string.memery_clean_result_notify_reboot);
            }
            Toast.makeText(mContext, format, Toast.LENGTH_SHORT).show();
        }
        unregisterStorageEventListener();
    }

    public void clearStatusBarBackgroundColor(String packageName){
        if (mStatusBarMgr != null) {
            mStatusBarMgr.clearStatusBarBackgroundColor(packageName);
        }
    }

    private void forceStopPackageAsUser(String pkg, int userId, int appInstanceIndex) {
        LogUtils.d(TAG, "forceStop pkg [" + pkg + "]" + ", userId=" + userId);
        if (PrizeOption.PRIZE_APP_MULTI_INSTANCES && PrizeAppInstanceUtils.getInstance(mContext).supportMultiInstance(pkg)) {
            try {
                clearStatusBarBackgroundColor(pkg);
                ActivityManagerNative.getDefault().forceStopPackage(pkg, appInstanceIndex, UserHandle.myUserId());
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        } else {
            clearStatusBarBackgroundColor(pkg);
            mActivityMgr.forceStopPackage(pkg);
        }
        //mActivityMgr.forceStopPackageAsUser(pkg, userId);
    }

    private void forceStopPackageAsUser(String pkg, int userId) {
        LogUtils.d(TAG, "forceStop pkg [" + pkg + "]" + ", userId=" + userId);
        clearStatusBarBackgroundColor(pkg);
        mActivityMgr.forceStopPackageAsUser(pkg, userId);
    }

    private void clearAllRunningFinish() {
        //Intent intent = new Intent("android.intent.action.FORCE_CLOSE_ALL_PROCESS");
        //intent.putExtra("isLearningClearing", mIsLearningClearing);
        //mContext.sendBroadcast(intent);
        mUserIdPkg = null;
        mFilterApplist = null;
        mCleanRunning = false;
        PrizeClearUtil.setClearRunningAppServiceRunning(false);
        SystemProperties.set("prize.clear.running", "0");
        Message msg = Message.obtain();
        msg.what = FORCE_STOP_SELF;
        H.sendMessageDelayed(msg, 500);
    }

    private ArrayList<RecentTaskInfo> getRecentTaskList() {
        return (ArrayList<RecentTaskInfo>) mActivityMgr.getRecentTasks(ActivityManager.getMaxRecentTasksStatic(), 5);
    }

    private ArrayList<Integer> getPersistendIdList() {
        ArrayList<Integer> list = new ArrayList();
        ArrayList<RecentTaskInfo> recentTasks = (ArrayList<RecentTaskInfo>) mActivityMgr.getRecentTasks(ActivityManager.getMaxRecentTasksStatic(), 2);
        if (!(recentTasks == null || recentTasks.isEmpty())) {
            Iterator it = recentTasks.iterator();
            while (it.hasNext()) {
                list.add(Integer.valueOf(((RecentTaskInfo) it.next()).persistentId));
            }
        }
        return list;
    }

    private void sendResultToastMsg(int count, long size) {
        Message msg = H.obtainMessage();
        msg.what = SHOW_CLEAN_RESULT;
        msg.arg1 = count;
        msg.obj = Long.toString(size);
        H.sendMessage(msg);
    }

    private String getTopPackageName() {
        String str = "null";
        List runningTasks = mActivityMgr.getRunningTasks(1);
        if (runningTasks == null || runningTasks.size() <= 0) {
            return str;
        }
        return ((RunningTaskInfo) runningTasks.get(0)).baseActivity.getPackageName();
    }


    class PrizeClearHandler extends Handler {

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case SHOW_CLEAN_RESULT:
                    long size;
                    if (msg.obj == null || !(msg.obj instanceof String)) {
                        size = 0;
                    } else {
                        try {
                            size = Long.parseLong((String) msg.obj);
                        } catch (NumberFormatException e) {
                            size = 0;
                        }
                    }
                    showCleanResultToast(msg.arg1, size);
                    return;
                case START_CLEAN_RUNNING:
                    if (Environment.MEDIA_MOUNTED.equals(mStorageOldState) && Environment.MEDIA_UNMOUNTED.equals(mStorageNewState)) {
                        LogUtils.w(TAG, "isSDCardFormating : true");
                        showMountingToast();
                        H.sendEmptyMessage(CLEAN_ALL_RUNNING_FINISH);
                        return;
                    }
                    new ClearRunningThread().start();
                    return;
                case START_DELAY_CLEAN_RUNNING:
                    if (Environment.MEDIA_MOUNTED.equals(mStorageOldState) && Environment.MEDIA_UNMOUNTED.equals(mStorageNewState)) {
                        LogUtils.w(TAG, "isSDCardFormating : true");
                        showMountingToast();
                        H.sendEmptyMessage(CLEAN_ALL_RUNNING_FINISH);
                        return;
                    }
                    new DelayClearThread().start();
                    return;
                case CLEAN_ALL_RUNNING_FINISH:
                    LogUtils.e(TAG, "CLEAE_ALL_RUNNING_FINISH");
                    clearAllRunningFinish();
                    return;
                case FORCE_STOP_SELF:
                    if (mHasTrashclean && mCleanTrash && PrizeClearUtil.isManuClearTrashing(mContext)) {
                        PrizeClearUtil.sendBroadcastForTrashClean(mContext, 2);
                    }
                    mIsLearningClearing = false;
                    if (!PrizeClearUtil.isServiceRunning()) {
                        PrizeClearUtil.forceStopSelf(mContext);
                        return;
                    }
                    return;
                default:
                    return;
            }
        }
    }


    class ClearRunningThread extends Thread {

        @Override
        public void run() {
            mClearThreadRunning = true;
            ArrayList<RecentTaskInfo> recentTaskList = getRecentTaskList();
            long availMem = PrizeClearUtil.getAvailMem();
            int count = 0;
            int size = recentTaskList.size();
            if (mShowCleanFinishToast && size == 0) {
                LogUtils.d(TAG, "recent task is 0, show message to user");
                mClearThreadRunning = false;
                PrizeClearUtil.sendBroadcastFinishRemoveTask(mContext);
                sendResultToastMsg(0, 0);
                H.sendEmptyMessage(CLEAN_ALL_RUNNING_FINISH);
                return;
            }
            int userId;
            int appIndex;
            UserIdPkg userIdPkg;
            boolean isSystemApp;
            ArrayList<UserIdPkg> lockedAppList;
            //if (availMem < (PrizeClearUtil.getThreshClearPersist(mContext) * 1024) * 1024) {
            //    return;
            //}
            PrizeClearFilterManager clearFilterMgr = PrizeClearFilterManager.getInstance();
            String topActivityPackageName = PrizeClearUtil.getTopActivityPackageName(mActivityMgr);
            ArrayList<String> launcher = PrizeClearUtil.getLauncherList(mContext);
            ArrayList<String> keyguard = PrizeClearUtil.getKeyguard(mContext);
            ArrayList<String> inputMethod = PrizeClearUtil.getDefInputMethodList(mContext);
            ArrayList<String> wallpaper = PrizeClearUtil.getWallpaperInfo(mContext);
            ArrayList<String> forceStopFilterPkgList = clearFilterMgr.getFilterListFromSP(mContext, PrizeClearUtil.FORCE_STOP);//PrizeClearUtil.getForceStopFilterPkgList(mContext);
            ArrayList<UserIdPkg> lockApps = PrizeClearUtil.getLockedAppsList(mContext);
            if (mClearLock) {
                lockedAppList = new ArrayList();
            } else {
                lockedAppList = lockApps;
            }
            ArrayList<String> pkgFilterList = clearFilterMgr.getFilterListFromSP(mContext, PrizeClearUtil.PACKAGE);
            ArrayList<String> processFilterList = clearFilterMgr.getFilterListFromSP(mContext, PrizeClearUtil.PROCESS);
            String topPackageName = getTopPackageName();
            ArrayList<Integer> uidList = new ArrayList();
            PackageManager packageManager = mContext.getPackageManager();
            ArrayList<UserIdPkg> userIdPkgList = new ArrayList();
            if (mScreenOnLowMemClear && topActivityPackageName.equals("com.android.recents")) {
                mClearTask = false;
            }
            if (mClearTask) {
                ArrayList<Integer> persistendIdList = getPersistendIdList();
                int index = 0;
                while (index < size) {
                    int sum;
                    RecentTaskInfo recentTaskInfo = recentTaskList.get(index);
                    String packageName = recentTaskInfo.baseIntent.getComponent().getPackageName();
                    String className = recentTaskInfo.baseIntent.getComponent().getClassName();
                    userId = recentTaskInfo.userId;
                    appIndex = recentTaskInfo.appInstanceIndex;
                    int uid = 0;
                    try {
                        uid = packageManager.getPackageUidAsUser(packageName, userId);
                    } catch (PackageManager.NameNotFoundException e) {
                        LogUtils.e(TAG, "error get uid for " + packageName);
                    }
                    userIdPkg = new UserIdPkg(uid, userId, packageName, appIndex);
                    boolean canBeRemove = !(lockedAppList.contains(userIdPkg));
                    if ((mFilterApplist == null || !mFilterApplist.contains(packageName))
                            && (pkgFilterList == null || !pkgFilterList.contains(packageName))) {
                        if (PrizeClearUtil.isPhoneInCall(mContext) && packageName.equals("com.android.incallui")) {
                            canBeRemove = false;
                        }
                        if (keyguard.contains(packageName) || launcher.contains(packageName) || inputMethod.contains(packageName)) {
                            canBeRemove = false;
                        }
                    } else {
                        canBeRemove = false;
                    }

                    if (className != null && mKillAppFilterActivityName != null && mKillAppFilterActivityName.equals(className) && mKillAppFilterAppIndex == appIndex) {
                        LogUtils.d(TAG, "filter activity name = " + className);
                        canBeRemove = false;
                    }

                    boolean isRedundent = false;
                    if (className != null) {
                        isRedundent = PrizeClearUtil.isRedundent(className);
                    }

                    if (!canBeRemove && !isRedundent) {
                        sum = count;
                    } else {
                        mActivityMgr.removeTask(recentTaskInfo.persistentId);
                        LogUtils.d(TAG, "RemoveTask Pkg =" + packageName + ", UserId=" + userId + ", Redundent=" + isRedundent);
                        if (!(forceStopFilterPkgList.contains(packageName)
                                || PrizeClearFilterManager.getInstance().isFilterService(mContext, packageName)
                                || wallpaper.contains(packageName)
                                || isRedundent
                                //|| PrizeClearUtil.isInFilterList(packageName, AppConstant.whiteList)
                        )) {
                            LogUtils.e(TAG, "ForceStopPackage Pkg =  " + packageName + " for remove task");
                            forceStopPackageAsUser(packageName, 0, recentTaskInfo.appInstanceIndex);
                            userIdPkgList.add(userIdPkg);
                        }
                        if (persistendIdList.contains(Integer.valueOf(recentTaskInfo.persistentId))) {
                            sum = count + 1;
                        } else {
                            sum = count;
                        }
                        if (uid >= 10000) {
                            uidList.add(Integer.valueOf(uid));
                        }
                    }
                    index++;
                    count = sum;
                }
            }
            mClearThreadRunning = false;
            if (mShowCleanFinishToast) {
                PrizeClearUtil.sendBroadcastFinishRemoveTask(mContext);
            }
            long spareMem = PrizeClearUtil.getAvailMem() - availMem;
            if (spareMem < 0 || count == 0) {
                spareMem = 0;
            }
            LogUtils.d(TAG, "remove task, show user message!");
            sendResultToastMsg(count, spareMem);


            ArrayList<Integer> orphanUidList = PrizeClearUtil.getOrphanUidList(mContext, uidList);
            if (!(orphanUidList == null || orphanUidList.isEmpty())) {
                for (int i = 0; i < orphanUidList.size(); i++) {
                    LogUtils.d(TAG, "kill native orphan process = " + orphanUidList.get(i));
                    PrizeClearUtil.killProcess((orphanUidList.get(i)).intValue(), "native orphan");
                }
            }
            SystemProperties.set("prize.clear.running", "0");
            if ("com.android.camera".equals(topActivityPackageName)) {
                mCameraRunning = true;
            }
            ArrayList<String> importantProviderPkg = PrizeClearUtil.getImportantProviderPkg(mContext, topActivityPackageName);
            String processName = PrizeClearUtil.getProcessName(mContext, topActivityPackageName);
            ArrayList<ActivityManager.RunningAppProcessInfo> runningAppProcessesList = PrizeClearUtil.getRunningAppProcesses(mContext);
            int proSize = runningAppProcessesList.size();
            int proIndex = 0;
            while (proIndex < proSize) {
                boolean isKill = true;
                ActivityManager.RunningAppProcessInfo runningAppProcessInfo = runningAppProcessesList.get(proIndex);
                int length = runningAppProcessInfo.pkgList.length;
                String runProName = runningAppProcessInfo.processName;
                int pid = runningAppProcessInfo.pid;
                int uid = runningAppProcessInfo.uid;
                userId = UserHandle.getUserId(uid);
                if ((runningAppProcessInfo.flags & ActivityManager.RunningAppProcessInfo.FLAG_PERSISTENT) != 0) {
                    LogUtils.d(TAG, "skip persist " + runProName);
                } else {
                    if (PrizeClearUtil.mLocalProcessFilterList.contains(runProName)) {
                        LogUtils.d(TAG, "skip localClearFilterProcess=" + runProName);
                    } else if (!(mCameraRunning && "android.process.safer".equals(runProName))) {
                        for (int i = 0; i < length; i++) {
                            try {
                                ApplicationInfo applicationInfo = mPackageMgr.getApplicationInfo(runningAppProcessInfo.pkgList[i], PackageManager.GET_META_DATA);
                                if (applicationInfo != null) {
                                    boolean isPersistent = (applicationInfo.flags & ApplicationInfo.FLAG_PERSISTENT) != 0;
                                    String pkgName = applicationInfo.packageName;
                                    userIdPkg = new UserIdPkg(uid, userId, pkgName);
                                    boolean isLockedApp = lockedAppList.contains(userIdPkg);
                                    if (userIdPkgList.contains(userIdPkg)) {
                                        LogUtils.d(TAG, "duplicate packagename = " + pkgName + ", userid=" + userIdPkg.userId);
                                    } else if (forceStopFilterPkgList.contains(pkgName)) {
                                        LogUtils.d(TAG, "skip force stop filter package : " + pkgName);
                                    } else if (isPersistent) {
                                        LogUtils.d(TAG, "skip appmanager persist " + pkgName);
                                    } else if (mClearSystem || !PrizeClearUtil.isSystemApp(mContext, pkgName)) {
                                        if (!pkgName.contains("org.simalliance.openmobileapi") || !mIsLearningClearing) {
                                            if ((mUserIdPkg != null && userIdPkg.equals(mUserIdPkg))
                                                    || (mFilterApplist != null && mFilterApplist.contains(pkgName))
                                                    || (!mClearFront && pkgName.equals(topActivityPackageName))
                                                    || launcher.contains(pkgName)
                                                    || keyguard.contains(pkgName)
                                                    || inputMethod.contains(pkgName)
                                                    || (PrizeClearUtil.isPhoneInCall(mContext) && pkgName.equals("com.android.incallui"))
                                                    || pkgName.equals("com.android.server.telecom")
                                                    || wallpaper.contains(pkgName)
                                                    || (pkgFilterList != null && pkgFilterList.contains(pkgName))
                                                    || PrizeClearUtil.isSystemProviderPackage(mContext, pkgName)
                                                    || PrizeClearUtil.mServiceFilterList_Exp.contains(pkgName)
                                                    || processFilterList.contains(pkgName)
                                                    //|| PrizeClearUtil.isInFilterList(pkgName, AppConstant.whiteList)
                                                    ) {
                                                isKill = false;
                                            }
                                            LogUtils.e(TAG, "pid=" + pid
                                                    + "\t isKill=" + isKill
                                                    + "\t userId=" + userId
                                                    + "\t process=" + runProName
                                                    + "\t pkg=" + pkgName);
                                            if (!isLockedApp && isKill) {
                                                int killType = PrizeClearFilterManager.getInstance().getKillType(mContext, runProName, pkgName, isPersistent);
                                                if (killType == 2) {
                                                    forceStopPackageAsUser(pkgName, userId);
                                                    userIdPkgList.add(userIdPkg);
                                                } else if (killType == 1) {
                                                    PrizeClearUtil.killProcess(pid, runProName);
                                                } else if (killType == 0) {
                                                    LogUtils.d(TAG, "skip process " + runProName);
                                                }
                                            }
                                        } else {
                                            LogUtils.d(TAG, "learning clear skip simalliance packagename = " + pkgName);
                                        }
                                    } else {
                                        LogUtils.d(TAG, "skip system pkg = " + pkgName);
                                    }
                                }
                            } catch (PackageManager.NameNotFoundException e) {
                                LogUtils.w(TAG, "Error retrieving ApplicationInfo for pkg:" + runningAppProcessInfo.pkgList[size]);
                            }
                        }
                    }
                }
                proIndex++;
            }

            if (mHasForcetrim && PrizeClearUtil.isTrimMemoring(mContext)) {
                LogUtils.i(TAG, "finish regular clear, start force trim memory");
                mActivityMgr.removeTask(-1);
            }
            H.sendEmptyMessage(CLEAN_ALL_RUNNING_FINISH);
        }
    }

    class DelayClearThread extends Thread {
        @Override
        public void run() {
            PrizeClearFilterManager clearFilterMgr = PrizeClearFilterManager.getInstance();
            ArrayList<String> needKillAudioList = CommonUtil.getNeedKillAudioList(mContext);
            ArrayList<String> thirdWhiteList = clearFilterMgr.getFilterListFromSP(mContext, PrizeClearUtil.THIRD_WHITE_LIST);
            LogUtils.d(TAG, "needKillAudioList size : " + needKillAudioList.size());
            for (String pkg : needKillAudioList) {
                if (!thirdWhiteList.contains(pkg)) {
                    PrizeClearUtil.forceStopPackage(mContext, pkg);
                }
            }

            //kill all net & gps app ,not in dozewhitelist @{
            ArrayList<String> needKillNetList = TrafficUtil.getUsingNetPackages(mContext, 10);
            ArrayList<String> pkgFilterList = clearFilterMgr.getFilterListFromSP(mContext, PrizeClearUtil.PACKAGE);
            ArrayList<String> deepClearProcessList = clearFilterMgr.getFilterListFromSP(mContext, PrizeClearUtil.DEEP_CLEAR);
            if (needKillNetList != null && !needKillNetList.isEmpty()) {
                String[] dozeWhite = null;
                try {
                    IDeviceIdleController deviceIdleController = IDeviceIdleController.Stub.asInterface(
                            ServiceManager.getService(Context.DEVICE_IDLE_CONTROLLER));
                    dozeWhite = deviceIdleController.getFullPowerWhitelist();//.getUserPowerWhitelist();
                } catch (RemoteException e) {
                    LogUtils.i(TAG, "get doze error : " + e);
                }
                if (dozeWhite != null) {
                    ArrayList<String> dozeWhiteList = new ArrayList<>();
                    dozeWhiteList.clear();
                    for (String pkg : dozeWhite) {
                        dozeWhiteList.add(pkg);
                    }
                    if (LogUtils.DEBUG) {
                        for (int i = 0; i < needKillNetList.size(); i++) {
                            LogUtils.i(TAG, "UseNetList[" + needKillNetList.get(i) + "]");
                        }
                        for (int i = 0; i < dozeWhiteList.size(); i++) {
                            LogUtils.i(TAG, "DozeWhiteList[" + dozeWhiteList.get(i) + "]");
                        }
                    }
                    for (String pkg : needKillNetList) {
                        if (!dozeWhiteList.contains(pkg) && !pkgFilterList.contains(pkg) && !deepClearProcessList.contains(pkg) && !thirdWhiteList.contains(pkg)) {
                            PrizeClearUtil.forceStopPackage(mContext, pkg);
                        }
                    }
                }
            }
            //---end doze@}
            H.sendEmptyMessage(CLEAN_ALL_RUNNING_FINISH);
        }
    }

    class PrizeStorageEventListener extends StorageEventListener {

        @Override
        public void onStorageStateChanged(String path, String oldState, String newState) {
            LogUtils.i(TAG, "Received storage state changed notification that " + path + " changed state from " + oldState + " to " + newState);
            mStorageOldState = oldState;
            mStorageNewState = newState;
        }
    }
}

