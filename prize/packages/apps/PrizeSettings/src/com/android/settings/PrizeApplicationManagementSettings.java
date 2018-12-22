/*******************************************
create by liuweiquan 20160708
 *********************************************/
package com.android.settings;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;

import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceScreen;

import com.mediatek.common.prizeoption.PrizeOption;
import com.android.internal.logging.MetricsLogger;
import com.android.internal.logging.MetricsProto.MetricsEvent;

public class PrizeApplicationManagementSettings extends SettingsPreferenceFragment {
	private static final String TAG = "PrizeApplicationManagementSettings";
	
	private static final String KEY_APPLICATION= "application_settings";
	private static final String KEY_APP_NET_CONTROL = "app_net_control_settings";
	private static final String KEY_AUTO_BOOT_APP_MANAGE = "auto_boot_app_manage_settings";
	
	private Preference mApplicationPref;
	private Preference mAppNetControlPref;
	private Preference mAutoBootAppManagePref;
	
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.prize_application_management);
		initPreferences();
	}

	private void initPreferences() {
		// TODO Auto-generated method stub
		mAppNetControlPref=findPreference(KEY_APP_NET_CONTROL);
		Intent intent = mAppNetControlPref.getIntent();
		if (mAppNetControlPref.getContext().getPackageManager().resolveActivity(intent, 0) == null) {		            
			getPreferenceScreen().removePreference(mAppNetControlPref);
		}
	}

	@Override
	public void onResume() {
		super.onResume();
	}

	@Override
	public void onPause() {
		super.onPause();
	}
	
	protected int getMetricsCategory() {
        return MetricsEvent.PRIVACY;
    }
}
