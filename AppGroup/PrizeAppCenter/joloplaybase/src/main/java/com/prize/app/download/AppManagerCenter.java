package com.prize.app.download;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.SystemProperties;
import android.text.TextUtils;

import com.prize.app.BaseApplication;
import com.prize.app.constants.Constants;
import com.prize.app.database.dao.DownLoadedDAO;
import com.prize.app.database.dao.XutilsDAO;
import com.prize.app.net.datasource.base.AppsItemBean;
import com.prize.app.util.DataStoreUtils;
import com.prize.app.util.FileUtils;
import com.prize.app.util.HttpUtils;
import com.prize.app.util.InstallResultCallBack;
import com.prize.app.util.JLog;
import com.prize.app.util.MD5Util;
import com.prize.app.util.PackageUtils;

import java.io.File;
import java.util.HashSet;

/**
 * APP管理：安装，下载，删除等
 */
public class AppManagerCenter {
    protected static final String TAG = "AppManagerCenter";

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
    /**
     * 应用已完成下载，但尚未安装
     */
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
    // /**
    // * @desc 1.7应用卸载
    // */
    /**
     * 应用卸载
     **/
    private static final int APP_STATE_UNINSTALL = APP_ACTIVITIES_OVER + 1;
    /**
     * 应用正在卸载中
     **/
    private static final int APP_STATE_UNINSTALLING = APP_STATE_UNINSTALL + 1;


    public static final int APP_PATCHING = APP_STATE_UNINSTALLING + 1;

    // ------------------ APP状态 End ------------------

    private static final Context context = BaseApplication.curContext;
    private static HashSet<String> staticInstallPkg = new HashSet<String>();
    private static HashSet<String> staticUnInstallPkg = new HashSet<String>();

