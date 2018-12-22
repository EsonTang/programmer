/*
 * Copyright (C) 2015 The Android Open Source Project
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
 * limitations under the License
 */

package com.android.phone.settings;

import android.content.Context;
import android.media.AudioManager;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;
import android.provider.Settings;
import android.telecom.TelecomManager;
import android.telephony.CarrierConfigManager;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.android.ims.ImsManager;
import com.android.internal.telephony.Phone;
import com.android.internal.telephony.PhoneFactory;
import com.android.phone.PhoneGlobals;
import com.android.phone.R;
import com.android.phone.settings.TtyModeListPreference;
import com.mediatek.phone.PhoneFeatureConstants.FeatureOption;
import com.mediatek.settings.TelephonyUtils;

import java.util.List;

public class AccessibilitySettingsFragment extends PreferenceFragment {
    private static final String LOG_TAG = AccessibilitySettingsFragment.class.getSimpleName();
    private static final boolean DBG = true;//(PhoneGlobals.DBG_LEVEL >= 2);

    private static final String BUTTON_TTY_KEY = "button_tty_mode_key";
    private static final String BUTTON_HAC_KEY = "button_hac_key";

    private final PhoneStateListener mPhoneStateListener = new PhoneStateListener() {
        /**
         * Disable the TTY setting when in/out of a call (and if carrier doesn't
         * support VoLTE with TTY).
         * @see android.telephony.PhoneStateListener#onCallStateChanged(int,
         * java.lang.String)
         */
        @Override
        public void onCallStateChanged(int state, String incomingNumber) {
            if (DBG) Log.d(LOG_TAG, "PhoneStateListener.onCallStateChanged: state=" + state);
            Preference pref = getPreferenceScreen().findPreference(BUTTON_TTY_KEY);
            if (pref != null) {
                final boolean isVolteTtySupported = ImsManager.isVolteEnabledByPlatform(mContext)
                        && getVolteTtySupported();
                pref.setEnabled((isVolteTtySupported && !isVideoCallInProgress()) ||
                        (state == TelephonyManager.CALL_STATE_IDLE));
            }
        }
    };

    private Context mContext;
    private AudioManager mAudioManager;

