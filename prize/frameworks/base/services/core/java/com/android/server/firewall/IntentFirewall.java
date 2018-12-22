/*
 * Copyright (C) 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.server.firewall;

import android.app.AppGlobals;
import android.content.ComponentName;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.content.pm.IPackageManager;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.os.FileObserver;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteException;
import android.util.ArrayMap;
import android.util.Slog;
import android.util.Xml;
import com.android.internal.util.ArrayUtils;
import com.android.internal.util.XmlUtils;
import com.android.server.EventLogTags;
import com.android.server.IntentResolver;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

/*prize liuwei 2018-04-10 add for log wake up new process with service begin*/
import com.android.server.am.ActivityManagerService;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
/*prize liuwei 2018-04-10 add for log wake up new process with service end*/

/*prize add for feature: frozen app 2018-05-02 begin */
import android.os.ServiceManager;

import android.util.SparseArray;

import com.android.server.pm.PackageManagerService;
import android.content.pm.IPackageManager;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.os.SystemClock;
import android.content.Context;
import android.os.UserHandle;

import java.util.Calendar;
import android.text.format.DateUtils;

import com.android.server.ServiceThread;
import android.os.Handler;

import android.os.Process;
import android.hardware.display.DisplayManager;

import android.view.Display;
import android.content.pm.PackageInfo;
import android.provider.Settings;
import android.database.ContentObserver;
import android.text.TextUtils;
import android.net.Uri;
import android.annotation.UserIdInt;
import android.content.pm.ProviderInfo;

import java.io.PrintWriter;
import android.app.NotificationManager;
import android.app.INotificationManager;
import com.android.server.LocalServices;
import android.app.usage.UsageStatsManagerInternal;

import android.app.admin.DevicePolicyManager;
import android.telephony.TelephonyManager;
/*prize add for feature: frozen app 2018-05-02 end */

public class IntentFirewall {
    static final String TAG = "IntentFirewall";

    // e.g. /data/system/ifw or /data/secure/system/ifw
    private static final File RULES_DIR = new File(Environment.getDataSystemDirectory(), "ifw");

    private static final int LOG_PACKAGES_MAX_LENGTH = 150;
    private static final int LOG_PACKAGES_SUFFICIENT_LENGTH = 125;

    private static final String TAG_RULES = "rules";
    private static final String TAG_ACTIVITY = "activity";
    private static final String TAG_SERVICE = "service";
    private static final String TAG_BROADCAST = "broadcast";

    private static final int TYPE_ACTIVITY = 0;
    private static final int TYPE_BROADCAST = 1;
    private static final int TYPE_SERVICE = 2;

    /*prize add for feature: frozen app 2018-05-02 begin */
    private static final int TYPE_CONTENT_PROVIDER = 3;
    /*prize add for feature: frozen app 2018-05-02 end */

    private static final HashMap<String, FilterFactory> factoryMap;

    private final AMSInterface mAms;

    private final RuleObserver mObserver;

    private FirewallIntentResolver mActivityResolver = new FirewallIntentResolver();
    private FirewallIntentResolver mBroadcastResolver = new FirewallIntentResolver();
    private FirewallIntentResolver mServiceResolver = new FirewallIntentResolver();

    /*prize add for feature: frozen app 2018-05-02 begin */
    private final static int TYPE_KILLED = 0;
    private final static int TYPE_FORCESTOP = 1;
    private final static int TYPE_PROCESS_CACHE_EMPTY = 2;
    private final static int TYPE_PROCESS_BACKGROUP_LIMIT = 3;

    private final static int RULE_ALLOW = 0;
    private final static int RULE_BLOCK = 1;
    private final static int RULE_INVALID= -1;

    private final static int NOT_THIRD_PARTY_APP = 0;
    private final static int THIRD_PARTY_APP = 1;

    private final static int WHILE_RULE_ALLOW = 1;
    private final static int WHILE_RULE_BLOCK = 0;

    private final static long DISABLE_TEMP_BLOCK_TIME = 1000 * 60 * 60;  //60 mins
    private final static int UPGRADE_USAGE_STATS_TIME = 1;  // 1 hour
    private final static int FROZEN_APP_OCCUR_TIME = 10;  // 5 hours -> 10 hours
    private final static long KEEP_CACHE_LIST_TIME = 1000 * 60 * 60 * 3;  //3 hours


    private final static int SPECIAL_CONDITION_RULE = 100; // SpecialCondition //ruleFromCacheActivityList //ruleFromTempBlock //getRuleFromFrozenList //getRuleFromUsageStats
    private final static int CACHE_ACTIVITY_RULE = 200;
    private final static int TEMP_BLOCK_RULE = 300;
    private final static int FROZEN_RULE = 400;
    private final static int USAGE_STATS_RULE = 500;
    private final static int PASS_RULE = 1000;

    private final boolean PRIZE_FIREWALL_ENABLE = true;

    private SparseArray<Integer> mFrozenListUidByForeceOrKilled = new SparseArray<Integer>();
    private SparseArray<Integer> mFrozenListUidByUsageStats = new SparseArray<Integer>();
    private SparseArray<Integer> mWhileList = new SparseArray<Integer>();
    private SparseArray<Integer> mThirdPartyUidList = new SparseArray<Integer>();
    private SparseArray<Integer> mInputMethodList = new SparseArray<Integer>();
    private SparseArray<Integer> mWidgetList = new SparseArray<Integer>();
    private SparseArray<UidInfo> mTempBlockList = new SparseArray<UidInfo>();
    //private SparseArray<Integer> mCacheActivityList = new SparseArray<Integer>();
    private SparseArray<UidInfo> mCacheActivityList = new SparseArray<UidInfo>();

    private SparseArray<Integer> mCarrierPrivilegedApps = new SparseArray<Integer>();
    private SparseArray<Integer> mAdministorPackages = new SparseArray<Integer>();

    private PackageManagerService mPms;
    private boolean mBooted = false;
    private Context mContext;
    private Handler mFireWallHandler;
    private DisplayManager mDisplayManager;
    private long mLastUpGradeUsageStatsTime = 0;
    private UidInfo mInvalid = new UidInfo(0,0,0);

    private static final int sDateFormatFlags =
            DateUtils.FORMAT_SHOW_DATE
            | DateUtils.FORMAT_SHOW_TIME
            | DateUtils.FORMAT_SHOW_YEAR
            | DateUtils.FORMAT_NUMERIC_DATE;

    public static final String[] purebackground_defenablelist = {
        //tools
        "com.sdu.didi.psnger",
        "com.tencent.mobileqq",
        "com.tencent.mm",
        "cn.com.fetion",
        "com.alibaba.android.rimet",
        //sport
        "com.boohee.one",
        "com.codoon.gps",
        "com.hnjc.dl",
        "com.rjfittime.app",
        "co.runner.app",
        "com.bamboo.ibike",
        "com.lipian.gcwds",
    };

    public static final String[] disablelog_broadcast = {
        //tools
        "android.intent.action.BATTERY_CHANGED",
        "android.net.wifi.SCAN_RESULTS",
        "android.net.conn.CONNECTIVITY_CHANGE",
        "android.intent.action.TIME_TICK",
        "android.net.wifi.STATE_CHANGE",
        "android.intent.action.SIG_STR",
        "android.intent.action.CLOSE_SYSTEM_DIALOGS",
        "android.intent.action.USER_PRESENT",
    };

    public static final String[] black_broadcast_cache_activity = {
        //tools
        "android.intent.action.BATTERY_CHANGED",
        //"android.net.wifi.SCAN_RESULTS",
        //"android.intent.action.TIME_TICK",
        //"android.net.wifi.STATE_CHANGE",
        //"android.intent.action.SIG_STR",
        //"android.intent.action.CLOSE_SYSTEM_DIALOGS",
        //"android.intent.action.USER_PRESENT",
    };

    private static class UidInfo{
        public int myUid;
        public int processCount;
        public long lastExistTime;
        public int rule;
        public UidInfo(int uid, int processCount,long lastExistTime ){
            this.myUid = uid;
            this.processCount = processCount;
            this.lastExistTime = lastExistTime;
            this.rule = -1;
        }

        @Override
        public String toString() {
            return "UidInfo{uid:" + myUid + "/ processCount:" + processCount + " / lastExistTime:"+ lastExistTime+ " / rule:"+ rule+"}";
        }
    }

    private static class FireWallRule{
        public int blockRule;
        public int myUid;

