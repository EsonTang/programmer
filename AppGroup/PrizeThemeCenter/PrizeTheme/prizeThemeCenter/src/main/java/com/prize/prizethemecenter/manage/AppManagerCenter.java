package com.prize.prizethemecenter.manage;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.SystemProperties;
import android.text.TextUtils;

import com.prize.app.BaseApplication;
import com.prize.app.constants.Constants;
import com.prize.app.util.DataStoreUtils;
import com.prize.app.util.JLog;
import com.prize.prizethemecenter.bean.DownloadInfo;
import com.prize.prizethemecenter.bean.SingleThemeItemBean;
import com.prize.prizethemecenter.ui.utils.DBUtils;
import com.prize.prizethemecenter.ui.utils.FileUtils;

import java.io.File;
import java.util.HashSet;

/**
 * APP管理：安装，下载，删除等
 */
public class AppManagerCenter {
    // ------------------ APP状态 Start ------------------
    /**
     * 应用不存在
     */
    public static final int APP_STATE_UNEXIST = 0x1000;
    /**
     * 应用正在被下载
     */
    public static final int APP_STATE_DOWNLOADING = APP_STATE_UNEXIST + 1;
    /**
     * 应用下载被暂停
     */
    public static final int APP_STATE_DOWNLOAD_PAUSE = APP_STATE_DOWNLOADING + 1;
    // /** * 应用已完成下载 */
    public static final int APP_STATE_DOWNLOADED = APP_STATE_DOWNLOAD_PAUSE + 1;
    /**
     * 应用已被安装
     */
    public static final int APP_STATE_INSTALLED = APP_STATE_DOWNLOADED + 1;
    /**
     * 应用需要更新
     */
    public static final int APP_STATE_UPDATE = APP_STATE_INSTALLED + 1;
    /**
     * 应用等待下载
     */
    public static final int APP_STATE_WAIT = APP_STATE_UPDATE + 1;
    /**
     * 应用正在被安装（仅静默安装时使用）
     **/
    public static final int APP_STATE_INSTALLING = APP_STATE_WAIT + 1;
    /**
     * 查看礼包）
     **/
    public static final int APP_LOKUP_GIFT = APP_STATE_INSTALLING + 1;
    /**
     * 已经领取
     **/
    public static final int APP_RECEIVED_GIFT = APP_LOKUP_GIFT + 1;
    /**
     * 全部领完
     **/
    public static final int APP_NO_ACTIVATION_CODE = APP_RECEIVED_GIFT + 1;
    /**
     * 礼包活动结束
     **/
    public static final int APP_ACTIVITIES_OVER = APP_NO_ACTIVATION_CODE + 1;
    // ------------------ APP状态 End ------------------
    // /**
    // * @desc 1.7应用卸载
    // */
    /**
     * 应用卸载
     **/
    public static final int APP_STATE_UNINSTALL = APP_ACTIVITIES_OVER + 1;
    /**
     * 应用正在卸载中
     **/
    public static final int APP_STATE_UNINSTALLING = APP_STATE_UNINSTALL + 1;

    // ------------------ APP状态 End ------------------

    private static final Context context = BaseApplication.curContext;
    protected static final String TAG = "AppManagerCenter";

    private static HashSet<String> staticInstallPkg = new HashSet<String>();
    private static HashSet<String> staticUnInstallPkg = new HashSet<String>();
    private static String downloadFilePath;
    private static File downloadTmpFile;

    /**
     * 是否存在该游戏
     *
     * @param appPackage
     * @return
     */
    public static boolean isAppExist(String appPackage) {
        try {
            BaseApplication.curContext.getPackageManager().getApplicationInfo(
                    appPackage, 0);
            return true;
        } catch (Exception e) {
        }
        return false;
    }

    public static int getGameAppState(SingleThemeItemBean.ItemsBean bean, int type) {
        if (null == bean) {
            return APP_STATE_UNEXIST;
        }
        int appState = APP_STATE_UNEXIST; // 默认不存在
        DownloadTask task = DownloadTaskMgr.getInstance().getDownloadTask(bean.getId() + type);
        do {
            downloadFilePath = FileUtils.getDownloadTempPath(bean.getId() + "", type);
            downloadTmpFile = new File(downloadFilePath);
            if (downloadTmpFile.exists()) {
                if (null == task) {
                } else {
                    switch (task.gameDownloadState) {
                        case DownloadState.STATE_DOWNLOAD_WAIT:
                            appState = APP_STATE_WAIT;
                            break;
                        case DownloadState.STATE_DOWNLOAD_START_LOADING:
                        case DownloadState.STATE_DOWNLOAD_UPDATE_PROGRESS:
                            appState = APP_STATE_DOWNLOADING;
                            break;
                        case DownloadState.STATE_DOWNLOAD_PAUSE:
                        case DownloadState.STATE_DOWNLOAD_ERROR: // 错误下载，需要重新下载，设置成暂停状态
                            appState = APP_STATE_DOWNLOAD_PAUSE;
                            break;
                        case DownloadState.STATE_DOWNLOAD_SUCESS:
                            JLog.i(TAG, "task-->" + task + "--info.song_name..>"
                                    + bean.getName() + "--STATE_DOWNLOAD_SUCESS="
                                    + appState);
                            if (FileUtils.isFileExists(bean, type)) {
                                appState = APP_STATE_DOWNLOADED; // 默认下载成功
                            }
                        default:
                            break;
                    }
                }
            }else if (FileUtils.isFileExists(bean, type)) {
                appState = APP_STATE_DOWNLOADED;
                if (task != null && bean.getMd5() != null) {
                    if (!bean.getMd5().equals(task.loadGame.getMd5())) {
                        appState = APP_STATE_UPDATE;
                        return appState;
                    }
                }
                DownloadInfo info = DBUtils.findDownloadById(bean.getId() + type);
                if (info != null && info.currentState == 7)
                    appState = APP_STATE_INSTALLED;
                return appState;
            }else{ if (null == task) {
            } else {
                switch (task.gameDownloadState) {
                    case DownloadState.STATE_DOWNLOAD_WAIT:
                        appState = APP_STATE_WAIT;
                        break;
                    case DownloadState.STATE_DOWNLOAD_START_LOADING:
                    case DownloadState.STATE_DOWNLOAD_UPDATE_PROGRESS:
                        appState = APP_STATE_DOWNLOADING;
                        break;
                    case DownloadState.STATE_DOWNLOAD_PAUSE:
                    case DownloadState.STATE_DOWNLOAD_ERROR: // 错误下载，需要重新下载，设置成暂停状态
                        appState = APP_STATE_DOWNLOAD_PAUSE;
                        break;
                    case DownloadState.STATE_DOWNLOAD_SUCESS:
                        JLog.i(TAG, "task-->" + task + "--info.song_name..>"
                                + bean.getName() + "--STATE_DOWNLOAD_SUCESS="
                                + appState);
                        if (FileUtils.isFileExists(bean, type)) {
                            appState = APP_STATE_DOWNLOADED; // 默认下载成功
                        }
                    default:
                        break;
                }
            }
            }
        } while (false);
        return appState;
    }

