package com.mediatek.phone.ext;

import android.app.AlertDialog;
import android.content.IntentFilter;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceScreen;
import android.preference.SwitchPreference;


public interface IMobileNetworkSettingsExt {
    /**
     * called in onCreate() of the Activity
     * Plug-in can init itself, preparing for it's function
     * @param activity the MobileNetworkSettings activity
     * @param subId sub id
     * @internal
     */
    void initOtherMobileNetworkSettings(PreferenceActivity activity, int subId);

    /**
     * called in onCreate() of the Activity.
     * Plug-in can init itself, preparing for it's function
     * @param activity the MobileNetworkSettings activity
     * @param currentTab current Tab
     * @internal
     */
    void initMobileNetworkSettings(PreferenceActivity activity, int currentTab);

    /**
     * Attention, returning false means nothing but telling host to go on its own flow.
     * host would never return plug-in's "false" to the caller of onPreferenceTreeClick()
     *
     * @param preferenceScreen the clicked preference screen
     * @param preference the clicked preference
     * @return true if plug-in want to skip host flow. whether return true or false, host will
     * return true to its real caller.
     * @internal
     */
    boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference);

    /**
     * This interface is for updating the MobileNetworkSettings' item "Preferred network type"
     * @param preference there are two cases:
     *                   1. mButtonPreferredNetworkMode in host APP
     *                   2. mButtonEnabledNetworks in host APP
     * @internal
     */
    void updateNetworkTypeSummary(ListPreference preference);

    /**
     * TODO: Clear about what is this interface for
     * @param preference
     * @internal
     */
    void updateLTEModeStatus(ListPreference preference);

    /**
     * Allow Plug-in to customize the AlertDialog passed.
     * This API should be called right before builder.create().
     * Plug-in should check the preference to determine how the Dialog should act.
     * @param preference the clicked preference
     * @param builder the AlertDialog.Builder passed from host APP
     * @internal
     */
    void customizeAlertDialog(Preference preference, AlertDialog.Builder builder);


    /**
     * Update the ButtonPreferredNetworkMode's summary and enable when sim2 is CU card.
     * @param listPreference ButtonPreferredNetworkMode
     * @internal
     */
    void customizePreferredNetworkMode(ListPreference listPreference, int subId);

    /**
     * Preference Change, update network preference value and summary
     * @param preference the clicked preference
     * @param objValue choose obj value
     * @internal
     */
    void onPreferenceChange(Preference preference, Object objValue);

    /**
     * For Plug-in to update Preference.
     * @internal
     */
    void onResume();

    /**
     * For Plug-in to update Preference.
     * @internal
     */
    void onPause();

    /**
     * For Plug-in to pause event and listener registration.
     * @internal
     */
    void unRegister();

    /**
     * for CT feature , CT Plug-in should return true.
     * @return true,if is CT Plug-in
     * @internal
     */
    boolean isCtPlugin();

    /**
     * For changing entry names in list preference dialog box.
     * @param buttonEnabledNetworks list preference
     * @internal
     */
    void changeEntries(ListPreference buttonEnabledNetworks);

    /**
     * For updating network mode and summary.
     * @param buttonEnabledNetworks list preference
     * @param networkMode network mode
     * @internal
     */
    void updatePreferredNetworkValueAndSummary(ListPreference buttonEnabledNetworks,
            int networkMode);

     /**
     * For updating Enhanced4GLteSwitchPreference.
     * @param prefAct PreferenceActivity
     * @param switchPreference SwitchPreference
     */
    void customizeEnhanced4GLteSwitchPreference(PreferenceActivity prefAct,
                           SwitchPreference switchPreference);

    /**
     * For CMCC dual VOLTE feature.
     * @param subId sub id
     * @param enableForCtVolte enhance4glte state.
     * @return true if this is CMCC card.
     */
    boolean customizeDualVolteOpDisable(int subId, boolean enableForCtVolte);

    /**
     * For CMCC VOLTE feature.
     * when is CMCC card, VOLTE show enable.
     * else VOLTE show disable.
     * SIM_STATE_CHANGED broadcast register.
     * @param intentFilter SIM_STATE_CHANGED.
     */
    void customizeDualVolteIntentFilter(IntentFilter intentFilter);

    /**
     * For CMCC VOLTE feature.
     * when is CMCC card, VOLTE show enable.
     * else VOLTE show disable.
     * SIM_STATE_CHANGED broadcast dual with.
     * @param action SIM_STATE_CHANGED.
     * @return true if SIM_STATE_CHAGNED.
     */
    boolean customizeDualVolteReceiveIntent(String action);

    /**
     * For CMCC VOLTE feature.
     * when is CMCC card ,VOLTE show.
     * else VOLTE button hide.
     * @param preferenceScreen Mobile preferenceScreen
     * @param preference mEnhancedButton4glte
     * @param showPreference if true means CMCC card, need show item
     */
    void customizeDualVolteOpHide(PreferenceScreen preferenceScreen,
            Preference preference, boolean showPreference);
}
