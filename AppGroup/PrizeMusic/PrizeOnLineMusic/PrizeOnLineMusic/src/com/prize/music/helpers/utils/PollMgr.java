/*******************************************
 *版权所有©2015,深圳市铂睿智恒科技有限公司
 *
 *内容摘要：
 *当前版本：1.0.0
 *作	者：yiyi
 *完成日期：2015年7月23日
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
package com.prize.music.helpers.utils;

import com.prize.app.util.JLog;
import com.prize.music.service.PrizeMusicService;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;


/**
 * 定时启动更新passport
 * @author liukun
 * @version 1.0.0
 */
public class PollMgr {

	public static void startPollingService(Context context,
										   long intervalMillis, Class<?> cls, String action) {
		AlarmManager manager = (AlarmManager) context
				.getSystemService(Context.ALARM_SERVICE);

		Intent intent = new Intent(context, cls);
		intent.putExtra(PrizeMusicService.OPT_TYPE, 5);
		PendingIntent pendingIntent = PendingIntent.getService(context, 99,
				intent, PendingIntent.FLAG_UPDATE_CURRENT);

		long triggerAtTime = System.currentTimeMillis()+ 60 * 1000;
		
		manager.set(AlarmManager.RTC_WAKEUP, triggerAtTime,
				pendingIntent);
		JLog.i("lk","startPollingService");
	}

	public static void stopPollingService(Context context, Class<?> cls,
										  String action) {
		AlarmManager manager = (AlarmManager) context
				.getSystemService(Context.ALARM_SERVICE);
		Intent intent = new Intent(context, cls);
		intent.putExtra(PrizeMusicService.OPT_TYPE, 5);
		PendingIntent pendingIntent = PendingIntent.getService(context, 99,
				intent, PendingIntent.FLAG_UPDATE_CURRENT);
		manager.cancel(pendingIntent);
		JLog.i("lk","stopPollingService");
	}
}
