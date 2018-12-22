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

package com.prize.appcenter.ui.util;

import android.app.DownloadManager;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.webkit.MimeTypeMap;

import com.google.gson.Gson;
import com.prize.app.BaseApplication;
import com.prize.app.beans.ClientInfo;
import com.prize.app.constants.Constants;
import com.prize.app.net.datasource.base.AppsItemBean;
import com.prize.app.util.CommonUtils;
import com.prize.app.util.PreferencesUtils;
import com.prize.app.util.safe.XXTEAUtil;

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
//	private static ArrayList<AppsItemBean> updateApps = new ArrayList<AppsItemBean>();
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
	 * 方法描述：删除所有已下载的apk安装包
	 */
	public boolean removeAllDownLoadApk() {
		ArrayList<String> paths = new ArrayList<String>();
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
						context.getContentResolver().delete(
								MediaStore.Files.getContentUri("external"),
								"_DATA=?",
								new String[] { paths.get(i) });
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
	 * @param downloadManager DownloadManager
	 * @param bean AppsItemBean
	 * @param context Context
	 */

	public static void downloadApk(DownloadManager downloadManager,
			AppsItemBean bean, Context context) {
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
					Uri.parse(bean.downloadUrl));
			request.setDestinationInExternalPublicDir(
					Constants.DOWNLOAD_FOLDER_NAME, Constants.DOWNLOAD_FILE_NAME);
			// 设置文件类型
			MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
			String mimeString = mimeTypeMap.getMimeTypeFromExtension(MimeTypeMap
					.getFileExtensionFromUrl(bean.downloadUrl));
			request.setMimeType(mimeString);
			// 是否在通知栏中显示
			request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_HIDDEN);
			request.setVisibleInDownloadsUi(true);
			
			mClientInfo.setUserId(CommonUtils.queryUserId());
			mClientInfo.setClientStartTime(System.currentTimeMillis());
			mClientInfo.setNetStatus(ClientInfo.networkType);
			if(BaseApplication.isNewSign){
				mClientInfo.setApkSign("new");
			}
			//
			String headParams = new Gson().toJson(mClientInfo);

			headParams = XXTEAUtil.getParamsEncypt(headParams);
			if (!TextUtils.isEmpty(headParams)) {
				request.addRequestHeader("params", headParams);
			}
			downloadId = downloadManager.enqueue(request);
			/*save download id to preferences **/
			PreferencesUtils.putLong(context, Constants.KEY_NAME_DOWNLOAD_ID,
					downloadId);
		} catch (Exception e) {
			Log.i("longbaoxiu","UpdateDataUtils-downloadApk:"+e.getMessage());
			e.printStackTrace();
		}
	}
	
}
