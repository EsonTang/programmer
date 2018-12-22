package com.android.systemui.recents.utils;

/**
 * Created by prize on 2018/1/12.
 */

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;
import android.net.Uri;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.android.systemui.R;
import com.mediatek.systemui.statusbar.util.FeatureOptions;

import android.os.Handler;
import android.provider.Settings.System;

public class AppLockManager {

    private static final String TAG = "AppLockManager";
    private static final boolean DEBUG = true;
    private int DEFAULT_LOCK_APP_LIMIT = FeatureOptions.LOW_RAM_SUPPORT ? 2 : 5;
    private static AppLockManager mAppLockManager = null;
    public static final String separator = "#";
    private SharedPreferences mSharedPreferences;
    private static final String SHARED_PRE_NAME = "Configuration";
    private Context mContext;
    private ArrayList<String> mDefaultLockedApps;
    private ArrayList<String> mBackupLockedApps;
    private ArrayList<String> mRedundantTasks;
    private PrizeFileUtil mPrizeFileUtil;
    private ArrayList<String> mForbidLockApps;
    private ArrayList<String> mLockedApps;
    private static final String LOCK_APP_LIMIT = "lock_app_limit";
    private boolean isSmartEnable = false; //省电模式
    private static final String SMART_NAME = "is_smart_enable";
    private static final Uri mSmartUrl = System.getUriFor(SMART_NAME);
    private final Object object = new Object();

    private AppLockManager(Context context) {
        mContext = context;
        mPrizeFileUtil = PrizeFileUtil.getInstance();
        mSharedPreferences = context.getSharedPreferences(SHARED_PRE_NAME, Context.MODE_PRIVATE);
        mPrizeFileUtil.init();
        mRedundantTasks = new ArrayList(Arrays.asList(context.getResources().getStringArray(R.array.redundant_tasks)));
        mLockedApps = mPrizeFileUtil.getInfoFromXml(PrizeFileUtil.PATH, PrizeFileUtil.LOCKED);
        mForbidLockApps = mPrizeFileUtil.getInfoFromXml(PrizeFileUtil.PATH, PrizeFileUtil.FORBID);
        mDefaultLockedApps = mPrizeFileUtil.getInfoFromXml(PrizeFileUtil.PATH, PrizeFileUtil.DEFAULT_LOCKED);
        mBackupLockedApps = mPrizeFileUtil.getInfoFromXml(PrizeFileUtil.PATH, PrizeFileUtil.BACKUP_LOCKED);
        copyLockedAppsFromBackup();
        if (mLockedApps.size() == 0 || mForbidLockApps.size() == 0 || mDefaultLockedApps.size() == 0) {
            String[] lock_applications = context.getResources().getStringArray(R.array.lock_applications);
            String[] lock_applications_exp = context.getResources().getStringArray(R.array.lock_applications_exp);
            String[] forbid_lock_applications = context.getResources().getStringArray(R.array.forbid_lock_applications);
            getDefaultLockForbidList(Arrays.asList(lock_applications), Arrays.asList(forbid_lock_applications), DEFAULT_LOCK_APP_LIMIT);
            mPrizeFileUtil.saveInfoToXml(PrizeFileUtil.PATH, PrizeFileUtil.LOCKED, mLockedApps);
            mPrizeFileUtil.saveInfoToXml(PrizeFileUtil.PATH, PrizeFileUtil.FORBID, mForbidLockApps);
        }
    }

    public static AppLockManager getInstance(Context context) {
        if (mAppLockManager == null) {
            mAppLockManager = new AppLockManager(context);
        }
        return mAppLockManager;
    }

