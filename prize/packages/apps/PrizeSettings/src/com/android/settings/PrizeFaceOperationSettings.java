package com.android.settings;

import android.app.admin.DevicePolicyManager;
import android.content.Context;
import android.content.Intent;
import android.hardware.fingerprint.Fingerprint;
import android.hardware.fingerprint.FingerprintManager;
import android.os.Bundle;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceScreen;
import android.util.Log;

import com.android.internal.logging.MetricsLogger;
import com.android.internal.logging.MetricsProto.MetricsEvent;
import com.android.settings.face.PrizeFaceRelevantSettings;
import com.android.settings.fingerprint.PrizeFingerRelevantSettings;

import java.util.List;

/**
 * Created by Administrator on 2017/10/13.
 */

public class PrizeFaceOperationSettings extends SettingsPreferenceFragment
        implements Preference.OnPreferenceChangeListener, Preference.OnPreferenceClickListener {
    private static final String TAG = "PrizeFaceOperationSettings";

    public static final String KEY_FUNCTION_CATEGORY = "prize_face_operation_function";
    public static final String KEY_FUNCTION_MANAGER = "prize_face_operation_function_manager";

    @Override
    protected int getMetricsCategory() {
        return MetricsEvent.FINGERPRINT;
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        Log.d(TAG, "onPreferenceChange");
        return true;
    }

    @Override
    public boolean onPreferenceTreeClick(Preference preference) {
        Log.d(TAG, "onPreferenceTreeClick  preference.getKey : " + preference.getKey());
        return super.onPreferenceTreeClick(preference);
    }

    @Override
    public boolean onPreferenceClick(Preference preference) {
        if (null != preference && preference.getKey().equals(KEY_FUNCTION_MANAGER)) {

        }
        return false;
    }

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
    }

    @Override
    public void onResume() {
        super.onResume();

        createPreferenceHierarchy();
    }

    private void createPreferenceHierarchy() {
        PreferenceScreen root = getPreferenceScreen();
        if (root != null) {
            root.removeAll();
        }
        addPreferencesFromResource(R.xml.prize_face_operation_settings);
        root = getPreferenceScreen();

        /** Fingerprint manager */
        /*FingerprintManager fpm = (FingerprintManager) getActivity().getSystemService(
                Context.FINGERPRINT_SERVICE);
        if (!fpm.isHardwareDetected()) {
            //Toast.makeText();//No fingerprint sensor.
            //return;
        }*/

        Preference pref = getPreferenceByKey(root, KEY_FUNCTION_MANAGER);
        if (pref != null) {
            //pref.setOnPreferenceChangeListener(this);
            //pref.setOnPreferenceClickListener(this);
            Intent iFingerprint = new Intent();
            /*final List<Fingerprint> items = fpm.getEnrolledFingerprints();
            final int fingerprintCount = items != null ? items.size() : 0;*/
            final String clazz;
            /*if (fingerprintCount > 0) {*/
            clazz = PrizeFaceRelevantSettings.class.getName();
            iFingerprint.setClassName("com.android.settings", clazz);
            /*} else {
                //clazz = FingerprintEnrollIntroduction.class.getName();
                iFingerprint = launchChooseLock();
            }*/
            iFingerprint.putExtra(ChooseLockSettingsHelper.EXTRA_KEY_IS_FACEPRINT, true);
            pref.setIntent(iFingerprint);
        }
    }

    private Preference getPreferenceByKey(PreferenceScreen root, String key) {
        Preference pref = root.findPreference(key);
        return pref;
    }

}
