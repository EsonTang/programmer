/* Copyright Statement:
 *
 * This software/firmware and related documentation ("MediaTek Software") are
 * protected under relevant copyright laws. The information contained herein
 * is confidential and proprietary to MediaTek Inc. and/or its licensors.
 * Without the prior written permission of MediaTek inc. and/or its licensors,
 * any reproduction, modification, use or disclosure of MediaTek Software,
 * and information contained herein, in whole or in part, shall be strictly prohibited.
 */
/* MediaTek Inc. (C) 2010. All rights reserved.
 *
 * BY OPENING THIS FILE, RECEIVER HEREBY UNEQUIVOCALLY ACKNOWLEDGES AND AGREES
 * THAT THE SOFTWARE/FIRMWARE AND ITS DOCUMENTATIONS ("MEDIATEK SOFTWARE")
 * RECEIVED FROM MEDIATEK AND/OR ITS REPRESENTATIVES ARE PROVIDED TO RECEIVER ON
 * AN "AS-IS" BASIS ONLY. MEDIATEK EXPRESSLY DISCLAIMS ANY AND ALL WARRANTIES,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE IMPLIED WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE OR NONINFRINGEMENT.
 * NEITHER DOES MEDIATEK PROVIDE ANY WARRANTY WHATSOEVER WITH RESPECT TO THE
 * SOFTWARE OF ANY THIRD PARTY WHICH MAY BE USED BY, INCORPORATED IN, OR
 * SUPPLIED WITH THE MEDIATEK SOFTWARE, AND RECEIVER AGREES TO LOOK ONLY TO SUCH
 * THIRD PARTY FOR ANY WARRANTY CLAIM RELATING THERETO. RECEIVER EXPRESSLY ACKNOWLEDGES
 * THAT IT IS RECEIVER'S SOLE RESPONSIBILITY TO OBTAIN FROM ANY THIRD PARTY ALL PROPER LICENSES
 * CONTAINED IN MEDIATEK SOFTWARE. MEDIATEK SHALL ALSO NOT BE RESPONSIBLE FOR ANY MEDIATEK
 * SOFTWARE RELEASES MADE TO RECEIVER'S SPECIFICATION OR TO CONFORM TO A PARTICULAR
 * STANDARD OR OPEN FORUM. RECEIVER'S SOLE AND EXCLUSIVE REMEDY AND MEDIATEK'S ENTIRE AND
 * CUMULATIVE LIABILITY WITH RESPECT TO THE MEDIATEK SOFTWARE RELEASED HEREUNDER WILL BE,
 * AT MEDIATEK'S OPTION, TO REVISE OR REPLACE THE MEDIATEK SOFTWARE AT ISSUE,
 * OR REFUND ANY SOFTWARE LICENSE FEES OR SERVICE CHARGE PAID BY RECEIVER TO
 * MEDIATEK FOR SUCH MEDIATEK SOFTWARE AT ISSUE.
 *
 * The following software/firmware and/or related documentation ("MediaTek Software")
 * have been modified by MediaTek Inc. All revisions are subject to any receiver's
 * applicable license agreements with MediaTek Inc.
 */
package com.mediatek.settings;

import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.AudioManager;
import android.media.AudioSystem;
import android.os.PersistableBundle;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemProperties;
import android.preference.Preference;
import android.preference.PreferenceScreen;
import android.provider.Settings;
import android.telecom.TelecomManager;
import android.telephony.CarrierConfigManager;
import android.telephony.RadioAccessFamily;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.WindowManager;
import android.widget.Toast;

import com.android.ims.ImsException;
import com.android.ims.ImsManager;
import com.android.internal.telephony.ITelephony;
import com.android.internal.telephony.Phone;
import com.android.internal.telephony.PhoneConstants;
import com.android.internal.telephony.PhoneFactory;
import com.android.internal.telephony.TelephonyProperties;

import com.android.phone.GsmUmtsAdditionalCallOptions;
import com.android.phone.GsmUmtsCallForwardOptions;
import com.android.phone.GsmUmtsAdditionalCallOptions;
import com.android.phone.settings.fdn.FdnSetting;
import com.android.phone.SubscriptionInfoHelper;
import com.android.phone.PhoneGlobals;
import com.android.phone.PhoneUtils;
import com.android.phone.R;
import com.android.phone.SubscriptionInfoHelper;

import com.mediatek.internal.telephony.ITelephonyEx;
import com.mediatek.internal.telephony.OperatorUtils;
import com.mediatek.phone.ext.ExtensionManager;
import com.mediatek.settings.cdma.CdmaCallWaitingUtOptions;

//M: Add for data roaming tips
import com.mediatek.settings.cdma.TelephonyUtilsEx;
import com.mediatek.telephony.TelephonyManagerEx;

