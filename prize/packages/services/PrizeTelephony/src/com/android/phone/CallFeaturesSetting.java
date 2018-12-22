/*
* Copyright (C) 2014 MediaTek Inc.
* Modification based on code covered by the mentioned copyright
* and/or permission notice(s).
*/
/*
 * Copyright (C) 2008 The Android Open Source Project
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

package com.android.phone;

import android.app.ActionBar;
import android.app.Activity;
import android.app.ActivityOptions;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.os.UserHandle;
import android.os.UserManager;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceScreen;
import android.provider.Settings;
import android.telecom.PhoneAccountHandle;
import android.telecom.TelecomManager;
import android.telephony.CarrierConfigManager;
import android.telephony.PhoneStateListener;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;

import com.android.ims.ImsConfig;
import com.android.ims.ImsException;
import com.android.ims.ImsManager;
import com.android.internal.telephony.CallForwardInfo;
import com.android.internal.telephony.Phone;
import com.android.internal.telephony.PhoneConstants;
import com.android.internal.telephony.TelephonyIntents;
import com.android.phone.PhoneGlobals.SubInfoUpdateListener;
import com.android.phone.common.util.SettingsUtil;
import com.android.phone.settings.AccountSelectionPreference;
import com.android.phone.settings.PhoneAccountSettingsFragment;
import com.android.phone.settings.VoicemailSettingsActivity;
import com.android.phone.settings.fdn.FdnSetting;
import com.android.services.telephony.sip.SipUtil;

import com.mediatek.ims.config.ImsConfigContract;
import com.mediatek.phone.ext.DefaultCallFeaturesSettingExt;
import com.mediatek.phone.ext.ExtensionManager;
import com.mediatek.phone.ext.ICallFeaturesSettingExt;
import com.mediatek.settings.CallBarring;
import com.mediatek.settings.IpPrefixPreference;
import com.mediatek.settings.TelephonyUtils;
import com.mediatek.settings.cdg.CdgCallSettings;
import com.mediatek.settings.cdg.CdgUtils;
import com.mediatek.settings.cdma.CdmaCallForwardOptions;
import com.mediatek.settings.cdma.CdmaCallWaitOptions;
import com.mediatek.settings.vtss.GsmUmtsVTCBOptions;
import com.mediatek.settings.vtss.GsmUmtsVTCFOptions;
import com.mediatek.settings.cdma.CdmaCallWaitingUtOptions;
import com.mediatek.settings.cdma.TelephonyUtilsEx;

import java.lang.String;
import java.util.ArrayList;
import java.util.List;

/**
 * Top level "Call settings" UI; see res/xml/call_feature_setting.xml
 *
 * This preference screen is the root of the "Call settings" hierarchy available from the Phone
 * app; the settings here let you control various features related to phone calls (including
 * voicemail settings, the "Respond via SMS" feature, and others.)  It's used only on
 * voice-capable phone devices.
 *
 * Note that this activity is part of the package com.android.phone, even
 * though you reach it from the "Phone" app (i.e. DialtactsActivity) which
 * is from the package com.android.contacts.
 *
 * For the "Mobile network settings" screen under the main Settings app,
 * See {@link MobileNetworkSettings}.
 *
 * @see com.android.phone.MobileNetworkSettings
 */