    public void getDefaultLockForbidList(List<String> lockPkgs, List<String> forbidPkgs, int limit) {

        if (DEBUG) {
            Log.d(TAG, "setLockAppConfs() lockPkgs:" + lockPkgs + ",forbidPkgs:" + forbidPkgs + ",mLockedApps:" + mLockedApps + ",mFobidLockApps " + mForbidLockApps);
        }
        List<String> lockedApps = isSmartEnable ? mBackupLockedApps : mLockedApps;
        if (lockPkgs != null) {
            for (String str : mDefaultLockedApps) {
                if (!(lockPkgs.contains(str) || !lockedApps.contains(str))) {
                    lockedApps.remove(str);
                }
            }
            mDefaultLockedApps.clear();
            for (String pkg : lockPkgs) {
                if (!pkg.contains(separator)) {
                    pkg = formatConversion(pkg, 0);
                }
                if (!(lockedApps.contains(pkg))) {
                    lockedApps.add(pkg);
                }
                mDefaultLockedApps.add(pkg);
            }
        }
        if (forbidPkgs != null) {
            mForbidLockApps.clear();
            for (String forbid : forbidPkgs) {
                mForbidLockApps.add(forbid);
                if (!forbid.contains(separator)) {
                    String h = formatConversion(forbid, 0);
                    if (mLockedApps.contains(h)) {
                        mLockedApps.remove(h);
                    }
                    forbid = formatConversion(forbid, 1);
                    if (mLockedApps.contains(forbid)) {
                        mLockedApps.remove(forbid);
                    }
                } else if (mLockedApps.contains(forbid)) {
                    mLockedApps.remove(forbid);
                }
            }
            mPrizeFileUtil.saveInfoToXml(PrizeFileUtil.PATH, PrizeFileUtil.FORBID, mForbidLockApps);
        }

        if (DEFAULT_LOCK_APP_LIMIT != limit) {
            DEFAULT_LOCK_APP_LIMIT = limit;
            mSharedPreferences.edit().putInt(LOCK_APP_LIMIT, DEFAULT_LOCK_APP_LIMIT).apply();
        }

        if (isSmartEnable) {
            mPrizeFileUtil.saveInfoToXml(PrizeFileUtil.PATH, PrizeFileUtil.BACKUP_LOCKED, lockedApps);
        } else {
            mPrizeFileUtil.saveInfoToXml(PrizeFileUtil.PATH, PrizeFileUtil.LOCKED, lockedApps);
        }
        mPrizeFileUtil.saveInfoToXml(PrizeFileUtil.PATH, PrizeFileUtil.DEFAULT_LOCKED, mDefaultLockedApps);
        if (DEBUG) {
            Log.d(TAG, "setLockAppConfs() mLockedApps:" + mLockedApps + ",mFobidLockApps " + mForbidLockApps + ", DEFAULT_LOCK_APP_LIMIT:" + DEFAULT_LOCK_APP_LIMIT);
        }
    }

