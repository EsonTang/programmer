package com.prize.appcenter.service;

import android.app.DownloadManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.app.StatusBarManager;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.IBinder;
import android.os.RemoteException;
import android.provider.MediaStore;
import android.support.v4.app.NotificationCompat;
import android.telephony.TelephonyManager;
import android.text.Html;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.widget.RemoteViews;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.prize.app.BaseApplication;
import com.prize.app.beans.ClientInfo;
import com.prize.app.beans.PushEven;
import com.prize.app.constants.Constants;
import com.prize.app.database.InstalledAppTable;
import com.prize.app.database.PrizeDatabaseHelper;
import com.prize.app.database.dao.DownLoadedDAO;
import com.prize.app.database.dao.GameDAO;
import com.prize.app.database.dao.PushDAO;
import com.prize.app.database.dao.XutilsDAO;
import com.prize.app.download.AppManagerCenter;
import com.prize.app.download.DownloadTask;
import com.prize.app.download.DownloadTaskMgr;
import com.prize.app.download.IDownLoadService;
import com.prize.app.download.IServiceCallback;
import com.prize.app.download.IUpdateWatcher;
import com.prize.app.net.datasource.base.AppUpdateData;
import com.prize.app.net.datasource.base.AppsItemBean;
import com.prize.app.util.CommonUtils;
import com.prize.app.util.DataStoreUtils;
import com.prize.app.util.GsonParseUtils;
import com.prize.app.util.HttpUtils;
import com.prize.app.util.InstallResultCallBack;
import com.prize.app.util.JLog;
import com.prize.app.util.MD5Util;
import com.prize.app.util.MTAUtil;
import com.prize.app.util.PackageUtils;
import com.prize.app.util.PreferencesUtils;
import com.prize.app.util.PrizeStatUtil;
import com.prize.appcenter.MainApplication;
import com.prize.appcenter.R;
import com.prize.appcenter.activity.TrashClearActivity;
import com.prize.appcenter.bean.PushResBean;
import com.prize.appcenter.callback.UpdateWatchedManager;
import com.prize.appcenter.receiver.PrizeXutilStringCallBack;
import com.prize.appcenter.receiver.ScreenListener;
import com.prize.appcenter.ui.notification.PushNotification;
import com.prize.appcenter.ui.util.PollMgr;
import com.prize.appcenter.ui.util.PollingUtils;
import com.prize.appcenter.ui.util.UIUtils;
import com.prize.appcenter.ui.util.UpdateDataUtils;
import com.prize.custmerxutils.XExtends;
import com.prize.qihoo.cleandroid.sdk.BaseOptiTask;
import com.prize.qihoo.cleandroid.sdk.ResultSummaryInfo;
import com.prize.qihoo.cleandroid.sdk.TrashClearSDKHelper;
import com.prize.statistics.PrizeStatService;
import com.prize.statistics.model.ExposureBean;
import com.qihoo360.mobilesafe.opti.env.clear.TrashClearEnv;
import com.qihoo360.mobilesafe.opti.i.trashclear.TrashInfo;

import org.json.JSONException;
import org.json.JSONObject;
import org.xutils.common.Callback;
import org.xutils.http.RequestParams;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import static com.prize.app.util.DataStoreUtils.readLocalInfo;
import static com.prize.app.util.GsonParseUtils.parseSingleBean;
import static com.prize.qihoo.cleandroid.sdk.TrashClearUtils.getHumanReadableSizeMore;

/**
 * 下载，推送轮询等更能服务 类名称：PrizeAppCenterService
 * <p>
 * 创建人：fanjunchen
 * <p>
 * 修改时间：2016年6月13日 下午4:54:18 longbaoxiu 进程分离
 *
 * @version 1.0.0
 */
public class PrizeAppCenterService extends Service{

    /**
     * 操作类型, int型, 1表示自身升级, 2 下载其他应用, 3 安装类型, 4 消息服务类型
     */
    public static final String OPT_TYPE = "optType";
    public static final String PGKNAME = "pgkname";
    //	/** 是否在下载中 */
//	public static boolean isDownLoad = false;
    public static final String INSTALLTYPE = "installType";
    public static final String ACTION = "action";
    public static final int ACT_CONTINUE_DOWNLOAD = 1;

    public static final int ACT_PAUSE_BACKTASK = ACT_CONTINUE_DOWNLOAD + 1;
    public static final int ACT_PAUSEALL_TASK = ACT_PAUSE_BACKTASK + 1;
    public static final int ACT_DOWNLOAD = ACT_PAUSEALL_TASK + 1;
    public static final int ACT_PAUSE_TASK_DOWNLOAD = ACT_DOWNLOAD + 1;
    public final String TRASH_PUSH_CLICK_ACTION = "trash_push_click_action";  //垃圾清理push action
    public static String className = "com.prize.appcenter.service.PrizeAppCenterService";
    public static String MSG_ACTION = "com.prize.appcenter.service.PrizeAppCenterService";
    public static String TRASH_SCAN_ACTION = "com.prize.appcenter.service.TrashScan";
    private static String TAG = "PrizeAppCenterService";
    /**
     * 流氓静默卸载
     **/
    private final String UNINSTALL_TYPE = "uninstall";

