package com.mediatek.telecom.volte;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.provider.Settings;
import android.telecom.DisconnectCause;
import android.telecom.PhoneAccount;
import android.telecom.PhoneAccountHandle;
import android.telecom.TelecomManager;
import android.telephony.PhoneNumberUtils;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.text.TextUtils;

import com.android.server.telecom.Log;
import com.android.server.telecom.R;
import com.android.server.telecom.TelecomSystem;
import com.android.server.telecom.TelephonyUtil;
import com.android.server.telecom.components.ErrorDialogActivity;
import com.mediatek.telecom.TelecomManagerEx;
import com.mediatek.telecom.TelecomUtils;

public class TelecomVolteUtils {

    private static final String LOG_TAG = "TelecomVolteUtils";

    /**
     * In the past, Contacts will carry this extra to indicate the dial request is for Ims only.
     * But it will cause normal number(like 10010) cannot be dialed out without VoLTE registered.
     * Now we change to use uri number(contain "@") with "tel:" scheme to indicate Ims only.
     * So this is not used anymore.
     */
    public static final String EXTRA_VOLTE_IMS_CALL_OLD = "com.mediatek.phone.extra.ims";
    /**
     * This is used to record that the dial request is Ims only request,
     * pass this info from CallIntentProcessor to CallsManager(intent -> Bundle)
     */
    public static final String EXTRA_VOLTE_IMS_CALL = "com.mediatek.telecom.extra.ims";
    public static final String ACTION_IMS_SETTING = "android.settings.WIRELESS_SETTINGS";

    public static final boolean MTK_IMS_SUPPORT = SystemProperties.get("persist.mtk_ims_support")
            .equals("1");
    public static final boolean MTK_VOLTE_SUPPORT = SystemProperties.get(
            "persist.mtk_volte_support").equals("1");
    /**
     * for single ims project:
     * 0: main slot volte disable
     * 1: main slot volte available
     *
     * for multiple ims project:
     * 1: slot1 volte available, slot2 disable
     * 2: slot1 volte disable, slot2 available
     * 3: both slot1/slot2 volte available
     */
    public static final String PROP_MTK_VOLTE_ENABLE = "persist.mtk.volte.enable";

    public static boolean isVolteSupport() {
        return MTK_IMS_SUPPORT && MTK_VOLTE_SUPPORT;
    }

//----------------------------For volte ims call only------------------------------------
    /**
     * For now, we treat uri number(contain "@") with scheme of "tel:" as ims only request,
     * which should be dialed only via VoLTE.
     * In the past, we use extra of EXTRA_VOLTE_IMS_CALL_OLD to indicate that.
     * Contacts & Dialer will also carry this extr now, but we do not use it.
     * @param intent
     * @return
     */
    public static boolean isImsCallOnlyRequest(Intent intent) {
        boolean result = false;
        if(isVolteSupport() && intent != null) {
            Uri handle = intent.getData();
            if (handle != null) {
                String scheme = handle.getScheme();
                String uriString = handle.getSchemeSpecificPart();
                // we treat uri number(contain "@") with scheme of "tel:" as ims only request.
                if (PhoneAccount.SCHEME_TEL.equals(scheme)
                        && PhoneNumberUtils.isUriNumber(uriString)) {
                    log("isImsCallOnlyRequest()...TRUE!");
                    result = true;
                }
            }
        }
        return result;
    }

    /**
     * re-get handle uri from intent.
     * For Ims only(tel:xxx@xx), will be changed to sip:xxx@xx in some judge, then we re-get it.
     * @param intent
     * @param defaultHandle
     * @return
     */
    public static Uri getHandleFromIntent(Intent intent, Uri defaultHandle) {
        Uri handle = defaultHandle;
        if (intent != null) {
            handle = intent.getData();
        }
        if (handle == null) {
            log("getHandleFromIntent()... handle is null, need check!");
        }
        return handle;
    }

    public static boolean isImsEnabled() {
        return SystemProperties.getInt(PROP_MTK_VOLTE_ENABLE, 0) > 0;
    }

    public static void showImsDisableDialog(Context context) {
        final Intent intent = new Intent(context, ErrorDialogActivity.class);
        intent.putExtra(ErrorDialogActivity.SHOW_IMS_DISABLE_DIALOG_EXTRA, true);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivityAsUser(intent, UserHandle.CURRENT);
    }

