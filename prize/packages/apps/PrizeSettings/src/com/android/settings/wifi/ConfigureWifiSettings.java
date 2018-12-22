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
package com.android.settings.wifi;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.net.NetworkScoreManager;
import android.net.NetworkScorerAppManager;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.UserManager;
import android.provider.Settings;
import android.support.v14.preference.SwitchPreference;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.Preference;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;
import com.android.internal.logging.MetricsProto.MetricsEvent;
import com.android.settings.AppListSwitchPreference;
import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.Utils;

import com.mediatek.settings.FeatureOption;
import com.mediatek.settings.UtilsExt;
import com.mediatek.settings.ext.ISettingsMiscExt;
import com.mediatek.wifi.ConfigureWifiSettingsExt;

import java.util.Collection;
import java.util.List;
import android.security.Credentials;
import android.support.v7.preference.Preference.OnPreferenceClickListener;
import android.net.wifi.WpsInfo;
import android.app.Dialog;
import android.app.DialogFragment;
import com.android.settings.DividerPreference;
public class ConfigureWifiSettings extends SettingsPreferenceFragment
        implements Preference.OnPreferenceChangeListener {
    private static final String TAG = "ConfigureWifiSettings";

    private static final String KEY_MAC_ADDRESS = "mac_address";
    private static final String KEY_SAVED_NETWORKS = "saved_networks";
    private static final String KEY_CURRENT_IP_ADDRESS = "current_ip_address";
    private static final String KEY_NOTIFY_OPEN_NETWORKS = "notify_open_networks";
    private static final String KEY_SLEEP_POLICY = "sleep_policy";
    private static final String KEY_CELLULAR_FALLBACK = "wifi_cellular_data_fallback";
    private static final String KEY_WIFI_ASSISTANT = "wifi_assistant";
	private static final String KEY_INSTALL_CREDENTIALS = "install_credentials";
    private static final String KEY_WIFI_DIRECT = "wifi_direct";
    private static final String KEY_WPS_PUSH = "wps_push_button";
    private static final String KEY_WPS_PIN = "wps_pin_entry";

    private WifiManager mWifiManager;
    private NetworkScoreManager mNetworkScoreManager;
    private AppListSwitchPreference mWifiAssistantPreference;
    private IntentFilter mFilter;
    private ISettingsMiscExt mExt;

    /// M: Sleep policy change & Notify open networks bug fix
    private ConfigureWifiSettingsExt mConfigureWifiSettingsExt;

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        addPreferencesFromResource(R.xml.wifi_configure_settings);
		final Context context = getActivity();
		getActivity().setTitle(context.getString(R.string.wifi_menu_advanced));
        /// M: Sleep policy change & Notify open networks bug fix @{
        mConfigureWifiSettingsExt = new ConfigureWifiSettingsExt(this);
        mConfigureWifiSettingsExt.onCreate();
        /// @}
        mExt = UtilsExt.getMiscPlugin(this.getActivity());
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mWifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        mFilter = new IntentFilter();
        mFilter.addAction(WifiManager.LINK_CONFIGURATION_CHANGED_ACTION);
        mFilter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
        mNetworkScoreManager =
                (NetworkScoreManager) getSystemService(Context.NETWORK_SCORE_SERVICE);

        /// M: Sleep policy change & Notify open networks bug fix
        mConfigureWifiSettingsExt.onActivityCreated(getContentResolver());
    }

    @Override
    public void onResume() {
        super.onResume();
        initPreferences();
        getActivity().registerReceiver(mReceiver, mFilter);

        /// M: Sleep policy change & Notify open networks bug fix
        mConfigureWifiSettingsExt.onResume();
        refreshWifiInfo();
    }

    @Override
    public void onPause() {
        super.onPause();
        getActivity().unregisterReceiver(mReceiver);

        /// M: Sleep policy change & Notify open networks bug fix
        mConfigureWifiSettingsExt.onPause();
    }

    private void initPreferences() {
        List<WifiConfiguration> configs = mWifiManager.getConfiguredNetworks();
        if (configs == null || configs.size() == 0) {
            removePreference(KEY_SAVED_NETWORKS);
        }

        SwitchPreference notifyOpenNetworks =
                (SwitchPreference) findPreference(KEY_NOTIFY_OPEN_NETWORKS);
        notifyOpenNetworks.setChecked(Settings.Global.getInt(getContentResolver(),
                Settings.Global.WIFI_NETWORKS_AVAILABLE_NOTIFICATION_ON, 0) == 1);
        notifyOpenNetworks.setEnabled(mWifiManager.isWifiEnabled());

        final Context context = getActivity();
        if (avoidBadWifiConfig()) {
            // Hide preference toggle, always avoid bad wifi networks.
            removePreference(KEY_CELLULAR_FALLBACK);
        } else {
            // Show preference toggle, initialized based on current settings value.
            boolean currentSetting = avoidBadWifiCurrentSettings();
            SwitchPreference pref = (SwitchPreference) findPreference(KEY_CELLULAR_FALLBACK);
            // TODO: can this ever be null? The return value of avoidBadWifiConfig() can only
            // change if the resources change, but if that happens the activity will be recreated...
            if (pref != null) {
                pref.setChecked(currentSetting);
            }
        }
        mWifiAssistantPreference = (AppListSwitchPreference) findPreference(KEY_WIFI_ASSISTANT);
        Collection<NetworkScorerAppManager.NetworkScorerAppData> scorers =
                NetworkScorerAppManager.getAllValidScorers(context);
        if (UserManager.get(context).isAdminUser() && !scorers.isEmpty()) {
            mWifiAssistantPreference.setOnPreferenceChangeListener(this);
            initWifiAssistantPreference(scorers);
        } else if (mWifiAssistantPreference != null) {
            getPreferenceScreen().removePreference(mWifiAssistantPreference);
        }

        ListPreference sleepPolicyPref = (ListPreference) findPreference(KEY_SLEEP_POLICY);
        if (sleepPolicyPref != null) {
            if (Utils.isWifiOnly(context)) {
                sleepPolicyPref.setEntries(R.array.wifi_sleep_policy_entries_wifi_only);
            }
            sleepPolicyPref.setOnPreferenceChangeListener(this);
            int value = Settings.Global.getInt(getContentResolver(),
                    Settings.Global.WIFI_SLEEP_POLICY,
                    Settings.Global.WIFI_SLEEP_POLICY_NEVER);
            String stringValue = String.valueOf(value);
            sleepPolicyPref.setValue(stringValue);
            updateSleepPolicySummary(sleepPolicyPref, stringValue);
        }

        Intent intent = new Intent(Credentials.INSTALL_AS_USER_ACTION);
        intent.setClassName("com.android.certinstaller",
                "com.android.certinstaller.CertInstallerMain");
        intent.putExtra(Credentials.EXTRA_INSTALL_AS_UID, android.os.Process.WIFI_UID);
        Preference pref = findPreference(KEY_INSTALL_CREDENTIALS);
        pref.setIntent(intent);


        Intent wifiDirectIntent = new Intent(context,
                com.android.settings.Settings.WifiP2pSettingsActivity.class);
        Preference wifiDirectPref = findPreference(KEY_WIFI_DIRECT);
        wifiDirectPref.setIntent(wifiDirectIntent);

        // WpsDialog: Create the dialog like WifiSettings does.
        Preference wpsPushPref = findPreference(KEY_WPS_PUSH);
        wpsPushPref.setOnPreferenceClickListener(new OnPreferenceClickListener() {
                public boolean onPreferenceClick(Preference arg0) {
                    WpsFragment wpsFragment = new WpsFragment(WpsInfo.PBC);
                    wpsFragment.show(getFragmentManager(), KEY_WPS_PUSH);
                    return true;
                }
        });

        // WpsDialog: Create the dialog like WifiSettings does.
        Preference wpsPinPref = findPreference(KEY_WPS_PIN);
        wpsPinPref.setOnPreferenceClickListener(new OnPreferenceClickListener(){
                public boolean onPreferenceClick(Preference arg0) {
                    WpsFragment wpsFragment = new WpsFragment(WpsInfo.DISPLAY);
                    wpsFragment.show(getFragmentManager(), KEY_WPS_PIN);
                    return true;
                }
        });
    }

    private void updateSleepPolicySummary(Preference sleepPolicyPref, String value) {
        if (value != null) {
            String[] values = getResources().getStringArray(R.array.wifi_sleep_policy_values);
            final int summaryArrayResId = Utils.isWifiOnly(getActivity()) ?
                    R.array.wifi_sleep_policy_entries_wifi_only : R.array.wifi_sleep_policy_entries;
            String[] summaries = getResources().getStringArray(summaryArrayResId);
            for (int i = 0; i < values.length; i++) {
                if (value.equals(values[i])) {
                    if (i < summaries.length) {
                        sleepPolicyPref.setSummary(summaries[i]);
                        return;
                    }
                }
            }
        }

        sleepPolicyPref.setSummary("");
        Log.e(TAG, "Invalid sleep policy value: " + value);
    }

    private boolean avoidBadWifiConfig() {
        return getActivity().getResources().getInteger(
                com.android.internal.R.integer.config_networkAvoidBadWifi) == 1;
    }

    private boolean avoidBadWifiCurrentSettings() {
        return "1".equals(Settings.Global.getString(getContentResolver(),
                Settings.Global.NETWORK_AVOID_BAD_WIFI));
    }
    @Override
    public boolean onPreferenceTreeClick(Preference preference) {
        String key = preference.getKey();

        if (KEY_NOTIFY_OPEN_NETWORKS.equals(key)) {
            Settings.Global.putInt(getContentResolver(),
                    Settings.Global.WIFI_NETWORKS_AVAILABLE_NOTIFICATION_ON,
                    ((SwitchPreference) preference).isChecked() ? 1 : 0);
        } else if (KEY_CELLULAR_FALLBACK.equals(key)) {
            // On: avoid bad wifi. Off: prompt.
            String settingName = Settings.Global.NETWORK_AVOID_BAD_WIFI;
            Settings.Global.putString(getContentResolver(), settingName,
                    ((SwitchPreference) preference).isChecked() ? "1" : null);
        } else {
            return super.onPreferenceTreeClick(preference);
        }
        return true;
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        final Context context = getActivity();
        String key = preference.getKey();

        if (KEY_WIFI_ASSISTANT.equals(key)) {
            NetworkScorerAppManager.NetworkScorerAppData wifiAssistant =
                    NetworkScorerAppManager.getScorer(context, (String) newValue);
            if (wifiAssistant == null) {
                mNetworkScoreManager.setActiveScorer(null);
                return true;
            }

            Intent intent = new Intent();
            if (wifiAssistant.mConfigurationActivityClassName != null) {
                // App has a custom configuration activity; launch that.
                // This custom activity will be responsible for launching the system
                // dialog.
                intent.setClassName(wifiAssistant.mPackageName,
                        wifiAssistant.mConfigurationActivityClassName);
            } else {
                // Fall back on the system dialog.
                intent.setAction(NetworkScoreManager.ACTION_CHANGE_ACTIVE);
                intent.putExtra(NetworkScoreManager.EXTRA_PACKAGE_NAME,
                        wifiAssistant.mPackageName);
            }

            startActivity(intent);
            // Don't update the preference widget state until the child activity returns.
            // It will be updated in onResume after the activity finishes.
            return false;
        }

        if (KEY_SLEEP_POLICY.equals(key)) {
            try {
                String stringValue = (String) newValue;
                Settings.Global.putInt(getContentResolver(), Settings.Global.WIFI_SLEEP_POLICY,
                        Integer.parseInt(stringValue));
                updateSleepPolicySummary(preference, stringValue);
            } catch (NumberFormatException e) {
                Toast.makeText(context, R.string.wifi_setting_sleep_policy_error,
                        Toast.LENGTH_SHORT).show();
                return false;
            }
        }

        return true;
    }

    private void refreshWifiInfo() {
        final Context context = getActivity();
        WifiInfo wifiInfo = mWifiManager.getConnectionInfo();

        DividerPreference wifiMacAddressPref = (DividerPreference)findPreference(KEY_MAC_ADDRESS);
        String macAddress = wifiInfo == null ? null : wifiInfo.getMacAddress();
        /// M: for CT wifi MAC address name show feature.
        wifiMacAddressPref.setSummary(!TextUtils.isEmpty(macAddress)
                ? mExt.customizeMacAddressString(macAddress,
                        context.getString(R.string.status_unavailable))
                : context.getString(R.string.status_unavailable));
        wifiMacAddressPref.setSelectable(false);

        /// M: DHCPV6C feature @{
        if (FeatureOption.MTK_DHCPV6C_WIFI) {
            mConfigureWifiSettingsExt.refreshWifiInfo();
        } else {
            Preference wifiIpAddressPref = findPreference(KEY_CURRENT_IP_ADDRESS);
            String ipAddress = Utils.getWifiIpAddresses(context);
            wifiIpAddressPref.setSummary(ipAddress == null ?
                    context.getString(R.string.status_unavailable) : ipAddress);
            wifiIpAddressPref.setSelectable(false);
        }
        /// @}
    }

    private void initWifiAssistantPreference(
            Collection<NetworkScorerAppManager.NetworkScorerAppData> scorers) {
        int count = scorers.size();
        String[] packageNames = new String[count];
        int i = 0;
        for (NetworkScorerAppManager.NetworkScorerAppData scorer : scorers) {
            packageNames[i] = scorer.mPackageName;
            i++;
        }
        mWifiAssistantPreference.setPackageNames(packageNames,
                mNetworkScoreManager.getActiveScorerPackage());
    }

    @Override
    protected int getMetricsCategory() {
        return MetricsEvent.CONFIGURE_WIFI;
    }

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(WifiManager.LINK_CONFIGURATION_CHANGED_ACTION) ||
                action.equals(WifiManager.NETWORK_STATE_CHANGED_ACTION)) {
                refreshWifiInfo();
            }
        }
    };

	 /* Wrapper class for the WPS dialog to properly handle life cycle events like rotation. */
    public static class WpsFragment extends DialogFragment {
        private static int mWpsSetup;

        // Public default constructor is required for rotation.
        public WpsFragment() {
            super();
        }

        public WpsFragment(int wpsSetup) {
            super();
            mWpsSetup = wpsSetup;
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            return new WpsDialog(getActivity(), mWpsSetup);
        }
    }
}
