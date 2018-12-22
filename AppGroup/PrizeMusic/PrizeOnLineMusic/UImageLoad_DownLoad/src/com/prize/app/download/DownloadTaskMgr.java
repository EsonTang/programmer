package com.prize.app.download;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map.Entry;

import org.json.JSONObject;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.widget.Toast;

import com.prize.app.BaseApplication;
import com.prize.app.beans.ClientInfo;
import com.prize.app.constants.Constants;
import com.prize.app.constants.RequestMethods;
import com.prize.app.constants.RequestResCode;
import com.prize.app.database.dao.AppDAO;
import com.prize.app.database.dao.DownLoadedDAO;
import com.prize.app.database.dao.GameDAO;
import com.prize.app.syncsongs.Executor;
import com.prize.app.syncsongs.impl.ThreadExecutor;
import com.prize.app.threads.SQLSingleThreadExcutor;
import com.prize.app.threads.SingleThreadExecutor;
import com.prize.app.util.DataStoreUtils;
import com.prize.app.util.FileUtils;
import com.prize.app.util.JLog;
import com.prize.app.util.SDKUtil;
import com.prize.app.util.ToastUtils;
import com.prize.app.xiami.RequestManager;
import com.prize.onlinemusibean.SongDetailInfo;
import com.xiami.sdk.XiamiSDK;
import com.xiami.sdk.entities.LocalSongTag;
import com.xiami.sdk.utils.ImageUtil;

/**
 **
 * 下载管理类
 * 
 * @author prize
 * @version V1.0
 */
public final class DownloadTaskMgr {
	private static final String TAG = "DownloadTaskMgr";

	/** 下载任务 */
	private HashMap<String, DownloadTask> loadGametasks;
	/** 下载模块的状态监听，通下载线程交互 */
	private DownloadState loadListener;
	/** 下载模块的handler，和UI交互 **/
	private Handler loadHandler;
	/** 通知UI下载的状态 */
	private HashSet<UIDownLoadListener> uiListners;
	private HashSet<UIDownLoadListener> loopListners; // 专门用户循环
	private GameDAO loadGameDAO;
	private Executor syncSongsExecutor;
	// private AppDAO appDAO;
	private long lastRefreshUI = 0;
	private static volatile DownloadTaskMgr instance = null;
	XiamiSDK mXiamiSDK;

	private DownloadTaskMgr() {
		// 不允许外部实例化
		initDownloadAppMode();
	}

	/**
	 * 只通知UI,不做其他逻辑处理
	 * 
	 * @param state
	 *            下载状态
	 * @param errorCode
	 *            错误码
	 * @param song_id
	 */
	private void notifyUIDownloadState(int state, int errorCode, int song_Id) {
		if (null == uiListners || 0 == uiListners.size()) {
			return;
		}
		if (null == loopListners) {
			loopListners = new HashSet<UIDownLoadListener>();
		}
		loopListners.clear();
		loopListners.addAll(uiListners);

		for (UIDownLoadListener listener : loopListners) {
			listener.handleDownloadState(state, errorCode, song_Id);
		}
		// 最后通知的时间
		lastRefreshUI = System.currentTimeMillis();
		loopListners.clear();
	}

	/**
	 * 提供 appManagerCenter，静默安装的时候，状态变化刷新
	 */
	public void notifyRefreshUI(int state) {
		notifyDLTaskUIMsgToHandler(state, -1, 0, false);
	}

	private void notifyDLTaskUIMsgToHandler(int state, int song_id,
			int errorCode, boolean isBackground) {
		if (isBackground) {

		} else {
			notifyDLTaskUIMsgToHandler(state, song_id, errorCode, 0);
		}
	}

