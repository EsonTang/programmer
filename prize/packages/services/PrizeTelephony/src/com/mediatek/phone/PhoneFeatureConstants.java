/*
* Copyright (C) 2011-2014 Mediatek.inc.
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
package com.mediatek.phone;

import android.content.Context;
import android.media.AudioManager;
import android.os.SystemProperties;
import android.util.Log;

import com.android.internal.telephony.PhoneConstants;
import com.android.phone.PhoneGlobals;

import com.mediatek.internal.telephony.ratconfiguration.RatConfiguration;
import com.mediatek.telephony.TelephonyManagerEx;

public class PhoneFeatureConstants {

    public static final class FeatureOption {
        private static final String TAG = "FeatureOption";
        private static final String MTK_DUAL_MIC_SUPPORT = "MTK_DUAL_MIC_SUPPORT";
        private static final String MTK_DUAL_MIC_SUPPORT_on = "MTK_DUAL_MIC_SUPPORT=true";
        private final static String ONE = "1";
        private final static String TWO = "2";

        public static boolean isMtkDualMicSupport() {
            String state = null;
            AudioManager audioManager = (AudioManager)
                    PhoneGlobals.getInstance().getSystemService(Context.AUDIO_SERVICE);
            if (audioManager != null) {
                state = audioManager.getParameters(MTK_DUAL_MIC_SUPPORT);
                Log.d(state, "isMtkDualMicSupport(): state: " + state);
                if (state.equalsIgnoreCase(MTK_DUAL_MIC_SUPPORT_on)) {
                    return true;
                }
            }
            return false;
        }

        public static boolean isMtkFemtoCellSupport() {
            boolean isSupport = ONE.equals(
                    SystemProperties.get("ro.mtk_femto_cell_support")) ? true : false;
            Log.d(TAG, "isMtkFemtoCellSupport(): " + isSupport);
            return isSupport;
        }

        public static boolean isMtk3gDongleSupport() {
            boolean isSupport = ONE.equals(
                    SystemProperties.get("ro.mtk_3gdongle_support")) ? true : false;
            Log.d(TAG, "isMtk3gDongleSupport()" + isSupport);
            return isSupport;
        }

        public static boolean isMtkLteSupport() {
            boolean isSupport = ONE.equals(
                    SystemProperties.get("ro.boot.opt_lte_support")) ? true : false;
            if (isNeedDisable4G()) {
                isSupport = false;
            }
            Log.d(TAG, "isMtkLteSupport(): " + isSupport);
            return isSupport;
        }

        /**
         * This is work around solution for bad die phone
         * 0-- default; 1-- good; 2-- bad
         * @return
         */
        public static boolean isNeedDisable4G() {
            boolean isSupport = TWO.equals(
                    SystemProperties.get("persist.radio.lte.chip")) ? true : false;
            Log.d(TAG, "isNeedDisable4G(): " + isSupport);
            return isSupport;
        }

        /**
         * C2k 5M (CLLWG)
         */
        public static boolean isMtkC2k5MSupport() {
            boolean isSupport = RatConfiguration.isC2kSupported() &&
                    RatConfiguration.isLteFddSupported() &&
                    RatConfiguration.isLteTddSupported() &&
                    RatConfiguration.isWcdmaSupported() &&
                    RatConfiguration.isGsmSupported() &&
                    !RatConfiguration.isTdscdmaSupported();

            Log.d(TAG, "isMtkC2k5M(): " + isSupport);
            return isSupport;
        }

        /**
         * C2k 4M (CLLG)
         */
        public static boolean isMtkC2k4MSupport() {
            boolean isSupport = RatConfiguration.isC2kSupported() &&
                    RatConfiguration.isLteFddSupported() &&
                    RatConfiguration.isLteTddSupported() &&
                    RatConfiguration.isGsmSupported() &&
                    !RatConfiguration.isWcdmaSupported() &&
                    !RatConfiguration.isTdscdmaSupported();

            Log.d(TAG, "isMtkC2k4M(): " + isSupport);
            return isSupport;
        }

        /**
         * C2k 3M (CWG)
         */
        public static boolean isMtkC2k3MSupport() {
            boolean isSupport = RatConfiguration.isC2kSupported() &&
                    RatConfiguration.isWcdmaSupported() &&
                    RatConfiguration.isGsmSupported() &&
                    !RatConfiguration.isLteFddSupported() &&
                    !RatConfiguration.isLteTddSupported() &&
                    !RatConfiguration.isTdscdmaSupported();

            Log.d(TAG, "isMtkC2k3M(): " + isSupport);
            return isSupport;
        }

        public static boolean isMtkSvlteSupport() {
            boolean isSupport = ONE.equals(
                    SystemProperties.get("ro.boot.opt_c2k_lte_mode")) ? true : false;
            Log.d(TAG, "isMtkSvlteSupport(): " + isSupport);
            return isSupport;
        }

        public static boolean isMtkSrlteSupport() {
            boolean isSupport = TWO.equals(
                    SystemProperties.get("ro.boot.opt_c2k_lte_mode")) ? true : false;
            Log.d(TAG, "isMtkSrlteSupport(): " + isSupport);
            return isSupport;
        }

        public static boolean isMtkTddDataOnlySupport() {
            boolean isSupport = ONE.equals(SystemProperties.get(
                    "ro.mtk_tdd_data_only_support")) ? true : false;
            Log.d(TAG, "isMtkTddDataOnlySupport(): " + isSupport);
            return isSupport;
        }

        public static boolean isMtkCtaSet() {
            boolean isSupport = ONE.equals(
                    SystemProperties.get("ro.mtk_cta_set")) ? true : false;
            Log.d(TAG, "isMtkCtaSet(): " + isSupport);
            return isSupport;
        }

        public static boolean isMTKA1Support() {
            boolean isSupport = ONE.equals(
                    SystemProperties.get("ro.mtk_a1_feature")) ? true : false;
            Log.d(TAG, "isMTKA1Support(): " + isSupport);
            return isSupport;
        }

        public static boolean isMTKSimSwitchSupport() {
            boolean isSupport = ONE.equals(SystemProperties.get(
                    "ro.mtk_disable_cap_switch")) ? true : false;
            Log.d(TAG, "isMTKSimSwitchSupport(): " + !isSupport);
            return !isSupport;
        }

        public static boolean isCTLteTddTestSupport() {
            String[] type = TelephonyManagerEx.getDefault().getSupportCardType(
                    PhoneConstants.SIM_ID_1);
            if (type == null) {
                return false;
            }
            boolean isUsimOnly = false;
            if ((type.length == 1) && ("USIM".equals(type[0]))) {
                isUsimOnly = true;
            }
            return isMtkSvlteSupport()
                    && (ONE.equals(SystemProperties.get("persist.sys.forcttddtest", "0")))
                    && isUsimOnly;
        }
    }
}
