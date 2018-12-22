/*
* Copyright (C) 2014 MediaTek Inc.
* Modification based on code covered by the mentioned copyright
* and/or permission notice(s).
*/
/*
 * Copyright (C) 2010 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.calendar;

import android.app.ActionBar;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.backup.BackupManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Vibrator;
import android.preference.PreferenceActivity;
import android.preference.PreferenceCategory;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.preference.RingtonePreference;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.provider.CalendarContract;
import android.provider.SearchRecentSuggestions;
import android.provider.CalendarContract.CalendarCache;
import android.provider.Settings;
import android.text.TextUtils;
import android.text.format.Time;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.calendar.R;
import com.android.calendar.alerts.AlertReceiver;
import com.android.timezonepicker.TimeZoneInfo;
import com.android.timezonepicker.TimeZonePickerDialog;
import com.android.timezonepicker.TimeZonePickerDialog.OnTimeZoneSetListener;
import com.mediatek.calendar.MTKToast;
import com.android.timezonepicker.TimeZonePickerUtils;

import java.lang.reflect.Field;

public class PrizeSettingsActivity extends PreferenceActivity implements
OnSharedPreferenceChangeListener, OnPreferenceChangeListener, OnTimeZoneSetListener  {
    
	// The name of the shared preferences file. This name must be maintained for historical
    // reasons, as it's what PreferenceManager assigned the first time the file was created.
    static final String SHARED_PREFS_NAME = "com.android.calendar_preferences";
    static final String SHARED_PREFS_NAME_NO_BACKUP = "com.android.calendar_preferences_no_backup";

    private static final String FRAG_TAG_TIME_ZONE_PICKER = "TimeZonePicker";

    // Preference keys
    public static final String KEY_HIDE_DECLINED = "preferences_hide_declined";
    public static final String KEY_WEEK_START_DAY = "preferences_week_start_day";
    public static final String KEY_SHOW_WEEK_NUM = "preferences_show_week_num";
    public static final String KEY_DAYS_PER_WEEK = "preferences_days_per_week";
    public static final String KEY_SKIP_SETUP = "preferences_skip_setup";

    public static final String KEY_CLEAR_SEARCH_HISTORY = "preferences_clear_search_history";

    public static final String KEY_ALERTS_CATEGORY = "preferences_alerts_category";
    public static final String KEY_ALERTS = "preferences_alerts";
    public static final String KEY_ALERTS_VIBRATE = "preferences_alerts_vibrate";
    ///M:Add a new key to save vibrate setting for calendar event reminder.
    public static final String KEY_VIBRATE_FOR_EVENT_REMINDER = "preferences_vibrate_forEventReminder";
    public static final String KEY_ALERTS_RINGTONE = "preferences_alerts_ringtone";
    public static final String KEY_ALERTS_POPUP = "preferences_alerts_popup";

    public static final String KEY_SHOW_CONTROLS = "preferences_show_controls";

    public static final String KEY_DEFAULT_REMINDER = "preferences_default_reminder";
    public static final int NO_REMINDER = -1;
    public static final String NO_REMINDER_STRING = "-1";
    public static final int REMINDER_DEFAULT_TIME = 10; // in minutes

    public static final String KEY_DEFAULT_CELL_HEIGHT = "preferences_default_cell_height";
    public static final String KEY_VERSION = "preferences_version";

    /** Key to SharePreference for default view (CalendarController.ViewType) */
    public static final String KEY_START_VIEW = "preferred_startView";
    /**
     *  Key to SharePreference for default detail view (CalendarController.ViewType)
     *  Typically used by widget
     */
    public static final String KEY_DETAILED_VIEW = "preferred_detailedView";
    public static final String KEY_DEFAULT_CALENDAR = "preference_defaultCalendar";

    // These must be in sync with the array preferences_week_start_day_values
    public static final String WEEK_START_DEFAULT = "-1";
    public static final String WEEK_START_SATURDAY = "7";
    public static final String WEEK_START_SUNDAY = "1";
    public static final String WEEK_START_MONDAY = "2";

    // These keys are kept to enable migrating users from previous versions
    private static final String KEY_ALERTS_TYPE = "preferences_alerts_type";
    private static final String ALERT_TYPE_ALERTS = "0";
    private static final String ALERT_TYPE_STATUS_BAR = "1";
    private static final String ALERT_TYPE_OFF = "2";
    static final String KEY_HOME_TZ_ENABLED = "preferences_home_tz_enabled";
    static final String KEY_HOME_TZ = "preferences_home_tz";
    
    public static final String KEY_PREFRENCE_ACCOUNT = "preferences_account";
    public static final String KEY_PREFRENCE_CALENDAR = "preferences_calendar";
    public static final String KEY_PREFRENCE_QUICK_RESPONSE = "preferences_quick_responses";
    
    public static final String BUILD_VERSION = "build_version";

    // Default preference values
    public static final int DEFAULT_START_VIEW = CalendarController.ViewType.MONTH;//prize-public-standard:change default #ViewType to BUTTON_MONTH_INDEX-pengcancan-20160606
    public static final int DEFAULT_DETAILED_VIEW = CalendarController.ViewType.DAY;
    public static final boolean DEFAULT_SHOW_WEEK_NUM = false;
    // This should match the XML file.
    public static final String DEFAULT_RINGTONE = "content://settings/system/notification_sound";
    
    private CalendarController mController;
    
    Preference mAccount;
    PreferenceScreen mCalendar;
    Preference mQuickResponse;

    CheckBoxPreference mAlert;
    CheckBoxPreference mVibrate;
    RingtonePreference mRingtone;
    CheckBoxPreference mPopup;
    CheckBoxPreference mUseHomeTZ;
    CheckBoxPreference mHideDeclined;
    Preference mHomeTZ;
    TimeZonePickerUtils mTzPickerUtils;
    ListPreference mWeekStart;
    ListPreference mDefaultReminder;
    
    private FragmentManager fm;
    private ActionBar mActionBar;

    private String mTimeZoneId;

    private TextView titleTx;

    /** Return a properly configured SharedPreferences instance */
    public static SharedPreferences getSharedPreferences(Context context) {
        return context.getSharedPreferences(SHARED_PREFS_NAME, Context.MODE_PRIVATE);
    }

    /** Set the default shared preferences in the proper context */
    public static void setDefaultValues(Context context) {
        PreferenceManager.setDefaultValues(context, SHARED_PREFS_NAME, Context.MODE_PRIVATE,
                R.xml.prize_settings_preferences, false);
    }
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
        initStatusBar();
        getActionBarContainer().addView(LayoutInflater.from(this).inflate(R.layout.prize_settings_actionbar, null,false));
        titleTx = (TextView) findViewById(R.id.title);
    	// Make sure to always use the same preferences file regardless of the package name
        // we're running under
        final PreferenceManager preferenceManager = getPreferenceManager();
        final SharedPreferences sharedPreferences = getSharedPreferences(this);
        preferenceManager.setSharedPreferencesName(SHARED_PREFS_NAME);
    	
    	addPreferencesFromResource(R.xml.prize_settings_preferences);
        /*prize-lets lists width adjustment's And to screen -lixing-2015-7-8-start*/
    	mActionBar = getActionBar();
    	mActionBar.setElevation(getResources().getDimension(R.dimen.prizeactionbar_lines));
        ListView listView = (ListView) findViewById(android.R.id.list);
        if(null != listView){
            listView.setPadding( 0, listView.getPaddingTop(), 0, listView.getPaddingBottom());
            //prize-public-standard:add divider line to listview-pengcancan-20160530-start
        	Drawable divider = getDrawable(R.drawable.listview_divider);
        	listView.setDivider(divider);
        	listView.setDividerHeight(0);
			TextView view = new TextView(this);
			view.setLines(0);
			listView.addFooterView(view, null, false);
			listView.setFooterDividersEnabled(false);


            //prize-public-standard:add divider line to listview-pengcancan-20160530-end
        }
        /*prize-lets lists width adjustment's And to screen -lixing-2015-7-8-end*/
        
        final PreferenceScreen preferenceScreen = getPreferenceScreen();
        mAlert = (CheckBoxPreference) preferenceScreen.findPreference(KEY_ALERTS);
        mVibrate = (CheckBoxPreference) preferenceScreen.findPreference(KEY_ALERTS_VIBRATE);
        Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        if (vibrator == null || !vibrator.hasVibrator()) {
            PreferenceCategory mAlertGroup = (PreferenceCategory) preferenceScreen
                    .findPreference(KEY_ALERTS_CATEGORY);
            mAlertGroup.removePreference(mVibrate);
        }

        mRingtone = (RingtonePreference) preferenceScreen.findPreference(KEY_ALERTS_RINGTONE);
        String ringToneUri = Utils.getRingTonePreference(this);
        /// M: Set default ringtone when old ringtone has been deleted from storage
        /// exclude that there is no ringtone choosed. @{
        if (!TextUtils.isEmpty(ringToneUri) &&
                !RingtoneManager.isRingtoneExist(this, Uri.parse(ringToneUri))) {
            ringToneUri = DEFAULT_RINGTONE;
            Utils.setRingTonePreference(this, DEFAULT_RINGTONE);
        }
        /// @}

        // Set the ringToneUri to the backup-able shared pref only so that
        // the Ringtone dialog will open up with the correct value.
        final Editor editor = preferenceScreen.getEditor();
        editor.putString(GeneralPreferences.KEY_ALERTS_RINGTONE, ringToneUri).apply();

        String ringtoneDisplayString = getRingtoneTitleFromUri(this, ringToneUri);
        mRingtone.setSummary(ringtoneDisplayString == null ? "" : ringtoneDisplayString);
        
        mAccount = preferenceScreen.findPreference(KEY_PREFRENCE_ACCOUNT);
        mCalendar = (PreferenceScreen) preferenceScreen.findPreference(KEY_PREFRENCE_CALENDAR);
        mQuickResponse = preferenceScreen.findPreference(KEY_PREFRENCE_QUICK_RESPONSE);
        
        mPopup = (CheckBoxPreference) preferenceScreen.findPreference(KEY_ALERTS_POPUP);
        mUseHomeTZ = (CheckBoxPreference) preferenceScreen.findPreference(KEY_HOME_TZ_ENABLED);
        mHideDeclined = (CheckBoxPreference) preferenceScreen.findPreference(KEY_HIDE_DECLINED);
        mWeekStart = (ListPreference) preferenceScreen.findPreference(KEY_WEEK_START_DAY);
        mDefaultReminder = (ListPreference) preferenceScreen.findPreference(KEY_DEFAULT_REMINDER);
        mHomeTZ = preferenceScreen.findPreference(KEY_HOME_TZ);
        mWeekStart.setSummary(mWeekStart.getEntry());
        mDefaultReminder.setSummary(mDefaultReminder.getEntry());
        
        try {
            final PackageInfo packageInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            findPreference(BUILD_VERSION).setSummary(packageInfo.versionName);
        } catch (NameNotFoundException e) {
            findPreference(BUILD_VERSION).setSummary("?");
        }

        // This triggers an asynchronous call to the provider to refresh the data in shared pref
        mTimeZoneId = Utils.getTimeZone(this, null);

        SharedPreferences prefs = CalendarUtils.getSharedPreferences(this,
                Utils.SHARED_PREFS_NAME);

        // Utils.getTimeZone will return the currentTimeZone instead of the one
        // in the shared_pref if home time zone is disabled. So if home tz is
        // off, we will explicitly read it.
        if (!prefs.getBoolean(KEY_HOME_TZ_ENABLED, false)) {
            mTimeZoneId = prefs.getString(KEY_HOME_TZ, Time.getCurrentTimezone());
        }

        mHomeTZ.setOnPreferenceClickListener(new OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                showTimezoneDialog();
                return true;
            }
        });

        if (mTzPickerUtils == null) {
            mTzPickerUtils = new TimeZonePickerUtils(this);
        }
        CharSequence timezoneName = mTzPickerUtils.getGmtDisplayName(this, mTimeZoneId,
                System.currentTimeMillis(), false);
        mHomeTZ.setSummary(timezoneName != null ? timezoneName : mTimeZoneId);

        TimeZonePickerDialog tzpd = (TimeZonePickerDialog) getFragmentManager()
                .findFragmentByTag(FRAG_TAG_TIME_ZONE_PICKER);
        if (tzpd != null) {
            tzpd.setOnTimeZoneSetListener(this);
        }
        
        mAccount.setOnPreferenceClickListener(new OnPreferenceClickListener() {
			
			@Override
			public boolean onPreferenceClick(Preference preference) {
				Intent nextIntent = new Intent(Settings.ACTION_ADD_ACCOUNT);
	            final String[] array = { "com.android.calendar" };
	            nextIntent.putExtra(Settings.EXTRA_AUTHORITIES, array);
	            nextIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
	            startActivity(nextIntent);
				return true;
			}
		});
        
        mCalendar.setOnPreferenceClickListener(new OnPreferenceClickListener() {
			
			@Override
			public boolean onPreferenceClick(Preference preference) {
				mController.sendEvent(this, CalendarController.EventType.LAUNCH_SELECT_VISIBLE_CALENDARS, null, null,
	                    0, 0);
				return true;
			}
		});
        
        mQuickResponse.setOnPreferenceClickListener(new OnPreferenceClickListener() {
			
			@Override
			public boolean onPreferenceClick(Preference preference) {
				FragmentTransaction ft = fm.beginTransaction();
				ft.add(android.R.id.content, new QuickResponseSettings(),KEY_PREFRENCE_QUICK_RESPONSE);
				ft.commit();
				mActionBar.setTitle(R.string.quick_response_settings_title);
                titleTx.setText(R.string.quick_response_settings_title);
				return true;
			}
		});

        migrateOldPreferences(sharedPreferences);

        updateChildPreferences();
        
        mController = CalendarController.getInstance(this);
        
        fm = getFragmentManager();
    }
    
    @Override
    protected void onStart() {
    	super.onStart();
    	getPreferenceScreen().getSharedPreferences()
        .registerOnSharedPreferenceChangeListener(this);
    	setPreferenceListeners(this);
    }
    
    @Override
    public void onStop() {
        getPreferenceScreen().getSharedPreferences()
                .unregisterOnSharedPreferenceChangeListener(this);
        setPreferenceListeners(null);
        super.onStop();
    }
    
    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals(KEY_ALERTS)) {
            updateChildPreferences();
            ///M: check whether provider is available first.
            boolean canUseProvider = Utils.canUseProviderByUri(getContentResolver(),
                                           CalendarContract.CalendarAlerts.CONTENT_URI);
            if (canUseProvider) {
                    Intent intent = new Intent();
                    intent.setClass(PrizeSettingsActivity.this, AlertReceiver.class);
                    if (mAlert.isChecked()) {
                        intent.setAction(AlertReceiver.ACTION_DISMISS_OLD_REMINDERS);
                    } else {
                        intent.setAction(AlertReceiver.EVENT_REMINDER_APP_ACTION);
                    }
                    PrizeSettingsActivity.this.sendBroadcast(intent);
            } else {
                Toast.makeText(PrizeSettingsActivity.this, R.string.operation_failed, Toast.LENGTH_LONG).show();
            }
        }
        BackupManager.dataChanged(PrizeSettingsActivity.this.getPackageName());
    }

    /**
     * Handles time zone preference changes
     */
    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        String tz;
        if (preference == mUseHomeTZ) {
            if ((Boolean)newValue) {
                tz = mTimeZoneId;
            } else {
                tz = CalendarCache.TIMEZONE_TYPE_AUTO;
            }
            Utils.setTimeZone(this, tz);
            return true;
        } else if (preference == mHideDeclined) {
            mHideDeclined.setChecked((Boolean) newValue);
            Intent intent = new Intent(Utils.getWidgetScheduledUpdateAction(this));
            intent.setDataAndType(CalendarContract.CONTENT_URI, Utils.APPWIDGET_DATA_TYPE);
            sendBroadcast(intent);
            return true;
        } else if (preference == mWeekStart) {
            mWeekStart.setValue((String) newValue);
            mWeekStart.setSummary(mWeekStart.getEntry());
        } else if (preference == mDefaultReminder) {
            mDefaultReminder.setValue((String) newValue);
            mDefaultReminder.setSummary(mDefaultReminder.getEntry());
        } else if (preference == mRingtone) {
            if (newValue instanceof String) {
                Utils.setRingTonePreference(this, (String) newValue);
                String ringtone = getRingtoneTitleFromUri(this, (String) newValue);
                mRingtone.setSummary(ringtone == null ? "" : ringtone);
            }
            return true;
        } else if (preference == mVibrate) {
            mVibrate.setChecked((Boolean) newValue);
            return true;
        } else {
            return true;
        }
        return false;
    }

    public String getRingtoneTitleFromUri(Context context, String uri) {
        if (TextUtils.isEmpty(uri)) {
            return null;
        }

        Ringtone ring = RingtoneManager.getRingtone(this, Uri.parse(uri));
        if (ring != null) {
            return ring.getTitle(context);
        }
        return null;
    }

    /**
     * If necessary, upgrades previous versions of preferences to the current
     * set of keys and values.
     * @param prefs the preferences to upgrade
     */
    private void migrateOldPreferences(SharedPreferences prefs) {
        // If needed, migrate vibration setting from a previous version

        mVibrate.setChecked(Utils.getDefaultVibrate(this, prefs));

        // If needed, migrate the old alerts type setting
        if (!prefs.contains(KEY_ALERTS) && prefs.contains(KEY_ALERTS_TYPE)) {
            String type = prefs.getString(KEY_ALERTS_TYPE, ALERT_TYPE_STATUS_BAR);
            if (type.equals(ALERT_TYPE_OFF)) {
                mAlert.setChecked(false);
                mPopup.setChecked(false);
                mPopup.setEnabled(false);
            } else if (type.equals(ALERT_TYPE_STATUS_BAR)) {
                mAlert.setChecked(true);
                mPopup.setChecked(false);
                mPopup.setEnabled(true);
            } else if (type.equals(ALERT_TYPE_ALERTS)) {
                mAlert.setChecked(true);
                mPopup.setChecked(true);
                mPopup.setEnabled(true);
            }
            // clear out the old setting
            prefs.edit().remove(KEY_ALERTS_TYPE).commit();
        }
    }

    /**
     * Keeps the dependent settings in sync with the parent preference, so for
     * example, when notifications are turned off, we disable the preferences
     * for configuring the exact notification behavior.
     */
    private void updateChildPreferences() {
        if (mAlert.isChecked()) {
            mVibrate.setEnabled(true);
            mRingtone.setEnabled(true);
            mPopup.setEnabled(true);
        } else {
            mVibrate.setEnabled(false);
            mRingtone.setEnabled(false);
            mPopup.setEnabled(false);
        }
    }


    @Override
    public boolean onPreferenceTreeClick(
            PreferenceScreen preferenceScreen, Preference preference) {
        final String key = preference.getKey();
        if (KEY_CLEAR_SEARCH_HISTORY.equals(key)) {
            SearchRecentSuggestions suggestions = new SearchRecentSuggestions(this,
                    Utils.getSearchAuthority(this),
                    CalendarRecentSuggestionsProvider.MODE);
            suggestions.clearHistory();
            ///M:here use MTKToast to avoid show very long time when click many times
            MTKToast.toast(this, R.string.search_history_cleared);
            ///@}
            return true;
        } else {
            return super.onPreferenceTreeClick(preferenceScreen, preference);
        }
    }

    @Override
    public void onTimeZoneSet(TimeZoneInfo tzi) {
        if (mTzPickerUtils == null) {
            mTzPickerUtils = new TimeZonePickerUtils(this);
        }

        final CharSequence timezoneName = mTzPickerUtils.getGmtDisplayName(
                this, tzi.mTzId, System.currentTimeMillis(), false);
        mHomeTZ.setSummary(timezoneName);
        Utils.setTimeZone(this, tzi.mTzId);
    }
    
    /**
     * Sets up all the preference change listeners to use the specified
     * listener.
     */
    private void setPreferenceListeners(OnPreferenceChangeListener listener) {
        mUseHomeTZ.setOnPreferenceChangeListener(listener);
        mHomeTZ.setOnPreferenceChangeListener(listener);
        mWeekStart.setOnPreferenceChangeListener(listener);
        mDefaultReminder.setOnPreferenceChangeListener(listener);
        mRingtone.setOnPreferenceChangeListener(listener);
        mHideDeclined.setOnPreferenceChangeListener(listener);
        mVibrate.setOnPreferenceChangeListener(listener);
    }
    
    private void showTimezoneDialog() {

        Bundle b = new Bundle();
        b.putLong(TimeZonePickerDialog.BUNDLE_START_TIME_MILLIS, System.currentTimeMillis());
        b.putString(TimeZonePickerDialog.BUNDLE_TIME_ZONE, Utils.getTimeZone(this, null));

        FragmentManager fm = getFragmentManager();
        TimeZonePickerDialog tzpd = (TimeZonePickerDialog) fm
                .findFragmentByTag(FRAG_TAG_TIME_ZONE_PICKER);
        if (tzpd != null) {
            tzpd.dismiss();
        }
        tzpd = new TimeZonePickerDialog();
        tzpd.setArguments(b);
        tzpd.setOnTimeZoneSetListener(this);
        tzpd.show(fm, FRAG_TAG_TIME_ZONE_PICKER);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        } else if (item.getItemId() == R.id.action_add_account) {
            Intent nextIntent = new Intent(Settings.ACTION_ADD_ACCOUNT);
            final String[] array = { "com.android.calendar" };
            nextIntent.putExtra(Settings.EXTRA_AUTHORITIES, array);
            nextIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(nextIntent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
    	/*getMenuInflater().inflate(R.menu.settings_title_bar, menu);*/
//        getActionBar()
//                .setDisplayOptions(ActionBar.DISPLAY_HOME_AS_UP, ActionBar.DISPLAY_HOME_AS_UP);
//        
        ActionBar bar = getActionBar();
        if (bar != null) {
            bar.setDisplayOptions( ActionBar.DISPLAY_HOME_AS_UP | ActionBar.DISPLAY_SHOW_TITLE);
            bar.setTitle(getString(R.string.preferences_title));
        }
        return true;
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    protected boolean isValidFragment(String fragmentName) {
        // This activity is not exported so we can just approve everything
        return true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ///M: To remove its CalendarController instance if exists @{
        CalendarController.removeInstance(this);
        ///@}
    }
    
    @Override
    public void onBackPressed() {
    	if (fm != null && fm.findFragmentByTag(KEY_PREFRENCE_QUICK_RESPONSE) != null) {
			FragmentTransaction ft = fm.beginTransaction();
			Fragment fragment = fm.findFragmentByTag(KEY_PREFRENCE_QUICK_RESPONSE);
			ft.remove(fragment);
			ft.commit();
			mActionBar.setTitle(R.string.preferences_title);
            titleTx.setText(R.string.preferences_title);
			return;
		}
    	super.onBackPressed();
    }

    private void initStatusBar() {
        Window window = getWindow();
		/*window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS
				| WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
		window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
				//| View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
				| View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
		window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);*/
        /*if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.N){
            window.setStatusBarColor(getResources().getColor(R.color.prize_bottom_button_bg_color));      // prize modify zhaojian 8.0 2017803
        }else {
            window.setStatusBarColor(Color.TRANSPARENT);
        }*/
        window.setStatusBarColor(getResources().getColor(R.color.prize_bottom_button_bg_color));

        WindowManager.LayoutParams lp = getWindow().getAttributes();
        try {
            Class statusBarManagerClazz = Class.forName("android.app.StatusBarManager");
            Field grayField = statusBarManagerClazz.getDeclaredField("STATUS_BAR_INVERSE_GRAY");
            Object gray = grayField.get(statusBarManagerClazz);
            Class windowManagerLpClazz = lp.getClass();
            Field statusBarInverseField = windowManagerLpClazz.getDeclaredField("statusBarInverse");
            statusBarInverseField.set(lp,gray);
            getWindow().setAttributes(lp);
        } catch (Exception e) {
        }
    }

    public FrameLayout getActionBarContainer() {
        Window window = getWindow();
        View v = window.getDecorView();
        int resId = getResources().getIdentifier("action_bar_container", "id", "android");
        return (FrameLayout)v.findViewById(resId);
    }
}
