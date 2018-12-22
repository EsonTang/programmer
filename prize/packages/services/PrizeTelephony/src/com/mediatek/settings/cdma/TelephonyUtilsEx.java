package com.mediatek.settings.cdma;

import android.content.Context;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemProperties;
import android.provider.Settings;
import android.telephony.RadioAccessFamily;
import android.telephony.ServiceState;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;

import com.android.internal.telephony.IccCardConstants;
import com.android.internal.telephony.IccCardConstants.CardType;
import com.android.internal.telephony.ITelephony;
import com.android.internal.telephony.Phone;
import com.android.internal.telephony.PhoneConstants;
import com.android.internal.telephony.PhoneFactory;
import com.android.internal.telephony.uicc.UiccController;
import com.android.phone.PhoneGlobals;
import com.android.phone.PhoneUtils;

import com.mediatek.internal.telephony.ITelephonyEx;
import com.mediatek.phone.PhoneFeatureConstants.FeatureOption;
import com.mediatek.settings.TelephonyUtils;
import com.mediatek.telephony.TelephonyManagerEx;

import java.util.List;
/**
 * Some util functions for C2K features.
 */
public class TelephonyUtilsEx {

    private static final String TAG = "TelephonyUtilsEx";
    private static final boolean DBG =
        SystemProperties.get("ro.build.type").equals("eng") ? true : false;
    private static final String PROPERTY_3G_SIM = "persist.radio.simswitch";
    private static final String PROPERTY_CDMA_SLOT = "persist.radio.cdma_slot";

    static final int preferredNetworkMode = Phone.PREFERRED_NT_MODE;
    public static final int CT_SIM = TelephonyManagerEx.APP_FAM_3GPP2;
    public static final int GSM_SIM = TelephonyManagerEx.APP_FAM_3GPP;
    public static final int C_G_SIM = CT_SIM | GSM_SIM;
    public static final int SIM_TYPE_NONE = TelephonyManagerEx.APP_FAM_NONE;
    private static final int MODE_PHONE1_ONLY = 1;
    private static final String[] PROPERTY_RIL_FULL_UICC_TYPE = {
        "gsm.ril.fulluicctype",
        "gsm.ril.fulluicctype.2",
        "gsm.ril.fulluicctype.3",
        "gsm.ril.fulluicctype.4",
    };

    private static final String[] CT_NUMERIC = { "45502", "45507", "46003", "46011", "46012",
            "46013", "20404" };
    private static final String[] SMARTFREN_NUMERIC = { "51009", "51028"};

    /**
     * Whether is CDMA phone.
     * @param phone the phone object.
     * @return true if is cdma phone.
     */
    public static boolean isCDMAPhone(Phone phone) {
        boolean result = false;

        if (phone != null) {
            int phoneType = phone.getPhoneType();
            if (phoneType == PhoneConstants.PHONE_TYPE_CDMA) {
                result = true;
            }
        }
        if (DBG) log("isCDMAPhone: " + result);

        return result;
    }

    /**
     * Get sim type.
     * @return sim type.
     */
    public static int getSimType(int slotId) {
        int simType = SIM_TYPE_NONE;
        TelephonyManagerEx telephonyManagerEx = TelephonyManagerEx.getDefault();
        if (telephonyManagerEx != null) {
            simType = telephonyManagerEx.getIccAppFamily(slotId);
        }

        if (DBG) {
            log("simType: " + simType);
        }
        return simType;
    }

    /**
     * Check is airplane mode on.
     * @return true if airplane mode on
    */
    public static boolean isAirPlaneMode() {
        boolean isAirPlaneMode = Settings.System.getInt(
                PhoneGlobals.getInstance().getContentResolver(),
                Settings.System.AIRPLANE_MODE_ON, -1) == 1;

        if (DBG) log("isAirPlaneMode = " + isAirPlaneMode);

        return isAirPlaneMode;
    }

    /**
     * Check lte data only mode.
     * @param context for getContentResolver
     * @return true if it is LteDataOnly mode
    */
    public static boolean is4GDataOnly(Context context) {
        boolean result = false;

        if (context != null && SubscriptionManager.isValidSubscriptionId(getCdmaSubId())) {
            int networkMode = Settings.Global.getInt(context.getContentResolver(),
                    android.provider.Settings.Global.PREFERRED_NETWORK_MODE + getCdmaSubId(),
                    preferredNetworkMode);
            if (networkMode == Phone.NT_MODE_LTE_TDD_ONLY) {
                result = true;
           }
        }

        if (DBG) {
            log("is4GDataOnly: " + result);
        }
        return result;
    }

