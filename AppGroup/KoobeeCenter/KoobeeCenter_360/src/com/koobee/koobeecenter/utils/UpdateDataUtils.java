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

package com.koobee.koobeecenter.utils;

import java.io.File;

import android.app.DownloadManager;
import android.content.Context;
import android.net.Uri;
import android.os.Environment;
import android.text.TextUtils;
import android.webkit.MimeTypeMap;

import com.google.gson.Gson;
import com.prize.app.BaseApplication;
import com.prize.app.beans.ClientInfo;
import com.prize.app.constants.Constants;
import com.prize.app.net.datasource.base.AppsItemBean;
import com.prize.app.util.CommonUtils;
import com.prize.app.util.JLog;
import com.prize.app.util.PreferencesUtils;
import com.prize.app.util.safe.XXTEAUtil;

public class UpdateDataUtils {
	private static UpdateDataUtils instance;
	private Context mCtx;
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

	static long downloadId = 0;

	/**
	 * 
	 * 应用自升级app
	 * 
	 * @param downloadManager
	 * @param bean
	 * @param downloadId
	 * @param context
	 */

	public static void downloadApk(DownloadManager downloadManager,
			AppsItemBean bean, Context context) {
		JLog.i("hu", "downloadApk start");
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
			JLog.i("hu", "bean.downloadUrl=="+bean.downloadUrl);
			request.setMimeType(mimeString);
			// 在通知栏中显示
			request.setShowRunningNotification(false);
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
			/** save download id to preferences **/
			PreferencesUtils.putLong(context, Constants.KEY_NAME_DOWNLOAD_ID,
					downloadId);
			JLog.i("hu", "sss  downloadApk");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			JLog.i("hu", e.toString());
		}
	}

}
