package com.mediatek.dialer.sos;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.SystemProperties;
import android.util.Log;


public class PowerButtonReceiver extends BroadcastReceiver {
     private static final String TAG = "PowerButtonReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        boolean mIsSupportSOS = SystemProperties.get("persist.mtk_sos_quick_dial").equals("1");
        Log.d(TAG, "Boot Completed, SOS support:" + mIsSupportSOS);
        if (mIsSupportSOS) {
           intent.setClass(context, PowerButtonReceiverService.class);
           context.startService(intent);
        }
    }
}