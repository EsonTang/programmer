package com.prize.prizethemecenter.manage;

import com.prize.prizethemecenter.bean.SingleThemeItemBean;
import com.prize.prizethemecenter.ui.utils.FileUtils;

import java.io.File;

/**
 * Created by Administrator on 2016/12/22.
 */
public class DownloadTask {
    public SingleThemeItemBean.ItemsBean loadGame;

    /** 下载状态 */
    public int gameDownloadState = DownloadState.STATE_DOWNLOAD_WAIT;
    /** 已经下载的大小 */
    public int gameDownloadPostion;
    /** 下载的执行线程，被中断或者暂停或者停止，请置成null，否则无法continue */
    private Download download;
    public int type; //下载的文件类型
    public int progress;
    public int loadFlag; // 任务标记
    private String TAG = "DownloadTask";
    private static final int LOADTASK_FLAG_BACKGROUD = 0x0001;
    /** 用户主动暂停下载，恢复下载的时候，需要用户主动恢复 */
    private static final int LOADTASK_FLAG_STOP_BY_USER = 0x0002;
    public String isUpdate_install;
    /** 下载的执行线程，被中断或者暂停或者停止，请置成null，否则无法continue */

    /**
     * 以Kb单位返回
     */
    protected int downloadSpeed;
    public DownloadTask(SingleThemeItemBean.ItemsBean itemsBean,int type) {
        loadGame = itemsBean;
        this.type = type;
        loadFlag = 0;
    }

    public int getGameDownloadState() {
        return gameDownloadState;
    }

    public void setGameDownloadState(int gameDownloadState) {
        this.gameDownloadState = gameDownloadState;
    }

    public void setBackgroundTaskFlag(boolean isBackground) {
        if (isBackground) {
            loadFlag |= LOADTASK_FLAG_BACKGROUD;
        } else {
            loadFlag &= (~LOADTASK_FLAG_BACKGROUD);
        }
    }

    public void onDestory() {
        download=null;
        progress = 0;
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
     * @param isUserPaused
     *            true:用户主动暂停,false:用户取消暂停
     */
    private void setLoadTaskPauseFlag(boolean isUserPaused) {
        if (isUserPaused) {
            loadFlag |= LOADTASK_FLAG_STOP_BY_USER;
        } else {
            loadFlag &= (~LOADTASK_FLAG_STOP_BY_USER); // 移除暂停标识
        }
    }

    public void startTask(DownloadState listener,int type) {
        setLoadTaskPauseFlag(false);
        if (null == download) {
            download = new Download(loadGame, listener,type);
        } else {
            // 已经有下载线程
            return;
        }
        if (DownloadState.STATE_DOWNLOAD_ERROR == gameDownloadState) {
            DownloadTask.deleteTmpDownloadFile(FileUtils.getTempFileName(loadGame),type);
            // setDownloadSize(Integer.parseInt(loadGame.apkSize), 0);
        }
        download.readyDownload();
        ThreadPoolManager.getDownloadPool().execute(download);

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
    public void cancelTask(int type) {
        // cancel download
        if (null != download) {
            download.stopDownloadByResult(DownloadState.ERROR_NONE);
            download = null;
        }
        gameDownloadPostion = 0;
        gameDownloadState = 0;
//        deleteTmpDownloadFile(loadGame.id,type);
//        deleteDownloadFile(loadGame.id,type);
    }

    /**
     * 重置task
     */
    public void resetTask(SingleThemeItemBean.ItemsBean game,int type) {
        cancelTask(type);
        loadGame = game;
        this.type = type;
    }

    /**
     * task 是否在下载中
     *
     * @return
     */
    public boolean taskIsLoading() {
        if (null != download) {
            return true;
        }
        return false;
    }

    public void setDownloadSize(int totalSize, int loadSize) {
        gameDownloadPostion = loadSize;
        loadGame.setSize(totalSize+"");
        loadSize = loadSize >> 10;
        totalSize = totalSize >> 10;

        if (totalSize != 0) {
            progress = loadSize * 100 / totalSize;
        }
    }

    public void setDownloadSize(int loadSize) {
        gameDownloadPostion = loadSize;
        progress = 0;
    }

    public void setDownloadSpeed(int downloadSpeed) {
        this.downloadSpeed = downloadSpeed;
    }



    /**
     * 删除下载的临时文件
     *
     * @param gameCode
     */
    public static void deleteTmpDownloadFile(String gameCode,int type) {
        String downloadFile = FileUtils.getDownloadTempPath(gameCode,type);
        // 删除临时文件
        File file = new File(downloadFile);
        if (file.exists()) {
            file.delete();
        }
    }


    /**
     * 删除已经下载的文件
     *
     * @param gameCode
     */
    public static void deleteDownloadFile(String gameCode,int type) {
        String downloadFile = FileUtils.getDownloadPath(gameCode,type);
        // 删除临时文件
        File file = new File(downloadFile);
        if (file.exists()) {
            file.delete();
        }
    }


}
