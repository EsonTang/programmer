package com.android.dialer;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.app.Service;
import android.telephony.TelephonyManager;
import com.android.dialer.DialerApplication;
import android.util.Log;

public class PrizePhoneStateReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(Intent.ACTION_NEW_OUTGOING_CALL)) {
        } else {
            TelephonyManager tm =
                    (TelephonyManager) context.getSystemService(Service.TELEPHONY_SERVICE);

            switch (tm.getCallState()) {
                case TelephonyManager.CALL_STATE_RINGING:
                    Log.d("PrizePhoneStateReceiver","[CALL_STATE_RINGING]");
                    break;
                case TelephonyManager.CALL_STATE_OFFHOOK:
                    Log.d("PrizePhoneStateReceiver","[CALL_STATE_OFFHOOK]");
                case TelephonyManager.CALL_STATE_IDLE:
                    Log.d("PrizePhoneStateReceiver","[CALL_STATE_IDLE]");
					DialerApplication.stopSpeaking();
                    break;
            }
        }
    }
}
