package com.prize.smartcleaner;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Environment;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.os.storage.StorageEventListener;
import android.os.storage.StorageManager;

import com.prize.smartcleaner.utils.LogUtils;
import com.prize.smartcleaner.utils.PrizeClearUtil;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;

public class PrizeClearProcessManager {
    public static final String TAG = "PrizeClearProcessManager";
    private static ClearProcessThread mClearProcessThread;
    private static PrizeClearProcessManager mClearProcessManager = null;
    private static String mStorageOldState = Environment.MEDIA_MOUNTED;
    private static String mStorageNewState = Environment.MEDIA_MOUNTED;
    PrizeStorageEventListener mStorageEventListener = new PrizeStorageEventListener();
    private PendingIntent mPendingIntent;
    private Context mContext;
    private boolean mCameraRunning = false;
    private StorageManager mStorageMgr = null;
    private ArrayList<RunningAppProcessInfo> mPendingPersistentInfos = new ArrayList();
    private ArrayList<String> mCleanedProcessList = new ArrayList();

    public static final PrizeClearProcessManager getInstance() {
        if (mClearProcessManager == null) {
            mClearProcessManager = new PrizeClearProcessManager();
        }
        return mClearProcessManager;
    }

    public void init(Context context) {
        mContext = context;
        Intent intent = new Intent(context, PrizeClearSystemService.class);
        intent.setAction(PrizeClearSystemService.DELAYSTART);
        mPendingIntent = PendingIntent.getService(mContext, 0, intent, 0);
        if (mStorageMgr == null) {
            mStorageMgr = (StorageManager) context.getSystemService(Context.STORAGE_SERVICE);
            mStorageMgr.registerListener(mStorageEventListener);
        }
    }

    /**
     * start kill process by screen off
     */
    public void startKillProcessByScreenOff() {

        LogUtils.d(TAG, "start kill process by screen off");

        if (PrizeClearUtil.isPhoneInCall(mContext) || PrizeClearUtil.isScreenOn(mContext)) {
            LogUtils.d(TAG, "cancle kill process since phone busy and screen on");
            return;
        }
        if (PrizeClearUtil.isMorningCleanning() || PrizeClearUtil.isScreenOffCleanning()) {
            LogUtils.d(TAG, "cancle kill process since morning or screen off cleanning");
            return;
        }

        if (Environment.MEDIA_MOUNTED.equals(mStorageOldState) && Environment.MEDIA_UNMOUNTED.equals(mStorageNewState)) {
            LogUtils.w(TAG, "isSDCardFormating : true");
            return;
        }

        if (PrizeClearUtil.isAutoClearRunning() || PrizeClearUtil.isServiceRunning()) {
            LogUtils.w(TAG, "There is auto clear running, so cancel");
            return;
        }

        long before = PrizeClearUtil.getAvailMem(mContext);

        LogUtils.d(TAG, "before clear the mem is: " + before);

        LogUtils.d(TAG, "Start killing normal processes");

        PrizeClearUtil.setScreenOffClearFlag(true);
        mClearProcessThread = new ClearProcessThread(mContext, this);
        mClearProcessThread.start();
    }


    public void prepareToKillProcess() {
        LogUtils.d(TAG, "Prepare to kill process command!!");
        if (isInTimeRange()) {
            if (PrizeClearUtil.isPhoneInCall(mContext) || PrizeClearUtil.isScreenOn(mContext)) {
                LogUtils.d(TAG, "Delay 30 minutes since phone busy and screen on");
                clearProcessDelay();
                return;
            }
            long before = PrizeClearUtil.getAvailMem(mContext);

            LogUtils.d(TAG, "before clear the mem is: " + before);

            LogUtils.d(TAG, "Start killing normal processes");
            startClearProcess();
            return;
        }
        LogUtils.d(TAG, "Time is not is 2:00 to 5:30,so return");
        PrizeClearUtil.forceStopSelf(mContext);
    }

