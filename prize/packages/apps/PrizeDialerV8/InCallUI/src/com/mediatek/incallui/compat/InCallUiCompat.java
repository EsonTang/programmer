package com.mediatek.incallui.compat;

import android.util.Log;

public class InCallUiCompat {
    private static final String TAG = InCallUiCompat.class.getSimpleName();

    //[STK Notify] STK service need to know the display state of incall ui
    public static String COMPAT_CLASS_TELECOMMANAGEREX =
            "com.mediatek.telecom.TelecomManagerEx";
    public static String COMPAT_FIELD_ACTION_INCALL_SCREEN_STATE_CHANGED =
            "ACTION_INCALL_SCREEN_STATE_CHANGED";
    private static Boolean sStkNotifyCompat = null;

    public static boolean isStkNotifyCompat() {
        if (sStkNotifyCompat == null) {
            sStkNotifyCompat = InCallUICompatUtils.isFieldAvailable(COMPAT_CLASS_TELECOMMANAGEREX,
                    COMPAT_FIELD_ACTION_INCALL_SCREEN_STATE_CHANGED);
            Log.d(TAG, "init isStkNotifyCompat got " + sStkNotifyCompat);
        }
        return sStkNotifyCompat;
    }

}
