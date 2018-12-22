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
 * limitations under the License.
 */

package com.android.settings;

import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.os.SystemProperties;
import android.support.v14.preference.SwitchPreference;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.Preference.OnPreferenceClickListener;
import android.support.v7.preference.PreferenceScreen;
import android.telephony.CarrierConfigManager;
import android.telecom.TelecomManager;
import android.telephony.PhoneStateListener;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TabHost;
import android.widget.TabHost.OnTabChangeListener;
import android.widget.TabHost.TabContentFactory;
import android.widget.TabHost.TabSpec;
import android.widget.TabWidget;
import android.widget.TextView;
import android.widget.Toast;

import com.android.ims.ImsConfig;
import com.android.ims.ImsException;
import com.android.ims.ImsManager;
import com.android.internal.logging.MetricsLogger;
import com.android.internal.logging.MetricsProto.MetricsEvent;
import com.android.internal.telephony.Phone;
import com.android.internal.telephony.PhoneConstants;
import com.android.settings.widget.SwitchBar;

import com.mediatek.settings.UtilsExt;
import com.mediatek.settings.ext.DefaultWfcSettingsExt;
import com.mediatek.settings.ext.IWfcSettingsExt;
import com.mediatek.settings.sim.SimHotSwapHandler;
import com.mediatek.settings.sim.SimHotSwapHandler.OnSimHotSwapListener;
import com.mediatek.settings.sim.TelephonyUtils;


/**
 * "Wi-Fi Calling settings" screen.  This preference screen lets you
 * enable/disable Wi-Fi Calling and change Wi-Fi Calling mode.
 */
