package com.prize.prizeappoutad.receiver;

import java.util.List;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.prize.prizeappoutad.constants.Constants;
import com.prize.prizeappoutad.manager.SelfUpdataUtilsManager;
import com.prize.prizeappoutad.service.ThirdAdService;
import com.prize.prizeappoutad.utils.JLog;

/**
 * 程序的入口， 通过接收系统广播开启服务， 利用服务弹出广告页面和检查自升级
 * 
 * @author huangchangguo 2016.10.14
 * 
 */
public class ServiceReceiver extends BroadcastReceiver {

	private static final String TAG = "huang-ServiceReceiver";

	@Override
	public void onReceive(Context context, Intent intent) {
		switch (intent.getAction()) {
		case Intent.ACTION_BOOT_COMPLETED:
		case Constants.ACTION_BOOT_COMPLETED:
		case Constants.INTENT_ACTION_APP_LAUNCH:
			synchronized (this) {
				String serviceName = Constants.AD_SERVICE_NAME;
				String packageName = intent.getStringExtra("packagename");
				// Boolean isRecentRunningApp = SelfUpdataUtils
				// .isRecentRunningApp(context, packageName);
				// JLog.i(TAG, "startService-isRecentApp: "
				// + isRecentRunningApp);
				// if (isRecentRunningApp) {
				// return;
				// }

				// if (intent.getFlags() ==
				// Intent.FLAG_ACTIVITY_LAUNCHED_FROM_HISTORY) {
				// JLog.i(TAG, "startService-isHistory:3");
				// return;
				// }
				// boolean hasAdService = isServiceWork(context, serviceName);
				Log.i(TAG, "startService-packageName:" + packageName);
				// if (hasAdService) {
				// return;
				// }
				Intent i = new Intent(context, ThirdAdService.class);
				if (packageName != null) {
					i.putExtra("packagename", packageName);
				}
				context.startService(i);
			}
			break;
		default:
			break;
		}
	}

	/**
	 * 判断某个服务是否正在运行的方法
	 * 
	 * @param mContext
	 * @param serviceName
	 *            是包名+服务的类名（例如：com.prize.testbackstage.TestService）
	 * @return true代表正在运行，false代表服务没有正在运行
	 */
	public boolean isServiceWork(Context mContext, String serviceName) {
		boolean isWork = false;
		ActivityManager myAM = (ActivityManager) mContext
				.getSystemService(Context.ACTIVITY_SERVICE);
		List<RunningServiceInfo> myList = myAM.getRunningServices(40);
		if (myList.size() <= 0) {
			return false;
		}
		for (int i = 0; i < myList.size(); i++) {
			String mName = myList.get(i).service.getClassName().toString();
			if (mName.equals(serviceName)) {
				isWork = true;
				break;
			}
		}
		return isWork;
	}
}
