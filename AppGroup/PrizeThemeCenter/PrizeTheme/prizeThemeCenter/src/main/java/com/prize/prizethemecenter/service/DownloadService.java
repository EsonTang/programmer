package com.prize.prizethemecenter.service;

import android.app.ActivityManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;

import com.prize.prizethemecenter.manage.DownloadTaskMgr;

import java.util.List;

/**
 *
 */
public class DownloadService extends Service {

	private static DownloadTaskMgr DOWNLOAD_MANAGER;
	private static final String ACTION = "com.prize.prizethemecenter.download.service";

	public static DownloadTaskMgr getDownloadManager(Context appContext) {
		if (!DownloadService.isServiceRunning(appContext)) {
			Intent downloadSvr = new Intent(appContext, DownloadService.class);
			downloadSvr.setAction(DownloadService.ACTION);
			appContext.startService(downloadSvr);
		}
		if (DownloadService.DOWNLOAD_MANAGER == null) {
			DownloadService.DOWNLOAD_MANAGER = DownloadTaskMgr.getInstance();
		}
		return DOWNLOAD_MANAGER;
	}

	public DownloadService() {
		super();
	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public void onCreate() {
		super.onCreate();
	}

	@Override
	public void onStart(Intent intent, int startId) {
		super.onStart(intent, startId);
	}

	@Override
	public void onDestroy() {
		if (DOWNLOAD_MANAGER != null) {
//			try {
//				DOWNLOAD_MANAGER.stopAllDownload();
//				DOWNLOAD_MANAGER.backupDownloadInfoList();
//			} catch (DbException e) {
//				LogUtils.e(e.getMessage(), e);
//			}
		}
		super.onDestroy();
	}

	public static boolean isServiceRunning(Context context) {
		boolean isRunning = false;

		ActivityManager activityManager = (ActivityManager) context
				.getSystemService(Context.ACTIVITY_SERVICE);
		List<ActivityManager.RunningServiceInfo> serviceList = activityManager
				.getRunningServices(Integer.MAX_VALUE);

		if (serviceList == null || serviceList.size() == 0) {
			return false;
		}

		for (int i = 0; i < serviceList.size(); i++) {
			if (serviceList.get(i).service.getClassName().equals(
					DownloadService.class.getName())) {
				isRunning = true;
				break;
			}
		}
		return isRunning;
	}
}