    /**
     * 是否存在该游戏
     *
     * @param appPackage 包名
     * @return boolean
     */
    public static boolean isAppExist(String appPackage) {
        try {
            BaseApplication.curContext.getPackageManager().getApplicationInfo(
                    appPackage, 0);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 获取应用的versionName(getPackageArchiveInfo)
     *
     * @param appPackage 包名
     * @param context Context
     */
    public static String getAppVersionName(String appPackage, Context context) {
        if (appPackage == null) {
            return "";
        }

        Context mContext = BaseApplication.curContext == null ? context
                : BaseApplication.curContext;
        if (mContext == null) {
            return "";
        }
        try {
            ApplicationInfo applicationInfo = mContext.getPackageManager()
                    .getApplicationInfo(appPackage, 0);
            if (isNewMethod()) {
                PackageInfo packageInfo = mContext.getPackageManager()
                        .getPackageArchiveInfo(applicationInfo.publicSourceDir,
                                0);
                if (packageInfo != null) {
                    return packageInfo.versionName;
                } else {
                    return "";
                }

            } else {
                PackageInfo packInfo = mContext.getPackageManager()
                        .getPackageInfo(appPackage, 0);
                return packInfo.versionName;
            }
        } catch (Exception e) {
            return "";
        }
    }

    /**
     * 获取app的versioncode(getPackageArchiveInfo)
     *
     * @param appPackage 包名
     * @param context    Context
     */
    public static int getAppVersionCode(String appPackage, Context context) {
        Context mContext = BaseApplication.curContext== null ? context
                : BaseApplication.curContext;
        if (mContext == null) {
            return -1;
        }
        if (BaseApplication.isThird) {
            return getAppVersionCode(appPackage);
        }
        if (appPackage == null) {
            return -1;
        }

        try {
            ApplicationInfo applicationInfo = null;
            applicationInfo = mContext.getPackageManager().getApplicationInfo(
                    appPackage, 0);
            if (isNewMethod()) {
                PackageInfo packageInfo = mContext.getPackageManager()
                        .getPackageArchiveInfo(applicationInfo.publicSourceDir,
                                0);
                if (packageInfo != null) {
                    return packageInfo.versionCode;
                } else {
                    return -1;
                }


            } else {
                return applicationInfo.versionCode;
            }
        } catch (Exception e) {
            return -1;
        }
    }

    /**
     * 获取app的versioncode
     *
     * @param appPackage 包名
     * @return 获取app versionCode
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
        } catch (NameNotFoundException e) {
            return versionCode;
        }
        return versionCode;
    }

    //

    /**
     * 获取本地versionCode（适用于自身app）
     *
     * @return String
     */
    public static String getLocalVersionCode() {
        String versionCode = null;
        try {
            PackageInfo packageInfo = BaseApplication.curContext
                    .getPackageManager().getPackageInfo(
                            BaseApplication.curContext.getPackageName(),
                            PackageManager.GET_META_DATA);

            if (packageInfo != null) {
                versionCode = String.valueOf(packageInfo.versionCode);
            }
        } catch (NameNotFoundException e) {
            return null;
        }
        return versionCode;
    }

    /**
     * @param packageName  包名
     * @param gameCode    : 用来查询是否存在APK或下载中的临时文件
     * @param versionCode : 版本号，用来判断是否要更新
     * @return int类型 app的状态
     */
    public static int getGameAppState(String packageName, String gameCode,
                                      int versionCode) {
        if (null == gameCode) {
            gameCode = "";
        }
        if (null == packageName) {
            return APP_STATE_UNEXIST;
        }
        int appState = APP_STATE_UNEXIST; // 默认不存在

        do {
            if (staticInstallPkg.contains(packageName)) {
                appState = APP_STATE_INSTALLING;
                break;
            }

            // 先判断是否存在 和 是否更新
            if (isAppExist(packageName)) {
                appState = APP_STATE_INSTALLED;
                if (appIsNeedUpate(packageName, versionCode)) {
                    // 还需要更新
                    appState = APP_STATE_UPDATE;
                }
            }
            // 需要判断是否有TASK，原因：山寨游戏是最新版本，但被下载替换中，故要判断是否下载。
            DownloadTask task = DownloadTaskMgr.getInstance().getDownloadTask(
                    packageName);
            if (null == task) {
                // null == task || task.isBackgroundTask()
                // 没有下载，do nothing, 或者是后台任务，不处理
            } else {
                switch (task.gameDownloadState) {
                    case DownloadState.STATE_DOWNLOAD_WAIT:
                        if (!task.isBackgroundTask()) {
                            appState = APP_STATE_WAIT;
                        } else {
//                            appState = APP_STATE_UPDATE;
                            // 先判断是否存在 和 是否更新
                            if (isAppExist(packageName)) {
                                appState = APP_STATE_INSTALLED;
                                if (appIsNeedUpate(packageName, versionCode)) {
                                    // 还需要更新
                                    appState = APP_STATE_UPDATE;
                                }
                            } else {
                                appState = APP_STATE_UNEXIST;
                            }
                        }
                        break;
                    case DownloadState.STATE_DOWNLOAD_START_LOADING:
                    case DownloadState.STATE_DOWNLOAD_UPDATE_PROGRESS:
                        if (!task.isBackgroundTask()) {
                            appState = APP_STATE_DOWNLOADING;
                        }
                        // appState = APP_STATE_DOWNLOADING;
                        break;
                    case DownloadState.STATE_PATCHING:
                        JLog.i(TAG, "DownloadState.STATE_PATCHING");
                        appState = APP_PATCHING;
                        break;
                    case DownloadState.STATE_PATCH_SUCESS:
                        JLog.i(TAG, "DownloadState.STATE_PATCH_SUCESS");
                        if (BaseApplication.isThird) {
                            appState = APP_STATE_DOWNLOADED;
                        }
                        break;
                    case DownloadState.STATE_DOWNLOAD_PAUSE:
                    case DownloadState.STATE_DOWNLOAD_ERROR: // 错误下载，需要重新下载，设置成暂停状态
                        if (!task.isBackgroundTask()) {
                            appState = APP_STATE_DOWNLOAD_PAUSE;
                        }
                        break;
                    case DownloadState.STATE_DOWNLOAD_SUCESS:
                        if (APP_STATE_UPDATE == appState) {
                            // app已存在，但需要更新
                            if (versionCode > task.loadGame.versionCode) {
                                // 线上版本的版本号比下载的版本号还新，重新下载
                                appState = APP_STATE_UPDATE;
                            } else {
                                // add by huanglingjun 2015-12-21
                                if (BaseApplication.isThird) {
                                    String APKFilePath = FileUtils
                                            .getGameAPKFilePath(task.loadGame.id);
                                    File APKFile = new File(APKFilePath);
                                    if (APKFile.exists()) {
                                        // APK存在且 下载的版本和线上的版本一致
                                        appState = APP_STATE_DOWNLOADED; // 下载成功，提示安装
                                    } else {
                                        // 下载过未安装，apk又被手动从文件夹删除的情况下提示更新
                                        appState = APP_STATE_UPDATE;
                                    }
                                } else {
                                    // 下载的版本和线上的版本一致
                                    appState = APP_STATE_DOWNLOADED; // 下载成功，提示安装
                                }
                            }
                        } else if (APP_STATE_INSTALLED == appState) {
                            appState = APP_STATE_INSTALLED; // 下载成功，提示安装
                        } else {

                            File apkFile = new File(
                                    FileUtils.getGameAPKFilePath(gameCode));
                            if (apkFile.exists()) {
                                appState = APP_STATE_DOWNLOADED; // 默认下载成功，提示安装

                            }
                        }
                        break;
                    default:
                        // do nothing
                        break;
                }
            }
        } while (false);
        return appState;
    }

    /**
     * 安装下载下来的APK包
     *
     * @param gameApp : 用来统计数据
     */
    public static void installGameApk(AppsItemBean gameApp) {
        installGameApk(gameApp, true);
    }

    /**
     * @param gameApp AppsItemBean
     * @param isAddToMygame : false 不添加到我玩中，用于 A 替换成 B，避免A还存在
     */
    public static void installGameApk(AppsItemBean gameApp,
                                      boolean isAddToMygame) {
        if (null == gameApp) {
            return;
        }
        final String packageName = gameApp.packageName;
        String gameCode = gameApp.id;
        if (staticInstallPkg.contains(packageName)) {
            // 已经在安装
            return;
        }

        String gameAPKFilePath = FileUtils.getGameAPKFilePath(gameCode);
        File gameAPKFile = new File(gameAPKFilePath);
        /* MD5校验，防止下载丢包等错误 longbaoxiu----20151118 start--- */
        if (!MD5Util.isDownComplete(gameAPKFilePath, gameApp.packageName)) {
            DownloadTaskMgr.getInstance().cancelDownload(gameApp);
            new Thread(new Runnable() {

                @Override
                public void run() {

                    try {
                        HttpUtils.doPost(packageName, "The different packageName");
                    } catch (Exception e) {
                        e.printStackTrace();

                    }

                }
            }).start();
            return;
        }

        JLog.i(TAG, "MD5一致=");
        /* MD5校验，防止下载丢包等错误 longbaoxiu----20151118 end--- */
        if (gameAPKFile.exists()) {

            if (!BaseApplication.isThird) {
                installRoot(gameAPKFilePath, packageName);
            } else {
                if (isAddToMygame) {
                    PackageUtils.installNormal(context, gameAPKFilePath);
                }
            }
        } else {
            // 文件被删除了,重新下载
            JLog.i(TAG, "文件被删除了,重新下载");
            startDownload(gameApp);
        }
    }

    private static void refreshUI(String pkg) {
        DownloadTaskMgr.getInstance().notifyRefreshUI(pkg,
                DownloadState.STATE_DOWNLOAD_REFRESH);
    }

    /**
     * 静默安装，完成后删除apk包
     *
     * @param filePath apk路径
     * @param pkg      包名
     */
    private static void installRoot(final String filePath, final String pkg) {
        Runnable task = new Runnable() {
            @Override
            public void run() {
                staticInstallPkg.add(pkg);
                refreshUI(pkg);
                PackageUtils.installSys(context, filePath, new
                        InstallResultCallBack() {
                            @Override
                            public void back(int returnCode) {
                                staticInstallPkg.remove(pkg);
                                refreshUI(pkg);

                                if (returnCode != PackageUtils.INSTALL_SUCCEEDED) {
                                    DownloadTask  task= DownloadTaskMgr.getInstance().getDownloadTask(pkg);
                                    if(task !=null&&task.isBackgroundTask()){
                                        XutilsDAO.storeAppInstallFaile(task.loadGame);
                                    }
//                                   AppsItemBean bean= task.loadGame;
                                    DownloadTaskMgr.getInstance().removeTask(pkg);
                                    DownLoadedDAO.getInstance().deleteSingle(pkg);

                                    File file = new File(filePath);
                                    if (file != null && file.exists() && file.isFile()) {
                                        file.delete();
                                    }
                                    if (returnCode == PackageUtils.INSTALL_FAILED_MISSING_SHARED_LIBRARY) {
                                        try {
                                            HttpUtils.doPost(pkg,
                                                    "INSTALL_FAILED_MISSING_SHARED_LIBRARY");
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }
                                    } else {
                                        try {
                                            HttpUtils.doPost(pkg,
                                                    "INSTALL_FAILED_OHTER_REASON---error code="
                                                            + returnCode);
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }
                                    }
                                }
                            }
                        }, pkg);
            }
        };

        new Thread(task).start();
    }
    /*
     * 卸载
     *
     * @param packageName 包名
     */

    public static void uninstallSilent(final String packageName) {
        staticUnInstallPkg.add(packageName);
        Runnable task = new Runnable() {
            @Override
            public void run() {
                PackageUtils.uninstallSilent(BaseApplication.curContext,
                        packageName);
                staticUnInstallPkg.remove(packageName);
            }
        };
        new Thread(task).start();
    }

    /**
     * 判断是否正在卸载
     *
     * @param packageName 包名
     * @return  boolean
     */
    public static boolean isUninstalling(final String packageName) {
        for (String str : staticUnInstallPkg) {
            if (packageName.equals(str)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 卸载
     *
     * @return true：表示有安装任务
     */
    public static boolean hasInstallTask() {
        return staticInstallPkg.size() > 0;
    }

    /**
     * 判断是否要更新版本(getPackageArchiveInfo)，根据versionCode来判断
     *
     * @param packageName 包名
     * @param versionCode versionCode
     * @return  boolean
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
        } catch (NameNotFoundException e) {
            return false;
        }
        return false;
    }

    /**
     * 开始下载任务
     *
     * @param game 下载应用的信息bean
     */
    public static void startDownload(AppsItemBean game) {
        startDownload(game, false, true);
    }

    public static void startDownloadBackground(AppsItemBean game, boolean isDownloadNow) {
        startDownload(game, true, isDownloadNow);
    }

// ///////////////////////////////////以下代码为下载API////////////////////////////////////////////////////////

    /**
     * 开始下载
     *
     * @param game AppsItemBean
     * @param isBackground : 是否是后台任务
     */
    private static void startDownload(AppsItemBean game,
                                      final boolean isBackground, boolean isDownloadNow) {
        if (null == game || null == game.downloadUrl
                || null == game.packageName) {
            return;
        }
        if (JLog.isDebug) {
            JLog.i(TAG, "startDownload开始下载-应用名称：" + game.name + "--应用包名：="
                    + game.packageName);
        }
        DownloadTaskMgr.getInstance().startDownload(game, isBackground, isDownloadNow);
    }

    /**
     * 暂停下载
     *
     * @param game AppsItemBean
     * @param isUserPressed :用户主动停止
     */

    public static void pauseDownload(AppsItemBean game, boolean isUserPressed) {
        DownloadTaskMgr.getInstance().pauseDownload(game, isUserPressed);
    }

    public static void pauseAllBackgroudDownload() {
        DownloadTaskMgr.getInstance().pauseAllBackgroudDownload();
    }

    /**
     * 取消下载，会删除已下载的文件，从数据库中删除下载信息
     *
     * @param game AppsItemBean
     */
    public static void cancelDownload(AppsItemBean game) {
        DownloadTaskMgr.getInstance().cancelDownload(game);
    }

//    /**
//     * 删除下载的APK包或下载的临时文件
//     *
//     * @param game AppsItemBean
//     */
//    public static void deleteDownloadGameApk(AppsItemBean game) {
//        DownloadTaskMgr.getInstance().cancelDownload(game);
//    }

    /**
     * 继续下载所有下载中任务
     */
    public static void continueAllDownload() {
        DownloadTaskMgr.getInstance().continueAllDownload();
    }
    /**
     * 继续下载暂停任务
     */
    public static void continuePauseTask() {
        DownloadTaskMgr.getInstance().continuePauseTask();
    }

    /**
     * 继续下载后台任务
     */
    public static void continueBackgroundDownload() {
        DownloadTaskMgr.getInstance().continueBackgroundDownload();
    }

    /**
     * 暂停所有下载中任务
     */
    public static void pauseAllDownload() {
        DownloadTaskMgr.getInstance().pauseAllDownload();
    }

    /**
     * 根据pkgName查询游戏的下载进度
     *
     * @param pkgName 包名
     * @return float类型
     */
    public static float getDownloadProgress(String pkgName) {
        return DownloadTaskMgr.getInstance().getDownloadProgress(pkgName);
    }


//    public static boolean hasDownloadingApp() {
//        return DownloadTaskMgr.getInstance().hasDownloadingTask();
//    }

    // ///////////////////////////////////////下载的接口
// end/////////////////////////////////////////////////////////////////////
//    public static final String OLD_PKG_NAME = "com.socogame.ppc";

//    /**
//     * 判定是否存在旧版的版本
//     *
//     * @return
//     */
//    public static boolean isOldVersionExist() {
//        List<PackageInfo> pkgs = context.getPackageManager()
//                .getInstalledPackages(PackageManager.GET_UNINSTALLED_PACKAGES);
//        int pkgSize = pkgs.size();
//        for (int i = 0; i < pkgSize; i++) {
//            PackageInfo pkgInfo = pkgs.get(i);
//            if (OLD_PKG_NAME.equalsIgnoreCase(pkgInfo.packageName)) {
//                if (isSystemApp(pkgInfo) || isSystemUpdateApp(pkgInfo)) {
//                    return false;
//                }
//            }
//        }
//        return false;
//    }
//
//    /**
//     * 是否是系统应用
//     *
//     * @param pInfo
//     * @return
//     */
//    public static boolean isSystemApp(PackageInfo pInfo) {
//        return ((pInfo.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 0);
//    }
    /**
     * 是否是系统应用
     *
     * @param packageName 包名
     * @return boolean
     */
    public static boolean isSystemApp(String packageName) {
        try {
            PackageInfo pInfo = BaseApplication.curContext.getApplicationContext().getPackageManager().getPackageInfo(
                    packageName, PackageManager.GET_UNINSTALLED_PACKAGES);
            return ((pInfo.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 0);
        } catch (Exception e) {
            return false;
        }
    }
//
//    /**
//     * 是否是系统应用更新
//     *
//     * @param pInfo
//     * @return
//     */
//    public static boolean isSystemUpdateApp(PackageInfo pInfo) {
//        return ((pInfo.applicationInfo.flags & ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) != 0);
//    }
//
//    public static void downloadStatistics(AppsItemBean gameInfo,
//                                          boolean isBackground) {
//        if (gameInfo != null && gameInfo.id + "" != null) {
//
//            // MobclickAgent.onEvent(BaseApplication.curContext,
//            // Constants.EVT_DOWNLOAD_GAME, Constants.EVT_P_PKG_NAME
//            // + gameInfo.packageName + Constants.EVT_P_LISTCODE
//            // + gameInfo.listcode);
//        }
//    }

//    private static final String SCHEME = "package";
//    /**
//     * 调用系统InstalledAppDetails界面所需的Extra名称(用于Android 2.1及之前版本)
//     */
//    private static final String APP_PKG_NAME_21 = "com.android.settings.ApplicationPkgName";
//    /**
//     * 调用系统InstalledAppDetails界面所需的Extra名称(用于Android 2.2)
//     */
//    private static final String APP_PKG_NAME_22 = "pkg";
//    /**
//     * InstalledAppDetails所在包名
//     */

//    private static final String APP_DETAILS_PACKAGE_NAME = "com.android.settings";
//    /**
//     * InstalledAppDetails类名
//     */

//    private static final String APP_DETAILS_CLASS_NAME = "com.android.settings.InstalledAppDetails";

//    /**
//     * 调用系统InstalledAppDetails界面显示已安装应用程序的详细信息。 对于Android 2.3（Api Level
//     * 9）以上，使用SDK提供的接口； 2.3以下，使用非公开的接口（查看InstalledAppDetails源码）。
//     *
//     * @param context  Context
//     * @param packageName 应用程序的包名
//     */
//    public static void showInstalledAppDetails(Context context,
//                                               String packageName) {
//        if (!AppManagerCenter.isAppExist(packageName)) {
//            // 不存在的应用，退出
//            return;
//        }
//        Intent intent = new Intent();
//        final int apiLevel = Build.VERSION.SDK_INT;
//        if (apiLevel >= 9) { // 2.3（ApiLevel 9）以上，使用SDK提供的接口
//            intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
//            Uri uri = Uri.fromParts(SCHEME, packageName, null);
//            intent.setData(uri);
//        } else { // 2.3以下，使用非公开的接口（查看InstalledAppDetails源码）
//// 2.2和2.1中，InstalledAppDetails使用的APP_PKG_NAME不同。
//            final String appPkgName = (apiLevel == 8 ? APP_PKG_NAME_22
//                    : APP_PKG_NAME_21);
//            intent.setAction(Intent.ACTION_VIEW);
//            intent.setClassName(APP_DETAILS_PACKAGE_NAME,
//                    APP_DETAILS_CLASS_NAME);
//            intent.putExtra(appPkgName, packageName);
//        }
//        context.startActivity(intent);
//    }

    /**
     * 删除下载监听句柄
     *
     * @param listener IServiceCallback
     */
    public static void removeDownloadRefreshHandle(IServiceCallback listener) {
        DownloadTaskMgr.getInstance().removeUIDownloadListener(listener);

    }

    /**
     * 设置下载监听句柄UI对download状态的监听. 注意：当UI界面销毁，或者的被置于后台的时候，移除监听。避免重复多次的刷新数据和UI
     * 记得删除,否则会引起内存泄露
     *
     * @param listener  IServiceCallback
     */
    public static void setDownloadRefreshHandle(IServiceCallback listener) {
        DownloadTaskMgr.getInstance().setUIDownloadListener(listener);

    }

//    /***
//     * 是否需要过滤
//     *
//     * @param item AppsItemBean
//     * @return true：需要过滤 false：无需过滤
//     */
//    public static boolean isNeedFilter(AppsItemBean item) {
//        if (isAppExist(item.packageName)) {
//            return !appIsNeedUpate(item.packageName, item.versionCode);
//        } else {
//            return false;
//
//        }
//    }

    /**
     * 是否需要读取app真实的版本号
     * @return boolean
     */
    public static boolean isNewMethod() {
        return "1".equals(SystemProperties.get("ro.prize_app_update_appcenter",
                "0"));
    }

//
//    /**
//     * 取消安裝任務（僅僅對等待安装的任务有效）
//     *
//     * @param packageName
//     */
//    public static void cancelInstallTask(String packageName) {
//
//        if (staticInstallPkg != null) {
//            staticInstallPkg.remove(packageName);
//        }
//    }

}