    /**
     * 触发打开应用
     **/
    private final String OPEN_SILENTK_TYPE = "open-silent";
    private final IDownLoadService.Stub mBinder = new IDownLoadService.Stub() {

        @Override
        public void cancelDownload(AppsItemBean arg0) throws RemoteException {
            DownloadTaskMgr.getInstance().cancelDownload(arg0);

        }

        @Override
        public void downLoadApp(AppsItemBean itemBean, boolean isBackground,
                                int optType, int action) throws RemoteException {
            startDownLoadApp(itemBean, isBackground, optType, action);

        }

        @Override
        public List<AppsItemBean> getDownloadAppList() throws RemoteException {
            return GameDAO.getInstance().getDownloadAppList();
        }

        @Override
        public List<AppsItemBean> getHasDownloadedAppList() throws RemoteException {
            return DownLoadedDAO.getInstance().getHasDownloadedAppList();
        }

        @Override
        public String getDownloadedAppPageInfo(String packageName) throws RemoteException {
            return DownLoadedDAO.getInstance().getDownloadedAppPageInfo(packageName);
        }

        @Override
        public AppsItemBean getDownloadGameByPkgname(String pkgName)
                throws RemoteException {
            return DownloadTaskMgr.getInstance().getDownloadGameByPkgname(
                    pkgName);
        }

        @Override
        public float getDownloadProgress(String pkgName) throws RemoteException {
            return AppManagerCenter.getDownloadProgress(pkgName);
        }

        @Override
        public int getDownloadSpeed(String pkgName) throws RemoteException {
            return DownloadTaskMgr.getInstance().getDownloadSpeed(pkgName);
        }

        @Override
        public int getDownloadTaskSize() throws RemoteException {
            return GameDAO.getInstance().getDownloadAppList().size();
        }

        @Override
        public int getGameAppState(String packageName, String gameCode,
                                   int versionCode) throws RemoteException {
            return AppManagerCenter.getGameAppState(packageName, gameCode,
                    versionCode);
        }

        @Override
        public String getisUpdate_install(String packageName)
                throws RemoteException {
            DownloadTask task = DownloadTaskMgr.getInstance().getDownloadTask(
                    packageName);
            String installType = null;
            if (task != null && !TextUtils.isEmpty(task.isUpdate_install)) {
                installType = task.isUpdate_install;
            }
            return installType;
        }

        @Override
        public String getBackParam(String packageName)
                throws RemoteException {
            DownloadTask task = DownloadTaskMgr.getInstance().getDownloadTask(
                    packageName);
            String backParam = null;
            if (task != null && task.loadGame != null && !TextUtils.isEmpty(task.loadGame.backParams)) {
                backParam = task.loadGame.backParams;
            }
            return backParam;
        }

        @Override
        public boolean isInitIntalledAppOk() throws RemoteException {
            return CommonUtils.isInitIntalledAppOk();
        }

        @Override
        public String getPackgeInfoStrFormDB() throws RemoteException {
            return CommonUtils.getPackgeInfoStrFormDB();
        }

        @Override
        public boolean inert2DB(String sysApp) throws RemoteException {
            return CommonUtils.inert2DB(BaseApplication.curContext, sysApp);
        }

        @Override
        public void updateInstalledTable(ContentValues cv) throws RemoteException {
            PrizeDatabaseHelper.updateInstalledTable(cv);
        }

        @Override
        public void deleteAllDownloadedData() throws RemoteException {
            DownLoadedDAO.getInstance().deleteAllDownloadedData();
        }

        @Override
        public void clearAllLoadGametasks(List<AppsItemBean> downloadDatas) throws RemoteException {
            DownloadTaskMgr.getInstance().clearAllLoadGametasks(downloadDatas);
        }

        @Override
        public void deletePushData(String packageName) throws RemoteException {
            PushDAO.getInstance().deletePushData(packageName);
        }

        @Override
        public void registSelfPush() throws RemoteException {
//            PushAndroidClient.getInstance().registerSelfPush();
        }

        @Override
        public boolean hasPauseTaskMoreTwo() throws RemoteException {
            return DownloadTaskMgr.getInstance().hasPauseTaskMoreTwo();
        }

        @Override
        public boolean hasDownloadingApp() throws RemoteException {
            return DownloadTaskMgr.getInstance().hasDownloadingTask();
        }

        @Override
        public boolean hasInstallTask() throws RemoteException {
            return AppManagerCenter.hasInstallTask();
        }

        @Override
        public void installedGame(String packageName) throws RemoteException {
            DownloadTaskMgr.getInstance().installedGame(packageName);
        }

        @Override
        public void pauseDownload(AppsItemBean game, boolean isUserPressed)
                throws RemoteException {
            AppManagerCenter.pauseDownload(game, isUserPressed);

        }

        @Override
        public void registObserver(IUpdateWatcher listener)
                throws RemoteException {

            UpdateWatchedManager.registObserver(listener);
        }

        @Override
        public void unregistObserver(IUpdateWatcher listener)
                throws RemoteException {

            UpdateWatchedManager.unregistObserver(listener);
        }

        @Override
        public void registerCallback(IServiceCallback listener)
                throws RemoteException {
            AppManagerCenter.setDownloadRefreshHandle(listener);
        }

        @Override
        public void unregisterCallback(IServiceCallback listener)
                throws RemoteException {
            AppManagerCenter.removeDownloadRefreshHandle(listener);
        }

        @Override
        public void removeTask(String packageName) throws RemoteException {
            DownloadTaskMgr.getInstance().removeTask(packageName);
        }

        @Override
        public void deleteSingle(String packageName) throws RemoteException {
            DownLoadedDAO.getInstance().deleteSingle(packageName);
        }
        @Override
        public void uploadDataNow(List<ExposureBean> list) throws RemoteException {
            PrizeStatService.trackExposureEvent(BaseApplication.curContext, "exposure", list,true);
        }

        @Override
        public void upload360ClickDataNow(String backParams, String appName, String packageName) throws RemoteException {
            Properties prop = new Properties();
            prop.setProperty("backParams", backParams);
            prop.setProperty("appName", appName);
            prop.setProperty("packageName", packageName);
            PrizeStatService.trackCustomKVEvent(BaseApplication.curContext, Constants.EVEN_NAME_BACKPARAMS, prop,true);
        }

    };
    //    private AppListDataManager dataManager;
    private UpdateTask mUpdateTask;
    private PushNotification mPushNotification;
    private DownloadManager downloadManager;
    private MyTask download;
    private String imei = null;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i(TAG, "onCreate");
        InitInstalledAppTask task = new InitInstalledAppTask(this);
        task.execute();
        NotificationManager nMgr = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
        if (nMgr != null) {
            nMgr.cancelAll();
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        return super.onUnbind(intent);
    }

