package com.prize.app.download;

import java.io.File;
import java.util.HashMap;

import org.json.JSONObject;

import android.text.TextUtils;

import com.prize.app.BaseApplication;
import com.prize.app.constants.RequestMethods;
import com.prize.app.threads.DownloadThreadPool;
import com.prize.app.util.FileUtils;
import com.prize.app.util.JLog;
import com.prize.app.util.SDKUtil;
import com.prize.app.util.ToastUtils;
import com.prize.app.xiami.RequestManager;
import com.prize.onlinemusibean.SongDetailInfo;
import com.xiami.sdk.XiamiSDK;

/**
 * 下载执行模块
 * 
 * @author prize
 *
 */
public class DownloadTask {
	public SongDetailInfo loadGame;
	/** 下载状态 */
	public int gameDownloadState = DownloadState.STATE_DOWNLOAD_WAIT;
	/** 已经下载的大小 */
	public int gameDownloadPostion;
	/** 下载的执行线程，被中断或者暂停或者停止，请置成null，否则无法continue */
	private Download download;
	protected int progress;
	/**
	 * 以Kb单位返回
	 */
	protected int downloadSpeed;
	public int loadFlag; // 任务标记
	private String TAG = "DownloadTask";
	// private boolean isStartInstall;
	/***** 下载的标记值 *****/
	/** 后台任务 ****/
	private static final int LOADTASK_FLAG_BACKGROUD = 0x0001;
	/** 用户主动暂停下载，恢复下载的时候，需要用户主动恢复 */
	private static final int LOADTASK_FLAG_STOP_BY_USER = 0x0002;

	// public String isUpdate_install;

	public DownloadTask() {
		loadGame = new SongDetailInfo();
	}

	public DownloadTask(SongDetailInfo game) {
		loadGame = game;
		loadFlag = 0;
	}

	// public void setStartInstall(boolean isStartInstall) {
	// this.isStartInstall = isStartInstall;
	// }
	//
	// public boolean getIsStartInstall() {
	// return isStartInstall;
	// }

	/**
	 * 删除下载的临时文件
	 * 
	 * @param gameCode
	 */
	public static void deleteTmpDownloadFile(String gameCode) {
		String downloadFile = FileUtils.getDownloadTmpFilePath(gameCode);
		// 删除临时文件
		File file = new File(downloadFile);
		if (file.exists()) {
			file.delete();
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

	public void startTask(DownloadState listener) {
		setLoadTaskPauseFlag(false);
		if (null == download) {
			download = new Download(loadGame, listener);
		} else {
			// 已经有下载线程
			return;
		}

		// 继续所有下载的时候,如果是错误下载，删除下载的临时文件
		if (DownloadState.STATE_DOWNLOAD_ERROR == gameDownloadState) {
			DownloadTask.deleteTmpDownloadFile(DownloadHelper.getTempFileName(loadGame));
			// setDownloadSize(Integer.parseInt(loadGame.apkSize), 0);
		}
		// if has running, do nothing
		// 启动下载,状态都置成 等待下载
		download.readyDownload();
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
		// deleteTmpDownloadFile(loadGame.id);
	}

	/**
	 * 重置task
	 */
	public void resetTask(SongDetailInfo game) {
		cancelTask();
		loadGame = game;
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
		loadGame.totalSize = totalSize;
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

}
