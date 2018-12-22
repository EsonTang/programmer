package com.prize.app.download;

import android.annotation.SuppressLint;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteCallbackList;
import android.os.RemoteException;
import android.text.TextUtils;
import android.util.Log;

import com.prize.app.BaseApplication;
import com.prize.app.beans.ClientInfo;
import com.prize.app.constants.Constants;
import com.prize.app.database.dao.AppDAO;
import com.prize.app.database.dao.DownLoadedDAO;
import com.prize.app.database.dao.GameDAO;
import com.prize.app.database.dao.XutilsDAO;
import com.prize.app.net.datasource.base.AppsItemBean;
import com.prize.app.threads.SQLSingleThreadExcutor;
import com.prize.app.util.ApkUtils;
import com.prize.app.util.DataStoreUtils;
import com.prize.app.util.FileUtils;
import com.prize.app.util.JLog;
import com.prize.app.util.MD5Util;
import com.prize.app.util.SignUtils;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

/**
 * *
 * 下载管理类
 *
 * @author prize
 * @version V1.0
 */
public final class DownloadTaskMgr {
    private static final String TAG = "DownloadTaskMgr";

    /**
     * 下载的游戏
     */
    private HashMap<String, DownloadTask> loadGametasks;
    /**
     * 下载模块的状态监听，通下载线程交互
     */
    private DownloadState loadListener;
    /**
     * 下载模块的handler，和UI交互
     **/
    private static Handler loadHandler;

    private static final RemoteCallbackList<IServiceCallback> uiListners = new RemoteCallbackList<IServiceCallback>();
    private GameDAO loadGameDAO;
    /**
     * 后台下载数据库
     **/
    private AppDAO appDAO;
    private long lastRefreshUI = 0;
    private static DownloadTaskMgr instance;
    private static boolean isScreenOn = true;

    private DownloadTaskMgr() {
        // 不允许外部实例化

        initDownloadAppMode();

    }

    /**
     * 只通知UI,不做其他逻辑处理
     *
     * @param state     下载状态
     * @param errorCode 错误码
     * @param pkgName   包名
     */
    @SuppressLint("NewApi")
    private void notifyUIDownloadState(int state, int errorCode,
                                       String pkgName, int position, boolean isNewDownload) {
        if (null == uiListners) {
            return;
        }
        callback(state, errorCode, pkgName, position, isNewDownload);

        // 最后通知的时间
        lastRefreshUI = System.currentTimeMillis();
    }