public class CallFeaturesSetting extends PreferenceActivity
        implements Preference.OnPreferenceChangeListener,
                SubInfoUpdateListener {
    private static final String LOG_TAG = "CallFeaturesSetting";
    private static final boolean DBG = true;//(PhoneGlobals.DBG_LEVEL >= 2);

    // String keys for preference lookup
    // TODO: Naming these "BUTTON_*" is confusing since they're not actually buttons(!)
    // TODO: Consider moving these strings to strings.xml, so that they are not duplicated here and
    // in the layout files. These strings need to be treated carefully; if the setting is
    // persistent, they are used as the key to store shared preferences and the name should not be
    // changed unless the settings are also migrated.
    private static final String VOICEMAIL_SETTING_SCREEN_PREF_KEY = "button_voicemail_category_key";
    private static final String BUTTON_FDN_KEY   = "button_fdn_key";
    private static final String BUTTON_RETRY_KEY       = "button_auto_retry_key";
    private static final String BUTTON_GSM_UMTS_OPTIONS = "button_gsm_more_expand_key";
    private static final String BUTTON_CDMA_OPTIONS = "button_cdma_more_expand_key";

    /// M: add for call private voice feature @{
    private static final String BUTTON_CP_KEY          = "button_voice_privacy_key";
    /// @}

    private static final String CALL_FORWARDING_KEY = "call_forwarding_key";
    private static final String ADDITIONAL_GSM_SETTINGS_KEY = "additional_gsm_call_settings_key";
    /// M: GSM type phone call settings item --> call barring
    private static final String BUTTON_CB_EXPAND = "button_cb_expand_key";

    /// M: CDMA type phone call settings item --> call forward & call wait
    private static final String KEY_CALL_FORWARD = "button_cf_expand_key";
    private static final String KEY_CALL_WAIT = "button_cw_key";
    private static final String KEY_CALLER_ID = "button_caller_id";

    private static final String IP_PREFIX_KEY = "button_ip_prefix_key";

    private static final String PHONE_ACCOUNT_SETTINGS_KEY =
            "phone_account_settings_preference_screen";

    private static final String ENABLE_VIDEO_CALLING_KEY = "button_enable_video_calling";

    private Phone mPhone;
    private SubscriptionInfoHelper mSubscriptionInfoHelper;
    private TelecomManager mTelecomManager;

    private CheckBoxPreference mButtonAutoRetry;
    private PreferenceScreen mVoicemailSettingsScreen;
    private CheckBoxPreference mEnableVideoCalling;

    /*
     * Click Listeners, handle click based on objects attached to UI.
     */

    // Click listener for all toggle events
    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        /// M: Add for our inner features @{
        if (onPreferenceTreeClickMTK(preferenceScreen, preference)) {
            return true;
        }
        /// @}
        if (preference == mButtonAutoRetry) {
            android.provider.Settings.Global.putInt(getApplicationContext().getContentResolver(),
                    android.provider.Settings.Global.CALL_AUTO_RETRY,
                    mButtonAutoRetry.isChecked() ? 1 : 0);
            return true;
        }
        return false;
    }

    /**
     * Implemented to support onPreferenceChangeListener to look for preference
     * changes.
     *
     * @param preference is the preference to be changed
     * @param objValue should be the value of the selection, NOT its localized
     * display value.
     */
    @Override
    public boolean onPreferenceChange(Preference preference, Object objValue) {
        if (DBG) log("onPreferenceChange: \"" + preference + "\" changed to \"" + objValue + "\"");

        if (preference == mEnableVideoCalling) {
            int phoneId = SubscriptionManager.getPhoneId(mPhone.getSubId());
            if (ImsManager.isEnhanced4gLteModeSettingEnabledByUser(mPhone.getContext(), phoneId)) {
                PhoneGlobals.getInstance().phoneMgrEx.enableVideoCalling((boolean) objValue, mPhone.getSubId());
                ///M: For Plugin to get updated video Preference
                ExtensionManager.getCallFeaturesSettingExt()
                                .videoPreferenceChange((boolean) objValue);
            } else {
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                DialogInterface.OnClickListener networkSettingsClickListener =
                        new Dialog.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                startActivity(new Intent(mPhone.getContext(),
                                        com.android.phone.MobileNetworkSettings.class));
                            }
                        };
                builder.setMessage(getResources().getString(
                                R.string.enable_video_calling_dialog_msg))
                        .setNeutralButton(getResources().getString(
                                R.string.enable_video_calling_dialog_settings),
                                networkSettingsClickListener)
                        .setPositiveButton(android.R.string.ok, null)
                        .show();
                return false;
            }
        }

        // Always let the preference setting proceed.
        return true;
    }

    @Override
    protected void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        if (DBG) log("onCreate: Intent is " + getIntent());

        // Make sure we are running as an admin user.
        if (!UserManager.get(this).isAdminUser()) {
            Toast.makeText(this, R.string.call_settings_admin_user_only,
                    Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        mSubscriptionInfoHelper = new SubscriptionInfoHelper(this, getIntent());
        mSubscriptionInfoHelper.setActionBarTitle(
                getActionBar(), getResources(), R.string.call_settings_with_label);

        /*PRIZE-Add-DialerV8-wangzhong-2017_7_19-start*/
        getActionBar().setElevation(this.getResources().getDimensionPixelOffset(R.dimen.prize_elevation_top));
        /*PRIZE-Add-DialerV8-wangzhong-2017_7_19-end*/

        mPhone = mSubscriptionInfoHelper.getPhone();
        mTelecomManager = TelecomManager.from(this);
        /// M: Register related listeners & events.
        registerEventCallbacks();
        /// M: Add for MTK hotswap
        if (mPhone == null) {
            log("onCreate: mPhone is null, finish!!!");
            finish();
            return;
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        TelephonyManager telephonyManager =
                (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        telephonyManager.listen(mPhoneStateListener, PhoneStateListener.LISTEN_NONE);
    }

    @Override
    protected void onResume() {
        super.onResume();

        PreferenceScreen preferenceScreen = getPreferenceScreen();
        if (preferenceScreen != null) {
            preferenceScreen.removeAll();
        }

        /*PRIZE-Change-DialerV8-wangzhong-2017_7_19-start*/
        /*addPreferencesFromResource(R.xml.call_feature_setting);*/
        addPreferencesFromResource(R.xml.prize_call_feature_setting);
        getListView().setPadding(0, getResources().getDimensionPixelOffset(R.dimen.prize_preferences_bg_margin2),
                0, getResources().getDimensionPixelOffset(R.dimen.prize_preferences_bg_margin2));
        getListView().setBackgroundColor(getResources().getColor(R.color.prize_preferences_lowest_bg));
        getListView().setDivider(null);
        android.graphics.drawable.ColorDrawable drawable = new android.graphics.drawable.ColorDrawable(android.graphics.Color.TRANSPARENT);
        getListView().setSelector(drawable);
        /*PRIZE-Change-DialerV8-wangzhong-2017_7_19-end*/

        TelephonyManager telephonyManager =
                (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        telephonyManager.listen(mPhoneStateListener, PhoneStateListener.LISTEN_CALL_STATE);

        Preference phoneAccountSettingsPreference = findPreference(PHONE_ACCOUNT_SETTINGS_KEY);
        if (telephonyManager.isMultiSimEnabled() || !SipUtil.isVoipSupported(mPhone.getContext())) {
            getPreferenceScreen().removePreference(phoneAccountSettingsPreference);
        }

        PreferenceScreen prefSet = getPreferenceScreen();
        mVoicemailSettingsScreen =
                (PreferenceScreen) findPreference(VOICEMAIL_SETTING_SCREEN_PREF_KEY);
        Intent voiceMailIntent = new Intent(this, VoicemailSettingsActivity.class);
        SubscriptionInfoHelper.addExtrasToIntent(voiceMailIntent, SubscriptionManager
                                        .from(this).getSubscriptionInfo(mPhone.getSubId()));
        mVoicemailSettingsScreen.setIntent(voiceMailIntent);

        mButtonAutoRetry = (CheckBoxPreference) findPreference(BUTTON_RETRY_KEY);

        mEnableVideoCalling = (CheckBoxPreference) findPreference(ENABLE_VIDEO_CALLING_KEY);

        PersistableBundle carrierConfig =
                PhoneGlobals.getInstance().getCarrierConfigForSubId(mPhone.getSubId());

        if (carrierConfig.getBoolean(CarrierConfigManager.KEY_AUTO_RETRY_ENABLED_BOOL)) {
            mButtonAutoRetry.setOnPreferenceChangeListener(this);
            int autoretry = Settings.Global.getInt(
                    getContentResolver(), Settings.Global.CALL_AUTO_RETRY, 0);
            mButtonAutoRetry.setChecked(autoretry != 0);
        } else {
            prefSet.removePreference(mButtonAutoRetry);
            mButtonAutoRetry = null;
        }
        Intent fdnIntent = new Intent(this, FdnSetting.class);
        SubscriptionInfoHelper.addExtrasToIntent(fdnIntent, SubscriptionManager
                                        .from(this).getSubscriptionInfo(mPhone.getSubId()));
        Preference cdmaOptions = prefSet.findPreference(BUTTON_CDMA_OPTIONS);
        Preference gsmOptions = prefSet.findPreference(BUTTON_GSM_UMTS_OPTIONS);
        if (carrierConfig.getBoolean(CarrierConfigManager.KEY_WORLD_PHONE_BOOL)) {
            cdmaOptions.setIntent(mSubscriptionInfoHelper.getIntent(CdmaCallOptions.class));
            gsmOptions.setIntent(mSubscriptionInfoHelper.getIntent(GsmUmtsCallOptions.class));
        } else {
            prefSet.removePreference(cdmaOptions);
            prefSet.removePreference(gsmOptions);

            int phoneType = mPhone.getPhoneType();
            Preference fdnButton = prefSet.findPreference(BUTTON_FDN_KEY);
            if (carrierConfig.getBoolean(
                    CarrierConfigManager.KEY_HIDE_CARRIER_NETWORK_SETTINGS_BOOL)) {
                prefSet.removePreference(fdnButton);
            } else {
                if (phoneType == PhoneConstants.PHONE_TYPE_CDMA
                        /// M: [CT VOLTE]
                        || (TelephonyUtilsEx.isCtVolteEnabled() && TelephonyUtilsEx
                            .isCtSim(mPhone.getSubId())) || TelephonyUtilsEx
                            .isSmartFren4gSim(mPhone.getContext(), mPhone.getSubId())) {

                    /// Add for CDG OMH, show fdn when CDG OMH SIM card. @{
                    if(CdgUtils.isCdgOmhSimCard(mPhone.getSubId())) {
                        fdnButton.setIntent(fdnIntent);
                    } else {
                    /// @}
                        prefSet.removePreference(fdnButton);
                    }

                    if (!carrierConfig.getBoolean(
                            CarrierConfigManager.KEY_VOICE_PRIVACY_DISABLE_UI_BOOL)) {
                        /*PRIZE-Change-DialerV8-wangzhong-2017_7_19-start*/
                        /*addPreferencesFromResource(R.xml.cdma_call_privacy);*/
                        addPreferencesFromResource(R.xml.prize_cdma_call_privacy);
                        /*PRIZE-Change-DialerV8-wangzhong-2017_7_19-end*/
                        /// M: for ALPS02087723, get the right cdma phone instance @{
                        CdmaVoicePrivacyCheckBoxPreference ccp =
                                (CdmaVoicePrivacyCheckBoxPreference)findPreference(BUTTON_CP_KEY);
                        if (ccp != null) {
                            /// M: [CT VOLTE]
                            if ((TelephonyUtilsEx.isCtVolteEnabled() && TelephonyUtilsEx
                                .isCtSim(mPhone.getSubId())) || TelephonyUtilsEx
                                .isSmartFren4gSim(mPhone.getContext(), mPhone.getSubId())) {
                                log("Voice privacy option removed");
                                prefSet.removePreference(ccp);
                            } else {
                               ccp.setPhone(mPhone);
                            }
                        }
                        /// @}
                    }
                    /// M: For C2K project to group GSM and C2K Call Settings @{
                    log("isCdmaSupport = " + TelephonyUtils.isCdmaSupport());
                    if (TelephonyUtils.isCdmaSupport()) {
                        /*PRIZE-Change-DialerV8-wangzhong-2017_7_19-start*/
                        /*addPreferencesFromResource(R.xml.mtk_cdma_call_options);*/
                        addPreferencesFromResource(R.xml.prize_mtk_cdma_call_options);
                        /*PRIZE-Change-DialerV8-wangzhong-2017_7_19-end*/
                    if (!TelephonyUtilsEx
                                   .isSmartFren4gSim(mPhone.getContext(), mPhone.getSubId())) {
                             Preference callerIDPreference = prefSet.findPreference(KEY_CALLER_ID);
                             log("No SmartFren SIM, so remove Caller ID pref for CDMA");
                             prefSet.removePreference(callerIDPreference);
                         }
                    }
                    /// @}
                } else if (phoneType == PhoneConstants.PHONE_TYPE_GSM) {
                    fdnButton.setIntent(fdnIntent);

                    if (carrierConfig.getBoolean(
                            CarrierConfigManager.KEY_ADDITIONAL_CALL_SETTING_BOOL)) {
                        /*PRIZE-Change-DialerV8-wangzhong-2017_7_19-start*/
                        /*addPreferencesFromResource(R.xml.gsm_umts_call_options);*/
                        addPreferencesFromResource(R.xml.prize_gsm_umts_call_options);
                        /*PRIZE-Change-DialerV8-wangzhong-2017_7_19-end*/

                        GsmUmtsCallOptions.init(prefSet, mSubscriptionInfoHelper);
                    }
                } else {
                    throw new IllegalStateException("Unexpected phone type: " + phoneType);
                }
            }
        }

        /// M: VILTE enable not dependent on data enable for some operators @{
        boolean isNonDepOnData = carrierConfig.getBoolean(
                        CarrierConfigManager.KEY_VILTE_ENABLE_NOT_DEPENDENT_ON_DATA_ENABLE_BOOL);
        /// @}
        int phoneId = SubscriptionManager.getPhoneId(mPhone.getSubId());
        if (ImsManager.isVtEnabledByPlatform(mPhone.getContext(), phoneId) &&
                ImsManager.isVtProvisionedOnDevice(mPhone.getContext()) &&
                (mPhone.mDcTracker.isDataEnabled(true) || isNonDepOnData)) {
            boolean currentValue =
                    ImsManager.isEnhanced4gLteModeSettingEnabledByUser(mPhone.getContext(), phoneId)
                    ? PhoneGlobals.getInstance().phoneMgrEx.isVideoCallingEnabled(
                            getOpPackageName(), mPhone.getSubId()) : false;
            mEnableVideoCalling.setChecked(currentValue);
            mEnableVideoCalling.setOnPreferenceChangeListener(this);
        } else {
            prefSet.removePreference(mEnableVideoCalling);
        }

        if (ImsManager.isVolteEnabledByPlatform(this) &&
                !carrierConfig.getBoolean(
                        CarrierConfigManager.KEY_CARRIER_VOLTE_TTY_SUPPORTED_BOOL)) {
            TelephonyManager tm = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
            /* tm.listen(mPhoneStateListener, PhoneStateListener.LISTEN_CALL_STATE); */
        }

        Preference wifiCallingSettings = findPreference(
                getResources().getString(R.string.wifi_calling_settings_key));

        final PhoneAccountHandle simCallManager = mTelecomManager.getSimCallManager();
        if (simCallManager != null) {
            Intent intent = PhoneAccountSettingsFragment.buildPhoneAccountConfigureIntent(
                    this, simCallManager);
            Log.d(LOG_TAG, "--- simCallManager is not null ---");
            if (intent != null) {
                PackageManager pm = mPhone.getContext().getPackageManager();
                List<ResolveInfo> resolutions = pm.queryIntentActivities(intent, 0);
                if (!resolutions.isEmpty()) {
                    Log.d(LOG_TAG, "--- set wfc ---");
                    wifiCallingSettings.setTitle(resolutions.get(0).loadLabel(pm));
                    wifiCallingSettings.setSummary(null);
                    wifiCallingSettings.setIntent(intent);
                } else {
                    prefSet.removePreference(wifiCallingSettings);
                }
            } else {
                prefSet.removePreference(wifiCallingSettings);
            }
        } else if (!ImsManager.isWfcEnabledByPlatform(mPhone.getContext()) ||
                !ImsManager.isWfcProvisionedOnDevice(mPhone.getContext())) {
            Log.d(LOG_TAG, "--- remove wfc,platform support : "
                    + ImsManager.isWfcEnabledByPlatform(mPhone.getContext()));
            prefSet.removePreference(wifiCallingSettings);
        } else {
            int resId = com.android.internal.R.string.wifi_calling_off_summary;
            if (ImsManager.isWfcEnabledByUser(mPhone.getContext())) {
                int wfcMode = ImsManager.getWfcMode(mPhone.getContext());
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
                        if (DBG) log("Unexpected WFC mode value: " + wfcMode);
                }
            }
            wifiCallingSettings.setSummary(resId);
        }

        ///M: [OMH]
        updateOmhItems();

        /// M: [IP-prefix]
        setIpFunction();

        /// M: update screen status
        updateScreenStatus();

        /// M: WFC @{
        //ExtensionManager.getCallFeaturesSettingExt().customizeWfcRemoval(this, wifiCallingSettings);
        ExtensionManager.getCallFeaturesSettingExt().initOtherCallFeaturesSetting(this);
        ExtensionManager.getCallFeaturesSettingExt()
                .onCallFeatureSettingsEvent(DefaultCallFeaturesSettingExt.RESUME);
        /// @}
    }

    @Override
    protected void onNewIntent(Intent newIntent) {
        setIntent(newIntent);

        mSubscriptionInfoHelper = new SubscriptionInfoHelper(this, getIntent());
        mSubscriptionInfoHelper.setActionBarTitle(
                getActionBar(), getResources(), R.string.call_settings_with_label);
        mPhone = mSubscriptionInfoHelper.getPhone();
    }

    private static void log(String msg) {
        Log.d(LOG_TAG, msg);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        final int itemId = item.getItemId();
        if (itemId == android.R.id.home) {  // See ActionBar#setDisplayHomeAsUpEnabled()
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Finish current Activity and go up to the top level Settings ({@link CallFeaturesSetting}).
     * This is useful for implementing "HomeAsUp" capability for second-level Settings.
     */
    public static void goUpToTopLevelSetting(
            Activity activity, SubscriptionInfoHelper subscriptionInfoHelper) {
        Intent intent = subscriptionInfoHelper.getIntent(CallFeaturesSetting.class);
        intent.setAction(Intent.ACTION_MAIN);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        activity.startActivity(intent);
        activity.finish();
    }

    // -------------------- Mediatek ---------------------
    /// M: Add for plug-in @{
    private ICallFeaturesSettingExt mExt;
    /// Add for CDG OMH
    private CdgCallSettings mCdgCallSettings = null;
    private static final String ACTION_OMH = "com.mediatek.internal.omh.cardcheck";


    /**
     * Add for IMS provisioning
     */
    private void updateVtOption() {
        new Thread() {

            // TODO: to check this API, should not be using another thread in UI code
            @Override
            public void run() {
                try{
                    ImsManager ims = ImsManager.getInstance(
                            mPhone.getContext(), mPhone.getPhoneId());
                    ImsConfig imsCfg = ims.getConfigInterface();
                    /// getVtProvisioned api contains two cases:
                    /// 1. Don't support provision, it will return true, so that
                    ///    the provision value will not affect the decision(show/not)
                    /// 2. Support provision, it will return the current status.
                    int phoneId = TelephonyUtils.getMainCapabilityPhoneId(mPhone.getContext());
                    if (TelephonyUtils.isSupportMims()) {
                        phoneId = SubscriptionManager.getPhoneId(mPhone.getSubId());
                    }
                    boolean enableProvision = imsCfg.getVtProvisioned();
                    boolean enablePlatform = ImsManager.isVtEnabledByPlatform(
                            mPhone.getContext(), phoneId);
                    log("updateVtOption enableProvision = " + enableProvision
                            + "enablePlatform = " + enablePlatform);

                    PreferenceScreen prefSet = getPreferenceScreen();
                    if (enableProvision && enablePlatform) {
                        if (prefSet != null && mEnableVideoCalling == null) {
                            prefSet.addPreference(mEnableVideoCalling);
                        }
                    }
                } catch (ImsException e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }
    /// @}

    private void updateOmhItems() {
        if (CdgUtils.isCdgOmhSimCard(mSubscriptionInfoHelper.getSubId())) {
            log("new CdgCallSettings.");
            mCdgCallSettings = new CdgCallSettings(this, mSubscriptionInfoHelper);
            Preference callForwardPreference = this.findPreference(KEY_CALL_FORWARD);
            if (callForwardPreference != null) {
                this.getPreferenceScreen().removePreference(callForwardPreference);
            }

            Preference callWaitPreference = this.findPreference(KEY_CALL_WAIT);
            if (callWaitPreference != null) {
                this.getPreferenceScreen().removePreference(callWaitPreference);
            }
        }
    }

    @Override
    protected void onDestroy() {
        if (UserManager.get(this).isAdminUser()) {
            /// M: WFC @{
            ExtensionManager.getCallFeaturesSettingExt()
                    .onCallFeatureSettingsEvent(DefaultCallFeaturesSettingExt.DESTROY);
            /// @}
            unregisterEventCallbacks();
        }
        super.onDestroy();
        /// M: add for dual volte feature @{
        /* PRIZE Telephony zhoushuanghua add for 60719 <2018_06_19> start */
        TelephonyUtils.setParameters(mPhone.getSubId(), null,null);
		/* PRIZE Telephony zhoushuanghua add for 60719 <2018_06_19> end */
        /// @}
    }

    @Override
    public void handleSubInfoUpdate() {
        finish();
    }

    /**
     * For internal features
     * @param preferenceScreen
     * @param preference
     * @return
     */
    private boolean onPreferenceTreeClickMTK(
            PreferenceScreen preferenceScreen, Preference preference) {

        log("onPreferenceTreeClickMTK" + preference.getKey());
        PersistableBundle carrierConfig =
                PhoneGlobals.getInstance().getCarrierConfigForSubId(mPhone.getSubId());
        /// M: add for dual volte feature @{
        /* PRIZE Telephony zhoushuanghua add for 60719 <2018_06_19> start */
        TelephonyUtils.setParameters(mPhone.getSubId(), preference,mSubscriptionInfoHelper);
        /* PRIZE Telephony zhoushuanghua add for 60719 <2018_06_19> end */
        /// @}
        /// Add for [VoLTE_SS] @{
        if (preference == preferenceScreen.findPreference(CALL_FORWARDING_KEY) ||
            preference == preferenceScreen.findPreference(ADDITIONAL_GSM_SETTINGS_KEY) ||
            preference == preferenceScreen.findPreference(BUTTON_CB_EXPAND)) {

            if (TelephonyUtils.shouldShowOpenMobileDataDialog(
                    this, mPhone.getSubId())) {
                TelephonyUtils.showOpenMobileDataDialog(this, mPhone.getSubId());
            } else {
                Intent intent;
                if (preference == preferenceScreen.findPreference(CALL_FORWARDING_KEY)) {
                    if (carrierConfig.getBoolean(CarrierConfigManager.KEY_SUPPORT_VT_SS_BOOL)) {
                        intent = mSubscriptionInfoHelper.getIntent(GsmUmtsVTCFOptions.class);
                    } else {
                        intent = mSubscriptionInfoHelper.getIntent(GsmUmtsCallForwardOptions.class);
                    }

                } else if (preference == preferenceScreen.findPreference(BUTTON_CB_EXPAND)) {
                    if (carrierConfig.getBoolean(CarrierConfigManager.KEY_SUPPORT_VT_SS_BOOL)) {
                        intent = mSubscriptionInfoHelper.getIntent(GsmUmtsVTCBOptions.class);
                    } else {
                        intent = mSubscriptionInfoHelper.getIntent(CallBarring.class);
                    }
                } else {
                    intent = mSubscriptionInfoHelper.getIntent(GsmUmtsAdditionalCallOptions.class);
                }
                SubscriptionInfoHelper.addExtrasToIntent(intent, SubscriptionManager
                                      .from(this).getSubscriptionInfo(mPhone.getSubId()));
                startActivity(intent);
            }
            return true;
        }
        /// @}
        /// M: CDMA type phone call setting item click handling
        if (preference == preferenceScreen.findPreference(KEY_CALL_FORWARD) ||
            preference == preferenceScreen.findPreference(KEY_CALL_WAIT) ||
            preference == preferenceScreen.findPreference(KEY_CALLER_ID)) {
            /// M: [CT VOLTE] @{
            if (((TelephonyUtilsEx.isCtVolteEnabled() && TelephonyUtilsEx
                        .isCt4gSim(mPhone.getSubId())) || TelephonyUtilsEx
                        .isSmartFren4gSim(mPhone.getContext(), mPhone.getSubId()))
                    && TelephonyUtils.shouldShowOpenMobileDataDialog(this, mPhone.getSubId())) {
                TelephonyUtils.showOpenMobileDataDialog(this, mPhone.getSubId());
            } else {
            /// @}
                if (preference == preferenceScreen.findPreference(KEY_CALL_FORWARD)) {
                    Intent intent;
                    /// M:[CT VOLTE] @{
                    if ((TelephonyUtilsEx.isCtVolteEnabled() && TelephonyUtilsEx
                            .isCt4gSim(mPhone.getSubId())) || TelephonyUtilsEx
                            .isSmartFren4gSim(mPhone.getContext(), mPhone.getSubId())) {
                        intent = mSubscriptionInfoHelper.getIntent(GsmUmtsCallForwardOptions.class);
                    } else {
                    /// @}
                        intent = mSubscriptionInfoHelper.getIntent(CdmaCallForwardOptions.class);
                    }
                    SubscriptionInfoHelper.addExtrasToIntent(intent, SubscriptionManager
                                          .from(this).getSubscriptionInfo(mPhone.getSubId()));
                    startActivity(intent);
                } else if (preference == preferenceScreen.findPreference(KEY_CALLER_ID)) {
                    Intent intent
                         = mSubscriptionInfoHelper.getIntent(GsmUmtsAdditionalCallOptions.class);
                    SubscriptionInfoHelper.addExtrasToIntent(intent, SubscriptionManager
                                          .from(this).getSubscriptionInfo(mPhone.getSubId()));
                    startActivity(intent);
                } else { // (preference ==
                         // preferenceScreen.findPreference(KEY_CALL_WAIT))
                    /// M: remove CNIR and move CW option to cdma call option.
                    /// TODO: Check whether need mForeground
                    boolean isImsOn = TelephonyUtils.isImsServiceAvailable(this, mPhone.getSubId());
                    if ((TelephonyUtilsEx.isCtVolteEnabled() && TelephonyUtilsEx
                            .isCt4gSim(mPhone.getSubId()) && isImsOn) || TelephonyUtilsEx
                            .isSmartFren4gSim(mPhone.getContext(), mPhone.getSubId())) {
                        Intent intent = mSubscriptionInfoHelper
                                .getIntent(CdmaCallWaitingUtOptions.class);
                        startActivity(intent);
                    } else {
                        showDialog(CdmaCallWaitOptions.CW_MODIFY_DIALOG);
                    }
                }
            }
            return true;
        }
        /// Add for CDG OMH @{
        if (mCdgCallSettings != null && mCdgCallSettings.onPreferenceTreeClick(
                preferenceScreen, preference)) {
            log("onPreferenceTreeClickMTK, handled by CDG call settings.");
            return true;
        }
        /// @}
        return false;
    }

    private void updateScreenStatus() {
        PreferenceScreen pres = getPreferenceScreen();

        boolean isAirplaneModeEnabled = TelephonyUtils.isAirplaneModeOn(
                PhoneGlobals.getInstance());
        boolean hasSubId = SubscriptionManager.isValidSubscriptionId(mPhone.getSubId());
        log("updateScreenStatus, hasSubId " + hasSubId);

        for (int i = 0; i < pres.getPreferenceCount(); i++) {
            Preference pref = pres.getPreference(i);
            pref.setEnabled(!isAirplaneModeEnabled && hasSubId);
        }

        ///M: Fix for the issue ALPS03368607 @{
        CdmaVoicePrivacyCheckBoxPreference ccp =
                (CdmaVoicePrivacyCheckBoxPreference) findPreference(BUTTON_CP_KEY);
        if (ccp != null) {
            /// M: [CT VOLTE]
            if ((TelephonyUtilsEx.isCtVolteEnabled() && TelephonyUtilsEx
                            .isCtSim(mPhone.getSubId())) || TelephonyUtilsEx
                            .isSmartFren4gSim(mPhone.getContext(), mPhone.getSubId())) {
                log("Voice privacy option removed");
                pres.removePreference(ccp);
            } else {
                ccp.setPhone(mPhone);
            }
        }
        /// @}

        /// M: The CF UI will be disabled when air plane mode is on.
        /// but SS should be still workable when IMS is registered,
        /// So Enable the CF UI when IMS is registered. {@
        if (hasSubId) {
            boolean isImsOn = TelephonyUtils.isImsServiceAvailable(this, mPhone.getSubId());
            Preference prefCf = getPreferenceScreen().findPreference(CALL_FORWARDING_KEY);
            Preference prefCb = getPreferenceScreen().findPreference(BUTTON_CB_EXPAND);
            Preference prefCw = getPreferenceScreen().findPreference(ADDITIONAL_GSM_SETTINGS_KEY);
            if (prefCf != null) {
                if (isImsOn) {
                    log(" --- set SS item enabled when IMS is registered ---");
                    prefCf.setEnabled(true);
                    prefCb.setEnabled(true);
                    prefCw.setEnabled(true);
                }
            }
            if (TelephonyUtilsEx.isSmartFren4gSim(mPhone.getContext(), mPhone.getSubId())) {
                Preference prefCdmaCf = getPreferenceScreen().findPreference(KEY_CALL_FORWARD);
                Preference prefCdmaCw = getPreferenceScreen().findPreference(KEY_CALL_WAIT);
                Preference prefCdmaCi = getPreferenceScreen().findPreference(KEY_CALLER_ID);
                log(" -- set CDMA SS item enabled when IMS is registered for SmartFren only --");
                if (prefCdmaCf != null) {
                    prefCdmaCf.setEnabled(true);
                }
                if (prefCdmaCw != null) {
                    prefCdmaCw.setEnabled(true);
                }
                if (prefCdmaCi != null) {
                    prefCdmaCi.setEnabled(true);
                }
            }
        }

        updateVtEnableStatus();
    }

    private void setIpFunction() {
        Preference prefIp = getPreferenceScreen().findPreference(IP_PREFIX_KEY);
        Intent intent = new Intent(this, IpPrefixPreference.class);
        SubscriptionInfoHelper.addExtrasToIntent(intent, SubscriptionManager
                                        .from(this).getSubscriptionInfo(mPhone.getSubId()));
        if (prefIp != null) {
            prefIp.setIntent(intent);
        }
    }

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            log("onReceive, action = " + action);
            if (Intent.ACTION_AIRPLANE_MODE_CHANGED.equals(action) ||
                    ImsManager.ACTION_IMS_STATE_CHANGED.equals(action)) {
                updateScreenStatus();
            } else if (ACTION_OMH.equals(action)) {
                log("update omh items");
                updateOmhItems();
            //  When IMS Configuration Provisioning value changed,
            // remove/add mEnableVideoCalling item.@{
            } else if (ImsConfigContract.ACTION_IMS_CONFIG_CHANGED == action) {
                int actionId = intent.getIntExtra(ImsConfigContract.EXTRA_CHANGED_ITEM, -1);
                log("EXTRA_CHANGED_ITEM actionId = " + actionId);
                if (ImsConfig.ConfigConstants.LVC_SETTING_ENABLED == actionId) {
                    updateVtOption();
                }
            }
        }
    };

    // dialog creation method, called by showDialog()
    @Override
    protected Dialog onCreateDialog(int dialogId) {
        /// M: remove CNIR and move CW option to cdma call option.
        if (dialogId == CdmaCallWaitOptions.CW_MODIFY_DIALOG) {
            return new CdmaCallWaitOptions(this, mPhone).createDialog();
        }

        /// Add for CDG OMH @{
        if (mCdgCallSettings != null) {
            return mCdgCallSettings.onCreateDialog(dialogId);
        }
        /// @}
        return null;
    }

    /**
     * Add call status listener, for VT items(should be disable during calling)
     */
    private final PhoneStateListener mPhoneStateListener = new PhoneStateListener() {
        @Override
        public void onCallStateChanged(int state, String incomingNumber) {
            boolean enabled = (state == TelephonyManager.CALL_STATE_IDLE);
            log("[onCallStateChanged] enabled = " + enabled);
            updateVtEnableStatus();
        }
    };

    /**
     * 1. Listen sim hot swap related change.
     * 2. ACTION_AIRPLANE_MODE_CHANGED
     * 3. ACTION_IMS_STATE_CHANGED
     * 4. Call Status for VT item
     */
    private void registerEventCallbacks() {
        PhoneGlobals.getInstance().addSubInfoUpdateListener(this);
        /// register airplane mode
        IntentFilter intentFilter = new IntentFilter(Intent.ACTION_AIRPLANE_MODE_CHANGED);
        intentFilter.addAction(ImsManager.ACTION_IMS_STATE_CHANGED);
        intentFilter.addAction(ACTION_OMH);
        intentFilter.addAction(ImsConfigContract.ACTION_IMS_CONFIG_CHANGED);
        registerReceiver(mReceiver, intentFilter);
    }

    private void unregisterEventCallbacks() {
        PhoneGlobals.getInstance().removeSubInfoUpdateListener(this);
        unregisterReceiver(mReceiver);
    }

    /**
     * This is for VT option, when during call, disable it.
     */
    private void updateVtEnableStatus() {
        boolean hasSubId = mPhone != null
                && SubscriptionManager.isValidSubscriptionId(mPhone.getSubId());
        log("[updateVtEnableStatus] isInCall = " + TelephonyUtils.isInCall(this) + ", hasSubId = "
                + hasSubId);
        if (mEnableVideoCalling != null) {
            mEnableVideoCalling.setEnabled(hasSubId && !TelephonyUtils.isInCall(this));
        }
    }
}
