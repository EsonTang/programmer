/*
* Copyright (C) 2014 MediaTek Inc.
* Modification based on code covered by the mentioned copyright
* and/or permission notice(s).
/*
* created by prize-linkh at 20150724
*/

package com.android.settings;

import android.os.Bundle;
import android.app.Activity;
import android.preference.Preference;
import android.preference.SwitchPreference;
import android.preference.PreferenceScreen;
import android.provider.Settings;
import android.preference.PreferenceCategory;
import com.mediatek.common.prizeoption.PrizeOption;
import com.android.internal.logging.MetricsLogger;

public class NavigationBarSettings extends SettingsPreferenceFragment implements
        Preference.OnPreferenceChangeListener, NavBarStylePreference.OnClickListener {    
    private static final String TAG = "NavigationBarSettings";
    
    private static final String KEY_HIDE_NAVBAR = "hide_navbar";
    private static final String KEY_SELECT_NAVBAR_STYLE = "select_navbar_style";
    private static final String KEY_NAVBAR_STYLE_PREFIX = "navbar_style_";

    private boolean mSupportNavBarStyle;
    private boolean mSupportHidingNavBar = PrizeOption.PRIZE_DYNAMICALLY_HIDE_NAVBAR;
    PreferenceCategory mSelectNavBarStylePrefCat;
    private int mCurrentStyle = 0;
    private NavBarStylePreference mPreSelectedNavBarStylePreference;
    private boolean mAllowHidingNavBar = true;
    private SwitchPreference mHideNavBarSwitchPref;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final Activity activity = getActivity();

        mSupportNavBarStyle = activity.getResources().getBoolean(
                com.prize.internal.R.bool.support_navbar_style);
        MLog.d(TAG, "mSupportNavBarStyle ? " + mSupportNavBarStyle + ", mSupportHidingNavBar ? " + mSupportHidingNavBar);
        
        addPreferencesFromResource(R.xml.navigation_bar_prize);

        mCurrentStyle = Settings.System.getInt(
                            getActivity().getContentResolver(), 
                            Settings.System.PRIZE_NAVIGATION_BAR_STYLE,
                            mCurrentStyle);

        MLog.d(TAG, "mCurrentStyle ? " + mCurrentStyle);

        int allow = Settings.System.getInt(
                            getActivity().getContentResolver(), 
                            Settings.System.PRIZE_ALLOW_HIDING_NAVBAR,
                            1);
        mAllowHidingNavBar = (allow == 1) ? true : false;
        MLog.d(TAG, "mAllowHidingNavBar ? " + mAllowHidingNavBar);

        
        initAllPreference();
    }

    @Override
    protected int getMetricsCategory() {
        return MetricsLogger.PRIVACY;
    }

    private void initAllPreference() {
        mHideNavBarSwitchPref = (SwitchPreference)findPreference(KEY_HIDE_NAVBAR);
        if(mSupportHidingNavBar) {
            mHideNavBarSwitchPref.setChecked(mAllowHidingNavBar);
            mHideNavBarSwitchPref.setOnPreferenceChangeListener(this);
        } else {
            getPreferenceScreen().removePreference(mHideNavBarSwitchPref);
        }

        mSelectNavBarStylePrefCat = (PreferenceCategory)findPreference(KEY_SELECT_NAVBAR_STYLE);
        if(mSupportNavBarStyle) {
            int N = mSelectNavBarStylePrefCat.getPreferenceCount();
            for(int i = 0; i < N; ++i) {
                Preference pref = mSelectNavBarStylePrefCat.getPreference(i);
                if(pref instanceof NavBarStylePreference) {
                    NavBarStylePreference nbsPref = (NavBarStylePreference)pref;
                    nbsPref.setOnClickListener(this);
                    
                    if(mCurrentStyle >= 0 && mCurrentStyle == nbsPref.getStyleIndex()) {
                        nbsPref.setChecked(true);
                        mPreSelectedNavBarStylePreference = nbsPref;
                    } else {
                        nbsPref.setChecked(false);
                    }
                }
            }
        } else {
            getPreferenceScreen().removePreference(mSelectNavBarStylePrefCat);
        } 
    }

    private void updateNavBarStylePreferences(NavBarStylePreference activedPref) {
        if(activedPref != null) {
            if(mPreSelectedNavBarStylePreference != null) {
                mPreSelectedNavBarStylePreference.setChecked(false);
            }
            mPreSelectedNavBarStylePreference = activedPref;
        }
    }
    
    @Override
    public boolean onPreferenceChange(Preference preference, Object objValue) {
        MLog.d(TAG, "onPreferenceChange() pref ? " + preference);
        if(mHideNavBarSwitchPref == preference) {
            boolean value = (boolean)objValue;
            int allow = value ? 1 : 0;
            Settings.System.putInt(
                            getActivity().getContentResolver(), 
                            Settings.System.PRIZE_ALLOW_HIDING_NAVBAR,
                            allow);
            return true;
        }
        return false;
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        //MLog.d(TAG, "onPreferenceTreeClick(). pref ? " + preference);
        return false;
    }    

    @Override
     public void onRadioButtonClicked(NavBarStylePreference pref) {
        MLog.d(TAG, "onRadioButtonClicked(). which ? " + pref);
        int styleIndex = pref.getStyleIndex();
        if(styleIndex < 0) {
            MLog.d(TAG, "invalid style index. Ignore.");
        } else if(mCurrentStyle == styleIndex) {
            MLog.d(TAG, "same style index. Ignore.");
        } else {
            updateNavBarStylePreferences(pref);

            Settings.System.putInt(
                            getActivity().getContentResolver(), 
                            Settings.System.PRIZE_NAVIGATION_BAR_STYLE,
                            styleIndex);
            mCurrentStyle = styleIndex;
            MLog.d(TAG, "style " + styleIndex + " saved.");
        }
    }
}