        public FireWallRule(int uid){
            this.myUid = uid;
            blockRule = 0;
        }

        @Override
        public String toString() {
            return "FireWallRule{uid:" + myUid + " / blockRule:"+ blockRule+ "}";
        }
    }
    /*prize add for feature: frozen app 2018-05-02 end */

    static {
        FilterFactory[] factories = new FilterFactory[] {
                AndFilter.FACTORY,
                OrFilter.FACTORY,
                NotFilter.FACTORY,

                StringFilter.ACTION,
                StringFilter.COMPONENT,
                StringFilter.COMPONENT_NAME,
                StringFilter.COMPONENT_PACKAGE,
                StringFilter.DATA,
                StringFilter.HOST,
                StringFilter.MIME_TYPE,
                StringFilter.SCHEME,
                StringFilter.PATH,
                StringFilter.SSP,

                CategoryFilter.FACTORY,
                SenderFilter.FACTORY,
                SenderPackageFilter.FACTORY,
                SenderPermissionFilter.FACTORY,
                PortFilter.FACTORY
        };

        // load factor ~= .75
        factoryMap = new HashMap<String, FilterFactory>(factories.length * 4 / 3);
        for (int i=0; i<factories.length; i++) {
            FilterFactory factory = factories[i];
            factoryMap.put(factory.getTagName(), factory);
        }
    }

    /*prize add for feature: frozen app 2018-05-02 begin */
    private final DisplayManager.DisplayListener mDisplayListener
            = new DisplayManager.DisplayListener() {
        @Override public void onDisplayAdded(int displayId) {
        }

        @Override public void onDisplayRemoved(int displayId) {
        }

        @Override public void onDisplayChanged(int displayId) {
            if(mDisplayManager != null){
                boolean isScreenOn = mDisplayManager.getDisplay(Display.DEFAULT_DISPLAY).getState() == Display.STATE_ON;
                if (displayId == Display.DEFAULT_DISPLAY && isScreenOn) {
                    mFireWallHandler.post(DisplayChangeRunnable);
                }
            }

        }
    };
    /*prize add for feature: frozen app 2018-05-02 end */

    public IntentFirewall(AMSInterface ams, Handler handler) {
        mAms = ams;
        mHandler = new FirewallHandler(handler.getLooper());
        File rulesDir = getRulesDir();
        rulesDir.mkdirs();

        readRulesDir(rulesDir);

        mObserver = new RuleObserver(rulesDir);
        mObserver.startWatching();

        /*prize add for feature: frozen app 2018-05-02 begin */
        ServiceThread mHandlerThread = new ServiceThread(TAG,
                Process.THREAD_PRIORITY_FOREGROUND, false /*allowIo*/);
        mHandlerThread.start();
        mFireWallHandler =  new Handler(mHandlerThread.getLooper());
        /*prize add for feature: frozen app 2018-05-02 end */
    }

    /*prize add for feature: frozen app 2018-05-02 begin */
    Runnable DisplayChangeRunnable = new Runnable() {
        @Override
        public void run() {

            if(!isFireWallEnable()) return;

            updateCarrier();
            updateAdminPkg();

            synchronized (IntentFirewall.this) {
                long compare = getUpgradeCompareTime();
                if(compare > mLastUpGradeUsageStatsTime){
                    Slog.e(TAG, "upgrade usagestatus list");
                    updateGradeListFromUsageStatsService(Process.myPid(),true);
                    upgradeWhileList(true);
                }

            }
        }
    };

    private void updateCarrier(){
        TelephonyManager telephonyManager = mContext.getSystemService(TelephonyManager.class);
        setupMapInvalid(mCarrierPrivilegedApps);

        if(telephonyManager != null){
            List<String> tmpPkgs = telephonyManager.getPackagesWithCarrierPrivileges();
            for(String pkg: tmpPkgs){
                int uid = mPms.getPackageUid(pkg,PackageManager.MATCH_UNINSTALLED_PACKAGES, UserHandle.USER_SYSTEM);
                if(uid > 10000){
                    mCarrierPrivilegedApps.put(uid,RULE_ALLOW);
                }
            }
        }

    }

    private void updateAdminPkg(){
        setupMapInvalid(mAdministorPackages);
        DevicePolicyManager dpm = mContext.getSystemService(DevicePolicyManager.class);
        if (dpm != null){
            List<ComponentName> curAdmins = dpm.getActiveAdmins();
            if (curAdmins != null) {
                for(ComponentName cn: curAdmins){
                    int uid = mPms.getPackageUid(cn.getPackageName(),PackageManager.MATCH_UNINSTALLED_PACKAGES, UserHandle.USER_SYSTEM);
                    if(uid > 10000){
                        mAdministorPackages.put(uid,RULE_ALLOW);
                    }
                }
            }
        }
    }

    private void setupMapInvalid(SparseArray<Integer> hashList){
        for(int i = 0, nsize = hashList.size(); i < nsize; i++){
            hashList.put(hashList.keyAt(i),RULE_INVALID);
        }
    }

    Runnable BootUsageStats = new Runnable() {
        @Override
        public void run() {
            synchronized (IntentFirewall.this) {
                if(!PRIZE_FIREWALL_ENABLE) return;
                checkPms();
                updateGradeListFromUsageStatsService(10050,true);
                upgradeWhileList(true);
                upgradeInputList();
                setupThirdAppUidInfo();
                mBooted = true;
            }

        }
    };

    Runnable UpdateUsageStats = new Runnable() {
        @Override
        public void run() {
            synchronized (IntentFirewall.this) {
                if(!isFireWallEnable()) return;
                checkPms();
                updateGradeListFromUsageStatsService(10050,false);
                upgradeWhileList(false);
            }

        }
    };

    public void reportProcessStart(final int uid){
        if(!isFireWallEnable()) return;
        mFireWallHandler.post(new Runnable() {
            @Override
            public void run() {
                synchronized (IntentFirewall.this) {
                    onReportProcessStart(uid);
                }
            }
        });
    }

    public void upgradeSingleAppUsageStats(final String pkg){
        mFireWallHandler.post(new Runnable() {
            @Override
            public void run() {
                synchronized (IntentFirewall.this) {
                    Slog.e(TAG, "notifycation update pkg:"+pkg);
                    onUpgradeSingleAppUsageStats(pkg);
                }
            }
        });
    }

    private void setupThirdAppUidInfo(){
        List<Integer> uidList = mPms.setupThirdPartyApp();
        for(int uid:uidList){
            mThirdPartyUidList.put(uid,NOT_THIRD_PARTY_APP);
        }
    }

    private void onReportProcessStart(final int uid){
        if(!isThirdPartyForUid(uid)){
            return;
        }

        UidInfo uidInfo = mTempBlockList.get(uid,mInvalid);
        if(!isInvalidUidInfo(uidInfo)){
            if(uidInfo.processCount >= 0){
                uidInfo.processCount++;
                uidInfo.lastExistTime = 0;
                Slog.e(TAG, "onReportProcessStart uidInfo:"+uidInfo);
            }

            return;
        }

        //create a new uidinfo object
        uidInfo = createUidInfo(uid);
        uidInfo.processCount = 1;
        uidInfo.lastExistTime = 0;
        Slog.e(TAG, "onReportProcessStart uidInfo:"+uidInfo);
        mTempBlockList.put(uid,uidInfo);
    }

    private void onReportProcessDied(final int uid,final boolean mainProcess){
        if(!isThirdPartyForUid(uid)){
            return;
        }

        if(mainProcess){
            Slog.e(TAG, "onReportProcessDied main process died uid:"+uid);
            onFrozenListBlock(uid);
            //onMainProcessDied(uid);
        }

        UidInfo uidInfo = mTempBlockList.get(uid,mInvalid);
        if(!isInvalidUidInfo(uidInfo)){
            if(uidInfo.processCount >0){
                uidInfo.processCount--;
            }

            if(uidInfo.processCount == 0){
                //uidInfo.lastExistTime = SystemClock.elapsedRealtime();
                uidInfo.lastExistTime = System.currentTimeMillis();
                cacheActivityClear(uid);
            }
            Slog.e(TAG, "onReportProcessDied uidInfo:"+uidInfo);
            return;
        }

    }

    private void onMainProcessDied(final int uid){
        mFireWallHandler.post(new Runnable() {
            @Override
            public void run() {
                synchronized (IntentFirewall.this) {
                    if(!isThirdPartyForUid(uid)){
                        return;
                    }

                    UidInfo uidInfo  = mCacheActivityList.get(uid,mInvalid);
                    onUpdateCache(uid,uidInfo,"onMainProcessDied");
                }
            }
        });
    }