    private final boolean isInTimeRange() {
        long currentTimeMillis = System.currentTimeMillis();
        Calendar start = Calendar.getInstance();
        start.setTimeInMillis(currentTimeMillis);
        start.set(Calendar.HOUR_OF_DAY, 2);
        start.set(Calendar.MINUTE, 0);
        start.set(Calendar.SECOND, 0);
        start.set(Calendar.MILLISECOND, 0);
        long startTimeInMillis = start.getTimeInMillis();
        Calendar end = Calendar.getInstance();
        end.setTimeInMillis(currentTimeMillis);
        end.set(Calendar.HOUR_OF_DAY, 5);
        end.set(Calendar.MINUTE, 30);
        end.set(Calendar.SECOND, 0);
        end.set(Calendar.MILLISECOND, 0);
        long endTimeInMillis = end.getTimeInMillis();
        if (startTimeInMillis >= currentTimeMillis || currentTimeMillis >= endTimeInMillis) {
            return false;
        }
        return true;
    }


    private void clearProcessDelay() {
        AlarmManager alarmManager = ((AlarmManager) mContext.getSystemService(Context.ALARM_SERVICE));
        alarmManager.setExact(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + 1800000, mPendingIntent);
    }

    public void startClearProcess() {
        if (!PrizeClearUtil.isMorningCleanning()/* && !PrizeClearUtil.isScreenOffCleanning()*/) {
            if (Environment.MEDIA_MOUNTED.equals(mStorageOldState) && Environment.MEDIA_UNMOUNTED.equals(mStorageNewState)) {
                LogUtils.w(TAG, "isSDCardFormating : true");
                clearProcessDelay();
            } else if (PrizeClearUtil.isAutoClearRunning() || PrizeClearUtil.isServiceRunning()) {
                LogUtils.w(TAG, "There is auto clear running, so delay");
                clearProcessDelay();
            } else {
                PrizeClearUtil.setMorningClearFlag(true);
                mClearProcessThread = new ClearProcessThread(mContext, this);
                mClearProcessThread.start();
            }
        }
    }

