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

package com.prize.prizenavigation.utils;

import android.app.DownloadManager;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Environment;
import android.text.TextUtils;
import android.webkit.MimeTypeMap;

import com.google.gson.Gson;
import com.prize.prizenavigation.bean.AppsItemBean;
import com.prize.prizenavigation.bean.ClientInfo;
import com.prize.prizenavigation.utils.safe.XXTEAUtil;

import java.io.File;
import java.util.ArrayList;

/**
 * 类描述：更新数据管理工具
 * 
 * @author prize
 * @version 版本
 */
public class UpdateDataUtils {
	private static UpdateDataUtils instance;
	private static ClientInfo mClientInfo = ClientInfo.getInstance();
	private UpdateDataUtils() {
		super();
	}

	public static UpdateDataUtils getUpdateInstance() {
		if (instance == null) {
			instance = new UpdateDataUtils();
		}
		return instance;
	}


	/**
	 * 方法描述：得到所有已下载过的apk的路劲
	 */
	public ArrayList<String> getDownLoadApkFiles() {
		ArrayList<String> paths = new ArrayList<String>();
		if (Environment.getExternalStorageState().equals(
				Environment.MEDIA_MOUNTED)) {
			String path = Environment.getExternalStorageDirectory()
					.getAbsolutePath() + "/prizeAppCenter/download";
			File file = new File(path);
			if (file.exists() && file.isDirectory()) {
				File[] files = file.listFiles(); // 得到所有已下载的apk文件
				if (files == null || files.length <= 0) {
					return paths;
				}
				for (File childFile : files) {
					if (childFile.getName().endsWith(".apk")) {
						paths.add(childFile.getAbsolutePath());
					}
				}
			} else {
				file.mkdirs();
			}
		}
		return paths;
	}

	/**
	 * 方法描述：得到已下载完的未删除的安装包的信息
	 */
	public ArrayList<AppsItemBean> getDownLoadApkInfo(Context context,
													  ArrayList<String> paths) {
		PackageManager pm = context.getPackageManager();
		ArrayList<AppsItemBean> apkInfo = new ArrayList<AppsItemBean>();
		for (int i = 0; i < paths.size(); i++) {
			PackageInfo packageInfo = pm.getPackageArchiveInfo(paths.get(i),
					PackageManager.GET_ACTIVITIES);
			AppsItemBean appsItemBean = new AppsItemBean();
			if (packageInfo != null) {
				appsItemBean.versionCode = packageInfo.versionCode;
				appsItemBean.packageName = packageInfo.packageName;
				apkInfo.add(appsItemBean);
			}
		}
		return apkInfo;
	}

	/**
	 * 方法描述：删除所有已下载的apk安装包
	 */
	public boolean removeAllDownLoadApk() {
//		ArrayList<String> paths = new ArrayList<String>();
		if (Environment.getExternalStorageState().equals(
				Environment.MEDIA_MOUNTED)) {
			String path = Environment.getExternalStorageDirectory()
					.getAbsolutePath() + "/prizeAppCenter/download";
			File file = new File(path);
			if (file.exists() && file.isDirectory()) {
				File[] files = file.listFiles();
				for (File childFile : files) {
					if (childFile.getName().endsWith(".apk")) { // 得到所有已下载的apk文件
						childFile.delete();
					}
				}
			} else {
				file.mkdirs();
			}
			return true;
		}
		return false;
	}

	/**
	 * 方法描述：应用安装完成后删除安装包
	 */
	public boolean removeInstalledApk(Context context, String packageName) {
		if (packageName == null)
			return false;
		try {
			PackageManager pm = context.getPackageManager();
			ArrayList<String> paths = getDownLoadApkFiles();
			if (paths == null || paths.size() <= 0)
				return false;
			for (int i = 0; i < paths.size(); i++) {
				PackageInfo packageInfo = pm.getPackageArchiveInfo(paths.get(i),
						PackageManager.GET_ACTIVITIES);
				if (packageInfo != null) {
					if (packageName.equals(packageInfo.packageName)) {
						return new File(paths.get(i)).delete();
					}
				}
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return false;
	}

	static long downloadId = 0;

	/**
	 *
	 * 应用自升级app
	 *
	 * @param downloadManager
	 * @param bean
	 * @param context
	 */

	public static void downloadApk(DownloadManager downloadManager,
								   AppsItemBean bean, Context context) {
		LogUtil.i("lk", "downloadApk start");
		File folder = Environment
				.getExternalStoragePublicDirectory(Constants.DOWNLOAD_FOLDER_NAME);
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
		// java.lang.IllegalArgumentException: Unknown URL content://downloads/my_downloads 可能出现错误
		try {
			DownloadManager.Request request = new DownloadManager.Request(
					Uri.parse(bean.downloadUrlCdn));
			request.setDestinationInExternalPublicDir(
					Constants.DOWNLOAD_FOLDER_NAME, Constants.DOWNLOAD_FILE_NAME);
			// 设置文件类型
			MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
			String mimeString = mimeTypeMap.getMimeTypeFromExtension(MimeTypeMap
					.getFileExtensionFromUrl(bean.downloadUrlCdn));
			LogUtil.i("lk", "bean.downloadUrlCdn=="+bean.downloadUrlCdn);
			request.setMimeType(mimeString);
			// 在通知栏中显示
			request.setShowRunningNotification(false);
			request.setVisibleInDownloadsUi(true);

//			mClientInfo.setUserId(CommonUtils.queryUserId());
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
			LogUtil.i("lk", "sss  downloadApk===downloadId=="+downloadId);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			LogUtil.i("lk", e.toString());
		}
	}
}
