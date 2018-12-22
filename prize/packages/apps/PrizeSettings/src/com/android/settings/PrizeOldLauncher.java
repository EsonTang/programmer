package com.android.settings;

import android.os.Bundle;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.support.v7.preference.Preference;
import android.support.v7.preference.Preference.OnPreferenceClickListener;
import android.support.v7.preference.PreferenceScreen;
import android.support.v14.preference.SwitchPreference;
import android.provider.Settings;
import com.android.internal.logging.MetricsProto.MetricsEvent;
import android.view.View;
import com.android.settings.R;
import android.widget.ImageView;
import android.content.Intent;
import android.util.Log;
/* prize-add-search function-lijimeng-20170412-start*/
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settings.search.Indexable;
import com.android.settings.search.SearchIndexableRaw;
import java.util.List;
import java.util.ArrayList;
import com.mediatek.common.prizeoption.PrizeOption;
/* prize-add-search function-lijimeng-20170412-end*/
public class PrizeOldLauncher extends SettingsPreferenceFragment implements
        Preference.OnPreferenceChangeListener, OnPreferenceClickListener, Indexable {

    private static final String KEY_PRIZE_OLD_LAUNCHER = "prize_old_launcher";
    private SwitchPreference mPrizeOldLauncherPreference;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
		
        addPreferencesFromResource(R.xml.prize_old_launcher);
        mPrizeOldLauncherPreference = (SwitchPreference)findPreference(KEY_PRIZE_OLD_LAUNCHER);
		updatePrizeOldLauncherEnable();
		
        mPrizeOldLauncherPreference.setOnPreferenceChangeListener(this);
        mPrizeOldLauncherPreference.setOnPreferenceClickListener(this);
    }

    @Override
    protected int getMetricsCategory() {
        return MetricsEvent.DISPLAY;
    }
	
	
    @Override
    public void onResume() {
        super.onResume();
    }

	private void updatePrizeOldLauncherEnable() {
		final boolean bOldLauncher = Settings.System.getInt(getContentResolver(),Settings.System.PRIZE_OLD_LAUNCHER,0) == 1;
		mPrizeOldLauncherPreference.setChecked(bOldLauncher);
	}
	
    @Override
    public boolean onPreferenceTreeClick(Preference preference) {
        return super.onPreferenceTreeClick(preference);
		
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object objValue) {
        if (preference == mPrizeOldLauncherPreference) {
            boolean value = (Boolean) objValue;
            Settings.System.putInt(getContentResolver(), Settings.System.PRIZE_OLD_LAUNCHER, value ? 1 : 0);
            Settings.System.putInt(getContentResolver(), Settings.System.PRIZE_VOICE_KEY, value ? 1 : 0);
			/* prize-delete- bugid 32816-lijimeng-20170421-start*/
			// Settings.System.putInt(getContentResolver(), Settings.System.PRIZE_VOICE_DIALER_KEY, value ? 1 : 0);
			// Settings.System.putInt(getContentResolver(), Settings.System.PRIZE_VOICE_CALL_KEY, value ? 1 : 0);
			// Settings.System.putInt(getContentResolver(), Settings.System.PRIZE_VOICE_MMS_KEY, value ? 1 : 0);
			/* prize-delete- bugid 32816-lijimeng-20170421-end*/
			mPrizeOldLauncherPreference.setChecked(value);
			Intent intent = new Intent();
			intent.putExtra("value", value);
     		intent.setAction("prize.old.launcher.BOARDCAST");
			getContext().sendBroadcast(intent);
        }
        return true;
    }

    @Override
    public boolean onPreferenceClick(Preference preference) {
        return false;
    }
	// add search function lijimeng 20170412
	 public static final Indexable.SearchIndexProvider SEARCH_INDEX_DATA_PROVIDER =
        new BaseSearchIndexProvider() {
			@Override
            public List<SearchIndexableRaw> getRawDataToIndex(Context context, boolean enabled) {
                final List<SearchIndexableRaw> indexables = new ArrayList<SearchIndexableRaw>();
				SearchIndexableRaw indexable = new SearchIndexableRaw(context);
				if(PrizeOption.PRIZE_OLD_LAUNCHER){
					final String screenTitle = context.getString(R.string.prize_old_launcher);
					indexable.title = context.getString(R.string.prize_old_launcher);
					indexable.screenTitle = screenTitle;
					indexables.add(indexable);
				}
				return indexables;
            }
        };
}
