
package com.prize.qihoo.cleandroid.sdk;

import java.io.File;
import java.lang.reflect.Method;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.os.Handler;
import android.os.IBinder;
import android.os.Process;
import android.os.RemoteException;
import android.text.TextUtils;
import android.util.Log;

import com.qihoo360.mobilesafe.opti.env.clear.TrashClearEnv;
import com.qihoo360.mobilesafe.opti.i.plugins.ApkInfo;
import com.qihoo360.mobilesafe.opti.i.plugins.IApkScanService;

/**
 * 开启独立进程进行安装包扫描，避免扫描异常的安装包时，导致卫士主进程中断，黑屏闪退的问题；
 */
public class ApkScanService extends Service {

    private static final boolean DEBUG = SDKEnv.DEBUG;

    private static final String TAG = "ApkScanService";

    public static final String ACTION_CLEAR_SERVICE = "ACTION_CLEAR_SERVICE";

    private Context mContext;

    @Override
    public void onCreate() {
        super.onCreate();
        mContext = getApplicationContext();
        if (DEBUG) {
            Log.d(TAG, "onCreate");
        }
    }

    @Override
    public void onDestroy() {
        if (DEBUG) {
            Log.d(TAG, "onDestroy");
        }
        super.onDestroy();
        killSelf();// nobug
    }

    @Override
    public IBinder onBind(Intent intent) {
        if (DEBUG) {
            Log.d(TAG, "onBind");
        }
        return mClearService;
    }

    private final IApkScanService.Stub mClearService = new IApkScanService.Stub() {

        private final Object mLockObj = new Object();

        private int mCallCount = 0;

        @Override
        public ApkInfo scanApk(String apkPath) throws RemoteException {
            ApkInfo apkInfo = getApkInfo(mContext, apkPath);
            if (DEBUG) {
                Log.d(TAG, "scanApk " + apkInfo);
            }
            return apkInfo;
        }

        @Override
        public int create() {
            synchronized (mLockObj) {
                mCallCount++;
            }
            if (DEBUG) {
                Log.d(TAG, "create mCallCount:" + mCallCount);
            }
            return 0;
        }

        @Override
        public int destroy() {
            synchronized (mLockObj) {
                mCallCount--;
                if (DEBUG) {
                    Log.d(TAG, "destory mCallCount:" + mCallCount);
                }
                if (mCallCount == 0) {
                    killSelf();
                }
            }
            return 0;
        }
    };

    private void killSelf() {
        Handler mHandler = new Handler();
        mHandler.postDelayed(new Runnable() {

            @Override
            public void run() {
                if (DEBUG) {
                    Log.i(TAG, "destory apk scan process killed");
                }
                // 当没人调用时，杀掉自己
                Process.killProcess(Process.myPid());
            }
        }, 100);
    }

    /**
     * 加载安装包信息
     */
    public static ApkInfo getApkInfo(Context context, String apkPath) {
        boolean isApk = false;
        ApkInfo apkInfo = new ApkInfo();
        apkInfo.path = apkPath;
        File apkFile = new File(apkInfo.path);
        apkInfo.size = apkFile.length();
        apkInfo.desc = apkFile.getName();
        apkInfo.modifyTime = apkFile.lastModified();

        PackageManager packageManager = context.getPackageManager();
        PackageInfo apkPackageInfo = null;
        // 获得PackageInfo信息
        try {
            apkPackageInfo = packageManager.getPackageArchiveInfo(apkInfo.path, (PackageManager.GET_PERMISSIONS));
        } catch (Throwable e) {
            if (DEBUG) {
                Log.e(TAG, "getPackageArchiveInfo error " + apkInfo.path);
            }
        }

        if (null != apkPackageInfo) {
            // 加载version信息
            apkInfo.apkVersionName = apkPackageInfo.versionName;
            apkInfo.apkVersionCode = apkPackageInfo.versionCode;
            apkInfo.packageName = apkPackageInfo.packageName;
            apkInfo.apkIconID = apkPackageInfo.applicationInfo.icon;

            // 加载隐藏class:AssetManager
            try {
                // 获得程序名称和图标
                if (0 == apkPackageInfo.applicationInfo.labelRes) {
                    apkInfo.desc = String.valueOf(packageManager.getApplicationLabel(apkPackageInfo.applicationInfo));
                } else {
                    Resources res = getApkResByRefrect(context, apkInfo.path);
                    if (res != null) {
                        apkInfo.desc = res.getString(apkPackageInfo.applicationInfo.labelRes);
                    }
                    if (!TextUtils.isEmpty(apkInfo.desc)) {
                        apkInfo.desc = apkInfo.desc.trim();
                    }
                }
                isApk = true;
            } catch (Throwable e) {
                if (DEBUG) {
                    Log.e(TAG, "loadApkInfo " + apkInfo.packageName + " error" + e.getMessage());
                }
            }
        }
        if (!isApk) {
            apkInfo.dataType = TrashClearEnv.APK_TYPE_DAMAGED;
        }

        // TODO
        // setApkInfoTypes(packageManager, apkInfo);

        return apkInfo;
    }

    /**
     * 通过反射加载安装包Resources
     */
    public static Resources getApkResByRefrect(Context context, String apkFilePath) {
        Method addAssetPathMethod = null;
        Object instance = null;
        Class<?> clazz = null;
        Resources apkRes = null;
        try {
            clazz = Class.forName("android.content.res.AssetManager");
            instance = clazz.newInstance();
            addAssetPathMethod = clazz.getMethod("addAssetPath", String.class);
            addAssetPathMethod.invoke(instance, apkFilePath);
            Resources res = context.getResources();
            apkRes = new Resources((AssetManager) instance, res.getDisplayMetrics(), res.getConfiguration());
        } catch (Throwable e) {
            if (DEBUG) {
                Log.e(TAG, "Class.forName(\"android.content.res.AssetManager\") error", e);
            }
        }
        return apkRes;
    }
}
