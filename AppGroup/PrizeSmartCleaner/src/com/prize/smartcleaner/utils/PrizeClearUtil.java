package com.prize.smartcleaner.utils;

/**
 * Created by xiarui on 2018/1/15.
 */

import android.app.ActivityManager;
import android.app.ActivityManagerNative;
import android.app.AlarmManager;
import android.app.IActivityManager;
import android.app.PendingIntent;
import android.app.WallpaperInfo;
import android.app.WallpaperManager;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.location.LocationManager;
import android.media.AudioManager;
import android.os.PowerManager;
import android.os.Process;
import android.os.RemoteException;
import android.os.SystemProperties;
import android.preference.PreferenceManager;
import android.util.Xml;
import android.provider.Settings;
import android.telephony.TelephonyManager;

import com.prize.smartcleaner.PrizeClearSystemService;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import com.prize.smartcleaner.bean.UserIdPkg;
import com.prize.smartcleaner.MyFileFilter;

public class PrizeClearUtil {

    public static final String TAG = "PrizeClearUtil";

    private static boolean mIsAutoClearRunning = false;
    private static boolean mIsMorningCleanning = false;
    private static boolean mIsScreenOffCleanning = false;
    private static boolean mServiceRunning = false;

    public static final int PROCESS = 0x0000;
    public static final int PACKAGE = 0x0001;
    public static final int DEEP_CLEAR = 0x0002;
    public static final int LEARN = 0x0003;
    public static final int ONLY_KILL_PKG = 0x0004;
    public static final int SERVICE = 0x0005;
    public static final int THIRD_WHITE_LIST = 0x0006;
    public static final int FORCE_STOP = 0x0007;

    public static final List<String> mRedundentList = Arrays.asList(new String[]{
            "com.tencent.mm.plugin.appbrand.ui.AppBrandUI",
            "com.tencent.mm.plugin.appbrand.ui.AppBrandInToolsUI"
    });

    public static final List<String> mDeepClearProcessList = Arrays.asList(new String[]{
            "system",
            "com.android.phone",
            "com.android.systemui",
            "android.process.acore",
            "com.adups.fota",
            "com.adups.fota.sysoper",
            "com.android.providers.downloads",
            "com.android.nfc",
            "com.android.bluetooth",
            "com.google.android.marvin.talkback",
            "se.dirac.acs",
            "com.samsung.accessory",
            "com.samsung.accessory.framework",
            "com.samsung.accessory:systemprovider",
            "com.redteamobile.roaming",

            /*系统应用*/
            "com.prize.prizesecurity",
            "com.android.deskclock",
            "com.prize.appcenter",
            "com.prize.weather",
            "com.prize.gamecenter",
            "com.prize.prizethemecenter",
            "com.prize.smart",
            "com.prize.tts",
            "com.prize.sysresmon",
            "com.levect.lc.koobee", //杂志锁屏
            "com.android.settings",
            "com.mediatek.mtklogger"

    });

    public static final List<String> mDeepClearProcessList_Exp = Arrays.asList(new String[]{
            "com.viber.voip",
            "com.zing.zalo",
            "com.facebook.lite",
            "com.facebook.lite:fbns",
            "com.google.android.marvin.talkback",
            "com.whatsapp",
            "com.twitter.android",
            "jp.naver.line.android",
            "com.facebook.katana:dash",
            "com.facebook.katana:nodex",
            "com.facebook.katana",
            "com.bbm",
            "com.facebook.orca",
            "com.android.launcher3",
            "com.google.android.gms.persistent",
            "com.google.android.gms",
            "com.bsb.hike",
            "com.jiochat.jiochatapp",
            "com.jiochat.jiochatapp:CMCoreService",
            "com.jio.join",
            "com.truecaller",
            "com.imo.android.imoim"
    });

    public static final List<String> mLearnClearPkgList = Arrays.asList(new String[]{
            "com.tencent.mobileqq",
            "com.tencent.mm",
            "com.android.incallui",
            "com.android.dlna.service",
            "com.samsung.android.app.watchmanager",
            "com.eastedge.taxidriverforpad",
            "com.joyskim.taxis_driver",
            "cn.edaijia.android.driverclient",
            "com.xtc.watch",
            "com.sankuai.meituan.meituanwaimaibusiness",
            "com.prize.smartcleaner",
            "com.duoduo.vip.taxi",
            "android.sample.cts",
            "net.zdsoft.taxiclient2",
            "com.kuaidi.daijia.driver",
            "com.adups.fota",
            "com.sdu.didi.gui",
            "com.hn.client.driver",
            "me.ele.napos",
            "com.funcity.taxi.driver",
            "com.sdu.didi.gsui",
            "com.mobiletools.systemhelper",
            "com.yongche",
            "com.nearme.statistics.rom",
            "com.chinaunicom.registerhelper",
            "com.ubercab.driver",
            "com.anyimob.taxi",
            "com.samsung.accessory",
            "com.alibaba.android.rimet"
    });

    public static final List<String> mLearnClearPkgList_Exp = Arrays.asList(new String[]{
            "com.instagram.android",
            "com.viber.voip",
            "com.zing.zalo",
            "com.google.android.wearable.app",
            "com.mediatek.bluetooth",
            "com.google.android.marvin.talkback",
            "com.whatsapp",
            "com.twitter.android",
            "jp.naver.line.android",
            "com.facebook.katana",
            "com.bbm",
            "com.facebook.orca",
            "com.google.android.gms.persistent",
            "com.google.android.gms",
            "com.truecaller",
            "com.bsb.hike",
            "com.snapchat.android",
            "com.imo.android.imoim"
    });

