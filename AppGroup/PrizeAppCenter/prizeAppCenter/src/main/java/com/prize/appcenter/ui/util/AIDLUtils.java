package com.prize.appcenter.ui.util;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.RemoteException;
import android.text.TextUtils;
import android.util.Log;

import com.prize.app.database.PrizeDatabaseHelper;
import com.prize.app.database.dao.DownLoadedDAO;
import com.prize.app.database.dao.PushDAO;
import com.prize.app.download.AppManagerCenter;
import com.prize.app.download.DownloadTaskMgr;
import com.prize.app.download.IDownLoadService;
import com.prize.app.download.IServiceCallback;
import com.prize.app.download.IUpdateWatcher;
import com.prize.app.net.datasource.base.AppsItemBean;
import com.prize.app.util.JLog;
import com.prize.appcenter.MainApplication;
import com.prize.appcenter.service.PrizeAppCenterService;
import com.prize.appcenter.service.ServiceBinder;
import com.prize.appcenter.service.ServiceToken;
import com.prize.statistics.model.ExposureBean;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


/**
 * AIDL操作工具类
 * <p>
 * 类名称：AIDLUtils
 * <p>
 * 创建人：longbaoxiu
 * <p>
 * 修改时间：2016年6月13日 下午3:12:50
 *
 * @version 1.0.0
 */
public class AIDLUtils {
    private final static String TAG = "AIDLUtils";
    public static IDownLoadService mService = null;
    private static HashMap<Context, ServiceBinder> sConnectionMap = new HashMap<Context, ServiceBinder>();

    public static ServiceToken bindToService(Activity context) {
        return bindToService(context, null);
    }

    public static ServiceToken bindToService(Context context,
                                             ServiceConnection callback) {
        if (JLog.isDebug) {
            JLog.i(TAG, "bindToService-context=" + context);
        }
        if (context == null)
            return null;

        ContextWrapper cw = new ContextWrapper(context);
        cw.startService(new Intent(cw, PrizeAppCenterService.class));

        ServiceBinder sb = new ServiceBinder(callback);
        if (cw.bindService(
                new Intent().setClass(cw, PrizeAppCenterService.class), sb, 0)) {
            sConnectionMap.put(cw, sb);
            return new ServiceToken(cw);
        }
        return null;
    }

    /**
     * @param token ServiceToken
     */
    public static void unbindFromService(ServiceToken token) {
        if (token == null) {
            return;
        }
        ContextWrapper cw = token.mWrappedContext;
        ServiceBinder sb = sConnectionMap.remove(cw);
        if (sb == null) {
            return;
        }
        cw.unbindService(sb);
        if (sConnectionMap.isEmpty()) {
            mService = null;
        }
    }

