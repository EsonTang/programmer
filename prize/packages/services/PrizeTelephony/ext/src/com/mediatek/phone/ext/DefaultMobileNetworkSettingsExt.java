package com.mediatek.phone.ext;

import android.app.AlertDialog;
import android.content.IntentFilter;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceScreen;
import android.preference.SwitchPreference;
import android.util.Log;


public class DefaultMobileNetworkSettingsExt implements IMobileNetworkSettingsExt {

    @Override
    public void initOtherMobileNetworkSettings(PreferenceActivity activity, int subId) {
    }

    @Override
    public void initMobileNetworkSettings(PreferenceActivity activity, int currentTab) {
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        return false;
    }

    @Override
    public void updateLTEModeStatus(ListPreference preference) {
    }

    @Override
    public void updateNetworkTypeSummary(ListPreference preference) {
    }

    @Override
    public void customizeAlertDialog(Preference preference, AlertDialog.Builder builder) {
    }

    @Override
    public void customizePreferredNetworkMode(ListPreference listPreference, int subId) {
    }

    @Override
    public void onPreferenceChange(Preference preference, Object objValue) {
    }

    @Override
    public void onResume() {
    }

    @Override
    public void onPause() {
    }

    @Override
    public void unRegister() {
    }

    @Override
    public boolean isCtPlugin() {
        return false;
    }

    @Override
    public void changeEntries(ListPreference buttonEnabledNetworks) {
    }

    @Override
    public void updatePreferredNetworkValueAndSummary(ListPreference buttonEnabledNetworks,
            int networkMode) {
    }

    @Override
    public void customizeEnhanced4GLteSwitchPreference(PreferenceActivity prefAct,
                           SwitchPreference switchPreference) {
       Log.d("DefaultMobileNetworkSettingsExt", "customizeEnhanced4GLteSwitchPreference");
    }

    @Override
    public boolean customizeDualVolteOpDisable(int subId, boolean enableForCtVolte) {
        return enableForCtVolte;
    }

    @Override
    public void customizeDualVolteIntentFilter(IntentFilter intentFilter) {
    }

    @Override
    public boolean customizeDualVolteReceiveIntent(String action) {
        return false;
    }

    @Override
    public void customizeDualVolteOpHide(PreferenceScreen preferenceScreen,
            Preference preference, boolean showPreference) {        
    }
}