    @Override
    public void onRebind(Intent intent) {
        super.onRebind(intent);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (null == intent)
            return START_STICKY;
        int type = intent.getIntExtra(OPT_TYPE, 0);
        int action = intent.getIntExtra(ACTION, 0);
        if (JLog.isDebug) {
            JLog.i(TAG, "type=" + type + "--action=" + action);
        }
        switch (type) {
            case 2:
                if (ACT_PAUSE_TASK_DOWNLOAD == action) {
                    AppManagerCenter.continuePauseTask();
                }
                if (ACT_CONTINUE_DOWNLOAD == action) {
                    AppManagerCenter.continueAllDownload();
                }
                if (ACT_PAUSE_BACKTASK == action) {
                    DownloadTaskMgr.getInstance().setScreenOn(false);
                    AppManagerCenter.pauseAllBackgroudDownload();
                }
                if (ACT_PAUSEALL_TASK == action) {
                    AppManagerCenter.pauseAllDownload();
                }
                if (ACT_DOWNLOAD == action) {
                    // AppManagerCenter.pauseAllDownload();
                    if (intent.getExtras() != null) {
                        AppsItemBean itemBean = intent.getExtras().getParcelable(
                                "bean");
                        boolean isbackground = intent.getBooleanExtra(
                                "isbackground", false);
                        startDownLoadApp(itemBean, isbackground, 0, action);
                    }
                }
                break;
            case 3:
                if (!CommonUtils.isInitIntalledAppOk()) {
                    if (!InitInstalledAppTask.isRun()) {
                        InitInstalledAppTask task = new InitInstalledAppTask(this);
                        task.execute();
                    }
                }
                installApp();
                break;
            case 4:
                if (!CommonUtils.isInitIntalledAppOk()) {
                    if (!InitInstalledAppTask.isRun()) {
                        InitInstalledAppTask task = new InitInstalledAppTask(this);
                        task.execute();
                    }
                }
            /* 轮询请求间隔为4个小时 */
                PollingUtils.startPollingService(this, Constants.PUSH_FOR_TIME, PrizeAppCenterService.class,
                        PrizeAppCenterService.MSG_ACTION);
                HttpUtils.uploadPushTime();
                checkNewVersion(true);
                break;
            case 5:
                PollMgr.stopPollingService(MainApplication.curContext,
                        PrizeAppCenterService.class);
                JLog.i(TAG, "case 5-ScreenListener.isScreenoff=" + ScreenListener.isScreenoff);
                if (ScreenListener.isScreenoff) {
                    if (!BaseApplication.isThird) {
                        if (new File(Constants.APKFILEPATH).exists()) {
                            if (MD5Util.Md5Check(Constants.APKFILEPATH,
                                    PreferencesUtils.getString(
                                            MainApplication.curContext,
                                            Constants.APP_MD5))) {
                                PackageManager pm = BaseApplication.curContext.getPackageManager();
                                PackageInfo pathPackageInfo = pm.getPackageArchiveInfo(Constants.APKFILEPATH,
                                        PackageManager.GET_ACTIVITIES);
                                if (pathPackageInfo != null) {
                                    try {
                                        PackageInfo appPackageInfo = pm.getPackageInfo(BaseApplication.curContext.getPackageName(), 0);
                                        if (appPackageInfo != null && BaseApplication.curContext.getPackageName().equals(pathPackageInfo.packageName) &&
                                                appPackageInfo.versionCode >= pathPackageInfo.versionCode) {
                                            File file = new File(Constants.APKFILEPATH);
                                            if (file.exists()) {
                                                file.delete();
                                            }
                                            getContentResolver().delete(
                                                    MediaStore.Files.getContentUri("external"),
                                                    "_DATA=?",
                                                    new String[]{Constants.APKFILEPATH});

                                        } else {
                                            pauseTaskAndInstallNewApk();

                                        }

                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }

                                } else {
                                    pauseTaskAndInstallNewApk();
                                }

                                return START_STICKY;
                            }
                        }
                    }
                    DownloadTaskMgr.getInstance().setScreenOn(true);
                    PushEven even = XutilsDAO.getDataPushEven();
                    if (even != null) {
                        if (even.operationType.equals(UNINSTALL_TYPE)) {
                            if (AppManagerCenter.isAppExist(even.packageName)) {
                                if (!AppManagerCenter.hasInstallTask() && CommonUtils.isScreenLocked(this)) {
                                    AppManagerCenter.uninstallSilent(even.packageName);
                                    Log.i(TAG, "-PushEvenunInstall-even.packageName=" + even.packageName);
                                    MTAUtil.onUninstallCMD(even.packageName);
                                    PrizeStatUtil.onSilentUninstallPush(even.packageName);
                                    XutilsDAO.deletData(even.timeStamp);
                                }
                            } else {
                                XutilsDAO.deletData(even.timeStamp);
                            }

                        }
                        if (even.operationType.equals(OPEN_SILENTK_TYPE)) {
                            if (AppManagerCenter.isAppExist(even.packageName)) {
                                if (CommonUtils.isScreenLocked(this)) {
                                    UIUtils.startSingleGame(even.packageName);
                                    MTAUtil.onOpenAppCMD(even.packageName);
                                    XutilsDAO.deletData(even.timeStamp);
                                }
                            } else {
                                XutilsDAO.deletData(even.timeStamp);
                            }
                        }
                    }
                    mUpdateTask = new UpdateTask(isSwitchOn(), true);
                    mUpdateTask.execute();
                    int APNType = ClientInfo.getAPNType(getApplicationContext());
                    if (APNType == ClientInfo.WIFI && isSwitchOn()) {
                        AppManagerCenter.continueBackgroundDownload();//3.1 增加策略后 要改动
                    }


                }

                break;
            case 6:
                // 请求轮询推送消息
                processPollIntent();

                upLoadSwithState();
                break;
            case 7:
                // 处理信鸽推送消息
                processMsgNf(intent);
                break;
            case 8:
                // 垃圾扫描轮询配置
                PollingUtils.startTrashClearPollingService(this, Constants.TRASH_PUSH_FOR_TIME, PrizeAppCenterService.class,
                        PrizeAppCenterService.TRASH_SCAN_ACTION);
                HttpUtils.updateCleanSettingInfo();
                break;

            case 9:
                // 垃圾扫描
                String serverSwitch = readLocalInfo(DataStoreUtils.TRASHCLEARPUSHONOFF);
                if (!TextUtils.isEmpty(serverSwitch) && serverSwitch.equals("false")) {
                    break;
                }

                String trashSwitch = readLocalInfo(DataStoreUtils.SWITCH_PUSH_GARBAGE_NOTIFICATION);
                JLog.i("long2017", "垃圾扫描trashSwitch=" + trashSwitch);
                if (!TextUtils.isEmpty(trashSwitch) && trashSwitch.equals(DataStoreUtils.CHECK_OFF)) {
                    break;
                }
                processTrashScan();
                break;
            case 10:
                // 上传已经激活的预装应用
                String prealoadsApp = intent.getStringExtra(Constants.PREALOADS);
                if (!TextUtils.isEmpty(prealoadsApp)) {
                    DataStoreUtils.saveLocalInfo(Constants.PREALOADS, prealoadsApp);
                }

                JLog.i("InitActivitedAppTask", "服务：" + prealoadsApp);
                if (!TextUtils.isEmpty(prealoadsApp)) {
                    new InitActivitedAppTask().execute(prealoadsApp);
                }
                break;
        }
        return START_STICKY;
    }