public class TelephonyUtils {
    private static final String TAG = "TelephonyUtils";
    public static final int MODEM_2G = 0x02;
    public static final String USIM = "USIM";
    private static final String MULTI_IMS_SUPPORT = "ro.mtk_multiple_ims_support";
    ///Add for [Dual_Mic]
    private static final String DUALMIC_MODE = "Enable_Dual_Mic_Setting";
    private static final String GET_DUALMIC_MODE = "Get_Dual_Mic_Setting";
    private static final String DUALMIC_ENABLED = "Get_Dual_Mic_Setting=1";
    /// Add for [ANC]
    private static final int GET_SPEECH_ANC_SUPPORT = 0xB0;
    private static final int SET_SPEECH_ANC_STATUS = 0xB1;
    private static final int GET_SPEECH_ANC_ENABLED = 0xB2;
    private static final int ANC_ENABLE = 1;
    private static final int ANC_DISABLE = 0;

    /// M: add for dual volte feature @{
    private static final int DATA_USAGE_DIALOG = 1001;
    private static final int DATA_ROAMING_DIALOG = 1002;
    private static final int DATA_TRAFFIC_DIALOG = 1003;
    private static final String ADDITIONAL_GSM_SETTINGS_KEY = "additional_gsm_call_settings_key";
    private static final String BUTTON_CB_EXPAND = "button_cb_expand_key";
    private static final String CALL_FORWARDING_KEY = "call_forwarding_key";
    private static final String KEY_CALL_FORWARD = "button_cf_expand_key";
    private static final String KEY_CALLER_ID = "button_caller_id";
    private static final String SUB_ID_EXTRA =
            "com.android.phone.settings.SubscriptionInfoHelper.SubscriptionId";
    /// @}
    /// Add for [HAC]
    private static final String GET_HAC_SUPPORT = "GET_HAC_SUPPORT";
    private static final String GET_HAC_SUPPORT_ON = "GET_HAC_SUPPORT=1";
    private static final String GET_HAC_ENABLE = "GET_HAC_ENABLE";
    private static final String GET_HAC_ENABLE_ON = "GET_HAC_ENABLE=1";

    public static final int GET_PIN_PUK_RETRY_EMPTY = -1;
    public static final String[] PROPERTY_SIM_PIN2_RETRY = {
        "gsm.sim.retry.pin2",
        "gsm.sim.retry.pin2.2",
        "gsm.sim.retry.pin2.3",
        "gsm.sim.retry.pin2.4",
    };

    public static final String PROPERTY_SIM_PUK2_RETRY[] = {
        "gsm.sim.retry.puk2",
        "gsm.sim.retry.puk2.2",
        "gsm.sim.retry.puk2.3",
        "gsm.sim.retry.puk2.4",
    };

    private static final String[] CMCC_CU_NUMERIC = {"46000", "46002", "46007", "46008",
        "46001", "46006", "46009", "45407"};
    /// M: add for dual volte feature @{
    private static int mDialogID = -1;
    private static Preference[] mPreferences = {null, null, null, null};
    private static Preference mPreference = null;
    private static PreferenceScreen mPreferenceScreen = null;
    private static SubscriptionInfoHelper mSubscriptionInfoHelper = null;
    /// @}
    private static final String OPERATOR_OP09 = "OP09";
    private static final String SEGDEFAULT = "SEGDEFAULT";

    /**
     * set DualMic noise reduction mode.
     * @param dualMic the value to show the user set
     */
    public static void setDualMicMode(String dualMic) {
        Context context = PhoneGlobals.getInstance().getApplicationContext();
        if (context == null) {
            return;
        }
        AudioManager audioManager = (AudioManager)
                context.getSystemService(Context.AUDIO_SERVICE);
        audioManager.setParameters(DUALMIC_MODE + "=" + dualMic);
    }