    private void resetTempBlockIfNeed(int receiveUid){
        UidInfo uidInfo = mTempBlockList.get(receiveUid,mInvalid);
        if(!isInvalidUidInfo(uidInfo)){
            if(uidInfo.processCount >= 0){
                uidInfo.lastExistTime = 0; //reset time for allow temp block
            }
        }

    }

    private UidInfo createUidInfo(final int uid){
        return new UidInfo(uid,0,0);
    }

    private boolean isInvalidUidInfo(UidInfo uidInfo){
        return uidInfo == mInvalid;
    }

    private void onUpgradeSingleAppUsageStats(final String pkg){
        if(!isFireWallEnable()) return;
        checkPms();
        int uid = mPms.getPackageUid(pkg,PackageManager.MATCH_UNINSTALLED_PACKAGES,
                    UserHandle.USER_SYSTEM);
        if(!isThirdPartyForUid(uid)){
            return;
        }

        //onFrozenListAllow(uid);
        resetTempBlockIfNeed(uid);
        mFrozenListUidByUsageStats.put(uid,RULE_ALLOW);
    }

    private void upgradeWhileList(boolean log){
        if(log) Slog.i(TAG, "upgradeWhileList enter");
        for(String pkg:purebackground_defenablelist){
            final int myUserId = UserHandle.myUserId();
            PackageInfo pi = mPms.getPackageInfo(pkg, 0, myUserId);
            if(pi == null) continue;

            if(pi.applicationInfo != null && pi.applicationInfo.uid > 10000){
                if(log) Slog.i(TAG, "publicWhileList package:"+pkg+" uid:"+pi.applicationInfo.uid);
                mWhileList.put(pi.applicationInfo.uid,WHILE_RULE_ALLOW);
            }
        }

    }

    private void upgradeInputList(){
        Slog.i(TAG, "upgradeInputList enter");
        handleInputMethodPkg();
        mContext.getContentResolver().registerContentObserver(Settings.Secure.getUriFor(Settings.Secure.DEFAULT_INPUT_METHOD),false,mInputContentObserver);
    }

    private ContentObserver mInputContentObserver = new ContentObserver(mFireWallHandler){
        @Override
        public void onChange(boolean selfChange, Uri uri, @UserIdInt int userId) {
            Slog.i(TAG, "mInputContentObserver onChange");
            mFireWallHandler.post(new Runnable() {
                @Override
                public void run() {
                    handleInputMethodPkg();
                }
            });

        }
    };

    private void handleInputMethodPkg(){
        mInputMethodList.clear();
        String inputPkg = getDefaultInputMethodPkgName(mContext);
        int inputUid = mPms.getPackageUid(inputPkg,PackageManager.MATCH_UNINSTALLED_PACKAGES,
                    UserHandle.USER_SYSTEM);
        Slog.i(TAG, "mInputContentObserver handleInputMethodPkg inputPkg:"+inputPkg+" inputUid:"+inputUid);
        mInputMethodList.put(inputUid,WHILE_RULE_ALLOW);
    }

    private String getDefaultInputMethodPkgName(Context context) {
        String mDefaultInputMethodPkg = null;

        String mDefaultInputMethodCls = Settings.Secure.getString(
                context.getContentResolver(),
                Settings.Secure.DEFAULT_INPUT_METHOD);
        //input method class info
        Slog.d(TAG, "mDefaultInputMethodCls=" + mDefaultInputMethodCls);
        if (!TextUtils.isEmpty(mDefaultInputMethodCls)) {
            //input method package info
            mDefaultInputMethodPkg = mDefaultInputMethodCls.split("/")[0];
            Slog.d(TAG, "mDefaultInputMethodPkg=" + mDefaultInputMethodPkg);
        }
        return mDefaultInputMethodPkg;
    }

    public void updateListFromKilled(final int uid, final boolean mainProcess){
        if(!isFireWallEnable()) return;
        mFireWallHandler.post(new Runnable() {
            @Override
            public void run() {
                synchronized (IntentFirewall.this) {
                    if(uid < 10000){
                        return;
                    }

                    onReportProcessDied(uid,mainProcess);
                }
            }
        });
    }

    public void updateCacheActivity(final int uid){
        if(!isFireWallEnable() || uid < 10020) return;

        mFireWallHandler.post(new Runnable() {
            @Override
            public void run() {
                synchronized (IntentFirewall.this) {
                    if(!isThirdPartyForUid(uid)){
                        return;
                    }

                    UidInfo uidInfo  = mCacheActivityList.get(uid,mInvalid);
                    onUpdateCache(uid,uidInfo,"updateCacheActivity");
                }
            }
        });

    }

    public void updateCacheActivityFromHomeKey(final String pkgName){
        checkPms();
        if(!isFireWallEnable()) return;

        mFireWallHandler.post(new Runnable() {
            @Override
            public void run() {
                synchronized (IntentFirewall.this) {
                    final int uid = mPms.getPackageUid(pkgName,PackageManager.MATCH_UNINSTALLED_PACKAGES,UserHandle.USER_SYSTEM);
                    if(uid < 10020) return;

                    if(!isThirdPartyForUid(uid)){
                        return;
                    }

                    UidInfo uidInfo  = mCacheActivityList.get(uid,mInvalid);
                    onFrozenListAllow(uid);
                    onUpdateCache(uid,uidInfo,"updateCacheActivityFromHomeKey");
                }
            }
        });

    }

    public void updateBserviceStats(final int uid){
        if(!isFireWallEnable() || uid < 10020) return;

        mFireWallHandler.post(new Runnable() {
            @Override
            public void run() {
                synchronized (IntentFirewall.this) {
                    if(!isThirdPartyForUid(uid)){
                        return;
                    }

                    UidInfo uidInfo  = mCacheActivityList.get(uid,mInvalid);
                    onUpdateCache(uid,uidInfo,"updateBserviceStats");
                }
            }
        });

    }

    private void onUpdateCache(final int uid,UidInfo uidInfo,String reason){
        int block = mFrozenListUidByForeceOrKilled.get(uid,RULE_INVALID);
        if(block == RULE_BLOCK){
            Slog.e(TAG, "onUpdateCache uid:"+uid+" "+reason+" ignore, because of frozen!");
            return;
        }

        mFireWallHandler.post(UpdateUsageStats);
        if(!isInvalidUidInfo(uidInfo)){
            if(uidInfo.rule == RULE_BLOCK && !reason.equals("updateCacheActivityFromHomeKey")){
                return;
            }

            uidInfo.rule = RULE_BLOCK;
            uidInfo.lastExistTime = System.currentTimeMillis();
            Slog.e(TAG, reason + " uidInfo:"+uidInfo);
            return;
        }

        uidInfo = createUidInfo(uid);
        uidInfo.processCount = -1;
        uidInfo.lastExistTime = System.currentTimeMillis();
        uidInfo.rule = RULE_BLOCK;
        Slog.e(TAG, "create "+reason + " uidInfo:"+uidInfo);
        mCacheActivityList.put(uid,uidInfo);
    }



    public void updateListFromForceStop(final int uid){
        if(!isFireWallEnable()) return;
        if(!isThirdPartyForUid(uid)){
            return;
        }

        cacheActivityClear(uid);
        putFrozenListWithUid(uid,TYPE_FORCESTOP);
    }

    private boolean isFireWallEnable(){
        return mBooted && PRIZE_FIREWALL_ENABLE;
    }

    public void updateProcessRecordBackgroupLimit(final int uid){

        if(!isFireWallEnable()) return;

        if(!isThirdPartyForUid(uid)){
            return;
        }

        onProcessRecordBackgroupLimit(uid);
    }

    private void onProcessRecordBackgroupLimit(final int uid){
        boolean notificationEnable = isNoficationEnable(uid);

        if(notificationEnable){
            UidInfo uidInfo  = mCacheActivityList.get(uid,mInvalid);
            onUpdateCache(uid,uidInfo,"onProcessRecordBackgroupLimit");
            return;
        }

        cacheActivityClear(uid);
        int block = mFrozenListUidByForeceOrKilled.get(uid,RULE_INVALID);
        if(block == RULE_BLOCK){
            return;
        }

        Slog.e(TAG, "onProcessRecordBackgroupLimit uid:"+uid);
        putFrozenListWithUid(uid,TYPE_PROCESS_BACKGROUP_LIMIT);
    }

