package com.mediatek.phone.ext;

import java.util.ArrayList;

import android.content.Context;

import com.android.internal.telephony.Phone;



/**
 * Telephony connection service extension plugin for op09.
*/
public interface ITelephonyConnectionServiceExt {

    /**
     * Check courrent mode is 4G data only mode.
     *
     * @param context from telephony connection service.
     * @param phone is call via by user
     * @return true if in 4G data only mode.
     */
     boolean isDataOnlyMode(Context context, Phone phone);

    /**
     * Customize strings which contains 'SIM', replace 'SIM' by 'UIM' etc.
     * @param stringList string list
     * @param slotId slot id
     * @return new string list
     */
    public ArrayList<String> customizeSimDisplayString(ArrayList<String> stringList, int slotId);
}
