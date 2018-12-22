package com.prize.prizethemecenter.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import com.prize.app.constants.Constants;
import com.prize.app.util.JLog;
import com.prize.app.util.MD5Util;
import com.prize.app.util.PackageUtils;
import com.prize.app.util.PreferencesUtils;
import com.prize.prizethemecenter.MainApplication;
import com.prize.prizethemecenter.manage.AppManagerCenter;
import com.prize.prizethemecenter.receiver.ScreenListener;
import com.prize.prizethemecenter.ui.utils.PollMgr;

import java.io.File;
/**
 * 主题商店服务类
 * @author pengy
 */
public class PrizeThemeCenterService extends Service{

	public static final String OPT_TYPE = "optType";
	private static final String TAG = "hu";
	public static final String ACTION = "action";
	public static final int ACT_CONTINUE_DOWNLOAD = 1;
	public static final int ACT_PAUSEALL_TASK = ACT_CONTINUE_DOWNLOAD +1;

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		if (null == intent)
			return START_STICKY;
		int type = intent.getIntExtra(OPT_TYPE, 0);
		int action = intent.getIntExtra(ACTION, 0);
		JLog.i("3333", "type==" + type + "--action==" + action);
		switch (type) {
			case 2:
				if(ACT_CONTINUE_DOWNLOAD == action){   /**继续下载全部 action==1*/
					AppManagerCenter.continueAllDownload();
				}
				if(ACT_PAUSEALL_TASK == action){   /**暂停下载全部 action==2*/
					AppManagerCenter.pauseAllDownload();
				}
               break;
			case 5:
				PollMgr.stopPollingService(MainApplication.curContext,
						PrizeThemeCenterService.class, "");
				JLog.i(TAG, "111111" + ScreenListener.isScreenoff);
				if (ScreenListener.isScreenoff) {
					if (new File(Constants.APKFILEPATH).exists()) {
						if (MD5Util.Md5Check(Constants.APKFILEPATH,
								PreferencesUtils.getString(
										MainApplication.curContext,
										Constants.APP_MD5))) {
							JLog.i(TAG, "onScreenOff--Md5Checkok");
							Runnable task = new Runnable() {
								@Override
								public void run() {
									boolean installFlag = PackageUtils.installApkDefaul(
											MainApplication.curContext,
											Constants.APKFILEPATH);
									JLog.i(TAG, "installFlag==" + installFlag);
									if (installFlag != true) {
										PreferencesUtils.putLong(
												MainApplication.curContext,
												Constants.KEY_NAME_DOWNLOAD_ID,
												-1);
									}
								}
							};
							new Thread(task).start();
							return START_STICKY;
						}
					}
				}
				break;
			default:
				break;
		}
		return START_STICKY;
	}
}