    private boolean isNoficationEnable(final int uid){
        checkPms();

        INotificationManager service = NotificationManager.getService();
        try {
             return service.areNotificationsEnabledForPackage(mPms.getNameForUid(uid),uid);
        } catch (RemoteException e) {
            /* ignore - local call */
        }

        return false;
    }

    public void updateListFromCacheEmpty(final int uid, final int pid){
        if(!isFireWallEnable() || uid < 10020) return;
        if(!isThirdPartyForUid(uid)){
            return;
        }

        cacheActivityClear(uid);
        int block = mFrozenListUidByForeceOrKilled.get(uid,RULE_INVALID);
        if(block == RULE_BLOCK){
            return;
        }
        Slog.e(TAG, "updateListFromCacheEmpty uid:"+uid+" pid:"+pid);
        mFireWallHandler.post(UpdateUsageStats);
        putFrozenListWithUid(uid,TYPE_PROCESS_CACHE_EMPTY);
    }

    private void putFrozenListWithUid(final int uid, final int type){

        mFireWallHandler.post(new Runnable() {
            @Override
            public void run() {
                synchronized (IntentFirewall.this) {
                    checkPms();

                    if(TYPE_FORCESTOP == type){
                        upgradeUidInfoForUid(uid);
                    }

                    onFrozenListBlock(uid);
                    //mFrozenListUidByForeceOrKilled.put(uid,RULE_BLOCK);
                }
            }
        });

    }

    private void cacheActivityClear(final int uid){
        mCacheActivityList.put(uid,mInvalid);
    }

    private void upgradeUidInfoForUid(final int uid){
        UidInfo uidInfo = mTempBlockList.get(uid,mInvalid);
        if(!isInvalidUidInfo(uidInfo)){
            if(uidInfo.processCount >= 0){
                uidInfo.processCount = 0;
                uidInfo.lastExistTime = 0;
                Slog.e(TAG, "updateListFromForceStop uidInfo:"+uidInfo+" reset because of forcestop");
            }

        }
    }


    private boolean isFronzenListBlock(int uid){
        int block = mFrozenListUidByForeceOrKilled.get(uid,RULE_INVALID);
        return block == RULE_BLOCK;
    }

    public void nofityBootComplete(Context context){
        Slog.e(TAG, "notify boot complete");
        this.mContext = context;
        mFireWallHandler.post(BootUsageStats);

        mDisplayManager = (DisplayManager)context.getSystemService(Context.DISPLAY_SERVICE);
        mDisplayManager.registerDisplayListener(mDisplayListener, null);
        UsageStatsManagerInternal mUsageStats = LocalServices.getService(UsageStatsManagerInternal.class);
    }

    public boolean isWorking(){
        return mBooted;
    }

    private void onFrozenListAllow(final int uid){
        int block = mFrozenListUidByForeceOrKilled.get(uid,RULE_INVALID);
        if(block == RULE_ALLOW){
            return;
        }

        mFrozenListUidByForeceOrKilled.put(uid,RULE_ALLOW);
    }

    private void onFrozenListBlock(final int uid){
        int block = mFrozenListUidByForeceOrKilled.get(uid,RULE_INVALID);
        if(block == RULE_BLOCK){
            return;
        }

        mFrozenListUidByForeceOrKilled.put(uid,RULE_BLOCK);
    }
    /*prize add for feature: frozen app 2018-05-02 end */

    /**
     * This is called from ActivityManager to check if a start activity intent should be allowed.
     * It is assumed the caller is already holding the global ActivityManagerService lock.
     */
    public boolean checkStartActivity(Intent intent, int callerUid, int callerPid,
            String resolvedType, ApplicationInfo resolvedApp) {
        /*prize add for feature: frozen app 2018-05-02 begin */
        resetTempBlockIfNeed(resolvedApp.uid);
        onFrozenListAllow(resolvedApp.uid);
        cacheActivityClear(resolvedApp.uid);
        /*prize add for feature: frozen app 2018-05-02 end */
        return checkIntent(mActivityResolver, intent.getComponent(), TYPE_ACTIVITY, intent,
                callerUid, callerPid, resolvedType, resolvedApp.uid);
    }

    /*prize add for feature: frozen app 2018-05-02 begin */
    private void updateGradeListFromUsageStatsService(int receivingUid,boolean force){
        if(receivingUid < 10020 && !force){
            return;
        }

        UsageStatsManager mUsageStatsManager=(UsageStatsManager)mContext.getSystemService(Context.USAGE_STATS_SERVICE);
        Calendar calendar = Calendar.getInstance();
        long endTime = calendar.getTimeInMillis();
        calendar.add(Calendar.YEAR, -1);
        long startTime = calendar.getTimeInMillis();
        List<UsageStats> list=mUsageStatsManager.queryUsageStats(UsageStatsManager.INTERVAL_YEARLY, startTime, endTime);

        long compareTime = getCompareTime();
        for(UsageStats us:list){
            if(force){
                Slog.e(TAG, "updateGradeListFromUsageStatsService pkg:"+us.getPackageName()+" now:"+DateUtils.formatDateTime(mContext, compareTime, sDateFormatFlags)+" Format:"+DateUtils.formatDateTime(mContext, us.getLastTimeUsed(), sDateFormatFlags));
            }

            int uid = mPms.getPackageUid(us.getPackageName(),PackageManager.MATCH_UNINSTALLED_PACKAGES,
                                UserHandle.USER_SYSTEM);
            if(uid > 1000 && uid < 10000){
                if(force) Slog.i(TAG, "updateGradeListFromUsageStatsService system app uid:"+uid+" RULE_ALLOW");
                mFrozenListUidByUsageStats.put(uid,RULE_ALLOW);
                continue;
            }

            if(uid > 10000){
                boolean allow = compareResult(compareTime,us.getLastTimeUsed());

                if(allow){
                    if(force) Slog.i(TAG, "updateGradeListFromUsageStatsService uid:"+uid+" RULE_ALLOW");
                    mFrozenListUidByUsageStats.put(uid,RULE_ALLOW);
                    continue;
                }

                if(force) Slog.i(TAG, "updateGradeListFromUsageStatsService uid:"+uid+" RULE_BLOCK");
                mFrozenListUidByUsageStats.put(uid,RULE_BLOCK);
            }

        }

        mLastUpGradeUsageStatsTime = getNewTime();

    }

    private boolean compareResult(long cmp1, long cmp2){
        Calendar calendarCmp1 = Calendar.getInstance();
        calendarCmp1.setTimeInMillis(cmp1);
        Calendar calendarCmp2 = Calendar.getInstance();
        calendarCmp2.setTimeInMillis(cmp2);

        return compareItem(calendarCmp1,calendarCmp2);
    }

    private boolean compareItem(Calendar calendarCmp1,Calendar calendarCmp2){

        Calendar c = Calendar.getInstance();
        c.set(2010,1,1);
        if(compareDetail(c,calendarCmp2,Calendar.YEAR)){
            return false;
        }

        if(calendarCmp1.getTimeInMillis() > calendarCmp2.getTimeInMillis()){
            return false;
        }

        return true;

    }

    private boolean compareDetail(Calendar calendarCmp1,Calendar calendarCmp2,int type){
        //Slog.i(TAG, "updateGradeListFromUsageStatsService compareDetail cmp1:"+calendarCmp1.get(type)+" cmp2:"+calendarCmp2.get(type));
        if(calendarCmp1.get(type) > calendarCmp2.get(type)){
            return true;
        }

        return false;
    }

    private long getNewTime(){
        Calendar mCalendar = Calendar.getInstance();
        return mCalendar.getTimeInMillis();
    }

    private long getUpgradeCompareTime(){
        Calendar mCalendar = Calendar.getInstance();
        mCalendar.add(Calendar.HOUR_OF_DAY, -UPGRADE_USAGE_STATS_TIME);  //1 hour ago
        //mCalendar.add(Calendar.MINUTE, -1);   // 1 minute ago
        return mCalendar.getTimeInMillis();
    }

    private long getCompareTime(){
        Calendar mCalendar = Calendar.getInstance();
        //mCalendar.add(Calendar.DAY_OF_YEAR, -1);  //1 day ago
        mCalendar.add(Calendar.HOUR_OF_DAY, -FROZEN_APP_OCCUR_TIME);  //16 hours ago
        return mCalendar.getTimeInMillis();
    }

