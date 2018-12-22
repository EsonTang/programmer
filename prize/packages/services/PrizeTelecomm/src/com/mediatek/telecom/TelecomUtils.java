
package com.mediatek.telecom;

import android.app.AppOpsManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.CountryDetector;
import android.net.Uri;
import android.net.sip.SipManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.provider.Settings;
import android.telecom.PhoneAccount;
import android.telecom.PhoneAccountHandle;
import android.telecom.TelecomManager;
import android.telephony.PhoneNumberUtils;
import android.telephony.ServiceState;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.text.TextUtils;

import android.widget.Toast;
import com.android.i18n.phonenumbers.NumberParseException;
import com.android.i18n.phonenumbers.PhoneNumberUtil;
import com.android.i18n.phonenumbers.Phonenumber.PhoneNumber;
import com.android.internal.telephony.ITelephony;
import com.android.internal.telephony.Phone;
import com.android.internal.telephony.PhoneConstants;
import com.android.internal.telephony.PhoneFactory;
import com.android.server.telecom.Call;
import com.android.server.telecom.Constants;
import com.android.server.telecom.Log;
import com.android.server.telecom.PhoneAccountRegistrar;
import com.android.server.telecom.TelecomSystem;
import com.android.server.telecom.TelephonyUtil;
import com.android.server.telecom.components.ErrorDialogActivity;
import com.google.android.collect.Sets;
import com.mediatek.common.dm.DmAgent;
import com.mediatek.cta.CtaUtils;
import com.mediatek.internal.telephony.ITelephonyEx;

