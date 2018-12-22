package com.prize.prizenavigation.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import com.prize.prizenavigation.NavigationApplication;
import com.prize.prizenavigation.receiver.ScreenListener;
import com.prize.prizenavigation.utils.Constants;
import com.prize.prizenavigation.utils.MD5Util;
import com.prize.prizenavigation.utils.PackageUtils;
import com.prize.prizenavigation.utils.PollMgr;
import com.prize.prizenavigation.utils.PreferencesUtils;

import java.io.File;

/**
 * 提示服务类
 * @author liukun
 */
public class PrizeNavigationService extends Service {

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

		switch (type) {
			case 5:
				PollMgr.stopPollingService(NavigationApplication.getContext(),
						PrizeNavigationService.class, "");
				if (ScreenListener.isScreenoff) {
					if (new File(Constants.APKFILEPATH).exists()) {
						if (MD5Util.Md5Check(Constants.APKFILEPATH,
								PreferencesUtils.getString(
										NavigationApplication.getContext(),
										Constants.APP_MD5))) {
							Runnable task = new Runnable() {
								@Override
								public void run() {
									boolean installFlag = PackageUtils.installApkDefaul(
											NavigationApplication.getContext(),
											Constants.APKFILEPATH);
									if (installFlag != true) {
										PreferencesUtils.putLong(
												NavigationApplication.getContext(),
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
