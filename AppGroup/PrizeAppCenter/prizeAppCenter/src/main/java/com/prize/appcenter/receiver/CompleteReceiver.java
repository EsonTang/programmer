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

package com.prize.appcenter.receiver;

import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.prize.app.constants.Constants;
import com.prize.app.util.CommonUtils;
import com.prize.app.util.DataStoreUtils;
import com.prize.app.util.MD5Util;
import com.prize.app.util.PackageUtils;
import com.prize.app.util.PreferencesUtils;

import java.io.File;

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

	@Override
	public void onReceive(Context context, Intent intent) {

		long downloadId = PreferencesUtils.getLong(context,
				Constants.KEY_NAME_DOWNLOAD_ID);
		long completeDownloadId = intent.getLongExtra(
				DownloadManager.EXTRA_DOWNLOAD_ID, -1);
		if (completeDownloadId == downloadId) {
			if (new File(Constants.APKFILEPATH).exists()) {
				if (MD5Util.Md5Check(Constants.APKFILEPATH,
						PreferencesUtils.getString(context, Constants.APP_MD5))) {
					DataStoreUtils.saveLocalInfo(
							DataStoreUtils.DOWNLOAD_NEWVERSION_OK,
							DataStoreUtils.DOWNLOAD_NEWVERSION_OK);
					if (isNeedInStall&& !CommonUtils.isScreenLocked(context)) {
						PackageUtils.installNormal(context,
								Constants.APKFILEPATH);
					}
				} else {
					File file = new File(Constants.APKFILEPATH);
					if (file.exists()) {
						file.delete();
					}
					PreferencesUtils.putLong(context,Constants.KEY_NAME_DOWNLOAD_ID, -1);
				}

			}

		}

	}

	public void setmDownFinish(DownFinish mDownFinish) {
		this.mDownFinish = mDownFinish;
	}

	public interface DownFinish {
		void onDownFinish(Intent intent);
	}

}
