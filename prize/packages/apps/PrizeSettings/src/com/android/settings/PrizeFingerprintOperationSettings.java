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
import android.widget.Toast;

import com.android.internal.logging.MetricsLogger;
import com.android.internal.logging.MetricsProto.MetricsEvent;
import com.android.settings.fingerprint.PrizeFingerRelevantSettings;
import com.android.settings.fingerprint.FingerprintEnrollIntroduction;
import com.android.settings.fingerprint.FingerprintSettings;

import java.util.List;
/* prize-add-search function-lijimeng-20170412-start*/
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settings.search.Indexable;
import com.android.settings.search.SearchIndexableRaw;
import java.util.List;
import java.util.ArrayList;
/* prize-add-search function-lijimeng-20170412-end*/

/**
 * Created by wangzhong on 2016/7/5.
 */
public class PrizeFingerprintOperationSettings extends SettingsPreferenceFragment
        implements Preference.OnPreferenceChangeListener, Preference.OnPreferenceClickListener, Indexable {

    private static final String TAG = "PrizeFingerprintOperationSettings";

    public static final String KEY_FUNCTION_CATEGORY = "prize_fingerprint_operation_function";
    public static final String KEY_FUNCTION_MANAGER = "prize_fingerprint_operation_function_manager";

    public static final String KEY_LONGPRESS_CATEGORY = "prize_fingerprint_operation_longpress";
    public static final String KEY_LONGPRESS_INCALL = "prize_fingerprint_operation_longpress_incall";
    public static final String KEY_LONGPRESS_TAKE = "prize_fingerprint_operation_longpress_take";
    public static final String KEY_LONGPRESS_CALLRECORD = "prize_fingerprint_operation_longpress_callrecord";
    public static final String KEY_LONGPRESS_SCREENCAPTURE = "prize_fingerprint_operation_longpress_screencapture";
    public static final String KEY_LONGPRESS_RETURNHOME = "prize_fingerprint_operation_longpress_returnhome";
    public static final String KEY_LONGPRESS_NOTICE = "prize_fingerprint_operation_longpress_notice";

    public static final String KEY_CLICK_CATEGORY = "prize_fingerprint_operation_click";
    public static final String KEY_CLICK_BACK = "prize_fingerprint_operation_click_back";
    public static final String KEY_CLICK_SLIDELAUNCHER = "prize_fingerprint_operation_click_slidelauncher";
    public static final String KEY_CLICK_MUSIC = "prize_fingerprint_operation_click_music";
    public static final String KEY_CLICK_VIDEO = "prize_fingerprint_operation_click_video";

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
        addPreferencesFromResource(R.xml.prize_fingerprint_operation_settings);
        root = getPreferenceScreen();

        /** Fingerprint manager */
        FingerprintManager fpm = (FingerprintManager) getActivity().getSystemService(
                Context.FINGERPRINT_SERVICE);
        if (!fpm.isHardwareDetected()) {
            //Toast.makeText();//No fingerprint sensor.
            //return;
        }

        Preference pref = getPreferenceByKey(root, KEY_FUNCTION_MANAGER);
        if (pref != null) {
            //pref.setOnPreferenceChangeListener(this);
            //pref.setOnPreferenceClickListener(this);
            Intent iFingerprint = new Intent();
            final List<Fingerprint> items = fpm.getEnrolledFingerprints();
            final int fingerprintCount = items != null ? items.size() : 0;
            final String clazz;
            /*if (fingerprintCount > 0) {*/
                clazz = PrizeFingerRelevantSettings.class.getName();
                iFingerprint.setClassName("com.android.settings", clazz);
            /*} else {
                //clazz = FingerprintEnrollIntroduction.class.getName();
                iFingerprint = launchChooseLock();
            }*/
            iFingerprint.putExtra(ChooseLockSettingsHelper.EXTRA_KEY_IS_FINGERPRINT, true);
            pref.setIntent(iFingerprint);
        }
    }

    private Preference getPreferenceByKey(PreferenceScreen root, String key) {
        Preference pref = root.findPreference(key);
        return pref;
    }

    private Intent launchChooseLock() {
        Intent intent = new Intent(this.getActivity(), ChooseLockGeneric.class);
        long challenge = this.getActivity().getSystemService(FingerprintManager.class).preEnroll();
        intent.putExtra(ChooseLockGeneric.ChooseLockGenericFragment.MINIMUM_QUALITY_KEY,
                DevicePolicyManager.PASSWORD_QUALITY_SOMETHING);
        intent.putExtra(ChooseLockGeneric.ChooseLockGenericFragment.HIDE_DISABLED_PREFS, true);
        intent.putExtra(ChooseLockSettingsHelper.EXTRA_KEY_HAS_CHALLENGE, true);
        intent.putExtra(ChooseLockSettingsHelper.EXTRA_KEY_CHALLENGE, challenge);
        intent.putExtra(ChooseLockSettingsHelper.EXTRA_KEY_FOR_FINGERPRINT, true);
        return intent;
    }

    // add search function lijimeng 20170412
    public static final Indexable.SearchIndexProvider SEARCH_INDEX_DATA_PROVIDER =
            new BaseSearchIndexProvider() {
                @Override
                public List<SearchIndexableRaw> getRawDataToIndex(Context context, boolean enabled) {
                    final List<SearchIndexableRaw> indexables = new ArrayList<SearchIndexableRaw>();
                    SearchIndexableRaw indexable = new SearchIndexableRaw(context);
                    FingerprintManager fpm = (FingerprintManager) context.getSystemService(
                            Context.FINGERPRINT_SERVICE);
                    if(!fpm.isHardwareDetected()){
                        return indexables;
                    }
                    final String screenTitle = context.getString(R.string.fingerprint_settings_title);
                    indexable.title = context.getString(R.string.fingerprint_settings_title);
                    indexable.screenTitle = screenTitle;
                    indexables.add(indexable);
                    SearchIndexableRaw fmanager = new SearchIndexableRaw(context);
                    final String fm = context.getString(R.string.fingerprint_settings_title);
                    fmanager.title = context.getString(R.string.prize_fingerprint_management_title);
                    fmanager.screenTitle = fm;
                    indexables.add(fmanager);
                    return indexables;
                }
            };
}