import com.mediatek.telephony.TelephonyManagerEx;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public class TelecomUtils {

    private static final String TAG = TelecomUtils.class.getSimpleName();
    // add to enable specify a slot to MO.
    // using cmd:adb shell am start -a android.intent.action.CALL
    // -d tel:10010 --ei com.android.phone.extra.slot 1
    public static final String EXTRA_SLOT = "com.android.phone.extra.slot";

    // Add temp feature option for ip dial.
    public static final boolean MTK_IP_PREFIX_SUPPORT = true;

    private static final Object sLockObject = new Object();

    // M: Add for upgrade debug.
    private static boolean sUpgradeLoggingEnabled = false;

    /*
     * M: get initial number from intent.
     */
    public static String getInitialNumber(Context context, Intent intent) {
        Log.d(TAG, "getInitialNumber(): " + intent);

        String action = intent.getAction();
        if (TextUtils.isEmpty(action)) {
            return "";
        }

        // If the EXTRA_ACTUAL_NUMBER_TO_DIAL extra is present, get the phone
        // number from there.  (That extra takes precedence over the actual data
        // included in the intent.)
        if (intent.hasExtra(Constants.EXTRA_ACTUAL_NUMBER_TO_DIAL)) {
            String actualNumberToDial =
                intent.getStringExtra(Constants.EXTRA_ACTUAL_NUMBER_TO_DIAL);
            return actualNumberToDial;
        }

        return PhoneNumberUtils.getNumberFromIntent(intent, context);
    }

    public static boolean isDMLocked() {
        boolean locked = false;
        try {
            IBinder binder = ServiceManager.getService("DmAgent");
            DmAgent agent = null;
            if (binder != null) {
                agent = DmAgent.Stub.asInterface(binder);
            }
            if (agent != null) {
                locked = agent.isLockFlagSet();
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }

        return locked;
    }


    /// M: For Ip dial @{
    /**
     * This function used to check whether the dial request is a Ip dial request.
     * If airplane mode is on, do not check ip prefix.
     * @param context
     * @param call
     * @param extras
     * @return
     */
    public static boolean isIpDialRequest(Context context, Call call, Bundle extras) {
        return  (TelecomUtils.MTK_IP_PREFIX_SUPPORT
                && extras.getBoolean(Constants.EXTRA_IS_IP_DIAL, false)
                && call.getHandle() != null
                && PhoneAccount.SCHEME_TEL.equals(call.getHandle().getScheme())
                && !TelephonyUtil.shouldProcessAsEmergency(context, call.getHandle())
                && !isAirPlaneModeOn(context));
    }

    /**
     * This function used to get certain phoneAccount for Ip dial.
     * simAccounts.size() == 0, => no sim account, set null.
     * simAccounts.size() == 1, => only one sim account, use it.
     * simAccounts.size()  > 1, => if valid default account exist, do nothing; or set null(select)
     * @param simAccounts
     * @param defaultPhoneAccoutHandle
     * @return
     */
    public static PhoneAccountHandle getPhoneAccountForIpDial(List<PhoneAccountHandle> simAccounts,
            PhoneAccountHandle defaultPhoneAccoutHandle) {
        PhoneAccountHandle result = defaultPhoneAccoutHandle;
        if (simAccounts == null || simAccounts.isEmpty()) {
            result = null;
        } else if (simAccounts.size() == 1) {
            result = simAccounts.get(0);
        } else if (result != null && !simAccounts.contains(result)) {
            result = null;
        }
        Log.d(TAG, "getPhoneAccountForIpDial()...account changed: %s => %s",
                defaultPhoneAccoutHandle, result);
        return result;
    }

    /**
     * to check if the airplane mode is on or off.
     * @param ctx
     * @return boolean  true is on
     */
    public static boolean isAirPlaneModeOn(Context ctx) {
        int airplaneMode = Settings.Global.getInt(ctx.getContentResolver(),
                Settings.Global.AIRPLANE_MODE_ON, 0);
        if (airplaneMode == 0) {
            return false;
        } else {
            return true;
        }
    }

    /**
     * This function used to set ip prefix for certain call.
     * @param context
     * @param call
     * @return true for success, false for fail.
     */
    public static boolean handleIpPrefixForCall(Context context, Call call) {
        boolean result = true;
        if (call.isIpCall() && call.getTargetPhoneAccountEx() != null) {
            String ipPrefix = getIpPrefix(context, call.getTargetPhoneAccountEx());
            Log.d(TAG, "handleIpPrefixForCall()...ipPrefix = %s", ipPrefix);
            // If radio is off, do not go to Call Setting;
            // just pass it to Telephony, which will return error message. see ALPS02400819;
            if (TextUtils.isEmpty(ipPrefix)
                    && TelecomUtils.isRadioOn(call.getTargetPhoneAccountEx(), context)) {
                Log.d(TAG, "handleIpPrefixForCall()...go to ip prefix setting");
                TelecomUtils.gotoIpPrefixSetting(context, call.getTargetPhoneAccountEx());
                result = false;
            } else {
                Uri newHandle = TelecomUtils.rebuildHandleWithIpPrefix(context,
                        call.getHandle(), ipPrefix);
                call.setHandle(newHandle, TelecomManager.PRESENTATION_ALLOWED);
                Log.d(TAG, "handleIpPrefixForCall()...handle changed: %s => %s", call.getHandle(),
                        newHandle);
                result = true;
            }
        }
        Log.d(TAG, "handleIpPrefixForCall()...result = %s", result);
        return result;
    }

    /**
     * This function used to get ip prefix based on certain phoneAccountHandle.
     * @param context
     * @param account
     * @return
     */
    public static String getIpPrefix(Context context, PhoneAccountHandle account) {
        String ipPrefix = "";
        if (context != null && account != null) {
            int subId = getSubIdByAccount(context, account);
            if (SubscriptionManager.isValidSubscriptionId(subId)) {
                ipPrefix = Settings.System.getString(context.getContentResolver(),
                        "ipprefix" + subId);
            }
        }
        return ipPrefix;
    }

    /**
     * This function used to guide user to setting UI to set ip prefix.
     * @param context
     * @param account
     */
    public static void gotoIpPrefixSetting(Context context, PhoneAccountHandle account) {
        if (context != null && account != null) {
            int subId = getSubIdByAccount(context, account);
            final Intent intent = new Intent(Intent.ACTION_MAIN);
            intent.setClassName(Constants.PHONE_PACKAGE, Constants.IP_PREFIX_SETTING_CLASS_NAME);
            intent.putExtra(PhoneConstants.SUBSCRIPTION_KEY, subId);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            context.startActivity(intent);
        }
    }

    /**
     * This function used to get sub id based on certain phoneAccountHandle.
     * @param context
     * @param accountHandle
     * @return
     * @deprecated
     */
    public static int getSubIdByAccount(Context context, PhoneAccountHandle accountHandle) {
        return getSubId(context, accountHandle);
    }

    /**
     * check the radio is on or off by phone account.
     *
     * @param PhoneAccountHandle the selected phone account
     * @return true if radio on
     */
    public static boolean isRadioOn(PhoneAccountHandle account, Context context) {
        int subId = getSubIdByAccount(context, account);
        Log.d(TAG, "[isRadioOn]subId:" + subId);
        boolean isRadioOn = true;
        final ITelephony iTel = ITelephony.Stub.asInterface(
                ServiceManager.getService(Context.TELEPHONY_SERVICE));
        if (iTel != null) {
            try {
                isRadioOn = iTel.isRadioOnForSubscriber(subId, context.getPackageName());
            } catch (RemoteException e) {
                Log.d(TAG, "[isRadioOn] failed to get radio state for sub " + subId);
                isRadioOn = false;
            }
        } else {
            Log.d(TAG, "[isRadioOn]failed to check radio");
        }
        Log.d(TAG, "[isRadioOn]isRadioOn:" + isRadioOn);
        return isRadioOn;
    }

    /**
     * rebuild handle with ip prefix; if ip prefix is null, return default handle.
     * @param context
     * @param defaultHandle
     * @param ipPrefix
     * @return
     */
    public static Uri rebuildHandleWithIpPrefix(Context context, Uri defaultHandle,
            String ipPrefix) {
        Uri resultHandle = defaultHandle;
        if (context != null && !TextUtils.isEmpty(ipPrefix) && defaultHandle != null) {
            String uriString = defaultHandle.getSchemeSpecificPart();
            if (uriString.indexOf(ipPrefix) < 0) {
                uriString = ipPrefix + filtCountryCode(context, uriString);
            }
            resultHandle = Uri.fromParts(defaultHandle.getScheme(), uriString, null);
        }
        return resultHandle;
    }

    /**
     * remove the country code from the number in international format.
     *
     * @param number
     * @return
     */
    private static String filtCountryCode(Context context, String number) {
        String countryIso = null;
        if (!TextUtils.isEmpty(number) && number.contains("+")) {
            try {
                CountryDetector mDetector = (CountryDetector) context
                        .getSystemService(Context.COUNTRY_DETECTOR);
                PhoneNumberUtil numUtil = PhoneNumberUtil.getInstance();
                if (mDetector != null && mDetector.detectCountry() != null) {
                    countryIso = mDetector.detectCountry().getCountryIso();
                } else {
                    countryIso = context.getResources().getConfiguration().locale
                            .getCountry();
                }
                PhoneNumber num = numUtil.parse(number, countryIso);
                return num == null ? number : String.valueOf(num
                        .getNationalNumber());
            } catch (NumberParseException e) {
                e.printStackTrace();
                Log.d(TAG, "parse phone number ... " + e);
            }
        }
        return number;
    }
    /// @}

    /**
     * Update default account handle when there has a valid suggested account
     * handle which not same with default.
     * @param extras The extra got from Intent.
     * @param accounts The all available accounts for current call.
     * @param defaultAccountHandle The default account handle.
     * @return newAccountHandle
     */
    public static boolean shouldShowAccountSuggestion(Bundle extras,
            List<PhoneAccountHandle> accounts, PhoneAccountHandle defaultAccountHandle) {
        boolean shouldShowAccountSuggestion = false;
        PhoneAccountHandle suggestedAccountHandle = getSuggestedPhoneAccountHandle(extras);

        if (accounts != null && defaultAccountHandle != null && suggestedAccountHandle != null
                && accounts.contains(suggestedAccountHandle)
                && !suggestedAccountHandle.equals(defaultAccountHandle)) {
            shouldShowAccountSuggestion = true;
        }
        Log.d(TAG, "shouldShowAccountSuggestion: " + shouldShowAccountSuggestion);
        return shouldShowAccountSuggestion;
    }

    /**
     * Added for suggesting phone account feature.
     * @param extras The extra got from Intent.
     * @param accounts The available PhoneAccounts.
     * @return The available suggested PhoneAccountHandle.
     */
    public static PhoneAccountHandle getSuggestedPhoneAccountHandle(Bundle extras) {
        PhoneAccountHandle suggestedAccountHandle = null;
        if (extras != null) {
            suggestedAccountHandle = extras
                    .getParcelable(TelecomManagerEx.EXTRA_SUGGESTED_PHONE_ACCOUNT_HANDLE);
        }
        Log.d(TAG, "Suggested PhoneAccountHandle is " + suggestedAccountHandle);
        return suggestedAccountHandle;
    }

    /**
     * original defined in CallsManager, we add it here for prevent MMI call when current is guest user
     * however, we still keep the original implementation in CallsManager.
     * @param handle
     * @return
     */
    public static boolean isPotentialMMICode(Uri handle) {
        return (handle != null && handle.getSchemeSpecificPart() != null
                && handle.getSchemeSpecificPart().contains("#"));
    }

    /**
     * Check if the account has registered to network.
     * @param context The context for get service.
     * @param account The account for check.
     * @return A boolean indicates the check result.
     */
    static boolean isAccountInService(Context context, PhoneAccount account) {
        boolean result = false;
        ITelephonyEx iTelephonyEx = ITelephonyEx.Stub.asInterface(ServiceManager
                .getService("phoneEx"));
        TelephonyManager tem = (TelephonyManager) context
                .getSystemService(Context.TELEPHONY_SERVICE);
        if (iTelephonyEx == null) {
            Log.d(TAG, "iTelephonyEx is Null.");
            return result;
        }

        int subId = -1;
        try {
            subId = tem.getSubIdForPhoneAccount(account);
        } catch (NumberFormatException e) {
            Log.d(TAG, "account sub id error.");
            return result;
        }

        ServiceState ss = null;
        Log.d(TAG, "isAccountInService subId = " + subId);
        try {
            ss = ServiceState.newFromBundle(iTelephonyEx.getServiceState(subId));
            Log.d(TAG, "isAccountInService = " + ss);
            if (ss.getVoiceRegState() == ServiceState.STATE_IN_SERVICE) {
                result = true;
            }
        } catch (RemoteException e) {
            e.printStackTrace();
            result = false;
        }
        Log.d(TAG, "isAccountInService account = " + account + " result = " + result);
        return result;
    }

    /**
     * Check if a account support MMI code.
     *
     * @return A boolean indicates the check result.
     */
    public static boolean isSupportMMICode(PhoneAccountHandle phoneAccountHandle) {
        return !isCdmaPhoneAccount(phoneAccountHandle);
    }

    /**
     * M: CC: For 3G VT only
     * @return
     */
    public static boolean isSupport3GVT() {
        return SystemProperties.get("ro.mtk_vt3g324m_support").equals("1");
    }

    /**
     * This function used to get PhoneAccountHandle(s) which support VoLTE.
     * @return
     */
    public static List<PhoneAccountHandle> getVoltePhoneAccountHandles() {
        List<PhoneAccountHandle> phoneAccountHandles = new ArrayList<PhoneAccountHandle>();
        PhoneAccountRegistrar phoneAccountRegistrar = TelecomSystem.getInstance().
                getPhoneAccountRegistrar();
        if (phoneAccountRegistrar != null) {
            phoneAccountHandles.addAll(phoneAccountRegistrar.getVolteCallCapablePhoneAccounts());
        }
        return phoneAccountHandles;
    }

    /**
     * This function used to get PhoneAccountHandle(s), which is sim based.
     * @return
     */
    public static List<PhoneAccountHandle> getSimPhoneAccountHandles() {
        List<PhoneAccountHandle> simPhoneAccountHandles = new ArrayList<PhoneAccountHandle>();
        PhoneAccountRegistrar phoneAccountRegistrar = TelecomSystem.getInstance().
                getPhoneAccountRegistrar();
        if (phoneAccountRegistrar != null) {
            simPhoneAccountHandles.addAll(
                    phoneAccountRegistrar.getSimPhoneAccountsOfCurrentUser());
        }
        return simPhoneAccountHandles;
    }

    /**
     * This function used to get PhoneAccountHandle by slot id.
     * @param context
     * @param slotId
     * @return
     */
    public static PhoneAccountHandle getPhoneAccountHandleWithSlotId(Context context,
            int slotId, PhoneAccountHandle defaultPhoneAccountHandle) {
        PhoneAccountHandle result = defaultPhoneAccountHandle;
        if (SubscriptionManager.isValidSlotId(slotId)) {
            SubscriptionInfo subInfo = SubscriptionManager.from(context)
                    .getActiveSubscriptionInfoForSimSlotIndex(slotId);
            List<PhoneAccountHandle> phoneAccountHandles = getSimPhoneAccountHandles();
            if (subInfo != null && phoneAccountHandles != null && !phoneAccountHandles.isEmpty()) {
                for (PhoneAccountHandle accountHandle : phoneAccountHandles) {
                    if (Objects.equals(accountHandle.getId(), subInfo.getIccId())) {
                        result = accountHandle;
                        break;
                    }
                }
            }
        }
        Log.d(TAG, "getPhoneAccountHandleWithSlotId()... slotId = %s; account changed: %s => %s",
                slotId, defaultPhoneAccountHandle, result);
        return result;
    }

    /**
     * This function used to start ErrorDialogActivity to show error message.
     * @param context
     * @param msgId
     */
    public static void showErrorDialog(Context context, int msgId) {
        if (context == null) {
            return;
        }
        final Intent errorIntent = new Intent(context, ErrorDialogActivity.class);
        errorIntent.putExtra(ErrorDialogActivity.ERROR_MESSAGE_ID_EXTRA, msgId);
        errorIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivityAsUser(errorIntent, UserHandle.CURRENT);
    }

    /**
     * To make the phoneaccount selection UI
     * show the accounts in ascend sequence,
     * we sort the PhoneAccount by slotId ascend.
     * since slotId is start from 0, so we only need to
     * put the PhoneAccount object to an ArrayList with
     * the index as its slotId.
     */
    public static void sortPhoneAccountsBySortKeyAscend(List<PhoneAccount> phoneAccounts){
        if (phoneAccounts == null || phoneAccounts.size() <= 1) {
            return ;
        }

        List<PhoneAccount> sortedList = new ArrayList<PhoneAccount>();
        sortedList.addAll(phoneAccounts);

        Collections.sort(sortedList, new java.util.Comparator<PhoneAccount>(){
            @Override
            public int compare(PhoneAccount a, PhoneAccount b) {
                /// M: fix CR:ALPS02837867,selectAccount show order error about sim2,sim1.
                /// add phoneId as new OrderKey about PhoneAccount.@{
                int aSortKey = (a.getExtras() == null ? Integer.MAX_VALUE : a.getExtras().getInt(
                        PhoneAccount.EXTRA_PHONE_ACCOUNT_SORT_KEY, Integer.MAX_VALUE));
                int bSortKey = (b.getExtras() == null ? Integer.MAX_VALUE : b.getExtras().getInt(
                        PhoneAccount.EXTRA_PHONE_ACCOUNT_SORT_KEY, Integer.MAX_VALUE));
                return aSortKey - bSortKey;
                /// @}
            }
        });

        if (sortedList.size() > 0) {
            phoneAccounts.clear();
            phoneAccounts.addAll(sortedList);
        }
    }

    private static final Set<String> SINGLETON_VILTE_OPERATORS =
            Sets.newArraySet("46000", "46002", "46004", "46007", "46008");

    public static boolean supportsSingletonVilteCall(
            PhoneAccountHandle handle) {
        PhoneAccount phoneAccount = TelecomSystem.getInstance().getPhoneAccountRegistrar()
                .getPhoneAccountUnchecked(handle);
        return SINGLETON_VILTE_OPERATORS.contains(getOperatorFromPhoneAccount(phoneAccount));
    }

    private static String getOperatorFromPhoneAccount(PhoneAccount phoneAccount) {
        /// TODO: 2 binder calls required for this action. We might carry the subId
        /// in the PhoneAccount to reduce binder calls.
        TelephonyManager tm = TelephonyManager.from(TelecomSystem.getInstance().getContext());
        if (tm != null) {
            int subId = tm.getSubIdForPhoneAccount(phoneAccount);
            if (subId != SubscriptionManager.INVALID_SUBSCRIPTION_ID) {
                return tm.getSimOperator(subId);
            }
        }
        return "";
    }
    /// @}

    /**
     * Get subId with phoneAccountHandle
     * @param context
     * @param phoneAccountHandle
     * @return subId
     */
    public static int getSubId(Context context, PhoneAccountHandle phoneAccountHandle) {
        int subId = SubscriptionManager.INVALID_SUBSCRIPTION_ID;
        if (context == null || phoneAccountHandle == null) {
            return subId;
        }
        TelephonyManager tem = (TelephonyManager) context
                .getSystemService(Context.TELEPHONY_SERVICE);
        if (tem != null) {
            try {
                PhoneAccount account = TelecomSystem.getInstance().getPhoneAccountRegistrar()
                        .getPhoneAccountUnchecked(phoneAccountHandle);
                if (account != null
                        && account.hasCapabilities(PhoneAccount.CAPABILITY_SIM_SUBSCRIPTION)) {
                    subId = tem.getSubIdForPhoneAccount(account);
                } else if (account == null) {
                    /// M: [ALPS02753678]In SELDOM case, like ALPS02753678 demonstrated, The
                    /// PhoneAccount has not been registered when it was reported to Telecom.
                    /// In such scenario, we have to double confirming that whether can we
                    /// retrieve the subId in some other way instead, like via SubscriptionManager.
                    Log.w(TAG, "[getPhoneId]PhoneAccount not registered," +
                            " try SubscriptionManager, the iccId = " + phoneAccountHandle.getId());
                    /// M: FIXME: It's NOT right to presume the PhoneAccount id == iccId.
                    SubscriptionInfo subscriptionInfo = SubscriptionManager.from(context)
                            .getSubscriptionInfoForIccId(phoneAccountHandle.getId());
                    if (subscriptionInfo != null) {
                        subId = subscriptionInfo.getSubscriptionId();
                        Log.d(TAG, "[getPhoneId]get subId from SubscriptionManager: " + subId);
                    }
                    /// @}
                } else {
                    /// M: do nothing for account has no CAPABILITY_SIM_SUBSCRIPTION case.
                }
            } catch(Exception e) {
                Log.d(TAG, "getSubIdForPhoneAccount error: " + e.toString());
                e.printStackTrace();
            }
        }
        return subId;
    }

    /**
     * @deprecated
     * Should use TelecomUtils.isInDsdaMode() instead.
     * @return
     */
    public static boolean isSvlte() {
        return (SystemProperties.get("ro.boot.opt_c2k_lte_mode").equals("1"));
    }

    /**
     * M: [CTA] checking conference permission.
     * Don't crash because other ongoing calls would be effected.
     *
     * @param permission the permission to be checked.
     * @param msg the debug message display in log.
     * @return if the caller has the permission
     */
    public static boolean checkCallingCtaPermission(String permission, String msg) {
        try {
            // The boolean return value is useless.
            CtaUtils.enforceCheckPermission(permission, msg);
        } catch (SecurityException e) {
            Log.e(TAG, e, "[checkCallerPermission]CTA Permission checking failed, " +
                    "Permission: "+ permission);
            return false;
        }
        return true;
    }

    /**
     * M: [CTA] checking conference permission.
     * In InCallAdapter, the CtaUtils can't retrieve the package name by itself.
     * Because, the Binder.getCallingPid() will return 0 there. And it's not possible to
     * the the package name without right pid. So we have to pass package name explicitly.
     *
     * @param permission the permission to be checked.
     * @param callingPackageName the caller's package name.
     * @param msg the debug message display in log.
     */
    public static boolean checkCallingCtaPermission(String permission,
                                                    String callingPackageName, String msg) {
        try {
            // The boolean return value is useless.
            CtaUtils.enforceCheckPermission(callingPackageName, permission, msg);
        } catch (SecurityException e) {
            Log.e(TAG, e, "[checkCallerPermission]CTA Permission checking failed, " +
                    "Permission: "+ permission);
            return false;
        }
        return true;
    }

    /**
     * M: Check caller's permission. Only for both CTA and AOSP permission checking.
     * Can't use CTA permission checking here because it would still valid even in
     * Non-CTA projects.
     *
     * @param context The application context
     * @param permission The permission to check
     * @param callingPackageName The caller's package name
     * @param msg The message print in the log if failed.
     * @return has permission or not.
     */
    public static boolean checkCallingPermission(Context context, String permission,
                                                 String callingPackageName, String msg) {
        try {
            /// This checking is only valid for SDK >= 23 callers. For earlier ones, it would pass
            /// directly.
            context.enforcePermission(permission,
                    0/*pid is not required for checking caller permission*/,
                    Binder.getCallingUid(), msg);
        } catch (SecurityException e) {
            Log.e(TAG, e, "[checkCallerPermission]Permission checking failed for" +
                    " SDK level >= 23 caller. Permission: "+ permission);
            return false;
        }

        /// Reach here means a SDK >= 23 caller passed the checking OR the caller just a SDK < 23
        /// one. So we need to double check with the AppOpManager.
        AppOpsManager appOpsManager = (AppOpsManager) context.getSystemService(
                Context.APP_OPS_SERVICE);
        if (appOpsManager == null) {
            Log.w(TAG, "[checkCallingPermission]Failed to get AppOpsManager");
            return false;
        }
        String op = AppOpsManager.permissionToOp(permission);
        int opMode = appOpsManager.noteOp(op, Binder.getCallingUid(), callingPackageName);
        Log.d(TAG, "[checkCallingPermission]permission: " + permission + " -> op: " + op
                + ", checking mode = " + opMode);
        return opMode == AppOpsManager.MODE_ALLOWED;
    }

    /// M: If there is anything need to be done in the main thread, try to extend this cluster.
    /// Aware!!! Do carefully error handling here to avoid system crash. @{
    private static Toast sToast;
    private static final int SHOW_TOAST = 1;
    private static WeakReference<Handler> sRefHandler = new WeakReference<Handler>(null);
    private static Handler getMainThreadHandler() {
        synchronized (sRefHandler) {
            if (sRefHandler == null || sRefHandler.get() == null) {
                sRefHandler = new WeakReference<Handler>(new Handler(Looper.getMainLooper()) {
                    @Override
                    public void handleMessage(Message msg) {
                        if (msg.what == SHOW_TOAST) {
                            Context context = TelecomSystem.getInstance().getContext();
                            if (context == null) {
                                Log.w(TAG, "[handleMessage]SHOW_TOAST, context null");
                                return;
                            }
                            String message = (String) msg.obj;
                            /// This method will be running only in main thread.
                            /// So no need synchronized creating this instance.
                            if (sToast == null) {
                                sToast = Toast.makeText(context, message, Toast.LENGTH_LONG);
                            } else {
                                sToast.setText(message);
                            }
                            sToast.show();
                        }
                    }
                });
            }
            return sRefHandler.get();
        }
    }
    /// @}

    /**
     * M: A wrapper for show Toast in Telecom.
     * This helper will avoid the flooding toast issue.
     * For example, Toast.makeText().show() many times.
     * TODO: Change all the Telecom toast here.
     *
     * @param resId the toast text resId.
     */
    public static void showToast(int resId) {
        showToast(TelecomSystem.getInstance().getContext().getString(resId));
    }

    public static void showToast(String message) {
        if (TextUtils.isEmpty(message)) {
            Log.w(TAG, "Empty message for Toast. ", new Throwable());
            return;
        }
        Handler handler = getMainThreadHandler();
        Message msg = handler.obtainMessage(SHOW_TOAST);
        msg.obj = message;
        msg.sendToTarget();
    }

    /**
     * M: ALPS02822905
     * query VoIP is supported or not
     * @param context
     */
    public static boolean isVoipSupported(Context context) {
        return SipManager.isVoipSupported(context) &&
                context.getResources().getBoolean(
                        com.android.internal.R.bool.config_built_in_sip_phone) &&
                context.getResources().getBoolean(
                        com.android.internal.R.bool.config_voice_capable);
    }

    /**
     * M: judge dsda mode
     * TODO: Can we skip some calling to TelephonyManagerEx#isInDsdaMode() ?
     * It would be good if we reduce the binder calls.
     */
    public static boolean isInDsdaMode() {
        return TelephonyManagerEx.getDefault().isInDsdaMode();
    }

    /**
     * M: Get the number from call.
     * This number is different with call.getPhoneNumber() which is retrieved from CallerInfo.
     */
    public static String getNumberFromCall(Call call) {
        if (call != null) {
            Uri handle = call.getHandle();
            if (handle != null) {
                return handle.getSchemeSpecificPart();
            }
        }
        return "";
    }

    /**
     * M: check whether a PhoneAccount is a CDMA account.
     *
     * @param phoneAccountHandle the phoneAccount might not registered.
     * @return true if the account is a CDMA account.
     */
    public static boolean isCdmaPhoneAccount(PhoneAccountHandle phoneAccountHandle) {
        if (phoneAccountHandle == null) {
            return false;
        }
        PhoneAccount phoneAccount = TelecomSystem.getInstance().getPhoneAccountRegistrar()
                .getPhoneAccountUnchecked(phoneAccountHandle);
        if (phoneAccount != null) {
            return phoneAccount.hasCapabilities(PhoneAccount.CAPABILITY_CDMA_CALL_PROVIDER);
        }

        /// In else case, the PhoneAccount haven't been registered.
        /// The phoneAccountHandle id will be the phoneId in such case
        /// The only way to check the CDMA capability is to use the Sub APIs.
        String handleId = phoneAccountHandle.getId();
        if (handleId != null && handleId.length() < 2 && TextUtils.isDigitsOnly(handleId)) {
            /// The PhoneAccount id is the phone Id in some special case.
            return getPhoneTypeByPhoneId(Integer.parseInt(handleId))
                    == PhoneConstants.PHONE_TYPE_CDMA;
        }

        /// The flow shouldn't arrive here. at least for CDMA.
        /// We should assert here, but the issues are horrible.
        return false;
    }

    /**
     * M: Get the PhoneType by phoneId.
     * This method will call the Telephony/Sub FWK to check the phone type.
     * Don't rely on this method because it's a little bit dangerous.
     * Might have unexpected dead lock because of the TelephonyManager and phone thread.
     *
     * @param phoneId the phone id
     * @return the phone type
     */
    private static int getPhoneTypeByPhoneId(int phoneId) {
        int subId = SubscriptionManager.getSubIdUsingPhoneId(phoneId);
        if (subId == SubscriptionManager.INVALID_SUBSCRIPTION_ID) {
            Log.w(TAG, "getPhoneType, Invalid subId got with phoneId:" + phoneId);
            return PhoneConstants.PHONE_TYPE_NONE;
        }

        TelephonyManager tem =
                (TelephonyManager) TelecomSystem.getInstance().getContext()
                        .getSystemService(Context.TELEPHONY_SERVICE);
        int type = tem.getCurrentPhoneType(subId);
        Log.d(TAG, "getPhoneType, phoneId:"
                + phoneId + ", subId:" + subId + ", phone type:" + type);
        return type;
    }

    /**
     * M: A helper to covert Binder to printable string.
     * @param b Bundle to be covert
     * @return printable string of the Bundle content.
     */
    public static String bundleToString(Bundle b) {
        if (b == null) {
            return "null";
        }
        StringBuilder sb = new StringBuilder("Bundle(").append(b.hashCode()).append("){");
        for (String key : b.keySet()) {
            Object value = b.get(key);
            sb.append(key).append(": ")
                    .append(value == null ? "null" : value.toString())
                    .append(", ");
        }
        sb.append("}");
        return sb.toString();
    }

    /**
     * M: MTK would save some configurations in preferences, this method retrieves the
     * SharedPreferences instance.
     * @return the SharedPreference instance or null.
     */
    public static SharedPreferences getExtendedPreferences() {
        if (TelecomSystem.getInstance() == null) {
            return null;
        }
        Context context = TelecomSystem.getInstance().getContext();
        if (context == null) {
            return null;
        }
        return context.getSharedPreferences("pref_ext.xml", Context.MODE_PRIVATE);
    }

    /**
     * M: Add for upgrade debug.
     */
    public static boolean getUpgradeLoggingEnabled() {
        return sUpgradeLoggingEnabled;
    }

    /**
     * M: Add for upgrade debug.
     */
    public static void setUpgradeLoggingEnabled(boolean enabled) {
        sUpgradeLoggingEnabled = enabled;
    }
}