    private int getBlockRule(int receiveUid, int callingUid, int type, Intent intent, FireWallRule rule,ComponentName resolved){

        checkPms();

        if(handleSpecialCondition(receiveUid,callingUid,type, intent,rule) == RULE_ALLOW){
            return RULE_ALLOW;
        }

        int block = ruleFromCacheActivityList(intent,receiveUid,callingUid,type,rule,resolved);
        if(block == RULE_BLOCK){
            return block;
        }

        block = ruleFromTempBlock(receiveUid,rule);
        if(block == RULE_BLOCK){
            return block;
        }


        block = getRuleFromFrozenList(receiveUid,rule);
        if(!isBlockInvalid(block)){
            return block;
        }

        block = getRuleFromUsageStats(receiveUid,rule);
        if(!isBlockInvalid(block)){
            return block;
        }

        passRule(rule);
        return RULE_ALLOW;
    }

    private void passRule(FireWallRule rule){
        rule.blockRule = PASS_RULE;
    }

    private int getRuleFromFrozenList(int receiveUid,FireWallRule rule){
        rule.blockRule = FROZEN_RULE;
        return mFrozenListUidByForeceOrKilled.get(receiveUid,RULE_INVALID);
    }

    private int handleSpecialCondition(int receiveUid, int callingUid, int type,Intent intent,FireWallRule rule){
        if(!isFireWallEnable()) return RULE_ALLOW;

        rule.blockRule = SPECIAL_CONDITION_RULE;

        if(handleSystemCall(receiveUid,callingUid,type,intent) == RULE_ALLOW) return RULE_ALLOW;

        if(!isThirdPartyForUid(receiveUid)) return RULE_ALLOW;

        if(getRuleFromWhile(receiveUid) == WHILE_RULE_ALLOW) return RULE_ALLOW;

        if(getRuleFromInputMethod(receiveUid) == WHILE_RULE_ALLOW) return RULE_ALLOW;

        if(isActiveDeviceAdmin(receiveUid)) return RULE_ALLOW;

        if(isCarrierApp(receiveUid)) return RULE_ALLOW;

        return RULE_INVALID;
    }

    private int ruleFromCacheActivityList(Intent intent,int receiveUid,int callingUid,int type,FireWallRule rule,ComponentName resolved){
        rule.blockRule = CACHE_ACTIVITY_RULE;
        UidInfo uidInfo  = mCacheActivityList.get(receiveUid,mInvalid);
        if(!isInvalidUidInfo(uidInfo)){
            if(getUidInfoRule(uidInfo,receiveUid) == RULE_BLOCK){
                if(type == TYPE_BROADCAST){
                    return onCacheActivityBroadcast(intent,receiveUid,callingUid,resolved);
                }

                return onCacheActivityServiceOrProvider(receiveUid,callingUid);
            }
        }

        return RULE_ALLOW;
    }

    private int getUidInfoRule(UidInfo uidInfo, final int receiveUid){
        if(uidInfo.rule != RULE_BLOCK){
            return RULE_ALLOW;
        }

        final long now = System.currentTimeMillis();
        long distance = now - uidInfo.lastExistTime;
        if(distance < KEEP_CACHE_LIST_TIME ){
            return RULE_BLOCK;
        }

        if(distance >= KEEP_CACHE_LIST_TIME ){
            //cacheActivityClear(receiveUid);
            onFrozenListBlock(receiveUid);
        }

        return RULE_ALLOW;
    }

    private int onCacheActivityServiceOrProvider(int receiveUid,int callingUid){
        if(callingUid == Process.SYSTEM_UID){
            return RULE_ALLOW;
        }

        if(receiveUid == callingUid){
            return RULE_ALLOW;
        }

        return RULE_BLOCK;
    }

    private int onCacheActivityBroadcast(Intent intent,int receiveUid,int callingUid,ComponentName resolved){
        if(callingUid == Process.SYSTEM_UID){
            return onBlackCacheActivityBroadcast(intent);
        }

        if(resolved != null){
            return RULE_ALLOW;
        }

        if(receiveUid == callingUid){
            return RULE_ALLOW;
        }

        return RULE_BLOCK;
    }

    private int onBlackCacheActivityBroadcast(Intent intent){
        if(intent != null && !TextUtils.isEmpty(intent.getAction())){
            for(String str : black_broadcast_cache_activity){
                if(str.equals(intent.getAction())){
                    return RULE_BLOCK;
                }
            }
        }

        return RULE_ALLOW;
    }

    public void dumpStats(PrintWriter pw,String uidStr){
        int uid = 0;
        try {
            uid = Integer.parseInt(uidStr);
        } catch (RuntimeException e) {
            pw.println("uidStr convert err!!!");
        }

        if(!isThirdPartyForUid(uid)) return;

        if(true){
            pw.println("dumpstats third uid list: \n");
            for(int i = 0, nsize = mThirdPartyUidList.size(); i < nsize; i++){
                pw.println("dumpstats  uid:"+ mThirdPartyUidList.keyAt(i)+" value:"+mThirdPartyUidList.get(mThirdPartyUidList.keyAt(i),RULE_INVALID));
            }

            pw.println("dumpstats mAdministorPackages uid list: \n");  //mAdministorPackages
            for(int i = 0, nsize = mAdministorPackages.size(); i < nsize; i++){
                pw.println("dumpstats  mAdministorPackages uid:"+ mAdministorPackages.keyAt(i)+" value:"+mAdministorPackages.get(mAdministorPackages.keyAt(i),RULE_INVALID));
            }

            pw.println("dumpstats mCarrierPrivilegedApps uid list: \n");  //mCarrierPrivilegedApps
            for(int i = 0, nsize = mCarrierPrivilegedApps.size(); i < nsize; i++){
                pw.println("dumpstats  mCarrierPrivilegedApps uid:"+ mCarrierPrivilegedApps.keyAt(i)+" value:"+mCarrierPrivilegedApps.get(mCarrierPrivilegedApps.keyAt(i),RULE_INVALID));
            }

            pw.println("dumpstats uid packageName:"+mPms.getNameForUid(uid));
            pw.println("dumpstats mWhileList uid:"+uid+" rule:"+mWhileList.get(uid,RULE_INVALID));
            pw.println("dumpstats mWidgetList uid:"+uid+" rule:"+mWidgetList.get(uid,RULE_INVALID));
            pw.println("dumpstats mInputMethodList uid:"+uid+" rule:"+mInputMethodList.get(uid,RULE_INVALID));
            pw.println("dumpstats CACHE_ACTIVITY_RULE uid:"+uid+" rule:"+mCacheActivityList.get(uid,mInvalid));
            pw.println("dumpstats TEMP_BLOCK_RULE uid:"+uid+" rule:"+mTempBlockList.get(uid,mInvalid));
            pw.println("dumpstats FROZEN_RULE uid:"+uid+" rule:"+mFrozenListUidByForeceOrKilled.get(uid,RULE_INVALID));
            pw.println("dumpstats USAGE_STATS_RULE uid:"+uid+" rule:"+mFrozenListUidByUsageStats.get(uid,RULE_INVALID));
        }

    }


    public void printFreshUsageStats(PrintWriter pw){
        UsageStatsManager mUsageStatsManager=(UsageStatsManager)mContext.getSystemService(Context.USAGE_STATS_SERVICE);
        Calendar calendar = Calendar.getInstance();
        long endTime = calendar.getTimeInMillis();
        calendar.add(Calendar.YEAR, -1);
        long startTime = calendar.getTimeInMillis();
        List<UsageStats> list=mUsageStatsManager.queryUsageStats(UsageStatsManager.INTERVAL_YEARLY, startTime, endTime);

        long compareTime = getCompareTime();
        for(UsageStats us:list){
            pw.println("updateGradeListFromUsageStatsService pkg:"+us.getPackageName()+" now:"+DateUtils.formatDateTime(mContext, compareTime, sDateFormatFlags)+" Format:"+DateUtils.formatDateTime(mContext, us.getLastTimeUsed(), sDateFormatFlags));

            int uid = mPms.getPackageUid(us.getPackageName(),PackageManager.MATCH_UNINSTALLED_PACKAGES,
                                UserHandle.USER_SYSTEM);
            if(uid > 1000 && uid < 10000){
                pw.println("updateGradeListFromUsageStatsService system app uid:"+uid+" RULE_ALLOW");
                mFrozenListUidByUsageStats.put(uid,RULE_ALLOW);
                continue;
            }

            if(uid > 10000){
                boolean allow = compareResult(compareTime,us.getLastTimeUsed());

                if(allow){
                    pw.println("updateGradeListFromUsageStatsService uid:"+uid+" pkg:"+us.getPackageName()+" RULE_ALLOW");
                    mFrozenListUidByUsageStats.put(uid,RULE_ALLOW);
                    continue;
                }

                pw.println("updateGradeListFromUsageStatsService uid:"+uid+" pkg:"+us.getPackageName()+" RULE_BLOCK");
                mFrozenListUidByUsageStats.put(uid,RULE_BLOCK);
            }

        }

        mLastUpGradeUsageStatsTime = getNewTime();

    }

