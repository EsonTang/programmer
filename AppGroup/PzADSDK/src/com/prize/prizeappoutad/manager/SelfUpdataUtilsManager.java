/*******************************************
 *版权所有©2015,深圳市铂睿智恒科技有限公司
 *
 *内容摘要：
 *当前版本：
 *作	者：
 *完成日期：
 *修改记录：
 *修改日期：
 *版 本 号：
 *修 改 人：
 *修改内容：
...
 *修改记录：
 *修改日期：
 *版 本 号：
 *修 改 人：
 *修改内容：
 *********************************************/

package com.prize.prizeappoutad.manager;

import java.io.File;
import java.util.List;

import okhttp3.Call;
import android.app.ActivityManager;
import android.app.ActivityManager.RecentTaskInfo;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.app.ActivityManager.RunningTaskInfo;
import android.app.DownloadManager;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Environment;
import android.os.SystemProperties;
import android.text.TextUtils;
import android.webkit.MimeTypeMap;

import com.google.gson.Gson;
import com.prize.prizeappoutad.bean.AppSelfUpdateBean;
import com.prize.prizeappoutad.bean.ClientInfo;
import com.prize.prizeappoutad.constants.Constants;
import com.prize.prizeappoutad.utils.JLog;
import com.prize.prizeappoutad.utils.PreferencesUtils;
import com.prize.prizeappoutad.utils.XXTEAUtil;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.FileCallBack;

/**
 * 
 ** 自升级、得到包名工具类
 * 
 * @author huangchangguo
 * @version V1.0
 */
public class SelfUpdataUtilsManager {
	private static final String TAG = "huang-SelfUpdataUtilsManager";
	static long downloadId = 0;

	/**
	 * 判断是否要更新版本(getPackageArchiveInfo) 根据versionCode来判断
	 * 
	 * @param packageName
	 * @param versionCode
	 * @return
	 */
	public static boolean appIsNeedUpate(String packageName, int versionCode,
			Context context) {

		try {
			ApplicationInfo applicationInfo = context.getPackageManager()
					.getApplicationInfo(packageName, 0);
			if (!isNewMethod()) {
				JLog.i(TAG, "this.versionCode: " + applicationInfo.versionCode
						+ "  versionCode: " + versionCode);
				return applicationInfo.versionCode < versionCode;
			}
			PackageInfo packageInfo = context.getPackageManager()
					.getPackageArchiveInfo(applicationInfo.publicSourceDir, 0);
			if (packageInfo != null) {
				JLog.i(TAG, "this.versionCode: " + packageInfo.versionCode
						+ "  versionCode: " + versionCode);
				return packageInfo.versionCode < versionCode;
			}
		} catch (NameNotFoundException e) {
			return false;
		}
		return false;
	}

	public static boolean isNewMethod() {
		JLog.i("huang-SelfUpdataUtils",
				"isNewMethod-=" + SystemProperties.get("ro.fota.version", "0"));
		JLog.i("huang-SelfUpdataUtils", "isNewMethod-prize_="
				+ SystemProperties.get("ro.prize_app_update_appcenter", "0"));
		// return "1".equals(SystemProperties.get("ro.fota.version", "0"));
		return "1".equals(SystemProperties.get("ro.prize_app_update_appcenter",
				"0"));
	}

	public static boolean isWiFiActive(Context inContext) {
		Context context = inContext.getApplicationContext();
		ConnectivityManager connectivity = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		if (connectivity != null) {
			NetworkInfo[] info = connectivity.getAllNetworkInfo();
			if (info != null) {
				for (int i = 0; i < info.length; i++) {
					if (info[i].getTypeName().equals("WIFI")
							&& info[i].isConnected()) {
						return true;
					}
				}
			}
		}
		return false;
	}

	/**
	 * 
	 * 应用自升级，下载app
	 * 
	 * @param downloadManager
	 * @param bean
	 * @param downloadId
	 * @param context
	 */