    /**
     * 注册下载监听
     *
     * @param refreshHanle IServiceCallback
     * @return boolean
     */
    public static boolean registerCallback(IServiceCallback refreshHanle) {
        if (JLog.isDebug) {
            JLog.i("lonogbaoxiu-" + TAG, "registerCallback-refreshHanle=" + refreshHanle + "--mService=" + mService);
        }
        try {
            if (mService == null || refreshHanle == null)
                return false;
            mService.registerCallback(refreshHanle);
            return true;
        } catch (RemoteException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 反注册下载监听
     *
     * @param refreshHanle IServiceCallback
     */
    public static void unregisterCallback(IServiceCallback refreshHanle) {
        if (mService == null)
            return;
        try {
            mService.unregisterCallback(refreshHanle);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    /**
     * 设置手机里需要更新的app的监听回调
     *
     * @param refreshHanle IUpdateWatcher
     */
    public static void registObserver(IUpdateWatcher refreshHanle) {
        if (mService == null)
            return;
        try {
            mService.registObserver(refreshHanle);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public static void unregistObserver(IUpdateWatcher refreshHanle) {
        try {
            if (mService == null)
                return;
            mService.unregistObserver(refreshHanle);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    /**
     * getGameAppState(这里用一句话描述这个方法的作用) (返回app状态（安装中，下载中，更新等等）)
     *
     * @param packageName 包名
     * @param gameCode    应用id
     * @param versionCode 版本号
     * @return 返回app状态（安装中，下载中，更新等等）
     */
    public static int getGameAppState(String packageName, String gameCode,
                                      int versionCode) {
        try {
            if (mService == null) {
                JLog.i(TAG, "getGameAppState=--mService == null");
                //重新启动PrizeAppCenterService，关闭当前界面
                return AppManagerCenter.getGameAppState(packageName, gameCode, versionCode);
            }
            return mService.getGameAppState(packageName, gameCode, versionCode);
        } catch (RemoteException e) {
            e.printStackTrace();
            return 0;
        }
    }

    /**
     * 返回下载进度
     *
     * @param pkgName 包名
     * @return 下载进度
     */
    public static float getDownloadProgress(String pkgName) {
        try {
            if (mService == null)
                return 0;
            return mService.getDownloadProgress(pkgName);
        } catch (RemoteException e) {
            e.printStackTrace();
            return 0;
        }
    }

    /**
     * 获取下载进度
     *
     * @param pkgName 包名
     * @return 下载速度
     */
    public static int getDownloadSpeed(String pkgName) {
        try {
            if (mService == null)
                return 0;
            return mService.getDownloadSpeed(pkgName);
        } catch (RemoteException e) {
            e.printStackTrace();
            return 0;
        }
    }

    /**
     * 暂停下载
     *
     * @param game          AppsItemBean
     * @param isUserPressed 是否是用户主动停止暂停
     */
    public static void pauseDownload(AppsItemBean game, boolean isUserPressed) {
        try {
            if (mService == null)
                return;
            mService.pauseDownload(game, isUserPressed);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

//    public static int getDownloadTaskSize() {
//        try {
//            if (mService == null)
//                return 0;
//            return mService.getDownloadTaskSize();
//        } catch (RemoteException e) {
//            e.printStackTrace();
//            return 0;
//        }
//    }

    /**
     * 取消下载任务
     */
    public static void cancelDownload(AppsItemBean game) {
        try {
            if (mService == null)
                return;
            mService.cancelDownload(game);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    /**
     * 查询下载任务的数据
     *
     * @return List<AppsItemBean>
     */
    public static List<AppsItemBean> getDownloadAppList() {
        JLog.i(TAG, "-getDownloadAppList-mService=" + mService);
        try {
            if (mService == null)
                return new ArrayList<AppsItemBean>();
            return mService.getDownloadAppList();
        } catch (RemoteException e) {
            e.printStackTrace();
            return new ArrayList<AppsItemBean>();
        }
    }

    /**
     * 获取暂停的任务个数是否大于等于2
     *
     * @return boolean 大于等于2返回true
     */
    public static boolean hasPauseTaskMoreTwo() {
        try {
            return mService != null && mService.hasPauseTaskMoreTwo();
        } catch (RemoteException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 查询已下载任务的数据（2.0新增）
     *
     * @return List<AppsItemBean>
     */
    public static List<AppsItemBean> getHasDownloadedAppList() {
        try {
            JLog.i(TAG, "-getHasDownloadedAppList-mService=" + mService);
            if (mService == null)
                return new ArrayList<AppsItemBean>();
            return mService.getHasDownloadedAppList();
        } catch (RemoteException e) {
            e.printStackTrace();
            return new ArrayList<AppsItemBean>();
        }
    }

    /**
     * 查询已下载app的页面信息（2.9新增）
     *
     * @return List<AppsItemBean>
     */
    public static String getDownloadedAppPageInfo(String packageName) {
        try {
            JLog.i(TAG, "-getHasDownloadedAppList-mService=" + mService);
            if (mService == null)
                return null;
            return mService.getDownloadedAppPageInfo(packageName);
        } catch (RemoteException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 删除已下载数据（2.0新增）
     */
    public static void deleteSingle(String pkg) {
        try {
            if (mService == null) {
                DownLoadedDAO.getInstance().deleteSingle(pkg);
                return;
            }
            mService.deleteSingle(pkg);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    /**
     * 查询是否有安装任务
     *
     * @return true：有下载任务
     */
    public static boolean hasInstallTask() {
        try {
            return mService != null && mService.hasInstallTask();
//            if (mService == null)
//                return false;
//            return mService.hasInstallTask();
        } catch (RemoteException e) {
            e.printStackTrace();
            return false;
        }
    }

//    /**
//     * 查询是否有下载任务
//     *
//     * @return true：有下载任务
//     */
//    public static boolean hasDownloadingApp() {
//        try {
//            if (mService == null)
//                return false;
//            return mService.hasDownloadingApp();
//        } catch (RemoteException e) {
//            e.printStackTrace();
//            return false;
//        }
//    }

    public static void pauseAllDownload() {
        Intent intentManager = new Intent(MainApplication.curContext,
                PrizeAppCenterService.class);
        intentManager.putExtra(PrizeAppCenterService.ACTION,
                PrizeAppCenterService.ACT_PAUSEALL_TASK);
        intentManager.putExtra(PrizeAppCenterService.OPT_TYPE, 2);
        try {
            MainApplication.curContext.startService(intentManager);
        } catch (Throwable e) {
            e.printStackTrace();
            Log.i(TAG, "pauseAllDownload-e=" + e.getMessage());
        }

    }

    public static void continueAllDownload(Context context) {
        Intent intentManager = new Intent(context,
                PrizeAppCenterService.class);
        intentManager.putExtra(PrizeAppCenterService.ACTION,
                PrizeAppCenterService.ACT_CONTINUE_DOWNLOAD);
        intentManager.putExtra(PrizeAppCenterService.OPT_TYPE, 2);
        try {
            context.startService(intentManager);
        } catch (Throwable e) {
            e.printStackTrace();
            Log.i(TAG, "continueAllDownload-e=" + e.getMessage());
        }
    }

    public static void continuePauseTask(Context context) {
        Intent intentManager = new Intent(context,
                PrizeAppCenterService.class);
        intentManager.putExtra(PrizeAppCenterService.ACTION,
                PrizeAppCenterService.ACT_PAUSE_TASK_DOWNLOAD);
        intentManager.putExtra(PrizeAppCenterService.OPT_TYPE, 2);
        try {
            context.startService(intentManager);
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    public static void pauseAllBackgroudDownload(Context context) {
        Intent intentManager = new Intent(context, PrizeAppCenterService.class);
        intentManager.putExtra(PrizeAppCenterService.ACTION, PrizeAppCenterService.ACT_PAUSE_BACKTASK);
        intentManager.putExtra(PrizeAppCenterService.OPT_TYPE, 2);
        context.startService(intentManager);
    }

    public static void removeTask(String packageName) {
        try {
            if (mService == null)
                return;
            mService.removeTask(packageName);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public static AppsItemBean getDownloadGameByPkgname(String pkgName) {
        try {
            if (mService == null)
                return null;
            return mService.getDownloadGameByPkgname(pkgName);
        } catch (RemoteException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static void installedGame(String packageName) {
        try {
            if (mService == null)
                return;
            mService.installedGame(packageName);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public static String getisUpdate_install(String packageName) {
        try {
            if (mService == null)
                return null;
            return mService.getisUpdate_install(packageName);
        } catch (RemoteException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String getBackParam(String packageName) {
        try {
            if (mService == null)
                return null;
            return mService.getBackParam(packageName);
        } catch (RemoteException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 上传360曝光数据及时上传
     *
     * @param list List<ExposureBean>
     */
    public static void uploadDataNow(List<ExposureBean> list) {
        try {
            if (list == null || list.size() == 0 || mService == null)
                return;
            mService.uploadDataNow(list);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    /**
     * 上传360点击数据及时上传
     *
     * @param backParams  String
     * @param appName     应用名称
     * @param packageName 包名
     */
    public static void upload360ClickDataNow(String backParams, String appName, String packageName) {
        try {

            if (TextUtils.isEmpty(backParams) || mService == null)
                return;
            mService.upload360ClickDataNow(backParams, appName, packageName);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

//    public static boolean isInitIntalledAppOk() {
//        try {
//            if (JLog.isDebug) {
//                JLog.i(TAG, "isInitIntalledAppOk-mService=" + mService);
//            }
//            if (mService == null)
//                return CommonUtils.isInitIntalledAppOk();
//            return mService.isInitIntalledAppOk();
//        } catch (RemoteException e) {
//            e.printStackTrace();
//            return false;
//        }
//    }

//    public static String getPackgeInfoStrFormDB() {
//        try {
//            if (JLog.isDebug) {
//                JLog.i(TAG, "getPackgeInfoStrFormDB-mService=" + mService);
//            }
//            if (mService == null)
//                return CommonUtils.getPackgeInfoStrFormDB();
//            return mService.getPackgeInfoStrFormDB();
//        } catch (RemoteException e) {
//            e.printStackTrace();
//            return null;
//        }
//    }

//    public static boolean inert2DB(String sysInfo) {
//        try {
//            if (JLog.isDebug) {
//                JLog.i(TAG, "inert2DB-mService=" + mService);
//            }
//            if (mService == null)
//                return CommonUtils.inert2DB(BaseApplication.curContext, sysInfo);
//            return mService.inert2DB(sysInfo);
//        } catch (RemoteException e) {
//            e.printStackTrace();
//            return false;
//        }
//    }

    public static void updateInstalledTable(ContentValues value) {
        try {
            if (JLog.isDebug) {
                JLog.i(TAG, "updateInstalledTable-mService=" + mService);
            }
            if (mService == null) {
                PrizeDatabaseHelper.updateInstalledTable(value);
                return;
            }
            mService.updateInstalledTable(value);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public static void deleteAllDownloadedData() {
        try {
            if (JLog.isDebug) {
                JLog.i(TAG, "deleteAllDownloadedData-mService=" + mService);
            }
            if (mService == null) {
                DownLoadedDAO.getInstance().deleteAllDownloadedData();
                return;
            }
            mService.deleteAllDownloadedData();
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public static void clearAllLoadGametasks(List<AppsItemBean> downloadDatas) {
        try {
            if (JLog.isDebug) {
                JLog.i(TAG, "deleteAllDownloadedData-mService=" + mService);
            }
            if (mService == null) {
                DownloadTaskMgr.getInstance()
                        .clearAllLoadGametasks(downloadDatas);
                return;
            }
            mService.clearAllLoadGametasks(downloadDatas);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public static void deletePushData(String packageName) {
        try {
            if (JLog.isDebug) {
                JLog.i(TAG, "deleteAllDownloadedData-mService=" + mService);
            }
            if (mService == null) {
                PushDAO.getInstance().deletePushData(packageName);
                return;
            }
            mService.deletePushData(packageName);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    /**
     * 注册自制push
     */
    public static void registSelfPush() {
//        if (mService == null || TextUtils.isEmpty(PreferencesUtils.getKEY_TID()))
//            return;
//
//        try {
//            mService.registSelfPush();
//        } catch (RemoteException e) {
//            e.printStackTrace();
//        }
    }
}