    private int ruleFromTempBlock(int receiveUid,FireWallRule rule){
        rule.blockRule = TEMP_BLOCK_RULE;
        UidInfo uidInfo = mTempBlockList.get(receiveUid,mInvalid);
        if(!isInvalidUidInfo(uidInfo)){
            //Slog.i(TAG, "ruleFromTempBlock uidInfo:"+uidInfo);
            if(uidInfo.processCount == 0 && uidInfo.lastExistTime > 0){
                final long now = System.currentTimeMillis();
                long distance = now - uidInfo.lastExistTime;
                Slog.i(TAG, "ruleFromTempBlock uidInfo:"+uidInfo+" distance:"+distance+" DISABLE_TEMP_BLOCK_TIME:"+DISABLE_TEMP_BLOCK_TIME);
                if(distance < DISABLE_TEMP_BLOCK_TIME ){
                    return RULE_BLOCK;
                }

                if(distance >= DISABLE_TEMP_BLOCK_TIME ){
                    onFrozenListAllow(receiveUid);
                }
            }
        }

        return RULE_ALLOW;
    }

    private int handleSystemCall(int receiveUid, int callingUid, int type,Intent intent){
        if(type == TYPE_SERVICE || type == TYPE_CONTENT_PROVIDER){
            if(callingUid == Process.SYSTEM_UID) return RULE_ALLOW;
            if(serviceWidget(receiveUid) == WHILE_RULE_ALLOW) return RULE_ALLOW;
            return RULE_INVALID;
        }

        //TYPE_BROADCAST
        if(broadcastWidget(intent,receiveUid,callingUid) == WHILE_RULE_ALLOW){
            return RULE_ALLOW;
        }

        return RULE_INVALID;
    }

    private int serviceWidget(int receiveUid){
        int block = mWidgetList.get(receiveUid,RULE_INVALID);
        if(!isBlockInvalid(block)){
            return block;
        }

        return RULE_INVALID;
    }


    private int broadcastWidget(Intent intent, int receiveUid, int callingUid){
        if(TextUtils.isEmpty(intent.getAction())){
            return WHILE_RULE_BLOCK;
        }

        if("android.appwidget.action.APPWIDGET_ENABLED".equals(intent.getAction())){
            Slog.i(TAG, "handleWidget android.appwidget.action.APPWIDGET_ENABLED widget uid:"+receiveUid);
            mWidgetList.put(receiveUid,WHILE_RULE_ALLOW);
            return WHILE_RULE_ALLOW;
        }

        if("android.appwidget.action.APPWIDGET_DISABLED".equals(intent.getAction())){
            Slog.i(TAG, "handleWidget android.appwidget.action.APPWIDGET_DISABLED widget uid:"+receiveUid);
            mWidgetList.put(receiveUid,RULE_INVALID);
            return WHILE_RULE_ALLOW;
        }

        int block = mWidgetList.get(receiveUid,RULE_INVALID);
        if(!isBlockInvalid(block)){
            return block;
        }

        if(callingUid == Process.SYSTEM_UID && receiveUid < 10000) return WHILE_RULE_ALLOW;
        //if(callingUid == Process.SYSTEM_UID) return WHILE_RULE_ALLOW;

        return RULE_INVALID;
    }

    private int getRuleFromInputMethod(int receiveUid){
        int block = mInputMethodList.get(receiveUid,RULE_INVALID);
        if(!isBlockInvalid(block)){
            return block;
        }

        return WHILE_RULE_BLOCK;
    }

    private boolean isActiveDeviceAdmin(int receiveUid) {
        int block = mAdministorPackages.get(receiveUid,RULE_INVALID);
        if(!isBlockInvalid(block)){
            return true;
        }

        return false;
    }

    private boolean isCarrierApp(int receiveUid) {
        int block = mCarrierPrivilegedApps.get(receiveUid,RULE_INVALID);
        if(!isBlockInvalid(block)){
            return true;
        }

        return false;
    }

    private int getRuleFromWhile(int receiveUid){
        int block = mWhileList.get(receiveUid,RULE_INVALID);
        if(!isBlockInvalid(block)){
            return block;
        }

        return WHILE_RULE_BLOCK;
    }

    private int getRuleFromUsageStats(int receiveUid,FireWallRule rule){
        int block = -1;
        rule.blockRule = USAGE_STATS_RULE;
        block = mFrozenListUidByUsageStats.get(receiveUid,RULE_INVALID);
        if(!isBlockInvalid(block)){
            return block;
        }

        updateGradeListFromUsageStatsService(receiveUid,false);

        block = mFrozenListUidByUsageStats.get(receiveUid,RULE_INVALID);
        if(!isBlockInvalid(block)){
            return block;
        }

        mFrozenListUidByUsageStats.put(receiveUid,RULE_BLOCK);
        return RULE_BLOCK;
    }

    private boolean isThirdPartyForUid(int receiveUid){
        if(receiveUid < 10020) return false;

        int isThirdApp = mThirdPartyUidList.get(receiveUid,RULE_INVALID);

        if(isThirdApp == RULE_INVALID){ //can't find uid
            //Slog.i(TAG, "isThirdParty receiveUid:"+receiveUid+" ?"+mPms.isThirdPartyApp(receiveUid));
            mThirdPartyUidList.put(receiveUid,THIRD_PARTY_APP);
        }

        return mThirdPartyUidList.get(receiveUid,RULE_INVALID) == THIRD_PARTY_APP;
    }


    private void checkPms(){
        if(mPms == null){
            mPms = (PackageManagerService) ServiceManager.getService("package");
        }
    }

    private boolean isBlockInvalid(int block){
        return block == RULE_INVALID;
    }
    /*prize add for feature: frozen app 2018-05-02 end */

    /*prize liuwei 2018-04-10 add for log wake up new process with service begin*/
    public boolean checkService(ComponentName resolvedService, Intent intent, int callerUid,
            int callerPid, String resolvedType, ApplicationInfo resolvedApp,boolean isNewProcess, boolean exported) {
         /*prize add for feature: frozen app 2018-05-02 begin */
         int receivingUid = resolvedApp.uid;
         FireWallRule rule = new FireWallRule(receivingUid);
         int block = getBlockRule(receivingUid,callerUid,TYPE_SERVICE,intent,rule,resolvedService);

         if(block == RULE_BLOCK){
            Slog.e(TAG, "block service Component:"+resolvedService+" Intent :"+intent+" uid:"+resolvedApp.uid+" callerUid:"+callerUid+" exported:"+exported+" rule:"+rule);
            return false;
         }
         /*prize add for feature: frozen app 2018-05-02 end */
    /*prize liuwei 2018-04-10 add for log wake up new process with service end*/
        if(callerUid != resolvedApp.uid){
            if(isNewProcess){
                Slog.i(TAG, "service Component:"+resolvedService+" Intent :"+intent+" uid:"+resolvedApp.uid+" callerUid:"+callerUid+" exported:"+exported);
            }
        }
        /*prize liuwei 2018-04-10 add for log wake up new process with service end*/

        return checkIntent(mServiceResolver, resolvedService, TYPE_SERVICE, intent, callerUid,
                callerPid, resolvedType, resolvedApp.uid);
    }

    /*prize add for feature: frozen app 2018-05-02 begin */
    public boolean checkContentProvider(int callerUid,int receivingUid, ProviderInfo pi) {
        FireWallRule rule = new FireWallRule(receivingUid);
        int block = getBlockRule(receivingUid,callerUid,TYPE_CONTENT_PROVIDER,null,rule,null);

        if(block == RULE_BLOCK){
            Slog.e(TAG, "block contentProvider Component:"+pi+"  uid:"+receivingUid+" callerUid:"+callerUid+" exported:"+pi.exported+" rule:"+rule);
            return false;
        }

        return true;
    }
    /*prize add for feature: frozen app 2018-05-02 end */