    public boolean lockApplication(String pkgName, int userId, String className) {
        if (DEBUG) {
            Log.d(TAG, "lockApplication() packageName:" + pkgName + ", userId=" + userId + ", mLockedApps=" + mLockedApps + ",mFobidLockApps:" + mForbidLockApps + ",DEFAULT_LOCK_APP_LIMIT:" + DEFAULT_LOCK_APP_LIMIT);
        }
        if (isSmartEnable) {
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(mContext, R.string.prize_smart_enable_forbid_lock_message, Toast.LENGTH_SHORT).show();
                }
            });
            return false;
        } else if (mForbidLockApps.contains(pkgName) || isRedundantTask(className)) {
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(mContext, R.string.prize_forbid_lock_toast_message, Toast.LENGTH_SHORT).show();
                }
            });
            return false;
        } else {
            String format = formatConversion(pkgName, userId);
            if (mDefaultLockedApps.contains(format) || getLockedAppCount() < DEFAULT_LOCK_APP_LIMIT) {
                synchronized (object) {
                    if (!mLockedApps.contains(format)) {
                        if (DEBUG) {
                            Log.d(TAG, "LOCK : " + format);
                        }
                        mLockedApps.add(format);
                        mPrizeFileUtil.saveInfoToXml(PrizeFileUtil.PATH, PrizeFileUtil.LOCKED, mLockedApps);
                    }
                }
                return true;
            }
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(mContext, R.string.prize_lock_too_much_toast_message, Toast.LENGTH_SHORT).show();
                }
            });
            return false;
        }
    }

    public boolean unlockApplication(String pkgName, int userId) {
        if (DEBUG) {
            Log.d(TAG, "unlockApplication() packageName:" + pkgName + ", userId=" + userId + ", mLockedApps=" + mLockedApps);
        }
        String format = formatConversion(pkgName, userId);
        synchronized (object) {
            if (mLockedApps.contains(format)) {
                Log.d(TAG, "UNLOCK : " + format);
                mLockedApps.remove(format);
                mPrizeFileUtil.saveInfoToXml(PrizeFileUtil.PATH, PrizeFileUtil.LOCKED, mLockedApps);
            }
        }
        return true;
    }

    /**
     * 省电模式处理方法
     */
    public void initContentObserver() {
        mContext.getContentResolver().registerContentObserver(mSmartUrl, false, null);
        if (1 == System.getInt(mContext.getContentResolver(), SMART_NAME, 0)) {
            copyLockedAppsToBackup();
            isSmartEnable = true;
        }
        if (DEBUG) {
            Log.d(TAG, "initContentObserver end, isSmartEnableOn:" + isSmartEnable);
        }
    }


    private void copyLockedAppsToBackup() {
        if (mLockedApps.size() > 0) {
            mBackupLockedApps = new ArrayList(mLockedApps);
            mPrizeFileUtil.saveInfoToXml(PrizeFileUtil.PATH, PrizeFileUtil.BACKUP_LOCKED, mBackupLockedApps);
            mLockedApps.clear();
            mPrizeFileUtil.saveInfoToXml(PrizeFileUtil.PATH, PrizeFileUtil.LOCKED, mLockedApps);
        }
    }

    private void copyLockedAppsFromBackup() {
        if (mBackupLockedApps.size() > 0) {
            mLockedApps.addAll(mBackupLockedApps);
            mPrizeFileUtil.saveInfoToXml(PrizeFileUtil.PATH, PrizeFileUtil.LOCKED, mLockedApps);
            mBackupLockedApps.clear();
            mPrizeFileUtil.saveInfoToXml(PrizeFileUtil.PATH, PrizeFileUtil.BACKUP_LOCKED, mBackupLockedApps);
        }
    }

    private int getLockedAppCount() {
        int i = 0;
        for (String contains : mLockedApps) {
            i = !mDefaultLockedApps.contains(contains) ? i + 1 : i;
        }
        return i;
    }

    public boolean isRedundantTask(String className) {
        if (DEBUG) {
            Log.d(TAG, "isRedundantTask activityName:" + className + ",mRedundantTasks:" + mRedundantTasks);
        }
        if (mRedundantTasks != null && !mRedundantTasks.isEmpty()) {
            for (String name : mRedundantTasks) {
                if (className.contains(name)) {
                    return true;
                }
            }
        }
        return false;
    }

    public String formatConversion(String pkgName, int userId) {
        return pkgName + separator + userId;
    }

    public ArrayList<String> getLockedApps() {
        return mLockedApps;
    }

    public boolean isDefaultLockedApp(String pkg, int userId) {
        return mDefaultLockedApps.contains(formatConversion(pkg, userId));
    }

    public boolean isLockedApp(String packageName, int userId, String activityName) {
        if (DEBUG) {
            Log.d(TAG, "isApplicationLocked packageName:" + packageName + ",userId:" + userId + ",activityName:" + activityName);
        }
        return isLockedApp(packageName, userId) && !isRedundantTask(activityName);
    }

    public boolean isLockedApp(String pkgName, int userId) {
        return isLockedApp(formatConversion(pkgName, userId));
    }

    public boolean isLockedApp(String str) {
        return mLockedApps.contains(str);
    }

    private String getPkgName(String str) {
        if (str == null || str.isEmpty()) {
            return null;
        }
        if (str.contains("#")) {
            return str.split("#")[0];
        }
        return str;
    }
}