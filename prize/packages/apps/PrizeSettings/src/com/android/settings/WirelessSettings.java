/*
 * Copyright (C) 2009 The Android Open Source Project
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


import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.admin.DevicePolicyManager;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.nfc.NfcAdapter;
import android.nfc.NfcManager;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.os.UserManager;
import android.provider.SearchIndexableResource;
import android.provider.Settings;
import android.support.v14.preference.SwitchPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceScreen;
import android.telephony.CarrierConfigManager;
import android.telephony.PhoneStateListener;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;

import com.android.ims.ImsManager;
import com.android.internal.logging.MetricsProto.MetricsEvent;
import com.android.internal.telephony.TelephonyIntents;
import com.android.internal.telephony.TelephonyProperties;
import com.android.settings.nfc.NfcEnabler;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settings.search.Indexable;
import com.android.settingslib.RestrictedLockUtils;
import com.android.settingslib.RestrictedPreference;

import com.mediatek.settings.ext.DefaultWfcSettingsExt;
import com.mediatek.settings.ext.IRCSSettings;
import com.mediatek.settings.ext.ISettingsMiscExt;
import com.mediatek.settings.ext.IWWOPJoynSettingsExt;
import com.mediatek.settings.ext.IWfcSettingsExt;
import com.mediatek.settings.FeatureOption;
import com.mediatek.settings.PhoneConfigurationSettings;
import com.mediatek.settings.UtilsExt;
import com.mediatek.settings.ext.IRCSSettings;
import com.mediatek.settings.sim.TelephonyUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class WirelessSettings extends SettingsPreferenceFragment implements Indexable {
    private static final String TAG = "WirelessSettings";

    private static final String KEY_TOGGLE_AIRPLANE = "toggle_airplane";
    private static final String KEY_TOGGLE_NFC = "toggle_nfc";
    /// M: Add MTK nfc seting @{
    private static final String KEY_MTK_TOGGLE_NFC = "toggle_mtk_nfc";
    private static final String ACTION_MTK_NFC = "mediatek.settings.NFC_SETTINGS";
    /// @}
    private static final String KEY_WIMAX_SETTINGS = "wimax_settings";
    private static final String KEY_ANDROID_BEAM_SETTINGS = "android_beam_settings";
    private static final String KEY_VPN_SETTINGS = "vpn_settings";
    private static final String KEY_TETHER_SETTINGS = "tether_settings";
    private static final String KEY_PROXY_SETTINGS = "proxy_settings";
    private static final String KEY_MOBILE_NETWORK_SETTINGS = "mobile_network_settings";
    private static final String KEY_MANAGE_MOBILE_PLAN = "manage_mobile_plan";
    private static final String KEY_WFC_SETTINGS = "wifi_calling_settings";
    private static final String KEY_NETWORK_RESET = "network_reset";

    public static final String EXIT_ECM_RESULT = "exit_ecm_result";
    public static final int REQUEST_CODE_EXIT_ECM = 1;
    private static final String SUB_ID = "sub_id";
    
    // Modify by zhudaopeng at 2016-11-15 Start
    // private AirplaneModeEnabler mAirplaneModeEnabler;
    // private SwitchPreference mAirplaneModePreference;
    // Modify by zhudaopeng at 2016-11-15 Start
    
    private NfcEnabler mNfcEnabler;
    private NfcAdapter mNfcAdapter;

    private ConnectivityManager mCm;
    private TelephonyManager mTm;
    private PackageManager mPm;
    private UserManager mUm;

    private static final int MANAGE_MOBILE_PLAN_DIALOG_ID = 1;
    private static final String SAVED_MANAGE_MOBILE_PLAN_MSG = "mManageMobilePlanMessage";

    private PreferenceScreen mButtonWfc;

    /// M: Added for  UniService Pack setting @{
    private PhoneConfigurationSettings mButtonPreferredPhoneConfiguration;
    /// @}

    /// M: RCSE key&intent @{
    private static final String RCSE_SETTINGS_INTENT = "com.mediatek.rcse.RCSE_SETTINGS";
    private static final String KEY_RCSE_SETTINGS = "rcse_settings";
    /// @}

    private boolean mRemoveWfcPreferenceMode;
    /// M: Wfc plugin @{
    IWfcSettingsExt mWfcExt;
    /// @}


    /**
     * Invoked on each preference click in this hierarchy, overrides
     * PreferenceFragment's implementation.  Used to make sure we track the
     * preference click events.
     */
    @Override
    public boolean onPreferenceTreeClick(Preference preference) {
        log("onPreferenceTreeClick: preference=" + preference);
        /// M: The property may be a conbination string of "true" and "false".
        boolean isInECMMode = SystemProperties.get(TelephonyProperties.PROPERTY_INECM_MODE)
                                .contains("true");
        /// M: for 02894779, should not start exit ECM dialog in guest mode
        // Modify by zhudaopeng at 2016-11-15 Start
        // if (preference == mAirplaneModePreference && isInECMMode && UserHandle.myUserId() == 0) {
        //     // In ECM mode launch ECM app dialog
        //     startActivityForResult(
        //         new Intent(TelephonyIntents.ACTION_SHOW_NOTICE_ECM_BLOCK_OTHERS, null),
        //         REQUEST_CODE_EXIT_ECM);
        //     return true;
        // } else 
        // Modify by zhudaopeng at 2016-11-15 End
        if (preference == findPreference(KEY_MANAGE_MOBILE_PLAN)) {
            onManageMobilePlanClick();
        }
        // Let the intents be launched by the Preference manager
        return super.onPreferenceTreeClick(preference);
    }

    private String mManageMobilePlanMessage;
    public void onManageMobilePlanClick() {
        log("onManageMobilePlanClick:");
        mManageMobilePlanMessage = null;
        Resources resources = getActivity().getResources();

        NetworkInfo ni = mCm.getActiveNetworkInfo();
        if (mTm.hasIccCard() && (ni != null)) {
            // Check for carrier apps that can handle provisioning first
            Intent provisioningIntent = new Intent(TelephonyIntents.ACTION_CARRIER_SETUP);
            List<String> carrierPackages =
                    mTm.getCarrierPackageNamesForIntent(provisioningIntent);
            if (carrierPackages != null && !carrierPackages.isEmpty()) {
                if (carrierPackages.size() != 1) {
                    Log.w(TAG, "Multiple matching carrier apps found, launching the first.");
                }
                provisioningIntent.setPackage(carrierPackages.get(0));
                startActivity(provisioningIntent);
                return;
            }

            // Get provisioning URL
            String url = mCm.getMobileProvisioningUrl();
            if (!TextUtils.isEmpty(url)) {
                Intent intent = Intent.makeMainSelectorActivity(Intent.ACTION_MAIN,
                        Intent.CATEGORY_APP_BROWSER);
                intent.setData(Uri.parse(url));
                intent.setFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT |
                        Intent.FLAG_ACTIVITY_NEW_TASK);
                try {
                    startActivity(intent);
                } catch (ActivityNotFoundException e) {
                    Log.w(TAG, "onManageMobilePlanClick: startActivity failed" + e);
                }
            } else {
                // No provisioning URL
                String operatorName = mTm.getSimOperatorName();
                if (TextUtils.isEmpty(operatorName)) {
                    // Use NetworkOperatorName as second choice in case there is no
                    // SPN (Service Provider Name on the SIM). Such as with T-mobile.
                    operatorName = mTm.getNetworkOperatorName();
                    if (TextUtils.isEmpty(operatorName)) {
                        mManageMobilePlanMessage = resources.getString(
                                R.string.mobile_unknown_sim_operator);
                    } else {
                        mManageMobilePlanMessage = resources.getString(
                                R.string.mobile_no_provisioning_url, operatorName);
                    }
                } else {
                    mManageMobilePlanMessage = resources.getString(
                            R.string.mobile_no_provisioning_url, operatorName);
                }
            }
        } else if (mTm.hasIccCard() == false) {
            // No sim card
            mManageMobilePlanMessage = resources.getString(R.string.mobile_insert_sim_card);
        } else {
            // NetworkInfo is null, there is no connection
            mManageMobilePlanMessage = resources.getString(R.string.mobile_connect_to_internet);
        }
        if (!TextUtils.isEmpty(mManageMobilePlanMessage)) {
            log("onManageMobilePlanClick: message=" + mManageMobilePlanMessage);
            showDialog(MANAGE_MOBILE_PLAN_DIALOG_ID);
        }
    }

    @Override
    public Dialog onCreateDialog(int dialogId) {
        log("onCreateDialog: dialogId=" + dialogId);
        switch (dialogId) {
            case MANAGE_MOBILE_PLAN_DIALOG_ID:
                return new AlertDialog.Builder(getActivity())
                            .setMessage(mManageMobilePlanMessage)
                            .setCancelable(false)
                            .setPositiveButton(com.android.internal.R.string.ok,
                                    new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int id) {
                                    log("MANAGE_MOBILE_PLAN_DIALOG.onClickListener id=" + id);
                                    mManageMobilePlanMessage = null;
                                }
                            })
                            .create();
        }
        return super.onCreateDialog(dialogId);
    }

    private void log(String s) {
        Log.d(TAG, s);
    }

    @Override
    protected int getMetricsCategory() {
        return MetricsEvent.WIRELESS;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            mManageMobilePlanMessage = savedInstanceState.getString(SAVED_MANAGE_MOBILE_PLAN_MSG);
        }
        log("onCreate: mManageMobilePlanMessage=" + mManageMobilePlanMessage);

        mCm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        mTm = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        mPm = getPackageManager();
        mUm = (UserManager) getSystemService(Context.USER_SERVICE);

        addPreferencesFromResource(R.xml.wireless_settings);
        /* Add by zhudaopeng at 2016-12-05 Start */
        getActivity().setTitle(getString(R.string.other_wireless_settings_title));
        /* Add by zhudaopeng at 2016-12-05 End */

        final boolean isAdmin = mUm.isAdminUser();

        final Activity activity = getActivity();
        // Modify by zhudaopeng at 2016-11-15 Start
        // mAirplaneModePreference = (SwitchPreference) findPreference(KEY_TOGGLE_AIRPLANE);
        // Modify by zhudaopeng at 2016-11-15 End
        SwitchPreference nfc = (SwitchPreference) findPreference(KEY_TOGGLE_NFC);

        RestrictedPreference androidBeam = (RestrictedPreference) findPreference(
                KEY_ANDROID_BEAM_SETTINGS);
        /// M: Get MTK NFC setting preference
        PreferenceScreen mtkNfc = (PreferenceScreen) findPreference(KEY_MTK_TOGGLE_NFC);
        
        // Modify by zhudaopeng at 2016-11-15 Start
        // mAirplaneModeEnabler = new AirplaneModeEnabler(activity, mAirplaneModePreference);
        // Modify by zhudaopeng at 2016-11-15 End

        mNfcEnabler = new NfcEnabler(activity, nfc, androidBeam);

        mButtonWfc = (PreferenceScreen) findPreference(KEY_WFC_SETTINGS);
        /* prize-add-by-lijimeng-for bugid 52470-20180312-start */
        getPreferenceScreen().removePreference(mButtonWfc);
        /* prize-add-by-lijimeng-for bugid 52470-20180312-end */
        /// M: Uniservice Pack @ {
        if (!"no".equals(SystemProperties.get("ro.mtk_carrierexpress_pack", "no"))
                && SystemProperties.getInt("ro.mtk_cxp_switch_mode", 0) != 2) {
            mButtonPreferredPhoneConfiguration = new PhoneConfigurationSettings(activity);
            getPreferenceScreen().addPreference(mButtonPreferredPhoneConfiguration);
            //mButtonPreferredPhoneConfiguration.init();
        }
        /// @}

        String toggleable = Settings.Global.getString(activity.getContentResolver(),
                Settings.Global.AIRPLANE_MODE_TOGGLEABLE_RADIOS);

        //enable/disable wimax depending on the value in config.xml
        final boolean isWimaxEnabled = isAdmin && this.getResources().getBoolean(
                com.android.internal.R.bool.config_wimaxEnabled);
        if (!isWimaxEnabled || RestrictedLockUtils.hasBaseUserRestriction(activity,
                UserManager.DISALLOW_CONFIG_MOBILE_NETWORKS, UserHandle.myUserId())) {
            PreferenceScreen root = getPreferenceScreen();
            Preference ps = findPreference(KEY_WIMAX_SETTINGS);
            if (ps != null) root.removePreference(ps);
        } else {
            if (toggleable == null || !toggleable.contains(Settings.Global.RADIO_WIMAX )
                    && isWimaxEnabled) {
                Preference ps = findPreference(KEY_WIMAX_SETTINGS);
                ps.setDependency(KEY_TOGGLE_AIRPLANE);
            }
        }

        // Manually set dependencies for Wifi when not toggleable.
        if (toggleable == null || !toggleable.contains(Settings.Global.RADIO_WIFI)) {
            findPreference(KEY_VPN_SETTINGS).setDependency(KEY_TOGGLE_AIRPLANE);
        }
        // Disable VPN.
        // TODO: http://b/23693383
        if (!isAdmin || RestrictedLockUtils.hasBaseUserRestriction(activity,
                UserManager.DISALLOW_CONFIG_VPN, UserHandle.myUserId())) {
            removePreference(KEY_VPN_SETTINGS);
        }

        // Manually set dependencies for Bluetooth when not toggleable.
        if (toggleable == null || !toggleable.contains(Settings.Global.RADIO_BLUETOOTH)) {
            // No bluetooth-dependent items in the list. Code kept in case one is added later.
        }

        // Manually set dependencies for NFC when not toggleable.
        if (toggleable == null || !toggleable.contains(Settings.Global.RADIO_NFC)) {
            findPreference(KEY_TOGGLE_NFC).setDependency(KEY_TOGGLE_AIRPLANE);
            findPreference(KEY_ANDROID_BEAM_SETTINGS).setDependency(KEY_TOGGLE_AIRPLANE);
            /// M: Manually set dependencies for NFC
            findPreference(KEY_MTK_TOGGLE_NFC).setDependency(KEY_TOGGLE_AIRPLANE);
        }

        // Remove NFC if not available
        mNfcAdapter = NfcAdapter.getDefaultAdapter(activity);
        if (mNfcAdapter == null) {
            getPreferenceScreen().removePreference(nfc);
            getPreferenceScreen().removePreference(androidBeam);
            mNfcEnabler = null;
            /// M: Remove MTK NFC setting
            getPreferenceScreen().removePreference(mtkNfc);
        } else {
            /// M: Remove NFC duplicate items @{
            if (FeatureOption.MTK_NFC_ADDON_SUPPORT) {
                getPreferenceScreen().removePreference(nfc);
                getPreferenceScreen().removePreference(androidBeam);
                mNfcEnabler = null;
            } else {
                getPreferenceScreen().removePreference(mtkNfc);
            }
            /// @}
        }

        // Remove Mobile Network Settings and Manage Mobile Plan for secondary users,
        // if it's a wifi-only device.
        if (!isAdmin || Utils.isWifiOnly(getActivity()) ||
                RestrictedLockUtils.hasBaseUserRestriction(activity,
                        UserManager.DISALLOW_CONFIG_MOBILE_NETWORKS, UserHandle.myUserId())) {
            removePreference(KEY_MOBILE_NETWORK_SETTINGS);
            removePreference(KEY_MANAGE_MOBILE_PLAN);
        }
        // Remove Mobile Network Settings and Manage Mobile Plan
        // if config_show_mobile_plan sets false.
        final boolean isMobilePlanEnabled = this.getResources().getBoolean(
                R.bool.config_show_mobile_plan);
        if (!isMobilePlanEnabled) {
            Preference pref = findPreference(KEY_MANAGE_MOBILE_PLAN);
            if (pref != null) {
                removePreference(KEY_MANAGE_MOBILE_PLAN);
            }
        }

        // Remove Airplane Mode settings if it's a stationary device such as a TV.
        if (mPm.hasSystemFeature(PackageManager.FEATURE_TELEVISION)) {
            removePreference(KEY_TOGGLE_AIRPLANE);
        }

        // Enable Proxy selector settings if allowed.
        Preference mGlobalProxy = findPreference(KEY_PROXY_SETTINGS);
        final DevicePolicyManager mDPM = (DevicePolicyManager)
                activity.getSystemService(Context.DEVICE_POLICY_SERVICE);
        // proxy UI disabled until we have better app support
        getPreferenceScreen().removePreference(mGlobalProxy);
        mGlobalProxy.setEnabled(mDPM.getGlobalProxyAdmin() == null);

        // Disable Tethering if it's not allowed or if it's a wifi-only device
        final ConnectivityManager cm =
                (ConnectivityManager) activity.getSystemService(Context.CONNECTIVITY_SERVICE);

        final boolean adminDisallowedTetherConfig = RestrictedLockUtils.checkIfRestrictionEnforced(
                activity, UserManager.DISALLOW_CONFIG_TETHERING, UserHandle.myUserId()) != null;
        if ((!cm.isTetheringSupported() && !adminDisallowedTetherConfig) ||
                RestrictedLockUtils.hasBaseUserRestriction(activity,
                        UserManager.DISALLOW_CONFIG_TETHERING, UserHandle.myUserId())) {
            getPreferenceScreen().removePreference(findPreference(KEY_TETHER_SETTINGS));
        } else if (!adminDisallowedTetherConfig) {
            Preference p = findPreference(KEY_TETHER_SETTINGS);
            p.setTitle(com.android.settingslib.Utils.getTetheringLabel(cm));

            // Grey out if provisioning is not available.
            p.setEnabled(!TetherSettings
                    .isProvisioningNeededButUnavailable(getActivity()));
        }

        // Remove network reset if not allowed
        if (RestrictedLockUtils.hasBaseUserRestriction(activity,
                UserManager.DISALLOW_NETWORK_RESET, UserHandle.myUserId())) {
            removePreference(KEY_NETWORK_RESET);
        }
        /// M: Remove the entrance if WWOP RCSE not support. @{
        IWWOPJoynSettingsExt joynExt = UtilsExt.getJoynSettingsPlugin(getActivity());
        if (joynExt.isJoynSettingsEnabled()) {
            Log.d(TAG, RCSE_SETTINGS_INTENT + " is enabled");
            Intent intent = new Intent(RCSE_SETTINGS_INTENT);
            findPreference(KEY_RCSE_SETTINGS).setIntent(intent);
        } else {
            Log.d(TAG, RCSE_SETTINGS_INTENT + " is not enabled");
            getPreferenceScreen().removePreference(findPreference(KEY_RCSE_SETTINGS));
        }
        /// @}
        /// M: add the entrance RCS switch. @{
        IRCSSettings rcsExt = UtilsExt.getRcsSettingsPlugin(getActivity());
        rcsExt.addRCSPreference(getActivity(), getPreferenceScreen());
        /// @}

        /// M: for plug-in, make wfc setting plug-in @{
        mWfcExt = UtilsExt.getWfcSettingsPlugin(getActivity());
        /// @}
    }

    @Override
    public void onResume() {
        super.onResume();

        // Modify by zhudaopeng at 2016-11-15 Start
        // mAirplaneModeEnabler.resume();
        // Modify by zhudaopeng at 2016-11-15 End
        if (mNfcEnabler != null) {
            mNfcEnabler.resume();
        }

        // update WFC setting
        final Context context = getActivity();
        mWfcExt.initPlugin(this);
        ///M : Remove wifi calling preference when monkey running
        if (ImsManager.isWfcEnabledByPlatform(context) &&
                ImsManager.isWfcProvisionedOnDevice(context) &&
                !Utils.isMonkeyRunning()) {
            if (mWfcExt.isWifiCallingProvisioned(context,
                SubscriptionManager.getDefaultVoicePhoneId())) {
                 /* prize-add-by-lijimeng-for bugid 52470-20180312-start */
              //  getPreferenceScreen().addPreference(mButtonWfc);
                 /* prize-add-by-lijimeng-for bugid 52470-20180312-end */

                //mButtonWfc.setSummary(WifiCallingSettings.getWfcModeSummary(
                //        context, ImsManager.getWfcMode(context, mTm.isNetworkRoaming())));
                /// M: L+L feature
                setWfcModeSummary();
                /// M: for plug-in
                //mButtonWfc.setSummary(mWfcExt.getWfcSummary(context,
                //        WifiCallingSettings.getWfcModeSummary(context,
                //        ImsManager.getWfcMode(context))));
                mWfcExt.customizedWfcPreference(getActivity(), getPreferenceScreen());
                /// M: for operator customization @{
                if (mRemoveWfcPreferenceMode) {
                   mButtonWfc.setSummary(null);
                }
            }
        } else {
            if (!TelephonyUtils.isSupportMims()) {
               removePreference(KEY_WFC_SETTINGS);
            }
        }

        /// M: @{
        TelephonyManager telephonyManager =
            (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        telephonyManager.listen(mPhoneStateListener, PhoneStateListener.LISTEN_CALL_STATE);

        updateMobileNetworkEnabled();

        if (!"no".equals(SystemProperties.get("ro.mtk_carrierexpress_pack", "no"))
                && SystemProperties.getInt("ro.mtk_cxp_switch_mode", 0) != 2) {
            mButtonPreferredPhoneConfiguration.initPreference();
        } else {
            removePreference("preferred_phone_configuration_key");
        }

        IntentFilter intentFilter =
            new IntentFilter(TelephonyIntents.ACTION_SUBINFO_RECORD_UPDATED);
        // listen to Carrier config change
        intentFilter.addAction(CarrierConfigManager.ACTION_CARRIER_CONFIG_CHANGED);
        getActivity().registerReceiver(mReceiver, intentFilter);
        /// @}

        /// M: WFC: get customized intent filter @{
        mWfcExt.onWirelessSettingsEvent(DefaultWfcSettingsExt.RESUME);
        /// @}
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        if (!TextUtils.isEmpty(mManageMobilePlanMessage)) {
            outState.putString(SAVED_MANAGE_MOBILE_PLAN_MSG, mManageMobilePlanMessage);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        // Modify by zhudaopeng at 2016-11-15 Start
        // mAirplaneModeEnabler.pause();
        // Modify by zhudaopeng at 2016-11-15 End
        if (mNfcEnabler != null) {
            mNfcEnabler.pause();
        }

        /// M:  @{
        TelephonyManager telephonyManager =
            (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        telephonyManager.listen(mPhoneStateListener, PhoneStateListener.LISTEN_NONE);

        getActivity().unregisterReceiver(mReceiver);

        if (!"no".equals(SystemProperties.get("ro.mtk_carrierexpress_pack", "no"))
                && SystemProperties.getInt("ro.mtk_cxp_switch_mode", 0) != 2) {
            mButtonPreferredPhoneConfiguration.deinitPreference();
        }

        mWfcExt.onWirelessSettingsEvent(DefaultWfcSettingsExt.PAUSE);
        /// @}
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE_EXIT_ECM) {
            Boolean isChoiceYes = data.getBooleanExtra(EXIT_ECM_RESULT, false);
            // Set Airplane mode based on the return value and checkbox state
            
            // Modify by zhudaopeng at 2016-11-15 Start
            // mAirplaneModeEnabler.setAirplaneModeInECM(isChoiceYes,
            //        mAirplaneModePreference.isChecked());
            // Modify by zhudaopeng at 2016-11-15 End
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected int getHelpResource() {
        return R.string.help_url_more_networks;
    }

    /**
     * For Search.
     */
    public static final Indexable.SearchIndexProvider SEARCH_INDEX_DATA_PROVIDER =
        new BaseSearchIndexProvider() {
            @Override
            public List<SearchIndexableResource> getXmlResourcesToIndex(
                    Context context, boolean enabled) {
                // Remove wireless settings from search in demo mode
                if (UserManager.isDeviceInDemoMode(context)) {
                    return Collections.emptyList();
                }
                SearchIndexableResource sir = new SearchIndexableResource(context);
                sir.xmlResId = R.xml.wireless_settings;
                return Arrays.asList(sir);
            }

            @Override
            public List<String> getNonIndexableKeys(Context context) {
                final ArrayList<String> result = new ArrayList<String>();

                final UserManager um = (UserManager) context.getSystemService(Context.USER_SERVICE);
                final boolean isSecondaryUser = !um.isAdminUser();
                final boolean isWimaxEnabled = !isSecondaryUser
                        && context.getResources().getBoolean(
                        com.android.internal.R.bool.config_wimaxEnabled);
                if (!isWimaxEnabled) {
                    result.add(KEY_WIMAX_SETTINGS);
                }

                if (isSecondaryUser) { // Disable VPN
                    result.add(KEY_VPN_SETTINGS);
                }

                // Remove NFC if not available
                final NfcManager manager = (NfcManager)
                        context.getSystemService(Context.NFC_SERVICE);
                if (manager != null) {
                    NfcAdapter adapter = manager.getDefaultAdapter();
                    if (adapter == null) {
                        result.add(KEY_TOGGLE_NFC);
                        result.add(KEY_ANDROID_BEAM_SETTINGS);
                        /// M: Remove MTK NFC setting
                        result.add(KEY_MTK_TOGGLE_NFC);
                    } else {
                        /// M: Remove NFC duplicate items @{
                        if (FeatureOption.MTK_NFC_ADDON_SUPPORT) {
                            result.add(KEY_TOGGLE_NFC);
                            result.add(KEY_ANDROID_BEAM_SETTINGS);
                        } else {
                            result.add(KEY_MTK_TOGGLE_NFC);
                        }
                        /// @}
                    }
                }

                // Remove Mobile Network Settings and Manage Mobile Plan if it's a wifi-only device.
                if (isSecondaryUser || Utils.isWifiOnly(context)) {
                    result.add(KEY_MOBILE_NETWORK_SETTINGS);
                    result.add(KEY_MANAGE_MOBILE_PLAN);
                }

                // Remove Mobile Network Settings and Manage Mobile Plan
                // if config_show_mobile_plan sets false.
                final boolean isMobilePlanEnabled = context.getResources().getBoolean(
                        R.bool.config_show_mobile_plan);
                if (!isMobilePlanEnabled) {
                    result.add(KEY_MANAGE_MOBILE_PLAN);
                }

                final PackageManager pm = context.getPackageManager();

                // Remove Airplane Mode settings if it's a stationary device such as a TV.
                if (pm.hasSystemFeature(PackageManager.FEATURE_TELEVISION)) {
                    result.add(KEY_TOGGLE_AIRPLANE);
                }

                // proxy UI disabled until we have better app support
                result.add(KEY_PROXY_SETTINGS);

                // Disable Tethering if it's not allowed or if it's a wifi-only device
                ConnectivityManager cm = (ConnectivityManager)
                        context.getSystemService(Context.CONNECTIVITY_SERVICE);
                if (isSecondaryUser || !cm.isTetheringSupported()) {
                    result.add(KEY_TETHER_SETTINGS);
                }

                if (!ImsManager.isWfcEnabledByPlatform(context) ||
                        !ImsManager.isWfcProvisionedOnDevice(context)) {
                    result.add(KEY_WFC_SETTINGS);
                }

                if (RestrictedLockUtils.hasBaseUserRestriction(context,
                        UserManager.DISALLOW_NETWORK_RESET, UserHandle.myUserId())) {
                    result.add(KEY_NETWORK_RESET);
                }

                ///M: Reomve RCSE search if not support.
                IWWOPJoynSettingsExt joynExt = UtilsExt.getJoynSettingsPlugin(context);
                if (!joynExt.isJoynSettingsEnabled()) {
                    result.add(KEY_RCSE_SETTINGS);
                }

                return result;
            }
        };

    ///M:
    private static boolean isAPKInstalled(Context context, String action) {
         Intent intent = new Intent(action);
         List<ResolveInfo> apps = context.getPackageManager().queryIntentActivities(intent, 0);
         return !(apps == null || apps.size() == 0);
    }

    /// M: update MOBILE_NETWORK_SETTINGS enabled state by multiple conditions
    private void updateMobileNetworkEnabled() {
        Preference preference = (Preference) findPreference(KEY_MOBILE_NETWORK_SETTINGS);

        if (preference != null) {
            if (preference instanceof RestrictedPreference) {
                RestrictedPreference rp = (RestrictedPreference) preference;
                if (rp.isDisabledByAdmin()) {
                    Log.d(TAG, "MOBILE_NETWORK_SETTINGS disabled by Admin");
                    return;
                }
            }
            // modify in a simple way to get whether there is sim card inserted
            ISettingsMiscExt miscExt = UtilsExt.getMiscPlugin(getActivity());
            TelephonyManager telephonyManager =
                (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
            int callState = telephonyManager.getCallState();
            int simNum = SubscriptionManager.from(getActivity()).getActiveSubscriptionInfoCount();
            Log.d(TAG, "callstate = " + callState + " simNum = " + simNum);
            if (simNum > 0 && callState == TelephonyManager.CALL_STATE_IDLE
                    && !miscExt.isWifiOnlyModeSet()) {
                preference.setEnabled(true);
            } else {
                /// M: for plug-in
                preference.setEnabled(UtilsExt.getSimManagmentExtPlugin(getActivity())
                        .useCtTestcard() || false);
            }
        }
    }

    private PhoneStateListener mPhoneStateListener = new PhoneStateListener() {
        @Override
        public void onCallStateChanged(int state, String incomingNumber) {
            super.onCallStateChanged(state, incomingNumber);
            Log.d(TAG, "PhoneStateListener, new state=" + state);
            if (state == TelephonyManager.CALL_STATE_IDLE && getActivity() != null) {
                updateMobileNetworkEnabled();
            }
        }
    };

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (TelephonyIntents.ACTION_SUBINFO_RECORD_UPDATED.equals(action)) {
                Log.d(TAG, "ACTION_SIM_INFO_UPDATE received");
                updateMobileNetworkEnabled();
            // when received Carrier config changes, update WFC buttons
            } else if (CarrierConfigManager.ACTION_CARRIER_CONFIG_CHANGED.equals(action)) {
                Log.d(TAG, "carrier config changed...");
                if (mButtonWfc != null) {
                    ///M : Remove wifi calling preference when monkey running
                    if (ImsManager.isWfcEnabledByPlatform(context) && !Utils.isMonkeyRunning()) {
                        Log.d(TAG, "wfc enabled, add WCF setting");
                        getPreferenceScreen().addPreference(mButtonWfc);
                        setWfcModeSummary();
                        mWfcExt.initPlugin(WirelessSettings.this);
                        mWfcExt.customizedWfcPreference(getActivity(), getPreferenceScreen());
                        mWfcExt.onWirelessSettingsEvent(DefaultWfcSettingsExt.CONFIG_CHANGE);
                        /// M: for operator customization @{
                        if (mRemoveWfcPreferenceMode) {
                           mButtonWfc.setSummary(null);
                        }
                    } else {
                        Log.d(TAG, "wfc disabled, remove WCF setting");
                        mWfcExt.onWirelessSettingsEvent(DefaultWfcSettingsExt.CONFIG_CHANGE);
                        if (!TelephonyUtils.isSupportMims()) {
                           getPreferenceScreen().removePreference(mButtonWfc);
                        }
                    }
                }
            }
        }
    };
    /// @}
    /// M: L+L feature
    private void setWfcModeSummary() {
        //if no sim -> disabled
        //if two sim -> null
        //if one sim -> get the sim mode
        Context context = getActivity();
        mWfcExt.initPlugin(WirelessSettings.this);
        List<SubscriptionInfo> si = SubscriptionManager.from(
                context).getActiveSubscriptionInfoList();
        try {
            if (si == null) {
                mButtonWfc.setSummary(mWfcExt
                        .getWfcSummary(context, R.string.wifi_calling_disabled));
            } else if (si.size() > 0) {
                if (TelephonyUtils.isSupportMims()) {
                    mButtonWfc.setSummary("");
                } else {
                    int slotId = TelephonyUtils.getMainCapabilityPhoneId();
                    int subId = SubscriptionManager.getSubIdUsingPhoneId(slotId);
                    Log.d(TAG, "setWfcModeSummary--subId:" + subId);
                    mButtonWfc.setSummary(mWfcExt.getWfcSummary(context,
                            WifiCallingSettings.getWfcModeSummary(context,
                            ImsManager.getWfcMode(context,
                                    mTm.isNetworkRoaming(subId), slotId), slotId)));
                }
            }
        //Plugin need setSummary when two sims?
        } catch (IndexOutOfBoundsException ex) {
            Log.e(TAG, "IndexOutOfBoundsException");
        }
        //Plugin need setSummary when two sims?
    }
}
