/*******************************************
 * 版权所有©2015,深圳市铂睿智恒科技有限公司
 * 
 * 内容摘要：法律信息
 * 当前版本：V1.0
 * 作    者：黄典俊
 * 完成日期：2015-07-08
 *

 *********************************************/
package com.android.settings;

import android.content.Intent;
import android.os.Bundle;

import android.support.v7.preference.Preference;
import android.support.v7.preference.Preference.OnPreferenceClickListener;
import android.support.v7.preference.PreferenceScreen;

import android.provider.Settings;
import android.view.View.OnClickListener;
import com.android.internal.logging.MetricsLogger;
import com.android.internal.logging.MetricsProto.MetricsEvent;

public class PrizeLegalInformationSettings extends SettingsPreferenceFragment implements OnPreferenceClickListener {

    private static final String KEY_PRIZE_LICENSE = "prize_license";
	private static final String KEY_WALLPAPER_ATTRIBUTIONS = "wallpaper_attributions";
    
    private Preference mPrizeLicense;
	private Preference mWallpaperAttributions;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.prize_legal_information);	
        initializeAllPreferences();
    }
 	@Override
    protected int getMetricsCategory() {
        return MetricsEvent.PRIVACY;
    }
    /**
     * 方法描述： PrizeLegalInformaintion-initial-UI
     * @author huangdianjun
     */
    private void initializeAllPreferences(){

        final PreferenceScreen root = getPreferenceScreen();
        mPrizeLicense = root.findPreference(KEY_PRIZE_LICENSE);
        mPrizeLicense.setOnPreferenceClickListener(this);
        mWallpaperAttributions = root.findPreference(KEY_WALLPAPER_ATTRIBUTIONS);
        mWallpaperAttributions.setOnPreferenceClickListener(this);
    }

    @Override
    public boolean onPreferenceClick(Preference preference) {
    	if (preference == mPrizeLicense){
			final Intent intent = new Intent();
            intent.setClass(getActivity(), SettingsLicenseActivity.class);
			startActivity(intent);
		}
        return true;
    }
  
}