	/**
	 * 通知task的状态给Handler，发送给UI的监听.只通知UI,不做其他逻辑处理
	 * 
	 * @param state
	 * @param song_id
	 * @param errorCode
	 */
	private synchronized void notifyDLTaskUIMsgToHandler(int state,
			int song_id, int errorCode, long delayMillis) {
		Message msg = Message.obtain();
		msg.what = state;
		msg.arg1 = errorCode;
		msg.arg2 = song_id;
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
		mXiamiSDK = new XiamiSDK(BaseApplication.curContext, SDKUtil.KEY,
				SDKUtil.SECRET);
		loadGameDAO = GameDAO.getInstance();
		syncSongsExecutor = ThreadExecutor.getInstance();
		loadHandler = new Handler() {
			public void handleMessage(Message msg) {
				loadHandler
						.removeMessages(DownloadState.STATE_DOWNLOAD_UPDATE_PROGRESS); // 避免过度刷屏
				notifyUIDownloadState(msg.what, msg.arg1, msg.arg2);
			};
		};

		// 下载模块的状态监听，同下载线程交互
		loadListener = new DownloadState() {
			@Override
			public void onDownloadState(int state, int song_id, int errorCode) {
				DownloadTask task = null;
//				JLog.i(TAG, "进度更新---state=" + state + "--song_id=" + song_id);
				synchronized (loadGametasks) {
					task = loadGametasks.get(String.valueOf(song_id));
					if (null == task) {
						return;
					}
					// 先更新task的状态，后续执行，会和状态相关
					task.gameDownloadState = state;

					if (DownloadState.STATE_DOWNLOAD_SUCESS == state) {
						task.resetDownloadRunnable();// 移除下载任务线程，否则pause全部的时候，状态会被置成暂停
						if (checkDownloadSucess(task.loadGame)) {
							// if (task.isBackgroundTask()) {
							// } else {
							loadGameDAO.updateState(song_id, state,
									System.currentTimeMillis());
							syncSongsExecutor.execute(song_id);
							JLog.i(TAG, "SUCESS-state=" + state);
							// }
							// loadGameDAO.updateState(song_id, state);
							// 继续后台任务的下载
							// if (task.isBackgroundTask()) {
							// } else {
							// continueBackgroundDownload();
							// }
						} else {
							// 下载成功后，发现文件出错，重新下载
							task.gameDownloadState = DownloadState.STATE_DOWNLOAD_ERROR;
							startDownload(task.loadGame,
									task.isBackgroundTask());
							return;
						}
					} else if ((DownloadState.STATE_DOWNLOAD_PAUSE == state)
							|| (DownloadState.STATE_DOWNLOAD_ERROR == state)) {
						// 下载任务线程被结束或暂停了
						task.resetDownloadRunnable();
						if (task.isBackgroundTask()) {
							// appDAO.updateState(song_id, state);
						} else {
							loadGameDAO.updateState(song_id, state);
						}
						// loadGameDAO.updateState(song_id, state);
						if ((DownloadState.ERROR_CODE_TIME_OUT == errorCode)
								|| (DownloadState.ERROR_CODE_HTTP == errorCode)
								|| (DownloadState.ERROR_CODE_URL_ERROR == errorCode)) {
							// 如果是超时，或者网络连接失败，重试
							task.gameDownloadState = state;
							startDownload(task.loadGame,
									task.isBackgroundTask());

							return;
						}

						if (task.isBackgroundTask()) {
							// 后台任务被暂停，不继续后台任务下载.原因： 可能是因为正常任务把后台任务中止的。故不再启动
						} else {
							// 正常任务暂停后，继续后台任务的下载
							continueBackgroundDownload();
						}
					}
				}
				if (task != null)
					notifyDLTaskUIMsgToHandler(state, song_id, errorCode,
							task.isBackgroundTask());
			}

			@Override
			public void updateDownloadProgress(int song_id,
					int downloadFileSize, int downloadPosition,
					int downloadSpeed) {
				DownloadTask task = loadGametasks.get(String.valueOf(song_id));
				if (null == task) {
					return;
				}
				// 判断刷新的频率，防止过度刷屏
				long current = System.currentTimeMillis();
				if ((current - lastRefreshUI) < 1000) {
					// 如果900ms内已经刷新
				} else {
					if (!task.isBackgroundTask()) {
						notifyDLTaskUIMsgToHandler(
								DownloadState.STATE_DOWNLOAD_UPDATE_PROGRESS,
								song_id, DownloadState.ERROR_NONE,
								task.isBackgroundTask());
					} else {
					}
				}

				task.setDownloadSize(downloadFileSize, downloadPosition);
				task.setDownloadSpeed(downloadSpeed);
				// update database
				if (task.isBackgroundTask()) {
					// appDAO.updateDownloadSize(song_id, downloadFileSize,
					// downloadPosition);
				} else {
					loadGameDAO.updateDownloadSize(song_id + "",
							downloadFileSize, downloadPosition);
				}
				// loadGameDAO.updateDownloadSize(song_id, downloadFileSize,
				// downloadPosition);
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
						DownloadState.STATE_DOWNLOAD_MODE_INIT, -1, 0, 5000); // 5s后执行，需要等UI准备好
			}
		});
	}

	/**
	 * 查看网络状态，是否有SD卡
	 * 
	 * @param force
	 * @return 错误值 : DownloadState.ERROR_CODE_NO_NET,
	 *         DownloadState.ERROR_CODE_NOT_WIFI
	 *         ,DownloadState.ERROR_CODE_NO_SDCARD
	 */
	private int checkNetAndSpace() {
		int netType = ClientInfo.networkType;
		int errorCode = DownloadState.ERROR_NONE;

		if (netType == ClientInfo.NONET) {
			// 通知网络错误
			errorCode = DownloadState.ERROR_CODE_NO_NET;
		} else if ((netType != ClientInfo.WIFI)
				&& BaseApplication.isDownloadWIFIOnly()) {
			// 通知网络设置
			// errorCode = DownloadState.ERROR_CODE_NOT_WIFI;
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
	 * @param game
	 * @param force
	 * @return false: 继续下载， true： 新下载
	 */
	public boolean startDownload(final SongDetailInfo songDetailInfo,
			boolean isBackground) {
		boolean isNewDownload = false;
		if (null == songDetailInfo || songDetailInfo.song_id <= 0) {
			return isNewDownload;
		}

		int song_id = songDetailInfo.song_id;

		// check net and space
		int checkResult = checkNetAndSpace();
		if (DownloadState.ERROR_NONE == checkResult) {

		} else {
			// 通知错误
			notifyDLTaskUIMsgToHandler(DownloadState.STATE_DOWNLOAD_ERROR,
					songDetailInfo.song_id, checkResult, isBackground);

			return false;
		}
		synchronized (loadGametasks) {
			DownloadTask loadGameTask = loadGametasks.get(String
					.valueOf(song_id));

			if (null == loadGameTask) {
				// 新的下载
				// DownloadTask.deleteTmpDownloadFile(DownloadHelper
				// .getTempFileName(game));
				DownloadHelper.deleteTmpDownloadFile(songDetailInfo);
				loadGameTask = new DownloadTask(songDetailInfo);
				loadGametasks.put(String.valueOf(song_id), loadGameTask);
				isNewDownload = true;
				loadGameTask.setBackgroundTaskFlag(isBackground);// 新增任务时，初始化
				loadGameDAO.insertGame(loadGameTask);
				// }
			} else {
				if (loadGameTask.isBackgroundTask() && !isBackground) {
					// 后台任务才会切换状态
					loadGameTask.setBackgroundTaskFlag(false);
					loadGameDAO.insertGame(loadGameTask);
					loadGameDAO.updateGameFlag(loadGameTask);
				}
				// continue download.
				if ((DownloadState.STATE_DOWNLOAD_ERROR == loadGameTask.gameDownloadState)) {
					loadGameTask.resetTask(songDetailInfo); // 替换下载游戏信息
					if (loadGameTask.isBackgroundTask()) {
						// appDAO.updateGame(loadGameTask);
					} else {
						loadGameDAO.updateGame(loadGameTask);
					}
					// loadGameDAO.updateGame(loadGameTask);
				} else if (DownloadState.STATE_DOWNLOAD_SUCESS == loadGameTask.gameDownloadState) {
					// 判断文件是否存在，如果不存在，重新下载. -----用户删除了文件，或者更换了SD卡
					if (DownloadHelper.isFileExists(loadGameTask.loadGame)) {
						return isNewDownload;
					} else {
						loadGameTask.gameDownloadState = DownloadState.STATE_DOWNLOAD_ERROR;
					}
				}
			}

			startDownloadTask(loadGameTask);
			return isNewDownload;
		}
	}

	/**
	 * 批量开始下载接口
	 * 
	 * @param game
	 * @param force
	 * @return false: 继续下载， true： 新下载
	 */
	public void startBatchDownload(final ArrayList<SongDetailInfo> lists,
			boolean isBackground) {
		// ArrayList<DownloadTask> newloadGameTask = new
		// ArrayList<DownloadTask>();
		// ArrayList<DownloadTask> oldloadGameTask = new
		// ArrayList<DownloadTask>();
		boolean isNewDownload = false;
		for (SongDetailInfo songDetailInfo : lists) {
			if (null == songDetailInfo || songDetailInfo.song_id <= 0) {
				return;
			}

			int song_id = songDetailInfo.song_id;

			// check net and space
			int checkResult = checkNetAndSpace();
			if (DownloadState.ERROR_NONE == checkResult) {

			} else {
				// 通知错误
				notifyDLTaskUIMsgToHandler(DownloadState.STATE_DOWNLOAD_ERROR,
						songDetailInfo.song_id, checkResult, isBackground);

				return;
			}
			synchronized (loadGametasks) {
				DownloadTask loadGameTask = loadGametasks.get(String
						.valueOf(song_id));

				if (null == loadGameTask) {
					// 新的下载
					DownloadHelper.deleteTmpDownloadFile(songDetailInfo);
					loadGameTask = new DownloadTask(songDetailInfo);
					loadGametasks.put(String.valueOf(song_id), loadGameTask);
					isNewDownload = true;
					loadGameTask.setBackgroundTaskFlag(isBackground);// 新增任务时，初始化
					loadGameDAO.insertGame(loadGameTask);
					// newloadGameTask.add(loadGameTask);
				} else {
					// continue download.
					if ((DownloadState.STATE_DOWNLOAD_ERROR == loadGameTask.gameDownloadState)) {
						loadGameTask.resetTask(songDetailInfo); // 替换下载游戏信息
						loadGameDAO.updateGame(loadGameTask);
					} else if (DownloadState.STATE_DOWNLOAD_SUCESS == loadGameTask.gameDownloadState) {
						// 判断文件是否存在，如果不存在，重新下载. -----用户删除了文件，或者更换了SD卡
						if (DownloadHelper.isFileExists(loadGameTask.loadGame)) {
						} else {
							loadGameTask.gameDownloadState = DownloadState.STATE_DOWNLOAD_ERROR;
						}
					}
				}

				startDownloadTask(loadGameTask);

			}

		}
	}

	// /**
	// * 停止所有下载
	// */
	// public void pauseAllBackgroundDownload() {
	// if (null == loadGametasks || 0 == loadGametasks.size()) {
	// return;
	// }
	// synchronized (loadGametasks) {
	// Iterator<Entry<String, DownloadTask>> ite = loadGametasks
	// .entrySet().iterator();
	// Entry<String, DownloadTask> entity = null;
	// DownloadTask task = null;
	// while (ite.hasNext()) {
	// entity = ite.next();
	// task = entity.getValue();
	// if (task.isBackgroundTask()) {
	// pauseDownloadTask(task, false);
	// }
	// }
	// }
	// }

	public void continueBackgroundDownload() {
		if (ClientInfo.networkType != ClientInfo.WIFI) {
			// 非WIFI网络，直接退出
			return;
		}

		// String autoLoad = DataStoreUtils
		// .readLocalInfo(DataStoreUtils.AUTO_LOAD_UPDATE_PKG);
		// if (DataStoreUtils.CHECK_OFF.equals(autoLoad)) {
		// return;
		// }

		if (null == loadGametasks || 0 == loadGametasks.size()) {
			return;
		}

		int checkResult = checkNetAndSpace();
		if (DownloadState.ERROR_NONE == checkResult) {

		} else {
			// 通知错误
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
				if ((DownloadState.STATE_DOWNLOAD_SUCESS != task.gameDownloadState)
						&& task.isBackgroundTask()) {
					startDownloadTask(task);
				}
			}
		}
	}

	/**
	 * cancel downloadtask and delete from database
	 * 
	 * @param game
	 */
	public void cancelDownloadNoDB(SongDetailInfo game) {
		if (null == game || null == String.valueOf(game.song_id)) {
			return;
		}
		DownloadTask task = null;
		synchronized (loadGametasks) {
			task = loadGametasks.remove(String.valueOf(game.song_id));
			if (task == null)
				return;
		}
		if (null != task) {
			// stop task
			task.cancelTask();
			// notifyDLTaskUIMsgToHandler(DownloadState.STATE_DOWNLOAD_CANCEL,
			// game.song_id, 0, task.isBackgroundTask());
		}
	}

	/**
	 * cancel downloadtask and delete from database
	 * 
	 * @param game
	 */
	public void cancelDownload(SongDetailInfo game) {
		if (null == game || null == String.valueOf(game.song_id)) {
			return;
		}

		DownloadTask loadGameTask = removeTask(String.valueOf(game.song_id));
		if (null != loadGameTask) {
			// stop task
			loadGameTask.cancelTask();
			notifyDLTaskUIMsgToHandler(DownloadState.STATE_DOWNLOAD_CANCEL,
					game.song_id, 0, loadGameTask.isBackgroundTask());
		}
	}

	/**
	 * pause Download ，停止下载，必须把执行线程置成null
	 * 
	 * @param game
	 */
	public void pauseDownload(SongDetailInfo game, boolean isUser) {
		if (null == game || null == String.valueOf(game.song_id)) {
			return;
		}
		DownloadTask loadGameTask = loadGametasks.get(String
				.valueOf(game.song_id));
		// pause task
		pauseDownloadTask(loadGameTask, isUser);
	}

	/**
	 * 
	 * @param loadGameTask
	 * @param isUser
	 */
	private void pauseDownloadTask(DownloadTask loadGameTask, boolean isUser) {
		if (null == loadGameTask) {
			return;
		}
		loadGameTask.pauseTask(isUser);
		if (loadGameTask.isBackgroundTask()) {
		} else {
			loadGameDAO.updateGameFlag(loadGameTask);
		}
	}

	/**
	 * 
	 * @param loadGameTask
	 * @param isUser
	 */
	private void pauseBatchDownloadTask(
			Iterator<Entry<String, DownloadTask>> ite, boolean isUser) {
		loadGameDAO.updateBatchGameFlag(ite, true);

	}

	/**
	 * 继续所有下载
	 */
	public void continueAllDownload() {
		if (null == loadGametasks || 0 == loadGametasks.size()) {
			return;
		}

		int checkResult = checkNetAndSpace();
		if (DownloadState.ERROR_NONE == checkResult) {

		} else {
			// 通知错误
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
				JLog.i(TAG, "task.gameDownloadState=" + task.gameDownloadState);
				if (DownloadState.STATE_DOWNLOAD_SUCESS != task.gameDownloadState) {
					startDownloadTask(task);

				}
			}
		}
	}

	/**
	 * 启动下载
	 * 
	 * @param task
	 */
	private void startDownloadTask(DownloadTask task) {
		if (null == task) {
			return;
		}
		if (!task.isBackgroundTask()) {
			task.startTask(loadListener);
		} else if (task.isBackgroundTask()) {
			task.startTask(loadListener);
		}
		if (task.isBackgroundTask()) {
		} else {
			loadGameDAO.updateGameFlag(task);
		}
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
			Entry<String, DownloadTask> entity = null;
			DownloadTask task = null;
			while (ite.hasNext()) {
				entity = ite.next();
				task = entity.getValue();
				pauseDownloadTask(task, true);
				// pauseDownloadTask(task, task.isUserPause());
			}
		}
	}

	/**
	 * 停止所有下载
	 */
	public void pauseBatchDownload() {
		if (null == loadGametasks || 0 == loadGametasks.size()) {
			return;
		}
		synchronized (loadGametasks) {
			Iterator<Entry<String, DownloadTask>> ite = loadGametasks
					.entrySet().iterator();

			pauseBatchDownloadTask(ite, true);
		}
	}

	/**
	 * 停止所有下载
	 */
	public void pauseAllBackgroudDownload() {
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

	public int getDownloadProgress(String song_id) {
		DownloadTask task = loadGametasks.get(song_id);
		if (null == task) {
			return 0;
		}
		return task.progress;
	}

	public void setUIDownloadListener(UIDownLoadListener refreshHanle) {
		if (null == uiListners) {
			uiListners = new HashSet<UIDownLoadListener>();
		}
		uiListners.add(refreshHanle);
	}

	public void removeUIDownloadListener(UIDownLoadListener refreshHanle) {
		if (null == uiListners) {
			return;
		}
		uiListners.remove(refreshHanle);
	}

	public SongDetailInfo getDownloadGameBysong_id(String song_id) {
		DownloadTask task = loadGametasks.get(song_id);
		if (null == task) {
			return null;
		}
		// task.setStartInstall(false);
		return task.loadGame;
	}

	// public SongDetailInfo getDownloadGameBysong_id(String song_id) {
	// DownloadTask task = loadGametasks.get(song_id);
	// if (null == task || !task.getIsStartInstall()) {
	// return null;
	// }
	// task.setStartInstall(false);
	// return task.loadGame;
	// }

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
			// appDAO.deleteData(packageName);
			loadGameDAO.deleteData(packageName);
			return task;
		}
	}

	public DownloadTask getDownloadTask(String song_id) {
		return loadGametasks.get(song_id);
	}

	public boolean DownloadTaskCotain(String song_id) {
		return (loadGametasks.get(song_id) != null);
	}

	private boolean checkDownloadSucess(SongDetailInfo info) {
		if (null == info) {
			return false;
		}
		// 下载成功，rename tmp download file
		try {
			// 文件重命名
			if (renameTempMusic(info)) {
				// 保存到已下载的数据库
				String downloadFile = FileUtils
						.getDownMusicFilePath(DownloadHelper.getFileName(info));
				LoadBitmapFromNetAndWriteTags(info, downloadFile);
				BaseApplication.curContext.sendBroadcast(new Intent(
						Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri
								.parse("file://" + downloadFile)));
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

	/**
	 * 
	 * 写入歌曲信息
	 * 
	 * @param info
	 * @param path
	 * @return void
	 * @see
	 */
	private void LoadBitmapFromNetAndWriteTags(SongDetailInfo info, String path) {
		Bitmap bitmap = null;
		HttpURLConnection conn = null;
		try {
			String tranUrl=ImageUtil.transferImgUrl(
					info.album_logo,300);
			JLog.i("LoadBitmapFromNetAndWriteTags", "tranUrl="+tranUrl);
			conn = (HttpURLConnection) new URL(tranUrl).openConnection();
			conn.setConnectTimeout(20 * 1000);
			conn.setReadTimeout(20 * 1000);
//			conn.setDoInput(true);
//			conn.setDoOutput(true);
			if (((HttpURLConnection) conn).getResponseCode() == 200) {
				InputStream inputStream = conn.getInputStream();
				bitmap = BitmapFactory.decodeStream(inputStream);
				inputStream.close();
			}
			if (bitmap != null) {
				ByteArrayOutputStream stream = new ByteArrayOutputStream();
				bitmap.compress(Bitmap.CompressFormat.PNG, 90, stream);
				byte[] byteArray = stream.toByteArray();
				bitmap.recycle();
				LocalSongTag songTag = new LocalSongTag();
				songTag.setAlbumName(info.album_name);
				if (!TextUtils.isEmpty(info.artist_name)) {
					songTag.setArtistName(info.artist_name);
				}
				songTag.setSingers(info.singers);
				songTag.setSongName(info.song_name);
				JLog.i("LoadBitmapFromNetAndWriteTags", "info.album_name=" + info.album_name
						+ "--info.singers=" + info.singers
						+ "---info.song_name=" + info.song_name);
				mXiamiSDK.writeFileTags(path, songTag, byteArray);
			}else{
				LocalSongTag songTag = new LocalSongTag();
				songTag.setAlbumName(info.album_name);
				if (!TextUtils.isEmpty(info.artist_name)) {
					songTag.setArtistName(info.artist_name);
				}
				songTag.setSingers(info.singers);
				songTag.setSongName(info.song_name);
				JLog.i("LoadBitmapFromNetAndWriteTags", "bitmap=null;info.album_name=" + info.album_name
						+ "--info.singers=" + info.singers
						+ "---info.info.album_logo=" + info.album_logo
						+ "---info.song_name=" + info.song_name);
				mXiamiSDK.writeFileTags(path, songTag, null);
			}
		} catch (Exception e) {
			JLog.i("LoadBitmapFromNetAndWriteTags", "Exception=" +e.getMessage());
			e.printStackTrace();
		} finally {
			if (conn != null) {
				conn.disconnect();
			}
		}

	}

	/**
	 * 将临时文件重命名
	 * 
	 * @param gameCode
	 * @return
	 * @throws IOException
	 */
	private boolean renameTempMusic(SongDetailInfo gameCode) throws IOException {
		File oldfile = new File(FileUtils.getDownloadTmpFilePath(DownloadHelper
				.getTempFileName(gameCode)));
		File newfile = new File(FileUtils.getDownMusicFilePath(DownloadHelper
				.getFileName(gameCode)));
		JLog.i(TAG, "oldfile=" + oldfile + "--newfile=" + newfile);
		if (newfile.exists()) {
			newfile.delete();
		}

		if (oldfile.exists()) {
			oldfile.renameTo(newfile);
			return true;
		} else {
			// 文件意外删除了
			return false;
		}
	}

	/**
	 * 方法描述：清空下载完成的任务
	 */
	public void clearAllLoadGametasks(ArrayList<SongDetailInfo> datas) {
		if (loadGametasks != null && loadGametasks.size() > 0) {
			for (SongDetailInfo SongDetailInfo : datas) {
				DownloadTask task = loadGametasks.get(String
						.valueOf(SongDetailInfo.song_id));
				if (task != null) {
					task.pauseTask(true);
					loadGametasks
							.remove(String.valueOf(SongDetailInfo.song_id));
				}
			}
		}
	}

	/**
	 * 
	 * @param pkg
	 * @param vercode
	 * @return void
	 * @see
	 */
	public void updateTaskState(final String pkg, int vercode) {
		// synchronized (loadGametasks) {
		// if (loadGametasks != null) {
		// DownloadTask task = loadGametasks.get(pkg);
		// if (null == task)
		// return;
		// task.setDownloadSize(0);
		// if (task.loadGame != null) {
		// task.loadGame.versionCode = vercode;
		//
		// }
		// }
		// }
	}

	public void removeTaskState(final String pkg) {
		synchronized (loadGametasks) {
			if (loadGametasks != null) {
				loadGametasks.remove(pkg);
			}
		}
	}

	Handler mMediaplayerHandler = new Handler() {
		public void handleMessage(Message msg) {
			if (msg.what == RequestResCode.REQUEST_OK) {
				DownloadTask task = (DownloadTask) msg.obj;
				startDownloadTask(task);
			}
		};
	};

	public int getDownloadSpeed(String song_id) {
		DownloadTask task = loadGametasks.get(song_id);
		if (null == task) {
			return 0;
		}
		return task.downloadSpeed;
	}
}
