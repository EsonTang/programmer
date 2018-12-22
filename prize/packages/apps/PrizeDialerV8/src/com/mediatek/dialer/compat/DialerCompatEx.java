package com.mediatek.dialer.compat;

import android.content.Context;
import android.telecom.TelecomManager;
import android.util.Log;

/**
 * [portable]Dialer new features compatible
 */
public class DialerCompatEx {
    private static final String TAG = DialerCompatEx.class.getSimpleName();

    //[MTK SIM Contacts feature] INDICATE_PHONE_SIM,IS_SDN_CONTACT
    private static final String COMPAT_CLASS_RAWCONTACTS =
            "android.provider.ContactsContract$RawContacts";
    private static final String COMPAT_FIELD_INDICATE_PHONE_SIM= "INDICATE_PHONE_SIM";
    private static Boolean sSimContactsCompat = null;

    public static boolean isSimContactsCompat() {
        if (sSimContactsCompat == null) {
            sSimContactsCompat = DialerCompatExUtils.isFieldAvailable(
                    COMPAT_CLASS_RAWCONTACTS, COMPAT_FIELD_INDICATE_PHONE_SIM);
            Log.d(TAG, "init isSimContactsCompat got " + sSimContactsCompat);
        }
        return sSimContactsCompat;
    }

    /* package */static void setSimContactsCompat(Boolean supported) {
        sSimContactsCompat = supported;
    }

    // [VoLTE ConfCallLog] Whether the VoLTE conference calLog compatible.
    private static final String COMPAT_CLASS_CALLS = "android.provider.CallLog$Calls";
    private static final String COMPAT_FIELD_CONFERENCE_CALL_ID = "CONFERENCE_CALL_ID";
    private static Boolean sConferenceCallLogCompat = null;

    public static boolean isConferenceCallLogCompat() {
        if (sConferenceCallLogCompat == null) {
            sConferenceCallLogCompat = DialerCompatExUtils.isFieldAvailable(
                    COMPAT_CLASS_CALLS, COMPAT_FIELD_CONFERENCE_CALL_ID);
            Log.d(TAG, "init isConferenceCallLogCompat got " + sConferenceCallLogCompat);
        }
        return sConferenceCallLogCompat;
    }

    /* package */static void setConferenceCallLogCompat(Boolean supported) {
        sConferenceCallLogCompat = supported;
    }

    // [VoLTE ConfCall] Whether the VoLTE enhanced conference call (Launch
    // conference call directly from dialer) supported.
    // TODO maybe need to check whether the Contacts and Telecom supported
    private static final String COMPAT_CLASS_PHONEACCOUNT = "android.telecom.PhoneAccount";
    private static final String COMPAT_FIELD_CAPABILITY_VOLTE_CONFERENCE_ENHANCED =
            "CAPABILITY_VOLTE_CONFERENCE_ENHANCED";
    private static Boolean sVolteEnhancedConfCallCompat = null;

    public static boolean isVolteEnhancedConfCallCompat() {
        if (sVolteEnhancedConfCallCompat == null) {
            sVolteEnhancedConfCallCompat = DialerCompatExUtils.isFieldAvailable(
                    COMPAT_CLASS_PHONEACCOUNT, COMPAT_FIELD_CAPABILITY_VOLTE_CONFERENCE_ENHANCED);
            Log.d(TAG, "init isVolteEnhancedConfCallCompat got " + sVolteEnhancedConfCallCompat);
        }
        return sVolteEnhancedConfCallCompat;
    }

    private static final String COMPAT_FIELD_IP_PREFIX = "IP_PREFIX";
    private static Boolean sIpPrefixCompat = null;

    /**
     * [IP Dial] IP call prefix.
     */
    public static boolean isIpPrefixCompat() {
        if (sIpPrefixCompat == null) {
            sIpPrefixCompat = DialerCompatExUtils.isFieldAvailable(
                    COMPAT_CLASS_CALLS, COMPAT_FIELD_IP_PREFIX);
            Log.d(TAG, "init isIpPrefixCompat got " + sIpPrefixCompat);
        }
        return sIpPrefixCompat;
    }

    /* package */static void setIpPrefixCompat(Boolean supported) {
        sIpPrefixCompat = supported;
    }

    /**
     * Blocked Number Permission check for portable. corresponding to
     * BlockedNumberProvider, only default or system dialer can read/write its db.
     */
    public static boolean isDefaultOrSystemDialer(Context context) {
        String self = context.getApplicationInfo().packageName;
        final TelecomManager telecom = context.getSystemService(TelecomManager.class);
        if (self.equals(telecom.getDefaultDialerPackage())
                || self.equals(telecom.getSystemDialerPackage())) {
            return true;
        }
        Log.d(TAG, "isDefaultOrSystemDialer, return false");
        return false;
    }
}
