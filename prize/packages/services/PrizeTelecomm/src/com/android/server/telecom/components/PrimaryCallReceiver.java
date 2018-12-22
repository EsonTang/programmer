package com.android.server.telecom.components;

import com.android.server.telecom.Log;
import com.android.server.telecom.TelecomSystem;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.mediatek.telecom.PerformanceTracker;
//M :  For OP18 Plugin to prevent MO video call when low battery
import com.mediatek.telecom.ext.ExtensionManager;

/**
 * Single point of entry for all outgoing and incoming calls. {@link UserCallIntentProcessor} serves
 * as a trampoline that captures call intents for individual users and forwards it to
 * the {@link PrimaryCallReceiver} which interacts with the rest of Telecom, both of which run only as
 * the primary user.
 */
public class PrimaryCallReceiver extends BroadcastReceiver implements TelecomSystem.Component {

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.startSession("PCR.oR");
        ///M: [Performance Track]
        PerformanceTracker.getInstance().trackMO(
                PerformanceTracker.MO_CALL_TEMP_ID, PerformanceTracker.MO_RECEIVED_BROADCAST);

        synchronized (getTelecomSystem().getLock()) {
            if (!ExtensionManager.getCallMgrExt()
                    .shouldPreventVideoCallIfLowBattery(context, intent)) {
                getTelecomSystem().getCallIntentProcessor().processIntent(intent);
            }
        }
        Log.endSession();
    }

    @Override
    public TelecomSystem getTelecomSystem() {
        return TelecomSystem.getInstance();
    }
}