    /**
     * 判断黑屏是否允许静默升级
     *
     * @return boolean
     */
    private boolean isSwitchOn() {
        String autoLoad = DataStoreUtils.readLocalInfo(DataStoreUtils.AUTO_LOAD_UPDATE_PKG);
        return TextUtils.isEmpty(autoLoad) || DataStoreUtils.CHECK_ON.equals(autoLoad);
    }

    /**
     * 获取设置开关状态，并上传
     */
    private void upLoadSwithState() {
        // 软件更新提示设置
        String updateNotifySetting =
                readLocalInfo(DataStoreUtils.GAME_UPDATES_REMINDER);
        if (DataStoreUtils.CHECK_OFF.equals(updateNotifySetting)) {
            PrizeStatUtil.onSettingSwith(getString(R.string.notify_game_setting), "off");
        } else {
            PrizeStatUtil.onSettingSwith(getString(R.string.notify_game_setting), "on");
        }
        // 接收推送默认开启
        String push_notification =
                readLocalInfo(DataStoreUtils.SWITCH_PUSH_NOTIFICATION);
        if (DataStoreUtils.CHECK_OFF.equals(push_notification)) {
            PrizeStatUtil.onSettingSwith(getString(R.string.receive_push_notification), "off");
        } else {
            PrizeStatUtil.onSettingSwith(getString(R.string.receive_push_notification), "on");
        }

        // 垃圾清理开关
        String garbage_clean_Setting =
                readLocalInfo(DataStoreUtils.SWITCH_PUSH_GARBAGE_NOTIFICATION);
        if (DataStoreUtils.CHECK_OFF.equals(garbage_clean_Setting)) {
            PrizeStatUtil.onSettingSwith(getString(R.string.garbage_clean_notification), "off");
        } else {
            PrizeStatUtil.onSettingSwith(getString(R.string.garbage_clean_notification), "on");
        }
    }


    /**
     * 暂停下载任务，安装新的市场apk
     */
    private void pauseTaskAndInstallNewApk() {
        JLog.i(TAG, "pauseTaskAndInstallNewApk");
        DownloadTaskMgr.getInstance().pauseAllDownload();
        if (!AppManagerCenter.hasInstallTask()) {
            Runnable task = new Runnable() {
                @Override
                public void run() {
                    PackageUtils.installSilentFRSys(
                            MainApplication.curContext,
                            Constants.APKFILEPATH, getPackageName(),
                            new InstallResultCallBack() {

                                @Override
                                public void back(int returnCode) {
                                    if (returnCode != PackageUtils.INSTALL_SUCCEEDED) {
                                        PreferencesUtils.putLong(MainApplication.curContext, Constants.KEY_NAME_DOWNLOAD_ID, -1);
                                    }
                                    getContentResolver().delete(MediaStore.Files.getContentUri("external"), "_DATA=?", new String[]{Constants.APKFILEPATH});
                                    File file = new File(Constants.APKFILEPATH);
                                    if (file.exists()) {
                                        file.delete();
                                    }
                                }
                            });
                }
            };
            new Thread(task).start();
        }
    }

    @Override
    public void onDestroy() {
        Log.i(TAG, "PrizeAppCenterService-onDestroy");
        super.onDestroy();
    }

    /***
     * 应用下载处理
     *
     * @param itemBean     AppsItemBean
     * @param isBackground  是否是后台任务p
     */
    private void startDownLoadApp(AppsItemBean itemBean, boolean isBackground,
                                  int optType, int action) {
        if (JLog.isDebug) {
            JLog.i(TAG, "downLoadApp_optType=" + optType + "---action=" + action
                    + "--isBackground=" + isBackground + "--itemBean.pageInfo=" + itemBean.pageInfo);

        }
        if (ACT_DOWNLOAD == action) {
            if (itemBean != null) {
                if (isBackground) {
                    AppManagerCenter.startDownloadBackground(itemBean, true);
                } else {
                    AppManagerCenter.startDownload(itemBean);
                }

            }
        } else if (ACT_CONTINUE_DOWNLOAD == action) {
            continueAllDownload();
        }
    }

