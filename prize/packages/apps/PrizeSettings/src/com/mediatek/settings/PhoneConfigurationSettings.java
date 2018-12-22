/*
 * Copyright (C) 2011 The Android Open Source Project
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

package com.mediatek.settings;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.DialogInterface;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.Preference;
import android.telecom.TelecomManager;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.util.AttributeSet;
import android.util.Log;
//import android.view.WindowManager;

import com.android.settings.R;

import com.mediatek.usp.UspManager;

import java.util.Map;

/**.
 * ListPreference to show options for phone configuration to user.
 */
public class PhoneConfigurationSettings extends ListPreference implements
        Preference.OnPreferenceChangeListener {

    private static final String TAG = "PhoneConfigurationSettings";

    private static final String OM_PACKAGE_VALUE = "00";

    private Context mContext;
    private String mSelectedPhoneConfig;
    private AlertDialog mCarrierConfigAlertDialog;
    private UspManager mUspManager;

    private BroadcastReceiver mIntentReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Log.d(TAG, "onReceive():" + action);
            if (action.equals(TelephonyManager.ACTION_PHONE_STATE_CHANGED)) {
                boolean isCallStateIdle = !TelecomManager.from(context).isInCall();
                Log.d(TAG, "isCallStateIdle:" + isCallStateIdle);
                PhoneConfigurationSettings.this.setEnabled(isCallStateIdle);
            }
            /// @}
        }
    };

    /**.
     * Constructor
     * @param context context
     */
    public PhoneConfigurationSettings(Context context) {
        this(context, null);
    }

    /**.
     * Constructor
     * @param context context
     * @param attrs attribute set
     */
    public PhoneConfigurationSettings(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        mUspManager = (UspManager) mContext.getSystemService(Context.USP_SERVICE);
        setTitle(R.string.preferred_phone_configuration_title);
        setDialogTitle(R.string.preferred_phone_configuration_dialogtitle);
        setOnPreferenceChangeListener(this);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
            mSelectedPhoneConfig = (String) newValue;
            //NOTE onPreferenceChange seems to be called even if there is no change
            //Check if the value is already selected
            if (getValue().equals(mSelectedPhoneConfig)) {
                Log.d(TAG, "onPreferenceChange: pref not changed, so return:"
                        + mSelectedPhoneConfig);
                return true;
            }
            Log.d(TAG, "onPreferenceChange: set mSelectedPhoneConfig " + mSelectedPhoneConfig);
            String message = mContext.getResources().getString(R.string
                    .preferred_phone_configuration_dialog_message,
                    getEntries()[findIndexOfValue(mSelectedPhoneConfig)]);
            AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
            builder.setMessage(message)
                    .setIconAttribute(android.R.attr.alertDialogIcon)
                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            handleCarrierConfigChange();
                        }
                    })
                    .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            setValue(mUspManager.getActiveOpPack());
                        }
                    })
                    .setOnCancelListener(new DialogInterface.OnCancelListener() {
                        public void onCancel(DialogInterface dialog) {
                            setValue(mUspManager.getActiveOpPack());
                        }
                    });
            mCarrierConfigAlertDialog = builder.show();
        return true;
    }


    /**.
     * For UniService feature: provide user listPreference to select operator
     * @return
     */
    public void initPreference() {
        Map<String, String> opList = mUspManager.getAllOpPackList();
        Log.d(TAG, "opList:" + opList);

        if (opList != null) {
            TelephonyManager telephonyManager =
                    (TelephonyManager) mContext.getSystemService(Context.TELEPHONY_SERVICE);
            String mccMnc = telephonyManager.getSimOperatorNumericForPhone(SubscriptionManager
                    .getDefaultVoicePhoneId());
            // Operator Id of operator whose sim is present
            String currentCarrierId = mUspManager.getOpPackFromSimInfo(mccMnc);
            Log.d(TAG, "mccMnc:" + mccMnc + ",currentCarrierId:" + currentCarrierId);
            int size;
            String selectedOpPackId = mUspManager.getActiveOpPack();
            Log.d(TAG, "selectedOpPackId: " + selectedOpPackId);
            if (selectedOpPackId == null || selectedOpPackId.isEmpty()) {
                size = opList.size() + 1;
            } else {
                size = opList.size();
            }
            CharSequence[] choices = new CharSequence[size];
            CharSequence[] values = new CharSequence[size];
            // Need to fill arrays like this, cz map is unordered so extracted key array
            //may be unsynced with value array
            int index = 0;
            for (Map.Entry<String, String> pair : opList.entrySet()) {
                String choice = pair.getValue();
                if (currentCarrierId != null && currentCarrierId.equals(pair.getKey())) {
                    choice += mContext.getResources().getString(R.string.recommended);
                }
                // OP ID
                values[index] = pair.getKey();
                // OP Name
                choices[index] = choice;
                Log.d(TAG, "value[" + index + "]: " + values[index]
                        + "-->Choice[" + index + "]: " + choices[index]);
                index++;
            }
            //String selectedOpPackId = mUspManager.getActiveOpPack();
            //Log.d(TAG, "selectedOpPackId: " + selectedOpPackId);
            if (selectedOpPackId == null || selectedOpPackId.isEmpty()) {
                values[index] = OM_PACKAGE_VALUE;
                choices[index] = mContext.getResources().getString(R.string.om_package_name);
                selectedOpPackId = OM_PACKAGE_VALUE;
            }
            setEntries(choices);
            setEntryValues(values);
            setValue(selectedOpPackId);
            setSummary(getEntry());
            boolean isCallStateIdle = !TelecomManager.from(mContext).isInCall();
            Log.d(TAG, "isCallStateIdle:" + isCallStateIdle);
            this.setEnabled(isCallStateIdle);
        } else {
            this.setEnabled(false);
        }
        IntentFilter filter = new IntentFilter();
        filter.addAction(TelephonyManager.ACTION_PHONE_STATE_CHANGED);
        mContext.registerReceiver(mIntentReceiver, filter);
    }

    /**.
     * For UniService feature: deinit
     * @return
     */
    public void deinitPreference() {
        mContext.unregisterReceiver(mIntentReceiver);
    }

    private void handleCarrierConfigChange() {
        Log.d(TAG, "new value:" + mSelectedPhoneConfig);
        setValue(mSelectedPhoneConfig);
        Log.d(TAG, "entry:" + getEntry());
        Log.d(TAG, "value:" + getValue());
        mUspManager.setOpPackActive(getValue().toString());
        setSummary(getEntry());
    }
}