public class WifiCallingSettings extends SettingsPreferenceFragment
        implements SwitchBar.OnSwitchChangeListener,
        Preference.OnPreferenceChangeListener {

    private static final String TAG = "WifiCallingSettings";

    //String keys for preference lookup
    private static final String BUTTON_WFC_MODE = "wifi_calling_mode";
    private static final String BUTTON_WFC_ROAMING_MODE = "wifi_calling_roaming_mode";
    private static final String PREFERENCE_EMERGENCY_ADDRESS = "emergency_address_key";

    private static final int REQUEST_CHECK_WFC_EMERGENCY_ADDRESS = 1;

    public static final String EXTRA_LAUNCH_CARRIER_APP = "EXTRA_LAUNCH_CARRIER_APP";

    public static final int LAUCH_APP_ACTIVATE = 0;
    public static final int LAUCH_APP_UPDATE = 1;

    //UI objects
    //private SwitchBar mSwitchBar;
    //private Switch mSwitch;
    private ListPreference mButtonWfcMode;
    private ListPreference mButtonWfcRoamingMode;
    private Preference mUpdateAddress;
    private TextView mEmptyView;

    //private boolean mValidListener = false;
    private boolean mEditableWfcMode = true;
    private boolean mEditableWfcRoamingMode = true;
    /// M: for operator customization @{
    private boolean mRemoveWfcPreferenceMode;
    /// @}

    /// M: Wfc plugin
    IWfcSettingsExt mWfcExt;

    /// M: for 03044866, should not show the dialog every time Activity resumed
    private boolean mAlertAlreadyShowed = false;

    /// M: fix Google bug: only listen to default sub, listen to Phone state change instead @{
    /*
    private final PhoneStateListener mPhoneStateListener = new PhoneStateListener() {
    */
         /*
         * Enable/disable controls when in/out of a call and depending on
         * TTY mode and TTY support over VoLTE.
         * @see android.telephony.PhoneStateListener#onCallStateChanged(int,
         * java.lang.String)
         */
    /*
        @Override
        public void onCallStateChanged(int state, String incomingNumber) {
            final SettingsActivity activity = (SettingsActivity) getActivity();
            boolean isNonTtyOrTtyOnVolteEnabled = ImsManager
                    .isNonTtyOrTtyOnVolteEnabled(activity);
            final SwitchBar switchBar = activity.getSwitchBar();
            boolean isWfcEnabled = switchBar.getSwitch().isChecked()
                    && isNonTtyOrTtyOnVolteEnabled;

            switchBar.setEnabled((state == TelephonyManager.CALL_STATE_IDLE)
                    && isNonTtyOrTtyOnVolteEnabled);

            boolean isWfcModeEditable = true;
            boolean isWfcRoamingModeEditable = false;
            final CarrierConfigManager configManager = (CarrierConfigManager)
                    activity.getSystemService(Context.CARRIER_CONFIG_SERVICE);
            if (configManager != null) {
                PersistableBundle b = configManager.getConfig();
                if (b != null) {
                    isWfcModeEditable = b.getBoolean(
                            CarrierConfigManager.KEY_EDITABLE_WFC_MODE_BOOL);
                    isWfcRoamingModeEditable = b.getBoolean(
                            CarrierConfigManager.KEY_EDITABLE_WFC_ROAMING_MODE_BOOL);
                }
            }

            Preference pref = getPreferenceScreen().findPreference(BUTTON_WFC_MODE);
            if (pref != null) {
                pref.setEnabled(isWfcEnabled && isWfcModeEditable
                        && (state == TelephonyManager.CALL_STATE_IDLE));
            }
            Preference pref_roam = getPreferenceScreen().findPreference(BUTTON_WFC_ROAMING_MODE);
            if (pref_roam != null) {
                pref_roam.setEnabled(isWfcEnabled && isWfcRoamingModeEditable
                        && (state == TelephonyManager.CALL_STATE_IDLE));
            }
        }
    };
    */
    /// @}

    private final OnPreferenceClickListener mUpdateAddressListener =
            new OnPreferenceClickListener() {
                /*
                 * Launch carrier emergency address managemnent activity
                 */
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    final Context context = getActivity();
                    Intent carrierAppIntent = getCarrierActivityIntent(context);
                    if (carrierAppIntent != null) {
                        carrierAppIntent.putExtra(EXTRA_LAUNCH_CARRIER_APP, LAUCH_APP_UPDATE);
                        startActivity(carrierAppIntent);
                    }
                    return true;
                }
    };

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        final SettingsActivity activity = (SettingsActivity) getActivity();

        //mSwitchBar = activity.getSwitchBar();
        //mSwitch = mSwitchBar.getSwitch();
        //mSwitchBar.show();

        mEmptyView = (TextView) getView().findViewById(android.R.id.empty);
        setEmptyView(mEmptyView);
        mEmptyView.setText(R.string.wifi_calling_off_explanation);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        //mSwitchBar.hide();
    }

    private void showAlert(Intent intent) {
        Context context = getActivity();

        CharSequence title = intent.getCharSequenceExtra(Phone.EXTRA_KEY_ALERT_TITLE);
        CharSequence message = intent.getCharSequenceExtra(Phone.EXTRA_KEY_ALERT_MESSAGE);

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage(message)
                .setTitle(title)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setPositiveButton(android.R.string.ok, null);
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private IntentFilter mIntentFilter;

    private BroadcastReceiver mIntentReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Log.d(TAG, "onReceive()... " + action);
            if (action.equals(ImsManager.ACTION_IMS_REGISTRATION_ERROR)) {
                // If this fragment is active then we are immediately
                // showing alert on screen. There is no need to add
                // notification in this case.
                //
                // In order to communicate to ImsPhone that it should
                // not show notification, we are changing result code here.
                setResultCode(Activity.RESULT_CANCELED);

                // UX requirement is to disable WFC in case of "permanent" registration failures.
                mSwitchPref.setChecked(false);

                showAlert(intent);
            /// M: listen to WFC config changes and update the screen @{
            } else if (action.equals(CarrierConfigManager.ACTION_CARRIER_CONFIG_CHANGED)) {
                int phoneId = getCurrentPhoneId();
                if (phoneId != INVALID_PHONE_ID
                        && !ImsManager.isWfcEnabledByPlatform(context, phoneId)
                        && !TelephonyUtils.isSupportMims()) {
                    Log.d(TAG, "carrier config changed, finish WFC activity");
                    getActivity().finish();
                }
            } else if (action.equals(TelephonyManager.ACTION_PHONE_STATE_CHANGED)) {
                int phoneId = getCurrentPhoneId();
                if (phoneId != INVALID_PHONE_ID
                        && ImsManager.isWfcEnabledByPlatform(context, phoneId)) {
                    updateEnabledState(phoneId);
                }
            }
            /// @}
        }
    };

    @Override
    protected int getMetricsCategory() {
        return MetricsEvent.WIFI_CALLING;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.wifi_calling_settings);

        /// M: for plug-in, make wfc setting plugin & add custom preferences @{
        mWfcExt = UtilsExt.getWfcSettingsPlugin(getActivity());
        mWfcExt.initPlugin(this);
        /// @}

        mButtonWfcMode = (ListPreference) findPreference(BUTTON_WFC_MODE);
        mButtonWfcMode.setOnPreferenceChangeListener(this);

        mButtonWfcRoamingMode = (ListPreference) findPreference(BUTTON_WFC_ROAMING_MODE);
        mButtonWfcRoamingMode.setOnPreferenceChangeListener(this);

        mUpdateAddress = (Preference) findPreference(PREFERENCE_EMERGENCY_ADDRESS);
        mUpdateAddress.setOnPreferenceClickListener(mUpdateAddressListener);

        TelephonyManager tm = (TelephonyManager) this.getSystemService(Context.TELEPHONY_SERVICE);
        if (tm.getSimState() == TelephonyManager.SIM_STATE_ABSENT) {
            getPreferenceScreen().removePreference(mButtonWfcRoamingMode);
            getPreferenceScreen().removePreference(mUpdateAddress);
        }

        /// M: L+L feature
        mSelectableSubInfos = SubscriptionManager.from(getContext())
                .getActiveSubscriptionInfoList();
        mSwitchPref = (SwitchPreference) findPreference(ENABLE_WFC);
        mSwitchPref.setOnPreferenceChangeListener(this);

        /// M: Add custom preferences & listener/register/observers for these preferences
        mWfcExt.addOtherCustomPreference();

        mIntentFilter = new IntentFilter();
        mIntentFilter.addAction(ImsManager.ACTION_IMS_REGISTRATION_ERROR);
        /// M: listen to Carrier config changes
        mIntentFilter.addAction(CarrierConfigManager.ACTION_CARRIER_CONFIG_CHANGED);
        mIntentFilter.addAction(TelephonyManager.ACTION_PHONE_STATE_CHANGED);

        /* M: L+L feature, move to onResume
        CarrierConfigManager configManager = (CarrierConfigManager)
                getSystemService(Context.CARRIER_CONFIG_SERVICE);
        boolean isWifiOnlySupported = true;
        if (configManager != null) {
            PersistableBundle b = configManager.getConfig();
            if (b != null) {
                mEditableWfcMode = b.getBoolean(CarrierConfigManager.KEY_EDITABLE_WFC_MODE_BOOL);
                mEditableWfcRoamingMode = b.getBoolean(
                        CarrierConfigManager.KEY_EDITABLE_WFC_ROAMING_MODE_BOOL);
                isWifiOnlySupported = b.getBoolean(
                        CarrierConfigManager.KEY_CARRIER_WFC_SUPPORTS_WIFI_ONLY_BOOL, true);
                mRemoveWfcPreferenceMode = b.getBoolean(
                        CarrierConfigManager.KEY_WFC_REMOVE_PREFERENCE_MODE_BOOL, false);
                Log.d(TAG, "WFC mRemoveWfcPreferenceMode" + mRemoveWfcPreferenceMode);
            }
        }

        if (!isWifiOnlySupported) {
            mButtonWfcMode.setEntries(R.array.wifi_calling_mode_choices_without_wifi_only);
            mButtonWfcMode.setEntryValues(R.array.wifi_calling_mode_values_without_wifi_only);
            mButtonWfcRoamingMode.setEntries(
                    R.array.wifi_calling_mode_choices_v2_without_wifi_only);
            mButtonWfcRoamingMode.setEntryValues(
                    R.array.wifi_calling_mode_values_without_wifi_only);
        }*/

        /// M: for [SIM Hot Swap] @{
        mSimHotSwapHandler = new SimHotSwapHandler(getActivity());
        mSimHotSwapHandler.registerOnSimHotSwap(new OnSimHotSwapListener() {
            @Override
            public void onSimHotSwap() {
                Log.d(TAG, "onSimHotSwap, finish Activity~~");
                finish();
            }
        });

        mWfcExt.onWfcSettingsEvent(DefaultWfcSettingsExt.CREATE);

    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume");
        final Context context = getActivity();

        /// M: L+L feature
        if (mSir != null) {
            int phoneId = SubscriptionManager.getPhoneId(
                    mSir.getSubscriptionId());
            //if (ImsManager.isWfcEnabledByPlatform(context, phoneId)) {
                /// M: fix Google bug: only listen to default sub, @{
                // listen to Phone state change instead
                /*
                TelephonyManager tm = (TelephonyManager)
                        getSystemService(Context.TELEPHONY_SERVICE);
                tm.listen(mPhoneStateListener, PhoneStateListener.LISTEN_CALL_STATE);
                */
                /// @}
                //mSwitchBar.addOnSwitchChangeListener(this);

                //mValidListener = true;
            //}
            /// M: Move from onCreate to onResume
            CarrierConfigManager configManager = (CarrierConfigManager)
                    getSystemService(Context.CARRIER_CONFIG_SERVICE);
            boolean isWifiOnlySupported = true;
            if (configManager != null) {
                PersistableBundle b = configManager.getConfigForSubId(mSir.getSubscriptionId());
                if (b != null) {
                    mEditableWfcMode = b.getBoolean(
                            CarrierConfigManager.KEY_EDITABLE_WFC_MODE_BOOL);
                    mEditableWfcRoamingMode = b.getBoolean(
                            CarrierConfigManager.KEY_EDITABLE_WFC_ROAMING_MODE_BOOL);
                    isWifiOnlySupported = b.getBoolean(
                            CarrierConfigManager.KEY_CARRIER_WFC_SUPPORTS_WIFI_ONLY_BOOL, true);
                    mRemoveWfcPreferenceMode = b.getBoolean(
                            CarrierConfigManager.KEY_WFC_REMOVE_PREFERENCE_MODE_BOOL, false);
                    Log.d(TAG, "WFC mRemoveWfcPreferenceMode" + mRemoveWfcPreferenceMode +
                               "\nmEditableWfcMode:" + mEditableWfcMode +
                               "\nmEditableWfcRoamingMode:" + "\nmEditableWfcRoamingMode" +
                               "\nisWifiOnlySupported:" + isWifiOnlySupported);
                }
            }
            if (!isWifiOnlySupported) {
                mButtonWfcMode.setEntries(R.array.wifi_calling_mode_choices_without_wifi_only);
                mButtonWfcMode.setEntryValues(R.array.wifi_calling_mode_values_without_wifi_only);
                mButtonWfcRoamingMode.setEntries(
                        R.array.wifi_calling_mode_choices_v2_without_wifi_only);
                mButtonWfcRoamingMode.setEntryValues(
                        R.array.wifi_calling_mode_values_without_wifi_only);
            }

            updateScreen(phoneId);
        } else {
            final PreferenceScreen preferenceScreen = getPreferenceScreen();
            preferenceScreen.setEnabled(false);
            mSwitchPref.setEnabled(false);
        }
        context.registerReceiver(mIntentReceiver, mIntentFilter);

        Intent intent = getActivity().getIntent();
        /// M: for 03044866, should not show the dialog every time Activity resumed @{
        if (intent.getBooleanExtra(Phone.EXTRA_KEY_ALERT_SHOW, false) && !mAlertAlreadyShowed) {
            showAlert(intent);
            mAlertAlreadyShowed = true;
        }
        /// @}

        /// M: for plug-in
        mWfcExt.onWfcSettingsEvent(DefaultWfcSettingsExt.RESUME);
    }

    @Override
    public void onPause() {
        super.onPause();

        final Context context = getActivity();

        //if (mValidListener) {
            //mValidListener = false;
            /// M: fix Google bug: only listen to default sub, @{
            // listen to Phone state change instead
            /*
            TelephonyManager tm = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
            tm.listen(mPhoneStateListener, PhoneStateListener.LISTEN_NONE);
            */
            /// @}
            //mSwitchBar.removeOnSwitchChangeListener(this);
        //}

        context.unregisterReceiver(mIntentReceiver);

        mWfcExt.onWfcSettingsEvent(DefaultWfcSettingsExt.PAUSE);
    }

    @Override
    public void onDestroy() {
        mSimHotSwapHandler.unregisterOnSimHotSwap();
        mWfcExt.onWfcSettingsEvent(DefaultWfcSettingsExt.DESTROY);
        super.onDestroy();
    }

    /**
     * Listens to the state change of the switch.
     */
    @Override
    public void onSwitchChanged(Switch switchView, boolean isChecked) {
        /* M: L+L feature
        Log.d(TAG, "OnSwitchChanged");
        /// M:  Decide whether wfc switch is to be toggled or not @{
        // Revert user action with toast, if IMS is enabling or disabling
        if (isInSwitchProcess()) {
            Log.d(TAG, "[onClick] Switching process ongoing");
            Toast.makeText(getActivity(), R.string.Switch_not_in_use_string, Toast.LENGTH_SHORT)
                    .show();
            mSwitchBar.setChecked(!isChecked);
            return;
        }
        /// @}

        final Context context = getActivity();
        Log.d(TAG, "onSwitchChanged(" + isChecked + ")");

        if (!isChecked) {
            updateWfcMode(context, false);
            return;
        }

        // Call address management activity before turning on WFC
        Intent carrierAppIntent = getCarrierActivityIntent(context);
        if (carrierAppIntent != null) {
            carrierAppIntent.putExtra(EXTRA_LAUNCH_CARRIER_APP, LAUCH_APP_ACTIVATE);
            startActivityForResult(carrierAppIntent, REQUEST_CHECK_WFC_EMERGENCY_ADDRESS);
        } else {
            updateWfcMode(context, true);
        }*/
    }

    /*
     * Get the Intent to launch carrier emergency address management activity.
     * Return null when no activity found.
     */
    private static Intent getCarrierActivityIntent(Context context) {
        // Retrive component name from carrirt config
        CarrierConfigManager configManager = context.getSystemService(CarrierConfigManager.class);
        if (configManager == null) return null;

        PersistableBundle bundle = configManager.getConfig();
        if (bundle == null) return null;

        String carrierApp = bundle.getString(
                CarrierConfigManager.KEY_WFC_EMERGENCY_ADDRESS_CARRIER_APP_STRING);

        if (TextUtils.isEmpty(carrierApp)) return null;

        ComponentName componentName = ComponentName.unflattenFromString(carrierApp);
        if (componentName == null) return null;

        // Build and return intent
        Intent intent = new Intent();
        intent.setComponent(componentName);
        return intent;
    }

    /*
     * Turn on/off WFC mode with ImsManager and update UI accordingly
     */
    private void updateWfcMode(Context context, boolean wfcEnabled) {
        int phoneId = getCurrentPhoneId();
        Log.i(TAG, "updateWfcMode(" + wfcEnabled + ")" + " phoneId = " + phoneId);
        if (phoneId != INVALID_PHONE_ID) {
            ImsManager.setWfcSetting(context, wfcEnabled, phoneId);

            int wfcMode = ImsManager.getWfcMode(context, false, phoneId);
            int wfcRoamingMode = ImsManager.getWfcMode(context, true, phoneId);
            updateButtonWfcMode(context, wfcEnabled, wfcMode, wfcRoamingMode, phoneId);
            /// M: for plug-in
            mWfcExt.updateWfcModePreference(
                    getPreferenceScreen(), mButtonWfcMode, wfcEnabled, wfcMode);

            if (wfcEnabled) {
                MetricsLogger.action(getActivity(), getMetricsCategory(), wfcMode);
            } else {
                MetricsLogger.action(getActivity(), getMetricsCategory(), -1);
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        final Context context = getActivity();

        if (requestCode == REQUEST_CHECK_WFC_EMERGENCY_ADDRESS) {
            Log.d(TAG, "WFC emergency address activity result = " + resultCode);

            if (resultCode == Activity.RESULT_OK) {
                updateWfcMode(context, true);
            }
        }
    }

    private void updateButtonWfcMode(Context context, boolean wfcEnabled,
                                     int wfcMode, int wfcRoamingMode, int phoneId) {
        Log.d(TAG, "updateButtonWfcMode wfcEnabled = " + wfcEnabled);
        mButtonWfcMode.setSummary(getWfcModeSummary(context, wfcMode, phoneId));
        mButtonWfcMode.setEnabled(wfcEnabled && mEditableWfcMode);
        // mButtonWfcRoamingMode.setSummary is not needed; summary is just selected value.
        mButtonWfcRoamingMode.setEnabled(wfcEnabled && mEditableWfcRoamingMode);

        final PreferenceScreen preferenceScreen = getPreferenceScreen();
        boolean updateAddressEnabled = (getCarrierActivityIntent(context) != null);
        if (wfcEnabled) {
            if (mEditableWfcMode) {
                preferenceScreen.addPreference(mButtonWfcMode);
            } else {
                // Don't show WFC (home) preference if it's not editable.
                preferenceScreen.removePreference(mButtonWfcMode);
            }
            if (mEditableWfcRoamingMode) {
                preferenceScreen.addPreference(mButtonWfcRoamingMode);
            } else {
                // Don't show WFC roaming preference if it's not editable.
                preferenceScreen.removePreference(mButtonWfcRoamingMode);
            }
            if (updateAddressEnabled) {
                preferenceScreen.addPreference(mUpdateAddress);
            } else {
                preferenceScreen.removePreference(mUpdateAddress);
            }
            /// M: for operator customization @{
            if (mRemoveWfcPreferenceMode) {
                preferenceScreen.removePreference(mButtonWfcMode);
            }
            /// @}
        } else {
            preferenceScreen.removePreference(mButtonWfcMode);
            preferenceScreen.removePreference(mButtonWfcRoamingMode);
            preferenceScreen.removePreference(mUpdateAddress);
        }
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        final Context context = getActivity();
        int phoneId = SubscriptionManager.getPhoneId(
                mSir.getSubscriptionId());
        Log.d(TAG, "onPreferenceChange phoneId = " + phoneId);
        if (preference == mButtonWfcMode) {
            if (TelecomManager.from(context).isInCall()) {
                return false;
            }
            mButtonWfcMode.setValue((String) newValue);
            int buttonMode = Integer.valueOf((String) newValue);

            int currentWfcMode = ImsManager.getWfcMode(context, false, phoneId);
            if (buttonMode != currentWfcMode) {
                ImsManager.setWfcMode(context, buttonMode, false, phoneId);
                mButtonWfcMode.setSummary(getWfcModeSummary(context, buttonMode, phoneId));
                MetricsLogger.action(getActivity(), getMetricsCategory(), buttonMode);
            }
            if (!mEditableWfcRoamingMode) {
                int currentWfcRoamingMode = ImsManager.getWfcMode(context, true, phoneId);
                if (buttonMode != currentWfcRoamingMode) {
                    ImsManager.setWfcMode(context, buttonMode, true, phoneId);
                    // mButtonWfcRoamingMode.setSummary is not needed; summary is selected value
                }
            }
        } else if (preference == mButtonWfcRoamingMode) {
            mButtonWfcRoamingMode.setValue((String) newValue);
            int buttonMode = Integer.valueOf((String) newValue);
            int currentMode = ImsManager.getWfcMode(context, true, phoneId);
            if (buttonMode != currentMode) {
                ImsManager.setWfcMode(context, buttonMode, true, phoneId);
                // mButtonWfcRoamingMode.setSummary is not needed; summary is just selected value.
                MetricsLogger.action(getActivity(), getMetricsCategory(), buttonMode);
            }
        }  else if (preference == mSwitchPref) { /// M: L+L feature
            /// M:  Decide whether wfc switch is to be toggled or not @{
            /* Revert user action with toast, if IMS is enabling or disabling */
            boolean isChecked = (Boolean) newValue;
            Log.d(TAG, "onPreferenceChange isChecked = " + isChecked);
            if (isInSwitchProcess(phoneId)) {
                Log.d(TAG, "[onClick] Switching process ongoing");
                Toast.makeText(getActivity(), R.string.Switch_not_in_use_string, Toast.LENGTH_SHORT)
                        .show();
                return false;
            }
            /// @}

            Log.d(TAG, "onSwitchChanged(" + isChecked + ")");

            if (!isChecked) {
                updateWfcMode(context, false);
                return true;
            }

            // Call address management activity before turning on WFC
            Intent carrierAppIntent = getCarrierActivityIntent(context);
            if (carrierAppIntent != null) {
                carrierAppIntent.putExtra(EXTRA_LAUNCH_CARRIER_APP, LAUCH_APP_ACTIVATE);
                startActivityForResult(carrierAppIntent, REQUEST_CHECK_WFC_EMERGENCY_ADDRESS);
            } else {
                updateWfcMode(context, true);
            }
        }
        return true;
    }

    static int getWfcModeSummary(Context context, int wfcMode, int phoneId) {
        int resId = com.android.internal.R.string.wifi_calling_off_summary;
        Log.d(TAG, "getWfcModeSummary , wfcMode = " + wfcMode);
        if (ImsManager.isWfcEnabledByUser(context, phoneId)) {
            switch (wfcMode) {
                case ImsConfig.WfcModeFeatureValueConstants.WIFI_ONLY:
                    resId = com.android.internal.R.string.wfc_mode_wifi_only_summary;
                    break;
                case ImsConfig.WfcModeFeatureValueConstants.CELLULAR_PREFERRED:
                    resId = com.android.internal.R.string.wfc_mode_cellular_preferred_summary;
                    break;
                case ImsConfig.WfcModeFeatureValueConstants.WIFI_PREFERRED:
                    resId = com.android.internal.R.string.wfc_mode_wifi_preferred_summary;
                    break;
                default:
                    Log.e(TAG, "Unexpected WFC mode value: " + wfcMode);
            }
        }
        return resId;
    }
    ///----------------------------------------MTK-----------------------------------------------
    /// M: @{
    /* Is IMS enabling or disabling */
    private boolean isInSwitchProcess(int phoneId) {
        int imsState = PhoneConstants.IMS_STATE_DISABLED;
        try {
         imsState = ImsManager.getInstance(getActivity(), phoneId).getImsState();
        } catch (ImsException e) {
           return false;
        }
        Log.d(TAG, "isInSwitchProcess , imsState = " + imsState);
        return imsState == PhoneConstants.IMS_STATE_DISABLING
                || imsState == PhoneConstants.IMS_STATE_ENABLING;
    }
    /// @}

    private void updateEnabledState(int phoneId) {
        final SettingsActivity activity = (SettingsActivity) getActivity();
        if (activity == null) {
            return;
        }
        boolean isNonTtyOrTtyOnVolteEnabled = ImsManager
                .isNonTtyOrTtyOnVolteEnabled(activity, phoneId);
        boolean isWfcEnabled = mSwitchPref.isChecked() && isNonTtyOrTtyOnVolteEnabled;

        boolean isCallStateIdle = !TelecomManager.from(activity).isInCall();
        Log.d(TAG, "isWfcEnabled: " + isWfcEnabled
                + ", isCallStateIdle: " + isCallStateIdle);
        mSwitchPref.setEnabled(isCallStateIdle && isNonTtyOrTtyOnVolteEnabled);

        Preference pref = getPreferenceScreen().findPreference(BUTTON_WFC_MODE);
        if (pref != null) {
            pref.setEnabled(isWfcEnabled && isCallStateIdle);
        }
    }

    /// M: L+L feature @{
    private static final String ENABLE_WFC = "enable_wifi_calling";
    private static final String SUB_ID = "sub_id";
    private static final int INVALID_PHONE_ID = -1;

    private TabHost mTabHost;
    private TabWidget mTabWidget;
    private ListView mListView;
    private List<SubscriptionInfo> mSelectableSubInfos;
    private SubscriptionInfo mSir;
    private SwitchPreference mSwitchPref;
    private SimHotSwapHandler mSimHotSwapHandler;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView");
        if (mSelectableSubInfos == null) {
            mSir = null;
        } else {
            Log.d(TAG, "mSelectableSubInfos size: " + mSelectableSubInfos.size());
            if (mSelectableSubInfos.size() > 0) {
                final Intent intent = getIntent();
                final String action = intent.getAction();
                int subId;
                if (TelephonyUtils.isSupportMims()) {
                    subId = intent.getIntExtra(SUB_ID,
                           SubscriptionManager.INVALID_SUBSCRIPTION_ID);
                } else {
                    subId = SubscriptionManager.getSubIdUsingPhoneId(TelephonyUtils
                                                                    .getMainCapabilityPhoneId());
                }

                Log.d(TAG, "Current view subId:" + subId);
                if (SubscriptionManager.isValidSubscriptionId(subId)) {
                    for (SubscriptionInfo subInfo : mSelectableSubInfos) {
                        if (subInfo.getSubscriptionId() == subId) {
                            mSir = subInfo;
                        }
                    }
                } else {
                    mSir = mSelectableSubInfos.get(0);
                }
            }
            if (TelephonyUtils.isSupportMims() && mSelectableSubInfos.size() > 1) {
                View view = inflater.inflate(R.layout.wifi_calling_tabs, container, false);
                final ViewGroup prefsContainer = (ViewGroup) view.findViewById(
                        R.id.prefs_container);
                Utils.prepareCustomPreferencesList(container, view, prefsContainer, false);
                View prefs = super.onCreateView(inflater, prefsContainer, savedInstanceState);
                prefsContainer.addView(prefs);

                mTabHost = (TabHost) view.findViewById(android.R.id.tabhost);
                mTabWidget = (TabWidget) view.findViewById(android.R.id.tabs);
                mListView = (ListView) view.findViewById(android.R.id.list);

                mTabHost.setup();
                mTabHost.setOnTabChangedListener(mTabListener);
                mTabHost.clearAllTabs();
                mTabHost.setCurrentTab(mSir.getSimSlotIndex());

                for (int i = 0; i < mSelectableSubInfos.size(); i++) {
                    mTabHost.addTab(buildTabSpec(String.valueOf(i),
                            String.valueOf(mSelectableSubInfos.get(i).getDisplayName())));
                }
                return view;
            }
        }
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    private OnTabChangeListener mTabListener = new OnTabChangeListener() {
        @Override
        public void onTabChanged(String tabId) {
            Log.d(TAG, "onTabChange, tabId = " + tabId);
            final int slotId = Integer.parseInt(tabId);
            mSir = mSelectableSubInfos.get(slotId);
            Log.d(TAG, "Current subId: " + mSir.getSubscriptionId());
            CarrierConfigManager configManager = (CarrierConfigManager)
                    getSystemService(Context.CARRIER_CONFIG_SERVICE);
            boolean isWifiOnlySupported = true;
            if (configManager != null) {
                PersistableBundle b = configManager.getConfigForSubId(mSir.getSubscriptionId());
                if (b != null) {
                    mEditableWfcMode = b.getBoolean(
                            CarrierConfigManager.KEY_EDITABLE_WFC_MODE_BOOL);
                    mEditableWfcRoamingMode = b.getBoolean(
                            CarrierConfigManager.KEY_EDITABLE_WFC_ROAMING_MODE_BOOL);
                    isWifiOnlySupported = b.getBoolean(
                            CarrierConfigManager.KEY_CARRIER_WFC_SUPPORTS_WIFI_ONLY_BOOL, true);
                    mRemoveWfcPreferenceMode = b.getBoolean(
                            CarrierConfigManager.KEY_WFC_REMOVE_PREFERENCE_MODE_BOOL, false);
                    Log.d(TAG, "WFC mRemoveWfcPreferenceMode" + mRemoveWfcPreferenceMode +
                               "\nmEditableWfcMode:" + mEditableWfcMode +
                               "\nmEditableWfcRoamingMode:" + "\nmEditableWfcRoamingMode" +
                               "\nisWifiOnlySupported:" + isWifiOnlySupported);
                }
            }
            if (!isWifiOnlySupported) {
                mButtonWfcMode.setEntries(R.array.wifi_calling_mode_choices_without_wifi_only);
                mButtonWfcMode.setEntryValues(R.array.wifi_calling_mode_values_without_wifi_only);
                mButtonWfcRoamingMode.setEntries(
                        R.array.wifi_calling_mode_choices_v2_without_wifi_only);
                mButtonWfcRoamingMode.setEntryValues(
                        R.array.wifi_calling_mode_values_without_wifi_only);
            }
            updateScreen(slotId);
        }
    };

    private TabContentFactory mEmptyTabContent = new TabContentFactory() {
        @Override
        public View createTabContent(String tag) {
            return new View(mTabHost.getContext());
        }
    };

    private TabSpec buildTabSpec(String tag, String title) {
        return mTabHost.newTabSpec(tag).setIndicator(title).setContent(
                mEmptyTabContent);
    }

    private void updateScreen(int phoneId) {
        final Context context = getActivity();
        boolean isWfcEnabledByPlatform = ImsManager.isWfcEnabledByPlatform(context, phoneId);
        boolean isWfcProvisioned = ImsManager.isWfcProvisionedOnDevice(context);
        Log.d(TAG, "[UpdateScreen] isWfcEnabledByPlatform:" + isWfcEnabledByPlatform + "\n" +
                 "isWfcProvisioned:" + isWfcProvisioned);
        if (TelephonyUtils.isSupportMims() && !(isWfcEnabledByPlatform && isWfcProvisioned)) {
            final PreferenceScreen preferenceScreen = getPreferenceScreen();
            mSwitchPref.setChecked(false);
            mSwitchPref.setEnabled(false);
            preferenceScreen.removePreference(mButtonWfcMode);
            preferenceScreen.removePreference(mButtonWfcRoamingMode);
            preferenceScreen.removePreference(mUpdateAddress);
            return;
        }
        boolean wfcEnabled = ImsManager.isWfcEnabledByUser(context, phoneId)
                && ImsManager.isNonTtyOrTtyOnVolteEnabled(context, phoneId);
        mSwitchPref.setChecked(wfcEnabled);
        int wfcMode = ImsManager.getWfcMode(context, false, phoneId);
        int wfcRoamingMode = ImsManager.getWfcMode(context, true, phoneId);
        mButtonWfcMode.setValue(Integer.toString(wfcMode));
        mButtonWfcRoamingMode.setValue(Integer.toString(wfcRoamingMode));
        Log.d(TAG, "updateScreen, phoneId = " + phoneId + " wfcMode = "
                + wfcMode + " wfcEnabled = " + wfcEnabled);
        updateButtonWfcMode(context, wfcEnabled, wfcMode, wfcRoamingMode, phoneId);
        updateEnabledState(phoneId);
        mWfcExt.updateWfcModePreference(getPreferenceScreen(), mButtonWfcMode, wfcEnabled, wfcMode);
    }

    private int getCurrentPhoneId() {
        int phoneId = INVALID_PHONE_ID;
        if (mSir != null) {
            phoneId = SubscriptionManager.getPhoneId(mSir.getSubscriptionId());
        }
        Log.d(TAG, "Current PhoneId: " + phoneId);
        return phoneId;
    }
    /// L+L feature @}
}
