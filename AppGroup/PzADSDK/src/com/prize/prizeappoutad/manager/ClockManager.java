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

package com.prize.prizeappoutad.manager;

import java.util.Calendar;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;

import com.prize.prizeappoutad.constants.Constants;
import com.prize.prizeappoutad.utils.JLog;

/**
 * 
 * 闹钟
 * 
 * @author huangchangguo
 * @version V1.0
 */
public class ClockManager {

	private static final String TAG = "huang-ClockManager";
	// test 2分钟后检查更新
	// private static long TIME = 5 * 60 * 1000;
	private static AlarmManager am;

	// 4个小时后检查更新
	private static long TIME = 4 * 60 * 60 * 1000;

	/**
	 * 开启闹钟
	 */
	public static void startAlam(Context context) {
		try {
			Intent intent = new Intent();
			intent.setAction(Constants.ACTION_PRIZE_ALARM);
			PendingIntent pendingIntent = PendingIntent.getService(context, 5,
					intent, PendingIntent.FLAG_UPDATE_CURRENT);
			if (am == null) {
				am = (AlarmManager) context
						.getSystemService(Context.ALARM_SERVICE);
			}
			am.setRepeating(AlarmManager.RTC_WAKEUP,
					SystemClock.elapsedRealtime(), TIME, pendingIntent);
			JLog.i(TAG, "--------启动周期闹钟:" + SystemClock.elapsedRealtime());
		} catch (Exception e) {
			e.printStackTrace();
			JLog.i(TAG, "--------启动闹钟-------Exception:" + e.toString());
		}
	}

	public static void stopAlam(Context context) {
		JLog.i(TAG, "-------关闭闹钟-------");
		Intent intent = new Intent(Constants.ACTION_PRIZE_ALARM);
		PendingIntent pendingIntent = PendingIntent.getService(context, 5,
				intent, PendingIntent.FLAG_UPDATE_CURRENT);
		if (am == null) {
			am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
		}
		am.cancel(pendingIntent);
	}

}