    public boolean checkBroadcast(Intent intent, ComponentName resolvedBroadcast,int callerUid, int callerPid,
            String resolvedType, int receivingUid) {
         /*prize add for feature: frozen app 2018-05-02 begin */
         FireWallRule rule = new FireWallRule(receivingUid);
         int block = getBlockRule(receivingUid,callerUid,TYPE_BROADCAST,intent,rule,resolvedBroadcast);

         if(block == RULE_BLOCK){
            //disablelog_broadcast
            if (!TextUtils.isEmpty(intent.getAction())) {
                boolean found = false;
                for(String log:disablelog_broadcast){
                    if(log.equals(intent.getAction())){
                        found = true;
                        break;
                    }
                }

                if(!found){
                    Slog.e(TAG, "block Broadcast Intent :"+intent+" componentName:"+resolvedBroadcast+" uid:"+receivingUid+" callerUid:"+callerUid+" rule:"+rule);
                }

            }

            return false;
         }
         /*prize add for feature: frozen app 2018-05-02 end */

        return checkIntent(mBroadcastResolver, intent.getComponent(), TYPE_BROADCAST, intent,
                callerUid, callerPid, resolvedType, receivingUid);
    }

    public boolean checkIntent(FirewallIntentResolver resolver, ComponentName resolvedComponent,
            int intentType, Intent intent, int callerUid, int callerPid, String resolvedType,
            int receivingUid) {
        boolean log = false;
        boolean block = false;

        // For the first pass, find all the rules that have at least one intent-filter or
        // component-filter that matches this intent
        List<Rule> candidateRules;
        candidateRules = resolver.queryIntent(intent, resolvedType, false, 0);
        if (candidateRules == null) {
            candidateRules = new ArrayList<Rule>();
        }
        resolver.queryByComponent(resolvedComponent, candidateRules);

        // For the second pass, try to match the potentially more specific conditions in each
        // rule against the intent
        for (int i=0; i<candidateRules.size(); i++) {
            Rule rule = candidateRules.get(i);
            if (rule.matches(this, resolvedComponent, intent, callerUid, callerPid, resolvedType,
                    receivingUid)) {
                block |= rule.getBlock();
                log |= rule.getLog();

                // if we've already determined that we should both block and log, there's no need
                // to continue trying rules
                if (block && log) {
                    break;
                }
            }
        }

        if (log) {
            logIntent(intentType, intent, callerUid, resolvedType);
        }

        return !block;
    }

    private static void logIntent(int intentType, Intent intent, int callerUid,
            String resolvedType) {
        // The component shouldn't be null, but let's double check just to be safe
        ComponentName cn = intent.getComponent();
        String shortComponent = null;
        if (cn != null) {
            shortComponent = cn.flattenToShortString();
        }

        String callerPackages = null;
        int callerPackageCount = 0;
        IPackageManager pm = AppGlobals.getPackageManager();
        if (pm != null) {
            try {
                String[] callerPackagesArray = pm.getPackagesForUid(callerUid);
                if (callerPackagesArray != null) {
                    callerPackageCount = callerPackagesArray.length;
                    callerPackages = joinPackages(callerPackagesArray);
                }
            } catch (RemoteException ex) {
                Slog.e(TAG, "Remote exception while retrieving packages", ex);
            }
        }

        EventLogTags.writeIfwIntentMatched(intentType, shortComponent, callerUid,
                callerPackageCount, callerPackages, intent.getAction(), resolvedType,
                intent.getDataString(), intent.getFlags());
    }

    /**
     * Joins a list of package names such that the resulting string is no more than
     * LOG_PACKAGES_MAX_LENGTH.
     *
     * Only full package names will be added to the result, unless every package is longer than the
     * limit, in which case one of the packages will be truncated and added. In this case, an
     * additional '-' character will be added to the end of the string, to denote the truncation.
     *
     * If it encounters a package that won't fit in the remaining space, it will continue on to the
     * next package, unless the total length of the built string so far is greater than
     * LOG_PACKAGES_SUFFICIENT_LENGTH, in which case it will stop and return what it has.
     */
    private static String joinPackages(String[] packages) {
        boolean first = true;
        StringBuilder sb = new StringBuilder();
        for (int i=0; i<packages.length; i++) {
            String pkg = packages[i];

            // + 1 length for the comma. This logic technically isn't correct for the first entry,
            // but it's not critical.
            if (sb.length() + pkg.length() + 1 < LOG_PACKAGES_MAX_LENGTH) {
                if (!first) {
                    sb.append(',');
                } else {
                    first = false;
                }
                sb.append(pkg);
            } else if (sb.length() >= LOG_PACKAGES_SUFFICIENT_LENGTH) {
                return sb.toString();
            }
        }
        if (sb.length() == 0 && packages.length > 0) {
            String pkg = packages[0];
            // truncating from the end - the last part of the package name is more likely to be
            // interesting/unique
            return pkg.substring(pkg.length() - LOG_PACKAGES_MAX_LENGTH + 1) + '-';
        }
        return null;
    }

    public static File getRulesDir() {
        return RULES_DIR;
    }

    /**
     * Reads rules from all xml files (*.xml) in the given directory, and replaces our set of rules
     * with the newly read rules.
     *
     * We only check for files ending in ".xml", to allow for temporary files that are atomically
     * renamed to .xml
     *
     * All calls to this method from the file observer come through a handler and are inherently
     * serialized
     */
    private void readRulesDir(File rulesDir) {
        FirewallIntentResolver[] resolvers = new FirewallIntentResolver[3];
        for (int i=0; i<resolvers.length; i++) {
            resolvers[i] = new FirewallIntentResolver();
        }

        File[] files = rulesDir.listFiles();
        if (files != null) {
            for (int i=0; i<files.length; i++) {
                File file = files[i];

                if (file.getName().endsWith(".xml")) {
                    readRules(file, resolvers);
                }
            }
        }

        Slog.i(TAG, "Read new rules (A:" + resolvers[TYPE_ACTIVITY].filterSet().size() +
                " B:" + resolvers[TYPE_BROADCAST].filterSet().size() +
                " S:" + resolvers[TYPE_SERVICE].filterSet().size() + ")");

        synchronized (mAms.getAMSLock()) {
            mActivityResolver = resolvers[TYPE_ACTIVITY];
            mBroadcastResolver = resolvers[TYPE_BROADCAST];
            mServiceResolver = resolvers[TYPE_SERVICE];
        }
    }

    /**
     * Reads rules from the given file and add them to the given resolvers
     */
    private void readRules(File rulesFile, FirewallIntentResolver[] resolvers) {
        // some temporary lists to hold the rules while we parse the xml file, so that we can
        // add the rules all at once, after we know there weren't any major structural problems
        // with the xml file
        List<List<Rule>> rulesByType = new ArrayList<List<Rule>>(3);
        for (int i=0; i<3; i++) {
            rulesByType.add(new ArrayList<Rule>());
        }

        FileInputStream fis;
        try {
            fis = new FileInputStream(rulesFile);
        } catch (FileNotFoundException ex) {
            // Nope, no rules. Nothing else to do!
            return;
        }

        try {
            XmlPullParser parser = Xml.newPullParser();

            parser.setInput(fis, null);

            XmlUtils.beginDocument(parser, TAG_RULES);

            int outerDepth = parser.getDepth();
            while (XmlUtils.nextElementWithin(parser, outerDepth)) {
                int ruleType = -1;

                String tagName = parser.getName();
                if (tagName.equals(TAG_ACTIVITY)) {
                    ruleType = TYPE_ACTIVITY;
                } else if (tagName.equals(TAG_BROADCAST)) {
                    ruleType = TYPE_BROADCAST;
                } else if (tagName.equals(TAG_SERVICE)) {
                    ruleType = TYPE_SERVICE;
                }

                if (ruleType != -1) {
                    Rule rule = new Rule();

                    List<Rule> rules = rulesByType.get(ruleType);

                    // if we get an error while parsing a particular rule, we'll just ignore
                    // that rule and continue on with the next rule
                    try {
                        rule.readFromXml(parser);
                    } catch (XmlPullParserException ex) {
                        Slog.e(TAG, "Error reading an intent firewall rule from " + rulesFile, ex);
                        continue;
                    }

                    rules.add(rule);
                }
            }
        } catch (XmlPullParserException ex) {
            // if there was an error outside of a specific rule, then there are probably
            // structural problems with the xml file, and we should completely ignore it
            Slog.e(TAG, "Error reading intent firewall rules from " + rulesFile, ex);
            return;
        } catch (IOException ex) {
            Slog.e(TAG, "Error reading intent firewall rules from " + rulesFile, ex);
            return;
        } finally {
            try {
                fis.close();
            } catch (IOException ex) {
                Slog.e(TAG, "Error while closing " + rulesFile, ex);
            }
        }

        for (int ruleType=0; ruleType<rulesByType.size(); ruleType++) {
            List<Rule> rules = rulesByType.get(ruleType);
            FirewallIntentResolver resolver = resolvers[ruleType];

            for (int ruleIndex=0; ruleIndex<rules.size(); ruleIndex++) {
                Rule rule = rules.get(ruleIndex);
                for (int i=0; i<rule.getIntentFilterCount(); i++) {
                    resolver.addFilter(rule.getIntentFilter(i));
                }
                for (int i=0; i<rule.getComponentFilterCount(); i++) {
                    resolver.addComponentFilter(rule.getComponentFilter(i), rule);
                }
            }
        }
    }

