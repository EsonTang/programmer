/*
* Copyright (C) 2011-2014 MediaTek Inc.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*      http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

package com.mediatek.systemui.statusbar.util;

import android.content.Context;
import android.hardware.display.DisplayManager;
import android.hardware.display.WifiDisplayStatus;
import android.net.ConnectivityManager;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemProperties;
import android.provider.Settings;
import android.telephony.ServiceState;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.android.internal.telephony.ITelephony;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * [SystemUI] Support "dual SIM" and "Notification toolbar".
 */
public class SIMHelper {

    private static final String TAG = "SIMHelper";

    private static List<SubscriptionInfo> sSimInfos;

    public static final int SLOT_INDEX_DEFAULT = 0;
    public static final int SLOT_INDEX_1 = 1;
    public static final int SLOT_INDEX_2 = 2;
    public static final int SLOT_INDEX_3 = 3;
    public static final int INVALID_SLOT_ID = -1000;

    public static Context sContext;

    private SIMHelper() {
    }

    /**
     * Get the default SIM id of the assigned business.
     *
     * @param context
     * @param businessType
     * @return The default SIM id, or -1 if it is not defined.
     */
    public static long getDefaultSIM(Context context, String businessType) {
        return Settings.System.getLong(context.getContentResolver(), businessType, -1);
    }

    public static void setDefaultSIM(Context context, String businessType, long simId) {
        Settings.System.putLong(context.getContentResolver(), businessType, simId);
    }

    public static List<SubscriptionInfo> getSIMInfoList(Context context) {
        if (sSimInfos == null || sSimInfos.size() == 0) {
            sSimInfos = getSortedSIMInfoList(context);
        }
        return sSimInfos;
    }
    private static List<SubscriptionInfo> getSortedSIMInfoList(Context context) {
        if (sSimInfos == null || sSimInfos.size() == 0) {
            Log.d("@M_" + TAG, "getSortedSIMInfoList, SubscriptionInfo is null");
            return null;
        }

        List<SubscriptionInfo> simInfoList = sSimInfos;
        Collections.sort(sSimInfos, new Comparator<SubscriptionInfo>() {
            @Override
            public int compare(SubscriptionInfo a, SubscriptionInfo b) {
                if (a.getSimSlotIndex() < b.getSimSlotIndex()) {
                    return -1;
                } else if (a.getSimSlotIndex() > b.getSimSlotIndex()) {
                    return 1;
                } else {
                    return 0;
                }
            }
        });
        return simInfoList;
    }

    /// M: [SystemUI] Support "dual SIM" @{
    public static SubscriptionInfo getSubInfoById(Context context, int subId) {
        if (sSimInfos == null || sSimInfos.size() == 0) {
            Log.d("@M_" + TAG, "getSubInfoById, SubscriptionInfo is null");
            return null;
        }

        for (SubscriptionInfo info : sSimInfos) {
            if (info.getSubscriptionId() == subId) {
                return info;
            }
        }
        return null;
    }

    public static SubscriptionInfo getSubInfoBySlot(Context context, int slotId) {
        if (sSimInfos == null || sSimInfos.size() == 0) {
            Log.d("@M_" + TAG, "getSubInfoBySlot, SubscriptionInfo is null");
            return null;
        }

        for (SubscriptionInfo info : sSimInfos) {
            if (info.getSimSlotIndex() == slotId) {
                return info;
            }
        }
        return null;
    }

    /**
     * Update Active SubscriptionInfos.
     *
     * @param context A Context object
     */
    public static void updateSIMInfos(Context context) {
        sSimInfos = SubscriptionManager.from(context).getActiveSubscriptionInfoList();
    }

    public static int getFirstSubInSlot(int slotId) {
        int[] subIds = SubscriptionManager.getSubId(slotId);
        if (subIds != null && subIds.length > 0) {
            return subIds[0];
        }
        Log.d(TAG, "Cannot get first sub in slot: " + slotId);
        return SubscriptionManager.INVALID_SUBSCRIPTION_ID;
    }

