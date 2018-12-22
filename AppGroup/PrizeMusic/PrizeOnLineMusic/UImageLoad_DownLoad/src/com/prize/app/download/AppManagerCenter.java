package com.prize.app.download;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import u.aly.dw;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;

import com.prize.app.BaseApplication;
import com.prize.app.database.dao.DownLoadedDAO;
import com.prize.app.database.dao.GameDAO;
import com.prize.app.util.FileUtils;
import com.prize.app.util.HttpUtils;
import com.prize.app.util.JLog;
import com.prize.app.util.PackageUtils;
import com.prize.onlinemusibean.SongDetailInfo;

/**
 * APP管理：安装，下载，删除等
 */
public class AppManagerCenter {
	// ------------------ APP状态 Start ------------------
	/** * 应用不存在 */
	public static final int APP_STATE_UNEXIST = 0x1000;
	/** * 应用正在被下载 */
	public static final int APP_STATE_DOWNLOADING = APP_STATE_UNEXIST + 1;
	/** * 应用下载被暂停 */
	public static final int APP_STATE_DOWNLOAD_PAUSE = APP_STATE_DOWNLOADING + 1;
	// /** * 应用已完成下载 */
	public static final int STATE_DOWNLOADED = APP_STATE_DOWNLOAD_PAUSE + 1;
	/** * 应用已被安装 */
	// public static final int APP_STATE_INSTALLED = APP_STATE_DOWNLOADED + 1;
	/** * 应用需要更新 */
	// public static final int APP_STATE_UPDATE = APP_STATE_INSTALLED + 1;
	/** * 应用等待下载 */
	public static final int APP_STATE_WAIT = APP_STATE_DOWNLOAD_PAUSE + 1;
	/** 应用正在被安装（仅静默安装时使用） **/
	public static final int APP_STATE_INSTALLING = APP_STATE_WAIT + 1;
	/** 查看礼包） **/
	public static final int APP_LOKUP_GIFT = APP_STATE_INSTALLING + 1;
	/** 已经领取 **/
	public static final int APP_RECEIVED_GIFT = APP_LOKUP_GIFT + 1;
	/** 全部领完 **/
	public static final int APP_NO_ACTIVATION_CODE = APP_RECEIVED_GIFT + 1;
	/** 礼包活动结束 **/
	public static final int APP_ACTIVITIES_OVER = APP_NO_ACTIVATION_CODE + 1;
	// ------------------ APP状态 End ------------------

	private static final Context context = BaseApplication.curContext;
	protected static final String TAG = "AppManagerCenter";

	private static HashSet<String> staticInstallPkg = new HashSet<String>();

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

	/**
	 * 是否存在该游戏
	 * 
	 * @param appPackage
	 * @return
	 */
	public static String getAppVersionName(String appPackage) {
		String versionName = null;
		if (appPackage == null) {
			return versionName;
		}
		try {
			PackageInfo packageInfo = BaseApplication.curContext
					.getPackageManager().getPackageInfo(appPackage,
							PackageManager.GET_META_DATA);

			if (packageInfo != null) {
				versionName = packageInfo.versionName;
			}
		} catch (NameNotFoundException e) {
			return versionName;
		}
		return versionName;
	}

	// /**
	// * 获取本地versionCode
	// *
	// * @param appPackage
	// * @return
	// */
	// public static String getLocalVersionCode() {
	// String versionCode = null;
	// try {
	// PackageInfo packageInfo = BaseApplication.curContext
	// .getPackageManager().getPackageInfo(
	// BaseApplication.curContext.getPackageName(),
	// PackageManager.GET_META_DATA);
	//
	// if (packageInfo != null) {
	// versionCode = String.valueOf(packageInfo.versionCode);
	// }
	// } catch (NameNotFoundException e) {
	// return versionCode;
	// }
	// return versionCode;
	// }

