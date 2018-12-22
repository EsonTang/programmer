/*******************************************
create by liuweiquan 20160708
 *********************************************/
package com.android.settings;

import android.content.Intent;
import android.os.Bundle;
import android.os.UserHandle;
import android.support.v14.preference.SwitchPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceCategory;
import android.support.v7.preference.PreferenceScreen;

import android.provider.Settings;
import android.util.Log;

import com.android.internal.logging.MetricsLogger;
import com.android.internal.logging.MetricsProto.MetricsEvent;
import com.mediatek.common.prizeoption.PrizeOption;

/// add new menu to search db liup 20160622 start
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settings.search.Indexable;
import com.android.settings.search.SearchIndexableRaw;
import java.util.List;
import java.util.ArrayList;
import android.content.Context;
import android.provider.SearchIndexableResource;
import com.android.internal.widget.LockPatternUtils;
/// add new menu to search db liup 20160622 end

public class PrizeNoticeStatusBarSettings extends SettingsPreferenceFragment 
	implements Preference.OnPreferenceChangeListener, Indexable{/////add Indexable liup 20160622
	private static final String TAG = "PrizeNoticeStatusBarSettings";
	
	private static final String KEY_NOTICE_CENTRE = "notification_centre_settings";
	private static final String KEY_LOCKSCREEN_DROP_DOWN_STATUSBAR = "lockscreen_drop_down_statusbar";
	private static final String KEY_LOCKSCREEN_BRIGHT_SCREEN = "lockscreen_bright_screen";
    private static final String KEY_SHOW_BATTERY_PERCENTAGE = "battery_percentage";
    private static final String KEY_REAL_TIME_NETWORK_SPEED = "real_time_network_speed";
    
	private static final String STATUSBAR_SETTINGS_CATEGORY = "statusbar_settings_category";
	private static final String OTHER_SETTINGS_CATEGORY = "other_category";
	private static final int MY_USER_ID = UserHandle.myUserId();
	private Preference mNotificationCentrePref;
	private SwitchPreference mShowBatteryPercentagePrf;
	private SwitchPreference mRealTimeNetworkSpeedPrf;
	private SwitchPreference mLockDropDownStatusbarPrf;
	private SwitchPreference mLockBrightScreenPrf;
	
	private PreferenceCategory mStatusBarCategory;
	private PreferenceCategory mOtherCategory;
	
	private LockPatternUtils mLockPatternUtils;
	
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.prize_notice_statusbar_settings);
		mLockPatternUtils = new LockPatternUtils(getContext());
		initPreference();
	}

	@Override
	public void onResume() {
		super.onResume();
		if(mLockPatternUtils != null && mLockPatternUtils.isSecure(MY_USER_ID)){
			mLockDropDownStatusbarPrf.setChecked(false);
			mLockDropDownStatusbarPrf.setEnabled(false);
		}else{
			mLockDropDownStatusbarPrf.setEnabled(true);
		}
	}

	@Override
	public void onPause() {
		super.onPause();
	}
	
	protected int getMetricsCategory() {
        return MetricsEvent.PRIVACY;
    }
	
	private void initPreference(){
		mStatusBarCategory = (PreferenceCategory) findPreference(STATUSBAR_SETTINGS_CATEGORY);
		mOtherCategory = (PreferenceCategory) findPreference(OTHER_SETTINGS_CATEGORY);
		
        mShowBatteryPercentagePrf = new SwitchPreference(getActivity());
        mShowBatteryPercentagePrf.setKey(KEY_SHOW_BATTERY_PERCENTAGE);
        mShowBatteryPercentagePrf.setTitle(R.string.show_battery_percentage_title);
        mShowBatteryPercentagePrf.setOrder(-4);
        if(PrizeOption.PRIZE_SYSTEMUI_BATTERY_METER){
            mShowBatteryPercentagePrf.setChecked(Settings.System.getInt(getActivity().getContentResolver(),
                    "battery_percentage_enabled", 0) == 1);
        }else{
        	mShowBatteryPercentagePrf.setChecked(false);
        	mShowBatteryPercentagePrf.setEnabled(false);
        }
        mStatusBarCategory.addPreference(mShowBatteryPercentagePrf);
        
        mRealTimeNetworkSpeedPrf = new SwitchPreference(getActivity());
        mRealTimeNetworkSpeedPrf.setKey(KEY_REAL_TIME_NETWORK_SPEED);
        mRealTimeNetworkSpeedPrf.setTitle(R.string.real_time_network_speed);       
        mRealTimeNetworkSpeedPrf.setChecked(Settings.System.getInt(getActivity().getContentResolver(),
        		Settings.System.PRIZE_REAL_TIME_NETWORK_SPEED_SWITCH, 0) == 1);
        mRealTimeNetworkSpeedPrf.setOnPreferenceChangeListener(this);	
        mStatusBarCategory.addPreference(mRealTimeNetworkSpeedPrf);                      
        
        mLockDropDownStatusbarPrf = (SwitchPreference) findPreference(KEY_LOCKSCREEN_DROP_DOWN_STATUSBAR);
		mLockDropDownStatusbarPrf.setSummary(R.string.prize_lockscreen_drop_down_statusbar_summary);
        int bDropDownStatusbar = Settings.System.getInt(getContentResolver(),
				Settings.System.PRIZE_ALLOW_DROPDOWN_NOTIFICATIONBAR_SWITCH, 0);
		if (bDropDownStatusbar == 1) {
			mLockDropDownStatusbarPrf.setChecked(true);
		} else {
			mLockDropDownStatusbarPrf.setChecked(false);
		}
		mLockDropDownStatusbarPrf.setOnPreferenceChangeListener(this);	
        
        mLockBrightScreenPrf = (SwitchPreference) findPreference(KEY_LOCKSCREEN_BRIGHT_SCREEN);
        int bScreenOnForNotification = Settings.System.getInt(getContentResolver(),
				Settings.System.PRIZE_SCREEN_ON_FOR_NOTIFICATION_SWITCH, 0);
		if (bScreenOnForNotification == 1) {
			mLockBrightScreenPrf.setChecked(true);
		} else {
			mLockBrightScreenPrf.setChecked(false);
		}
		mLockBrightScreenPrf.setOnPreferenceChangeListener(this);	
		mOtherCategory.removePreference(mLockBrightScreenPrf);
	}
	
	@Override
    public boolean onPreferenceChange(Preference preference, Object objValue) {
		if(preference == mLockDropDownStatusbarPrf){			
			boolean bDropDownStatusbar = (Boolean) objValue;
			Settings.System.putInt(getContentResolver(),
					Settings.System.PRIZE_ALLOW_DROPDOWN_NOTIFICATIONBAR_SWITCH, bDropDownStatusbar ? 1 : 0);
			mLockDropDownStatusbarPrf.setChecked(bDropDownStatusbar);
			Log.d(TAG,"onPreferenceChange "+preference);
		}
		if(preference == mLockBrightScreenPrf){
			boolean bScreenOnForNotification = (Boolean) objValue;
			Settings.System.putInt(getContentResolver(),
					Settings.System.PRIZE_SCREEN_ON_FOR_NOTIFICATION_SWITCH, bScreenOnForNotification ? 1 : 0);
			mLockBrightScreenPrf.setChecked(bScreenOnForNotification);
			Log.d(TAG,"onPreferenceChange "+preference);
		}
		if(preference == mRealTimeNetworkSpeedPrf){
			boolean brtNetworkSpeed = (Boolean) objValue;
			Settings.System.putInt(getContentResolver(),
					Settings.System.PRIZE_REAL_TIME_NETWORK_SPEED_SWITCH, brtNetworkSpeed ? 1 : 0);
			mRealTimeNetworkSpeedPrf.setChecked(brtNetworkSpeed);
			Log.d(TAG,"onPreferenceChange "+preference);
		}
		return true;
	}

	@Override
	public boolean onPreferenceTreeClick(Preference preference) {
		// TODO Auto-generated method stub
		if(preference == mShowBatteryPercentagePrf){
			if (preference instanceof SwitchPreference) {
                SwitchPreference pref = (SwitchPreference) preference;
                int bgState = pref.isChecked() ? 1 : 0;
                Log.d(TAG, "background power saving state: " + bgState);
                Settings.System.putInt(getActivity().getContentResolver(),
                        "battery_percentage_enabled", bgState);
                if (mShowBatteryPercentagePrf != null) {
                    mShowBatteryPercentagePrf.setChecked(pref.isChecked());
                }
            }
			return true;
		}
		return super.onPreferenceTreeClick(preference);
	}
	
		/// add new menu to search db liup 20160622 start
	public static final SearchIndexProvider SEARCH_INDEX_DATA_PROVIDER =
        new BaseSearchIndexProvider() {
            @Override
			public List<SearchIndexableResource> getXmlResourcesToIndex(Context context, boolean enabled) {
				List<SearchIndexableResource> result = new ArrayList<SearchIndexableResource>();
				SearchIndexableResource sir = new SearchIndexableResource(context);
				sir.xmlResId = R.xml.prize_notice_statusbar_settings;
				result.add(sir);
				return result;
			}
			@Override
            public List<SearchIndexableRaw> getRawDataToIndex(Context context, boolean enabled) {
                final List<SearchIndexableRaw> indexables = new ArrayList<SearchIndexableRaw>();
				SearchIndexableRaw indexable = new SearchIndexableRaw(context);
				
				/*final String screenTitle = context.getString(R.string.notification_statusbar_settings_title);
				indexable.title = context.getString(R.string.notification_statusbar_settings_title);
				indexable.screenTitle = screenTitle;
				indexables.add(indexable);*/
				final String screenTitle = context.getString(R.string.notification_statusbar_settings_title);
				indexable.title = context.getString(R.string.real_time_network_speed);
				indexable.screenTitle = screenTitle;
				indexables.add(indexable);
				SearchIndexableRaw batteryIndexable = new SearchIndexableRaw(context);
				
				batteryIndexable.title = context.getString(R.string.show_battery_percentage_title);
				batteryIndexable.screenTitle = screenTitle;
				indexables.add(batteryIndexable);
				return indexables;
            }
			@Override
			public List<String> getNonIndexableKeys(Context context) {
				List<String> list = new ArrayList<String>();
				list.add(KEY_LOCKSCREEN_BRIGHT_SCREEN);
				return list;
			}
        };
	/// add new menu to search db liup 20160622 end
}