    public static boolean hasService(ServiceState ss) {
        if (ss != null) {
            // Consider the device to be in service if either voice or data service is available.
            // Some SIM cards are marketed as data-only and do not support voice service, and on
            // these SIM cards, we want to show signal bars for data service as well as the "no
            // service" or "emergency calls only" text that indicates that voice is not available.
            switch (ss.getVoiceRegState()) {
                case ServiceState.STATE_POWER_OFF:
                    return false;
                case ServiceState.STATE_OUT_OF_SERVICE:
                case ServiceState.STATE_EMERGENCY_ONLY:
                    return ss.getDataRegState() == ServiceState.STATE_IN_SERVICE;
                default:
                    return true;
            }
        } else {
            return false;
        }
    }

    public static int getSlotCount() {
        //FIXME: the slot count may not always be equal to phone count
        return TelephonyManager.getDefault().getPhoneCount();
    }

    public static boolean isSimInsertedBySlot(Context context, int slotId) {
        if (sSimInfos != null) {
            if (slotId <= getSlotCount() - 1) {
                SubscriptionInfo info = getSubInfoBySlot(context, slotId);
                if (info != null) {
                    return true;
                } else {
                    return false;
                }
            } else {
                return false; // default return false
            }
        } else {
            Log.d("@M_" + TAG, "isSimInsertedBySlot, SubscriptionInfo is null");
            return false;
        }
    }
    /// @}

    /// M: add HotKnot in quicksetting @{
    private static boolean bMtkHotKnotSupport =
            SystemProperties.get("ro.mtk_hotknot_support").equals("1");
    public static final boolean isMtkHotKnotSupport() {
        Log.d("@M_" + TAG, "isMtkHotKnotSupport, bMtkHotKnotSupport = " + bMtkHotKnotSupport);
        return bMtkHotKnotSupport;
    }
    /// add HotKnot in quicksetting @}

    /// M: add AudioProfile in quicksetting @{
    private static boolean bMtkAudioProfilesSupport =
            SystemProperties.get("ro.mtk_audio_profiles").equals("1")
                && !SystemProperties.get("ro.mtk_a1_feature").equals("1");

    public static final boolean isMtkAudioProfilesSupport() {
        return bMtkAudioProfilesSupport;
    }
    /// add AudioProfile in quicksetting @}

    /// M: add DataConnection in quicksetting @{
    private static boolean bMtkGemini3GSwitchSupport =
            SystemProperties.get("ro.mtk_gemini_3g_switch").equals("1");

    public static final boolean isMtkGemini3GSwitchSupport() {
        return bMtkGemini3GSwitchSupport;
    }

    public static void setContext(Context context) {
        sContext = context;
    }

    public static boolean isWifiOnlyDevice() {
        ConnectivityManager cm =
                (ConnectivityManager) sContext.getSystemService(sContext.CONNECTIVITY_SERVICE);
        return  !(cm.isNetworkSupported(ConnectivityManager.TYPE_MOBILE));
    }

    private static boolean bMtkGeminiSupport =
            SystemProperties.get("ro.mtk_gemini_support").equals("1");
    public static final boolean isMtkGeminiSupport() {
        return bMtkGeminiSupport;
    }
    /// M: add DataConnection in quicksetting @}

    /// M: Remove CastTile when WFD is not support in quicksetting @{
    public static boolean isWifiDisplaySupport() {
        DisplayManager mDisplayManager =
            (DisplayManager) sContext.getSystemService(Context.DISPLAY_SERVICE);
        WifiDisplayStatus status = mDisplayManager.getWifiDisplayStatus();
        if (status == null || status.getFeatureState()
                == WifiDisplayStatus.FEATURE_STATE_UNAVAILABLE) {
            return false;
        }
        return true;
    }
    /// M: Remove CastTile when WFD is not support in quicksetting @}

    /**
     * Get the radio state of default data sim.
     *
     * @param subId default data sim subid
     * @return The radio state of default data sim
     */
    public static boolean isRadioOn(int subId) {
        ITelephony telephony = ITelephony.Stub.asInterface(ServiceManager
                .getService(Context.TELEPHONY_SERVICE));
        try {
            if (telephony != null) {
                return telephony.isRadioOnForSubscriber(subId, sContext.getPackageName());
            }
        } catch (RemoteException e) {
            Log.e(TAG, "mTelephony exception");
        }
        return false;
    }
}
