package com.koobee.koobeecenter.receiver;

import java.io.File;

import com.koobee.koobeecenter.MainApplication;
import com.koobee.koobeecenter.utils.PollMgr;
import com.prize.app.constants.Constants;
import com.prize.app.util.JLog;
import com.prize.app.util.MD5Util;
import com.prize.app.util.PackageUtils;
import com.prize.app.util.PreferencesUtils;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class KoobeeCenterService extends Service{
    
	public static final String OPT_TYPE = "optType";
	private static final String TAG = "KoobeeCenterService";
	public static String MSG_ACTION = "com.prize.appcenter.service.PrizeAppCenterService";
	public static String className = "com.prize.appcenter.service.PrizeAppCenterService";
	

	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		// TODO Auto-generated method stub
		if (null == intent)
			return START_STICKY;
		JLog.i(TAG, "onStartCommand--"+ScreenListener.isScreenoff);
		int type = intent.getIntExtra(OPT_TYPE, 0);
		switch (type) {
		case 5:
			PollMgr.stopPollingService(MainApplication.curContext,
					KoobeeCenterService.class, "");
			if (ScreenListener.isScreenoff) {
				if (new File(Constants.APKFILEPATH).exists()) {
					JLog.i(TAG, "exists--"+new File(Constants.APKFILEPATH).exists());
					if (MD5Util.Md5Check(Constants.APKFILEPATH,PreferencesUtils.getString(MainApplication.curContext,Constants.APP_MD5))) {
							JLog.i(TAG, "onScreenOff--Md5Checkok");
							Runnable task = new Runnable() {
								@Override
								public void run() {
									JLog.i(TAG, "0000--run");
									int installFlag = PackageUtils.install(MainApplication.curContext,Constants.APKFILEPATH);
									JLog.i(TAG, "installFlag--"+installFlag+"----"+Constants.APKFILEPATH);
									if (installFlag != PackageUtils.INSTALL_SUCCEEDED) {
										PreferencesUtils.putLong(MainApplication.curContext,Constants.KEY_NAME_DOWNLOAD_ID,-1);
									}
								}
							};
							new Thread(task).start();
							return START_STICKY;
					}
				}
			}
			break;
		}
		return START_STICKY;
	}
	 
}
