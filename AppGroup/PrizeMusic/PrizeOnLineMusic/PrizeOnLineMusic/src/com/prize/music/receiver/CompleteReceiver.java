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

package com.prize.music.receiver;

import java.io.File;
import java.io.IOException;

import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.provider.MediaStore;

import com.prize.app.beans.ClientInfo;
import com.prize.app.constants.Constants;
import com.prize.app.net.datasource.base.AppsItemBean;
import com.prize.app.util.FileUtils;
import com.prize.app.util.JLog;
import com.prize.app.util.MD5Util;
import com.prize.app.util.PackageUtils;
import com.prize.music.R;
import com.prize.music.activities.MainActivity;
import com.prize.music.helpers.utils.PreferencesUtils;
import com.prize.music.helpers.utils.UpdateDataUtils;
import com.prize.music.ui.dialog.UpdateSelfDialog;

/**
 **
 * 调用系统下载逻辑 完成后收到的广播
 * 
 * @author longbaoxiu
 * @version V1.0
 */
public class CompleteReceiver extends BroadcastReceiver {
	DownFinish mDownFinish;
	public static boolean isNeedInStall = false;
	DownloadManager downloadManager;

	@Override
	public void onReceive(Context context, Intent intent) {
		long downloadId = PreferencesUtils.getLong(context,
				Constants.KEY_NAME_DOWNLOAD_ID);
		long completeDownloadId = intent.getLongExtra(
				DownloadManager.EXTRA_DOWNLOAD_ID, -1);
		JLog.i("CompleteReceiver", "completeDownloadId="+completeDownloadId+"---downloadId="+downloadId);
		if (completeDownloadId == downloadId) {
			if (new File(Constants.APKFILEPATH).exists()) {// if
																// (FileUtils.renameTempMusic())
																// {
				if (MD5Util.Md5Check(Constants.APKFILEPATH,
						PreferencesUtils.getString(context, Constants.APP_MD5))) {
					JLog.i("CompleteReceiver", "Md5Check");
					try {
//						if (FileUtils.renameTempMusic()) {
//							File oldfile = new File(Constants.APKFILEPATH);
//							context.getContentResolver().delete(
//									MediaStore.Files.getContentUri("external"), "_DATA=?",
//									new String[] { oldfile.getAbsolutePath() });
							if (isNeedInStall) {
								PackageUtils.installNormal(context,
										Constants.APKFILEPATH);
							}


//						}
					} catch (Exception e) {
						e.printStackTrace();

					}
				} else {
					if (downloadManager == null) {
						downloadManager = (DownloadManager) context
								.getSystemService(Context.DOWNLOAD_SERVICE);
					}
					File file = new File(Constants.APKFILETEMPPATH);
					if (file.exists()) {
						UpdateDataUtils.deletePrizeMusicApk(context, file,
								downloadManager);
					}
					// downloadManager.remove(PreferencesUtils.getLong(context,
					// Constants.KEY_NAME_DOWNLOAD_ID));
					// PreferencesUtils.putLong(context,
					// Constants.KEY_NAME_DOWNLOAD_ID, -1);
				}

			}

		}
		// if (new File(Constants.APKFILEPATH).exists()) {
		// if (MD5Util.Md5Check(Constants.APKFILEPATH,
		// PreferencesUtils.getString(context, Constants.APP_MD5))) {
		// DataStoreUtils.saveLocalInfo(
		// DataStoreUtils.DOWNLOAD_NEWVERSION_OK,
		// DataStoreUtils.DOWNLOAD_NEWVERSION_OK);
		// if (isNeedInStall) {
		// PackageUtils.installNormal(context,
		// Constants.APKFILEPATH);
		// }
		// } else {
		// if (downloadManager == null) {
		// downloadManager = (DownloadManager) context
		// .getSystemService(Context.DOWNLOAD_SERVICE);
		// }
		// File file = new File(Constants.APKFILEPATH);
		// if (file.exists()) {
		// UpdateDataUtils.deletePrizeMusicApk(context, file,
		// downloadManager);
		// }
		// // downloadManager.remove(PreferencesUtils.getLong(context,
		// // Constants.KEY_NAME_DOWNLOAD_ID));
		// // PreferencesUtils.putLong(context,
		// // Constants.KEY_NAME_DOWNLOAD_ID, -1);
		// }
		//
		// }
		//
		// }

	}
	

	public void setmDownFinish(DownFinish mDownFinish) {
		this.mDownFinish = mDownFinish;
	}

	public interface DownFinish {
		void onDownFinish(Intent intent);
	}

}
