package com.android.lpserver;

import android.annotation.TargetApi;
import android.app.Notification;
import android.content.Intent;
import android.os.Build;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.util.Log;

@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
public class QHBNotificationService extends NotificationListenerService {
    private static final String TAG = "QHBNotificationService";
    private static QHBNotificationService service;

    @Override
    public void onCreate() {
        super.onCreate();
        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            onListenerConnected();
        }
    }

//    private Config getConfig() {
//        return Config.getConfig(this);
//    }

    /**
     * When the system receives a new notification starting callback
     * @param sbn
     */
    @Override
    public void onNotificationPosted(final StatusBarNotification sbn) {
        Log.d(TAG, "QHBNotificationService....onNotificationPosted");
        if(BuildConfig.DEBUG) {
            Log.d(TAG, "QHBNotificationService....onNotificationRemoved");
        }
        QiangHongBaoService.handeNotificationPosted(new IStatusBarNotification() {
            @Override
            public String getPackageName() {
                return sbn.getPackageName();
            }

            @Override
            public Notification getNotification() {   //Returns notification object；
                Log.d(TAG, "QHBNotificationService....getNotification");
                return sbn.getNotification();
            }
        });
    }

    /**
     * When the system notification is deleted after the departure callback
     * @param sbn
     */
    @Override
    public void onNotificationRemoved(StatusBarNotification sbn) {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            super.onNotificationRemoved(sbn);
        }
        if(BuildConfig.DEBUG) {
            Log.d(TAG, "QHBNotificationService...onNotificationRemoved");
        }
    }

    @Override
    public void onListenerConnected() {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            super.onListenerConnected();
        }

        Log.d(TAG, "QHBNotificationService...onListenerConnected");
        service = this;
        //Send a broadcast, has been connected to the
        Intent intent = new Intent(Config.ACTION_NOTIFY_LISTENER_SERVICE_CONNECT);
        sendBroadcast(intent);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "QHBNotificationService...onDestroy");
        service = null;
        //Send a broadcast, has been connected to the
        Intent intent = new Intent(Config.ACTION_NOTIFY_LISTENER_SERVICE_DISCONNECT);
        sendBroadcast(intent);
    }

    /** Whether to start the notification bar monitor*/
    public static boolean isRunning() {
        if(service == null) {
            return false;
        }
        return true;
    }
}
