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

package com.prize.appcenter.ui.util;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningTaskInfo;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.text.TextUtils;

import com.prize.app.util.DataStoreUtils;
import com.prize.app.util.JLog;
import com.prize.appcenter.receiver.RequestAlarmReceiver;
import com.prize.appcenter.service.PrizeAppCenterService;

import java.util.Calendar;
import java.util.List;

/**
 * 轮询工具类
 * *
 *
 * @author longbaoxiu
 * @version V1.0
 */
public class PollingUtils {

    public static final int RETRIVE_SERVICE_COUNT = 50;

    /**
     * 开启轮询服务
     *
     * @param context Context
     * @param seconds 秒为单位
     * @param cls
     * @param action  String
     */
    public static void startPollingService(Context context, int seconds,
                                           Class<?> cls, String action) {
        String requestFrequency = DataStoreUtils.readLocalInfo(DataStoreUtils.PUSHREQUESTFREQUENCY);
        if (!TextUtils.isEmpty(requestFrequency)) {
            seconds = Integer.parseInt(requestFrequency) * 60 * 60;
        }
        // 获取AlarmManager系统服务
        AlarmManager manager = (AlarmManager) context
                .getSystemService(Context.ALARM_SERVICE);

        // 包装需要执行Service的Intent
        Intent intent = new Intent(context, cls);
        intent.setAction(action);
        // @prize { added by fanjunchen
        if (PrizeAppCenterService.MSG_ACTION.equals(action)) {
            intent.putExtra(PrizeAppCenterService.OPT_TYPE, 4);
        }
        // @prize }

        PendingIntent pendingIntent = null;
        try {
            pendingIntent = PendingIntent.getService(context, 0, intent,
                    PendingIntent.FLAG_UPDATE_CURRENT);
        } catch (Exception e) {
            e.printStackTrace();
        }

        long triggerAtTime = System.currentTimeMillis() + seconds * 1000;
        manager.cancel(pendingIntent);

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


//		manager.set(AlarmManager.RTC_WAKEUP, triggerAtTime + seconds * 1000,
//				pendingIntent);

    }

    /**
     * 开启垃圾扫描轮询服务
     *
     * @param context Context
     * @param seconds 秒为单位
     * @param cls     Class<?>
     * @param action  String
     */
    public static void startTrashClearPollingService(Context context, int seconds,
                                                     Class<?> cls, String action) {
        String requestFrequency = DataStoreUtils.readLocalInfo(DataStoreUtils.TRASHCLEARPUSHFREQUENCY);
        JLog.i("long2017", "requestFrequency=" + requestFrequency);
        if (!TextUtils.isEmpty(requestFrequency)) {
            seconds = Integer.parseInt(requestFrequency) * 60;
        }
        // 获取AlarmManager系统服务
        AlarmManager manager = (AlarmManager) context
                .getSystemService(Context.ALARM_SERVICE);

        // 包装需要执行Service的Intent
        Intent intent = new Intent(context, cls);
        intent.setAction(action);
        if (PrizeAppCenterService.TRASH_SCAN_ACTION.equals(action)) {
            intent.putExtra(PrizeAppCenterService.OPT_TYPE, 8);
        }

        PendingIntent pendingIntent = null;
        try {
            pendingIntent = PendingIntent.getService(context, 1, intent,
                    PendingIntent.FLAG_UPDATE_CURRENT);
        } catch (Exception e) {
            e.printStackTrace();
        }

        long triggerAtTime = System.currentTimeMillis() + seconds * 1000;
        manager.cancel(pendingIntent);

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

//        manager.set(AlarmManager.RTC_WAKEUP, triggerAtTime + seconds * 1000,
//                pendingIntent);

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
        return isRunning;
    }


    /**
     * 判断app是否在运行
     *
     * @param context Context
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

//	/**
//	 *
//	 * 安装成功通知
//	 *
//	 * @param context
//	 *            秒为单位
//	 * @param cls
//	 * @param action
//	 * @see
//	 */
//	public static void startServiceNotice(Context context, Class<?> cls,
//			String action) {
//		// 包装需要执行Service的Intent
//		Intent intent = new Intent(context, cls);
//		intent.setAction(action);
//		context.startActivity(intent);
//	}


    private static final int INTERVAL = 1000 * 60 * 60 * 24 * 2;// 48h
    private static final int REQUEST_CODE = 1;

    /**
     * 定时每2天凌晨5点启动一次
     *
     * @param context Context
     */
    public static void setFixedTime(Context context) {
        JLog.i("PollingUtils", "setFixedTime");
        stopFixedTimeAlarm(context);
        Intent intent = new Intent(context, RequestAlarmReceiver.class);
        //此处的action与RequestAlarmReceiver在清单文件中的action一致
        intent.setAction("prize_cancel_all_notification");
        PendingIntent sender = PendingIntent.getBroadcast(context,
                REQUEST_CODE, intent, PendingIntent.FLAG_CANCEL_CURRENT);

        AlarmManager manager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        // Schedule the alarm!
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 5);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 10);
        calendar.set(Calendar.MILLISECOND, 0);
        //该方法用于设置重复闹钟，第一个参数表示闹钟类型，第二个参数表示闹钟首次执行时间，第三个参数表示闹钟两次执行的间隔时间，第三个参数表示闹钟响应动作。
        if(manager!=null){
            manager.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(),
                    INTERVAL, sender);
        }
    }

    private static void stopFixedTimeAlarm(Context context) {
        JLog.i("PollingUtils", "stopFixedTimeAlarm");
        AlarmManager manager = (AlarmManager) context
                .getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, RequestAlarmReceiver.class);
        intent.setAction("prize_cancel_all_notification");
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context,
                REQUEST_CODE, intent, PendingIntent.FLAG_CANCEL_CURRENT);
        manager.cancel(pendingIntent);
    }
}