    private void finishCleanProcess(int pid) {
        unregisterListener();
        //killSystemUI(mContext, pid);
        PrizeClearUtil.setScreenOffClearFlag(false);
        PrizeClearUtil.setMorningClearFlag(false);
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
        }
        if (PrizeClearUtil.isMorningClearTrashing(mContext)) {
            PrizeClearUtil.sendBroadcastForTrashClean(mContext, 1);
        }
        long after = PrizeClearUtil.getAvailMem(mContext);
        LogUtils.d(TAG, "after clear the mem is: " + after);
        if (!PrizeClearUtil.isAutoClearRunning()) {
            PrizeClearUtil.forceStopSelf(mContext);
        } else {
            LogUtils.d(TAG, "auto clear is also running, so don't kill!");
        }
    }

    private void unregisterListener() {
        if (mStorageMgr != null) {
            mStorageMgr.unregisterListener(mStorageEventListener);
        }
    }

    private void killSystemUI(Context context, int pid) {
        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        if (pid != 0) {
            LogUtils.d(TAG, "kill process:  com.android.systemui");
            PrizeClearUtil.killProcess(pid, "com.android.systemui");
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {

            }
        }
    }

    private boolean isPersistentProcess(RunningAppProcessInfo runningAppProcessInfo) {
        if (runningAppProcessInfo == null || (runningAppProcessInfo.flags & RunningAppProcessInfo.FLAG_PERSISTENT) == 0) {
            return false;
        }
        return true;
    }

    private void addProcessToCleanedList(RunningAppProcessInfo runningAppProcessInfo) {
        if (runningAppProcessInfo != null) {
            synchronized (mCleanedProcessList) {
                for (String pkg : runningAppProcessInfo.pkgList) {
                    if (!mCleanedProcessList.contains(pkg)) {
                        mCleanedProcessList.add(pkg);
                    }
                }
            }
        }
    }

    private boolean isInCleanedList(RunningAppProcessInfo runningAppProcessInfo) {
        boolean isInCleanedList = false;
        synchronized (mCleanedProcessList) {
            for (String pkg : runningAppProcessInfo.pkgList) {
                if (mCleanedProcessList.contains(pkg)) {
                    isInCleanedList = true;
                    break;
                }
            }
        }
        return isInCleanedList;
    }

    private void clear() {
        synchronized (mCleanedProcessList) {
            mCleanedProcessList.clear();
        }
    }


    class ClearProcessThread extends Thread {

        private Context mContext;
        private ActivityManager mActMgr;
        private PrizeClearProcessManager mClearProcessManager;

        public ClearProcessThread(Context context, PrizeClearProcessManager processMgr) {
            mContext = context;
            mClearProcessManager = processMgr;
            mActMgr = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        }

        @Override
        public void run() {
            ArrayList<RunningAppProcessInfo> appProcesses = PrizeClearUtil.getRunningAppProcesses(mContext);
            int size = appProcesses.size();
            String topPkg = PrizeClearUtil.getTopActivityPackageName(mActMgr);
            if ("com.mediatek.camera".equals(topPkg)) {
                mClearProcessManager.mCameraRunning = true;
            }
            int pidOfSystemUI = 0;
            int pidOfTencentMM = -1;
            int pidOfTencentMM2 = -1;

            ArrayList<String> launcher = PrizeClearUtil.getLauncherList(mContext);
            ArrayList<String> keyguard = PrizeClearUtil.getKeyguard(mContext);
            ArrayList<String> inputMethod = PrizeClearUtil.getDefInputMethodList(mContext);
            ArrayList<String> wallpaper = PrizeClearUtil.getWallpaperInfo(mContext);
            ArrayList<String> music = PrizeClearUtil.getActiveAudioPackage(mContext);
            ArrayList<String> location = PrizeClearUtil.getInUseLocationPkgList(mContext);
            ArrayList<String> net = TrafficUtil.getUsingNetPackages(mContext, 0);
            ArrayList<String> lockApps = PrizeClearUtil.getLockedAppsPkgList(mContext);
            ArrayList<String> pkgFilterList = PrizeClearFilterManager.getInstance().getFilterListFromSP(mContext, PrizeClearUtil.PACKAGE);
            ArrayList<String> deepClearProcessList = PrizeClearFilterManager.getInstance().getFilterListFromSP(mContext, PrizeClearUtil.DEEP_CLEAR);
            ArrayList<String> onlyKillPkgList = PrizeClearFilterManager.getInstance().getFilterListFromSP(mContext, PrizeClearUtil.ONLY_KILL_PKG);
            boolean needRestartTencent = PrizeClearUtil.restartTencentSwitch(mContext);
            if (needRestartTencent && lockApps != null && !lockApps.isEmpty()) {
                lockApps.remove("com.tencent.mobileqq");
                lockApps.remove("com.tencent.mm");
            }
            /**三国群英传，凌晨无法查杀问题，因为三国群英传按home键退出后，audio锁没释放**/
            if (music.contains("com.tencent.tmgp.sgqyz")) {
                music.remove("com.tencent.tmgp.sgqyz");
                LogUtils.d(TAG, "remove com.tencent.tmgp.sgqyz from audio list");
            }
            int index = 1;
            while (index < size) {
                RunningAppProcessInfo runningAppProcessInfo = appProcesses.get(index);
                String processName = runningAppProcessInfo.processName;
                int pid = runningAppProcessInfo.pid;
                String[] pkgList = runningAppProcessInfo.pkgList;
                int userId = UserHandle.getUserId(pid);
                boolean isRemove = true;
                if (PrizeClearUtil.mLocalProcessFilterList.contains(processName)) {
                    LogUtils.d(TAG, "skip localClearFilterProcess=" + processName);
                } else if (mClearProcessManager.isInCleanedList(runningAppProcessInfo)) {
                    LogUtils.d(TAG, "skip duplicate process " + processName);
                } else {
                    if ("com.android.systemui".equals(processName)) {
                        pidOfSystemUI = runningAppProcessInfo.pid;
                    }
                    if (processName.equals("com.android.systemui")
                            || processName.contains("com.prize.smartcleaner")
                            || PrizeClearUtil.isInFilterList(launcher, pkgList)
                            || PrizeClearUtil.isInFilterList(keyguard, pkgList)
                            || PrizeClearUtil.isInFilterList(inputMethod, pkgList)
                            || PrizeClearUtil.isInFilterList(wallpaper, pkgList)
                            || PrizeClearUtil.isInFilterList(music, pkgList)
                            || PrizeClearUtil.isInFilterList(lockApps, pkgList)
                            || PrizeClearUtil.isInFilterList(location, pkgList)
                            || PrizeClearUtil.isInFilterList(net, pkgList)
                            || PrizeClearUtil.isInFilterList(pkgFilterList, pkgList)
                            //|| PrizeClearUtil.isInFilterList(processName, AppConstant.whiteList)
                            || PrizeClearUtil.isInFilterList(PrizeClearUtil.mServiceFilterList_Exp, pkgList)
                            || PrizeClearUtil.isTopPkgInRunningPkgList(pkgList, topPkg)
                            || (deepClearProcessList != null && deepClearProcessList.contains(processName))
                            || (onlyKillPkgList != null && PrizeClearUtil.isInFilterList(onlyKillPkgList, pkgList))
                            || (mClearProcessManager.mCameraRunning && "android.process.safer".equals(processName))
                            || (deepClearProcessList != null && PrizeClearUtil.isInFilterList(deepClearProcessList, pkgList))) {
                        isRemove = false;
                    }
                    LogUtils.d(TAG, "[i] " + index + "  [d] " + isRemove + " [processName] " + processName);
                    if (isRemove) {
                        if (processName != null && processName.contains("com.tencent.mm")) {
                            if (!processName.equals("com.tencent.mm")) {
                                PrizeClearUtil.killProcess(pid, processName);
                            } else if (userId == 0) {
                                pidOfTencentMM = pid;
                            } else {
                                pidOfTencentMM2 = pid;
                            }
                        } else if (processName != null && processName.contains("com.tencent.mobileqq")) {
                            PrizeClearUtil.killProcess(pid, processName);
                        } else {
                            if (mClearProcessManager.isPersistentProcess(runningAppProcessInfo)) {
                                LogUtils.d(TAG, "add " + processName + " to mPendingPersistentInfos");
                                mClearProcessManager.mPendingPersistentInfos.add(runningAppProcessInfo);
                            } else {
                                PrizeClearUtil.cleanProcessPkgs(mContext, runningAppProcessInfo);
                                mClearProcessManager.addProcessToCleanedList(runningAppProcessInfo);
                            }
                        }
                    }
                }
                index++;
            }
            mClearProcessManager.clear();
            if (mClearProcessManager.mPendingPersistentInfos != null && !mClearProcessManager.mPendingPersistentInfos.isEmpty()) {
                Iterator it = mClearProcessManager.mPendingPersistentInfos.iterator();
                while (it.hasNext()) {
                    RunningAppProcessInfo processes = (RunningAppProcessInfo) it.next();
                    if (processes != null) {
                        PrizeClearUtil.killProcess(processes.pid, processes.processName);
                    }
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {

                    }
                }
                mClearProcessManager.mPendingPersistentInfos.clear();
            }

            if (needRestartTencent) {
                if (pidOfTencentMM != -1 && !topPkg.equals("com.tencent.mm")) {
                    LogUtils.d(TAG, "kill user_0 tencent.mm pid=" + pidOfTencentMM);
                    PrizeClearUtil.killProcess(pidOfTencentMM, "com.tencent.mm");
                }
                if (pidOfTencentMM2 != -1 && !topPkg.equals("com.tencent.mm")) {
                    LogUtils.d(TAG, "kill user_999 tencent.mm pid=" + pidOfTencentMM2);
                    PrizeClearUtil.killProcess(pidOfTencentMM2, "com.tencent.mm");
                }
            }

            mClearProcessManager.finishCleanProcess(pidOfSystemUI);
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
