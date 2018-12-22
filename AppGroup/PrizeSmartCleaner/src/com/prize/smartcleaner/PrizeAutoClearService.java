package com.prize.smartcleaner;

import android.app.ActivityManager;
import android.app.Service;
import android.app.usage.UsageStats;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.UserHandle;

import com.prize.smartcleaner.utils.LogUtils;
import com.prize.smartcleaner.utils.PrizeClearUtil;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;

/**
 * Created by xiarui on 2018/1/24.
 */

public class PrizeAutoClearService extends Service {

    private static final String TAG = "PrizeAutoClearService";

    public static final String ACSTART = "com.prize.android.service.ACSTART";
    public static final String DELAY_ACSTART = "com.prize.android.service.DELAY_ACSTART";

    private Context mContext;
    private PowerManager mPowerMgr;
    private int MAX_SAVE_RECENT_NUM = 6;
    private ArrayList<String> mFilterAppList = new ArrayList();

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        mPowerMgr = (PowerManager) getSystemService(Context.POWER_SERVICE);
        mPowerMgr.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, this.getClass().getName()).acquire(200);
        LogUtils.d(TAG, "PrizeAutoClearService.onCreate");
        this.mContext = getApplicationContext();
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        LogUtils.d(TAG, "PrizeAutoClearService.onStartCommand");
        if (intent == null) {
            LogUtils.d(TAG, "intent is null, return");
        } else if (!PrizeClearUtil.isMorningCleanning()) {
            mPowerMgr.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, this.getClass().getName()).acquire(15000);
            String action = intent.getAction();
            LogUtils.d(TAG, "PrizeAutoClearService.onStartCommand action = " + action);
            if (ACSTART.equals(action)) {
                new Thread(new AutoClearRunnable()).start();
            } else if (DELAY_ACSTART.equals(action)) {
                new Thread(new DelayClearRunnable()).start();
            }
        } else {
            LogUtils.d(TAG, "isMorningSystemRunning is running!!");
        }

        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        LogUtils.d(TAG, "PrizeAutoClearService.onDestroy");
        super.onDestroy();
    }

    public ArrayList<String> getFilterAppList() {
        return filterComparatorAlgorithm(getRecentTaskSortList(PrizeClearUtil.getRecentTasks(mContext)),
                getUsageStatsList(PrizeClearUtil.getUsageStats(mContext)));
    }

    public ArrayList<HashMap<String, Object>> getRecentTaskSortList(ArrayList<ActivityManager.RecentTaskInfo> recentTaskInfos) {
        ArrayList<HashMap<String, Object>> taskList = new ArrayList();
        int size = recentTaskInfos.size();
        LogUtils.d(TAG, "--------------task list---------------");
        for (int i = 0; i < size; i++) {
            int sort;
            ActivityManager.RecentTaskInfo recentTaskInfo = recentTaskInfos.get(i);
            HashMap hashMap = new HashMap();
            String packageName = recentTaskInfo.baseIntent.getComponent().getPackageName();
            if (i >= MAX_SAVE_RECENT_NUM) {
                sort = 0;
            } else {
                sort = MAX_SAVE_RECENT_NUM - i;
            }
            hashMap.put("packageName", packageName);
            hashMap.put("sort", Integer.valueOf(sort));

            LogUtils.d(TAG, " [sort] " + i + " [value] " + sort + " [packageName] " + packageName);

            taskList.add(hashMap);
        }
        return taskList;
    }

    public ArrayList<HashMap<String, Object>> getUsageStatsList(ArrayList<UsageStats> usageList) {
        ArrayList<HashMap<String, Object>> list = new ArrayList();
        Collections.sort(usageList, new LaunchCountComparator());
        int size = usageList.size();
        int i = 0;
        int launchSortTemp = 0;
        int frontLaunchTemp = 0;
        while (i < size) {
            int launchSort;
            UsageStats usageStats = usageList.get(i);
            HashMap hashMap = new HashMap();
            String packageName = usageStats.getPackageName();
            int launchCount = usageStats.mLaunchCount;
            if (i == 0) {
                launchSort = MAX_SAVE_RECENT_NUM;
                launchSortTemp = launchCount;
            } else {
                launchSort = launchSortTemp;
                launchSortTemp = frontLaunchTemp;
            }
            if (launchCount == launchSortTemp) {
                launchSortTemp = launchSort;
            } else {
                launchSort--;
                launchSortTemp = launchSort;
            }
            if (launchSort < 0) {
                launchSort = 0;
            }
            hashMap.put("packageName", packageName);
            hashMap.put("launchCount", Integer.valueOf(launchSort));
            LogUtils.d(TAG, " [i] " + i + " [launchSort] " + launchSort + " [launchCount] " + launchCount + " [packageName] " + packageName);
            list.add(hashMap);
            i++;
            frontLaunchTemp = launchCount;
        }
        return list;
    }

    public ArrayList<String> filterComparatorAlgorithm(ArrayList<HashMap<String, Object>> recentTasks, ArrayList<HashMap<String, Object>> usageStats) {

        String str;
        ArrayList<HashMap<String, Object>> tempList = new ArrayList();
        int recentSize = recentTasks.size();
        int usageSize = usageStats.size();
        for (int i = 0; i < recentSize; i++) {
            int launchCount = 0;
            HashMap<String, Object> task = recentTasks.get(i);
            String pkgName = task.get("packageName").toString();
            int sort = Integer.parseInt(task.get("sort").toString());
            for (int j = 0; j < usageSize; j++) {
                HashMap<String, Object> usage = usageStats.get(j);
                if (pkgName.equals(usage.get("packageName").toString())) {
                    launchCount = Integer.parseInt(usage.get("launchCount").toString());
                    break;
                }
            }
            task.put("launchCount", Integer.valueOf(launchCount));
            task.put("add", Integer.valueOf(sort));
            tempList.add(task);
        }
        Collections.sort(tempList, new AddSortComparator());
        int finalSize = tempList.size();
        finalSize = finalSize > MAX_SAVE_RECENT_NUM ? MAX_SAVE_RECENT_NUM : finalSize;
        ArrayList<String> finalList = new ArrayList();
        LogUtils.d(TAG, "-------------final-----------");

        for (int i = 1; i <= finalSize; i++) {
            HashMap<String, Object> hashMap = tempList.get(i - 1);
            String pkgName = hashMap.get("packageName").toString();
            int sort = Integer.parseInt(hashMap.get("sort").toString());
            int launchCount = Integer.parseInt(hashMap.get("launchCount").toString());
            int add = Integer.parseInt(hashMap.get("add").toString());
            finalList.add(pkgName);
            LogUtils.d(TAG, " [add] " + add + " [sort] " + sort + " [launchCount] " + launchCount + " [packageName] " + pkgName);
        }
        ArrayList<String> list = PrizeClearUtil.getLauncherList(mContext);
        if (list != null) {
            for (int i = 0; i < list.size(); i++) {
                str = list.get(i);
                if (finalList.contains(str)) {
                    LogUtils.d(TAG, " remove home package: " + str);
                    finalList.remove(finalList.indexOf(str));
                }
            }
        }
        list = TrafficUtil.getUsingNetPackages(mContext, 0);
        if (list != null) {
            for (int i = 0; i < list.size(); i++) {
                str = list.get(i);
                if (!finalList.contains(str)) {
                    finalList.add(str);
                    LogUtils.d(TAG, " add net using package : " + str);
                }
            }
        }
        list = PrizeClearUtil.getActiveAudioPackage(mContext);
        if (list != null) {
            for (int i = 0; i < list.size(); i++) {
                str = list.get(i);
                if (!finalList.contains(str)) {
                    finalList.add(str);
                    LogUtils.d(TAG, " add music package: " + str);
                }
            }
        }
        list = PrizeClearUtil.getPerceptiblePkg(mContext);
        if (list != null) {
            for (int i = 0; i < list.size(); i++) {
                str = list.get(i);
                if (!finalList.contains(str)) {
                    finalList.add(str);
                    LogUtils.d(TAG, " add perceptible package: " + str);
                }
            }
        }
        list = PrizeClearUtil.getInUseLocationPkgList(mContext);
        if (list != null) {
            for (int i = 0; i < list.size(); i++) {
                str = list.get(i);
                if (!finalList.contains(str)) {
                    finalList.add(str);
                    LogUtils.d(TAG, " add using location package: " + str);
                }
            }
        }
        list = PrizeClearFilterManager.getInstance().getFilterListFromSP(mContext, PrizeClearUtil.LEARN);
        if (list != null) {
            for (int i = 0; i < list.size(); i++) {
                str = list.get(i);
                if (!finalList.contains(str)) {
                    finalList.add(str);
                    LogUtils.d(TAG, " add package in learnFilterList: " + str);
                }
            }
        }
        return finalList;
    }

    class LaunchCountComparator implements Comparator {

        @Override
        public int compare(Object obj, Object obj2) {
            return compareByLaunchCount((UsageStats) obj, (UsageStats) obj2);
        }

        public int compareByLaunchCount(UsageStats usageStats, UsageStats usageStats2) {
            int i = usageStats.mLaunchCount;
            int i2 = usageStats2.mLaunchCount;
            if (i > i2) {
                return -1;
            }
            if (i < i2) {
                return 1;
            }
            return 0;
        }
    }

    class AddSortComparator implements Comparator {

        @Override
        public int compare(Object obj, Object obj2) {
            return compareByAddSort((HashMap) obj, (HashMap) obj2);
        }

        public int compareByAddSort(HashMap hashMap, HashMap hashMap2) {
            int add1 = Integer.parseInt(hashMap.get("add").toString());
            int add2 = Integer.parseInt(hashMap2.get("add").toString());
            int sort1 = Integer.parseInt(hashMap.get("sort").toString());
            int sort2 = Integer.parseInt(hashMap2.get("sort").toString());
            if (add1 > add2) {
                return -1;
            }
            if (add1 < add2) {
                return 1;
            }
            if (sort1 > sort2) {
                return -1;
            }
            if (sort1 < sort2) {
                return 1;
            }
            return 0;
        }
    }

    class AutoClearRunnable implements Runnable {

        @Override
        public void run() {
            PrizeClearUtil.setAutoClearFlag(true);
            LogUtils.d(TAG, "save recent number: " + MAX_SAVE_RECENT_NUM);
            if (!(PrizeClearUtil.isPhoneInCall(mContext) || PrizeClearUtil.isScreenOn(mContext))) {
                mFilterAppList = getFilterAppList();
                Intent intent = PrizeClearUtil.getIntent(mContext, new Intent(PrizeClearSystemService.REQUEST_APP_CLEAN_RUNNING));
                if (intent != null) {
                    LogUtils.d(TAG, "start ClearRunningAppService from PrizeAutoClearService.");
                    intent.putStringArrayListExtra("filterapplist", mFilterAppList);
                    intent.putExtra("isLearningClearing", true);
                    intent.putExtra("IsShowCleanFinishToast", false);
                    intent.putExtra("clean_trash", false);
                    mContext.startServiceAsUser(intent, UserHandle.CURRENT);
                }
            } else {
                LogUtils.d(TAG, "cancel auto clear service , phone in call or screen is on");
            }
            PrizeClearUtil.setAutoClearFlag(false);
        }
    }

    class DelayClearRunnable implements Runnable {
        @Override
        public void run() {
            PrizeClearUtil.setAutoClearFlag(true);
            if (!(PrizeClearUtil.isPhoneInCall(mContext) || PrizeClearUtil.isScreenOn(mContext))) {
                Intent intent = PrizeClearUtil.getIntent(mContext, new Intent(PrizeClearSystemService.SCREEN_OFF_DELAY_CLEAR));
                if (intent != null) {
                    LogUtils.d(TAG, "start ClearRunningAppService from PrizeAutoClearService.");
                    intent.putExtra("IsShowCleanFinishToast", false);
                    mContext.startServiceAsUser(intent, UserHandle.CURRENT);
                }
            } else {
                LogUtils.d(TAG, "cancel delay clear service , phone in call or screen is on");
            }
            PrizeClearUtil.setAutoClearFlag(false);
        }
    }

}
