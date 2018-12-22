package com.prize.app;

import android.app.Application;
import android.content.ComponentName;
import android.content.Context;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;
import android.text.TextUtils;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.prize.app.beans.ClientInfo;
import com.prize.app.util.DataStoreUtils;
import com.prize.app.util.JLog;
import com.prize.app.util.MTAUtil;
import com.prize.app.util.PrizeStatUtil;

public class BaseApplication extends Application implements ServiceConnection {

    //三方版：compile 'com.android.support:multidex:1.0.1'
    //三方版：extends MultiDexApplication
    private static String TAG = BaseApplication.class.getSimpleName();
    // 记录当前 Context
    public static Context curContext;

    public static Handler handler = new Handler() {
        public void handleMessage(android.os.Message msg) {//&& task.isUpdate_install.equals("download_install")
            if (msg != null && msg.what == 0 && msg.getData() != null) {
                String appName = msg.getData().getString("name");
                String pkgname = msg.getData().getString("packageName");
                String appid = msg.getData().getString("id");
                String pageInfo = msg.getData().getString("pageInfo");
                String isUpdate_install = msg.getData().getString("isUpdate_install");
                String backParams = msg.getData().getString("backParams");
                if (!TextUtils.isEmpty(isUpdate_install) && isUpdate_install.equals("download_install")) {
                    MTAUtil.onAPPDownloadSuccess(appName, pkgname);
                    PrizeStatUtil.onAPPDownloadCusSuccess(appid, appName, pageInfo, backParams);
                    PrizeStatUtil.appDownloadSuccess(appid, pkgname, appName, pageInfo);
                } else {
                    PrizeStatUtil.onAPPDownloadUpdateSuccess(appid, appName, pageInfo);
                    PrizeStatUtil.appDownloadSuccess(appid, pkgname, appName, pageInfo);
                }
                if (JLog.isDebug) {
                    JLog.i(TAG, "appName=" + appName + "--pkgname=" + pkgname);
                }
            }
            if (msg != null && msg.what == 1) {
                MTAUtil.onTrashClearPushShow();
            }
        }

    };

    // private static Context appContext;

    /**
     * 主线程ID
     */
    protected static int mMainThreadId = -1;
    /**
     * 主线程Handler
     */
    private static Handler mMainThreadHandler;
//    /**
//     * 主线程Looper
//     */
//    private static Looper mMainLooper;

    /**
     * 系统版和三方版切换开关，系统版为false，三方版为true
     */
    public static boolean isThird = false;
    /**
     * 非定制版false，定制版为true
     */
    public static boolean isOeder = false;
    /**
     * 酷赛版和酷比版切换开关，酷赛版为true，酷比版为false
     */
    public static boolean isCoosea = false;
    /**
     * 新旧签名开关，新签名true，旧签名false
     */
    public static boolean isNewSign = true;
    /**
     * Global request queue for Volley
     */
    private static RequestQueue mRequestQueue;
    public static boolean isOnCreate = false;

    @Override
    public void onCreate() {
        curContext = this.getApplicationContext();
        mMainThreadId = android.os.Process.myTid();
        mMainThreadHandler = new Handler();
        super.onCreate();
    }

    /**
     * 初始化全部所需的数据库
     */
    public static void initBaseApp() {
        ClientInfo.initClientInfo();

    }

    /**
     * 是否只允许WIFI环境下下载
     *
     * @return boolean
     */
    public static boolean isDownloadWIFIOnly() {
        String wifiSettingString = DataStoreUtils
                .readLocalInfo(DataStoreUtils.DOWNLOAD_WIFI_ONLY);
        return TextUtils.isEmpty(wifiSettingString) || wifiSettingString.equals(DataStoreUtils.DOWNLOAD_WIFI_ONLY_ENABLE);
    }

    /**
     * 获取主线程ID
     */
    public static int getMainThreadId() {
        return mMainThreadId;
    }

    /**
     * 获取主线程的handler
     */
    public static Handler getMainThreadHandler() {
        return mMainThreadHandler;
    }

    /**
     * @return The Volley Request queue, the queue will be created if it is null
     */
    public static RequestQueue getRequestQueue() {
        // lazy initialize the request queue, the queue instance will be
        // created when it is accessed for the first time
        if (mRequestQueue == null) {
            mRequestQueue = Volley.newRequestQueue(curContext.getApplicationContext());
        }

        return mRequestQueue;
    }

    /**
     * Adds the specified request to the global queue, if tag is specified then
     * it is used else Default TAG is used.
     *
     * @param req Request
     * @param tag Object
     */
    public static <T> void addToRequestQueue(Request<T> req, Object tag) {
        // set the default tag if tag is empty
        req.setShouldCache(false);
        req.setTag(tag == null ? TAG : tag);
        req.setRetryPolicy(new DefaultRetryPolicy(15000, 0,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        getRequestQueue().add(req);
    }


    /**
     * Cancels all pending requests by the specified TAG, it is important to
     * specify a TAG so that the pending/ongoing requests can be cancelled.
     *
     * @param tag Object
     */
    public static void cancelPendingRequests(Object tag) {
        JLog.i(TAG, "cancelPendingRequests(Object tag)--tag:" + tag);
        if (mRequestQueue != null) {
            mRequestQueue.cancelAll(tag);
        }
    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {

    }

    @Override
    public void onServiceDisconnected(ComponentName name) {

    }

}