package com.prize.appcenter;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.RemoteException;
import android.text.TextUtils;
import android.util.Log;

import com.bumptech.glide.Glide;
import com.nostra13.universalimageloader.cache.disc.naming.Md5FileNameGenerator;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;
import com.prize.app.BaseApplication;
import com.prize.app.beans.Person;
import com.prize.app.constants.Constants;
import com.prize.app.database.PrizeDatabaseHelper;
import com.prize.app.database.PrizeMainDBHelper;
import com.prize.app.database.dao.XutilsDAO;
import com.prize.app.download.DownloadTaskMgr;
import com.prize.app.util.CommonUtils;
import com.prize.app.util.FileUtils;
import com.prize.appcenter.receiver.NetStateReceiver;
import com.prize.appcenter.receiver.ScreenListener;
import com.prize.appcenter.receiver.ScreenListener.ScreenStateListener;
import com.prize.appcenter.service.PrizeAppCenterService;
import com.prize.appcenter.service.ServiceToken;
import com.prize.appcenter.ui.util.AIDLUtils;
import com.prize.appcenter.ui.util.AppUpdateCache;
import com.prize.appcenter.ui.util.PollingUtils;
import com.prize.custmerxutils.XExtends;
import com.prize.qihoo.cleandroid.sdk.plugins.ApkScanProcessImpl;
import com.prize.qihoo.cleandroid.sdk.plugins.ClearSDKUpdateImpl;
import com.prize.qihoo.cleandroid.sdk.plugins.PtManagerImpl;
import com.prize.qihoo.cleandroid.sdk.plugins.SharedPreferencesImpl;
import com.prize.statistics.PrizeStatService;
import com.qihoo.cleandroid.sdk.utils.ClearModuleUtils;
import com.qihoo.cleandroid.sdk.utils.ClearSDKException;
import com.qihoo360.mobilesafe.opti.env.clear.ClearOptionEnv;
import com.qihoo360.mobilesafe.opti.i.IFunctionManager;
import com.qihoo360.mobilesafe.opti.i.plugins.IApkScanProcess;
import com.qihoo360.mobilesafe.opti.i.plugins.IPtManager;
import com.qihoo360.mobilesafe.opti.i.plugins.ISharedPreferences;
import com.qihoo360.mobilesafe.opti.i.plugins.IUpdate;
import com.tencent.stat.StatService;
import com.umeng.analytics.MobclickAgent;

import java.util.List;

public class MainApplication extends BaseApplication {
    protected String TAG = "MainApplication";
    private LoginDataCallBack loginDataCallBack;
    private Person person;
    private ServiceToken mToken;
    private static MainApplication instance;
    private Context mContext;
    private ClearSDKUpdateImpl mClearUpdateImpl;

    public static MainApplication getInstance() {
        return instance;
    }

    /*2.0版本 差分包 升级使用到的动态链接库 2016.8.11*/
    static {
        System.loadLibrary("ApkPatchLibrary");
    }

