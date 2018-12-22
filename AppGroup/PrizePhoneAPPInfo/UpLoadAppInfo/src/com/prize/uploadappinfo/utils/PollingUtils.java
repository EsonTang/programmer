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

package com.prize.uploadappinfo.utils;

import java.util.List;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningTaskInfo;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;

import com.prize.uploadappinfo.service.UploadService;

/**
 * 轮询工具类
 **
 * @author longbaoxiu
 * @version V1.0
 */
public class PollingUtils {

	public static final int RETRIVE_SERVICE_COUNT = 50;

	/**
	 * 
	 * 开启轮询服务
	 * 
	 * @param context
	 * @param seconds
	 *            秒为单位
	 * @param cls
	 * @param action
	 * @see
	 */
	public static void startPollingService(Context context, int seconds) {
		// 获取AlarmManager系统服务
		AlarmManager manager = (AlarmManager) context
				.getSystemService(Context.ALARM_SERVICE);

		// 包装需要执行Service的Intent
		Intent intent = new Intent(context, UploadService.class);
		// @prize { added by fanjunchen
		intent.putExtra(UploadService.OPT_TYPE, 3);
		PendingIntent pendingIntent = null;
		try {
			pendingIntent = PendingIntent.getService(context, 0, intent,
					PendingIntent.FLAG_UPDATE_CURRENT);
		} catch (Exception e) {
		}

		long triggerAtTime = System.currentTimeMillis();
		manager.cancel(pendingIntent);
		manager.set(AlarmManager.RTC_WAKEUP, triggerAtTime + seconds * 1000,
				pendingIntent);

	}

	public static void rebootUploadService(Context context, int seconds) {
		PendingIntent alarmSender = null;
		Intent startIntent = new Intent(context, UploadService.class);
		try {
			alarmSender = PendingIntent.getService(context, 0, startIntent,
					PendingIntent.FLAG_UPDATE_CURRENT);
		} catch (Exception e) {
		}
		AlarmManager am = (AlarmManager) context
				.getSystemService(Activity.ALARM_SERVICE);
		am.setInexactRepeating(AlarmManager.RTC_WAKEUP,
				System.currentTimeMillis(), seconds * 1000, alarmSender);
	}

	public static boolean isServiceRunning(Context context, String className) {
		boolean isRunning = false;
		ActivityManager activityManager = (ActivityManager) context
				.getSystemService(Context.ACTIVITY_SERVICE);
		List<ActivityManager.RunningServiceInfo> serviceInfos = activityManager
				.getRunningServices(RETRIVE_SERVICE_COUNT);

		if (null == serviceInfos || serviceInfos.size() < 1) {
			return false;
		}

		for (int i = 0; i < serviceInfos.size(); i++) {
			if (serviceInfos.get(i).service.getClassName().contains(className)) {
				isRunning = true;
				break;
			}
		}
		JLog.i("PollingUtils", className + " isRunning =  " + isRunning);
		return isRunning;
	}

	public static void cancleAlarmManager(Context context) {
		JLog.i("ServiceUtil-AlarmManager", "cancleAlarmManager to start ");
		Intent intent = new Intent(context, UploadService.class);
		PendingIntent pendingIntent = PendingIntent.getService(context, 0,
				intent, PendingIntent.FLAG_UPDATE_CURRENT);
		AlarmManager alarm = (AlarmManager) context
				.getSystemService(Activity.ALARM_SERVICE);
		alarm.cancel(pendingIntent);
	}

	/**
	 * 
	 * 停止轮询服务
	 * 
	 * @param context
	 * @param cls
	 * @param action
	 * @see
	 */
	public static void stopPollingService(Context context, Class<?> cls,
			String action) {
		AlarmManager manager = (AlarmManager) context
				.getSystemService(Context.ALARM_SERVICE);
		Intent intent = new Intent(context, cls);
		intent.setAction(action);
		PendingIntent pendingIntent = PendingIntent.getService(context, 0,
				intent, PendingIntent.FLAG_UPDATE_CURRENT);
		// 取消正在执行的服务
		manager.cancel(pendingIntent);
	}

	/**
	 * 
	 * 判断app是否在运行
	 * 
	 * @param context
	 * @return boolean
	 */
	public static boolean isAppIsRunning(Context context) {
		ActivityManager am = (ActivityManager) context
				.getSystemService(Context.ACTIVITY_SERVICE);
		List<RunningTaskInfo> list = am.getRunningTasks(100);
		for (RunningTaskInfo info : list) {
			if (info.topActivity.getPackageName().equals(
					context.getApplicationInfo().packageName)
					&& info.baseActivity.getPackageName().equals(
							context.getApplicationInfo().packageName)) {
				return true;
			}
		}
		return false;
	}

	/***
	 * 
	 * @param context
	 * @param cls
	 * @param op_code
	 *            /** 操作类型, int型, 1,表示有网络连接时, 2 下载其他应用, 3 安装类型, 4 消息服务类型
	 */
	public static void startServiceWithtValue(Context context,
			String appName,String pkgName,String type, int op_code) {
		// 包装需要执行Service的Intent
		Intent intent = new Intent(context, UploadService.class);
		intent.putExtra(UploadService.APP_NAME, appName);
		intent.putExtra(UploadService.APP_PACKAGE, pkgName);
		intent.putExtra(UploadService.APP_TYPE, type);
		intent.putExtra(UploadService.OPT_TYPE, op_code);
		context.startService(intent);
	}

	public static void startService(Context context,
		 int op_code) {
		Intent intent = new Intent(context, UploadService.class);
		intent.putExtra(UploadService.OPT_TYPE, op_code);
		context.startService(intent);
	}

}