    public static final List<String> mPackageFilterList = Arrays.asList(new String[]{
            "android",
            "com.prize.sysresmon",
            "com.prize.luckymonkeyhelper",
            "com.prize.dozewhitelisthelper",
            "com.prize.globaldata",
            "com.prize.rootcheck",
            "com.prize.prizeappoutad", //广告屏蔽
            "com.prize.cloudlist",
            "com.prize.smartcleaner",
            "com.prize.tts",
            "com.android.prizeblacklist",
            "com.android.keyguard",
            "com.android.systemui",
            "com.android.phone",
            "com.android.launcher3",
            "com.prize.inforstream",
            "com.android.smspush",
            "com.android.defcontainer",
            "com.android.floatwindow",
            "com.android.lpserver",
            "com.android.nfc",
            "com.prize.boot",
            "com.android.providers.calendar",
            "org.simalliance.openmobileapi.service",
            "com.android.externalstorage",
            "com.android.bluetoothmidiservice",
            "com.android.bluetooth",
            "com.mediatek.bluetooth.dtt",
            "com.mediatek.schpwronoff",
            "com.google.android.marvin.talkback",
            "com.sohu.inputmethod.sogouoem",
            "com.qe.powerlife",
            "com.baidu.map.location",
            "com.amap.android.location",
            "com.mediatek.location.lppe.main",
            "com.amap.android.ams", //高德地图
            "se.dirac.acs", //乐视
            //"com.mediatek.mtklogger",
            "com.adups.fota",
    });

    public static final List<String> mPackageFilterList_Exp = Arrays.asList(new String[]{
            "com.google.android.marvin.talkback",
            "com.android.server.telecom",
            "com.google.android.syncadapters.calendar",
            "com.google.android.syncadapters.contacts",
            "com.google.android.gms.persistent",
            "com.google.android.gms"
    });

    public static final List<String> mProcessFilterList = Arrays.asList(new String[]{
            "system",
            "android.process.acore",
            "android.process.media",
            "com.android.keyguard",
            "com.android.providers.downloads",
            "com.mediatek.providers.drm"
    });

    public static final List<String> mProcessFilterList_Exp = Arrays.asList(new String[]{
            "com.google.android.marvin.talkback",
            "com.google.android.gms.persistent",
            "com.google.android.gms"
    });

    public static final List<String> mOnlyKillPkgList = Arrays.asList(new String[]{
            "com.prize.prizesecurity",
            "com.prize.cloudlist",
            "com.adups.fota",
            "com.adups.fota.sysoper",
            "com.android.deskclock"
    });

    public static final List<String> mServiceFilterList = Arrays.asList(new String[]{
            "com.prize.appcenter#com.prize.appcenter:remote#com.prize.appcenter:xg_service_v2#com.prize.appcenter:xg_service_v3",
            "com.prize.weather#com.prize.weather:widgetservice#com.prize.weather:locationservice",
            "com.prize.gamecenter#com.prize.gamecenter:gamecenter_remote",
            "com.prize.prizethemecenter#com.prize.prizethemecenter:xg_service_v3",
            "com.levect.lc.koobee#com.levect.lc.koobee", //杂志锁屏
            "com.android.deskclock#com.android.deskclock",
            "com.adups.fota#com.adups.fota",
            "com.adups.fota.sysoper#com.adups.fota.sysoper",
            "com.redteamobile.roaming#NONE",
            "com.google.android.gms#NONE",
            "com.google.android.tts#NONE",
            "com.android.chrome#NONE"
    });

    public static final List<String> mServiceFilterList_Exp = Arrays.asList(new String[]{
            "android.ext.services",
            "com.android.calllogbackup",
            "com.redteamobile.virtual.softsim",
            "com.android.vendors.bridge.softsim",
            "com.qti.dpmserviceapp",
            "se.dirac.acs",
            "com.mediatek.atci.service"
    });

    public static final List<String> mLocalProcessFilterList = Arrays.asList(new String[]{
            "com.mediatek.wfo.impl",
            "com.mediatek.ims",
            "com.mediatek.gba"
    });

    public static final List<String> mRestartPkgPro = Arrays.asList(new String[]{
            "com.tencent.mm",
            "com.tencent.mobileqq"
    });

    public static final List<String> mRestartPkgPro_Exp = Arrays.asList(new String[]{
            "com.tencent.mm",
            "com.tencent.mobileqq",
            "com.zing.zalo",
            "com.facebook.orca",
            "com.facebook.katana",
            "com.instagram.android",
            "jp.naver.line.android",
            "com.whatsapp",
            "com.bbm",
            "com.skype.raider",
            "com.viber.voip",
            "com.path",
            "com.facebook.lite",
            "com.truecaller",
            "com.bsb.hike",
            "com.snapchat.android",
            "com.twitter.android",
            "com.imo.android.imoim",
            "com.google.android.gm"
    });

    public static final List<String> mRemoveTaskFilterPkgList = Arrays.asList(new String[]{
            ///"com.android.deskclock",
            //"com.android.calendar",
            "com.android.dialer",
            "com.mediatek.mtklogger"
    });
    public static final List<String> mRemoveTaskFilterPkgList_Exp = Arrays.asList(new String[0]);

    public static final List<String> mThirdPkgFilterList = Arrays.asList(new String[0]);

    //mForceStopFilterPkgList = mRestartPkgPro + mRemoveTaskFilterPkgList
    public static final List<String> mForceStopFilterPkgList = Arrays.asList(new String[]{
            "com.tencent.mm",
            "com.tencent.mobileqq",
            "com.android.dialer",
            "com.android.settings",
            "com.mediatek.mtklogger"
    });

    public static void setAutoClearFlag(boolean isRunning) {
        mIsAutoClearRunning = isRunning;
    }

    public static boolean isAutoClearRunning() {
        return mIsAutoClearRunning;
    }

    public static void setScreenOffClearFlag(boolean isScreenOffCleanning) {
        mIsScreenOffCleanning = isScreenOffCleanning;
    }