    @Override
    public void onCreate() {
        super.onCreate();
//        LeakCanary.install(this);
        mContext = getApplicationContext();
        instance = this;
        isCoosea = BuildConfig.ISCOOSEA;
        isNewSign = BuildConfig.ISNEWSIGN;
        isOeder = BuildConfig.ISOEDER;

//        // 线程检测策略
//        StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
//                .detectDiskReads()
//                .detectDiskWrites()
//                .detectNetwork()   // or .detectAll() for all detectable problems
//                .penaltyLog()
//                .build());
//        // 虚拟机检测策略
//        StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()
//                .detectLeakedSqlLiteObjects()
//                .detectLeakedClosableObjects()
//                .penaltyLog()
//                .penaltyDeath()
//                .build());


        FileUtils.initAppPath();
        // UM禁止默认的页面统计方式
//        initApp();
        XExtends.Ext.init(this);
        XExtends.Ext.setDebug(false);
        String processName = getProcessName(this, mMainThreadId);
        StatService.setContext(this);
        if (processName != null) {
            boolean defaultProcess = processName.equals(getPackageName());
            if (defaultProcess) {
                initApp();
                MobclickAgent.openActivityDurationTrack(false);
                initImageLoader(this);
                PrizeMainDBHelper.initPrizeSQLiteDatabase();
                PrizeStatService.init(this);
                isOnCreate = true;

                // @prize { modified by fanjunchen MsgNotificationService to
                // PrizeAppCenterService
                if (!PollingUtils.isServiceRunning(this, PrizeAppCenterService.className)) {
                    /* 假如在开机的时候不能 启动闹钟，在进入应用的时候再次启动闹钟 间隔小时 */
                    PollingUtils.startPollingService(this, Constants.PUSH_FOR_TIME,
                            PrizeAppCenterService.class, PrizeAppCenterService.MSG_ACTION);
                    /*垃圾扫描 间隔时间启动*/
                    PollingUtils.startTrashClearPollingService(this, Constants.TRASH_PUSH_FOR_TIME,
                            PrizeAppCenterService.class, PrizeAppCenterService.TRASH_SCAN_ACTION);
                    if (Build.VERSION.SDK_INT < 23) {
                        PollingUtils.setFixedTime(this);
                    }
                }
                mToken = AIDLUtils.bindToService(this, this);
                // @prize }
                if (person == null) {
                    person = queryUserId();
                }
                registerObserver();

            }

            if (processName.equals(getPackageName() + getString(R.string.remote_process_name))) {
                initApp();
                PrizeDatabaseHelper.initPrizeSQLiteDatabase();
                DownloadTaskMgr.getInstance();
                PrizeStatService.init(this);
                XutilsDAO.init(this);
//                PushAndroidClient.getInstance().registerSelfPush();
                if (Build.VERSION.SDK_INT > 21 && Build.VERSION.SDK_INT < 23 && !BaseApplication.isThird) {
                    CommonUtils.writePushPermissionSetting();
                    if (CommonUtils.isEnabled(this)) {
                        Intent intent = new Intent();
                        intent.setAction("android.service.notification.NotificationListenerService");
                        intent.setPackage(getPackageName());
                        startService(intent);
                    }

                }
            }
        }
        registerScreenLister();

        // TODO 请设置正确的清理SDK授权码，注意区分 测试签名和正式签名
        // 建议代码
        // String clearSdkAuthorizationCode = BuildConfig.DEBUG ? "测试签名授权码" :
        // "正式签名授权码";
//        String clearSdkAuthorizationCode = "S2tlVVq+txSsl7AhzaI2GPvabkgqnySzfwXCa5HpFl7/VtWwr/XgfY+e911oP1ClXRtEPXMfkX+3NgeYsyKsjw==";
        //涉及新旧签名，对应同的签名授权码
        String clearSdkAuthorizationCode = BuildConfig.CLEAR_SDKAUTHORIZATION_CODE;
        if (BuildConfig.DEBUG) {
            clearSdkAuthorizationCode = "EfII3uh/TluNsOkFRrjMtg7r0bovaCi4m4SZjN61WdXydQYqr9mIac/uYabaSzl/LMFfTjCxcQrXWuLPaI1ugA==";
        }
        ClearModuleUtils.sAutoUpdateInterval = 24 * 60 * 60 * 1000;
        // 设置清理SDK 依赖的参数
        ClearModuleUtils.setClearSDKEnv(clearSdkAuthorizationCode, new IFunctionManager() {
            @Override
            public Object query(Class<?> c) {
                String name = c.getName();
                if (name.equals(IPtManager.class.getName())) {
                    // Root插件
                    return new PtManagerImpl();
                } else if (name.equals(ISharedPreferences.class.getName())) {
                    // SharedPreferences 插件
                    return new SharedPreferencesImpl(mContext);
                } else if (name.equals(IApkScanProcess.class.getName())) {
                    // 跨进程扫描安装包插件
                    return new ApkScanProcessImpl(mContext);
                } else if (name.equals(IUpdate.class.getName())) {
                    // 数据升级插件； 多进程程序，建议只在常驻进程返回接口，避免多进程频繁出发升级
                    if (mClearUpdateImpl == null) {
                        mClearUpdateImpl = new ClearSDKUpdateImpl(mContext);
                    }
                    return mClearUpdateImpl;
                }
                Log.e("FunctionManagerImpl", "query interface is not supported " + name);
                return null;
            }
        });
        // 厂商或者有系统权限时，建议开启，提高清理能力
        try {
            ClearModuleUtils.getClearModulel(mContext).setOption(ClearOptionEnv.DEFAULT_LOAD_SOLIB, "1");
        } catch (ClearSDKException e) {
            e.printStackTrace();
        }

    }

    private void initImageLoader(Context context) {
        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(
                context).threadPriority(Thread.NORM_PRIORITY - 2)
                .denyCacheImageMultipleSizesInMemory()
                .diskCacheFileNameGenerator(new Md5FileNameGenerator())
                .diskCacheSize(50 * 1024 * 1024)
                // 50 Mb
                .tasksProcessingOrder(QueueProcessingType.LIFO)
                // .writeDebugLogs() // Remove for release app
                .build();
        // Initialize ImageLoader with configuration.
        ImageLoader.getInstance().init(config);

    }

    public void initApp() {
        BaseApplication.initBaseApp();
        // 网络切换监听
        NetStateReceiver netstateReceiver = new NetStateReceiver();
        this.registerReceiver(netstateReceiver, new IntentFilter(
                ConnectivityManager.CONNECTIVITY_ACTION));
    }

