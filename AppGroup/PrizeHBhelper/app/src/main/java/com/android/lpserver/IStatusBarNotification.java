package com.android.lpserver;

import android.app.Notification;

public interface IStatusBarNotification {

    String getPackageName();
    Notification getNotification();
}