    static Filter parseFilter(XmlPullParser parser) throws IOException, XmlPullParserException {
        String elementName = parser.getName();

        FilterFactory factory = factoryMap.get(elementName);

        if (factory == null) {
            throw new XmlPullParserException("Unknown element in filter list: " + elementName);
        }
        return factory.newFilter(parser);
    }

    /**
     * Represents a single activity/service/broadcast rule within one of the xml files.
     *
     * Rules are matched against an incoming intent in two phases. The goal of the first phase
     * is to select a subset of rules that might match a given intent.
     *
     * For the first phase, we use a combination of intent filters (via an IntentResolver)
     * and component filters to select which rules to check. If a rule has multiple intent or
     * component filters, only a single filter must match for the rule to be passed on to the
     * second phase.
     *
     * In the second phase, we check the specific conditions in each rule against the values in the
     * intent. All top level conditions (but not filters) in the rule must match for the rule as a
     * whole to match.
     *
     * If the rule matches, then we block or log the intent, as specified by the rule. If multiple
     * rules match, we combine the block/log flags from any matching rule.
     */
    private static class Rule extends AndFilter {
        private static final String TAG_INTENT_FILTER = "intent-filter";
        private static final String TAG_COMPONENT_FILTER = "component-filter";
        private static final String ATTR_NAME = "name";

        private static final String ATTR_BLOCK = "block";
        private static final String ATTR_LOG = "log";

        private final ArrayList<FirewallIntentFilter> mIntentFilters =
                new ArrayList<FirewallIntentFilter>(1);
        private final ArrayList<ComponentName> mComponentFilters = new ArrayList<ComponentName>(0);
        private boolean block;
        private boolean log;

        @Override
        public Rule readFromXml(XmlPullParser parser) throws IOException, XmlPullParserException {
            block = Boolean.parseBoolean(parser.getAttributeValue(null, ATTR_BLOCK));
            log = Boolean.parseBoolean(parser.getAttributeValue(null, ATTR_LOG));

            super.readFromXml(parser);
            return this;
        }

        @Override
        protected void readChild(XmlPullParser parser) throws IOException, XmlPullParserException {
            String currentTag = parser.getName();

            if (currentTag.equals(TAG_INTENT_FILTER)) {
                FirewallIntentFilter intentFilter = new FirewallIntentFilter(this);
                intentFilter.readFromXml(parser);
                mIntentFilters.add(intentFilter);
            } else if (currentTag.equals(TAG_COMPONENT_FILTER)) {
                String componentStr = parser.getAttributeValue(null, ATTR_NAME);
                if (componentStr == null) {
                    throw new XmlPullParserException("Component name must be specified.",
                            parser, null);
                }

                ComponentName componentName = ComponentName.unflattenFromString(componentStr);
                if (componentName == null) {
                    throw new XmlPullParserException("Invalid component name: " + componentStr);
                }

                mComponentFilters.add(componentName);
            } else {
                super.readChild(parser);
            }
        }

        public int getIntentFilterCount() {
            return mIntentFilters.size();
        }

        public FirewallIntentFilter getIntentFilter(int index) {
            return mIntentFilters.get(index);
        }

        public int getComponentFilterCount() {
            return mComponentFilters.size();
        }

        public ComponentName getComponentFilter(int index) {
            return mComponentFilters.get(index);
        }
        public boolean getBlock() {
            return block;
        }

        public boolean getLog() {
            return log;
        }
    }

    private static class FirewallIntentFilter extends IntentFilter {
        private final Rule rule;

        public FirewallIntentFilter(Rule rule) {
            this.rule = rule;
        }
    }

    private static class FirewallIntentResolver
            extends IntentResolver<FirewallIntentFilter, Rule> {
        @Override
        protected boolean allowFilterResult(FirewallIntentFilter filter, List<Rule> dest) {
            return !dest.contains(filter.rule);
        }

        @Override
        protected boolean isPackageForFilter(String packageName, FirewallIntentFilter filter) {
            return true;
        }

        @Override
        protected FirewallIntentFilter[] newArray(int size) {
            return new FirewallIntentFilter[size];
        }

        @Override
        protected Rule newResult(FirewallIntentFilter filter, int match, int userId) {
            return filter.rule;
        }

        @Override
        protected void sortResults(List<Rule> results) {
            // there's no need to sort the results
            return;
        }

        public void queryByComponent(ComponentName componentName, List<Rule> candidateRules) {
            Rule[] rules = mRulesByComponent.get(componentName);
            if (rules != null) {
                candidateRules.addAll(Arrays.asList(rules));
            }
        }

        public void addComponentFilter(ComponentName componentName, Rule rule) {
            Rule[] rules = mRulesByComponent.get(componentName);
            rules = ArrayUtils.appendElement(Rule.class, rules, rule);
            mRulesByComponent.put(componentName, rules);
        }

        private final ArrayMap<ComponentName, Rule[]> mRulesByComponent =
                new ArrayMap<ComponentName, Rule[]>(0);
    }

    final FirewallHandler mHandler;

    private final class FirewallHandler extends Handler {
        public FirewallHandler(Looper looper) {
            super(looper, null, true);
        }

        @Override
        public void handleMessage(Message msg) {
            readRulesDir(getRulesDir());
        }
    };

    /**
     * Monitors for the creation/deletion/modification of any .xml files in the rule directory
     */
    private class RuleObserver extends FileObserver {
        private static final int MONITORED_EVENTS = FileObserver.CREATE|FileObserver.MOVED_TO|
                FileObserver.CLOSE_WRITE|FileObserver.DELETE|FileObserver.MOVED_FROM;

        public RuleObserver(File monitoredDir) {
            super(monitoredDir.getAbsolutePath(), MONITORED_EVENTS);
        }

        @Override
        public void onEvent(int event, String path) {
            if (path.endsWith(".xml")) {
                // we wait 250ms before taking any action on an event, in order to dedup multiple
                // events. E.g. a delete event followed by a create event followed by a subsequent
                // write+close event
                mHandler.removeMessages(0);
                mHandler.sendEmptyMessageDelayed(0, 250);
            }
        }
    }

    /**
     * This interface contains the methods we need from ActivityManagerService. This allows AMS to
     * export these methods to us without making them public, and also makes it easier to test this
     * component.
     */
    public interface AMSInterface {
        int checkComponentPermission(String permission, int pid, int uid,
                int owningUid, boolean exported);
        Object getAMSLock();
    }

    /**
     * Checks if the caller has access to a component
     *
     * @param permission If present, the caller must have this permission
     * @param pid The pid of the caller
     * @param uid The uid of the caller
     * @param owningUid The uid of the application that owns the component
     * @param exported Whether the component is exported
     * @return True if the caller can access the described component
     */
    boolean checkComponentPermission(String permission, int pid, int uid, int owningUid,
            boolean exported) {
        return mAms.checkComponentPermission(permission, pid, uid, owningUid, exported) ==
                PackageManager.PERMISSION_GRANTED;
    }

    boolean signaturesMatch(int uid1, int uid2) {
        try {
            IPackageManager pm = AppGlobals.getPackageManager();
            return pm.checkUidSignatures(uid1, uid2) == PackageManager.SIGNATURE_MATCH;
        } catch (RemoteException ex) {
            Slog.e(TAG, "Remote exception while checking signatures", ex);
            return false;
        }
    }

}
