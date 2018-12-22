package com.mediatek.telecom;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Process;

import com.android.server.telecom.Log;
import com.android.server.telecom.TelecomSystem;

/**
 * M: CR ALPS02783601. For show boot up missed call notification.
 */
public class BootCompletedBroadcastReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        Log.i(this, "Action received: %s.", action);
        if (TelecomSystem.getInstance() == null) {
            Log.w(this, "TelecomSystem is not initialized!");
            return;
        }
        if (Intent.ACTION_BOOT_COMPLETED.equals(action)) {
            TelecomSystem.getInstance().getCallsManager()
                    .reloadMissedCallsOfUser(Process.myUserHandle());
        }
    }

}
