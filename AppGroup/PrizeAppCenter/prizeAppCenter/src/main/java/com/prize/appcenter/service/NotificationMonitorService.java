package com.prize.appcenter.service;


import android.annotation.SuppressLint;
import android.app.NotificationManager;
import android.content.Context;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.telecom.Log;
import android.text.TextUtils;

import com.prize.app.util.JLog;

/**
 * longbaoxiu
 * 2017/11/9.20:47
 */

@SuppressLint("OverrideAbstract")
public class NotificationMonitorService extends NotificationListenerService {


    @Override
    public void onCreate() {
        super.onCreate();
        Log.i("XSL_Test", "NotificationMonitorService-onCreate ");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i("XSL_Test", "NotificationMonitorService-onDestroy ");
    }

    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        super.onNotificationPosted(sbn);
        if (JLog.isDebug) {
            JLog.i("MainActivity", "sbn.getPackageName()=" + sbn.getPackageName() + "--getPackageName()=" + getPackageName());
        }
        if (TextUtils.isEmpty(sbn.getPackageName()) || !sbn.getPackageName().equals(getPackageName()))
            return;
        StatusBarNotification[] result = getActiveNotifications();
        StatusBarNotification notice;
        if (result.length <= 6) return;
        int targetNum = 0;
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        for (int i = 0; i < result.length; i++) {
            notice = result[i];
            if (notice != null && notice.getPackageName().equals(sbn.getPackageName())) {
                targetNum++;
                if (targetNum > 6&&notificationManager!=null) {
                    notificationManager.cancel(notice.getId());
                    JLog.i("MainActivity", "NotificationMonitorService-StatusBarNotification[" + i + "]=" + notice);
                }
            }
        }
    }

}