    /**
     * 开始下载任务
     *
     * @param itemsBean 下载应用的信息bean
     * @return void
     */
    public static void startDownload(SingleThemeItemBean.ItemsBean itemsBean, int type) {
        startDownload(itemsBean, false, type);
    }

    /**
     * 开始下载
     *
     * @param game
     * @param isBackground : 是否是后台任务
     */
    private static void startDownload(SingleThemeItemBean.ItemsBean info,
                                      final boolean isBackground, int type) {
        if (null == info) {
            return;
        }
        DownloadTaskMgr.getInstance().startDownload(info, isBackground, type);
    }

    /**
     * UI对download状态的监听. 注意：当UI界面销毁，或者的被置于后台的时候，移除监听。避免重复多次的刷新数据和UI
     * 记得删除,否则会引起内存泄露
     *
     * @param refreshHanle
     */
    public static void setDownloadRefreshHandle(UIDownLoadListener refreshHandle) {
        DownloadTaskMgr.getInstance().setUIDownloadListener(refreshHandle);
    }

    /**
     * 删除下载监听句柄
     *
     * @param refreshHanle
     */
    public static void removeDownloadRefreshHandle(UIDownLoadListener refreshHandle) {
        DownloadTaskMgr.getInstance().removeUIDownloadListener(refreshHandle);
    }


    /**
     * 暂停下载
     *
     * @param context
     * @param gameCode
     * @param isUserPressed :用户主动停止
     */

    public static void pauseDownload(SingleThemeItemBean.ItemsBean game, boolean isUserPressed, int type) {
        DownloadTaskMgr.getInstance().pauseDownloadTask(game, isUserPressed, type);
    }

    /**
     * 继续下载所有下载中任务
     */
    public static void continueAllDownload() {
        DownloadTaskMgr.getInstance().continueAllDownload();
    }

    /**
     * 暂停所有下载中任务
     */
    public static void pauseAllDownload() {
        DownloadTaskMgr.getInstance().pauseAllDownload();
    }

    /**
     * 取消下载，会删除已下载的文件，从数据库中删除下载信息
     *
     * @param context
     * @param gameCode
     */
    public static void cancelDownload(SingleThemeItemBean.ItemsBean game, int type) {
        DownloadTaskMgr.getInstance().cancelDownload(game, type);
    }


    /**
     * 判断是否要更新版本(getPackageArchiveInfo)，根据versionCode来判断
     *
     * @param packageName
     * @param versionCode
     * @return
     */

    public static boolean appIsNeedUpate(String packageName, int versionCode) {
        if (BaseApplication.isThird) {
            return getAppVersionCode(packageName) < versionCode;
        }
        try {
            if (!isNewMethod()) {
                return getAppVersionCode(packageName) < versionCode;
            }
            String shieldpackages = DataStoreUtils.readLocalInfo(Constants.SHIELDPACKAGES);
            if (!TextUtils.isEmpty(shieldpackages) && !shieldpackages.contains(packageName)) {
                JLog.i(TAG, "!shieldpackages.contains(packageName)");
                return getAppVersionCode(packageName) < versionCode;
            }
            PackageManager pm = BaseApplication.curContext
                    .getPackageManager();
            ApplicationInfo applicationInfo = pm.getApplicationInfo(packageName, 0);
            PackageInfo packageInfo = pm.getPackageArchiveInfo(
                    applicationInfo.publicSourceDir, 0);
            if (packageInfo != null) {
                return packageInfo.versionCode < versionCode;
            }
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
        return false;
    }

    /**
     * 获取app的versioncode
     *
     * @param appPackage
     * @return
     */
    public static int getAppVersionCode(String appPackage) {
        int versionCode = 0;
        if (appPackage == null) {
            return versionCode;
        }
        try {
            PackageInfo packageInfo = BaseApplication.curContext
                    .getPackageManager().getPackageInfo(appPackage,
                            PackageManager.GET_META_DATA);

            if (packageInfo != null) {
                versionCode = packageInfo.versionCode;
            }
        } catch (PackageManager.NameNotFoundException e) {
            return versionCode;
        }
        return versionCode;
    }

    public static boolean isNewMethod() {
        return "1".equals(SystemProperties.get("ro.prize_app_update_appcenter",
                "0"));
    }
}