    public static boolean isScreenOffCleanning() {
        return mIsScreenOffCleanning;
    }

    public static void setMorningClearFlag(boolean isMorningCleanning) {
        mIsMorningCleanning = isMorningCleanning;
    }

    public static boolean isMorningCleanning() {
        return mIsMorningCleanning;
    }

    public static void setClearRunningAppServiceRunning(boolean serviceRunning) {
        mServiceRunning = serviceRunning;
        LogUtils.d(TAG, "=====setClearRunningAppServiceRunning=====  " + mServiceRunning);
    }

    public static boolean isServiceRunning() {
        return mServiceRunning;
    }

    /**
     * get usage stats
     * @param context
     * @return
     */
    public static ArrayList<UsageStats> getUsageStats(Context context) {
        long endt = System.currentTimeMillis();
        long statt = endt - 86400000L; //24小时内
        UsageStatsManager uSM = (UsageStatsManager)context.getSystemService(Context.USAGE_STATS_SERVICE);
        List<UsageStats> list = uSM.queryUsageStats(UsageStatsManager.INTERVAL_BEST, statt, endt);
        if (list.size() == 0) {
            LogUtils.d(TAG, " return empty ArrayList<UsageStats>();!");
            return new ArrayList();
        }
        return (ArrayList)list;
    }

    public static int getMorningClearRandomHour(Context context) {
        SharedPreferences defaultSharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        int start = defaultSharedPreferences.getInt("MorningClearRandomTimeStart", 2);
        int end = defaultSharedPreferences.getInt("MorningClearRandomTimeEnd", 5);
        if (start < end) {
            int nextInt = new Random().nextInt(end - start) + start;
            LogUtils.d(TAG, "getMorningClearRandomHour=" + nextInt);
            return nextInt;
        } else {
            LogUtils.e(TAG, "wrong parameter for morning clear random hour! end hour must be bigger than start hour");
            return 2;
        }
    }

    public static int getMorningClearRandomMin(Context context) {
        return new Random().nextInt(60);
    }

    public static long getAvailMem(Context context) {
        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        ActivityManager.MemoryInfo memoryInfo = new ActivityManager.MemoryInfo();
        activityManager.getMemoryInfo(memoryInfo);
        return (memoryInfo.availMem / 1024) / 1024;
    }

    public static long getAvailMem() {
        IActivityManager iActivityManager = ActivityManagerNative.getDefault();
        ActivityManager.MemoryInfo memoryInfo = new ActivityManager.MemoryInfo();
        try {
            iActivityManager.getMemoryInfo(memoryInfo);
        } catch (RemoteException e) {
        }
        return memoryInfo.availMem;
    }

    public static boolean isInFilterList(List<String> filterList, String[] pkgList) {
        if (filterList == null || pkgList == null) {
            return false;
        }
        for (String pkg : pkgList) {
            if (filterList.contains(pkg)) {
                return true;
            }
        }
        return false;
    }

    public static boolean isInFilterList(String pkgName, String[] filterList) {
        for (String pkg : filterList) {
            if (pkgName.contains(pkg)) {
                return true;
            }
        }
        return false;
    }


    public static boolean isTopPkgInRunningPkgList(String[] runningPkgList, String topPkg) {
        if (runningPkgList == null || topPkg == null) {
            return false;
        }
        for (String pkg : runningPkgList) {
            if (pkg.equals(topPkg)) {
                return true;
            }
        }
        return false;
    }

    public static ArrayList<String> getDefInputMethodList(Context context) {
        ArrayList<String> inputMethodList = new ArrayList();
        inputMethodList.add(getDefaultInputMethod(context));
        return inputMethodList;
    }

    public static String getDefaultInputMethod(Context context) {
        String str = "com.sohu.inputmethod.sogouoem";
        String string = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.DEFAULT_INPUT_METHOD);
        if (string != null && !string.isEmpty()) {
            int indexOf = string.indexOf("/");
            if (indexOf != -1) {
                return string.substring(0, indexOf);
            }
            return str;
        } else {
            LogUtils.d(TAG, "can't get default input method");
            return str;
        }
    }

    public static ArrayList<String> getWallpaperInfo(Context context) {
        ArrayList<String> list = new ArrayList();
        WallpaperInfo wallpaperInfo = WallpaperManager.getInstance(context).getWallpaperInfo();
        if (wallpaperInfo != null) {
            list.add(wallpaperInfo.getPackageName());
        }
        return list;
    }

    public static ArrayList<String> getLauncherList(Context context) {
        ArrayList<String> launcher = new ArrayList();
        String defLauncher = "com.android.launcher3";
        PackageManager packageManager = context.getPackageManager();
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        ResolveInfo resolveInfo = packageManager.resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY);
        if (!(resolveInfo == null || resolveInfo.activityInfo == null)) {
            defLauncher = resolveInfo.activityInfo.packageName;
        }
        if (defLauncher == null) {
            defLauncher = "com.android.launcher3";
        }
        launcher.add(defLauncher);
        return launcher;
    }

    public static ArrayList<String> getKeyguard(Context context) {
        ArrayList<String> list = new ArrayList();
        //for (ResolveInfo resolveInfo : context.getPackageManager().queryIntentServices(new Intent("intent.action.keyguard"), PackageManager.GET_META_DATA)) {
        //    list.add(resolveInfo.serviceInfo.packageName);
        //}
        list.add("com.android.systemui.keyguard.KeyguardService");
        return list;
    }

    /**
     * get running app process info
     * @param context
     * @return
     */
    public static List<ActivityManager.RunningAppProcessInfo> getRunningAppProcessInfo(Context context) {
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> appProcessInfos = am.getRunningAppProcesses();

        for (ActivityManager.RunningAppProcessInfo appProcessInfo : appProcessInfos) {
            LogUtils.d(TAG, "processInfo pid = " + appProcessInfo.pid + " , uid = " + appProcessInfo.uid + ", name = " + appProcessInfo.processName);
        }

        return appProcessInfos;
    }

    public static ArrayList<ActivityManager.RecentTaskInfo> getRecentTasks(Context context) {
        return (ArrayList<ActivityManager.RecentTaskInfo>) ((ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE)).getRecentTasks(ActivityManager.getMaxRecentTasksStatic(), 2);
    }

    /**
     * get recent tasks
     * @param context
     * @return
     */