    /**
     * Check is svlte slot inserted.
     * @return true if svlte slot inserted
     */
    public static boolean isSvlteSlotInserted() {
        boolean result = false;

        if (SubscriptionManager.isValidSubscriptionId(getCdmaSubId())) {
            int slotId = SubscriptionManager.getSlotId(getCdmaSubId());
                TelephonyManager telephonyManager = TelephonyManager.getDefault();
            if (telephonyManager != null) {
                result = telephonyManager.hasIccCard(slotId);
            }
        }
        if (DBG) {
            log("isSvlteSlotInserted = " + result);
        }
        return result;
    }

    /**
     * Check Radio State by target slot.
     *
     * @param slotId
     *            for check
     * @return true if radio is on
     */
    public static boolean getRadioStateForSlotId(final int slotId) {
        int currentSimMode = Settings.System.getInt(PhoneGlobals.getInstance()
                .getContentResolver(), Settings.System.MSIM_MODE_SETTING, -1);
        boolean radiosState = ((currentSimMode & (MODE_PHONE1_ONLY << slotId)) == 0)
                ? false : true;
        if (DBG) {
            log("soltId: " + slotId + ", radiosState : " + radiosState);
        }
        return radiosState;
    }

    /**
     * Check is svlte slot Radio On.
     * @return true if svlte slot Radio on
     */
    public static boolean isSvlteSlotRadioOn() {
        boolean result = false;

        if (SubscriptionManager.isValidSubscriptionId(getCdmaSubId())) {
            int slotId = SubscriptionManager.getSlotId(getCdmaSubId());
            result = getRadioStateForSlotId(slotId);
        }
        if (DBG) log("isSvlteSlotRadioOn = " + result);
        return result;
    }

    /**
     * Check whether Roaming or not.
     * @return true if Roaming
     */
    public static boolean isRoaming(Phone phone) {
        boolean result = false;
        int sub = -1;
        if (phone != null) {
            sub = phone.getSubId();
            ServiceState state = phone.getServiceState();
            if (state.getRoaming()) {
                result = true;
            }
        }
        if (DBG) {
            log("isRoaming[" + sub + "] " + result);
        }
        return result;
    }

    /**
     * Check whether it is CDMA SIM and Roaming by Phone type
     * used in the scenario that want to know roaming or not before camp on network
     * NOTICE: only for LTE DATA only feature
     * @return true if it is roaming by service state or a CDMA card but phone type is GSM
     */
    public static boolean isCdmaRoaming(Phone phone) {
        boolean result = false;
        int sub = -1;
        if (phone != null) {
            sub = phone.getSubId();
            boolean isCdma = isCdmaCardInserted(phone);
            result = isRoaming(phone)
                        || (isCdma && (phone.getPhoneType() == PhoneConstants.PHONE_TYPE_GSM));
        }
        if (DBG) {
            log("isCdmaRoaming[" + sub + "] " + result);
        }
        return result;
    }

    /**
     * Get the main phone id
     * @return
     */
    public static int getMainPhoneId() {
        int mainPhoneId = SubscriptionManager.INVALID_PHONE_INDEX;

        String curr3GSim = SystemProperties.get(PROPERTY_3G_SIM, "1");
        if (DBG) log("current 3G Sim = " + curr3GSim);

        if (!TextUtils.isEmpty(curr3GSim)) {
            int curr3GPhoneId = Integer.parseInt(curr3GSim);
            mainPhoneId = curr3GPhoneId - 1;
        }
        if (DBG) log("getMainPhoneId: " + mainPhoneId);

        return mainPhoneId;
    }

    /**
     * Check if phone has 4G capability.
     */
    public static boolean isCapabilityPhone(Phone phone) {
        boolean result = TelephonyUtilsEx.getMainPhoneId() == phone.getPhoneId();
        if (DBG) {
            log("isCapabilityPhone result = " + result
                + " phoneId = " + phone.getPhoneId());
        }
        return result;
    }

    public static boolean isCdma4gCard(int subId) {
        boolean result = false;
        CardType cardType = TelephonyManagerEx.getDefault().getCdmaCardType(
                SubscriptionManager.getSlotId(subId));
        if (cardType != null) {
            result = cardType.is4GCard();
        } else {
            if (DBG) {
                log("isCdma4gCard: cardType == null ");
            }
        }
        if (DBG) {
            log("isCdma4gCard result = " + result + "; subId = " + subId);
        }
        return result;
    }