    /**
     * 方法描述：注册锁屏监听者
     */
    private void registerScreenLister() {
        ScreenListener screenListener = new ScreenListener(this);
        screenListener.begin(new ScreenStateListener() {

            @Override
            public void onUserPresent() {

            }

            @Override
            public void onScreenOn() {
                AIDLUtils.pauseAllBackgroudDownload(getApplicationContext());
            }

            // 锁屏并且电量高于30%时才会调用
            @Override
            public void onScreenOff() {
            }

            @Override
            public void onScreenOffNoRLLevel() {
            }
        });
    }

    /**
     * 方法描述：查询是否登录云账号
     *
     * @return void
     */
    private Person queryUserId() {
        ContentResolver resolver = this.getContentResolver();
        Uri uri = Uri
                .parse("content://com.prize.appcenter.provider.appstore/table_person");
        Person person = new Person();
        String userId = null;
        String realName = null;
        String imgPath = null;
        try {
            Cursor cs = resolver.query(uri, null, null, null, null);
            if (cs != null && cs.moveToFirst()) {
                userId = cs.getString(cs.getColumnIndex("userId"));
                realName = cs.getString(cs.getColumnIndex("realName"));
                imgPath = cs.getString(cs.getColumnIndex("avatar"));
            }
            if (cs != null) {
                cs.close();
            }
            if (TextUtils.isEmpty(userId)) {
                return null;
            } else {
                if (!TextUtils.isEmpty(imgPath)) {
                    person.setAvatar(imgPath);
                } else {
                    person.setAvatar("");
                }
                if (!TextUtils.isEmpty(realName)) {
                    person.setRealName(realName);
                } else {
                    person.setRealName("");
                }
                person.setUserId(userId);
                return person;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public void queryPerson() {
        person = queryUserId();
        if (loginDataCallBack != null) {
            loginDataCallBack.setPerson(person);
        }
    }

    /**
     * 类描述：云账号是否登录监听接口，用于实时给个人中心页和详情页传递登录信息
     *
     * @author 作者
     * @version 版本
     */
    public interface LoginDataCallBack {
        /**
         * 方法描述：实时改变登录状态
         *
         * @param person Person
         */
        void setPerson(Person person);
    }

    public void setLoginCallBack(LoginDataCallBack loginDataCallBack) {
        this.loginDataCallBack = loginDataCallBack;
    }

    public Person getPerson() {
        return person;
    }

    /**
     * 方法描述：注册数据库监听器，已便实时刷新数据
     */
    public void registerObserver() {
        PersonObserver personResolver = new PersonObserver(handler);
        Uri personUri = Uri.parse("content://com.prize.appcenter.provider.appstore/table_person");
        this.getContentResolver().registerContentObserver(personUri, true,
                personResolver);
    }

    private class PersonObserver extends ContentObserver {

        PersonObserver(Handler handler) {
            super(handler);
        }

        /**
         * 当所监听的Uri发生改变时，就会回调此方法
         *
         * @param selfChange 此值意义不大 一般情况下该回调值false
         */
        @Override
        public void onChange(boolean selfChange) {
            queryPerson();
        }

    }

    /**
     * @return null may be returned if the specified process not found
     */
    public static String getProcessName(Context cxt, int pid) {
        ActivityManager am = (ActivityManager) cxt
                .getSystemService(Context.ACTIVITY_SERVICE);
        if (am == null) return null;
        List<RunningAppProcessInfo> runningApps = am.getRunningAppProcesses();
        if (runningApps == null) {
            return null;
        }
        for (RunningAppProcessInfo procInfo : runningApps) {
            if (procInfo.pid == pid) {
                return procInfo.processName;
            }
        }
        return null;
    }

    @Override
    public void onTerminate() {
        // 程序终止的时候执行
        ImageLoader.getInstance().clearMemoryCache();
        Glide.get(this).clearMemory();
        AppUpdateCache.getInstance().clearCache();
        super.onTerminate();
    }

    /**
     * APP字体大小，不随系统的字体大小变化而变化的方法
     */
    @Override
    public Resources getResources() {
        Resources res = super.getResources();
        Configuration config = new Configuration();
        config.setToDefaults();
        res.updateConfiguration(config, res.getDisplayMetrics());
        return res;
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
        AIDLUtils.unbindFromService(mToken);

    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        try {
            Log.i(TAG, "onServiceConnected");
//            Log.i("PushAndroidClient", "onServiceConnected");
            //设置死亡代理监听
            AIDLUtils.mService.asBinder().linkToDeath(deathRecipient, 0);
            AIDLUtils.registSelfPush();
        } catch (RemoteException e) {
            e.printStackTrace();
            Log.i(TAG, "onServiceConnected-" + e);
        }
    }

    private IBinder.DeathRecipient deathRecipient = new IBinder.DeathRecipient() {
        @Override
        public void binderDied() {
            Log.i(TAG, "binderDied");
            if (AIDLUtils.mService != null) {
                AIDLUtils.mService.asBinder().unlinkToDeath(this, 0);
                AIDLUtils.mService = null;
            }
            AIDLUtils.bindToService(MainApplication.this, MainApplication.this);
        }
    };
}
