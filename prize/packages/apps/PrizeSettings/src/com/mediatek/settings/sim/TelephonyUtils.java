package com.mediatek.settings.sim;

import android.content.Context;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemProperties;
import android.provider.Settings;
import android.telecom.PhoneAccount;
import android.telecom.PhoneAccountHandle;
import android.telecom.TelecomManager;
import android.telephony.RadioAccessFamily;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;

import com.android.internal.telephony.ITelephony;
import com.android.internal.telephony.PhoneConstants;
import com.mediatek.internal.telephony.ITelephonyEx;
import com.mediatek.settings.FeatureOption;

import java.util.Iterator;

public class TelephonyUtils {
    private static boolean DBG = SystemProperties.get("ro.build.type").equals("eng") ? true : false;
    private static final String TAG = "TelephonyUtils";
    private static final String MULTI_IMS_SUPPORT = "ro.mtk_multiple_ims_support";

    /**
     * Get whether airplane mode is in on.
     * @param context Context.
     * @return True for on.
     */
    public static boolean isAirplaneModeOn(Context context) {
        return Settings.System.getInt(context.getContentResolver(),
                Settings.Global.AIRPLANE_MODE_ON, 0) != 0;
    }

    /**
     * Calling API to get subId is in on.
     * @param subId Subscribers ID.
     * @return {@code true} if radio on
     */
    public static boolean isRadioOn(int subId, Context context) {
        ITelephony phone = ITelephony.Stub.asInterface(ServiceManager
                .getService(Context.TELEPHONY_SERVICE));
        boolean isOn = false;
        try {
            // for ALPS02460942, during SIM switch, radio is unavailable, consider it as OFF
            if (phone != null && !isCapabilitySwitching()) {
                isOn = subId == SubscriptionManager.INVALID_SUBSCRIPTION_ID ? false :
                    phone.isRadioOnForSubscriber(subId, context.getPackageName());
            } else {
                log("capability switching, or phone is null ? " + (phone == null));
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        log("isRadioOn = " + isOn + ", subId: " + subId);
        return isOn;
    }

    /**
     * capability switch.
     * @return true : switching
     */
    public static boolean isCapabilitySwitching() {
        ITelephonyEx telephonyEx = ITelephonyEx.Stub.asInterface(
                ServiceManager.getService(Context.TELEPHONY_SERVICE_EX));
        boolean isSwitching = false;
        try {
            if (telephonyEx != null) {
                isSwitching = telephonyEx.isCapabilitySwitching();
            } else {
                Log.d(TAG, "mTelephonyEx is null, returen false");
            }
        } catch (RemoteException e) {
            Log.e(TAG, "RemoteException = " + e);
        }
        log("isSwitching = " + isSwitching);
        return isSwitching;
    }

    private static void log(String msg){
        if (DBG) {
            Log.d(TAG, msg);
        }
    }
    /**
     * Get the phone id with main capability.
     */
    public static int getMainCapabilityPhoneId() {
        int phoneId = SubscriptionManager.INVALID_PHONE_INDEX;
        ITelephonyEx telephony = ITelephonyEx.Stub.asInterface(
                ServiceManager.getService(Context.TELEPHONY_SERVICE_EX));

        if (telephony != null) {
            try {
                phoneId = telephony.getMainCapabilityPhoneId();
            } catch (RemoteException e) {
                log("getMainCapabilityPhoneId: remote exception");
            }
        } else {
            log("ITelephonyEx service not ready!");
            phoneId = SystemProperties.getInt(PhoneConstants.PROPERTY_CAPABILITY_SWITCH, 1) - 1;
            if (phoneId < 0 || phoneId >= TelephonyManager.getDefault().getPhoneCount()) {
                phoneId = SubscriptionManager.INVALID_PHONE_INDEX;
            }
            log("getMainCapabilityPhoneId: phoneId = " + phoneId);
        }
        return phoneId;
    }

    public static boolean isSupportMims() {
        boolean isSupportMims = (SystemProperties.getInt(MULTI_IMS_SUPPORT, 1) > 1);
        log("is support Mims:" + isSupportMims);
        return isSupportMims;
    }
}