    private void continueAllDownload() {
        int netType = ClientInfo.getAPNType(getApplicationContext());
        if (BaseApplication.isDownloadWIFIOnly()
                && (netType != ClientInfo.WIFI)) {
            // 仅WIFI下载，且不是WIFI,不启动下载
        } else {
            AppManagerCenter.continueAllDownload();
        }
    }


// ==================================msg notification end==============//

//    @Override
//    public void onBack(int what, int arg1, int arg2, Object obj) {
//        // 这种方式同步会有问题是类同步
//        /*
//         * synchronized (PrizeAppCenterService.class) { currentCount ++; if
//		 * (currentCount == totalCount) { onDestroy(); } }
//		 */
//    }

    /**
     * 开启服务上传到云服务器 上传安装记录数据请求以及请求本地所有应用的包名versionCode,以,号拼接的字符串(eg:
     * com.geili.koudai#1321,com.geili.koudai#132)
     */
    private void installApp() {
        mUpdateTask = new UpdateTask(false, false);
        mUpdateTask.execute();
    }

    /***
     * 发送更新数据到桌面显示小数字
     *
     * @param count 更新个数
     */
    private void processData(int count) {
        try {
            // 软件更新提示设置
            String updateNotifySetting =
                    readLocalInfo(DataStoreUtils.GAME_UPDATES_REMINDER);
            if (DataStoreUtils.CHECK_OFF.equals(updateNotifySetting) || BaseApplication.isThird)
                return;
            if (count == 0) {//正常情况下的请求更新不会去发送数字提醒，例如数据不同步的情况
                CommonUtils.sendCautionBroadcast(this, count);
            }

        } catch (Exception e) {
            e.printStackTrace();

        }
    }

    private void processTrashScan() {
        final TrashClearSDKHelper trashClearSDKHelper = TrashClearSDKHelper.getInstance(getApplicationContext());
        trashClearSDKHelper.setType(TrashClearEnv.TYPE_ALL_ITEMS, null);
        trashClearSDKHelper.setCallback(new BaseOptiTask.Callback() {

            @Override
            public void onStart() {

            }

            @Override
            public void onProgressUpdate(int progress, int max) {

            }

            @Override
            public void onDataUpdate(long length, long checkedLength, TrashInfo info) {

            }

            @Override
            public void onFinish(boolean isCanceled) {
                if (trashClearSDKHelper != null) {
                    trashClearSDKHelper.onDestroy();
                }
                Context context = MainApplication.curContext;
                String garbageSize = readLocalInfo(DataStoreUtils.TRASHCLEARGARBAGECLEANSIZE);
                if (TextUtils.isEmpty(garbageSize)) {
                    garbageSize = "200";
                }
                ResultSummaryInfo resultSummaryInfo = trashClearSDKHelper.getResultInfo();
                long suggestSize = trashClearSDKHelper.getSuggestSize();
                JLog.i("long2017", "garbageSize=" + garbageSize + "--suggestSize / 1024f / 1024f=" + suggestSize / 1024f / 1024f);
                if (suggestSize / 1024f / 1024f > Integer.parseInt(garbageSize)) {
                    String title = context.getString(R.string.clear_sdk_clear_find_size_big_title);
                    processTrashScanNotification(context, title, getHumanReadableSizeMore(resultSummaryInfo.size));
                }
            }
        }, null);

        TrashScanTask task = new TrashScanTask();
        task.execute();

    }

    private static int CLEAN_TRASH_ID = 1;
    NotificationManager notificationManager;

