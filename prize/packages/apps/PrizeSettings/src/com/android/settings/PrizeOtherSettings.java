/*******************************************
create by liuweiquan 20160708
 *********************************************/
package com.android.settings;

import android.os.Bundle;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceCategory;
import android.support.v7.preference.PreferenceScreen;

import com.mediatek.common.prizeoption.PrizeOption;
import com.android.internal.logging.MetricsLogger;
import com.android.internal.logging.MetricsProto.MetricsEvent;

import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settings.search.Indexable;
import com.android.settings.search.SearchIndexableRaw;
import java.util.List;
import java.util.ArrayList;
import android.content.Context;
public class PrizeOtherSettings extends SettingsPreferenceFragment implements Indexable{
	private static final String TAG = "PrizeOtherSettings";
	
	private static final String KEY_DATE_TIME= "date_time_settings";
	private static final String KEY_LANGUAGE = "language_settings";
	private static final String KEY_STORAGE = "storage_settings";
	private static final String KEY_APPLICATION_MANAGEMENT = "application_management_settings";
	private static final String KEY_PURE_MANAGE = "pure_manage_settings";
	private static final String KEY_ACCESSIBILITY = "accessibility_settings";
	private static final String KEY_PRINT = "print_settings";
    private static final String KEY_PRIVACY = "privacy_settings";

	private static final String COMMON_SETTINGS_CATEGORY = "common_settings_category";
	private static final String OTHER_SETTINGS_CATEGORY = "other_settings_category";
	
	private Preference mDateTimePref;
	private Preference mLanguagePref;
	private Preference mStoragePref;
	private Preference mApplicationManagementPref;
	private Preference mPureManagePref;
	private Preference mAccessibilityPref;
	private Preference mPrintPref;
	private Preference mPrivacyPref;
	
	private PreferenceCategory mCommonSettingsCategory;
	
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.prize_other_settings);
		initPreference();
	}

	private void initPreference() {
		// TODO Auto-generated method stub
		mCommonSettingsCategory=(PreferenceCategory) findPreference(COMMON_SETTINGS_CATEGORY);
		mPureManagePref=findPreference(KEY_PURE_MANAGE);
		if (!PrizeOption.PRIZE_PURE_BACKGROUND) {
			mCommonSettingsCategory.removePreference(mPureManagePref);
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
	// Enable indexing of searchable data
    public static final Indexable.SearchIndexProvider SEARCH_INDEX_DATA_PROVIDER =
        new BaseSearchIndexProvider() {
			@Override
            public List<SearchIndexableRaw> getRawDataToIndex(Context context, boolean enabled) {
                final List<SearchIndexableRaw> indexables = new ArrayList<SearchIndexableRaw>();
				SearchIndexableRaw indexable = new SearchIndexableRaw(context);
				final String screenTitle = context.getString(R.string.other_settings_title);
				indexable.title = context.getString(R.string.other_settings_title);
				indexable.screenTitle = screenTitle;
				indexables.add(indexable);
				return indexables;
            }
        };
}
