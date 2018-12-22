/* Copyright Statement:
 *
 * This software/firmware and related documentation ("MediaTek Software") are
 * protected under relevant copyright laws. The information contained herein
 * is confidential and proprietary to MediaTek Inc. and/or its licensors.
 * Without the prior written permission of MediaTek inc. and/or its licensors,
 * any reproduction, modification, use or disclosure of MediaTek Software,
 * and information contained herein, in whole or in part, shall be strictly prohibited.
 *
 * MediaTek Inc. (C) 2011. All rights reserved.
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
 */


package com.mediatek.dialer.others;

import android.content.Context;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.SwitchPreference;
import android.telecom.TelecomManager;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.android.dialer.R;
import com.android.ims.ImsManager;

public class CallOthersSettingsFragment extends PreferenceFragment
        implements Preference.OnPreferenceChangeListener {

    private static final String BUTTON_EVS_KEY = "button_evs_key";
    private static final String TAG = "CallOthersSetting";

    private SwitchPreference mButtonEVS;
    private Context mContext = null;
    private TelephonyManager mTelephonyManager = null;

    /**
     * Add call status listener, for EVS items(should be disable during calling and enable etc)
     */
    private final PhoneStateListener mPhoneStateListener = new PhoneStateListener() {
        @Override
        public void onCallStateChanged(int state, String incomingNumber) {
            boolean enabled = (state == TelephonyManager.CALL_STATE_IDLE);
            log("[onCallStateChanged] enabled = " + enabled);
            updateEVSState();
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        log("onCreate");
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.call_other_setting);
        mContext = getActivity();
        mTelephonyManager = (TelephonyManager)mContext.getSystemService(Context.TELEPHONY_SERVICE);
        mButtonEVS = (SwitchPreference) findPreference(BUTTON_EVS_KEY);
        mButtonEVS.setOnPreferenceChangeListener(this);
    }

    @Override
    public void onResume() {
        log("onResume");
        super.onResume();
        if (mButtonEVS != null) {
            mButtonEVS.setChecked(getEvsMode());
            updateEVSState();
        }
        mTelephonyManager.listen(mPhoneStateListener, PhoneStateListener.LISTEN_CALL_STATE);
    }

    @Override
    public void onPause() {
        log("onPause");
        super.onPause();
        mTelephonyManager.listen(mPhoneStateListener, PhoneStateListener.LISTEN_NONE);
    }

    /**
     * Supports onPreferenceChangeListener to look for preference changes.
     *
     * @param preference The preference to be changed
     * @param objValue The value of the selection, NOT its localized display value.
     * @return
     */
    @Override
    public boolean onPreferenceChange(Preference preference, Object objValue) {
        log("onPreferenceChange, preference =" + preference + "objValue=" + objValue);
        if (preference == mButtonEVS) {
            boolean enabled = (Boolean) objValue;
            mButtonEVS.setChecked(enabled);
            setEvsMode(enabled);
        } else {
            log("preference is null!");
        }
        return true;
    }

    /**
     * get Evs Mode.
     * @return  Evs Mode.
     */
    private boolean getEvsMode() {
        boolean evsMode = ImsManager.isEvsEnabledByUser(mContext);
        //boolean evsMode = true;
        log("getEvsMode, evsMode = " + evsMode);
        return evsMode;
    }

    /**
     * set Evs Mode.
     * @param enabled  set Evs mode true or false
     */
    private void setEvsMode(boolean enabled) {
        ImsManager.setEvsSetting(mContext, enabled);
        log("setEvsMode, enabled = " + enabled);
    }

    /**
     * update Evs status.
     */
    private void updateEVSState() {
        if (mButtonEVS != null) {
            boolean inCall = TelecomManager.from(mContext).isInCall();
            log("updateEVSState, incall = " + inCall);
            mButtonEVS.setEnabled(!inCall);
        }
    }
    private void log(final String log) {
        Log.i(TAG, log);
    }

}
