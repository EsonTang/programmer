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
package com.prize.appcenter.ui.util;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import com.prize.appcenter.service.PrizeAppCenterService;

/**
 * 黑屏2分钟以及亮屏定时管理类
 *
 * @author yiyi
 * @version 1.0.0
 */
public class PollMgr {
    public static void startPollingService(Context context, Class<?> cls) {
        AlarmManager manager = (AlarmManager) context
                .getSystemService(Context.ALARM_SERVICE);
        if (manager == null) return;
        Intent intent = new Intent(context, cls);
        // intent.setAction(action);
        intent.putExtra(PrizeAppCenterService.OPT_TYPE, 5);
        PendingIntent pendingIntent = PendingIntent.getService(context, 99,
                intent, PendingIntent.FLAG_UPDATE_CURRENT);
        long triggerAtTime = System.currentTimeMillis() + 120 * 1000;
        if (Build.VERSION.SDK_INT >= 23) {
            // In SDK 23 and above, dosing will prevent setExact, setExactAndAllowWhileIdle will force
            // the device to run this task whilst dosing.
            manager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtTime,
                    pendingIntent);
        } else if (Build.VERSION.SDK_INT >= 19) {
            manager.setExact(AlarmManager.RTC_WAKEUP, triggerAtTime,
                    pendingIntent);
        } else {
            manager.set(AlarmManager.RTC_WAKEUP, triggerAtTime,
                    pendingIntent);
        }


    }

    public static void stopPollingService(Context context, Class<?> cls) {
        AlarmManager manager = (AlarmManager) context
                .getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, cls);
        intent.putExtra(PrizeAppCenterService.OPT_TYPE, 5);
        PendingIntent pendingIntent = PendingIntent.getService(context, 99,
                intent, PendingIntent.FLAG_UPDATE_CURRENT);
        manager.cancel(pendingIntent);
    }
}