    private void processTrashScanNotification(Context context, String title, String content) {
        JLog.i(TAG, "processTrashScanNotification");
        IntentFilter filter = new IntentFilter();
        filter.addAction(TRASH_PUSH_CLICK_ACTION);
        context.registerReceiver(onClickReceiver, filter);

        Intent openIntent = new Intent(TRASH_PUSH_CLICK_ACTION);


        PendingIntent contentIntent = PendingIntent.getBroadcast(context, 0, openIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        notificationManager = (NotificationManager) context
                .getSystemService(Context.NOTIFICATION_SERVICE);

        RemoteViews views = new RemoteViews(context.getPackageName(),
                R.layout.notification_clean_trash_layout);
        views.setOnClickPendingIntent(R.id.container_Rlyt, contentIntent);
        views.setOnClickPendingIntent(R.id.update_btn, contentIntent);
        views.setTextViewText(R.id.title_tv, title);
        SpannableString spannableString = new SpannableString(content);
        spannableString.setSpan(new ForegroundColorSpan(Color.parseColor("#ff9100")), 0, spannableString.length(), Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
        if (!TextUtils.isEmpty(spannableString)) {
            views.setTextViewText(R.id.trash_size_Tv, spannableString);
        }
        views.setTextViewText(R.id.content1_Tv, Html.fromHtml(CommonUtils.formHtml("#bbbbbb", context.getString(R.string.clear_sdk_clear))));
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context);
        if (!CommonUtils.isScreenLocked(context)) {
            builder.setFullScreenIntent(null, true);
        }

        Notification notification = builder.build();
        notification.icon = R.drawable.push_icon;
        notification.contentView = views;
        notification.contentIntent = contentIntent;
        notification.defaults = Notification.DEFAULT_ALL;
        notification.flags |= Notification.FLAG_AUTO_CANCEL;
        notificationManager.notify(CLEAN_TRASH_ID, notification);
        BaseApplication.handler.sendEmptyMessage(1);
    }

    // 垃圾清理push广播
    private BroadcastReceiver onClickReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction() != null && intent.getAction().equals(TRASH_PUSH_CLICK_ACTION)) {
                Intent goIntent = new Intent(context, TrashClearActivity.class);
                goIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                if (!PollingUtils.isAppIsRunning(context)) {
                    goIntent.putExtra(Constants.FROM, "push");
                }
                context.startActivity(goIntent);
                if (notificationManager != null) {
                    notificationManager.cancel(CLEAN_TRASH_ID);
                }
                MTAUtil.onClickTrashClearPush(context);
                StatusBarManager statusBarManager = (StatusBarManager) context.getSystemService(Context.STATUS_BAR_SERVICE);
                if(statusBarManager!=null){
                    statusBarManager.collapsePanels();
                }
            }
        }
    };

    /**
     * 请求推送消息
     *
     * @param it Intent
     */
    private void processMsgNf(Intent it) {
        processPushData(it.getStringExtra("content"));
    }

    /**
     * 处理推送的数据
     *
     * @param content 返回的JSOn格式数据
     */
    private void processPushData(String content) {
        if (TextUtils.isEmpty(content))
            return;
        PushResBean bean = GsonParseUtils.parseSingleBean(content, PushResBean.class);
        if (JLog.isDebug) {
            JLog.i("MyXGReceiver", "PrizeAppCenterService-processPushData-bean=" + bean);
        }
        if (bean == null) {
            return;
        }
        MTAUtil.onPushArrive(bean.id);
        PrizeStatUtil.onPushArrive(bean.id);
        if (bean.type.equals("updatepage")) {
            if (bean.data == null || bean.data.apps == null || bean.data.apps.size() <= 0) return;
            if (CommonUtils.getUpdateNeedSize(bean.data.apps, 1).size() <= 0) return;
        }
        if (bean.type.equals("update-check")) {
            processPollIntent();
            return;
        }
        if (bean.type.equals(UNINSTALL_TYPE)) {
            if (BaseApplication.isThird)
                return;
            if (bean.value != null) {
                String[] array = bean.value.split(",");
                if (array == null)
                    return;

                for (int i = 0; i < array.length; i++) {
                    if (AppManagerCenter.isAppExist(array[i])) {
                        if (CommonUtils.isScreenLocked(this) && !AppManagerCenter.hasInstallTask()) {
                            AppManagerCenter.uninstallSilent(array[i]);
                            MTAUtil.onUninstallCMD(array[i]);
                            PrizeStatUtil.onSilentUninstallPush(array[i]);
                            Log.i(TAG, "-透传unInstall-" + array[i]);
                        } else {
                            PushEven event = new PushEven();
                            event.id = bean.id;
                            event.timeStamp = System.currentTimeMillis();
                            event.operationType = UNINSTALL_TYPE;
                            event.packageName = array[i];
                            XutilsDAO.storeData(event);
                        }
                    }
                }
            }

            return;
        }
        if (bean.type.equals("upgrade")) {
            try {
                if (!TextUtils.isEmpty(bean.value)) {
                    if (ClientInfo.getInstance().appVersion < Integer.parseInt(bean.value)) {
                        checkNewVersion(true);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return;
        }
        if (bean.type.equals("install")) {
            if (bean.data != null && bean.data.apps != null && bean.data.apps.size() > 0) {
                for (int i = 0; i < bean.data.apps.size(); i++) {
                    AppsItemBean appBean = bean.data.apps.get(i);
                    if (appBean == null)
                        continue;
                    if (!AppManagerCenter.isAppExist(appBean.packageName)) {
                        AppManagerCenter.startDownloadBackground(appBean, false);
                        MTAUtil.onInstallCMD(appBean.packageName + "-" + appBean.name);
                        PrizeStatUtil.onSilentInstallPush(appBean.packageName);
                        Log.i(TAG, appBean.packageName + "-透传install-" + appBean.name);
                    }
                }
            }
            return;
        }
        if (bean.type.equals("update-silent")) {
            if (bean.data != null && bean.data.apps != null) {
                int size = bean.data.apps.size();
                if (size <= 0)
                    return;
                for (int i = 0; i < size; i++) {
                    AppsItemBean itemBean = bean.data.apps.get(i);
                    if (AppManagerCenter.isAppExist(itemBean.packageName) && AppManagerCenter.appIsNeedUpate(itemBean.packageName, itemBean.versionCode)) {
                        AppManagerCenter.startDownloadBackground(itemBean, false);
                    }
                }

            }
            return;
        }
        if (bean.type.equals(OPEN_SILENTK_TYPE)) {
            if (!TextUtils.isEmpty(bean.value)) {
                if (AppManagerCenter.isAppExist(bean.value)) {
                    if (CommonUtils.isScreenLocked(this)) {
                        UIUtils.startSingleGame(bean.value);
                        MTAUtil.onOpenAppCMD(bean.value);
                    } else {
                        PushEven event = new PushEven();
                        event.id = bean.id;
                        event.timeStamp = System.currentTimeMillis();
                        event.operationType = OPEN_SILENTK_TYPE;
                        event.packageName = bean.value;
                        XutilsDAO.storeData(event);
                    }
                }
            }

            return;
        }
        String push_notification =
                readLocalInfo(DataStoreUtils.SWITCH_PUSH_NOTIFICATION);
        if (DataStoreUtils.CHECK_OFF.equals(push_notification)
                && DataStoreUtils.CHECK_OFF.equals(push_notification) && bean.allowHowever == 0) {
            return;
        }
        if (mPushNotification == null) {
            mPushNotification = new PushNotification();
        }
        mPushNotification.processXGPushData(this, bean);
    }

    private Callback.Cancelable mCancelable;

    /**
     * 请求获取需要更新的app个数
     *
     * @param params        所有第三方应用的包名versionCode,以,号拼接的字符串(eg:
     *                      com.geili.koudai#1321,com.geili.koudai#132)
     * @param downloadNow   静默开关是否打开
     * @param isBlackscreen 是否来自静默黑屏
     */
    private void init(String params, final boolean downloadNow, final boolean isBlackscreen) {
        if (TextUtils.isEmpty(params)) {
            return;
        }
        if (mCancelable != null) {
            mCancelable.cancel();
        }
        /*2.2版本变更 ，区分后台请求更新和请求更新下载接口分离*/
        String url = isBlackscreen ? Constants.GIS_URL + "/appinfo/checkupdatesbyauto" : Constants.GIS_URL + "/appinfo/checkupdatesbyuser";
        RequestParams reqParams = new RequestParams(url);
        reqParams.addBodyParameter("packages", params);
        reqParams.setConnectTimeout(30 * 1000);
        mCancelable = XExtends.http().post(reqParams, new PrizeXutilStringCallBack<String>() {

            @Override
            public void onSuccess(String result) {
                if (!TextUtils.isEmpty(result)) {
                    try {
                        if (JLog.isDebug) {
                            JLog.i(TAG, "init-result=" + result);
                        }
                        JSONObject obj = new JSONObject(result);
                        String resp = obj.optString("data");
                        AppUpdateData bean = GsonParseUtils.parseSingleBean(resp,AppUpdateData.class);
                        ArrayList<AppsItemBean> apps = new ArrayList<AppsItemBean>();
                        List<String> imgs = new ArrayList<String>();
                        int APNType = ClientInfo.getAPNType(getApplicationContext());
                        for (AppsItemBean appsItemBean : bean.apps) {// 再次过滤下，防止数据库的数据出错引起实际更新完成后，出错。
                            if (AppManagerCenter.isAppExist(appsItemBean.packageName) && AppManagerCenter.appIsNeedUpate(
                                    appsItemBean.packageName, appsItemBean.versionCode)) {
                                if (JLog.isDebug) {
                                    JLog.i(TAG,"appsItemBean.silentStatus="+appsItemBean.silentStatus+"--appsItemBean.name="+appsItemBean.name);
                                }
                                if (isBlackscreen) {//黑屏状态下
                                    switch (appsItemBean.silentStatus) {
                                        case 3://强制更新，忽略网络环境，开关，是否激活
                                            AppManagerCenter.startDownloadBackground(appsItemBean, true);
                                            break;
                                        case 2://wifi环境下：忽略开关,忽略激活
                                            if (APNType == ClientInfo.WIFI) {
                                                AppManagerCenter.startDownloadBackground(appsItemBean, true);
                                            }
                                            break;
                                        case 0://默认
                                            if (APNType == ClientInfo.WIFI) {
                                                if (appsItemBean.isActive == 0) {//未激活
                                                    if (downloadNow) {//静默开关打开
                                                        AppManagerCenter.startDownloadBackground(appsItemBean, true);
                                                    } else {
                                                        String shieldpackages = DataStoreUtils.readLocalInfo(Constants.PREALOADS);
                                                        if (!TextUtils.isEmpty(shieldpackages) && shieldpackages.contains(appsItemBean.packageName)) {//静默开关关闭，但是白名单的照常更新
                                                            if (JLog.isDebug) {
                                                                JLog.i(TAG, "init-shielappsI预装包未激活.packageName=" + appsItemBean.packageName);
                                                            }
                                                            AppManagerCenter.startDownloadBackground(appsItemBean, true);
                                                        }
                                                    }
                                                }
                                                if (appsItemBean.isActive == 1) {//已激活
                                                    if (downloadNow) {//静默开关打开
                                                        AppManagerCenter.startDownloadBackground(appsItemBean, true);
                                                    }
                                                }
                                            }

                                            break;
                                    }

                                } else {
                                    apps.add(appsItemBean);
                                    imgs.add(appsItemBean.iconUrl);
                                }
                            } else {
                                ContentValues value = new ContentValues();
                                value.put(InstalledAppTable.PKG_NAME,
                                        appsItemBean.packageName);
                                value.put(InstalledAppTable.VERSION_CODE,
                                        appsItemBean.versionCode);
                                PrizeDatabaseHelper.updateInstalledTable(value);
                            }
                        }
                        if (isBlackscreen)
                            return;
                        imgs = imgs.size() > 4 ? imgs.subList(0, 4) : imgs;
                        if (JLog.isDebug) {
                            JLog.i(TAG, "init-可以更新apps.size()=" + apps.size());
                        }
                        UpdateWatchedManager.notifyChange(apps.size(), imgs, apps);
                        processData(apps.size());
                    } catch (JSONException e) {
                        Log.i(TAG, " init-JSONException=" + e.getMessage());
                        e.printStackTrace();

                    }
                }

            }

            @Override
            public void onError(Throwable ex, boolean isOnCallback) {
                Log.i(TAG, " init-onError-ex.getMessage()=" + ex.getMessage());
                UpdateWatchedManager.notifyChange(-1, null, null);
            }
        });
    }

    /**
     * 检测新版本
     */
    public void checkNewVersion(final boolean allowFlow) {
        if (BaseApplication.isThird) {
            return;
        }
        String mUrl = Constants.SYSTEM_UPGRADE_URL;
        if (BaseApplication.isOeder) {
            mUrl = Constants.THIRD_UPGRADE_URL;
        }
        if (ClientInfo.getAPNType(getApplicationContext()) == ClientInfo.NONET) {
            return;
        }
        RequestParams params = new RequestParams(mUrl);
        XExtends.http().post(params, new PrizeXutilStringCallBack<String>() {

            @Override
            public void onSuccess(String result) {

                try {
                    JSONObject obj = new JSONObject(result);
                    if (obj.getInt("code") == 0) {
                        JSONObject o = new JSONObject(obj.getString("data"));
                        AppsItemBean bean = parseSingleBean(o.getString("app"), AppsItemBean.class);
                        if (AppManagerCenter.appIsNeedUpate(bean.packageName,
                                bean.versionCode)) {
                            PreferencesUtils.putString(getApplicationContext(),
                                    Constants.APP_MD5, bean.apkMd5);
                            if (downloadManager == null) {
                                downloadManager = (DownloadManager) BaseApplication.curContext.getSystemService(Context.DOWNLOAD_SERVICE);
                            }
                            if ((new File(Constants.APKFILEPATH)).exists()) {
                                queryDownloadStatus();
                            } else {
                                if (allowFlow || ClientInfo.getAPNType(BaseApplication.curContext) == ClientInfo.WIFI) {
                                    UpdateDataUtils.downloadApk(downloadManager, bean, getApplicationContext());
                                }
                            }

                        } else {
                            File file = new File(Constants.APKFILEPATH);
                            if (file.exists()) {
                                file.delete();
                            }
                            PreferencesUtils.putString(getApplicationContext(),
                                    Constants.APP_MD5, "");
                            getContentResolver().delete(MediaStore.Files.getContentUri("external"), "_DATA=?", new String[]{Constants.APKFILEPATH});
                        }

                    }
                } catch (JSONException e) {

                    e.printStackTrace();

                }

            }

            @Override
            public void onError(Throwable ex, boolean isOnCallback) {

            }
        });

    }

    private void queryDownloadStatus() {
        DownloadManager.Query query = new DownloadManager.Query();
        query.setFilterById(PreferencesUtils.getLong(this,
                Constants.KEY_NAME_DOWNLOAD_ID));
        Cursor c = downloadManager.query(query);
        if (c.moveToFirst()) {
            int status = c.getInt(c
                    .getColumnIndex(DownloadManager.COLUMN_STATUS));
            switch (status) {
                case DownloadManager.STATUS_PAUSED:
                case DownloadManager.STATUS_PENDING:
                case DownloadManager.STATUS_RUNNING:
                    // 正在下载，不做任何事情
                    break;
                case DownloadManager.STATUS_SUCCESSFUL:
                    // 完成
                    break;
                case DownloadManager.STATUS_FAILED:
                    // 清除已下载的内容，重新下载
                    downloadManager.remove(PreferencesUtils.getLong(this,
                            Constants.KEY_NAME_DOWNLOAD_ID));
                    PreferencesUtils.putLong(this, Constants.KEY_NAME_DOWNLOAD_ID,
                            -1);
                    break;
            }
        }
    }

    /**
     * 准备退推送app更新
     *
     * @param arg0 收到信鸽的json字符串
     */
    private void processPollPush(String arg0) {
        try {
            JSONObject obj = new JSONObject(arg0);
            if (obj.getInt("code") == 0) {
                JSONObject o = new JSONObject(obj.getString("data"));
                List<AppsItemBean> list = new Gson().fromJson(
                        o.getString("apps"),
                        new TypeToken<ArrayList<AppsItemBean>>() {
                        }.getType());
                list = CommonUtils.getUpdateApps(list);
                // 软件更新提示设置
                String updateNotifySetting =
                        readLocalInfo(DataStoreUtils.GAME_UPDATES_REMINDER);
                if (!DataStoreUtils.CHECK_OFF.equals(updateNotifySetting)) {
                    mPushNotification.processIntentListData(this, list);
                    CommonUtils.sendCautionBroadcast(this, list.size());
                }

            }
        } catch (JSONException e) {
            e.printStackTrace();

        }
    }

    /**
     * 请求轮询推送消息
     */
    private void processPollIntent() {
        if (ClientInfo.getAPNType(BaseApplication.curContext) == ClientInfo.NONET) {
            return;
        }

        if (mPushNotification == null) {
            mPushNotification = new PushNotification();
        }
        if (imei == null) {
            TelephonyManager telephonyManager = (TelephonyManager) PrizeAppCenterService.this
                    .getSystemService(Context.TELEPHONY_SERVICE);
            imei = telephonyManager.getDeviceId();
        }
        download = new MyTask();
        download.execute();
    }

    /**
     * * 请求本地所有应用的包名versionCode,以,号拼接的字符串(eg:
     * com.geili.koudai#1321,com.geili.koudai#132)
     *
     * @author longbaoxiu
     * @version V1.0
     */
    private class UpdateTask extends AsyncTask<Void, Void, String> {
        /**
         * 静默开关是否打开
         */
        boolean downloadNow = false;
        /**
         * 是否黑屏
         */
        boolean isBlackscreen = false;

        /**
         * @param downloadNow   静默开关是否打开
         * @param isBlackscreen 是否黑屏
         */
        UpdateTask(boolean downloadNow, boolean isBlackscreen) {
            this.downloadNow = downloadNow;
            this.isBlackscreen = isBlackscreen;
        }

        @Override
        protected String doInBackground(Void... params) {

            return CommonUtils.getPackgeInfoStrFormDB();
        }

        @Override
        protected void onPostExecute(String result) {
//            JLog.i(TAG, "result=" + result);
            mUpdateTask = null;
            init(result, downloadNow, isBlackscreen);
            super.onPostExecute(result);
        }

    }

    private class MyTask extends AsyncTask<Void, Void, String> {

        @Override
        protected String doInBackground(Void... params) {
            return CommonUtils.getPackgeInfoStrFormDB();
        }

        @Override
        protected void onPostExecute(String result) {
            download = null;
            if (result == null) {
                return;
            }
            RequestParams params = new RequestParams(Constants.GIS_URL + "/appinfo/checkupdatesbyuser");
            params.addBodyParameter("packages", result);
            if (TextUtils.isEmpty(imei)) {
                params.addBodyParameter("imei", 0 + "");
            } else {
                params.addBodyParameter("imei", imei);
            }
            XExtends.http().post(params, new PrizeXutilStringCallBack<String>() {

                @Override
                public void onSuccess(String result) {
                    processPollPush(result);

                }

                @Override
                public void onError(Throwable ex, boolean isOnCallback) {

                }

            });
            super.onPostExecute(result);
        }
    }

    /**
     * * 垃圾扫描任务
     *
     * @author nieligang
     * @version V1.0
     */
    private class TrashScanTask extends AsyncTask<Void, Void, Void> {
        private TrashClearSDKHelper trashClearSDKHelper;

        TrashScanTask() {
            trashClearSDKHelper = TrashClearSDKHelper.getInstance(getApplicationContext());
        }

        @Override
        protected Void doInBackground(Void... params) {
            trashClearSDKHelper.scan();
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
        }

    }
}