    /**
     * get DualMic noise reduction mode.
     */
    public static boolean isDualMicModeEnabled() {
        Context context = PhoneGlobals.getInstance().getApplicationContext();
        if (context == null) {
            return false;
        }
        String state = null;
        AudioManager audioManager = (AudioManager)
                context.getSystemService(Context.AUDIO_SERVICE);
        if (audioManager != null) {
            state = audioManager.getParameters(GET_DUALMIC_MODE);
            log("getDualMicMode(): state: " + state);
            if (state.equalsIgnoreCase(DUALMIC_ENABLED)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Add for ANC. Enable or disable the function.
     * @param flag If true, will enable ANC, else disable.
     */
    public static void setANCEnable(boolean flag) {
        int status = AudioSystem.setAudioCommand(SET_SPEECH_ANC_STATUS,
                flag ? ANC_ENABLE : ANC_DISABLE);
        log("setANCEnable()... flag = " + flag + ", status = " + status);
    }

    /**
     * check whether support ANC feature.
     * @return true support ANC featre. false do not support ANC feature
     */
    public static boolean isANCSupport() {
        // for 02965607 getAudioCommand is not available
        // currently ANC is not support, force to set to false
        // int support = AudioSystem.getAudioCommand(GET_SPEECH_ANC_SUPPORT);
        int support = 0;
        log("isANCSupport()... support = " + support);
        //if (ANC_ENABLE == support) {
        //    return true;
        //}
        return false;
    }

    /**
     * Add for ANC. Get the ANC status.
     * @return true if enable, else return false
     */
    public static boolean isANCEnabled() {
        // for 02965607 getAudioCommand is not available
        // currently ANC is not support, force to set to false
        // int enabled = AudioSystem.getAudioCommand(GET_SPEECH_ANC_ENABLED);
        int enabled = 0;
        log("isANCEnabled()... Enabled = " + enabled);
       // if (ANC_ENABLE == enabled) {
        //    return true;
      //  }
        return false;
    }

    /**
     * add for HAC(hearing aid compatible).
     * if return true, support HAC ,show UI. otherwise, disappear.
     * @return true, support. false, not support.
     */
    public static boolean isHacSupport() {
        Context context = PhoneGlobals.getInstance().getApplicationContext();
        if (context == null) {
            return false;
        }
        AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        if (audioManager != null) {
            String hac = audioManager.getParameters(GET_HAC_SUPPORT);
            log("hac support: " + hac);
            return GET_HAC_SUPPORT_ON.equals(hac);
        }
        return false;
    }

    /**
     * Get HAC's state. For upgrade issue.
     * In KK we don't use DB, so we still need use query Audio State to sync with DB.
     * @return 1, HAC enable; 0, HAC disable.
     */
    public static int isHacEnable() {
        Context context = PhoneGlobals.getInstance().getApplicationContext();
        if (context == null) {
            log("isHacEnable : context is null");
            return 0;
        }
        AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        if (audioManager != null) {
            String hac = audioManager.getParameters(GET_HAC_ENABLE);
            log("hac enable: " + hac);
            return GET_HAC_ENABLE_ON.equals(hac) ? 1 : 0;
        }
        log("isHacEnable : audioManager is null");
        return 0;
    }

    /**
     * Check if the subscription card is USIM or SIM.
     * @param context using for query phone
     * @param subId according to the phone
     * @return true if is USIM card
     */
    public static boolean isUSIMCard(Context context, int subId) {
        log("isUSIMCard()... subId = " + subId);
        String type = PhoneUtils.getPhoneUsingSubId(subId).getIccCard().getIccCardType();
        log("isUSIMCard()... type = " + type);
        return USIM.equals(type);
    }

    public static boolean isSimStateReady(int slot) {
        boolean isSimStateReady = false;
        isSimStateReady = TelephonyManager.SIM_STATE_READY == TelephonyManager.
                getDefault().getSimState(slot);
        log("isSimStateReady: "  + isSimStateReady);
        return isSimStateReady;
    }

    public static void goUpToTopLevelSetting(Activity activity, Class<?> targetClass) {
        Intent intent = new Intent(activity.getApplicationContext(), targetClass);
        intent.setAction(Intent.ACTION_MAIN);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        activity.startActivity(intent);
        activity.finish();
    }

    public static boolean isAllRadioOff(Context context) {
        boolean result = true;
        boolean airplaneModeOn = isAirplaneModeOn(context);
        int subId;
        List<SubscriptionInfo> activeSubList = PhoneUtils.getActiveSubInfoList();
        for (int i = 0; i < activeSubList.size(); i++) {
            subId = activeSubList.get(i).getSubscriptionId();
            if (isRadioOn(subId, context)) {
                result = false;
                break;
            }
        }
        return result || airplaneModeOn;
    }

    /**
     * check the radio is on or off by sub id.
     *
     * @param subId the sub id
     * @return true if radio on
     */
    public static boolean isRadioOn(int subId, Context context) {
        log("[isRadioOn]subId:" + subId);
        boolean isRadioOn = false;
        final ITelephony iTel = ITelephony.Stub.asInterface(
                ServiceManager.getService(Context.TELEPHONY_SERVICE));
        if (iTel != null && PhoneUtils.isValidSubId(subId)) {
            try {
                isRadioOn = iTel.isRadioOnForSubscriber(subId, context.getPackageName());
            } catch (RemoteException e) {
                log("[isRadioOn] failed to get radio state for sub " + subId);
                isRadioOn = false;
            }
        } else {
            log("[isRadioOn]failed to check radio");
        }
        log("[isRadioOn]isRadioOn:" + isRadioOn);

        return isRadioOn && !isAirplaneModeOn(PhoneGlobals.getInstance());
    }

    /**
     * Get pin2 left retry times.
     * @param subId the sub which one user want to get
     * @return the left times
     */
    public static int getPin2RetryNumber(int subId) {
        if (!PhoneUtils.isValidSubId(subId)) {
            log("getPin2RetryNumber : inValid SubId = " + subId);
            return -1;
        }
        int slot = SubscriptionManager.getSlotId(subId);
        log("getPin2RetryNumber : --> Sub:Slot = " + subId + ":" + slot);
        String pin2RetryStr;
        if (isGeminiProject()) {
            if (slot < PROPERTY_SIM_PIN2_RETRY.length) {
                pin2RetryStr = PROPERTY_SIM_PIN2_RETRY[slot];
            } else {
                Log.w(TAG, "PIN2 --> Slot num is invalid : Error happened !!");
                pin2RetryStr = PROPERTY_SIM_PIN2_RETRY[0];
            }
        } else {
            pin2RetryStr = PROPERTY_SIM_PIN2_RETRY[0];
        }
        return SystemProperties.getInt(pin2RetryStr, GET_PIN_PUK_RETRY_EMPTY);
    }

    /**
     * Get the pin2 retry tips messages.
     * @param context
     * @param subId
     * @return
     */
    public static String getPinPuk2RetryLeftNumTips(Context context, int subId, boolean isPin) {
        if (!PhoneUtils.isValidSubId(subId)) {
            log("getPinPuk2RetryLeftNumTips : inValid SubId =  " + subId);
            return " ";
        }
        int retryCount = GET_PIN_PUK_RETRY_EMPTY;
        if (isPin) {
            retryCount = getPin2RetryNumber(subId);
        } else {
            retryCount = getPuk2RetryNumber(subId);
        }
        log("getPinPuk2RetryLeftNumTips : retry count = " + retryCount + " isPin : " + isPin);
        switch (retryCount) {
            case GET_PIN_PUK_RETRY_EMPTY:
                return " ";
            default:
                return context.getString(R.string.retries_left, retryCount);
        }
    }

    /**
     * Get puk2 left retry times.
     * @param subId the sub which one user want to get
     * @return the left times
     */
    public static int getPuk2RetryNumber(int subId) {
        if (!PhoneUtils.isValidSubId(subId)) {
            log("getPuk2RetryNumber : inValid SubId = " + subId);
            return -1;
        }
        int slot = SubscriptionManager.getSlotId(subId);
        log("getPuk2RetryNumber --> Sub:Slot = " + subId + ":" + slot);
        String puk2RetryStr;
        if (isGeminiProject()) {
            if (slot < PROPERTY_SIM_PIN2_RETRY.length) {
                puk2RetryStr = PROPERTY_SIM_PUK2_RETRY[slot];
            } else {
                Log.w(TAG, "PUK2 --> Slot num is invalid : Error happened !!");
                puk2RetryStr = PROPERTY_SIM_PUK2_RETRY[0];
            }
        } else {
            puk2RetryStr = PROPERTY_SIM_PUK2_RETRY[0];
        }
        return SystemProperties.getInt(puk2RetryStr, GET_PIN_PUK_RETRY_EMPTY);
    }

    public static boolean isPhoneBookReady(Context context, int subId) {
        final ITelephonyEx telephonyEx = ITelephonyEx.Stub.asInterface(
                ServiceManager.getService("phoneEx")); //TODO: Use Context.TELEPHONY_SERVICEEX
        boolean isPhoneBookReady = false;
        try {
            isPhoneBookReady = telephonyEx.isPhbReady(subId);
            Log.d(TAG, "[isPhoneBookReady]isPbReady:" + isPhoneBookReady + " ||subId:" + subId);
        } catch (RemoteException e) {
            Log.e(TAG, "[isPhoneBookReady]catch exception:");
            e.printStackTrace();
        }
        if (!isPhoneBookReady) {
            Toast.makeText(context,
                    context.getString(R.string.fdn_phone_book_busy), Toast.LENGTH_SHORT).show();
        }
        return isPhoneBookReady;
    }

    /**
     * Return whether the project is Gemini or not.
     * @return If Gemini, return true, else return false
     */
    public static boolean isGeminiProject() {
        boolean isGemini = TelephonyManager.getDefault().isMultiSimEnabled();
        log("isGeminiProject : " + isGemini);
        return isGemini;
    }

    /**
     * Add for [MTK_Enhanced4GLTE].
     * Get the phone is inCall or not.
     */
    public static boolean isInCall(Context context) {
        TelecomManager manager = (TelecomManager) context.getSystemService(
                Context.TELECOM_SERVICE);
        boolean inCall = false;
        if (manager != null) {
            inCall = manager.isInCall();
        }
        log("[isInCall] = " + inCall);
        return inCall;
    }
    /**
     * Add for [MTK_Enhanced4GLTE].
     * Get the phone is inCall or not.
     */
    public static boolean isAirplaneModeOn(Context context) {
        return Settings.Global.getInt(context.getContentResolver(),
                Settings.Global.AIRPLANE_MODE_ON, 0) != 0;
    }

    ///M: Add for [VoLTE_SS] @{
    /**
     * Get whether the IMS is IN_SERVICE.
     * @param subId the sub which one user selected.
     * @return true if the ImsPhone is IN_SERVICE, else false.
     */
    public static boolean isImsServiceAvailable(Context context, int subId) {
        boolean available = false;
        if (PhoneUtils.isValidSubId(subId)) {
            int phoneId = getMainCapabilityPhoneId(context);
            if (isSupportMims()) {
                phoneId = SubscriptionManager.getPhoneId(subId);
            }
            try {
                ImsManager imsManager = ImsManager.getInstance(context, phoneId);
                available = imsManager.getImsRegInfo();
            } catch (ImsException e) {
                log("Get IMS register info fail.");
            }
        }
        log("isImsServiceAvailable[ " + subId + "], available = " + available);
        return available;
    }

    /**
     * Get the SIM card's mobile data connection status, which is inserted in the given sub
     * @param subId the given subId
     * @param context
     * @return true, if enabled, else false.
     */
    public static boolean isMobileDataEnabled(int subId) {
        if (PhoneUtils.isValidSubId(subId)) {
            boolean isDataEnable = PhoneUtils.getPhoneUsingSubId(subId).getDataEnabled();
            log("[isMobileDataEnabled] isDataEnable = " + isDataEnable);
            return isDataEnable;
        }
        log("[isMobileDataEnabled] SubId = " + subId);
        return false;
    }

    /**
     * When SS from VoLTE we should make the Mobile Data Connection open, if don't open,
     * the query will fail, so we should give users a tip, tell them how to get SS successfully.
     * This function is get the point, whether we should show a tip to user. Conditions:
     * 1. VoLTE condition / CMCC support VoLTE card, no mater IMS enable/not
     * 2. Mobile Data Connection is not enable
     * @param subId the given subId
     * @return true if should show tip, else false.
     */
    public static boolean shouldShowOpenMobileDataDialog(Context context, int subId) {
        boolean result = false;
        if (!PhoneUtils.isValidSubId(subId)) {
            log("[shouldShowOpenMobileDataDialog] invalid subId!!!  " + subId);
            return false;
        }
        PersistableBundle carrierConfig =
                   PhoneGlobals.getInstance().getCarrierConfigForSubId(subId);

        /// M: For plug-in @{
        if(!ExtensionManager.getCallFeaturesSettingExt().
                needShowOpenMobileDataDialog(context, subId) || !carrierConfig.
                getBoolean(CarrierConfigManager.KEY_SHOW_OPEN_MOBILE_DATA_DIALOG_BOOL)) {
            return false;
        }
        /// @}
        String mccMnc = TelephonyManager.getDefault().getSimOperator(subId);
        if (OperatorUtils.isNotSupportXcap(mccMnc)) {
             return false;
        }

        Phone phone = PhoneUtils.getPhoneUsingSubId(subId);
        if (isImsServiceAvailable(context, subId) ||
                (isGsmUtSupport(phone.getPhoneId())
                && (phone.getCsFallbackStatus() == PhoneConstants.UT_CSFB_PS_PREFERRED))
                /// M: [CT VOLTE]
                || ((TelephonyUtilsEx.isCtVolteEnabled() && TelephonyUtilsEx
                    .isCt4gSim(subId)) || TelephonyUtilsEx.isSmartFren4gSim(context, subId))) {
            log("[shouldShowOpenMobileDataDialog] ss query need mobile data connection!");
            ///M: WFC <when wfc registered, need not check for mobile data as SS can go over wifi>@{
            boolean isWfcEnabled = ((TelephonyManager)context
                    .getSystemService(Context.TELEPHONY_SERVICE)).isWifiCallingAvailable();
            if (isWfcEnabled && !isCmccOrCuCard(subId)) {
                return result;
            }
            /// @}

            boolean isNetworkRoaming = !TelephonyManagerEx.getDefault().isInHomeNetwork(subId);

            if (!TelephonyUtils.isMobileDataEnabled(subId)) {
                result = true;
            /// M: add for dual volte feature @{
            } else if ((subId != SubscriptionManager.getDefaultDataSubscriptionId()) &&
                isSupportDualVolte(subId)) {
                result = true;
            /// @}
            } else if (isNetworkRoaming && isCmccOrCuCard(subId)
                && !phone.getDataRoamingEnabled() && !isOP09ASupport()) {
                //M: Add for data roaming tips
                //When CMCC Network is Roaming and data roaming not enabled,
                //we should also give user tips to turn it on.
                log("[shouldShowOpenMobileDataDialog] network is roaming!");
                result = true;
            }
        }
        log("[shouldShowOpenMobileDataDialog] subId: result = " + subId + ":" + result);
        return result;
    }

    /**
     *M: Add for data connection and roaming and data traffic tips
     * Get tip message, let user open the mobile data connection or data roaming.
     * @param context current context
     * @param subId the given subId
     * @return tip message shown to user
     */
    public static String getTipsDialogMessage(final Context context, int subId) {
        String message = "";
        Phone phone = PhoneUtils.getPhoneUsingSubId(subId);

        boolean isMobileDataAvailable = TelephonyUtils.isMobileDataEnabled(subId);
        boolean isRoamingAvailable = phone.getDataRoamingEnabled();
        boolean isNetworkRoaming = !TelephonyManagerEx.getDefault().isInHomeNetwork(subId);
        /// M: add for dual volte feature @{
        boolean isUseDataTraffic = ((subId != SubscriptionManager.getDefaultDataSubscriptionId())
                                        || !isMobileDataAvailable);
        String displayName = PhoneUtils.getSubDisplayName(subId);
        mDialogID = -1;

        if (isUseDataTraffic &&
               (SystemProperties.getInt("ro.mtk_multiple_ims_support", 1) > 1)) {
            if (isNetworkRoaming && !isRoamingAvailable) {
                message = context.getString(
                R.string.volte_ss_not_available_tips_data_roaming, displayName);
                mDialogID = DATA_ROAMING_DIALOG;
            } else {
                message = context.getString(
                R.string.volte_ss_not_available_tips_data_traffic, displayName);
                mDialogID = DATA_TRAFFIC_DIALOG;
            }
        /// @}
        } else if (!isMobileDataAvailable &&
                (SystemProperties.getInt("ro.mtk_multiple_ims_support", 1) == 1)) {
            if (isNetworkRoaming && !isRoamingAvailable) {
                message = context.getString(
                R.string.volte_ss_not_available_tips_data_roaming, displayName);
                mDialogID = DATA_ROAMING_DIALOG;
            } else {
                message = context.getString(
                R.string.volte_ss_not_available_tips_data, displayName);
                mDialogID = DATA_USAGE_DIALOG;
            }
        } else {
            if (isNetworkRoaming && !isRoamingAvailable) {
                message = context.getString(
                R.string.volte_ss_not_available_tips_roaming, displayName);
                mDialogID = DATA_ROAMING_DIALOG;
            }
        }
        log("getTipsDialogMessage, isUseDataTraffic= " + isUseDataTraffic +
                ", mDialogID= " + mDialogID);
        return message;
    }

    /**
     * Show a tip dialog, let user open the mobile data connection.
     * @param context
     */
    public static void showOpenMobileDataDialog(final Context context, int subId) {
        AlertDialog.Builder b = new AlertDialog.Builder(context);
        // M: Modified for data connection and roaming tips
        String message = getTipsDialogMessage(context, subId);
        /*String message = context.getString(
                R.string.volte_ss_not_available_tips, PhoneUtils.getSubDisplayName(subId));*/
        b.setMessage(message);
        switch (mDialogID) {
            /// M: add for dual volte feature @{
            case DATA_TRAFFIC_DIALOG:
                b.setPositiveButton(R.string.alert_dialog_ok,
                        new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    log("show DATA_TRAFFIC_DIALOG, OK button clicked!");
                    Intent intent = null;
                    /* PRIZE Telephony zhoushuanghua add for 60719 <2018_06_19> start */
                    if (mSubscriptionInfoHelper == null || mPreference == null) {
                        log("mSubscriptionInfoHelper || mPreference is null!");
                        return;
                    }
                    /* PRIZE Telephony zhoushuanghua add for 60719 <2018_06_19> end */
                    log("showOpenMobileDataDialog, mPreference = " + mPreference);
                    if (mPreference.getKey().equals(CALL_FORWARDING_KEY)
                       || (mPreference.getKey().equals(KEY_CALL_FORWARD))) {
                        intent = mSubscriptionInfoHelper.getIntent(GsmUmtsCallForwardOptions.class);
                    } else if (mPreference.getKey().equals(BUTTON_CB_EXPAND)) {
                        intent = mSubscriptionInfoHelper.getIntent(CallBarring.class);
                    } else if (mPreference.getKey().equals(ADDITIONAL_GSM_SETTINGS_KEY)) {
                        intent = mSubscriptionInfoHelper.
                                getIntent(GsmUmtsAdditionalCallOptions.class);
                    } else {
                        intent = mSubscriptionInfoHelper.getIntent(CdmaCallWaitingUtOptions.class);
                    }
                    SubscriptionInfoHelper.addExtrasToIntent(intent, SubscriptionManager
                                      .from(context).getSubscriptionInfo(subId));
                    context.startActivity(intent);
                    setParameters(null, null, null);
                }
                });
                b.setNegativeButton(android.R.string.cancel, null);
                b.setCancelable(false);
                log("Need to add cancel button.");
                break;
            /// @}
            case DATA_ROAMING_DIALOG:
            case DATA_USAGE_DIALOG:
                b.setPositiveButton(R.string.alert_dialog_ok,
                        new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    log("showOpenMobileDataTips, OK button clicked!");
                }
                });
                b.setCancelable(false);
                break;
            default:
                log("Unknown abnormal case!");
                break;
        }
        AlertDialog dialog = b.create();
        // make the dialog more obvious by bluring the background.
        dialog.getWindow().addFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND);
        dialog.show();
    }
    /// @}

    public static boolean is2GOnlyProject() {
        boolean result = true;
        Phone[] phones = PhoneFactory.getPhones();
        for(Phone phone : phones) {
            if (phone.getRadioAccessFamily() != RadioAccessFamily.RAF_GSM) {
                result = false;
            }
        }

        log("[is2GOnlyProject] result = " + result);

        return result;
    }

    /// Add for [MagiConference] @{
    // Get Support / Enable Status.
    private static final String GET_MAGI_CONFERENCE_SUPPORT = "GET_MAGI_CONFERENCE_SUPPORT";
    private static final String GET_MAGI_CONFERENCE_ENABLE = "GET_MAGI_CONFERENCE_ENABLE";
    // Result: 1 means support the feature, the status is enabled. 0 means don't support / disabled
    private static final String MAGI_CONFERENCE_SUPPORT = "GET_MAGI_CONFERENCE_SUPPORT=1";
    private static final String MAGI_CONFERENCE_ENABLE = "GET_MAGI_CONFERENCE_ENABLE=1";
    // Set Enable / Disable
    private static final String SET_MAGI_CONFERENCE_ENABLE = "SET_MAGI_CONFERENCE_ENABLE=1";
    private static final String SET_MAGI_CONFERENCE_DISABLE = "SET_MAGI_CONFERENCE_ENABLE=0";
    /**
     * Check whether support MagiConference feature.
     * @return true if support MagiConference feature, else return false.
     */
    public static boolean isMagiConferenceSupport() {
        Context context = PhoneGlobals.getInstance().getApplicationContext();
        if (context == null) {
            return false;
        }
        AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        if (audioManager != null) {
            String magiConference = audioManager.getParameters(GET_MAGI_CONFERENCE_SUPPORT);
            log("[isMagiConferenceSupport] support: " + magiConference);
            return MAGI_CONFERENCE_SUPPORT.equals(magiConference);
        }
        return false;
    }

    /**
     * Get the MagiConference function status.
     * @return true if the function is enabled, else return false.
     */
    public static boolean isMagiConferenceEnable() {
        Context context = PhoneGlobals.getInstance().getApplicationContext();
        if (context == null) {
            return false;
        }
        AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        if (audioManager != null) {
            String isEnabled = audioManager.getParameters(GET_MAGI_CONFERENCE_ENABLE);
            log("[isMagiConferenceEnable] enable: " + isEnabled);
            return MAGI_CONFERENCE_ENABLE.equals(isEnabled);
        }
        return false;
    }

    /**
     * Enable or disable MagiConference function.
     * @param flag If true, enable MagiConference, else disable.
     */
    public static void setMagiConferenceEnable(boolean flag) {
        int status = AudioSystem.setParameters(
                flag ? SET_MAGI_CONFERENCE_ENABLE : SET_MAGI_CONFERENCE_DISABLE);
        log("[setMagiConferenceEnable] flag = " + flag + ", status = " + status);
    }
    /// @}

    /**
     * Return whether the phone is hot swap or not.
     * @return If hot swap, return true, else return false
     */
    public static boolean isHotSwapHanppened(List<SubscriptionInfo> originaList,
            List<SubscriptionInfo> currentList) {
        boolean result = true;
        if (originaList.equals(currentList)) {
            result = false;
        }

        log("isHotSwapHanppened : " + result);
        return result;
    }

    /**
     * Return whether the project is support WCDMA Preferred.
     * @return If support, return true, else return false
     */
    public static boolean isWCDMAPreferredSupport() {
        String isWCDMAPreferred = SystemProperties.get("ro.mtk_rat_wcdma_preferred");
        if (TextUtils.isEmpty(isWCDMAPreferred)) {
            log("isWCDMAPreferredSupport : false; isWCDMAPreferred is empty. ");
            return false;
        }
        log("isWCDMAPreferredSupport : " + isWCDMAPreferred);
        return "1".equals(isWCDMAPreferred);
    }

    /**
     * CDMA project feature option.
     * @return true if this project support CDMA.
     */
    public static boolean isCdmaSupport() {
        return "1".equals(SystemProperties.get("ro.boot.opt_c2k_support"));
    }

    private static void log(String msg) {
        Log.d(TAG, msg);
    }

    /**
     * M: Return if the sim card is cmcc or not. @{
     * @param subId sub id identify the sim card
     * @return true if the sim card is cmcc
     */
    public static boolean isCmccOrCuCard(int subId) {
        boolean result = false;
        String numeric = TelephonyManager.getDefault().getSimOperator(subId);
        for (String cmccOrCu : CMCC_CU_NUMERIC) {
            if (cmccOrCu.equals(numeric)) {
                result = true;
            }
        }
        log("isCmccOrCuCard:" + result);
        return result;
    }

    /// M: add for dual volte feature @{
    /**
     * M: Return if the sim card supports dual volte. @{
     * @param subId sub id identify the sim card
     * @return true if the sim card supports dual volte
     */
    public static boolean isSupportDualVolte(int subId) {
        boolean result = false;
        result = (SystemProperties.getInt("ro.mtk_multiple_ims_support", 1) > 1) &&
                (isCmccOrCuCard(subId) || (TelephonyUtilsEx.isCtVolteEnabled() &&
                TelephonyUtilsEx.isCt4gSim(subId)));
        return result ;
    }
    /// @}

    /**
     * M: Get status whether the sim card is invalid or not.
     * @param subId sub id identify the sim card
     * @return true if the sim card is invalid
     */
    public static boolean isInvalidSimCard(int subId) {
        boolean result = false;
        String numeric = TelephonyManager.getDefault().getSimOperator(subId);
        if (numeric == null || numeric == "") {
            result = true;
        }

        log("isInvalidSimCard:" + result);
        return result;
    }
    /** @} */

    private static boolean isGsmUtSupport(int phoneId) {
        if (SystemProperties.get("ro.mtk_bsp_package").equals("1")) {
            return false;
        }

        int subId = SubscriptionManager.getSubIdUsingPhoneId(phoneId);
        if (SystemProperties.get("persist.mtk_ims_support").equals("1") &&
                SystemProperties.get("persist.mtk_volte_support").equals("1")) {
            if (isCmccOrCuCard(subId)
                    && isUSIMCard(PhoneGlobals.getInstance().getApplicationContext(), subId)) {
                return true;
            }
        }
        return false;
    }

    /// M: add for dual volte feature @{
    /**
     * set the Parameters from CallfeaturesSetting.
     * @param preferenceScreen PreferenceScreen
     * @param preference Preference
     * @param subscriptionInfoHelper SubscriptionInfoHelper
     */
    public static void setParameters(PreferenceScreen preferenceScreen,
            Preference preference, SubscriptionInfoHelper subscriptionInfoHelper) {
        log("setParameters, preferenceScreen=" + preferenceScreen +
                "preference:" + preference + ",subscriptionInfoHelper:" + subscriptionInfoHelper);
        mPreference = preference;
        mPreferenceScreen = preferenceScreen;
        mSubscriptionInfoHelper = subscriptionInfoHelper;
    }

    /**
     * set the Parameters from CallfeaturesSetting by SubId.
     * @param subId SubId
     * @param preference Preference
     */
    public static void setParameters(int subId, Preference preference) {
        if (PhoneUtils.isValidSubId(subId)) {
            int phoneId = SubscriptionManager.getPhoneId(subId);
            mPreferences[phoneId] = preference;
            log("setParameters, mPreferences[" + phoneId + "]" + mPreferences[phoneId]);
        }
    }
    /// @}

	/* PRIZE Telephony zhoushuanghua add for 60719 <2018_06_19> start */
    public static void setParameters(int subId, Preference preference,SubscriptionInfoHelper subscriptionInfoHelper) {
        if (PhoneUtils.isValidSubId(subId)) {
            mPreference = preference;
            mSubscriptionInfoHelper = subscriptionInfoHelper;
        }
    }
	/* PRIZE Telephony zhoushuanghua add for 60719 <2018_06_19> end */

    private static boolean isOP09ASupport() {
        log("isOP09ASupport.");
        return OPERATOR_OP09.equals(SystemProperties.get("persist.operator.optr", ""))
                && SEGDEFAULT.equals(SystemProperties.get("persist.operator.seg", ""));
    }

    public static boolean isSupportMims() {
        boolean isSupportMims = (SystemProperties.getInt(MULTI_IMS_SUPPORT, 1) > 1);
        log("is support Mims:" + isSupportMims);
        return isSupportMims;
    }

    /**
     * Get the phone id with main capability.
     * @param context Context.
     * @return phoneId.
     */
    public static int getMainCapabilityPhoneId(Context context) {
        int phoneId = SubscriptionManager.INVALID_PHONE_INDEX;
        ITelephonyEx telephony = ITelephonyEx.Stub.asInterface(
                ServiceManager.getService(context.TELEPHONY_SERVICE_EX));

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

     /**
     * Get the Intent.
     * @context Context.
     * @param subId SubId.
     * @param newActivityClass The class of the activity for the intent to start.
     * @return Intent containing extras for the subscription id.
     */
    public static Intent getIntent(Context context, int subId, Class newActivityClass) {
        Intent intent = new Intent(context, newActivityClass);
        if (PhoneUtils.isValidSubId(subId)) {
            intent.putExtra(SUB_ID_EXTRA, subId);
        }
        return intent;
    }
}
