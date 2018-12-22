package com.prize.appcenter.receiver;


import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.text.TextUtils;

import com.prize.app.util.DataStoreUtils;


/**
 * 定时闹钟触发
 * <p>
 * longbaoxiu
 * 2016/12/27.17:10
 */

public class RequestAlarmReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (Build.VERSION.SDK_INT >= 23) {
            return;
        }
        if (!TextUtils.isEmpty(DataStoreUtils.readLocalInfo(com.prize.app.constants.Constants.PHONE_BOOOT_TIME))) {
            long time = Long.parseLong(DataStoreUtils.readLocalInfo(com.prize.app.constants.Constants.PHONE_BOOOT_TIME));
            if (System.currentTimeMillis() - time < 30 * 60 * 1000) {
                return;
            }
        }
        NotificationManager nMgr = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (nMgr != null) {
            nMgr.cancelAll();
        }
    }
}