    public static void showNoImsAccountDialog(Context context) {
        // for now, we use "Call not sent."
        final Intent errorIntent = new Intent(context, ErrorDialogActivity.class);
        int errorMessageId = -1;
        errorMessageId = R.string.outgoing_call_failed;
        if (errorMessageId != -1) {
            errorIntent.putExtra(ErrorDialogActivity.ERROR_MESSAGE_ID_EXTRA, errorMessageId);
        }
        errorIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivityAsUser(errorIntent, UserHandle.CURRENT);
    }

    /**
     * select proper phoneAccount from accounts with VoLTE capability.
     * No account => no account, dial fail.
     * One account => use it to dial.
     * More than one accounts & not include default => select phoneAccount.
     * More than one accounts & include default => use default to dial.
     * @param volteAccounts accounts wth VoLTE capability
     * @param defaultPhoneAccoutHandle
     * @return
     */
    public static PhoneAccountHandle getPhoneAccountForVoLTE(List<PhoneAccountHandle> volteAccounts,
            PhoneAccountHandle defaultPhoneAccoutHandle) {
        PhoneAccountHandle result = defaultPhoneAccoutHandle;
        if (volteAccounts == null || volteAccounts.isEmpty()) {
            result = null;
        } else if (volteAccounts.size() == 1) {
            result = volteAccounts.get(0);
        } else if (result != null && !volteAccounts.contains(result)) {
            result = null;
        }
        Log.d(LOG_TAG, "getPhoneAccountForVoLTE()...account changed: %s => %s",
                defaultPhoneAccoutHandle, result);
        return result;
    }

    //-------------For VoLTE conference dial (one key conference)------------------
    public static boolean isConferenceDialRequest(Intent intent) {
        return (intent != null) ? isConferenceDialRequest(intent.getExtras()) : false;
    }

    public static boolean isConferenceDialRequest(Bundle bundle) {
        return (bundle != null)
                ? bundle.getBoolean(TelecomManagerEx.EXTRA_VOLTE_CONF_CALL_DIAL, false)
                : false;
    }

    public static ArrayList<String> getConferenceDialNumbers(Intent intent) {
        return (intent != null)
                ? getConferenceDialNumbers(intent.getExtras())
                : new ArrayList<String>();
    }

    public static ArrayList<String> getConferenceDialNumbers(Bundle bundle) {
        ArrayList<String> result = new ArrayList<String>();
        if (bundle != null && bundle.containsKey(TelecomManagerEx.EXTRA_VOLTE_CONF_CALL_NUMBERS)) {
            result.addAll(bundle
                    .getStringArrayList(TelecomManagerEx.EXTRA_VOLTE_CONF_CALL_NUMBERS));
        }
        log("getConferenceDialNumbers()...size of numbers: " + result.size());
        return result;
    }

    public static final String FAKE_NUMBER = "10010";
    /**
     * For Conference Dial, handle is meaningless;
     * But later process will check it, Ecc or MMI will trigger other logic.
     * So here we change the handle to be a valid fake handle if it is Ecc or MMI.
     * @param context
     * @param handle
     * @return
     */
    public static Uri checkHandleForConferenceDial(Context context, Uri handle) {
        Uri result = handle;
        if (TelecomSystem.getInstance().getCallsManager().isPotentialMMIOrInCallMMI(handle)
                || TelephonyUtil.shouldProcessAsEmergency(context, handle)) {
            log("checkHandleForConferenceDial()...change to fake handle from: " + handle);
            result = Uri.fromParts(PhoneAccount.SCHEME_TEL, FAKE_NUMBER, null);
        }
        return result;
    }

    public static boolean containsEccNumber(Context context, List<String> numbers) {
        boolean result = false;
        if (context != null && numbers != null && !numbers.isEmpty()) {
            for (String number : numbers) {
                result = PhoneNumberUtils.isPotentialLocalEmergencyNumber(context, number);
                if (result) {
                    break;
                }
            }
        }
        return result;
    }