//    public static List<ActivityManager.RecentTaskInfo> getRecentTasks(Context context) {
//
//        ActivityManager mAm = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
//
//        // Remove home/recents/excluded tasks
//        int userId = UserHandle.USER_CURRENT;
//        boolean includeFrontMostExcludedTask = true;
//        int minNumTasksToQuery = 10;
//        int numLatestTasks = ActivityManager.getMaxRecentTasksStatic();
//        int numTasksToQuery = Math.max(minNumTasksToQuery, numLatestTasks);
//        int flags = ActivityManager.RECENT_IGNORE_HOME_STACK_TASKS |
//                ActivityManager.RECENT_INGORE_DOCKED_STACK_TOP_TASK |
//                ActivityManager.RECENT_INGORE_PINNED_STACK_TASKS |
//                ActivityManager.RECENT_IGNORE_UNAVAILABLE |
//                ActivityManager.RECENT_INCLUDE_PROFILES;
//        if (includeFrontMostExcludedTask) {
//            flags |= ActivityManager.RECENT_WITH_EXCLUDED;
//        }
//        List<ActivityManager.RecentTaskInfo> tasks = null;
//        try {
//            tasks = mAm.getRecentTasksForUser(numTasksToQuery, flags, userId);
//        } catch (Exception e) {
//            LogUtils.e(TAG, "Failed to get recent tasks");
//            e.printStackTrace();
//        }
//
//        // Break early if we can't get a valid set of tasks
//        if (tasks == null) {
//            return new ArrayList<>();
//        }
//
//        Collections.reverse(tasks);
//
//        return tasks.subList(0, Math.min(tasks.size(), numLatestTasks));
//    }

    public static ArrayList<ActivityManager.RunningAppProcessInfo> getRunningAppProcesses(Context context) {
        return (ArrayList<ActivityManager.RunningAppProcessInfo>) ((ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE)).getRunningAppProcesses();
    }

    public static ArrayList<String> getLockedAppListFromFile() {
        File file = new File("/data/system/recenttask", "locked_apps.xml");
        if (file.exists()) {
            return getInfoFromXmlPullParser(file);
        }
        return null;
    }

    public static ArrayList<String> getLockedAppsPkgList(Context context) {
        ArrayList<String> lockPkgList = new ArrayList();
        ArrayList<String> lockList = getLockedAppListFromFile();
        if (lockList != null) {
            Iterator it = lockList.iterator();
            while (it.hasNext()) {
                String str = (String) it.next();
                if (str != null) {
                    str = getLockedPkgName(str);
                    if (!(str == null || str.isEmpty() || lockPkgList.contains(str))) {
                        LogUtils.d(TAG, "lock list:  " + str);
                        lockPkgList.add(str);
                    }
                }
            }
        }
        return lockPkgList;
    }

    public static ArrayList<UserIdPkg> getLockedAppsList(Context context) {
        ArrayList<UserIdPkg> userIdPkgList = new ArrayList();
        ArrayList<String> lockedAppList = getLockedAppListFromFile();
        if (lockedAppList != null) {
            PackageManager packageManager = context.getPackageManager();
            Iterator it = lockedAppList.iterator();
            while (it.hasNext()) {
                String str = (String) it.next();
                if (str != null) {
                    UserIdPkg userIdPkg = getUserIdInfo(str);
                    if (userIdPkg != null) {
                        int uid = 0;
                        try {
                            uid = packageManager.getPackageUidAsUser(userIdPkg.pkgName, userIdPkg.userId);
                        } catch (PackageManager.NameNotFoundException e) {
                            LogUtils.e(TAG, "NameNotFoundException in getRecentLockUserListFromFile " + userIdPkg.pkgName + " for user " + userIdPkg.userId);
                        }
                        userIdPkg.uid = uid;
                        LogUtils.d(TAG, "lock list:  pkg=" + userIdPkg.pkgName + ", userId=" + userIdPkg.userId + ", uid=" + uid);
                        userIdPkgList.add(userIdPkg);
                    }
                }
            }
        }
        return userIdPkgList;
    }

    private static UserIdPkg getUserIdInfo(String str) {
        int i = 0;
        if (str == null || str.isEmpty()) {
            return null;
        }
        UserIdPkg userIdPkg = new UserIdPkg(0, 0, str, 0);
        if (str.contains("#")) {
            String[] split = str.split("#");
            if (split.length == 2) {
                userIdPkg.pkgName = split[i];
                try {
                    i = Integer.parseInt(split[1]);
                } catch (NumberFormatException e) {
                }
                userIdPkg.appIndex = i;
                //userIdPkg.userId = i;
            } else {
                userIdPkg.appIndex = i;
                //userIdPkg.userId = i;
                userIdPkg.pkgName = str;
            }
        } else {
            userIdPkg.appIndex = i;
            //userIdPkg.userId = i;
            userIdPkg.pkgName = str;
        }
        return userIdPkg;
    }

    private static String getLockedPkgName(String lockPkg) {
        if (lockPkg == null || lockPkg.isEmpty()) {
            return null;
        }
        if (lockPkg.contains("#")) {
            return lockPkg.split("#")[0];
        }
        return lockPkg;
    }

    public static boolean isScreenOn(Context context) {
        PowerManager powerManager = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        LogUtils.d(TAG, "is screen on:" + powerManager.isScreenOn());
        return powerManager.isScreenOn();
    }

    public static ArrayList<String> getPerceptiblePkg(Context context) {
        ArrayList<String> list = new ArrayList();
        ArrayList<ActivityManager.RunningAppProcessInfo> runningAppProcesses = getRunningAppProcesses(context);
        if (runningAppProcesses != null) {
            Iterator it = runningAppProcesses.iterator();
            while (it.hasNext()) {
                ActivityManager.RunningAppProcessInfo runningAppProcessInfo = (ActivityManager.RunningAppProcessInfo) it.next();
                if (runningAppProcessInfo.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_PERCEPTIBLE
                        || runningAppProcessInfo.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND_SERVICE) {
                    list.add(runningAppProcessInfo.pkgList[0]);
                }
            }
        }
        return list;
    }

    public static ArrayList<String> getActivityServicesList(String packagename) {
        ArrayList<String> list = new ArrayList();
        try {
            java.lang.Process exec = Runtime.getRuntime().exec("dumpsys activity services " + packagename);
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(exec.getInputStream()));
            while (true) {
                String readLine = bufferedReader.readLine();
                if (readLine == null) {
                    break;
                } else if (readLine.contains("* Client AppBindRecord")) {
                    list.add(readLine.substring(readLine.indexOf(":") + 1, readLine.indexOf("/")));
                }
            }
            bufferedReader.close();
            exec.waitFor();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return list;
    }

    public static int[] getActiveAudioPids(Context context) {
        int[] pid = ((AudioManager) context.getSystemService(Context.AUDIO_SERVICE)).getActiveAudioPids();
        if (pid == null || pid.length == 0) {
            return null;
        }
        return pid;
    }

    public static ArrayList<String> getActiveAudioPackage(Context context) {
        ArrayList<String> pkgList = new ArrayList();
        ArrayList<ActivityManager.RunningAppProcessInfo> runningList = getRunningAppProcesses(context);
        int[] pids = getActiveAudioPids(context);
        if (pids != null) {
            int length = pids.length;
            if (length > 0) {
                for (int i = 0; i < length; i++) {
                    int pid = pids[i];
                    for (int j = 0; j < runningList.size(); j++) {
                        ActivityManager.RunningAppProcessInfo runningAppProcessInfo = runningList.get(j);
                        if (runningAppProcessInfo.processName.equals("system")) {
                            LogUtils.d(TAG, " processName22 " + runningAppProcessInfo.processName);
                        } else if (pid == runningAppProcessInfo.pid) {
                            for (String pkg : runningAppProcessInfo.pkgList) {
                                if (pkgList != null && !pkgList.contains(pkg)) {
                                    pkgList.add(pkg);
                                    LogUtils.d(TAG, "in use audio pkg : " + pkg);
                                }
                            }
                        }
                    }
                }
            }
        }
        if (pkgList.contains("com.iflytek.speechcloud")) {
            ArrayList<String> usingIflytekPackage = getActivityServicesList("com.iflytek.speechcloud");
            if (usingIflytekPackage != null) {
                int i = 0;
                while (i < usingIflytekPackage.size()) {
                    String str = (String) usingIflytekPackage.get(i);
                    if (!pkgList.contains(str)) {
                        pkgList.add(str);
                        LogUtils.d(TAG, " using com.iflytek.speechcloud package: " + str);
                    }
                    i++;
                }
            }
        }
        return pkgList;
    }


    /**
     * get use location service package list
     * @param context
     * @return
     */
    public static ArrayList<String> getInUseLocationPkgList(Context context) {
        ArrayList<String> locationPkgList = new ArrayList<>();
        ArrayList<String> list = null;
        LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        if (locationManager != null) {
            list = (ArrayList<String>) locationManager.getInUsePackagesList();
        }
        if (list != null) {
            if (list.size() == 0) {
                LogUtils.i(TAG, "no pkg use location !!");
            } else {
                for (String pkg : list) {
                    if ("android".equals(pkg)) {
                        LogUtils.i(TAG, " filter location package : android");
                    } else {
                        locationPkgList.add(pkg);
                        LogUtils.i(TAG, " in use location package: [ " + pkg + " ] ");
                    }
                }
            }
        } else {
            LogUtils.i(TAG, "no pkg use location !!");
        }
        return locationPkgList;
    }

    public static boolean isSystemProviderPackage(Context context, String pkg) {
        if (pkg == null || pkg.equals("") || !pkg.contains("com.android.providers.")) {
            return false;
        }
        try {
            ApplicationInfo applicationInfo = context.getPackageManager().getApplicationInfo(pkg, PackageManager.GET_META_DATA);
            if (applicationInfo == null || (applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) == 0) {
                return false;
            }
            LogUtils.d(TAG, "skip system provider package: " + pkg);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }

    public static void cleanProcessPkgs(Context context, ActivityManager.RunningAppProcessInfo runningAppProcessInfo) {
        ActivityManager actMgr = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        for (String pkg : runningAppProcessInfo.pkgList) {
            ApplicationInfo appInfo = null;
            try {
                appInfo = context.getPackageManager().getApplicationInfo(pkg, PackageManager.GET_META_DATA);
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }
            if (appInfo != null) {
                boolean isPersistent = false;
                if ((appInfo.flags & ApplicationInfo.FLAG_PERSISTENT) != 0) {
                    isPersistent = true;
                }
                String pkgName = appInfo.packageName;
                if (isPersistent) {
                    LogUtils.d(TAG, "K [processName] " + runningAppProcessInfo.processName + "  [pid] " + runningAppProcessInfo.pid);
                    Process.killProcess(runningAppProcessInfo.pid);
                } else {
                    LogUtils.d(TAG, "F package :" + pkgName);
                    actMgr.forceStopPackage(pkgName);
                }
            }
        }
    }

    public static void killProcess(int pid, String pkg) {
        Process.killProcess(pid);
        LogUtils.d(TAG, "K [" + pid + "][" + pkg + "]");
    }

    public static void forceStopSelf(Context context) {
        LogUtils.d(TAG, "----forcestop by myself----");
        ((ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE)).forceStopPackage("com.prize.smartcleaner");
    }

    public static void forceStopPackage(Context context, String pkgName) {
        LogUtils.d(TAG, "F package :" + pkgName);
        ((ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE)).forceStopPackage(pkgName);
    }

    public static boolean restartTencentSwitch(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getBoolean("RestartTencentSwitch", true);
    }

    public static Intent getIntent(Context context, Intent i) {
        List<ResolveInfo> queryIntentServices = context.getPackageManager().queryIntentServices(i, 0);
        if (queryIntentServices == null || queryIntentServices.size() != 1) {
            return null;
        }
        ResolveInfo resolveInfo = (ResolveInfo) queryIntentServices.get(0);
        ComponentName componentName = new ComponentName(resolveInfo.serviceInfo.packageName, resolveInfo.serviceInfo.name);
        Intent intent = new Intent(i);
        intent.setComponent(componentName);
        LogUtils.d(TAG, "start service componentName : " + componentName.toString());
        return intent;
    }

    public static boolean isUserAMonkey() {
        return ActivityManager.isUserAMonkey();
    }

    public static boolean isPhoneInCall(Context context) {
        int state = TelephonyManager.getDefault().getCallState();
        if (state == TelephonyManager.CALL_STATE_IDLE) {
            LogUtils.d(TAG, "isPhoneInCall :" + false);
            return false;
        }
        LogUtils.d(TAG, "isPhoneInCall :" + true);
        return true;
    }

    public static void sendBroadcastForTrashClean(Context context, int i) {
//        Intent intent = new Intent("com.prize.cleandroid.ui.PRIZE_SAFE_CENTER_CLEAN_UP");
//        intent.putExtra("scan_from", i);
//        LogUtils.d(TAG, "sendBroadcast for trash clean!");
//        context.sendBroadcast(intent);
    }

    public static void sendBroadcastFinishRemoveTask(Context context) {
//        Intent intent = new Intent("com.prize.FinishRemoveTask");
//        LogUtils.d(TAG, "sendBroadcast for finishing remove task!");
//        context.sendBroadcast(intent);
    }

    public static String getTopActivityPackageName(ActivityManager manager) {
        String topActivityPackage = null;
        String topActivityClass = null;
        List<ActivityManager.RunningTaskInfo> runningTaskInfos = manager.getRunningTasks(1);
        if (runningTaskInfos != null) {
            ComponentName componentName = runningTaskInfos.get(0).topActivity;
            topActivityPackage = componentName.getPackageName();
            topActivityClass = componentName.getClassName();
        }
        return topActivityPackage;
    }

    private static ArrayList<String> getInfoFromXmlPullParser(File file) {
        FileInputStream fileInputStream = null;
        ArrayList<String> arrayList = new ArrayList();

        try {
            fileInputStream = new FileInputStream(file);
            XmlPullParser newPullParser = Xml.newPullParser();
            newPullParser.setInput(fileInputStream, null);
            int next;
            do {
                next = newPullParser.next();
                if (next == XmlPullParser.START_TAG) {
                    if ("p".equals(newPullParser.getName())) {
                        String attributeValue = newPullParser.getAttributeValue(null, "att");
                        if (attributeValue != null) {
                            arrayList.add(attributeValue);
                        }
                    }
                }
            } while (next != XmlPullParser.END_DOCUMENT);

        } catch (FileNotFoundException e) {
            LogUtils.i(TAG, "file not found exception " + e);
        } catch (XmlPullParserException e) {
            LogUtils.i(TAG, "xml pull parser exception " + e);
        } catch (IOException e) {
            LogUtils.i(TAG, "io exception " + e);
        } finally {
            if (fileInputStream != null) {
                try {
                    fileInputStream.close();
                } catch (IOException e) {
                    LogUtils.i(TAG, "Failed to close state FileInputStream " + e);
                }
            }
        }

        return arrayList;
    }

    public static ArrayList<Integer> getOrphanUidList(Context context, ArrayList<Integer> uidList) {
        ArrayList<Integer> orphanList = new ArrayList();
        File file = new File("/proc/");
        if (file == null || !file.exists()) {
            LogUtils.d(TAG, "can't read from /proc");
            return null;
        }
        File[] subFileList = file.listFiles(new MyFileFilter());
        if (subFileList != null) {
            for (File sub : subFileList) {
                if (sub != null) {
                    String name = sub.getName();
                    if (isOrphan(sub.getAbsolutePath() + "/status", uidList)) {
                        try {
                            orphanList.add(Integer.valueOf(Integer.parseInt(name)));
                        } catch (NumberFormatException e) {
                            LogUtils.e(TAG, "error parsing orphan pid " + e);
                        }
                    }
                }
            }
        }
        return orphanList;
    }

    private static boolean isOrphan(String dir, ArrayList<Integer> uidList) {
        InputStream fileInputStream = null;
        BufferedReader bufferedReader = null;
        InputStreamReader inputStreamReader = null;
        boolean isOrphan = false;
        try {
            if (new File(dir.toString()).exists()) {
                fileInputStream = new FileInputStream(dir.toString());
                try {
                    inputStreamReader = new InputStreamReader(fileInputStream);
                    try {
                        String readLine = "";
                        bufferedReader = new BufferedReader(inputStreamReader);
                        do {
                            try {
                                readLine = bufferedReader.readLine();
                                if (readLine != null) {
                                    if (readLine.startsWith("PPid:")) {
                                        readLine = readLine.substring("PPid:".length(), readLine.length()).trim();
                                        if (!readLine.equals("1")) {
                                            break;
                                        }
                                    }
                                }
                            } catch (Exception e) {
                                LogUtils.e(TAG, "error parsing orphan process uid " + e);
                            }
                        } while (!readLine.startsWith("Uid:"));
                        readLine = readLine.substring("Uid:".length(), readLine.length()).trim();
                        int uid = Integer.parseInt(readLine.substring(0, readLine.indexOf("\t")));
                        if (uidList.contains(Integer.valueOf(uid))) {
                            LogUtils.d(TAG, "find native orphan process dir=" + dir + ", uid=" + uid);
                            isOrphan = true;
                        }
                        if (fileInputStream != null) {
                            try {
                                fileInputStream.close();
                            } catch (IOException e) {
                            }
                        }
                        if (bufferedReader != null) {
                            try {
                                bufferedReader.close();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                        if (inputStreamReader != null) {
                            try {
                                inputStreamReader.close();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    } catch (Exception e) {
                        if (fileInputStream != null) {
                            try {
                                fileInputStream.close();
                            } catch (IOException e2) {
                            }
                        }
                        if (inputStreamReader != null) {
                            try {
                                inputStreamReader.close();
                            } catch (IOException e2) {
                                e2.printStackTrace();
                            }
                        }
                        return isOrphan;
                    }
                } catch (Exception e) {
                    if (fileInputStream != null) {
                        fileInputStream.close();
                    }
                    if (inputStreamReader != null) {
                        inputStreamReader.close();
                    }
                    return isOrphan;
                }
            }
        } catch (Exception e) {
            LogUtils.e(TAG, "open file failed " + e);
        }
        return isOrphan;
    }

    public static ArrayList<String> getImportantProviderPkg(Context context, String pkgName) {
        ArrayList<String> importantPkgList = new ArrayList();
        ArrayList<ActivityManager.RunningAppProcessInfo> p = getRunningAppProcesses(context);
        if (p == null || p.isEmpty() || pkgName == null) {
            return null;
        }
        int i = 0;
        Iterator it = p.iterator();
        while (it.hasNext()) {
            ActivityManager.RunningAppProcessInfo runningAppProcessInfo = (ActivityManager.RunningAppProcessInfo) it.next();
            if (runningAppProcessInfo.pkgList[0].equals(pkgName)) {
                i = runningAppProcessInfo.pid;
                break;
            }
        }
        if (i == 0) {
            return null;
        }
        Iterator it2 = p.iterator();
        while (it2.hasNext()) {
            ActivityManager.RunningAppProcessInfo runningAppProcessInfo = (ActivityManager.RunningAppProcessInfo) it2.next();
            if (runningAppProcessInfo.importanceReasonCode == ActivityManager.RunningAppProcessInfo.REASON_PROVIDER_IN_USE
                    && runningAppProcessInfo.importanceReasonPid == i) {
                for (int j = 0; j < runningAppProcessInfo.pkgList.length; j++) {
                    LogUtils.d(TAG, "backImportantProviderPkg=" + pkgName + " depend on " + runningAppProcessInfo.pkgList[j]);
                    importantPkgList.add(runningAppProcessInfo.pkgList[j]);
                }
            }
        }
        return importantPkgList;
    }

    public static String getProcessName(Context context, String pkgName) {
        ArrayList<ActivityManager.RunningAppProcessInfo> p = getRunningAppProcesses(context);
        if (p == null || p.isEmpty() || pkgName == null) {
            return null;
        }
        String processName = "";
        Iterator it = p.iterator();
        while (it.hasNext()) {
            ActivityManager.RunningAppProcessInfo runningAppProcessInfo = (ActivityManager.RunningAppProcessInfo) it.next();
            for (String pkg : runningAppProcessInfo.pkgList) {
                if (pkg.equals(pkgName)) {
                    processName = runningAppProcessInfo.processName;
                    break;
                }
            }
        }
        //LogUtils.d(TAG, "fore process : " + processName);
        return processName;
    }

    /**
     * set screen off clear alarm
     * @param context
     */
    public static void setScreenOffClearAlarm(Context context) {
        AlarmManager alarmMgr = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, PrizeClearSystemService.class);
        intent.setAction(PrizeClearSystemService.SCREEN_OFF_CLEAR);
        PendingIntent pendingIntent = PendingIntent.getService(context, 0, intent, 0);
        alarmMgr.cancel(pendingIntent);
        int min = 1;//new Random().nextInt(5) + 10; //[10, 15)
        long totalMillis = (long) (min * 60 * 1000);
        LogUtils.d(TAG, "setScreenOffClearAlarm ramdomMinute = " + min);
        alarmMgr.setExact(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + totalMillis, pendingIntent);
    }

    /**
     * cancel screen off clear alarm
     * @param context
     */
    public static void cancelScreenOffClearAlarm(Context context) {
        AlarmManager alarmMgr = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, PrizeClearSystemService.class);
        intent.setAction(PrizeClearSystemService.SCREEN_OFF_CLEAR);
        PendingIntent pendingIntent = PendingIntent.getService(context, 0, intent, 0);
        alarmMgr.cancel(pendingIntent);
        LogUtils.d(TAG, "------cancelScreenOffClearAlarm-----");
    }

    /**
     * set morning clear alarm
     * @param context
     */
    public static void setMorningClearAlarm(Context context) {
        AlarmManager alarmMgr = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, PrizeClearSystemService.class);
        intent.setAction(PrizeClearSystemService.MSSTART);
        PendingIntent pendingIntent = PendingIntent.getService(context, 0, intent, 0);
        alarmMgr.cancel(pendingIntent);
        int hour = getMorningClearRandomHour(context);
        int min = getMorningClearRandomMin(context);
        long totalSec = (long) (((hour * 60) + min) * 60);
        LogUtils.d(TAG, "ramdomHour = " + hour + " ramdomMinute = " + min);
        alarmMgr.setExact(AlarmManager.RTC_WAKEUP, getNextDayTime(System.currentTimeMillis(), totalSec), pendingIntent);
    }

    static long getNextDayTime(long millis, long totalSec) {
        return getNextDayTime(Calendar.getInstance(), millis, totalSec);
    }

    static long getNextDayTime(Calendar calendar, long millis, long totalSec) {
        calendar.setTimeInMillis(millis);
        int hour = ((int) totalSec) / 3600;
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        long sec = totalSec - ((long) (hour * 3600));
        int min = ((int) sec) / 60;
        calendar.set(Calendar.MINUTE, min);
        calendar.set(Calendar.SECOND, ((int) sec) - (min * 60));
        calendar.set(Calendar.MILLISECOND, 0);
        calendar.add(Calendar.DAY_OF_MONTH, 1);

        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int date = calendar.get(Calendar.DATE);
        int hourOfDay = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);
        int second = calendar.get(Calendar.SECOND);
        int millisecond = calendar.get(Calendar.MILLISECOND);
        LogUtils.d(TAG, "next morning clean : " + year + "-" + (month + 1) + "-" + date + " " + hourOfDay + ":" + minute + ":" + second + ":" + millisecond);

        return calendar.getTimeInMillis();
    }

    public static boolean isManuClearTrashing(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getBoolean("ManuClearTrashSwitch", true);
    }

    public static boolean isMorningClearTrashing(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getBoolean("MorningClearTrashSwitch", true);
    }

    public static boolean isTrimMemoring(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getBoolean("TrimMemorySwitch", true);
    }

    public static long getThreshClearPersist(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getLong("ThreshClearPersist", 800);
    }

    public static boolean isSystemApp(Context context, String packageName) {
        try {
            ApplicationInfo applicationInfo = context.getPackageManager().getApplicationInfo(packageName, PackageManager.GET_META_DATA);
            if (applicationInfo == null || (applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) == 0) {
                return false;
            }
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }

    public static ArrayList<String> getForceStopFilterPkgList(Context context) {
        ArrayList<String> list = new ArrayList();
        list.addAll(getRestartPkgList(context));
        list.addAll(getRemoveTaskFilterPkgList(context));
        return list;
    }

    public static ArrayList<String> getRestartPkgList(Context context) {
        File file = new File("/data/system/config/systemConfigList.xml");
        if (!file.exists()) {
            return getRestartPkgProList(context);
        }
        ArrayList<String> list = getListFromXml(file, "KillRestartServiceProNew");
        if (list == null || list.isEmpty()) {
            return getRestartPkgProList(context);
        }
        return list;
    }

    public static ArrayList<String> getRestartPkgProList(Context context) {
        if (context.getPackageManager().hasSystemFeature("prize.version.exp")) {
            return new ArrayList(mRestartPkgPro_Exp);
        }
        return new ArrayList(mRestartPkgPro);
    }

    public static ArrayList<String> getRemoveTaskFilterPkgList(Context context) {
        File file = new File("/data/system/config/systemConfigList.xml");
        if (!file.exists()) {
            return getRmTaskFilterPkgList(context);
        }
        ArrayList<String> list = getListFromXml(file, "RemoveTaskFilterPkgNew");
        if (list == null || list.isEmpty()) {
            return getRmTaskFilterPkgList(context);
        }
        return list;
    }

    public static ArrayList<String> getRmTaskFilterPkgList(Context context) {
        if (context.getPackageManager().hasSystemFeature("prize.version.exp")) {
            return new ArrayList(mRemoveTaskFilterPkgList_Exp);
        }
        return new ArrayList(mRemoveTaskFilterPkgList);
    }

    private static ArrayList<String> getListFromXml(File file, String name) {
        FileInputStream fileInputStream = null;
        ArrayList<String> arrayList = new ArrayList();
        if (!(name == null || name.isEmpty())) {
            try {
                fileInputStream = new FileInputStream(file);
                XmlPullParser newPullParser = Xml.newPullParser();
                newPullParser.setInput(fileInputStream, null);
                int next;
                do {
                    next = newPullParser.next();
                    if (next == 2 && name.equals(newPullParser.getName())) {
                        String nextText = newPullParser.nextText();
                        if (nextText != null) {
                            arrayList.add(nextText);
                        }
                    }
                } while (next != 1);
            } catch (Exception e) {

            } finally {
                if (fileInputStream != null) {
                    try {
                        fileInputStream.close();
                    } catch (IOException e2) {
                        LogUtils.i(TAG, "Failed to close state FileInputStream " + e2);
                    }
                }
            }
        }
        return arrayList;
    }

    public static boolean isRedundent(String str) {
        if (str == null || str.isEmpty()) {
            return false;
        }
        ArrayList<String> list = getRedundentList();
        if (list != null) {
            Iterator it = list.iterator();
            while (it.hasNext()) {
                String str2 = (String) it.next();
                if (str2 != null && str.contains(str2)) {
                    return true;
                }
            }
        }
        return false;
    }

    private static ArrayList<String> getRedundentList() {
        File file = new File("/data/system/config/systemConfigList.xml");
        if (!file.exists()) {
            return new ArrayList(mRedundentList);
        }
        ArrayList<String> list = getListFromXml(file, "RedundentTaskClass");
        if (list == null || list.isEmpty()) {
            return new ArrayList(mRedundentList);
        }
        return list;
    }

    public static String formatCount(int count) {
        return String.format(Locale.getDefault(), "%d", new Object[]{Integer.valueOf(count)});
    }

}