    private TtyModeListPreference mButtonTty;
    private CheckBoxPreference mButtonHac;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mContext = getActivity().getApplicationContext();
        mAudioManager = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);

        addPreferencesFromResource(R.xml.accessibility_settings);

        mButtonTty = (TtyModeListPreference) findPreference(
                getResources().getString(R.string.tty_mode_key));
        mButtonHac = (CheckBoxPreference) findPreference(BUTTON_HAC_KEY);

        if (PhoneGlobals.getInstance().phoneMgr.isTtyModeSupported()) {
            mButtonTty.init();
        } else {
            getPreferenceScreen().removePreference(mButtonTty);
            mButtonTty = null;
        }

        // PhoneGlobals.getInstance().phoneMgr.isHearingAidCompatibilitySupported()
        if (TelephonyUtils.isHacSupport()) {
            int hac = Settings.System.getInt(mContext.getContentResolver(),
                    Settings.System.HEARING_AID, SettingsConstants.HAC_DISABLED);
            mButtonHac.setChecked(hac == SettingsConstants.HAC_ENABLED);
        } else {
            getPreferenceScreen().removePreference(mButtonHac);
            mButtonHac = null;
        }

        /// M: add for mtk features @{
        initUi(getPreferenceScreen());
        /// @}
    }

    @Override
    public void onResume() {
        super.onResume();
        TelephonyManager tm =
                (TelephonyManager) mContext.getSystemService(Context.TELEPHONY_SERVICE);
        tm.listen(mPhoneStateListener, PhoneStateListener.LISTEN_CALL_STATE);
    }

    @Override
    public void onPause() {
        super.onPause();
        TelephonyManager tm =
                (TelephonyManager) mContext.getSystemService(Context.TELEPHONY_SERVICE);
        tm.listen(mPhoneStateListener, PhoneStateListener.LISTEN_NONE);
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        if (preference == mButtonTty) {
            return true;
        } else if (preference == mButtonHac) {
            int hac = mButtonHac.isChecked()
                    ? SettingsConstants.HAC_ENABLED : SettingsConstants.HAC_DISABLED;
            // Update HAC value in Settings database.
            Settings.System.putInt(mContext.getContentResolver(), Settings.System.HEARING_AID, hac);

            // Update HAC Value in AudioManager.
            mAudioManager.setParameter(SettingsConstants.HAC_KEY,
                    hac == SettingsConstants.HAC_ENABLED
                            ? SettingsConstants.HAC_VAL_ON : SettingsConstants.HAC_VAL_OFF);
            return true;
            /// M: add for mtk feature @{
        } else {
           return onPreferenceTreeClick(preference);
        }   /// @}
    }

    private boolean getVolteTtySupported() {
        CarrierConfigManager configManager =
                (CarrierConfigManager) mContext.getSystemService(Context.CARRIER_CONFIG_SERVICE);
        return configManager.getConfig().getBoolean(
                CarrierConfigManager.KEY_CARRIER_VOLTE_TTY_SUPPORTED_BOOL);
    }

    private boolean isVideoCallInProgress() {
        final Phone[] phones = PhoneFactory.getPhones();
        if (phones == null) {
            if (DBG) Log.d(LOG_TAG, "isVideoCallInProgress: No phones found. Return false");
            return false;
        }

        for (Phone phone : phones) {
            if (phone.isVideoCallPresent()) {
                return true;
            }
        }
        return false;
    }

    /// --------------------------------- MTK -------------------------------------
    private static final String TAG = "AccessibilitySettingsFragment";
    private CheckBoxPreference mButtonDualMic;
    /// Add for [ANC] (Active Noise Reduction)
    private CheckBoxPreference mButtonAnc;
    /// Add for [MagiConference]
    private CheckBoxPreference mButtonMagiConference;

    private static final String BUTTON_DUAL_MIC_KEY = "button_dual_mic_key";
    private static final String BUTTON_MAGI_CONFERENCE_KEY = "button_magi_conference_key";
    private static final String BUTTON_ANC_KEY = "button_anc_key";

    private void initUi(PreferenceScreen prf) {
        mButtonDualMic = (CheckBoxPreference) prf.findPreference(BUTTON_DUAL_MIC_KEY);
        if (FeatureOption.isMtkDualMicSupport() && !FeatureOption.isMTKA1Support()) {
            mButtonDualMic.setChecked(TelephonyUtils.isDualMicModeEnabled());
        } else {
            prf.removePreference(mButtonDualMic);
            mButtonDualMic = null;
        }

        mButtonMagiConference = (CheckBoxPreference) prf.findPreference(
                BUTTON_MAGI_CONFERENCE_KEY);
        if (TelephonyUtils.isMagiConferenceSupport() && !FeatureOption.isMTKA1Support()) {
            mButtonMagiConference.setChecked(TelephonyUtils.isMagiConferenceEnable());
        } else {
            prf.removePreference(mButtonMagiConference);
            mButtonMagiConference = null;
        }

        mButtonAnc = (CheckBoxPreference) prf.findPreference(BUTTON_ANC_KEY);
        if (TelephonyUtils.isANCSupport() && !FeatureOption.isMTKA1Support()) {
            mButtonAnc.setChecked(TelephonyUtils.isANCEnabled());
        } else {
            prf.removePreference(mButtonAnc);
            mButtonAnc = null;
        }
    }

    public boolean onPreferenceTreeClick(Preference preference) {
        if (preference == mButtonDualMic) {

            Log.d(TAG, "onPreferenceChange mButtonDualmic turn on : " + mButtonDualMic.isChecked());

            TelephonyUtils.setDualMicMode(mButtonDualMic.isChecked() ? SettingsConstants.DUA_VAL_ON
                    : SettingsConstants.DUAL_VAL_OFF);
            return true;
        } else if (preference == mButtonAnc) {
            boolean isChecked = mButtonAnc.isChecked();

            Log.d(TAG, "onPreferenceChange mButtonANC turn on : " + isChecked);

            TelephonyUtils.setANCEnable(isChecked);
            mButtonAnc.setSummary(isChecked ? R.string.anc_off : R.string.anc_on);
            return true;
        } else if (preference == mButtonMagiConference) {
            boolean isChecked = mButtonMagiConference.isChecked();

            Log.d(TAG, "onPreferenceChange mButtonMagiConference turn on : " + isChecked);

            TelephonyUtils.setMagiConferenceEnable(isChecked);
            return true;
        }
        return false;
    }
}