    private void callback(int state, int errorCode, String pkgName, int position, boolean isNewDownload) {
        try {
            final int N = uiListners.beginBroadcast();
            for (int i = 0; i < N; i++) {
                IServiceCallback listener = uiListners.getBroadcastItem(i);
                if (listener != null) {
                    listener.handleDownloadState(state, errorCode, pkgName, position, isNewDownload);
                }
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        } catch (IllegalArgumentException illegalArgumentException) {
            illegalArgumentException.printStackTrace();
        } finally {
            try {
                uiListners.finishBroadcast();
            } catch (IllegalArgumentException illegalArgumentException) {
                illegalArgumentException.printStackTrace();
            }

        }

    }

    void notifyRefreshUI(String pkg, int state) {
        notifyDLTaskUIMsgToHandler(state, pkg, 0, false, 0, false);
    }

    private void notifyDLTaskUIMsgToHandler(int state, String pkgname,
                                            int errorCode, boolean isBackground, int position, boolean isNewDownload) {
        if (!isBackground) {
            notifyDLTaskUIMsgToHandler(state, pkgname, errorCode, 0, position, isNewDownload);

        }
    }

    /**
     * 通知task的状态给Handler，发送给UI的监听.只通知UI,不做其他逻辑处理
     *
     * @param state     下载状态
     * @param pkgname   包名
     * @param errorCode 错误码
     */
    private synchronized void notifyDLTaskUIMsgToHandler(int state,
                                                         String pkgname, int errorCode, long delayMillis, int position, boolean isNewDownload) {
        Bundle data = new Bundle();
        Message msg = Message.obtain();
        data.putString("pkgname", pkgname);
        data.putInt("state", state);
        data.putInt("errorCode", errorCode);
        data.putInt("position", position);
        data.putBoolean("isNewDownload", isNewDownload);
        msg.setData(data);
        loadHandler.sendMessageDelayed(msg, delayMillis);
    }

    public static DownloadTaskMgr getInstance() {
        if (instance == null) {
            synchronized (DownloadTaskMgr.class) {
                if (instance == null) {
                    instance = new DownloadTaskMgr();
                }
            }
        }
        return instance;
    }

    /**
     * 初始化洗澡模块,UI线程调用
     */
    private void initDownloadAppMode() {
        JLog.e(TAG, "initDownloadAppMode");
        loadGameDAO = GameDAO.getInstance();
        appDAO = AppDAO.getInstance();
        // 下载模块的handler，和UI交互
        if (Looper.myLooper() == null) {
            Looper.prepare();
            Looper.loop();
        }
        loadHandler = new Handler() {
            public void handleMessage(Message msg) {
                loadHandler.removeMessages(DownloadState.STATE_DOWNLOAD_UPDATE_PROGRESS); // 避免过度刷屏
                Bundle bundle = msg.getData();
                int state = bundle.getInt("state");
                String pkgname = bundle.getString("pkgname");
                int errorCode = bundle.getInt("errorCode");
                int position = bundle.getInt("position");
                boolean isNewDownload = bundle.getBoolean("isNewDownload");
                notifyUIDownloadState(state, errorCode, pkgname, position, isNewDownload);
            }

        };

        // 下载模块的状态监听，同下载线程交互
        loadListener = new DownloadState() {
            @Override
            public void onDownloadState(int state, String pkgname,
                                        int errorCode, int position, boolean isNewDownload) {
                DownloadTask task;
                synchronized (loadGametasks) {
                    task = loadGametasks.get(pkgname);
                    if (null == task) {
                        return;
                    }
                    JLog.i(TAG, "---->onDownloadState-pkgname：" + pkgname + "---state=" + state);
                    // 先更新task的状态，后续执行，会和状态相关
                    task.gameDownloadState = state;

                    if (DownloadState.STATE_DOWNLOAD_SUCESS == state) {
                        task.resetDownloadRunnable();// 移除下载任务线程，否则pause全部的时候，状态会被置成暂停
                        if (checkDownloadSucess(pkgname)) {
                            if (!task.isBackgroundTask()) {
                                loadGameDAO.updateState(pkgname, state);
                            }
                        } else {
                            // 下载成功后，发现文件出错，重新下载
                            task.gameDownloadState = DownloadState.STATE_DOWNLOAD_ERROR;
                            startDownload(task.loadGame,
                                    task.isBackgroundTask(), true);
                            return;
                        }
                    } else if ((DownloadState.STATE_DOWNLOAD_PAUSE == state)
                            || (DownloadState.STATE_DOWNLOAD_ERROR == state)) {
                        // 下载任务线程被结束或暂停了
                        task.resetDownloadRunnable();
                        if (task.isBackgroundTask()) {
                            appDAO.updateState(pkgname, state);
                        } else {
                            loadGameDAO.updateState(pkgname, state);
                        }
                        // loadGameDAO.updateState(pkgname, state);
                        if ((DownloadState.ERROR_CODE_TIME_OUT == errorCode)
                                || (DownloadState.ERROR_CODE_HTTP == errorCode)
                                || (DownloadState.ERROR_CODE_URL_ERROR == errorCode)) {
                            // 如果是超时，或者网络连接失败，重试
                            task.gameDownloadState = state;
                            startDownload(task.loadGame,
                                    task.isBackgroundTask(), true);

                            return;
                        }
                    } else if (DownloadState.STATE_PATCHING == state) {
                        if (task.isBackgroundTask()) {
                            appDAO.updateState(pkgname, state);
                            task.isUpdate_install = "update_install";
                            loadGameDAO.insertGame(task);
                            loadGameDAO.updateState(pkgname,
                                    state);
                        } else {
                            Uri downloadedUri = Uri
                                    .parse("content://com.prize.appcenter.provider.appstore/table_downLoaded");
                            try {
                                BaseApplication.curContext.getContentResolver().insert(
                                        downloadedUri,
                                        DownLoadedDAO.getInstance().getContentValues(
                                                task.loadGame));
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    } else if (DownloadState.STATE_PATCH_SUCESS == state) {
                        task.resetDownloadRunnable();// 移除下载任务线程，否则pause全部的时候，状态会被置成暂停
                        loadGameDAO.updateState(pkgname, DownloadState.STATE_DOWNLOAD_SUCESS);
                        processDifState(pkgname, task);
                    } else if (DownloadState.STATE_PATCH_FAILE == state) {
//                        task.resetDownloadRunnable();// 移除下载任务线程，否则pause全部的时候，状态会被置成暂停
//                        processDifState(pkgname,task);
                        cancelDownload(task.loadGame);
                        if (task != null && task.loadGame != null && task.loadGame.appPatch != null) {
                            task.loadGame.appPatch = null;
                            startDownload(task.loadGame, task.isBackgroundTask(), true);
                        }

                    }
                }
                //此时的话 ，已下载的数据为插入到已下载数据库
                if (task != null)
                    notifyDLTaskUIMsgToHandler(state, pkgname, errorCode,
                            task.isBackgroundTask(), position, isNewDownload);
            }

            @Override
            public void updateDownloadProgress(String pkgname,
                                               int downloadFileSize, int downloadPosition,
                                               int downloadSpeed) {
                DownloadTask task = loadGametasks.get(pkgname);
                JLog.i(TAG, "---->updateDownloadProgress-pkgname：" + pkgname);
                if (null == task) {
                    return;
                }
                // 判断刷新的频率，防止过度刷屏
                long current = System.currentTimeMillis();
                if ((current - lastRefreshUI) >= 800) {
                    if (!task.isBackgroundTask()) {
                        notifyDLTaskUIMsgToHandler(
                                DownloadState.STATE_DOWNLOAD_UPDATE_PROGRESS,
                                pkgname, DownloadState.ERROR_NONE,
                                task.isBackgroundTask(), downloadSpeed, false);
                    }
                }

                task.setDownloadSize(downloadFileSize, downloadPosition);

                task.setDownloadSpeed(downloadSpeed);
                // update database
                if (task.isBackgroundTask()) {
                    appDAO.updateDownloadSize(pkgname, downloadFileSize,
                            downloadPosition);
                } else {
                    loadGameDAO.updateDownloadSize(pkgname, downloadFileSize,
                            downloadPosition);
                }
            }
        };
        // 下载游戏的数据池
        loadGametasks = new HashMap<String, DownloadTask>();

        // 从数据库中初始化下载数据
        initDownloadGameTaskFromDB();
    }

    /**
     * 初始化下载的数据
     */
    private void initDownloadGameTaskFromDB() {
        SQLSingleThreadExcutor.getInstance().execute(new Runnable() {

            @Override
            public void run() {
                // 从数据库中恢复下载数据
                HashMap<String, DownloadTask> taskMap = loadGameDAO
                        .getAllDownloadExeTask();
                if (null == taskMap) {
                    return;
                }
                loadGametasks.putAll(taskMap);
                notifyDLTaskUIMsgToHandler(
                        DownloadState.STATE_DOWNLOAD_MODE_INIT, null, 0, 5000,
                        0, false); // 5s后执行，需要等UI准备好
            }
        });
    }

    /**
     * 查看网络状态，是否有SD卡
     *
     * @return 错误值 : DownloadState.ERROR_CODE_NO_NET,
     * DownloadState.ERROR_CODE_NOT_WIFI
     * ,DownloadState.ERROR_CODE_NO_SDCARD
     */
    private int checkNetAndSpace() {
        int netType = ClientInfo.getAPNType(BaseApplication.curContext);
        int errorCode = DownloadState.ERROR_NONE;

        if (netType == ClientInfo.NONET) {
            // 通知网络错误
            errorCode = DownloadState.ERROR_CODE_NO_NET;
        } else if ((netType != ClientInfo.WIFI)
                && BaseApplication.isDownloadWIFIOnly()) {
            // 通知网络设置
//             errorCode = DownloadState.ERROR_CODE_NOT_WIFI;
        } else if (!Environment.getExternalStorageState().equals(
                android.os.Environment.MEDIA_MOUNTED)) {
            // 通知无SD卡
            errorCode = DownloadState.ERROR_CODE_NO_SDCARD;
        }
        return errorCode;
    }

    /**
     * 下载接口
     *
     * @param game         AppsItemBean
     * @param isBackground 是否是后台下载
     * @return false: 继续下载， true： 新下载
     */
    boolean startDownload(AppsItemBean game, boolean isBackground, boolean isDownloadNow) {
        boolean isNewDownload = false;
        if (null == game || null == game.packageName
                || null == game.downloadUrl) {
            return false;
        }

        String pkgnameString = game.packageName;
        if (game.appPatch != null) {
            String oldApkSource = ApkUtils.getSourceApkPath(BaseApplication.curContext, pkgnameString);
            JLog.i(TAG, "startDownload-oldApkSource=" + oldApkSource);
            if (!TextUtils.isEmpty(oldApkSource) && !TextUtils.isEmpty(game.appPatch.fromApkMd5)) {
                // 校验一下本地安装APK的MD5是不是和真实的MD5一致
                if (SignUtils.checkMd5(oldApkSource, game.appPatch.fromApkMd5)) {
                    JLog.i(TAG, "startDownload-校验一下本地安装APK的MD5成功");
                } else {
                    game.appPatch = null;
                }
            } else {
                game.appPatch = null;
            }
        }
        // check net and space
        int checkResult = checkNetAndSpace();
        if (DownloadState.ERROR_NONE == checkResult) {

        } else {
            // 通知错误
            notifyDLTaskUIMsgToHandler(DownloadState.STATE_DOWNLOAD_ERROR,
                    pkgnameString, checkResult, isBackground, game.position, false);

            return false;
        }
        synchronized (loadGametasks) {
            DownloadTask loadGameTask = loadGametasks.get(pkgnameString);
            if (JLog.isDebug) {
                JLog.i(TAG, "startDownload开始下载loadGameTask=" + loadGameTask
                        + "---pkgnameString=" + pkgnameString + "--isBackground="
                        + isBackground + "--isDownloadNow=" + isDownloadNow);
            }
            if (null == loadGameTask) {
                if (isBackground && XutilsDAO.isAppInstallFaile(game)) {
//                    if("com.prize.music".equals(pkgnameString)){
//                        JLog.i(TAG,"音乐下载失败了2次，后台不再继续下载");
//                    }
                    return false;
                }
                // 新的下载
                DownloadTask.deleteTmpDownloadFile(game.id);
                loadGameTask = new DownloadTask(game);
                loadGametasks.put(pkgnameString, loadGameTask);
                if (JLog.isDebug) {
                    JLog.i(TAG, "startDownload开始下载：任务插入数据库(null == loadGameTask)，放入HashMap-pkgnameString："
                            + pkgnameString + "--isDownloadNow=" + isDownloadNow);
                }
                isNewDownload = true;
                loadGameTask.setBackgroundTaskFlag(isBackground);// 新增任务时，初始化
                // insert to database
                if (loadGameTask.isBackgroundTask()) {
                    appDAO.insertGame(loadGameTask);
                } else {
                    loadGameDAO.insertGame(loadGameTask);
                }
            } else {

                JLog.i(TAG,
                        "startDownload- loadGameTask不等于空=" + loadGameTask
                                + "---pkgnameString=" + pkgnameString
                                + "--isBackground=" + isBackground
                                + "---loadGameTask.isBackgroundTask()="
                                + loadGameTask.isBackgroundTask() + "--isDownloadNow=" + isDownloadNow);

                if (loadGameTask.isBackgroundTask() && !isBackground) {
                    // 后台任务才会切换状态
                    loadGameTask.setBackgroundTaskFlag(false);
                    loadGameDAO.insertGame(loadGameTask);
//                    loadGameDAO.updateGameFlag(loadGameTask);
                }
                // query apk url
                String str = loadGameDAO.getAppDownUrl(pkgnameString);
                String pageInfo = loadGameDAO.getAppPageInf(pkgnameString);
                String backParams = loadGameDAO.getAppBackParams(pkgnameString);
                if (!TextUtils.isEmpty(pageInfo)) {//别的地方点击的以第一次点击的为准
                    loadGameTask.loadGame.pageInfo = pageInfo;
                }
                if (!TextUtils.isEmpty(backParams)) {//别的地方点击的以第一次点击的为准
                    loadGameTask.loadGame.backParams = backParams;
                }
                if (!TextUtils.isEmpty(str)) {
                    loadGameTask.loadGame.downloadUrl = str;
                }
                if (!TextUtils.isEmpty(game.cardId)) {
                    loadGameTask.loadGame.cardId = game.cardId;
                    loadGameTask.loadGame.cardPosition = game.cardPosition;
                }
                // continue download.
                if ((game.versionCode > loadGameTask.loadGame.versionCode)
                        || (DownloadState.STATE_DOWNLOAD_ERROR == loadGameTask.gameDownloadState)) {
                    // 线上版本比下载中的版本更新了，或者下载出错了，重新下载
                    loadGameTask.resetTask(game); // 替换下载游戏信息
                    if (loadGameTask.isBackgroundTask()) {
                        appDAO.updateGame(loadGameTask);
                    } else {
                        loadGameDAO.updateGame(loadGameTask);
                    }
                    // loadGameDAO.updateGame(loadGameTask);
                } else if (DownloadState.STATE_DOWNLOAD_SUCESS == loadGameTask.gameDownloadState) {
                    // 下载的版本已经最新，并已下载成功.
                    // 判断文件是否存在，如果不存在，重新下载. -----用户删除了文件，或者更换了SD卡
                    File apkFile = new File(
                            FileUtils
                                    .getGameAPKFilePath(loadGameTask.loadGame.id));
                    if (apkFile.exists()) {
                        // install
                        JLog.i(TAG, "loadGameTask.loadGame.apkMd5="
                                + loadGameTask.loadGame.packageName);
                        /* MD5校验，防止下载丢包等错误 变动 longbaoxiu----20151119 start--- */
                        if (MD5Util.isDownComplete(apkFile.getAbsolutePath(),
                                loadGameTask.loadGame.packageName)) {
                            if (loadGameTask.isBackgroundTask()
                                    && BaseApplication.isThird) {

                            } else {
                                AppManagerCenter
                                        .installGameApk(loadGameTask.loadGame);

                            }
                            notifyDLTaskUIMsgToHandler(
                                    DownloadState.STATE_DOWNLOAD_SUCESS,
                                    loadGameTask.loadGame.packageName, 0, 1000,
                                    game.position, false);
                        } else {
                            if (apkFile != null && apkFile.exists()
                                    && apkFile.isFile()) {
                                apkFile.delete();
                            }
                            loadGameTask.gameDownloadState = DownloadState.STATE_DOWNLOAD_ERROR;
                        }
                        /* MD5校验，防止下载丢包等错误 longbaoxiu----2015119 start--- */
                        return false;
                    } else {
                        loadGameTask.gameDownloadState = DownloadState.STATE_DOWNLOAD_ERROR;
                    }
                }
            }

			/*
             * if (!loadGameTask.isBackgroundTask() && isThrid) { // cancel
			 * background task first，优先下载前台任务 pauseAllBackgroundDownload(); }
			 */
            if (isDownloadNow) {
                startDownloadTask(loadGameTask, isNewDownload);
            }

            return isNewDownload;
        }
    }


    public void continueBackgroundDownload() {
        JLog.i(TAG, "continueBackgroundDownload");
        if (ClientInfo.getAPNType(BaseApplication.curContext) != ClientInfo.WIFI) {
            // 非WIFI网络，直接退出
            return;
        }
        String autoLoad = DataStoreUtils
                .readLocalInfo(DataStoreUtils.AUTO_LOAD_UPDATE_PKG);
        if (DataStoreUtils.CHECK_OFF.equals(autoLoad)) {
            return;
        }

        if (null == loadGametasks || 0 == loadGametasks.size()) {
            return;
        }

        int checkResult = checkNetAndSpace();
        if (DownloadState.ERROR_NONE == checkResult) {

        } else {
            // 通知错误
            return;
        }

        JLog.i(TAG, "continueBackgroundDownload-loadGametasks.size=" + loadGametasks.size());
        synchronized (loadGametasks) {
            Iterator<Entry<String, DownloadTask>> ite = loadGametasks
                    .entrySet().iterator();
            Entry<String, DownloadTask> entity;
            DownloadTask task;
            while (ite.hasNext()) {
                entity = ite.next();
                task = entity.getValue();
                if ((DownloadState.STATE_DOWNLOAD_SUCESS != task.gameDownloadState)
                        && task.isBackgroundTask()&&!AppManagerCenter.isAppExist(task.loadGame.packageName)) {//启动后台任务，不启动更新任务
                    startDownloadTask(task, false);
                }
            }
        }
    }

    /**
     * cancel downloadtask and delete from database
     *
     * @param game AppsItemBean
     */
    public void cancelDownload(AppsItemBean game) {
        if (null == game || null == game.packageName) {
            return;
        }

        DownloadTask loadGameTask = removeTask(game.packageName);
        if (null != loadGameTask) {
            // stop task
            loadGameTask.cancelTask();
            notifyDLTaskUIMsgToHandler(DownloadState.STATE_DOWNLOAD_CANCEL,
                    game.packageName, 0, loadGameTask.isBackgroundTask(),
                    game.position, false);
        }
    }

    /**
     * pause Download ，停止下载，必须把执行线程置成null
     *
     * @param game AppsItemBean
     */
    public void pauseDownload(AppsItemBean game, boolean isUser) {
        if (null == game || null == game.packageName) {
            return;
        }
        DownloadTask loadGameTask = loadGametasks.get(game.packageName);
        pauseDownloadTask(loadGameTask, isUser);
    }

    /**
     * @param loadGameTask DownloadTask
     * @param isUser       是否用户自己主动操作
     */
    private void pauseDownloadTask(DownloadTask loadGameTask, boolean isUser) {
        if (null == loadGameTask) {
            return;
        }
        loadGameTask.pauseTask(isUser);
        if (loadGameTask.isBackgroundTask()) {
            appDAO.updateGameFlag(loadGameTask);
        } else {
            loadGameDAO.updateGameFlag(loadGameTask);
        }
    }

    /**
     * 继续所有下载
     */
    void continueAllDownload() {
        if (null == loadGametasks || 0 == loadGametasks.size()) {
            return;
        }

        int checkResult = checkNetAndSpace();
        if (DownloadState.ERROR_NONE != checkResult) {
            return;
        }

        synchronized (loadGametasks) {
            Iterator<Entry<String, DownloadTask>> ite = loadGametasks
                    .entrySet().iterator();
            Log.i("NetStateReceiver", "continueAllDownload-loadGametasks.size()=" + loadGametasks.size());
            Entry<String, DownloadTask> entity = null;
            DownloadTask task = null;
            while (ite.hasNext()) {
                entity = ite.next();
                task = entity.getValue();
                if (DownloadState.STATE_DOWNLOAD_SUCESS != task.gameDownloadState
                        && DownloadState.STATE_DOWNLOAD_INSTALLED != task.gameDownloadState) {
                    // 用户主动暂停或者 后台任务不启动继续下载，由外部控制
                    if (!task.isUserPause() && !task.isBackgroundTask()) {
                        startDownloadTask(task, false);
                    }
                }
            }
        }
    }

    /**
     * 继续所有下载
     */
    void continuePauseTask() {
        if (null == loadGametasks || 0 == loadGametasks.size()) {
            return;
        }
        int checkResult = checkNetAndSpace();
        if (DownloadState.ERROR_NONE != checkResult) {
            return;
        }

        synchronized (loadGametasks) {
            Iterator<Entry<String, DownloadTask>> ite = loadGametasks
                    .entrySet().iterator();
            Entry<String, DownloadTask> entity = null;
            DownloadTask task = null;
            while (ite.hasNext()) {
                entity = ite.next();
                task = entity.getValue();
                if (DownloadState.STATE_DOWNLOAD_PAUSE == task.gameDownloadState) {
                    //后台任务不启动继续下载
                    if (!task.isBackgroundTask()) {
                        startDownloadTask(task, false);
                    }
                }
            }
        }
    }

    /**
     * 启动下载
     *
     * @param task DownloadTask
     */
    private void startDownloadTask(DownloadTask task, boolean isNewDownload) {
        if (null == task) {
            return;
        }
        if (JLog.isDebug) {
            JLog.i(TAG, "startDownloadTask-task=" + task.loadGame.packageName + "--" + task.loadGame.name + "-task.isBackgroundTask()=" + task.isBackgroundTask() + "--task=" + task);
        }
        if (!task.isBackgroundTask()) {
            task.startTask(loadListener, isNewDownload);
        } else if (task.isBackgroundTask() && isScreenOn) {
            task.startTask(loadListener, isNewDownload);
        }
        if (task.isBackgroundTask()) {
            appDAO.updateGameFlag(task);
        } else {
            loadGameDAO.updateGameFlag(task);
        }
    }

    public void setScreenOn(boolean isScreenOn) {
        this.isScreenOn = isScreenOn;
    }

    /**
     * 停止所有下载
     */
    public void pauseAllDownload() {
        if (null == loadGametasks || 0 == loadGametasks.size()) {
            return;
        }
        synchronized (loadGametasks) {
            Iterator<Entry<String, DownloadTask>> ite = loadGametasks
                    .entrySet().iterator();
            Entry<String, DownloadTask> entity;
            DownloadTask task;
            while (ite.hasNext()) {
                entity = ite.next();
                task = entity.getValue();
                pauseDownloadTask(task, task.isUserPause());
            }
        }
    }

    /**
     * 停止所有下载
     */
    void pauseAllBackgroudDownload() {
        if (null == loadGametasks || 0 == loadGametasks.size()) {
            return;
        }
        synchronized (loadGametasks) {
            Iterator<Entry<String, DownloadTask>> ite = loadGametasks
                    .entrySet().iterator();
            Entry<String, DownloadTask> entity = null;
            DownloadTask task = null;
            while (ite.hasNext()) {
                entity = ite.next();
                task = entity.getValue();
                if (task.isBackgroundTask()) {
                    pauseDownloadTask(task, task.isUserPause());
                }
            }
        }
    }

    /**
     * 判断是否有正在下载的任务
     */
    public boolean hasDownloadingTask() {
        if (null == loadGametasks || 0 == loadGametasks.size()) {
            return false;
        }
        synchronized (loadGametasks) {
            Iterator<Entry<String, DownloadTask>> ite = loadGametasks
                    .entrySet().iterator();
            Entry<String, DownloadTask> entity = null;
            DownloadTask task = null;
            while (ite.hasNext()) {
                entity = ite.next();
                task = entity.getValue();

                if (task.isBackgroundTask()) {
                    // 后台任务，不计算
                } else {
                    if (task.taskIsLoading()) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    public float getDownloadProgress(String pkgName) {
        DownloadTask task = loadGametasks.get(pkgName);
        if (null == task) {
            return 0;
        }
        return task.progress;
    }

    void removeUIDownloadListener(IServiceCallback refreshHanle) {
        if (null == uiListners) {
            return;
        }
        uiListners.unregister(refreshHanle);
    }

    void setUIDownloadListener(IServiceCallback refreshHanle) {
        uiListners.register(refreshHanle);
    }

    public AppsItemBean getDownloadGameByPkgname(String pkgName) {
        DownloadTask task = loadGametasks.get(pkgName);
        if (null == task) {
            return null;
        }
        return task.loadGame;
    }

    /**
     * 下载，并安装成功游戏
     *
     * @param packageName 包名
     */
    public void installedGame(String packageName) {
        DownloadTask task = loadGametasks.get(packageName);
        if (null == task) {
            // if ((null == task) || task.isBackgroundTask()) {
            return;
        }
        // add by longbaoxiu
        removeTask(packageName);

        notifyDLTaskUIMsgToHandler(DownloadState.STATE_DOWNLOAD_INSTALLED,
                packageName, DownloadState.ERROR_NONE, task.isBackgroundTask(),
                0, false);
    }

    public DownloadTask removeTask(String packageName) {
        if (null == packageName) {
            return null;
        }
        DownloadTask task = null;
        synchronized (loadGametasks) {
            task = loadGametasks.remove(packageName);
            if (task == null)
                return null;
            // remove from DB
            appDAO.deleteData(packageName);
            loadGameDAO.deleteData(packageName);
            return task;
        }
    }

    public DownloadTask getDownloadTask(String pkgName) {
        return loadGametasks.get(pkgName);
    }

    /**
     * 更新app所在页面信息
     * @param pkgName 包名
     * @param pageInfo 页面信息
     */
    void updatePageInfo(String pkgName,String pageInfo) {
        DownloadTask task = loadGametasks.get(pkgName);
        if(task!=null&&task.loadGame!=null){
            task.loadGame.pageInfo=pageInfo;
            GameDAO.getInstance().updatePageInfo(task.loadGame.packageName,pageInfo);
        }
    }

    /**
     * 获取暂停的任务个数是否大于等于2
     *
     * @return boolean 大于等于2返回true
     */
    public boolean hasPauseTaskMoreTwo() {
        if (loadGametasks != null && loadGametasks.size() >= 2) {
            int count = 0;
            DownloadTask task;
            for (String key : loadGametasks.keySet()) {
                task = loadGametasks.get(key);
                if (task == null) continue;
                if (JLog.isDebug) {
                    JLog.i(TAG, "task.isBackgroundTask()=" + task.isBackgroundTask());
                }
                if (task.gameDownloadState == DownloadState.STATE_DOWNLOAD_PAUSE && !task.isBackgroundTask()) {
                    count++;
                }
                if (count >= 2) return true;
            }
            return count >= 2;
        }
        return false;
//        Cursor cursor = PrizeDatabaseHelper.query(
//                DownloadGameTable.TABLE_NAME_GAME, mColunmGameItemName, DownloadGameTable.GAME_DOWNLOAD_STATE+ "=?",
//                new String[]{String.valueOf(DownloadState.STATE_DOWNLOAD_PAUSE)}, null, null, null);
//        if (JLog.isDebug) {
//            JLog.i("GameDAO","hasPauseTaskMoreTwo-cursor.getCount()="+cursor.getCount());
//        }
//        return cursor!=null&&cursor.getCount()>=2;
    }


    private boolean checkDownloadSucess(String pkgname) {
        if (null == pkgname) {
            return false;
        }
        DownloadTask task = loadGametasks.get(pkgname);
        if (null == task) {
            return false;
        }
        try {
            // 文件重命名
            String gameCode = String.valueOf(task.loadGame.id);
            if (renameDownloadGameAPP(gameCode)) {
                processDifState(pkgname, task);
                if (!TextUtils.isEmpty(task.isUpdate_install)) {
                    Message msg = Message.obtain();
                    msg.what = 0;
                    Bundle data = new Bundle();
                    data.putString("name", task.loadGame.name);
                    data.putString("packageName", pkgname);
                    data.putString("id", task.loadGame.id);
                    data.putString("isUpdate_install", task.isUpdate_install);
                    if (!TextUtils.isEmpty(task.loadGame.pageInfo)) {
                        data.putString("pageInfo", task.loadGame.pageInfo);
                        if (JLog.isDebug) {
                            JLog.i(TAG, "checkDownloadSucess-task.loadGame.pageInfo=" + task.loadGame.pageInfo);
                        }
                    }
                    if (!TextUtils.isEmpty(task.loadGame.backParams)) {
                        data.putString("backParams", task.loadGame.backParams);
                        if (JLog.isDebug) {
                            JLog.i(TAG, "checkDownloadSucess-task.loadGame.pageInfo=" + task.loadGame.backParams);
                        }
                    }
                    msg.setData(data);
                    if (BaseApplication.handler != null) {
                        BaseApplication.handler.sendMessage(msg);
                    }
                }

                return true;
            } else {
                // 文件意外删除了
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    private void processDifState(String pkgname, DownloadTask task) {
        if (task.isBackgroundTask()) {
            appDAO.updateState(pkgname,
                    DownloadState.STATE_DOWNLOAD_SUCESS);
            loadGameDAO.insertGame(task);
            loadGameDAO.updateState(pkgname,
                    DownloadState.STATE_DOWNLOAD_SUCESS);
            // 后台任务第三方版不安装,
            AppManagerCenter.installGameApk(task.loadGame, false);

            String shieldpackages = DataStoreUtils.readLocalInfo(Constants.PREALOADS);
            JLog.i(TAG, "processDifState-task.isUpdate_install=" + task.isUpdate_install + "--pkgname=" + pkgname);
            if (!TextUtils.isEmpty(shieldpackages) && !shieldpackages.contains(pkgname) && task.isUpdate_install.equals(Constants.UPDATE_INSTALL)) {
                // add by longbaoxiu 2017-02-24 后台更新任务并且是非预装任务添加到已下载
                Uri downloadedUri = Uri.parse("content://com.prize.appcenter.provider.appstore/table_downLoaded");
                try {
                    BaseApplication.curContext.getContentResolver().insert(
                            downloadedUri, DownLoadedDAO.getInstance().getContentValues(task.loadGame));
                } catch (Exception e) {
                    e.printStackTrace();
                    JLog.i(TAG, "processDifState-e=" + e.getMessage());
                }
            }


        } else {
            // add by huanglingjun 2015-12-21 前台任务下载完才会添加到已下载
            Uri downloadedUri = Uri.parse("content://com.prize.appcenter.provider.appstore/table_downLoaded");
            try {
                BaseApplication.curContext.getContentResolver().insert(
                        downloadedUri,
                        DownLoadedDAO.getInstance().getContentValues(
                                task.loadGame));
//                notifyDLTaskUIMsgToHandler(DownloadState.STATE_DOWNLOAD_SUCESS, pkgname, DownloadState.ERROR_NONE,
//                        task.isBackgroundTask(), 0, false);
                AppManagerCenter.installGameApk(task.loadGame);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 将临时文件重命名
     *
     * @param gameCode 应用的id
     * @throws IOException IOException
     */
    private boolean renameDownloadGameAPP(String gameCode) throws IOException {
        File oldfile = new File(FileUtils.getDownloadTmpFilePath(gameCode));
        File newfile = new File(FileUtils.getGameAPKFilePath(gameCode));

        if (newfile.exists()) {
            newfile.delete();
        }

        if (oldfile.exists()) {
//            if (CommonUtils.isLegalApk(oldfile.getAbsolutePath())) {
            oldfile.renameTo(newfile);
            return true;
//            }
//            return false;
        } else {
            // 文件意外删除了
            return false;
        }
    }

    /**
     * 方法描述：清空下载完成的任务
     */
    public void clearAllLoadGametasks(List<AppsItemBean> datas) {
        if (loadGametasks != null && loadGametasks.size() > 0) {
            for (AppsItemBean appsItemBean : datas) {
                if (appsItemBean == null || TextUtils.isEmpty(appsItemBean.packageName)) {
                    continue;
                }
                DownloadTask task = loadGametasks.get(appsItemBean.packageName);
                if (task != null) {
                    task.pauseTask(true);
                    loadGametasks.remove(appsItemBean.packageName);
                }
            }
        }
    }


    public int getDownloadSpeed(String song_id) {
        DownloadTask task = loadGametasks.get(song_id);
        if (null == task) {
            return 0;
        }
        return task.downloadSpeed;
    }
}