	public static void downloadApk(DownloadManager downloadManager,
			AppSelfUpdateBean bean, Context context) {
		// 存储的参数还需要改
		// TODO
		File folder = Environment
				.getExternalStoragePublicDirectory(Constants.APKPATH);
		if (!folder.exists() || !folder.isDirectory()) {
			folder.mkdirs();
		}
		if (downloadManager == null) {
			downloadManager = (DownloadManager) context
					.getSystemService(Context.DOWNLOAD_SERVICE);
		}
		long saveId = PreferencesUtils.getLong(context,
				Constants.KEY_NAME_DOWNLOAD_ID);
		if (downloadId == saveId) {
			return;
		}
		// java.lang.IllegalArgumentException: Unknown URL
		// content://downloads/my_downloads 可能出现错误
		try {
			DownloadManager.Request request = new DownloadManager.Request(
					Uri.parse(bean.downloadUrl));
			request.setDestinationInExternalPublicDir(Constants.APKPATH,
					Constants.DOWNLOAD_FILE_NAME);
			// 设置文件类型
			MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
			String mimeString = mimeTypeMap
					.getMimeTypeFromExtension(MimeTypeMap
							.getFileExtensionFromUrl(bean.downloadUrl));
			request.setMimeType(mimeString);
			// 在通知栏中显示
			request.setShowRunningNotification(false);
			request.setVisibleInDownloadsUi(false);
			ClientInfo mClientInfo = ClientInfo.getInstance(context);
			mClientInfo.setClientStartTime(System.currentTimeMillis());
			mClientInfo.setNetStatus(ClientInfo.networkType);

			String headParams = new Gson().toJson(mClientInfo);

			headParams = XXTEAUtil.getParamsEncypt(headParams);
			if (!TextUtils.isEmpty(headParams)) {
				request.addRequestHeader("params", headParams);
			}
			downloadId = downloadManager.enqueue(request);
			/** save download id to preferences **/
			PreferencesUtils.putLong(context, Constants.KEY_NAME_DOWNLOAD_ID,
					downloadId);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public interface setOnDownloadedLinstener {
		void downloadedLinstener(String path);
	}

	public static void downloadSelf(Context context, AppSelfUpdateBean bean,
			setOnDownloadedLinstener linstener) {

		String downloadurl = bean.downloadUrl;
		JLog.i(TAG, "downloadSelf-bean.downloadUrl:" + downloadurl);
		if (downloadurl == null || ClientInfo.networkType == ClientInfo.NONET) {
			return;
		}

		synchronized (context) {
			// 下载APK

			String apkpath = Constants.APKPATH;
			String downloadFileName = Constants.DOWNLOAD_FILE_NAME;
			downloadApk(downloadurl, apkpath, downloadFileName, linstener);
		}

	}

	public static void downloadApk(String downloadurl, String destFileDir,
			String destFileName, final setOnDownloadedLinstener linstener) {
		if (downloadurl == null && destFileDir == null && destFileName == null) {
			return;
		}
		OkHttpUtils.get().url(downloadurl).build()
				.execute(new FileCallBack(destFileDir, destFileName) {

					@Override
					public void onResponse(File arg0, int arg1) {
						String absolutePath = arg0.getAbsolutePath();
						JLog.i(TAG, "OkHttpUtils-onResponse" + absolutePath);
						linstener.downloadedLinstener(absolutePath);
						// installSilence(absolutePath);
					}

					@Override
					public void onError(Call arg0, Exception arg1, int arg2) {
						JLog.i(TAG, "OkHttpUtils-onError" + arg1.getMessage());
						linstener.downloadedLinstener(null);
					}
				});
	}

	/**
	 * @return The lists of the RunningApps
	 */
	@SuppressWarnings("deprecation")
	public static Boolean isRecentRunningApp(Context context,
			String NativePcgName) {

		// final PackageManager pm = context.getPackageManager();
		ActivityManager am = (ActivityManager) context
				.getSystemService(Context.ACTIVITY_SERVICE);
		// runningProcess = am.getRunningAppProcesses();
		// List<RunningTaskInfo> runningTasks = am.getRunningTasks(50);
		List<RecentTaskInfo> recentTasks = am.getRecentTasks(25,
				ActivityManager.RECENT_IGNORE_UNAVAILABLE);
		JLog.i(TAG, "recentTasks:" + recentTasks.size());
		for (RecentTaskInfo recentTaskInfo : recentTasks) {
			try {
				String name = recentTaskInfo.description.getClass()
						.getPackage().getName();

				String packageName = recentTaskInfo.baseIntent.getComponent()
						.getPackageName();
				JLog.i(TAG, "recentTasks:-origActivity:" + " packageName:"
						+ packageName);
				if (packageName.equals(NativePcgName)) {
					// return true;
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		return false;
	}

	/**
	 * @return The lists of the RunningApps
	 */
	@SuppressWarnings("deprecation")
	public static void isRecentTasks(Context context, String NativePcgName) {

		// final PackageManager pm = context.getPackageManager();
		ActivityManager am = (ActivityManager) context
				.getSystemService(Context.ACTIVITY_SERVICE);
		List<RunningAppProcessInfo> runningAP = am.getRunningAppProcesses();
		List<RunningTaskInfo> runningTasks = am.getRunningTasks(0);
		JLog.i(TAG, "recentTasks:" + runningAP.size());
		for (RunningAppProcessInfo recentTaskInfo : runningAP) {
			try {
				String name = recentTaskInfo.processName;

				JLog.i(TAG, "processName: " + name);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

	}

}