    public static boolean isConferenceInvite(Bundle extras) {
        boolean result = false;
        if (extras != null && extras.containsKey(TelecomManagerEx.EXTRA_VOLTE_CONF_CALL_INCOMING)) {
            result = extras.getBoolean(TelecomManagerEx.EXTRA_VOLTE_CONF_CALL_INCOMING, false);
        }
        log("isConferenceInvite()...result : " + result);
        return result;
    }

    //-------------For VoLTE normal call switch to ECC------------------
    /**
     * This function used to judge that whether the call has been marked as Ecc by NW or not.
     * @param bundle
     * @param defaultValue
     * @return
     */
    public static boolean isEmergencyCallChanged(Bundle bundle, boolean defaultValue) {
        boolean isChanged = false;
        if (isVolteSupport() && bundle != null
                && bundle.containsKey(TelecomManagerEx.EXTRA_VOLTE_MARKED_AS_EMERGENCY)) {
            boolean isEcc = bundle.getBoolean(TelecomManagerEx.EXTRA_VOLTE_MARKED_AS_EMERGENCY);
            if (isEcc != defaultValue) {
                log("isEmergencyCallChanged: " + defaultValue + " => " + isEcc);
                isChanged = true;
            }
        }
        return isChanged;
    }

    //--------------[VoLTE_SS] notify user when volte mmi request while data off-------------
    /**
     * Check whether the disconnect call is a mmi dial request with data off case.
     * @param disconnectCause use this info to check
     */
    public static boolean isMmiWithDataOff(DisconnectCause disconnectCause) {
        boolean result = false;
        if (disconnectCause != null) {
            int disconnectCode = disconnectCause.getCode();
            String disconnectReason = disconnectCause.getReason();
            if (disconnectCode == DisconnectCause.ERROR && !TextUtils.isEmpty(disconnectReason)
                    && disconnectReason.contains(
                    TelecomManagerEx.DISCONNECT_REASON_VOLTE_SS_DATA_OFF)) {
                result = true;
            }
        }
        return result;
    }

    /**
     * Notify user to open data connection.
     * @param context
     * @param phoneAccountHandle
     */
    public static void showNoDataDialog(Context context, PhoneAccountHandle phoneAccountHandle) {
        int subId = TelecomUtils.getSubId(context, phoneAccountHandle);

        if (subId != SubscriptionManager.INVALID_SUBSCRIPTION_ID) {
            String errorMessage = context.getString(
                    R.string.volte_ss_not_available_tips, getSubDisplayName(context, subId));
            final Intent errorIntent = new Intent(context, ErrorDialogActivity.class);
            errorIntent.putExtra(ErrorDialogActivity.ERROR_MESSAGE_STRING_EXTRA, errorMessage);
            errorIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivityAsUser(errorIntent, UserHandle.CURRENT);
        }
    }

    /**
     * Get the sub's display name.
     * @param subId the sub id
     * @return the sub's display name, may return null
     */
    private static String getSubDisplayName(Context context, int subId) {
        String displayName = "";
        SubscriptionInfo subInfo = SubscriptionManager.from(context).getActiveSubscriptionInfo(subId);
        if (subInfo != null) {
            displayName = subInfo.getDisplayName().toString();
        }
        if (TextUtils.isEmpty(displayName)) {
            log("getSubDisplayName()... subId / subInfo: " + subId + " / " + subInfo);
        }
        return displayName;
    }

    public static void dumpVolteExtra(Bundle extra) {
        if (extra == null) {
            log("dumpVolteExtra()... no extra to dump !");
            return;
        }
        log("----------dumpVolteExtra begin-----------");
        if (extra.containsKey(TelecomManagerEx.EXTRA_VOLTE_MARKED_AS_EMERGENCY)) {
            log(TelecomManagerEx.EXTRA_VOLTE_MARKED_AS_EMERGENCY + " = "
                    + extra.getBoolean(TelecomManagerEx.EXTRA_VOLTE_MARKED_AS_EMERGENCY));
        }
        if (extra.containsKey(TelecomManagerEx.EXTRA_VOLTE_PAU_FIELD)) {
            log(TelecomManagerEx.EXTRA_VOLTE_PAU_FIELD + " = "
                    + extra.getString(TelecomManagerEx.EXTRA_VOLTE_PAU_FIELD));
        }
        log("----------dumpVolteExtra end-----------");
    }

    private static void log(String msg) {
        Log.d(LOG_TAG, ">>>>>" + msg);
    }
}
