/*
 * Copyright (C) 2008 The Android Open Source Project
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

package com.android.settings;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.DatePickerDialog.OnDateSetListener;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.app.TimePickerDialog.OnTimeSetListener;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.provider.Settings.SettingNotFoundException;
import android.support.v14.preference.SwitchPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.Preference.OnPreferenceChangeListener;
import android.text.format.DateFormat;
import android.util.Log;
import android.widget.DatePicker;
import android.widget.TimePicker;
import com.android.internal.logging.MetricsProto.MetricsEvent;
import com.android.settings.dashboard.SummaryLoader;
import com.android.settingslib.RestrictedLockUtils;
import com.android.settings.widget.wheelView.PrizeTimeDialog;
import com.android.settingslib.RestrictedSwitchPreference;
import com.android.settingslib.datetime.ZoneGetter;

import java.util.Calendar;
import java.util.Date;

import com.mediatek.settingslib.RestrictedListPreference;
import static com.android.settingslib.RestrictedLockUtils.EnforcedAdmin;

public class DateTimeSettings extends SettingsPreferenceFragment
        implements OnTimeSetListener, OnDateSetListener, OnPreferenceChangeListener ,
        DialogInterface.OnClickListener, OnCancelListener {
 
    private static final String TAG = "DateTimeSettings";

    private static final String HOURS_12 = "12";
    private static final String HOURS_24 = "24";

    // Used for showing the current date format, which looks like "12/31/2010", "2010/12/13", etc.
    // The date value is dummy (independent of actual date).
    private Calendar mDummyDate;

    /// M: modify as MTK add GPS time Sync feature
    private static final String KEY_AUTO_TIME = "auto_time_list";
    private static final String KEY_AUTO_TIME_ZONE = "auto_zone";

    private static final int DIALOG_DATEPICKER = 0;
    private static final int DIALOG_TIMEPICKER = 1;

    // have we been launched from the setup wizard?
    protected static final String EXTRA_IS_FIRST_RUN = "firstRun";

    // Minimum time is Nov 5, 2007, 0:00.
    private static final long MIN_DATE = 1194220800000L;

    /// M: modify as MTK add GPS time Sync feature
    /// M: private RestrictedSwitchPreference mAutoTimePref;
    private SwitchPreference mAutoTimePref;

    private Preference mTimePref;
    private Preference mTime24Pref;
    private SwitchPreference mAutoTimeZonePref;
    private Preference mTimeZone;
    private Preference mDatePref;

    /// M: add for GPS time sync feature @{
    private static final int DIALOG_GPS_CONFIRM = 2;
    private static final int AUTO_TIME_NETWORK_INDEX = 0;
    private static final int AUTO_TIME_GPS_INDEX = 1;
    private static final int AUTO_TIME_OFF_INDEX = 2;
    /// @}

    @Override
    protected int getMetricsCategory() {
        return MetricsEvent.DATE_TIME;
    }

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        addPreferencesFromResource(R.xml.date_time_prefs);

        initUI();
    }

    private void initUI() {
        boolean autoTimeEnabled = getAutoState(Settings.Global.AUTO_TIME);
        boolean autoTimeZoneEnabled = getAutoState(Settings.Global.AUTO_TIME_ZONE);

        /// M: MTK use RestrictedListPreference instead of google SwitchPerference
        mAutoTimePref = (SwitchPreference) findPreference(KEY_AUTO_TIME);
        mAutoTimePref.setOnPreferenceChangeListener(this);
        // EnforcedAdmin admin = RestrictedLockUtils.checkIfAutoTimeRequired(getActivity());
        // mAutoTimePref.setDisabledByAdmin(admin);
		if(autoTimeEnabled){
			mAutoTimePref.setChecked(autoTimeEnabled);
		}else{
			mAutoTimePref.setChecked(autoTimeEnabled);
		}
        Intent intent = getActivity().getIntent();
        boolean isFirstRun = intent.getBooleanExtra(EXTRA_IS_FIRST_RUN, false);

        mDummyDate = Calendar.getInstance();

        /// M: MTK use RestrictedListPreference instead of google SwitchPerference @{
        // If device admin requires auto time device policy manager will set
        // Settings.Global.AUTO_TIME to true. Note that this app listens to that change.
        // mAutoTimePref.setChecked(autoTimeEnabled);
        boolean autoTimeGpsEnabled = getAutoState(Settings.System.AUTO_TIME_GPS);
        int index = 0;
        if (autoTimeEnabled) {
            index = AUTO_TIME_NETWORK_INDEX;
        } else if (autoTimeGpsEnabled) {
            index = AUTO_TIME_GPS_INDEX;
        } else {
            index = AUTO_TIME_OFF_INDEX;
        }
       // mAutoTimePref.setValueIndex(index);
       // mAutoTimePref.setSummary(mAutoTimePref.getEntries()[index]);
        /// @}

        mAutoTimeZonePref = (SwitchPreference) findPreference(KEY_AUTO_TIME_ZONE);
        mAutoTimeZonePref.setOnPreferenceChangeListener(this);
        // Override auto-timezone if it's a wifi-only device or if we're still in setup wizard.
        // TODO: Remove the wifiOnly test when auto-timezone is implemented based on wifi-location.
        if (Utils.isWifiOnly(getActivity()) || isFirstRun) {
            getPreferenceScreen().removePreference(mAutoTimeZonePref);
            autoTimeZoneEnabled = false;
		}
        mAutoTimeZonePref.setChecked(autoTimeZoneEnabled);

        mTimePref = findPreference("time");
        mTime24Pref = findPreference("24 hour");
        mTimeZone = findPreference("timezone");
        mDatePref = findPreference("date");
        if (isFirstRun) {
            getPreferenceScreen().removePreference(mTime24Pref);
        }

        /// M: modify as MTK add GPS time Sync feature
        boolean autoEnabled = autoTimeEnabled || autoTimeGpsEnabled;
        mTimePref.setEnabled(!autoEnabled);
        mDatePref.setEnabled(!autoEnabled);
        mTimeZone.setEnabled(!autoTimeZoneEnabled);
    }

    @Override
    public void onResume() {
        super.onResume();

        ((SwitchPreference)mTime24Pref).setChecked(is24Hour());

        // Register for time ticks and other reasons for time change
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_TIME_TICK);
        filter.addAction(Intent.ACTION_TIME_CHANGED);
        filter.addAction(Intent.ACTION_TIMEZONE_CHANGED);
        getActivity().registerReceiver(mIntentReceiver, filter, null, null);

        updateTimeAndDateDisplay(getActivity());
    }

    @Override
    public void onPause() {
        super.onPause();
        getActivity().unregisterReceiver(mIntentReceiver);
    }

    public void updateTimeAndDateDisplay(Context context) {
        final Calendar now = Calendar.getInstance();
        mDummyDate.setTimeZone(now.getTimeZone());
        // We use December 31st because it's unambiguous when demonstrating the date format.
        // We use 13:00 so we can demonstrate the 12/24 hour options.
        mDummyDate.set(now.get(Calendar.YEAR), 11, 31, 13, 0, 0);
        Date dummyDate = mDummyDate.getTime();
        mDatePref.setSummary(DateFormat.getLongDateFormat(context).format(now.getTime()));
		
        mTimePref.setSummary(DateFormat.getTimeFormat(getActivity()).format(now.getTime()));
        mTimeZone.setSummary(ZoneGetter.getTimeZoneOffsetAndName(now.getTimeZone(), now.getTime()));
        mTime24Pref.setSummary(DateFormat.getTimeFormat(getActivity()).format(dummyDate));
    }

    @Override
    public void onDateSet(DatePicker view, int year, int month, int day) {
        final Activity activity = getActivity();
        if (activity != null) {
            setDate(activity, year, month, day);
            updateTimeAndDateDisplay(activity);
        }
    }

    @Override
    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
        final Activity activity = getActivity();
        if (activity != null) {
            setTime(activity, hourOfDay, minute);
            updateTimeAndDateDisplay(activity);
        }

        // We don't need to call timeUpdated() here because the TIME_CHANGED
        // broadcast is sent by the AlarmManager as a side effect of setting the
        // SystemClock time.
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (preference.getKey().equals(KEY_AUTO_TIME)) {
            /// M: modify as MTK add GPS time Sync feature @{
            // int index = Integer.parseInt(newValue.toString());
            // boolean autoEnabled = true;
            // if (index == AUTO_TIME_NETWORK_INDEX) {
                // Settings.Global.putInt(getContentResolver(),
                        // Settings.Global.AUTO_TIME, 1);
                // Settings.Global.putInt(getContentResolver(),
                        // Settings.System.AUTO_TIME_GPS, 0);
            // } else if (index == AUTO_TIME_GPS_INDEX) {
                // showDialog(DIALOG_GPS_CONFIRM);
                // setOnCancelListener(this);
            // } else {
                // Settings.Global.putInt(getContentResolver(), Settings.Global.AUTO_TIME, 0);
                // Settings.Global.putInt(getContentResolver(), Settings.System.AUTO_TIME_GPS, 0);
                // autoEnabled = false;
            // }
           // mAutoTimePref.setSummary(mAutoTimePref.getEntries()[index]);
            /// @}
			boolean autoEnabled = (boolean)newValue;
			Settings.Global.putInt(getContentResolver(),Settings.Global.AUTO_TIME, autoEnabled? 1:0);
            mTimePref.setEnabled(!autoEnabled);
            mDatePref.setEnabled(!autoEnabled);
        } else if (preference.getKey().equals(KEY_AUTO_TIME_ZONE)) {
            boolean autoZoneEnabled = (Boolean) newValue;
            Settings.Global.putInt(
                    getContentResolver(), Settings.Global.AUTO_TIME_ZONE, autoZoneEnabled ? 1 : 0);
            mTimeZone.setEnabled(!autoZoneEnabled);
        }
        return true;
    }

    @Override
    public Dialog onCreateDialog(int id) {
        final Calendar calendar = Calendar.getInstance();
        switch (id) {
        case DIALOG_DATEPICKER:
            DatePickerDialog d = new DatePickerDialog(
                    getActivity(),
                    this,
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH));
            configureDatePicker(d.getDatePicker());
            return d;
        case DIALOG_TIMEPICKER:
             /* prize-modify-by-lijimeng-for bugid 51870-20180319-start*/
