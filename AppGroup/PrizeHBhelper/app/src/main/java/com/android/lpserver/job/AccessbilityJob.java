package com.android.lpserver.job;

import android.view.accessibility.AccessibilityEvent;

import com.android.lpserver.IStatusBarNotification;
import com.android.lpserver.QiangHongBaoService;

public interface AccessbilityJob {
    String getTargetPackageName();
    void onCreateJob(QiangHongBaoService service);
    void onReceiveJob(AccessibilityEvent event);
    void onStopJob();
    void onNotificationPosted(IStatusBarNotification service);
    boolean isEnable();
}
