package com.prize.app.download;

import android.text.TextUtils;

import com.prize.app.constants.Constants;
import com.prize.app.net.datasource.base.AppsItemBean;
import com.prize.app.threads.DownloadThreadPool;
import com.prize.app.util.FileUtils;
import com.prize.app.util.JLog;

import java.io.File;

/**
 * 下载执行模块
 *
 * @author prize
 */
public class DownloadTask {
    public AppsItemBean loadGame;
    /**
     * 下载状态
     */
    public int gameDownloadState = DownloadState.STATE_DOWNLOAD_WAIT;
    /**
     * 已经下载的大小
     */
    public int gameDownloadPostion;
    /**
     * 下载的执行线程，被中断或者暂停或者停止，请置成null，否则无法continue
     */
    private Download download;
    protected float progress;
    public int loadFlag; // 任务标记
    /**
     * 后台任务
     ****/
    private static final int LOADTASK_FLAG_BACKGROUD = 0x0001;
    /**
     * 用户主动暂停下载，恢复下载的时候，需要用户主动恢复
     */
    private static final int LOADTASK_FLAG_STOP_BY_USER = 0x0002;
    public String isUpdate_install;

    /**
     * 以Kb单位返回
     */
    protected int downloadSpeed;

    public DownloadTask() {
        loadGame = new AppsItemBean();
    }

    public DownloadTask(AppsItemBean game) {
        loadGame = game;
        loadFlag = 0;
        if (AppManagerCenter.isAppExist(game.packageName)) {
            isUpdate_install = Constants.UPDATE_INSTALL;
            if (game.downloadUrl.contains("/appstore/appinfo/")) {
                game.downloadUrl = game.downloadUrl + "&type=up";
            }
        } else {
            isUpdate_install = Constants.DOWNLOAD_INSTALL;
            if (game.downloadUrl.contains("/appstore/appinfo/")) {
                game.downloadUrl = game.downloadUrl + "&type=down";
            }
        }
        game.installType=isUpdate_install;
    }

    /**
     * 删除下载的临时文件
     *
     * @param gameCode appId
     */
    public static void deleteTmpDownloadFile(String gameCode) {
        String downloadFile = FileUtils.getDownloadTmpFilePath(gameCode);
        // 删除临时文件
        File file = new File(downloadFile);
        if (file.exists()) {
            file.delete();
        }
        File file2 = new File(FileUtils.getPatchFilePath(gameCode));
        if (file2.exists()) {
            file2.delete();
        }
    }

    public void setBackgroundTaskFlag(boolean isBackground) {
        if (isBackground) {
            loadFlag |= LOADTASK_FLAG_BACKGROUD;
        } else {
            loadFlag &= (~LOADTASK_FLAG_BACKGROUD);
        }
    }

    public boolean isBackgroundTask() {
        return ((loadFlag & LOADTASK_FLAG_BACKGROUD) != 0);
    }

    public boolean isUserPause() {
        return ((loadFlag & LOADTASK_FLAG_STOP_BY_USER) != 0);
    }

    /**
     * 设置暂停标识
     *
     * @param isUserPaused true:用户主动暂停,false:用户取消暂停
     */
    private void setLoadTaskPauseFlag(boolean isUserPaused) {
        if (isUserPaused) {
            loadFlag |= LOADTASK_FLAG_STOP_BY_USER;
        } else {
            loadFlag &= (~LOADTASK_FLAG_STOP_BY_USER); // 移除暂停标识
        }
    }

    void startTask(DownloadState listener,boolean isNewDownload) {
        setLoadTaskPauseFlag(false);
        if (null == download) {
            String toApkMd5 = null;
            String downloadUrl = loadGame.downloadUrl;
            if (loadGame.appPatch != null && !TextUtils.isEmpty(loadGame.appPatch.toApkMd5)) {
                toApkMd5 = loadGame.appPatch.toApkMd5;
                downloadUrl = loadGame.appPatch.patchUrl;
            }
            download = new Download(String.valueOf(loadGame.id), downloadUrl,
                    loadGame.packageName, Integer.parseInt(loadGame.apkSize),
                    loadGame.position, toApkMd5, listener,loadGame.pageInfo);
        } else {
            // 已经有下载线程
            return;
        }

        // 继续所有下载的时候,如果是错误下载，删除下载的临时文件
        if (DownloadState.STATE_DOWNLOAD_ERROR == gameDownloadState) {
            DownloadTask.deleteTmpDownloadFile(loadGame.id + "");
            setDownloadSize(Integer.parseInt(loadGame.apkSize), 0);
        }
        // 启动下载,状态都置成 等待下载
        download.readyDownload(isNewDownload);
        DownloadThreadPool.getDownloadThreadExe().execute(download);
    }

    /**
     * 必须把执行线程置成null,否则无法continue
     */
    public void pauseTask(boolean isUser) {
        // stop download
        setLoadTaskPauseFlag(isUser);
        if (null == download) {
            return;
        }
        download.stopDownloadByResult(DownloadState.ERROR_NONE);
        download = null;
    }

    public void resetDownloadRunnable() {
        download = null;
    }

    /**
     * 取消任务
     */
    public void cancelTask() {
        // cancel download
        if (null != download) {
            download.stopDownloadByResult(DownloadState.ERROR_NONE);
            download = null;
        }
        gameDownloadPostion = 0;
        gameDownloadState = 0;
        deleteTmpDownloadFile(loadGame.id);
    }

    /**
     * 重置task
     */
    public void resetTask(AppsItemBean game) {
        cancelTask();
        loadGame = game;
        int state = AppManagerCenter.getGameAppState(game.packageName, game.id,
                game.versionCode);
        isUpdate_install = state == AppManagerCenter.APP_STATE_UPDATE ? "update_install"
                : "download_install";
    }

    /**
     * task 是否在下载中
     *
     * @return boolean
     */
    public boolean taskIsLoading() {
//		if (null != download) {
        return null != download;
//		}
//		return false;
    }

    public void setDownloadSize(int totalSize, int loadSize) {
        gameDownloadPostion = loadSize;
        loadGame.apkSize = totalSize + "";
        loadSize = loadSize >> 10;
        totalSize = totalSize >> 10;
        if (totalSize != 0) {
            progress = (float) loadSize * 100 / totalSize;
        }
        if (JLog.isDebug) {
            JLog.i("DownloadTask","loadSize="+loadSize);
            JLog.i("DownloadTask","totalSize="+totalSize);
            JLog.i("DownloadTask","progress="+progress);
        }
    }

    public void setDownloadSize(int loadSize) {
        gameDownloadPostion = loadSize;
        progress = 0;
    }

    public void setDownloadSpeed(int downloadSpeed) {
        this.downloadSpeed = downloadSpeed;
    }
}
