package com.android.launcher3.prefernce;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceScreen;
import android.preference.SwitchPreference;
import android.util.Log;

import com.android.launcher3.LauncherAppState;
/*import com.android.launcher3.LauncherExtPlugin;*/
import com.android.launcher3.PagedView;
import com.android.launcher3.R;
import com.android.launcher3.notify.PreferencesManager;

public class LauncherPrefercensActivity extends PreferenceActivity implements
		Preference.OnPreferenceChangeListener {

	public static final String SET_IS_SUPPORT_CIRCULAR = "set_is_support_circular";
	public static final String SET_ISSUPPORT_FOLDER_EFFECT = "set_is_support_effect";
	private ListPreference mListFolderEffectPreference = null;

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.preference.PreferenceActivity#onCreate(android.os.Bundle)
	 * 设置桌面一些开关
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.addPreferencesFromResource(R.xml.launcher_style_settings);
		SwitchPreference switchCycleSlide = (SwitchPreference) findPreference(SET_IS_SUPPORT_CIRCULAR);
		switchCycleSlide.setChecked(PreferencesManager.getKeyCycle(this));
		switchCycleSlide
				.setOnPreferenceChangeListener((OnPreferenceChangeListener) this);

		mListFolderEffectPreference = (ListPreference) this
				.findPreference(SET_ISSUPPORT_FOLDER_EFFECT);
		this.getPreferenceScreen().removePreference(mListFolderEffectPreference);
		if (mListFolderEffectPreference !=null) {
			mListFolderEffectPreference.setOnPreferenceChangeListener(this);
		}
	}

	@SuppressWarnings("deprecation")
	@Override
	public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen,
			Preference preference) {
		return super.onPreferenceTreeClick(preferenceScreen, preference);
	}

	@Override
	public boolean onPreferenceChange(Preference preference, Object objValue) {
		String key = preference.getKey();
		if (SET_IS_SUPPORT_CIRCULAR.equals(key)) {
			PreferencesManager.setCurrentCycle(this,
					Boolean.parseBoolean(objValue.toString()));

		}
		return true;
	}

}
