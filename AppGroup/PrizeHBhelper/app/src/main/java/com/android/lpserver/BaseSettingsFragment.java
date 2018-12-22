package com.android.lpserver;

import android.os.Bundle;
import android.preference.PreferenceFragment;

public class BaseSettingsFragment extends PreferenceFragment {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getPreferenceManager().setSharedPreferencesName(Config.PREFERENCE_NAME);
    }
}
