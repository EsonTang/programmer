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
package com.prize.cloud.receiver;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;

/**
 * 定时启动更新passport
 * 
 * @author yiyi
 * @version 1.0.0
 */
public class PollMgr {
	public static final long IntervalMillis = AlarmManager.INTERVAL_HALF_DAY;// seconds

	public static void startPollingService(Context context,
			long intervalMillis, Class<?> cls, String action) {
		AlarmManager manager = (AlarmManager) context
				.getSystemService(Context.ALARM_SERVICE);

		Intent intent = new Intent(context, cls);
		intent.setAction(action);
		PendingIntent pendingIntent = PendingIntent.getService(context, 0,
				intent, PendingIntent.FLAG_UPDATE_CURRENT);

		long triggerAtTime = SystemClock.elapsedRealtime() + 60 * 1000;

		manager.setInexactRepeating(AlarmManager.ELAPSED_REALTIME,
				triggerAtTime, intervalMillis, pendingIntent);
	}

	public static void stopPollingService(Context context, Class<?> cls,
			String action) {
		AlarmManager manager = (AlarmManager) context
				.getSystemService(Context.ALARM_SERVICE);
		Intent intent = new Intent(context, cls);
		intent.setAction(action);
		PendingIntent pendingIntent = PendingIntent.getService(context, 0,
				intent, PendingIntent.FLAG_UPDATE_CURRENT);
		manager.cancel(pendingIntent);
	}
}