//            return new TimePickerDialog(
//                    getActivity(),
//                    this,
//                    calendar.get(Calendar.HOUR_OF_DAY),
//                    calendar.get(Calendar.MINUTE),
//                    DateFormat.is24HourFormat(getActivity()));
        /// M: modify as MTK add GPS time Sync feature @{
             /* prize-modify-by-lijimeng-for bugid 51870-20180319-end*/
        case DIALOG_GPS_CONFIRM:
            int msg;
            if (Settings.Secure.isLocationProviderEnabled(getContentResolver(),
                    LocationManager.GPS_PROVIDER)) {
                msg = R.string.gps_time_sync_attention_gps_on;
            } else {
                msg = R.string.gps_time_sync_attention_gps_off;
            }
            return new AlertDialog.Builder(getActivity()).setMessage(
                    getActivity().getResources().getString(msg)).setTitle(
                    R.string.proxy_error).setIcon(
                    android.R.drawable.ic_dialog_alert).setPositiveButton(
                    android.R.string.yes, this).setNegativeButton(
                    android.R.string.no, this).create();
        /// @}
        default:
            throw new IllegalArgumentException();
        }
    }

    static void configureDatePicker(DatePicker datePicker) {
        // The system clock can't represent dates outside this range.
        Calendar t = Calendar.getInstance();
        t.clear();
        t.set(1970, Calendar.JANUARY, 1);
        datePicker.setMinDate(t.getTimeInMillis());
        t.clear();
        t.set(2037, Calendar.DECEMBER, 31);
        datePicker.setMaxDate(t.getTimeInMillis());
    }

    /*
    @Override
    public void onPrepareDialog(int id, Dialog d) {
        switch (id) {
        case DIALOG_DATEPICKER: {
            DatePickerDialog datePicker = (DatePickerDialog)d;
            final Calendar calendar = Calendar.getInstance();
            datePicker.updateDate(
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH));
            break;
        }
        case DIALOG_TIMEPICKER: {
            TimePickerDialog timePicker = (TimePickerDialog)d;
            final Calendar calendar = Calendar.getInstance();
            timePicker.updateTime(
                    calendar.get(Calendar.HOUR_OF_DAY),
                    calendar.get(Calendar.MINUTE));
            break;
        }
        default:
            break;
        }
    }
    */
    @Override
    public boolean onPreferenceTreeClick(Preference preference) {
        if (preference == mDatePref) {
            showDialog(DIALOG_DATEPICKER);
        } else if (preference == mTimePref) {
            // The 24-hour mode may have changed, so recreate the dialog
            /* prize-modify-by-lijimeng-for bugid 51870-20180319-start*/
//            removeDialog(DIALOG_TIMEPICKER);
//            showDialog(DIALOG_TIMEPICKER);
            PrizeTimeDialog prizeTimeDialog = new PrizeTimeDialog(getPrefContext());
            prizeTimeDialog.show();
        } else if (preference == mTime24Pref) {
            final boolean is24Hour = ((SwitchPreference)mTime24Pref).isChecked();
            set24Hour(is24Hour);
            updateTimeAndDateDisplay(getActivity());
            timeUpdated(is24Hour);
        }
        return super.onPreferenceTreeClick(preference);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode,
            Intent data) {
        updateTimeAndDateDisplay(getActivity());
    }

    private void timeUpdated(boolean is24Hour) {
        Intent timeChanged = new Intent(Intent.ACTION_TIME_CHANGED);
        timeChanged.putExtra(Intent.EXTRA_TIME_PREF_24_HOUR_FORMAT, is24Hour);
        getActivity().sendBroadcast(timeChanged);
    }

    /*  Get & Set values from the system settings  */

    private boolean is24Hour() {
        return DateFormat.is24HourFormat(getActivity());
    }

    private void set24Hour(boolean is24Hour) {
        Settings.System.putString(getContentResolver(),
                Settings.System.TIME_12_24,
                is24Hour? HOURS_24 : HOURS_12);
    }

    private boolean getAutoState(String name) {
        try {
            return Settings.Global.getInt(getContentResolver(), name) > 0;
        } catch (SettingNotFoundException snfe) {
            return false;
        }
    }

    /* package */ static void setDate(Context context, int year, int month, int day) {
        Calendar c = Calendar.getInstance();

        c.set(Calendar.YEAR, year);
        c.set(Calendar.MONTH, month);
        c.set(Calendar.DAY_OF_MONTH, day);
		/* prize-modify-by-lijimeng-for bugid 44711-20171206-start*/
        // long when = Math.max(c.getTimeInMillis(), MIN_DATE);
        long when = c.getTimeInMillis();
		/* prize-modify-by-lijimeng-for bugid 44711-20171206-end*/

        if (when / 1000 < Integer.MAX_VALUE) {
            ((AlarmManager) context.getSystemService(Context.ALARM_SERVICE)).setTime(when);
        }
    }

    /* package */ static void setTime(Context context, int hourOfDay, int minute) {
        Calendar c = Calendar.getInstance();

        c.set(Calendar.HOUR_OF_DAY, hourOfDay);
        c.set(Calendar.MINUTE, minute);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);
        /* prize-modify-by-lijimeng-for bugid 44711-20171206-start*/
        // long when = Math.max(c.getTimeInMillis(), MIN_DATE);
        long when = c.getTimeInMillis();
		/* prize-modify-by-lijimeng-for bugid 44711-20171206-end*/

        if (when / 1000 < Integer.MAX_VALUE) {
            ((AlarmManager) context.getSystemService(Context.ALARM_SERVICE)).setTime(when);
        }
    }

    private BroadcastReceiver mIntentReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final Activity activity = getActivity();
            if (activity != null) {
                updateTimeAndDateDisplay(activity);
            }
        }
    };

    private static class SummaryProvider implements SummaryLoader.SummaryProvider {

        private final Context mContext;
        private final SummaryLoader mSummaryLoader;

        public SummaryProvider(Context context, SummaryLoader summaryLoader) {
            mContext = context;
            mSummaryLoader = summaryLoader;
        }

        @Override
        public void setListening(boolean listening) {
            if (listening) {
                final Calendar now = Calendar.getInstance();
                mSummaryLoader.setSummary(this, ZoneGetter.getTimeZoneOffsetAndName(
                        now.getTimeZone(), now.getTime()));
            }
        }
    }

    public static final SummaryLoader.SummaryProviderFactory SUMMARY_PROVIDER_FACTORY
            = new SummaryLoader.SummaryProviderFactory() {
        @Override
        public SummaryLoader.SummaryProvider createSummaryProvider(Activity activity,
                                                                   SummaryLoader summaryLoader) {
            return new SummaryProvider(activity, summaryLoader);
        }
    };

    /// M: modify as MTK add GPS time Sync feature @{
    @Override
    public void onClick(DialogInterface dialog, int which) {
        if (which == DialogInterface.BUTTON_POSITIVE) {
            Log.d(TAG, "Enable GPS time sync");
            boolean gpsEnabled = Settings.Secure.isLocationProviderEnabled(
                    getContentResolver(), LocationManager.GPS_PROVIDER);
            if (!gpsEnabled) {
                Settings.Secure.setLocationProviderEnabled(
                        getContentResolver(), LocationManager.GPS_PROVIDER,
                        true);
            }
            Settings.Global.putInt(getContentResolver(),
                    Settings.Global.AUTO_TIME, 0);
            Settings.Global.putInt(getContentResolver(),
                    Settings.System.AUTO_TIME_GPS, 1);
            // mAutoTimePref.setValueIndex(AUTO_TIME_GPS_INDEX);
            // mAutoTimePref.setSummary(mAutoTimePref.getEntries()[AUTO_TIME_GPS_INDEX]);
        } else if (which == DialogInterface.BUTTON_NEGATIVE) {
            Log.d(TAG, "DialogInterface.BUTTON_NEGATIVE");
            reSetAutoTimePref();
        }
    }

    private void reSetAutoTimePref() {
        Log.d(TAG, "reset AutoTimePref as cancel the selection");
        boolean autoTimeEnabled = getAutoState(Settings.Global.AUTO_TIME);
        boolean autoTimeGpsEnabled = getAutoState(Settings.System.AUTO_TIME_GPS);
        int index = 0;
        if (autoTimeEnabled) {
            index = AUTO_TIME_NETWORK_INDEX;
        } else if (autoTimeGpsEnabled) {
            index = AUTO_TIME_GPS_INDEX;
        } else {
            index = AUTO_TIME_OFF_INDEX;
        }
        // mAutoTimePref.setValueIndex(index);
        // mAutoTimePref.setSummary(mAutoTimePref.getEntries()[index]);
        boolean autoEnabled = autoTimeEnabled || autoTimeGpsEnabled;
        Log.d(TAG, "reset AutoTimePref as cancel the selection autoEnabled: " + autoEnabled);
        mTimePref.setEnabled(!autoEnabled);
        mDatePref.setEnabled(!autoEnabled);
    }

    @Override
    public void onCancel(DialogInterface arg0) {
        Log.d(TAG, "onCancel Dialog");
        reSetAutoTimePref();
    }
    /// @}
}