	/**
	 *
	 * @param song_id
	 * @param version
	 *            : 版本号，用来判断是否要更新
	 * @param gameId
	 *            : 用来查询是否存在APK或下载中的临时文件
	 * @return
	 */
	public static int getGameAppState(SongDetailInfo info) {
		JLog.i(TAG, "info-->" + info.song_id + "--info.song_name..>"
				+ info.song_name);
		if (null == info || info.song_id <= 0) {
			return APP_STATE_UNEXIST;
		}
		int appState = APP_STATE_UNEXIST; // 默认不存在

		do {
			// 先判断是否存在 和 是否更新
			if (DownloadHelper.isFileExists(info)) {
				appState = STATE_DOWNLOADED;
				JLog.i(TAG, "iDownloadHelper.isFileExists");
				return appState;
			}
			// 需要判断是否有TASK，原因：山寨游戏是最新版本，但被下载替换中，故要判断是否下载。
			DownloadTask task = DownloadTaskMgr.getInstance().getDownloadTask(
					String.valueOf(info.song_id));
			if (null == task) {
			} else {
				switch (task.gameDownloadState) {
				case DownloadState.STATE_DOWNLOAD_WAIT:
					if (!task.isBackgroundTask()) {
						appState = APP_STATE_WAIT;
					}

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
							+ info.song_name + "--STATE_DOWNLOAD_SUCESS="
							+ appState);
					if (DownloadHelper.isFileExists(info)) {
						appState = STATE_DOWNLOADED; // 默认下载成功
					}
					break;
				default:
					break;
				}
			}
		} while (false);
		return appState;
	}

	private static void refreshUI() {
		DownloadTaskMgr.getInstance().notifyRefreshUI(
				DownloadState.STATE_DOWNLOAD_REFRESH);
	}

	/**
	 * 静默安装，完成后删除apk包
	 * 
	 * @param path
	 *            apk路径
	 * @param pkg
	 *            包名
	 */
	public static void installRoot(final String path, final String pkg) {
		Runnable task = new Runnable() {
			@Override
			public void run() {
				staticInstallPkg.add(pkg);
				refreshUI();
				int installFlag = PackageUtils.install(context, path);
				if (installFlag != PackageUtils.INSTALL_SUCCEEDED) {

					DownloadTaskMgr.getInstance().removeTask(pkg);
					DownLoadedDAO.getInstance().deleteSingle(pkg);
					File file = new File(path);
					if (file != null && file.exists() && file.isFile()) {
						file.delete();
					}
					if (installFlag == PackageUtils.INSTALL_FAILED_MISSING_SHARED_LIBRARY) {
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
											+ installFlag);
						} catch (Exception e) {
							e.printStackTrace();

						}
					}
				}
				staticInstallPkg.remove(pkg);
				refreshUI();
			}
		};
		new Thread(task).start();
	}

	/**
	 * 卸载游戏
	 * 
	 * @param song_id
	 */
	public static void uninstallGameApp(final String song_id) {
		PackageUtils.uninstallNormal(BaseApplication.curContext, song_id);
		// 不希望用户卸载
		// if (isAppExist(song_id)) {
		// Runnable task = new Runnable() {
		// @Override
		// public void run() {
		// PackageUtils.uninstall(BaseApplication.curContext, song_id);
		// refreshUI();
		// }
		// };
		// new Thread(task).start();
		// }
	}

	/**
	 * 判断是否要更新版本，根据versionCode来判断
	 * 
	 * @param song_id
	 * @param versionCode
	 * @return
	 */

	public static boolean appIsNeedUpate(String song_id, int versionCode) {
		try {
			PackageInfo packageInfo = BaseApplication.curContext
					.getPackageManager().getPackageInfo(song_id,
							PackageManager.GET_META_DATA);

			if (packageInfo != null) {
				JLog.i(TAG, "song_id=" + song_id
						+ "--packageInfo.versionCode -->"
						+ packageInfo.versionCode + "--netCode=" + versionCode);
				if (packageInfo.versionCode < versionCode) {

					return true;
				}
			}
		} catch (NameNotFoundException e) {
			return false;
		}
		return false;
	}

	/**
	 * 开始下载任务
	 * 
	 * @param game
	 *            下载应用的信息bean
	 * @return void
	 */
	public static void startDownload(SongDetailInfo game) {
		startDownload(game, false);
	}
	/**
	 * 开始批量下载任务
	 * 
	 * @param game
	 *            下载应用的信息bean
	 * @return void
	 */
	public static void startBatchDownload(ArrayList<SongDetailInfo> lists) {
		startBatchDownload(lists, false);
	}


	// ///////////////////////////////////以下代码为下载API////////////////////////////////////////////////////////
	/**
	 * 开始下载
	 * 
	 * @param game
	 * @param isBackground
	 *            : 是否是后台任务
	 */
	private static void startBatchDownload(ArrayList<SongDetailInfo> lists,
			final boolean isBackground) {
		if (null == lists||lists.size()<=0) {
			return;
		}
		DownloadTaskMgr.getInstance().startBatchDownload(lists, isBackground);
	}
	/**
	 * 开始下载
	 * 
	 * @param game
	 * @param isBackground
	 *            : 是否是后台任务
	 */
	private static void startDownload(SongDetailInfo info,
			final boolean isBackground) {
		if (null == info) {
			return;
		}
		DownloadTaskMgr.getInstance().startDownload(info, isBackground);
	}

	/**
	 * 暂停下载
	 * 
	 * @param context
	 * @param gameCode
	 * @param isUserPressed
	 *            :用户主动停止
	 */

	public static void pauseDownload(SongDetailInfo game, boolean isUserPressed) {
		DownloadTaskMgr.getInstance().pauseDownload(game, isUserPressed);
	}

	public static void pauseAllBackgroudDownload() {
		DownloadTaskMgr.getInstance().pauseAllBackgroudDownload();
	}

	/**
	 * 取消下载，会删除已下载的文件，从数据库中删除下载信息
	 * 
	 * @param context
	 * @param gameCode
	 */
	public static void cancelDownload(SongDetailInfo game) {
		DownloadTaskMgr.getInstance().cancelDownload(game);
	}

	/**
	 * 批量取消下载，从数据库中删除下载信息
	 * 
	 * @param context
	 * @param gameCode
	 */
	public static void cancelBatchDownload(ArrayList<SongDetailInfo> infos) {
		for (SongDetailInfo info : infos) {
			DownloadTaskMgr.getInstance().cancelDownloadNoDB(info);
		}
		GameDAO.getInstance().deleteDataBatch(infos);
	}

	/**
	 * 删除下载的APK包或下载的临时文件
	 * 
	 * @param gameCode
	 */
	public static void deleteDownloadGameApk(SongDetailInfo game) {
		DownloadTaskMgr.getInstance().cancelDownload(game);
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
	 * 暂停所有下载中任务
	 */
	public static void pauseBatchDownload() {
		DownloadTaskMgr.getInstance().pauseBatchDownload();
	}

	/**
	 * 根据Song_id查询下载进度
	 * 
	 * @param Song_id
	 * @return
	 */
	public static int getDownloadProgress(String song_id) {
		return DownloadTaskMgr.getInstance().getDownloadProgress(song_id);
	}

	public static int getDownloadSpeed(String song_id) {
		return DownloadTaskMgr.getInstance().getDownloadSpeed(song_id);
	}

	/**
	 * UI对download状态的监听. 注意：当UI界面销毁，或者的被置于后台的时候，移除监听。避免重复多次的刷新数据和UI
	 * 记得删除,否则会引起内存泄露
	 * 
	 * @param refreshHanle
	 */
	public static void setDownloadRefreshHandle(UIDownLoadListener refreshHanle) {
		DownloadTaskMgr.getInstance().setUIDownloadListener(refreshHanle);
	}

	/**
	 * 删除下载监听句柄
	 * 
	 * @param refreshHanle
	 */
	public static void removeDownloadRefreshHandle(
			UIDownLoadListener refreshHanle) {
		DownloadTaskMgr.getInstance().removeUIDownloadListener(refreshHanle);
	}

	public static boolean hasDownloadingApp() {
		return DownloadTaskMgr.getInstance().hasDownloadingTask();
	}

	// ///////////////////////////////////////下载的接口
	// end/////////////////////////////////////////////////////////////////////
	public static final String OLD_PKG_NAME = "com.socogame.ppc";

	// /**
	// * 判定是否存在旧版的版本
	// *
	// * @return
	// */
	// public static boolean isOldVersionExist() {
	// List<PackageInfo> pkgs = context.getPackageManager()
	// .getInstalledPackages(PackageManager.GET_UNINSTALLED_PACKAGES);
	// int pkgSize = pkgs.size();
	// for (int i = 0; i < pkgSize; i++) {
	// PackageInfo pkgInfo = pkgs.get(i);
	// if (OLD_PKG_NAME.equalsIgnoreCase(pkgInfo.song_id)) {
	// if (isSystemApp(pkgInfo) || isSystemUpdateApp(pkgInfo)) {
	// return false;
	// }
	// }
	// }
	// return false;
	// }

	/**
	 * 是否是系统应用
	 * 
	 * @param pInfo
	 * @return
	 */
	public static boolean isSystemApp(PackageInfo pInfo) {
		return ((pInfo.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 0);
	}

	public static boolean isSystemApp(String song_id) {
		try {
			PackageInfo pInfo = context.getPackageManager().getPackageInfo(
					song_id, PackageManager.GET_UNINSTALLED_PACKAGES);
			return ((pInfo.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 0);
		} catch (Exception e) {
			return false;
		}
	}

	/**
	 * 是否是系统应用更新
	 * 
	 * @param pInfo
	 * @return
	 */
	public static boolean isSystemUpdateApp(PackageInfo pInfo) {
		return ((pInfo.applicationInfo.flags & ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) != 0);
	}

	// public static void downloadStatistics(SongDetailInfo gameInfo,
	// boolean isBackground) {
	// if (gameInfo != null && gameInfo.id + "" != null) {
	//
	// // MobclickAgent.onEvent(BaseApplication.curContext,
	// // Constants.EVT_DOWNLOAD_GAME, Constants.EVT_P_PKG_NAME
	// // + gameInfo.song_id + Constants.EVT_P_LISTCODE
	// // + gameInfo.listcode);
	// }
	// }

	private static final String SCHEME = "package";
	/**
	 * 调用系统InstalledAppDetails界面所需的Extra名称(用于Android 2.1及之前版本)
	 */
	private static final String APP_PKG_NAME_21 = "com.android.settings.ApplicationPkgName";
	/**
	 * 调用系统InstalledAppDetails界面所需的Extra名称(用于Android 2.2)
	 */
	private static final String APP_PKG_NAME_22 = "pkg";
	/**
	 * InstalledAppDetails所在包名
	 */

	private static final String APP_DETAILS_PACKAGE_NAME = "com.android.settings";
	/**
	 * InstalledAppDetails类名
	 */

	private static final String APP_DETAILS_CLASS_NAME = "com.android.settings.InstalledAppDetails";

	/**
	 * 调用系统InstalledAppDetails界面显示已安装应用程序的详细信息。 对于Android 2.3（Api Level
	 * 9）以上，使用SDK提供的接口； 2.3以下，使用非公开的接口（查看InstalledAppDetails源码）。
	 * 
	 * @param context
	 * @param song_id
	 *            应用程序的包名
	 */
	public static void showInstalledAppDetails(Context context, String song_id) {
		if (!AppManagerCenter.isAppExist(song_id)) {
			// 不存在的应用，退出
			return;
		}
		Intent intent = new Intent();
		final int apiLevel = Build.VERSION.SDK_INT;
		if (apiLevel >= 9) { // 2.3（ApiLevel 9）以上，使用SDK提供的接口
			intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
			Uri uri = Uri.fromParts(SCHEME, song_id, null);
			intent.setData(uri);
		} else { // 2.3以下，使用非公开的接口（查看InstalledAppDetails源码）
			// 2.2和2.1中，InstalledAppDetails使用的APP_PKG_NAME不同。
			final String appPkgName = (apiLevel == 8 ? APP_PKG_NAME_22
					: APP_PKG_NAME_21);
			intent.setAction(Intent.ACTION_VIEW);
			intent.setClassName(APP_DETAILS_PACKAGE_NAME,
					APP_DETAILS_CLASS_NAME);
			intent.putExtra(appPkgName, song_id);
		}
		context.startActivity(intent);
	}

}