    /**
     * Check if phone has 3G cdma card.
     * @param subId sub id for sim.
     * @return whether the card has capability.
     */
    public static boolean isCdma3gCard(int subId) {
        boolean result = false;
        String cardType = TelephonyManagerEx.getDefault().getIccCardType(
                SubscriptionManager.getSlotId(subId));
        if (cardType != null && cardType.equals("SIM")) {
            result = TelephonyManagerEx.getDefault().isCt3gDualMode(
                    SubscriptionManager.getSlotId(subId));
        } else {
            if (DBG) {
                log("isCdma3gCard: cardType == null ");
            }
        }
        if (DBG) {
            log("isCdma3gCard result = " + result + "; subId = " + subId);
        }
        return result;
    }

    /**
     * The system property value must -1 to get the slotId.
     * @return SubscriptionManager.INVALID_SUBSCRIPTION_ID if invalid
     */
    public static int getCdmaSubId() {
        int slotId = SystemProperties.getInt(PROPERTY_CDMA_SLOT, -1) -1;
        if (DBG) {
            log("[getCdmaSlotId] : slotId = " + slotId);
        }
        if (SubscriptionManager.isValidSlotId(slotId)) {
            int subId[] = SubscriptionManager.getSubId(slotId);
            if (subId != null) {
                if (DBG) log("[getCdmaSlotId] : subId = " + subId[0]);
                return subId[0];
            }
        }
        return SubscriptionManager.INVALID_SUBSCRIPTION_ID;
    }

    public static boolean isCdmaCardInserted(Phone phone) {
        int simType = getSimType(phone.getPhoneId());
        boolean result = (simType == CT_SIM || simType == C_G_SIM);
        if (DBG) {
            log("isCdmaCardInserted simType = " + simType
                + "result = " + result);
        }
        return result;
    }

    private static void log(String msg) {
        Log.d(TAG, msg);
    }
    public static boolean isCtSim(int subId) {
        boolean ctSim = false;
        String numeric = TelephonyManager.getDefault().getSimOperator(subId);
        for (String ct : CT_NUMERIC) {
            if (ct.equals(numeric)) {
                ctSim = true;
                break;
            }
        }
        Log.d(TAG, "getSimOperator:" + numeric + ", sub id :" + subId + ", isCtSim " + ctSim);
        return ctSim;
    }

    public static boolean isCt4gSim(int subId) {
        return isCtSim(subId) && isCdma4gCard(subId);
    }

    public static boolean isCtVolteEnabled() {
        boolean result = SystemProperties.get("persist.mtk_ct_volte_support").equals("1");
        if (DBG) {
            Log.d(TAG, "isCtVolteEnabled " + result);
        }
        return result;
    }

    /**
     * Check if phone has both slot CT.
     * @param phoneSubscriptionManager provide SubScription Manager.
     * @return true if phone has both slot CT.
     */
    public static boolean isBothslotCtSim(SubscriptionManager phoneSubscriptionManager) {
        List<SubscriptionInfo> infos = phoneSubscriptionManager.getActiveSubscriptionInfoList();
        /*PRIZE-Change-DialerV8-wangzhong-2017_7_19-start*/
        /*if (infos.size() <= 1) {
            return false;
        }*/
        if (null == infos || infos.size() <= 1) {
            return false;
        }
        /*PRIZE-Change-DialerV8-wangzhong-2017_7_19-end*/
        boolean result = false;
        for (SubscriptionInfo info : infos) {
            int subId = info.getSubscriptionId();
            result = isCtSim(subId);
            if (result == false) {
                break;
            }
        }
        return result;
    }

    /**
     * Check if phone has both slot CT4G.
     * @param phoneSubscriptionManager provide SubScription Manager.
     * @return true if phone has both slot CT4G.
     */
    public static boolean isBothslotCt4gSim(SubscriptionManager phoneSubscriptionManager) {
        List<SubscriptionInfo> infos = phoneSubscriptionManager.getActiveSubscriptionInfoList();
        if (infos.size() <= 1) {
            return false;
        }
        boolean result = false;
        for (SubscriptionInfo info : infos) {
            int subId = info.getSubscriptionId();
            result = isCt4gSim(subId);
            if (result == false) {
                break;
            }
        }
        return result;
    }

    public static boolean isSmartFrenSim(int subId) {
        boolean sfSim = false;
        String numeric = TelephonyManager.getDefault().getSimOperator(subId);
        for (String sf : SMARTFREN_NUMERIC) {
            if (sf.equals(numeric)) {
                sfSim = true;
                break;
            }
        }
        Log.d(TAG, "getSimOperator:" + numeric + ", sub id :" + subId + ", isSmartFrenSim "
                             + sfSim);
        return sfSim;
    }

    public static boolean isSmartFren4gSim(Context context, int subId) {
        return isSmartFrenSim(subId) && TelephonyUtils.isImsServiceAvailable(context, subId);
    }
}
